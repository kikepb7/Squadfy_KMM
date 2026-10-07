# ADR-0002 · El ciclo de vida del partido lo gobierna el servidor

- **Estado:** Aceptado. **Implementado en el backend** (BE-002/BE-003: procesos idempotentes, ventana validada en cada petición, bloqueo en las inscripciones). En la app aplica el apartado 4, que el ADR-0006 matiza.
- **Fecha:** 2026-10-05

## Contexto
La convocatoria (apertura, cierre, aforo y lista de espera) y el sorteo afectan a varios usuarios a la vez y tienen que ocurrir **aunque nadie abra la app**. Hoy el backend depende de dos crons, a las 08:00 y a las 00:00 UTC:
- **No son idempotentes ni transaccionales**, y no tienen lock distribuido ni catch-up.
- **El enroll no comprueba las fechas.**

## Decisión
1. **Estado derivado del tiempo:**
   - `signupState` se calcula en cada lectura y en cada escritura comparando con `signupOpensAt` y `signupClosesAt`.
   - El cron no es responsable de abrir ni de cerrar, así que un cron que se pierde no rompe la regla.
2. **Jobs idempotentes con catch-up:**
   - `EnsureUpcomingMatchesJob`, cada 15 minutos: para cada calendario activo, garantiza que exista el partido de la siguiente fecha válida, con unicidad `(clubId, matchDate)`.
   - `AutoDrawJob`, cada 5 minutos: sortea los partidos con `signupClosesAt ≤ now`, `status = SCHEDULED` y sin sorteo.
   - Ambos usan **ShedLock** o `SELECT … FOR UPDATE SKIP LOCKED` para soportar varias instancias.
3. **Concurrencia en la inscripción:** se bloquea la fila del partido (`PESSIMISTIC_WRITE`) mientras se cuentan e insertan los `CONFIRMED`. Los `DataIntegrityViolationException` se mapean a 409.
4. **El cliente** calcula `signupState` en local con `kotlinx-datetime` (`SignupWindowPolicy` en `feature/club/domain`), solo para refrescar la UI (cuenta atrás, habilitar o deshabilitar botones). Siempre refresca desde el servidor después de cada acción.

## Consecuencias
- La lógica de ventana del cliente existe solo para la UX y se cubre con tests contra los mismos casos del servidor (tabla de ejemplos en la spec 003).
- El backend necesita Flyway (spec 008) para crear los índices únicos.
