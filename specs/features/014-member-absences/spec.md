# 014 · Ausencias de jugadores

- **Estado:** Approved (el owner pidió el 2026-10-07 completar la funcionalidad mínima del MVP)
- **Reglas:** BE-008 RN-C1…C5, APP-RN-03, APP-RN-06
- **Backend:** BE-008 (implementada), `BACKEND.md` §7.3 y §8.2
- **Flag:** `MEMBER_ABSENCES`
- **Depende de:** 003 (miembros), 005 (convocatoria)
- **Repos afectados:** Squadfy_App

## Problema / objetivo
Un jugador que se va de vacaciones o está lesionado quiere avisar una sola vez y olvidarse: que no le lleguen avisos de convocatoria y que no ocupe plaza por error. Al resto del club también le interesa saber quién no estará.

## Historias de usuario
- **US-014-01** Como miembro quiero registrar un periodo de ausencia, con un motivo opcional, para no recibir convocatorias ni quedarme apuntado sin querer.
- **US-014-02** Como miembro quiero ver quién estará ausente en el club.

## Criterios de aceptación
- **AC-014-01** En el club, la sección «Ausencias» (Ajustes o Miembros) lista `GET /clubs/{id}/absences?from=hoy`: el miembro (nombre resuelto, APP-RN-06), el periodo y el motivo, ordenados por fecha de inicio.
- **AC-014-02** «Añadir ausencia» abre un selector de rango de fechas en la zona del club, más un motivo opcional (≤ 200).
  - Se valida en local que desde ≤ hasta, que hasta ≥ hoy y que el periodo dure como máximo 1 año.
  - Se llama a `POST /clubs/{id}/members/me/absences` (201).
  - Un 400 del servidor muestra su motivo genérico de validación.
- **AC-014-03** Al crear una ausencia aparece el aviso «Te hemos desapuntado de las convocatorias abiertas de esas fechas», y la convocatoria vigente se refresca (spec 005).
- **AC-014-04** Cada miembro puede borrar solo sus ausencias (`DELETE …/members/me/absences/{id}` → 204). Las de otros no muestran la acción.
- **AC-014-05** La convocatoria muestra «Tienes una ausencia ese día» si una ausencia propia cubre la fecha del partido, sin bloquear «Apuntarme» (BE-008 RN-C4).
- **AC-014-06** Todo queda tras el flag `MEMBER_ABSENCES`: activo en PRE al terminar la spec y en PRO cuando esté `Done`.

## Contrato de API
`GET /clubs/{clubId}/absences?from=&to=` · `POST /clubs/{clubId}/members/me/absences {fromDate, toDate, reason?}` · `DELETE /clubs/{clubId}/members/me/absences/{absenceId}` · `MemberAbsenceDto(id, clubId, clubMemberId, fromDate, toDate, reason?, createdAt)`.
