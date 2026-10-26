---
name: squadfy-architecture
description: >
  Project structure, layer boundaries, naming, visibility and DI for the Squadfy KMP app (Android + iOS), adapted from
  the owner's android-architecture skill. Use it when adding a feature, module, screen or flow, naming any class or
  file, wiring Koin, or deciding which layer code belongs in, and when reviewing or refactoring code for architecture.
  Where it conflicts with the android-module-structure, android-di-koin or android-data-layer skills, this one wins.
---

# Squadfy architecture (KMP adaptation of android-architecture)

## 1. Layers and dependency direction
`ui -> presentation -> domain <- data`, with `di` wiring them.

| Layer | Where it lives here | Contains |
|---|---|---|
| domain | `core/domain`, `feature/<f>/domain` | Pure Kotlin: models, value objects, repository interfaces, use cases, domain errors, calculators (no Android, Compose, Ktor, Room, Firebase or Koin APIs) |
| data | `core/data`, `core/firebase`, `feature/<f>/data`, `feature/<f>/data-firebase`, `feature/club/data-contract`, `feature/<f>/database` | DTOs, data sources, mappers to domain, repository implementations, caching and sync |
| presentation + ui | `feature/<f>/presentation`, `core/presentation`, `core/designsystem` | ViewModels, UiState/UiAction/UiEvent, UI models, presentation mappers and formatters, Screen/Content composables, components, theme |
| di | `di/` package of each module, assembled in `composeApp` | Koin modules only |

Rules:
- `domain` never depends on other layers. `presentation` never sees DTOs, Ktor, Room or Firebase types.
- Business rules live in the server (Cloud Functions, ADR-0010) and in `domain`; never in composables or ViewModels.
- Packages inside a presentation module stay **per screen** (`clubs/`, `detail/`, `schedule/`…): ViewModel, models,
  mappers, Screen and its `components/` together. Shared UI goes to `core/designsystem`.
- No `utils`/`helpers` dumping grounds; name the responsibility (`MatchDateFormatter`, `RatingDeltaMapper`).

## 2. Naming
| Kind | Pattern |
|---|---|
| ViewModel | `[Feature]ViewModel` |
| Stateful screen (collects the ViewModel) | `[Feature]Screen` |
| Stateless content | `[Feature]Content` |
| Screen lifecycle state | `[Feature]ScreenUiState` (sealed: `Loading`, `Loaded`, `Error`) |
| Content state | `[Feature]UiState` |
| Actions / events | `[Feature]UiAction` / `[Feature]UiEvent` |
| Repository | `[Feature]Repository`; implementations prefixed by backend variant: `Firebase[Feature]Repository`, `Ktor[Feature]Repository` (`…Impl` is ambiguous with two backends) |
| Use case | `[Verb][Subject]UseCase` with a verb method: `observeClubUseCase.observe(id)` |
| Mapper / formatter / parser | `[Source]Mapper`, `[Type]Formatter`, `[Type]Parser` |
| DTO | `[Entity]Dto` |
Never `Manager`, `Helper`, `Processor`, `Utils`. File name = class name.

Existing code still uses `State`/`Action`/`Event` and `Root`/`Screen`: rename to this scheme whenever a screen is touched.

## 3. Visibility
- `internal` by default: ViewModels, use case implementations, repository implementations, mappers, UiState and
  composables used only inside the module.
- `public` only for what other modules use: navigation graph builders and routes, Koin modules, domain contracts,
  design-system components.
- `private` for local helpers.

## 4. Dependency injection (Koin, not Hilt)
Hilt is Android-only and cannot run on iOS, so the project uses Koin with the same principles:
- constructor injection everywhere (`viewModelOf(::ClubsViewModel)`, `singleOf(::FirebaseClubRepository) bind ClubRepository::class`);
- one module per concern in a `di/` package; no service locator calls (`KoinComponent.get()`) outside `di/` and the
  composition root (`composeApp`, `koinViewModel()` in the screen);
- no global mutable singletons.

## 5. Entry points
Single activity (`MainActivity`), `SquadfyApplication`, `iOSApp` + `MainViewController`; Compose Navigation with
type-safe routes; no fragments.

## 6. Review checklist
- Dependencies point inward; the layer only holds its own concerns; the name says the role; it is testable in isolation.
- Refactor when a ViewModel parses or formats, a repository returns DTOs, UI computes business values, or a module mixes
  responsibilities.
