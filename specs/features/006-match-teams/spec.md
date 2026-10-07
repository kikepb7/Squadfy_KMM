# 006 · Detalle del partido, equipos y equilibrio

- **Estado:** In progress (implementada en `app-parity-feature`; falta el E2E)
- **Reglas:** BE-003 RN-1…10, BE-002 RN-7c, APP-RN-04, APP-RN-06, APP-RN-09
- **Backend:** BE-003, BE-008 (RN-A6, RN-D3), `BACKEND.md` §7.4, §8.3 (`/matches/{id}`, `/teams`, `/team-balance`)
- **Depende de:** 005
- **Repos afectados:** Squadfy_App

## Problema / objetivo
Al cerrarse la convocatoria, el backend **publica los equipos solo**, equilibrados por posición y rating Elo. La app tiene que:
- mostrarlos a todos;
- dejar que los gestores vean el equilibrio y rectifiquen, ya sea repitiendo el sorteo o moviendo jugadores a mano.

Hoy la app solo tiene un botón «Sortear» que fuerza `AUTO`, y espera un modelo de participantes distinto.

## Criterios de aceptación
- **AC-006-01** «Detalle del partido» (`GET /matches/{id}`), al que se llega desde la convocatoria (AC-005-12), el historial o una push, muestra:
  - fecha, formato, duración y estado;
  - Equipo A y Equipo B, con cada jugador resuelto (APP-RN-06) y agrupado por posición, con su dorsal;
  - mi equipo resaltado («Juegas en el A»).
- **AC-006-02** Sin equipos (`teamA` vacío): si la convocatoria sigue abierta, se muestra «Los equipos se publicarán al cerrar la convocatoria ({closesAt})»; si ya ha cerrado, «Equipos pendientes».
- **AC-006-03** Gestores, panel «Equilibrio» (`GET /matches/{id}/team-balance`):
  - rating medio y total de cada equipo;
  - una barra con `teamAExpectedScore` («A 52 % · B 48 %»);
  - el rating de cada jugador, de mayor a menor.

  Un 409 (sin equipos) oculta el panel. Los no gestores no lo ven y la app no lo pide (APP-RN-09).
- **AC-006-04** Gestores: «Repetir sorteo» (`POST /teams {mode: AUTO}`), con la confirmación «Los jugadores recibirán un aviso con su nuevo equipo».
- **AC-006-05** Gestores, «Ajuste manual»:
  - un modo de edición en el que cada jugador confirmado se puede pasar al otro equipo con un toque o arrastrándolo;
  - mientras se edita, se valida en local que los equipos sean disjuntos, no estén vacíos y su tamaño difiera ≤ 1;
  - al guardar se llama a `POST /teams {mode: MANUAL, manualTeamA, manualTeamB}` y se refresca el equilibrio.

  Mientras se edita **no** se recalcula el equilibrio en local, porque el rating está en el servidor y es la fuente de verdad. Lo que se muestra es la diferencia de jugadores por posición.
- **AC-006-06** Solo se puede rectificar en `SCHEDULED`. En `COMPLETED` o `CANCELLED` el detalle es de solo lectura. Un 400 o 409 del servidor muestra un mensaje específico y refresca.
- **AC-006-07** El modelo `Match` de domain (`id`, `clubId`, `scheduledAt`, `status: MatchStatus`, `teamA`/`teamB: List<ClubMemberId>`, `durationMinutes`, `minutesPlayed`, marcadores y eventos) sustituye a `ClubMatchModel` y `MatchParticipantModel`. `GenerateTeamsUseCase(mode, a, b)` ya no fija el modo.
- **AC-006-08** **Invitados en los equipos** (flag `MATCH_GUESTS`): `teamAGuests`/`teamBGuests` se muestran en su equipo con la etiqueta «Invitado». En el equilibrio, `isGuest = true` aparece como «Invitado · 1000». El ajuste manual permite mover invitados y envía su `guestId` junto a los `clubMemberId`.
- **AC-006-09** Sin equipos y con la convocatoria cerrada, el texto es «Los equipos se publicarán el {drawAt}» (BE-008 RN-D3).
