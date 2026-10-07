package com.kikepb.club.presentation.match

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.repository.PlayerStatInput
import com.kikepb.club.domain.usecase.AddGuestUseCase
import com.kikepb.club.domain.usecase.CancelSignupUseCase
import com.kikepb.club.domain.usecase.CreateMatchUseCase
import com.kikepb.club.domain.usecase.GenerateTeamsUseCase
import com.kikepb.club.domain.usecase.GetClubMatchesUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.ListSignupsUseCase
import com.kikepb.club.domain.usecase.RecordMatchResultUseCase
import com.kikepb.club.domain.usecase.RemoveSignupUseCase
import com.kikepb.club.domain.usecase.SignUpForMatchUseCase
import com.kikepb.club.presentation.match.MatchAction.OnCancelSignup
import com.kikepb.club.presentation.match.MatchAction.OnConfirmAddGuest
import com.kikepb.club.presentation.match.MatchAction.OnConfirmResult
import com.kikepb.club.presentation.match.MatchAction.OnCreateTestMatch
import com.kikepb.club.presentation.match.MatchAction.OnDismissAddGuestDialog
import com.kikepb.club.presentation.match.MatchAction.OnDismissResultDialog
import com.kikepb.club.presentation.match.MatchAction.OnGenerateTeams
import com.kikepb.club.presentation.match.MatchAction.OnGuestPositionSelected
import com.kikepb.club.presentation.match.MatchAction.OnIncrementStat
import com.kikepb.club.presentation.match.MatchAction.OnRefresh
import com.kikepb.club.presentation.match.MatchAction.OnRemoveSignup
import com.kikepb.club.presentation.match.MatchAction.OnShowAddGuestDialog
import com.kikepb.club.presentation.match.MatchAction.OnShowResultDialog
import com.kikepb.club.presentation.match.MatchAction.OnSignUp
import com.kikepb.club.presentation.match.MatchEvent.ShowMessage
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class MatchViewModel(
    private val getClubMatchesUseCase: GetClubMatchesUseCase,
    private val createMatchUseCase: CreateMatchUseCase,
    private val listSignupsUseCase: ListSignupsUseCase,
    private val signUpForMatchUseCase: SignUpForMatchUseCase,
    private val cancelSignupUseCase: CancelSignupUseCase,
    private val addGuestUseCase: AddGuestUseCase,
    private val removeSignupUseCase: RemoveSignupUseCase,
    private val generateTeamsUseCase: GenerateTeamsUseCase,
    private val recordMatchResultUseCase: RecordMatchResultUseCase,
    private val getClubMembersUseCase: GetClubMembersUseCase,
    private val sessionStorage: SessionStorage,
    private val featureFlags: FeatureFlags,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val eventChannel = Channel<MatchEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MatchState())

    val state = combine(
        flow = _state,
        flow2 = getClubMembersUseCase(clubId = clubId),
        flow3 = sessionStorage.observeAuthInfo(),
        flow4 = featureFlags.observeAll()
    ) { current, members, authInfo, flags ->
        val myMembership = members.find { it.userId == authInfo?.user?.id }
        val enabledFlags = flags.filter { it.enabled }.map { it.flag }.toSet()
        current.copy(
            members = members,
            myMemberId = myMembership?.id,
            isAdmin = myMembership?.role?.isManager == true,
            isGuestsEnabled = FeatureFlag.MATCH_GUESTS in enabledFlags,
            isTestMatchEnabled = FeatureFlag.DEV_TEST_MATCH in enabledFlags,
            isManualScoreEnabled = FeatureFlag.MANUAL_SCORE in enabledFlags
        )
    }
        .onStart { refresh() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = MatchState()
        )

    fun onAction(action: MatchAction) {
        when (action) {
            OnRefresh -> refresh()
            OnCreateTestMatch -> createTestMatch()
            OnSignUp -> signUp()
            OnCancelSignup -> cancelSignup()
            OnShowAddGuestDialog -> _state.update { it.copy(showAddGuestDialog = true) }
            OnDismissAddGuestDialog -> dismissAddGuestDialog()
            is OnGuestPositionSelected -> _state.update { it.copy(guestPosition = action.position) }
            OnConfirmAddGuest -> confirmAddGuest()
            is OnRemoveSignup -> removeSignup(signupId = action.signupId)
            OnGenerateTeams -> generateTeams()
            OnShowResultDialog -> _state.update { it.copy(showResultDialog = true) }
            OnDismissResultDialog -> _state.update { it.copy(showResultDialog = false) }
            is OnIncrementStat -> incrementStat(clubMemberId = action.clubMemberId, statType = action.statType)
            OnConfirmResult -> confirmResult()
        }
    }

    private fun refresh() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getClubMatchesUseCase(clubId = clubId)) {
                is Success -> {
                    val latestMatch = result.data.maxByOrNull { it.scheduledAt }
                    _state.update { it.copy(isLoading = false, match = latestMatch) }
                    latestMatch?.let { loadSignups(matchId = it.id) }
                }
                is Failure -> {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private suspend fun loadSignups(matchId: String) {
        when (val result = listSignupsUseCase(matchId = matchId)) {
            is Success -> _state.update { it.copy(signups = result.data) }
            is Failure -> eventChannel.send(ShowMessage(result.error.toUiText()))
        }
    }

    private fun createTestMatch() {
        if (!featureFlags.isEnabled(FeatureFlag.DEV_TEST_MATCH)) return
        _state.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            val now = Clock.System.now()
            val scheduledAt = now.plus(2.hours)
            val signupClosesAt = now.plus(1.hours)

            when (val result = createMatchUseCase(
                clubId = clubId,
                scheduledAt = scheduledAt.toString(),
                signupOpensAt = now.toString(),
                signupClosesAt = signupClosesAt.toString()
            )) {
                is Success -> {
                    _state.update { it.copy(isPerformingAction = false, match = result.data) }
                    loadSignups(matchId = result.data.id)
                }
                is Failure -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun signUp() {
        val matchId = _state.value.match?.id ?: return
        _state.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            when (val result = signUpForMatchUseCase(matchId = matchId)) {
                is Success -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    loadSignups(matchId = matchId)
                }
                is Failure -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun cancelSignup() {
        val matchId = _state.value.match?.id ?: return
        _state.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            when (val result = cancelSignupUseCase(matchId = matchId)) {
                is Success -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    loadSignups(matchId = matchId)
                }
                is Failure -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun dismissAddGuestDialog() {
        _state.value.guestNameState.clearText()
        _state.value.guestRatingState.clearText()
        _state.update { it.copy(showAddGuestDialog = false, guestPosition = null) }
    }

    private fun confirmAddGuest() {
        if (!featureFlags.isEnabled(FeatureFlag.MATCH_GUESTS)) return
        val matchId = _state.value.match?.id ?: return
        val guestName = _state.value.guestNameState.text.toString().trim()
        if (guestName.isBlank()) return
        val rating = _state.value.guestRatingState.text.toString().trim().toIntOrNull()
        val position = _state.value.guestPosition

        _state.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            when (val result = addGuestUseCase(matchId = matchId, guestName = guestName, position = position, rating = rating)) {
                is Success -> {
                    dismissAddGuestDialog()
                    _state.update { it.copy(isPerformingAction = false, showAddGuestDialog = false) }
                    loadSignups(matchId = matchId)
                }
                is Failure -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun removeSignup(signupId: String) {
        val matchId = _state.value.match?.id ?: return
        _state.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            when (val result = removeSignupUseCase(matchId = matchId, signupId = signupId)) {
                is Success -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    loadSignups(matchId = matchId)
                }
                is Failure -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun generateTeams() {
        val matchId = _state.value.match?.id ?: return
        _state.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            when (val result = generateTeamsUseCase(matchId = matchId)) {
                is Success -> _state.update { it.copy(isPerformingAction = false, match = result.data) }
                is Failure -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun incrementStat(clubMemberId: String, statType: StatType) {
        _state.update { current ->
            val existing = current.playerStats[clubMemberId] ?: PlayerStatDraft()
            val updated = when (statType) {
                StatType.GOAL -> existing.copy(goals = existing.goals + 1)
                StatType.ASSIST -> existing.copy(assists = existing.assists + 1)
                StatType.YELLOW -> existing.copy(yellowCards = existing.yellowCards + 1)
                StatType.RED -> existing.copy(redCards = existing.redCards + 1)
            }
            current.copy(playerStats = current.playerStats + (clubMemberId to updated))
        }
    }

    private fun confirmResult() {
        if (!featureFlags.isEnabled(FeatureFlag.MANUAL_SCORE)) return
        val match = _state.value.match ?: return
        val teamAScore = _state.value.teamAScoreState.text.toString().trim().toIntOrNull()
        val teamBScore = _state.value.teamBScoreState.text.toString().trim().toIntOrNull()
        if (teamAScore == null || teamBScore == null) {
            viewModelScope.launch { eventChannel.send(ShowMessage(UiText.DynamicString("Introduce el marcador de ambos equipos"))) }
            return
        }

        val playerStats = _state.value.playerStats.map { (clubMemberId, draft) ->
            PlayerStatInput(
                clubMemberId = clubMemberId,
                goals = draft.goals,
                assists = draft.assists,
                yellowCards = draft.yellowCards,
                redCards = draft.redCards,
                minutesPlayed = 90
            )
        }

        _state.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            when (val result = recordMatchResultUseCase(
                matchId = match.id,
                teamAScore = teamAScore,
                teamBScore = teamBScore,
                playerStats = playerStats
            )) {
                is Success -> _state.update {
                    it.copy(isPerformingAction = false, showResultDialog = false, match = result.data, playerStats = emptyMap())
                }
                is Failure -> {
                    _state.update { it.copy(isPerformingAction = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }
}

data class MatchState(
    val isLoading: Boolean = true,
    val isPerformingAction: Boolean = false,
    val isAdmin: Boolean = false,
    val myMemberId: String? = null,
    val members: List<ClubMemberModel> = emptyList(),
    val match: ClubMatchModel? = null,
    val signups: List<MatchSignupModel> = emptyList(),
    val playerStats: Map<String, PlayerStatDraft> = emptyMap(),
    val guestNameState: TextFieldState = TextFieldState(),
    val guestPosition: String? = null,
    val guestRatingState: TextFieldState = TextFieldState(),
    val showAddGuestDialog: Boolean = false,
    val teamAScoreState: TextFieldState = TextFieldState(),
    val teamBScoreState: TextFieldState = TextFieldState(),
    val showResultDialog: Boolean = false,
    // Feature flags (spec 013): pending backend features and QA tooling
    val isGuestsEnabled: Boolean = false,
    val isTestMatchEnabled: Boolean = false,
    val isManualScoreEnabled: Boolean = false
) {
    val mySignup: MatchSignupModel?
        get() = signups.find { it.clubMemberId == myMemberId && it.status == "CONFIRMED" }

    val confirmedSignups: List<MatchSignupModel>
        get() = signups.filter { it.status == "CONFIRMED" }
}

data class PlayerStatDraft(
    val goals: Int = 0,
    val assists: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0
)

enum class StatType { GOAL, ASSIST, YELLOW, RED }

sealed interface MatchAction {
    data object OnRefresh : MatchAction
    data object OnCreateTestMatch : MatchAction
    data object OnSignUp : MatchAction
    data object OnCancelSignup : MatchAction
    data object OnShowAddGuestDialog : MatchAction
    data object OnDismissAddGuestDialog : MatchAction
    data class OnGuestPositionSelected(val position: String?) : MatchAction
    data object OnConfirmAddGuest : MatchAction
    data class OnRemoveSignup(val signupId: String) : MatchAction
    data object OnGenerateTeams : MatchAction
    data object OnShowResultDialog : MatchAction
    data object OnDismissResultDialog : MatchAction
    data class OnIncrementStat(val clubMemberId: String, val statType: StatType) : MatchAction
    data object OnConfirmResult : MatchAction
}

sealed interface MatchEvent {
    data class ShowMessage(val message: UiText) : MatchEvent
}
