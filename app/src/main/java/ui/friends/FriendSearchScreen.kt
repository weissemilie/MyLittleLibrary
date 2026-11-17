package dk.au.emilieweiss.mylittlelibrary.ui.friends

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dk.au.emilieweiss.mylittlelibrary.ui.components.PlaceholderScreen

@Composable
fun FriendSearchScreen(onBackClick: () -> Unit) {
    PlaceholderScreen("Søg i venners bøger") {
        TextButton(onClick = onBackClick) { Text("Tilbage") }
    }
}