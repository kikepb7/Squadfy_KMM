package com.kikepb.club.presentation.announcement

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.AnnouncementEntry
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.EntryStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.ParticipantType
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.policy.WindowState
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.repository.AbsenceRepository
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.club.domain.usecase.GetAbsencesUseCase
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.usecase.AddGuestToAnnouncementUseCase
import com.kikepb.club.domain.usecase.EnrollUseCase
import com.kikepb.club.domain.usecase.GetAnnouncementHistoryUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetCurrentAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.RemoveGuestFromAnnouncementUseCase
import com.kikepb.club.domain.usecase.WithdrawUseCase
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeFeatureFlags
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.member
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.notification.InAppPushCenter
import com.kikepb.core.domain.notification.PushMessage
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

private val NOW = Instant.parse("2026-10-10T12:00:00Z")

private fun announcement(
    entries: List<AnnouncementEntry> = emptyList(),
    waitlist: List<AnnouncementEntry> = emptyList(),
    closesAt: String = "2026-10-14T20:00:00Z"
) = MatchAnnouncementModel(
    id = "a-1", matchId = "m-1", clubId = "club-1", maxPlayers = 10, confirmedCount = entries.size, waitlistCount = waitlist.size,
    opensAt = Instant.parse("2026-10-08T22:00:00Z"), closesAt = Instant.parse(closesAt), drawAt = Instant.parse(closesAt),
    status = AnnouncementStatus.OPEN, entries = entries, waitlist = waitlist
)

private fun entry(id: String, memberId: String?, status: EntryStatus = EntryStatus.CONFIRMED, invitedBy: String? = null) = AnnouncementEntry(
    id = id, participantType = if (memberId == null) ParticipantType.GUEST else ParticipantType.MEMBER, clubMemberId = memberId,
    guestName = if (memberId == null) "Luis" else null, guestPosition = null, invitedByMemberId = invitedBy, status = status,
    enrolledAt = NOW
)

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncementViewModelTest {

    private class FakeAnnouncementRepository : AnnouncementRepository {
        var current: Result<CurrentAnnouncementModel?, ClubError> = Result.Success(
            CurrentAnnouncementModel(announcement(), Instant.parse("2026-10-15T18:00:00Z"), MyEnrollmentStatus.NOT_ENROLLED, null)
        )
        var enrollResult: Result<MatchAnnouncementModel, ClubError> = Result.Success(announcement(entries = listOf(entry("e-1", "me"))))
        var currentRequests = 0
        override suspend fun getCurrent(clubId: String): Result<CurrentAnnouncementModel?, ClubError> {
            currentRequests++
            return current
        }
        override suspend fun getHistory(clubId: String): Result<List<MatchAnnouncementModel>, ClubError> = Result.Success(emptyList())
        override suspend fun getByMatch(matchId: String) = TODO("not used")
        override suspend fun enroll(announcementId: String) = enrollResult
        override suspend fun withdraw(announcementId: String): Result<MatchAnnouncementModel, ClubError> = Result.Success(announcement())
        override suspend fun addGuest(announcementId: String, name: String, position: PlayerPosition?): Result<MatchAnnouncementModel, ClubError> =
            Result.Success(announcement(entries = listOf(entry("g-1", null, invitedBy = "me"))))
        override suspend fun removeGuest(announcementId: String, guestId: String): Result<MatchAnnouncementModel, ClubError> = Result.Success(announcement())
    }

    private class NoScheduleRepository : ScheduleRepository {
        override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> = Result.Success(null)
        override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> = Result.Success(emptyList())
        override suspend fun addException(clubId: String, date: LocalDate, type: ScheduleExceptionType, newScheduledAt: Instant?, reason: String?) = TODO("not used")
        override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> = TODO("not used")
    }

    private object FixedClock : Clock {
        override fun now(): Instant = NOW
    }

    private val repository = FakeAnnouncementRepository()
    private val clubRepository = FakeClubRepository()
    private val pushCenter = InAppPushCenter()
    private val absenceRepository = object : AbsenceRepository {
        var absences = emptyList<MemberAbsenceModel>()
        override suspend fun getAbsences(clubId: String, from: LocalDate?, to: LocalDate?): Result<List<MemberAbsenceModel>, ClubError> = Result.Success(absences)
        override suspend fun addMyAbsence(clubId: String, fromDate: LocalDate, toDate: LocalDate, reason: String?) = TODO("not used")
        override suspend fun deleteMyAbsence(clubId: String, absenceId: String): EmptyResult<ClubError> = TODO("not used")
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(flags: FakeFeatureFlags = FakeFeatureFlags(), role: ClubMemberRole = ClubMemberRole.PLAYER): AnnouncementViewModel {
        clubRepository.members.value = listOf(member("me", userId = "me", role = role), member("m-other", userId = "other"))
        return AnnouncementViewModel(
            getClubMembersUseCase = GetClubMembersUseCase(clubRepository),
            observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubRepository, FakeSessionStorage("me")),
            featureFlags = flags,
            getAbsencesUseCase = GetAbsencesUseCase(absenceRepository),
            getCurrentAnnouncementUseCase = GetCurrentAnnouncementUseCase(repository),
            getAnnouncementHistoryUseCase = GetAnnouncementHistoryUseCase(repository),
            getScheduleUseCase = GetScheduleUseCase(NoScheduleRepository()),
            enrollUseCase = EnrollUseCase(repository),
            withdrawUseCase = WithdrawUseCase(repository),
            addGuestUseCase = AddGuestToAnnouncementUseCase(repository),
            removeGuestUseCase = RemoveGuestFromAnnouncementUseCase(repository),
            clock = FixedClock,
            inAppPushCenter = pushCenter,
            savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1"))
        )
    }

    @Test
    fun `AC-005-01 no scheduled match shows the empty state`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(null)
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.hasLoaded)
        assertNull(viewModel.state.value.current)
    }

    @Test
    fun `AC-005-03 the window is open with the club announcement times`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(WindowState.OPEN, viewModel.state.value.windowState)
    }

    @Test
    fun `AC-005-05 enrolling uses the returned announcement to update my status`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        viewModel.onAction(AnnouncementAction.OnEnrollClick)

        assertEquals(MyEnrollmentStatus.CONFIRMED, viewModel.state.value.current?.myStatus)
        assertEquals(1, viewModel.state.value.confirmed.size)
    }

    @Test
    fun `AC-005-05 a full announcement puts me on the waitlist with my position`() = runTest(UnconfinedTestDispatcher()) {
        repository.enrollResult = Result.Success(
            announcement(entries = listOf(entry("e-1", "m-other")), waitlist = listOf(entry("e-2", "me", EntryStatus.WAITLISTED)))
        )
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        viewModel.events.test {
            viewModel.onAction(AnnouncementAction.OnEnrollClick)
            assertIs<AnnouncementEvent.ShowMessage>(awaitItem())
        }
        assertEquals(MyEnrollmentStatus.WAITLISTED, viewModel.state.value.current?.myStatus)
        assertEquals(1, viewModel.state.value.current?.myWaitlistPosition)
    }

    @Test
    fun `AC-005-07 a closed window error shows a message and refreshes`() = runTest(UnconfinedTestDispatcher()) {
        repository.enrollResult = Result.Failure(ClubError.Remote(RemoteError(DataError.Remote.BAD_REQUEST)))
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)
        val requestsBefore = repository.currentRequests

        viewModel.onAction(AnnouncementAction.OnEnrollClick)

        assertEquals(requestsBefore + 1, repository.currentRequests)
    }

    @Test
    fun `AC-005-07 a 403 NOT_CLUB_MEMBER leaves the club screen`() = runTest(UnconfinedTestDispatcher()) {
        repository.enrollResult = Result.Failure(ClubError.NotClubMember)
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        viewModel.events.test {
            viewModel.onAction(AnnouncementAction.OnEnrollClick)
            assertEquals(AnnouncementEvent.NotMemberAnymore, awaitItem())
        }
    }

    @Test
    fun `AC-005-13 guests can be added only with the flag on, and the host can remove them`() = runTest(UnconfinedTestDispatcher()) {
        val off = viewModel(FakeFeatureFlags(FeatureFlag.MATCH_GUESTS to false))
        off.state.launchIn(backgroundScope)
        assertFalse(off.state.value.canAddGuest)

        val on = viewModel(FakeFeatureFlags(FeatureFlag.MATCH_GUESTS to true))
        on.state.launchIn(backgroundScope)
        assertTrue(on.state.value.canAddGuest)

        on.onAction(AnnouncementAction.OnAddGuestClick)
        (on.state.value.dialog as AnnouncementDialog.AddGuest).name.edit { append("Luis") }
        on.onAction(AnnouncementAction.OnConfirmAddGuest)

        val guest = on.state.value.confirmed.single()
        assertTrue(guest.isGuest)
        assertTrue(on.state.value.canRemoveGuest(guest))
    }

    @Test
    fun `AC-005-13 a regular member cannot remove a guest of somebody else`() = runTest(UnconfinedTestDispatcher()) {
        repository.current = Result.Success(
            CurrentAnnouncementModel(
                announcement(entries = listOf(entry("g-1", null, invitedBy = "m-other"))),
                Instant.parse("2026-10-15T18:00:00Z"), MyEnrollmentStatus.NOT_ENROLLED, null
            )
        )
        val viewModel = viewModel(FakeFeatureFlags(FeatureFlag.MATCH_GUESTS to true))
        viewModel.state.launchIn(backgroundScope)

        assertFalse(viewModel.state.value.canRemoveGuest(viewModel.state.value.confirmed.single()))
    }

    @Test
    fun `AC-009-04 a push of the visible club refreshes the tab and shows its text`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.onAction(AnnouncementAction.OnVisibilityChanged(true))
        val before = repository.currentRequests

        viewModel.events.test {
            assertTrue(pushCenter.onForegroundMessage(PushMessage(mapOf("type" to "match.announcement.opened", "clubId" to "club-1"), body = "¡Apúntate!")))
            assertIs<AnnouncementEvent.ShowMessage>(awaitItem())
        }
        assertEquals(before + 1, repository.currentRequests)

        viewModel.onAction(AnnouncementAction.OnVisibilityChanged(false))
        assertFalse(pushCenter.isClubVisible("club-1"))
    }

    @Test
    fun `AC-005-15 my absence on the match day shows a warning without blocking sign-up`() = runTest(UnconfinedTestDispatcher()) {
        // The match of the default announcement is on 2026-10-15
        absenceRepository.absences = listOf(MemberAbsenceModel("ab-1", "me", LocalDate(2026, 10, 14), LocalDate(2026, 10, 16), null))
        val viewModel = viewModel(flags = FakeFeatureFlags(FeatureFlag.MEMBER_ABSENCES to true))
        viewModel.state.launchIn(backgroundScope)

        assertTrue(viewModel.state.value.hasAbsenceOnMatchDay)
        assertEquals(WindowState.OPEN, viewModel.state.value.windowState)

        val off = viewModel(flags = FakeFeatureFlags(FeatureFlag.MEMBER_ABSENCES to false))
        off.state.launchIn(backgroundScope)
        assertFalse(off.state.value.hasAbsenceOnMatchDay)
    }
}
