# 002 · Tareas

## Fase 1 · core
- [x] **T-001** `BackendErrorCode` + `RemoteError` en `core/domain`, más `mapError` en `Result`. *Verificación:* `HttpClientExtTest` (AC-002-05).
- [x] **T-002** `ErrorResponseDto` + helpers `api*` (`apiGet/Post/Put/Patch/Delete/PostMultipart/PutMultipart`) que devuelven `RemoteError`. Los helpers antiguos delegan en ellos y siguen devolviendo `DataError.Remote` hasta que cada repositorio migre. *Verificación:* `HttpClientExtTest` (AC-002-03/04).
- [x] **T-003** `Json` tolerante (`coerceInputValues`, `explicitNulls = false`). *Verificación:* `HttpClientExtTest` (AC-002-02).
- [x] **T-004** Base URL `/api/v1` en la convention + revisión de `x-api-key`. *Verificación:* `HttpClientExtTest` (AC-002-01). `x-api-key`: el backend no la lee (D-2).
- [x] **T-005** Refresh: rotación (conservando la foto), 401 → logout, 429 o sin red → **sin** logout (antes, cualquier fallo cerraba la sesión), y `change-password` ya no queda excluido del refresh. *Verificación:* `TokenRefreshTest` (AC-002-10).
- [x] **T-006** `RemoteError.toUiText()` + strings ES/EN para cada code. *Verificación:* `RemoteErrorToUiTextTest` (AC-002-06).
- [x] **T-007** Inyección de `Clock` en Koin (hecho en la spec 005: `single<Clock> { Clock.System }` en `CoreDataModule`). *Pospuesta a la spec 005*, que es la primera que la necesita (la ventana de convocatoria).

## Fase 2 · auth
- [x] **T-008** `UserSerializableDTO` sin `profilePictureUrl` obligatorio; login → usuario de la respuesta de login + foto de `/users/{id}` (AC-002-11). *Verificación:* `AuthDtoFixtureTest`.
- [x] **T-009** Login: estados para `EMAIL_NOT_VERIFIED` (reenviar), `INVALID_CREDENTIALS` y 429 (AC-002-08/09) + `LoginViewModelTest`.
- [x] **T-010** Validaciones de registro AUTH-3 (AC-002-13): ya se cumplían; la regla de contraseña del cliente es más estricta que la del backend.
- [x] **T-011** Deep link `squadfy://reset-password` en Android (`intent-filter`) e iOS (esquema `squadfy` ya registrado); se quitan los patrones `https://squadfy.com/api/auth/reset-password`. El deep link de verificación queda sin efecto, porque el email abre el navegador (AC-002-12). *Verificación pendiente:* `adb shell am start -d "squadfy://reset-password?token=x"`.

## Fase 3 · chat, perfil y dispositivos
- [x] **T-012** Rutas de chat v1 + `creator` + tests actualizados (AC-002-15).
- [x] **T-013** `/users` y foto de perfil v1 (AC-002-14).
- [x] **T-014** `/devices` v1 (AC-002-16).

## Fase 4 · Verificación
- [x] **T-015** Fixtures reales (`BACKEND.md` §9) en los tests de mappers (AC-002-17): `AuthDtoFixtureTest` y `ChatDtoFixtureTest`.
- [ ] **T-016** E2E manual contra el backend local:
  ```bash
  cd ../Squadfy_Backend && docker compose up -d
  JWT_SECRET_BASE64=$(openssl rand -base64 32) FIREBASE_ENABLED=false ./gradlew :app:bootRun --args='--spring.profiles.active=dev'
  ```
  Registro → verificar con Mailpit (http://localhost:8025) → login → chat → foto → logout. **Pendiente: requiere arrancar Docker y el backend; lo hace el owner o una sesión con el backend en alcance.**
