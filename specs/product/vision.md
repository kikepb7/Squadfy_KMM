# Visión de producto

**Squadfy** gestiona clubes de fútbol amateur que juegan **un partido semanal** (5v5, 7v7 u 11v11).

## Problema
Hoy el partido semanal se organiza por WhatsApp:
- quién va y quién se borra;
- si hay plazas;
- cómo se hacen equipos parejos;
- quién marcó.

Es manual, genera discusiones y no deja historial ni sirve para medir el nivel.

## Propuesta de valor
1. **Club**: lo crea un usuario (owner), que invita al resto con un **código**. Un usuario puede estar en varios clubes, con una ficha distinta en cada uno.
2. **Ciclo automático**: con el horario configurado, el sistema crea cada semana el partido y su convocatoria.
   - Se **abre** el día siguiente al partido anterior.
   - Se **cierra** a las 22:00 del día antes.
   - Si el cupo está lleno hay **lista de espera**, que sube sola.
3. **Equipos equilibrados automáticos**: al cerrar la convocatoria se publican dos equipos parejos por posición y **rating Elo**. Los gestores pueden rectificarlos.
4. **Resultado y competitividad**: los gestores registran goles, asistencias, tarjetas y minutos y cierran el partido. Con eso se actualizan el rating y las clasificaciones (rating y estadísticas).
5. **Avisos**: push de apertura, recordatorio, equipos, cancelación y «tienes plaza». Cada club se puede silenciar.
6. **Chat** entre usuarios (ya existe).

## Alcance del MVP desplegable
| Incluido | Fuera del MVP (backlog) |
|---|---|
| Auth completo (registro, verificación, login, reset, cambio de contraseña, perfil y foto) | Foto de miembro distinta por club |
| Clubes: crear, unirse, editar, logo, código, roles, expulsar, vetar, transferir, salir | — |
| Horario semanal (día, hora, zona, formato, duración, activo) | — |
| Convocatoria vigente: apuntarse, desapuntarse, lista de espera, cuenta atrás | Estadísticas por temporada (el backend no filtra por fechas) |
| Equipos publicados, rectificación y equilibrio (gestores) | Economía o cuotas (`feature/economy`), onboarding guiado |
| Gestión del partido: eventos, minutos, cerrar, reabrir, cancelar, partido extra | Noticias en Inicio |
| Clasificaciones de rating y de estadísticas, y mis estadísticas | Chat de club (el chat actual se mantiene) |
| Push del ciclo de partido + silenciar club | Tiempo real del ciclo de partido (WebSocket) |
| Inicio con mis clubes y el estado de cada convocatoria | Web o escritorio |
| Release Android (Play) + iOS (TestFlight o App Store) con backend desplegado | |
| **Pendiente del backend, tras feature flag (D-1, 2026-10-07):** invitados que añade el jugador responsable, excepciones de calendario, hora de sorteo y cierre configurable (`drawTime`), marcador manual | |

La valoración del jugador es **siempre automática**: es el rating Elo, calculado a partir del resultado y de las estadísticas del partido (BE-003 RN-6). Nadie la introduce a mano.

## Métricas de éxito
- Un club completa **4 semanas seguidas** usando solo la app: convocatoria, equipos automáticos, resultado y clasificación.
- **≥ 70 %** de los miembros de un club responden a la convocatoria antes del cierre.
- **0 errores de la app** ante respuestas válidas del backend: ningún `SERIALIZATION` ni ningún 404 de ruta en Crashlytics o en los logs durante la beta.
