package com.kikepb.club.presentation.teams

import androidx.lifecycle.SavedStateHandle
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetCurrentAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetMatchUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.GetTeamBalanceUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.presentation.fake.FakeClubLiveUpdates
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeMatchRepository
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.StubScheduleRepository
import com.kikepb.club.presentation.fake.match
import com.kikepb.club.presentation.fake.member
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TeamsViewModelTest {

    private val announcement = MatchAnnouncementModel(
        id = "a-1", matchId = "match-1", clubId = "club-1", maxPlayers = 10, confirmedCount = 4, waitlistCount = 0,
        opensAt = Instant.parse("2026-10-08T22:00:00Z"), closesAt = Instant.parse("2026-10-14T20:00:00Z"),
        drawAt = Instant.parse("2026-10-14T20:30:00Z"), status = AnnouncementStatus.CLOSED, entries = emptyList(), waitlist = emptyList()
    )

    private class FakeAnnouncements(var current: Result<CurrentAnnouncementModel?, ClubError>) : AnnouncementRepository {
        override suspend fun getCurrent(clubId: String) = current
        override suspend fun getHistory(clubId: String) = TODO("not used")
        override suspend fun getByMatch(matchId: String) = TODO("not used")
        override suspend fun enroll(announcementId: String) = TODO("not used")
        override suspend fun withdraw(announcementId: String) = TODO("not used")
        override suspend fun addGuest(announcementId: String, name: String, position: PlayerPosition?) = TODO("not used")
        override suspend fun removeGuest(announcementId: String, guestId: String) = TODO("not used")
    }

    private val announcements = FakeAnnouncements(
        Result.Success(CurrentAnnouncementModel(announcement, Instant.parse("2026-10-15T18:00:00Z"), MyEnrollmentStatus.CONFIRMED, null))
    )
    private val matches = FakeMatchRepository()
    private val clubs = FakeClubRepository().apply {
        members.value = listOf(member("me", userId = "me"), member("m-2", userId = "u-2"))
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = TeamsViewModel(
        getCurrentAnnouncementUseCase = GetCurrentAnnouncementUseCase(announcements),
        getMatchUseCase = GetMatchUseCase(matches),
        getTeamBalanceUseCase = GetTeamBalanceUseCase(matches),
        getScheduleUseCase = GetScheduleUseCase(StubScheduleRepository()),
        getClubMembersUseCase = GetClubMembersUseCase(clubs),
        observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubs, FakeSessionStorage("me")),
        clubLiveUpdates = FakeClubLiveUpdates(),
        savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1"))
    )

    @Test
    fun `AC-018-07 the teams of the current match are shown with me in mine`() = runTest(UnconfinedTestDispatcher()) {
        matches.current = Result.Success(match(teamA = listOf("me"), teamB = listOf("m-2")))
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        val state = viewModel.state.value
        assertTrue(state.hasTeams)
        assertEquals("me", state.myMemberId)
        assertEquals(com.kikepb.club.domain.model.Team.A, state.match?.teamOf("me"))
        assertEquals(announcement.drawAt, state.drawAt)
    }

    @Test
    fun `AC-018-07 before the draw the tab says when teams are published`() = runTest(UnconfinedTestDispatcher()) {
        matches.current = Result.Success(match())
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.hasMatch)
        assertFalse(viewModel.state.value.hasTeams)
    }

    @Test
    fun `AC-018-07 without a current match there is nothing to draw`() = runTest(UnconfinedTestDispatcher()) {
        announcements.current = Result.Success(null)
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.hasLoaded)
        assertFalse(viewModel.state.value.hasMatch)
    }

    @Test
    fun `AC-017-02 a failed first load offers a retry`() = runTest(UnconfinedTestDispatcher()) {
        announcements.current = Result.Failure(ClubError.NotFound)
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.loadFailed)
        assertFalse(viewModel.state.value.isRefreshing)
    }
}
