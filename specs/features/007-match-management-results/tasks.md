# 007 · Tareas

## Fase 1 · Dominio
- [x] **T-001** Use cases de eventos, minutos, ciclo, partido extra y marcador manual (ya en `MatchUseCases` desde la 006). `RecordMatchResultUseCase` y los contadores +G/+A/+TA/+TR ya no existían (AC-007-10).
- [x] **T-002** `MatchCyclePolicy` (registrar, cerrar, reabrir, cancelar, minutos válidos) + `MatchCyclePolicyTest` (AC-007-04…07/09).
- [x] **T-003** `CreateExtraMatchUseCase.validate` (fecha futura, duración 10–180) + test (AC-007-08).

## Fase 2 · Data
- [x] **T-004** Rutas en `KtorMatchRepository` (hechas en la 006); `MatchDtoFixtureTest` ya cubre eventos, minutos y `isManualScore`.

## Fase 3 · Presentación
- [x] **T-005** `MatchDetailViewModel`: modo acta, añadir y borrar eventos con deshacer, minutos validados en local, cerrar/reabrir/cancelar con confirmación, último partido cerrado vía `?status=COMPLETED` y marcador manual tras `MANUAL_SCORE` + tests (AC-007-01…07/10).
- [x] **T-006** UI del detalle: marcador oficial con la etiqueta «Manual», acta cronológica, controles por jugador (solo miembros: los invitados no tienen estadísticas), minutos y rating del partido en el `COMPLETED` de solo lectura (AC-007-03/09).
- [x] **T-007** «Partido extra» en la pestaña Partido para gestores (`ExtraMatchViewModel` + diálogo), con el atajo de prueba tras `DEV_TEST_MATCH` + `ExtraMatchViewModelTest` (AC-007-08).
- [x] **T-008** Selectores de fecha y hora compartidos (`components/Pickers.kt`).
- [x] **T-009** Strings ES/EN.

## Fase 4 · Verificación
- [~] **T-010** E2E con el backend local: crear un partido extra dentro de 15 min → apuntarse → sorteo → registrar goles y minutos → cerrar (los ratings cambian) → reabrir → cancelar otro partido. — **2026-10-08**: acta (gol con minuto), marcador y acciones de gestor verificados. Cerrar/reabrir pendiente: `POST /clubs/{id}/matches` (partido extra) da 500 en el backend (NPE en `MatchPlanningService` por la llamada `$default` que esquiva el proxy de Spring) y el partido semanal aún no ha empezado.
