package com.kikepb.club.presentation.create

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.CreateClubError.BlankName
import com.kikepb.club.domain.model.CreateClubError.InvalidMaxMembers
import com.kikepb.club.domain.model.CreateClubError.NameTooLong
import com.kikepb.club.domain.model.CreateClubError.Remote
import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.presentation.create.CreateClubAction.OnClearErrors
import com.kikepb.club.presentation.create.CreateClubAction.OnClearLogoSelection
import com.kikepb.club.presentation.create.CreateClubAction.OnCreateClub
import com.kikepb.club.presentation.create.CreateClubAction.OnLogoSelected
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.club.presentation.utils.ClubLogoSelection
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateClubViewModel(
    private val createClubUseCase: CreateClubUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateClubState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<CreateClubEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: CreateClubAction) {
        when (action) {
            OnCreateClub -> createClub()
            is OnLogoSelected -> _state.update { it.copy(logoSelection = ClubLogoSelection(bytes = action.bytes, mimeType = action.mimeType)) }
            OnClearLogoSelection -> _state.update { it.copy(logoSelection = null) }
            OnClearErrors -> _state.update { it.copy(nameError = null, maxMembersError = null) }
        }
    }

    private fun createClub() {
        _state.update { it.copy(isLoading = true, nameError = null, maxMembersError = null) }

        viewModelScope.launch {
            when (val result = createClubUseCase(
                name = _state.value.nameState.text.toString().trim(),
                description = _state.value.descriptionState.text.toString().trim().ifBlank { null },
                maxMembersRaw = _state.value.maxMembersState.text.toString().trim().ifBlank { null },
                logoBytes = _state.value.logoSelection?.bytes,
                logoMimeType = _state.value.logoSelection?.mimeType
            )) {
                is Success -> {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(CreateClubEvent.Success)
                }
                is Failure -> when (val error = result.error) {
                    BlankName, NameTooLong -> _state.update { it.copy(isLoading = false, nameError = error.toUiText()) }
                    InvalidMaxMembers -> _state.update { it.copy(isLoading = false, maxMembersError = error.toUiText()) }
                    is Remote -> {
                        _state.update { it.copy(isLoading = false) }
                        eventChannel.send(CreateClubEvent.Error(error.toUiText()))
                    }
                }
            }
        }
    }
}

data class CreateClubState(
    val nameState: TextFieldState = TextFieldState(),
    val descriptionState: TextFieldState = TextFieldState(),
    val maxMembersState: TextFieldState = TextFieldState(),
    val logoSelection: ClubLogoSelection? = null,
    val isLoading: Boolean = false,
    val nameError: UiText? = null,
    val maxMembersError: UiText? = null
) {
    val canSubmit: Boolean get() = nameState.text.isNotBlank() && !isLoading
}

sealed interface CreateClubAction {
    data object OnCreateClub : CreateClubAction
    data class OnLogoSelected(val bytes: ByteArray, val mimeType: String?) : CreateClubAction
    data object OnClearLogoSelection : CreateClubAction
    data object OnClearErrors : CreateClubAction
}

sealed interface CreateClubEvent {
    data object Success : CreateClubEvent
    data class Error(val message: UiText) : CreateClubEvent
}
