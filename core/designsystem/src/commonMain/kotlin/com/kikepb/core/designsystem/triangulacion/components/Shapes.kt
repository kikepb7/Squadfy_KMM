package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.SKEW_TAN

/**
 * El Corte: rounded rectangle whose top-right corner is bevelled at 45°. [radius] rounds the other three corners,
 * [cut] is the leg of the bevel. Never bevel more than that corner.
 */
class CutShape(private val radius: Dp = Radius.md, private val cut: Dp = Radius.cutMd) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { radius.toPx() }.coerceAtMost(minOf(size.width, size.height) / 2f)
        val c = with(density) { cut.toPx() }.coerceAtMost(minOf(size.width, size.height))
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - c, 0f)
            lineTo(w, c)
            lineTo(w, h - r)
            quadraticTo(w, h, w - r, h)
            lineTo(r, h)
            quadraticTo(0f, h, 0f, h - r)
            lineTo(0f, r)
            quadraticTo(0f, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Small Corte: chips, badges and secondary identity surfaces. */
val CutSm = CutShape(radius = Radius.md, cut = Radius.cutSm)

/** Standard Corte: primary button, ticket-like cards, player card. */
val CutMd = CutShape(radius = Radius.md, cut = Radius.cutMd)

/** Large Corte: hero blocks and the club crest. */
val CutLg = CutShape(radius = Radius.lg, cut = Radius.cutLg)

/** Parallelogram slanted −18°: one segment of the places bar, the balance bar or a tab. */
object SkewShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val skew = size.height * SKEW_TAN
        return Outline.Generic(
            Path().apply {
                moveTo(skew, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width - skew, size.height)
                lineTo(0f, size.height)
                close()
            }
        )
    }
}
