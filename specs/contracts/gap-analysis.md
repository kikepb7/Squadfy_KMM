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

## 2. Funciones de la app que mantiene el producto (D-1): ya en el backend (BE-008)
El backend las implementó el 2026-10-07 (spec BE-008, commit `e375f70` en `master`). La app las conecta tras sus flags:

| Función | Flag | Spec de la app |
|---|---|---|
| Invitados (máximo 2 por miembro; los miembros tienen prioridad) | `MATCH_GUESTS` | 005, 006 |
| Excepciones del calendario (`CANCELLED` / `RESCHEDULED`) | `SCHEDULE_EXCEPTIONS` | 004 |
| Ausencias de jugadores | `MEMBER_ABSENCES` | 014 |
| Hora de cierre y hora de sorteo configurables | `CUSTOM_DRAW_TIME` | 004, 005 |
| Marcador manual oficial | `MANUAL_SCORE` | 007 |
| Valoración automática por partido (`ratingChanges`) | — | 008 |

Se retiran: la valoración manual 1–99, el `PATCH` de miembro por un admin, el inicio de temporada y el índice de rendimiento. La foto por club queda en el backlog del backend.

## 3. Lo que falta en el backend para ir a producción (propuestas de specs BE)
| # | Brecha | Impacto | Prioridad |
|---|---|---|---|
| ~~BE-GAP-1~~ | Resuelto en el backend (spec 010, ya en `master`): `DELETE /me {password}` y la página web `/account/delete`. La app lo consume tras `ACCOUNT_DELETION` (spec 011). Falta desplegarlo. | Apple 5.1.1(v) y Google Play | ✅ (pendiente de despliegue) |
| ~~BE-GAP-2~~ | Resuelto en el backend (spec 010): límites por cuenta, con la IP como red de seguridad, y 429 con `Retry-After`. Flag `rate-limit` activo en `prod` (spec 011). | — | ✅ |
| BE-GAP-3 | Despliegue en curso en el backend (spec 013): Render con staging (`release`) y producción (`master`), CD con CI en verde, CORS y health checks. La app ya separa los candidatos (staging) de los tags (producción) (spec 015 T-008). Faltan las cuentas y los secretos (manual). | Sin backend público no hay release | 🔴 Bloqueante (en curso) |
| ~~BE-GAP-4~~ | Resuelto (spec 011 del backend): página pública `/account/verify-email`. | — | ✅ |
| BE-GAP-5 | Hay que servir `/.well-known/assetlinks.json` y `apple-app-site-association` si se quieren App Links o Universal Links (por ejemplo, para el enlace de verificación). | Opcional en el MVP (se puede usar `squadfy://`) | 🟢 Baja |
| ~~BE-GAP-6~~ | Resuelto (spec 011 del backend): solo se dan de baja los dispositivos propios (404 con los ajenos). | — | ✅ |
| ~~BE-GAP-7~~ | Resuelto: `master` incluye la documentación, los arreglos de chat, V7 y BE-008. | — | ✅ |
| ~~BE-GAP-8~~ | Resuelto (spec 012 del backend): `409 USER_EXISTS`. La app acepta los dos códigos. | — | ✅ |
| ~~BE-GAP-9~~ | Resuelto (spec 012 del backend): `from`/`to` en las estadísticas, que la app ya consume (spec 015). | — | ✅ |
| BE-GAP-10 | Contenido generado por usuarios en el chat: denunciar mensajes y bloquear usuarios (Apple 1.2). La app oculta el chat en iOS en PRO mientras no exista (D-13). | Necesario para tener chat en iOS | 🟡 Media |
