package com.dodamsoft.ajangajang.util.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.dodamsoft.ajangajang.domain.model.AreaScore
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaMeta
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaType
import com.dodamsoft.ajangajang.domain.model.ResultTier
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Renders a 1080 × 1350 share card bitmap for a [CheckResult].
 * Pure Android Canvas — no Compose involvement, so it renders identically from any thread.
 */
object ResultImageExporter {

    private const val WIDTH = 1080
    private const val HEIGHT = 1350
    private const val PADDING = 64f

    private const val CREAM = 0xFFFFF9F5.toInt()
    private const val CORAL = 0xFFE78F7C.toInt()
    private const val PEACH = 0xFFE8A87C.toInt()
    private const val MINT = 0xFF67BFA3.toInt()
    private const val DARK_TEXT = 0xFF3A2C28.toInt()
    private const val SUB_TEXT = 0xFF7A6158.toInt()
    private const val BAR_BG = 0xFFF5E1D6.toInt()
    private const val BADGE_BG = 0xFFFFECE4.toInt()

    fun render(
        @Suppress("UNUSED_PARAMETER") context: Context,
        result: CheckResult,
        childProfile: ChildProfile?,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(CREAM)

        // 1. Top wordmark + date
        drawTopHeader(canvas)

        // 2. Child name + tier badge
        drawChildStrip(canvas, result, childProfile)

        // 3. Giant overall %
        drawOverallPercentage(canvas, result)

        // 4. Area bars (4 rows)
        drawAreaBars(canvas, result.areaScores)

        // 5. Tip card (single line)
        drawTipCard(canvas, result)

        // 6. Footer watermark
        drawFooter(canvas)

        return bitmap
    }

    private fun drawTopHeader(canvas: Canvas) {
        val wordmarkPaint = textPaint(48f, CORAL, bold = true)
        canvas.drawText("아장아장", PADDING, 110f, wordmarkPaint)

        // Mint dot
        canvas.drawCircle(PADDING + wordmarkPaint.measureText("아장아장") + 20, 100f, 12f, fillPaint(MINT))

        // Date top-right
        val datePaint = textPaint(28f, SUB_TEXT)
        val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        val dateWidth = datePaint.measureText(dateText)
        canvas.drawText(dateText, WIDTH - PADDING - dateWidth, 110f, datePaint)
    }

    private fun drawChildStrip(canvas: Canvas, result: CheckResult, childProfile: ChildProfile?) {
        val name = childProfile?.name ?: "우리 아이"
        val age = childProfile?.ageDisplay() ?: "${result.stage.months}개월"
        val namePaint = textPaint(68f, DARK_TEXT, bold = true)
        val nameY = 250f
        // Reserve space for the date strip (~200px) + breathing room
        val maxWidth = WIDTH - PADDING * 2 - 200f
        val fullLine = "$name · $age"
        val displayLine = ellipsize(fullLine, namePaint, maxWidth)
        canvas.drawText(displayLine, PADDING, nameY, namePaint)

        // Tier pill
        val tierLabel = result.overallTier.label
        val tierPaint = textPaint(30f, tierColor(result.overallTier), bold = true)
        val pillLeft = PADDING
        val pillTop = 290f
        val pillPadH = 28f
        val pillPadV = 16f
        val textWidth = tierPaint.measureText(tierLabel)
        val pillRight = pillLeft + textWidth + pillPadH * 2
        val pillBottom = pillTop + 30f + pillPadV * 2
        canvas.drawRoundRect(
            RectF(pillLeft, pillTop, pillRight, pillBottom),
            40f, 40f,
            fillPaint(withAlpha(tierColor(result.overallTier), 0x2F)),
        )
        canvas.drawText(tierLabel, pillLeft + pillPadH, pillBottom - pillPadV - 6, tierPaint)
    }

    private fun drawOverallPercentage(canvas: Canvas, result: CheckResult) {
        val cx = WIDTH / 2f
        val cy = 560f
        val tierC = tierColor(result.overallTier)

        // Outer ring background
        val ringRadius = 220f
        val ringRect = RectF(cx - ringRadius, cy - ringRadius, cx + ringRadius, cy + ringRadius)

        val ringBgPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = BADGE_BG
            strokeWidth = 32f
        }
        canvas.drawCircle(cx, cy, ringRadius, ringBgPaint)

        val ringFgPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = tierC
            strokeWidth = 32f
            strokeCap = Paint.Cap.ROUND
        }
        val sweepAngle = result.overallRatio.coerceIn(0f, 1f) * 360f
        canvas.drawArc(ringRect, -90f, sweepAngle, false, ringFgPaint)

        // Big % text centered
        val percentage = (result.overallRatio * 100).toInt()
        val percentPaint = textPaint(180f, tierC, bold = true)
        val percentText = "$percentage%"
        val textWidth = percentPaint.measureText(percentText)
        canvas.drawText(percentText, cx - textWidth / 2, cy + 50, percentPaint)

        // Label below
        val labelPaint = textPaint(26f, SUB_TEXT)
        val label = "${result.checkedIds.size} / ${result.stage.totalCount()} 항목 충족"
        val labelWidth = labelPaint.measureText(label)
        canvas.drawText(label, cx - labelWidth / 2, cy + 110, labelPaint)
    }

    private fun drawAreaBars(canvas: Canvas, scores: List<AreaScore>) {
        val top = 860f
        val rowHeight = 64f
        val gap = 16f

        scores.forEachIndexed { index, score ->
            val y = top + index * (rowHeight + gap)
            val accent = areaColor(score.type)

            // Label
            val label = DevelopmentAreaMeta.labelOf(score.type)
            canvas.drawText(label, PADDING, y + 36, textPaint(28f, DARK_TEXT, bold = true))

            // Bar
            val barLeft = PADDING + 290f
            val barRight = WIDTH - PADDING - 140f
            val barTop = y + 18f
            val barBottom = barTop + 22f
            canvas.drawRoundRect(
                RectF(barLeft, barTop, barRight, barBottom),
                11f, 11f,
                fillPaint(withAlpha(accent, 0x33)),
            )
            if (score.total > 0) {
                canvas.drawRoundRect(
                    RectF(barLeft, barTop, barLeft + (barRight - barLeft) * score.ratio, barBottom),
                    11f, 11f,
                    fillPaint(accent),
                )
            }

            // Right-aligned percentage
            val pct = (score.ratio * 100).toInt()
            val pctText = "${score.checked}/${score.total} · $pct%"
            val pctPaint = textPaint(24f, SUB_TEXT)
            val pctWidth = pctPaint.measureText(pctText)
            canvas.drawText(pctText, WIDTH - PADDING - pctWidth, y + 36, pctPaint)
        }
    }

    private fun drawTipCard(canvas: Canvas, result: CheckResult) {
        val tipBody = result.stage.growthTips.firstOrNull()?.body ?: return

        val cardLeft = PADDING
        val cardRight = WIDTH - PADDING
        val cardTop = 1180f
        val cardBottom = cardTop + 80f
        val rect = RectF(cardLeft, cardTop, cardRight, cardBottom)
        canvas.drawRoundRect(rect, 24f, 24f, fillPaint(BADGE_BG))

        val iconPaint = textPaint(32f, CORAL)
        canvas.drawText("💡", cardLeft + 24, cardTop + 50, iconPaint)

        val bodyPaint = textPaint(22f, DARK_TEXT)
        val maxWidth = cardRight - cardLeft - 96
        val truncated = ellipsize(tipBody, bodyPaint, maxWidth)
        canvas.drawText(truncated, cardLeft + 80, cardTop + 50, bodyPaint)
    }

    private fun drawFooter(canvas: Canvas) {
        val footerPaint = textPaint(22f, SUB_TEXT)
        val text = "아장아장 앱에서 확인 · 도담소프트"
        val textWidth = footerPaint.measureText(text)
        canvas.drawText(text, WIDTH / 2f - textWidth / 2, HEIGHT - 56f, footerPaint)
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return text.substring(0, end) + ellipsis
    }

    private fun textPaint(sizePx: Float, color: Int, bold: Boolean = false): Paint = Paint().apply {
        isAntiAlias = true
        this.color = color
        textSize = sizePx
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
    }

    private fun fillPaint(color: Int): Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.color = color
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (alpha shl 24) or (color and 0x00FFFFFF)

    private fun tierColor(tier: ResultTier): Int = when (tier) {
        ResultTier.NORMAL -> 0xFF7FC8A9.toInt()
        ResultTier.CAUTION -> 0xFFF4B860.toInt()
        ResultTier.CONSULT -> CORAL
    }

    private fun areaColor(type: DevelopmentAreaType): Int = when (type) {
        DevelopmentAreaType.SOCIAL -> 0xFFF7A8B8.toInt()
        DevelopmentAreaType.LANGUAGE -> 0xFF8CC8E8.toInt()
        DevelopmentAreaType.COGNITIVE -> 0xFFBFA8D9.toInt()
        DevelopmentAreaType.PHYSICAL -> 0xFF9FD9BF.toInt()
    }
}
