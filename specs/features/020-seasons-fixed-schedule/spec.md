# 020 · Temporadas, horario fijo con excepciones y nuevo selector de hora

- **Estado:** Draft
- **Owner:** Enrique Palma
- **Reglas:** BE-002 (horario), BE-008 RN-B2 (excepciones), BE-005 (ratings y estadísticas); nuevas `APP-RN-S1…S6` (temporadas) y `APP-RN-H1…H3` (horario)
- **ADRs:** ADR-0010 (Firebase es el backend de lanzamiento)
- **Repos afectados:** Squadfy_App (app y `firebase/`). El backend propio está congelado: allí no habrá temporadas (gap, §Contrato).

## Problema / objetivo
- Un club amateur juega a lo largo de una **temporada** (septiembre a junio). La clasificación de este año no debe mezclarse con la del anterior.
- El **horario es el del club**: el mismo día y la misma hora todas las semanas. A veces una semana no hay partido o se juega otro día, y hay que poder indicarlo con su motivo.
- El selector de hora actual es el reloj estándar de Material, poco claro para elegir la hora del partido.

## Fuera de alcance
- Temporadas en el backend propio (variante `api`, congelada).
- Varios horarios por club (dos partidos fijos por semana).
- Copas, jornadas o calendarios de liga.

## Historias de usuario
- **US-020-01** Como owner o admin quiero que el club tenga una temporada con inicio y fin, para que la clasificación se reinicie cada año.
- **US-020-02** Como miembro quiero ver la clasificación de la temporada actual y la de temporadas anteriores.
- **US-020-03** Como owner o admin quiero cancelar o mover el partido de una semana concreta indicando el motivo, sin tocar el horario del club.
- **US-020-04** Como miembro quiero saber por qué una semana no hay partido o se juega en otro momento.
- **US-020-05** Como owner o admin quiero elegir la hora del partido de forma rápida y clara.

## Reglas
**Temporadas**
- **APP-RN-S1** Cada club tiene temporadas. Por defecto van del **1 de septiembre al 30 de junio** del año siguiente, y se nombran «2026/27». Solo una puede estar en curso (`ACTIVE`).
- **APP-RN-S2** Al crear un club se crea su temporada:
  - entre septiembre y junio, la que está en curso;
  - en julio y agosto, la siguiente, que empezará el 1 de septiembre.
- **APP-RN-S3** El owner y los admins pueden cambiar el inicio y el fin de la temporada en curso o de la siguiente. Condiciones:
  - el inicio va antes que el fin, y dura como mucho 12 meses;
  - no se solapa con otra temporada;
  - las temporadas cerradas no se tocan.
- **APP-RN-S4** La clasificación es **por temporada**:
  - el rating Elo empieza en 1000 en cada temporada;
  - las estadísticas (PJ, goles, asistencias, victorias, minutos…) cuentan solo los partidos completados dentro de las fechas de la temporada.
- **APP-RN-S5** **Renovación automática**:
  - cuando pasa el fin de la temporada en curso, se cierra (`CLOSED`) y su clasificación queda congelada;
  - se crea la siguiente con las mismas fechas un año después (`UPCOMING`), que pasa a `ACTIVE` el día de su inicio.
- **APP-RN-S6** Las temporadas cerradas se pueden consultar en la Tabla con un selector de temporada.

**Horario**
- **APP-RN-H1** El horario semanal es único por club y vale para todas las semanas: día, hora, formato, duración, cierre y sorteo (es lo de hoy).
- **APP-RN-H2** El owner y los admins pueden **cancelar** el partido de una semana o **moverlo** a otro día u hora de esa semana. El **motivo es obligatorio** (hoy es opcional). Las excepciones dejan de depender de la flag `SCHEDULE_EXCEPTIONS`, que se elimina.
- **APP-RN-H3** Los miembros ven el motivo en la pestaña Partido («Esta semana no hay partido: Festivo», «Esta semana se juega el jueves 20:00: campo ocupado»). Además reciben un push si la semana ya tenía la convocatoria abierta.

## Criterios de aceptación
- **AC-020-01** *Given* un club nuevo creado en octubre *When* se crea *Then* tiene la temporada «2026/27», del 1/9/2026 al 30/6/2027, en curso.
- **AC-020-02** *Given* un club nuevo creado en julio *When* se crea *Then* su temporada es la siguiente, aún no empezada, y la Tabla dice «La temporada empieza el 1 de septiembre».
- **AC-020-03** *Given* el owner *When* cambia el fin de la temporada en curso a una fecha válida *Then* se guarda; con fechas solapadas o al revés, ve el error y no se guarda. Un jugador no puede cambiarlas.
- **AC-020-04** *Given* partidos completados en dos temporadas *When* un miembro abre la Tabla *Then*:
  - ve la clasificación de la temporada en curso;
  - el Elo de esa temporada parte de 1000;
  - puede elegir la anterior y ve sus datos congelados.
- **AC-020-05** *Given* la temporada en curso con fin el 30/6 *When* el job diario corre el 1/7 *Then* la cierra y crea «2027/28» (1/9/2027 al 30/6/2028). Si se ejecuta dos veces, no se duplica nada.
- **AC-020-06** *Given* el horario de los miércoles *When* el owner cancela la semana del 4/11 con motivo «Festivo» *Then*:
  - no hay partido esa semana;
  - los miembros ven el motivo;
  - el horario no cambia, y la semana siguiente se planifica con normalidad.
- **AC-020-07** *Given* una semana con partido *When* el owner la mueve al jueves 20:00 con motivo *Then* el partido y su convocatoria pasan a esa hora, y los apuntados siguen apuntados.
- **AC-020-08** *Given* el formulario de excepción *When* el motivo está vacío *Then* no se puede guardar. El servidor también lo rechaza con `VALIDATION_ERROR`.
- **AC-020-09** *Given* PRO *When* el owner abre el horario *Then* ve la sección de excepciones (sin flag).
- **AC-020-10** *Given* el selector de hora *When* el owner elige la hora del partido *Then*:
  - ve la hora en grande, con la tipografía de cifras de la marca;
  - puede cambiar horas y minutos con pasos de 15 minutos o tocando los números;
  - tiene accesos rápidos a las horas habituales (18:00, 19:00, 20:00, 21:00, 22:00);
  - confirma con un único CTA.
  
  Sirve para la hora del partido, la del cierre, la del sorteo y la de una semana movida. Funciona con lector de pantalla.

## Casos límite y errores
| Situación | Comportamiento esperado | Código |
|---|---|---|
| Partido completado en julio, fuera de temporada | Sale en el historial; no cuenta en ninguna clasificación (ver D-19) | — |
| Reabrir un partido de una temporada cerrada | No se permite | `FAILED_PRECONDITION` (409) |
| Cambiar fechas y que un partido completado quede fuera | No se permite si hay partidos completados que cambiarían de temporada | `CONFLICT` (409) |
| Cancelar una semana que ya está jugada (partido completado) | No se permite | `CONFLICT` (409) |
| Dos excepciones para la misma semana | La nueva sustituye a la anterior | — |

## UX
- **Tabla:**
  - un selector de temporada arriba («2026/27 · en curso», «2025/26»);
  - en pretemporada, el estado vacío «La temporada empieza el 1 de septiembre».
- **Ajustes del club** (owner y admin): la sección «Temporada», con las fechas, el estado y la próxima renovación.
- **Horario:**
  - la sección «Excepciones» siempre visible para quien gestiona el club;
  - el formulario lleva el tipo (cancelar o mover), la semana, la nueva hora si se mueve y el **motivo obligatorio**.
- **Partido:** si la semana actual está cancelada, una tarjeta con el motivo y la fecha del siguiente partido. Hoy, en ese caso, sale «No hay partido programado».
- **Selector de hora:**
  - una hoja inferior con la hora en grande (`stat`, Bricolage);
  - botones − y + de 15 minutos;
  - las horas y los minutos se pueden tocar para editarlos con una rejilla de números;
  - chips de horas habituales y un CTA «Usar 20:00».
  
  Con temas Tiza y Noche, y sin textos escritos a mano.

## Contrato de API (Firebase, `specs/contracts/firebase.md`)
- **Firestore:**
  - `clubs/{clubId}/seasons/{seasonId}` con `{name, startsOn, endsOn, status}`;
  - los ratings pasan a `clubs/{clubId}/seasons/{seasonId}/ratings/{memberId}`.
- **Callables:**
  - nuevas: `getSeasons`, `updateSeason`;
  - con `seasonId` opcional (por defecto, la temporada en curso): `getRatings`, `getStats`, `getMyRating` y `getMyStats`;
  - `addScheduleException` exige `reason`.
- **Job diario:** `rollSeasons`.
- **Migración:** los ratings y estadísticas actuales pasan a la temporada 2026/27, porque todos los partidos jugados son de este curso.
- **Gap:** el backend propio no tiene temporadas. La variante `api` sigue mostrando la clasificación histórica completa.

## Preguntas abiertas
- ❓ **D-18** Si el Elo vuelve a 1000 cada temporada, los primeros sorteos de septiembre quedan desequilibrados.
  - **Propuesta:** el sorteo usa el Elo de la temporada en curso. Mientras un jugador tenga menos de 3 partidos en ella, usa su Elo final de la temporada anterior.
- ❓ **D-19** Los partidos jugados fuera de temporada (julio y agosto), ¿cuentan?
  - **Propuesta:** son amistosos. Salen en el historial pero no en la clasificación.
- ❓ **D-20** Al mover una semana, ¿puede pasar a otra semana o solo a otro día de la misma?
  - **Propuesta:** solo dentro de la misma semana (de lunes a domingo), para no pisar la siguiente.

## Checklist de verificación
- [ ] Todos los AC tienen un test que los cita (Functions con Vitest y emulador; la app con kotlin.test)
- [ ] `./gradlew testDebugUnitTest`, `npm test` y `npm run test:emulator` en verde
- [ ] Demo manual en Android e iOS: temporada, Tabla por temporada, cancelar y mover una semana, y el selector de hora
