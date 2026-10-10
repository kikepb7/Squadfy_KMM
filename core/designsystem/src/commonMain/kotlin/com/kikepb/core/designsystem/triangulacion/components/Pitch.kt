package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.tri_pitch_a11y
import squadfy_app.core.designsystem.generated.resources.tri_team_a_caps
import squadfy_app.core.designsystem.generated.resources.tri_team_b_caps
import kotlin.math.abs

/** A player on the pitch. [number] is the shirt number; without it the disc shows the initials. */
@Immutable
data class PitchPlayer(
    val id: String,
    val name: String,
    val number: Int? = null,
    val position: TriPosition? = null,
    val isMe: Boolean = false,
    val isGuest: Boolean = false
)

/** Where a player stands, in fractions of the pitch (0..1 from the top-left corner), and its line from the goal. */
@Immutable
data class PitchSlot(val player: PitchPlayer, val team: TriTeam, val x: Float, val y: Float, val line: Int = 0) {
    val isGoalkeeper: Boolean get() = player.position == TriPosition.GK
}

/**
 * Line-up for the pitch view (presentation only, no domain rule; spec 018, screen 06-campo): each team in its half,
 * by lines from its own goal (goalkeeper, defenders, midfielders, forwards; no position = midfield). A line spreads
 * wider the more players it has (2 → the middle third, 3 → 20–80 %), and a line of more than five is split in two.
 */
object PitchLayout {
    private val lineDepth = mapOf(TriPosition.GK to 0.09f, TriPosition.DEF to 0.215f, TriPosition.MID to 0.325f, TriPosition.FWD to 0.415f)

    fun place(teamA: List<PitchPlayer>, teamB: List<PitchPlayer>): List<PitchSlot> = placeTeam(teamA, TriTeam.A) + placeTeam(teamB, TriTeam.B)

    /** Horizontal positions of a line of [count] players, centred on the pitch. */
    fun spread(count: Int): List<Float> {
        if (count <= 1) return listOf(0.5f)
        val span = minOf(0.8f, 0.34f + 0.26f * (count - 2))
        return List(count) { i -> 0.5f - span / 2 + span * i / (count - 1) }
    }

    private fun placeTeam(players: List<PitchPlayer>, team: TriTeam): List<PitchSlot> {
        val byLine = players.groupBy { it.position ?: TriPosition.MID }
        val slots = mutableListOf<PitchSlot>()
        var lineIndex = 0
        TriPosition.entries.forEach { line ->
            val members = byLine[line].orEmpty()
            if (members.isEmpty()) return@forEach
            val rows = if (members.size > 5 && line != TriPosition.GK) members.chunked((members.size + 1) / 2) else listOf(members)
            rows.forEachIndexed { rowIndex, row ->
                val depth = (lineDepth.getValue(line) + if (rows.size > 1) (rowIndex - 0.5f) * 0.07f else 0f).coerceIn(0.05f, 0.45f)
                spread(row.size).forEachIndexed { i, x ->
                    slots += PitchSlot(row[i], team, x, if (team == TriTeam.A) depth else 1f - depth, lineIndex)
                }
                lineIndex++
            }
        }
        return slots
    }

    /**
     * The dashed triangulation lines: each outfield player with the close team-mates of the next line (never inside
     * the same line, never the goalkeeper).
     */
    fun links(slots: List<PitchSlot>): List<Pair<PitchSlot, PitchSlot>> {
        val out = mutableListOf<Pair<PitchSlot, PitchSlot>>()
        TriTeam.entries.forEach { team ->
            val outfield = slots.filter { it.team == team && !it.isGoalkeeper }
            val lines = outfield.map { it.line }.distinct().sorted()
            lines.zipWithNext().forEach { (near, far) ->
                outfield.filter { it.line == near }.forEach { a ->
                    outfield.filter { it.line == far }.forEach { b -> if (abs(a.x - b.x) <= 0.36f) out += a to b }
                }
            }
        }
        return out
    }
}

/**
 * Campo11: both teams on the pitch with the triangulation lines between close team-mates and «you» highlighted.
 * A in the top half (Volt), B in the bottom half (Brasa).
 */
@Composable
fun Pitch(
    teamA: List<PitchPlayer>,
    teamB: List<PitchPlayer>,
    modifier: Modifier = Modifier
) {
    val colors = Triangulacion.colors
    val slots = remember(teamA, teamB) { PitchLayout.place(teamA, teamB) }
    val links = remember(slots) { PitchLayout.links(slots) }
    val description = stringResource(Res.string.tri_pitch_a11y)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surfaceInverse)
            .semantics { contentDescription = description }
    ) {
        val w = maxWidth
        val h = maxHeight
        val discSize = (w * 0.095f).coerceIn(30.dp, 46.dp)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Mowing stripes
            val bands = 10
            val band = size.height / bands
            for (i in 0 until bands step 2) drawRect(colors.onInverse.copy(alpha = 0.035f), topLeft = Offset(0f, i * band), size = Size(size.width, band))
            val line = colors.onInverse.copy(alpha = 0.32f)
            val stroke = Stroke(1.4.dp.toPx())
            val mx = size.width * 0.045f
            val my = size.height * 0.035f
            val fieldW = size.width - 2 * mx
            val fieldH = size.height - 2 * my
            drawRoundRect(line, topLeft = Offset(mx, my), size = Size(fieldW, fieldH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = stroke)
            drawLine(line, Offset(mx, size.height / 2), Offset(size.width - mx, size.height / 2), strokeWidth = stroke.width)
            drawCircle(line, radius = size.width * 0.105f, center = center, style = stroke)
            drawCircle(line, radius = 2.dp.toPx(), center = center)
            val boxW = fieldW * 0.5f
            val boxH = fieldH * 0.15f
            val smallW = fieldW * 0.26f
            val smallH = fieldH * 0.065f
            drawRect(line, topLeft = Offset((size.width - boxW) / 2, my), size = Size(boxW, boxH), style = stroke)
            drawRect(line, topLeft = Offset((size.width - smallW) / 2, my), size = Size(smallW, smallH), style = stroke)
            drawRect(line, topLeft = Offset((size.width - boxW) / 2, size.height - my - boxH), size = Size(boxW, boxH), style = stroke)
            drawRect(line, topLeft = Offset((size.width - smallW) / 2, size.height - my - smallH), size = Size(smallW, smallH), style = stroke)
            links.forEach { (a, b) ->
                drawLine(
                    color = (if (a.team == TriTeam.A) colors.teamA else colors.teamB).copy(alpha = 0.8f),
                    start = Offset(a.x * size.width, a.y * size.height),
                    end = Offset(b.x * size.width, b.y * size.height),
                    strokeWidth = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                )
            }
        }
        PitchTeamLabel(stringResource(Res.string.tri_team_a_caps), colors.teamA, colors.onTeamA, Modifier.align(Alignment.TopStart).padding(start = w * 0.045f + 6.dp, top = h * 0.035f + 6.dp))
        PitchTeamLabel(stringResource(Res.string.tri_team_b_caps), colors.teamB, colors.onTeamB, Modifier.align(Alignment.BottomStart).padding(start = w * 0.045f + 6.dp, bottom = h * 0.035f + 6.dp))
        val labelWidth = discSize * 2.2f
        slots.forEach { slot ->
            PlayerDisc(
                slot = slot,
                discSize = discSize,
                modifier = Modifier
                    .width(labelWidth)
                    .offset(x = w * slot.x - labelWidth / 2, y = h * slot.y - discSize / 2)
            )
        }
    }
}

@Composable
private fun PitchTeamLabel(text: String, background: Color, content: Color, modifier: Modifier) {
    Text(
        text = text,
        style = Triangulacion.type.labelCaps,
        color = content,
        modifier = modifier.background(background, RoundedCornerShape(Radius.xs)).padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/** Ficha: disc with the shirt number. Dashed ring = you, light ring = goalkeeper, outline only = guest. */
@Composable
fun PlayerDisc(slot: PitchSlot, discSize: Dp, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    val fill = if (slot.team == TriTeam.A) colors.teamA else colors.teamB
    val content = if (slot.team == TriTeam.A) colors.onTeamA else colors.onTeamB
    val player = slot.player
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(discSize)
                .drawBehind {
                    if (player.isMe) drawCircle(
                        color = colors.onInverse,
                        radius = size.minDimension / 2 + 5.dp.toPx(),
                        style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 3.dp.toPx())))
                    )
                    // Soft drop shadow so the disc sits on the grass
                    drawCircle(Color.Black.copy(alpha = 0.25f), radius = size.minDimension / 2, center = center.copy(y = center.y + 2.dp.toPx()))
                }
                .clip(CircleShape)
                .then(if (player.isGuest) Modifier.background(colors.surfaceInverse).border(2.dp, fill, CircleShape) else Modifier.background(fill))
                .then(if (slot.isGoalkeeper) Modifier.border(3.dp, colors.onInverse.copy(alpha = 0.9f), CircleShape) else Modifier)
        ) {
            Text(
                text = player.number?.toString() ?: initialsOf(player.name),
                style = Triangulacion.type.title.copy(fontSize = (discSize.value * 0.42f).sp, fontWeight = FontWeight.Bold),
                color = if (player.isGuest) fill else content
            )
        }
        Text(
            text = player.name,
            style = Triangulacion.type.label.copy(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(0f, 1f), blurRadius = 3f)
            ),
            color = colors.onInverse,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun PitchPreview() {
    SquadfyTheme {
        Box(Modifier.background(Triangulacion.colors.surface100).padding(16.dp)) {
            Pitch(teamA = emptyList(), teamB = emptyList())
        }
    }
}

@Preview
@Composable
private fun PitchDarkPreview() {
    SquadfyTheme(darkTheme = true) {
        Box(Modifier.background(Triangulacion.colors.surface100).padding(16.dp)) {
            Pitch(teamA = emptyList(), teamB = emptyList())
        }
    }
}
