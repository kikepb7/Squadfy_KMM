# 009 · Notificaciones push del ciclo de partido

- **Estado:** Draft
- **Reglas:** BE-005 RN-1…9, APP-RN-08, APP-RN-14
- **Backend:** BE-005, `BACKEND.md` §13
- **Depende de:** 002 (`/devices`), 005, 006
- **Repos afectados:** Squadfy_App (+ configuración de Firebase y APNs)

## Problema / objetivo
El backend ya envía push de todo el ciclo de partido. En la app, en cambio:
- en Android no hay `onMessageReceived` ni canal de notificación;
- el deep link de chat estaba roto (se arregló en la spec 001);
- no hay ninguna navegación para las push de partido.

## Criterios de aceptación
- **AC-009-01** **Registro**:
  - tras el login, si hay permiso, se registra el token FCM con `POST /devices {token, platform}`;
  - con `onNewToken`, se vuelve a registrar;
  - en el logout, se da de baja con `DELETE /devices/{token}`;
  - el permiso se pide (Android 13+ e iOS) la primera vez que se entra en un club, no en el arranque, con una explicación previa.
- **AC-009-02** **Android**: hay un canal `match_updates` («Partidos») y otro `chat` («Mensajes»). `onMessageReceived` muestra la notificación cuando la app está en primer plano.
- **AC-009-03** **Enrutado** por `data.type` (en Android y en iOS):

  | `type` | Destino |
  |---|---|
  | `match.announcement.opened`, `.closing_soon`, `.waitlist.promoted` | `squadfy://club/{clubId}/announcement`, que abre la pestaña Partido del club |
  | `match.teams.published` | `squadfy://match/{matchId}?clubId={clubId}` |
  | `match.cancelled` | Pestaña Partido del club |
  | `match.rescheduled` (BE-008 RN-B3) | Pestaña Partido del club |
  | `new_message` | `squadfy://chat_details/{chatId}` |
  | Desconocido | Abrir la app sin navegar |
- **AC-009-04** Al recibir una push en primer plano cuyo `clubId` es el club abierto, la pantalla correspondiente se refresca (APP-RN-08) y se muestra un snackbar en lugar de la notificación del sistema.
- **AC-009-05** En Ajustes del club, el interruptor «Silenciar notificaciones» usa `GET/PUT /clubs/{id}/notification-settings` y debajo explica «Seguirás recibiendo el aviso si te toca plaza».
- **AC-009-06** **iOS**:
  - el `UNUserNotificationCenter` delegate enruta con el mismo mapa;
  - en release, `aps-environment = production`;
  - la clave APNs está subida a Firebase (manual).
- **AC-009-07** Si `FIREBASE_ANDROID_PACKAGE` se define en el backend, coincide con el `applicationId`, incluido `.debug` si existe. Esto se documenta en la spec 012.
- **AC-009-08** Hay un test unitario del router: el mapa de `data` → ruta, con todos los tipos y con un tipo desconocido.
