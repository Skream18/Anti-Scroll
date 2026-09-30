package com.antiscroll.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AntiScrollGreen = Color(0xFF76B900)
val AntiScrollBackground = Color(0xFF0F0F0F)
val AntiScrollSurface = Color(0xFF1A1A1A)
val AntiScrollBorder = Color(0xFF2A2A2A)
val AntiScrollTextPrimary = Color(0xFFFFFFFF)
val AntiScrollTextSecondary = Color(0xFF8A8A8A)

private val AntiScrollColorScheme = darkColorScheme(
    primary = AntiScrollGreen,
    onPrimary = Color.Black,
    background = AntiScrollBackground,
    onBackground = AntiScrollTextPrimary,
    surface = AntiScrollSurface,
    onSurface = AntiScrollTextPrimary,
    surfaceVariant = AntiScrollSurface,
    onSurfaceVariant = AntiScrollTextSecondary,
    outline = AntiScrollBorder,
)

@Composable
fun AntiScrollTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AntiScrollColorScheme,
        content = content,
    )
}
