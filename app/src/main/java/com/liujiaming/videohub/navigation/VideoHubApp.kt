package com.liujiaming.videohub.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import com.liujiaming.videohub.feature.emby.AddEmbyScreen
import com.liujiaming.videohub.feature.filesource.FileSourceScreen
import com.liujiaming.videohub.feature.filesource.FileSourceTutorialScreen
import com.liujiaming.videohub.feature.filesource.ManageFileSourceScreen
import com.liujiaming.videohub.feature.fnos.AddFnosScreen
import com.liujiaming.videohub.feature.jellyfin.AddJellyfinScreen
import com.liujiaming.videohub.feature.media.MediaLibraryScreen
import com.liujiaming.videohub.feature.settings.AboutScreen
import com.liujiaming.videohub.feature.settings.DownloadSettingsScreen
import com.liujiaming.videohub.feature.settings.GeneralSettingsScreen
import com.liujiaming.videohub.feature.settings.PlaybackSettingsScreen
import com.liujiaming.videohub.feature.settings.PlayerInterfaceSettingsScreen
import com.liujiaming.videohub.feature.settings.ResourceSettingsScreen
import com.liujiaming.videohub.feature.settings.SubtitleTrackSettingsScreen
import com.liujiaming.videohub.feature.server.MediaServerScreen
import com.liujiaming.videohub.feature.settings.SettingsScreen

/**
 * 应用主导航组件。
 * 使用手动维护的返回栈（backStack）来管理页面导航，
 * 支持页面跳转（navigateTo）、根页面切换（switchRoot）和返回（goBack）操作。
 * 同时处理系统返回键事件，实现双击退出功能。
 */
@Composable
fun VideoHubApp() {
    // 获取当前上下文，用于处理返回键事件和 Toast 提示
    val context = LocalContext.current
    // 手动维护的导航返回栈，初始页面为媒体库页面
    val backStack = remember { mutableListOf(VideoHubScreen.MediaLibrary).toMutableStateList() }
    // 当前显示的页面，取返回栈的最后一个元素
    val currentScreen = backStack.last()
    // 记录上一次按下返回键的时间戳，用于双击退出判断
    var lastExitPressTime by remember { mutableStateOf(0L) }

    /**
     * 导航到指定页面，将新页面压入返回栈。
     * 如果目标页面与当前页面相同则忽略操作。
     * @param screen 目标页面枚举值
     */
    fun navigateTo(screen: VideoHubScreen) {
        if (currentScreen == screen) return
        backStack.add(screen)
    }

    /**
     * 切换根页面，清空返回栈并将目标页面作为新的根页面。
     * 用于底部导航栏切换主要功能模块时使用。
     * @param screen 目标根页面枚举值
     */
    fun switchRoot(screen: VideoHubScreen) {
        if (currentScreen == screen) return
        backStack.clear()
        backStack.add(screen)
    }

    /**
     * 返回上一页，从返回栈中移除当前页面。
     * 仅在返回栈中有多个页面时生效。
     */
    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    // 处理系统返回键事件
    BackHandler {
        // 如果返回栈中有多个页面，直接返回上一页
        if (backStack.size > 1) {
            goBack()
            return@BackHandler
        }

        // 已在根页面，实现双击退出逻辑
        val now = System.currentTimeMillis()
        if (now - lastExitPressTime <= EXIT_CONFIRM_INTERVAL_MS) {
            // 两次按键间隔小于 2 秒，退出应用
            (context as? Activity)?.finish()
        } else {
            // 第一次按下返回键，提示用户再按一次退出
            lastExitPressTime = now
            Toast.makeText(context, "再按一次退出app", Toast.LENGTH_SHORT).show()
        }
    }

    // 根据当前页面枚举值渲染对应的 Composable 界面
    when (currentScreen) {
        // 媒体库页面 - 应用首页，展示媒体内容
        VideoHubScreen.MediaLibrary -> MediaLibraryScreen(
            onAddFileSourceClick = { navigateTo(VideoHubScreen.FileSource) },
            onAddServerClick = { navigateTo(VideoHubScreen.ServerList) },
            onFileClick = { switchRoot(VideoHubScreen.FileSource) },
            onServerClick = { switchRoot(VideoHubScreen.ServerList) },
            onSettingsClick = { switchRoot(VideoHubScreen.Settings) }
        )

        // 影视服务器列表页面
        VideoHubScreen.ServerList -> MediaServerScreen(
            onEmbyClick = { navigateTo(VideoHubScreen.AddEmby) },
            onEditConnectedEmbyClick = { navigateTo(VideoHubScreen.AddEmby) },
            onJellyfinClick = { navigateTo(VideoHubScreen.AddJellyfin) },
            onFnosClick = { navigateTo(VideoHubScreen.AddFnos) },
            onMediaClick = { switchRoot(VideoHubScreen.MediaLibrary) },
            onFileClick = { switchRoot(VideoHubScreen.FileSource) },
            onSettingsClick = { switchRoot(VideoHubScreen.Settings) }
        )

        // 添加 Emby 服务器页面
        VideoHubScreen.AddEmby -> AddEmbyScreen(
            onBackClick = ::goBack
        )

        // 添加 Jellyfin 服务器页面
        VideoHubScreen.AddJellyfin -> AddJellyfinScreen(
            onBackClick = ::goBack
        )

        // 添加 FnOS 服务器页面
        VideoHubScreen.AddFnos -> AddFnosScreen(
            onBackClick = ::goBack
        )

        // 文件源页面 - 展示本地文件源列表
        VideoHubScreen.FileSource -> FileSourceScreen(
            onAddFileSourceClick = { navigateTo(VideoHubScreen.ManageFileSource) },
            onTutorialClick = { navigateTo(VideoHubScreen.FileSourceTutorial) },
            onMediaClick = { switchRoot(VideoHubScreen.MediaLibrary) },
            onServerClick = { switchRoot(VideoHubScreen.ServerList) },
            onSettingsClick = { switchRoot(VideoHubScreen.Settings) }
        )

        // 文件源教程页面
        VideoHubScreen.FileSourceTutorial -> FileSourceTutorialScreen(
            onBackClick = ::goBack
        )

        // 管理文件源页面
        VideoHubScreen.ManageFileSource -> ManageFileSourceScreen(
            onBackClick = ::goBack
        )

        // 设置主页面
        VideoHubScreen.Settings -> SettingsScreen(
            onGeneralClick = { navigateTo(VideoHubScreen.GeneralSettings) },
            onResourceClick = { navigateTo(VideoHubScreen.ResourceSettings) },
            onDownloadClick = { navigateTo(VideoHubScreen.DownloadSettings) },
            onPlaybackClick = { navigateTo(VideoHubScreen.PlaybackSettings) },
            onPlayerInterfaceClick = { navigateTo(VideoHubScreen.PlayerInterfaceSettings) },
            onSubtitleTrackClick = { navigateTo(VideoHubScreen.SubtitleTrackSettings) },
            onAboutClick = { navigateTo(VideoHubScreen.About) },
            onMediaClick = { switchRoot(VideoHubScreen.MediaLibrary) },
            onFileClick = { switchRoot(VideoHubScreen.FileSource) },
            onServerClick = { switchRoot(VideoHubScreen.ServerList) }
        )

        // 通用设置页面
        VideoHubScreen.GeneralSettings -> GeneralSettingsScreen(
            onBackClick = ::goBack
        )

        // 资源设置页面
        VideoHubScreen.ResourceSettings -> ResourceSettingsScreen(
            onBackClick = ::goBack
        )

        // 下载设置页面
        VideoHubScreen.DownloadSettings -> DownloadSettingsScreen(
            onBackClick = ::goBack
        )

        // 播放设置页面
        VideoHubScreen.PlaybackSettings -> PlaybackSettingsScreen(
            onBackClick = ::goBack
        )

        // 播放器界面设置页面
        VideoHubScreen.PlayerInterfaceSettings -> PlayerInterfaceSettingsScreen(
            onBackClick = ::goBack
        )

        // 字幕轨道设置页面
        VideoHubScreen.SubtitleTrackSettings -> SubtitleTrackSettingsScreen(
            onBackClick = ::goBack
        )

        // 关于页面
        VideoHubScreen.About -> AboutScreen(
            onBackClick = ::goBack
        )
    }
}

/**
 * 应用页面枚举类，定义所有可导航的页面。
 * 用于导航返回栈的类型安全标识。
 */
private enum class VideoHubScreen {
    /** 媒体库页面（首页） */
    MediaLibrary,
    /** 影视服务器列表页面 */
    ServerList,
    /** 添加 Emby 服务器页面 */
    AddEmby,
    /** 添加 Jellyfin 服务器页面 */
    AddJellyfin,
    /** 添加 FnOS 服务器页面 */
    AddFnos,
    /** 文件源页面 */
    FileSource,
    /** 文件源教程页面 */
    FileSourceTutorial,
    /** 管理文件源页面 */
    ManageFileSource,
    /** 设置主页面 */
    Settings,
    /** 通用设置页面 */
    GeneralSettings,
    /** 资源设置页面 */
    ResourceSettings,
    /** 下载设置页面 */
    DownloadSettings,
    /** 播放设置页面 */
    PlaybackSettings,
    /** 播放器界面设置页面 */
    PlayerInterfaceSettings,
    /** 字幕轨道设置页面 */
    SubtitleTrackSettings,
    /** 关于页面 */
    About
}

/** 退出确认间隔时间（毫秒），两次返回键按下时间小于此值则退出应用 */
private const val EXIT_CONFIRM_INTERVAL_MS = 2000L
