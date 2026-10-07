# 006 · Tareas

## Fase 1 · Dominio
- [x] **T-001** `MatchModel` (equipos con invitados, minutos, eventos, marcador manual, `ratingChanges`, `scheduleDate`), `TeamBalanceModel` y enums `MatchStatus`/`MatchEventType`/`Team`. No había `ClubMatchModel` ni `MatchParticipantModel` que retirar: se eliminaron con el flujo legacy en la 005 (AC-006-07).
- [x] **T-002** `MatchRepository` + use cases; `GenerateTeamsUseCase.auto/manual` ya no fija el modo y valida el reparto (AC-006-05/07).
- [x] **T-003** `MatchTeamsPolicy` (rectificable, texto sin equipos, reparto inicial y movimiento) + `MatchTeamsPolicyTest` (AC-006-02/05/06/08/09).

## Fase 2 · Data
- [x] **T-004** DTOs + mappers (eventos en orden cronológico) + `MatchDtoFixtureTest` (AC-006-03/07/08).
- [x] **T-005** `KtorMatchRepository` (409 en el equilibrio → sin panel). Incluye las rutas de la 007 para no reabrir el repositorio.

## Fase 3 · Presentación
- [x] **T-006** `MatchDetailViewModel`: refresco network-first, equilibrio solo para gestores, repetir sorteo con confirmación, ajuste manual con validación local y 400/409 → mensaje y recarga + `MatchDetailViewModelTest` (AC-006-01…09).
- [x] **T-007** `MatchDetailScreen` + ruta `MatchDetailRoute(clubId, matchId)`, abierta desde «Ver partido» y el historial de la convocatoria (AC-005-10/12). Equipos por posición con dorsal, mi equipo resaltado, invitados con su etiqueta, panel de equilibrio y resumen de posiciones al editar. El formato sale del horario porque `MatchDto` no lo incluye.
- [x] **T-008** Strings ES/EN.

## Fase 4 · Verificación
- [x] **T-009** E2E con el backend local: cerrar la convocatoria → los equipos aparecen al llegar `drawAt` → un gestor repite el sorteo y mueve un invitado → el equilibrio se actualiza; un jugador no ve el panel. — **Hecho 2026-10-08**: equipos tras el sorteo AUTO, «Juegas en el B», equilibrio con ratings y apertura desde la push `match.teams.published`.
