# 002 · Base de la API v1: red, errores, auth, perfil, chat y dispositivos

- **Estado:** In progress. Aprobada por el owner el 2026-10-07 («continúa con las siguientes fases»). Falta la verificación E2E contra el backend local (T-016).
- **Reglas:** AUTH-1…4, APP-RN-07, APP-RN-11, APP-RN-13
- **ADRs:** ADR-0005
- **Backend:** `BACKEND.md` §5, §6, §8.1, §8.6, §8.7, §11 y §12; BE-007
- **Repos afectados:** Squadfy_App

## Problema / objetivo
Las rutas antiguas ya no existen en el backend (`migracion-v1.md`), así que **hoy toda la app da 404**, incluidos el login y el chat. Además, los errores del backend llegan con un `code` que la app descarta, y varios DTOs exigen campos que el backend ya no envía.

Esta spec deja la **infraestructura común** lista para v1:
- la base URL;
- el parseo de errores;
- los códigos 201/204;
- el JSON tolerante;
- y migra lo que no es de club: auth, perfil, chat y dispositivos.

Todas las demás features dependen de esta.

## Fuera de alcance
- Los endpoints de clubes y partidos (specs 003–008).
- El ciclo de vida de las push del partido (009). Aquí solo se cambian las rutas de `/devices`.

## Historias de usuario
- **US-002-01** Como usuario quiero registrarme, verificar el email, iniciar sesión, recuperar y cambiar la contraseña contra el backend real.
- **US-002-02** Como usuario quiero que la app me diga **por qué** falla una acción («Email no verificado», «Demasiados intentos», «Código no válido»), no un genérico «conflict».
- **US-002-03** Como usuario quiero seguir usando el chat y mi foto de perfil.

## Criterios de aceptación
**Red y errores**
- **AC-002-01** El `BASE_URL_HTTP` por defecto termina en `/api/v1` y ninguna ruta del cliente contiene `/api/`. *Verificación:* test o grep sobre las constantes de rutas.
- **AC-002-02** `Json` usa `ignoreUnknownKeys = true` y `coerceInputValues = true`. Un JSON con campos extra o con un enum desconocido no produce `SERIALIZATION` (APP-RN-13).
- **AC-002-03** Ante un error HTTP, la app parsea `{code, message}` o `{code: "VALIDATION_ERROR", errors[]}` y lo convierte en `RemoteError(status: DataError.Remote, code: String?, messages: List<String>)`. Un body vacío (401 sin token) o no JSON produce `code = null` sin lanzar excepciones.
- **AC-002-04** Las respuestas 201 se deserializan como las 200, y las 204 se tratan como `Result.Success(Unit)`.
- **AC-002-05** Hay un catálogo `BackendErrorCode` (enum + `UNKNOWN`) con todos los códigos de `BACKEND.md` §11. `USER_EXITS` y `USER_EXISTS` se tratan como equivalentes (BE-GAP-8).
- **AC-002-06** `RemoteError.toUiText()` devuelve un texto específico para cada `code` conocido y, si no lo hay, el genérico de su estado HTTP (APP-RN-07).

**Auth**
- **AC-002-07** Los flujos de registro, login, logout, reenvío de verificación, contraseña olvidada, reset y cambio de contraseña funcionan contra el backend local (`docker compose` + `bootRun` en el perfil dev).
- **AC-002-08** Login con 403 `EMAIL_NOT_VERIFIED`: se muestra «Verifica tu email» con un botón «Reenviar» (`POST /auth/resend-verification`). Login con 401 `INVALID_CREDENTIALS`: «Email o contraseña incorrectos».
- **AC-002-09** Ante un 429 `RATE_LIMIT_EXCEEDED` se muestra «Demasiados intentos, prueba más tarde» y la sesión **no** se cierra.
- **AC-002-10** Después de cada refresh se guarda el **nuevo** refresh token, porque el backend lo rota. Si el refresh responde 401 `INVALID_TOKEN`, la sesión se cierra (APP-RN-11).
- **AC-002-11** Tras el login, la app compone `UserModel` con el `user` de la respuesta de login (equivale a `GET /me`: email, username y verificado) más la foto de `GET /users/{id}`. Si la foto falla, el login no falla. `UserSerializableDTO` ya no exige `profilePictureUrl`, y la rotación de tokens conserva la foto guardada.
- **AC-002-12** El deep link `squadfy://reset-password?token=…` abre la pantalla de nueva contraseña en Android y en iOS. Se eliminan el filtro `https://squadfy.com/api/auth/reset-password` y el deep link de verificación, porque el enlace de verificación se abre en el navegador (BACKEND §6.1).
- **AC-002-13** El registro valida en local **al menos** lo que exige AUTH-3: usuario de 3–20 caracteres. La regla local de contraseña (≥ 9, con dígito y mayúscula) es más estricta que la del backend, y se mantiene porque todo lo que acepta el cliente también lo acepta el servidor. El cliente nunca puede ser más laxo que el backend.

**Perfil, chat y dispositivos**
- **AC-002-14** Búsqueda de usuario: `GET /users?query=`. Mi perfil público: `GET /users/{id}`. Foto: `upload-url` → `PUT` de los bytes con las `headers` recibidas → `PUT /me/profile-picture {publicUrl}`. Borrar foto: `DELETE /me/profile-picture`.
- **AC-002-15** Las rutas de chat pasan a `/chats`, `/chats/{id}`, `/chats/{id}/messages`, `/chats/{id}/participants` y `/chats/{id}/participants/me`, y la de mensajes a `/messages/{id}`. El WebSocket no cambia. `ChatDto.creator` es opcional en el cliente.
- **AC-002-16** El dispositivo se registra con `POST /devices {token, platform}` y se da de baja con `DELETE /devices/{token}`.
- **AC-002-17** Los 151 tests existentes siguen en verde con las rutas actualizadas, y se añaden tests de `RemoteError` y de los mappers con **fixtures JSON reales** de `BACKEND.md` (constitución V.4).
- **AC-002-18** Si el backend tiene la verificación de email desactivada (backend spec 009, `EMAIL_VERIFICATION_ENABLED=false`), el registro devuelve `hasVerifiedEmail = true`. En ese caso la pantalla de éxito dice que ya se puede iniciar sesión y no ofrece reenviar el email.

## Casos límite
| Situación | Comportamiento |
|---|---|
| 401 a mitad de una petición | El plugin `Auth` refresca una vez y reintenta. Si el refresh da 401, se cierra la sesión |
| 401 en `/auth/login` | No se intenta refresh (las rutas `auth/` están excluidas) |
| 429 en el refresh | **No** se cierra la sesión: se reintenta en la siguiente petición (mitiga BE-GAP-2) |
| Error 500 con body HTML | `code = null` y el texto genérico de 5xx |

## Preguntas abiertas
- ✅ **D-2** (resuelta el 2026-10-08, ADR-0008: se elimina) ¿Se mantiene la cabecera `x-api-key`? Verificado el 2026-10-07: el backend v1 **no la lee** (no aparece en ningún controlador ni filtro). Se sigue enviando, sin efecto, hasta que se decida; quitarla implica eliminar `API_KEY` de la convention, del CI y de `local.properties`.
