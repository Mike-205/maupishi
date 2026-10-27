package ke.co.maupishi.app.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ke.co.maupishi.app.R
import ke.co.maupishi.app.ui.designsystem.MaupishiTheme
import ke.co.maupishi.app.ui.designsystem.components.MaupishiScaffold
import ke.co.maupishi.app.ui.designsystem.components.MaupishiText

/**
 * The app's start screen. For now it only shows the app name; browsing content
 * is added once recipes can be loaded.
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    MaupishiScaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        MaupishiText(
            text = stringResource(R.string.app_name),
            style = MaupishiTheme.typography.titleLarge,
            modifier = Modifier
                .padding(innerPadding)
                .padding(MaupishiTheme.spacing.md),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    MaupishiTheme(darkTheme = false) { HomeScreen() }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeScreenDarkPreview() {
    MaupishiTheme(darkTheme = true) { HomeScreen() }
}
