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
import kotlin.math.hypot

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

/** Where a player stands, in fractions of the pitch (0..1 from the top-left corner). */
@Immutable
data class PitchSlot(val player: PitchPlayer, val team: TriTeam, val x: Float, val y: Float) {
    val isGoalkeeper: Boolean get() = player.position == TriPosition.GK
}

/**
 * Line-up for the pitch view (presentation only, no domain rule): each team in its half, by lines from its own goal
 * (goalkeeper, defenders, midfielders, forwards; no position = midfield). A line of more than four is split in two.
 */
object PitchLayout {
    private val lineDepth = mapOf(TriPosition.GK to 0.055f, TriPosition.DEF to 0.17f, TriPosition.MID to 0.29f, TriPosition.FWD to 0.41f)

    fun place(teamA: List<PitchPlayer>, teamB: List<PitchPlayer>): List<PitchSlot> = placeTeam(teamA, TriTeam.A) + placeTeam(teamB, TriTeam.B)

    private fun placeTeam(players: List<PitchPlayer>, team: TriTeam): List<PitchSlot> {
        val byLine = players.groupBy { it.position ?: TriPosition.MID }
        val slots = mutableListOf<PitchSlot>()
        TriPosition.entries.forEach { line ->
            val members = byLine[line].orEmpty()
            if (members.isEmpty()) return@forEach
            val rows = if (members.size > 4 && line != TriPosition.GK) members.chunked((members.size + 1) / 2) else listOf(members)
            rows.forEachIndexed { rowIndex, row ->
                val depth = (lineDepth.getValue(line) + if (rows.size > 1) (rowIndex - 0.5f) * 0.06f else 0f).coerceIn(0.05f, 0.46f)
                row.forEachIndexed { i, player ->
                    val x = 0.12f + 0.76f * (i + 1) / (row.size + 1)
                    val y = if (team == TriTeam.A) depth else 1f - depth
                    slots += PitchSlot(player, team, x, y)
                }
            }
        }
        return slots
    }

    /** Pairs of nearby outfield team-mates: the dashed triangulation lines. */
    fun links(slots: List<PitchSlot>): List<Pair<PitchSlot, PitchSlot>> {
        val out = mutableListOf<Pair<PitchSlot, PitchSlot>>()
        TriTeam.entries.forEach { team ->
            val outfield = slots.filter { it.team == team && !it.isGoalkeeper }
            for (i in outfield.indices) for (j in i + 1 until outfield.size) {
                val a = outfield[i]
                val b = outfield[j]
                if (hypot((a.x - b.x) * 100f, (a.y - b.y) * 140f) < 34f) out += a to b
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
    modifier: Modifier = Modifier,
    discSize: Dp = 34.dp
) {
    val colors = Triangulacion.colors
    val slots = remember(teamA, teamB) { PitchLayout.place(teamA, teamB) }
    val links = remember(slots) { PitchLayout.links(slots) }
    val description = stringResource(Res.string.tri_pitch_a11y)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(90f / 120f)
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surfaceInverse)
            .semantics { contentDescription = description }
    ) {
        val w = maxWidth
        val h = maxHeight
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripe = size.height / 12f
            for (i in 0 until 12 step 2) drawRect(colors.onInverse.copy(alpha = 0.035f), topLeft = Offset(0f, i * stripe), size = Size(size.width, stripe))
            val line = colors.onInverse.copy(alpha = 0.35f)
            val stroke = Stroke(1.2.dp.toPx())
            val m = 10.dp.toPx()
            drawRect(line, topLeft = Offset(m, m), size = Size(size.width - 2 * m, size.height - 2 * m), style = stroke)
            drawLine(line, Offset(m, size.height / 2), Offset(size.width - m, size.height / 2), strokeWidth = stroke.width)
            drawCircle(line, radius = size.width * 0.11f, center = center, style = stroke)
            val boxW = size.width * 0.46f
            val boxH = size.height * 0.13f
            val smallW = size.width * 0.24f
            val smallH = size.height * 0.05f
            drawRect(line, topLeft = Offset((size.width - boxW) / 2, m), size = Size(boxW, boxH), style = stroke)
            drawRect(line, topLeft = Offset((size.width - smallW) / 2, m), size = Size(smallW, smallH), style = stroke)
            drawRect(line, topLeft = Offset((size.width - boxW) / 2, size.height - m - boxH), size = Size(boxW, boxH), style = stroke)
            drawRect(line, topLeft = Offset((size.width - smallW) / 2, size.height - m - smallH), size = Size(smallW, smallH), style = stroke)
            links.forEach { (a, b) ->
                drawLine(
                    color = (if (a.team == TriTeam.A) colors.teamA else colors.teamB).copy(alpha = 0.75f),
                    start = Offset(a.x * size.width, a.y * size.height),
                    end = Offset(b.x * size.width, b.y * size.height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }
        }
        PitchTeamLabel(stringResource(Res.string.tri_team_a_caps), colors.teamA, colors.onTeamA, Modifier.align(Alignment.TopStart).padding(12.dp))
        PitchTeamLabel(stringResource(Res.string.tri_team_b_caps), colors.teamB, colors.onTeamB, Modifier.align(Alignment.BottomStart).padding(12.dp))
        val labelWidth = discSize * 2f
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

/** Ficha: disc with the shirt number. Dashed ring = you, white border = goalkeeper, outline only = guest. */
@Composable
fun PlayerDisc(slot: PitchSlot, discSize: Dp, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    val fill = if (slot.team == TriTeam.A) colors.teamA else colors.teamB
    val content = if (slot.team == TriTeam.A) colors.onTeamA else colors.onTeamB
    val player = slot.player
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(discSize)
                .drawBehind {
                    if (player.isMe) drawCircle(
                        color = colors.onInverse,
                        radius = size.minDimension / 2 + 5.dp.toPx(),
                        style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)))
                    )
                }
                .clip(CircleShape)
                .then(if (player.isGuest) Modifier.border(2.dp, fill, CircleShape) else Modifier.background(fill))
                .then(if (slot.isGoalkeeper) Modifier.border(2.dp, colors.onInverse, CircleShape) else Modifier)
        ) {
            Text(
                text = player.number?.toString() ?: initialsOf(player.name),
                style = Triangulacion.type.title.copy(fontSize = (discSize.value * 0.42f).sp, fontWeight = FontWeight.Bold),
                color = if (player.isGuest) fill else content
            )
        }
        Text(
            text = player.name,
            style = Triangulacion.type.bodySm.copy(fontSize = 11.sp, lineHeight = 13.sp),
            color = colors.onInverse,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )
    }
}

@Preview
@Composable
private fun PitchPreview() {
    SquadfyTheme {
        Pitch(
            teamA = listOf(
                PitchPlayer("1", "Marcos", 1, TriPosition.GK), PitchPlayer("4", "Iván", 4, TriPosition.DEF),
                PitchPlayer("3", "Hugo", 3, TriPosition.DEF), PitchPlayer("5", "Sergio", 5, TriPosition.MID),
                PitchPlayer("8", "Dani", 8, TriPosition.MID), PitchPlayer("10", "Álvaro", 10, TriPosition.MID, isMe = true),
                PitchPlayer("9", "Álex", 9, TriPosition.FWD)
            ),
            teamB = listOf(
                PitchPlayer("13", "Nico", 13, TriPosition.GK), PitchPlayer("2", "Jaime", 2, TriPosition.DEF),
                PitchPlayer("15", "Gabi", 15, TriPosition.DEF), PitchPlayer("6", "Tomás", 6, TriPosition.MID),
                PitchPlayer("16", "Pablo", 16, TriPosition.MID), PitchPlayer("14", "Nacho", 14, TriPosition.MID),
                PitchPlayer("11", "Leo", 11, TriPosition.FWD, isGuest = true)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
