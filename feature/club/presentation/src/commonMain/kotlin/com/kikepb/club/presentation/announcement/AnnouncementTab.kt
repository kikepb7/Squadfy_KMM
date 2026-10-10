package com.kikepb.club.presentation.announcement

import kotlin.time.Clock
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.style.TextOverflow
import com.kikepb.club.presentation.mapper.matchFormatLabel
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.triangulacion.components.Countdown
import com.kikepb.core.designsystem.triangulacion.components.Plazas
import com.kikepb.core.designsystem.triangulacion.components.PosChip
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.components.TriSectionTitle
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.components.rememberCountdown
import com.kikepb.core.designsystem.triangulacion.format.TriDates
import com.kikepb.core.designsystem.triangulacion.format.formatClubTime
import com.kikepb.core.designsystem.triangulacion.format.formatLongDayCaps
import com.kikepb.core.designsystem.triangulacion.format.formatMatchDate
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import squadfy_app.core.designsystem.generated.resources.Res as DesignRes
import squadfy_app.core.designsystem.generated.resources.tri_closes_in
import squadfy_app.feature.club.presentation.generated.resources.announcement_absence_body
import squadfy_app.feature.club.presentation.generated.resources.announcement_enrolled_message
import squadfy_app.feature.club.presentation.generated.resources.announcement_opens_at
import squadfy_app.feature.club.presentation.generated.resources.announcement_confirmed_count_title
import squadfy_app.feature.club.presentation.generated.resources.announcement_opens_in_caps
import squadfy_app.feature.club.presentation.generated.resources.announcement_stale_title
import squadfy_app.feature.club.presentation.generated.resources.announcement_state_waitlisted
import squadfy_app.feature.club.presentation.generated.resources.announcement_teams_line
import squadfy_app.feature.club.presentation.generated.resources.announcement_waitlist_count_title
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
            contentPadding = PaddingValues(horizontal = Space.s4, vertical = Space.s4),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val current = state.current
            if (state.isStale && state.lastUpdatedAt != null) {
                item(key = "stale") {
                    TriNotice(
                        title = stringResource(Res.string.announcement_stale_title),
                        body = stringResource(Res.string.announcement_offline, formatDateTime(state.lastUpdatedAt, state.timeZoneId)),
                        tone = TriNoticeTone.Warn,
                        icon = TriIcon.Alert
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
                item(key = "match-card") { MatchCard(state = state, onAction = onAction, onOpenMatch = onOpenMatch) }
                item(key = "confirmed-title") {
                    TriSectionTitle(
                        text = stringResource(Res.string.announcement_confirmed_count_title, state.confirmed.size),
                        modifier = Modifier.padding(top = Space.s4)
                    )
                }
                if (state.confirmed.isEmpty()) {
                    item(key = "confirmed-empty") { Hint(text = stringResource(Res.string.announcement_empty_list)) }
                }
                itemsIndexed(items = state.confirmed, key = { _, entry -> "c-${entry.id}" }) { index, entry ->
                    EntryRow(entry = entry, state = state, index = index + 1, onRemove = { onAction(AnnouncementAction.OnRemoveGuest(entry)) })
                }
                if (state.waitlist.isNotEmpty()) {
                    item(key = "waitlist-title") {
                        TriSectionTitle(
                            text = stringResource(Res.string.announcement_waitlist_count_title, state.waitlist.size),
                            modifier = Modifier.padding(top = Space.s4)
                        )
                    }
                    itemsIndexed(items = state.waitlist, key = { _, entry -> "w-${entry.id}" }) { index, entry ->
                        EntryRow(entry = entry, state = state, index = index + 1, onRemove = { onAction(AnnouncementAction.OnRemoveGuest(entry)) })
                    }
                }
            }
            if (state.isManager && !state.isStale && state.hasLoaded) item(key = "manager-actions") { managerActions() }
            if (onOpenMatch != null && state.upcoming.isNotEmpty()) {
                item(key = "upcoming-title") { TriSectionTitle(text = stringResource(Res.string.announcement_upcoming_title), modifier = Modifier.padding(top = Space.s4)) }
                items(items = state.upcoming, key = { "u-${it.id}" }) { next ->
                    LinkRow(
                        text = stringResource(Res.string.announcement_upcoming_item, formatDateTime(next.closesAt, state.timeZoneId), next.confirmedCount, next.maxPlayers),
                        onClick = { onOpenMatch(next.matchId) }
                    )
                }
            }
            if (onOpenMatch != null && state.past.isNotEmpty()) {
                item(key = "history-title") { TriSectionTitle(text = stringResource(Res.string.announcement_history_title), modifier = Modifier.padding(top = Space.s4)) }
                items(items = state.past, key = { "h-${it.id}" }) { past ->
                    val closedAt = formatDateTime(past.closesAt, state.timeZoneId)
                    LinkRow(
                        text = if (past.status == AnnouncementStatus.CANCELLED) stringResource(Res.string.announcement_history_cancelled, closedAt)
                        else stringResource(Res.string.announcement_history_item, closedAt, past.confirmedCount),
                        onClick = { onOpenMatch(past.matchId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoMatchCard(isManager: Boolean, onOpenSchedule: () -> Unit) {
    TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s5) {
        Text(text = stringResource(Res.string.announcement_none), style = Triangulacion.type.displayMd, color = Triangulacion.colors.ink)
        Spacer(Modifier.height(6.dp))
        Hint(text = stringResource(Res.string.announcement_none_hint))
        if (isManager) {
            Spacer(Modifier.height(Space.s4))
            TriButton(text = stringResource(Res.string.announcement_configure_schedule), onClick = onOpenSchedule, variant = TriButtonVariant.Secondary, icon = TriIcon.Calendar)
        }
    }
}

/**
 * Partido A (spec 018): date line, draw time and format, the places, the countdown and the enrollment state
 * (free, called up, waitlist, closed, not open yet, cancelled), each in one truthful line.
 */
@Composable
private fun MatchCard(state: AnnouncementState, onAction: (AnnouncementAction) -> Unit, onOpenMatch: ((String) -> Unit)?) {
    val current = state.current ?: return
    val announcement = current.announcement
    val colors = Triangulacion.colors
    val local = TriDates.local(current.matchScheduledAt, state.timeZoneId)
    val isOpen = state.windowState == WindowState.OPEN && !state.isStale
    val isActing = AnnouncementOperation.ENROLLMENT in state.acting
    TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s5) {
        TriCaps(text = "${formatLongDayCaps(local.date)} · ${formatClubTime(current.matchScheduledAt, state.timeZoneId)}", color = colors.ink)
        Text(
            text = if (state.windowState != WindowState.CANCELLED) {
                stringResource(Res.string.announcement_teams_line, formatMatchDate(announcement.drawAt, state.timeZoneId), matchFormatLabel(announcement.maxPlayers))
            } else matchFormatLabel(announcement.maxPlayers),
            style = Triangulacion.type.bodySm,
            color = colors.inkMuted,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.height(Space.s4))
        Plazas(max = announcement.maxPlayers, confirmed = announcement.confirmedCount)
        if (state.windowState == WindowState.OPEN || state.windowState == WindowState.NOT_OPEN) {
            Spacer(Modifier.height(Space.s5))
            val target = if (state.windowState == WindowState.NOT_OPEN) announcement.opensAt else announcement.closesAt
            val countdown = rememberCountdown(target)
            Countdown(
                value = countdown,
                label = if (state.windowState == WindowState.NOT_OPEN) stringResource(Res.string.announcement_opens_in_caps) else stringResource(DesignRes.string.tri_closes_in),
                urgent = state.windowState == WindowState.OPEN && countdown.days == 0 && countdown.hours < 3
            )
        }
        if (state.hasAbsenceOnMatchDay) {
            Spacer(Modifier.height(Space.s4))
            TriNotice(title = stringResource(Res.string.announcement_absence_warning), body = stringResource(Res.string.announcement_absence_body), tone = TriNoticeTone.Warn)
        }
        Spacer(Modifier.height(Space.s5))
        when {
            current.myStatus == MyEnrollmentStatus.CONFIRMED -> {
                TriNotice(title = stringResource(Res.string.announcement_enrolled_message), tone = TriNoticeTone.Ok)
                if (isOpen) EnrollLink(stringResource(Res.string.announcement_withdraw), isActing) { onAction(AnnouncementAction.OnWithdrawClick) }
            }
            current.myStatus == MyEnrollmentStatus.WAITLISTED -> {
                TriNotice(title = stringResource(Res.string.announcement_state_waitlisted, current.myWaitlistPosition ?: 0), tone = TriNoticeTone.Warn)
                if (isOpen) EnrollLink(stringResource(Res.string.announcement_leave_waitlist), isActing) { onAction(AnnouncementAction.OnWithdrawClick) }
            }
            isOpen -> TriButton(
                text = stringResource(Res.string.announcement_enroll),
                onClick = { onAction(AnnouncementAction.OnEnrollClick) },
                variant = TriButtonVariant.Volt,
                icon = TriIcon.Plus,
                isLoading = isActing,
                large = true,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            state.windowState == WindowState.NOT_OPEN ->
                TriNotice(title = stringResource(Res.string.announcement_opens_at, formatMatchDate(announcement.opensAt, state.timeZoneId)), icon = TriIcon.Calendar)
            state.windowState == WindowState.CANCELLED ->
                TriNotice(title = stringResource(Res.string.announcement_cancelled), tone = TriNoticeTone.Danger)
            else -> TriNotice(title = stringResource(Res.string.announcement_closed), icon = TriIcon.Lock)
        }
        if (state.canAddGuest) {
            Spacer(Modifier.height(Space.s3))
            TriButton(
                text = stringResource(Res.string.guests_add),
                onClick = { onAction(AnnouncementAction.OnAddGuestClick) },
                variant = TriButtonVariant.Secondary,
                icon = TriIcon.User,
                enabled = AnnouncementOperation.GUESTS !in state.acting,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (onOpenMatch != null && state.windowState == WindowState.CLOSED) {
            Spacer(Modifier.height(Space.s2))
            TriTextButton(
                text = stringResource(Res.string.announcement_view_match),
                onClick = { onOpenMatch(announcement.matchId) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ColumnScope.EnrollLink(text: String, isActing: Boolean, onClick: () -> Unit) {
    Spacer(Modifier.height(Space.s2))
    TriTextButton(
        text = text,
        onClick = onClick,
        enabled = !isActing,
        color = Triangulacion.colors.ink,
        underline = true,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}

@Composable
private fun EntryRow(entry: AnnouncementEntry, state: AnnouncementState, index: Int?, onRemove: () -> Unit) {
    val colors = Triangulacion.colors
    val member = entry.clubMemberId?.let { state.members[it] }
    val formerMember = stringResource(Res.string.former_member)
    val name = if (entry.isGuest) {
        val host = entry.invitedByMemberId?.let { state.members[it]?.username } ?: formerMember
        stringResource(Res.string.announcement_guest_of, entry.guestName.orEmpty(), host)
    } else {
        member?.username ?: formerMember
    }
    val position = (if (entry.isGuest) entry.guestPosition else member?.position).toTri()
    val isMe = !entry.isGuest && entry.clubMemberId != null && entry.clubMemberId == state.me?.id
    TriCard(modifier = Modifier.fillMaxWidth(), highlighted = isMe, contentPadding = 0.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            index?.let { Text(text = it.toString(), style = Triangulacion.type.label.tabular, color = colors.inkMuted, modifier = Modifier.widthIn(min = 16.dp)) }
            TriAvatar(name = entry.guestName ?: member?.username.orEmpty(), imageUrl = member?.pictureUrl, size = 44.dp, isMe = isMe)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = Triangulacion.type.title, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                member?.shirtNumber?.let { Text(text = "#$it", style = Triangulacion.type.bodySm.tabular, color = colors.inkMuted) }
            }
            position?.let { PosChip(it) }
            if (state.canRemoveGuest(entry)) {
                TriRoundIconButton(icon = TriIcon.X, contentDescription = stringResource(Res.string.guests_remove), onClick = onRemove, filled = true)
            }
        }
    }
}

@Composable
private fun LinkRow(text: String, onClick: () -> Unit) {
    TriCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, contentPadding = 0.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = Space.s4, vertical = 14.dp)) {
            Text(text = text, style = Triangulacion.type.body, color = Triangulacion.colors.ink, modifier = Modifier.weight(1f))
            TriIconView(icon = TriIcon.ChevR, contentDescription = null, size = 20.dp, tint = Triangulacion.colors.inkMuted)
        }
    }
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
private fun Hint(text: String) {
    Text(text = text, style = Triangulacion.type.body, color = Triangulacion.colors.inkMuted)
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "Partido · abierta · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun AnnouncementOpenTizaPreview() = ScreenPreview { AnnouncementContent(state = AnnouncementState(now = Clock.System.now()), onAction = {}, onOpenSchedule = {}, onOpenMatch = {}) }

@Preview(name = "Partido · abierta · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun AnnouncementOpenNochePreview() = ScreenPreview(darkTheme = true) { AnnouncementContent(state = AnnouncementState(now = Clock.System.now()), onAction = {}, onOpenSchedule = {}, onOpenMatch = {}) }

