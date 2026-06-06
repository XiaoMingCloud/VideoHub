package com.liujiaming.videohub.feature.settings

object PlaybackOptions {
    val seekIntervals = listOf(
        "1秒",
        "3秒",
        "5秒",
        "10秒",
        "15秒",
        "20秒",
        "30秒",
        "60秒",
        "自定义"
    )

    val cloudVideoOpenModes = listOf(
        "播放原画",
        "播放最高分辨率视频流"
    )

    val resumePlaybackModes = listOf(
        "每次询问",
        "继续播放",
        "从头开始"
    )
}
