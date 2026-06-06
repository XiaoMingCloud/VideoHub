package com.liujiaming.videohub.feature.settings

object PlayerInterfaceOptions {
    val autoHideControlPanelDurations = listOf(
        "1秒",
        "3秒",
        "5秒",
        "10秒"
    )

    val progressRightTimeModes = listOf(
        "视频总时长",
        "视频剩余时长"
    )

    val longPressSpeeds = listOf(
        "2x",
        "4x",
        "6x",
        "8x"
    )

    fun normalizeAutoHideControlPanel(value: String): String {
        return if (value in autoHideControlPanelDurations) value else "5秒"
    }

    fun normalizeProgressRightTime(value: String): String {
        return if (value in progressRightTimeModes) value else "视频总时长"
    }

    fun normalizeLongPressSpeed(value: String): String {
        return if (value in longPressSpeeds) value else "2x"
    }
}
