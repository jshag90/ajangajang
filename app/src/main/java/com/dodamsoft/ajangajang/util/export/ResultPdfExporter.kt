package com.dodamsoft.ajangajang.util.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaMeta
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaType
import com.dodamsoft.ajangajang.domain.model.ResultTier
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ResultPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    // Brand palette (mirrors Compose theme)
    private const val CORAL = 0xFFE78F7C.toInt()
    private const val PEACH = 0xFFE8A87C.toInt()
    private const val MINT = 0xFF67BFA3.toInt()
    private const val CREAM = 0xFFFFF9F5.toInt()
    private const val OUTLINE = 0xFFEBD3C4.toInt()
    private const val DARK_TEXT = 0xFF3A2C28.toInt()
    private const val SUB_TEXT = 0xFF7A6158.toInt()

    fun render(
        @Suppress("UNUSED_PARAMETER") context: Context,
        result: CheckResult,
        childProfile: ChildProfile?,
    ): PdfDocument {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdf.startPage(pageInfo)
        drawPage(page.canvas, result, childProfile)
        pdf.finishPage(page)
        return pdf
    }

    private fun drawPage(
        canvas: Canvas,
        result: CheckResult,
        childProfile: ChildProfile?,
    ) {
        // Background
        canvas.drawColor(CREAM)

        var y = MARGIN

        // Header
        val titlePaint = textPaint(22f, DARK_TEXT, bold = true)
        canvas.drawText("아장아장 발달 체크 결과", MARGIN, y + 20, titlePaint)
        y += 36

        // Child + date strip
        val strip = buildString {
            append(childProfile?.name ?: "우리 아이")
            append(" · ")
            append(childProfile?.ageDisplay() ?: "${result.stage.months}개월")
            append("  |  ")
            append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")))
        }
        canvas.drawText(strip, MARGIN, y + 14, textPaint(11f, SUB_TEXT))
        y += 28

        // Divider
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint(OUTLINE, 1f))
        y += 18

        // Overall score block
        y = drawOverallBlock(canvas, y, result)
        y += 18

        // Area scores
        y = drawAreaScores(canvas, y, result)
        y += 16

        val footerReserve = 40f

        // Unfulfilled items
        if (result.unfulfilledItems.isNotEmpty() && y < PAGE_HEIGHT - footerReserve - 60) {
            y = drawSectionHeader(canvas, y, "확인이 필요한 항목")
            y += 8
            val maxItems = 8
            val items = result.unfulfilledItems.take(maxItems).map { (type, item) ->
                "[${DevelopmentAreaMeta.labelOf(type)}] ${item.text}"
            }
            y = drawBullets(
                canvas = canvas,
                y = y,
                items = items,
                bulletColor = CORAL,
                maxY = PAGE_HEIGHT - footerReserve - 20,
            )
            val overflow = result.unfulfilledItems.size - items.size
            if (overflow > 0 && y < PAGE_HEIGHT - footerReserve - 20) {
                canvas.drawText(
                    "· 외 ${overflow}건 더 보기",
                    MARGIN + 14,
                    y + 12,
                    textPaint(9.5f, SUB_TEXT, bold = true),
                )
                y += 16
            }
            y += 10
        }

        // Doctor questions
        if (result.stage.doctorQuestions.isNotEmpty() && y < PAGE_HEIGHT - footerReserve - 60) {
            y = drawSectionHeader(canvas, y, "의사와 공유할 중요한 사항")
            y += 8
            val maxItems = 5
            val items = result.stage.doctorQuestions.take(maxItems)
            y = drawBullets(
                canvas = canvas,
                y = y,
                items = items,
                bulletColor = MINT,
                maxY = PAGE_HEIGHT - footerReserve - 20,
            )
            val overflow = result.stage.doctorQuestions.size - items.size
            if (overflow > 0 && y < PAGE_HEIGHT - footerReserve - 20) {
                canvas.drawText(
                    "· 외 ${overflow}건 더 보기",
                    MARGIN + 14,
                    y + 12,
                    textPaint(9.5f, SUB_TEXT, bold = true),
                )
                y += 16
            }
            y += 10
        }

        // Growth tips
        if (result.stage.growthTips.isNotEmpty() && y < PAGE_HEIGHT - footerReserve - 60) {
            y = drawSectionHeader(canvas, y, "발달 촉진 팁")
            y += 8
            val maxTips = 3
            val tips = result.stage.growthTips.take(maxTips)
            for (tip in tips) {
                if (y > PAGE_HEIGHT - footerReserve - 24) break
                val title = tip.title?.let { "$it — " } ?: ""
                y = drawWrappedText(
                    canvas = canvas,
                    y = y,
                    text = "· $title${tip.body}",
                    paint = textPaint(9.5f, DARK_TEXT),
                    maxWidth = PAGE_WIDTH - MARGIN * 2 - 8,
                    lineGap = 13f,
                )
                y += 4
            }
            val overflow = result.stage.growthTips.size - tips.size
            if (overflow > 0 && y < PAGE_HEIGHT - footerReserve - 20) {
                canvas.drawText(
                    "외 ${overflow}건 더 보기",
                    MARGIN,
                    y + 12,
                    textPaint(9.5f, SUB_TEXT, bold = true),
                )
            }
        }

        // Footer
        val footerY = PAGE_HEIGHT - 24f
        canvas.drawLine(MARGIN, footerY - 16, PAGE_WIDTH - MARGIN, footerY - 16, linePaint(OUTLINE, 0.5f))
        val footerPaint = textPaint(8.5f, SUB_TEXT)
        canvas.drawText(
            "아장아장 · 도담소프트 · 참고 출처: CDC, 우리아이114",
            MARGIN,
            footerY,
            footerPaint,
        )
    }

    private fun drawOverallBlock(canvas: Canvas, startY: Float, result: CheckResult): Float {
        val percentage = (result.overallRatio * 100).toInt()
        val tierPaint = textPaint(12f, tierColor(result.overallTier), bold = true)
        val percentPaint = textPaint(52f, tierColor(result.overallTier), bold = true)

        // Card bg
        val cardRect = RectF(MARGIN, startY, PAGE_WIDTH - MARGIN, startY + 100)
        canvas.drawRoundRect(cardRect, 16f, 16f, fillPaint(0xFFFFECE4.toInt()))
        canvas.drawRoundRect(cardRect, 16f, 16f, strokePaint(OUTLINE, 1f))

        canvas.drawText("전체 달성률", cardRect.left + 20, cardRect.top + 24, textPaint(11f, SUB_TEXT))
        canvas.drawText("$percentage%", cardRect.left + 20, cardRect.top + 78, percentPaint)
        canvas.drawText(result.overallTier.label, cardRect.left + 130, cardRect.top + 48, tierPaint)
        canvas.drawText(
            "${result.checkedIds.size} / ${result.stage.totalCount()} 항목 충족",
            cardRect.left + 130,
            cardRect.top + 68,
            textPaint(10f, SUB_TEXT),
        )

        // Progress bar on right
        val barLeft = cardRect.right - 200
        val barRight = cardRect.right - 20
        val barTop = cardRect.top + 78
        val barBottom = barTop + 10
        canvas.drawRoundRect(RectF(barLeft, barTop, barRight, barBottom), 5f, 5f, fillPaint(0xFFF5DDD3.toInt()))
        canvas.drawRoundRect(
            RectF(barLeft, barTop, barLeft + (barRight - barLeft) * result.overallRatio, barBottom),
            5f, 5f, fillPaint(tierColor(result.overallTier)),
        )
        return cardRect.bottom
    }

    private fun drawAreaScores(canvas: Canvas, startY: Float, result: CheckResult): Float {
        var y = startY
        y = drawSectionHeader(canvas, y, "영역별 점수")
        y += 10

        val rowHeight = 30f
        val labelWidth = 110f
        val barHeight = 8f
        val rightWidth = 90f

        result.areaScores.forEach { score ->
            val label = "${DevelopmentAreaMeta.labelOf(score.type)}"
            canvas.drawText(label, MARGIN, y + 15, textPaint(10.5f, DARK_TEXT, bold = true))

            val barLeft = MARGIN + labelWidth
            val barRight = PAGE_WIDTH - MARGIN - rightWidth
            val barTop = y + 10
            val barBottom = barTop + barHeight
            val accent = areaColor(score.type)
            canvas.drawRoundRect(
                RectF(barLeft, barTop, barRight, barBottom), 4f, 4f,
                fillPaint(withAlpha(accent, 0x33)),
            )
            if (score.total > 0) {
                canvas.drawRoundRect(
                    RectF(barLeft, barTop, barLeft + (barRight - barLeft) * score.ratio, barBottom),
                    4f, 4f, fillPaint(accent),
                )
            }

            val pct = (score.ratio * 100).toInt()
            canvas.drawText(
                "${score.checked}/${score.total} · $pct%",
                PAGE_WIDTH - MARGIN - rightWidth + 4,
                y + 15,
                textPaint(10f, SUB_TEXT),
            )
            y += rowHeight
        }
        return y
    }

    private fun drawSectionHeader(canvas: Canvas, y: Float, title: String): Float {
        canvas.drawText(title, MARGIN, y + 14, textPaint(13f, DARK_TEXT, bold = true))
        val lineY = y + 19
        canvas.drawLine(MARGIN, lineY, MARGIN + 30, lineY, linePaint(CORAL, 2f))
        return y + 22
    }

    private fun drawBullets(
        canvas: Canvas,
        y: Float,
        items: List<String>,
        bulletColor: Int,
        maxY: Float = PAGE_HEIGHT.toFloat(),
    ): Float {
        var cursor = y
        val paint = textPaint(10f, DARK_TEXT)
        val maxWidth = PAGE_WIDTH - MARGIN * 2 - 14
        for (text in items) {
            if (cursor > maxY) break
            canvas.drawCircle(MARGIN + 4, cursor + 7, 2.2f, fillPaint(bulletColor))
            cursor = drawWrappedText(
                canvas = canvas,
                y = cursor,
                text = text,
                paint = paint,
                maxWidth = maxWidth,
                lineGap = 13f,
                startX = MARGIN + 14,
            )
            cursor += 3
        }
        return cursor
    }

    private fun drawWrappedText(
        canvas: Canvas,
        y: Float,
        text: String,
        paint: Paint,
        maxWidth: Float,
        lineGap: Float,
        startX: Float = MARGIN,
    ): Float {
        val words = text.toCharArray()
        val sb = StringBuilder()
        var cursorY = y
        var start = 0
        while (start < words.size) {
            var end = start
            var lastGood = start
            while (end < words.size) {
                sb.append(words[end])
                if (paint.measureText(sb.toString()) > maxWidth) {
                    if (lastGood == start) {
                        lastGood = end
                    }
                    break
                }
                if (words[end] == ' ') lastGood = end
                end++
            }
            val lineEnd = if (end >= words.size) words.size else (lastGood.takeIf { it > start } ?: end)
            val line = String(words, start, lineEnd - start).trim()
            canvas.drawText(line, startX, cursorY + paint.textSize, paint)
            cursorY += lineGap
            start = lineEnd
            sb.setLength(0)
            while (start < words.size && words[start] == ' ') start++
        }
        return cursorY
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

    private fun strokePaint(color: Int, strokeWidth: Float): Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        this.color = color
        this.strokeWidth = strokeWidth
    }

    private fun linePaint(color: Int, strokeWidth: Float): Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        this.color = color
        this.strokeWidth = strokeWidth
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
