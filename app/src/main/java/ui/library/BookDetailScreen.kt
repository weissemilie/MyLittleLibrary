package dk.au.emilieweiss.mylittlelibrary.ui.library

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dk.au.emilieweiss.mylittlelibrary.ui.components.PlaceholderScreen

@Composable
fun BookDetailScreen(bookId: String, onBackClick: () -> Unit) {
    PlaceholderScreen("Bog $bookId") {
        TextButton(onClick = onBackClick) { Text("Tilbage") }
    }
}