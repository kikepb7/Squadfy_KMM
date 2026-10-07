# Mapa de consumo de la API v1

> **Contrato normativo:**
> - `../Squadfy_Backend/docs/BACKEND.md` (§5 convenciones, §8 endpoints, §9 DTOs, §10 enums, §11 errores);
> - OpenAPI `/v3/api-docs` (perfil `dev`);
> - la guía de migración `../Squadfy_Backend/docs/api/migracion-v1.md`.
>
> Este documento **no redefine** el contrato (ADR-0005). Indica **quién lo consume en la app**, la ruta antigua que hay que sustituir y la spec que hace la migración.
> Leyenda: ✅ ya consumido en v1 · 🔁 la app usa la ruta antigua · ➕ la app no lo usa todavía · 🗑 ruta o función de la app sin equivalente (se elimina) · ⏳ pendiente de una spec del backend (oculto tras un flag, spec 013)

## Convenciones que impactan al cliente (spec 002)
- **Base URL:** `BASE_URL_HTTP = https://<host>/api/v1`. Las rutas del cliente no llevan `/api/v1`.
- **Json:** `ignoreUnknownKeys = true` y `coerceInputValues = true`. Los DTOs llevan valores por defecto en todo lo opcional. Los enums se reciben como `String` y se mapean de forma segura (APP-RN-13).
- **Fechas:** `Instant` ISO-8601 UTC (con microsegundos) → `kotlin.time.Instant`. `LocalTime` `"HH:mm:ss"`.
- **201/204:** las creaciones devuelven 201 con body; salir, expulsar y vetar devuelven 204 sin body. Los helpers de `core/data` aceptan cualquier 2xx y `Unit` para 204.
- **Errores:** `{code, message}` o `{code: "VALIDATION_ERROR", errors: [..]}`. Un 401 sin token llega **sin body**. Se parsean a `RemoteError(status, code, messages)` (spec 002).

## Auth y perfil
| Endpoint v1 | Ruta actual en la app | Consumidor | Spec | Estado |
|---|---|---|---|---|
| `POST /auth/{register,login,refresh,logout,resend-verification,forgot-password,reset-password,change-password}` | `/auth/...` (sin v1) | `core/data/auth/KtorAuthRepositoryImpl`, `HttpClientFactory` (refresh) | 002 | ✅ |
| `GET /auth/verify?token=` | `VERIFY_EMAIL_ROUTE` (la app lo llama desde un deep link) | `AuthGraph` | 002 | 🔁 el enlace del email abre el **navegador** (§6), y el deep link de verificación de la app deja de tener sentido |
| `GET /me` | — | Sesión y perfil | 002 | ➕ no hace falta todavía: el login devuelve el mismo `UserDto` |
| `GET /users?query=` | `GET /participants?query=` | chat `KtorChatParticipantService` | 002 | ✅ |
| `GET /users/{userId}` | `GET /participants` (mi perfil) | chat y perfil | 002 | ✅ |
| `POST /me/profile-picture/upload-url?mimeType=` | `POST /participants/profile-picture-upload` | perfil | 002 | ✅ |
| `PUT /me/profile-picture {publicUrl}` | `POST /participants/confirm-profile-picture` | perfil | 002 | ✅ |
| `DELETE /me/profile-picture` | `DELETE /participants/profile-picture` | perfil | 002 | ✅ |

## Chat
| Endpoint v1 | Ruta actual | Spec | Estado |
|---|---|---|---|
| `GET/POST /chats` | `GET/POST /chat` | 002 | ✅ |
| `GET /chats/{id}`, `GET /chats/{id}/messages?before=&pageSize=` | `/chat/{id}`, `/chat/{id}/messages` | 002 | ✅ |
| `POST /chats/{id}/participants` | `POST /chat/{id}/add` | 002 | ✅ |
| `DELETE /chats/{id}/participants/me` | `DELETE /chat/{id}/leave` | 002 | ✅ |
| `DELETE /messages/{id}` | `DELETE /message/{id}` | 002 | ✅ |
| WS `/ws/chat` (`Authorization` en el handshake) | `${BASE_URL_WS}/chat` | — | ✅ sin cambios |

## Clubes y miembros
| Endpoint v1 | Ruta actual | Consumidor | Spec | Estado |
|---|---|---|---|---|
| `GET /clubs` | `GET /club` | globalPosition + club | 003 | 🔁 |
| `POST /clubs` → 201 | `POST /club/create` | `CreateClubUseCase` | 003 | 🔁 |
| `POST /clubs/join` | `POST /club/join` | `JoinClubUseCase` | 003 | 🔁 errores 400 `INVALID_INVITATION_CODE`, 403 `BANNED_FROM_CLUB`, 409 |
| `GET /clubs/{id}` | `GET /club/{id}` | ClubDetail | 003 | 🔁 |
| `PATCH /clubs/{id}` | — | Ajustes (gestor) | 003 | ➕ |
| `PUT /clubs/{id}/logo` multipart `clubLogo` | `POST /club/{id}/logo` | Crear club y Ajustes | 003 | 🔁 cambia el método |
| `POST /clubs/{id}/invitation-code` | — | Ajustes (gestor) | 003 | ➕ |
| `POST /clubs/{id}/transfer-ownership {memberId}` | — | Ficha de miembro (owner) | 003 | ➕ |
| `GET /clubs/{id}/members` | `GET /club/{id}/members` | Miembros, resolución de nombres | 003 | 🔁 el DTO pierde `email` y las estadísticas |
| `PATCH /clubs/{id}/members/me {shirtNumber?, position?}` | `PATCH /club/{id}/members/{memberId}` (`ClubService`, código muerto) | Mi ficha | 003 | 🔁 |
| `DELETE /clubs/{id}/members/me` → 204 | — | Ajustes («Salir») | 003 | ➕ |
| `DELETE /clubs/{id}/members/{memberId}` → 204 | — | Ficha de miembro (gestor) | 003 | ➕ |
| `PATCH /clubs/{id}/members/{memberId}/role {role}` | — | Ficha de miembro (gestor) | 003 | ➕ |
| `GET /clubs/{id}/bans`, `POST/DELETE /clubs/{id}/members/{memberId}/ban` | — | Ajustes › Vetados | 003 | ➕ |
| — | `POST /club/{id}/members/{memberId}/photo` | Foto por club | 003 | 🗑 se usa la foto de perfil global (`/me/profile-picture`) |

## Horario y partidos
| Endpoint v1 | Ruta actual | Spec | Estado |
|---|---|---|---|
| `GET/POST/PATCH /clubs/{id}/schedule` | `PATCH /club/{id}/schedule` → `ClubDTO` | 004 | 🔁 nuevo DTO `ClubMatchScheduleDto` |
| `GET/POST /clubs/{id}/schedule/exceptions` `{date, type: CANCELLED\|RESCHEDULED, newScheduledAt?, reason?}` → 201, `DELETE …/exceptions/{exceptionId}` → 204 | `GET/POST/DELETE /club/{id}/schedule/exceptions[/{id}]` | 004 | 🔁 flag `SCHEDULE_EXCEPTIONS` (BE-008) |
| Horario: `closeDaysBefore`, `closeTime`, `drawDaysBefore`, `drawTime` en `ClubMatchScheduleDto` y en sus peticiones | `drawTime` en `PATCH /club/{id}/schedule` | 004 | 🔁 flag `CUSTOM_DRAW_TIME` (BE-008) |
| `GET /clubs/{id}/absences?from=&to=`, `POST /clubs/{id}/members/me/absences` → 201, `DELETE …/members/me/absences/{id}` → 204 | — | 014 | ➕ flag `MEMBER_ABSENCES` (BE-008) |
| `PUT /matches/{id}/score {teamAScore, teamBScore}`, `DELETE /matches/{id}/score` | `POST /club/matches/{id}/result` | 007 | ✅ flag `MANUAL_SCORE` (BE-008) |
| `GET /clubs/{id}/matches?status=` | `GET /club/{id}/matches` | 006/007 | ✅ `?status=COMPLETED` decide si se puede reabrir |
| `POST /clubs/{id}/matches {scheduledAt, format?, durationMinutes?}` → 201 | `POST /club/{id}/matches` (botón «partido de prueba») | 007 | ✅ «Partido extra» para gestores; `DEV_TEST_MATCH` solo añade el atajo «dentro de 15 min» |
| `GET /matches/{id}` | `GET /club/matches/{id}` (sin uso) | 006 | ✅ `KtorMatchRepository` |
| `POST /matches/{id}/cancel` | `POST /club/matches/{id}/cancel` (sin uso) | 007 | ✅ |
| `POST /matches/{id}/teams {mode, manualTeamA?, manualTeamB?}` | `POST /club/matches/{id}/generate-teams` | 006 | ✅ IDs = `clubMemberId` o `guestId` (BE-008) |
| `GET /matches/{id}/team-balance` | — | 006 | ✅ 409 = sin equipos (panel oculto) |
| `POST /matches/{id}/events` → 201, `DELETE /matches/{id}/events/{eventId}` | — | 007 | ✅ borrar con deshacer |
| `PUT /matches/{id}/players/{memberId}/minutes {minutes}` | — | 007 | ✅ |
| `POST /matches/{id}/complete`, `POST /matches/{id}/reopen` | `POST /club/matches/{id}/result` | 007 | ✅ el resultado deja de enviarse como marcador |

## Convocatorias
| Endpoint v1 | Ruta actual | Spec | Estado |
|---|---|---|---|
| `GET /clubs/{id}/announcements/current` (404 = sin partido) | — | 005, 010 | ➕ pantalla principal |
| `GET /clubs/{id}/announcements`, `GET /announcements/{id}`, `GET /matches/{id}/announcement` | `GET /club/matches/{id}/signups` | 005/006 | 🔁 |
| `POST /announcements/{id}/enrollment` | `POST /club/matches/{id}/signups` | 005 | 🔁 |
| `DELETE /announcements/{id}/enrollment` | `DELETE /club/matches/{id}/signups/me` | 005 | 🔁 |
| `POST /announcements/{id}/guests {name, position?}`, `DELETE /announcements/{id}/guests/{guestId}` → `MatchAnnouncementDto` | `POST /club/matches/{id}/guests`, `DELETE /club/matches/{id}/signups/{signupId}` | 005 | 🔁 flag `MATCH_GUESTS` (BE-008: máximo 2 por miembro; los miembros tienen prioridad) |

## Rating y estadísticas
| Endpoint v1 | Spec | Estado |
|---|---|---|
| `GET /clubs/{id}/ratings`, `GET /clubs/{id}/ratings/me` | 008 | ➕ (sustituye a `performanceIndex`) |
| `GET /clubs/{id}/stats?sortBy=`, `GET /clubs/{id}/stats/me` | 008 | ➕ |

## Notificaciones
| Endpoint v1 | Ruta actual | Spec | Estado |
|---|---|---|---|
| `POST /devices {token, platform}` → 201 | `POST /notification/register` | 002 (ruta) y 009 (ciclo) | ✅ |
| `DELETE /devices/{token}` | `DELETE /notification/{token}` | 002 | ✅ |
| `GET/PUT /clubs/{id}/notification-settings {muted}` | — | 009 | ➕ |
| Push `data.type` ∈ `match.announcement.opened`, `match.announcement.closing_soon`, `match.teams.published`, `match.cancelled`, `match.waitlist.promoted`, `new_message` | solo `chatId` | 009 | ➕ |

## DTOs que cambian de forma incompatible en la app (causarían `SERIALIZATION`)
| DTO de la app | Cambio en v1 | Spec |
|---|---|---|
| `UserSerializableDTO` | Sin `profilePictureUrl` (se obtiene con `GET /users/{id}`) | 002 |
| `ClubMemberDTO` (club) | **Sin** `email`, `goalsScored`, `assists`, `yellowCards`, `redCards`, `minutesPlayed` ni `matchesPlayed` (eran obligatorios en la app). Añade `createdAt`/`updatedAt`. `role` pasa a ser `OWNER\|ADMIN\|CAPTAIN\|PLAYER` | 003 |
| `ClubDTO` / gp `ClubDto` | Sin campos de horario (se obtienen con `/schedule`) | 003/004 |
| `ClubMatchDTO` | Se sustituye por `MatchDto` (equipos = `List<clubMemberId>`, `minutesPlayed` map, eventos) | 006/007 |
| `MatchSignupDTO` | Se sustituye por `MatchAnnouncementDto`/`EntryDto` | 005 |
| `ChatDto` | Añade `creator` | 002 |
| `ChatParticipantDto` | Sin `email` | 002 |
| `MatchAnnouncementDto` (BE-008) | `drawAt`; las entradas ganan `participantType` (MEMBER/GUEST), `guestName`, `guestPosition` e `invitedByMemberId`, y `clubMemberId` es nulo en los invitados | 005 |
| `MatchDto` (BE-008) | `enrolledGuests`, `teamAGuests`/`teamBGuests` (`MatchGuestDto`), `isManualScore`, `ratingChanges` y `scheduleDate` | 006/007/008 |

## Historial
| Fecha | Cambio |
|---|---|
| 2026-10-05 | Borrador de contrato propio (ADR-0001), ya reemplazado |
| 2026-10-07 | Pasa a ser un mapa de consumo del backend v1 (ADR-0005) |
| 2026-10-07 | Spec 002: auth, `/users`, foto, chat y `/devices` migrados a v1 (✅) |
| 2026-10-07 | BE-008 publicada: invitados, excepciones, ausencias, cierre y sorteo configurables, marcador manual y `ratingChanges` |
| 2026-10-07 | D-1: invitados, excepciones, `drawTime` y marcador manual pasan a ⏳ (backend en curso) y quedan tras flags |
| 2026-10-07 | Spec 006: `GET /matches/{id}`, `/teams` (AUTO/MANUAL con invitados), `/team-balance` y `GET /matches/{id}/announcement` (✅). `KtorMatchRepository` ya expone también las rutas de la 007 |
| 2026-10-07 | Spec 007: eventos, minutos, cerrar, reabrir, cancelar, partido extra y marcador manual (✅) |
