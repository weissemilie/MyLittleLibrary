package dk.au.emilieweiss.mylittlelibrary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dk.au.emilieweiss.mylittlelibrary.data.model.Book
import dk.au.emilieweiss.mylittlelibrary.ui.theme.extendedColors

/**
 * Bogens forside i forholdet 2:3. Pladsholderkortet i en af temaets farver ligger bagved,
 * så det vises hvis bogen ikke har en forside, eller hvis forsiden ikke kan hentes.
 */
@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extended = MaterialTheme.extendedColors
    val palette = listOf(
        scheme.primaryContainer to scheme.onPrimaryContainer,
        scheme.secondaryContainer to scheme.onSecondaryContainer,
        scheme.tertiaryContainer to scheme.onTertiaryContainer,
        extended.skyBlue to extended.onSkyBlue,
        extended.pink to extended.onPink
    )
    val (background, content) = palette[Math.floorMod(book.title.hashCode(), palette.size)]

    Box(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(MaterialTheme.shapes.medium)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(12.dp)
        )
        if (book.coverUrl.isNotBlank()) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = "Forside på ${book.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
