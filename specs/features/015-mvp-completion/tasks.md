# 015 · Tareas

- [x] **T-001** D-12: `primary` del tema claro pasa a Brand900, con contenido blanco (AC-015-01).
- [x] **T-002** D-13: flags por plataforma (`AppPlatform`, `defaultInProOnIos`), flag `CHAT` y pestaña condicionada (AC-015-02).
- [x] **T-003** `SignOutUseCase` compartido; el Perfil se abre desde Inicio, con cierre de sesión y acceso a los flags en PRE (AC-015-03).
- [x] **T-004** Búsqueda parcial de usuarios en Crear chat (AC-015-04). `SearchChatParticipantsUseCase` (mínimo 2 caracteres) → `GET /users/search?q=`; Crear chat y Añadir miembros muestran todas las coincidencias y se añaden con un toque. Tests: use case (3), MockEngine (2) y `CreateChatViewModelTest`.
- [x] **T-005** Foto por club: `pictureUrl` en las listas de miembros, más subir y quitar la foto en «Mi ficha» (AC-015-05). Room v4 con la migración `3→4` (`ADD COLUMN clubPictureUrl`), `PUT`/`DELETE /clubs/{id}/members/me/picture`; los avatares de miembros, convocatoria, equipos y ficha usan `pictureUrl`. Tests: esquema v4 (`ClubDatabaseMigrationsSchemaTest`), fixture del DTO y `MemberClubPictureTest` (2).
- [x] **T-006** Tiempo real: `CLUB_DATA_CHANGED` → refresco de Partido, horario y ausencias (AC-015-06). `ClubLiveUpdates` en `core/domain` y `WebSocketClubLiveUpdates` en `chat/data`, sobre el mismo socket. El flujo del conector pasa a compartido (`shareIn`): antes, cada colector habría abierto su propio WebSocket. Partido se refresca con cualquier cambio del club; el detalle, solo con su partido; Horario con `SCHEDULE` y Ausencias con `ABSENCES`. Tests: parseo (3), Partido y detalle de partido.
- [x] **T-007** Estadísticas por periodo (AC-015-07). `StatsPeriod` (todo, este año y últimos 30 días; «hoy» en la zona horaria del club) → `from`/`to` inclusivos en `GET /stats`; chips en Clasificación › Estadísticas. Tests: VM (rangos) y MockEngine (parámetros).
- [x] **T-008** Workflow de release: staging para `release/*`, producción para los tags (AC-015-08). Los candidatos se llaman `X.Y.Z-rc.N` (`-PversionNameSuffix`) y usan `STAGING_BASE_URL_*`; los tags `vX.Y.Z` usan `PRO_BASE_URL_*`. Verificado: el APK con el sufijo sale como `1.0.0-rc.7`.
- [x] **T-009** Eliminar los mocks de Inicio y sus flags (AC-015-09). Se retiran `HOME_RECENT_MATCHES` y `HOME_NEWS`, los componentes, mappers y modelos de ejemplo, sus textos y los módulos `feature:globalPosition:domain`/`data`, que solo contenían datos de ejemplo.
- [x] **T-010** Contrato y gap analysis (AC-015-10). En `api-v1.md`: `/users/search`, la foto por club, `from`/`to`, `CLUB_DATA_CHANGED`, `/features` (sin uso) y la página de verificación. En el gap analysis: BE-GAP-2/4/6/8/9 resueltos, BE-GAP-3 en curso (Render) y BE-GAP-10 nuevo (moderación del chat).
- [x] **T-011** Verificación: tests, builds, iOS y E2E contra el backend local actualizado. 349 tests en verde, más `assembleDebug`, la compilación iOS, `checkHardcodedStrings` y ktlint. E2E en el emulador contra el backend `master` actual (specs 010–013), con usuarios nuevos en el Postgres local:
  - Perfil desde Inicio y cierre de sesión. Apareció un crash: el flujo del token FCM volvía a emitir tras `firstOrNull()`. Corregido (`FIX | 015 · T-011`).
  - Búsqueda parcial «15b» → `e2e15b…`.
  - Tiempo real: B se apunta por API y la pantalla de A pasa a 1/10 sin tocarla, también en la release minificada.
  - Estadísticas por periodo: 200 con `from`/`to`.
  - Borrado de cuenta real de B desde la app: vuelve al login, su login da 401, sale de los miembros y de la convocatoria.
  - Sin probar en E2E: la subida de la foto por club, porque el backend local no tiene almacenamiento (Supabase en `localhost:54321`). Está cubierta por los tests de VM, DTO y migración.
