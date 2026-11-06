package com.complejidad.rutam401.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Universidad Católica de Oriente colors: Primary and Gold are sampled from the official logo. */
object AppColors {
    val Primary = Color(0xFF007851)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryMuted = Color(0xFFDCEFE7)
    val PrimarySurface = Color(0xFF005F40)
    val Gold = Color(0xFFE9C303)
    val Accent = Primary
    val Background = Color(0xFFF4F5F0)
    val Surface = Color(0xFFFFFFFF)
    val Text = Color(0xFF12211B)
    val TextMuted = Color(0xFF55615B)
    val Border = Color(0xFFC8CEC9)
    val Correct = Color(0xFF1E7B45)
    val Wrong = Color(0xFFB3261E)
    val WarningBackground = Color(0xFFF9DEDC)
    val WarningText = Color(0xFF8C1D18)
}

@Composable
fun RutaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppColors.Primary,
            onPrimary = AppColors.OnPrimary,
            background = AppColors.Background,
            surface = AppColors.Surface,
            onBackground = AppColors.Text,
            onSurface = AppColors.Text,
        ),
        content = content,
    )
}
