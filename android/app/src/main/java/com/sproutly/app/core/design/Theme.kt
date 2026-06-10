package com.sproutly.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = LeafMint,
    onPrimary = BgDeep,
    primaryContainer = LeafDeep,
    onPrimaryContainer = LeafMint,
    secondary = LeafGreen,
    onSecondary = BgDeep,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgElevated,
    onSurfaceVariant = TextMuted,
    outline = Divider,
    error = Error,
    onError = BgDeep,
)

@Composable
fun SproutlyTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = SproutlyTypography,
        content = content,
    )
}
