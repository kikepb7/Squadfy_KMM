# 001 · Base de calidad (build, tests, CI)

- **Estado:** In progress
- **Reglas:** constitución III y IV
- **Repos afectados:** Squadfy_App

## Problema / objetivo
No se puede trabajar en modo SDD si el build no es reproducible, los tests no compilan y el CI no se ejecuta. En la auditoría encontré lo siguiente:
- El build dependía del JDK por defecto de la máquina. Con un JDK EA (`23-valhalla`), KSP fallaba.
- KSP `2.2.0-2.0.2` no estaba alineado con Kotlin `2.2.20`.
- Ningún módulo KMP tenía `kotlinx-coroutines-test`, `turbine`, `ktor-client-mock` ni `junit`, así que **ningún test compilaba**, incluido el placeholder de `composeApp`.
- El CI se disparaba en `master` (la rama es `main`), sin `API_KEY` ni `google-services.json`. Solo testeaba `composeApp`, anunciaba un detekt que no existe y lanzaba 3 emuladores sin tests instrumentados.
- El HEAD del branch no compilaba (imports y DTO). El working tree lo arreglaba, pero el `ClubDto` de globalPosition exigía campos que el backend no envía, y la lista de clubes de Inicio fallaba en silencio con `SERIALIZATION`.
- Había deep links rotos: push → `chat_detail` frente a la ruta `chat_details`, y reset de contraseña `reset_password` frente a `reset-password`.
- `.gitignore` excluía `.claude/skills`, así que las skills del proyecto no se versionaban.

## Criterios de aceptación
- **AC-001-01** *Given* cualquier JDK en la máquina *When* se ejecuta `./gradlew` *Then* el daemon usa el JDK 17 (`gradle/gradle-daemon-jvm.properties`). ✅
- **AC-001-02** *Given* un test nuevo en `commonTest` o `androidUnitTest` de cualquier módulo KMP *Then* compila sin declarar dependencias de test por módulo (`configureKmpTestDependencies`). ✅
- **AC-001-03** `./gradlew testDebugUnitTest` termina en verde (151 tests a 2026-10-05). ✅
- **AC-001-04** El CI se ejecuta en push y PR a `main` con los secretos `SQUADFY_API_KEY` y `GOOGLE_SERVICES_JSON`, y ejecuta los tests de **todos** los módulos. ✅ (falta dar de alta los secretos en GitHub)
- **AC-001-05** Un `GET /club` sin campos de calendario se deserializa correctamente. ✅
- **AC-001-06** Los deep links de push de chat y de reset de contraseña navegan a su pantalla. ✅ (falta la verificación manual)
- **AC-001-07** ktlint pasa con `ignoreFailures = false` en todos los módulos.
- **AC-001-08** Cobertura agregada con Kover en todos los módulos, con un gate mínimo del 40 % en `domain`.
- **AC-001-09** Los esquemas de Room (`*/schemas/*.json`) están versionados.
- **AC-001-10** *(se cumple con AC-003-16)* Se elimina el código muerto: `ClubService`, `KtorClubRepositoryImpl`, `GlobalPositionService`, `KtorGlobalPositionRepositoryImpl`, `ClubLogoUploadUrlsResponseDTO`, el iOS `ChatDataModule.ios.kt` mal nombrado de globalPosition, y los `Platform.kt` sin uso.
- **AC-001-11** *(se cumple con AC-011-04)* Logging de red: en release, `LogLevel.NONE`; en debug, `HEADERS` con `sanitizeHeader { it == Authorization }`. El `androidLogger()` de Koin solo se activa en debug.
