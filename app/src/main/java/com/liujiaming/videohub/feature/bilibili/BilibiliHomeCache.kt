package com.liujiaming.videohub.feature.bilibili

import android.content.Context
import com.liujiaming.videohub.feature.emby.EmbyLibrarySection
import com.liujiaming.videohub.feature.emby.EmbyLibrarySummary
import com.liujiaming.videohub.feature.emby.EmbyMediaHome
import com.liujiaming.videohub.feature.emby.EmbyMediaItem
import org.json.JSONArray
import org.json.JSONObject

object BilibiliHomeCache {
    private const val PREFERENCES_NAME = "bilibili_home_cache"
    private const val CACHE_TTL_MS = 120 * 1000L

    fun load(context: Context, mid: String): CachedBilibiliHome? {
        val preferences = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val jsonText = preferences.getString(cacheKey(mid), null) ?: return null
        val cachedAt = preferences.getLong(cacheTimeKey(mid), 0L)
        return runCatching {
            CachedBilibiliHome(
                home = JSONObject(jsonText).toMediaHome(),
                cachedAt = cachedAt
            )
        }.getOrNull()
    }

    fun save(context: Context, mid: String, home: EmbyMediaHome) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(cacheKey(mid), home.toJson().toString())
            .putLong(cacheTimeKey(mid), System.currentTimeMillis())
            .apply()
    }

    fun isFresh(cached: CachedBilibiliHome): Boolean {
        return System.currentTimeMillis() - cached.cachedAt <= CACHE_TTL_MS
    }

    private fun cacheKey(mid: String) = "home_$mid"

    private fun cacheTimeKey(mid: String) = "home_time_$mid"

    private fun EmbyMediaHome.toJson(): JSONObject {
        return JSONObject()
            .put("sourceName", sourceName)
            .put("libraries", libraries.toJsonArray { it.toJson() })
            .put("resumeItems", resumeItems.toJsonArray { it.toJson() })
            .put("latestTitle", latestTitle)
            .put("latestItems", latestItems.toJsonArray { it.toJson() })
            .put("librarySections", librarySections.toJsonArray { it.toJson() })
    }

    private fun EmbyLibrarySummary.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("name", name)
            .put("collectionType", collectionType)
            .put("itemCount", itemCount)
            .put("imageUrl", imageUrl)
    }

    private fun EmbyLibrarySection.toJson(): JSONObject {
        return JSONObject()
            .put("libraryId", libraryId)
            .put("title", title)
            .put("items", items.toJsonArray { it.toJson() })
    }

    private fun EmbyMediaItem.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("name", name)
            .put("type", type)
            .put("imageUrl", imageUrl)
            .put("playbackProgress", playbackProgress)
            .put("played", played)
            .put("subtitle", subtitle)
            .put("sourceType", sourceType)
    }

    private fun JSONObject.toMediaHome(): EmbyMediaHome {
        return EmbyMediaHome(
            sourceName = optString("sourceName", "Bilibili"),
            libraries = optJSONArray("libraries").toList { it.toLibrarySummary() },
            resumeItems = optJSONArray("resumeItems").toList { it.toMediaItem() },
            latestTitle = optString("latestTitle", "Bilibili 收藏"),
            latestItems = optJSONArray("latestItems").toList { it.toMediaItem() },
            librarySections = optJSONArray("librarySections").toList { it.toLibrarySection() }
        )
    }

    private fun JSONObject.toLibrarySummary(): EmbyLibrarySummary {
        return EmbyLibrarySummary(
            id = optString("id"),
            name = optString("name", "Bilibili 收藏夹"),
            collectionType = optString("collectionType"),
            itemCount = optInt("itemCount", 0),
            imageUrl = optString("imageUrl")
        )
    }

    private fun JSONObject.toLibrarySection(): EmbyLibrarySection {
        return EmbyLibrarySection(
            libraryId = optString("libraryId"),
            title = optString("title", "Bilibili 收藏夹"),
            items = optJSONArray("items").toList { it.toMediaItem() }
        )
    }

    private fun JSONObject.toMediaItem(): EmbyMediaItem {
        return EmbyMediaItem(
            id = optString("id"),
            name = optString("name", "Bilibili 视频"),
            type = optString("type"),
            imageUrl = optString("imageUrl"),
            playbackProgress = optDouble("playbackProgress", 0.0).toFloat().coerceIn(0f, 1f),
            played = optBoolean("played", false),
            subtitle = optString("subtitle"),
            sourceType = optString("sourceType", "Bilibili")
        )
    }

    private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        forEach { array.put(transform(it)) }
        return array
    }

    private fun <T> JSONArray?.toList(transform: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optJSONObject(index)?.let { add(transform(it)) }
            }
        }
    }
}

data class CachedBilibiliHome(
    val home: EmbyMediaHome,
    val cachedAt: Long
)
