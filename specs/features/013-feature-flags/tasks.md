# 013 · Tareas

- [x] **T-001** `AppEnvironment`, catálogo `FeatureFlag` y `FeatureFlagResolver` + tests (AC-013-02/03).
- [x] **T-002** `SQUADFY_ENV` → `BuildKonfig.ENVIRONMENT`, validado en la convention (AC-013-01).
- [x] **T-003** `DefaultFeatureFlags`, el store de DataStore, el remoto NoOp y Koin + `DefaultFeatureFlagsTest` (AC-013-04/05/09).
- [x] **T-004** Pantalla de depuración `FeatureFlagsScreen` + ViewModel + test, con una ruta solo en PRE (AC-013-06).
- [x] **T-005** Condicionar la UI existente: Partido (invitados, partido de prueba, resultado), Horario (excepciones, `drawTime`) e Inicio (partidos y noticias), sin llamadas de red con el flag apagado (AC-013-07/08). *Nota:* el test de «sin llamada de red» de `ScheduleSettingsViewModel` se escribe en la spec 004, que reescribe ese ViewModel y su repositorio; hoy queda cubierto por el código (`if (exceptionsEnabled) loadExceptions()`).
- [x] **T-006** Documentación: `CLAUDE.md`, la skill `sdd-workflow` y la sección del CI (AC-013-10). El CI fija `SQUADFY_ENV=pre`.
- [x] **T-007** Verificación: `./gradlew testDebugUnitTest` y `:composeApp:assembleDebug` en PRE y en PRO. ✅ 164 tests en verde; APK en PRE y en PRO; `-PSQUADFY_ENV=staging` hace fallar el build.
