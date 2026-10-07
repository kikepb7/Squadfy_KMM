package com.kikepb.globalPosition.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.policy.WindowState
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.globalPosition.presentation.home.HomeAnnouncementStatus
import com.kikepb.globalPosition.presentation.home.HomeClubCardModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.globalposition.presentation.generated.resources.Res
import squadfy_app.feature.globalposition.presentation.generated.resources.home_enroll
import squadfy_app.feature.globalposition.presentation.generated.resources.home_free_seats
import squadfy_app.feature.globalposition.presentation.generated.resources.home_next_match
import squadfy_app.feature.globalposition.presentation.generated.resources.home_no_match
import squadfy_app.feature.globalposition.presentation.generated.resources.home_retry
import squadfy_app.feature.globalposition.presentation.generated.resources.home_status_confirmed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_status_not_enrolled
import squadfy_app.feature.globalposition.presentation.generated.resources.home_status_waitlisted
import squadfy_app.feature.globalposition.presentation.generated.resources.home_unavailable
import squadfy_app.feature.globalposition.presentation.generated.resources.home_window_cancelled
import squadfy_app.feature.globalposition.presentation.generated.resources.home_window_closed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_window_not_open
import squadfy_app.feature.globalposition.presentation.generated.resources.home_window_open
import kotlin.time.Instant

/** A club on Home with its next match, announcement state, my status and free spots (AC-010-01/02/06). */
@Composable
fun HomeClubCard(
    card: HomeClubCardModel,
    now: Instant,
    onClick: () -> Unit,
    onEnroll: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SquadfyAvatarPhoto(displayText = initials(card.club.name), imageUrl = card.club.clubLogoUrl, size = AvatarSize.LARGE)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = card.club.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.extended.textPrimary,
                        maxLines = 1
                    )
                    AnnouncementSummary(card = card, now = now, onRetry = onRetry)
                }
            }
            if (card.canEnroll(now)) {
                SquadfyButton(text = stringResource(Res.string.home_enroll), onClick = onEnroll, isLoading = card.isEnrolling, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun AnnouncementSummary(card: HomeClubCardModel, now: Instant, onRetry: () -> Unit) {
    when (val status = card.status) {
        HomeAnnouncementStatus.Loading -> Hint(text = "…")
        HomeAnnouncementStatus.NoMatch -> Hint(text = stringResource(Res.string.home_no_match))
        HomeAnnouncementStatus.Unavailable -> Row(verticalAlignment = Alignment.CenterVertically) {
            Hint(text = stringResource(Res.string.home_unavailable))
            TextButton(onClick = onRetry) { Text(text = stringResource(Res.string.home_retry)) }
        }
        is HomeAnnouncementStatus.Loaded -> {
            val current = status.current
            Text(
                text = stringResource(Res.string.home_next_match, formatMatchTime(current.matchScheduledAt, status.timeZoneId)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
            val window = when (card.windowState(now)) {
                WindowState.OPEN -> stringResource(Res.string.home_window_open)
                WindowState.NOT_OPEN -> stringResource(Res.string.home_window_not_open)
                WindowState.CLOSED -> stringResource(Res.string.home_window_closed)
                WindowState.CANCELLED -> stringResource(Res.string.home_window_cancelled)
                null -> null
            }
            window?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (card.windowState(now) == WindowState.OPEN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                val mine = when (current.myStatus) {
                    MyEnrollmentStatus.CONFIRMED -> stringResource(Res.string.home_status_confirmed)
                    MyEnrollmentStatus.WAITLISTED -> stringResource(Res.string.home_status_waitlisted, current.myWaitlistPosition ?: 0)
                    MyEnrollmentStatus.NOT_ENROLLED -> stringResource(Res.string.home_status_not_enrolled)
                }
                AssistChip(onClick = {}, label = { Text(text = mine) })
                card.freeSeats?.let { Hint(text = stringResource(Res.string.home_free_seats, it)) }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPlaceholder)
}

/** APP-RN-03: "15/10 20:00" in the club time zone (device zone when the schedule is unknown). */
private fun formatMatchTime(instant: Instant, timeZoneId: String?): String {
    val zone = timeZoneId?.let { runCatching { TimeZone.of(it) }.getOrNull() } ?: TimeZone.currentSystemDefault()
    val local = instant.toLocalDateTime(zone)
    fun Int.two() = toString().padStart(2, '0')
    return "${local.day.two()}/${(local.month.ordinal + 1).two()} ${local.hour.two()}:${local.minute.two()}"
}

private fun initials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
