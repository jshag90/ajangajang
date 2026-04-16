package com.dodamsoft.ajangajang.util.export

import android.graphics.Paint
import android.graphics.Typeface

internal fun textPaint(sizePx: Float, color: Int, bold: Boolean = false): Paint = Paint().apply {
    isAntiAlias = true
    this.color = color
    textSize = sizePx
    typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
}

internal fun fillPaint(color: Int): Paint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.FILL
    this.color = color
}

internal fun strokePaint(color: Int, strokeWidth: Float): Paint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.STROKE
    this.color = color
    this.strokeWidth = strokeWidth
}

internal fun withAlpha(color: Int, alpha: Int): Int =
    (alpha shl 24) or (color and 0x00FFFFFF)
