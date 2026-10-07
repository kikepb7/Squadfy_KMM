# Reglas de negocio

> **Fuente normativa: el backend** (ADR-0005). Las reglas de dominio están en `../Squadfy_Backend/specs/NNN-*/spec.md` (RN-x) y en `../Squadfy_Backend/docs/BACKEND.md` §7.
> Este documento es el **espejo para el cliente**:
> - Parte A resume las reglas del backend con su referencia `BE-NNN RN-x`, para que las specs y los tests de la app las citen sin ambigüedad.
> - Parte B define las reglas **propias de la app** (`APP-RN-xx`): UX, presentación y comportamiento del cliente.
>
> Si la Parte A contradice al backend, **gana el backend**: corrige este fichero.

---

## Parte A · Reglas del dominio (backend)

### Clubes y membresía (BE-001)
| Ref | Regla (resumen) |
|---|---|
| BE-001 RN-1/2 | El creador es `OWNER` (uno por club). Roles: `OWNER`, `ADMIN`, `CAPTAIN` y `PLAYER`. **Gestor = OWNER o ADMIN**. `CAPTAIN` **no** tiene permisos de gestión. |
| BE-001 RN-3 | El código de invitación tiene 8 caracteres alfanuméricos en mayúsculas y la búsqueda no distingue mayúsculas. Los gestores lo regeneran y el anterior deja de valer. Todos los miembros lo ven. |
| BE-001 RN-4/5 | No se puede unir dos veces (409). Un club lleno (`maxMembers`) devuelve 409. |
| BE-001 RN-6 | Posición opcional: `GOALKEEPER`, `DEFENDER`, `MIDFIELDER` o `FORWARD`. Cada miembro edita su dorsal (1–999) y su posición (`PATCH members/me`). |
| BE-001 RN-7 | Solo los miembros ven el club. Solo los gestores cambian el logo, el código, los roles o expulsan. |
| BE-001 RN-8 | El owner no puede salir sin transferir antes la propiedad (409). |
| BE-001 RN-9 | Jerarquía: el OWNER actúa sobre cualquiera y asigna ADMIN, CAPTAIN o PLAYER. Un ADMIN solo actúa sobre CAPTAIN y PLAYER y alterna entre esos dos. Nadie se expulsa a sí mismo ni cambia su propio rol (400). |
| BE-001 RN-10/12 | Quien sale o es expulsado conserva su historial y su rating, deja de verse y sale de las convocatorias abiertas (con promoción desde la lista de espera). Si vuelve con el código, recupera su `clubMemberId` como `PLAYER`. |
| BE-001 RN-11 | Al transferir la propiedad, el nuevo miembro pasa a OWNER y el antiguo a ADMIN. |
| BE-001 RN-13 | Los gestores editan el nombre, la descripción y `maxMembers`, que no puede quedar por debajo de los miembros actuales (400). |
| BE-001 RN-14 | Veto: tiene los permisos de la expulsión. Un vetado no puede volver (403 `BANNED_FROM_CLUB`) hasta que se levante el veto. Los gestores ven la lista de vetados. |
| BACKEND §7.1 | **Privacidad:** el email de los miembros no se expone. El propio email solo aparece en `GET /me`. |

### Ciclo semanal y convocatoria (BE-002)
| Ref | Regla (resumen) |
|---|---|
| BE-002 RN-1 | Un horario por club: día, hora local, `timeZone` IANA (por defecto `Europe/Madrid`), `format` (`FIVE_A_SIDE`=10, `SEVEN_A_SIDE`=14, `ELEVEN_A_SIDE`=22 plazas), `matchDurationMinutes` (10–180, por defecto 60) e `isActive`. |
| BE-002 RN-2 | Todo cálculo de calendario se hace en la zona del club. La API envía instantes en UTC. |
| BE-002 RN-3/11 | Planificación idempotente: se crea el siguiente partido junto con su convocatoria. Al crear o reactivar el horario, el primero se planifica de inmediato. |
| BE-002 RN-4 | `opensAt` = inicio del día siguiente al último partido no cancelado (o «ahora»). |
| BE-002 RN-5 | `closesAt` = **22:00 (hora del club) del día anterior al partido**. Si el partido se crea después de ese corte, cierra a la hora de inicio. |
| BE-002 RN-6 | Solo los miembros se apuntan o desapuntan, y solo con `OPEN` y dentro de `[opensAt, closesAt)`; fuera de ese intervalo, 400 `BAD_REQUEST`. **Tras el cierre nadie puede apuntarse ni desapuntarse.** |
| BE-002 RN-7/7b/7c | `CONFIRMED` mientras haya plazas y `WAITLISTED` cuando no (FIFO). Si se desapunta un confirmado, sube el primero de la lista de espera. Solo los `CONFIRMED` juegan. |
| BE-002 RN-8/9 | Cancelar un partido cancela su convocatoria. Un partido cancelado no se recrea. Un `COMPLETED` no se puede cancelar. |
| BE-002 RN-10 | Los gestores configuran el horario y crean partidos extra (fecha futura) o los cancelan. |

### Sorteo y rating (BE-003)
| Ref | Regla (resumen) |
|---|---|
| BE-003 RN-1…5 | Se sortea entre `CONFIRMED` (mínimo 2). Los tamaños difieren ≤ 1; los porteros se reparten primero y luego cada posición; se minimiza la diferencia de rating; a igualdad de nivel, el resultado es aleatorio. |
| BE-003 RN-6 | Rating Elo **automático** por club y jugador: empieza en 1000, se actualiza al cerrar un partido y es provisional hasta 10 partidos. **Nadie lo introduce a mano.** |
| BE-003 RN-7/8 | `MANUAL`: equipos disjuntos, no vacíos, con diferencia ≤ 1 y solo con inscritos; solo los gestores, y solo en `SCHEDULED`. Repetir el sorteo sustituye los equipos. |
| BE-003 RN-9 | **Al cerrarse la convocatoria los equipos se publican automáticamente.** Los gestores pueden rectificar después (`AUTO` o `MANUAL`). |
| BE-003 RN-10 | Equilibrio (solo gestores): rating medio y total por equipo, rating de cada jugador y `teamAExpectedScore` (0–1). |
| BE-003 RN-11 | La clasificación por rating es **pública dentro del club**. Los empates comparten posición (1, 2, 2, 4). `/ratings/me` incluye `rank` y `totalPlayers`. |

### Resultado y estadísticas (BE-004)
| Ref | Regla (resumen) |
|---|---|
| BE-004 RN-1 | Estados efectivos: `SCHEDULED → COMPLETED` o `SCHEDULED → CANCELLED` (`IN_PROGRESS` existe pero no se usa). |
| BE-004 RN-2 | Los gestores registran eventos (`GOAL`, `ASSIST`, `YELLOW_CARD`, `RED_CARD`, minuto opcional 1–120), solo de jugadores de los equipos y solo en `SCHEDULED`. |
| BE-004 RN-3/4 | Cerrar (`complete`): `SCHEDULED`, hora de inicio pasada y con equipos. **El marcador = los goles registrados.** Actualiza el rating. |
| BE-004 RN-5 | Solo se puede reabrir el **último** partido cerrado: se revierte su rating y se pueden corregir eventos y minutos. |
| BE-004 RN-6 | Minutos: la duración del partido, salvo que un gestor fije otro valor (0–duración) antes de cerrar. |
| BE-004 RN-7/8 | Las estadísticas se derivan de los partidos `COMPLETED`. Clasificación ordenable por `GOALS`, `ASSISTS`, `MATCHES`, `MINUTES` o `WINS`; incluye a todos los miembros activos; los empates comparten posición. **No hay filtro por temporada.** |

### Notificaciones (BE-005)
| Ref | Regla (resumen) |
|---|---|
| BE-005 RN-1…6 | Push de apertura (a todos), recordatorio 24 h antes del cierre si quedan plazas (a los no inscritos), equipos publicados (a los jugadores, indicando A o B), cancelación (a los inscritos) y promoción desde la lista de espera (al promocionado). |
| BE-005 RN-7 | Silenciar un club bloquea todas sus push, **salvo** la de promoción. |
| BE-005 RN-8 | Solo push (FCM), con textos en español y fechas en la zona del club. |

### Auth (BACKEND §6)
| Ref | Regla (resumen) |
|---|---|
| AUTH-1 | El access token dura 15 min y el refresh token 30 días, **rotado** en cada refresh. Cambiar o restablecer la contraseña cierra todas las sesiones. |
| AUTH-2 | Login sin verificar el email → 403 `EMAIL_NOT_VERIFIED`, con la opción de reenviar el correo. Registro con email o username repetidos → 409 `USER_EXITS` (sic). |
| AUTH-3 | Validación: username de 3–20 caracteres; contraseña de ≥ 8 caracteres con al menos un dígito o un carácter especial. |
| AUTH-4 | El reset de contraseña llega por deep link `squadfy://reset-password?token=…`, válido 30 min. |

### Pendiente de spec del backend (D-1, decidido el 2026-10-07)
El owner del producto ha pedido estas funciones y el backend las implementará. Las reglas definitivas las fija la spec del backend; hasta entonces son **propuestas ❓**, y la app las mantiene tras un feature flag (spec 013).

| Ref provisional | Regla propuesta | Flag |
|---|---|---|
| BE-PEND-GUESTS ❓ | Un miembro **apuntado** puede añadir invitados a mano. Cada invitado depende de ese miembro («invitado de X»). Quedan por definir en el backend: si el invitado ocupa plaza o va a la lista de espera, y qué ocurre si el responsable se borra. | `MATCH_GUESTS` |
| BE-PEND-EXCEPTIONS ❓ | Los gestores marcan fechas sin partido; esa semana no se planifica partido ni convocatoria. | `SCHEDULE_EXCEPTIONS` |
| BE-PEND-DRAWTIME ❓ | Los gestores configuran la hora de cierre y sorteo del día anterior, en lugar de las 22:00 fijas de BE-002 RN-5. | `CUSTOM_DRAW_TIME` |
| BE-PEND-SCORE ❓ | Los gestores pueden fijar el marcador a mano, sin registrar todos los goles como eventos. | `MANUAL_SCORE` |
| BE-003 RN-6 (vigente) | **La valoración es siempre automática**: rating Elo actualizado a partir del resultado y de las estadísticas del partido (goles, asistencias, tarjetas). Nadie la introduce a mano. | — |

---

## Parte B · Reglas propias de la app (`APP-RN`)

| ID | Regla |
|---|---|
| APP-RN-01 | **Convocatoria abierta en la UI** ⇔ `status == OPEN && opensAt ≤ ahora < closesAt` (el `status` puede tardar hasta 5 min en pasar a CLOSED). Lo calcula `AnnouncementWindowPolicy` (domain) con un `Clock` inyectado. |
| APP-RN-02 | Apuntarse o desapuntarse solo se ofrece con APP-RN-01 verdadero. Después del cierre se muestra «Convocatoria cerrada»; no se ofrece «pedir a un admin», porque el backend no lo permite (BE-002 RN-6). |
| APP-RN-03 | Las fechas del ciclo de partido se muestran **en la zona horaria del club** (`schedule.timeZone`). Si la del dispositivo es distinta, se añade la etiqueta de la zona (por ejemplo «20:00 (Madrid)»). |
| APP-RN-04 | Las acciones de gestión solo se muestran si `myRole ∈ {OWNER, ADMIN}`. `myRole` se obtiene del miembro cuyo `userId` es el de la sesión. Las acciones exclusivas del owner (transferir, nombrar ADMIN) solo se muestran a `OWNER`. Un 403 del servidor se muestra siempre como «No tienes permiso», sea cual sea la UI. |
| APP-RN-05 | En la UI de jerarquía (BE-001 RN-9) solo se ofrecen las opciones válidas para el actor: un ADMIN no ve «Hacer admin» ni puede actuar sobre otros ADMIN, y nadie ve acciones sobre sí mismo. |
| APP-RN-06 | Los identificadores `clubMemberId` se resuelven a nombre y foto con los miembros del club (cacheados en Room). Un `clubMemberId` desconocido (miembro que ha salido) se muestra como «Exjugador». |
| APP-RN-07 | **Errores**: la UI decide según el `code` del backend y el estado HTTP, **nunca** según `message`. Cada `code` conocido tiene su texto ES/EN, y un `code` desconocido usa el texto genérico de su estado HTTP. |
| APP-RN-08 | **Refresco** según ADR-0006: al entrar, con pull-to-refresh, al volver a primer plano, al recibir una push del club y tras cada acción. Un dato network-first sin red se muestra con la marca «sin conexión · actualizado hace X». |
| APP-RN-09 | **Rating**: se muestra el entero redondeado. Si `isProvisional`, va con la etiqueta «Provisional». Un `PLAYER` o `CAPTAIN` ve la clasificación pública, pero no el equilibrio de equipos (es solo para gestores). |
| APP-RN-10 | **Marcador**: es la suma de los eventos `GOAL` por equipo. En la UI no hay campos para escribir el marcador a mano. |
| APP-RN-11 | La sesión se cierra (con navegación a Login y limpieza de Room, DataStore y dispositivo FCM) cuando el refresh devuelve 401 `INVALID_TOKEN`, después de cambiar la contraseña o en el logout. |
| APP-RN-12 | Tras crear o unirse a un club, se navega a ese club. La pestaña «Clubs» lista mis clubes. |
| APP-RN-13 | Las enumeraciones desconocidas que lleguen del backend no rompen la app: un rol desconocido se trata como `PLAYER`, una posición desconocida como `null`, y un estado desconocido produce un estado `UNKNOWN` que la UI muestra en solo lectura. |
| APP-RN-14 | Las notificaciones de un club silenciado no se muestran en la app (el backend tampoco las envía). El ajuste se puede cambiar en Ajustes del club. |
| APP-RN-15 | **Feature flags** (spec 013): toda funcionalidad sin terminar o pendiente del backend se oculta tras un `FeatureFlag`, en vez de borrarla o dejarla visible. Cada flag tiene un valor por defecto para **PRE** y otro para **PRO**. |
| APP-RN-16 | Orden de resolución de un flag: override local (**solo en PRE**) → valor remoto (cuando exista, ADR-0007) → valor por defecto del entorno. En PRO los overrides locales se ignoran. |
| APP-RN-17 | Con un flag apagado, la funcionalidad no se ve (no se muestra deshabilitada) y su código **no hace llamadas de red**. |
