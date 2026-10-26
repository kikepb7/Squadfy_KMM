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
- [ ] **Cloud Messaging**: la clave APNs (`.p8`) subida a las dos apps de iOS.
- [ ] **App Check**:
  - Play Integrity en las apps de Android y App Attest en las de iOS;
  - registrar los tokens de depuración que imprimen las builds debug (`Enter this debug secret…` en Android; `App Check debug token` en el log de Xcode).
  - Después, `ENFORCE_APP_CHECK=true` en `firebase/functions/.env.squadfy-5233d`, y la exigencia en Firestore, Storage y RTDB desde la consola.
- [ ] **Analytics**: retención de datos (2 meses) y sin señales de anuncios.
- [ ] **Crashlytics**: activado. Solo recibe informes de builds release con el consentimiento del usuario.
- [ ] **Sign in with Apple en Android y en la web**: un *Services ID* de Apple con el dominio `squadfy-5233d.firebaseapp.com` y la URL de retorno de Firebase, configurado en el proveedor Apple de Firebase.

Apple:
- [ ] `TEAM_ID` en `iosApp/Configuration/Config.xcconfig`.
- [ ] En los dos bundles, las capacidades Push Notifications, Sign in with Apple y App Attest (`devicecheck.appattest-environment` ya está en las entitlements de Release).

Google Play:
- [ ] Los SHA-1 y SHA-256 de *Play App Signing* en la app «Prod» de Firebase (la firma de subida ya está).
- [ ] La URL de borrado de cuenta en *Data safety* (`/account/delete`).

Contraseñas:
- [ ] Las contraseñas del keystore en `keystore.properties`, o en los secretos del entorno `production` del repositorio.

## 4. Desplegar (solo cuando lo decida el owner)
```bash
cd firebase && cp .firebaserc.example .firebaserc
firebase deploy --only firestore,database,storage,functions,remoteconfig,hosting
```
1. Primero, con `ENFORCE_APP_CHECK` sin definir o a `false`.
2. Cuando las builds release se atestigüen y los tokens de depuración estén registrados, pásalo a `true` y vuelve a desplegar las Functions.

## 5. Privacidad (RGPD)
- Los informes de fallos (Crashlytics) y las estadísticas de uso (Analytics) están **apagados por defecto**, tanto en el manifest como en el Info.plist.
- Cada uno tiene su interruptor en Perfil. La elección se guarda en el dispositivo y se aplica al arrancar.
- Analytics registra el `screen_view` con el nombre de la clase de la ruta. Nunca lleva ids, nombres ni texto.
- Sin id de usuario, sin identificador publicitario y sin personalización de anuncios.
- La política está en `firebase/hosting/legal/privacy.html`. Si cambian los proveedores o los datos, actualízala.
