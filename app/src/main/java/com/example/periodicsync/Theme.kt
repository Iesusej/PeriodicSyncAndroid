package com.example.periodicsync

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme()
    MaterialTheme(colorScheme = colors, content = content)
}
