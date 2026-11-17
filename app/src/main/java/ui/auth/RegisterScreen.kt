package dk.au.emilieweiss.mylittlelibrary.ui.auth

import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dk.au.emilieweiss.mylittlelibrary.ui.components.PlaceholderScreen

@Composable
fun RegisterScreen(onBackClick: () -> Unit) {
    PlaceholderScreen("Opret bruger") {
        TextButton(onClick = onBackClick) { Text("Tilbage") }
    }
}