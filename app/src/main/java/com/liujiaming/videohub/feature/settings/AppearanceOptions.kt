package com.liujiaming.videohub.feature.settings

/**
 * 外观模式选项定义。
 * 提供三种外观模式：自动（跟随系统）、浅色、深色。
 */
object AppearanceOptions {
    /** 自动模式，跟随系统暗色/亮色设置 */
    const val AUTO = "自动"
    /** 强制浅色模式 */
    const val LIGHT = "浅色"
    /** 强制深色模式 */
    const val DARK = "深色"

    /** 所有可选的外观模式列表 */
    val modes = listOf(AUTO, LIGHT, DARK)
}
