package com.liujiaming.videohub.feature.media

import android.content.Context

object MediaSourceSelectionStore {
    private const val PREFERENCES_NAME = "media_source_selection"

    fun load(context: Context): MediaSourceType {
        val value = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString("sourceType", MediaSourceType.Emby.name)
        return runCatching { MediaSourceType.valueOf(value ?: MediaSourceType.Emby.name) }
            .getOrDefault(MediaSourceType.Emby)
    }

    fun save(context: Context, sourceType: MediaSourceType) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("sourceType", sourceType.name)
            .apply()
    }
}
