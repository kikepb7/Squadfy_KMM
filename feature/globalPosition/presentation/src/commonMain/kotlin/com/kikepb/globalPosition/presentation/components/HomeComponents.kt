package com.kikepb.globalPosition.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.kikepb.core.designsystem.triangulacion.components.CutLg
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.policy.WindowState
import com.kikepb.core.designsystem.triangulacion.components.ClubCrest
import com.kikepb.core.designsystem.triangulacion.components.Countdown
import com.kikepb.core.designsystem.triangulacion.components.FormGuide
import com.kikepb.core.designsystem.triangulacion.components.MatchResult
import com.kikepb.core.designsystem.triangulacion.components.MatchTicket
import com.kikepb.core.designsystem.triangulacion.components.PlacesCompact
import com.kikepb.core.designsystem.triangulacion.components.RatingDelta
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriTag
import com.kikepb.core.designsystem.triangulacion.components.TriTagTone
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.components.rememberCountdown
import com.kikepb.core.designsystem.triangulacion.format.TriDates
import com.kikepb.core.designsystem.triangulacion.format.formatClubTime
import com.kikepb.core.designsystem.triangulacion.format.formatDayMonth
import com.kikepb.core.designsystem.triangulacion.format.formatMatchDate
import com.kikepb.core.designsystem.triangulacion.theme.CrestTone
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import com.kikepb.globalPosition.presentation.home.HomeAnnouncementStatus
import com.kikepb.globalPosition.presentation.home.HomeClubCardModel
import com.kikepb.globalPosition.presentation.home.HomeLastMatchday
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.globalposition.presentation.generated.resources.Res
import squadfy_app.feature.globalposition.presentation.generated.resources.home_enroll
import squadfy_app.feature.globalposition.presentation.generated.resources.home_format_players
import squadfy_app.feature.globalposition.presentation.generated.resources.home_format_spots
import squadfy_app.feature.globalposition.presentation.generated.resources.home_no_match
import squadfy_app.feature.globalposition.presentation.generated.resources.home_result_draw
import squadfy_app.feature.globalposition.presentation.generated.resources.home_result_loss
import squadfy_app.feature.globalposition.presentation.generated.resources.home_result_win
import squadfy_app.feature.globalposition.presentation.generated.resources.home_retry
import squadfy_app.feature.globalposition.presentation.generated.resources.home_state_cancelled
import squadfy_app.feature.globalposition.presentation.generated.resources.home_state_closed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_state_confirmed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_state_opens
import squadfy_app.feature.globalposition.presentation.generated.resources.home_state_waitlisted
import squadfy_app.feature.globalposition.presentation.generated.resources.home_tag_cancelled
import squadfy_app.feature.globalposition.presentation.generated.resources.home_tag_closed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_tag_confirmed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_tag_not_open
import squadfy_app.feature.globalposition.presentation.generated.resources.home_tag_open
import squadfy_app.feature.globalposition.presentation.generated.resources.home_tag_waitlisted
import squadfy_app.feature.globalposition.presentation.generated.resources.home_ticket_detail
import squadfy_app.feature.globalposition.presentation.generated.resources.home_unavailable
import kotlin.time.Instant

/** «Fútbol 7» for the usual formats (10/14/22 places), otherwise «n plazas». */
@Composable
fun formatLabel(maxPlayers: Int): String =
    if (maxPlayers in setOf(10, 14, 22)) stringResource(Res.string.home_format_players, maxPlayers / 2)
    else stringResource(Res.string.home_format_spots, maxPlayers)

/**
 * Next match of Home (spec 018, Inicio A): the ticket with places and countdown, and below it the one action that
 * matters (Volt «Apuntarme») or my state in one line.
 */
@Composable
fun NextMatchBlock(
    card: HomeClubCardModel,
    now: Instant,
    onOpenClub: () -> Unit,
    onEnroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loaded = card.status as? HomeAnnouncementStatus.Loaded ?: return
    val current = loaded.current
    val announcement = current.announcement
    val window = card.windowState(now)
    val target = if (window == WindowState.NOT_OPEN) announcement.opensAt else announcement.closesAt
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        run {
            MatchTicket(
                modifier = Modifier.fillMaxWidth().clip(CutLg).clickable(role = Role.Button, onClick = onOpenClub),
                whenText = formatMatchDate(current.matchScheduledAt, loaded.timeZoneId),
                detail = stringResource(
                    Res.string.home_ticket_detail,
                    formatClubTime(current.matchScheduledAt, loaded.timeZoneId),
                    formatLabel(announcement.maxPlayers)
                ),
                clubName = card.club.name,
                crest = { ClubCrest(name = card.club.name, size = 24.dp, tone = CrestTone.forKey(card.club.id), logoUrl = card.club.clubLogoUrl) },
                notchColor = Triangulacion.colors.surface100
            ) {
                PlacesCompact(max = announcement.maxPlayers, confirmed = announcement.confirmedCount, modifier = Modifier.weight(1f))
                if (window == WindowState.OPEN || window == WindowState.NOT_OPEN) {
                    Countdown(value = rememberCountdown(target), inverse = true, label = null, cellWidth = 44.dp)
                }
            }
        }
        when {
            card.canEnroll(now) -> TriButton(
                text = stringResource(Res.string.home_enroll),
                onClick = onEnroll,
                variant = TriButtonVariant.Volt,
                icon = TriIcon.Plus,
                isLoading = card.isEnrolling,
                large = true,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            current.myStatus == MyEnrollmentStatus.CONFIRMED ->
                TriNotice(title = stringResource(Res.string.home_state_confirmed), tone = TriNoticeTone.Ok)
            current.myStatus == MyEnrollmentStatus.WAITLISTED ->
                TriNotice(title = stringResource(Res.string.home_state_waitlisted, current.myWaitlistPosition ?: 0), tone = TriNoticeTone.Warn)
            window == WindowState.NOT_OPEN ->
                TriNotice(title = stringResource(Res.string.home_state_opens, formatMatchDate(announcement.opensAt, loaded.timeZoneId)), icon = TriIcon.Calendar)
            window == WindowState.CANCELLED ->
                TriNotice(title = stringResource(Res.string.home_state_cancelled), tone = TriNoticeTone.Danger)
            else -> TriNotice(title = stringResource(Res.string.home_state_closed), icon = TriIcon.Lock)
        }
    }
}

/** A club in «Tus clubs»: crest, name, next match and my state in a tag. */
@Composable
fun HomeClubRow(
    card: HomeClubCardModel,
    now: Instant,
    onClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Triangulacion.colors
    TriCard(modifier = modifier.fillMaxWidth(), onClick = onClick, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ClubCrest(name = card.club.name, size = 40.dp, tone = CrestTone.forKey(card.club.id), logoUrl = card.club.clubLogoUrl)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = card.club.name, style = Triangulacion.type.title, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val subtitle = when (val status = card.status) {
                    HomeAnnouncementStatus.Loading -> null
                    HomeAnnouncementStatus.NoMatch -> stringResource(Res.string.home_no_match)
                    HomeAnnouncementStatus.Unavailable -> stringResource(Res.string.home_unavailable)
                    is HomeAnnouncementStatus.Loaded -> formatMatchDate(status.current.matchScheduledAt, status.timeZoneId)
                }
                if (subtitle != null) Text(text = subtitle, style = Triangulacion.type.bodySm, color = colors.inkMuted)
            }
            if (card.status == HomeAnnouncementStatus.Unavailable) {
                TriTextButton(text = stringResource(Res.string.home_retry), onClick = onRetry)
            } else {
                ClubStateTag(card, now)
            }
        }
    }
}

@Composable
private fun ClubStateTag(card: HomeClubCardModel, now: Instant) {
    val current = card.current ?: return
    when {
        current.myStatus == MyEnrollmentStatus.CONFIRMED -> TriTag(text = stringResource(Res.string.home_tag_confirmed))
        current.myStatus == MyEnrollmentStatus.WAITLISTED ->
            TriTag(text = stringResource(Res.string.home_tag_waitlisted, current.myWaitlistPosition ?: 0), tone = TriTagTone.Warn)
        else -> when (card.windowState(now)) {
            WindowState.OPEN -> TriTag(text = stringResource(Res.string.home_tag_open), tone = TriTagTone.Brand)
            WindowState.NOT_OPEN -> TriTag(text = stringResource(Res.string.home_tag_not_open), tone = TriTagTone.Neutral)
            WindowState.CLOSED -> TriTag(text = stringResource(Res.string.home_tag_closed), tone = TriTagTone.Neutral)
            WindowState.CANCELLED -> TriTag(text = stringResource(Res.string.home_tag_cancelled), tone = TriTagTone.Danger)
            null -> Unit
        }
    }
}

/** «Última jornada»: date and club, my score, the result word, my rating change and my form. */
@Composable
fun LastMatchdayCard(matchday: HomeLastMatchday, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    val local = TriDates.local(matchday.scheduledAt, matchday.timeZoneId)
    TriCard(modifier = modifier.fillMaxWidth(), onClick = onClick, contentPadding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriCaps(
                text = "${stringResource(TriDates.shortDay(local.dayOfWeek))} ${formatDayMonth(local.date)} · ${matchday.clubName}",
                modifier = Modifier.weight(1f)
            )
            matchday.ratingChange?.let { RatingDelta(it) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "${matchday.myScore} · ${matchday.theirScore}",
                style = Triangulacion.type.displayLg.tabular.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.ink
            )
            ResultTag(matchday.result, modifier = Modifier.padding(start = 12.dp))
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                FormGuide(results = matchday.form, size = 26.dp)
            }
        }
    }
}

@Composable
fun ResultTag(result: MatchResult, modifier: Modifier = Modifier) {
    when (result) {
        MatchResult.WIN -> TriTag(text = stringResource(Res.string.home_result_win), modifier = modifier)
        MatchResult.DRAW -> TriTag(text = stringResource(Res.string.home_result_draw), tone = TriTagTone.Neutral, modifier = modifier)
        MatchResult.LOSS -> TriTag(text = stringResource(Res.string.home_result_loss), tone = TriTagTone.Danger, modifier = modifier)
    }
}
