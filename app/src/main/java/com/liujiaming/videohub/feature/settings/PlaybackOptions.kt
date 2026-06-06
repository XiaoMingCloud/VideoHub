package com.liujiaming.videohub.feature.settings

/**
 * 播放设置选项定义。
 * 包含快进/快退间隔、云端视频打开模式、续播模式等选项。
 */
object PlaybackOptions {
    /** 快进/快退时间间隔选项列表 */
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

    /** 云端视频打开模式选项列表 */
    val cloudVideoOpenModes = listOf(
        "播放原画",
        "播放最高分辨率视频流"
    )

    /** 续播模式选项列表（每次询问/继续播放/从头开始） */
    val resumePlaybackModes = listOf(
        "每次询问",
        "继续播放",
        "从头开始"
    )
}
