package com.kikepb.club.presentation.extramatch

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.usecase.CreateExtraMatchUseCase
import com.kikepb.club.domain.usecase.ExtraMatchError
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.extra_match_created
import squadfy_app.feature.club.presentation.generated.resources.extra_match_duration_invalid
import squadfy_app.feature.club.presentation.generated.resources.extra_match_future
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/** "Extra match" for managers (AC-007-08); with `DEV_TEST_MATCH` it offers a quick test match in 15 minutes. */
class ExtraMatchViewModel(
    featureFlags: FeatureFlags,
    private val createExtraMatchUseCase: CreateExtraMatchUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val eventChannel = Channel<ExtraMatchEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ExtraMatchState())

    val state = combine(_state, featureFlags.observe(FeatureFlag.DEV_TEST_MATCH)) { current, testMatch ->
        current.copy(quickTestEnabled = testMatch)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = _state.value
    )

    fun onAction(action: ExtraMatchAction) {
        when (action) {
            ExtraMatchAction.OnOpen -> open()
            ExtraMatchAction.OnDismiss -> _state.update { ExtraMatchState(timeZoneId = it.timeZoneId) }
            ExtraMatchAction.OnPickDate -> _state.update { it.copy(picker = ExtraMatchPicker.DATE) }
            ExtraMatchAction.OnPickTime -> _state.update { it.copy(picker = ExtraMatchPicker.TIME) }
            ExtraMatchAction.OnDismissPicker -> _state.update { it.copy(picker = null) }
            is ExtraMatchAction.OnDatePicked -> _state.update { it.copy(date = action.date, picker = null, error = null) }
            is ExtraMatchAction.OnTimePicked -> _state.update { it.copy(time = action.time, picker = null, error = null) }
            ExtraMatchAction.OnQuickTest -> {
                val soon = (clock.now() + QUICK_TEST_DELAY).toLocalDateTime(_state.value.zone)
                _state.update { it.copy(date = soon.date, time = LocalTime(soon.hour, soon.minute), error = null) }
            }
            ExtraMatchAction.OnConfirm -> create()
        }
    }

    private fun open() {
        _state.update { it.copy(isOpen = true) }
        viewModelScope.launch {
            getScheduleUseCase(clubId).onSuccess { schedule ->
                schedule?.let { _state.update { state -> state.copy(timeZoneId = it.timeZone) } }
            }
        }
    }

    private fun create() {
        val current = _state.value
        val date = current.date ?: return
        val time = current.time ?: return
        val scheduledAt = LocalDateTime(date, time).toInstant(current.zone)
        val durationText = current.duration.text.toString().trim()
        val duration = durationText.toIntOrNull()
        val error = if (durationText.isNotEmpty() && duration == null) {
            ExtraMatchError.INVALID_DURATION
        } else {
            CreateExtraMatchUseCase.validate(scheduledAt, duration, clock.now())
        }
        if (error != null) {
            _state.update { it.copy(error = error.toUiText()) }
            return
        }
        if (current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            // The format defaults to the club schedule's one (CreateMatchRequest)
            createExtraMatchUseCase(clubId, scheduledAt, format = null, durationMinutes = duration)
                .onSuccess { match ->
                    _state.update { ExtraMatchState(timeZoneId = it.timeZoneId) }
                    eventChannel.send(ExtraMatchEvent.Created(match.id, UiText.Resource(Res.string.extra_match_created)))
                }
                .onFailure { failure -> _state.update { it.copy(isSaving = false, error = failure.toUiText()) } }
        }
    }

    private fun ExtraMatchError.toUiText(): UiText = when (this) {
        ExtraMatchError.NOT_IN_FUTURE -> UiText.Resource(Res.string.extra_match_future)
        ExtraMatchError.INVALID_DURATION -> UiText.Resource(Res.string.extra_match_duration_invalid)
    }

    private companion object {
        val QUICK_TEST_DELAY = 15.minutes
    }
}

enum class ExtraMatchPicker { DATE, TIME }

data class ExtraMatchState(
    val isOpen: Boolean = false,
    val isSaving: Boolean = false,
    val timeZoneId: String = TimeZone.currentSystemDefault().id,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val duration: TextFieldState = TextFieldState(),
    val picker: ExtraMatchPicker? = null,
    val error: UiText? = null,
    val quickTestEnabled: Boolean = false
) {
    val zone: TimeZone get() = runCatching { TimeZone.of(timeZoneId) }.getOrDefault(TimeZone.currentSystemDefault())
    val canConfirm: Boolean get() = date != null && time != null && !isSaving
}

sealed interface ExtraMatchAction {
    data object OnOpen : ExtraMatchAction
    data object OnDismiss : ExtraMatchAction
    data object OnPickDate : ExtraMatchAction
    data object OnPickTime : ExtraMatchAction
    data object OnDismissPicker : ExtraMatchAction
    data class OnDatePicked(val date: LocalDate) : ExtraMatchAction
    data class OnTimePicked(val time: LocalTime) : ExtraMatchAction
    data object OnQuickTest : ExtraMatchAction
    data object OnConfirm : ExtraMatchAction
}

sealed interface ExtraMatchEvent {
    data class Created(val matchId: String, val message: UiText) : ExtraMatchEvent
}
