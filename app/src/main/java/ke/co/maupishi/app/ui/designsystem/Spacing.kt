package ke.co.maupishi.app.ui.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Named gaps and sizes, read through [MaupishiTheme.spacing]. Screens use
 * these names instead of writing `dp` values.
 */
data class MaupishiSpacing(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    /** Smallest comfortable size for anything the user taps. */
    val minTouchTarget: Dp,
)

// TEMP: placeholder scale until the look is decided.
internal val TempSpacing = MaupishiSpacing(
    xs = 4.dp,
    sm = 8.dp,
    md = 16.dp,
    lg = 24.dp,
    xl = 32.dp,
    minTouchTarget = 48.dp,
)
