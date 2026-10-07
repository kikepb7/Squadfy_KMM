# 003 · Plan técnico

## Cumplimiento de la constitución
| Principio | Cumple | Nota |
|---|---|---|
| I | ✅ | `MemberPermissions` solo decide qué se muestra; el 403 del servidor manda |
| II | ✅ | Enums en domain; un único `ClubRepository` (se elimina `ClubService`) |
| III | ✅ | `Migration(2,3)` con test; se elimina el modo destructivo; strings en recursos |
| IV | ✅ | Se deja de guardar el email de otros miembros |
| V | ✅ | Fixtures de `ClubDto` y `ClubMemberDto` de `BACKEND.md` §9 |

## domain (`feature/club/domain`)
- `model/ClubMemberRole.kt`, `PlayerPosition.kt`, `ClubMember.kt` (sin stats ni email; con `createdAt`).
- `policy/MemberPermissions.kt`: `fun of(actor: ClubMember, target: ClubMember?): Permissions`. Es pura y testeada con una tabla de 4×4 combinaciones de roles más el caso «sobre sí mismo».
- `error/ClubError.kt`: `sealed`, con `InvalidInvitationCode`, `Banned`, `AlreadyMemberOrFull`, `Forbidden`, `MaxMembersBelowCurrent` y `Remote(RemoteError)`, mapeados desde `BackendErrorCode` y el estado HTTP.
- `ClubRepository` gana `editClub`, `regenerateInvitationCode`, `updateMyMembership`, `leave`, `removeMember`, `changeRole`, `transferOwnership`, `ban`, `unban` y `getBans`.

## data
- `dto/` v1: `ClubDto`, `ClubMemberDto`, `ClubBanDto`, `InvitationCodeDto` y las requests. Las rutas siguen `api-v1.md` (§Clubes).
- `OfflineFirstClubRepositoryImpl`:
  - las mutaciones actualizan Room con el DTO devuelto;
  - `leave`, `remove` y `ban` borran de Room el club o el miembro;
  - `syncClubs` hace un upsert y elimina los que ya no están.

## database
- `ClubEntity` v3 y `ClubMemberEntity` v3.
- `Migration(2,3)`: tablas nuevas, copia de las columnas que se conservan y drop de las antiguas (SQLite: create–copy–drop–rename).
- Se exporta `3.json` y se añade `ClubDatabaseMigrationTest` (androidUnitTest con Robolectric, o test instrumentado si el driver no lo permite).

## presentation
- `clubs/ClubsListScreen` (nueva) en la pestaña Clubs. Setup queda como el estado vacío o la acción de crear o unirse.
- `detail/settings/*`: secciones por rol (AC-003-10) y diálogos de edición.
- `member/MemberDetailScreen`: acciones según `MemberPermissions`. Se eliminan los bloques de estadísticas (pasan a la spec 008).
- Navegación: `onClubCreated(clubId)` → `navigate(ClubDetailRoute(clubId)) { popUpTo<SetupGraph> { inclusive = true } }`.

## Tests
| AC | Test |
|---|---|
| 04 | `JoinClubUseCaseTest`, `JoinClubViewModelTest` |
| 05 | `ClubMemberMapperTest` (fixture + rol o posición desconocidos) |
| 06/07 | `MemberPermissionsTest` (tabla), `MemberDetailViewModelTest` |
| 10–13 | `ClubSettingsViewModelTest` |
| 15 | `ClubDatabaseMigrationTest` |
