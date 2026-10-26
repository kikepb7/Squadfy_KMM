---
name: squadfy-patterns
description: >
  Implementation patterns for the Squadfy KMP app, adapted from the owner's android-patterns skill: unidirectional data
  flow, ViewModel shape, sealed screen state, Flow composition, typed errors, repositories, use cases and Compose
  Screen/Content split. Use it whenever writing or reviewing a ViewModel, UiState/UiAction/UiEvent, a repository or use
  case, flow composition, error handling or a Compose screen. Where it conflicts with android-presentation-mvi,
  android-error-handling or android-compose-ui, this one wins.
---

# Squadfy patterns (KMP adaptation of android-patterns)

## 1. Unidirectional data flow
`UiAction -> onAction() -> internal state -> derived state -> UiState -> Content` and one-shot `UiEvent`s out.
Composables never own business state; they call `onAction`.

## 2. State model
```kotlin
@Immutable
internal sealed interface ClubsScreenUiState {
    data object Loading : ClubsScreenUiState
    data class Loaded(val content: ClubsUiState) : ClubsScreenUiState
    data class Error(val message: UiText) : ClubsScreenUiState
}

@Immutable
internal data class ClubsUiState(val clubs: List<ClubItemUiState>, val isRefreshing: Boolean = false)

@Immutable
internal sealed interface ClubsUiAction { data object Retry : ClubsUiAction; data class OpenClub(val id: String) : ClubsUiAction }

internal sealed interface ClubsUiEvent { data class NavigateToClub(val id: String) : ClubsUiEvent; data class ShowError(val message: UiText) : ClubsUiEvent }
```
- Prefer sealed states over boolean flags (`Loading`/`Loaded`/`Error` instead of `isLoading` + nullable data).
- Keep internal typed state (ids, raw input) separate from the UiState the screen renders.
- Forms keep their editable fields in the UiState (TextFieldState is fine, it survives recomposition) and in-flight
  flags (`isSaving`) that disable the submit button.
- Strings for the UI are `UiText` (resources), never hardcoded.

## 3. ViewModel
```kotlin
internal class ClubsViewModel(
    private val observeClubsUseCase: ObserveClubsUseCase,
    private val mapper: ClubsUiStateMapper,
) : ViewModel() {
    private val eventChannel = Channel<ClubsUiEvent>()
    val uiEvents: Flow<ClubsUiEvent> = eventChannel.receiveAsFlow()

    val uiState: StateFlow<ClubsScreenUiState> = observeClubsUseCase.observe()
        .map { result ->
            when (result) {
                is Result.Success -> ClubsScreenUiState.Loaded(mapper.map(result.data))
                is Result.Failure -> ClubsScreenUiState.Error(result.error.toUiText())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClubsScreenUiState.Loading)

    fun onAction(action: ClubsUiAction) { when (action) { … } }
}
```
- `onAction` is the single entry point. Expose `StateFlow` via `stateIn(WhileSubscribed(5_000))`.
- **Events go through a `Channel` + `receiveAsFlow()`** (not `SharedFlow`): a `SharedFlow` drops an event emitted while
  nobody collects (rotation, background), a `Channel` buffers it until the screen collects it once.
- The ViewModel coordinates; parsing, formatting and mapping live in named classes (`…Mapper`, `…Formatter`).
- Ids that identify the screen come from the route via `SavedStateHandle`.

## 4. Flows
- `combine` for several latest values, `flatMapLatest`/`mapLatest` when a new input cancels old work, `scan` for
  accumulation, `distinctUntilChanged` before expensive work, `stateIn`/`shareIn` to share upstream work.
- Never subscribe twice to the same cold source with side effects (e.g. a flow with `onEach { load() }` collected by two
  `combine`s): derive from one shared flow.

## 5. Errors
- Operations return the project's typed `Result<T, DataError>` (`core/domain/util`), the KMP equivalent of explicit
  domain errors; never throw through flows.
- Initial load failure -> `Error` screen state with retry; refresh failure -> keep last data and send a `ShowError`
  event; one-time errors are events, not durable state.

## 6. Repositories and use cases
- Repositories expose domain models: `Flow<…>` for live data (Firestore listeners), `suspend fun …: Result<…, DataError>`
  for commands. No DTOs or Firebase types in interfaces.
- Use cases: `[Verb][Subject]UseCase` with a verb method (`fetch`, `observe`, `submit`…), small and focused.

## 7. Compose
- `[Feature]Screen(viewModel = koinViewModel(), …navigation callbacks)` collects `uiState` with
  `collectAsStateWithLifecycle()` and events with `ObserveAsEvents`; `[Feature]Content(uiState, onAction)` is stateless
  and has Tiza and Noche previews (no sample mocks, spec 018).
- Never pass the ViewModel down. Local visual state (sheet visibility, focus, expansion) stays in Compose.
- Lazy lists always use stable `key`s. Remember derived values; no formatting or sorting in composition.

## 8. Review checklist
ViewModel coordinates instead of computing; actions/state/events are distinct; flows are shared and lifecycle-aware;
UI is stateless; errors are explicit; mapping/formatting are in named classes; sealed state instead of flags.
