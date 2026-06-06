package com.liujiaming.videohub.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var currentScreen by remember { mutableStateOf(VideoHubScreen.MediaLibrary) }

    when (currentScreen) {
        VideoHubScreen.MediaLibrary -> MediaLibraryScreen(
            onAddFileSourceClick = { currentScreen = VideoHubScreen.FileSource },
            onAddServerClick = { currentScreen = VideoHubScreen.ServerList },
            onFileClick = { currentScreen = VideoHubScreen.FileSource },
            onServerClick = { currentScreen = VideoHubScreen.ServerList },
            onSettingsClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.ServerList -> MediaServerScreen(
            onEmbyClick = { currentScreen = VideoHubScreen.AddEmby },
            onJellyfinClick = { currentScreen = VideoHubScreen.AddJellyfin },
            onFnosClick = { currentScreen = VideoHubScreen.AddFnos },
            onMediaClick = { currentScreen = VideoHubScreen.MediaLibrary },
            onFileClick = { currentScreen = VideoHubScreen.FileSource },
            onSettingsClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.AddEmby -> AddEmbyScreen(
            onBackClick = { currentScreen = VideoHubScreen.ServerList }
        )

        VideoHubScreen.AddJellyfin -> AddJellyfinScreen(
            onBackClick = { currentScreen = VideoHubScreen.ServerList }
        )

        VideoHubScreen.AddFnos -> AddFnosScreen(
            onBackClick = { currentScreen = VideoHubScreen.ServerList }
        )

        VideoHubScreen.FileSource -> FileSourceScreen(
            onAddFileSourceClick = { currentScreen = VideoHubScreen.ManageFileSource },
            onTutorialClick = { currentScreen = VideoHubScreen.FileSourceTutorial },
            onMediaClick = { currentScreen = VideoHubScreen.MediaLibrary },
            onServerClick = { currentScreen = VideoHubScreen.ServerList },
            onSettingsClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.FileSourceTutorial -> FileSourceTutorialScreen(
            onBackClick = { currentScreen = VideoHubScreen.FileSource }
        )

        VideoHubScreen.ManageFileSource -> ManageFileSourceScreen(
            onBackClick = { currentScreen = VideoHubScreen.FileSource }
        )

        VideoHubScreen.Settings -> SettingsScreen(
            onGeneralClick = { currentScreen = VideoHubScreen.GeneralSettings },
            onResourceClick = { currentScreen = VideoHubScreen.ResourceSettings },
            onDownloadClick = { currentScreen = VideoHubScreen.DownloadSettings },
            onPlaybackClick = { currentScreen = VideoHubScreen.PlaybackSettings },
            onPlayerInterfaceClick = { currentScreen = VideoHubScreen.PlayerInterfaceSettings },
            onSubtitleTrackClick = { currentScreen = VideoHubScreen.SubtitleTrackSettings },
            onAboutClick = { currentScreen = VideoHubScreen.About },
            onMediaClick = { currentScreen = VideoHubScreen.MediaLibrary },
            onFileClick = { currentScreen = VideoHubScreen.FileSource },
            onServerClick = { currentScreen = VideoHubScreen.ServerList }
        )

        VideoHubScreen.GeneralSettings -> GeneralSettingsScreen(
            onBackClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.ResourceSettings -> ResourceSettingsScreen(
            onBackClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.DownloadSettings -> DownloadSettingsScreen(
            onBackClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.PlaybackSettings -> PlaybackSettingsScreen(
            onBackClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.PlayerInterfaceSettings -> PlayerInterfaceSettingsScreen(
            onBackClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.SubtitleTrackSettings -> SubtitleTrackSettingsScreen(
            onBackClick = { currentScreen = VideoHubScreen.Settings }
        )

        VideoHubScreen.About -> AboutScreen(
            onBackClick = { currentScreen = VideoHubScreen.Settings }
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
