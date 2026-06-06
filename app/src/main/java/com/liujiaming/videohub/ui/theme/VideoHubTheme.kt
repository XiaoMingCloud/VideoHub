package com.liujiaming.videohub.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val BackgroundGray = Color(0xFFF4F4F6)
val TextGray = Color(0xFF8E8E93)
val DividerGray = Color(0xFFE9E9ED)
val ActiveGreen = Color(0xFF00C853)
val PrimaryText = Color(0xFF111111)
val FormHintGray = Color(0xFFAAAAAA)
val FormBorderGray = Color(0xFFE0E0E0)
val FormInputBackground = Color(0xFFFAFAFA)
val ButtonDisabledGray = Color(0xFFE0E0E0)
val SettingsIconGreen = Color(0xFF2E7D32)

@Composable
fun VideoHubTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundGray,
            content = content
        )
    }
}
