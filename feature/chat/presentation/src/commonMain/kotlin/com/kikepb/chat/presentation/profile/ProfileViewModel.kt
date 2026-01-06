package com.kikepb.chat.presentation.profile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.usecases.participant.FetchLocalParticipantUseCase
import com.kikepb.chat.domain.usecases.profile.ChangePasswordUseCase
import com.kikepb.chat.domain.usecases.profile.DeleteProfilePictureUseCase
import com.kikepb.chat.domain.usecases.profile.UploadProfilePictureUseCase
import com.kikepb.chat.presentation.profile.ProfileAction.OnChangePasswordClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnConfirmDeleteClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnDeletePictureClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnPictureSelected
import com.kikepb.chat.presentation.profile.ProfileAction.OnToggleCurrentPasswordVisibility
import com.kikepb.chat.presentation.profile.ProfileAction.OnToggleNewPasswordVisibility
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataError.Remote.CONFLICT
import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.domain.validation.PasswordValidator
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.error_current_password_equal_to_new_one
import squadfy_app.feature.chat.presentation.generated.resources.error_current_password_incorrect
import squadfy_app.feature.chat.presentation.generated.resources.error_invalid_file_type

class ProfileViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val fetchLocalParticipantUseCase: FetchLocalParticipantUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
    private val sessionStorage: SessionStorage
): ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ProfileState())
    val state = combine(
        _state,
        sessionStorage.observeAuthInfo()
    ) { currentState, authInfo ->
        if (authInfo != null) {
            currentState.copy(
                username = authInfo.user.username,
                emailTextState = TextFieldState(initialText = authInfo.user.email),
                profilePictureUrl = authInfo.user.profilePictureUrl
            )
        } else currentState
    }
        .onStart {
            if (!hasLoadedInitialData) {
                observeCanChangePassword()
                fetchLocalParticipantDetails()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = ProfileState()
        )

    private fun fetchLocalParticipantDetails() =
        viewModelScope.launch {
            fetchLocalParticipantUseCase.fetchLocalParticipant()
        }


    private fun toggleCurrentPasswordVisibility() = _state.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }

    private fun toggleNewPasswordVisibility() = _state.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }

    private fun changePassword() {
        if (!state.value.canChangePassword && state.value.isChangingPassword) return

        _state.update { it.copy(isChangingPassword = true, isPasswordChangeSuccessful = false) }

        viewModelScope.launch {
            val currentPassword = state.value.currentPasswordTextState.text.toString()
            val newPassword = state.value.newPasswordTextState.text.toString()

            changePasswordUseCase.changePassword(currentPassword = currentPassword, newPassword = newPassword)
                .onSuccess {
                    state.value.currentPasswordTextState.clearText()
                    state.value.newPasswordTextState.clearText()

                    _state.update { it.copy(
                        isChangingPassword = false,
                        newPasswordError = null,
                        isNewPasswordVisible = false,
                        isCurrentPasswordVisible = false,
                        isPasswordChangeSuccessful = true
                    ) }
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        UNAUTHORIZED -> UiText.Resource(RString.error_current_password_incorrect)
                        CONFLICT -> UiText.Resource(RString.error_current_password_equal_to_new_one)
                        else -> error.toUiText()
                    }

                    _state.update { it.copy(newPasswordError = errorMessage, isChangingPassword = false) }
                }
        }
    }

    private fun observeCanChangePassword() {
        val isCurrentPasswordValidFlow = snapshotFlow {
            _state.value.currentPasswordTextState.text.toString()
        }.map { it.isNotBlank() }.distinctUntilChanged()

        val isNewPasswordValidFlow = snapshotFlow {
            _state.value.newPasswordTextState.text.toString()
        }.map {
            PasswordValidator.validate(password = it).isValidPassword
        }.distinctUntilChanged()

        combine(
            isCurrentPasswordValidFlow,
            isNewPasswordValidFlow
        ) { isCurrentValid, isNewValid ->
            _state.update { it.copy(canChangePassword = isCurrentValid && isNewValid) }
        }.launchIn(scope = viewModelScope)
    }

    private fun uploadProfilePicture(bytes: ByteArray, mimeType: String?) {
        if (state.value.isUploadingImage) return

        if (mimeType == null) {
            _state.update { it.copy(imageError = UiText.Resource(RString.error_invalid_file_type)) }
            return
        }
        _state.update { it.copy(isUploadingImage = true, imageError = null) }

        viewModelScope.launch {
            uploadProfilePictureUseCase.uploadProfilePicture(imageBytes = bytes, mimeType = mimeType)
                .onSuccess {
                    _state.update { it.copy(isUploadingImage = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isUploadingImage = false, imageError = error.toUiText()) }
                }
        }
    }

    private fun showDeleteConfirmation() = _state.update { it.copy(showDeleteConfirmationDialog = true) }

    private fun dismissDeleteConfirmation() = _state.update { it.copy(showDeleteConfirmationDialog = false) }

    private fun deleteProfilePicture() {
        if (state.value.isDeletingImage && state.value.profilePictureUrl == null) return

        _state.update { it.copy(isDeletingImage = true, imageError = null, showDeleteConfirmationDialog = false) }

        viewModelScope.launch {
            deleteProfilePictureUseCase.deleteProfilePicture()
                .onSuccess {
                    _state.update { it.copy(isDeletingImage = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(imageError = error.toUiText(), isDeletingImage = false) }
                }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is OnChangePasswordClick -> changePassword()
            is OnToggleCurrentPasswordVisibility -> toggleCurrentPasswordVisibility()
            is OnToggleNewPasswordVisibility -> toggleNewPasswordVisibility()
            is OnPictureSelected -> uploadProfilePicture(bytes = action.bytes, mimeType = action.mimeType)
            is OnDeletePictureClick -> showDeleteConfirmation()
            is OnConfirmDeleteClick -> deleteProfilePicture()
            is ProfileAction.OnDismissDeleteConfirmationDialogClick -> dismissDeleteConfirmation()
            else -> Unit
        }
    }
}

data class ProfileState(
    val username: String = "",
    val userInitials: String = "--",
    val profilePictureUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val isDeletingImage: Boolean = false,
    val showDeleteConfirmationDialog: Boolean = false,
    val imageError: UiText? = null,
    val emailTextState: TextFieldState = TextFieldState(),
    val currentPasswordTextState: TextFieldState = TextFieldState(),
    val newPasswordTextState: TextFieldState = TextFieldState(),
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isChangingPassword: Boolean = false,
    val newPasswordError: UiText? = null,
    val canChangePassword: Boolean = false,
    val isPasswordChangeSuccessful: Boolean = false
)

sealed interface ProfileAction {
    data object OnDismiss: ProfileAction
    data object OnUploadPictureClick: ProfileAction
    class OnPictureSelected(val bytes: ByteArray, val mimeType: String?): ProfileAction
    data object OnDeletePictureClick: ProfileAction
    data object OnConfirmDeleteClick: ProfileAction
    data object OnDismissDeleteConfirmationDialogClick: ProfileAction
    data object OnToggleCurrentPasswordVisibility: ProfileAction
    data object OnToggleNewPasswordVisibility: ProfileAction
    data object OnChangePasswordClick: ProfileAction
}