# 005 · Tareas

## Fase 1 · Dominio
- [x] **T-001** Modelos y enums de convocatoria (con entradas de invitado de BE-008). `Cached<T>` no hizo falta: el ViewModel conserva el último valor y la marca de obsoleto.
- [x] **T-002** `AnnouncementWindowPolicy` (estado, cuenta atrás, mi estado tras una acción, invitados por miembro) + test con la tabla de ejemplos, incluida la semana del cambio de hora (AC-005-03).
- [x] **T-003** `AnnouncementRepository` + use cases (vigente, historial, apuntarse, desapuntarse, añadir y quitar invitado).

## Fase 2 · Data
- [x] **T-004** DTOs + mappers + test con el fixture de `BACKEND.md` (AC-005-02/06/14).
- [x] **T-005** `KtorAnnouncementRepository` (404 → sin partido). La fusión tras una acción y la marca de obsoleto viven en el ViewModel (AC-005-05/09).

## Fase 3 · Presentación
- [x] **T-006** `AnnouncementViewModel`: estado, ticker ligado a la suscripción (no corre con la pantalla cerrada), acciones, errores tipados y refresco al cruzar la ventana + `AnnouncementViewModelTest` (AC-005-03/04/05/07/08).
- [x] **T-007** UI: la pestaña Partido pasa a ser la primera; cabecera en la zona del club, cuenta atrás, botón principal según mi estado, listas con nombres resueltos y «Exjugador», pull-to-refresh, refresco al volver a primer plano y marca de sin conexión (AC-005-02…09).
- [~] **T-008** Historial de convocatorias (AC-005-10): se carga y se mostrará con el detalle de partido de la spec 006, que es su destino al tocarlo.
- [x] **T-009** Invitados tras `MATCH_GUESTS` (activo en PRE) (AC-005-13); flujo legacy (signups, invitados antiguos, resultado) eliminado. El «partido de prueba» (`DEV_TEST_MATCH`) pasa a la spec 007 como partido extra.
- [x] **T-010** Strings ES/EN.

## Fase 4 · Verificación
- [ ] **T-011** E2E con el backend local: crear un horario 5v5 → la convocatoria se abre al momento → 11 usuarios se apuntan → el 11.º queda en espera → uno se desapunta → el de espera sube. Para comprobar el cierre, crear un partido extra cercano.
