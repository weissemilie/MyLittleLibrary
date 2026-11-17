package dk.au.emilieweiss.mylittlelibrary.ui.friends

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dk.au.emilieweiss.mylittlelibrary.ui.components.PlaceholderScreen

@Composable
fun FriendsScreen(onSearchClick: () -> Unit) {
    PlaceholderScreen("Venner") {
        Button(onClick = onSearchClick) { Text("Søg i venners bøger") }
    }
}