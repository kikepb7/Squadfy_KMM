<!--
Título del PR: `ÁREA | NNN · T-xxx descripción` (p. ej. `CLUB | 003 · T-002 SignupWindowPolicy`).
Rellena cada sección; si una no aplica, escribe «No aplica» en vez de borrarla.
-->

## Resumen
<!-- Qué cambia y por qué, en 2-4 frases. -->


## Spec y tareas
- **Spec:** `specs/features/NNN-slug/`
- **Tareas:** T-xxx, T-xxx
- **Criterios de aceptación:** AC-NNN-xx, AC-NNN-xx
- **Reglas de negocio:** BE-NNN RN-x / APP-RN-xx
- **Issue o decisión relacionada:** <!-- D-x del roadmap, issue, ADR… -->

## Tipo de cambio
- [ ] Funcionalidad nueva (`feature/NNN-slug`)
- [ ] Corrección (`fix/slug`)
- [ ] Mantenimiento, build o CI (`chore/slug`)
- [ ] Release (`release/X.Y.Z`)
- [ ] Hotfix (`hotfix/X.Y.Z`)
- [ ] Solo documentación o SDD

## Rama (ADR-0009)
<!-- main ← release/X.Y.Z | hotfix/X.Y.Z · develop ← feature/* | fix/* | chore/* | release/* | hotfix/* · release/* ← fix/* -->
`origen` → `destino`

## Cambios principales
<!-- Por capa o por módulo: domain, data, presentation, navegación, build… -->
-

## Cómo probarlo
<!-- Pasos para reproducirlo. Indica el entorno (PRE/PRO), las feature flags necesarias y los datos (p. ej. backend local + `scripts/dev/seed_demo.py`). -->
1.
2.

**Resultado esperado:**

## Capturas
<!-- Obligatorias si cambia la UI: tema Tiza (claro) y Noche (oscuro). Borra las filas que no apliquen. -->
| | Tiza | Noche |
|---|---|---|
| Android | | |
| iOS | | |

## Backend
- [ ] No depende de cambios en el backend
- [ ] Depende de BE-NNN (ya desplegado en PRE / pendiente)
- [ ] Cambia el contrato consumido: `specs/contracts/api-v1.md` actualizado (y `gap-analysis.md` si falta algo)

## Checklist
**Calidad**
- [ ] `./gradlew testDebugUnitTest` en verde, y los tests nuevos citan su `AC-NNN-xx`
- [ ] `./gradlew checkHardcodedStrings` en verde: los textos están en `strings.xml` (ES y EN)
- [ ] `./gradlew :composeApp:assembleDebug` compila
- [ ] iOS compila (`./gradlew :composeApp:compileKotlinIosSimulatorArm64`) si se toca código común o UI
- [ ] Probado en el emulador o en un dispositivo (Android y, si cambia la UI, iOS)

**Convenciones**
- [ ] Colores y tipografía desde `Triangulacion` (sin hex sueltos); previews Tiza y Noche en cada pantalla nueva
- [ ] Sin mocks ni datos de prueba en la app
- [ ] DTOs nuevos con valores por defecto en los campos opcionales
- [ ] Cambio de esquema de Room con su `Migration` explícita y el esquema exportado
- [ ] Trabajo sin terminar o pendiente del backend tras un `FeatureFlag` (on en PRE, off en PRO)

**Seguridad**
- [ ] Sin secretos, claves ni `google-services.json` / `GoogleService-Info.plist` en el diff
- [ ] Sin logs de tokens ni datos personales

**SDD**
- [ ] `tasks.md` de la spec y `specs/roadmap.md` actualizados
- [ ] `CLAUDE.md` o las skills actualizados si cambia una convención

## Riesgos y despliegue
<!-- Qué puede romperse, migraciones, flags que activar en PRO, pasos posteriores al merge… -->


## Notas para el revisor
<!-- Dónde mirar primero, decisiones discutibles o preguntas abiertas. -->
