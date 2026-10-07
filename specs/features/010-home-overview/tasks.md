# 010 · Tareas

- [x] **T-001** Quitar de `globalPosition` el club duplicado (modelo, DTO, mappers, use cases y sincronización con Room). La presentación pasa a depender de `feature/club/domain`.
- [x] **T-002** `HomeClubCardModel` + orden de las tarjetas (`sortedForHome`) + `HomeCardOrderingTest` (AC-010-01/02/03).
- [x] **T-003** `GlobalPositionViewModel`: convocatoria vigente y horario por club (como mucho 4 en paralelo), error aislado por tarjeta con reintento, «Apuntarme» y refresco (AC-010-01/02/06/07).
- [x] **T-004** `HomeClubCard` (próximo partido en la zona del club, estado de la ventana, mi estado y plazas libres), estado vacío, pull-to-refresh y refresco al volver a primer plano.
- [x] **T-005** Strings ES/EN (se añade `values-en` al módulo).
- [~] **T-006** Mocks de partidos y noticias: siguen tras sus flags (apagados en PRO). Falta la decisión de borrarlos (AC-010-04).
- [ ] **T-007** E2E: dos clubes, uno con la convocatoria abierta → aparece primero con «Apuntarme» → apuntarse desde Inicio → pasa a «Convocado».
