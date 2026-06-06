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

@Composable
fun VideoHubApp() {
    val context = LocalContext.current
    val backStack = remember { mutableListOf(VideoHubScreen.MediaLibrary).toMutableStateList() }
    val currentScreen = backStack.last()
    var lastExitPressTime by remember { mutableStateOf(0L) }

    fun navigateTo(screen: VideoHubScreen) {
        if (currentScreen == screen) return
        backStack.add(screen)
    }

    fun switchRoot(screen: VideoHubScreen) {
        if (currentScreen == screen) return
        backStack.clear()
        backStack.add(screen)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    BackHandler {
        if (backStack.size > 1) {
            goBack()
            return@BackHandler
        }

        val now = System.currentTimeMillis()
        if (now - lastExitPressTime <= EXIT_CONFIRM_INTERVAL_MS) {
            (context as? Activity)?.finish()
        } else {
            lastExitPressTime = now
            Toast.makeText(context, "再按一次退出app", Toast.LENGTH_SHORT).show()
        }
    }

    when (currentScreen) {
        VideoHubScreen.MediaLibrary -> MediaLibraryScreen(
            onAddFileSourceClick = { navigateTo(VideoHubScreen.FileSource) },
            onAddServerClick = { navigateTo(VideoHubScreen.ServerList) },
            onFileClick = { switchRoot(VideoHubScreen.FileSource) },
            onServerClick = { switchRoot(VideoHubScreen.ServerList) },
            onSettingsClick = { switchRoot(VideoHubScreen.Settings) }
        )

        VideoHubScreen.ServerList -> MediaServerScreen(
            onEmbyClick = { navigateTo(VideoHubScreen.AddEmby) },
            onJellyfinClick = { navigateTo(VideoHubScreen.AddJellyfin) },
            onFnosClick = { navigateTo(VideoHubScreen.AddFnos) },
            onMediaClick = { switchRoot(VideoHubScreen.MediaLibrary) },
            onFileClick = { switchRoot(VideoHubScreen.FileSource) },
            onSettingsClick = { switchRoot(VideoHubScreen.Settings) }
        )

        VideoHubScreen.AddEmby -> AddEmbyScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.AddJellyfin -> AddJellyfinScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.AddFnos -> AddFnosScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.FileSource -> FileSourceScreen(
            onAddFileSourceClick = { navigateTo(VideoHubScreen.ManageFileSource) },
            onTutorialClick = { navigateTo(VideoHubScreen.FileSourceTutorial) },
            onMediaClick = { switchRoot(VideoHubScreen.MediaLibrary) },
            onServerClick = { switchRoot(VideoHubScreen.ServerList) },
            onSettingsClick = { switchRoot(VideoHubScreen.Settings) }
        )

        VideoHubScreen.FileSourceTutorial -> FileSourceTutorialScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.ManageFileSource -> ManageFileSourceScreen(
            onBackClick = ::goBack
        )

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

        VideoHubScreen.GeneralSettings -> GeneralSettingsScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.ResourceSettings -> ResourceSettingsScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.DownloadSettings -> DownloadSettingsScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.PlaybackSettings -> PlaybackSettingsScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.PlayerInterfaceSettings -> PlayerInterfaceSettingsScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.SubtitleTrackSettings -> SubtitleTrackSettingsScreen(
            onBackClick = ::goBack
        )

        VideoHubScreen.About -> AboutScreen(
            onBackClick = ::goBack
        )
    }
}

private enum class VideoHubScreen {
    MediaLibrary,
    ServerList,
    AddEmby,
    AddJellyfin,
    AddFnos,
    FileSource,
    FileSourceTutorial,
    ManageFileSource,
    Settings,
    GeneralSettings,
    ResourceSettings,
    DownloadSettings,
    PlaybackSettings,
    PlayerInterfaceSettings,
    SubtitleTrackSettings,
    About
}

private const val EXIT_CONFIRM_INTERVAL_MS = 2000L
