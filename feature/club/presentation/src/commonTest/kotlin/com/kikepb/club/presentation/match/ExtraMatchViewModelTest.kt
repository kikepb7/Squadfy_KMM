package com.kikepb.club.presentation.match

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.usecase.CreateExtraMatchUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.presentation.extramatch.ExtraMatchAction
import com.kikepb.club.presentation.extramatch.ExtraMatchEvent
import com.kikepb.club.presentation.extramatch.ExtraMatchViewModel
import com.kikepb.club.presentation.fake.FakeFeatureFlags
import com.kikepb.club.presentation.fake.FakeMatchRepository
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ExtraMatchViewModelTest {

    private class UtcScheduleRepository : ScheduleRepository {
        override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> = Result.Success(null)
        override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> = TODO("not used")
        override suspend fun addException(clubId: String, date: LocalDate, type: ScheduleExceptionType, newScheduledAt: Instant?, reason: String?) = TODO("not used")
        override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> = TODO("not used")
    }

    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-10-07T12:00:00Z")
    }
    private val repository = FakeMatchRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(flags: FakeFeatureFlags = FakeFeatureFlags()) = ExtraMatchViewModel(
        featureFlags = flags,
        createExtraMatchUseCase = CreateExtraMatchUseCase(repository),
        getScheduleUseCase = GetScheduleUseCase(UtcScheduleRepository()),
        clock = clock,
        savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1"))
    )

    @Test
    fun `AC-007-08 a past date is rejected locally`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.onAction(ExtraMatchAction.OnOpen)
        viewModel.onAction(ExtraMatchAction.OnDatePicked(LocalDate(2026, 10, 1)))
        viewModel.onAction(ExtraMatchAction.OnTimePicked(LocalTime(20, 0)))

        viewModel.onAction(ExtraMatchAction.OnConfirm)

        assertNotNull(viewModel.state.value.error)
        assertFalse("extra" in repository.calls)
    }

    @Test
    fun `AC-007-08 a future date creates the match and closes the form`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.onAction(ExtraMatchAction.OnOpen)
        viewModel.onAction(ExtraMatchAction.OnDatePicked(LocalDate(2026, 10, 9)))
        viewModel.onAction(ExtraMatchAction.OnTimePicked(LocalTime(20, 0)))

        viewModel.events.test {
            viewModel.onAction(ExtraMatchAction.OnConfirm)
            assertIs<ExtraMatchEvent.Created>(awaitItem())
        }
        assertTrue("extra" in repository.calls)
        assertFalse(viewModel.state.value.isOpen)
    }

    @Test
    fun `AC-007-08 the quick test match is only offered with DEV_TEST_MATCH`() = runTest(UnconfinedTestDispatcher()) {
        val off = viewModel(FakeFeatureFlags(FeatureFlag.DEV_TEST_MATCH to false))
        off.state.launchIn(backgroundScope)
        assertFalse(off.state.value.quickTestEnabled)

        val on = viewModel(FakeFeatureFlags(FeatureFlag.DEV_TEST_MATCH to true))
        on.state.launchIn(backgroundScope)
        assertTrue(on.state.value.quickTestEnabled)
        on.onAction(ExtraMatchAction.OnOpen)
        on.onAction(ExtraMatchAction.OnQuickTest)
        assertTrue(on.state.value.canConfirm)
    }
}
