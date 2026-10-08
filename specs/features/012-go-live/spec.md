# 012 · Puesta en producción (coordinada con el backend)

- **Estado:** Draft
- **Depende de:** todas las anteriores; BE-006 (despliegue pendiente), BE-GAP-1/2/3
- **Repos afectados:** Squadfy_Backend (despliegue), Squadfy_App (configuración de release), consolas (Play, App Store Connect, Firebase)

## Problema / objetivo
El backend tiene lista la imagen Docker, Flyway y la CI, pero **no tiene hosting**. La app necesita un dominio HTTPS estable y las tiendas configuradas. Esta spec es la **lista de lanzamiento** de extremo a extremo.

## Criterios de aceptación
**Backend (se ejecuta en Squadfy_Backend con su propio SDD)**
- **AC-012-01** Hosting elegido (decisión D-4), con HTTPS en un dominio propio (por ejemplo `api.squadfy.app`), `SPRING_PROFILES_ACTIVE=prod`, health checks en `/actuator/health/{liveness,readiness}` y CD desde `master`.
- **AC-012-02** Servicios gestionados configurados por variables de entorno (`BACKEND.md` §4):
  - Supabase Postgres con `sslmode=require` y Storage;
  - CloudAMQP con SSL en el puerto 5671;
  - Redis Cloud;
  - Mailgun;
  - Firebase (`FIREBASE_CREDENTIALS_PATH` como secreto);
  - `JWT_SECRET_BASE64` (≥ 256 bits);
  - `APP_PUBLIC_URL=https://api…`;
  - `RESET_PASSWORD_URL=squadfy://reset-password`;
  - `FIREBASE_ANDROID_PACKAGE` igual al `applicationId` de release.
- **AC-012-03** BE-GAP-2 resuelto: el rate limit de `/auth/refresh` es por usuario o token, o tiene un umbral compatible con access tokens de 15 min.
- **AC-012-04** BE-GAP-1 resuelto: endpoint de borrado de cuenta + página web de borrado.
- **AC-012-05** La rama `backend-documentation` está mergeada a `master` (BE-GAP-7).
- **AC-012-06** Backups automáticos de Postgres y un runbook mínimo: restaurar, rotar secretos y consultar logs.

**App**
- **AC-012-07** El CI de release inyecta `BASE_URL_HTTP=https://api…/api/v1`, `BASE_URL_WS=wss://api…/ws`, `GOOGLE_SERVICES_JSON` (proyecto Firebase de producción, el mismo que el backend) y la firma.
- **AC-012-08** Play Console:
  - ficha con capturas, icono e imagen destacada;
  - categoría Deportes;
  - clasificación de contenido;
  - Data Safety y política de privacidad;
  - URL de borrado de cuenta;
  - pista **Internal testing**, después **Closed testing**. En las cuentas personales nuevas, Google exige 12 testers durante 14 días antes de producción.
- **AC-012-09** App Store Connect: ficha, etiquetas de privacidad, cuenta de demo para la revisión (con un club de ejemplo y una convocatoria abierta), TestFlight interno y después externo.
- **AC-012-10** Firebase: la clave APNs subida, el SHA-1 y SHA-256 de la firma de release (y de Play App Signing) registrados.
- **AC-012-11** **Smoke test en producción** con 2 dispositivos (Android e iOS):
  - registro, verificación por email y login;
  - crear club, unirse con el código;
  - crear horario → recibir la push de apertura → apuntarse;
  - partido extra con cierre próximo → equipos publicados por push;
  - acta, cerrar el partido, comprobar las clasificaciones;
  - chat;
  - logout.
- **AC-012-12** Beta cerrada con **un club real durante 4 semanas** (métrica de `vision.md`), antes de abrir la producción.

## Documentos del lado app
- `plan.md`: plan y riesgos (incluida la decisión D-13).
- `privacy-data.md`: inventario de datos, Data Safety, etiquetas de privacidad y contenido de la política.
- `store-listing.md`: fichas ES/EN, categoría y clasificación.
- `release-checklist.md`: secretos, Firebase, cuenta de demo y smoke test.
