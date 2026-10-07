# Análisis de brechas (2026-10-07)

Se comparan dos estados:
- **Backend:** `Squadfy_Backend@backend-documentation`, commit `824f9d5`. Es `master` más los arreglos del WebSocket de chat (`f47e2d8`, `f309108`) y `docs/BACKEND.md`, **todavía sin mergear a `master`**.
- **App:** `Squadfy_App@refactor-feature`, commit `4f54037` más el working tree.

## 1. Lo que la app tiene que hacer (el backend ya lo ofrece)

| Capacidad | Backend v1 | App hoy | Spec de la app |
|---|---|---|---|
| Rutas `/api/v1`, errores `{code}`, 201/204 | ✅ | ❌ rutas antiguas: **todas** las llamadas dan 404 | 002 |
| `/me`, `/users`, foto de perfil, `/devices` | ✅ | ❌ rutas `/participants` y `/notification` | 002 |
| Chat REST v1 | ✅ | ❌ rutas `/chat` | 002 |
| Clubes: CRUD, código, logo | ✅ | 🟡 sin editar club ni regenerar o compartir el código | 003 |
| Roles, expulsar, vetar, transferir, salir | ✅ | ❌ las filas de Ajustes no hacen nada; el rol es `String` | 003 |
| Mis clubes en la pestaña Clubs, navegar al club tras crearlo | n/a | ❌ | 003 |
| Horario (formato, zona, duración, activo) | ✅ | 🟡 otro modelo (horas de inicio y fin, `drawTime`, temporada, excepciones) | 004 |
| Convocatoria vigente, apuntarse, lista de espera | ✅ | 🟡 otro modelo (signups, invitados), sin lista de espera ni ventana | 005 |
| Equipos publicados, rectificar, equilibrio | ✅ | 🟡 solo «Sortear» AUTO; sin equilibrio ni ajuste manual | 006 |
| Eventos, minutos, cerrar, reabrir, cancelar, partido extra | ✅ | 🟡 contadores que solo suman y un diálogo de marcador; «partido de prueba» | 007 |
| Ratings y clasificación de estadísticas | ✅ | ❌ índice calculado en el cliente | 008 |
| Push del ciclo de partido, silenciar club | ✅ | ❌ solo chat; sin `onMessageReceived` | 009 |
| Inicio real | n/a (`/clubs` + `/announcements/current`) | ❌ partidos y noticias son mocks | 010 |

## 2. Funciones que el backend aún no tiene (D-1 resuelta el 2026-10-07)
El owner del producto decide **mantener** estas funciones. Se implementan en el backend (la conversación o rama del backend está en curso) y en la app quedan **ocultas tras un feature flag** (spec 013) hasta que el backend las publique:

| Función | Regla propuesta (pendiente de la spec del backend) | Flag de la app | Spec del backend |
|---|---|---|---|
| **Invitados** | Un miembro apuntado añade invitados a mano, **bajo su responsabilidad** («invitado de X») | `MATCH_GUESTS` | BE-pendiente (invitados) |
| **Excepciones de calendario** | Fechas en las que no hay partido | `SCHEDULE_EXCEPTIONS` | BE-pendiente (excepciones) |
| **`drawTime`** | Hora configurable de cierre y sorteo (hoy fija a las 22:00 del día anterior) | `CUSTOM_DRAW_TIME` | BE-pendiente (`drawTime`) |
| **Marcador manual** | Introducir el resultado sin registrar todos los goles | `MANUAL_SCORE` | BE-pendiente (marcador manual) |

Se **retiran** de verdad, porque no se han pedido:
- la valoración 1–99 manual (la valoración es el rating automático);
- el `PATCH` de un miembro por parte de un admin;
- la foto de miembro por club;
- el inicio de temporada;
- el índice de rendimiento del cliente.

En cuanto el backend publique cada función: se actualizan `api-v1.md` y `business-rules.md` (Parte A), se activa el flag en PRE para hacer QA y, después, en PRO.

## 3. Lo que falta en el backend para ir a producción (propuestas de specs BE)
| # | Brecha | Impacto | Prioridad |
|---|---|---|---|
| BE-GAP-1 | **Borrado de cuenta** desde la app: no hay endpoint. | **Bloquea la publicación**: tanto Apple (App Store Review Guideline 5.1.1(v)) como Google Play exigen poder eliminar la cuenta si la app permite crearla, y Play pide además un enlace web de borrado. | 🔴 Bloqueante |
| BE-GAP-2 | **Rate limit de `/auth/refresh`**: 10 por hora y por IP en `prod`, con access tokens de 15 min. Un solo usuario activo más de 2,5 h, o varios en la misma wifi, recibe 429 y se le cierra la sesión. Está reconocido en el §16 de `BACKEND.md`. | Sesiones caídas en producción | 🔴 Alta |
| BE-GAP-3 | El despliegue está sin hacer: hosting, dominio HTTPS, registro de imágenes, CD (BE-006). | Sin backend público no hay release | 🔴 Bloqueante |
| BE-GAP-4 | `GET /auth/verify` responde 200 vacío en el navegador, sin página de confirmación. | UX pobre en el primer contacto | 🟡 Media |
| BE-GAP-5 | Hay que servir `/.well-known/assetlinks.json` y `apple-app-site-association` si se quieren App Links o Universal Links (por ejemplo, para el enlace de verificación). | Opcional en el MVP (se puede usar `squadfy://`) | 🟢 Baja |
| BE-GAP-6 | `DELETE /devices/{token}` no comprueba quién es el dueño del token. | Riesgo bajo, reconocido | 🟢 Baja |
| BE-GAP-7 | La rama `backend-documentation` no está mergeada a `master` (arreglos de chat y docs). | Desalineación entre ramas | 🟡 Media |
| BE-GAP-8 | Errata `USER_EXITS`. | La app tolera `USER_EXITS` y `USER_EXISTS` | 🟢 Baja |
| BE-GAP-9 | Estadísticas sin filtro por temporada. | Post-MVP | 🟢 Baja |
