package ovh.gabrielhuav.flasklogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ovh.gabrielhuav.flasklogin.ui.navigation.AppNavigation
import ovh.gabrielhuav.flasklogin.ui.theme.FlaskLoginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlaskLoginTheme {
                AppNavigation()
            }
        }
    }
}
