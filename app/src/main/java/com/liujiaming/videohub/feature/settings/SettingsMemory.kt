package com.liujiaming.videohub.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SettingsMemory {
    private const val PREFERENCES_NAME = "video_hub_settings"

    private var preferences: SharedPreferences? = null

    var hideSpoilers by mutableStateOf(false)
        private set
    var hapticFeedback by mutableStateOf(true)
        private set
    var mediaSort by mutableStateOf("添加日期 ↓")
        private set
    var fileSort by mutableStateOf("添加日期 ↓")
        private set
    var appearance by mutableStateOf("自动")
        private set
    var resolvedDarkMode by mutableStateOf(false)
        private set
    var onlineMetadataLanguage by mutableStateOf(MetadataLanguageOptions.AUTO_SYSTEM_LANGUAGE)
        private set
    var allowCellular by mutableStateOf(false)
        private set
    var cellularSwitchReminder by mutableStateOf(true)
        private set
    var downloadConcurrency by mutableStateOf(3)
        private set
    var showFloatingWindow by mutableStateOf(true)
        private set
    var pauseWhenPlaying by mutableStateOf(false)
        private set

    var fastForwardInterval by mutableStateOf("10秒")
        private set
    var rewindInterval by mutableStateOf("10秒")
        private set
    var cloudVideoOpenMode by mutableStateOf("播放原画")
        private set
    var resumePlaybackMode by mutableStateOf("每次询问")
        private set
    var hardwareAcceleration by mutableStateOf(true)
        private set
    var defaultPlaybackSpeed by mutableStateOf("1.0x")
        private set
    var subtitleOffset by mutableStateOf("0秒")
        private set
    var continuousPlayback by mutableStateOf(true)
        private set
    var rememberPlaybackProgress by mutableStateOf(true)
        private set
    var backgroundPlayback by mutableStateOf(false)
        private set
    var autoDeinterlace by mutableStateOf(false)
        private set

    var autoHideControlPanel by mutableStateOf("5秒")
        private set
    var progressRightTime by mutableStateOf("视频总时长")
        private set
    var longPressSpeed by mutableStateOf("2x")
        private set
    var doubleTapSeek by mutableStateOf(true)
        private set
    var defaultLandscape by mutableStateOf(false)
        private set

    var externalSubtitleRule by mutableStateOf(SubtitleTrackOptions.DEFAULT_EXTERNAL_SUBTITLE_RULE)
        private set
    var autoLoadBuiltInSubtitle by mutableStateOf(true)
        private set
    var builtInSubtitleLanguage by mutableStateOf("简体中文")
        private set
    var onlineSubtitleLanguage by mutableStateOf("简体中文")
        private set
    var defaultAudioLanguage by mutableStateOf("简体中文")
        private set

    fun init(context: Context) {
        if (preferences != null) return

        preferences = context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        hideSpoilers = readBoolean("hideSpoilers", hideSpoilers)
        hapticFeedback = readBoolean("hapticFeedback", hapticFeedback)
        mediaSort = readString("mediaSort", mediaSort)
        fileSort = readString("fileSort", fileSort)
        appearance = readString("appearance", appearance)
        onlineMetadataLanguage = MetadataLanguageOptions.normalize(
            readString("onlineMetadataLanguage", onlineMetadataLanguage)
        )
        updateString("onlineMetadataLanguage", onlineMetadataLanguage) { onlineMetadataLanguage = it }
        allowCellular = readBoolean("allowCellular", allowCellular)
        cellularSwitchReminder = readBoolean("cellularSwitchReminder", cellularSwitchReminder)
        downloadConcurrency = readInt("downloadConcurrency", downloadConcurrency)
        showFloatingWindow = readBoolean("showFloatingWindow", showFloatingWindow)
        pauseWhenPlaying = readBoolean("pauseWhenPlaying", pauseWhenPlaying)

        fastForwardInterval = readString("fastForwardInterval", fastForwardInterval)
        rewindInterval = readString("rewindInterval", rewindInterval)
        cloudVideoOpenMode = readString("cloudVideoOpenMode", cloudVideoOpenMode)
        resumePlaybackMode = readString("resumePlaybackMode", resumePlaybackMode)
        hardwareAcceleration = readBoolean("hardwareAcceleration", hardwareAcceleration)
        defaultPlaybackSpeed = readString("defaultPlaybackSpeed", defaultPlaybackSpeed)
        subtitleOffset = readString("subtitleOffset", subtitleOffset)
        continuousPlayback = readBoolean("continuousPlayback", continuousPlayback)
        rememberPlaybackProgress = readBoolean("rememberPlaybackProgress", rememberPlaybackProgress)
        backgroundPlayback = readBoolean("backgroundPlayback", backgroundPlayback)
        autoDeinterlace = readBoolean("autoDeinterlace", autoDeinterlace)

        autoHideControlPanel = PlayerInterfaceOptions.normalizeAutoHideControlPanel(
            readString("autoHideControlPanel", autoHideControlPanel)
        )
        updateString("autoHideControlPanel", autoHideControlPanel) { autoHideControlPanel = it }
        progressRightTime = PlayerInterfaceOptions.normalizeProgressRightTime(
            readString("progressRightTime", progressRightTime)
        )
        updateString("progressRightTime", progressRightTime) { progressRightTime = it }
        longPressSpeed = PlayerInterfaceOptions.normalizeLongPressSpeed(
            readString("longPressSpeed", longPressSpeed)
        )
        updateString("longPressSpeed", longPressSpeed) { longPressSpeed = it }
        doubleTapSeek = readBoolean("doubleTapSeek", doubleTapSeek)
        defaultLandscape = readBoolean("defaultLandscape", defaultLandscape)

        externalSubtitleRule = SubtitleTrackOptions.normalizeExternalSubtitleRule(
            readString("externalSubtitleRule", externalSubtitleRule)
        )
        updateString("externalSubtitleRule", externalSubtitleRule) { externalSubtitleRule = it }
        autoLoadBuiltInSubtitle = readBoolean("autoLoadBuiltInSubtitle", autoLoadBuiltInSubtitle)
        builtInSubtitleLanguage = readString("builtInSubtitleLanguage", builtInSubtitleLanguage)
        onlineSubtitleLanguage = readString("onlineSubtitleLanguage", onlineSubtitleLanguage)
        defaultAudioLanguage = readString("defaultAudioLanguage", defaultAudioLanguage)
    }

    fun updateHideSpoilers(value: Boolean) = updateBoolean("hideSpoilers", value) { hideSpoilers = it }
    fun updateHapticFeedback(value: Boolean) = updateBoolean("hapticFeedback", value) { hapticFeedback = it }
    fun updateAppearance(value: String) = updateString("appearance", value) { appearance = it }
    fun updateOnlineMetadataLanguage(value: String) {
        updateString("onlineMetadataLanguage", MetadataLanguageOptions.normalize(value)) {
            onlineMetadataLanguage = it
        }
    }
    fun updateAllowCellular(value: Boolean) = updateBoolean("allowCellular", value) { allowCellular = it }
    fun updateCellularSwitchReminder(value: Boolean) = updateBoolean("cellularSwitchReminder", value) { cellularSwitchReminder = it }
    fun updateDownloadConcurrency(value: Int) = updateInt("downloadConcurrency", value) { downloadConcurrency = it }
    fun updateShowFloatingWindow(value: Boolean) = updateBoolean("showFloatingWindow", value) { showFloatingWindow = it }
    fun updatePauseWhenPlaying(value: Boolean) = updateBoolean("pauseWhenPlaying", value) { pauseWhenPlaying = it }

    fun updateFastForwardInterval(value: String) = updateString("fastForwardInterval", value) { fastForwardInterval = it }
    fun updateRewindInterval(value: String) = updateString("rewindInterval", value) { rewindInterval = it }
    fun updateCloudVideoOpenMode(value: String) = updateString("cloudVideoOpenMode", value) { cloudVideoOpenMode = it }
    fun updateResumePlaybackMode(value: String) = updateString("resumePlaybackMode", value) { resumePlaybackMode = it }
    fun updateHardwareAcceleration(value: Boolean) = updateBoolean("hardwareAcceleration", value) { hardwareAcceleration = it }
    fun updateContinuousPlayback(value: Boolean) = updateBoolean("continuousPlayback", value) { continuousPlayback = it }
    fun updateRememberPlaybackProgress(value: Boolean) = updateBoolean("rememberPlaybackProgress", value) { rememberPlaybackProgress = it }
    fun updateBackgroundPlayback(value: Boolean) = updateBoolean("backgroundPlayback", value) { backgroundPlayback = it }
    fun updateAutoDeinterlace(value: Boolean) = updateBoolean("autoDeinterlace", value) { autoDeinterlace = it }

    fun updateAutoHideControlPanel(value: String) {
        updateString("autoHideControlPanel", PlayerInterfaceOptions.normalizeAutoHideControlPanel(value)) {
            autoHideControlPanel = it
        }
    }
    fun updateProgressRightTime(value: String) {
        updateString("progressRightTime", PlayerInterfaceOptions.normalizeProgressRightTime(value)) {
            progressRightTime = it
        }
    }
    fun updateDoubleTapSeek(value: Boolean) = updateBoolean("doubleTapSeek", value) { doubleTapSeek = it }
    fun updateDefaultLandscape(value: Boolean) = updateBoolean("defaultLandscape", value) { defaultLandscape = it }
    fun updateLongPressSpeed(value: String) {
        updateString("longPressSpeed", PlayerInterfaceOptions.normalizeLongPressSpeed(value)) {
            longPressSpeed = it
        }
    }

    fun updateExternalSubtitleRule(value: String) {
        updateString("externalSubtitleRule", SubtitleTrackOptions.normalizeExternalSubtitleRule(value)) {
            externalSubtitleRule = it
        }
    }
    fun updateAutoLoadBuiltInSubtitle(value: Boolean) = updateBoolean("autoLoadBuiltInSubtitle", value) { autoLoadBuiltInSubtitle = it }
    fun updateBuiltInSubtitleLanguage(value: String) = updateString("builtInSubtitleLanguage", value) { builtInSubtitleLanguage = it }
    fun updateOnlineSubtitleLanguage(value: String) = updateString("onlineSubtitleLanguage", value) { onlineSubtitleLanguage = it }
    fun updateDefaultAudioLanguage(value: String) = updateString("defaultAudioLanguage", value) { defaultAudioLanguage = it }

    fun updateResolvedDarkMode(value: Boolean) {
        resolvedDarkMode = value
    }

    private fun readBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences?.getBoolean(key, defaultValue) ?: defaultValue
    }

    private fun readInt(key: String, defaultValue: Int): Int {
        return preferences?.getInt(key, defaultValue) ?: defaultValue
    }

    private fun readString(key: String, defaultValue: String): String {
        return preferences?.getString(key, defaultValue) ?: defaultValue
    }

    private fun updateBoolean(key: String, value: Boolean, updateState: (Boolean) -> Unit) {
        updateState(value)
        preferences?.edit()?.putBoolean(key, value)?.apply()
    }

    private fun updateInt(key: String, value: Int, updateState: (Int) -> Unit) {
        updateState(value)
        preferences?.edit()?.putInt(key, value)?.apply()
    }

    private fun updateString(key: String, value: String, updateState: (String) -> Unit) {
        updateState(value)
        preferences?.edit()?.putString(key, value)?.apply()
    }
}
