package com.liujiaming.videohub.ui.theme

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.liujiaming.videohub.feature.settings.AppearanceOptions
import com.liujiaming.videohub.feature.settings.SettingsMemory

val BackgroundGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF121212) else Color(0xFFF4F4F6)

val TextGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF9A9AA1) else Color(0xFF8E8E93)

val DividerGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF2B2B2D) else Color(0xFFE9E9ED)

val ActiveGreen = Color(0xFF00C853)

val PrimaryText: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFFF4F4F5) else Color(0xFF111111)

val CardBackground: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF1C1C1E) else Color.White

val PageBackground: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF121212) else Color.White

val FormHintGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF77777D) else Color(0xFFAAAAAA)

val FormBorderGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF333338) else Color(0xFFE0E0E0)

val FormInputBackground: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF1E1E21) else Color(0xFFFAFAFA)

val ButtonDisabledGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF3A3A3D) else Color(0xFFE0E0E0)

val SettingsIconGreen = Color(0xFF2E7D32)

@Composable
fun VideoHubTheme(content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val view = LocalView.current
    val isDark = when (SettingsMemory.appearance) {
        AppearanceOptions.LIGHT -> false
        AppearanceOptions.DARK -> true
        else -> systemDark
    }
    val rootBackgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF4F4F6)

    SideEffect {
        SettingsMemory.updateResolvedDarkMode(isDark)
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
    }

    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = rootBackgroundColor,
            content = content
        )
    }
}
