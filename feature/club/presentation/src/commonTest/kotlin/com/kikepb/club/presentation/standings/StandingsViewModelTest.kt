package com.kikepb.club.presentation.standings

import com.kikepb.club.domain.usecase.GetClubMatchesUseCase
import com.kikepb.club.domain.usecase.GetSeasonsUseCase
import com.kikepb.club.domain.model.SeasonStatus
import com.kikepb.club.presentation.fake.FakeSeasonRepository
import com.kikepb.club.presentation.fake.season
import kotlin.time.Instant
import com.kikepb.club.domain.model.StatsPeriod
import com.kikepb.club.presentation.fake.FixedTestClock
import com.kikepb.club.presentation.fake.StubScheduleRepository
import com.kikepb.club.domain.usecase.GetScheduleUseCase
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
import com.kikepb.club.domain.usecase.SyncClubDetailUseCase
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
    private val seasonRepository = FakeSeasonRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        clubRepository.members.value = listOf(member("me", userId = "me"), member("m-2", userId = "u-2"))
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val scheduleRepository = StubScheduleRepository()
    private val fixedClock = FixedTestClock(Instant.parse("2026-10-08T10:00:00Z"))

    private fun viewModel() = StandingsViewModel(
        getClubMembersUseCase = GetClubMembersUseCase(clubRepository),
        observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubRepository, FakeSessionStorage("me")),
        getRatingLeaderboardUseCase = GetRatingLeaderboardUseCase(repository),
        getMyRatingUseCase = GetMyRatingUseCase(repository),
        getStatsLeaderboardUseCase = GetStatsLeaderboardUseCase(repository),
        syncClubDetailUseCase = SyncClubDetailUseCase(clubRepository),
        getScheduleUseCase = GetScheduleUseCase(scheduleRepository),
        clock = fixedClock,
        getClubMatchesUseCase = GetClubMatchesUseCase(matchRepository),
        getSeasonsUseCase = GetSeasonsUseCase(seasonRepository),
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

    @Test
    fun `AC-015-07 choosing a period reloads the stats with inclusive from and to`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        viewModel.onAction(StandingsAction.OnStatsPeriodSelected(StatsPeriod.THIS_YEAR))
        assertEquals("stats:GOALS:2026-01-01..2026-10-08", repository.calls.last())

        viewModel.onAction(StandingsAction.OnStatsPeriodSelected(StatsPeriod.LAST_30_DAYS))
        assertEquals("stats:GOALS:2026-09-09..2026-10-08", repository.calls.last())

        viewModel.onAction(StandingsAction.OnStatsPeriodSelected(StatsPeriod.ALL_TIME))
        assertEquals("stats:GOALS", repository.calls.last())
    }

    @Test
    fun `AC-018-06 each row shows the rating change of the last completed match`() = runTest(UnconfinedTestDispatcher()) {
        matchRepository.clubMatches = Result.Success(
            listOf(
                match(status = MatchStatus.COMPLETED, scheduledAt = "2026-10-01T18:00:00Z").copy(ratingChanges = mapOf("me" to 5)),
                match(status = MatchStatus.COMPLETED, scheduledAt = "2026-10-08T18:00:00Z").copy(id = "match-2", ratingChanges = mapOf("me" to 12, "m-2" to -3))
            )
        )
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(mapOf("me" to 12, "m-2" to -3), viewModel.state.value.lastMatchDeltas)
    }

    @Test
    fun `AC-020-04 the table opens on the season in progress and a closed one shows its frozen standings`() = runTest(UnconfinedTestDispatcher()) {
        seasonRepository.seasons = listOf(season(2026, SeasonStatus.ACTIVE), season(2025, SeasonStatus.CLOSED))
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals("2026-27", viewModel.state.value.selectedSeasonId)
        assertTrue("ratings@2026-27" in repository.calls)
        assertTrue(viewModel.state.value.isCurrentSeason)

        viewModel.onAction(StandingsAction.OnSeasonSelected("2025-26"))

        assertEquals("ratings@2025-26", repository.calls.last { it.startsWith("ratings") && !it.startsWith("ratings/me") })
        assertEquals("stats:GOALS@2025-26", repository.calls.last())
        assertFalse(viewModel.state.value.isCurrentSeason)
        assertTrue(viewModel.state.value.lastMatchDeltas.isEmpty())
    }

    @Test
    fun `AC-020-02 in pre-season the table explains when the season starts instead of the empty state`() = runTest(UnconfinedTestDispatcher()) {
        seasonRepository.seasons = listOf(season(2026, SeasonStatus.UPCOMING))
        repository.ratings = listOf(RatingEntry(1, "me", 1000, 0, true))
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.isPreSeason)
        assertFalse(viewModel.state.value.isEmpty)
    }

    @Test
    fun `AC-020-04 without seasons (own backend) the table works as before`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(null, viewModel.state.value.selectedSeasonId)
        assertTrue("ratings" in repository.calls)
    }
}
