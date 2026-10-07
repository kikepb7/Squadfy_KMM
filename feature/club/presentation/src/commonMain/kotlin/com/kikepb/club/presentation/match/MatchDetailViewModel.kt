package com.kikepb.club.presentation.match

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MatchEventModel
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchGuestModel
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.Team
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.policy.MatchCyclePolicy
import com.kikepb.club.domain.policy.MatchTeamsPolicy
import com.kikepb.club.domain.policy.MemberPermissions
import com.kikepb.club.domain.policy.TeamsPending
import com.kikepb.club.domain.policy.TeamsSplit
import com.kikepb.club.domain.usecase.AddMatchEventUseCase
import com.kikepb.club.domain.usecase.CancelMatchUseCase
import com.kikepb.club.domain.usecase.ClearManualScoreUseCase
import com.kikepb.club.domain.usecase.CompleteMatchUseCase
import com.kikepb.club.domain.usecase.DeleteMatchEventUseCase
import com.kikepb.club.domain.usecase.GenerateTeamsUseCase
import com.kikepb.club.domain.usecase.GetClubMatchesUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetMatchAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetMatchUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.GetTeamBalanceUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.ReopenMatchUseCase
import com.kikepb.club.domain.usecase.SetManualScoreUseCase
import com.kikepb.club.domain.usecase.SetPlayerMinutesUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
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
import kotlinx.datetime.TimeZone
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.match_cancelled_message
import squadfy_app.feature.club.presentation.generated.resources.match_completed_message
import squadfy_app.feature.club.presentation.generated.resources.match_event_deleted
import squadfy_app.feature.club.presentation.generated.resources.match_reopened_message
import squadfy_app.feature.club.presentation.generated.resources.match_teams_rejected
import squadfy_app.feature.club.presentation.generated.resources.match_teams_saved
import kotlin.time.Clock
import kotlin.time.Instant

/** Match detail: teams and balance (spec 006) plus the report, lifecycle and manual score (spec 007). Network-first (ADR-0006). */
class MatchDetailViewModel(
    getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val getMatchUseCase: GetMatchUseCase,
    private val getMatchAnnouncementUseCase: GetMatchAnnouncementUseCase,
    private val getTeamBalanceUseCase: GetTeamBalanceUseCase,
    private val generateTeamsUseCase: GenerateTeamsUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val getClubMatchesUseCase: GetClubMatchesUseCase,
    private val addMatchEventUseCase: AddMatchEventUseCase,
    private val deleteMatchEventUseCase: DeleteMatchEventUseCase,
    private val setPlayerMinutesUseCase: SetPlayerMinutesUseCase,
    private val completeMatchUseCase: CompleteMatchUseCase,
    private val reopenMatchUseCase: ReopenMatchUseCase,
    private val cancelMatchUseCase: CancelMatchUseCase,
    private val setManualScoreUseCase: SetManualScoreUseCase,
    private val clearManualScoreUseCase: ClearManualScoreUseCase,
    featureFlags: FeatureFlags,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")
    private val matchId = savedStateHandle.get<String>("matchId")
        ?: throw IllegalStateException("matchId is required")

    private val eventChannel = Channel<MatchDetailEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MatchDetailState())

    val state = combine(
        _state,
        getClubMembersUseCase(clubId),
        observeMyMembershipUseCase(clubId),
        featureFlags.observe(FeatureFlag.MANUAL_SCORE)
    ) { current, members, me, manualScore ->
        current.copy(
            members = members.associateBy { it.id },
            myMemberId = me?.id,
            isManager = me != null && MemberPermissions.canManageClub(me.role),
            manualScoreEnabled = manualScore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = _state.value
    )

    private var balanceRequested = false

    init {
        refresh()
        loadTimeZone()
        // The balance is managers-only: it is requested once the role is known (APP-RN-09)
        viewModelScope.launch {
            observeMyMembershipUseCase(clubId).collect { me ->
                if (me != null && MemberPermissions.canManageClub(me.role) && !balanceRequested) {
                    balanceRequested = true
                    loadBalance()
                    loadLatestCompleted()
                }
            }
        }
    }

    fun onAction(action: MatchDetailAction) {
        when (action) {
            MatchDetailAction.OnRefresh -> refresh()
            MatchDetailAction.OnRedrawClick -> _state.update { it.copy(dialog = MatchDetailDialog.ConfirmRedraw) }
            MatchDetailAction.OnConfirmRedraw -> redraw()
            MatchDetailAction.OnStartManualEdit -> _state.value.match?.let { match ->
                _state.update { it.copy(editing = MatchTeamsPolicy.initialSplit(match)) }
            }
            is MatchDetailAction.OnMovePlayer -> _state.update { state -> state.copy(editing = state.editing?.move(action.id)) }
            MatchDetailAction.OnCancelManualEdit -> _state.update { it.copy(editing = null) }
            MatchDetailAction.OnSaveManualEdit -> saveManual()
            MatchDetailAction.OnDismissDialog -> _state.update { it.copy(dialog = null) }
            MatchDetailAction.OnToggleReport -> _state.update { it.copy(reportMode = !it.reportMode, editing = null) }
            is MatchDetailAction.OnAddEventClick -> _state.update { it.copy(dialog = MatchDetailDialog.AddEvent(action.playerId, action.type)) }
            MatchDetailAction.OnConfirmAddEvent -> addEvent()
            is MatchDetailAction.OnDeleteEvent -> deleteEvent(action.event)
            is MatchDetailAction.OnUndoDeleteEvent -> launchWorking {
                addMatchEventUseCase(matchId, action.event.clubMemberId, action.event.type, action.event.minute).handleMatchResult()
            }
            is MatchDetailAction.OnEditMinutesClick -> _state.value.match?.let { match ->
                val minutes = TextFieldState(initialText = match.minutesOf(action.playerId).toString())
                _state.update { it.copy(dialog = MatchDetailDialog.EditMinutes(action.playerId, minutes)) }
            }
            MatchDetailAction.OnConfirmMinutes -> saveMinutes()
            MatchDetailAction.OnCompleteClick -> _state.update { it.copy(dialog = MatchDetailDialog.ConfirmComplete) }
            MatchDetailAction.OnConfirmComplete -> runCycle(Res.string.match_completed_message) { completeMatchUseCase(matchId) }
            MatchDetailAction.OnReopenClick -> _state.update { it.copy(dialog = MatchDetailDialog.ConfirmReopen) }
            MatchDetailAction.OnConfirmReopen -> runCycle(Res.string.match_reopened_message) { reopenMatchUseCase(matchId) }
            MatchDetailAction.OnCancelMatchClick -> _state.update { it.copy(dialog = MatchDetailDialog.ConfirmCancel) }
            MatchDetailAction.OnConfirmCancelMatch -> runCycle(Res.string.match_cancelled_message) { cancelMatchUseCase(matchId) }
            MatchDetailAction.OnManualScoreClick -> _state.value.match?.let { match ->
                _state.update { it.copy(dialog = MatchDetailDialog.ManualScore(match.teamAScore, match.teamBScore)) }
            }
            is MatchDetailAction.OnManualScoreChanged -> _state.update { state ->
                val dialog = state.dialog as? MatchDetailDialog.ManualScore ?: return@update state
                val range = SetManualScoreUseCase.SCORE_RANGE
                state.copy(
                    dialog = if (action.team == Team.A) dialog.copy(teamA = (dialog.teamA + action.delta).coerceIn(range))
                    else dialog.copy(teamB = (dialog.teamB + action.delta).coerceIn(range))
                )
            }
            MatchDetailAction.OnConfirmManualScore -> (_state.value.dialog as? MatchDetailDialog.ManualScore)?.let { dialog ->
                _state.update { it.copy(dialog = null) }
                launchWorking { setManualScoreUseCase(matchId, dialog.teamA, dialog.teamB).handleMatchResult() }
            }
            MatchDetailAction.OnClearManualScore -> launchWorking { clearManualScoreUseCase(matchId).handleMatchResult() }
        }
    }

    private fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            getMatchUseCase(matchId)
                .onSuccess { match -> _state.update { it.copy(match = match, isStale = false, now = clock.now()) } }
                .onFailure { error -> onLoadError(error) }
            getMatchAnnouncementUseCase(matchId).onSuccess { announcement ->
                _state.update { it.copy(announcement = announcement) }
            }
            if (balanceRequested) {
                loadBalance()
                loadLatestCompleted()
            }
            _state.update { it.copy(isRefreshing = false, hasLoaded = true) }
        }
    }

    private suspend fun onLoadError(error: ClubError) {
        if (error is ClubError.Remote && error.error.status == DataError.Remote.NO_INTERNET) _state.update { it.copy(isStale = true) }
        eventChannel.send(MatchDetailEvent.ShowMessage(error.toUiText()))
        if (error == ClubError.NotFound || error == ClubError.NotClubMember) eventChannel.send(MatchDetailEvent.Close)
    }

    private fun loadBalance() {
        viewModelScope.launch {
            // Success(null) = 409 without teams: the panel is hidden (AC-006-03)
            getTeamBalanceUseCase(matchId).onSuccess { balance -> _state.update { it.copy(balance = balance) } }
        }
    }

    private fun loadTimeZone() {
        viewModelScope.launch {
            getScheduleUseCase(clubId).onSuccess { schedule ->
                schedule?.let { _state.update { state -> state.copy(timeZoneId = it.timeZone, format = it.format) } }
            }
        }
    }

    private fun redraw() {
        _state.update { it.copy(dialog = null) }
        launchWorking { generateTeamsUseCase.auto(matchId).handleTeamsResult() }
    }

    private fun saveManual() {
        val split = _state.value.editing ?: return
        if (!split.isValid) return
        launchWorking {
            generateTeamsUseCase.manual(matchId, teamA = split.teamA.map { it.id }, teamB = split.teamB.map { it.id }).handleTeamsResult()
        }
    }

    private suspend fun Result<MatchModel, ClubError>.handleTeamsResult() {
        onSuccess { match ->
            _state.update { it.copy(match = match, editing = null) }
            eventChannel.send(MatchDetailEvent.ShowMessage(UiText.Resource(Res.string.match_teams_saved)))
            loadBalance()
        }
        onFailure { error ->
            // 400/409: the participants or the status changed meanwhile; show it and reload (AC-006-06)
            val status = (error as? ClubError.Remote)?.error?.status
            val message = if (status == DataError.Remote.CONFLICT || status == DataError.Remote.BAD_REQUEST) {
                UiText.Resource(Res.string.match_teams_rejected)
            } else {
                error.toUiText()
            }
            eventChannel.send(MatchDetailEvent.ShowMessage(message))
            if (status == DataError.Remote.CONFLICT || status == DataError.Remote.BAD_REQUEST) {
                _state.update { it.copy(editing = null) }
                refresh()
            }
        }
    }

    private fun loadLatestCompleted() {
        viewModelScope.launch {
            // Newest first: only the first completed match can be reopened (AC-007-06)
            getClubMatchesUseCase(clubId, MatchStatus.COMPLETED).onSuccess { matches ->
                _state.update { it.copy(latestCompletedId = matches.firstOrNull()?.id) }
            }
        }
    }

    private fun addEvent() {
        val dialog = _state.value.dialog as? MatchDetailDialog.AddEvent ?: return
        val minute = dialog.minute.text.toString().trim().toIntOrNull()
        _state.update { it.copy(dialog = null) }
        launchWorking { addMatchEventUseCase(matchId, dialog.playerId, dialog.type, minute).handleMatchResult() }
    }

    private fun deleteEvent(event: MatchEventModel) {
        launchWorking {
            deleteMatchEventUseCase(matchId, event.id)
                .onSuccess { match ->
                    _state.update { it.copy(match = match) }
                    // AC-007-02: the snackbar offers to record the same event again
                    eventChannel.send(MatchDetailEvent.ShowUndo(UiText.Resource(Res.string.match_event_deleted), event))
                }
                .onFailure { error -> onActionError(error) }
        }
    }

    private fun saveMinutes() {
        val dialog = _state.value.dialog as? MatchDetailDialog.EditMinutes ?: return
        val match = _state.value.match ?: return
        val minutes = dialog.minutes.text.toString().trim().toIntOrNull()
        if (!MatchCyclePolicy.isValidMinutes(match, minutes)) {
            _state.update { it.copy(dialog = dialog.copy(isInvalid = true)) }
            return
        }
        _state.update { it.copy(dialog = null) }
        launchWorking { setPlayerMinutesUseCase(matchId, dialog.playerId, minutes!!).handleMatchResult() }
    }

    private fun runCycle(message: org.jetbrains.compose.resources.StringResource, call: suspend () -> Result<MatchModel, ClubError>) {
        _state.update { it.copy(dialog = null) }
        launchWorking {
            call()
                .onSuccess { match ->
                    _state.update { it.copy(match = match, reportMode = false, editing = null) }
                    eventChannel.send(MatchDetailEvent.ShowMessage(UiText.Resource(message)))
                    loadLatestCompleted()
                    loadBalance()
                }
                .onFailure { error -> onActionError(error) }
        }
    }

    private suspend fun Result<MatchModel, ClubError>.handleMatchResult() {
        onSuccess { match -> _state.update { it.copy(match = match) } }
        onFailure { error -> onActionError(error) }
    }

    /** A 400/409 means the match changed meanwhile (e.g. completed by another manager): show it and reload. */
    private suspend fun onActionError(error: ClubError) {
        eventChannel.send(MatchDetailEvent.ShowMessage(error.toUiText()))
        val status = (error as? ClubError.Remote)?.error?.status
        if (status == DataError.Remote.CONFLICT || status == DataError.Remote.BAD_REQUEST) refresh()
    }

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

/** One row of a team: a member resolved from the club (APP-RN-06) or a guest. */
data class TeamPlayer(
    val id: String,
    val name: String?,
    val profilePictureUrl: String?,
    val shirtNumber: Int?,
    val position: PlayerPosition?,
    val isGuest: Boolean,
    val isMe: Boolean
)

sealed interface MatchDetailDialog {
    data object ConfirmRedraw : MatchDetailDialog
    data object ConfirmComplete : MatchDetailDialog
    data object ConfirmReopen : MatchDetailDialog
    data object ConfirmCancel : MatchDetailDialog
    data class AddEvent(val playerId: String, val type: MatchEventType, val minute: TextFieldState = TextFieldState()) : MatchDetailDialog
    data class EditMinutes(val playerId: String, val minutes: TextFieldState, val isInvalid: Boolean = false) : MatchDetailDialog
    data class ManualScore(val teamA: Int, val teamB: Int) : MatchDetailDialog
}

data class MatchDetailState(
    val hasLoaded: Boolean = false,
    val isRefreshing: Boolean = false,
    val isWorking: Boolean = false,
    val isStale: Boolean = false,
    val match: MatchModel? = null,
    val announcement: MatchAnnouncementModel? = null,
    val balance: TeamBalanceModel? = null,
    val members: Map<String, ClubMemberModel> = emptyMap(),
    val myMemberId: String? = null,
    val isManager: Boolean = false,
    val timeZoneId: String = TimeZone.currentSystemDefault().id,
    /** `MatchDto` has no format: the club schedule's one is shown. */
    val format: MatchFormat? = null,
    val now: Instant = Instant.DISTANT_PAST,
    val editing: TeamsSplit? = null,
    val dialog: MatchDetailDialog? = null,
    /** "Report" mode: per-player event and minute controls (AC-007-01). */
    val reportMode: Boolean = false,
    val latestCompletedId: String? = null,
    val manualScoreEnabled: Boolean = false
) {
    val canRecord: Boolean get() = match != null && MatchCyclePolicy.canRecord(match, isManager) && !isStale
    val canComplete: Boolean get() = match != null && MatchCyclePolicy.canComplete(match, isManager, now) && !isStale
    val canReopen: Boolean get() = match != null && MatchCyclePolicy.canReopen(match, isManager, latestCompletedId) && !isStale
    val canCancel: Boolean get() = match != null && MatchCyclePolicy.canCancel(match, isManager) && !isStale
    val canSetManualScore: Boolean get() = manualScoreEnabled && canRecord

    val canRectify: Boolean get() = match != null && MatchTeamsPolicy.canRectify(match, isManager) && !isStale

    val pending: TeamsPending? get() = match?.let { MatchTeamsPolicy.pending(it, announcement, now) }

    val myTeam: Team? get() = myMemberId?.let { id -> editing?.teamOf(id) ?: match?.teamOf(id) }

    val teamA: List<TeamPlayer> get() = editing?.let { split -> split.teamA.map { player(it.id, it.isGuest) } } ?: match.team(Team.A)
    val teamB: List<TeamPlayer> get() = editing?.let { split -> split.teamB.map { player(it.id, it.isGuest) } } ?: match.team(Team.B)

    private val guests: Map<String, MatchGuestModel>
        get() = match?.let { (it.enrolledGuests + it.teamAGuests + it.teamBGuests).associateBy(MatchGuestModel::guestId) }.orEmpty()

    private fun MatchModel?.team(team: Team): List<TeamPlayer> {
        this ?: return emptyList()
        val (members, teamGuests) = if (team == Team.A) teamA to teamAGuests else teamB to teamBGuests
        return members.map { player(it, isGuest = false) } + teamGuests.map { player(it.guestId, isGuest = true) }
    }

    fun player(id: String, isGuest: Boolean): TeamPlayer {
        if (isGuest) {
            val guest = guests[id]
            return TeamPlayer(id, guest?.name, null, null, guest?.position, isGuest = true, isMe = false)
        }
        val member = members[id]
        return TeamPlayer(id, member?.username, member?.profilePictureUrl, member?.shirtNumber, member?.position, isGuest = false, isMe = id == myMemberId)
    }
}

sealed interface MatchDetailAction {
    data object OnRefresh : MatchDetailAction
    data object OnRedrawClick : MatchDetailAction
    data object OnConfirmRedraw : MatchDetailAction
    data object OnStartManualEdit : MatchDetailAction
    data class OnMovePlayer(val id: String) : MatchDetailAction
    data object OnCancelManualEdit : MatchDetailAction
    data object OnSaveManualEdit : MatchDetailAction
    data object OnDismissDialog : MatchDetailAction
    data object OnToggleReport : MatchDetailAction
    data class OnAddEventClick(val playerId: String, val type: MatchEventType) : MatchDetailAction
    data object OnConfirmAddEvent : MatchDetailAction
    data class OnDeleteEvent(val event: MatchEventModel) : MatchDetailAction
    data class OnUndoDeleteEvent(val event: MatchEventModel) : MatchDetailAction
    data class OnEditMinutesClick(val playerId: String) : MatchDetailAction
    data object OnConfirmMinutes : MatchDetailAction
    data object OnCompleteClick : MatchDetailAction
    data object OnConfirmComplete : MatchDetailAction
    data object OnReopenClick : MatchDetailAction
    data object OnConfirmReopen : MatchDetailAction
    data object OnCancelMatchClick : MatchDetailAction
    data object OnConfirmCancelMatch : MatchDetailAction
    data object OnManualScoreClick : MatchDetailAction
    data class OnManualScoreChanged(val team: Team, val delta: Int) : MatchDetailAction
    data object OnConfirmManualScore : MatchDetailAction
    data object OnClearManualScore : MatchDetailAction
}

sealed interface MatchDetailEvent {
    data class ShowMessage(val message: UiText) : MatchDetailEvent
    data class ShowUndo(val message: UiText, val event: MatchEventModel) : MatchDetailEvent
    data object Close : MatchDetailEvent
}
