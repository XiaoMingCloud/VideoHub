package com.liujiaming.videohub

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.liujiaming.videohub.feature.settings.SettingsMemory
import com.liujiaming.videohub.navigation.VideoHubApp
import com.liujiaming.videohub.ui.theme.VideoHubTheme

/**
 * 应用主 Activity，作为整个应用的唯一入口。
 * 负责初始化设置记忆（SharedPreferences）并设置 Compose UI 内容。
 */
class MainActivity : ComponentActivity() {
    /**
     * Activity 创建时的回调方法。
     * - 初始化 [SettingsMemory]，读取用户偏好设置
     * - 通过 [setContent] 设置 Compose 界面，应用 [VideoHubTheme] 主题包裹 [VideoHubApp] 导航组件
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        // 初始化设置记忆，传入当前 Activity 上下文用于 SharedPreferences 访问
        SettingsMemory.init(this)
        setContent {
            // 应用主题包裹，根据用户设置自动切换亮色/暗色模式
            VideoHubTheme {
                // 主导航组件，管理所有页面的切换与返回栈
                VideoHubApp()
            }
        }
    }
}
