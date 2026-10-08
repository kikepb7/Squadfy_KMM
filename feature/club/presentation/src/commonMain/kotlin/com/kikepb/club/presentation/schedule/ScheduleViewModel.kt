package com.kikepb.club.presentation.schedule

import kotlinx.coroutines.Job
import com.kikepb.core.domain.realtime.ClubLiveUpdates
import com.kikepb.core.domain.realtime.ClubDataScope
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.policy.MemberPermissions
import com.kikepb.club.domain.usecase.AddScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.DeleteScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.GetScheduleExceptionsUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.SaveScheduleError
import com.kikepb.club.domain.usecase.SaveScheduleUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.util.DataError
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.exceptions_added
import squadfy_app.feature.club.presentation.generated.resources.exceptions_conflict
import squadfy_app.feature.club.presentation.generated.resources.schedule_created
import squadfy_app.feature.club.presentation.generated.resources.schedule_error_time_zone
import squadfy_app.feature.club.presentation.generated.resources.schedule_saved

/** Weekly schedule screen (spec 004). Managers edit; everyone sees the summary and the special weeks. */
class ScheduleViewModel(
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    featureFlags: FeatureFlags,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val saveScheduleUseCase: SaveScheduleUseCase,
    private val getScheduleExceptionsUseCase: GetScheduleExceptionsUseCase,
    private val addScheduleExceptionUseCase: AddScheduleExceptionUseCase,
    private val deleteScheduleExceptionUseCase: DeleteScheduleExceptionUseCase,
    private val clubLiveUpdates: ClubLiveUpdates,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val eventChannel = Channel<ScheduleEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ScheduleState())
    // Declared before init: an initializer placed after it would reset the job started there (spec 017)
    private var loadJob: Job? = null

    val state = combine(
        _state,
        observeMyMembershipUseCase(clubId),
        featureFlags.observe(FeatureFlag.CUSTOM_DRAW_TIME),
        featureFlags.observe(FeatureFlag.SCHEDULE_EXCEPTIONS)
    ) { current, me, customDeadlines, exceptionsEnabled ->
        current.copy(
            canEdit = me != null && MemberPermissions.canManageClub(me.role),
            customDeadlinesEnabled = customDeadlines,
            exceptionsEnabled = exceptionsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = ScheduleState()
    )

    private var exceptionsLoaded = false

    init {
        load()
        // Special weeks are only requested when their feature is on (APP-RN-17)
        viewModelScope.launch {
            featureFlags.observe(FeatureFlag.SCHEDULE_EXCEPTIONS).collect { enabled ->
                if (enabled && !exceptionsLoaded) loadExceptions()
            }
        }
        // AC-015-06: another manager changed the schedule or its special weeks
        viewModelScope.launch {
            clubLiveUpdates.observe(clubId).collect { change ->
                if (change.scope != ClubDataScope.SCHEDULE) return@collect
                load()
                if (exceptionsLoaded) loadExceptions()
            }
        }
    }

    fun onAction(action: ScheduleAction) {
        when (action) {
            ScheduleAction.OnRefresh -> {
                load(userInitiated = true)
                if (state.value.exceptionsEnabled) loadExceptions()
            }
            is ScheduleAction.OnDaySelected -> updateForm { it.copy(dayOfWeek = action.day) }
            is ScheduleAction.OnFormatSelected -> updateForm { it.copy(format = action.format) }
            is ScheduleAction.OnTimePicked -> onTimePicked(action.target, action.time)
            is ScheduleAction.OnCloseDaysChanged -> updateForm { it.copy(closeDaysBefore = action.days.coerceIn(0, 6)) }
            is ScheduleAction.OnDrawDaysChanged -> updateForm { it.copy(drawDaysBefore = action.days.coerceIn(0, 6)) }
            is ScheduleAction.OnActiveChanged -> onActiveChanged(action.active)
            ScheduleAction.OnConfirmDeactivate -> {
                updateForm { it.copy(isActive = false) }
                _state.update { it.copy(dialog = null) }
            }
            is ScheduleAction.OnOpenTimePicker -> _state.update { it.copy(dialog = ScheduleDialog.TimePicker(action.target)) }
            ScheduleAction.OnOpenExceptionForm -> _state.update { it.copy(exceptionForm = ExceptionForm()) }
            ScheduleAction.OnCloseExceptionForm -> _state.update { it.copy(exceptionForm = null) }
            is ScheduleAction.OnOpenDatePicker -> _state.update { it.copy(dialog = ScheduleDialog.DatePicker(action.target)) }
            is ScheduleAction.OnDatePicked -> onDatePicked(action.target, action.date)
            is ScheduleAction.OnExceptionTypeSelected -> updateExceptionForm { it.copy(type = action.type) }
            ScheduleAction.OnSaveException -> saveException()
            is ScheduleAction.OnDeleteExceptionClick -> _state.update { it.copy(dialog = ScheduleDialog.ConfirmDeleteException(action.exception)) }
            ScheduleAction.OnConfirmDeleteException -> deleteException()
            ScheduleAction.OnDismissDialog -> _state.update { it.copy(dialog = null) }
            ScheduleAction.OnSave -> save()
        }
    }

    /**
     * Spec 017: only a pull shows the refresh indicator; the first load uses [ScheduleState.isLoading] and automatic
     * reloads (live updates) are silent. The newest request wins, and unsaved edits are never overwritten.
     */
    private fun load(userInitiated: Boolean = false) {
        loadJob?.cancel()
        if (userInitiated) _state.update { it.copy(isRefreshing = true) }
        loadJob = viewModelScope.launch {
            try {
                getScheduleUseCase(clubId)
                    .onSuccess { schedule ->
                        val previous = _state.value
                        val hasUnsavedEdits = previous.schedule != null && previous.form != ScheduleDraft.from(previous.schedule)
                        _state.update {
                            it.copy(
                                schedule = schedule,
                                form = if (hasUnsavedEdits) it.form else schedule?.let(ScheduleDraft::from) ?: it.form ?: defaultDraft(),
                                isOffline = false
                            )
                        }
                        if (!hasUnsavedEdits) schedule?.let {
                            _state.value.timeZone.setTextAndPlaceCursorAtEnd(it.timeZone)
                            _state.value.duration.setTextAndPlaceCursorAtEnd(it.durationMinutes.toString())
                        }
                    }
                    .onFailure { error ->
                        if (error is ClubError.Remote && error.error.status == DataError.Remote.NO_INTERNET) _state.update { it.copy(isOffline = true) }
                        // Silent reloads do not repeat the same error message
                        if (userInitiated || _state.value.isLoading) eventChannel.send(ScheduleEvent.ShowMessage(error.toUiText()))
                    }
            } finally {
                _state.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    private fun loadExceptions() {
        viewModelScope.launch {
            getScheduleExceptionsUseCase(clubId)
                .onSuccess { exceptions ->
                    exceptionsLoaded = true
                    _state.update { it.copy(exceptions = exceptions) }
                }
                .onFailure { error -> eventChannel.send(ScheduleEvent.ShowMessage(error.toUiText())) }
        }
    }

    private fun save() {
        val current = state.value
        val form = current.form ?: return
        val timeZone = current.timeZone.text.toString().trim()
        if (runCatching { TimeZone.of(timeZone) }.isFailure) {
            _state.update { it.copy(formError = UiText.Resource(Res.string.schedule_error_time_zone)) }
            return
        }
        val duration = current.duration.text.toString().trim().toIntOrNull() ?: 0
        val draft = form.copy(timeZone = timeZone, durationMinutes = duration)
        val exists = current.schedule != null
        launchWorking {
            saveScheduleUseCase(clubId = clubId, draft = draft, exists = exists, customDeadlines = current.customDeadlinesEnabled)
                .onSuccess { schedule ->
                    _state.update { it.copy(schedule = schedule, form = ScheduleDraft.from(schedule), formError = null) }
                    val message = if (exists) Res.string.schedule_saved else Res.string.schedule_created
                    eventChannel.send(ScheduleEvent.ShowMessage(UiText.Resource(message)))
                }
                .onFailure { error ->
                    when (error) {
                        is SaveScheduleError.Invalid -> _state.update { it.copy(formError = error.reason.toUiText()) }
                        is SaveScheduleError.Remote -> {
                            eventChannel.send(ScheduleEvent.ShowMessage(error.error.toUiText()))
                            // 409: the schedule was created meanwhile; reload and continue in edit mode (AC-004-05)
                            if (error.error is ClubError.Remote && (error.error as ClubError.Remote).error.status == DataError.Remote.CONFLICT) load()
                        }
                    }
                }
        }
    }

    private fun saveException() {
        val form = state.value.exceptionForm ?: return
        val date = form.date ?: return
        val schedule = state.value.schedule ?: return
        val newScheduledAt = if (form.type == ScheduleExceptionType.RESCHEDULED) {
            val newDate = form.newDate ?: return
            val newTime = form.newTime ?: return
            LocalDateTime(newDate, newTime).toInstant(TimeZone.of(schedule.timeZone))
        } else {
            null
        }
        launchWorking {
            addScheduleExceptionUseCase(clubId, date, form.type, newScheduledAt, form.reason.text.toString())
                .onSuccess { created ->
                    _state.update { state -> state.copy(exceptions = (state.exceptions + created).sortedBy { it.date }, exceptionForm = null) }
                    eventChannel.send(ScheduleEvent.ShowMessage(UiText.Resource(Res.string.exceptions_added)))
                    eventChannel.send(ScheduleEvent.ScheduleChanged)
                }
                .onFailure { error ->
                    val message = if (error is ClubError.Remote && error.error.status == DataError.Remote.CONFLICT) {
                        UiText.Resource(Res.string.exceptions_conflict)
                    } else {
                        error.toUiText()
                    }
                    eventChannel.send(ScheduleEvent.ShowMessage(message))
                }
        }
    }

    private fun deleteException() {
        val exception = (state.value.dialog as? ScheduleDialog.ConfirmDeleteException)?.exception ?: return
        _state.update { it.copy(dialog = null) }
        launchWorking {
            deleteScheduleExceptionUseCase(clubId, exception.id)
                .onSuccess {
                    _state.update { state -> state.copy(exceptions = state.exceptions.filterNot { it.id == exception.id }) }
                    eventChannel.send(ScheduleEvent.ScheduleChanged)
                }
                .onFailure { error -> eventChannel.send(ScheduleEvent.ShowMessage(error.toUiText())) }
        }
    }

    private fun onTimePicked(target: TimeTarget, time: LocalTime) {
        _state.update { it.copy(dialog = null) }
        when (target) {
            TimeTarget.MATCH -> updateForm { it.copy(matchTime = time) }
            TimeTarget.CLOSE -> updateForm { it.copy(closeTime = time) }
            TimeTarget.DRAW -> updateForm { it.copy(drawTime = time) }
            TimeTarget.RESCHEDULE -> updateExceptionForm { it.copy(newTime = time) }
        }
    }

    private fun onDatePicked(target: DateTarget, date: LocalDate) {
        _state.update { it.copy(dialog = null) }
        when (target) {
            DateTarget.EXCEPTION -> updateExceptionForm { it.copy(date = date) }
            DateTarget.RESCHEDULE -> updateExceptionForm { it.copy(newDate = date) }
        }
    }

    private fun onActiveChanged(active: Boolean) {
        if (!active && state.value.schedule?.isActive == true) {
            _state.update { it.copy(dialog = ScheduleDialog.ConfirmDeactivate) }
        } else {
            updateForm { it.copy(isActive = active) }
        }
    }

    private fun updateForm(transform: (ScheduleDraft) -> ScheduleDraft) =
        _state.update { it.copy(form = transform(it.form ?: defaultDraft()), formError = null) }

    private fun updateExceptionForm(transform: (ExceptionForm) -> ExceptionForm) =
        _state.update { state -> state.copy(exceptionForm = state.exceptionForm?.let(transform)) }

    private fun defaultDraft() = ScheduleDraft(
        dayOfWeek = DayOfWeek.THURSDAY,
        matchTime = LocalTime(20, 0),
        timeZone = _state.value.timeZone.text.toString(),
        format = MatchFormat.FIVE_A_SIDE,
        durationMinutes = ScheduleDraft.DEFAULT_DURATION_MINUTES,
        closeDaysBefore = 1,
        closeTime = ScheduleDraft.DEFAULT_CLOSE_TIME,
        drawDaysBefore = 1,
        drawTime = ScheduleDraft.DEFAULT_CLOSE_TIME,
        isActive = true
    )

    private fun launchWorking(block: suspend () -> Unit) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }
            try {
                block()
            } finally {
                _state.update { it.copy(isWorking = false) }
            }
        }
    }
}

enum class TimeTarget { MATCH, CLOSE, DRAW, RESCHEDULE }
enum class DateTarget { EXCEPTION, RESCHEDULE }

sealed interface ScheduleDialog {
    data class TimePicker(val target: TimeTarget) : ScheduleDialog
    data class DatePicker(val target: DateTarget) : ScheduleDialog
    data object ConfirmDeactivate : ScheduleDialog
    data class ConfirmDeleteException(val exception: ScheduleExceptionModel) : ScheduleDialog
}

data class ExceptionForm(
    val date: LocalDate? = null,
    val type: ScheduleExceptionType = ScheduleExceptionType.CANCELLED,
    val newDate: LocalDate? = null,
    val newTime: LocalTime? = null,
    val reason: TextFieldState = TextFieldState()
) {
    val canSave: Boolean get() = date != null && (type == ScheduleExceptionType.CANCELLED || (newDate != null && newTime != null))
}

data class ScheduleState(
    /** First load only (nothing to show yet). */
    val isLoading: Boolean = true,
    /** Pull-to-refresh started by the user. */
    val isRefreshing: Boolean = false,
    val isWorking: Boolean = false,
    val isOffline: Boolean = false,
    val canEdit: Boolean = false,
    val customDeadlinesEnabled: Boolean = false,
    val exceptionsEnabled: Boolean = false,
    val schedule: ClubScheduleModel? = null,
    val form: ScheduleDraft? = null,
    val timeZone: TextFieldState = TextFieldState(initialText = TimeZone.currentSystemDefault().id),
    val duration: TextFieldState = TextFieldState(initialText = ScheduleDraft.DEFAULT_DURATION_MINUTES.toString()),
    val formError: UiText? = null,
    val exceptions: List<ScheduleExceptionModel> = emptyList(),
    val exceptionForm: ExceptionForm? = null,
    val dialog: ScheduleDialog? = null
)

sealed interface ScheduleAction {
    data object OnRefresh : ScheduleAction
    data class OnDaySelected(val day: DayOfWeek) : ScheduleAction
    data class OnFormatSelected(val format: MatchFormat) : ScheduleAction
    data class OnOpenTimePicker(val target: TimeTarget) : ScheduleAction
    data class OnTimePicked(val target: TimeTarget, val time: LocalTime) : ScheduleAction
    data class OnCloseDaysChanged(val days: Int) : ScheduleAction
    data class OnDrawDaysChanged(val days: Int) : ScheduleAction
    data class OnActiveChanged(val active: Boolean) : ScheduleAction
    data object OnConfirmDeactivate : ScheduleAction
    data object OnSave : ScheduleAction
    data object OnOpenExceptionForm : ScheduleAction
    data object OnCloseExceptionForm : ScheduleAction
    data class OnOpenDatePicker(val target: DateTarget) : ScheduleAction
    data class OnDatePicked(val target: DateTarget, val date: LocalDate) : ScheduleAction
    data class OnExceptionTypeSelected(val type: ScheduleExceptionType) : ScheduleAction
    data object OnSaveException : ScheduleAction
    data class OnDeleteExceptionClick(val exception: ScheduleExceptionModel) : ScheduleAction
    data object OnConfirmDeleteException : ScheduleAction
    data object OnDismissDialog : ScheduleAction
}

sealed interface ScheduleEvent {
    data class ShowMessage(val message: UiText) : ScheduleEvent
    /** A special week changed the planned matches: the announcement should refresh. */
    data object ScheduleChanged : ScheduleEvent
}
