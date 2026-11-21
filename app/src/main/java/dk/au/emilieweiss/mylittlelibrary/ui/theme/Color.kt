package dk.au.emilieweiss.mylittlelibrary.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Kernefarver
val Cream = Color(0xFFFFF7EA)
val CreamSurface = Color(0xFFFFFDF7)
val Orange = Color(0xFFE07A3A)
val Sage = Color(0xFFA8BD7A)
val Honey = Color(0xFFF2C14E)
val Cocoa = Color(0xFF3B2A20)

// Ekstra farver uden for colorScheme (tilgås via MaterialTheme.extendedColors)
val SkyBlue = Color(0xFFA9D3EA)
val Pink = Color(0xFFF4B6CE)

@Immutable
data class ExtendedColors(
    /** Himmelblå: illustrationer og toppen af detaljeskærmen. */
    val skyBlue: Color,
    val onSkyBlue: Color,
    /** Rosa: social accent til avatarer, vennekort og statuschippen "Afventer". */
    val pink: Color,
    val onPink: Color
)

val LightExtendedColors = ExtendedColors(
    skyBlue = SkyBlue,
    onSkyBlue = Cocoa,
    pink = Pink,
    onPink = Cocoa
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
