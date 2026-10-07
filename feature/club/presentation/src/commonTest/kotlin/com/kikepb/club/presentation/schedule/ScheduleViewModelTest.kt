package com.kikepb.club.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.usecase.AddScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.DeleteScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.GetScheduleExceptionsUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.SaveScheduleUseCase
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeFeatureFlags
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.member
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

private fun sampleSchedule() = ClubScheduleModel(
    id = "s", clubId = "club-1", dayOfWeek = DayOfWeek.THURSDAY, matchTime = LocalTime(20, 0), timeZone = "Europe/Madrid",
    format = MatchFormat.FIVE_A_SIDE, maxPlayers = 10, durationMinutes = 60, closeDaysBefore = 1, closeTime = LocalTime(22, 0),
    drawDaysBefore = 1, drawTime = LocalTime(22, 0), isActive = true
    )

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private class FakeScheduleRepository : ScheduleRepository {
        var schedule: ClubScheduleModel? = null
        var exceptionRequests = 0
        var saves = 0
        override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> = Result.Success(schedule)
        override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError> {
            saves++
            return Result.Success(sampleSchedule())
        }
        override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError> {
            saves++
            return Result.Success(sampleSchedule())
        }
        override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> {
            exceptionRequests++
            return Result.Success(emptyList())
        }
        override suspend fun addException(clubId: String, date: LocalDate, type: ScheduleExceptionType, newScheduledAt: Instant?, reason: String?) =
            TODO("not used")
        override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> = Result.Success(Unit)
    }

    private val scheduleRepository = FakeScheduleRepository()
    private val clubRepository = FakeClubRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(flags: FakeFeatureFlags, role: ClubMemberRole = ClubMemberRole.ADMIN): ScheduleViewModel {
        clubRepository.members.value = listOf(member("m-1", userId = "me", role = role))
        return ScheduleViewModel(
            observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubRepository, FakeSessionStorage("me")),
            featureFlags = flags,
            getScheduleUseCase = GetScheduleUseCase(scheduleRepository),
            saveScheduleUseCase = SaveScheduleUseCase(scheduleRepository),
            getScheduleExceptionsUseCase = GetScheduleExceptionsUseCase(scheduleRepository),
            addScheduleExceptionUseCase = AddScheduleExceptionUseCase(scheduleRepository),
            deleteScheduleExceptionUseCase = DeleteScheduleExceptionUseCase(scheduleRepository),
            savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1"))
        )
    }

    @Test
    fun `AC-013-08 special weeks are not requested while their feature is off`() = runTest(UnconfinedTestDispatcher()) {
        val flags = FakeFeatureFlags(FeatureFlag.SCHEDULE_EXCEPTIONS to false)
        val viewModel = viewModel(flags)
        viewModel.state.launchIn(backgroundScope)

        assertEquals(0, scheduleRepository.exceptionRequests)

        flags.set(FeatureFlag.SCHEDULE_EXCEPTIONS, true)
        assertEquals(1, scheduleRepository.exceptionRequests)
    }

    @Test
    fun `AC-004-02 a club without schedule offers creating one with defaults`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel(FakeFeatureFlags())
        viewModel.state.launchIn(backgroundScope)

        val state = viewModel.state.value
        assertEquals(null, state.schedule)
        assertNotNull(state.form)
        assertTrue(state.canEdit)
    }

    @Test
    fun `AC-004-06 an invalid draw time is rejected locally without calling the backend`() = runTest(UnconfinedTestDispatcher()) {
        scheduleRepository.schedule = sampleSchedule()
        val viewModel = viewModel(FakeFeatureFlags(FeatureFlag.CUSTOM_DRAW_TIME to true))
        viewModel.state.launchIn(backgroundScope)

        viewModel.onAction(ScheduleAction.OnTimePicked(TimeTarget.CLOSE, LocalTime(21, 0)))
        viewModel.onAction(ScheduleAction.OnTimePicked(TimeTarget.DRAW, LocalTime(20, 0)))
        viewModel.onAction(ScheduleAction.OnSave)

        assertNotNull(viewModel.state.value.formError)
        assertEquals(0, scheduleRepository.saves)
    }

    @Test
    fun `AC-004-03 members who are not managers only get the read-only summary`() = runTest(UnconfinedTestDispatcher()) {
        scheduleRepository.schedule = sampleSchedule()
        val viewModel = viewModel(FakeFeatureFlags(), role = ClubMemberRole.CAPTAIN)
        viewModel.state.launchIn(backgroundScope)

        assertFalse(viewModel.state.value.canEdit)
    }
}
