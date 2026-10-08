# 012 · Datos tratados (Data Safety de Play y etiquetas de privacidad de App Store)

Inventario sacado del código de la release (2026-10-08). SDKs incluidos: Firebase Cloud Messaging y Firebase Crashlytics (solo Android y con consentimiento). No hay SDKs de analítica, publicidad ni seguimiento. Permisos: Android pide `INTERNET` y `POST_NOTIFICATIONS`; iOS pide notificaciones remotas. Las fotos se eligen con el selector del sistema, que no necesita permiso de galería.

Si cambia algo de esto (otro SDK, analítica, ubicación…), hay que actualizar este documento y las dos fichas.

## Inventario
| Dato | Para qué | Dónde se guarda | ¿Obligatorio? | ¿Se borra con la cuenta? |
|---|---|---|---|---|
| Email | Cuenta, login, recuperar la contraseña | Backend (Postgres) | Sí | Sí (spec 010 del backend, RN-A3) |
| Nombre de usuario | Identificarse en clubes y chat | Backend | Sí | Sí, queda «Usuario eliminado» en el historial de los clubes (RN-A5) |
| Contraseña | Autenticación (hash en el backend) | Backend | Sí | Sí |
| Foto de perfil | Avatar | Almacenamiento del backend (Supabase Storage) | No | Sí |
| Datos del club: dorsal, posición, rol, convocatorias, eventos de partido, rating y estadísticas | Funcionalidad principal | Backend | Sí, al unirse a un club | Se anonimizan (RN-A5) para no descuadrar los partidos de los demás |
| Mensajes de chat | Chat entre miembros | Backend | No | Sí, todos (RN-A3) |
| Ausencias (fechas y motivo) | Retirarte de las convocatorias | Backend | No | Sí |
| Token de push (FCM/APNs) e identificador de instalación de Firebase | Enviar notificaciones | Backend (dispositivos) y Firebase | No (se puede negar el permiso) | Sí |
| Datos de fallos (traza, modelo, versión del SO, estado de la app) | Corregir errores | Firebase Crashlytics | No: opt-in en Perfil, desactivado por defecto, solo Android | Al retirar el consentimiento deja de enviarse; Crashlytics conserva los informes 90 días |
| Sesión (tokens) | Mantener la sesión | Dispositivo, cifrada (Android Keystore; Data Protection en iOS) | — | Se borra al cerrar sesión o borrar la cuenta |

Todo el tráfico va cifrado (HTTPS/WSS; la release rechaza `http`). No se vende ni se cede ningún dato. Firebase y Supabase actúan como proveedores de servicio (encargados del tratamiento), y Play no lo considera «compartir».

## Play Console › Seguridad de los datos
- **¿Recoge o comparte datos?** Recoge: sí. Comparte: no.
- **¿Cifrado en tránsito?** Sí.
- **¿Se puede pedir el borrado?** Sí: dentro de la app (Perfil › Eliminar cuenta) y en la web `ACCOUNT_DELETION_URL` (`https://<api>/account/delete`).
- **Tipos de datos:**

| Categoría de Play | Tipo | Recogido | Opcional | Fines |
|---|---|---|---|---|
| Información personal | Dirección de email | Sí | No | Funcionalidad de la app, Gestión de la cuenta |
| Información personal | ID de usuario (nombre de usuario) | Sí | No | Funcionalidad de la app, Gestión de la cuenta |
| Fotos y vídeos | Fotos | Sí | Sí | Funcionalidad de la app (foto de perfil y escudo del club) |
| Mensajes | Otros mensajes en la app (chat) | Sí | Sí | Funcionalidad de la app |
| Actividad en la app | Otro contenido generado por el usuario (convocatorias, eventos de partido, ausencias) | Sí | No | Funcionalidad de la app |
| Información y rendimiento de la app | Registros de fallos, diagnósticos | Sí | Sí | Analíticas (diagnóstico de fallos) |
| Identificadores del dispositivo u otros | ID de dispositivo (token FCM e ID de instalación) | Sí | Sí | Funcionalidad de la app (notificaciones) |

No se marcan: ubicación, contactos, calendario, salud, finanzas, audio, archivos, historial web ni apps instaladas.

## App Store Connect › Privacidad de la app
- **Seguimiento (tracking):** No. No hace falta el aviso de App Tracking Transparency.
- **Datos vinculados a la identidad del usuario** (fin: Funcionalidad de la app):
  - Información de contacto › Dirección de email
  - Contenido del usuario › Fotos, Otros contenidos del usuario (chat, convocatorias, eventos)
  - Identificadores › ID de usuario, ID del dispositivo (token de push)
- **Datos no vinculados:** ninguno. Crashlytics no está en iOS dentro del MVP; si se añade, se declaran «Datos de fallos» y «Otros datos de diagnóstico» como no vinculados y con fin de funcionalidad de la app.

## Política de privacidad (texto que tiene que cubrir la página `PRIVACY_POLICY_URL`)
1. Responsable y contacto (email de soporte).
2. Los datos del inventario y para qué se usan.
3. Encargados: Google Firebase (notificaciones y, con consentimiento, informes de fallos), Supabase (base de datos y archivos) y el proveedor de hosting que elija la decisión D-4 del backend.
4. Base legal: ejecución del servicio (cuenta, clubes, chat) y consentimiento (informes de fallos y notificaciones).
5. Conservación: mientras exista la cuenta. Al borrarla, lo personal se elimina al momento y el historial de partidos queda anonimizado (RN-A5).
6. Derechos y cómo ejercerlos: borrado en la app o en la web, más acceso y rectificación por email.
7. Menores: hay que decidir la edad mínima (pregunta para el owner, junto con la clasificación de contenido).
