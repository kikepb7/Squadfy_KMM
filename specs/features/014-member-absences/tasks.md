# 014 · Tareas

- [x] **T-001** `MemberAbsenceModel`, `AbsenceRepository`, use cases y validación local (desde ≤ hasta, hasta ≥ hoy, ≤ 1 año, motivo ≤ 200) + `AbsenceValidationTest` (AC-014-02/05).
- [x] **T-002** DTOs + `KtorAbsenceRepository` + `AbsenceDtoFixtureTest` (AC-014-01).
- [x] **T-003** `AbsencesViewModel` + `AbsencesScreen` (lista con nombres resueltos, alta con selector de fechas en la zona del club, borrado solo de las mías, aviso de desapuntado) + tests (AC-014-01…04).
- [x] **T-004** Fila «Ausencias» en Ajustes del club tras `MEMBER_ABSENCES`, y aviso «Tienes una ausencia ese día» en la convocatoria sin bloquear «Apuntarme» + test (AC-005-15, AC-014-05).
- [x] **T-005** `MEMBER_ABSENCES` (y `MANUAL_SCORE`, de la 007) pasan a estar activos en PRE; tests de flags ajustados (AC-014-06).
- [x] **T-006** Strings ES/EN.
- [x] **T-007** E2E: estar apuntado → añadir una ausencia que cubre el partido → la convocatoria me muestra desapuntado y con el aviso; borrarla → puedo volver a apuntarme. — **Hecho 2026-10-08**: la ausencia que cubre el sábado me desapunta, sube el de la espera y la convocatoria muestra el aviso sin bloquear «Apuntarme».
