package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion

/**
 * The signature illustration on the inverse surface: players joined by dashed passes, the ball in Volt and a
 * rival in Brasa. Decorative (no semantics). [variant] 0 = welcome triangle, 1 = draw zig-zag.
 */
@Composable
fun TriangulationArt(modifier: Modifier = Modifier, variant: Int = 0) {
    val colors = Triangulacion.colors
    Canvas(modifier = modifier) {
        val faint = colors.onInverse.copy(alpha = 0.45f)
        val dashed = PathEffect.dashPathEffect(floatArrayOf(8f, 10f))
        val dotted = PathEffect.dashPathEffect(floatArrayOf(4f, 10f))
        fun at(x: Float, y: Float) = Offset(size.width * x, size.height * y)
        if (variant == 0) {
            val top = at(0.5f, 0.12f); val left = at(0.12f, 0.86f); val mid = at(0.44f, 0.84f); val right = at(0.82f, 0.8f)
            drawLine(faint, top, left, strokeWidth = 2.dp.toPx(), pathEffect = dashed)
            drawLine(faint, top, right, strokeWidth = 2.dp.toPx(), pathEffect = dashed)
            drawLine(colors.volt, left, mid, strokeWidth = 3.dp.toPx(), pathEffect = dotted)
            drawLine(colors.volt, mid, right, strokeWidth = 3.dp.toPx(), pathEffect = dotted)
            drawCircle(colors.onInverse, radius = 14.dp.toPx(), center = top)
            drawCircle(colors.volt, radius = 13.dp.toPx(), center = left)
            drawCircle(colors.volt, radius = 18.dp.toPx(), center = mid)
            drawCircle(colors.teamB, radius = 13.dp.toPx(), center = right)
        } else {
            val a = at(0.2f, 0.75f); val b = at(0.42f, 0.2f); val c = at(0.52f, 0.55f); val d = at(0.64f, 0.85f); val e = at(0.82f, 0.25f)
            drawLine(faint, a, b, strokeWidth = 2.dp.toPx(), pathEffect = dashed)
            drawLine(faint, b, c, strokeWidth = 2.dp.toPx(), pathEffect = dashed)
            drawLine(faint, c, d, strokeWidth = 2.dp.toPx(), pathEffect = dashed)
            drawLine(faint, d, e, strokeWidth = 2.dp.toPx(), pathEffect = dashed)
            drawCircle(colors.volt, radius = 11.dp.toPx(), center = a)
            drawCircle(colors.volt, radius = 11.dp.toPx(), center = b)
            drawCircle(colors.volt, radius = 16.dp.toPx(), center = c)
            drawCircle(colors.teamB, radius = 11.dp.toPx(), center = d)
            drawCircle(colors.teamB, radius = 11.dp.toPx(), center = e)
        }
    }
}
