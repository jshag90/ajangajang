package com.dodamsoft.ajangajang.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Coral40,
    onPrimary = Color.White,
    primaryContainer = CoralContainerLight,
    onPrimaryContainer = OnCoralLight,
    secondary = Peach40,
    onSecondary = Color.White,
    secondaryContainer = PeachContainerLight,
    onSecondaryContainer = OnPeachLight,
    tertiary = Mint40,
    onTertiary = Color.White,
    tertiaryContainer = MintContainerLight,
    onTertiaryContainer = OnMintLight,
    background = CreamBackground,
    onBackground = OnCream,
    surface = CreamSurface,
    onSurface = OnCream,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = OnCreamVariant,
    outline = CreamOutline,
    outlineVariant = CreamOutline,
    error = StateDanger,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Coral80,
    onPrimary = Color(0xFF3A140B),
    primaryContainer = CoralContainerDark,
    onPrimaryContainer = OnCoralDark,
    secondary = Peach80,
    onSecondary = Color(0xFF3A1F05),
    secondaryContainer = PeachContainerDark,
    onSecondaryContainer = OnPeachDark,
    tertiary = Mint80,
    onTertiary = Color(0xFF0B3629),
    tertiaryContainer = MintContainerDark,
    onTertiaryContainer = OnMintDark,
    background = DarkBackground,
    onBackground = OnDark,
    surface = DarkSurface,
    onSurface = OnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = StateDanger,
    onError = Color.White,
)

// 영유아 앱: 전반적으로 둥글고 폭신한 셰이프
private val AjangShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun AjangajangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Dynamic color disabled: 브랜드 파스텔 팔레트를 일관되게 유지
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AjangTypography,
        shapes = AjangShapes,
        content = content,
    )
}
