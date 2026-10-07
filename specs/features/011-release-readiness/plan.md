# 011 · Plan

## Cumplimiento de la constitución
| Principio | Estado | Nota |
|---|---|---|
| I. El backend es la fuente de verdad | ✅ | El borrado de cuenta usa el endpoint del backend (D-3). No hay reglas propias. |
| III. Calidad (tests y build en verde) | ✅ | Cada parte se verifica con `testDebugUnitTest`, `assembleDebug`, `assembleRelease` y la compilación de iOS. |
| IV. Seguridad y privacidad | ✅ | Release con HTTPS obligatorio, sin logs sensibles, sesión cifrada y sin backup. |

## Diseño por partes
1. **Módulos vacíos (AC-011-10):** fuera de `settings.gradle.kts`, `composeApp` y Koin.
2. **Release Android (AC-011-01/02/03):** en `AndroidApplicationConventionPlugin`:
   - `signingConfigs.release` desde `keystore.properties` o las variables `SIGNING_*` (si faltan, la release queda sin firmar, para poder compilarla en local);
   - `isMinifyEnabled` e `isShrinkResources`, con `proguard-rules.pro` en `composeApp`;
   - `versionCode` desde `-PversionCode` y `versionName` `1.0.0`.

   `BuildKonfigConventionPlugin` hace fallar el build si una tarea de release (Android `*Release*` o el `CONFIGURATION=Release` de Xcode) resuelve `BASE_URL_*` a `http`/`ws`. El cleartext a `10.0.2.2` pasa a un `network_security_config` solo de `src/debug`.
3. **Logging (AC-011-04):** BuildKonfig expone `IS_RELEASE`, que activa una release con HTTPS o el entorno `pro`. Así:
   - Ktor usa `LogLevel.NONE` en release, y en debug `HEADERS` sin `Authorization`;
   - Kermit sube a `Warn`;
   - el logger de Koin solo se instala en debug;
   - se quita el log del token FCM.
4. **Sesión cifrada (AC-011-05):**
   - el `SessionStorage` cifra con un `SessionCipher` expect/actual: AES-GCM con la clave en Android Keystore, y Keychain en iOS;
   - migra la sesión en claro existente;
   - `allowBackup = false` y `dataExtractionRules`.
5. **Branding (AC-011-06):** el icono adaptativo y el splash usan el vector del logo de Squadfy (`SquadfyBrandLogo`), y la app se llama «Squadfy».
6. **i18n (AC-011-09):** los textos de auth y chat pasan a ES (por defecto) y EN, y un test de Gradle (`checkHardcodedStrings`) falla si quedan literales en `Text("…")` en `presentation`.
7. **Cuenta (AC-011-07/08):**
   - `DeleteAccountUseCase` llama a `DELETE /me` y luego hace un logout local completo;
   - el diálogo con contraseña va en Perfil, tras el flag `ACCOUNT_DELETION` (apagado en PRE y en PRO hasta que el backend lo tenga);
   - los enlaces de privacidad van en Registro y Perfil.
8. **Accesibilidad (AC-011-11):** `contentDescription` en los iconos de acción y áreas táctiles de ≥ 48 dp en el design system.
9. **iOS (AC-011-12):**
   - bundle `com.kikepb.squadfy`;
   - `TEAM_ID` sigue vacío hasta que el owner lo dé (manual);
   - entitlement `production` para Release;
   - sin `print()`;
   - el plist sale del tracking y el CI lo escribe desde el secreto `GOOGLE_SERVICE_INFO_PLIST`.
10. **Crashlytics (AC-011-13):**
    - plugin y SDK en `composeApp` (Android);
    - la recogida está desactivada por defecto y se activa con el consentimiento guardado en DataStore, que se pide en Perfil;
    - iOS queda fuera del MVP (pendiente).
11. **CI de release (AC-011-14):** un job con `workflow_dispatch` y tags `v*`:
    - descodifica el keystore;
    - ejecuta `bundleRelease -PversionCode=${{ github.run_number }}` con `SQUADFY_ENV=pro` y las URLs de producción;
    - sube el AAB a Play Internal con `r0adkll/upload-google-play`.

## Estrategia de tests
| AC | Test |
|---|---|
| 011-03 | `assembleRelease` falla con `BASE_URL_HTTP=http://…`, comprobado a mano y documentado en tasks |
| 011-05 | `EncryptedSessionStorageTest` (cifra, descifra y migra el texto en claro) con un cipher fake |
| 011-07 | `DeleteAccountViewModelTest`: contraseña vacía, 401 y éxito que cierra sesión |
| 011-09 | tarea `checkHardcodedStrings` en el CI |
| 011-01 | `assembleRelease` minificado e instalado en el emulador: login, club y convocatoria |
