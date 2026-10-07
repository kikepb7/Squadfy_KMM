# ADR-0006 · Estrategia de caché por dominio: offline-first frente a network-first

- **Estado:** Propuesto
- **Fecha:** 2026-10-07

## Contexto
La constitución (II.3) pide offline-first con Room como fuente de lectura. Pero el ciclo de partido (convocatoria, equipos, eventos) cambia en el servidor por procesos programados, como el cierre o la publicación automática de equipos. Además, el backend **no emite estos cambios por WebSocket**: recomienda refrescar al abrir la pantalla, con pull-to-refresh y al recibir una push (`BACKEND.md` §14).

Si esos datos se cachearan en Room, la app podría mostrar «convocatoria abierta» o unos equipos obsoletos y llevar al usuario a acciones que el servidor va a rechazar.

## Decisión
| Dominio | Estrategia | Motivo |
|---|---|---|
| Sesión, perfil (`/me`) | DataStore cifrado | Ya existe |
| Clubes y miembros | **Offline-first** (Room `squadfy_club.db`) | Cambian poco. Se necesitan para resolver nombres y fotos desde `clubMemberId` |
| Horario del club | Offline-first (Room) | Cambia poco. Se muestra en Inicio |
| Convocatoria, partido, equipos, equilibrio | **Network-first** con caché **en memoria** (repositorio con `StateFlow`) y estado `stale` visible si no hay red | Lo cambia el servidor; solo debe mostrarse fresco |
| Ratings y estadísticas | Network-first con caché en memoria | Son derivados y se recalculan al cerrar un partido |
| Chat | Offline-first + WebSocket | Ya existe |

**Refresco:**
- al entrar en la pantalla;
- con pull-to-refresh;
- al volver la app a primer plano;
- al recibir una push cuyo `data.clubId` coincide con el club abierto (spec 009);
- tras cada acción propia, usando el DTO que devuelve la acción.

## Consecuencias
- La constitución II.3 se reescribe como «offline-first salvo excepción del ADR-0006».
- No hacen falta tablas de Room para partidos. Sí se añade una tabla `club_schedule` (spec 004).
