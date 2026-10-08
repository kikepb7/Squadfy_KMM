# 012 · Plan técnico

- **Spec:** ./spec.md (en `Draft`: este plan cubre solo el lado app, que es documentación y configuración, sin código funcional nuevo)
- **Rama:** `feature/011-release-readiness` (la parte app sale junto con la 011) · backend: su propio SDD

## Cumplimiento de la constitución
| Principio | Cumple | Nota |
|---|---|---|
| I. Servidor autoritativo | ✅ | No hay reglas nuevas. El borrado de cuenta y los límites los aplica el backend (spec 010 del backend). |
| II. Capas y MVI | ✅ | Sin cambios de arquitectura. |
| III. Calidad | ✅ | El CI de release (011 T-011) construye la AAB minificada y firmada; la verificación es el smoke test de AC-012-11. |
| IV. Seguridad | ✅ | Los secretos solo viven en GitHub, Play Console y App Store Connect; la cuenta de demo no se escribe en el repo. |
| V. Contrato | ✅ | Sin endpoints nuevos. |

## Lado app (lo que se puede preparar desde este repo)
1. **Configuración de producción (AC-012-07).** Ya existe el workflow `squadfy-release.yml` (011 T-011). Se documentan los valores exactos de los secretos y variables que hay que dar de alta (`release-checklist.md` §1).
2. **Fichas de tienda (AC-012-08/09).** Los textos ES/EN, la categoría y el cuestionario de contenido van en `store-listing.md`. El inventario de datos para Data Safety (Play) y las etiquetas de privacidad (App Store) va en `privacy-data.md`, sacado de lo que la app y el backend tratan de verdad (SDKs incluidos en la release: FCM y Crashlytics; sin analítica ni publicidad).
3. **Cuenta de demo para la revisión (AC-012-09).** Pasos para prepararla en PRO, con un club, un horario y una convocatoria abierta (`release-checklist.md` §3). Las credenciales van en las notas de revisión de App Store Connect y en «Acceso a la app» de Play Console, nunca en el repo.
4. **Firebase (AC-012-10).** Checklist: huellas SHA de la clave de subida y de Play App Signing, clave APNs, app iOS con el bundle `com.kikepb.squadfy`, restricción de las API keys (`release-checklist.md` §2).
5. **Smoke test en producción (AC-012-11).** Checklist reproducible con dos dispositivos (`release-checklist.md` §4).

## Backend (Squadfy_Backend, fuera del alcance de este repo)
AC-012-01…06: hosting, servicios gestionados, fusionar la spec 010 (borrado de cuenta y rate limit por cuenta) y backups. Se siguen en el SDD del backend.

## Riesgos detectados al preparar las fichas
- **Contenido generado por usuarios (chat).** Apple (guideline 1.2) pide, en apps con UGC, poder denunciar contenido y bloquear usuarios, además de un contacto publicado. La app no tiene ni denuncia ni bloqueo en el chat. Queda como **D-13** en el roadmap, porque necesita una decisión de producto y probablemente una spec del backend.
- **Prueba cerrada de Play.** Las cuentas personales nuevas necesitan 12 testers durante 14 días antes de pedir producción. Conviene empezarla en cuanto haya backend de PRO.

## Estrategia de verificación
| AC | Verificación |
|---|---|
| 012-07 | Ejecutar `Release` en GitHub Actions con los secretos de PRO: la AAB llega a Internal testing |
| 012-08 | Play Console sin avisos en «Contenido de la app» (Data Safety, borrado, privacidad, clasificación) |
| 012-09 | Build en TestFlight con la cuenta de demo funcionando |
| 012-10 | Push recibida en Android (release de Play) e iOS (TestFlight) |
| 012-11 | Checklist de `release-checklist.md` §4 completo en los dos dispositivos |
| 012-12 | 4 semanas de beta cerrada con un club real |
