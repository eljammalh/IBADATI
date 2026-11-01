package com.ibadati.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.ibadati.app.R
import com.ibadati.app.data.Status
import com.ibadati.app.data.ThemeMode

/** IBM Plex Sans Arabic covers both Arabic and Latin, so both languages share one balanced look. */
val Plex = FontFamily(
    Font(R.font.plex_regular, FontWeight.Normal),
    Font(R.font.plex_medium, FontWeight.Medium),
    Font(R.font.plex_semibold, FontWeight.SemiBold),
    Font(R.font.plex_bold, FontWeight.Bold),
)

/** Amiri: classical naskh, for decorative headings and phrases. */
val Amiri = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F5E4E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE7DD),
    onPrimaryContainer = Color(0xFF052E25),
    secondary = Color(0xFFA77B22),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4E7C8),
    onSecondaryContainer = Color(0xFF3A2805),
    tertiary = Color(0xFF3F6A8A),
    tertiaryContainer = Color(0xFFD6E6F2),
    onTertiaryContainer = Color(0xFF0E2A40),
    background = Color(0xFFF7F4EC),
    onBackground = Color(0xFF1B1C18),
    surface = Color(0xFFF7F4EC),
    onSurface = Color(0xFF1B1C18),
    surfaceVariant = Color(0xFFEAE5D8),
    onSurfaceVariant = Color(0xFF5A574D),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCFAF4),
    surfaceContainer = Color(0xFFFFFDF8),
    surfaceContainerHigh = Color(0xFFF0ECE1),
    surfaceContainerHighest = Color(0xFFE8E3D6),
    outline = Color(0xFF8D897C),
    outlineVariant = Color(0xFFDCD6C6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD1B6),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF14503F),
    onPrimaryContainer = Color(0xFFCFE7DD),
    secondary = Color(0xFFE6C27A),
    onSecondary = Color(0xFF3A2805),
    secondaryContainer = Color(0xFF5A4413),
    onSecondaryContainer = Color(0xFFF4E7C8),
    tertiary = Color(0xFF9CC5E4),
    tertiaryContainer = Color(0xFF244A66),
    onTertiaryContainer = Color(0xFFD6E6F2),
    background = Color(0xFF0C1311),
    onBackground = Color(0xFFE3E3DE),
    surface = Color(0xFF0C1311),
    onSurface = Color(0xFFE3E3DE),
    surfaceVariant = Color(0xFF26302C),
    onSurfaceVariant = Color(0xFFBCC5C0),
    surfaceContainerLowest = Color(0xFF080D0C),
    surfaceContainerLow = Color(0xFF111917),
    surfaceContainer = Color(0xFF151E1B),
    surfaceContainerHigh = Color(0xFF1D2724),
    surfaceContainerHighest = Color(0xFF26302C),
    outline = Color(0xFF86908B),
    outlineVariant = Color(0xFF34403B),
)

/** Colors for the prayer status highlight and charts. */
@Immutable
data class WorshipColors(
    val mosque: Color,
    val jamaa: Color,
    val onTime: Color,
    val missed: Color,
    val gold: Color,
    val dark: Boolean,
) {
    fun status(s: Int): Color? = when (s) {
        Status.MOSQUE -> mosque
        Status.JAMAA -> jamaa
        Status.ON_TIME -> onTime
        Status.MISSED -> missed
        else -> null
    }
}

private val LightWorship = WorshipColors(
    mosque = Color(0xFF00796B),
    jamaa = Color(0xFFC9971F),
    onTime = Color(0xFF5AAE3C),
    missed = Color(0xFFD04A3A),
    gold = Color(0xFFB8892E),
    dark = false,
)

private val DarkWorship = WorshipColors(
    mosque = Color(0xFF2BB59D),
    jamaa = Color(0xFFE6BB52),
    onTime = Color(0xFF86CF5E),
    missed = Color(0xFFF07563),
    gold = Color(0xFFE6C27A),
    dark = true,
)

val LocalWorshipColors = staticCompositionLocalOf { LightWorship }

private fun Typography.withFont(f: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f),
    titleLarge = titleLarge.copy(fontFamily = f),
    titleMedium = titleMedium.copy(fontFamily = f),
    titleSmall = titleSmall.copy(fontFamily = f),
    bodyLarge = bodyLarge.copy(fontFamily = f),
    bodyMedium = bodyMedium.copy(fontFamily = f),
    bodySmall = bodySmall.copy(fontFamily = f),
    labelLarge = labelLarge.copy(fontFamily = f),
    labelMedium = labelMedium.copy(fontFamily = f),
    labelSmall = labelSmall.copy(fontFamily = f),
)

private val AppTypography = Typography().withFont(Plex)

@Composable
fun isDark(themeMode: Int): Boolean = when (themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun IbadatiTheme(themeMode: Int, content: @Composable () -> Unit) {
    val dark = isDark(themeMode)
    CompositionLocalProvider(LocalWorshipColors provides if (dark) DarkWorship else LightWorship) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
