package com.dodamsoft.ajangajang.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 4-axis radar chart. Stateless and self-contained.
 *
 * @param values 4 ratios in [0, 1] in the order: social / language / cognitive / physical.
 * @param labels 4 labels matching [values].
 */
@Composable
fun RadarChart4(
    values: FloatArray,
    labels: List<String>,
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outline,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    gridSteps: Int = 4,
) {
    require(values.size == 4) { "RadarChart4 expects exactly 4 values" }
    require(labels.size == 4) { "RadarChart4 expects exactly 4 labels" }

    val density = LocalDensity.current
    val labelTextSizePx = with(density) { 13.sp.toPx() }
    val labelArgb = labelColor.toArgb()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = min(size.width, size.height) / 2f - 56f

            // Angles: 12-, 3-, 6-, 9-o'clock
            val angles = floatArrayOf(
                (-Math.PI / 2).toFloat(),
                0f,
                (Math.PI / 2).toFloat(),
                Math.PI.toFloat(),
            )

            // Grid rings (4-sided polygons, concentric)
            for (step in 1..gridSteps) {
                val r = radius * step / gridSteps
                val path = Path()
                angles.forEachIndexed { i, angle ->
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color = gridColor.copy(alpha = 0.2f), style = Stroke(width = 1f))
            }

            // Axes
            angles.forEach { angle ->
                val x = cx + radius * cos(angle)
                val y = cy + radius * sin(angle)
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(cx, cy),
                    end = Offset(x, y),
                    strokeWidth = 1f,
                )
            }

            // Data polygon
            val dataPath = Path()
            angles.forEachIndexed { i, angle ->
                val r = radius * values[i].coerceIn(0f, 1f)
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()
            drawPath(dataPath, color = fillColor.copy(alpha = 0.25f))
            drawPath(dataPath, color = fillColor, style = Stroke(width = 3f))

            // Vertex dots
            angles.forEachIndexed { i, angle ->
                val r = radius * values[i].coerceIn(0f, 1f)
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                drawCircle(fillColor, radius = 6f, center = Offset(x, y))
            }

            // Labels — use nativeCanvas + Paint for fast text rendering
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = labelArgb
                textSize = labelTextSizePx
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val labelOffset = 28f
            drawContext.canvas.nativeCanvas.apply {
                angles.forEachIndexed { i, angle ->
                    val x = cx + (radius + labelOffset) * cos(angle)
                    val y = cy + (radius + labelOffset) * sin(angle)
                    val label = labels[i]
                    val textWidth = paint.measureText(label)
                    val drawX = when (i) {
                        0 -> x - textWidth / 2f                    // top (center)
                        1 -> x                                      // right (left-aligned)
                        2 -> x - textWidth / 2f                    // bottom (center)
                        else -> x - textWidth                      // left (right-aligned)
                    }
                    val drawY = when (i) {
                        0 -> y - 8f                                 // top
                        1 -> y + 10f                                // right
                        2 -> y + 30f                                // bottom
                        else -> y + 10f                             // left
                    }
                    drawText(label, drawX, drawY, paint)
                }
            }
        }
    }
}

private fun Color.toArgb(): Int {
    val a = (alpha * 255).toInt() and 0xFF
    val r = (red * 255).toInt() and 0xFF
    val g = (green * 255).toInt() and 0xFF
    val b = (blue * 255).toInt() and 0xFF
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}
