# Contrato de la variante Firebase (spec 019, ADR-0010)

Este documento es la contrapartida de `api-v1.md` para la variante Firebase. Las reglas de negocio no cambian: son las del backend (`BE-NNN RN-x`), y las Functions las aplican.

Las fuentes ejecutables son estas, y si algo no cuadra mandan ellas:
- `firebase/functions/src/paths.ts` (modelo de datos);
- `firebase/firestore.rules`, `storage.rules` y `database.rules.json` (quién lee y quién escribe);
- `firebase/functions/src/index.ts` (funciones);
- los tests de `firebase/functions/test`.

## 1. Principios
- **Las Functions escriben el dominio.** Clubes, miembros, horario, convocatorias, partidos, ratings, ausencias y ajustes de club solo se escriben desde callables, jobs o triggers. Las reglas dejan leer a los miembros activos.
- El cliente escribe directamente en tres sitios:
  - su perfil (`users/{uid}`, sin `username` ni `email`);
  - sus dispositivos;
  - los mensajes de sus chats.
- **El contrato JSON es el del backend.** Las callables responden los mismos DTOs que `BACKEND.md` §9, por eso `feature/club/data-contract` (DTOs y mappers) lo comparten las dos variantes.
- Los ids:
  - Membresía: `{clubId}_{uid}`. Es estable si alguien vuelve (BE-001 RN-12) y la usan equipos, ratings y estadísticas (`clubMemberId`).
  - Partido: `{clubId}_{yyyyMMdd'T'HHmm}Z`, la fecha del horario en UTC, así que es idempotente.
  - Convocatoria: el mismo id que su partido.
  - Chats y mensajes: ids de Firestore.
- Región `europe-west1` (Functions, `FunctionsClient.REGION`). Firestore está en `eur3` y Realtime Database en `europe-west1`.

## 2. Firestore
| Ruta | Contenido | Lee | Escribe |
|---|---|---|---|
| `users/{uid}` | Perfil: `username`, `usernameLower`, `pictureUrl` y `updatedAt` | Cualquier usuario con sesión (búsqueda de chat; nunca el email) | El dueño: solo `pictureUrl` y `updatedAt`. El nombre lo fija `completeProfile` |
| `users/{uid}/devices/{token}` | Token FCM, plataforma e idioma | El dueño | El dueño |
| `users/{uid}/clubSettings/{clubId}` | `muted` | El dueño | `setClubMuted` |
| `usernames/{lower}` | Reserva de nombre único (`uid`) | Nadie (lo usan las Functions) | `completeProfile`, `deleteAccount` |
| `invitationCodes/{code}` | `clubId` | Nadie | Functions |
| `clubs/{clubId}` | Club (`BACKEND.md` §9 `ClubDto` sin miembros): `ownerId`, `membersCount`, `invitationCode`… | Miembros activos | Functions |
| `clubs/{clubId}/members/{memberId}` | Membresía: `userId`, `role`, `status`, dorsal, posición, fechas y fotos | Miembros activos. Cada uno, también las suyas (*collection group*) | Functions |
| `clubs/{clubId}/schedule/current` | Horario semanal | Miembros | Functions |
| `clubs/{clubId}/scheduleExceptions/{id}` | Semanas canceladas o movidas (BE-008) | Miembros | Functions |
| `clubs/{clubId}/matches/{matchId}` | Partido: equipos, invitados, eventos, minutos, marcador, `ratingChanges` y estado | Miembros | Functions |
| `clubs/{clubId}/announcements/{matchId}` | Convocatoria: ventana, plazas, lista de espera y marcas de los jobs | Miembros | Functions |
| `clubs/{clubId}/seasons/{seasonId}` | Temporada (spec 020): `{startYear, name, startsOn, endsOn, status}`, id `2026-27`; empieza en septiembre y acaba en junio | Miembros | Functions |
| `clubs/{clubId}/seasons/{seasonId}/ratings/{memberId}` | Elo de la temporada (`rating`, `matchesRated`); antes de la spec 020 estaba en `clubs/{clubId}/ratings`, y `ensureSeasons` lo mueve a la primera temporada | Miembros | Functions |
| `clubs/{clubId}/absences/{id}` | Ausencias | Miembros | Functions |
| `clubs/{clubId}/live/changes` | Señal de cambios (`version`, `kind`) para refrescar en vivo (ADR-0006) | Miembros | Functions |
| `chats/{chatId}` | `participantIds`, `participants.{uid}` (nombre y foto) y el último mensaje | Participantes | Functions (`createChat`, `addChatParticipants`, `leaveChat` y triggers) |
| `chats/{chatId}/messages/{id}` | `senderId`, `content` y `createdAt` (del servidor) | Participantes | El autor crea y borra los suyos. No se editan |
| `debugPushes/{id}` | Pushes que se habrían enviado (**solo en el emulador**) | — | Functions |

Los índices están en `firestore.indexes.json`: chats por participante y actividad; partidos y convocatorias por estado y fecha (también *collection group*); miembros por usuario y estado.

## 3. Realtime Database y Storage
- RTDB `presence/{uid}`: `{online, lastSeen}`. Solo lo escribe el dueño, y `onDisconnect` lo pone a `online=false`.
- RTDB `typing/{chatId}/{uid}`: el instante (ms) en que empezó a escribir. Solo lo escribe el dueño:
  - se borra a los 4 s de inactividad, al enviar y al desconectarse;
  - quien lo lee descarta las marcas de más de 8 s.
- Storage, con imágenes de hasta 5 MB:
  - `users/{uid}/avatar.jpg`: el dueño;
  - `clubs/{clubId}/logo.jpg`: los gestores del club;
  - `clubs/{clubId}/members/{uid}.jpg`: el miembro.

  Las URLs llegan a Firestore con `setClubLogo` y `setMyClubPicture`, y con la escritura de `users/{uid}.pictureUrl` para el avatar.

## 4. Callables
Todas exigen sesión. Si `ENFORCE_APP_CHECK=true`, también App Check.

| Área | Callables |
|---|---|
| Cuenta | `completeProfile`, `deleteAccount` |
| Clubes | `createClub`, `updateClub`, `joinClub`, `leaveClub`, `regenerateInvitationCode`, `changeMemberRole`, `transferOwnership`, `removeMember`, `banMember`, `unbanMember`, `updateMyMembership`, `setClubLogo`, `setMyClubPicture` |
| Horario | `createSchedule`, `getSchedule`, `updateSchedule`, `getScheduleExceptions`, `addScheduleException` (`reason` obligatorio; mover solo dentro de la semana; sustituye a la excepción anterior de esa semana), `deleteScheduleException` |
| Convocatoria | `getCurrentAnnouncement`, `getAnnouncementByMatch`, `getAnnouncementHistory`, `enroll`, `withdraw`, `addGuest`, `removeGuest` |
| Partidos | `getMatch`, `getClubMatches`, `createExtraMatch`, `cancelMatch`, `generateTeams`, `getTeamBalance`, `addMatchEvent`, `deleteMatchEvent`, `setPlayerMinutes`, `setMatchScore`, `clearMatchScore`, `completeMatch`, `reopenMatch` |
| Clasificación | `getRatings`, `getStats`, `getMyRating`, `getMyStats` (con `seasonId` opcional; por defecto, la temporada en curso o la siguiente en pretemporada; `getStats` sigue aceptando `from`/`to`) |
| Temporadas | `getSeasons({clubId})` (la más nueva primero), `updateSeason({clubId, seasonId, startsOn, endsOn})` (managers; día de septiembre y de junio) |
| Ausencias y avisos | `createAbsence`, `deleteAbsence`, `getAbsences`, `getClubNotificationSettings`, `setClubMuted` |
| Chat | `createChat`, `addChatParticipants`, `leaveChat` |
| Solo emulador | `runJobsNow({at?})`: ejecuta los jobs como si fuera `at` |

La petición es el cuerpo JSON del endpoint equivalente más los ids de la ruta (`clubId`, `matchId`, `announcementId`…). Los modelos de petición de la app están en `CycleRequests.kt` y en los repositorios de `data-firebase`.

## 5. Errores
Una callable falla con un `HttpsError` cuyo código equivale al estado HTTP del backend, y `details.code` lleva el `BackendErrorCode` de `BACKEND.md` §11. La app lo convierte en el mismo `DataError.Remote` con `toRemoteErrorWithCode`.

| HTTP del backend | Código de la callable |
|---|---|
| 400 | `invalid-argument` |
| 401 | `unauthenticated` (también si la cuenta ya no tiene perfil: una cuenta borrada no puede escribir) |
| 403 | `permission-denied` |
| 404 | `not-found` |
| 409 | `failed-precondition` |
| 429 | `resource-exhausted` |

## 6. Jobs y triggers
| Función | Cuándo | Qué hace |
|---|---|---|
| `planUpcomingMatches` | Cada hora, en el minuto 5 | Planifica el partido de la semana siguiente de cada horario activo, con sus excepciones (BE-008 RN-B2) |
| `closeAnnouncementsAndDraw` | Cada 5 min | Cierra las convocatorias vencidas y, a la hora de sorteo, publica los equipos (BE-003 y BE-008) |
| `sendAnnouncementNotifications` | Cada 5 min | Avisa de que la convocatoria está abierta y recuerda el cierre, una vez cada cosa (marcas en la convocatoria) |
| `rollSeasons` | Cada día a las 00:15 UTC | Cierra las temporadas terminadas, crea la siguiente con los mismos días y la activa en su inicio (spec 020). Es idempotente |
| `onChatMessageCreated` y `onChatMessageDeleted` | Al crear o borrar un mensaje | Último mensaje del chat y push `new_message` |
| `onUserProfileUpdated` | Al cambiar `users/{uid}` | Copia el nombre y la foto a las membresías y a los chats |
| `onAuthUserDeleted` | Al borrar el usuario de Auth (web, consola) | El mismo borrado que `deleteAccount`. Es idempotente |

Los pushes usan los mismos `type` que el backend (`BACKEND.md` §13) y respetan el silencio por club.

## 7. Hosting
- `/legal/privacy`: política de privacidad.
- `/account/delete`: borrado de cuenta en la web.
- `/auth/action`: enlaces de Firebase Auth (`verifyEmail`, `resetPassword`, `recoverEmail`). En la consola, las plantillas de email tienen que apuntar a `https://<dominio>/auth/action`.
