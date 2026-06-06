package com.liujiaming.videohub.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 应用设置记忆管理器（全局单例）。
 * 使用 SharedPreferences 持久化所有用户设置，并通过 Compose 的 mutableStateOf 实现响应式更新。
 * 每个设置项都有对应的 update 方法，同时更新内存状态和持久化存储。
 */
object SettingsMemory {
    /** SharedPreferences 文件名 */
    private const val PREFERENCES_NAME = "video_hub_settings"

    private var preferences: SharedPreferences? = null

    // ==================== 通用设置 ====================

    /** 是否隐藏剧透内容 */
    var hideSpoilers by mutableStateOf(false)
        private set
    /** 是否开启触觉反馈 */
    var hapticFeedback by mutableStateOf(true)
        private set
    /** 媒体排序方式 */
    var mediaSort by mutableStateOf("添加日期 ↓")
        private set
    /** 文件排序方式 */
    var fileSort by mutableStateOf("添加日期 ↓")
        private set
    /** 外观模式（自动/浅色/深色） */
    var appearance by mutableStateOf("自动")
        private set
    /** 解析后的暗色模式状态（由主题组件更新） */
    var resolvedDarkMode by mutableStateOf(false)
        private set
    /** 在线元数据语言 */
    var onlineMetadataLanguage by mutableStateOf(MetadataLanguageOptions.AUTO_SYSTEM_LANGUAGE)
        private set
    // ==================== 资源与下载设置 ====================

    /** 是否允许蜂窝网络下载 */
    var allowCellular by mutableStateOf(false)
        private set
    /** 是否显示蜂窝网络切换提醒 */
    var cellularSwitchReminder by mutableStateOf(true)
        private set
    /** 下载并发数 */
    var downloadConcurrency by mutableStateOf(3)
        private set
    /** 是否显示悬浮窗 */
    var showFloatingWindow by mutableStateOf(true)
        private set
    /** 播放时是否暂停其他应用 */
    var pauseWhenPlaying by mutableStateOf(false)
        private set

    // ==================== 播放设置 ====================

    /** 快进时间间隔 */
    var fastForwardInterval by mutableStateOf("10秒")
        private set
    /** 快退时间间隔 */
    var rewindInterval by mutableStateOf("10秒")
        private set
    /** 云端视频打开模式 */
    var cloudVideoOpenMode by mutableStateOf("播放原画")
        private set
    /** 续播模式 */
    var resumePlaybackMode by mutableStateOf("每次询问")
        private set
    /** 是否开启硬件加速 */
    var hardwareAcceleration by mutableStateOf(true)
        private set
    /** 默认播放倍速 */
    var defaultPlaybackSpeed by mutableStateOf("1.0x")
        private set
    /** 字幕偏移时间 */
    var subtitleOffset by mutableStateOf("0秒")
        private set
    /** 是否连续播放 */
    var continuousPlayback by mutableStateOf(true)
        private set
    /** 是否记住播放进度 */
    var rememberPlaybackProgress by mutableStateOf(true)
        private set
    /** 是否允许后台播放 */
    var backgroundPlayback by mutableStateOf(false)
        private set
    /** 是否自动去隔行 */
    var autoDeinterlace by mutableStateOf(false)
        private set

    // ==================== 播放器界面设置 ====================

    /** 控制面板自动隐藏时长 */
    var autoHideControlPanel by mutableStateOf("5秒")
        private set
    /** 进度条右侧时间显示模式 */
    var progressRightTime by mutableStateOf("视频总时长")
        private set
    /** 长按快进倍速 */
    var longPressSpeed by mutableStateOf("2x")
        private set
    /** 是否双击快进/快退 */
    var doubleTapSeek by mutableStateOf(true)
        private set
    /** 是否默认横屏 */
    var defaultLandscape by mutableStateOf(false)
        private set

    // ==================== 字幕和音轨设置 ====================

    /** 外挂字幕加载规则 */
    var externalSubtitleRule by mutableStateOf(SubtitleTrackOptions.DEFAULT_EXTERNAL_SUBTITLE_RULE)
        private set
    /** 是否自动加载内置字幕 */
    var autoLoadBuiltInSubtitle by mutableStateOf(true)
        private set
    /** 内置字幕优先语言 */
    var builtInSubtitleLanguage by mutableStateOf("简体中文")
        private set
    /** 在线字幕搜索语言 */
    var onlineSubtitleLanguage by mutableStateOf("简体中文")
        private set
    /** 默认音轨语言 */
    var defaultAudioLanguage by mutableStateOf("简体中文")
        private set

    /**
     * 初始化设置记忆。
     * 从 SharedPreferences 加载所有设置项到内存中，
     * 对部分字段进行标准化处理。应在 Application 或 MainActivity 创建时调用。
     *
     * @param context 应用上下文
     */
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

    // ==================== 更新方法（同时更新内存和持久化存储） ====================

    /** 更新剧透隐藏设置 */
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

    /** 更新解析后的暗色模式状态（由主题组件调用，不持久化） */
    fun updateResolvedDarkMode(value: Boolean) {
        resolvedDarkMode = value
    }

    // ==================== SharedPreferences 读写工具方法 ====================

    /** 读取布尔值 */
    private fun readBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences?.getBoolean(key, defaultValue) ?: defaultValue
    }

    /** 读取整数值 */
    private fun readInt(key: String, defaultValue: Int): Int {
        return preferences?.getInt(key, defaultValue) ?: defaultValue
    }

    /** 读取字符串值 */
    private fun readString(key: String, defaultValue: String): String {
        return preferences?.getString(key, defaultValue) ?: defaultValue
    }

    /** 更新布尔值（同时更新内存和持久化） */
    private fun updateBoolean(key: String, value: Boolean, updateState: (Boolean) -> Unit) {
        updateState(value)
        preferences?.edit()?.putBoolean(key, value)?.apply()
    }

    /** 更新整数值（同时更新内存和持久化） */
    private fun updateInt(key: String, value: Int, updateState: (Int) -> Unit) {
        updateState(value)
        preferences?.edit()?.putInt(key, value)?.apply()
    }

    /** 更新字符串值（同时更新内存和持久化） */
    private fun updateString(key: String, value: String, updateState: (String) -> Unit) {
        updateState(value)
        preferences?.edit()?.putString(key, value)?.apply()
    }
}
