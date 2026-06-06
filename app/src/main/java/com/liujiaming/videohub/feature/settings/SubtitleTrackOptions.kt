package com.liujiaming.videohub.feature.settings

object SubtitleTrackOptions {
    const val DEFAULT_EXTERNAL_SUBTITLE_RULE = "同目录下的同名字幕"

    val externalSubtitleRules = listOf(
        "不加载",
        DEFAULT_EXTERNAL_SUBTITLE_RULE,
        "同目录下的所有字幕"
    )

    fun normalizeExternalSubtitleRule(value: String): String {
        return when (value) {
            "同目录...字幕" -> DEFAULT_EXTERNAL_SUBTITLE_RULE
            in externalSubtitleRules -> value
            else -> DEFAULT_EXTERNAL_SUBTITLE_RULE
        }
    }
}
