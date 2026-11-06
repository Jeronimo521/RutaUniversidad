package com.complejidad.rutam401.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** PLACEHOLDER: change these values to the university's colors. */
object AppColors {
    val Primary = Color(0xFF17305A)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryMuted = Color(0xFFD5DDEA)
    val PrimarySurface = Color(0xFF263F6B)
    val Accent = Color(0xFF1B4FD8)
    val Background = Color(0xFFF4F5F0)
    val Surface = Color(0xFFFFFFFF)
    val Text = Color(0xFF10213A)
    val TextMuted = Color(0xFF55617A)
    val Border = Color(0xFFC4CBD6)
    val Correct = Color(0xFF1B4FD8)
    val Wrong = Color(0xFFB23709)
    val WarningBackground = Color(0xFFFBE3D7)
    val WarningText = Color(0xFF7F2905)
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
