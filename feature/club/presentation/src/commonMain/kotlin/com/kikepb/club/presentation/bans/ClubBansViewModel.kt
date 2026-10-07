package com.kikepb.club.presentation.bans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubBanModel
import com.kikepb.club.domain.usecase.GetClubBansUseCase
import com.kikepb.club.domain.usecase.UnbanMemberUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.bans_unbanned

/** Banned members of a club, managers only (BE-001 RN-14, AC-003-12). */
class ClubBansViewModel(
    private val getClubBansUseCase: GetClubBansUseCase,
    private val unbanMemberUseCase: UnbanMemberUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val _state = MutableStateFlow(ClubBansState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<ClubBansEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: ClubBansAction) {
        when (action) {
            ClubBansAction.OnRefresh -> load()
            is ClubBansAction.OnUnban -> unban(action.ban)
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getClubBansUseCase(clubId)
                .onSuccess { bans -> _state.update { it.copy(bans = bans) } }
                .onFailure { error -> eventChannel.send(ClubBansEvent.ShowMessage(error.toUiText())) }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun unban(ban: ClubBanModel) {
        _state.update { it.copy(unbanningIds = it.unbanningIds + ban.clubMemberId) }
        viewModelScope.launch {
            unbanMemberUseCase(clubId = clubId, memberId = ban.clubMemberId)
                .onSuccess {
                    _state.update { state -> state.copy(bans = state.bans - ban) }
                    eventChannel.send(ClubBansEvent.ShowMessage(UiText.Resource(Res.string.bans_unbanned, arrayOf(ban.username))))
                }
                .onFailure { error -> eventChannel.send(ClubBansEvent.ShowMessage(error.toUiText())) }
            _state.update { it.copy(unbanningIds = it.unbanningIds - ban.clubMemberId) }
        }
    }
}

data class ClubBansState(
    val bans: List<ClubBanModel> = emptyList(),
    val isLoading: Boolean = true,
    val unbanningIds: Set<String> = emptySet()
)

sealed interface ClubBansAction {
    data object OnRefresh : ClubBansAction
    data class OnUnban(val ban: ClubBanModel) : ClubBansAction
}

sealed interface ClubBansEvent {
    data class ShowMessage(val message: UiText) : ClubBansEvent
}
