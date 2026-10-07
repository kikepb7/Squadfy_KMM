package com.kikepb.club.presentation.match

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MatchGuestModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.PlayerRatingInTeam
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.model.Team
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.model.TeamStrengthModel
import com.kikepb.club.domain.policy.TeamsPending
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.usecase.GenerateTeamsUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetMatchAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetMatchUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.GetTeamBalanceUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeMatchRepository
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.match
import com.kikepb.club.presentation.fake.member
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MatchDetailViewModelTest {

    private val closesAt = Instant.parse("2026-10-14T20:00:00Z")
    private val drawAt = Instant.parse("2026-10-15T10:00:00Z")
    private var now = Instant.parse("2026-10-13T12:00:00Z")
    private var announcementStatus = AnnouncementStatus.OPEN

    private inner class MatchAnnouncementRepository : AnnouncementRepository {
        override suspend fun getCurrent(clubId: String): Result<CurrentAnnouncementModel?, ClubError> = TODO("not used")
        override suspend fun getHistory(clubId: String): Result<List<MatchAnnouncementModel>, ClubError> = TODO("not used")
        override suspend fun getByMatch(matchId: String): Result<MatchAnnouncementModel, ClubError> = Result.Success(
            MatchAnnouncementModel(
                id = "a-1", matchId = matchId, clubId = "club-1", maxPlayers = 10, confirmedCount = 4, waitlistCount = 0,
                opensAt = Instant.parse("2026-10-09T00:00:00Z"), closesAt = closesAt, drawAt = drawAt,
                status = announcementStatus, entries = emptyList(), waitlist = emptyList()
            )
        )
        override suspend fun enroll(announcementId: String) = TODO("not used")
        override suspend fun withdraw(announcementId: String) = TODO("not used")
        override suspend fun addGuest(announcementId: String, name: String, position: PlayerPosition?) = TODO("not used")
        override suspend fun removeGuest(announcementId: String, guestId: String) = TODO("not used")
    }

    private class NoScheduleRepository : ScheduleRepository {
        override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> = Result.Success(null)
        override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> = TODO("not used")
        override suspend fun addException(clubId: String, date: LocalDate, type: ScheduleExceptionType, newScheduledAt: Instant?, reason: String?) = TODO("not used")
        override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> = TODO("not used")
    }

    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    private val repository = FakeMatchRepository()
    private val clubRepository = FakeClubRepository()
    private val guest = MatchGuestModel(guestId = "g-1", name = "Luis", position = PlayerPosition.GOALKEEPER, invitedByMemberId = "me")

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(role: ClubMemberRole = ClubMemberRole.PLAYER): MatchDetailViewModel {
        clubRepository.members.value = listOf(member("me", userId = "me", role = role)) + (2..4).map { member("m-$it", userId = "u-$it") }
        return MatchDetailViewModel(
            getClubMembersUseCase = GetClubMembersUseCase(clubRepository),
            observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubRepository, FakeSessionStorage("me")),
            getMatchUseCase = GetMatchUseCase(repository),
            getMatchAnnouncementUseCase = GetMatchAnnouncementUseCase(MatchAnnouncementRepository()),
            getTeamBalanceUseCase = GetTeamBalanceUseCase(repository),
            generateTeamsUseCase = GenerateTeamsUseCase(repository),
            getScheduleUseCase = GetScheduleUseCase(NoScheduleRepository()),
            clock = clock,
            savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1", "matchId" to "match-1"))
        )
    }

    private val withTeams = match(teamA = listOf("me", "m-2"), teamB = listOf("m-3", "m-4"), teamBGuests = listOf(guest))

    @Test
    fun `AC-006-01 teams resolve members and highlight my team`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams)
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        val state = viewModel.state.value
        assertEquals(Team.A, state.myTeam)
        assertEquals("u-2", state.teamA.single { it.id == "m-2" }.name)
        assertTrue(state.teamA.single { it.id == "me" }.isMe)
    }

    @Test
    fun `AC-006-08 guests appear in their team flagged as guests`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams)
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        val luis = viewModel.state.value.teamB.single { it.isGuest }
        assertEquals("Luis", luis.name)
        assertEquals(PlayerPosition.GOALKEEPER, luis.position)
    }

    @Test
    fun `AC-006-02 without teams and the announcement open, they come at close`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(TeamsPending.AtClose(closesAt), viewModel.state.value.pending)
    }

    @Test
    fun `AC-006-09 without teams and the announcement closed, the draw time is shown`() = runTest(UnconfinedTestDispatcher()) {
        announcementStatus = AnnouncementStatus.CLOSED
        now = Instant.parse("2026-10-15T08:00:00Z")
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(TeamsPending.AtDraw(drawAt), viewModel.state.value.pending)
    }

    @Test
    fun `AC-006-03 players never request the team balance`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams)
        val viewModel = viewModel(role = ClubMemberRole.PLAYER)
        viewModel.state.launchIn(backgroundScope)

        assertFalse("balance" in repository.calls)
        assertFalse(viewModel.state.value.canRectify)
    }

    @Test
    fun `AC-006-03 managers load the balance`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams)
        val strength = TeamStrengthModel(2, 1000, 2000, listOf(PlayerRatingInTeam("me", 1000, false)))
        repository.balance = Result.Success(TeamBalanceModel(strength, strength, 0, 0.5))
        val viewModel = viewModel(role = ClubMemberRole.ADMIN)
        viewModel.state.launchIn(backgroundScope)

        assertTrue("balance" in repository.calls)
        assertEquals(0.5, viewModel.state.value.balance?.teamAExpectedScore)
        assertTrue(viewModel.state.value.canRectify)
    }

    @Test
    fun `AC-006-04 redraw asks for confirmation and sends AUTO`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams)
        val viewModel = viewModel(role = ClubMemberRole.OWNER)
        viewModel.state.launchIn(backgroundScope)

        viewModel.onAction(MatchDetailAction.OnRedrawClick)
        assertEquals(MatchDetailDialog.ConfirmRedraw, viewModel.state.value.dialog)
        viewModel.onAction(MatchDetailAction.OnConfirmRedraw)

        assertTrue("teams-auto" in repository.calls)
        assertNull(viewModel.state.value.dialog)
    }

    @Test
    fun `AC-006-05 manual adjustment validates locally and sends member and guest ids`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams)
        val viewModel = viewModel(role = ClubMemberRole.OWNER)
        viewModel.state.launchIn(backgroundScope)

        viewModel.onAction(MatchDetailAction.OnStartManualEdit)
        viewModel.onAction(MatchDetailAction.OnMovePlayer("me")) // 1 vs 4: invalid
        assertFalse(viewModel.state.value.editing!!.isValid)
        viewModel.onAction(MatchDetailAction.OnSaveManualEdit)
        assertFalse("teams-manual" in repository.calls)

        viewModel.onAction(MatchDetailAction.OnMovePlayer("g-1")) // 2 vs 3
        viewModel.onAction(MatchDetailAction.OnSaveManualEdit)

        assertEquals(listOf("m-2", "g-1"), repository.lastTeams?.first)
        assertEquals(listOf("m-3", "m-4", "me"), repository.lastTeams?.second)
        assertNull(viewModel.state.value.editing)
    }

    @Test
    fun `AC-006-06 a completed match is read-only even for managers`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams.copy(status = MatchStatus.COMPLETED))
        val viewModel = viewModel(role = ClubMemberRole.OWNER)
        viewModel.state.launchIn(backgroundScope)

        assertFalse(viewModel.state.value.canRectify)
    }

    @Test
    fun `AC-006-06 a 409 on the draw shows a message and reloads the match`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(withTeams)
        val viewModel = viewModel(role = ClubMemberRole.OWNER)
        viewModel.state.launchIn(backgroundScope)
        repository.failure = ClubError.Remote(RemoteError(DataError.Remote.CONFLICT))
        val getsBefore = repository.calls.count { it == "get" }

        viewModel.events.test {
            viewModel.onAction(MatchDetailAction.OnRedrawClick)
            viewModel.onAction(MatchDetailAction.OnConfirmRedraw)
            assertIs<MatchDetailEvent.ShowMessage>(awaitItem())
        }
        assertEquals(getsBefore + 1, repository.calls.count { it == "get" })
    }
}
