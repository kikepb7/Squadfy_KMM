package com.kikepb.club.presentation.standings

import androidx.lifecycle.SavedStateHandle
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetMyRatingUseCase
import com.kikepb.club.domain.usecase.GetMyStatsUseCase
import com.kikepb.club.domain.usecase.GetRatingLeaderboardUseCase
import com.kikepb.club.domain.usecase.GetRecentRatingChangesUseCase
import com.kikepb.club.domain.usecase.GetStatsLeaderboardUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeMatchRepository
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.FakeStandingsRepository
import com.kikepb.club.presentation.fake.match
import com.kikepb.club.presentation.fake.member
import com.kikepb.club.presentation.fake.stats
import com.kikepb.club.presentation.memberdetail.MemberStatsViewModel
import com.kikepb.core.domain.util.Result
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
class StandingsViewModelTest {

    private val repository = FakeStandingsRepository()
    private val clubRepository = FakeClubRepository()
    private val matchRepository = FakeMatchRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        clubRepository.members.value = listOf(member("me", userId = "me"), member("m-2", userId = "u-2"))
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = StandingsViewModel(
        getClubMembersUseCase = GetClubMembersUseCase(clubRepository),
        observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubRepository, FakeSessionStorage("me")),
        getRatingLeaderboardUseCase = GetRatingLeaderboardUseCase(repository),
        getMyRatingUseCase = GetMyRatingUseCase(repository),
        getStatsLeaderboardUseCase = GetStatsLeaderboardUseCase(repository),
        savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1"))
    )

    private fun memberStats(memberId: String) = MemberStatsViewModel(
        observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubRepository, FakeSessionStorage("me")),
        getMyStatsUseCase = GetMyStatsUseCase(repository),
        getMyRatingUseCase = GetMyRatingUseCase(repository),
        getStatsLeaderboardUseCase = GetStatsLeaderboardUseCase(repository),
        getRatingLeaderboardUseCase = GetRatingLeaderboardUseCase(repository),
        getRecentRatingChangesUseCase = GetRecentRatingChangesUseCase(matchRepository),
        savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1", "memberId" to memberId))
    )

    @Test
    fun `AC-008-02 rating standings load with my position`() = runTest(UnconfinedTestDispatcher()) {
        repository.ratings = listOf(RatingEntry(1, "m-2", 1040, 3, false), RatingEntry(2, "me", 1010, 3, false))
        repository.myRating = Result.Success(MyRating("me", 1010, 3, false, rank = 2, totalPlayers = 2))
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        val state = viewModel.state.value
        assertEquals(StandingsMode.RATING, state.mode)
        assertEquals(2, state.myRating?.rank)
        assertEquals("me", state.myMemberId)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `AC-008-03 stats default to goals and changing the order reloads them`() = runTest(UnconfinedTestDispatcher()) {
        repository.stats = listOf(StatsEntry(1, stats("me", goals = 2, matches = 1)))
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        viewModel.onAction(StandingsAction.OnModeSelected(StandingsMode.STATS))
        viewModel.onAction(StandingsAction.OnSortSelected(StatsSortBy.WINS))

        assertEquals(listOf("stats:GOALS", "stats:WINS"), repository.calls.filter { it.startsWith("stats") })
        assertEquals(StatsSortBy.WINS, viewModel.state.value.sortBy)
    }

    @Test
    fun `AC-008-06 empty state when nobody has completed matches`() = runTest(UnconfinedTestDispatcher()) {
        repository.ratings = listOf(RatingEntry(1, "me", 1000, 0, true), RatingEntry(1, "m-2", 1000, 0, true))
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.isEmpty)
    }

    @Test
    fun `AC-008-04 my profile uses the me endpoints`() = runTest(UnconfinedTestDispatcher()) {
        repository.myStats = stats("me", goals = 5, matches = 4)
        repository.myRating = Result.Success(MyRating("me", 1020, 4, false, rank = 1, totalPlayers = 2))
        val viewModel = memberStats("me")

        assertEquals(5, viewModel.state.value.stats?.goals)
        assertEquals(1, viewModel.state.value.rank)
        assertTrue("stats/me" in repository.calls && "ratings/me" in repository.calls)
    }

    @Test
    fun `AC-008-04 another member's profile reads their classification rows`() = runTest(UnconfinedTestDispatcher()) {
        repository.stats = listOf(StatsEntry(1, stats("m-2", goals = 3)), StatsEntry(2, stats("me")))
        repository.ratings = listOf(RatingEntry(1, "m-2", 1050, 2, true))
        val viewModel = memberStats("m-2")

        assertEquals(3, viewModel.state.value.stats?.goals)
        assertEquals(1050, viewModel.state.value.rating)
        assertFalse("stats/me" in repository.calls)
    }

    @Test
    fun `AC-008-08 the profile shows the member's recent match ratings`() = runTest(UnconfinedTestDispatcher()) {
        matchRepository.clubMatches = Result.Success(
            listOf(completed("a", "2026-10-01T18:00:00Z", 12), completed("b", "2026-09-24T18:00:00Z", -8))
        )
        val viewModel = memberStats("m-2")

        assertEquals(listOf(12, -8), viewModel.state.value.recentChanges.map { it.change })
    }

    private fun completed(id: String, at: String, change: Int): MatchModel =
        match(status = MatchStatus.COMPLETED, scheduledAt = at).copy(id = id, ratingChanges = mapOf("m-2" to change))
}
