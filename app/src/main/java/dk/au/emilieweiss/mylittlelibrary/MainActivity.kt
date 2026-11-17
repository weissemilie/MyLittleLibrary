package dk.au.emilieweiss.mylittlelibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dk.au.emilieweiss.mylittlelibrary.ui.navigation.AppNavigation
import dk.au.emilieweiss.mylittlelibrary.ui.theme.MyLittleLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyLittleLibraryTheme {
                AppNavigation()
            }
        }
    }
}