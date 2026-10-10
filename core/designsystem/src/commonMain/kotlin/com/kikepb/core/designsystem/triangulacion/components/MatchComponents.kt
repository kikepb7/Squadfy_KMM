package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.SKEW_TAN
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.tri_balance_a11y
import squadfy_app.core.designsystem.generated.resources.tri_balance_detail
import squadfy_app.core.designsystem.generated.resources.tri_balance_even
import squadfy_app.core.designsystem.generated.resources.tri_balance_off
import squadfy_app.core.designsystem.generated.resources.tri_balance_slight
import squadfy_app.core.designsystem.generated.resources.tri_closes_in
import squadfy_app.core.designsystem.generated.resources.tri_countdown_a11y
import squadfy_app.core.designsystem.generated.resources.tri_days
import squadfy_app.core.designsystem.generated.resources.tri_delta_down
import squadfy_app.core.designsystem.generated.resources.tri_delta_flat
import squadfy_app.core.designsystem.generated.resources.tri_delta_up
import squadfy_app.core.designsystem.generated.resources.tri_form_a11y
import squadfy_app.core.designsystem.generated.resources.tri_hours
import squadfy_app.core.designsystem.generated.resources.tri_minutes
import squadfy_app.core.designsystem.generated.resources.tri_next_match
import squadfy_app.core.designsystem.generated.resources.tri_places_a11y
import squadfy_app.core.designsystem.generated.resources.tri_places_compact
import squadfy_app.core.designsystem.generated.resources.tri_places_confirmed
import squadfy_app.core.designsystem.generated.resources.tri_places_free
import squadfy_app.core.designsystem.generated.resources.tri_places_full
import squadfy_app.core.designsystem.generated.resources.tri_places_left_caps
import squadfy_app.core.designsystem.generated.resources.tri_places_n
import squadfy_app.core.designsystem.generated.resources.tri_places_one
import squadfy_app.core.designsystem.generated.resources.tri_result_draw
import squadfy_app.core.designsystem.generated.resources.tri_result_loss
import squadfy_app.core.designsystem.generated.resources.tri_result_win
import squadfy_app.core.designsystem.generated.resources.tri_score_a11y
import squadfy_app.core.designsystem.generated.resources.tri_team_a_caps
import squadfy_app.core.designsystem.generated.resources.tri_team_b_caps
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

// ---------------------------------------------------------------------------------------------------------------
// Places
// ---------------------------------------------------------------------------------------------------------------

/** One slanted segment per place: filled = Volt, empty = surface-300 (or translucent on the inverse surface). */
@Composable
fun PlacesBar(
    max: Int,
    confirmed: Int,
    modifier: Modifier = Modifier,
    height: Dp = 22.dp,
    onInverse: Boolean = false,
    meIndex: Int? = null
) {
    val colors = Triangulacion.colors
    val empty = if (onInverse) colors.onInverse.copy(alpha = 0.18f) else colors.surface300
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (max <= 0) return@Canvas
        val gap = 3.dp.toPx()
        val skew = SKEW_TAN * size.height
        val segment = (size.width - gap * (max - 1) - skew) / max
        repeat(max) { i ->
            val x = i * (segment + gap)
            val path = Path().apply {
                moveTo(x + skew, 0f); lineTo(x + skew + segment, 0f); lineTo(x + segment, size.height); lineTo(x, size.height); close()
            }
            drawPath(path, if (i < confirmed) colors.volt else empty)
            if (meIndex == i) drawPath(path, colors.brand, style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
private fun placesLeftText(left: Int): String = when {
    left <= 0 -> stringResource(Res.string.tri_places_full)
    left == 1 -> stringResource(Res.string.tri_places_one)
    else -> stringResource(Res.string.tri_places_n, left)
}

/** Places block of the match card: «9/14 · QUEDAN 5 plazas», the bar and its legend. */
@Composable
fun Plazas(max: Int, confirmed: Int, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    val description = stringResource(Res.string.tri_places_a11y, confirmed, max)
    Column(modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            Text(text = confirmed.toString(), style = Triangulacion.type.displayXl.tabular.copy(fontSize = 48.sp, lineHeight = 48.sp), color = colors.ink)
            Text(text = "/$max", style = Triangulacion.type.title.tabular, color = colors.inkMuted, modifier = Modifier.padding(bottom = 4.dp))
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                TriCaps(text = stringResource(Res.string.tri_places_left_caps))
                Text(text = placesLeftText(max - confirmed), style = Triangulacion.type.title.copy(fontWeight = FontWeight.Bold), color = colors.ink)
            }
        }
        PlacesBar(max = max, confirmed = confirmed)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(colors.volt, stringResource(Res.string.tri_places_confirmed))
            LegendDot(colors.surface300, stringResource(Res.string.tri_places_free))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(width = 14.dp, height = 10.dp).clip(SkewShape).background(color))
        Text(text = label, style = Triangulacion.type.bodySm, color = Triangulacion.colors.inkMuted)
    }
}

/** Compact places for the ticket (on the inverse surface). */
@Composable
fun PlacesCompact(max: Int, confirmed: Int, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    val description = stringResource(Res.string.tri_places_a11y, confirmed, max)
    Column(modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = confirmed.toString(), style = Triangulacion.type.displayLg.tabular, color = colors.onInverse)
            Spacer(Modifier.width(6.dp))
            Text(text = stringResource(Res.string.tri_places_compact, max), style = Triangulacion.type.bodySm, color = colors.onInverseMuted, modifier = Modifier.padding(bottom = 5.dp))
        }
        PlacesBar(max = max, confirmed = confirmed, height = 18.dp, onInverse = true)
    }
}

// ---------------------------------------------------------------------------------------------------------------
// Countdown
// ---------------------------------------------------------------------------------------------------------------

data class CountdownValue(val days: Int, val hours: Int, val minutes: Int) {
    val isOver: Boolean get() = days == 0 && hours == 0 && minutes == 0

    companion object {
        fun of(remaining: Duration): CountdownValue {
            val total = remaining.inWholeMinutes.coerceAtLeast(0)
            return CountdownValue(days = (total / (24 * 60)).toInt(), hours = ((total / 60) % 24).toInt(), minutes = (total % 60).toInt())
        }
    }
}

/** Time left until [target], refreshed every 20 s. */
@Composable
fun rememberCountdown(target: Instant, clock: Clock = Clock.System): CountdownValue {
    var value by remember(target) { mutableStateOf(CountdownValue.of(target - clock.now())) }
    LaunchedEffect(target) {
        while (true) {
            value = CountdownValue.of(target - clock.now())
            delay(20.seconds)
        }
    }
    return value
}

/** CuentaAtras: days : hours : min in tabular cells. [inverse] for the ticket, [urgent] tints the label. */
@Composable
fun Countdown(
    value: CountdownValue,
    modifier: Modifier = Modifier,
    inverse: Boolean = false,
    urgent: Boolean = false,
    label: String? = stringResource(Res.string.tri_closes_in),
    cellWidth: Dp = 52.dp
) {
    val colors = Triangulacion.colors
    val description = stringResource(Res.string.tri_countdown_a11y, value.days, value.hours, value.minutes)
    Column(modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) {
            val tint = when {
                inverse -> colors.onInverseMuted
                urgent -> colors.warning
                else -> colors.inkMuted
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TriIconView(icon = TriIcon.Clock, contentDescription = null, size = 14.dp, tint = tint)
                TriCaps(text = label, color = tint)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CountdownCell(value.days, stringResource(Res.string.tri_days), inverse, cellWidth)
            CountdownSeparator(inverse)
            CountdownCell(value.hours, stringResource(Res.string.tri_hours), inverse, cellWidth)
            CountdownSeparator(inverse)
            CountdownCell(value.minutes, stringResource(Res.string.tri_minutes), inverse, cellWidth)
        }
    }
}

@Composable
private fun CountdownCell(value: Int, label: String, inverse: Boolean, width: Dp) {
    val colors = Triangulacion.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = width)
            .background(if (inverse) colors.onInverse.copy(alpha = 0.08f) else colors.surface300, RoundedCornerShape(Radius.sm))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Text(text = value.toString().padStart(2, '0'), style = Triangulacion.type.displayMd.tabular.copy(fontWeight = FontWeight.Bold), color = if (inverse) colors.onInverse else colors.ink)
        Text(text = label, style = Triangulacion.type.labelCaps.copy(fontSize = 10.sp), color = if (inverse) colors.onInverseMuted else colors.inkMuted)
    }
}

@Composable
private fun CountdownSeparator(inverse: Boolean) {
    Text(text = ":", style = Triangulacion.type.title, color = if (inverse) Triangulacion.colors.onInverseMuted else Triangulacion.colors.inkMuted)
}

// ---------------------------------------------------------------------------------------------------------------
// Ticket
// ---------------------------------------------------------------------------------------------------------------

/**
 * Ticket of the next match: inverse block with the large Corte, Volt fold and a perforation with notches. The
 * notches are painted with [notchColor] (the screen background behind the ticket).
 */
@Composable
fun MatchTicket(
    whenText: String,
    detail: String,
    modifier: Modifier = Modifier,
    clubName: String? = null,
    crest: (@Composable () -> Unit)? = null,
    caption: String = stringResource(Res.string.tri_next_match),
    notchColor: Color = Triangulacion.colors.surface100,
    footer: @Composable RowScope.() -> Unit
) {
    val colors = Triangulacion.colors
    Column(
        modifier = modifier
            .clip(CutLg)
            .background(colors.surfaceInverse)
            .drawBehind {
                val cut = Radius.cutLg.toPx()
                drawPath(Path().apply { moveTo(size.width - cut, 0f); lineTo(size.width, cut); lineTo(size.width - cut, cut); close() }, colors.volt)
            }
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(end = 18.dp)) {
                TriCaps(text = caption, color = colors.onInverseMuted, modifier = Modifier.weight(1f))
                if (clubName != null) {
                    Text(text = clubName, style = Triangulacion.type.label.copy(fontSize = 13.sp), color = colors.onInverse, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 150.dp))
                }
                if (crest != null) { Spacer(Modifier.width(8.dp)); crest() }
            }
            Text(text = whenText, style = Triangulacion.type.displayLg, color = colors.onInverse)
            Text(text = detail, style = Triangulacion.type.body, color = colors.onInverseMuted)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .drawBehind {
                    drawLine(colors.onInverse.copy(alpha = 0.22f), Offset(14.dp.toPx(), 1.dp.toPx()), Offset(size.width - 14.dp.toPx(), 1.dp.toPx()), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                    drawCircle(notchColor, 10.dp.toPx(), Offset(0f, 1.dp.toPx()))
                    drawCircle(notchColor, 10.dp.toPx(), Offset(size.width, 1.dp.toPx()))
                }
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = footer
        )
    }
}

// ---------------------------------------------------------------------------------------------------------------
// Balance, score, form and rating delta
// ---------------------------------------------------------------------------------------------------------------

/** Equilibrio: average rating of each team and a slanted bar split at the expected win of A (0..1). */
@Composable
fun TeamBalance(ratingA: Int, ratingB: Int, expectedA: Float, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    val diff = abs(ratingA - ratingB)
    val percentA = (expectedA * 100).roundToInt()
    val verdict = stringResource(
        when {
            diff <= 15 -> Res.string.tri_balance_even
            diff <= 40 -> Res.string.tri_balance_slight
            else -> Res.string.tri_balance_off
        }
    )
    val description = stringResource(Res.string.tri_balance_a11y, verdict, percentA)
    Column(modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TeamBadge(TriTeam.A, size = 18.dp)
                    TriCaps(text = stringResource(Res.string.tri_team_a_caps), color = colors.teamAText)
                }
                Text(text = ratingA.toString(), style = Triangulacion.type.displayLg.tabular, color = colors.ink)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TriCaps(text = stringResource(Res.string.tri_team_b_caps), color = colors.teamBText)
                    TeamBadge(TriTeam.B, size = 18.dp)
                }
                Text(text = ratingB.toString(), style = Triangulacion.type.displayLg.tabular, color = colors.ink)
            }
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            val skew = SKEW_TAN * size.height
            val gap = 3.dp.toPx()
            val split = (size.width - skew) * expectedA.coerceIn(0.05f, 0.95f)
            drawPath(Path().apply { moveTo(skew, 0f); lineTo(split + skew - gap / 2, 0f); lineTo(split - gap / 2, size.height); lineTo(0f, size.height); close() }, colors.teamA)
            drawPath(Path().apply { moveTo(split + skew + gap / 2, 0f); lineTo(size.width, 0f); lineTo(size.width - skew, size.height); lineTo(split + gap / 2, size.height); close() }, colors.teamB)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
            TriIconView(icon = TriIcon.Swap, contentDescription = null, size = 16.dp, tint = colors.inkMuted)
            Text(text = verdict, style = Triangulacion.type.label, color = colors.ink)
            Text(text = stringResource(Res.string.tri_balance_detail, diff, percentA), style = Triangulacion.type.bodySm, color = colors.inkMuted)
        }
    }
}

/** Marcador: both teams and the score in hero figures with a slanted Volt separator. */
@Composable
fun Scoreboard(
    scoreA: Int,
    scoreB: Int,
    nameA: String,
    nameB: String,
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    val colors = Triangulacion.colors
    val description = stringResource(Res.string.tri_score_a11y, scoreA, scoreB)
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ScoreTeam(TriTeam.A, nameA)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.semantics { contentDescription = description }) {
                Text(text = scoreA.toString(), style = Triangulacion.type.statXl, color = colors.ink)
                Box(Modifier.size(width = 14.dp, height = 44.dp).clip(SkewShape).background(colors.volt))
                Text(text = scoreB.toString(), style = Triangulacion.type.statXl, color = colors.ink)
            }
            ScoreTeam(TriTeam.B, nameB)
        }
        footer?.invoke(this)
    }
}

@Composable
private fun ScoreTeam(team: TriTeam, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TriAvatar(name = team.name, size = 36.dp, team = team)
        Text(text = name, style = Triangulacion.type.bodySm, color = Triangulacion.colors.inkMuted)
    }
}

enum class MatchResult { WIN, DRAW, LOSS }

private val TriangleDown = GenericShape { s, _ -> moveTo(s.width / 2, s.height); lineTo(0f, 0f); lineTo(s.width, 0f); close() }

@Composable
fun resultLetter(result: MatchResult): String = stringResource(
    when (result) {
        MatchResult.WIN -> Res.string.tri_result_win
        MatchResult.DRAW -> Res.string.tri_result_draw
        MatchResult.LOSS -> Res.string.tri_result_loss
    }
)

/** Forma: win = Volt circle, draw = neutral square, loss = inverted triangle. Shape and letter carry the meaning. */
@Composable
fun FormGuide(results: List<MatchResult>, modifier: Modifier = Modifier, size: Dp = 26.dp) {
    val colors = Triangulacion.colors
    val letters = results.map { resultLetter(it) }
    val description = stringResource(Res.string.tri_form_a11y, letters.joinToString(" "))
    Row(modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        results.forEachIndexed { index, result ->
            val shape = when (result) {
                MatchResult.WIN -> CircleShape
                MatchResult.DRAW -> RoundedCornerShape(5.dp)
                MatchResult.LOSS -> TriangleDown
            }
            val (background, content) = when (result) {
                MatchResult.WIN -> colors.volt to colors.onVolt
                MatchResult.DRAW -> colors.surface300 to colors.ink
                MatchResult.LOSS -> colors.danger to colors.surface100
            }
            Box(
                contentAlignment = if (result == MatchResult.LOSS) Alignment.TopCenter else Alignment.Center,
                modifier = Modifier.size(size).clip(shape).background(background)
            ) {
                Text(
                    text = letters[index],
                    style = Triangulacion.type.labelCaps.copy(letterSpacing = 0.sp, fontSize = (size.value * 0.42f).sp),
                    color = content,
                    modifier = if (result == MatchResult.LOSS) Modifier.padding(top = size * 0.12f) else Modifier
                )
            }
        }
    }
}

/** Rating variation: ▲ green, ▼ red or = neutral, always with the number. */
@Composable
fun RatingDelta(delta: Int, modifier: Modifier = Modifier, onInverse: Boolean = false) {
    val colors = Triangulacion.colors
    val (background, content) = when {
        delta > 0 -> colors.successSoft to colors.accentText
        delta < 0 -> colors.dangerSoft to colors.danger
        else -> (if (onInverse) colors.onInverse.copy(alpha = 0.12f) else colors.surface300) to colors.inkMuted
    }
    val description = when {
        delta > 0 -> stringResource(Res.string.tri_delta_up, delta)
        delta < 0 -> stringResource(Res.string.tri_delta_down, -delta)
        else -> stringResource(Res.string.tri_delta_flat)
    }
    Text(
        text = when {
            delta > 0 -> "▲ $delta"
            delta < 0 -> "▼ ${-delta}"
            else -> "="
        },
        style = Triangulacion.type.label.copy(fontSize = 12.sp).tabular,
        color = content,
        modifier = modifier
            .semantics { contentDescription = description }
            .clip(CutShape(radius = 3.dp, cut = 5.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

@Preview
@Composable
private fun MatchComponentsPreview() {
    SquadfyTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.background(Triangulacion.colors.surface100).padding(Space.s4)) {
            MatchTicket(whenText = "Jue 15 oct · 20:00", detail = "20:00 (Madrid) · Fútbol 7", clubName = "Albaicín FC") {
                PlacesCompact(max = 14, confirmed = 9, modifier = Modifier.weight(1f))
                Countdown(value = CountdownValue(2, 4, 15), inverse = true, label = null, cellWidth = 44.dp)
            }
            TriCard { Plazas(max = 14, confirmed = 9) }
            TriCard { TeamBalance(ratingA = 1032, ratingB = 1027, expectedA = 0.51f) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormGuide(listOf(MatchResult.WIN, MatchResult.DRAW, MatchResult.LOSS))
                RatingDelta(12); RatingDelta(-3); RatingDelta(0)
            }
            Scoreboard(scoreA = 2, scoreB = 1, nameA = "Equipo A", nameB = "Equipo B")
        }
    }
}

/**
 * Rating evolution: brand line over a soft area, the last point highlighted in Volt. [values] oldest first; with
 * fewer than two points nothing is drawn.
 */
@Composable
fun RatingSparkline(values: List<Int>, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    Canvas(modifier = modifier.fillMaxWidth().height(96.dp)) {
        if (values.size < 2) return@Canvas
        val min = values.min().toFloat()
        val max = values.max().toFloat()
        val span = (max - min).takeIf { it > 0f } ?: 1f
        val pad = 8.dp.toPx()
        val points = values.mapIndexed { i, v ->
            Offset(
                x = pad + (size.width - 2 * pad) * i / (values.size - 1),
                y = pad + (size.height - 2 * pad) * (1f - (v - min) / span)
            )
        }
        val line = Path().apply { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } }
        val area = Path().apply {
            addPath(line)
            lineTo(points.last().x, size.height); lineTo(points.first().x, size.height); close()
        }
        drawPath(area, colors.successSoft)
        drawPath(line, colors.accentText, style = Stroke(width = 2.5.dp.toPx()))
        drawCircle(colors.accentText, radius = 6.dp.toPx(), center = points.last())
        drawCircle(colors.volt, radius = 4.dp.toPx(), center = points.last())
    }
}
