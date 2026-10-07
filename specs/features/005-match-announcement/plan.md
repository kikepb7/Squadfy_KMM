# 005 · Plan técnico

## Cumplimiento de la constitución
| Principio | Cumple | Nota |
|---|---|---|
| I | ✅ | La policy es solo de UX; el servidor rechaza fuera de ventana (400) |
| II | ⚠️→✅ | Network-first con caché en memoria, justificado en el ADR-0006 |
| III | ✅ | Tabla de ejemplos como test; `Clock` inyectado |
| V | ✅ | Fixture real de `CurrentMatchAnnouncementDto` (`BACKEND.md` §9) |

## domain
- `model/`:
  - `MatchAnnouncement(id, matchId, clubId, maxPlayers, confirmedCount, waitlistCount, opensAt, closesAt, status: AnnouncementStatus, entries, waitlist)`;
  - `AnnouncementEntry(id, clubMemberId, status, enrolledAt)`;
  - `CurrentAnnouncement(announcement, matchScheduledAt, myStatus: MyEnrollmentStatus, myWaitlistPosition: Int?)`;
  - enums `AnnouncementStatus { OPEN, CLOSED, CANCELLED, UNKNOWN }` y `MyEnrollmentStatus { NOT_ENROLLED, CONFIRMED, WAITLISTED }`.
- `policy/AnnouncementWindowPolicy`:
  - `state(a, now): WindowState`;
  - `timeUntilClose(a, now): Duration?`.
- `repository/AnnouncementRepository`:
  - `observeCurrent(clubId): StateFlow<Cached<CurrentAnnouncement?>>`;
  - `refreshCurrent(clubId)`;
  - `enroll(announcementId)` y `withdraw(announcementId)`, que devuelven `Result<MatchAnnouncement, ClubError>`;
  - `history(clubId)`.
- `Cached<T>(value: T, fetchedAt: Instant, isStale: Boolean)`.

## data
- DTOs: `MatchAnnouncementDto`, `MatchAnnouncementEntryDto` y `CurrentMatchAnnouncementDto`, con valores por defecto y enums como `String`.
- `InMemoryAnnouncementRepository`: un `MutableStateFlow` por club. Si `refreshCurrent` recibe un 404, guarda `value = null`; si no hay red, marca `isStale = true` y conserva el último valor.
- Tras `enroll` o `withdraw`, fusiona la `announcement` devuelta con el `myStatus` recalculado. Se obtiene buscando mi `clubMemberId` en `entries` y en `waitlist`, y la posición en la lista de espera es el índice + 1.

## presentation
- `announcement/AnnouncementViewModel(clubId)`:
  - combina `observeCurrent`, los miembros (Room), el horario (zona horaria) y un ticker de 60 s basado en `Clock`;
  - produce `AnnouncementState`: `header`, `windowState`, `countdown`, `primaryAction`, `confirmed: List<MemberRowUi>`, `waitlist`, `isActing: Set<Action>` e `isStale`.
- `ObserveAsEvents` para los snackbars.
- `LifecycleResumeEffect` → refrescar.
- Se sustituye `MatchRoot` dentro de la pestaña; `MatchViewModel` se divide en `AnnouncementViewModel` (esta spec) y `MatchDetailViewModel` (spec 006/007).

## Tests
| AC | Test |
|---|---|
| 03 + tabla | `AnnouncementWindowPolicyTest` |
| 04/05 | `AnnouncementViewModelTest` (fake repository y un `TestClock`) |
| 06 | `MemberRowMapperTest` (incluye un miembro desconocido) |
| 07 | `AnnouncementViewModelTest` (errores) |
| 09 | `InMemoryAnnouncementRepositoryTest` (stale) |
