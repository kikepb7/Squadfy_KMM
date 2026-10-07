# 010 · Inicio: mis clubes y el estado de cada convocatoria

- **Estado:** In progress (implementada en `app-parity-feature`; falta el E2E)
- **Reglas:** APP-RN-01, APP-RN-03, APP-RN-08
- **Depende de:** 003, 005
- **Repos afectados:** Squadfy_App

## Problema / objetivo
Inicio (`feature/globalPosition`) muestra los clubes reales, pero **los partidos y las noticias son mocks**. Además, duplica los modelos de club y el botón de ajustes no hace nada. Debería responder de un vistazo a la pregunta «¿qué tengo pendiente esta semana en cada club?».

## Criterios de aceptación
- **AC-010-01** Para cada club, Inicio muestra una tarjeta con:
  - el logo y el nombre;
  - el próximo partido (fecha en la zona del club);
  - el estado de la convocatoria (`AnnouncementWindowPolicy`);
  - mi estado («Convocado», «En espera n.º 2», «Sin apuntar»);
  - las plazas libres.

  Los datos salen de `GET /clubs` y de un `GET /clubs/{id}/announcements/current` por club, en paralelo y como máximo 4 a la vez.
- **AC-010-02** Si la convocatoria está abierta y no estoy apuntado, la tarjeta tiene un CTA «Apuntarme» directo (`POST enrollment`). Al tocar la tarjeta se abre el club en la pestaña Partido.
- **AC-010-03** Las tarjetas se ordenan así: primero las convocatorias abiertas en las que no estoy apuntado, después el resto por fecha del partido, y al final los clubes sin partido.
- **AC-010-04** **Se eliminan** los mocks de partidos y noticias (`OfflineFirstGlobalPositionRepositoryImpl`, líneas 38–120), `MatchModel`/`NewsModel` de globalPosition y los componentes de partido «EN VIVO / VS» que no se usan.
- **AC-010-05** El botón de ajustes abre el Perfil (foto, username, cambiar contraseña, cerrar sesión y **eliminar cuenta**, spec 011), o se elimina.
- **AC-010-06** Si un club da error, solo su tarjeta muestra «No disponible · reintentar», y el resto se ve con normalidad.
- **AC-010-07** Pull-to-refresh y refresco al volver a primer plano.

## Decisiones de implementación
- **Modelos unificados**: Inicio usa directamente el dominio de `club` (`ObserveMyClubsUseCase`, convocatoria vigente, horario e inscripción). Se eliminan de `globalPosition` el `ClubModel` duplicado, su DTO, sus mappers, sus use cases y la sincronización con Room. `globalPosition/data` deja de depender de la base de datos de club.
- **Mocks de partidos y noticias (AC-010-04, parcial)**: siguiendo la directriz de flags (D-1, «no eliminar, activar o desactivar»), quedan tras `HOME_RECENT_MATCHES` y `HOME_NEWS`, que están apagados en PRO y sin ninguna petición. Borrarlos del todo queda como decisión del product owner.
- **Ajustes (AC-010-05)**: el botón sigue abriendo la pantalla de flags solo en PRE. El perfil y la eliminación de cuenta llegan con la spec 011.
- **Zona horaria**: cada tarjeta pide también el horario del club para mostrar la hora en su zona (APP-RN-03). Son dos peticiones por club, con un máximo de 4 clubes a la vez.
- **«Apuntarme»**: tras la inscripción se recarga la convocatoria de ese club, porque es el backend quien decide si quedo confirmado o en espera.

