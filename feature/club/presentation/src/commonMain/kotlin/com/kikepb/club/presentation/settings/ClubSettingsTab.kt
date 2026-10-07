package com.kikepb.club.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.policy.MemberPermissions
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.mediapicker.rememberImagePickerLauncher
import com.kikepb.core.presentation.share.rememberShareTextLauncher
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_confirm
import squadfy_app.feature.club.presentation.generated.resources.common_save
import squadfy_app.feature.club.presentation.generated.resources.edit_club_description
import squadfy_app.feature.club.presentation.generated.resources.edit_club_max_members
import squadfy_app.feature.club.presentation.generated.resources.edit_club_name
import squadfy_app.feature.club.presentation.generated.resources.edit_club_title
import squadfy_app.feature.club.presentation.generated.resources.settings_bans
import squadfy_app.feature.club.presentation.generated.resources.settings_change_logo
import squadfy_app.feature.club.presentation.generated.resources.settings_code_copied
import squadfy_app.feature.club.presentation.generated.resources.settings_copy_code
import squadfy_app.feature.club.presentation.generated.resources.settings_edit_club
import squadfy_app.feature.club.presentation.generated.resources.settings_invitation_code
import squadfy_app.feature.club.presentation.generated.resources.settings_leave
import squadfy_app.feature.club.presentation.generated.resources.settings_mute
import squadfy_app.feature.club.presentation.generated.resources.settings_mute_hint
import squadfy_app.feature.club.presentation.generated.resources.settings_leave_description
import squadfy_app.feature.club.presentation.generated.resources.settings_leave_title
import squadfy_app.feature.club.presentation.generated.resources.settings_owner_cannot_leave
import squadfy_app.feature.club.presentation.generated.resources.settings_regenerate_code
import squadfy_app.feature.club.presentation.generated.resources.settings_regenerate_description
import squadfy_app.feature.club.presentation.generated.resources.settings_regenerate_title
import squadfy_app.feature.club.presentation.generated.resources.settings_schedule
import squadfy_app.feature.club.presentation.generated.resources.settings_section_club
import squadfy_app.feature.club.presentation.generated.resources.settings_section_management
import squadfy_app.feature.club.presentation.generated.resources.settings_section_membership
import squadfy_app.feature.club.presentation.generated.resources.settings_share_code
import squadfy_app.feature.club.presentation.generated.resources.settings_share_text

/**
 * Settings tab of a club (spec 003, AC-003-10…14). [myMembership] decides what is shown (APP-RN-04);
 * the backend still answers 403 to anything not allowed.
 */
@Composable
fun ClubSettingsTab(
    club: ClubModel,
    myMembership: ClubMemberModel?,
    snackbarHostState: SnackbarHostState,
    onOpenBans: () -> Unit,
    onOpenSchedule: () -> Unit,
    onLeftClub: () -> Unit,
    extraSections: @Composable () -> Unit = {},
    viewModel: ClubSettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val shareText = rememberShareTextLauncher()
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val logoPicker = rememberImagePickerLauncher { picked ->
        viewModel.onAction(ClubSettingsAction.OnLogoPicked(bytes = picked.bytes, mimeType = picked.mimeType))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ClubSettingsEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            ClubSettingsEvent.LeftClub -> onLeftClub()
        }
    }

    val role = myMembership?.role
    val isManager = role != null && MemberPermissions.canManageClub(role)
    val canLeave = role != null && MemberPermissions.canLeave(role)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "code") {
            InvitationCodeCard(
                code = club.invitationCode,
                onCopy = {
                    clipboard.setText(AnnotatedString(club.invitationCode))
                    scope.launch { snackbarHostState.showSnackbar(getString(Res.string.settings_code_copied)) }
                },
                onShare = {
                    scope.launch { shareText(getString(Res.string.settings_share_text, club.name, club.invitationCode)) }
                }
            )
        }
        item(key = "club") {
            SettingsGroup(title = stringResource(Res.string.settings_section_club)) {
                SettingsRow(label = stringResource(Res.string.settings_schedule), icon = Icons.Outlined.CalendarMonth, onClick = onOpenSchedule)
                state.muted?.let { muted ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                    SettingsRow(
                        label = stringResource(Res.string.settings_mute),
                        icon = Icons.Outlined.NotificationsOff,
                        onClick = { viewModel.onAction(ClubSettingsAction.OnMutedChanged(!muted)) },
                        trailing = { Switch(checked = muted, onCheckedChange = { viewModel.onAction(ClubSettingsAction.OnMutedChanged(it)) }) }
                    )
                    Text(
                        text = stringResource(Res.string.settings_mute_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.extended.textPlaceholder,
                        modifier = Modifier.padding(start = 46.dp, end = 16.dp, bottom = 12.dp)
                    )
                }
            }
        }
        if (isManager) {
            item(key = "management") {
                SettingsGroup(title = stringResource(Res.string.settings_section_management)) {
                    SettingsRow(
                        label = stringResource(Res.string.settings_edit_club),
                        icon = Icons.Outlined.Edit,
                        onClick = { viewModel.onAction(ClubSettingsAction.OnEditClubClick(club)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                    SettingsRow(label = stringResource(Res.string.settings_change_logo), icon = Icons.Outlined.Image, onClick = { logoPicker.launch() })
                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                    SettingsRow(
                        label = stringResource(Res.string.settings_regenerate_code),
                        icon = Icons.Outlined.Refresh,
                        onClick = { viewModel.onAction(ClubSettingsAction.OnRegenerateCodeClick) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                    SettingsRow(label = stringResource(Res.string.settings_bans), icon = Icons.Outlined.Block, onClick = onOpenBans)
                }
            }
        }
        item(key = "extra") { extraSections() }
        if (role != null) {
            item(key = "membership") {
                SettingsGroup(title = stringResource(Res.string.settings_section_membership)) {
                    if (canLeave) {
                        SettingsRow(
                            label = stringResource(Res.string.settings_leave),
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            destructive = true,
                            onClick = { viewModel.onAction(ClubSettingsAction.OnLeaveClick) }
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.settings_owner_cannot_leave),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.extended.textPlaceholder,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    when (state.dialog) {
        SettingsDialog.EDIT_CLUB -> EditClubDialog(state = state, onAction = viewModel::onAction)
        SettingsDialog.REGENERATE_CODE -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.settings_regenerate_title),
            description = stringResource(Res.string.settings_regenerate_description),
            confirmButtonText = stringResource(Res.string.settings_regenerate_code),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { viewModel.onAction(ClubSettingsAction.OnConfirmRegenerateCode) },
            onCancelClick = { viewModel.onAction(ClubSettingsAction.OnDismissDialog) },
            onDismiss = { viewModel.onAction(ClubSettingsAction.OnDismissDialog) }
        )
        SettingsDialog.LEAVE -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.settings_leave_title),
            description = stringResource(Res.string.settings_leave_description),
            confirmButtonText = stringResource(Res.string.settings_leave),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { viewModel.onAction(ClubSettingsAction.OnConfirmLeave) },
            onCancelClick = { viewModel.onAction(ClubSettingsAction.OnDismissDialog) },
            onDismiss = { viewModel.onAction(ClubSettingsAction.OnDismissDialog) }
        )
        null -> Unit
    }
}

@Composable
private fun InvitationCodeCard(code: String, onCopy: () -> Unit, onShare: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(Res.string.settings_invitation_code),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )
            Text(
                text = code,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 3.sp),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SquadfyButton(
                    text = stringResource(Res.string.settings_copy_code),
                    onClick = onCopy,
                    style = com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.SECONDARY,
                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                SquadfyButton(
                    text = stringResource(Res.string.settings_share_code),
                    onClick = onShare,
                    leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EditClubDialog(state: ClubSettingsState, onAction: (ClubSettingsAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(ClubSettingsAction.OnDismissDialog) },
        title = { Text(text = stringResource(Res.string.edit_club_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SquadfyTextField(state = state.editName, title = stringResource(Res.string.edit_club_name), singleLine = true)
                SquadfyTextField(state = state.editDescription, title = stringResource(Res.string.edit_club_description))
                SquadfyTextField(
                    state = state.editMaxMembers,
                    title = stringResource(Res.string.edit_club_max_members),
                    singleLine = true,
                    keyboardType = KeyboardType.Number
                )
                state.editError?.let { error ->
                    Text(text = error.asString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(ClubSettingsAction.OnSaveClub) }, enabled = !state.isWorking) {
                Text(text = stringResource(Res.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(ClubSettingsAction.OnDismissDialog) }) {
                Text(text = stringResource(Res.string.common_cancel))
            }
        }
    )
}

@Composable
internal fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPlaceholder,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column { content() }
        }
    }
}

@Composable
internal fun SettingsRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    val contentColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.extended.textPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.extended.textPlaceholder
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = contentColor, modifier = Modifier.weight(1f))
        trailing?.invoke() ?: Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.extended.textPlaceholder
        )
    }
}
