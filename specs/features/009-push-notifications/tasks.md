# 009 · Tareas

## Fase 1 · Dominio
- [x] **T-001** `PushRouter` (tipo → destino → deep link) + `PushRouterTest` con todos los tipos, el chat sin `type` y los desconocidos (AC-009-03/08).
- [x] **T-002** `InAppPushCenter` (clubes visibles y push en primer plano) + test (AC-009-04); `NotificationPromptStore` en DataStore.

## Fase 2 · Data
- [x] **T-003** `KtorNotificationSettingsRepository` (`GET/PUT /clubs/{id}/notification-settings`) + use cases para silenciar (AC-009-05).
- [x] **T-004** Logout: baja con el token FCM antes de borrar la sesión + test (AC-009-01).

## Fase 3 · Presentación y plataformas
- [x] **T-005** Deep links `squadfy://club/{clubId}/announcement` y `squadfy://match/{matchId}?clubId=` en el grafo del club.
- [x] **T-006** La pestaña Partido y el detalle del partido se registran como visibles y, al recibir una push de su club, refrescan y muestran un snackbar + tests (AC-009-04).
- [x] **T-007** Android: `SquadfyMessagingService` en `composeApp` (`onNewToken`, `onMessageReceived`), canales `match_updates` y `chat`, icono monocromo y `MainActivity` enruta los extras de cualquier push (AC-009-02/03).
- [x] **T-008** iOS: `IosPushBridge` + `AppDelegate` (`willPresent` decide si se muestra el banner; `didReceive` enruta) (AC-009-03/04/06).
- [x] **T-009** Explicación previa al permiso la primera vez que se abre un club, y el interruptor «Silenciar notificaciones» en Ajustes con su aclaración (AC-009-01/05).

## Fase 4 · Verificación
- [ ] **T-010** Manual: subir la clave APNs a Firebase; comprobar que `FIREBASE_ANDROID_PACKAGE` coincide con el `applicationId` (AC-009-06/07, se documenta en la 012).
- [ ] **T-011** E2E con push reales: abrir la convocatoria → push → al tocarla, se abre la pestaña Partido; con el club abierto, snackbar y refresco; silenciar el club → no llegan push salvo la de plaza conseguida.
