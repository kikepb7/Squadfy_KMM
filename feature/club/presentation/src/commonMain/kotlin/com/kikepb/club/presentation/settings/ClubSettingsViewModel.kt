package com.kikepb.club.presentation.settings

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.usecase.EditClubUseCase
import com.kikepb.club.domain.usecase.GetClubMutedUseCase
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.club.domain.usecase.LeaveClubUseCase
import com.kikepb.club.domain.usecase.SetClubMutedUseCase
import com.kikepb.club.domain.usecase.RegenerateInvitationCodeUseCase
import com.kikepb.club.domain.usecase.UploadClubLogoUseCase
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
import squadfy_app.feature.club.presentation.generated.resources.settings_club_updated
import squadfy_app.feature.club.presentation.generated.resources.settings_logo_updated
import squadfy_app.feature.club.presentation.generated.resources.settings_regenerated

/** Club settings tab (spec 003, AC-003-10…14). Visibility per role is decided by `MemberPermissions` in the UI. */
class ClubSettingsViewModel(
    private val editClubUseCase: EditClubUseCase,
    private val regenerateInvitationCodeUseCase: RegenerateInvitationCodeUseCase,
    private val uploadClubLogoUseCase: UploadClubLogoUseCase,
    private val leaveClubUseCase: LeaveClubUseCase,
    private val getClubMutedUseCase: GetClubMutedUseCase,
    private val setClubMutedUseCase: SetClubMutedUseCase,
    featureFlags: FeatureFlags,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val _state = MutableStateFlow(ClubSettingsState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<ClubSettingsEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            featureFlags.observe(FeatureFlag.MEMBER_ABSENCES).collect { enabled -> _state.update { it.copy(absencesEnabled = enabled) } }
        }
        viewModelScope.launch {
            getClubMutedUseCase(clubId).onSuccess { muted -> _state.update { it.copy(muted = muted) } }
        }
    }

    fun onAction(action: ClubSettingsAction) {
        when (action) {
            is ClubSettingsAction.OnEditClubClick -> openEditDialog(action.club)
            ClubSettingsAction.OnSaveClub -> saveClub()
            ClubSettingsAction.OnRegenerateCodeClick -> _state.update { it.copy(dialog = SettingsDialog.REGENERATE_CODE) }
            ClubSettingsAction.OnConfirmRegenerateCode -> regenerateCode()
            ClubSettingsAction.OnLeaveClick -> _state.update { it.copy(dialog = SettingsDialog.LEAVE) }
            ClubSettingsAction.OnConfirmLeave -> leave()
            is ClubSettingsAction.OnLogoPicked -> uploadLogo(action.bytes, action.mimeType)
            ClubSettingsAction.OnDismissDialog -> _state.update { it.copy(dialog = null, editError = null) }
            is ClubSettingsAction.OnMutedChanged -> setMuted(action.muted)
        }
    }

    /** AC-009-05: optimistic switch, reverted if the backend rejects it. */
    private fun setMuted(muted: Boolean) {
        val previous = _state.value.muted
        _state.update { it.copy(muted = muted) }
        viewModelScope.launch {
            setClubMutedUseCase(clubId, muted)
                .onSuccess { saved -> _state.update { it.copy(muted = saved) } }
                .onFailure { error ->
                    _state.update { it.copy(muted = previous) }
                    eventChannel.send(ClubSettingsEvent.ShowMessage(error.toUiText()))
                }
        }
    }

    private fun openEditDialog(club: ClubModel) {
        _state.update {
            it.copy(
                dialog = SettingsDialog.EDIT_CLUB,
                editError = null,
                editName = TextFieldState(initialText = club.name),
                editDescription = TextFieldState(initialText = club.description.orEmpty()),
                editMaxMembers = TextFieldState(initialText = club.maxMembers?.toString().orEmpty())
            )
        }
    }

    private fun saveClub() = launchWorking {
        val current = _state.value
        editClubUseCase(
            clubId = clubId,
            name = current.editName.text.toString(),
            description = current.editDescription.text.toString(),
            maxMembersRaw = current.editMaxMembers.text.toString()
        )
            .onSuccess {
                _state.update { it.copy(dialog = null) }
                eventChannel.send(ClubSettingsEvent.ShowMessage(UiText.Resource(Res.string.settings_club_updated)))
            }
            .onFailure { error -> _state.update { it.copy(editError = error.toUiText()) } }
    }

    private fun regenerateCode() = launchWorking {
        _state.update { it.copy(dialog = null) }
        regenerateInvitationCodeUseCase(clubId)
            .onSuccess { code -> eventChannel.send(ClubSettingsEvent.ShowMessage(UiText.Resource(Res.string.settings_regenerated, arrayOf(code)))) }
            .onFailure { error -> eventChannel.send(ClubSettingsEvent.ShowMessage(error.toUiText())) }
    }

    private fun uploadLogo(bytes: ByteArray, mimeType: String?) = launchWorking {
        uploadClubLogoUseCase(clubId = clubId, bytes = bytes, mimeType = mimeType ?: "image/jpeg")
            .onSuccess { eventChannel.send(ClubSettingsEvent.ShowMessage(UiText.Resource(Res.string.settings_logo_updated))) }
            .onFailure { error -> eventChannel.send(ClubSettingsEvent.ShowMessage(error.toUiText())) }
    }

    private fun leave() = launchWorking {
        _state.update { it.copy(dialog = null) }
        leaveClubUseCase(clubId)
            .onSuccess { eventChannel.send(ClubSettingsEvent.LeftClub) }
            .onFailure { error -> eventChannel.send(ClubSettingsEvent.ShowMessage(error.toUiText())) }
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

enum class SettingsDialog { EDIT_CLUB, REGENERATE_CODE, LEAVE }

data class ClubSettingsState(
    val dialog: SettingsDialog? = null,
    val isWorking: Boolean = false,
    val editName: TextFieldState = TextFieldState(),
    val editDescription: TextFieldState = TextFieldState(),
    val editMaxMembers: TextFieldState = TextFieldState(),
    val editError: UiText? = null,
    /** Null until loaded: the switch is hidden. */
    val muted: Boolean? = null,
    val absencesEnabled: Boolean = false
)

sealed interface ClubSettingsAction {
    data class OnEditClubClick(val club: ClubModel) : ClubSettingsAction
    data object OnSaveClub : ClubSettingsAction
    data object OnRegenerateCodeClick : ClubSettingsAction
    data object OnConfirmRegenerateCode : ClubSettingsAction
    data object OnLeaveClick : ClubSettingsAction
    data object OnConfirmLeave : ClubSettingsAction
    data class OnLogoPicked(val bytes: ByteArray, val mimeType: String?) : ClubSettingsAction
    data object OnDismissDialog : ClubSettingsAction
    data class OnMutedChanged(val muted: Boolean) : ClubSettingsAction
}

sealed interface ClubSettingsEvent {
    data class ShowMessage(val message: UiText) : ClubSettingsEvent
    data object LeftClub : ClubSettingsEvent
}
