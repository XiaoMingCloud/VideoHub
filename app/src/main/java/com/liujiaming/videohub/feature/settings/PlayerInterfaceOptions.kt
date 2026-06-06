package com.liujiaming.videohub.feature.settings

/**
 * 播放器界面设置选项定义。
 * 包含控制面板自动隐藏时长、进度条右侧时间显示模式、
 * 长按快进倍速等选项，并提供标准化方法。
 */
object PlayerInterfaceOptions {
    /** 控制面板自动隐藏时长选项 */
    val autoHideControlPanelDurations = listOf(
        "1秒",
        "3秒",
        "5秒",
        "10秒"
    )

    /** 进度条右侧时间显示模式选项 */
    val progressRightTimeModes = listOf(
        "视频总时长",
        "视频剩余时长"
    )

    /** 长按快进倍速选项 */
    val longPressSpeeds = listOf(
        "2x",
        "4x",
        "6x",
        "8x"
    )

    /** 标准化自动隐藏时长，无效值回退为 5秒 */
    fun normalizeAutoHideControlPanel(value: String): String {
        return if (value in autoHideControlPanelDurations) value else "5秒"
    }

    /** 标准化进度条右侧时间模式，无效值回退为“视频总时长” */
    fun normalizeProgressRightTime(value: String): String {
        return if (value in progressRightTimeModes) value else "视频总时长"
    }

    /** 标准化长按快进倍速，无效值回退为 2x */
    fun normalizeLongPressSpeed(value: String): String {
        return if (value in longPressSpeeds) value else "2x"
    }
}
