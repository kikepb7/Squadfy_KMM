package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.tri_code_a11y
import squadfy_app.core.designsystem.generated.resources.tri_event_assist
import squadfy_app.core.designsystem.generated.resources.tri_event_goal
import squadfy_app.core.designsystem.generated.resources.tri_event_red
import squadfy_app.core.designsystem.generated.resources.tri_event_yellow
import squadfy_app.core.designsystem.generated.resources.tri_provisional_short
import squadfy_app.core.designsystem.generated.resources.tri_you
import squadfy_app.core.designsystem.generated.resources.tri_delta_down
import squadfy_app.core.designsystem.generated.resources.tri_delta_up
import squadfy_app.core.designsystem.generated.resources.tri_provisional
import squadfy_app.core.designsystem.generated.resources.tri_rank_a11y
import squadfy_app.core.designsystem.generated.resources.tri_number_a11y
import squadfy_app.core.designsystem.generated.resources.tri_rating_a11y
import squadfy_app.core.designsystem.generated.resources.tri_you_a11y
import squadfy_app.core.designsystem.generated.resources.tri_minute_a11y

/**
 * FilaRanking: position, avatar, name with «TÚ», shirt number and position, and the value with its delta (or unit).
 * The top three get a distinct position box (Volt, ink, outlined).
 * Accessibility: one sentence for screen readers («Puesto 1. Ana, tú. Dorsal 10. Delantero. 1040 de rating. Sube 12.»),
 * and with large fonts the value moves under the name so the name is never cut to a couple of letters.
 */
@Composable
fun RankingRow(
    position: Int,
    name: String,
    value: String,
    modifier: Modifier = Modifier,
    number: Int? = null,
    playerPosition: TriPosition? = null,
    delta: Int? = null,
    unit: String? = null,
    isMe: Boolean = false,
    provisional: Boolean = false,
    imageUrl: String? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = Triangulacion.colors
    val description = rankingDescription(position, name, value, number, playerPosition, delta, unit, isMe, provisional)
    val largeText = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
    TriCard(modifier = modifier.fillMaxWidth(), highlighted = isMe, contentPadding = 0.dp, onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .heightIn(min = 72.dp)
                .clearAndSetSemantics { contentDescription = description }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            RankPosition(position)
            if (!largeText) TriAvatar(name = name, size = 44.dp, isMe = isMe, imageUrl = imageUrl)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = name, style = Triangulacion.type.title, color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (isMe) Text(text = stringResource(Res.string.tri_you), style = Triangulacion.type.labelCaps, color = colors.accentText)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (number != null) Text(text = "#$number", style = Triangulacion.type.bodySm.tabular, color = colors.inkMuted)
                    if (playerPosition != null) PosChip(playerPosition)
                    if (provisional) TriTag(text = stringResource(Res.string.tri_provisional_short), tone = TriTagTone.Dashed)
                }
                if (largeText) RankingValue(value, delta, unit, Alignment.Start)
            }
            if (!largeText) RankingValue(value, delta, unit, Alignment.End)
        }
    }
}

/** From this font scale on, the ranking rows stack the value under the name. */
private const val LARGE_FONT_SCALE = 1.3f

@Composable
private fun RankingValue(value: String, delta: Int?, unit: String?, alignment: Alignment.Horizontal) {
    Column(horizontalAlignment = alignment, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = value, style = Triangulacion.type.displayMd.tabular.copy(fontWeight = FontWeight.Bold), color = Triangulacion.colors.ink)
        when {
            delta != null -> RatingDelta(delta)
            unit != null -> TriCaps(text = unit)
        }
    }
}

@Composable
private fun rankingDescription(
    position: Int,
    name: String,
    value: String,
    number: Int?,
    playerPosition: TriPosition?,
    delta: Int?,
    unit: String?,
    isMe: Boolean,
    provisional: Boolean
): String = buildList {
    add(stringResource(Res.string.tri_rank_a11y, position))
    add(if (isMe) stringResource(Res.string.tri_you_a11y, name) else name)
    if (number != null) add(stringResource(Res.string.tri_number_a11y, number))
    if (playerPosition != null) add(positionLongName(playerPosition))
    add(if (unit != null) "$value ${unit.lowercase()}" else stringResource(Res.string.tri_rating_a11y, value))
    val change = delta ?: 0
    if (change > 0) add(stringResource(Res.string.tri_delta_up, change))
    if (change < 0) add(stringResource(Res.string.tri_delta_down, -change))
    if (provisional) add(stringResource(Res.string.tri_provisional))
}.joinToString(". ")

@Composable
private fun RankPosition(position: Int) {
    val colors = Triangulacion.colors
    val shape = RoundedCornerShape(Radius.sm)
    val (background, content, border) = when (position) {
        1 -> Triple(colors.volt, colors.onVolt, null)
        2 -> Triple(colors.surfaceInverse, colors.onInverse, null)
        3 -> Triple(Color.Transparent, colors.ink, BorderStroke(2.dp, colors.ink))
        else -> Triple(colors.surface300, colors.ink, null)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .background(background, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
    ) {
        Text(text = position.toString(), style = Triangulacion.type.title.tabular.copy(fontWeight = FontWeight.Bold), color = content)
    }
}

enum class TimelineEventKind { GOAL, ASSIST, YELLOW_CARD, RED_CARD }

data class TimelineEvent(
    val id: String,
    val minute: Int?,
    val team: TriTeam?,
    val kind: TimelineEventKind,
    val who: String,
    val note: String? = null
)

@Composable
fun timelineEventName(kind: TimelineEventKind): String = stringResource(
    when (kind) {
        TimelineEventKind.GOAL -> Res.string.tri_event_goal
        TimelineEventKind.ASSIST -> Res.string.tri_event_assist
        TimelineEventKind.YELLOW_CARD -> Res.string.tri_event_yellow
        TimelineEventKind.RED_CARD -> Res.string.tri_event_red
    }
)

/** Event glyph: ball, arrow, or a filled card (the only filled icons of the system). */
@Composable
fun TimelineEventIcon(kind: TimelineEventKind, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(32.dp)
            .background(if (kind == TimelineEventKind.GOAL) colors.volt else colors.surface300, CircleShape)
    ) {
        when (kind) {
            TimelineEventKind.GOAL -> TriIconView(TriIcon.Ball, contentDescription = null, size = 18.dp, tint = colors.onVolt)
            TimelineEventKind.ASSIST -> TriIconView(TriIcon.ArrowR, contentDescription = null, size = 18.dp, tint = colors.ink)
            TimelineEventKind.YELLOW_CARD -> Box(Modifier.size(width = 11.dp, height = 15.dp).background(colors.cardYellow, RoundedCornerShape(2.dp)))
            TimelineEventKind.RED_CARD -> Box(Modifier.size(width = 11.dp, height = 15.dp).background(colors.cardRed, RoundedCornerShape(2.dp)))
        }
    }
}

/**
 * Cronología: events of team A on the left and team B on the right of a vertical axis, with the minute in the
 * middle. [onEventClick] lets managers act on an event (delete).
 */
@Composable
fun Timeline(
    events: List<TimelineEvent>,
    modifier: Modifier = Modifier,
    onEventClick: ((TimelineEvent) -> Unit)? = null
) {
    val colors = Triangulacion.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        events.forEach { event ->
            // «Minuto 23. Gol. Ana. Equipo A», read once per event
            val spoken = listOfNotNull(
                event.minute?.let { stringResource(Res.string.tri_minute_a11y, it) },
                event.note ?: timelineEventName(event.kind),
                event.who,
                event.team?.let { teamName(it) }
            ).joinToString(". ")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .semantics(mergeDescendants = true) { contentDescription = spoken }
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (event.team != TriTeam.B) TimelineCard(event, alignEnd = false, onClick = onEventClick?.let { { it(event) } })
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(64.dp).fillMaxHeight()) {
                    TriDashedLine(modifier = Modifier.fillMaxHeight().width(2.dp), vertical = true, color = colors.lineStrong.copy(alpha = 0.5f))
                    Text(
                        text = event.minute?.let { "$it'" } ?: "–",
                        style = Triangulacion.type.label.tabular.copy(fontSize = 13.sp),
                        color = colors.ink,
                        modifier = Modifier
                            .background(colors.surface100, RoundedCornerShape(Radius.xs))
                            .border(1.5.dp, colors.lineStrong, RoundedCornerShape(Radius.xs))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (event.team == TriTeam.B) TimelineCard(event, alignEnd = true, onClick = onEventClick?.let { { it(event) } })
                }
            }
        }
    }
}

@Composable
private fun TimelineCard(event: TimelineEvent, alignEnd: Boolean, onClick: (() -> Unit)?) {
    val colors = Triangulacion.colors
    TriCard(contentPadding = 10.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!alignEnd) TimelineEventIcon(event.kind)
            // Two lines each: with large fonts the names wrap instead of being cut
            Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start, modifier = Modifier.widthIn(max = 120.dp)) {
                Text(text = event.who, style = Triangulacion.type.label.copy(fontSize = 15.sp), color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = event.note ?: timelineEventName(event.kind), style = Triangulacion.type.bodySm, color = colors.inkMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (alignEnd) TimelineEventIcon(event.kind)
        }
    }
}

/**
 * CodigoClub: the invite code in cells with a dash in the middle. Case-insensitive: the field upper-cases and keeps
 * letters and digits only. It shows [length] cells and grows up to [maxLength] (the backend accepts 6–12).
 */
@Composable
fun InviteCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 8,
    maxLength: Int = 12,
    isError: Boolean = false,
    onDone: (() -> Unit)? = null
) {
    val colors = Triangulacion.colors
    val description = stringResource(Res.string.tri_code_a11y)
    BasicTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isLetterOrDigit() }.uppercase().take(maxLength)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done, autoCorrectEnabled = false),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
        modifier = modifier.semantics { contentDescription = description },
        decorationBox = { inner ->
            Box {
                Box(Modifier.alpha(0f).size(1.dp)) { inner() }
                val cells = if (value.length < length) length else (value.length + 1).coerceAtMost(maxLength)
                Row(horizontalArrangement = Arrangement.spacedBy(if (cells > length) 4.dp else 6.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    for (i in 0 until cells) {
                        if (cells == length && i == length / 2) Box(Modifier.size(width = 10.dp, height = 2.dp).background(colors.lineStrong))
                        val char = value.getOrNull(i)
                        val active = i == value.length
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .background(colors.surface200, RoundedCornerShape(Radius.sm))
                                .border(
                                    width = if (active) 2.dp else 1.5.dp,
                                    color = when {
                                        isError -> colors.danger
                                        active -> colors.focusRing
                                        else -> colors.lineStrong
                                    },
                                    shape = RoundedCornerShape(Radius.sm)
                                )
                        ) {
                            Text(text = char?.toString() ?: "", style = Triangulacion.type.displayMd.copy(fontWeight = FontWeight.Bold), color = colors.ink)
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun ListComponentsPreview() {
    SquadfyTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.background(Triangulacion.colors.surface100).padding(Space.s4)) {
            RankingRow(position = 1, name = "Álex Ruiz", value = "1088", number = 9, playerPosition = TriPosition.FWD, delta = 8)
            RankingRow(position = 2, name = "Álvaro Gil", value = "1061", number = 10, playerPosition = TriPosition.MID, delta = 12, isMe = true)
            Timeline(
                events = listOf(
                    TimelineEvent("1", 12, TriTeam.A, TimelineEventKind.GOAL, "Álex Ruiz", "Gol · asist. Dani M."),
                    TimelineEvent("2", 41, TriTeam.B, TimelineEventKind.YELLOW_CARD, "Hugo P.")
                )
            )
            InviteCodeField(value = "K7Q2M9", onValueChange = {})
            Spacer(Modifier.height(4.dp))
        }
    }
}
