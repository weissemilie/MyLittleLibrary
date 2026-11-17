package dk.au.emilieweiss.mylittlelibrary.ui.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dk.au.emilieweiss.mylittlelibrary.ui.components.PlaceholderScreen

@Composable
fun LoginScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    PlaceholderScreen("Log ind") {
        Button(onClick = onLoginClick) { Text("Log ind") }
        TextButton(onClick = onRegisterClick) { Text("Opret bruger") }
    }
}