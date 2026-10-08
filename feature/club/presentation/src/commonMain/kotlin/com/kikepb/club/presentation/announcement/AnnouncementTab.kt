package com.kikepb.club.presentation.announcement

import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import com.kikepb.club.presentation.components.LoadErrorCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.AnnouncementEntry
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.policy.WindowState
import com.kikepb.club.presentation.components.PositionChips
import com.kikepb.club.presentation.extramatch.ExtraMatchButton
import com.kikepb.club.presentation.mapper.initialsOf
import com.kikepb.club.presentation.mapper.label
import com.kikepb.club.presentation.util.formatDateTime
import com.kikepb.club.presentation.util.formatDuration
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.announcement_absence_warning
import squadfy_app.feature.club.presentation.generated.resources.announcement_cancelled
import squadfy_app.feature.club.presentation.generated.resources.announcement_closed
import squadfy_app.feature.club.presentation.generated.resources.announcement_closes_in
import squadfy_app.feature.club.presentation.generated.resources.announcement_configure_schedule
import squadfy_app.feature.club.presentation.generated.resources.announcement_confirmed_badge
import squadfy_app.feature.club.presentation.generated.resources.announcement_confirmed_title
import squadfy_app.feature.club.presentation.generated.resources.announcement_draw_at
import squadfy_app.feature.club.presentation.generated.resources.announcement_empty_list
import squadfy_app.feature.club.presentation.generated.resources.announcement_enroll
import squadfy_app.feature.club.presentation.generated.resources.announcement_guest_of
import squadfy_app.feature.club.presentation.generated.resources.announcement_history_cancelled
import squadfy_app.feature.club.presentation.generated.resources.announcement_history_item
import squadfy_app.feature.club.presentation.generated.resources.announcement_history_title
import squadfy_app.feature.club.presentation.generated.resources.announcement_upcoming_item
import squadfy_app.feature.club.presentation.generated.resources.announcement_upcoming_title
import squadfy_app.feature.club.presentation.generated.resources.announcement_leave_waitlist
import squadfy_app.feature.club.presentation.generated.resources.announcement_match_at
import squadfy_app.feature.club.presentation.generated.resources.announcement_none
import squadfy_app.feature.club.presentation.generated.resources.announcement_none_hint
import squadfy_app.feature.club.presentation.generated.resources.announcement_offline
import squadfy_app.feature.club.presentation.generated.resources.announcement_opens_in
import squadfy_app.feature.club.presentation.generated.resources.announcement_seats
import squadfy_app.feature.club.presentation.generated.resources.announcement_view_match
import squadfy_app.feature.club.presentation.generated.resources.announcement_waitlist_count
import squadfy_app.feature.club.presentation.generated.resources.announcement_waitlist_title
import squadfy_app.feature.club.presentation.generated.resources.announcement_waitlisted_badge
import squadfy_app.feature.club.presentation.generated.resources.announcement_withdraw
import squadfy_app.feature.club.presentation.generated.resources.announcement_withdraw_description
import squadfy_app.feature.club.presentation.generated.resources.announcement_withdraw_title
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_save
import squadfy_app.feature.club.presentation.generated.resources.former_member
import squadfy_app.feature.club.presentation.generated.resources.guests_add
import squadfy_app.feature.club.presentation.generated.resources.guests_name
import squadfy_app.feature.club.presentation.generated.resources.guests_priority_hint
import squadfy_app.feature.club.presentation.generated.resources.guests_remove
import squadfy_app.feature.club.presentation.generated.resources.guests_title

@Composable
fun AnnouncementTab(
    snackbarHostState: SnackbarHostState,
    onOpenSchedule: () -> Unit,
    onNotMemberAnymore: () -> Unit,
    /** Opens the match detail (spec 006) from "view match" and the history; null hides those entries. */
    onOpenMatch: ((matchId: String) -> Unit)? = null,
    viewModel: AnnouncementViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AnnouncementEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            AnnouncementEvent.NotMemberAnymore -> onNotMemberAnymore()
        }
    }
    // AC-009-04: while visible, pushes of this club refresh the tab instead of showing a notification
    LifecycleStartEffect(Unit) {
        viewModel.onAction(AnnouncementAction.OnVisibilityChanged(true))
        onStopOrDispose { viewModel.onAction(AnnouncementAction.OnVisibilityChanged(false)) }
    }
    // APP-RN-08: refresh when the app comes back to the foreground
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(AnnouncementAction.OnResume)
        onPauseOrDispose { }
    }

    AnnouncementContent(
        state = state,
        onAction = viewModel::onAction,
        onOpenSchedule = onOpenSchedule,
        onOpenMatch = onOpenMatch,
        managerActions = {
            // AC-007-08: a new match becomes the current announcement, so the tab refreshes
            ExtraMatchButton(onCreated = { message ->
                scope.launch { snackbarHostState.showSnackbar(message.asStringAsync()) }
                viewModel.onAction(AnnouncementAction.OnRefresh)
            })
        }
    )
    AnnouncementDialogs(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementContent(
    state: AnnouncementState,
    onAction: (AnnouncementAction) -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenMatch: ((matchId: String) -> Unit)?,
    managerActions: @Composable () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(AnnouncementAction.OnRefresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val current = state.current
            if (state.isStale && state.lastUpdatedAt != null) {
                item(key = "stale") {
                    Text(
                        text = stringResource(Res.string.announcement_offline, formatDateTime(state.lastUpdatedAt, state.timeZoneId)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (current == null) {
                when {
                    state.hasLoaded -> item(key = "none") { NoMatchCard(isManager = state.isManager, onOpenSchedule = onOpenSchedule) }
                    state.loadFailed -> item(key = "error") { LoadErrorCard(onRetry = { onAction(AnnouncementAction.OnRefresh) }) }
                    else -> item(key = "loading") { SquadfyLoadingIndicator() }
                }
            } else {
                item(key = "header") { HeaderCard(state = state, onOpenMatch = onOpenMatch) }
                item(key = "actions") { PrimaryActions(state = state, onAction = onAction) }
                item(key = "confirmed-title") { SectionTitle(text = stringResource(Res.string.announcement_confirmed_title)) }
                if (state.confirmed.isEmpty()) {
                    item(key = "confirmed-empty") { Hint(text = stringResource(Res.string.announcement_empty_list)) }
                }
                items(items = state.confirmed, key = { "c-${it.id}" }) { entry ->
                    EntryRow(entry = entry, state = state, index = null, onRemove = { onAction(AnnouncementAction.OnRemoveGuest(entry)) })
                }
                if (state.waitlist.isNotEmpty()) {
                    item(key = "waitlist-title") { SectionTitle(text = stringResource(Res.string.announcement_waitlist_title)) }
                    items(items = state.waitlist, key = { "w-${it.id}" }) { entry ->
                        EntryRow(
                            entry = entry,
                            state = state,
                            index = state.waitlist.indexOf(entry) + 1,
                            onRemove = { onAction(AnnouncementAction.OnRemoveGuest(entry)) }
                        )
                    }
                }
            }
            if (state.isManager && !state.isStale && state.hasLoaded) item(key = "manager-actions") { managerActions() }
            if (onOpenMatch != null && state.upcoming.isNotEmpty()) {
                item(key = "upcoming-title") { SectionTitle(text = stringResource(Res.string.announcement_upcoming_title)) }
                items(items = state.upcoming, key = { "u-${it.id}" }) { next ->
                    TextButton(onClick = { onOpenMatch(next.matchId) }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(Res.string.announcement_upcoming_item, formatDateTime(next.closesAt, state.timeZoneId), next.confirmedCount, next.maxPlayers),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.extended.textSecondary
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                }
            }
            if (onOpenMatch != null && state.past.isNotEmpty()) {
                item(key = "history-title") { SectionTitle(text = stringResource(Res.string.announcement_history_title)) }
                items(items = state.past, key = { "h-${it.id}" }) { past ->
                    HistoryRow(announcement = past, timeZoneId = state.timeZoneId, onClick = { onOpenMatch(past.matchId) })
                }
            }
        }
    }
}

@Composable
private fun NoMatchCard(isManager: Boolean, onOpenSchedule: () -> Unit) {
    Card {
        Text(
            text = stringResource(Res.string.announcement_none),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        Hint(text = stringResource(Res.string.announcement_none_hint))
        if (isManager) {
            SquadfyButton(text = stringResource(Res.string.announcement_configure_schedule), onClick = onOpenSchedule, style = SquadfyButtonStyle.SECONDARY)
        }
    }
}

@Composable
private fun HeaderCard(state: AnnouncementState, onOpenMatch: ((String) -> Unit)?) {
    val current = state.current ?: return
    val announcement = current.announcement
    Card {
        Text(
            text = stringResource(Res.string.announcement_match_at, formatDateTime(current.matchScheduledAt, state.timeZoneId)),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        Text(
            text = stringResource(Res.string.announcement_seats, announcement.confirmedCount, announcement.maxPlayers) +
                if (announcement.waitlistCount > 0) " · " + stringResource(Res.string.announcement_waitlist_count, announcement.waitlistCount) else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary
        )
        val remaining = state.timeToNextChange
        val windowText = when (state.windowState) {
            WindowState.NOT_OPEN -> remaining?.let { stringResource(Res.string.announcement_opens_in, formatDuration(it)) }
            WindowState.OPEN -> remaining?.let { stringResource(Res.string.announcement_closes_in, formatDuration(it)) }
            WindowState.CLOSED -> stringResource(Res.string.announcement_closed)
            WindowState.CANCELLED -> stringResource(Res.string.announcement_cancelled)
            null -> null
        }
        windowText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                color = if (state.windowState == WindowState.OPEN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.extended.textPlaceholder
            )
        }
        if (state.hasAbsenceOnMatchDay) {
            Text(
                text = stringResource(Res.string.announcement_absence_warning),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (announcement.drawAt != announcement.closesAt && state.windowState != WindowState.CANCELLED) {
            Hint(text = stringResource(Res.string.announcement_draw_at, formatDateTime(announcement.drawAt, state.timeZoneId)))
        }
        if (onOpenMatch != null && state.windowState == WindowState.CLOSED) {
            OutlinedButton(onClick = { onOpenMatch(announcement.matchId) }) { Text(text = stringResource(Res.string.announcement_view_match)) }
        }
    }
}

@Composable
private fun PrimaryActions(state: AnnouncementState, onAction: (AnnouncementAction) -> Unit) {
    val current = state.current ?: return
    val isOpen = state.windowState == WindowState.OPEN && !state.isStale
    val isActing = AnnouncementOperation.ENROLLMENT in state.acting
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (current.myStatus) {
            MyEnrollmentStatus.CONFIRMED -> AssistChip(onClick = {}, label = { Text(text = stringResource(Res.string.announcement_confirmed_badge)) })
            MyEnrollmentStatus.WAITLISTED -> AssistChip(
                onClick = {},
                label = { Text(text = stringResource(Res.string.announcement_waitlisted_badge, current.myWaitlistPosition ?: 0)) }
            )
            MyEnrollmentStatus.NOT_ENROLLED -> Unit
        }
        if (isOpen) {
            when (current.myStatus) {
                MyEnrollmentStatus.NOT_ENROLLED -> SquadfyButton(
                    text = stringResource(Res.string.announcement_enroll),
                    onClick = { onAction(AnnouncementAction.OnEnrollClick) },
                    isLoading = isActing,
                    modifier = Modifier.fillMaxWidth()
                )
                MyEnrollmentStatus.CONFIRMED -> SquadfyButton(
                    text = stringResource(Res.string.announcement_withdraw),
                    onClick = { onAction(AnnouncementAction.OnWithdrawClick) },
                    style = SquadfyButtonStyle.DESTRUCTIVE_SECONDARY,
                    isLoading = isActing,
                    modifier = Modifier.fillMaxWidth()
                )
                MyEnrollmentStatus.WAITLISTED -> SquadfyButton(
                    text = stringResource(Res.string.announcement_leave_waitlist),
                    onClick = { onAction(AnnouncementAction.OnWithdrawClick) },
                    style = SquadfyButtonStyle.SECONDARY,
                    isLoading = isActing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (state.canAddGuest) {
            OutlinedButton(
                onClick = { onAction(AnnouncementAction.OnAddGuestClick) },
                enabled = AnnouncementOperation.GUESTS !in state.acting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = stringResource(Res.string.guests_add), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun EntryRow(entry: AnnouncementEntry, state: AnnouncementState, index: Int?, onRemove: () -> Unit) {
    val member = entry.clubMemberId?.let { state.members[it] }
    val formerMember = stringResource(Res.string.former_member)
    val name = if (entry.isGuest) {
        val host = entry.invitedByMemberId?.let { state.members[it]?.username } ?: formerMember
        stringResource(Res.string.announcement_guest_of, entry.guestName.orEmpty(), host)
    } else {
        member?.username ?: formerMember
    }
    val position = if (entry.isGuest) entry.guestPosition else member?.position
    val isMe = !entry.isGuest && entry.clubMemberId != null && entry.clubMemberId == state.me?.id
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        shadowElevation = if (isMe) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            index?.let { Text(text = "$it.", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.extended.textPlaceholder) }
            SquadfyAvatarPhoto(displayText = initialsOf(entry.guestName ?: member?.username.orEmpty()), imageUrl = member?.pictureUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                val details = listOfNotNull(member?.shirtNumber?.let { "#$it" }, position?.let { stringResource(it.label) }).joinToString(" · ")
                if (details.isNotBlank()) Hint(text = details)
            }
            if (state.canRemoveGuest(entry)) {
                IconButton(onClick = onRemove) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(Res.string.guests_remove))
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(announcement: MatchAnnouncementModel, timeZoneId: String, onClick: () -> Unit) {
    val closedAt = formatDateTime(announcement.closesAt, timeZoneId)
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (announcement.status == AnnouncementStatus.CANCELLED) {
                stringResource(Res.string.announcement_history_cancelled, closedAt)
            } else {
                stringResource(Res.string.announcement_history_item, closedAt, announcement.confirmedCount)
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
}

@Composable
private fun AnnouncementDialogs(state: AnnouncementState, onAction: (AnnouncementAction) -> Unit) {
    val dismiss = { onAction(AnnouncementAction.OnDismissDialog) }
    when (val dialog = state.dialog) {
        AnnouncementDialog.ConfirmWithdraw -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.announcement_withdraw_title),
            description = stringResource(Res.string.announcement_withdraw_description),
            confirmButtonText = stringResource(Res.string.announcement_withdraw),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(AnnouncementAction.OnConfirmWithdraw) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        is AnnouncementDialog.AddGuest -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(text = stringResource(Res.string.guests_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Hint(text = stringResource(Res.string.guests_priority_hint))
                    SquadfyTextField(state = dialog.name, title = stringResource(Res.string.guests_name), singleLine = true)
                    PositionChips(selected = dialog.position, onSelected = { onAction(AnnouncementAction.OnGuestPositionSelected(it)) })
                }
            },
            confirmButton = {
                TextButton(onClick = { onAction(AnnouncementAction.OnConfirmAddGuest) }, enabled = dialog.name.text.isNotBlank()) {
                    Text(text = stringResource(Res.string.common_save))
                }
            },
            dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
        )
        null -> Unit
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.extended.textPrimary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun Hint(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPlaceholder)
}
