package com.antiscroll.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The NVIDIA-style green accent is identical in both themes; everything else
 * (background/surface/text/border) has a dark and a light variant. Screens read colors
 * only through `MaterialTheme.colorScheme.*` (never these raw values directly) so that
 * [AntiScrollTheme]'s `darkTheme` switch actually reaches every screen.
 */
val AntiScrollGreen = Color(0xFF76B900)

private val DarkBackground = Color(0xFF0F0F0F)
private val DarkSurface = Color(0xFF1A1A1A)
private val DarkBorder = Color(0xFF2A2A2A)
private val DarkTextPrimary = Color(0xFFFFFFFF)
private val DarkTextSecondary = Color(0xFF8A8A8A)

private val LightBackground = Color(0xFFFFFFFF)
private val LightSurface = Color(0xFFF2F2F2)
private val LightBorder = Color(0xFFDDDDDD)
private val LightTextPrimary = Color(0xFF0F0F0F)
private val LightTextSecondary = Color(0xFF6B6B6B)

private val AntiScrollDarkColorScheme = darkColorScheme(
    primary = AntiScrollGreen,
    onPrimary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
)

private val AntiScrollLightColorScheme = lightColorScheme(
    primary = AntiScrollGreen,
    onPrimary = Color.Black,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
)

@Composable
fun AntiScrollTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) AntiScrollDarkColorScheme else AntiScrollLightColorScheme,
        content = content,
    )
}
