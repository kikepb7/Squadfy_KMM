# 007 · Gestión del partido: eventos, minutos, cerrar, reabrir, cancelar y partido extra

- **Estado:** In progress (implementada en `app-parity-feature`; falta el E2E)
- **Reglas:** BE-004 RN-1…6, BE-008 RN-E, BE-002 RN-8/9/10, APP-RN-04, APP-RN-10
- **Backend:** BE-004, `BACKEND.md` §7.5 y §8.3
- **Depende de:** 006
- **Repos afectados:** Squadfy_App

## Problema / objetivo
El resultado de un partido es lo que alimenta el rating y las estadísticas. El backend trabaja con **eventos** (gol, asistencia, tarjetas) y **minutos**: el marcador se deriva de los goles, y el partido se cierra o se reabre.

La app, en cambio:
- tiene contadores que solo suman;
- tiene un diálogo de marcador libre;
- fija los minutos en 90.

## Criterios de aceptación
**Eventos y minutos (solo gestores, partido `SCHEDULED`)**
- **AC-007-01** En el detalle, el modo «Acta» lista los jugadores de cada equipo con botones para añadir ⚽ gol, 🅰️ asistencia, 🟨 o 🟥 (minuto opcional entre 1 y 120). Se llama a `POST /matches/{id}/events` (201) y la pantalla se actualiza con el `MatchDto` devuelto.
- **AC-007-02** La lista cronológica de eventos permite **borrar** uno (`DELETE /events/{eventId}`) con deshacer: el snackbar «Deshacer» vuelve a crear el evento.
- **AC-007-03** El marcador se muestra siempre como la suma de los goles por equipo (APP-RN-10). No hay campos para escribirlo.
- **AC-007-04** Minutos: cada jugador muestra los minutos efectivos (`minutesPlayed[id]`, o la duración si no hay ajuste) y un editor de 0 a la duración → `PUT /players/{memberId}/minutes`. Un valor fuera de rango se valida en local y, si llega al servidor, el 400 se muestra.

**Ciclo**
- **AC-007-05** «Cerrar partido» (`POST /complete`) solo se habilita si el estado es `SCHEDULED`, `now ≥ scheduledAt` y hay equipos. Pide confirmación con el resumen «A 3 – 2 B · los ratings se actualizarán». Al terminar, el partido queda `COMPLETED` y se invalidan los rankings (spec 008).
- **AC-007-06** «Reabrir» (`POST /reopen`) solo se ofrece en el **último** partido cerrado del club, comparando con `GET /clubs/{id}/matches?status=COMPLETED`. Avisa de que se revertirán los ratings.
- **AC-007-07** «Cancelar partido» (`POST /cancel`), con confirmación: «Se avisará a los apuntados». No se ofrece en `COMPLETED`.
- **AC-007-08** «Partido extra» (gestores, en la pestaña Partido): fecha y hora futuras en la zona del club, formato y duración opcionales → `POST /clubs/{id}/matches` (201). Sustituye al botón «partido de prueba».
- **AC-007-09** El partido `COMPLETED` es de solo lectura para todos: marcador, goleadores, asistentes, tarjetas y minutos.

**Limpieza**
- **AC-007-10** **Marcador manual** (flag `MANUAL_SCORE`, BE-008 RN-E):
  - con el partido en `SCHEDULED`, el gestor ve «Fijar marcador» (dos steppers de 0 a 99) → `PUT /matches/{id}/score`;
  - con `isManualScore`, el marcador muestra la etiqueta «Manual» y el botón «Quitar marcador manual» (`DELETE /score`, vuelve al marcador de los goles);
  - se avisa de que los goles registrados solo cuentan para cada goleador.

  `RecordMatchResultUseCase` (ruta antigua) se sustituye por `SetManualScoreUseCase` y `ClearManualScoreUseCase`. Se elimina el contador +G, +A, +TA, +TR (lo sustituye el acta por eventos).

## Decisiones de implementación
- **Partido extra y `DEV_TEST_MATCH`** (concilia AC-005 «partido de prueba» con AC-007-08): el «Partido extra» es una función de gestores, sin flag. `DEV_TEST_MATCH` (solo PRE) añade al formulario el atajo «Prueba: dentro de 15 min», que rellena fecha y hora.
- **Marcador**: se muestra el oficial que devuelve el backend (`teamAScore`/`teamBScore`): suma de goles o, con `isManualScore`, el manual (BE-008 RN-E2).
- **Rating del partido**: con el partido `COMPLETED`, cada jugador muestra su `ratingChanges[id]` («+12»). Las clasificaciones completas llegan en la 008.
- **Invalidar rankings**: la 008 es network-first y se recarga al abrirse, así que no hace falta una invalidación explícita.

