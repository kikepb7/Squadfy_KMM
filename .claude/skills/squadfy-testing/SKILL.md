---
name: squadfy-testing
description: >
  Testing standards for the Squadfy KMP app, adapted from the owner's android-testing skill: test naming with the
  acceptance criterion, `sut`, arrange/stub/act/assert blocks, expected/actual, Turbine for flows, fakes instead of mocks,
  data-driven cases. Use it when creating, reviewing or refactoring unit tests for ViewModels, use cases, mappers,
  calculators or repositories. Applies to new and modified tests; where it conflicts with android-testing, this one wins.
---

# Squadfy testing (KMP adaptation of android-testing)

## Tools (KMP)
- `kotlin.test` (`@Test`, `assertEquals`) + `kotlinx-coroutines-test` (`runTest`) + Turbine, in `commonTest`;
  `androidUnitTest` only for JVM-specific code (JUnit4 runner there).
- JUnit 5, MockK and Mockito do not run in KMP common tests: use **fakes** (`FakeClubRepository`) with configurable
  results and recorded calls, as the project already does.
- Data-driven cases: a list of cases iterated inside the test (KMP has no `@ParameterizedTest`).

## Naming
`` `AC-NNN-xx When <condition> then <expected result>` `` — the acceptance criterion id (constitution) followed by a
sentence. Without an AC (pure helpers): `` `When … then …` ``. One scenario per test.

## Structure
```kotlin
class ClubsViewModelTest {
    private val clubRepository = FakeClubRepository()
    private lateinit var sut: ClubsViewModel

    @Test
    fun `AC-002-01 When the clubs load then the screen is Loaded with them`() = runTest {
        // create objects and fakes
        val club = aClub(id = "c1")
        val expected = ClubsScreenUiState.Loaded(ClubsUiState(clubs = listOf(club.toItemUiState())))

        // stub functions
        clubRepository.clubs.value = listOf(club)

        // execute tested function
        sut = ClubsViewModel(ObserveClubsUseCase(clubRepository), ClubsUiStateMapper())

        // assert result
        sut.uiState.test {
            assertEquals(expected = ClubsScreenUiState.Loading, actual = awaitItem())
            assertEquals(expected = expected, actual = awaitItem())
        }
    }
}
```
- The subject is always `sut`; other objects are named by role.
- Four commented blocks: create, stub, execute, assert. `expected` vs `actual`, full-object assertions when the
  structure matters (whole UiState, whole event list).

## Flows and ViewModels
- Test through the public API: `onAction`, `uiState`, `uiEvents`. Assert the emitted sequence with Turbine
  (`awaitItem`, `expectNoEvents`, `awaitComplete`, `awaitError`), including intermediate states that are part of the
  contract (Loading → Loaded).
- Deterministic: test dispatchers (`MainDispatcherRule`/`Dispatchers.setMain`), virtual time instead of real delays.

## Checklist
Name explains the behaviour; `sut` used; blocks separated; expected vs actual explicit; data-driven cases as a list;
Turbine for flows; `runTest`; behaviour, not implementation.
