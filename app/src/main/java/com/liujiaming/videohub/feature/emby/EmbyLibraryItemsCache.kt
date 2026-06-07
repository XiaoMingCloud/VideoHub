package com.liujiaming.videohub.feature.emby

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object EmbyLibraryItemsCache {
    private const val PREFERENCES_NAME = "emby_library_items_cache"

    fun load(context: Context, userId: String, libraryId: String): CachedEmbyLibraryItems? {
        val preferences = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val jsonText = preferences.getString(itemsKey(userId, libraryId), null) ?: return null
        val cachedAt = preferences.getLong(timeKey(userId, libraryId), 0L)

        return runCatching {
            val json = JSONObject(jsonText)
            CachedEmbyLibraryItems(
                items = json.optJSONArray("items").toMediaItems(),
                totalRecordCount = json.optInt("totalRecordCount", 0),
                cachedAt = cachedAt
            )
        }.getOrNull()
    }

    fun save(
        context: Context,
        userId: String,
        libraryId: String,
        items: List<EmbyMediaItem>,
        totalRecordCount: Int
    ) {
        val json = JSONObject()
            .put("items", items.toJsonArray())
            .put("totalRecordCount", totalRecordCount)

        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(itemsKey(userId, libraryId), json.toString())
            .putLong(timeKey(userId, libraryId), System.currentTimeMillis())
            .apply()
    }

    private fun itemsKey(userId: String, libraryId: String): String {
        return "items_${userId}_$libraryId"
    }

    private fun timeKey(userId: String, libraryId: String): String {
        return "items_time_${userId}_$libraryId"
    }

    private fun List<EmbyMediaItem>.toJsonArray(): JSONArray {
        val array = JSONArray()
        forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("type", item.type)
                    .put("imageUrl", item.imageUrl)
                    .put("playbackProgress", item.playbackProgress)
                    .put("played", item.played)
                    .put("subtitle", item.subtitle)
                    .put("sourceType", item.sourceType)
            )
        }
        return array
    }

    private fun JSONArray?.toMediaItems(): List<EmbyMediaItem> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    EmbyMediaItem(
                        id = item.optString("id"),
                        name = item.optString("name", "未命名"),
                        type = item.optString("type"),
                        imageUrl = item.optString("imageUrl"),
                        playbackProgress = item.optDouble("playbackProgress", 0.0)
                            .toFloat()
                            .coerceIn(0f, 1f),
                        played = item.optBoolean("played", false),
                        subtitle = item.optString("subtitle"),
                        sourceType = item.optString("sourceType", "Emby")
                    )
                )
            }
        }
    }
}

data class CachedEmbyLibraryItems(
    val items: List<EmbyMediaItem>,
    val totalRecordCount: Int,
    val cachedAt: Long
)
