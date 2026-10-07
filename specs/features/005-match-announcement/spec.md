# 005 · Convocatoria vigente: apuntarse, desapuntarse y lista de espera

- **Estado:** In progress (falta el historial navegable, que llega con la 006, y el E2E). Aprobada por el owner el 2026-10-07.
- **Reglas:** BE-002 RN-4…9, APP-RN-01, APP-RN-02, APP-RN-03, APP-RN-06, APP-RN-08
- **ADRs:** ADR-0006
- **Backend:** BE-002, BE-008 (RN-A, RN-C, RN-D), `BACKEND.md` §7.3, §8.4 y §9 (`CurrentMatchAnnouncementDto`)
- **Depende de:** 002, 003
- **Repos afectados:** Squadfy_App

## Problema / objetivo
Es la pantalla que cada miembro abre cada semana: **¿hay partido, cuándo es, estoy dentro y cuántas plazas quedan?**

Hoy la pestaña Partido:
- elige el partido comparando strings;
- usa endpoints inexistentes (signups e invitados);
- no conoce la lista de espera ni la ventana;
- muestra un «partido de prueba».

## Historias de usuario
- **US-005-01** Como miembro quiero ver el próximo partido y la convocatoria con una cuenta atrás hasta el cierre, y apuntarme o desapuntarme con un toque.
- **US-005-02** Como miembro quiero saber si estoy confirmado o en lista de espera (y en qué puesto), quién va y cuántas plazas quedan.
- **US-005-03** Como miembro quiero consultar las convocatorias anteriores.

## Criterios de aceptación
- **AC-005-01** La pestaña «Partido» (la primera del club) carga `GET /clubs/{id}/announcements/current`. Si responde 404, muestra el estado vacío: «No hay partido programado», más el resumen del horario o el CTA «Configurar horario» para los gestores.
- **AC-005-02** La cabecera muestra:
  - la fecha y hora del partido (`matchScheduledAt`), en la zona del club (APP-RN-03);
  - el formato;
  - un contador `confirmedCount / maxPlayers`;
  - `waitlistCount` si es mayor que 0.
- **AC-005-03** `AnnouncementWindowPolicy.state(announcement, now)` devuelve `NOT_OPEN`, `OPEN`, `CLOSED` o `CANCELLED` según APP-RN-01. La UI muestra, según el estado:
  - «Abre {fecha}»;
  - «Cierra en {d h min}», con una cuenta atrás que se refresca cada minuto;
  - «Convocatoria cerrada».

  Cuando la cuenta atrás llega a 0, la pantalla se refresca sola.
- **AC-005-04** El botón principal depende de `myStatus` y del estado:

  | `myStatus` | OPEN | Otros estados |
  |---|---|---|
  | `NOT_ENROLLED` | «Apuntarme» | deshabilitado + motivo |
  | `CONFIRMED` | «Desapuntarme» (con confirmación) + insignia «Convocado» | insignia, sin botón |
  | `WAITLISTED` | «Salir de la lista de espera» + «En espera · n.º {myWaitlistPosition}» | insignia |
- **AC-005-05** El resultado de `POST/DELETE /announcements/{id}/enrollment` actualiza la pantalla con el `MatchAnnouncementDto` que devuelve el servidor, sin esperar al refresco. Si apuntarse devuelve `WAITLISTED`, aparece un snackbar «Cupo completo: estás en lista de espera (n.º X)».
- **AC-005-06** Listas:
  - «Convocados» (`entries`, en orden de inscripción) y «Lista de espera» (`waitlist`, numerada);
  - cada fila muestra el nombre, la foto, el dorsal y la posición, resueltos desde los miembros (APP-RN-06);
  - mi fila va resaltada.
- **AC-005-07** Errores:
  - 400 `BAD_REQUEST` al apuntarse o desapuntarse muestra «La convocatoria no está abierta» y refresca;
  - 409 muestra «Ya estás apuntado» y refresca;
  - 403 `NOT_CLUB_MEMBER` lleva a la lista de clubes con «Ya no eres miembro de este club».
- **AC-005-08** Refresco según APP-RN-08:
  - al entrar;
  - con pull-to-refresh;
  - al volver a primer plano;
  - al recibir una push del club (spec 009).

  Cada acción tiene su propio indicador de carga.
- **AC-005-09** Sin red, se muestra la última convocatoria en memoria con la marca «Sin conexión · actualizado hace X» y los botones de acción deshabilitados.
- **AC-005-10** «Convocatorias anteriores»: `GET /clubs/{id}/announcements`, con el estado de cada una y el número de confirmados; al tocar una se abre el detalle del partido (spec 006).
- **AC-005-11** Funciones condicionadas:
  - El botón «Crear partido de prueba» solo aparece con el flag `DEV_TEST_MATCH` (activo en PRE, inactivo en PRO) y en v1 crea un partido extra (`POST /clubs/{id}/matches`).
  - Se eliminan la baja de inscripciones ajenas y los use cases `SignUpForMatchUseCase`, `CancelSignupUseCase`, `RemoveSignupUseCase` y `ListSignupsUseCase`, junto con `MatchSignupDTO`, que pasan a la API de convocatorias v1.
- **AC-005-13** **Invitados** (flag `MATCH_GUESTS`, BE-008 RN-A):
  - con la convocatoria abierta, cualquier miembro ve «Añadir invitado» (nombre ≤ 80 + chips de posición opcionales) → `POST /announcements/{id}/guests`;
  - el botón se oculta cuando ya tiene 2 invitados en esa convocatoria; un 409 muestra «Máximo 2 invitados»;
  - las filas de invitado muestran «{nombre} · invitado de {anfitrión}», con su estado (confirmado o en espera);
  - el anfitrión y los gestores ven «Quitar» → `DELETE /announcements/{id}/guests/{guestId}`, y un 403 muestra «No tienes permiso»;
  - la cabecera explica «Los miembros tienen prioridad sobre los invitados».
- **AC-005-14** La cabecera muestra también la **hora del sorteo** (`drawAt`) si es distinta del cierre: «Equipos el jueves a las 12:00».
- **AC-005-15** Si el usuario tiene una ausencia (spec 014) que cubre la fecha del partido, aparece el aviso «Tienes una ausencia ese día» sin bloquear «Apuntarme» (BE-008 RN-C4).
- **AC-005-12** Si la convocatoria está `CLOSED` y el partido ya tiene equipos, la pantalla muestra un acceso «Ver equipos» (spec 006).

## Tabla de ejemplos (normativa para `AnnouncementWindowPolicy`; sale de BE-002 CA-1/CA-8)
Club `Europe/Madrid`, partido los jueves a las 20:00.

| Partido | `opensAt` (UTC) | `closesAt` (UTC) | `matchScheduledAt` (UTC) |
|---|---|---|---|
| jue 2026-10-15 | 2026-10-08T22:00Z (vie 9, 00:00 en Madrid) | 2026-10-14T20:00Z (mié 14, 22:00) | 2026-10-15T18:00Z |
| jue 2026-10-29 (semana del cambio de hora) | 2026-10-22T22:00Z | 2026-10-28T21:00Z (22:00 CET) | 2026-10-29T19:00Z |

Estado con el partido del 15 y `status = OPEN`:

| `now` (UTC) | Estado |
|---|---|
| 2026-10-08T21:59Z | NOT_OPEN |
| 2026-10-08T22:00Z | OPEN |
| 2026-10-14T19:59Z | OPEN |
| 2026-10-14T20:00Z | CLOSED (aunque `status` siga `OPEN`) |
| `status = CANCELLED` | CANCELLED |
