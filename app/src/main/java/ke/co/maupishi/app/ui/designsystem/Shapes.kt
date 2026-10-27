package ke.co.maupishi.app.ui.designsystem

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Corner shapes screens may use, read through [MaupishiTheme.shapes].
 * They are corner-based because Material's components, used inside our own,
 * only accept that kind of shape.
 */
data class MaupishiShapes(
    val small: CornerBasedShape,
    val medium: CornerBasedShape,
    val large: CornerBasedShape,
)

// TEMP: placeholder corner radii until the look is decided.
internal val TempShapes = MaupishiShapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)
