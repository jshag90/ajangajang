package com.dodamsoft.ajangajang.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 4-axis radar chart.
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
    val labelPaint = remember(labelArgb, labelTextSizePx) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = labelArgb
            textSize = labelTextSizePx
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = min(size.width, size.height) / 2f - 56f

            val angles = floatArrayOf(
                (-Math.PI / 2).toFloat(),
                0f,
                (Math.PI / 2).toFloat(),
                Math.PI.toFloat(),
            )

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

            angles.forEachIndexed { i, angle ->
                val r = radius * values[i].coerceIn(0f, 1f)
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                drawCircle(fillColor, radius = 6f, center = Offset(x, y))
            }

            val labelOffset = 28f
            drawContext.canvas.nativeCanvas.apply {
                angles.forEachIndexed { i, angle ->
                    val x = cx + (radius + labelOffset) * cos(angle)
                    val y = cy + (radius + labelOffset) * sin(angle)
                    val label = labels[i]
                    val textWidth = labelPaint.measureText(label)
                    val drawX = when (i) {
                        0 -> x - textWidth / 2f
                        1 -> x
                        2 -> x - textWidth / 2f
                        else -> x - textWidth
                    }
                    val drawY = when (i) {
                        0 -> y - 8f
                        1 -> y + 10f
                        2 -> y + 30f
                        else -> y + 10f
                    }
                    drawText(label, drawX, drawY, labelPaint)
                }
            }
        }
    }
}
