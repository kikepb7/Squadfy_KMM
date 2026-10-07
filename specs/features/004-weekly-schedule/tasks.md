# 004 · Tareas

- [x] **T-001** Dominio: `ClubScheduleModel`, `MatchFormat`, `ScheduleDraft`, `ScheduleExceptionModel`, `ScheduleValidation` (duración, días antes, cierre < inicio, cierre ≤ sorteo < inicio, fecha de excepción en día de partido futuro). *Verificación:* `ScheduleValidationTest` (incluye BE-008 CA-9).
- [x] **T-002** `ScheduleRepository` + use cases (obtener, crear o actualizar según exista, excepciones).
- [x] **T-003** Datos: DTOs v1, mappers (`HH:mm:ss`; el cierre y el sorteo solo se envían con `CUSTOM_DRAW_TIME`), `KtorScheduleRepository` network-first con el último valor en memoria (ADR-0006 revisado). *Verificación:* `ScheduleDtoFixtureTest`.
- [x] **T-004** Pantalla Horario (Ajustes › Horario): resumen para todos, formulario para gestores (chips de día y formato, `TimePicker`, zona horaria validada, duración, activo con confirmación) (AC-004-01/02/03/05/07).
- [x] **T-005** Cierre y sorteo configurables tras `CUSTOM_DRAW_TIME` (AC-004-06).
- [x] **T-006** Semanas especiales tras `SCHEDULE_EXCEPTIONS`: lista, alta (`DatePicker` limitado a días de partido futuros; cancelar o mover a fecha y hora) y deshacer con confirmación (AC-004-08). *Verificación:* `ScheduleViewModelTest` (sin petición con el flag apagado, AC-013-08).
- [x] **T-007** Flags `CUSTOM_DRAW_TIME` y `SCHEDULE_EXCEPTIONS` activos en PRE para QA; en PRO siguen apagados hasta cerrar la spec.
- [ ] **T-008** E2E con el backend local: crear el horario (se planifica el primer partido), cambiar el sorteo, cancelar y mover una semana y deshacerlo.
