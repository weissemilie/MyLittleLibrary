package dk.au.emilieweiss.mylittlelibrary.ui.library

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dk.au.emilieweiss.mylittlelibrary.ui.components.PlaceholderScreen

@Composable
fun LibraryScreen(onBookClick: (String) -> Unit) {
    PlaceholderScreen("Mit bibliotek") {
        Button(onClick = { onBookClick("test123") }) { Text("Åbn testbog") }
    }
}