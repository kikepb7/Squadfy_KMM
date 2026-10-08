# 011 · Preparación de la app para producción

- **Estado:** In progress (rama `feature/011-release-readiness`; el owner pidió avanzar el 2026-10-08)
- **Reglas:** constitución III y IV
- **Skill:** `kmp-release-readiness` (lista de comprobación detallada)
- **Depende de:** 002 (logging y red). Se puede avanzar en paralelo con 003–010.
- **Backend:** BE-GAP-1 (borrado de cuenta) y BE-GAP-2 (rate limit del refresh)
- **Repos afectados:** Squadfy_App (+ spec de backend para el borrado de cuenta)

## Problema / objetivo
La app no se puede publicar tal como está:
- no hay firma ni R8;
- la release permite tráfico en claro y registra tokens en el log;
- los tokens se guardan sin cifrar y con `allowBackup = true`;
- los textos están escritos en el código y mezclan ES y EN;
- hay módulos vacíos en el build;
- la configuración de iOS está incompleta;
- **no se puede borrar la cuenta**, que es requisito de las tiendas.

## Criterios de aceptación
**Build y seguridad (Android)**
- **AC-011-01** Release firmada, con un keystore fuera del repositorio: `keystore.properties` o variables de entorno. `isMinifyEnabled` e `isShrinkResources` activados, con `proguard-rules.pro` para kotlinx-serialization, Ktor, Room, Koin y Firebase. La release minificada completa el flujo E2E en un dispositivo real.
- **AC-011-02** `versionCode` sale de `-PversionCode` (en CI, `github.run_number`). `versionName` sigue SemVer, con `1.0.0` para el MVP.
- **AC-011-03** URLs por tipo de build:
  - release: `https://…/api/v1` y `wss://…/ws` (spec 012); **el build falla** si una release resuelve a `http`;
  - se elimina `usesCleartextTraffic`, y el cleartext hacia `10.0.2.2` queda solo en el `network_security_config` de debug.
- **AC-011-04** Logging:
  - Ktor en `NONE` en release, y en debug en `HEADERS` con `sanitizeHeader(Authorization)`;
  - Kermit con nivel `Warn` en release;
  - `androidLogger()` de Koin solo en debug;
  - no se registra el token FCM.
- **AC-011-05** Los tokens se guardan cifrados (Android Keystore / iOS Keychain), migrando la sesión existente. `allowBackup = false`, o `dataExtractionRules` que excluyan la sesión y las bases de datos.
- **AC-011-06** Branding: nombre «Squadfy», icono adaptativo, splash y el logo real en los TODO del design system (`SquadfySurface`, `SquadfyBrandLogo`, `ChatListHeader`).

**Producto y tiendas**
- **AC-011-07** **Eliminar cuenta**: Perfil › «Eliminar cuenta» pide confirmación con la contraseña, llama al endpoint del backend (**requiere la nueva spec del backend BE-008, ver la decisión D-3**), cierra la sesión y borra los datos locales. Además hay una URL web de borrado para la ficha de Play.
- **AC-011-08** Hay una política de privacidad publicada y enlazada desde Registro y Perfil. Se rellenan el formulario Data Safety (Play) y las etiquetas de privacidad (App Store): email, username, foto, token de dispositivo y contenido del chat.
- **AC-011-09** i18n: todo texto visible está en `composeResources/values/strings.xml` (ES, por defecto) y en `values-en`. Un test (o un grep en el CI) falla si hay literales en `Text("…")` dentro de `presentation`.
- **AC-011-10** Se retiran los módulos `economy` y `onboarding` del build y de Koin, junto con su `cinterop` y sus dependencias de Firebase duplicadas.
- **AC-011-11** Accesibilidad: hay `contentDescription` en los iconos de acción, las áreas táctiles miden ≥ 48 dp y el contraste cumple AA en los componentes del design system.

**iOS**
- **AC-011-12** `TEAM_ID` configurado y un **bundle ID canónico** común con Android (decisión D-5). Firma automática para TestFlight. `aps-environment` de producción en release. Se quitan los `print()`. `GoogleService-Info.plist` sale del tracking y se inyecta en el CI.

**Calidad**
- **AC-011-13** Crashlytics (o Sentry) en release, con opt-in según la política de privacidad.
- **AC-011-14** El job de release del CI construye el AAB firmado y lo sube a Play Internal Testing (con `r0adkll/upload-google-play` o Fastlane). El job de iOS (macOS runner) es opcional en el MVP.

## Decisiones (2026-10-08, owner)
- **D-5 Bundle ID canónico:** `com.kikepb.squadfy` en Android e iOS. En iOS hay que dar de alta la app nueva en Firebase y en App Store Connect (manual).
- **Crashes:** Firebase Crashlytics, solo en release y con consentimiento.
- **D-3 Borrado de cuenta:** `DELETE /api/v1/me {password}` → 204, y 401 `INVALID_CREDENTIALS` si la contraseña es incorrecta. Confirmado por la spec 010 del backend (implementada en la rama `account-deletion-feature`). En la app queda tras el flag `ACCOUNT_DELETION`: activo en PRE y apagado en PRO hasta que el backend esté desplegado.
- **URLs legales:** `PRIVACY_POLICY_URL` y `ACCOUNT_DELETION_URL` salen de BuildKonfig (sobrescribibles con `-P`, entorno o `local.properties`). La política de privacidad es por defecto `https://squadfy.app/privacy`, y la publica el owner. La página de borrado la sirve el propio backend (spec 010) en `/account/delete`, así que por defecto se deriva de `BASE_URL_HTTP` (`https://<host>/account/delete`) en lugar del placeholder `https://squadfy.app/delete-account`.
- **Sesión en iOS (AC-011-05):** en lugar del Keychain se usa la protección de datos de iOS. El fichero de sesión va en `Application Support/squadfy` con `NSFileProtectionCompleteUntilFirstUserAuthentication` y excluido de las copias de seguridad. Pasar al Keychain queda como mejora posterior, porque requiere probarlo en un dispositivo con los entitlements reales.
- **Contraste (AC-011-11):** el modo oscuro cumple AA en todos los tokens de texto, tras aclarar el rojo de error (`error` = Red200 con contenido oscuro). En claro, `textPlaceholder` pasa a cumplir AA (Base700 algo más oscuro). Queda pendiente el texto de marca en claro (decisión D-12 del roadmap).
