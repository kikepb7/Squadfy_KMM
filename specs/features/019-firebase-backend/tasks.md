# 019 · Tareas

## Fase 0 · Plataforma
- [x] **T-001** `SQUADFY_BACKEND` y las opciones del emulador en BuildKonfig, validados (AC-019-01). PRE usa el sufijo `.dev` en Android e iOS (D-14). Verificación: el build falla con un valor desconocido.
- [x] **T-002** Versiones alineadas con GitLive 2.7.0: BoM 34.18.0, plugins, iOS SDK 11.15.0 y productos SPM (D-17). Verificación: `assembleDebug` y `compileKotlinIosSimulatorArm64`.
- [x] **T-003** Módulo `core/firebase`: init, emuladores, sesión, `safeFirebaseCall`, mapeo de errores a `BackendErrorCode` y `FunctionsClient` (AC-019-02, 03).
- [x] **T-004** Carpeta `firebase/`: `firebase.json` con todos los emuladores, `.firebaserc.example` y el esqueleto de Functions (TypeScript, Jest). Verificación: `firebase emulators:start` arranca.
- [x] **T-005** Selección de los módulos Koin en `composeApp` según el backend (AC-019-01). Hay un conjunto de fuentes por variante (`src/backendFirebase` y `src/backendApi`). `core/data` se separa en compartido (`coreDataModule`) y backend propio (`coreApiNetworkModule` y `coreApiTelemetryModule`). Mientras la spec está en curso, la variante Firebase usa las implementaciones del backend propio para las features que aún no se han portado. Verificación: las dos variantes compilan y pasan los tests; la app PRE (`com.kikepb.squadfy.dev`) arranca en el emulador.

## Fase 1 · Cuenta
- [x] **T-006** `AuthRepository` neutral. El contrato se mantiene: Firebase ignora el refresh token, y `verifyEmail` y `resetPassword` reciben el `oobCode` del enlace de acción. `MainViewModel` detecta la sesión caducada por su ausencia, no por el refresh token, con el mismo comportamiento en la variante `api`.
- [x] **T-007** `FirebaseAuthRepository`: registro con `users/{uid}`, login, logout, verificación, recuperación, cambio de contraseña y sesión (AC-019-04, 05). La callable `completeProfile` reserva el nombre en `usernames/{lower}` (único, sin distinguir mayúsculas, de 3 a 20 caracteres, como BE-001) y crea el perfil sin el email; las reglas de `users` van con sus tests. `FirebaseSessionStorage` une Auth con `users/{uid}`. Verificación: tests de la callable contra el emulador (3/3); registro y login reales desde la app PRE contra el Emulator Suite.
- [x] **T-008b** Login con Google y Apple, con la pantalla para completar el perfil (AC-019-23).
  - Proveedores: Credential Manager en Android (Google) y un puente Swift en iOS (Apple primero y Google), como en TensoScan.
  - Pantalla «Elige tu nombre»: el nombre es único y, si se abandona, se borra la cuenta a medio crear.
  - Para borrar una cuenta sin contraseña se vuelve a iniciar sesión con el proveedor, y en Apple se revoca el acceso.
  - La variante `api` no ofrece proveedores.
  - Verificación: 5 tests de ViewModel.
- [x] **T-008** Callable `deleteAccount` con la anonimización y la sucesión del owner, y la reautenticación en la app (AC-019-06).
  - Mismas reglas que la spec 010 del backend: los clubes pasan al admin más antiguo o al miembro más antiguo, o se borran; las membresías quedan anonimizadas; se borran ausencias, mensajes, chats vacíos, dispositivos, fotos, el nombre y la cuenta.
  - Un token de una cuenta borrada ya no puede crear clubes ni membresías (401).
  - Verificación: 2 tests contra el emulador.

## Fase 2 · Reglas y modelo
- [x] **T-009** `firestore.rules`, `storage.rules` y `database.rules.json` con sus tests (AC-019-17, 21). Los tests de reglas usan `@firebase/rules-unit-testing` contra el emulador.
- [ ] **T-010** `specs/contracts/firebase.md`: colecciones, callables, errores y jobs.

## Fase 3 · Dominio en Functions
- [x] **T-011** Lógica pura portada con sus tests: calendario, plazos, asignador, equilibrador, Elo, estadísticas y roles. Está en `firebase/functions/src/domain`, y la zona horaria del club se calcula con Luxon. Verificación: 51 tests de Vitest en verde, con los mismos casos que los tests del backend (incluido el cambio de hora y el periodo en hora local).
- [x] **T-012** Clubes, miembros, roles, vetos, transferencia y código (AC-019-07).
  - Callables en `functions/src/clubs`; el id de membresía es `{clubId}_{uid}`. Las reglas dejan leer solo a los miembros activos.
  - En la app, `FirebaseClubRepository` (módulo `feature/club/data-firebase`) lee con listeners y escribe con callables.
  - Verificación: 8 tests de callables y reglas contra el emulador, 3 tests de mappers, y un club creado desde la app PRE.
- [x] **T-013** Horario, semanas especiales, planificación y partido extra (AC-019-08).
- [x] **T-014** Convocatoria, lista de espera, invitados y ausencias (AC-019-09).
- [x] **T-015** Jobs de cierre, sorteo y avisos (AC-019-10).
- [x] **T-016** Equipos, acta, Elo y reapertura (AC-019-11).
- [x] **T-017** Estadísticas y clasificación (AC-019-12).
- [x] **T-018** Push: los 7 tipos, silencio del club y limpieza de tokens (AC-019-13).

## Fase 4 · Capa de datos de la app
- [x] **T-019** `feature/club/data-firebase`: repositorios con listeners y callables (AC-019-14).
  - Las Functions responden el JSON del backend, así que los DTOs y mappers se comparten en `feature/club/data-contract`.
  - Los avisos en vivo llegan de `clubs/{clubId}/live/changes`.
  - `npm run seed` carga en el emulador datos de demo: plantilla, partidos jugados con Elo, el próximo con equipos y lista de espera, y ausencias.
  - Verificación: Inicio, Partido, Equipos (campo), Tabla y Resultado comprobados en la app PRE contra el emulador.
- [x] **T-020** `feature/chat/data-firebase`: chat, participantes, búsqueda y fotos (AC-019-15, 17).
  - Firestore con listeners: los mensajes pendientes salen como «enviando» y se pueden reintentar si fallan.
  - La búsqueda es por prefijo del nombre y la foto de perfil va en Storage.
  - El estado de conexión sale de `.info/connected` de Realtime Database.
  - La variante Firebase ya no usa Ktor ni WebSocket.
  - Verificación: lista de chats, conversación y envío desde la app PRE contra el emulador.
- [x] **T-021** Presencia y «escribiendo…» en Realtime Database (AC-019-16).
- [x] **T-022** Tokens de FCM en `users/{uid}/devices` (Android e iOS), con plataforma, idioma y zona horaria. El proveedor del token sigue en `chat/data` (`platformChatPushModule`).

## Fase 5 · Telemetría y configuración
- [x] **T-023** Analytics con consentimiento y `screen_view`; Crashlytics en iOS con CrashKiOS (AC-019-18).
- [ ] **T-024** Remote Config como `RemoteFeatureFlagSource` (AC-019-19, cierra D-10).
- [ ] **T-025** App Check (AC-019-20).
- [ ] **T-026** Hosting: privacidad, página de borrado de cuenta y página de acción de Auth.

## Fase 6 · Verificación
- [ ] **T-027** CI: emuladores con los tests de Functions y reglas, y las dos variantes de la app (AC-019-21, 22).
- [ ] **T-028** E2E en Android e iOS contra el emulador: el ciclo semanal completo con dos usuarios.
- [ ] **T-029** `docs/FIREBASE.md`, `CLAUDE.md` y roadmap. Despliegue a PRE (lo autoriza el owner).
