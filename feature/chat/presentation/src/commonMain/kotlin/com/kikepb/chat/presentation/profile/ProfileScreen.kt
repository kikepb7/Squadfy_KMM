package com.kikepb.chat.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.presentation.profile.ProfileAction.OnChangePasswordClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnConfirmDeleteClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnDeletePictureClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnDismiss
import com.kikepb.chat.presentation.profile.ProfileAction.OnDismissDeleteConfirmationDialogClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnToggleCurrentPasswordVisibility
import com.kikepb.chat.presentation.profile.ProfileAction.OnToggleNewPasswordVisibility
import com.kikepb.chat.presentation.profile.ProfileAction.OnUploadPictureClick
import com.kikepb.chat.presentation.profile.components.ProfileHeaderSection
import com.kikepb.chat.presentation.profile.components.ProfileSectionLayout
import com.kikepb.core.designsystem.components.avatar.AvatarSize.LARGE
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.DESTRUCTIVE_SECONDARY
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.SECONDARY
import com.kikepb.core.designsystem.components.dialogs.SquadfyAdaptiveDialogSheetLayout
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.divider.SquadfyHorizontalDivider
import com.kikepb.core.designsystem.components.textfields.SquadfyPasswordTextField
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.DeviceConfiguration.MOBILE_LANDSCAPE
import com.kikepb.core.presentation.util.DeviceConfiguration.MOBILE_PORTRAIT
import com.kikepb.core.presentation.util.clearFocusOnTap
import com.kikepb.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.chat.presentation.generated.resources.cancel
import squadfy_app.feature.chat.presentation.generated.resources.contact_squadfy_support_change_email
import squadfy_app.feature.chat.presentation.generated.resources.current_password
import squadfy_app.feature.chat.presentation.generated.resources.delete
import squadfy_app.feature.chat.presentation.generated.resources.delete_profile_picture
import squadfy_app.feature.chat.presentation.generated.resources.delete_profile_picture_desc
import squadfy_app.feature.chat.presentation.generated.resources.email
import squadfy_app.feature.chat.presentation.generated.resources.new_password
import squadfy_app.feature.chat.presentation.generated.resources.password
import squadfy_app.feature.chat.presentation.generated.resources.password_change_successful
import squadfy_app.feature.chat.presentation.generated.resources.password_hint
import squadfy_app.feature.chat.presentation.generated.resources.profile_image
import squadfy_app.feature.chat.presentation.generated.resources.save
import squadfy_app.feature.chat.presentation.generated.resources.upload_icon
import squadfy_app.feature.chat.presentation.generated.resources.upload_image
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.Res.drawable as RDrawable

@Composable
fun ProfileRoot(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SquadfyAdaptiveDialogSheetLayout(onDismiss = onDismiss) {
        ProfileScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    is OnDismiss -> onDismiss()
                    else -> Unit
                }
                viewModel.onAction(action = action)
            }
        )
    }
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit
) {
    val deviceConfiguration = currentDeviceConfiguration()

    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(size = 16.dp)
            )
            .verticalScroll(state = rememberScrollState())
    ) {
        ProfileHeaderSection(
            username = state.username,
            onCloseClick = { onAction(OnDismiss) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp)
        )
        SquadfyHorizontalDivider()
        ProfileSectionLayout(
            headerText = stringResource(resource = RString.profile_image)
        ) {
            Row {
                SquadfyAvatarPhoto(
                    displayText = state.userInitials,
                    size = LARGE,
                    imageUrl = state.profilePictureUrl,
                    onClick = { onAction(OnUploadPictureClick) }
                )
                Spacer(modifier = Modifier.width(width = 20.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    SquadfyButton(
                        text = stringResource(resource = RString.upload_image),
                        onClick = { onAction(OnUploadPictureClick) },
                        style = SECONDARY,
                        enabled = !state.isUploadingImage && !state.isDeletingImage,
                        isLoading = state.isUploadingImage,
                        leadingIcon = {
                            Icon(
                                imageVector = vectorResource(resource = RDrawable.upload_icon),
                                contentDescription = stringResource(resource = RString.upload_image)
                            )
                        }
                    )
                    SquadfyButton(
                        text = stringResource(resource = RString.delete),
                        onClick = { onAction(OnDeletePictureClick) },
                        style = DESTRUCTIVE_SECONDARY,
                        enabled = !state.isUploadingImage && !state.isDeletingImage && state.profilePictureUrl != null,
                        isLoading = state.isDeletingImage,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(resource = RString.delete)
                            )
                        }
                    )
                }
            }

            if (state.imageError != null) {
                Text(
                    text = state.imageError.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        SquadfyHorizontalDivider()
        ProfileSectionLayout(
            headerText = stringResource(resource = RString.email)
        ) {
            SquadfyTextField(
                state = state.emailTextState,
                enabled = false,
                supportingText = stringResource(resource = RString.contact_squadfy_support_change_email)
            )
        }
        SquadfyHorizontalDivider()
        ProfileSectionLayout(
            headerText = stringResource(RString.password)
        ) {
            SquadfyPasswordTextField(
                state = state.currentPasswordTextState,
                isPasswordVisible = state.isCurrentPasswordVisible,
                onToggleVisibilityClick = { onAction(OnToggleCurrentPasswordVisibility) },
                placeholder = stringResource(resource = RString.current_password),
                isError = state.newPasswordError != null,
            )
            SquadfyPasswordTextField(
                state = state.newPasswordTextState,
                isPasswordVisible = state.isNewPasswordVisible,
                onToggleVisibilityClick = { onAction(OnToggleNewPasswordVisibility) },
                placeholder = stringResource(resource = RString.new_password),
                isError = state.newPasswordError != null,
                supportingText = state.newPasswordError?.asString() ?: stringResource(resource = RString.password_hint)
            )

            if (state.isPasswordChangeSuccessful) {
                Text(
                    text = stringResource(RString.password_change_successful),
                    color = MaterialTheme.colorScheme.extended.success,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 16.dp, Alignment.End)
            ) {
                SquadfyButton(
                    text = stringResource(resource = RString.cancel),
                    style = SECONDARY,
                    onClick = { onAction(OnDismiss) }
                )
                SquadfyButton(
                    text = stringResource(resource = RString.save),
                    onClick = { onAction(OnChangePasswordClick) },
                    enabled = state.canChangePassword,
                    isLoading = state.isChangingPassword
                )
            }
        }

        if (deviceConfiguration in listOf(MOBILE_PORTRAIT, MOBILE_LANDSCAPE)) {
            Spacer(modifier = Modifier.weight(weight = 1f))
        }
    }

    if (state.showDeleteConfirmationDialog) {
        SquadfyDestructiveConfirmationDialog(
            title = stringResource(resource = RString.delete_profile_picture),
            description = stringResource(resource = RString.delete_profile_picture_desc),
            confirmButtonText = stringResource(resource = RString.delete),
            cancelButtonText = stringResource(resource = RString.cancel),
            onConfirmClick = { onAction(OnConfirmDeleteClick) },
            onCancelClick = { onAction(OnDismissDeleteConfirmationDialogClick) },
            onDismiss = { onAction(OnDismissDeleteConfirmationDialogClick) },
        )
    }
}

@Preview
@Composable
private fun ProfilePreview() {
    SquadfyTheme {
        ProfileScreen(
            state = ProfileState(),
            onAction = {}
        )
    }
}
