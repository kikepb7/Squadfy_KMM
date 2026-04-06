package com.kikepb.club.presentation.create

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.domain.usecase.UploadClubLogoUseCase
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateClubViewModel(
    private val createClubUseCase: CreateClubUseCase,
    private val uploadClubLogoUseCase: UploadClubLogoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateClubState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<CreateClubEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: CreateClubAction) {
        when (action) {
            CreateClubAction.OnSubmit -> submit()
            is CreateClubAction.OnLogoSelected -> {
                _state.update {
                    it.copy(
                        selectedLogoBytes = action.bytes,
                        selectedLogoMimeType = action.mimeType,
                        logoError = null
                    )
                }
            }
            CreateClubAction.OnClearLogoSelection -> {
                _state.update {
                    it.copy(selectedLogoBytes = null, selectedLogoMimeType = null, logoError = null)
                }
            }
            CreateClubAction.OnClearErrors -> {
                _state.update { it.copy(nameError = null, maxMembersError = null, logoError = null) }
            }
        }
    }

    private fun submit() {
        val name = _state.value.nameState.text.toString().trim()
        val description = _state.value.descriptionState.text.toString().trim().ifBlank { null }
        val maxMembersStr = _state.value.maxMembersState.text.toString().trim()

        if (name.isBlank()) {
            _state.update { it.copy(nameError = "El nombre del club es obligatorio") }
            return
        }
        if (name.length > 120) {
            _state.update { it.copy(nameError = "Máximo 120 caracteres") }
            return
        }

        val maxMembers: Int? = if (maxMembersStr.isBlank()) {
            null
        } else {
            val n = maxMembersStr.toIntOrNull()
            if (n == null || n < 1) {
                _state.update { it.copy(maxMembersError = "Introduce un número positivo válido") }
                return
            }
            n
        }

        _state.update { it.copy(isLoading = true, nameError = null, maxMembersError = null, logoError = null) }

        viewModelScope.launch {
            val logoUrl: String? = run {
                val bytes = _state.value.selectedLogoBytes
                val mimeType = _state.value.selectedLogoMimeType
                if (bytes != null && mimeType != null) {
                    _state.update { it.copy(isUploadingLogo = true) }
                    var resultUrl: String? = null
                    uploadClubLogoUseCase(bytes = bytes, mimeType = mimeType)
                        .onSuccess { url ->
                            resultUrl = url
                            _state.update { it.copy(isUploadingLogo = false) }
                        }
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isUploadingLogo = false,
                                    logoError = error.toUiText()
                                )
                            }
                            eventChannel.send(CreateClubEvent.Error(error.toUiText()))
                        }
                    resultUrl
                } else null
            }

            if (_state.value.logoError != null) return@launch

            createClubUseCase(
                name = name,
                description = description,
                clubLogoUrl = logoUrl,
                maxMembers = maxMembers
            )
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(CreateClubEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(CreateClubEvent.Error(error.toUiText()))
                }
        }
    }
}

data class CreateClubState(
    val nameState: TextFieldState = TextFieldState(),
    val descriptionState: TextFieldState = TextFieldState(),
    val maxMembersState: TextFieldState = TextFieldState(),
    val selectedLogoBytes: ByteArray? = null,
    val selectedLogoMimeType: String? = null,
    val isLoading: Boolean = false,
    val isUploadingLogo: Boolean = false,
    val nameError: String? = null,
    val maxMembersError: String? = null,
    val logoError: UiText? = null
) {
    val canSubmit: Boolean get() = nameState.text.isNotBlank() && !isLoading

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CreateClubState
        return nameState == other.nameState &&
            descriptionState == other.descriptionState &&
            maxMembersState == other.maxMembersState &&
            selectedLogoBytes.contentEquals(other.selectedLogoBytes) &&
            selectedLogoMimeType == other.selectedLogoMimeType &&
            isLoading == other.isLoading &&
            isUploadingLogo == other.isUploadingLogo &&
            nameError == other.nameError &&
            maxMembersError == other.maxMembersError &&
            logoError == other.logoError
    }

    override fun hashCode(): Int {
        var result = nameState.hashCode()
        result = 31 * result + descriptionState.hashCode()
        result = 31 * result + maxMembersState.hashCode()
        result = 31 * result + (selectedLogoBytes?.contentHashCode() ?: 0)
        result = 31 * result + (selectedLogoMimeType?.hashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isUploadingLogo.hashCode()
        result = 31 * result + (nameError?.hashCode() ?: 0)
        result = 31 * result + (maxMembersError?.hashCode() ?: 0)
        result = 31 * result + (logoError?.hashCode() ?: 0)
        return result
    }
}

sealed interface CreateClubAction {
    data object OnSubmit : CreateClubAction
    data class OnLogoSelected(val bytes: ByteArray, val mimeType: String?) : CreateClubAction
    data object OnClearLogoSelection : CreateClubAction
    data object OnClearErrors : CreateClubAction
}

sealed interface CreateClubEvent {
    data object Success : CreateClubEvent
    data class Error(val message: UiText) : CreateClubEvent
}
