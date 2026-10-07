package com.kikepb.club.presentation.memberdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.presentation.components.PositionChips
import com.kikepb.club.presentation.mapper.initialsOf
import com.kikepb.club.presentation.mapper.label
import com.kikepb.club.presentation.settings.SettingsGroup
import com.kikepb.club.presentation.settings.SettingsRow
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_save
import squadfy_app.feature.club.presentation.generated.resources.member_ban
import squadfy_app.feature.club.presentation.generated.resources.member_ban_description
import squadfy_app.feature.club.presentation.generated.resources.member_ban_title
import squadfy_app.feature.club.presentation.generated.resources.member_change_role
import squadfy_app.feature.club.presentation.generated.resources.member_edit_mine
import squadfy_app.feature.club.presentation.generated.resources.member_edit_title
import squadfy_app.feature.club.presentation.generated.resources.member_not_found
import squadfy_app.feature.club.presentation.generated.resources.member_position
import squadfy_app.feature.club.presentation.generated.resources.member_remove
import squadfy_app.feature.club.presentation.generated.resources.member_remove_description
import squadfy_app.feature.club.presentation.generated.resources.member_remove_title
import squadfy_app.feature.club.presentation.generated.resources.member_role
import squadfy_app.feature.club.presentation.generated.resources.member_shirt
import squadfy_app.feature.club.presentation.generated.resources.member_title
import squadfy_app.feature.club.presentation.generated.resources.member_transfer
import squadfy_app.feature.club.presentation.generated.resources.member_transfer_description
import squadfy_app.feature.club.presentation.generated.resources.member_transfer_title
import squadfy_app.feature.club.presentation.generated.resources.settings_section_management

@Composable
fun MemberDetailRoot(
    onBackClick: () -> Unit,
    viewModel: MemberDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MemberDetailEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            MemberDetailEvent.MemberLeftClub -> onBackClick()
        }
    }

    MemberDetailScreen(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, snackbarHostState = snackbarHostState)
}

@Composable
fun MemberDetailScreen(
    state: MemberDetailState,
    onAction: (MemberDetailAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        topBar = { SquadfyTopBar(title = stringResource(Res.string.member_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        val member = state.member
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            member == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = stringResource(Res.string.member_not_found), color = MaterialTheme.colorScheme.extended.textPlaceholder)
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MemberHeader(member = member)
                MemberInfo(member = member)
                MemberActions(state = state, onAction = onAction)
            }
        }
    }

    MemberDialogs(state = state, onAction = onAction)
}

@Composable
private fun MemberHeader(member: ClubMemberModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SquadfyAvatarPhoto(displayText = initialsOf(member.username), imageUrl = member.profilePictureUrl, size = AvatarSize.LARGE)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = member.username,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
            Text(
                text = stringResource(member.role.label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MemberInfo(member: ClubMemberModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoRow(label = stringResource(Res.string.member_shirt), value = member.shirtNumber?.toString() ?: "-")
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            InfoRow(label = stringResource(Res.string.member_position), value = stringResource(member.position.label))
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            InfoRow(label = stringResource(Res.string.member_role), value = stringResource(member.role.label))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPlaceholder)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
    }
}

@Composable
private fun MemberActions(state: MemberDetailState, onAction: (MemberDetailAction) -> Unit) {
    if (state.isSelf) {
        SettingsGroup(title = stringResource(Res.string.member_edit_title)) {
            SettingsRow(
                label = stringResource(Res.string.member_edit_mine),
                icon = Icons.Outlined.Edit,
                onClick = { onAction(MemberDetailAction.OnShowDialog(MemberDialog.EDIT_MINE)) }
            )
        }
        return
    }
    val hasActions = state.assignableRoles.isNotEmpty() || state.canRemove || state.canBan || state.canTransfer
    if (!hasActions) return
    SettingsGroup(title = stringResource(Res.string.settings_section_management)) {
        if (state.assignableRoles.isNotEmpty()) {
            SettingsRow(
                label = stringResource(Res.string.member_change_role),
                icon = Icons.Outlined.ManageAccounts,
                onClick = { onAction(MemberDetailAction.OnShowDialog(MemberDialog.CHANGE_ROLE)) }
            )
        }
        if (state.canTransfer) {
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            SettingsRow(
                label = stringResource(Res.string.member_transfer),
                icon = Icons.Outlined.WorkspacePremium,
                onClick = { onAction(MemberDetailAction.OnShowDialog(MemberDialog.TRANSFER)) }
            )
        }
        if (state.canRemove) {
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            SettingsRow(
                label = stringResource(Res.string.member_remove),
                icon = Icons.Outlined.PersonRemove,
                destructive = true,
                onClick = { onAction(MemberDetailAction.OnShowDialog(MemberDialog.REMOVE)) }
            )
        }
        if (state.canBan) {
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            SettingsRow(
                label = stringResource(Res.string.member_ban),
                icon = Icons.Outlined.Block,
                destructive = true,
                onClick = { onAction(MemberDetailAction.OnShowDialog(MemberDialog.BAN)) }
            )
        }
    }
}

@Composable
private fun MemberDialogs(state: MemberDetailState, onAction: (MemberDetailAction) -> Unit) {
    val name = state.member?.username.orEmpty()
    val dismiss = { onAction(MemberDetailAction.OnDismissDialog) }
    when (state.dialog) {
        MemberDialog.CHANGE_ROLE -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(text = stringResource(Res.string.member_change_role)) },
            text = {
                Column {
                    state.assignableRoles.forEach { role ->
                        Text(
                            text = stringResource(role.label),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAction(MemberDetailAction.OnChangeRole(role)) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
        )
        MemberDialog.REMOVE -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.member_remove_title, name),
            description = stringResource(Res.string.member_remove_description),
            confirmButtonText = stringResource(Res.string.member_remove),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(MemberDetailAction.OnConfirmRemove) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        MemberDialog.BAN -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.member_ban_title, name),
            description = stringResource(Res.string.member_ban_description),
            confirmButtonText = stringResource(Res.string.member_ban),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(MemberDetailAction.OnConfirmBan) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        MemberDialog.TRANSFER -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.member_transfer_title, name),
            description = stringResource(Res.string.member_transfer_description),
            confirmButtonText = stringResource(Res.string.member_transfer),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(MemberDetailAction.OnConfirmTransfer) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        MemberDialog.EDIT_MINE -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(text = stringResource(Res.string.member_edit_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SquadfyTextField(
                        state = state.editShirtNumber,
                        title = stringResource(Res.string.member_shirt),
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        isError = state.editError != null,
                        supportingText = state.editError?.asString()
                    )
                    Text(text = stringResource(Res.string.member_position), style = MaterialTheme.typography.labelMedium)
                    PositionChips(
                        selected = state.editPosition,
                        onSelected = { onAction(MemberDetailAction.OnEditPositionSelected(it)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onAction(MemberDetailAction.OnSaveMyMembership) }, enabled = !state.isWorking) {
                    Text(text = stringResource(Res.string.common_save))
                }
            },
            dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
        )
        null -> Unit
    }
}
