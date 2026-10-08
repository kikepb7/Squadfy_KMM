# 001 · Tareas

- [x] **T-001** Fijar el JDK 17 del daemon (`gradle/gradle-daemon-jvm.properties`). *Verificación:* `./gradlew` funciona sin `JAVA_HOME`.
- [x] **T-002** Subir KSP a `2.2.20-2.0.4`. *Verificación:* desaparece el aviso «ksp is too old».
- [x] **T-003** Añadir `kotlinx-coroutines-test`, `turbine`, `ktor-client-mock` y `junit4` al catálogo, junto con `configureKmpTestDependencies()` en `KmpLibraryConventionPlugin` y `CmpApplicationConventionPlugin`.
- [x] **T-004** Arreglar los tests de chat que no compilaban o fallaban: tipo del handler del `MockEngine` y `defaultRequest { contentType(Json) }` en `MockHttpClientFactory`; nombre ilegal `<=`; sesión iniciada en `ChatListViewModelTest`; conflación de StateFlow en `CreateChatViewModelTest`. *Verificación:* 151/151 en verde.
- [x] **T-005** `BuildKonfigConventionPlugin` lee `API_KEY`, `BASE_URL_HTTP` y `BASE_URL_WS` de `-P`, del entorno y de `local.properties`.
- [x] **T-006** Reescribir el CI: rama `main`, secretos, `testDebugUnitTest` en todos los módulos, quitar el job de emuladores y el texto de detekt.
- [x] **T-007** Dar valores por defecto a los campos opcionales del `ClubDto` de globalPosition.
- [x] **T-008** Deep links: `chat_details` (Android e iOS) y `reset-password` (manifest).
- [x] **T-009** `.gitignore`: versionar `.claude/skills` e ignorar `settings.local.json`, keystores, `google-services.json` y `.env`.
- [ ] **T-010** Dar de alta los secretos `SQUADFY_API_KEY` y `GOOGLE_SERVICES_JSON` en GitHub y verificar el primer run verde. *(manual, owner)* Mientras no existan, el CI ya no falla: usa una API key y un `google-services.json` de relleno (`.github/ci/google-services.placeholder.json`) y avisa con un warning. Antes, el CI de `main` fallaba en `processDebugGoogleServices` («Malformed root json») porque el secreto estaba vacío.
- [ ] **T-011** Commitear los esquemas de Room (`feature/club/database/schemas`, `feature/chat/database/schemas`).
- [ ] **T-012 (P)** Aplicar ktlint a todos los módulos desde la convention, ejecutar `ktlintFormat` en un commit aislado y poner `ignoreFailures = false`.
- [ ] **T-013 (P)** Kover agregado en la raíz con `kover(project(...))` para cada módulo, y un gate en el CI.
- [ ] ~~**T-014**~~ Se ha movido a la spec 003 (AC-003-16): el código muerto de club y globalPosition se elimina durante la migración a v1.
- [ ] ~~**T-015**~~ Se ha movido a la spec 011 (AC-011-04).
- [~] **T-016** Quitar `iosApp/iosApp/GoogleService-Info.plist` del tracking (`git rm --cached`) y restringir la API key de Firebase iOS por bundle ID en la consola de Google Cloud (se relaciona con AC-011-12). El plist ya no se versiona (spec 011 T-009). Falta la parte manual: restringir la clave, que sigue en el historial de git (owner, spec 011 T-013).
