package com.kikepb.club.presentation.detail

import kotlinx.coroutines.Job
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.SyncClubDetailUseCase
import com.kikepb.club.presentation.detail.ClubDetailAction.OnRefresh
import com.kikepb.club.presentation.detail.ClubDetailEvent.ShowMessage
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.create_logo_failed

class ClubDetailViewModel(
    getClubByIdUseCase: GetClubByIdUseCase,
    getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val syncClubDetailUseCase: SyncClubDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val eventChannel = Channel<ClubDetailEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ClubDetailState())
    // Declared before init: an initializer placed after it would reset the job started there (spec 017)
    private var syncJob: Job? = null

    val state = combine(
        flow = _state,
        flow2 = getClubByIdUseCase(clubId = clubId),
        flow3 = getClubMembersUseCase(clubId = clubId),
        flow4 = observeMyMembershipUseCase(clubId = clubId)
    ) { current, club, members, myMembership ->
        // While the first sync runs, an uncached club is "loading", not "missing"
        current.copy(club = club, members = members, myMembership = myMembership)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = ClubDetailState()
        )

    init {
        if (savedStateHandle.get<Boolean>("logoUploadFailed") == true) {
            viewModelScope.launch { eventChannel.send(ShowMessage(UiText.Resource(Res.string.create_logo_failed))) }
        }
    }

    fun onAction(action: ClubDetailAction) {
        when (action) {
            OnRefresh -> syncFromNetwork(userInitiated = true)
            ClubDetailAction.OnResume -> syncFromNetwork(userInitiated = false)
        }
    }

    /**
     * Spec 017: the latest sync wins. A resume refreshes silently; its error is only shown when there is nothing
     * cached to display (otherwise the cached club stays and a snackbar on every resume would be noise).
     */
    private fun syncFromNetwork(userInitiated: Boolean) {
        syncJob?.cancel()
        _state.update { it.copy(isLoading = true) }
        syncJob = viewModelScope.launch {
            try {
                syncClubDetailUseCase(clubId = clubId).onFailure { error ->
                    if (userInitiated || state.value.club == null) eventChannel.send(ShowMessage(error.toUiText()))
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class ClubDetailState(
    val isLoading: Boolean = true,
    val club: ClubModel? = null,
    val members: List<ClubMemberModel> = emptyList(),
    /** The current user's membership; null while members are not cached yet. */
    val myMembership: ClubMemberModel? = null
)

sealed interface ClubDetailAction {
    data object OnRefresh : ClubDetailAction
    data object OnResume : ClubDetailAction
}

sealed interface ClubDetailEvent {
    data class ShowMessage(val message: UiText) : ClubDetailEvent
}
