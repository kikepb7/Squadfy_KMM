# Glosario (lenguaje ubicuo)

Los nombres de la columna *Código* son los del backend v1 (`BACKEND.md` §9–10). La app usa los mismos.

| Término | Código | Definición |
|---|---|---|
| Club | `Club` / `ClubDto` | Grupo que juega junto. Tiene owner, código de invitación y `maxMembers` opcional. |
| Miembro (ficha) | `ClubMember`, id `clubMemberId` | Usuario dentro de **un** club: rol, dorsal y posición. Los equipos, convocatorias, ratings y estadísticas usan `clubMemberId`, no `userId`. |
| Usuario | `userId` | Identidad global (auth y chat). |
| Rol | `ClubMemberRole` | `OWNER`, `ADMIN`, `CAPTAIN` y `PLAYER`. |
| Gestor | — | Un miembro `OWNER` o `ADMIN`. |
| Veto | `ban` | Expulsión que impide volver a unirse hasta que se levanta. |
| Transferir la propiedad | `transfer-ownership` | El owner cede el rol `OWNER` y pasa a `ADMIN`. |
| Posición | `PlayerPosition` | `GOALKEEPER`, `DEFENDER`, `MIDFIELDER` o `FORWARD`. Es opcional. |
| Horario | `ClubMatchSchedule` | Día de la semana, hora local, `timeZone`, `format`, duración e `isActive`. Uno por club. |
| Formato | `MatchFormat` | `FIVE_A_SIDE` (10 plazas), `SEVEN_A_SIDE` (14) y `ELEVEN_A_SIDE` (22). Determina `maxPlayers`. |
| Partido | `Match` | Partido programado (automático o extra). Estados: `SCHEDULED`, `COMPLETED` y `CANCELLED`. |
| Convocatoria | `MatchAnnouncement` | Ventana `[opensAt, closesAt)` de inscripción a un partido. Estados: `OPEN`, `CLOSED` y `CANCELLED`. |
| Convocatoria vigente | `CurrentMatchAnnouncement` | La del próximo partido, más mi estado (`myStatus`) y mi posición en la lista de espera. Es la pantalla principal del club. |
| Inscripción | `MatchAnnouncementEntry` | Estado `CONFIRMED` o `WAITLISTED`. |
| Lista de espera | `waitlist` | Inscripciones por encima del cupo, en orden de llegada. Se promocionan solas. |
| Equipos | `teamA` / `teamB` | Listas de `clubMemberId` (más `teamAGuests`/`teamBGuests` para invitados). Se publican solas a la hora del sorteo (`drawAt`). |
| Rectificar | `POST /teams` `AUTO`/`MANUAL` | El gestor repite el sorteo o fija los equipos a mano. |
| Equilibrio | `TeamBalance` | Rating medio y total de cada equipo y resultado esperado de A (0–1). Solo para gestores. |
| Rating | `rating` | Nivel Elo automático por club (empieza en 1000). Es provisional hasta 10 partidos y se muestra en una clasificación pública. |
| Evento | `MatchEvent` | `GOAL`, `ASSIST`, `YELLOW_CARD` o `RED_CARD`, con minuto opcional. |
| Minutos | `minutesPlayed` | Por defecto, la duración del partido; el gestor puede ajustarlos. |
| Cerrar / reabrir partido | `complete` / `reopen` | Cerrar fija el resultado y actualiza el rating. Reabrir solo se permite en el último partido cerrado. |
| Estadísticas | `ClubStatsEntry` | PJ, V, E, D, goles, asistencias, tarjetas y minutos, derivados de los partidos cerrados. |
| Invitado | `MatchGuest`, id `guestId` | Jugador sin cuenta que un miembro añade a una convocatoria (máximo 2). Ocupa plaza solo si sobra (los miembros tienen prioridad) y juega el sorteo con un nivel de 1000. |
| Excepción | `ScheduleException` | Una semana del horario `CANCELLED` (sin partido) o `RESCHEDULED` (movida a `newScheduledAt`). |
| Ausencia | `MemberAbsence` | Periodo en el que un miembro no estará. Lo desapunta de las convocatorias abiertas y le silencia los avisos de ese periodo. |
| Hora de cierre / de sorteo | `closeDaysBefore`+`closeTime` / `drawDaysBefore`+`drawTime` | Cuándo se cierra la convocatoria (`closesAt`) y cuándo se publican los equipos (`drawAt`). |
| Marcador oficial | `teamAScore`/`teamBScore`, `isManualScore` | El manual si el gestor lo fijó; si no, la suma de los goles registrados. |
| Valoración del partido | `ratingChanges` | Variación del rating de cada miembro en un partido cerrado. |
| Silenciar club | `notification-settings.muted` | Bloquea las push del club, salvo la de «tienes plaza». |
