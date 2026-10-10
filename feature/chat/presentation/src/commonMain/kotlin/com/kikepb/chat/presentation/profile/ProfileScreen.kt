package com.kikepb.chat.presentation.profile

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.components.TriMenuRow
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.chat.presentation.generated.resources.profile_change_password
import squadfy_app.feature.chat.presentation.generated.resources.profile_change_photo
import squadfy_app.feature.chat.presentation.generated.resources.profile_close
import squadfy_app.feature.chat.presentation.generated.resources.profile_crash_reports_short
import squadfy_app.feature.chat.presentation.generated.resources.profile_title
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.presentation.profile.ProfileAction.OnChangePasswordClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnConfirmDeleteAccount
import com.kikepb.chat.presentation.profile.ProfileAction.OnDeleteAccountClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnDismissDeleteAccountDialog
import com.kikepb.chat.presentation.profile.ProfileAction.OnPrivacyPolicyClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnToggleDeleteAccountPasswordVisibility
import com.kikepb.chat.presentation.profile.ProfileAction.OnConfirmDeleteClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnDeletePictureClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnDismiss
import com.kikepb.chat.presentation.profile.ProfileAction.OnDismissDeleteConfirmationDialogClick
import com.kikepb.chat.presentation.profile.ProfileAction.OnPictureSelected
import com.kikepb.chat.presentation.profile.ProfileAction.OnToggleCurrentPasswordVisibility
import com.kikepb.chat.presentation.profile.ProfileAction.OnToggleNewPasswordVisibility
import com.kikepb.chat.presentation.profile.ProfileAction.OnUploadPictureClick
import com.kikepb.chat.presentation.profile.components.ProfileHeaderSection
import com.kikepb.chat.presentation.profile.components.ProfileSectionLayout
import com.kikepb.core.presentation.mediapicker.rememberImagePickerLauncher
import com.kikepb.core.designsystem.components.avatar.AvatarSize.LARGE
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.DESTRUCTIVE_SECONDARY
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.SECONDARY
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.TEXT
import com.kikepb.core.designsystem.components.dialogs.SquadfyAdaptiveDialogSheetLayout
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.divider.SquadfyHorizontalDivider
import com.kikepb.core.designsystem.components.textfields.SquadfyPasswordTextField
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.DeviceConfiguration.MOBILE_LANDSCAPE
import com.kikepb.core.presentation.util.DeviceConfiguration.MOBILE_PORTRAIT
import com.kikepb.core.domain.config.LegalLinks
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.core.presentation.util.clearFocusOnTap
import com.kikepb.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.chat.presentation.generated.resources.account
import squadfy_app.feature.chat.presentation.generated.resources.feature_flags
import squadfy_app.feature.chat.presentation.generated.resources.do_you_want_to_logout_desc
import squadfy_app.feature.chat.presentation.generated.resources.do_you_want_to_logout
import squadfy_app.feature.chat.presentation.generated.resources.logout
import squadfy_app.feature.chat.presentation.generated.resources.cancel
import squadfy_app.feature.chat.presentation.generated.resources.contact_squadfy_support_change_email
import squadfy_app.feature.chat.presentation.generated.resources.crash_reports
import squadfy_app.feature.chat.presentation.generated.resources.crash_reports_desc
import squadfy_app.feature.chat.presentation.generated.resources.current_password
import squadfy_app.feature.chat.presentation.generated.resources.delete
import squadfy_app.feature.chat.presentation.generated.resources.delete_account
import squadfy_app.feature.chat.presentation.generated.resources.delete_account_desc
import squadfy_app.feature.chat.presentation.generated.resources.delete_account_password
import squadfy_app.feature.chat.presentation.generated.resources.delete_profile_picture
import squadfy_app.feature.chat.presentation.generated.resources.delete_profile_picture_desc
import squadfy_app.feature.chat.presentation.generated.resources.email
import squadfy_app.feature.chat.presentation.generated.resources.new_password
import squadfy_app.feature.chat.presentation.generated.resources.password
import squadfy_app.feature.chat.presentation.generated.resources.password_change_successful
import squadfy_app.feature.chat.presentation.generated.resources.password_hint
import squadfy_app.feature.chat.presentation.generated.resources.privacy_policy
import squadfy_app.feature.chat.presentation.generated.resources.profile_image
import squadfy_app.feature.chat.presentation.generated.resources.save
import squadfy_app.feature.chat.presentation.generated.resources.upload_icon
import squadfy_app.feature.chat.presentation.generated.resources.upload_image
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.Res.drawable as RDrawable

@Composable
fun ProfileRoot(
    onDismiss: () -> Unit,
    onAccountDeleted: () -> Unit,
    onSignedOut: () -> Unit,
    /** PRE only: opens the feature flags debug screen (spec 013). */
    onFeatureFlagsClick: (() -> Unit)? = null,
    viewModel: ProfileViewModel = koinViewModel(),
    legalLinks: LegalLinks = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            ProfileEvent.OnAccountDeleted -> onAccountDeleted()
            ProfileEvent.OnSignedOut -> onSignedOut()
        }
    }
    val imagePickerLauncher = rememberImagePickerLauncher { pickedImageData ->
        viewModel.onAction(action = OnPictureSelected(bytes = pickedImageData.bytes, mimeType = pickedImageData.mimeType))
    }

    SquadfyAdaptiveDialogSheetLayout(onDismiss = onDismiss) {
        ProfileScreen(
            state = state,
            showFeatureFlags = onFeatureFlagsClick != null,
            onAction = { action ->
                when (action) {
                    is OnDismiss -> onDismiss()
                    is OnUploadPictureClick -> { imagePickerLauncher.launch() }
                    is OnPrivacyPolicyClick -> uriHandler.openUri(legalLinks.privacyPolicyUrl)
                    is ProfileAction.OnFeatureFlagsClick -> onFeatureFlagsClick?.invoke()
                    else -> Unit
                }
                viewModel.onAction(action = action)
            }
        )
    }
}

/**
 * Perfil (spec 018, screen 13): avatar with «Cambiar foto», then one row per option. Changing the password opens
 * its form in place; deleting the account still asks for the password (spec 011).
 */
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    showFeatureFlags: Boolean = false
) {
    val colors = Triangulacion.colors
    var isPasswordOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxSize()
            .background(color = colors.surface100, shape = RoundedCornerShape(size = Radius.lg))
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = Space.s4, vertical = Space.s3),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriRoundIconButton(icon = TriIcon.ChevL, contentDescription = stringResource(RString.profile_close), onClick = { onAction(OnDismiss) }, filled = true)
            Text(
                text = stringResource(RString.profile_title),
                style = Triangulacion.type.displayMd,
                color = colors.ink,
                modifier = Modifier.padding(start = Space.s3)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = Space.s4)
        ) {
            TriAvatar(name = state.username.ifBlank { state.userInitials }, imageUrl = state.profilePictureUrl, size = 104.dp)
            Text(text = state.username, style = Triangulacion.type.displayMd, color = colors.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TriButton(
                    text = stringResource(RString.profile_change_photo),
                    onClick = { onAction(OnUploadPictureClick) },
                    variant = TriButtonVariant.Secondary,
                    icon = TriIcon.Camera,
                    enabled = !state.isUploadingImage && !state.isDeletingImage,
                    isLoading = state.isUploadingImage
                )
                if (state.profilePictureUrl != null) {
                    TriButton(
                        text = stringResource(RString.delete),
                        onClick = { onAction(OnDeletePictureClick) },
                        variant = TriButtonVariant.Danger,
                        enabled = !state.isUploadingImage && !state.isDeletingImage,
                        isLoading = state.isDeletingImage
                    )
                }
            }
            state.imageError?.let { Text(text = it.asString(), style = Triangulacion.type.bodySm, color = colors.danger) }
        }

        TriMenuRow(
            text = stringResource(RString.email),
            icon = TriIcon.Mail,
            supportingText = state.emailTextState.text.toString(),
            onClick = {},
            trailing = {}
        )
        TriMenuRow(
            text = stringResource(RString.profile_change_password),
            icon = TriIcon.Lock,
            onClick = { isPasswordOpen = !isPasswordOpen },
            trailing = { TriIconView(icon = if (isPasswordOpen) TriIcon.ChevD else TriIcon.ChevR, contentDescription = null, size = 20.dp, tint = colors.inkMuted) }
        )
        if (isPasswordOpen) {
            TriCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        TriNotice(title = stringResource(RString.password_change_successful), tone = TriNoticeTone.Ok)
                    }
                    TriButton(
                        text = stringResource(resource = RString.save),
                        onClick = { onAction(OnChangePasswordClick) },
                        enabled = state.canChangePassword,
                        isLoading = state.isChangingPassword,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        // AC-011-13: crash reports are opt-in
        if (state.isCrashReportingAvailable) {
            TriMenuRow(
                text = stringResource(RString.profile_crash_reports_short),
                icon = TriIcon.Shield,
                supportingText = stringResource(RString.crash_reports_desc),
                onClick = { onAction(ProfileAction.OnCrashReportsChanged(enabled = !state.crashReportsEnabled)) },
                trailing = {
                    Switch(
                        checked = state.crashReportsEnabled,
                        onCheckedChange = { onAction(ProfileAction.OnCrashReportsChanged(enabled = it)) }
                    )
                }
            )
        }
        // Spec 011 AC-011-07/08: privacy policy link and, behind ACCOUNT_DELETION, in-app account deletion
        TriMenuRow(text = stringResource(RString.privacy_policy), icon = TriIcon.Info, onClick = { onAction(OnPrivacyPolicyClick) })
        TriMenuRow(
            text = stringResource(RString.logout),
            icon = TriIcon.Logout,
            onClick = { onAction(ProfileAction.OnLogoutClick) },
            trailing = if (state.isSigningOut) ({ SquadfyLoadingSpinner() }) else null
        )
        if (state.isAccountDeletionEnabled) {
            TriMenuRow(text = stringResource(RString.delete_account), icon = TriIcon.Trash, destructive = true, onClick = { onAction(OnDeleteAccountClick) })
        }
        if (showFeatureFlags) {
            TriMenuRow(text = stringResource(RString.feature_flags), icon = TriIcon.Sliders, onClick = { onAction(ProfileAction.OnFeatureFlagsClick) })
        }
        Spacer(modifier = Modifier.height(Space.s4))
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

    if (state.showLogoutConfirmation) {
        SquadfyDestructiveConfirmationDialog(
            title = stringResource(resource = RString.do_you_want_to_logout),
            description = stringResource(resource = RString.do_you_want_to_logout_desc),
            confirmButtonText = stringResource(resource = RString.logout),
            cancelButtonText = stringResource(resource = RString.cancel),
            onConfirmClick = { onAction(ProfileAction.OnConfirmLogout) },
            onCancelClick = { onAction(ProfileAction.OnDismissLogoutDialog) },
            onDismiss = { onAction(ProfileAction.OnDismissLogoutDialog) }
        )
    }

    if (state.showDeleteAccountDialog) {
        SquadfyDestructiveConfirmationDialog(
            title = stringResource(resource = RString.delete_account),
            description = stringResource(resource = RString.delete_account_desc),
            confirmButtonText = stringResource(resource = RString.delete_account),
            cancelButtonText = stringResource(resource = RString.cancel),
            onConfirmClick = { onAction(OnConfirmDeleteAccount) },
            onCancelClick = { onAction(OnDismissDeleteAccountDialog) },
            onDismiss = { onAction(OnDismissDeleteAccountDialog) },
            isConfirmEnabled = state.deleteAccountPasswordState.text.isNotBlank() && !state.isDeletingAccount,
            isConfirmLoading = state.isDeletingAccount
        ) {
            SquadfyPasswordTextField(
                state = state.deleteAccountPasswordState,
                isPasswordVisible = state.isDeleteAccountPasswordVisible,
                onToggleVisibilityClick = { onAction(OnToggleDeleteAccountPasswordVisibility) },
                placeholder = stringResource(resource = RString.delete_account_password),
                isError = state.deleteAccountError != null,
                supportingText = state.deleteAccountError?.asString(),
                enabled = !state.isDeletingAccount
            )
        }
    }
}

@Composable
private fun SquadfyLoadingSpinner() {
    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Triangulacion.colors.brand)
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "13 Perfil · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ProfileTizaPreview() = ScreenPreview { ProfileScreen(state = ProfileState(), onAction = {}) }

@Preview(name = "13 Perfil · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ProfileNochePreview() = ScreenPreview(darkTheme = true) { ProfileScreen(state = ProfileState(), onAction = {}) }
