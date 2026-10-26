# 019 · Firebase como backend de lanzamiento

- **Estado:** Approved (2026-10-26)
- **Owner:** kikepb7
- **Reglas:** todas las de `business-rules.md` Parte A (BE-001…BE-008 RN-x), sin cambios; APP-RN-xx sin cambios
- **ADRs:** ADR-0010 (nuevo), ADR-0005 y ADR-0006 (modificados)
- **Repos afectados:** Squadfy_App (incluye `firebase/`). Squadfy_Backend **no** se toca

## Problema / objetivo
La app tiene que poder publicarse sin desplegar ni operar el backend propio. Toda la funcionalidad actual tiene que funcionar contra Firebase, con el mismo comportamiento para el usuario:
- cuenta;
- clubes y roles;
- horario;
- convocatoria y lista de espera;
- sorteo;
- acta y Elo;
- clasificaciones;
- ausencias;
- chat;
- push;
- fotos.

El backend propio sigue integrado, pero congelado, por si en el futuro se vuelve a él.

## Fuera de alcance
- Migrar datos desde Postgres: no hay datos en producción.
- Cambiar reglas de negocio o pantallas. El rediseño es el de la spec 018.
- Modificar `Squadfy_Backend`.

## Historias de usuario
- **US-019-01** Como owner del producto quiero lanzar la app con Firebase para no operar servidores.
- **US-019-02** Como jugador quiero usar la app igual que hasta ahora (crear y unirme a clubes, apuntarme, ver los equipos, el acta y la clasificación, chatear y recibir avisos), y ahora además en tiempo real.
- **US-019-03** Como desarrollador quiero elegir el backend al compilar y desarrollar contra el Emulator Suite sin tocar los proyectos reales.

## Criterios de aceptación
**Selección del backend y plataforma**
- **AC-019-01** *Given* `SQUADFY_BACKEND=firebase` (por defecto) *When* se compila *Then* la app usa solo las implementaciones Firebase, y con `api` usa solo las de Ktor. Cualquier otro valor hace fallar el build.
- **AC-019-02** *Given* `squadfy.firebase.useEmulator=true` en `local.properties` *When* arranca una build debug *Then* Auth, Firestore, Realtime Database, Functions y Storage apuntan al Emulator Suite. En release, la opción se ignora.
- **AC-019-03** Los errores de Firebase y de las Functions se mapean a `DataError.Remote` y a `RemoteError(BackendErrorCode)` con los mismos códigos que el backend propio.

**Cuenta (Firebase Auth)**
- **AC-019-04** Registro con email y contraseña, que crea `users/{uid}` con el nombre de usuario y envía la verificación. Login, logout y recuperación de contraseña funcionan, y una sesión caducada o revocada cierra sesión.
- **AC-019-05** El cambio de contraseña exige la actual (reautenticación).
- **AC-019-06** El borrado de cuenta exige reautenticación y aplica las mismas reglas de borrado del backend (spec 010), en una función: anonimización del historial, salida de los clubes y sucesión del owner.

**Dominio en Functions (mismas reglas BE-NNN)**
- **AC-019-07** Clubes:
  - crear;
  - unirse por código (sin distinguir mayúsculas; rechaza vetados, miembros actuales y clubes llenos);
  - regenerar el código;
  - editar el club y la foto;
  - transferir;
  - salir;
  - expulsar;
  - vetar y quitar el veto;
  - cambiar el rol según la jerarquía.
- **AC-019-08** Horario y semanas especiales:
  - un job horario planifica el siguiente partido de forma idempotente;
  - cancelar y mover un partido;
  - partido extra;
  - cancelar un partido.
- **AC-019-09** Convocatoria:
  - ventana [opensAt, closesAt);
  - aforo y lista de espera recalculados en una transacción, con promoción y su push;
  - invitados (máximo 2 por miembro);
  - las ausencias desapuntan automáticamente.
- **AC-019-10** Un job cada 5 minutos cierra las convocatorias vencidas, sortea y publica los equipos a su hora. También envía los push de apertura y de «cierra pronto», una sola vez cada uno.
- **AC-019-11** Equipos:
  - sorteo AUTO y MANUAL;
  - equilibrio;
  - eventos, minutos y marcador manual;
  - cerrar el acta (Elo);
  - reabrir el último partido, revirtiendo el Elo.
- **AC-019-12** Clasificación de rating y estadísticas por periodo, con empates compartidos y miembros sin partidos a cero.
- **AC-019-13** Push de los 7 tipos actuales, respetando el silencio del club (salvo la promoción desde la lista de espera). Los tokens inválidos se borran.

**Tiempo real, chat y ficheros**
- **AC-019-14** Las pantallas de club, convocatoria, partido y chat se actualizan solas cuando cambian los datos (listeners), sin WebSocket.
- **AC-019-15** Chat en Firestore:
  - crear;
  - participantes;
  - historial paginado;
  - enviar;
  - borrar los mensajes propios;
  - salir.
  
  La búsqueda de usuarios es por prefijo del nombre.
- **AC-019-16** Presencia en Realtime Database: «en línea» y «escribiendo…» en el chat.
- **AC-019-17** Fotos en Storage:
  - perfil;
  - logo del club;
  - foto por club.
  
  Las reglas limitan quién escribe y el tamaño (5 MB, solo imágenes).

**Telemetría y configuración**
- **AC-019-18** Crashlytics y Analytics:
  - funcionan en Android e iOS;
  - respetan el consentimiento;
  - no reciben datos personales;
  - registran el `screen_view` de cada destino.
- **AC-019-19** Las feature flags remotas salen de Remote Config (cierra D-10). Siguen las reglas de spec 013: las overrides solo funcionan en PRE.
- **AC-019-20** App Check está activo, con Play Integrity y App Attest en release y el proveedor debug en desarrollo. Las Functions lo exigen en PRO.

**Login social**
- **AC-019-23** Login con Google (Android: Credential Manager; iOS: GoogleSignIn) y con Apple (iOS: AuthenticationServices; Android: proveedor OAuth de Firebase). La primera vez se pide el nombre de usuario para crear `users/{uid}`. Si se abandona sin completar el perfil, la cuenta se borra.

**Calidad**
- **AC-019-21** Tests:
  - reglas de Firestore, Storage y Realtime Database con `@firebase/rules-unit-testing` contra el emulador;
  - lógica pura de las Functions portada con sus tests;
  - funciones callable probadas contra el emulador.
  
  Todo se ejecuta en el CI.
- **AC-019-22** La variante `api` sigue compilando y sus tests siguen en verde (congelada).

## Casos límite y errores
| Situación | Comportamiento esperado | Código de error |
|---|---|---|
| Sin red | Lecturas desde la caché de Firestore; las acciones de dominio (callables) fallan con el error de red habitual | `NO_INTERNET` |
| Dos usuarios se apuntan a la última plaza a la vez | Transacción: uno CONFIRMED y el otro WAITLISTED | — |
| Código de invitación inexistente | Error tipado | `INVALID_INVITATION_CODE` |
| Usuario vetado intenta unirse | Error tipado | `BANNED_FROM_CLUB` |
| Acción sin permisos (reglas o función) | Error tipado | `FORBIDDEN` / `PERMISSION_DENIED` |
| Sesión revocada (contraseña cambiada en otro dispositivo) | Se cierra la sesión | `UNAUTHORIZED` |
| Demasiadas peticiones de Auth | Error tipado, sin cerrar la sesión | `RATE_LIMIT_EXCEEDED` |
| App Check falla en PRO | La callable se rechaza | `UNAUTHENTICATED` |

## UX
No hay pantallas nuevas. Los cambios visibles son estos:
- todo se actualiza en tiempo real;
- el chat muestra «en línea» y «escribiendo…»;
- la verificación de email y el restablecimiento de contraseña usan los enlaces de Firebase Auth, con la página de acción en Hosting;
- los emails de Firebase Auth se personalizan con la plantilla en ES/EN.

## Contrato
- `specs/contracts/firebase.md` (nuevo): colecciones, documentos, funciones callable con su entrada, salida y errores, jobs, triggers, Storage y Realtime Database. Es el equivalente de `api-v1.md` para la variante Firebase.
- `api-v1.md` no cambia (variante `api`, congelada).

## Decisiones (2026-10-26)
- ✅ **D-14** Un único proyecto de Firebase, **Squadfy (`squadfy-5233d`)**, con dos apps por plataforma:
  - PRE es `com.kikepb.squadfy.dev` (apps «Squadfy Android Dev» y «Squadfy iOS Dev»);
  - PRO es `com.kikepb.squadfy`.
  
  `SQUADFY_ENV=pre` añade el sufijo `.dev`, así que PRE se instala junto a PRO (cierra D-11). El proyecto antiguo `squadfy-backend` deja de usarse en la app.
- ✅ **D-15** Se activa el plan Blaze, con una alerta de presupuesto.
- ✅ **D-16** Login con email, Google y Apple (AC-019-23).
- ✅ **D-17** El Firebase iOS SDK baja a 11.15.0 (`upToNextMajor`), la versión de los bindings de GitLive 2.7.0.

## Checklist de verificación
- [ ] Todos los AC tienen un test que los cita
- [ ] `./gradlew testDebugUnitTest` en verde en las dos variantes
- [ ] `npm test` de `firebase/functions` y los tests de reglas en verde, en el emulador y en el CI
- [ ] Demo manual en Android e iOS contra el emulador y contra PRE: el ciclo semanal completo con dos usuarios
- [ ] `roadmap.md`, `CLAUDE.md` y `docs/FIREBASE.md` actualizados
