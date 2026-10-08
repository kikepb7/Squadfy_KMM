# 015 · Tareas

- [x] **T-001** D-12: `primary` del tema claro pasa a Brand900, con contenido blanco (AC-015-01).
- [x] **T-002** D-13: flags por plataforma (`AppPlatform`, `defaultInProOnIos`), flag `CHAT` y pestaña condicionada (AC-015-02).
- [x] **T-003** `SignOutUseCase` compartido; el Perfil se abre desde Inicio, con cierre de sesión y acceso a los flags en PRE (AC-015-03).
- [x] **T-004** Búsqueda parcial de usuarios en Crear chat (AC-015-04). `SearchChatParticipantsUseCase` (mínimo 2 caracteres) → `GET /users/search?q=`; Crear chat y Añadir miembros muestran todas las coincidencias y se añaden con un toque. Tests: use case (3), MockEngine (2) y `CreateChatViewModelTest`.
- [x] **T-005** Foto por club: `pictureUrl` en las listas de miembros, más subir y quitar la foto en «Mi ficha» (AC-015-05). Room v4 con la migración `3→4` (`ADD COLUMN clubPictureUrl`), `PUT`/`DELETE /clubs/{id}/members/me/picture`; los avatares de miembros, convocatoria, equipos y ficha usan `pictureUrl`. Tests: esquema v4 (`ClubDatabaseMigrationsSchemaTest`), fixture del DTO y `MemberClubPictureTest` (2).
- [ ] **T-006** Tiempo real: `CLUB_DATA_CHANGED` → refresco de Partido, horario y ausencias (AC-015-06).
- [ ] **T-007** Estadísticas por periodo (AC-015-07).
- [ ] **T-008** Workflow de release: staging para `release/*`, producción para los tags (AC-015-08).
- [ ] **T-009** Eliminar los mocks de Inicio y sus flags (AC-015-09).
- [ ] **T-010** Contrato y gap analysis (AC-015-10).
- [ ] **T-011** Verificación: tests, builds, iOS y E2E contra el backend local actualizado.
