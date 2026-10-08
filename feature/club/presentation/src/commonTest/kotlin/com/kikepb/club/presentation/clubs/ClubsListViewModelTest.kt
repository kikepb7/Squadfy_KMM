package com.kikepb.club.presentation.clubs

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.usecase.FetchMyClubsUseCase
import com.kikepb.club.domain.usecase.ObserveMyClubsUseCase
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.club
import com.kikepb.club.presentation.fake.member
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ClubsListViewModelTest {

    private val repository = FakeClubRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ClubsListViewModel(
        observeMyClubsUseCase = ObserveMyClubsUseCase(repository, FakeSessionStorage("me")),
        fetchMyClubsUseCase = FetchMyClubsUseCase(repository)
    )

    @Test
    fun `AC-017-02 an empty cache shows the loader until the first fetch answers, not the empty state`() = runTest(UnconfinedTestDispatcher()) {
        repository.fetchMyClubsGate = CompletableDeferred()
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isRefreshing)

        repository.fetchMyClubsGate?.complete(Unit)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.clubs.isEmpty())
    }

    @Test
    fun `AC-017-02 a failed first fetch still ends the loading state`() = runTest(UnconfinedTestDispatcher()) {
        repository.fetchMyClubsResult = Result.Failure(ClubError.NotFound)
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `AC-017-01 cached clubs show at once and only a pull shows the indicator`() = runTest(UnconfinedTestDispatcher()) {
        repository.clubs.value = listOf(club())
        repository.members.value = listOf(member("m1", "me"))
        repository.fetchMyClubsGate = CompletableDeferred()
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(1, viewModel.state.value.clubs.size)
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isRefreshing)

        viewModel.onAction(ClubsListAction.OnRefresh)
        assertTrue(viewModel.state.value.isRefreshing)
        repository.fetchMyClubsGate?.complete(Unit)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `AC-017-01 the screen fetches once per instance, not on every re-subscription`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope).cancel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(1, repository.myClubsFetches)
    }
}
