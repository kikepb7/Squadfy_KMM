# 021 · Tareas

## Fase 1 · Servidor (Functions)
- [x] **T-001** El silencio por club se aplica en todos los avisos (`recipientsOf`), salvo «tienes plaza», el cambio de rol y la transferencia (APP-RN-P1). Verificación: `notifications.test.ts` (AC-021-01).
- [x] **T-002** Avisos en el idioma de cada dispositivo (`locale` de `users/{uid}/devices`), con textos ES y EN en `notifications/messages.ts` (APP-RN-P2). Verificación: `messages.test.ts`.
- [x] **T-003** Recordatorio 2 h antes, una sola vez (`runMatchReminders`, marca `reminderSentAt`, índice de grupo de colección en `matches`) y resultado con el cambio de rating al cerrar el acta (APP-RN-P3, P4). Verificación: `notifications.test.ts` (AC-021-03/04).
- [x] **T-004** Temporada empezada y cerrada (con el líder), una vez por cambio (APP-RN-P5); alta de miembro a los gestores, cambio de rol y transferencia al miembro (APP-RN-P6). Verificación: `notifications.test.ts` (AC-021-06) y `seasons.test.ts`.
- [x] **T-005** Canal de Android por tipo (`channelOf`) y script del owner `scripts/send-app-notice.mjs` para los avisos de la app por *topic*, con `--env pre|pro` (APP-RN-P7).

## Fase 2 · App
- [x] **T-006** `PushRouter`: el recordatorio abre la pestaña Partido, el resultado abre el partido, los avisos de club y temporada abren el club y los de la app abren la tienda o la web (`https://` solo). Canales «Club y temporada» y «Novedades de Squadfy». Verificación: `PushRouterTest` (AC-021-03/06/07).
- [x] **T-007** Suscripción a los *topics* al tener token: Android con Firebase Messaging; iOS con un puente Swift (`PushTopicBridge` en `AppDelegate`). Verificación: `SyncPushTopicsUseCaseTest` (AC-021-07).
- [x] **T-008** Aviso de nueva versión desde Remote Config (`latest_version`, `min_version`, `store_url`): bloquea por debajo de la mínima y se puede descartar por debajo de la última. Verificación: `AppUpdatePolicyTest` (AC-021-08).
- [x] **T-009** Eventos de Analytics sin ids ni nombres: `club_created`, `club_joined`, `match_enrolled`, `teams_generated` (modo), `match_completed`, `schedule_exception_added` (tipo), `season_updated`, `update_prompt_shown` y `update_prompt_accepted`. Verificación: `AnalyticsEventsTest` (AC-021-09).
- [x] **T-010** El dispositivo guarda `appVersion` (para saber qué versiones siguen en uso antes de subir `min_version`).

## Fase 3 · Configuración y verificación
- [x] **T-011** Remote Config con una condición por app y los parámetros de versión, desplegado el 2026-10-27 (plantilla v2) (AC-021-10).
- [ ] **T-012** Desplegar las Functions (020 y 021) y el índice de `matches` (con permiso del owner).
- [ ] **T-013** Prueba en dispositivo: recibir cada tipo de aviso en ES y EN, silenciar un club, `send-app-notice.mjs --env pre` en una build Dev. En iOS hace falta la clave APNs (`docs/FIREBASE.md` §3).
