# 011 · Tareas

- [x] **T-001** Retirar `economy` y `onboarding` del build y de Koin (AC-011-10).
- [x] **T-002** Release Android: firma, R8 con `proguard-rules.pro`, `versionCode` y `versionName`, guarda HTTPS en release y cleartext solo en debug (AC-011-01/02/03). *Verificación:* `assembleRelease` con `http` falla con un mensaje claro; con `-PALLOW_INSECURE_RELEASE=true` (solo QA local) la release minificada (11,9 MB frente a 34 MB en debug) inicia sesión y navega por Inicio, el club, la clasificación y la ficha del miembro contra el backend local, sin errores de R8.
- [x] **T-003** Logging por tipo de build: `IS_RELEASE`, Ktor, Kermit, Koin y sin log del token FCM (AC-011-04).
- [x] **T-004** Sesión cifrada (Keystore/Keychain) con migración, `allowBackup = false` y reglas de extracción (AC-011-05). *Verificación:* `EncryptedSessionStorageTest`, y en el emulador una sesión en claro de la build anterior se migra a `enc1:` sin cerrar la sesión.
- [x] **T-005** Branding: nombre, icono adaptativo y splash con el logo de Squadfy (AC-011-06).
- [x] **T-006** i18n de auth y chat (ES/EN) + `checkHardcodedStrings` en el CI (AC-011-09). Verificación: auth, chat y designsystem pasan a ES por defecto + `values-en`; se corrige `core/presentation` (estaba invertido); los literales de ResetPassword, ChatListHeader, el contador de miembros y MatchCard pasan a recursos; `./gradlew checkHardcodedStrings` en verde y bloqueante en el CI; login visto en ES y EN en el emulador.
- [ ] **T-007** Eliminar cuenta tras `ACCOUNT_DELETION` + enlaces a privacidad y a la web de borrado (AC-011-07/08).
- [ ] **T-008** Accesibilidad: `contentDescription` y áreas táctiles (AC-011-11).
- [ ] **T-009** iOS: bundle canónico, entitlement de producción, sin `print()` y plist fuera del tracking (AC-011-12).
- [ ] **T-010** Crashlytics con consentimiento (AC-011-13).
- [ ] **T-011** Job de release en el CI con subida a Play Internal (AC-011-14).
- [ ] **T-012** Verificación: release minificada en el emulador (login, club, convocatoria y partido).
- [ ] **T-013 (manual, owner)** `TEAM_ID`, app iOS en Firebase con el bundle nuevo, keystore de subida y secretos del CI (`SIGNING_*`, `PLAY_SERVICE_ACCOUNT_JSON`, `GOOGLE_SERVICE_INFO_PLIST`), y publicar las páginas legales.
