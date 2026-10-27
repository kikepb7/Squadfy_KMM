# 021 · Notificaciones push completas, aviso de versión y telemetría por entorno

- **Estado:** Approved (2026-10-27, pedido por el owner)
- **Owner:** Enrique Palma
- **Reglas:** BE-005 RN-1/2/5 (avisos de convocatoria), BE-009 (silenciar club), APP-RN-14 (no se envía nada de un club silenciado); nuevas `APP-RN-P1…P8`
- **ADRs:** ADR-0010 (Firebase)
- **Repos afectados:** Squadfy_App (app y `firebase/`)

## Estado de partida (evaluación del 2026-10-27)
- **Ya hecho:**
  - FCM en Android e iOS: tokens en `users/{uid}/devices` con idioma y zona;
  - canales «Partidos» y «Chat»; el aviso abre la pantalla adecuada;
  - un aviso del club que estás viendo refresca la pantalla en vez de mostrar el banner;
  - 7 tipos: convocatoria abierta, cierra pronto, plaza desde la lista de espera, equipos publicados, partido cancelado, partido movido y mensaje de chat.
- **Fallos encontrados:**
  - el silencio por club **no se aplicaba** (`mutedUsers` existía pero nadie la llamaba);
  - todos los avisos salían en español, aunque el dispositivo guarda su idioma;
  - el dispositivo no guardaba la versión de la app.
- **Faltaba:**
  - aviso de nueva versión (push y dentro de la app);
  - avisos generales de la app;
  - recordatorio del partido;
  - resultado del partido;
  - temporadas;
  - avisos a los gestores (alta de miembro) y al miembro (cambio de rol);
  - eventos de Analytics (solo se registraba `screen_view`);
  - Remote Config por entorno.

## Reglas
- **APP-RN-P1** Un club silenciado no envía avisos al usuario. Excepciones, por ser personales y urgentes: «tienes plaza» (spec 005 RN-7) y el cambio de rol.
- **APP-RN-P2** El aviso va en el idioma del dispositivo: español, o inglés si el dispositivo está en inglés.
- **APP-RN-P3 · Recordatorio:** 2 horas antes del partido, a los convocados («Hoy juegas a las 20:00»). Una sola vez por partido.
- **APP-RN-P4 · Resultado:** al cerrar el acta, a los jugadores del partido. Lleva el marcador y su cambio de rating.
- **APP-RN-P5 · Temporadas:** cuando empieza una temporada, a los miembros («Empieza la temporada 2027/28») y cuando se cierra, con el primero de la clasificación. Una sola vez por cambio.
- **APP-RN-P6 · Club:**
  - a los gestores, cuando alguien se une;
  - al miembro, cuando le cambian el rol;
  - al nuevo owner, cuando le transfieren el club.
- **APP-RN-P7 · Avisos de la app** (por *topics* de FCM `app_<plataforma>_<idioma>`): nueva versión (abre la tienda) y anuncios generales. Solo los envía el owner, con un script con credenciales de administrador, nunca desde la app.
- **APP-RN-P8 · Versión mínima y última** (Remote Config, por plataforma y entorno):
  - por debajo de la mínima, la app pide actualizar y no deja seguir;
  - por debajo de la última, un aviso que se puede descartar hasta la siguiente versión.

## Criterios de aceptación
- **AC-021-01** *Given* un miembro con el club silenciado *When* se abre la convocatoria *Then* no recibe el aviso. Si sube desde la lista de espera, sí lo recibe.
- **AC-021-02** *Given* un dispositivo en inglés y otro en español *When* se publican los equipos *Then* cada uno recibe el texto en su idioma.
- **AC-021-03** *Given* un partido a las 20:00 *When* el job corre a partir de las 18:00 *Then* los convocados reciben el recordatorio. Si corre dos veces, no se repite.
- **AC-021-04** *Given* un partido completado *When* se cierra el acta *Then* cada jugador recibe el marcador y su cambio de rating. Los amistosos no llevan rating.
- **AC-021-05** *Given* el job de temporadas *When* se cierra una temporada y empieza otra *Then* los miembros reciben cada aviso una vez.
- **AC-021-06** *Given* alguien que se une al club *Then* los gestores reciben el aviso. *Given* un cambio de rol *Then* el miembro lo recibe.
- **AC-021-07** *Given* la app *When* arranca *Then* se suscribe a `app_<plataforma>_<idioma>` (y se da de baja del otro idioma). Un aviso de nueva versión abre la tienda.
- **AC-021-08** *Given* Remote Config con `min_version` por encima de la instalada *When* se abre la app *Then* aparece el aviso que obliga a actualizar. Con `latest_version` por encima, el aviso se puede descartar.
- **AC-021-09** *Given* el consentimiento de estadísticas *When* el usuario crea un club, se une, se apunta, sortea o cierra un acta *Then* se registra el evento, sin ids ni nombres.
- **AC-021-10** *Given* Remote Config *Then*:
  - tiene las condiciones `entorno_pre` (apps Dev) y `entorno_pro` (apps Prod), y `android` e `ios`;
  - los parámetros de versión y de tienda tienen un valor por entorno y plataforma.

## Fuera de alcance
- Avisos programados desde la consola de Firebase con segmentación de Analytics: se pueden usar, pero no son parte de la app.
- Denuncias y bloqueos del chat (D-13).
