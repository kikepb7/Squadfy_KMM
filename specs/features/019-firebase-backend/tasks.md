# 019 · Tareas

## Fase 0 · Plataforma
- [ ] **T-001** `SQUADFY_BACKEND` y las opciones del emulador en BuildKonfig, validados (AC-019-01). PRE usa el sufijo `.dev` en Android e iOS (D-14). Verificación: el build falla con un valor desconocido.
- [ ] **T-002** Versiones alineadas con GitLive 2.7.0: BoM 34.18.0, plugins, iOS SDK 11.15.0 y productos SPM (D-17). Verificación: `assembleDebug` y `compileKotlinIosSimulatorArm64`.
- [ ] **T-003** Módulo `core/firebase`: init, emuladores, sesión, `safeFirebaseCall`, mapeo de errores a `BackendErrorCode` y `FunctionsClient` (AC-019-02, 03).
- [ ] **T-004** Carpeta `firebase/`: `firebase.json` con todos los emuladores, `.firebaserc.example` y el esqueleto de Functions (TypeScript, Jest). Verificación: `firebase emulators:start` arranca.
- [ ] **T-005** Selección de los módulos Koin en `composeApp` según el backend (AC-019-01).

## Fase 1 · Cuenta
- [ ] **T-006** `AuthRepository` neutral: sesión observable, logout sin refresh token, verificación y restablecimiento con los enlaces de cada backend. Se adapta la variante `api` sin cambiar su comportamiento.
- [ ] **T-007** `FirebaseAuthRepository`: registro con `users/{uid}`, login, logout, verificación, recuperación, cambio de contraseña y sesión (AC-019-04, 05).
- [ ] **T-008b** Login con Google y Apple, con la pantalla para completar el perfil (AC-019-23).
- [ ] **T-008** Callable `deleteAccount` con la anonimización y la sucesión del owner, y la reautenticación en la app (AC-019-06).

## Fase 2 · Reglas y modelo
- [ ] **T-009** `firestore.rules`, `storage.rules` y `database.rules.json` con sus tests (AC-019-17, 21).
- [ ] **T-010** `specs/contracts/firebase.md`: colecciones, callables, errores y jobs.

## Fase 3 · Dominio en Functions
- [ ] **T-011** Lógica pura portada con sus tests: calendario, plazos, asignador, equilibrador, Elo, estadísticas y roles.
- [ ] **T-012** Clubes, miembros, roles, vetos, transferencia y código (AC-019-07).
- [ ] **T-013** Horario, semanas especiales, planificación y partido extra (AC-019-08).
- [ ] **T-014** Convocatoria, lista de espera, invitados y ausencias (AC-019-09).
- [ ] **T-015** Jobs de cierre, sorteo y avisos (AC-019-10).
- [ ] **T-016** Equipos, acta, Elo y reapertura (AC-019-11).
- [ ] **T-017** Estadísticas y clasificación (AC-019-12).
- [ ] **T-018** Push: los 7 tipos, silencio del club y limpieza de tokens (AC-019-13).

## Fase 4 · Capa de datos de la app
- [ ] **T-019** `feature/club/data-firebase`: repositorios con listeners y callables (AC-019-14).
- [ ] **T-020** `feature/chat/data-firebase`: chat, participantes, búsqueda y fotos (AC-019-15, 17).
- [ ] **T-021** Presencia y «escribiendo…» en Realtime Database (AC-019-16).
- [ ] **T-022** Tokens de FCM en `users/{uid}/devices` (Android e iOS).

## Fase 5 · Telemetría y configuración
- [ ] **T-023** Analytics con consentimiento y `screen_view`; Crashlytics en iOS con CrashKiOS (AC-019-18).
- [ ] **T-024** Remote Config como `RemoteFeatureFlagSource` (AC-019-19, cierra D-10).
- [ ] **T-025** App Check (AC-019-20).
- [ ] **T-026** Hosting: privacidad, página de borrado de cuenta y página de acción de Auth.

## Fase 6 · Verificación
- [ ] **T-027** CI: emuladores con los tests de Functions y reglas, y las dos variantes de la app (AC-019-21, 22).
- [ ] **T-028** E2E en Android e iOS contra el emulador: el ciclo semanal completo con dos usuarios.
- [ ] **T-029** `docs/FIREBASE.md`, `CLAUDE.md` y roadmap. Despliegue a PRE (lo autoriza el owner).
