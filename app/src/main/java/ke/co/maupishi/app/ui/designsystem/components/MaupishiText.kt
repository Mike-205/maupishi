package ke.co.maupishi.app.ui.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import ke.co.maupishi.app.ui.designsystem.MaupishiTheme

/**
 * Text drawn with one of our text styles. Leaving [color] unspecified uses the
 * color of the surface it sits on (set by the scaffold, button, and so on), which
 * keeps the text readable in both light and dark themes.
 */
@Composable
fun MaupishiText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaupishiTheme.typography.bodyLarge,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
    )
}
