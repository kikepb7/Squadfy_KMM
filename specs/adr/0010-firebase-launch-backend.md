# ADR-0010 · Firebase como backend de lanzamiento; el backend propio queda congelado

- **Estado:** Propuesto (2026-10-26). Modifica ADR-0005 y ADR-0006.
- **Fecha:** 2026-10-26

## Contexto
- `Squadfy_Backend` (Spring Boot, Postgres en Supabase, Redis, RabbitMQ) está completo, pero **no está desplegado** (BE-GAP-3, D-4). Desplegarlo y operarlo supone mantener 4–5 servicios gestionados.
- El owner quiere lanzar con **Firebase** y no mantener servidores. Si más adelante hace falta escalar, se volvería a un backend propio.
- TensoScan KMP ya usa Firebase con el SDK de GitLive, un módulo `core/firebase` y una carpeta `firebase/` (reglas, Remote Config, Hosting). Squadfy ya usa Firebase para FCM y Crashlytics.
- A diferencia de TensoScan, donde cada usuario solo ve sus datos, Squadfy tiene reglas que afectan a varios usuarios y que no se pueden confiar al cliente (constitución I): ventana de convocatoria, aforo y lista de espera, sorteo, Elo, estadísticas, roles y jobs programados.

## Decisión
1. **Firebase es el backend de lanzamiento**:
   - Auth;
   - Firestore como base de datos principal;
   - Realtime Database para la presencia y el «escribiendo…» del chat;
   - Cloud Functions (TypeScript, 2.ª gen., `europe-west1`) como servidor del dominio;
   - Storage, FCM, Remote Config, Analytics, Crashlytics, App Check y Hosting.
2. **El servidor sigue siendo la autoridad** (constitución I), ahora en `firebase/functions`:
   - toda escritura de clubes, horario, convocatorias, partidos, ratings y ausencias pasa por funciones callable o programadas;
   - las reglas de Firestore solo permiten leer a los miembros;
   - el cliente escribe directamente solo su perfil, sus dispositivos y los mensajes de chat, validados por las reglas.
3. **Las reglas de negocio no cambian.** `specs/product/business-rules.md` (BE-NNN RN-x) sigue siendo la referencia que cumplen las dos implementaciones. Las Functions portan la lógica pura del backend (`TeamBalancer`, `PlayerRatingCalculator`, `EnrollmentAllocator`, `MatchCalendar`, `DeadlineRule`, `ClubRolePolicy`) con sus tests.
4. **Dos implementaciones de la capa de datos detrás de las mismas interfaces del dominio.** Se elige al compilar con `SQUADFY_BACKEND=firebase|api`; por defecto, `firebase`.
   - La implementación `api` (Ktor, Room, `Squadfy_Backend`) queda **congelada**: se compila y se testea en el CI, pero no recibe funcionalidades nuevas.
   - `Squadfy_Backend` no se modifica.
5. **Errores:** las Functions devuelven en `details.code` los mismos `BackendErrorCode` que el backend propio, así que la presentación no distingue entre backends.
6. **Caché (modifica ADR-0006 para la variante Firebase):** se usa la caché offline de Firestore con listeners en tiempo real, sin Room. La variante `api` mantiene ADR-0006 tal cual.

## Consecuencias
- ➕ No hay servidores ni colas que operar. El tiempo real (listeners) sustituye al WebSocket y a `CLUB_DATA_CHANGED`. Push, Crashlytics y Remote Config son nativos.
- ➕ La UI, el dominio y sus tests no cambian; solo se añade una implementación de datos.
- ➖ La lógica de servidor existe dos veces: Kotlin (congelada) y TypeScript (viva). Si algún día se reactiva el backend propio, habrá que ponerlo al día.
- ➖ Firestore no tiene joins ni agregaciones flexibles. Estadísticas y clasificaciones se calculan en una función a partir de los partidos cerrados, y la búsqueda de usuarios es por prefijo.
- ➖ Requiere el plan **Blaze** (pago por uso) para Functions, Storage y Cloud Scheduler. A escala de lanzamiento se espera estar dentro del nivel gratuito; se vigila con alertas de presupuesto.
- ➖ Las versiones quedan atadas a GitLive: Firebase iOS SDK 11.x y la BoM de Android con la que se compila GitLive.
