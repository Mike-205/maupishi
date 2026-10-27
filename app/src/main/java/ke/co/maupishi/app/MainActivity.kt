package ke.co.maupishi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ke.co.maupishi.app.ui.designsystem.MaupishiTheme
import ke.co.maupishi.app.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaupishiTheme {
                HomeScreen()
            }
        }
    }
}
