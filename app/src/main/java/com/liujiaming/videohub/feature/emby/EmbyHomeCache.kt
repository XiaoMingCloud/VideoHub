package com.liujiaming.videohub.feature.emby

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object EmbyHomeCache {
    private const val PREFERENCES_NAME = "emby_home_cache"
    private const val CACHE_TTL_MS = 5 * 60 * 1000L

    fun load(context: Context, userId: String): CachedEmbyMediaHome? {
        val preferences = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val jsonText = preferences.getString(cacheKey(userId), null) ?: return null
        val cachedAt = preferences.getLong(cacheTimeKey(userId), 0L)

        return runCatching {
            CachedEmbyMediaHome(
                home = JSONObject(jsonText).toMediaHome(),
                cachedAt = cachedAt
            )
        }.getOrNull()
    }

    fun save(context: Context, userId: String, home: EmbyMediaHome) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(cacheKey(userId), home.toJson().toString())
            .putLong(cacheTimeKey(userId), System.currentTimeMillis())
            .apply()
    }

    fun isFresh(cached: CachedEmbyMediaHome): Boolean {
        return System.currentTimeMillis() - cached.cachedAt <= CACHE_TTL_MS
    }

    private fun cacheKey(userId: String) = "home_$userId"

    private fun cacheTimeKey(userId: String) = "home_time_$userId"

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
    }

    private fun JSONObject.toMediaHome(): EmbyMediaHome {
        return EmbyMediaHome(
            sourceName = optString("sourceName", "Emby"),
            libraries = optJSONArray("libraries").toList { it.toLibrarySummary() },
            resumeItems = optJSONArray("resumeItems").toList { it.toMediaItem() },
            latestTitle = optString("latestTitle", "最新媒体"),
            latestItems = optJSONArray("latestItems").toList { it.toMediaItem() },
            librarySections = optJSONArray("librarySections").toList { it.toLibrarySection() }
        )
    }

    private fun JSONObject.toLibrarySummary(): EmbyLibrarySummary {
        return EmbyLibrarySummary(
            id = optString("id"),
            name = optString("name", "媒体库"),
            collectionType = optString("collectionType"),
            itemCount = optInt("itemCount", 0),
            imageUrl = optString("imageUrl")
        )
    }

    private fun JSONObject.toLibrarySection(): EmbyLibrarySection {
        return EmbyLibrarySection(
            libraryId = optString("libraryId"),
            title = optString("title", "媒体库"),
            items = optJSONArray("items").toList { it.toMediaItem() }
        )
    }

    private fun JSONObject.toMediaItem(): EmbyMediaItem {
        return EmbyMediaItem(
            id = optString("id"),
            name = optString("name", "未命名"),
            type = optString("type"),
            imageUrl = optString("imageUrl")
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

data class CachedEmbyMediaHome(
    val home: EmbyMediaHome,
    val cachedAt: Long
)
