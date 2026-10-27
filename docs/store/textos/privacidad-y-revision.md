# Privacidad, clasificación y revisión (variante Firebase, 2026-10)

Actualiza `specs/features/012-go-live/privacy-data.md`, que se escribió para el backend propio. La política publicada está en `firebase/hosting/legal/privacy.html`.

## URLs (hay que desplegar Hosting antes de enviar la app)
- **Política de privacidad:** `https://squadfy-5233d.web.app/legal/privacy`
- **Borrado de cuenta** (Play › Seguridad de los datos): `https://squadfy-5233d.web.app/account/delete`
- **Soporte:** **[owner]** un email y una web; puede valer `https://squadfy-5233d.web.app`.

## SDK y permisos
- **SDK de Firebase:**
  - Authentication: email, Google y Apple.
  - Firestore, Realtime Database y Storage.
  - Cloud Functions.
  - Cloud Messaging.
  - Crashlytics y Analytics, **apagados por defecto** y solo con consentimiento en Perfil.
  - Remote Config.
  - App Check: Play Integrity en Android y App Attest en iOS.
- **Sin** publicidad, sin identificador publicitario y sin seguimiento.
- **Permisos:**
  - Android: Internet y notificaciones.
  - iOS: notificaciones remotas.
  - Las fotos se eligen con el selector del sistema, que no pide permiso.

## Google Play › Seguridad de los datos
- **¿Recoge datos?** Sí. **¿Los comparte?** No: Firebase es un proveedor de servicio.
- **¿Cifrado en tránsito?** Sí.
- **¿Se puede pedir el borrado?** Sí, desde la app (Perfil › Eliminar cuenta) y desde la URL de borrado.

| Categoría | Tipo | Opcional | Finalidad |
|---|---|---|---|
| Información personal | Dirección de correo | No | Funcionalidad de la app, gestión de la cuenta |
| Información personal | ID de usuario (nombre de usuario) | No | Funcionalidad, gestión de la cuenta |
| Fotos y vídeos | Fotos (perfil y escudo) | Sí | Funcionalidad |
| Mensajes | Otros mensajes en la app (chat) | Sí | Funcionalidad |
| Actividad en la app | Otro contenido generado por el usuario (convocatorias, acta, ausencias, motivos) | No | Funcionalidad |
| Actividad en la app | Interacciones con la app (pantallas vistas) | Sí, opt-in | Analíticas |
| Información y rendimiento de la app | Registros de fallos y diagnósticos | Sí, opt-in | Analíticas (diagnóstico) |
| Identificadores de dispositivo u otros | ID del dispositivo (token de notificaciones, ID de instalación) | Sí | Funcionalidad (notificaciones), seguridad (App Check) |

## App Store Connect › Privacidad de la app
- **Seguimiento:** No. No hace falta el aviso de App Tracking Transparency.
- **Datos vinculados a ti** (finalidad: funcionalidad de la app):
  - email;
  - ID de usuario;
  - fotos;
  - otro contenido del usuario;
  - ID del dispositivo.
- **Datos no vinculados a ti** (opt-in): datos de fallos y otros diagnósticos (funcionalidad); interacción con el producto (analíticas).

## Clasificación de contenido (propuesta, la decide el owner)
- **Play (IARC):**
  - sin violencia, sexo, drogas ni apuestas;
  - **los usuarios interactúan** y comparten contenido (chat entre miembros).
  - Público objetivo: 16+.
- **App Store:**
  - 12+, por contenido generado por los usuarios;
  - mientras el chat esté oculto en iOS (D-13), puede bastar con 4+. Hay que confirmarlo en el cuestionario.

## Notas para la revisión (App Store y Play)
```
Squadfy organiza el partido semanal de un club de fútbol amateur.
Cuenta de demo: [owner: email y contraseña de una cuenta de prueba en PRO, miembro de un club con partidos jugados].
1. Pestaña Clubes › Albaicín FC (club de demo).
2. Partido: apuntarse/desapuntarse. Equipos y Tabla muestran el sorteo y la clasificación.
3. Perfil › Eliminar cuenta borra la cuenta (no lo hagáis con la cuenta de demo).
El inicio de sesión con Apple está disponible junto a Google y email.
```
