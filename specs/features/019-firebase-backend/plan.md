# 019 · Plan técnico: Firebase como backend de lanzamiento

## Cumplimiento de la constitución
| Principio | Cumple | Nota |
|---|---|---|
| I.1 El servidor es la autoridad | ✅ | La autoridad pasa a ser `firebase/functions`; las reglas de Firestore impiden que el cliente escriba el dominio (ADR-0010) |
| I.2 Permisos por club en el servidor | ✅ | `requireMember` / `requireManager` en cada callable; las reglas solo permiten leer a los miembros |
| I.3 Operaciones atómicas e idempotentes | ✅ | Transacciones de Firestore; ids deterministas para partidos y planificación |
| II.1–II.6 Arquitectura del cliente | ✅ | Módulos `*/data-firebase` detrás de las mismas interfaces; `Result<T, DataError>` |
| II.3 Offline-first con Room | ⚠️ | En la variante Firebase, la caché offline de Firestore sustituye a Room (ADR-0010 §6). La variante `api` no cambia |
| III Calidad | ✅ | Tests de lógica, reglas y callables en el emulador, también en el CI |
| IV Seguridad | ✅ | Sin secretos (los ficheros de Firebase siguen fuera de git); App Check; minimización (el email no se guarda en Firestore) |
| V Contrato | ⚠️ | El contrato de la variante Firebase está en `specs/contracts/firebase.md`. `api-v1.md` no cambia |

## Estructura
```
build-logic   SQUADFY_BACKEND=firebase|api y opciones del emulador → BuildKonfig
core/domain   contratos nuevos agnósticos: AnalyticsTracker, PresenceRepository;
              AuthRepository neutral (sin refresh token ni tokens de email)
core/firebase NUEVO (como TensoScan): init, emuladores, sesión, errores (safeFirebaseCall),
              callables (FunctionsClient), Analytics, Crashlytics, Remote Config (flags),
              Messaging (tokens), Storage, App Check, FirebaseAuthRepository
feature/chat/data-firebase  NUEVO: chats y mensajes (Firestore), participantes, presencia (RTDB), fotos
feature/club/data-firebase  NUEVO: clubes, miembros, horario, convocatorias, partidos,
                            clasificaciones, ausencias y ajustes (lecturas por listener, escrituras por callables)
core/data, feature/*/data, feature/*/database  variante `api`, congelada (sin cambios funcionales)
composeApp    elige el conjunto de módulos Koin según BuildKonfig.BACKEND
firebase/     NUEVO: firebase.json, .firebaserc.example, firestore.rules, firestore.indexes.json,
              storage.rules, database.rules.json, remoteconfig.template.json, hosting/ (legal,
              borrado de cuenta, acción de Auth), functions/ (TypeScript)
```

**Versiones.** Se alinean con GitLive 2.7.0, compilado con Kotlin 2.2.21; Squadfy usa la 2.2.20 y son compatibles.
- Android: BoM 34.18.0, plugins google-services 4.5.0 y Crashlytics 3.0.8.
- iOS: SDK 11.15.0 por SPM, añadiendo Auth, Firestore, Database, Functions, Storage, RemoteConfig, AnalyticsCore, Crashlytics y AppCheck.

## Modelo de datos (Firestore, `europe-west1`)
```
users/{uid}                       username, usernameLower, pictureUrl, createdAt   (sin email)
users/{uid}/devices/{fcmToken}    platform, locale, timeZone, appVersion, updatedAt
users/{uid}/clubSettings/{clubId} muted
clubs/{clubId}                    name, description, logoUrl, ownerId, invitationCode, maxMembers,
                                  membersCount, createdAt
clubs/{clubId}/members/{uid}      memberId = uid (al volver se recupera el mismo id); username, pictureUrl,
                                  clubPictureUrl, shirtNumber, position, role, joinedAt, leftAt, bannedAt
clubs/{clubId}/schedule/current   dayOfWeek, time, timeZone, format, durationMinutes, closeRule, drawRule, active
clubs/{clubId}/scheduleExceptions/{yyyy-MM-dd}  type (CANCELLED|RESCHEDULED), newDate, newTime
clubs/{clubId}/matches/{matchId}  id determinista "yyyyMMddTHHmm"; status, scheduledAt, format, duration,
                                  manualScore, teamA[], teamB[] (memberId|guestId, minutes), events[],
                                  ratingChanges{memberId: delta}, balance, completedAt
clubs/{clubId}/announcements/{matchId}  status, opensAt, closesAt, drawAt, maxPlayers, entries[] (miembros e
                                  invitados, con status), openedNotifiedAt, closingReminderSentAt,
                                  teamsPublishedAt (con centinela para las consultas del job)
clubs/{clubId}/ratings/{memberId} rating, matchesPlayed
clubs/{clubId}/absences/{id}      memberId, fromDate, toDate, reason
invitationCodes/{CODE}            clubId (solo lo leen las Functions)
chats/{chatId}                    participantIds[], participants{uid: {username, pictureUrl}}, lastMessage, updatedAt
chats/{chatId}/messages/{id}      senderId, content, createdAt
```
- Las inscripciones van **dentro** de la convocatoria (como mucho unas 30), así que apuntarse es una única transacción.
- Las estadísticas por periodo las calcula la callable `getStats`, leyendo los partidos `COMPLETED` del periodo. A esta escala son pocas lecturas y no hace falta mantener contadores.

**Realtime Database:**
- `presence/{uid}`: `online` y `lastSeen` con `onDisconnect`;
- `typing/{chatId}/{uid}`.

**Storage** (solo imágenes, 5 MB):
- `users/{uid}/avatar.jpg`;
- `clubs/{clubId}/logo.jpg`, que solo escribe un manager;
- `clubs/{clubId}/members/{uid}.jpg`.

## Functions (TypeScript, 2.ª gen.)
- **Callables:**
  - **Clubes:** `createClub`, `joinClub`, `updateClub`, `regenerateInvitationCode`, `transferOwnership`, `leaveClub`, `kickMember`, `banMember`, `unbanMember`, `changeRole`, `updateMyMembership`.
  - **Horario y partidos:** `setSchedule`, `addScheduleException`, `removeScheduleException`, `createExtraMatch`, `cancelMatch`.
  - **Convocatoria:** `enroll`, `withdraw`, `addGuest`, `removeGuest`.
  - **Equipos y acta:** `drawTeams`, `setScore`, `clearScore`, `addEvent`, `removeEvent`, `setMinutes`, `completeMatch`, `reopenMatch`, `getTeamBalance`.
  - **Clasificaciones:** `getStats`, `getRatings`.
  - **Ausencias y ajustes:** `createAbsence`, `deleteAbsence`, `setClubMuted`.
  - **Chat y cuenta:** `createChat`, `addChatParticipants`, `leaveChat`, `deleteAccount`.
- **Programadas:**
  - `planUpcomingMatches`: cada hora;
  - `closeAndDraw`: cada 5 minutos;
  - `announcementReminders`: cada 5 minutos.
- **Triggers:**
  - `onMessageCreated`: push del chat y `lastMessage`;
  - `onUserProfileWritten`: replica el nombre y la foto en los miembros y en los chats.
- La lógica pura se porta desde `Squadfy_Backend/match/.../domain/model` con sus tests: calendario y plazos, asignador de plazas, equilibrador, Elo, estadísticas y política de roles.

## Estrategia de test
| AC | Test |
|---|---|
| 01, 02 | Tests de BuildKonfig y del `FirebaseInitializer` con fakes; los dos builds en el CI |
| 03 | `FirebaseErrorMapperTest` (commonTest) |
| 04–06 | `FirebaseAuthRepository` con fakes de la sesión; E2E en el emulador |
| 07–13 | Functions: tests unitarios de la lógica pura (Jest) y tests de integración de las callables contra el emulador |
| 14–17 | Mappers DTO↔dominio (commonTest); tests de reglas de Firestore, Storage y RTDB |
| 18–20 | Fakes de `AnalyticsTracker` y de Remote Config; `FeatureFlagResolver` ya testeado |
| 21 | Job del CI: `firebase emulators:exec "npm test"` |
| 22 | `testDebugUnitTest` con `SQUADFY_BACKEND=api` en el CI |
