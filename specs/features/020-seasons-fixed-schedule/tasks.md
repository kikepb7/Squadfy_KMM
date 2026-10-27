# 020 · Tareas

## Fase 1 · Servidor (Functions)
- [x] **T-001** Dominio puro de temporadas (`domain/season.ts`): temporada inicial, estado por fechas, siguiente temporada, validación septiembre/junio, temporada de un partido y Elo del sorteo (D-18). Verificación: `season.test.ts` (AC-020-01, 02, 03, 05; D-18, D-19).
- [x] **T-002** Temporadas en Firestore (`match/seasons.ts`): `ensureSeasons` (crea la primera al crear el club, migra los ratings antiguos, cierra y abre por fechas), callables `getSeasons` y `updateSeason`, job diario `rollSeasons` (también en `runJobsNow`). Verificación: `seasons.test.ts` contra el emulador (AC-020-01…05).
- [x] **T-003** Elo y estadísticas por temporada: `completeMatch` guarda en la temporada del partido (los amistosos, sin Elo), `reopenMatch` no reabre temporadas cerradas, sorteo y equilibrio con D-18, `getRatings`/`getStats`/`getMyRating`/`getMyStats` con `seasonId`. Verificación: `seasons.test.ts` y `cycle.test.ts`.
- [x] **T-004** Excepciones: motivo obligatorio, mover solo dentro de la semana (D-20), una excepción nueva sustituye a la anterior y el push lleva el motivo; si la convocatoria ya estaba abierta, avisa a todos los miembros (APP-RN-H3). Verificación: `seasons.test.ts` (AC-020-08) y `cycle.test.ts`.

## Fase 2 · App
- [x] **T-005** Dominio y datos: `SeasonModel`, `SeasonRepository` (Firebase con `getSeasons`/`updateSeason`; la variante `api` sin temporadas), `seasonId` en clasificación y estadísticas, `ClubDataScope.SEASON`.
- [x] **T-006** Selector de hora nuevo en el design system (`TriTimePickerSheet`): hora en grande, ± 15 min, rejilla de horas y minutos, horas habituales y un único CTA; accesible con TalkBack y VoiceOver (AC-020-10). Sustituye al reloj de Material en Horario, Partido extra y Excepciones.
- [x] **T-007** Excepciones siempre visibles para quien gestiona el club (fuera la flag `SCHEDULE_EXCEPTIONS`), con motivo obligatorio y la semana movida dentro de su semana (AC-020-06…09).
- [x] **T-008** Tabla por temporada: selector de temporada y estado de pretemporada (AC-020-04, 02).
- [x] **T-009** Ajustes del club: sección «Temporada» con el día de inicio (septiembre) y de fin (junio) (AC-020-03).
- [x] **T-010** Partido: tarjeta de semana cancelada o movida con el motivo (APP-RN-H3).

> T-005…T-010: verificadas con el CI del 2026-10-27 (run 10, `workflow_dispatch`): compilación y tests unitarios de todos los módulos, compilación iOS, Functions y emulador en verde. Falta la demo manual (T-011).

## Fase 3 · Verificación
- [ ] **T-011** `./gradlew testDebugUnitTest` y demo manual en Android e iOS.
