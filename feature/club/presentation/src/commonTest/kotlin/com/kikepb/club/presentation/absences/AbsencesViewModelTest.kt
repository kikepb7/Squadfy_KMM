package com.kikepb.club.presentation.absences

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.repository.AbsenceRepository
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.usecase.AddMyAbsenceUseCase
import com.kikepb.club.domain.usecase.DeleteMyAbsenceUseCase
import com.kikepb.club.domain.usecase.GetAbsencesUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.presentation.fake.FakeClubRepository
import com.kikepb.club.presentation.fake.FakeSessionStorage
import com.kikepb.club.presentation.fake.member
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AbsencesViewModelTest {

    private class FakeAbsenceRepository : AbsenceRepository {
        val absences = mutableListOf(
            MemberAbsenceModel("ab-other", "m-2", LocalDate(2026, 10, 20), LocalDate(2026, 10, 22), "Viaje"),
            MemberAbsenceModel("ab-mine", "me", LocalDate(2026, 10, 9), LocalDate(2026, 10, 9), null)
        )
        val added = mutableListOf<Pair<LocalDate, LocalDate>>()
        override suspend fun getAbsences(clubId: String, from: LocalDate?, to: LocalDate?): Result<List<MemberAbsenceModel>, ClubError> =
            Result.Success(absences.toList())
        override suspend fun addMyAbsence(clubId: String, fromDate: LocalDate, toDate: LocalDate, reason: String?): Result<MemberAbsenceModel, ClubError> {
            added += fromDate to toDate
            return Result.Success(MemberAbsenceModel("new", "me", fromDate, toDate, reason))
        }
        override suspend fun deleteMyAbsence(clubId: String, absenceId: String): EmptyResult<ClubError> {
            absences.removeAll { it.id == absenceId }
            return Result.Success(Unit)
        }
    }

    private class NoSchedule : ScheduleRepository {
        override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> = Result.Success(null)
        override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
        override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> = TODO("not used")
        override suspend fun addException(clubId: String, date: LocalDate, type: ScheduleExceptionType, newScheduledAt: Instant?, reason: String?) = TODO("not used")
        override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> = TODO("not used")
    }

    private val repository = FakeAbsenceRepository()
    private val clubRepository = FakeClubRepository()
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-10-07T12:00:00Z")
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        clubRepository.members.value = listOf(member("me", userId = "me"), member("m-2", userId = "u-2"))
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = AbsencesViewModel(
        getClubMembersUseCase = GetClubMembersUseCase(clubRepository),
        observeMyMembershipUseCase = ObserveMyMembershipUseCase(clubRepository, FakeSessionStorage("me")),
        getAbsencesUseCase = GetAbsencesUseCase(repository),
        addMyAbsenceUseCase = AddMyAbsenceUseCase(repository),
        deleteMyAbsenceUseCase = DeleteMyAbsenceUseCase(repository),
        getScheduleUseCase = GetScheduleUseCase(NoSchedule()),
        clock = clock,
        savedStateHandle = SavedStateHandle(mapOf("clubId" to "club-1"))
    )

    @Test
    fun `AC-014-01 absences are listed by start date`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)

        assertEquals(listOf("ab-mine", "ab-other"), viewModel.state.value.absences.map { it.id })
    }

    @Test
    fun `AC-014-02 an inverted period is rejected locally`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.onAction(AbsencesAction.OnAddClick)
        viewModel.onAction(AbsencesAction.OnDatePicked(AbsenceDateTarget.FROM, LocalDate(2026, 10, 12)))
        viewModel.onAction(AbsencesAction.OnDatePicked(AbsenceDateTarget.TO, LocalDate(2026, 10, 10)))

        viewModel.onAction(AbsencesAction.OnSave)

        assertNotNull(viewModel.state.value.form?.error)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `AC-014-03 creating an absence tells me I was withdrawn from open announcements`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)
        viewModel.onAction(AbsencesAction.OnAddClick)
        viewModel.onAction(AbsencesAction.OnDatePicked(AbsenceDateTarget.TO, LocalDate(2026, 10, 14)))

        viewModel.events.test {
            viewModel.onAction(AbsencesAction.OnSave)
            assertIs<AbsencesEvent.ShowMessage>(awaitItem())
        }
        assertEquals(listOf(LocalDate(2026, 10, 7) to LocalDate(2026, 10, 14)), repository.added)
        assertNull(viewModel.state.value.form)
    }

    @Test
    fun `AC-014-04 only my absences can be deleted`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = viewModel()
        viewModel.state.launchIn(backgroundScope)
        val (mine, other) = viewModel.state.value.absences.partition { it.clubMemberId == "me" }

        assertTrue(viewModel.state.value.canDelete(mine.single()))
        assertFalse(viewModel.state.value.canDelete(other.single()))

        viewModel.onAction(AbsencesAction.OnDeleteClick(mine.single()))
        viewModel.onAction(AbsencesAction.OnConfirmDelete)
        assertEquals(listOf("ab-other"), viewModel.state.value.absences.map { it.id })
    }
}
