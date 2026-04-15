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

data class TrendPoint(val ageMonths: Int, val ratio: Float)

/**
 * Simple single-series line chart for overall ratio over baby-age-at-check.
 * X-axis = ageMonths, Y-axis = ratio [0, 1].
 */
@Composable
fun TrendLineChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
    gridColor: Color = MaterialTheme.colorScheme.outline,
    axisLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    if (points.size < 2) return

    val density = LocalDensity.current
    val labelSizePx = with(density) { 11.sp.toPx() }
    val axisLabelArgb = axisLabelColor.toArgb()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val leftPad = 48f
            val rightPad = 24f
            val topPad = 24f
            val bottomPad = 40f

            val chartLeft = leftPad
            val chartRight = size.width - rightPad
            val chartTop = topPad
            val chartBottom = size.height - bottomPad
            val chartWidth = chartRight - chartLeft
            val chartHeight = chartBottom - chartTop

            // Grid lines at 0, 25, 50, 75, 100%
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = axisLabelArgb
                textSize = labelSizePx
                typeface = android.graphics.Typeface.DEFAULT
            }
            for (i in 0..4) {
                val y = chartTop + chartHeight * (1f - i / 4f)
                drawLine(
                    color = gridColor.copy(alpha = 0.2f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = 1f,
                )
                val label = "${i * 25}"
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    chartLeft - 8f - paint.measureText(label),
                    y + labelSizePx / 3f,
                    paint,
                )
            }

            // Build data path — if all points share an age, space them evenly by index
            val minAge = points.minOf { it.ageMonths }
            val maxAge = points.maxOf { it.ageMonths }
            val ageSpread = maxAge - minAge
            val spaceByIndex = ageSpread == 0

            fun px(i: Int): Offset {
                val p = points[i]
                val xRatio = if (spaceByIndex) {
                    if (points.size == 1) 0.5f else i.toFloat() / (points.size - 1)
                } else {
                    (p.ageMonths - minAge).toFloat() / ageSpread
                }
                val x = chartLeft + chartWidth * xRatio
                val y = chartTop + chartHeight * (1f - p.ratio.coerceIn(0f, 1f))
                return Offset(x, y)
            }

            val fillPath = Path().apply {
                moveTo(px(0).x, chartBottom)
                for (i in points.indices) {
                    val p = px(i)
                    lineTo(p.x, p.y)
                }
                lineTo(px(points.lastIndex).x, chartBottom)
                close()
            }
            drawPath(fillPath, color = fillColor)

            val linePath = Path().apply {
                val start = px(0)
                moveTo(start.x, start.y)
                for (i in 1 until points.size) {
                    val p = px(i)
                    lineTo(p.x, p.y)
                }
            }
            drawPath(linePath, color = lineColor, style = Stroke(width = 4f))

            // Dots + age labels
            points.forEachIndexed { i, point ->
                val p = px(i)
                drawCircle(lineColor, radius = 6f, center = p)
                drawCircle(Color.White, radius = 3f, center = p)

                val ageLabel = "${point.ageMonths}개월"
                drawContext.canvas.nativeCanvas.drawText(
                    ageLabel,
                    p.x - paint.measureText(ageLabel) / 2,
                    chartBottom + labelSizePx + 8f,
                    paint,
                )
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
