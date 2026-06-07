package com.liujiaming.videohub.feature.emby

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

/**
 * Emby 首页数据拉取客户端。
 * 通过 Emby REST API 获取媒体库列表、继续观看、最新媒体等首页数据。
 * 所有网络请求使用 HttpURLConnection 实现，统一通过 X-Emby-Token 进行鉴权。
 */
object EmbyHomeClient {
    private const val TAG = "EmbyHomeClient"

    fun fetchLibraryItemsPage(
        session: EmbyAuthSession,
        parentId: String,
        recursive: Boolean,
        startIndex: Int,
        limit: Int
    ): EmbyLibraryItemsPage {
        val json = getJsonObject(
            session = session,
            path = "/Users/${session.userId}/Items",
            query = mapOf(
                "ParentId" to parentId,
                "Recursive" to recursive.toString(),
                "IncludeItemTypes" to "Movie,Series,Episode,Video,Folder",
                "StartIndex" to startIndex.coerceAtLeast(0).toString(),
                "Limit" to limit.coerceAtLeast(1).toString(),
                "SortBy" to "DateCreated",
                "SortOrder" to "Descending"
            )
        )
        val items = json.optJSONArray("Items") ?: JSONArray()
        Log.d(
            TAG,
            "fetchLibraryItemsPage parentId=$parentId recursive=$recursive startIndex=$startIndex limit=$limit " +
                "items=${items.length()} total=${json.optInt("TotalRecordCount", -1)} types=${items.typeSummary()}"
        )
        return EmbyLibraryItemsPage(
            items = buildList {
                for (index in 0 until items.length()) {
                    add(items.getJSONObject(index).toMediaItem(session))
                }
            },
            totalRecordCount = json.optInt("TotalRecordCount", startIndex)
        )
    }

    fun fetchDirectChildrenPage(
        session: EmbyAuthSession,
        parentId: String,
        startIndex: Int,
        limit: Int
    ): EmbyLibraryItemsPage {
        val json = getJsonObject(
            session = session,
            path = "/Users/${session.userId}/Items",
            query = mapOf(
                "ParentId" to parentId,
                "Recursive" to "false",
                "StartIndex" to startIndex.coerceAtLeast(0).toString(),
                "Limit" to limit.coerceAtLeast(1).toString(),
                "SortBy" to "SortName",
                "SortOrder" to "Ascending"
            )
        )
        val items = json.optJSONArray("Items") ?: JSONArray()
        Log.d(
            TAG,
            "fetchDirectChildrenPage parentId=$parentId startIndex=$startIndex limit=$limit " +
                "items=${items.length()} total=${json.optInt("TotalRecordCount", -1)} " +
                "types=${items.typeSummary()}"
        )
        return EmbyLibraryItemsPage(
            items = buildList {
                for (index in 0 until items.length()) {
                    add(items.getJSONObject(index).toMediaItem(session))
                }
            },
            totalRecordCount = json.optInt("TotalRecordCount", startIndex)
        )
    }

    fun fetchFirstVideoInFolder(
        session: EmbyAuthSession,
        folderId: String
    ): EmbyMediaItem? {
        val json = getJsonObject(
            session = session,
            path = "/Users/${session.userId}/Items",
            query = mapOf(
                "ParentId" to folderId,
                "Recursive" to "true",
                "IncludeItemTypes" to "Movie,Series,Episode,Video",
                "StartIndex" to "0",
                "Limit" to "1",
                "SortBy" to "SortName",
                "SortOrder" to "Ascending"
            )
        )
        val items = json.optJSONArray("Items") ?: return null
        Log.d(
            TAG,
            "fetchFirstVideoInFolder folderId=$folderId items=${items.length()} types=${items.typeSummary()}"
        )
        return items.optJSONObject(0)?.toMediaItem(session)
    }

    /**
     * 拉取 Emby 首页的全部媒体数据。
     * 包括：媒体库列表、继续观看列表、每个媒体库的详情分区。
     *
     * @param session 已认证的 Emby 会话
     * @return 组装完成的首页数据模型
     */
    fun fetchHome(session: EmbyAuthSession): EmbyMediaHome {
        // 拉取用户的媒体库列表
        val libraries = fetchLibraries(session)
        // 拉取继续观看（断点续播）列表
        val resumeItems = fetchResumeItems(session)
        // 为每个媒体库拉取详情分区（包含最新的媒体项）
        val librarySections = libraries.map { library ->
            EmbyLibrarySection(
                libraryId = library.id,
                title = library.name,
                items = fetchLibraryItems(session, library.id)
            )
        }
        // 取第一个非空媒体库的项目作为"最新媒体"展示
        val latestItems = librarySections.firstOrNull { it.items.isNotEmpty() }?.items.orEmpty()

        return EmbyMediaHome(
            sourceName = session.serverName.ifBlank { "Emby" },
            libraries = libraries,
            resumeItems = resumeItems,
            latestTitle = libraries.firstOrNull()?.name ?: "最新媒体",
            latestItems = latestItems,
            librarySections = librarySections
        )
    }

    /**
     * 拉取用户可见的所有媒体库列表。
     * 调用 /Users/{userId}/Views 接口获取媒体库视图。
     *
     * @param session 已认证的 Emby 会话
     * @return 媒体库摘要列表
     */
    private fun fetchLibraries(session: EmbyAuthSession): List<EmbyLibrarySummary> {
        val json = getJsonObject(session, "/Users/${session.userId}/Views")
        val items = json.optJSONArray("Items") ?: return emptyList()

        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val id = item.optString("Id")
                if (id.isBlank()) continue

                val fallbackCount = item.optInt("ChildCount", 0)
                add(
                    EmbyLibrarySummary(
                        id = id,
                        name = item.optString("Name", "媒体库"),
                        collectionType = item.optString("CollectionType"),
                        // 通过额外请求获取精确的媒体项数量，如果失败则使用 ChildCount 作为回退值
                        itemCount = fetchLibraryItemCount(session, id, fallbackCount),
                        imageUrl = imageUrl(session, id, 240)
                    )
                )
            }
        }
    }

    /**
     * 获取指定媒体库中的媒体项总数。
     * 通过 Limit=0 的查询只获取 TotalRecordCount，不拉取实际数据。
     *
     * @param session 已认证的 Emby 会话
     * @param libraryId 媒体库 ID
     * @param fallbackCount 请求失败时的回退值
     * @return 媒体项总数
     */
    private fun fetchLibraryItemCount(
        session: EmbyAuthSession,
        libraryId: String,
        fallbackCount: Int
    ): Int {
        return runCatching {
            val json = getJsonObject(
                session = session,
                path = "/Users/${session.userId}/Items",
                query = mapOf(
                    "ParentId" to libraryId,
                    "Recursive" to "true",
                    "Limit" to "0"             // 不返回实际数据，仅获取总数
                )
            )
            json.optInt("TotalRecordCount", fallbackCount)
        }.getOrDefault(fallbackCount)
    }

    /**
     * 拉取指定媒体库中的媒体项列表（最多 10 个，按创建时间降序）。
     *
     * @param session 已认证的 Emby 会话
     * @param libraryId 媒体库 ID
     * @return 媒体项列表
     */
    private fun fetchLibraryItems(session: EmbyAuthSession, libraryId: String): List<EmbyMediaItem> {
        return runCatching {
            fetchItems(
                session = session,
                path = "/Users/${session.userId}/Items",
                query = mapOf(
                    "ParentId" to libraryId,
                    "Recursive" to "true",
                    "IncludeItemTypes" to "Movie,Series,Episode,Video,Folder",
                    "Limit" to "60",
                    "SortBy" to "DateCreated",     // 按创建时间排序
                    "SortOrder" to "Descending"     // 降序，最新的在前
                )
            )
        }.getOrDefault(emptyList())
    }

    /**
     * 拉取继续观看（断点续播）列表。
     * 优先尝试 /Items/Resume 接口，失败时回退到带 IsResumable 过滤器的通用接口。
     *
     * @param session 已认证的 Emby 会话
     * @return 可继续观看的媒体项列表
     */
    private fun fetchResumeItems(session: EmbyAuthSession): List<EmbyMediaItem> {
        return runCatching {
            // 优先使用专用的 Resume 接口
            fetchItems(
                session = session,
                path = "/Users/${session.userId}/Items/Resume",
                query = mapOf(
                    "Limit" to "10",
                    "MediaTypes" to "Video"
                )
            )
        }.getOrElse {
            // 回退方案：使用通用接口 + IsResumable 过滤器
            fetchItems(
                session = session,
                path = "/Users/${session.userId}/Items",
                query = mapOf(
                    "Recursive" to "true",
                    "Filters" to "IsResumable",
                    "Limit" to "10",
                    "SortBy" to "DatePlayed",      // 按播放时间排序
                    "SortOrder" to "Descending"
                )
            )
        }
    }

    /**
     * 通用的媒体项拉取方法，解析 Items 数组并转换为 [EmbyMediaItem] 列表。
     *
     * @param session 已认证的 Emby 会话
     * @param path API 路径
     * @param query 查询参数
     * @return 解析后的媒体项列表
     */
    private fun fetchItems(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String>
    ): List<EmbyMediaItem> {
        val json = getJsonObject(session, path, query)
        val items = json.optJSONArray("Items") ?: return emptyList()

        return buildList {
            for (index in 0 until items.length()) {
                add(items.getJSONObject(index).toMediaItem(session))
            }
        }
    }

    /**
     * 将 JSON 对象转换为 [EmbyMediaItem] 数据模型。
     * 自动为有效的媒体项生成封面图片 URL。
     */
    private fun JSONObject.toMediaItem(session: EmbyAuthSession): EmbyMediaItem {
        val id = optString("Id")
        val userData = optJSONObject("UserData")
        val runtimeTicks = optLong("RunTimeTicks", 0L).takeIf { it > 0L }
        val playbackTicks = userData?.optLong("PlaybackPositionTicks", 0L) ?: 0L
        val playbackProgress = if (runtimeTicks != null && playbackTicks > 0L) {
            (playbackTicks.toFloat() / runtimeTicks.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        return EmbyMediaItem(
            id = id,
            name = optString("Name", "未命名"),
            type = optString("Type"),
            imageUrl = if (id.isBlank()) "" else imageUrl(session, id, 360),
            playbackProgress = playbackProgress,
            played = userData?.optBoolean("Played", false) ?: false
        )
    }

    /**
     * 构建 Emby 媒体项封面图片的 URL。
     *
     * @param session 已认证的 Emby 会话
     * @param itemId 媒体项 ID
     * @param height 图片目标高度（像素）
     * @return 完整的图片 URL，包含 API Key 鉴权参数
     */
    private fun imageUrl(session: EmbyAuthSession, itemId: String, height: Int): String {
        return "${session.serverUrl}/Items/$itemId/Images/Primary?fillHeight=$height&quality=90&api_key=${encode(session.accessToken)}"
    }

    /**
     * 发送 GET 请求并返回解析后的 JSONObject。
     */
    private fun getJsonObject(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String> = emptyMap()
    ): JSONObject {
        return JSONObject(getTextResponse(session, path, query))
    }

    /**
     * 发送 HTTP GET 请求并返回响应文本。
     * 自动添加 X-Emby-Token 鉴权头。
     *
     * @param session 已认证的 Emby 会话
     * @param path API 路径
     * @param query 查询参数键值对
     * @return 响应体的文本内容
     * @throws EmbyAuthException 请求失败时抛出
     */
    private fun getTextResponse(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String>
    ): String {
        val url = buildUrl(session.serverUrl, path, query)
        Log.d(TAG, "GET $path query=$query")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000       // 连接超时 10 秒
            readTimeout = 15000          // 读取超时 15 秒
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Emby-Token", session.accessToken)  // 令牌鉴权
        }

        val responseCode = connection.responseCode
        // 根据响应码选择读取正常流或错误流
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        connection.disconnect()
        Log.d(TAG, "GET $path responseCode=$responseCode responseLength=${response.length}")

        if (responseCode !in 200..299) {
            throw EmbyAuthException("获取 Emby 媒体库失败($responseCode): $response")
        }

        return response
    }

    /**
     * 构建完整的请求 URL，拼接路径和查询参数。
     *
     * @param baseUrl 服务器基础 URL
     * @param path API 路径
     * @param query 查询参数
     * @return 完整的 URL 字符串
     */
    private fun buildUrl(baseUrl: String, path: String, query: Map<String, String>): String {
        val queryText = query.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        return if (queryText.isBlank()) "$baseUrl$path" else "$baseUrl$path?$queryText"
    }

    /** 对字符串进行 URL 编码 */
    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }

    private fun JSONArray.typeSummary(): String {
        val counts = linkedMapOf<String, Int>()
        for (index in 0 until length()) {
            val type = optJSONObject(index)?.optString("Type").orEmpty().ifBlank { "Unknown" }
            counts[type] = (counts[type] ?: 0) + 1
        }
        return counts.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}

/**
 * Emby 首页媒体数据模型，包含首页展示所需的全部数据。
 *
 * @property sourceName 数据源名称（服务器名称）
 * @property libraries 媒体库摘要列表
 * @property resumeItems 继续观看（断点续播）列表
 * @property latestTitle 最新媒体分区标题
 * @property latestItems 最新媒体项列表
 * @property librarySections 各媒体库的详情分区（包含库内媒体项）
 */
data class EmbyMediaHome(
    val sourceName: String,
    val libraries: List<EmbyLibrarySummary>,
    val resumeItems: List<EmbyMediaItem>,
    val latestTitle: String,
    val latestItems: List<EmbyMediaItem>,
    val librarySections: List<EmbyLibrarySection> = emptyList()
)

/**
 * 媒体库详情分区，表示一个媒体库及其包含的媒体项。
 *
 * @property libraryId 所属媒体库 ID
 * @property title 分区标题（通常为媒体库名称）
 * @property items 该分区内的媒体项列表
 */
data class EmbyLibrarySection(
    val libraryId: String,
    val title: String,
    val items: List<EmbyMediaItem>
)

data class EmbyLibraryItemsPage(
    val items: List<EmbyMediaItem>,
    val totalRecordCount: Int
)

/**
 * 媒体库摘要信息。
 *
 * @property id 媒体库 ID
 * @property name 媒体库名称
 * @property collectionType 集合类型（如 movies、tvshows 等）
 * @property itemCount 媒体项总数
 * @property imageUrl 媒体库封面图片 URL
 */
data class EmbyLibrarySummary(
    val id: String,
    val name: String,
    val collectionType: String,
    val itemCount: Int,
    val imageUrl: String = ""
)

/**
 * 媒体项数据模型，表示一个具体的媒体内容（电影、剧集、视频等）。
 *
 * @property id 媒体项 ID
 * @property name 媒体项名称
 * @property type 媒体项类型（Movie/Series/Episode/Video 等）
 * @property imageUrl 封面图片 URL
 */
data class EmbyMediaItem(
    val id: String,
    val name: String,
    val type: String,
    val imageUrl: String,
    val playbackProgress: Float = 0f,
    val played: Boolean = false
)
