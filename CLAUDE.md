# Squadfy App: guía para Claude Code

Squadfy gestiona clubes de fútbol amateur:
- un owner crea un club e invita a otros usuarios por código;
- el club juega un partido semanal con convocatoria automática (se abre el día siguiente al último partido y se cierra el día antes del siguiente);
- se hace un sorteo equilibrado de dos equipos;
- se registran el resultado, las estadísticas y la clasificación.

Es una app Kotlin Multiplatform (Android + iOS, Compose Multiplatform). Su backend es `../Squadfy_Backend` (Spring Boot, Kotlin, API `/api/v1`, documentado en `../Squadfy_Backend/docs/BACKEND.md`). **El backend es la fuente de verdad del dominio y del contrato** (ADR-0005).

## Spec-Driven Development (obligatorio)
- **La fuente de verdad es `specs/`.** Antes de cualquier cambio funcional, carga la skill `sdd-workflow` y lee `specs/roadmap.md`.
- Las reglas de negocio son las del backend (`BE-NNN RN-x`). Las reglas propias de la app son `APP-RN-xx`. Ambas están en `specs/product/business-rules.md`, y la skill `squadfy-domain` las resume.
- El contrato es el del backend (OpenAPI y `BACKEND.md`). `specs/contracts/api-v1.md` es el mapa de consumo de la app. Antes de tocar DTOs o endpoints, carga `squadfy-api-contract`.
- No te inventes reglas. Si algo no está especificado, añádelo como pregunta abierta en la spec y pregunta al usuario.

## Comandos
```bash
./gradlew testDebugUnitTest                         # todos los tests unitarios (debe estar en verde)
./gradlew :feature:club:domain:testDebugUnitTest    # bucle rápido de un módulo
./gradlew :composeApp:assembleDebug                 # APK debug
./gradlew :composeApp:ktlintCheck                   # ktlint (por ahora solo informa)
./gradlew checkHardcodedStrings                     # falla si hay textos de UI escritos a mano (bloquea el CI)
```
- El JDK 17 está fijado en `gradle/gradle-daemon-jvm.properties`. **No lo quites**: el JDK por defecto de esta máquina es un EA (`23-valhalla`) que rompe KSP.
- `local.properties` admite, de forma opcional, `BASE_URL_HTTP` (tiene que terminar en `/api/v1`) y `BASE_URL_WS` (`…/ws`); por defecto apuntan al emulador, `10.0.2.2:8080`. **No hay API key de cliente** (ADR-0008): no la reintroduzcas, porque cualquier clave compilada en la app se puede extraer.
- Backend local para E2E: `cd ../Squadfy_Backend && docker compose up -d`, y después `JWT_SECRET_BASE64=$(openssl rand -base64 32) FIREBASE_ENABLED=false ./gradlew :app:bootRun --args='--spring.profiles.active=dev'` (Swagger en `localhost:8080/swagger-ui.html`, emails en Mailpit `localhost:8025`).
- `composeApp/google-services.json` está en `.gitignore` y hace falta para compilar Android. Lo mismo pasa con `iosApp/iosApp/GoogleService-Info.plist` en iOS, que corresponde al bundle `com.kikepb.squadfy`.

## Arquitectura
- `core/{domain,data,presentation,designsystem}` y `feature/<f>/{domain,data,database,presentation}`. Las features son auth, chat, club y globalPosition. (`economy` y `onboarding` se retiraron en la spec 011; están en el historial de git).
- Clean Architecture + MVI + Koin + Room + Ktor. Offline-first para clubes, miembros y horario; network-first para convocatoria, partido y rankings (ADR-0006). Sigue las skills `android-*` del proyecto para cada capa.
- Los convention plugins están en `build-logic/convention`:
  - un módulo nuevo usa `convention.kmp.library`, `convention.cmp.library` o `convention.cmp.feature`;
  - las dependencias de test se añaden solas (`configureKmpTestDependencies`).
- `Result<T, DataError>` está en `core/domain/util`. `UiText` y el mapeo de errores están en `core/presentation`.

## Convenciones
- **domain**: Kotlin puro. Los estados, roles y posiciones son `enum`, nunca `String`.
- **data**: los DTOs `@Serializable` llevan **valores por defecto en todo campo opcional**. Las rutas no llevan el prefijo `/api`, porque la base URL ya lo incluye.
- **Room**: el esquema se exporta en `*/schemas` y se versiona. Cada cambio lleva una `Migration` explícita (está prohibido `fallbackToDestructiveMigration` de cara a la release).
- **Marca** (spec 016): el kit está en `docs/brand/` (LEEME, `tokens.json`, SVG). Los colores van por los tokens de `core/designsystem/theme` (nunca hex sueltos), la tipografía es Poppins y el logo es `SquadfyBrandLogo` (isotipo color/reverse). La lima (Peto Lime) nunca se usa como texto sobre fondo claro.
- **UI**: no hay strings escritos a mano; van en `composeResources/values*/strings.xml` (ES por defecto, EN).
- **Tests**:
  - `kotlin.test` + `kotlinx-coroutines-test` + Turbine en `commonTest`, y JUnit4 en `androidUnitTest`;
  - se usan fakes, no mocks;
  - el nombre del test cita el criterio de aceptación (`AC-NNN-xx`).
- **Commits**: `ÁREA | NNN · T-xxx descripción`, por ejemplo `CLUB | 003 · T-002 SignupWindowPolicy`. Solo se commitea cuando el usuario lo pide.
- **Ramas** (ADR-0009):
  - `main` es producción: solo recibe `release/X.Y.Z` y `hotfix/X.Y.Z`, y cada merge se etiqueta `vX.Y.Z`.
  - `develop` es la integración: el trabajo nuevo sale de `develop` en `feature/NNN-slug`, `fix/slug` o `chore/slug` y vuelve por PR.
  - Las releases se cortan con `scripts/start-release.sh release X.Y.Z`, que sube la versión en Android e iOS.
  - El CI rechaza los PRs que no siguen el modelo (job `branch-policy`).

## Trampas conocidas
- Desde la rama `app-parity-feature`, la app consume la **API v1** (BE-001…BE-008): clubes, horario, convocatorias, partidos, clasificaciones, push y ausencias (specs 002–010 y 014; ver `specs/contracts/api-v1.md`). Lo que el backend aún no tiene (borrado de cuenta, despliegue) está en `specs/contracts/gap-analysis.md`.
- Las funciones de BE-008 (invitados, semanas especiales, cierre y sorteo configurables, marcador manual y ausencias) están tras feature flags: activas en PRE y apagadas en PRO.
- `ClubMemberDto` v1 **no** trae `email` ni estadísticas, y el `UserDto` no trae foto: las estadísticas salen de `/stats` y `/ratings` (spec 008). Todo DTO nuevo lleva valores por defecto en los campos opcionales.
- Los equipos, convocatorias, ratings y estadísticas usan `clubMemberId`, no `userId`.
- **Feature flags** (spec 013): el trabajo sin terminar o pendiente del backend se oculta tras un `FeatureFlag` con valores por defecto para PRE y PRO; no se borra. Invitados, excepciones de calendario, `drawTime`, marcador manual y ausencias ya están en el backend (BE-008) y en la app, pero siguen tras sus flags (on en PRE y off en PRO) hasta que se den por buenos (D-1). La valoración es siempre automática (rating).
- Entorno: `-PSQUADFY_ENV=pre|pro` (o la variable de entorno, o `local.properties`); por defecto `pre`.
- Inicio (`globalPosition`) solo tiene la capa `presentation`: usa el dominio de `club` y no tiene mocks. Sus módulos `domain` y `data`, que solo servían datos de ejemplo, se retiraron en la spec 015.
- El backend tiene su propio SDD. No lo modifiques salvo que el usuario lo incluya en el alcance; propón specs del backend en `specs/roadmap.md`.
