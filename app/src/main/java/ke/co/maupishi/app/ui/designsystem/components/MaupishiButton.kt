package ke.co.maupishi.app.ui.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ke.co.maupishi.app.ui.designsystem.MaupishiTheme

/**
 * The app's primary button. It wraps Material's button today; changing how
 * buttons look later means editing this function, not the screens that use it.
 * It is never shorter than the minimum touch-target size.
 */
@Composable
fun MaupishiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val spacing = MaupishiTheme.spacing
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = spacing.minTouchTarget),
        enabled = enabled,
        shape = MaupishiTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaupishiTheme.colors.primary,
            contentColor = MaupishiTheme.colors.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
    ) {
        MaupishiText(text = text, style = MaupishiTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun MaupishiButtonLightPreview() {
    MaupishiTheme(darkTheme = false) {
        MaupishiButton(text = "Label", onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun MaupishiButtonDarkPreview() {
    MaupishiTheme(darkTheme = true) {
        MaupishiButton(text = "Label", onClick = {})
    }
}
