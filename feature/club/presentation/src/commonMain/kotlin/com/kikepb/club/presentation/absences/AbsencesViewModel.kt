package com.kikepb.club.presentation.absences

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.usecase.AbsenceValidationError
import com.kikepb.club.domain.usecase.AddAbsenceError
import com.kikepb.club.domain.usecase.AddMyAbsenceUseCase
import com.kikepb.club.domain.usecase.DeleteMyAbsenceUseCase
import com.kikepb.club.domain.usecase.GetAbsencesUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.absences_created
import squadfy_app.feature.club.presentation.generated.resources.absences_error_long
import squadfy_app.feature.club.presentation.generated.resources.absences_error_order
import squadfy_app.feature.club.presentation.generated.resources.absences_error_past
import squadfy_app.feature.club.presentation.generated.resources.absences_error_reason
import kotlin.time.Clock

/** Club absences: everyone's upcoming ones, adding and deleting mine (spec 014, behind MEMBER_ABSENCES). */
class AbsencesViewModel(
    getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val getAbsencesUseCase: GetAbsencesUseCase,
    private val addMyAbsenceUseCase: AddMyAbsenceUseCase,
    private val deleteMyAbsenceUseCase: DeleteMyAbsenceUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val eventChannel = Channel<AbsencesEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AbsencesState())

    val state = combine(_state, getClubMembersUseCase(clubId), observeMyMembershipUseCase(clubId)) { current, members, me ->
        current.copy(members = members.associateBy { it.id }, myMemberId = me?.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = _state.value
    )

    init {
        viewModelScope.launch {
            // APP-RN-03: "today" and the dates are those of the club
            getScheduleUseCase(clubId).onSuccess { schedule -> schedule?.let { _state.update { state -> state.copy(timeZoneId = it.timeZone) } } }
            load()
        }
    }

    fun onAction(action: AbsencesAction) {
        when (action) {
            AbsencesAction.OnRefresh -> viewModelScope.launch { load() }
            AbsencesAction.OnAddClick -> _state.update { it.copy(form = AbsenceForm(fromDate = today(), toDate = today())) }
            AbsencesAction.OnDismissForm -> _state.update { it.copy(form = null, picker = null) }
            is AbsencesAction.OnOpenPicker -> _state.update { it.copy(picker = action.target) }
            AbsencesAction.OnDismissPicker -> _state.update { it.copy(picker = null) }
            is AbsencesAction.OnDatePicked -> _state.update { state ->
                val form = state.form ?: return@update state
                state.copy(
                    picker = null,
                    form = if (action.target == AbsenceDateTarget.FROM) form.copy(fromDate = action.date, error = null) else form.copy(toDate = action.date, error = null)
                )
            }
            AbsencesAction.OnSave -> save()
            is AbsencesAction.OnDeleteClick -> _state.update { it.copy(confirmDelete = action.absence) }
            AbsencesAction.OnConfirmDelete -> delete()
            AbsencesAction.OnDismissDelete -> _state.update { it.copy(confirmDelete = null) }
        }
    }

    private suspend fun load() {
        _state.update { it.copy(isLoading = true) }
        getAbsencesUseCase(clubId, from = today())
            .onSuccess { absences -> _state.update { it.copy(absences = absences) } }
            .onFailure { error -> eventChannel.send(AbsencesEvent.ShowMessage(error.toUiText())) }
        _state.update { it.copy(isLoading = false) }
    }

    private fun save() {
        val form = _state.value.form ?: return
        if (_state.value.isWorking) return
        _state.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            addMyAbsenceUseCase(clubId, form.fromDate, form.toDate, form.reason.text.toString(), today())
                .onSuccess { created ->
                    _state.update { state -> state.copy(form = null, absences = (state.absences + created).sortedBy { it.fromDate }) }
                    // AC-014-03: the backend withdrew me from open announcements in that period
                    eventChannel.send(AbsencesEvent.ShowMessage(UiText.Resource(Res.string.absences_created)))
                }
                .onFailure { error ->
                    when (error) {
                        is AddAbsenceError.Invalid -> _state.update { it.copy(form = form.copy(error = error.reason.toUiText())) }
                        is AddAbsenceError.Remote -> _state.update { it.copy(form = form.copy(error = error.error.toUiText())) }
                    }
                }
            _state.update { it.copy(isWorking = false) }
        }
    }

    private fun delete() {
        val absence = _state.value.confirmDelete ?: return
        _state.update { it.copy(confirmDelete = null) }
        viewModelScope.launch {
            deleteMyAbsenceUseCase(clubId, absence.id)
                .onSuccess { _state.update { state -> state.copy(absences = state.absences.filterNot { it.id == absence.id }) } }
                .onFailure { error -> eventChannel.send(AbsencesEvent.ShowMessage(error.toUiText())) }
        }
    }

    private fun today(): LocalDate = clock.now().toLocalDateTime(_state.value.zone).date

    private fun AbsenceValidationError.toUiText(): UiText = UiText.Resource(
        when (this) {
            AbsenceValidationError.END_BEFORE_START -> Res.string.absences_error_order
            AbsenceValidationError.ENDS_IN_PAST -> Res.string.absences_error_past
            AbsenceValidationError.TOO_LONG -> Res.string.absences_error_long
            AbsenceValidationError.REASON_TOO_LONG -> Res.string.absences_error_reason
        }
    )
}

enum class AbsenceDateTarget { FROM, TO }

data class AbsenceForm(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val reason: TextFieldState = TextFieldState(),
    val error: UiText? = null
)

data class AbsencesState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val timeZoneId: String = TimeZone.currentSystemDefault().id,
    val absences: List<MemberAbsenceModel> = emptyList(),
    val members: Map<String, ClubMemberModel> = emptyMap(),
    val myMemberId: String? = null,
    val form: AbsenceForm? = null,
    val picker: AbsenceDateTarget? = null,
    val confirmDelete: MemberAbsenceModel? = null
) {
    val zone: TimeZone get() = runCatching { TimeZone.of(timeZoneId) }.getOrDefault(TimeZone.currentSystemDefault())

    /** AC-014-04: only my own absences can be deleted. */
    fun canDelete(absence: MemberAbsenceModel): Boolean = myMemberId != null && absence.clubMemberId == myMemberId
}

sealed interface AbsencesAction {
    data object OnRefresh : AbsencesAction
    data object OnAddClick : AbsencesAction
    data object OnDismissForm : AbsencesAction
    data class OnOpenPicker(val target: AbsenceDateTarget) : AbsencesAction
    data object OnDismissPicker : AbsencesAction
    data class OnDatePicked(val target: AbsenceDateTarget, val date: LocalDate) : AbsencesAction
    data object OnSave : AbsencesAction
    data class OnDeleteClick(val absence: MemberAbsenceModel) : AbsencesAction
    data object OnConfirmDelete : AbsencesAction
    data object OnDismissDelete : AbsencesAction
}

sealed interface AbsencesEvent {
    data class ShowMessage(val message: UiText) : AbsencesEvent
}
