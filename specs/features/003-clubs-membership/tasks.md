# 003 · Tareas

## Fase 1 · Dominio
- [~] **T-001** Enums `ClubMemberRole` y `PlayerPosition` con mapeo seguro ✅. Falta el modelo `ClubMember` v1, que se migra junto a los DTOs en T-005 (AC-003-05).
- [x] **T-002** `MemberPermissions` + test de tabla (AC-003-06). *Verificación:* `MemberPermissionsTest`.
- [x] **T-003** `ClubError` + mapeo desde `RemoteError` según la operación + test (AC-003-04/07/11). *Verificación:* `ClubErrorTest`.
- [ ] **T-004** Ampliar `ClubRepository` y añadir los use cases de las mutaciones.

## Fase 2 · Data y Room
- [ ] **T-005** DTOs v1 + mappers + tests con fixtures (AC-003-05).
- [ ] **T-006** `OfflineFirstClubRepositoryImpl` con las rutas v1 + test con MockEngine.
- [ ] **T-007** Room v3 + `Migration(2,3)` + test + quitar el modo destructivo + `3.json` (AC-003-15).
- [ ] **T-008 (P)** Eliminar el código muerto y unificar los modelos de club de globalPosition (AC-003-16).

## Fase 3 · Presentación
- [ ] **T-009** `ClubsListScreen` en la pestaña Clubs + estado vacío (AC-003-01).
- [ ] **T-010** Navegación tras crear o unirse + aviso de fallo del logo (AC-003-02/03).
- [ ] **T-011** Unirse: chips de posición, validaciones y errores tipados (AC-003-04).
- [ ] **T-012** Ficha del miembro: acciones según los permisos y confirmaciones (AC-003-07/08).
- [ ] **T-013** Ajustes por rol: compartir o copiar el código, editar, logo, regenerar, salir y transferir (AC-003-10/11/13/14).
- [ ] **T-014** Pantalla de vetados (AC-003-12).
- [ ] **T-015** Strings ES/EN de toda la feature.

## Fase 4 · Verificación
- [ ] **T-016** E2E con dos usuarios contra el backend local: crear, unirse, cambiar rol, expulsar, volver, vetar, que el vetado no pueda unirse, levantar el veto, transferir y que el owner antiguo salga.
