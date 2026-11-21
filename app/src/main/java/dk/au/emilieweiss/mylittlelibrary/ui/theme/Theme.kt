package dk.au.emilieweiss.mylittlelibrary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = Cocoa,
    primaryContainer = Color(0xFFFBE0CC),
    onPrimaryContainer = Cocoa,
    secondary = Sage,
    onSecondary = Cocoa,
    secondaryContainer = Color(0xFFE3ECCF),
    onSecondaryContainer = Cocoa,
    tertiary = Honey,
    onTertiary = Cocoa,
    tertiaryContainer = Color(0xFFFBEBBF),
    onTertiaryContainer = Cocoa,
    background = Cream,
    onBackground = Cocoa,
    surface = CreamSurface,
    onSurface = Cocoa,
    surfaceVariant = Color(0xFFF3E7D3),
    onSurfaceVariant = Color(0xFF6B5546),
    surfaceTint = Orange,
    outline = Color(0xFFA38E7C),
    outlineVariant = Color(0xFFE5D6BF),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    surfaceContainerLowest = CreamSurface,
    surfaceContainerLow = CreamSurface,
    surfaceContainer = Color(0xFFFBF1E0),
    surfaceContainerHigh = Color(0xFFF7EBD6),
    surfaceContainerHighest = Color(0xFFF3E7D3)
)

@Composable
fun MyLittleLibraryTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtendedColors provides LightExtendedColors) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
