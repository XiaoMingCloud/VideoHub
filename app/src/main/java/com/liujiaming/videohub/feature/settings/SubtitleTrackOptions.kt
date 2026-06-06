package com.liujiaming.videohub.feature.settings

/**
 * 字幕轨道设置选项定义。
 * 包含外挂字幕加载规则选项和标准化方法。
 */
object SubtitleTrackOptions {
    /** 默认的外挂字幕加载规则 */
    const val DEFAULT_EXTERNAL_SUBTITLE_RULE = "同目录下的同名字幕"

    /** 外挂字幕加载规则选项列表 */
    val externalSubtitleRules = listOf(
        "不加载",
        DEFAULT_EXTERNAL_SUBTITLE_RULE,
        "同目录下的所有字幕"
    )

    /**
     * 标准化外挂字幕规则值。
     * 将旧的简短名称映射为完整名称，无效值回退为默认规则。
     */
    fun normalizeExternalSubtitleRule(value: String): String {
        return when (value) {
            "同目录...字幕" -> DEFAULT_EXTERNAL_SUBTITLE_RULE
            in externalSubtitleRules -> value
            else -> DEFAULT_EXTERNAL_SUBTITLE_RULE
        }
    }
}
