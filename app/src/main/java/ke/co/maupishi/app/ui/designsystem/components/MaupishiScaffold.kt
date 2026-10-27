package ke.co.maupishi.app.ui.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ke.co.maupishi.app.ui.designsystem.MaupishiTheme

/**
 * The basic page structure every screen sits in. It sets the page background and
 * the default text color from the theme, and passes the content the padding it
 * needs to stay clear of the system bars.
 *
 * The content must apply the [PaddingValues] it receives, otherwise it can be
 * drawn underneath the status or navigation bar.
 */
@Composable
fun MaupishiScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaupishiTheme.colors.background,
        contentColor = MaupishiTheme.colors.onBackground,
        content = content,
    )
}
