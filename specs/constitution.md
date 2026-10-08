# Constitución técnica de Squadfy

Principios no negociables. Todo `plan.md` debe declarar que los cumple, o justificar la excepción con un ADR.

## I. El servidor es la autoridad del dominio
1. Las reglas que afectan a varios usuarios las calcula y valida **el backend**: ventana de convocatoria, aforo, sorteo de equipos, resultados, estadísticas y clasificación (ADR-0005). El cliente puede **anticipar** el resultado para la UX (por ejemplo, deshabilitar «Apuntarme» fuera de ventana), pero nunca es la fuente de verdad. **La app no implementa lógica de dominio para suplir un endpoint que falta:** si hace falta, se abre una spec en `Squadfy_Backend/specs/`.
2. Toda operación con permisos se valida en el servidor con el rol del miembro en **ese** club. Ocultar un botón en la UI no sustituye a la autorización.
3. Las operaciones concurrentes sobre un recurso compartido (apuntarse con aforo limitado, sortear, cerrar una convocatoria) son **atómicas e idempotentes**.

## II. Arquitectura del cliente (KMP + Compose Multiplatform)
1. Hay módulos por feature (`feature/<f>/{domain,data,database,presentation}`) y módulos `core/*`. Las dependencias entre capas siguen las skills `android-module-structure` y `android-di-koin`.
2. **domain** es Kotlin puro: sin Ktor, Room ni Compose. Contiene modelos, interfaces de repositorio y casos de uso. La lógica de negocio del cliente vive aquí y se testea en `commonTest`.
3. **data** sigue el patrón offline-first (Room es la fuente de lectura de la UI y la red sincroniza) **salvo en el ciclo de partido**, que es network-first con caché en memoria (ADR-0006). Los DTOs solo existen en data, y los mappers son explícitos.
4. **presentation** sigue MVI (`State`/`Action`/`Event`, Root/Screen), según `android-presentation-mvi`. Los ViewModels no conocen DTOs.
5. Los estados, roles y posiciones se modelan con **tipos** (`enum`/`sealed`), nunca con `String` mágicos fuera de la capa data.
6. Los errores se representan con `Result<T, DataError>`. Los errores de negocio del servidor (`error.code`) se mapean a errores tipados y después a `UiText`.

## III. Calidad
1. Toda regla de negocio del cliente tiene un test unitario. Todo ViewModel nuevo tiene tests de estado y eventos (Turbine).
2. `./gradlew testDebugUnitTest` en verde es condición para mergear. El CI lo ejecuta en cada PR contra `main`.
3. No se mergea código muerto ni mocks visibles al usuario. Las pantallas «en construcción» se ocultan con un flag.
4. El esquema de Room se exporta y se versiona en `*/schemas/`. A partir de la primera release **está prohibido** `fallbackToDestructiveMigration`, y cada cambio de esquema lleva su `Migration` con su test.
5. Todo texto visible sale de `composeResources/values*/strings.xml`. Los idiomas son ES (por defecto) y EN.

## IV. Seguridad y privacidad
1. No hay secretos en el repositorio. `google-services.json`, los keystores y las URLs de producción se inyectan desde `local.properties`, desde variables de entorno o desde los secrets del CI (los de release, en el Environment protegido `production`). No se compilan claves «secretas» en la app, porque se pueden extraer del binario (ADR-0008).
2. Las builds release usan HTTPS/WSS, minify con R8 y logging de red desactivado. Los tokens se guardan cifrados.
3. Se aplica la minimización de datos: el backend no expone los emails de otros usuarios y la app no los pide ni los guarda.
4. Requisitos de tienda: borrado de cuenta dentro de la app, política de privacidad y declaración de datos (Data Safety y Privacy Nutrition Labels).

## V. Contrato de API
1. El contrato lo define el backend (`../Squadfy_Backend/docs/BACKEND.md` y su OpenAPI). `specs/contracts/api-v1.md` es el **mapa de consumo** de la app. Los cambios que necesita la app se piden mediante una spec del backend.
2. Los DTOs del cliente toleran campos desconocidos (`ignoreUnknownKeys`) y dan **valores por defecto** a todo campo opcional o nuevo.
3. Los instantes llegan en ISO-8601 UTC y se muestran en la zona horaria del club (`schedule.timeZone`, APP-RN-03).
4. Cada DTO tiene un test de mapper con un **fixture JSON real** (de `BACKEND.md` o del OpenAPI), de modo que un cambio del backend rompe un test y no la app en producción.
