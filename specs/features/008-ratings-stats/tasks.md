# 008 · Tareas

## Fase 1 · Dominio
- [x] **T-001** Modelos `RatingEntry`, `MyRating`, `PlayerStats`, `StatsEntry`, `StatsSortBy` y `MatchRatingChange` + `StandingsRepository` y use cases.
- [x] **T-002** `GetRecentRatingChangesUseCase` (variación de rating en los últimos partidos cerrados) + test (AC-008-08).

## Fase 2 · Data
- [x] **T-003** DTOs + mappers + `KtorStandingsRepository` + `StandingsDtoFixtureTest` (AC-008-02/03/06).

## Fase 3 · Presentación
- [x] **T-004** `StandingsViewModel` + `StandingsTab`: selector Rating/Estadísticas, «Tu posición», «Provisional», mi fila resaltada, chips de orden, tabla con scroll horizontal y estado vacío. Sustituye a la tabla de plantilla provisional (AC-008-01…03/06/07).
- [x] **T-005** `MemberStatsViewModel` + sección en la ficha del miembro: si es la mía, `/me`; si es de otro, sus filas de las clasificaciones; y la variación de sus últimos partidos (AC-008-04/08).
- [x] **T-006** Detalle del partido: el rating del partido en verde o rojo por jugador (AC-008-08) + tests de los ViewModels.
- [x] **T-007** Strings ES/EN; se retiran los de la plantilla provisional.

## Fase 4 · Verificación
- [ ] **T-008** E2E con el backend local: cerrar dos partidos → los ratings y las estadísticas se actualizan; reabrir el último → vuelven al valor anterior.
