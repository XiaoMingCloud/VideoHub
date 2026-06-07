package com.liujiaming.videohub.feature.emby

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Emby 首页数据本地缓存管理器。
 * 使用 SharedPreferences 存储 Emby 首页媒体库数据的 JSON 序列化结果，
 * 支持按用户 ID 独立缓存，缓存有效期为 5 分钟。
 * 内部实现了数据模型与 JSON 之间的双向序列化/反序列化。
 */
object EmbyHomeCache {
    /** SharedPreferences 文件名 */
    private const val PREFERENCES_NAME = "emby_home_cache"
    /** 缓存有效期：5 分钟 */
    private const val CACHE_TTL_MS = 5 * 60 * 1000L

    /**
     * 从本地缓存加载指定用户的首页数据。
     *
     * @param context 应用上下文
     * @param userId 用户 ID
     * @return 缓存的首页数据（含缓存时间），如果无缓存或解析失败则返回 null
     */
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

    /**
     * 将首页数据保存到本地缓存。
     *
     * @param context 应用上下文
     * @param userId 用户 ID
     * @param home 要缓存的首页数据
     */
    fun save(context: Context, userId: String, home: EmbyMediaHome) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(cacheKey(userId), home.toJson().toString())
            .putLong(cacheTimeKey(userId), System.currentTimeMillis())
            .apply()
    }

    /**
     * 判断缓存数据是否仍在有效期内（5 分钟以内）。
     *
     * @param cached 缓存数据（含缓存时间戳）
     * @return true 表示缓存仍然新鲜，可以使用
     */
    fun isFresh(cached: CachedEmbyMediaHome): Boolean {
        return System.currentTimeMillis() - cached.cachedAt <= CACHE_TTL_MS
    }

    /** 生成缓存数据的 SharedPreferences key */
    private fun cacheKey(userId: String) = "home_$userId"

    /** 生成缓存时间的 SharedPreferences key */
    private fun cacheTimeKey(userId: String) = "home_time_$userId"

    // ==================== 序列化方法（模型 -> JSON） ====================

    /** 将 [EmbyMediaHome] 序列化为 JSONObject */
    private fun EmbyMediaHome.toJson(): JSONObject {
        return JSONObject()
            .put("sourceName", sourceName)
            .put("libraries", libraries.toJsonArray { it.toJson() })
            .put("resumeItems", resumeItems.toJsonArray { it.toJson() })
            .put("latestTitle", latestTitle)
            .put("latestItems", latestItems.toJsonArray { it.toJson() })
            .put("librarySections", librarySections.toJsonArray { it.toJson() })
    }

    /** 将 [EmbyLibrarySummary] 序列化为 JSONObject */
    private fun EmbyLibrarySummary.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("name", name)
            .put("collectionType", collectionType)
            .put("itemCount", itemCount)
            .put("imageUrl", imageUrl)
    }

    /** 将 [EmbyLibrarySection] 序列化为 JSONObject */
    private fun EmbyLibrarySection.toJson(): JSONObject {
        return JSONObject()
            .put("libraryId", libraryId)
            .put("title", title)
            .put("items", items.toJsonArray { it.toJson() })
    }

    /** 将 [EmbyMediaItem] 序列化为 JSONObject */
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

    // ==================== 反序列化方法（JSON -> 模型） ====================

    /** 从 JSONObject 反序列化为 [EmbyMediaHome] */
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

    /** 从 JSONObject 反序列化为 [EmbyLibrarySummary] */
    private fun JSONObject.toLibrarySummary(): EmbyLibrarySummary {
        return EmbyLibrarySummary(
            id = optString("id"),
            name = optString("name", "媒体库"),
            collectionType = optString("collectionType"),
            itemCount = optInt("itemCount", 0),
            imageUrl = optString("imageUrl")
        )
    }

    /** 从 JSONObject 反序列化为 [EmbyLibrarySection] */
    private fun JSONObject.toLibrarySection(): EmbyLibrarySection {
        return EmbyLibrarySection(
            libraryId = optString("libraryId"),
            title = optString("title", "媒体库"),
            items = optJSONArray("items").toList { it.toMediaItem() }
        )
    }

    /** 从 JSONObject 反序列化为 [EmbyMediaItem] */
    private fun JSONObject.toMediaItem(): EmbyMediaItem {
        return EmbyMediaItem(
            id = optString("id"),
            name = optString("name", "未命名"),
            type = optString("type"),
            imageUrl = optString("imageUrl"),
            playbackProgress = optDouble("playbackProgress", 0.0).toFloat().coerceIn(0f, 1f),
            played = optBoolean("played", false),
            subtitle = optString("subtitle"),
            sourceType = optString("sourceType", "Emby")
        )
    }

    // ==================== 通用工具方法 ====================

    /** 将列表序列化为 JSONArray，通过 transform 函数将每个元素转为 JSONObject */
    private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        forEach { array.put(transform(it)) }
        return array
    }

    /** 将 JSONArray 反序列化为列表，通过 transform 函数将每个 JSONObject 转为目标类型 */
    private fun <T> JSONArray?.toList(transform: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optJSONObject(index)?.let { add(transform(it)) }
            }
        }
    }
}

/**
 * 带缓存时间戳的 Emby 首页数据封装。
 *
 * @property home 首页媒体库数据
 * @property cachedAt 缓存写入的时间戳（毫秒）
 */
data class CachedEmbyMediaHome(
    val home: EmbyMediaHome,
    val cachedAt: Long
)
