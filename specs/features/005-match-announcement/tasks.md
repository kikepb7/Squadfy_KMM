# 005 · Tareas

## Fase 1 · Dominio
- [ ] **T-001** Modelos y enums de convocatoria + `Cached<T>`.
- [ ] **T-002** `AnnouncementWindowPolicy` + test con la tabla de ejemplos (AC-005-03).
- [ ] **T-003** `AnnouncementRepository` (interfaz) + use cases `GetCurrentAnnouncement`, `Enroll`, `Withdraw` y `GetAnnouncementHistory`.

## Fase 2 · Data
- [ ] **T-004** DTOs + mappers + test con el fixture de `BACKEND.md` (AC-005-02/06).
- [ ] **T-005** `InMemoryAnnouncementRepository` con 404 → null, stale y fusión tras una acción + tests (AC-005-05/09).

## Fase 3 · Presentación
- [ ] **T-006** `AnnouncementViewModel` (estado, ticker, acciones y errores) + tests (AC-005-03/04/05/07/08).
- [ ] **T-007** UI: cabecera, cuenta atrás, botón principal, listas, pull-to-refresh y marca de sin conexión (AC-005-02…09).
- [ ] **T-008** Historial de convocatorias (AC-005-10).
- [ ] **T-009** Partido de prueba tras `DEV_TEST_MATCH`, invitados tras `MATCH_GUESTS` y eliminar los use cases de signups (AC-005-11).
- [ ] **T-010** Strings ES/EN.

## Fase 4 · Verificación
- [ ] **T-011** E2E con el backend local: crear un horario 5v5 → la convocatoria se abre al momento → 11 usuarios se apuntan → el 11.º queda en espera → uno se desapunta → el de espera sube. Para comprobar el cierre, crear un partido extra cercano.
