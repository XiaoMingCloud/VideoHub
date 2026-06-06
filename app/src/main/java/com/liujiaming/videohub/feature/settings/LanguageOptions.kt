package com.liujiaming.videohub.feature.settings

object LanguageOptions {
    const val AUTO_SYSTEM_LANGUAGE = "自动（系统当前语言）"

    val commonLanguages = listOf(
        "简体中文",
        "繁体中文",
        "英语",
        "法语",
        "德语",
        "意大利语",
        "日语",
        "韩语",
        "葡萄牙语",
        "俄语",
        "西班牙语",
        "阿拉伯语",
        "克罗地亚语",
        "捷克语",
        "丹麦语",
        "荷兰语",
        "芬兰语",
        "希腊语",
        "希伯来语",
        "印地语",
        "匈牙利语",
        "印度尼西亚语",
        "爱尔兰语",
        "哈萨克语",
        "马来语",
        "蒙古语",
        "挪威语",
        "波斯语",
        "波兰语",
        "罗马尼亚语",
        "瑞典语",
        "泰语",
        "土耳其语",
        "土库曼语",
        "乌克兰语",
        "越南语",
        "威尔士语"
    )

    val autoAndCommonLanguages = listOf(AUTO_SYSTEM_LANGUAGE) + commonLanguages
}
