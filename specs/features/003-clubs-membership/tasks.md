# 003 · Tareas

## Fase 1 · Dominio
- [x] **T-001** Enums `ClubMemberRole` y `PlayerPosition` con mapeo seguro + modelos `ClubModel`/`ClubMemberModel` v1 (sin email, estadísticas ni horario) (AC-003-05).
- [x] **T-002** `MemberPermissions` + test de tabla (AC-003-06). *Verificación:* `MemberPermissionsTest`.
- [x] **T-003** `ClubError` + mapeo desde `RemoteError` según la operación + test (AC-003-04/07/11). *Verificación:* `ClubErrorTest`.
- [x] **T-004** `ClubRepository` v1 con `ClubError` + use cases de mutaciones, «mis clubes» con mi rol y mi membresía. *Verificación:* `ClubMembershipUseCasesTest`.

## Fase 2 · Data y Room
- [x] **T-005** DTOs v1 + mappers + tests con fixtures (AC-003-05). *Verificación:* `ClubDtoFixtureTest`.
- [x] **T-006** `OfflineFirstClubRepositoryImpl` con las rutas v1 (`/clubs`, `/members/me`, rol, expulsar, vetar, transferir, logo `PUT`). *El test con MockEngine queda pendiente: el repositorio necesita Room y el módulo no tiene Robolectric; se cubre con el E2E (T-016).*
- [x] **T-007** Room v3 + `Migration(1,3)`/`Migration(2,3)` (recrea la caché con el SQL exacto de `3.json`) + quitar el modo destructivo. *Verificación:* `ClubDatabaseMigrationsSchemaTest`.
- [x] **T-008 (P)** Código muerto eliminado (`ClubService`, `KtorClubRepositoryImpl`, `GlobalPositionService`, `KtorGlobalPositionRepositoryImpl`, foto por club, horario y excepciones antiguos). Inicio usa `/clubs` con el `ClubEntity` v3; la unificación completa de modelos de Inicio queda para la spec 010.

## Fase 3 · Presentación
- [x] **T-009** `ClubsListScreen` en la pestaña Clubs + estado vacío + pull-to-refresh (AC-003-01).
- [x] **T-010** Navegación tras crear o unirse + aviso de fallo del logo (AC-003-02/03).
- [x] **T-011** Unirse: chips de posición, validaciones (dorsal 1–999) y errores tipados (AC-003-04).
- [x] **T-012** Ficha del miembro: acciones según los permisos y confirmaciones; mi ficha editable (AC-003-07/08). *Verificación:* `MemberDetailStateTest`.
- [x] **T-013** Ajustes por rol: compartir o copiar el código, editar, logo, regenerar, vetados y salir (AC-003-10/11/13/14). La transferencia está en la ficha del miembro.
- [x] **T-014** Pantalla de vetados (AC-003-12).
- [~] **T-015** Strings ES/EN de las pantallas nuevas o reescritas. Quedan textos escritos a mano en crear, unirse y el banner, que pasan a la spec 011.

## Fase 4 · Verificación
- [ ] **T-016** E2E con dos usuarios contra el backend local: crear, unirse, cambiar rol, expulsar, volver, vetar, que el vetado no pueda unirse, levantar el veto, transferir y que el owner antiguo salga.
