# Firebase en Squadfy (spec 019, ADR-0010)

La app se lanza con Firebase como backend. El backend propio (`../Squadfy_Backend`, Supabase y PostgreSQL) sigue integrado, pero congelado, como variante `api`.

- **Modelo de datos, callables, errores y jobs**: `specs/contracts/firebase.md`.
- **Lado servidor** (reglas, Functions, Hosting, emuladores): `firebase/README.md`.
- **Configuración de iOS**: `iosApp/Firebase/README.md`.

## 1. Variantes y entornos
| Propiedad | Valores | Por defecto |
|---|---|---|
| `SQUADFY_BACKEND` | `firebase` o `api` | `firebase` |
| `SQUADFY_ENV` | `pre` o `pro` | `pre` en debug y `pro` en release |
| `squadfy.firebase.useEmulator` (`local.properties`) | `true` o `false` | `false`; solo funciona en debug |

Se pasan con `-P`, como variable de entorno o en `local.properties`.

- **PRE** es la app «Dev» (`com.kikepb.squadfy.dev`) y **PRO** la app «Prod» (`com.kikepb.squadfy`). Las dos están en el mismo proyecto de Firebase, `squadfy-5233d`.
- **Ficheros de configuración** (gitignored):
  - Android: `composeApp/google-services.json`, con los dos paquetes;
  - iOS: `iosApp/Firebase/Debug/GoogleService-Info.plist` y `iosApp/Firebase/Release/GoogleService-Info.plist`. Una fase de build copia el que toca.
- **Qué cambia en la variante `api`**: vuelve a Ktor, Room y el WebSocket. No hay presencia, ni inicio de sesión con Google o Apple, ni Analytics.

## 2. Probar en local (sin tocar el proyecto real)
```bash
cd firebase/functions && npm ci
npm run serve        # emuladores con Hosting; UI en http://localhost:4006, web en http://localhost:5006
npm run seed         # en otra terminal, la primera vez: club demo con partidos jugados, el próximo con equipos y chats
```
1. Añade `squadfy.firebase.useEmulator=true` a `local.properties` y compila en debug.
2. Entra con `demo.owner@squadfy.test`; la contraseña de demo, que solo existe en el emulador, está en `functions/scripts/seed-emulator.mjs`.

En el emulador los jobs programados no se disparan solos. La callable `runJobsNow({at})` los ejecuta como si fuera ese instante: cierra, sortea, planifica y avisa.

`serve` exporta los datos al salir y los importa al arrancar. Reiniciar los emuladores invalida las sesiones abiertas, así que la app vuelve a pedir el login: es lo esperado.

## 3. Pendiente en consolas antes de desplegar
Firebase (proyecto `squadfy-5233d`):
- [ ] **Authentication**:
  - plantillas de email (verificación, cambio de contraseña y de email): *Customize action URL* → `https://squadfy-5233d.web.app/auth/action`;
  - dominios autorizados: el de Hosting y, si lo hay, el dominio propio.
- [ ] **App web** registrada en el proyecto. Hosting la necesita para servir `/__/firebase/init.json` a las páginas de borrado de cuenta y de acción.
- [ ] **Cloud Messaging**: la clave APNs (`.p8`) subida a las dos apps de iOS. Sin ella, iOS no recibe ningún aviso (ni de partido ni de la app) y la suscripción a los *topics* falla.
- [ ] **App Check**:
  - Play Integrity en las apps de Android y App Attest en las de iOS;
  - registrar los tokens de depuración que imprimen las builds debug (`Enter this debug secret…` en Android; `App Check debug token` en el log de Xcode).
  - Después, `ENFORCE_APP_CHECK=true` en `firebase/functions/.env.squadfy-5233d`, y la exigencia en Firestore, Storage y RTDB desde la consola.
- [ ] **Analytics**: retención de datos (2 meses) y sin señales de anuncios.
- [ ] **Crashlytics**: activado. Solo recibe informes de builds release con el consentimiento del usuario. En iOS, la fase de compilación que sube los dSYM (`upload-symbols` de FirebaseCrashlytics) en las dos configuraciones; en Android el plugin ya sube el mapping de R8.
- [ ] **Sign in with Apple en Android y en la web**: un *Services ID* de Apple con el dominio `squadfy-5233d.firebaseapp.com` y la URL de retorno de Firebase, configurado en el proveedor Apple de Firebase.

Apple:
- [ ] `TEAM_ID` en `iosApp/Configuration/Config.xcconfig`.
- [ ] En los dos bundles, las capacidades Push Notifications, Sign in with Apple y App Attest (`devicecheck.appattest-environment` ya está en las entitlements de Release).

Google Play:
- [ ] Los SHA-1 y SHA-256 de *Play App Signing* en la app «Prod» de Firebase (la firma de subida ya está).
- [ ] La URL de borrado de cuenta en *Data safety* (`/account/delete`).

Contraseñas:
- [ ] Las contraseñas del keystore en `keystore.properties`, o en los secretos del entorno `production` del repositorio.

## 4. Estado del proyecto real (comprobado el 2026-10-27)
| Pieza | Estado |
|---|---|
| Apps | Android Dev y Prod, iOS Dev y Prod. Falta la app web (§3) |
| Firestore | `(default)` en `eur3`. Protección contra el borrado, PITR (7 días) y backup diario (se guarda 14 días), activados el 2026-10-27 |
| Functions | 61 en `europe-west1` (Node 22), desplegadas el 2026-10-26. **Las de las specs 020 y 021** (temporadas, motivo obligatorio, silencio por club, idioma, recordatorio, resultado y avisos de club) **no están desplegadas todavía** |
| Reglas | Firestore y Storage, iguales que las del repo. Realtime Database existe en `europe-west1` y deniega la raíz |
| Remote Config | Plantilla v2 desplegada el 2026-10-27: condiciones `pro_android`, `pro_ios`, `pre_android` y `pre_ios`; versión 1.0.0 en las cuatro y `store_url` solo en Android (iOS no tiene id de App Store aún); las flags usan el valor por defecto de la app |
| Analytics | Enlazado a la propiedad 560777788, con un flujo por app (las cuatro). Eventos de la spec 021 en §7 |
| Índices | El de grupo de colección de `matches` (`status`, `scheduledAt`) para el recordatorio **no está desplegado** |
| Hosting | Sin desplegar |
| App Check | Sin configurar: las callables reciben tokens no válidos y los aceptan porque `ENFORCE_APP_CHECK` está apagado |

⚠️ Dev y Prod comparten proyecto: las pruebas de la app Dev escriben en la base de datos de producción.

## 5. Desplegar (solo cuando lo decida el owner)
```bash
cd firebase && cp .firebaserc.example .firebaserc
firebase deploy --only firestore,database,storage,functions,remoteconfig,hosting
```
1. Primero, con `ENFORCE_APP_CHECK` sin definir o a `false`.
2. Cuando las builds release se atestigüen y los tokens de depuración estén registrados, pásalo a `true` y vuelve a desplegar las Functions.

## 6. Privacidad (RGPD)
- Los informes de fallos (Crashlytics) y las estadísticas de uso (Analytics) están **apagados por defecto**, tanto en el manifest como en el Info.plist.
- Cada uno tiene su interruptor en Perfil. La elección se guarda en el dispositivo y se aplica al arrancar.
- Analytics registra el `screen_view` con el nombre de la clase de la ruta y los eventos de §7. Nunca lleva ids, nombres ni texto.
- Sin id de usuario, sin identificador publicitario y sin personalización de anuncios.
- La política está en `firebase/hosting/legal/privacy.html`. Si cambian los proveedores o los datos, actualízala.

## 7. Avisos, versión y eventos (spec 021)
**Avisos push.** Los manda el servidor (tabla en `specs/contracts/firebase.md` §6), en el idioma del dispositivo y sin los del club silenciado. En Android hay cuatro canales, que el usuario puede silenciar por separado: Partidos, Mensajes, Club y temporada, y Novedades de Squadfy.

**Avisos de la app** (nueva versión o anuncio). La app se suscribe sola al *topic* de su plataforma e idioma. Los manda el owner con credenciales de administrador:
```bash
cd firebase/functions
# primero en una build Dev:
node scripts/send-app-notice.mjs update --version 1.1.0 --platform android --env pre
node scripts/send-app-notice.mjs announcement --es-title "…" --es-body "…" --en-title "…" --en-body "…" [--url https://…] --env pro
```
Añade `--dry-run` para validar sin enviar. El aviso de versión abre la tienda; con la app abierta, no se muestra si ya tiene esa versión.

**Aviso de nueva versión dentro de la app.** Lo controla Remote Config, por app:
- `latest_version`: por debajo, la app la ofrece una vez (se puede descartar hasta la siguiente);
- `min_version`: por debajo, la app no deja seguir hasta actualizar. Súbela solo con cambios incompatibles, y mira antes `appVersion` en `users/{uid}/devices`;
- `store_url`: la ficha de la tienda. **En iOS, ponla antes de subir las otras dos**: sin ella el botón queda desactivado.

**Eventos de Analytics** (solo con el consentimiento, sin ids ni nombres): `club_created`, `club_joined`, `match_enrolled`, `teams_generated` (`mode` auto o manual), `match_completed`, `schedule_exception_added` (`kind`), `season_updated`, `update_prompt_shown` (`required`), `update_prompt_accepted` y `screen_view`. Para verlos al momento en DebugView: `adb shell setprop debug.firebase.analytics.app com.kikepb.squadfy.dev` en Android, o `-FIRDebugEnabled` en el esquema de Xcode.

