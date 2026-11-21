package dk.au.emilieweiss.mylittlelibrary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import dk.au.emilieweiss.mylittlelibrary.R

val YoungSerif = FontFamily(Font(R.font.young_serif_regular, FontWeight.Normal))

// DM Sans er en variabel skrifttype, så vægtene sættes via variationSettings.
@OptIn(ExperimentalTextApi::class)
val DmSans = FontFamily(
    listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold
    ).map { weight ->
        Font(
            resId = R.font.dm_sans,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
        )
    }
)

private val Base = Typography()

val Typography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    displayMedium = Base.displayMedium.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    displaySmall = Base.displaySmall.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    headlineLarge = Base.headlineLarge.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    headlineMedium = Base.headlineMedium.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    headlineSmall = Base.headlineSmall.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    titleLarge = Base.titleLarge.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    titleMedium = Base.titleMedium.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    titleSmall = Base.titleSmall.copy(fontFamily = YoungSerif, fontWeight = FontWeight.Normal),
    bodyLarge = Base.bodyLarge.copy(fontFamily = DmSans),
    bodyMedium = Base.bodyMedium.copy(fontFamily = DmSans),
    bodySmall = Base.bodySmall.copy(fontFamily = DmSans),
    labelLarge = Base.labelLarge.copy(fontFamily = DmSans),
    labelMedium = Base.labelMedium.copy(fontFamily = DmSans),
    labelSmall = Base.labelSmall.copy(fontFamily = DmSans)
)
