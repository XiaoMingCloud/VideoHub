package com.liujiaming.videohub.feature.settings

object MetadataLanguageOptions {
    const val AUTO_SYSTEM_LANGUAGE = "Auto-自动"

    private val nativeToMetadataLanguage = linkedMapOf(
        "简体中文" to "Chinese(Simplified)-简体中文",
        "繁体中文" to "Chinese(Traditional)-繁体中文",
        "英语" to "English-English",
        "法语" to "French-Français",
        "德语" to "German-Deutsch",
        "意大利语" to "Italian-Italiano",
        "日语" to "Japanese-日本語",
        "韩语" to "Korean-한국어",
        "葡萄牙语" to "Portuguese-Português",
        "俄语" to "Russian-Русский",
        "西班牙语" to "Spanish-Español",
        "阿拉伯语" to "Arabic-العربية",
        "克罗地亚语" to "Croatian-Hrvatski",
        "捷克语" to "Czech-Čeština",
        "丹麦语" to "Danish-Dansk",
        "荷兰语" to "Dutch-Nederlands",
        "芬兰语" to "Finnish-Suomi",
        "希腊语" to "Greek-Ελληνικά",
        "希伯来语" to "Hebrew-עברית",
        "印地语" to "Hindi-हिन्दी",
        "匈牙利语" to "Hungarian-Magyar",
        "印度尼西亚语" to "Indonesian-Bahasa Indonesia",
        "爱尔兰语" to "Irish-Gaeilge",
        "哈萨克语" to "Kazakh-Қазақ тілі",
        "马来语" to "Malay-Bahasa Melayu",
        "蒙古语" to "Mongolian-Монгол",
        "挪威语" to "Norwegian-Norsk",
        "波斯语" to "Persian-فارسی",
        "波兰语" to "Polish-Polski",
        "罗马尼亚语" to "Romanian-Română",
        "瑞典语" to "Swedish-Svenska",
        "泰语" to "Thai-ไทย",
        "土耳其语" to "Turkish-Türkçe",
        "土库曼语" to "Turkmen-Türkmençe",
        "乌克兰语" to "Ukrainian-Українська",
        "越南语" to "Vietnamese-Tiếng Việt",
        "威尔士语" to "Welsh-Cymraeg"
    )

    val languages = listOf(AUTO_SYSTEM_LANGUAGE) + nativeToMetadataLanguage.values

    fun normalize(value: String): String {
        return when {
            value == LanguageOptions.AUTO_SYSTEM_LANGUAGE -> AUTO_SYSTEM_LANGUAGE
            value in languages -> value
            else -> nativeToMetadataLanguage[value] ?: AUTO_SYSTEM_LANGUAGE
        }
    }
}
