package ke.co.maupishi.app.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalColors = staticCompositionLocalOf<MaupishiColors> {
    error("MaupishiTheme is missing: wrap this content in MaupishiTheme { }")
}
private val LocalTypography = staticCompositionLocalOf<MaupishiTypography> {
    error("MaupishiTheme is missing: wrap this content in MaupishiTheme { }")
}
private val LocalSpacing = staticCompositionLocalOf<MaupishiSpacing> {
    error("MaupishiTheme is missing: wrap this content in MaupishiTheme { }")
}
private val LocalShapes = staticCompositionLocalOf<MaupishiShapes> {
    error("MaupishiTheme is missing: wrap this content in MaupishiTheme { }")
}

/**
 * The single way screens read design tokens: `MaupishiTheme.colors`,
 * `.typography`, `.spacing` and `.shapes`. The values are provided by the
 * [MaupishiTheme] composable below.
 */
object MaupishiTheme {
    val colors: MaupishiColors
        @Composable @ReadOnlyComposable get() = LocalColors.current

    val typography: MaupishiTypography
        @Composable @ReadOnlyComposable get() = LocalTypography.current

    val spacing: MaupishiSpacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val shapes: MaupishiShapes
        @Composable @ReadOnlyComposable get() = LocalShapes.current
}

/**
 * Applies our design tokens to everything inside [content], following the
 * system light/dark setting.
 *
 * The tokens are the source of truth. They are also translated into Material's
 * color scheme, typography and shapes, so the Material components wrapped inside
 * `ui/designsystem/components` render with our values. Dynamic (wallpaper) color
 * is deliberately not used: the look is decided by us, not by the user's wallpaper.
 */
@Composable
fun MaupishiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) TempDarkColors else TempLightColors
    val materialColors = remember(darkTheme) { colors.toMaterialColorScheme(darkTheme) }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides TempTypography,
        LocalSpacing provides TempSpacing,
        LocalShapes provides TempShapes,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = TempTypography.toMaterialTypography(),
            shapes = TempShapes.toMaterialShapes(),
            content = content,
        )
    }
}

// Material's color scheme has more roles than our tokens. Every role is set
// explicitly so that no default (purple) value from Material can appear.
private fun MaupishiColors.toMaterialColorScheme(darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = surfaceVariant,
        onPrimaryContainer = onSurfaceVariant,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = surfaceVariant,
        onSecondaryContainer = onSurfaceVariant,
        tertiary = primary,
        onTertiary = onPrimary,
        tertiaryContainer = surfaceVariant,
        onTertiaryContainer = onSurfaceVariant,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = primary,
        surfaceBright = surface,
        surfaceDim = background,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surfaceVariant,
        surfaceContainerHigh = surfaceVariant,
        surfaceContainerHighest = surfaceVariant,
        error = error,
        onError = onError,
        errorContainer = surfaceVariant,
        onErrorContainer = onSurfaceVariant,
        outline = outline,
        outlineVariant = outline,
    )
}

// Material text styles we do not define yet still get DM Sans, so Material
// components can never fall back to the system font.
private fun MaupishiTypography.toMaterialTypography(): Typography {
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = DmSans),
        displayMedium = base.displayMedium.copy(fontFamily = DmSans),
        displaySmall = base.displaySmall.copy(fontFamily = DmSans),
        headlineLarge = base.headlineLarge.copy(fontFamily = DmSans),
        headlineMedium = base.headlineMedium.copy(fontFamily = DmSans),
        headlineSmall = base.headlineSmall.copy(fontFamily = DmSans),
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = base.titleSmall.copy(fontFamily = DmSans),
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = base.bodySmall.copy(fontFamily = DmSans),
        labelLarge = labelLarge,
        labelMedium = base.labelMedium.copy(fontFamily = DmSans),
        labelSmall = base.labelSmall.copy(fontFamily = DmSans),
    )
}

private fun MaupishiShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = small,
    small = small,
    medium = medium,
    large = large,
    extraLarge = large,
)
