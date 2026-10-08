# 012 · Checklist de lanzamiento (lado app)

## 1. GitHub: secretos (AC-012-07, ADR-0008)
No hay API key de cliente. El CI normal no necesita ningún secreto: sin `GOOGLE_SERVICES_JSON` compila con un relleno.

**Secretos del repositorio** (Settings › Secrets and variables › Actions), opcionales:
| Nombre | Valor | Lo usa |
|---|---|---|
| `GOOGLE_SERVICES_JSON` | `google-services.json` de desarrollo/PRE | `squadfy-ci.yml` (sin él, usa el relleno) |
| `GOOGLE_SERVICE_INFO_PLIST` | `GoogleService-Info.plist` de la app iOS `com.kikepb.squadfy` | futuro job de iOS |

**Environment `production`** (Settings › Environments › New environment `production`):
- *Required reviewers*: el owner. Cada release se queda esperando su aprobación antes de leer los secretos.
- *Deployment branches and tags*: «Selected», con las ramas `release/*` y `hotfix/*` (builds de QA) y el tag `v*` (producción). Ver ADR-0009.
- Secretos del entorno:

| Nombre | Valor |
|---|---|
| `GOOGLE_SERVICES_JSON` | `google-services.json` del proyecto Firebase de producción. Tiene prioridad sobre el del repositorio |
| `PRO_BASE_URL_HTTP` | `https://<api>/api/v1` de producción (backend `master` en Render) |
| `PRO_BASE_URL_WS` | `wss://<api>/ws` de producción |
| `STAGING_BASE_URL_HTTP` | `https://<api-staging>/api/v1` (backend `release` en Render): lo usan los candidatos `X.Y.Z-rc.N` de `release/*` y `hotfix/*` |
| `STAGING_BASE_URL_WS` | `wss://<api-staging>/ws` |
| `SIGNING_KEYSTORE_BASE64` | `base64 -i upload.jks` |
| `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD` | los de la clave de subida |
| `PLAY_SERVICE_ACCOUNT_JSON` | cuenta de servicio con permiso de publicar en Play Console |

Variable opcional del entorno o del repositorio: `PRIVACY_POLICY_URL`.

Clave de subida (una sola vez, guardada fuera del repo y con copia de seguridad):
```bash
keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
```
La primera AAB se sube con **Play App Signing** activado: Google guarda la clave de firma, y la clave `upload` solo sirve para subir.

## 2. Firebase (AC-012-10)
- [ ] App Android `com.kikepb.squadfy` con las huellas **SHA-1 y SHA-256** de la clave de subida (`keytool -list -v -keystore upload.jks -alias upload`) **y** de la clave de Play App Signing (Play Console › Integridad de la app).
- [ ] Descargar el `google-services.json` resultante y actualizar el secreto `GOOGLE_SERVICES_JSON`.
- [ ] App iOS nueva con el bundle `com.kikepb.squadfy`: descargar `GoogleService-Info.plist` (va en `iosApp/iosApp/`, no se versiona) y actualizar el secreto `GOOGLE_SERVICE_INFO_PLIST`.
- [ ] Subir la clave **APNs** (.p8) en Configuración del proyecto › Cloud Messaging › app iOS.
- [ ] En Google Cloud › Credenciales, restringir las API keys: la de Android por paquete y SHA-1, y la de iOS por bundle. La clave iOS antigua aparece en el historial de git, así que conviene rotarla.
- [ ] Crashlytics: comprobar que el panel recibe la primera AAB (mapping subido por el CI con `CRASHLYTICS_MAPPING_UPLOAD=true`).
- [ ] Backend: `FIREBASE_ANDROID_PACKAGE=com.kikepb.squadfy` y las credenciales del mismo proyecto.

## 3. Cuenta de demo para la revisión (AC-012-09)
Se prepara en **PRO** con la app de release. Las credenciales se escriben solo en las consolas: App Store Connect › Información de revisión, y Play Console › Contenido de la app › Acceso a la app.
1. Registrar `review@<dominio>` (con una contraseña fuerte, guardada en el gestor del owner) y una segunda cuenta de apoyo.
2. Con la cuenta de demo: crear el club «Squadfy Demo FC» y unir 10 o más jugadores de apoyo con el código. Así la convocatoria se ve llena, con lista de espera.
3. Crear el horario semanal de forma que la convocatoria esté **abierta** el día de la revisión. Las revisiones duran 1–3 días, así que hay que revisarlo antes de enviar.
4. Dejar al menos un partido **cerrado** con goles y minutos, para que Clasificación y Estadísticas tengan datos.
5. Crear un chat con un par de mensajes.
6. En las notas de revisión: qué es un club, que el código de invitación está en Ajustes y cómo probar el borrado de cuenta. No usar la cuenta de demo para probar el borrado; se borra una cuenta propia.

## 4. Smoke test en producción (AC-012-11)
Dos dispositivos, A (Android, build de Play Internal) y B (iOS, TestFlight), con cuentas nuevas. Marcar cada paso en los dos.

| # | Paso | A | B |
|---|---|---|---|
| 1 | Registro → email de verificación → enlace → login | ☐ | ☐ |
| 2 | A crea un club (con escudo); B se une con el código | ☐ | ☐ |
| 3 | A crea el horario → llega la push de apertura a los dos → los dos se apuntan | ☐ | ☐ |
| 4 | Llenar el cupo → el siguiente entra en lista de espera; uno se borra → el primero en espera sube y recibe la push | ☐ | ☐ |
| 5 | Partido extra con cierre próximo → al cerrar, sorteo → push de equipos publicados | ☐ | ☐ |
| 6 | Acta: goles, asistencias y minutos → cerrar el partido → las clasificaciones se actualizan | ☐ | ☐ |
| 7 | Chat entre A y B, en tiempo real | ☐ | ☐ |
| 8 | Silenciar el club → no llega la siguiente push | ☐ | ☐ |
| 9 | Perfil: foto, cambiar la contraseña, informes de errores (solo A), política de privacidad | ☐ | ☐ |
| 10 | Cerrar sesión y volver a entrar | ☐ | ☐ |
| 11 | Borrar la cuenta de B desde la app → no puede entrar; en el club de A aparece «Usuario eliminado» en los partidos pasados | ☐ | ☐ |
| 12 | Borrar una cuenta de prueba desde la web `/account/delete` | ☐ | — |
| 13 | Crashlytics: con el consentimiento activado, un fallo forzado en una build interna aparece en el panel | ☐ | — |

## 5. Orden recomendado
1. Backend en PRO (spec 012 AC-012-01…06 del backend), con la spec 010 fusionada.
2. Secretos (§1) → ejecutar el workflow `Release` → Internal testing.
3. Firebase (§2) → smoke test (§4) en Internal y TestFlight.
4. Activar `ACCOUNT_DELETION` en PRO (`defaultInPro = true`) en cuanto el endpoint esté desplegado, y sacar otra build.
5. Closed testing en Play (12 testers durante 14 días) y TestFlight externo; al mismo tiempo, la beta con un club real (AC-012-12).
6. Fichas (`store-listing.md`, `privacy-data.md`) y la cuenta de demo (§3) → enviar a revisión.
