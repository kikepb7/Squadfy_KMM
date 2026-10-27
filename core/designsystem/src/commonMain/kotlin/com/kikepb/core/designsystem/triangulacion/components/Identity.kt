package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.CrestTone
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.tri_crest_a11y
import squadfy_app.core.designsystem.generated.resources.tri_pos_def
import squadfy_app.core.designsystem.generated.resources.tri_pos_def_long
import squadfy_app.core.designsystem.generated.resources.tri_pos_fwd
import squadfy_app.core.designsystem.generated.resources.tri_pos_fwd_long
import squadfy_app.core.designsystem.generated.resources.tri_pos_gk
import squadfy_app.core.designsystem.generated.resources.tri_pos_gk_long
import squadfy_app.core.designsystem.generated.resources.tri_pos_mid
import squadfy_app.core.designsystem.generated.resources.tri_pos_mid_long
import squadfy_app.core.designsystem.generated.resources.tri_team_a
import squadfy_app.core.designsystem.generated.resources.tri_team_b

enum class TriTeam { A, B }

/** Positions of the design system; each has its geometric glyph (■ GK, ◆ DEF, ● MID, ▲ FWD). */
enum class TriPosition { GK, DEF, MID, FWD }

/** Initials of a person: first letters of two words, or the first two characters of a single word. */
fun initialsOf(name: String): String {
    val words = name.trim().split(" ", ".", "_", "-").filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words.first().take(2)
        else -> words.take(2).joinToString("") { it.take(1) }
    }.uppercase()
}

/**
 * Avatar: photo or initials on surface-300. In a team context it takes the team fill; [isMe] adds the brand ring.
 * [number] shows the shirt number in a small badge.
 */
@Composable
fun TriAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    imageUrl: String? = null,
    team: TriTeam? = null,
    isMe: Boolean = false,
    number: Int? = null,
    onInverse: Boolean = false
) {
    val colors = Triangulacion.colors
    val (background, content) = when (team) {
        TriTeam.A -> colors.teamA to colors.onTeamA
        TriTeam.B -> colors.teamB to colors.onTeamB
        null -> (if (onInverse) colors.onInverse else colors.surface300) to (if (onInverse) colors.surfaceInverse else colors.ink)
    }
    Box(modifier = modifier.size(size)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .then(if (isMe) Modifier.border(2.dp, colors.brand, CircleShape).padding(3.dp) else Modifier)
                .clip(CircleShape)
                .background(background)
        ) {
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Text(
                    text = initialsOf(name),
                    style = Triangulacion.type.title.copy(fontSize = (size.value * 0.36f).sp, lineHeight = (size.value * 0.4f).sp),
                    color = content
                )
            }
        }
        if (number != null) {
            Text(
                text = number.toString(),
                style = Triangulacion.type.labelCaps.copy(fontSize = 10.sp, letterSpacing = 0.sp),
                color = colors.onInverse,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(colors.surfaceInverse, RoundedCornerShape(Radius.xs))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

/** Square team badge with the letter (A Volt / B Brasa). */
@Composable
fun TeamBadge(team: TriTeam, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    val colors = Triangulacion.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(if (team == TriTeam.A) colors.teamA else colors.teamB, RoundedCornerShape(Radius.xs))
    ) {
        Text(
            text = team.name,
            style = Triangulacion.type.labelCaps.copy(fontSize = (size.value * 0.55f).sp, letterSpacing = 0.sp),
            color = if (team == TriTeam.A) colors.onTeamA else colors.onTeamB
        )
    }
}

@Composable
fun teamName(team: TriTeam): String = stringResource(if (team == TriTeam.A) Res.string.tri_team_a else Res.string.tri_team_b)

private const val CREST_PATH = "M8 2h30l16 16v20c0 10-9 17-26 22C11 55 2 48 2 38V8a6 6 0 0 1 6-6z"
private const val CREST_FOLD = "M38 2l16 16H44a6 6 0 0 1-6-6z"

/** The club crest outline (shield with the Corte), scaled from the kit's 56×60 drawing. */
object CrestShape : Shape {
    private val base: Path = PathParser().parsePathString(CREST_PATH).toPath()
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply { addPath(base) }
        path.transform(Matrix().apply { scale(size.width / 56f, size.height / 60f) })
        return Outline.Generic(path)
    }
}

/** Club crest (Escudo): the club's initials, or its logo, inside the shield. Tone derived from the club id. */
@Composable
fun ClubCrest(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    tone: CrestTone = CrestTone.VOLT,
    logoUrl: String? = null
) {
    val colors = Triangulacion.colors
    val description = stringResource(Res.string.tri_crest_a11y, name)
    val fold = remember { PathParser().parsePathString(CREST_FOLD).toPath() }
    val initials = remember(name) {
        name.split(" ").filter { it.length > 2 || it.firstOrNull()?.isDigit() == true }
            .map { it.first() }.take(2).joinToString("").uppercase().ifEmpty { name.take(1).uppercase() }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = size, height = size * (60f / 56f))
            .semantics { contentDescription = description }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outline = CrestShape.createOutline(this.size, layoutDirection, this)
            val path = (outline as Outline.Generic).path
            drawPath(path, tone.background)
            if (tone == CrestTone.VOLT) drawPath(path, colors.lineStrong, style = Stroke(1.5.dp.toPx()))
            val foldPath = Path().apply { addPath(fold) }
            foldPath.transform(Matrix().apply { scale(this@Canvas.size.width / 56f, this@Canvas.size.height / 60f) })
            drawPath(foldPath, tone.content.copy(alpha = 0.9f))
        }
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CrestShape)
            )
        } else {
            Text(
                text = initials,
                style = Triangulacion.type.title.copy(fontSize = (size.value * 0.32f).sp, fontWeight = FontWeight.Bold),
                color = tone.content,
                modifier = Modifier.padding(bottom = size * 0.08f)
            )
        }
    }
}

/** The geometric glyph of a position. */
@Composable
fun PositionGlyph(position: TriPosition, color: Color, modifier: Modifier = Modifier, size: Dp = 10.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 10f
        when (position) {
            TriPosition.GK -> drawRoundRect(color, topLeft = Offset(1 * s, 1 * s), size = Size(8 * s, 8 * s), cornerRadius = CornerRadius(1 * s))
            TriPosition.DEF -> drawPath(Path().apply { moveTo(5 * s, 0.5f * s); lineTo(9.5f * s, 5 * s); lineTo(5 * s, 9.5f * s); lineTo(0.5f * s, 5 * s); close() }, color)
            TriPosition.MID -> drawCircle(color, radius = 4.2f * s)
            TriPosition.FWD -> drawPath(Path().apply { moveTo(5 * s, 0.8f * s); lineTo(9.6f * s, 9.2f * s); lineTo(0.4f * s, 9.2f * s); close() }, color)
        }
    }
}

@Composable
fun positionShortName(position: TriPosition): String = stringResource(
    when (position) {
        TriPosition.GK -> Res.string.tri_pos_gk
        TriPosition.DEF -> Res.string.tri_pos_def
        TriPosition.MID -> Res.string.tri_pos_mid
        TriPosition.FWD -> Res.string.tri_pos_fwd
    }
)

@Composable
fun positionLongName(position: TriPosition): String = stringResource(
    when (position) {
        TriPosition.GK -> Res.string.tri_pos_gk_long
        TriPosition.DEF -> Res.string.tri_pos_def_long
        TriPosition.MID -> Res.string.tri_pos_mid_long
        TriPosition.FWD -> Res.string.tri_pos_fwd_long
    }
)

/** Position chip: glyph + abbreviation (or the full name). [highlighted] = brand fill (player card). */
@Composable
fun PosChip(
    position: TriPosition,
    modifier: Modifier = Modifier,
    longName: Boolean = false,
    highlighted: Boolean = false
) {
    val colors = Triangulacion.colors
    val (background, content) = if (highlighted) colors.brand to colors.onBrand else colors.surface300 to colors.ink
    val label = if (longName) positionLongName(position) else positionShortName(position)
    // Screen readers say «Delantero», never the abbreviation «DEL» (accessibility audit 2026-10)
    val spoken = positionLongName(position)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .background(background, RoundedCornerShape(Radius.xs))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = spoken }
    ) {
        PositionGlyph(position = position, color = content, size = 9.dp)
        Text(
            text = if (longName) label else label.uppercase(),
            style = if (longName) Triangulacion.type.label.copy(fontSize = 13.sp) else Triangulacion.type.labelCaps.copy(fontSize = 12.sp),
            color = content
        )
    }
}

@Preview
@Composable
private fun IdentityPreview() {
    SquadfyTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ClubCrest(name = "Albaicín FC"); ClubCrest(name = "Cartuja Siete", tone = CrestTone.BRASA, size = 40.dp)
                TriAvatar(name = "Álex Ruiz"); TriAvatar(name = "Álvaro Gil", isMe = true); TriAvatar(name = "Marcos", team = TriTeam.A, size = 32.dp)
                TeamBadge(TriTeam.B)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TriPosition.entries.forEach { PosChip(it) }
            }
            PosChip(TriPosition.MID, longName = true, highlighted = true)
        }
    }
}
