package com.pixelclassics.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PixelClassicsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DosPalette.Black,
            surface = DosPalette.Black,
            primary = DosPalette.NcCyan,
            onBackground = DosPalette.Gray,
            onSurface = DosPalette.Gray,
        ),
        typography = MaterialTheme.typography,
        content = content,
    )
}

@Composable
fun BlackBackground(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DosPalette.Black),
    ) { content() }
}
