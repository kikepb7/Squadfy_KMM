# 013 · Plan técnico

## Cumplimiento de la constitución
| Principio | Cumple | Nota |
|---|---|---|
| I | ✅ | Los flags solo deciden qué se muestra o se llama; no sustituyen las reglas del servidor |
| II | ✅ | Catálogo y resolver puros en `core/domain`; implementación en `core/data`; consumo desde los ViewModels |
| III | ✅ | Tests del resolver (tabla), del catálogo (claves únicas) y de la implementación (fakes) |
| IV | ✅ | En PRO no hay overrides ni pantalla de depuración |

## core/domain · `com.kikepb.core.domain.featureflag`
- `AppEnvironment { PRE, PRO }`, con `fromKey("pre" | "pro")`.
- `enum class FeatureFlag(val key, val description, val defaultInPre, val defaultInPro)`, más `defaultFor(env)`.
- `enum class FlagValueSource { DEFAULT, REMOTE, OVERRIDE }` y `data class ResolvedFeatureFlag(flag, enabled, source)`.
- `object FeatureFlagResolver.resolve(flag, environment, remote: Map<String, Boolean>, overrides: Map<String, Boolean>)`, puro (AC-013-03).
- `interface FeatureFlags`: `environment`, `isEnabled(flag)`, `observe(flag): Flow<Boolean>`, `observeAll(): Flow<List<ResolvedFeatureFlag>>`.
- `interface FeatureFlagOverrides`: `setOverride(flag, enabled: Boolean?)` y `clearOverrides()`. En PRO no hace nada.
- `interface RemoteFeatureFlagSource`: `observe(): Flow<Map<String, Boolean>>`.

## core/data · `com.kikepb.core.data.featureflag`
- `BuildKonfig.ENVIRONMENT`: la `BuildKonfigConventionPlugin` resuelve `SQUADFY_ENV` (`-P`, entorno o `local.properties`; por defecto `pre`) y falla con cualquier otro valor.
- `FeatureFlagOverrideStore` (interfaz interna) + `DataStoreFeatureFlagOverrideStore`, con claves `ff_<key>` en el DataStore de preferencias que ya existe.
- `NoOpRemoteFeatureFlagSource` → `flowOf(emptyMap())`.
- `DefaultFeatureFlags(environment, store, remote, scope)`:
  - `combine(store.observe(), remote.observe())`;
  - mapea con el resolver;
  - `stateIn(scope, Eagerly, defaults)`.
  
  `isEnabled` lee el `value`. En PRO, `setOverride` y `clearOverrides` no hacen nada.
- Koin (`coreDataModule`): `single { DefaultFeatureFlags(...) }` enlazado a `FeatureFlags` y a `FeatureFlagOverrides`.

## core/presentation · `com.kikepb.core.presentation.featureflag`
- `FeatureFlagsViewModel` (state: `environment`, `items` con `ResolvedFeatureFlag`; actions: `OnToggle(flag, enabled)` y `OnReset`) y `FeatureFlagsScreen` (Root/Screen).
- Strings en `core/presentation/composeResources/values` (ES) y `values-en`.

## Consumidores
- `MatchViewModel` combina `flags.observe(MATCH_GUESTS | DEV_TEST_MATCH | MANUAL_SCORE)` y lo expone en `MatchState` (`isGuestsEnabled`, `isTestMatchEnabled`, `isManualScoreEnabled`). `MatchScreen` condiciona los botones y los diálogos.
- `ScheduleSettingsViewModel`: `loadExceptions()` solo se ejecuta si `SCHEDULE_EXCEPTIONS` está activo (APP-RN-17). El estado incluye `isExceptionsEnabled` y `isDrawTimeEnabled`, y la sección o el campo se ocultan si están apagados. Al guardar, `drawTime = null` si su flag está apagado.
- `GlobalPositionViewModel`: los partidos recientes y las noticias solo se cargan y se muestran con `HOME_RECENT_MATCHES` y `HOME_NEWS`.
- `NavigationRoot`: en PRE, el botón de ajustes de Inicio navega a `FeatureFlagsRoute`; en PRO la ruta no se registra.

## Tests
| AC | Test |
|---|---|
| 01 | Compilar con `-PSQUADFY_ENV=foo` → falla (verificación manual o en el CI) |
| 02 | `FeatureFlagCatalogTest` |
| 03 | `FeatureFlagResolverTest` (tabla PRE y PRO × override × remoto) |
| 04/05 | `DefaultFeatureFlagsTest` (fake store y fake remote; emite al cambiar; ignora los overrides en PRO) |
| 06 | `FeatureFlagsViewModelTest` |
| 08 | `ScheduleSettingsViewModelTest` (no pide excepciones con el flag apagado) y `MatchViewModel`, cubierto por el estado |
