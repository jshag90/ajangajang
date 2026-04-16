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

data class TrendPoint(val ageMonths: Int, val ratio: Float)

/**
 * Single-series line chart for overall ratio over baby age.
 * X-axis = ageMonths (falls back to index when all points share an age), Y-axis = ratio [0, 1].
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
    val labelPaint = remember(axisLabelArgb, labelSizePx) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = axisLabelArgb
            textSize = labelSizePx
            typeface = android.graphics.Typeface.DEFAULT
        }
    }

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
                    chartLeft - 8f - labelPaint.measureText(label),
                    y + labelSizePx / 3f,
                    labelPaint,
                )
            }

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

            points.forEachIndexed { i, point ->
                val p = px(i)
                drawCircle(lineColor, radius = 6f, center = p)
                drawCircle(Color.White, radius = 3f, center = p)

                val ageLabel = "${point.ageMonths}개월"
                drawContext.canvas.nativeCanvas.drawText(
                    ageLabel,
                    p.x - labelPaint.measureText(ageLabel) / 2,
                    chartBottom + labelSizePx + 8f,
                    labelPaint,
                )
            }
        }
    }
}
