# 002 · Plan técnico

## Cumplimiento de la constitución
| Principio | Cumple | Nota |
|---|---|---|
| I. Servidor autoritativo | ✅ | Solo cambian el transporte y el mapeo de errores |
| II. Capas y MVI | ✅ | `RemoteError` en `core/domain`; parseo en `core/data` |
| III. Calidad | ✅ | Fixtures reales + tests de los helpers HTTP con `MockEngine` |
| IV. Seguridad | ✅ | Se rota el refresh token y no se registran tokens en el log |
| V. Contrato | ✅ | Fixtures copiados de `BACKEND.md` §9 |

## core/domain
- `core/domain/util/RemoteError.kt`:
  ```kotlin
  data class RemoteError(val status: DataError.Remote, val code: BackendErrorCode, val messages: List<String> = emptyList()) : Error
  enum class BackendErrorCode { VALIDATION_ERROR, INVALID_REQUEST, BAD_REQUEST, INVALID_INVITATION_CODE, INVALID_CHAT_SIZE,
      INVALID_PROFILE_PICTURE, INVALID_DEVICE_TOKEN, INVALID_CREDENTIALS, INVALID_TOKEN, FORBIDDEN, NOT_CLUB_MEMBER,
      BANNED_FROM_CLUB, EMAIL_NOT_VERIFIED, NOT_FOUND, USER_NOT_FOUND, CONFLICT, USER_EXISTS, SAME_PASSWORD,
      RATE_LIMIT_EXCEEDED, STORAGE_ERROR, UNKNOWN;
      companion object { fun from(raw: String?): BackendErrorCode = if (raw == "USER_EXITS") USER_EXISTS else entries.firstOrNull { it.name == raw } ?: UNKNOWN } }
  ```
- **Compatibilidad:** los repositorios existentes devuelven `Result<T, DataError.Remote>`. Para no reescribir todo de golpe se hace lo siguiente:
  - `safeCall` devuelve `Result<T, RemoteError>`;
  - se añade el adaptador `Result<T, RemoteError>.asDataError()` para los repositorios que aún no lo usan;
  - auth y club pasan a `RemoteError` en esta spec y en la 003.
- Se inyecta `Clock` (`kotlin.time.Clock.System`) en el módulo de Koin de core, para usarlo en las specs 005 y 007.

## core/data
- `HttpClientFactory`:
  - `Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false }`;
  - el refresh guarda los tokens que devuelve `AuthenticatedUserDto`;
  - un 429 en el refresh no limpia la sesión;
  - se revisa `x-api-key` (pregunta abierta).
- `HttpClientExt.responseToResult`:
  - 2xx → body, o `Unit` si es 204 o el body está vacío;
  - en cualquier otro caso → `RemoteError` parseando `ErrorResponseDto(code: String? = null, message: String? = null, errors: List<String> = emptyList())`, con `runCatching`.
- `AuthRoutes`: las rutas siguen siendo relativas (la base URL incluye `/api/v1`). Se elimina `VERIFY_EMAIL_ROUTE` del flujo de deep link.
- `KtorAuthRepositoryImpl`: `login` → `/me` + `/users/{id}`, para construir `AuthInfoModel`.
- `BuildKonfigConventionPlugin`: `DEFAULT_BASE_URL_HTTP = "http://10.0.2.2:8080/api/v1"`.

## feature/auth (presentación)
- `LoginViewModel`: mapea `EMAIL_NOT_VERIFIED` → estado `showResendVerification` y `INVALID_CREDENTIALS` y `RATE_LIMIT_EXCEEDED` → `UiText`.
- `AuthGraph`: deep link `squadfy://reset-password?token={token}`; se eliminan los patrones `https://squadfy.com/api/auth/...`.
- `AndroidManifest`: `intent-filter` con `scheme=squadfy` y `host=reset-password`. En iOS, el `onOpenURL` ya enruta el esquema `squadfy`.

## feature/chat
- Rutas en `KtorChatService`, `KtorChatParticipantService`, `KtorChatMessageService` y `KtorDeviceTokenRepositoryImpl`.
- `ChatDTO.creator: ChatParticipantDTO? = null`. Se elimina `email` de `ChatParticipantDTO`, si lo tiene.
- Se actualizan las aserciones de ruta de los tests (`encodedPath`).

## Tests
| AC | Test |
|---|---|
| 01 | `RoutesTest` (no hay `/api/` en las constantes) |
| 02/03/04 | `HttpClientExtTest` (MockEngine: 200, 201, 204, 400 de validación, 403 con code, 401 vacío, 500 HTML, enum desconocido) |
| 05/06 | `BackendErrorCodeTest`, `RemoteErrorToUiTextTest` |
| 08/09 | `LoginViewModelTest` (fakes) |
| 10 | `TokenRefreshTest` (MockEngine: rotación, 401 y 429) |
| 11, 15, 16 | `KtorAuthRepositoryTest`, los tests de chat actualizados y `KtorDeviceTokenRepositoryImplTest` |
| 17 | Fixtures en `core/data/src/commonTest/resources/fixtures/*.json` |
