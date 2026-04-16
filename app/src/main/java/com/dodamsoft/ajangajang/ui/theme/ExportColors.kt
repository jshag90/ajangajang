package com.dodamsoft.ajangajang.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaType
import com.dodamsoft.ajangajang.domain.model.ResultTier

fun resultTierColor(tier: ResultTier): Color = when (tier) {
    ResultTier.NORMAL -> StateSuccess
    ResultTier.CAUTION -> StateWarning
    ResultTier.CONSULT -> StateDanger
}

fun resultTierArgb(tier: ResultTier): Int = resultTierColor(tier).toArgb()

fun areaAccent(type: DevelopmentAreaType): Color = when (type) {
    DevelopmentAreaType.SOCIAL -> AreaSocialPink
    DevelopmentAreaType.LANGUAGE -> AreaLanguageSky
    DevelopmentAreaType.COGNITIVE -> AreaCognitiveLavender
    DevelopmentAreaType.PHYSICAL -> AreaPhysicalMint
}

fun areaAccentArgb(type: DevelopmentAreaType): Int = areaAccent(type).toArgb()

object BrandArgb {
    val CORAL: Int = Coral40.toArgb()
    val PEACH: Int = Peach40.toArgb()
    val MINT: Int = Mint40.toArgb()
    val CREAM: Int = CreamBackground.toArgb()
    val OUTLINE: Int = CreamOutline.toArgb()
    val DARK_TEXT: Int = OnCream.toArgb()
    val SUB_TEXT: Int = OnCreamVariant.toArgb()
    val BAR_BG: Int = Color(0xFFF5E1D6).toArgb()
    val BADGE_BG: Int = Color(0xFFFFECE4).toArgb()
}
