package dk.au.emilieweiss.mylittlelibrary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(percent = 50), // tekstfelter (pille)
    small = RoundedCornerShape(percent = 50),      // chips og knapper (pille)
    medium = RoundedCornerShape(20.dp),            // kort
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)         // dialoger
)
