package com.liujiaming.videohub.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.liujiaming.videohub.feature.settings.AppearanceOptions
import com.liujiaming.videohub.feature.settings.SettingsMemory

/** 背景灰色，根据当前暗色模式动态切换 */
val BackgroundGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF121212) else Color(0xFFF4F4F6)

/** 次要文本灰色，用于辅助文字说明 */
val TextGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF9A9AA1) else Color(0xFF8E8E93)

/** 分割线灰色，用于列表项分隔 */
val DividerGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF2B2B2D) else Color(0xFFE9E9ED)

/** 激活状态绿色，用于底部导航栏选中项等 */
val ActiveGreen = Color(0xFF00C853)

/** 主文本颜色，用于标题和主要内容文字 */
val PrimaryText: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFFF4F4F5) else Color(0xFF111111)

/** 卡片背景色，用于各类卡片组件 */
val CardBackground: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF1C1C1E) else Color.White

/** 页面背景色，用于整个页面的底色 */
val PageBackground: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF121212) else Color.White

/** 表单提示文字灰色，用于输入框占位符等 */
val FormHintGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF77777D) else Color(0xFFAAAAAA)

/** 表单边框灰色 */
val FormBorderGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF333338) else Color(0xFFE0E0E0)

/** 表单输入框背景色 */
val FormInputBackground: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF1E1E21) else Color(0xFFFAFAFA)

/** 按钮禁用状态灰色 */
val ButtonDisabledGray: Color
    get() = if (SettingsMemory.resolvedDarkMode) Color(0xFF3A3A3D) else Color(0xFFE0E0E0)

/** 设置图标绿色，用于设置页面中的图标着色 */
val SettingsIconGreen = Color(0xFF2E7D32)

/**
 * VideoHub 应用主题组件。
 * 根据用户设置的外观模式（亮色/暗色/跟随系统）应用 Material3 主题。
 * 通过 [SideEffect] 同步更新 [SettingsMemory] 中的解析后暗色模式状态，
 * 供其他组件通过颜色属性动态响应主题变化。
 *
 * @param content 主题包裹的子组件内容
 */
@Composable
fun VideoHubTheme(content: @Composable () -> Unit) {
    // 获取系统当前的暗色模式状态
    val systemDark = isSystemInDarkTheme()
    // 根据用户设置的外观选项决定最终是否为暗色模式
    val isDark = when (SettingsMemory.appearance) {
        AppearanceOptions.LIGHT -> false       // 用户强制亮色模式
        AppearanceOptions.DARK -> true         // 用户强制暗色模式
        else -> systemDark                     // 跟随系统设置
    }

    // 在每次组合时同步更新解析后的暗色模式状态，供颜色属性读取
    SideEffect {
        SettingsMemory.updateResolvedDarkMode(isDark)
    }

    // 应用 Material3 主题，根据暗色模式选择对应的配色方案
    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
    ) {
        // 使用 Surface 组件填充整个屏幕并设置背景色
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundGray,
            content = content
        )
    }
}
