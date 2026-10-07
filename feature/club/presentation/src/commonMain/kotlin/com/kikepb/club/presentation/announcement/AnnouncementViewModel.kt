package com.kikepb.club.presentation.announcement

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.AnnouncementEntry
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.EntryStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.policy.AnnouncementWindowPolicy
import com.kikepb.club.domain.policy.MemberPermissions
import com.kikepb.club.domain.policy.WindowState
import com.kikepb.club.domain.usecase.AddGuestToAnnouncementUseCase
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.usecase.EnrollUseCase
import com.kikepb.club.domain.usecase.GetAbsencesUseCase
import com.kikepb.club.domain.usecase.coversMe
import com.kikepb.club.domain.usecase.GetAnnouncementHistoryUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetCurrentAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.RemoveGuestFromAnnouncementUseCase
import com.kikepb.club.domain.usecase.WithdrawUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.notification.InAppPushCenter
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.announcement_already_enrolled
import squadfy_app.feature.club.presentation.generated.resources.announcement_enrolled_message
import squadfy_app.feature.club.presentation.generated.resources.announcement_not_open_error
import squadfy_app.feature.club.presentation.generated.resources.announcement_waitlisted_message
import squadfy_app.feature.club.presentation.generated.resources.guests_added
import squadfy_app.feature.club.presentation.generated.resources.guests_max
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Current announcement of a club: window, enrollment, waitlist and guests (spec 005, ADR-0006). */
class AnnouncementViewModel(
    getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val featureFlags: FeatureFlags,
    private val getAbsencesUseCase: GetAbsencesUseCase,
    private val getCurrentAnnouncementUseCase: GetCurrentAnnouncementUseCase,
    private val getAnnouncementHistoryUseCase: GetAnnouncementHistoryUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val enrollUseCase: EnrollUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val addGuestUseCase: AddGuestToAnnouncementUseCase,
    private val removeGuestUseCase: RemoveGuestFromAnnouncementUseCase,
    private val clock: Clock,
    private val inAppPushCenter: InAppPushCenter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    // Buffered: one-off events must never suspend the action that emits them (e.g. refresh after an error)
    private val eventChannel = Channel<AnnouncementEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AnnouncementState(now = clock.now()))

    /** Wall clock that only ticks while the screen collects the state (AC-005-03). */
    private val ticker = flow {
        while (true) {
            emit(clock.now())
            delay(TICK)
        }
    }

    val state = combine(
        _state,
        getClubMembersUseCase(clubId),
        observeMyMembershipUseCase(clubId),
        featureFlags.observe(FeatureFlag.MATCH_GUESTS),
        ticker
    ) { current, members, me, guestsEnabled, now ->
        current.copy(members = members.associateBy { it.id }, me = me, guestsEnabled = guestsEnabled, now = maxOf(now, current.now))
    }
        .onEach { state -> refreshIfWindowChanged(state) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = _state.value
        )

    private var lastWindowState: WindowState? = null

    /** Once a window boundary is crossed (opens, closes) the backend state changed too: refresh. */
    private fun refreshIfWindowChanged(state: AnnouncementState) {
        val window = state.windowState
        if (lastWindowState != null && window != null && window != lastWindowState && !state.isRefreshing) refresh()
        if (window != null) lastWindowState = window
    }

    private var isVisible = false

    init {
        refresh()
        loadTimeZone()
        // AC-009-04: a push of this club while the tab is visible refreshes it and shows its text
        viewModelScope.launch {
            inAppPushCenter.messages.collect { message ->
                if (message.clubId != clubId) return@collect
                refresh()
                (message.body ?: message.title)?.let { eventChannel.send(AnnouncementEvent.ShowMessage(UiText.DynamicString(it))) }
            }
        }
    }

    fun onAction(action: AnnouncementAction) {
        when (action) {
            AnnouncementAction.OnRefresh, AnnouncementAction.OnResume -> refresh()
            is AnnouncementAction.OnVisibilityChanged -> onVisibilityChanged(action.visible)
            AnnouncementAction.OnEnrollClick -> enroll()
            AnnouncementAction.OnWithdrawClick -> _state.update { it.copy(dialog = AnnouncementDialog.ConfirmWithdraw) }
            AnnouncementAction.OnConfirmWithdraw -> withdraw()
            AnnouncementAction.OnAddGuestClick -> _state.update { it.copy(dialog = AnnouncementDialog.AddGuest()) }
            is AnnouncementAction.OnGuestPositionSelected -> _state.update { state ->
                val dialog = state.dialog as? AnnouncementDialog.AddGuest ?: return@update state
                state.copy(dialog = dialog.copy(position = action.position))
            }
            AnnouncementAction.OnConfirmAddGuest -> addGuest()
            is AnnouncementAction.OnRemoveGuest -> removeGuest(action.entry)
            AnnouncementAction.OnDismissDialog -> _state.update { it.copy(dialog = null) }
        }
    }

    private fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            getCurrentAnnouncementUseCase(clubId)
                .onSuccess { current ->
                    _state.update { it.copy(current = current, hasLoaded = true, isStale = false, lastUpdatedAt = clock.now()) }
                }
                .onFailure { error -> onLoadError(error) }
            // AC-005-15: my absences, only with MEMBER_ABSENCES on (APP-RN-17)
            if (featureFlags.isEnabled(FeatureFlag.MEMBER_ABSENCES)) {
                val today = clock.now().toLocalDateTime(_state.value.zone).date
                getAbsencesUseCase(clubId, from = today).onSuccess { absences -> _state.update { it.copy(absences = absences) } }
            }
            getAnnouncementHistoryUseCase(clubId).onSuccess { history ->
                // The current one is shown on top, the rest are past announcements (AC-005-10)
                _state.update { state -> state.copy(history = history.filterNot { it.id == state.current?.announcement?.id }) }
            }
            _state.update { it.copy(isRefreshing = false, now = clock.now()) }
        }
    }

    private fun onVisibilityChanged(visible: Boolean) {
        if (visible == isVisible) return
        isVisible = visible
        if (visible) inAppPushCenter.onClubVisible(clubId) else inAppPushCenter.onClubHidden(clubId)
    }

    override fun onCleared() {
        onVisibilityChanged(false)
        super.onCleared()
    }

    private fun loadTimeZone() {
        viewModelScope.launch {
            getScheduleUseCase(clubId).onSuccess { schedule ->
                schedule?.let { _state.update { state -> state.copy(timeZoneId = schedule.timeZone) } }
            }
        }
    }

    private fun enroll() {
        val announcement = _state.value.current?.announcement ?: return
        launchAction(AnnouncementOperation.ENROLLMENT) {
            enrollUseCase(announcement.id)
                .onSuccess { updated ->
                    val current = applyUpdate(updated)
                    val message = if (current?.myStatus == MyEnrollmentStatus.WAITLISTED) {
                        UiText.Resource(Res.string.announcement_waitlisted_message, arrayOf(current.myWaitlistPosition ?: 0))
                    } else {
                        UiText.Resource(Res.string.announcement_enrolled_message)
                    }
                    eventChannel.send(AnnouncementEvent.ShowMessage(message))
                }
                .onFailure { error -> onActionError(error) }
        }
    }

    private fun withdraw() {
        val announcement = _state.value.current?.announcement ?: return
        _state.update { it.copy(dialog = null) }
        launchAction(AnnouncementOperation.ENROLLMENT) {
            withdrawUseCase(announcement.id)
                .onSuccess { updated -> applyUpdate(updated) }
                .onFailure { error -> onActionError(error) }
        }
    }

    private fun addGuest() {
        val dialog = _state.value.dialog as? AnnouncementDialog.AddGuest ?: return
        val announcement = _state.value.current?.announcement ?: return
        val name = dialog.name.text.toString().trim()
        if (name.isBlank() || name.length > AddGuestToAnnouncementUseCase.MAX_NAME_LENGTH) return
        _state.update { it.copy(dialog = null) }
        launchAction(AnnouncementOperation.GUESTS) {
            addGuestUseCase(announcement.id, name, dialog.position)
                .onSuccess { updated ->
                    applyUpdate(updated)
                    eventChannel.send(AnnouncementEvent.ShowMessage(UiText.Resource(Res.string.guests_added)))
                }
                .onFailure { error ->
                    if (error is ClubError.Remote && error.error.status == DataError.Remote.CONFLICT) {
                        eventChannel.send(AnnouncementEvent.ShowMessage(UiText.Resource(Res.string.guests_max)))
                    } else {
                        onActionError(error)
                    }
                }
        }
    }

    private fun removeGuest(entry: AnnouncementEntry) {
        val announcement = _state.value.current?.announcement ?: return
        launchAction(AnnouncementOperation.GUESTS) {
            removeGuestUseCase(announcement.id, entry.id)
                .onSuccess { updated -> applyUpdate(updated) }
                .onFailure { error -> onActionError(error) }
        }
    }

    /** AC-005-05: the screen uses the announcement returned by the action without waiting for a refresh. */
    private fun applyUpdate(updated: MatchAnnouncementModel): CurrentAnnouncementModel? {
        var result: CurrentAnnouncementModel? = null
        // `me` lives in the combined state (from Room), not in the private mutable state
        val myMemberId = state.value.me?.id
        _state.update { state ->
            val current = state.current ?: return@update state
            val merged = AnnouncementWindowPolicy.withMyStatus(current, updated, myMemberId)
            result = merged
            state.copy(current = merged, lastUpdatedAt = clock.now(), now = clock.now())
        }
        return result
    }

    private suspend fun onLoadError(error: ClubError) {
        if (error == ClubError.NotClubMember) {
            eventChannel.send(AnnouncementEvent.NotMemberAnymore)
            return
        }
        val offline = error is ClubError.Remote && error.error.status == DataError.Remote.NO_INTERNET
        _state.update { it.copy(isStale = offline && it.current != null) }
        eventChannel.send(AnnouncementEvent.ShowMessage(error.toUiText()))
    }

    /** AC-005-07: specific texts for the business errors, then refresh to show the real state. */
    private suspend fun onActionError(error: ClubError) {
        val status = (error as? ClubError.Remote)?.error?.status
        val message = when {
            error == ClubError.NotClubMember -> {
                eventChannel.send(AnnouncementEvent.NotMemberAnymore)
                return
            }
            status == DataError.Remote.BAD_REQUEST -> UiText.Resource(Res.string.announcement_not_open_error)
            status == DataError.Remote.CONFLICT -> UiText.Resource(Res.string.announcement_already_enrolled)
            else -> error.toUiText()
        }
        eventChannel.send(AnnouncementEvent.ShowMessage(message))
        refresh()
    }

    private fun launchAction(operation: AnnouncementOperation, block: suspend () -> Unit) {
        if (operation in _state.value.acting) return
        viewModelScope.launch {
            _state.update { it.copy(acting = it.acting + operation) }
            try {
                block()
            } finally {
                _state.update { it.copy(acting = it.acting - operation) }
            }
        }
    }

    private companion object {
        val TICK = 1.minutes
    }
}

enum class AnnouncementOperation { ENROLLMENT, GUESTS }

sealed interface AnnouncementDialog {
    data object ConfirmWithdraw : AnnouncementDialog
    data class AddGuest(val name: TextFieldState = TextFieldState(), val position: PlayerPosition? = null) : AnnouncementDialog
}

data class AnnouncementState(
    val now: Instant,
    val hasLoaded: Boolean = false,
    val isRefreshing: Boolean = false,
    val isStale: Boolean = false,
    val lastUpdatedAt: Instant? = null,
    val current: CurrentAnnouncementModel? = null,
    val history: List<MatchAnnouncementModel> = emptyList(),
    val timeZoneId: String = TimeZone.currentSystemDefault().id,
    val members: Map<String, ClubMemberModel> = emptyMap(),
    val me: ClubMemberModel? = null,
    val guestsEnabled: Boolean = false,
    val absences: List<MemberAbsenceModel> = emptyList(),
    val acting: Set<AnnouncementOperation> = emptySet(),
    val dialog: AnnouncementDialog? = null
) {
    val windowState: WindowState? get() = current?.let { AnnouncementWindowPolicy.state(it.announcement, now) }
    val timeToNextChange: Duration? get() = current?.let { AnnouncementWindowPolicy.timeToNextChange(it.announcement, now) }
    val isManager: Boolean get() = me?.role?.let(MemberPermissions::canManageClub) == true

    val zone: TimeZone get() = runCatching { TimeZone.of(timeZoneId) }.getOrDefault(TimeZone.currentSystemDefault())

    /** AC-005-15: one of my absences covers the match day; signing up stays possible (BE-008 RN-C4). */
    val hasAbsenceOnMatchDay: Boolean
        get() = current?.let { absences.coversMe(me?.id, it.matchScheduledAt.toLocalDateTime(zone).date) } == true

    /** AC-005-13: any member while the window is open, at most 2 guests each; never offline. */
    val canAddGuest: Boolean
        get() {
            val announcement = current?.announcement ?: return false
            return guestsEnabled && me != null && !isStale && windowState == WindowState.OPEN &&
                AnnouncementWindowPolicy.myGuestsCount(announcement, me.id) < AnnouncementWindowPolicy.MAX_GUESTS_PER_MEMBER
        }

    fun canRemoveGuest(entry: AnnouncementEntry): Boolean =
        guestsEnabled && entry.isGuest && windowState == WindowState.OPEN && !isStale &&
            (isManager || (me != null && entry.invitedByMemberId == me.id))

    val confirmed: List<AnnouncementEntry> get() = current?.announcement?.entries.orEmpty().filter { it.status == EntryStatus.CONFIRMED }
    val waitlist: List<AnnouncementEntry> get() = current?.announcement?.waitlist.orEmpty()
}

sealed interface AnnouncementAction {
    data object OnRefresh : AnnouncementAction
    data object OnResume : AnnouncementAction
    data class OnVisibilityChanged(val visible: Boolean) : AnnouncementAction
    data object OnEnrollClick : AnnouncementAction
    data object OnWithdrawClick : AnnouncementAction
    data object OnConfirmWithdraw : AnnouncementAction
    data object OnAddGuestClick : AnnouncementAction
    data class OnGuestPositionSelected(val position: PlayerPosition?) : AnnouncementAction
    data object OnConfirmAddGuest : AnnouncementAction
    data class OnRemoveGuest(val entry: AnnouncementEntry) : AnnouncementAction
    data object OnDismissDialog : AnnouncementAction
}

sealed interface AnnouncementEvent {
    data class ShowMessage(val message: UiText) : AnnouncementEvent
    /** 403 NOT_CLUB_MEMBER: the user left or was removed (AC-005-07). */
    data object NotMemberAnymore : AnnouncementEvent
}
