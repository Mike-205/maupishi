package ke.co.maupishi.app.ui.designsystem

import androidx.compose.ui.graphics.Color

/**
 * The named color roles screens may use. Screens read these through
 * [MaupishiTheme.colors] and never write color values themselves.
 *
 * Each "on" color is the text/icon color meant to sit on the matching surface,
 * so pairs such as [onSurface] on [surface] always keep their checked contrast.
 */
data class MaupishiColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val outline: Color,
    val error: Color,
    val onError: Color,
)

// TEMP: neutral placeholder palette so screens can be built. The brand palette
// is undecided; nothing may depend on these specific values.
// Contrast (WCAG): every text pair is at least 4.5:1, outline and error at least 3:1.
internal val TempLightColors = MaupishiColors(
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF595959),
    primary = Color(0xFF262626),
    onPrimary = Color(0xFFFFFFFF),
    outline = Color(0xFF767676),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
)

// TEMP: see above.
internal val TempDarkColors = MaupishiColors(
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB3B3B3),
    primary = Color(0xFFE6E6E6),
    onPrimary = Color(0xFF121212),
    outline = Color(0xFF8A8A8A),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)
