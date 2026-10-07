# ADR-0004 · Algoritmo de sorteo equilibrado

- **Estado:** Reemplazado por ADR-0005 (2026-10-07): el sorteo lo implementa el backend con un rating Elo (BE-003). El cliente no tiene algoritmo propio.
- **Fecha:** 2026-10-05

## Contexto
El sorteo actual es un `shuffled()` partido por la mitad: ignora la posición, el nivel y los porteros. El producto pide equipos «lo más parejos posible por posición e incluso por estadísticas».

## Decisión
Se usa un algoritmo **determinista dado una semilla**, en Kotlin puro y sin dependencias:

1. **Fuerza** (BR-052): `rating` (1–99) combinado con la forma de la temporada, `w ≤ 0.3` según los partidos jugados. Los invitados usan solo `rating`.
2. **Porteros:** se ordenan por fuerza. El 1.º va a A y el 2.º a B; los demás se tratan como jugadores de campo sin posición.
3. **Voraz por posición:** se recorren los grupos DEF, MID, FWD y SIN_POSICIÓN. Dentro de cada grupo se ordena por fuerza descendente, deshaciendo empates con la semilla. Cada jugador va al equipo que:
   1. tiene plaza (capacidad `⌈n/2⌉` y `⌊n/2⌋`; el equipo que recibe la plaza extra lo decide el equilibrio);
   2. tiene menos jugadores de esa posición;
   3. tiene menos fuerza total;
   4. sale de la moneda de la semilla.
4. **Búsqueda local:**
   - Se evalúan todos los intercambios `a∈A ↔ b∈B` del mismo grupo de posición y se aplica el que más reduce `|ΣA − ΣB|`.
   - Se repite hasta que no haya mejora, con un máximo de 200 iteraciones.
   - Se permite un intercambio entre posiciones distintas solo si mantiene `|#posA − #posB| ≤ 1`.
5. **Variedad sin perder equilibrio:** se ejecuta con K = 16 semillas derivadas de `seed`, y se elige con la semilla entre las soluciones cuya diferencia es ≤ `minDiff + 2`.
6. **Salida:** `teamA`, `teamB`, `strengthA`, `strengthB`, `seed` y `algorithmVersion = "balanced-v1"`.

**Dónde vive:**
- La implementación de referencia está en `feature/club/domain` (`BalancedTeamDrawer`, `commonMain`), con tests de propiedades e invariantes (BR-054).
- El backend, que también es Kotlin, incorpora **el mismo código**, copiado o publicado como artefacto `squadfy-domain-draw`, y comparte la batería de tests.
- El servidor es quien ejecuta el sorteo de forma autoritativa. El cliente usa la implementación solo para la *preview* del ajuste manual (mostrar `ΣA` y `ΣB` mientras el admin mueve jugadores).

## Alternativas descartadas
- **Fuerza bruta o ILP:** es óptima, pero `C(22,11) ≈ 705 k` combinaciones por cada restricción de posición resulta cara en el servidor, no da variedad y es difícil de explicar.
- **Snake draft simple:** no gestiona las posiciones y produce sesgos sistemáticos.

## Consecuencias
- El sorteo es reproducible y auditable a partir de la semilla y la versión.
- Los parámetros (pesos de la forma, K y tolerancia) quedan versionados en `algorithmVersion`.
