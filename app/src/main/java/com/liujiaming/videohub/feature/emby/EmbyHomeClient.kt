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
 * Emby 棣栭〉鏁版嵁鎷夊彇瀹㈡埛绔€? * 閫氳繃 Emby REST API 鑾峰彇濯掍綋搴撳垪琛ㄣ€佺户缁鐪嬨€佹渶鏂板獟浣撶瓑棣栭〉鏁版嵁銆? * 鎵€鏈夌綉缁滆姹備娇鐢?HttpURLConnection 瀹炵幇锛岀粺涓€閫氳繃 X-Emby-Token 杩涜閴存潈銆? */
object EmbyHomeClient {
    private const val TAG = "EmbyHomeClient"

    fun playbackUrl(session: EmbyAuthSession, itemId: String): String {
        return buildUrl(
            baseUrl = session.serverUrl,
            path = "/Videos/$itemId/stream",
            query = mapOf(
                "static" to "true",
                "api_key" to session.accessToken
            )
        )
    }

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

    fun fetchItemDetail(
        session: EmbyAuthSession,
        itemId: String
    ): EmbyMediaItemDetail {
        val json = getJsonObject(
            session = session,
            path = "/Users/${session.userId}/Items/$itemId",
            query = mapOf(
                "Fields" to "Overview,MediaStreams,MediaSources,Path,Genres,Studios,ProductionYear,People"
            )
        )
        val mediaStreams = json.optJSONArray("MediaStreams") ?: JSONArray()
        val mediaSources = json.optJSONArray("MediaSources") ?: JSONArray()
        val people = json.optJSONArray("People") ?: JSONArray()
        Log.d(
            TAG,
            "fetchItemDetail itemId=$itemId overview=${json.optString("Overview").length} " +
                "streams=${mediaStreams.length()} streamTypes=${mediaStreams.streamTypeSummary()} " +
                "mediaSources=${mediaSources.length()} people=${people.length()}"
        )
        return EmbyMediaItemDetail(
            id = json.optString("Id"),
            name = json.optString("Name"),
            overview = json.optString("Overview"),
            runTimeTicks = json.optLong("RunTimeTicks", 0L),
            productionYear = json.optInt("ProductionYear", 0).takeIf { it > 0 },
            path = json.optString("Path"),
            mediaStreams = buildList {
                for (index in 0 until mediaStreams.length()) {
                    mediaStreams.optJSONObject(index)?.let { add(it.toMediaStreamInfo()) }
                }
            },
            mediaSources = buildList {
                for (index in 0 until mediaSources.length()) {
                    mediaSources.optJSONObject(index)?.let { add(it.toMediaSourceInfo()) }
                }
            },
            people = buildList {
                for (index in 0 until people.length()) {
                    people.optJSONObject(index)?.let { add(it.toPersonInfo(session)) }
                }
            }
        )
    }

    /**
     * 鎷夊彇 Emby 棣栭〉鐨勫叏閮ㄥ獟浣撴暟鎹€?     * 鍖呮嫭锛氬獟浣撳簱鍒楄〃銆佺户缁鐪嬪垪琛ㄣ€佹瘡涓獟浣撳簱鐨勮鎯呭垎鍖恒€?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @return 缁勮瀹屾垚鐨勯椤垫暟鎹ā鍨?     */
    fun fetchHome(session: EmbyAuthSession): EmbyMediaHome {
        // 鎷夊彇鐢ㄦ埛鐨勫獟浣撳簱鍒楄〃
        val libraries = fetchLibraries(session)
        // 鎷夊彇缁х画瑙傜湅锛堟柇鐐圭画鎾級鍒楄〃
        val resumeItems = fetchResumeItems(session)
        // 涓烘瘡涓獟浣撳簱鎷夊彇璇︽儏鍒嗗尯锛堝寘鍚渶鏂扮殑濯掍綋椤癸級
        val librarySections = libraries.map { library ->
            EmbyLibrarySection(
                libraryId = library.id,
                title = library.name,
                items = fetchLibraryItems(session, library.id)
            )
        }
        // 鍙栫涓€涓潪绌哄獟浣撳簱鐨勯」鐩綔涓?鏈€鏂板獟浣?灞曠ず
        val latestItems = librarySections.firstOrNull { it.items.isNotEmpty() }?.items.orEmpty()
        val librariesWithCover = libraries.map { library ->
            val previewCover = librarySections
                .firstOrNull { it.libraryId == library.id }
                ?.items
                ?.firstOrNull { it.imageUrl.isNotBlank() }
                ?.imageUrl
                .orEmpty()
            if (library.imageUrl.isBlank() && previewCover.isNotBlank()) {
                library.copy(imageUrl = previewCover)
            } else {
                library
            }
        }

        return EmbyMediaHome(
            sourceName = session.serverName.ifBlank { "Emby" },
            libraries = librariesWithCover,
            resumeItems = resumeItems,
            latestTitle = librariesWithCover.firstOrNull()?.name ?: "最新媒体",
            latestItems = latestItems,
            librarySections = librarySections
        )
    }

    /**
     * 鎷夊彇鐢ㄦ埛鍙鐨勬墍鏈夊獟浣撳簱鍒楄〃銆?     * 璋冪敤 /Users/{userId}/Views 鎺ュ彛鑾峰彇濯掍綋搴撹鍥俱€?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @return 濯掍綋搴撴憳瑕佸垪琛?     */
    private fun fetchLibraries(session: EmbyAuthSession): List<EmbyLibrarySummary> {
        val json = getJsonObject(session, "/Users/${session.userId}/Views")
        val items = json.optJSONArray("Items") ?: return emptyList()

        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val id = item.optString("Id")
                if (id.isBlank()) continue

                val fallbackCount = item.optInt("ChildCount", 0)
                val hasPrimaryImage = item.optJSONObject("ImageTags")?.has("Primary") == true ||
                    item.optString("PrimaryImageTag").isNotBlank()
                add(
                    EmbyLibrarySummary(
                        id = id,
                        name = item.optString("Name", "媒体库"),
                        collectionType = item.optString("CollectionType"),
                        itemCount = fetchLibraryItemCount(session, id, fallbackCount),
                        imageUrl = if (hasPrimaryImage) imageUrl(session, id, 240) else ""
                    )
                )
            }
        }
    }

    /**
     * 鑾峰彇鎸囧畾濯掍綋搴撲腑鐨勫獟浣撻」鎬绘暟銆?     * 閫氳繃 Limit=0 鐨勬煡璇㈠彧鑾峰彇 TotalRecordCount锛屼笉鎷夊彇瀹為檯鏁版嵁銆?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @param libraryId 濯掍綋搴?ID
     * @param fallbackCount 璇锋眰澶辫触鏃剁殑鍥為€€鍊?     * @return 濯掍綋椤规€绘暟
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
                    "Limit" to "0"             // 涓嶈繑鍥炲疄闄呮暟鎹紝浠呰幏鍙栨€绘暟
                )
            )
            json.optInt("TotalRecordCount", fallbackCount)
        }.getOrDefault(fallbackCount)
    }

    /**
     * 鎷夊彇鎸囧畾濯掍綋搴撲腑鐨勫獟浣撻」鍒楄〃锛堟渶澶?10 涓紝鎸夊垱寤烘椂闂撮檷搴忥級銆?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @param libraryId 濯掍綋搴?ID
     * @return 濯掍綋椤瑰垪琛?     */
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
                    "SortBy" to "DateCreated",
                    "SortOrder" to "Descending"
                )
            )
        }.getOrDefault(emptyList())
    }

    /**
     * 鎷夊彇缁х画瑙傜湅锛堟柇鐐圭画鎾級鍒楄〃銆?     * 浼樺厛灏濊瘯 /Items/Resume 鎺ュ彛锛屽け璐ユ椂鍥為€€鍒板甫 IsResumable 杩囨护鍣ㄧ殑閫氱敤鎺ュ彛銆?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @return 鍙户缁鐪嬬殑濯掍綋椤瑰垪琛?     */
    private fun fetchResumeItems(session: EmbyAuthSession): List<EmbyMediaItem> {
        return runCatching {
            // 浼樺厛浣跨敤涓撶敤鐨?Resume 鎺ュ彛
            fetchItems(
                session = session,
                path = "/Users/${session.userId}/Items/Resume",
                query = mapOf(
                    "Limit" to "10",
                    "MediaTypes" to "Video"
                )
            )
        }.getOrElse {
            fetchItems(
                session = session,
                path = "/Users/${session.userId}/Items",
                query = mapOf(
                    "Recursive" to "true",
                    "Filters" to "IsResumable",
                    "Limit" to "10",
                    "SortBy" to "DatePlayed",
                    "SortOrder" to "Descending"
                )
            )
        }
    }

    /**
     * 閫氱敤鐨勫獟浣撻」鎷夊彇鏂规硶锛岃В鏋?Items 鏁扮粍骞惰浆鎹负 [EmbyMediaItem] 鍒楄〃銆?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @param path API 璺緞
     * @param query 鏌ヨ鍙傛暟
     * @return 瑙ｆ瀽鍚庣殑濯掍綋椤瑰垪琛?     */
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
     * 灏?JSON 瀵硅薄杞崲涓?[EmbyMediaItem] 鏁版嵁妯″瀷銆?     * 鑷姩涓烘湁鏁堢殑濯掍綋椤圭敓鎴愬皝闈㈠浘鐗?URL銆?     */
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
     * 鏋勫缓 Emby 濯掍綋椤瑰皝闈㈠浘鐗囩殑 URL銆?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @param itemId 濯掍綋椤?ID
     * @param height 鍥剧墖鐩爣楂樺害锛堝儚绱狅級
     * @return 瀹屾暣鐨勫浘鐗?URL锛屽寘鍚?API Key 閴存潈鍙傛暟
     */
    private fun imageUrl(session: EmbyAuthSession, itemId: String, height: Int): String {
        return "${session.serverUrl}/Items/$itemId/Images/Primary?fillHeight=$height&quality=90&api_key=${encode(session.accessToken)}"
    }

    /**
     * 鍙戦€?GET 璇锋眰骞惰繑鍥炶В鏋愬悗鐨?JSONObject銆?     */
    private fun getJsonObject(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String> = emptyMap()
    ): JSONObject {
        return JSONObject(getTextResponse(session, path, query))
    }

    /**
     * 鍙戦€?HTTP GET 璇锋眰骞惰繑鍥炲搷搴旀枃鏈€?     * 鑷姩娣诲姞 X-Emby-Token 閴存潈澶淬€?     *
     * @param session 宸茶璇佺殑 Emby 浼氳瘽
     * @param path API 璺緞
     * @param query 鏌ヨ鍙傛暟閿€煎
     * @return 鍝嶅簲浣撶殑鏂囨湰鍐呭
     * @throws EmbyAuthException 璇锋眰澶辫触鏃舵姏鍑?     */
    private fun getTextResponse(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String>
    ): String {
        val url = buildUrl(session.serverUrl, path, query)
        Log.d(TAG, "GET $path query=$query")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Emby-Token", session.accessToken)
        }

        val responseCode = connection.responseCode
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
     * 鏋勫缓瀹屾暣鐨勮姹?URL锛屾嫾鎺ヨ矾寰勫拰鏌ヨ鍙傛暟銆?     *
     * @param baseUrl 鏈嶅姟鍣ㄥ熀纭€ URL
     * @param path API 璺緞
     * @param query 鏌ヨ鍙傛暟
     * @return 瀹屾暣鐨?URL 瀛楃涓?     */
    private fun buildUrl(baseUrl: String, path: String, query: Map<String, String>): String {
        val queryText = query.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        return if (queryText.isBlank()) "$baseUrl$path" else "$baseUrl$path?$queryText"
    }

    /** 瀵瑰瓧绗︿覆杩涜 URL 缂栫爜 */
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

    private fun JSONArray.streamTypeSummary(): String {
        val counts = linkedMapOf<String, Int>()
        for (index in 0 until length()) {
            val type = optJSONObject(index)?.optString("Type").orEmpty().ifBlank { "Unknown" }
            counts[type] = (counts[type] ?: 0) + 1
        }
        return counts.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    private fun JSONObject.toMediaStreamInfo(): EmbyMediaStreamInfo {
        return EmbyMediaStreamInfo(
            type = optString("Type"),
            codec = optString("Codec"),
            displayTitle = optString("DisplayTitle"),
            language = optString("Language"),
            width = optInt("Width", 0).takeIf { it > 0 },
            height = optInt("Height", 0).takeIf { it > 0 },
            bitRate = optInt("BitRate", 0).takeIf { it > 0 },
            profile = optString("Profile"),
            level = optDouble("Level", 0.0).takeIf { it > 0.0 },
            aspectRatio = optString("AspectRatio"),
            scanType = optString("ScanType"),
            videoRange = optString("VideoRange"),
            bitDepth = optInt("BitDepth", 0).takeIf { it > 0 },
            pixelFormat = optString("PixelFormat"),
            refFrames = optInt("RefFrames", 0).takeIf { it > 0 },
            frameRate = optDouble("RealFrameRate", 0.0)
                .takeIf { it > 0.0 }
                ?: optDouble("AverageFrameRate", 0.0).takeIf { it > 0.0 },
            channels = optInt("Channels", 0).takeIf { it > 0 },
            sampleRate = optInt("SampleRate", 0).takeIf { it > 0 },
            isDefault = optBoolean("IsDefault", false),
            isForced = optBoolean("IsForced", false)
        )
    }

    private fun JSONObject.toMediaSourceInfo(): EmbyMediaSourceInfo {
        return EmbyMediaSourceInfo(
            id = optString("Id"),
            path = optString("Path"),
            container = optString("Container"),
            size = optLong("Size", 0L).takeIf { it > 0L },
            bitRate = optInt("Bitrate", 0).takeIf { it > 0 }
                ?: optInt("BitRate", 0).takeIf { it > 0 },
            runTimeTicks = optLong("RunTimeTicks", 0L).takeIf { it > 0L }
        )
    }

    private fun JSONObject.toPersonInfo(session: EmbyAuthSession): EmbyPersonInfo {
        val id = optString("Id")
        return EmbyPersonInfo(
            id = id,
            name = optString("Name", "未命名"),
            role = optString("Role"),
            type = optString("Type"),
            imageUrl = if (id.isBlank()) "" else imageUrl(session, id, 220)
        )
    }
}

/**
 * Emby 棣栭〉濯掍綋鏁版嵁妯″瀷锛屽寘鍚椤靛睍绀烘墍闇€鐨勫叏閮ㄦ暟鎹€? *
 * @property sourceName 鏁版嵁婧愬悕绉帮紙鏈嶅姟鍣ㄥ悕绉帮級
 * @property libraries 濯掍綋搴撴憳瑕佸垪琛? * @property resumeItems 缁х画瑙傜湅锛堟柇鐐圭画鎾級鍒楄〃
 * @property latestTitle 鏈€鏂板獟浣撳垎鍖烘爣棰? * @property latestItems 鏈€鏂板獟浣撻」鍒楄〃
 * @property librarySections 鍚勫獟浣撳簱鐨勮鎯呭垎鍖猴紙鍖呭惈搴撳唴濯掍綋椤癸級
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
 * 濯掍綋搴撹鎯呭垎鍖猴紝琛ㄧず涓€涓獟浣撳簱鍙婂叾鍖呭惈鐨勫獟浣撻」銆? *
 * @property libraryId 鎵€灞炲獟浣撳簱 ID
 * @property title 鍒嗗尯鏍囬锛堥€氬父涓哄獟浣撳簱鍚嶇О锛? * @property items 璇ュ垎鍖哄唴鐨勫獟浣撻」鍒楄〃
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
 * 濯掍綋搴撴憳瑕佷俊鎭€? *
 * @property id 濯掍綋搴?ID
 * @property name 濯掍綋搴撳悕绉? * @property collectionType 闆嗗悎绫诲瀷锛堝 movies銆乼vshows 绛夛級
 * @property itemCount 濯掍綋椤规€绘暟
 * @property imageUrl 濯掍綋搴撳皝闈㈠浘鐗?URL
 */
data class EmbyLibrarySummary(
    val id: String,
    val name: String,
    val collectionType: String,
    val itemCount: Int,
    val imageUrl: String = ""
)

/**
 * 濯掍綋椤规暟鎹ā鍨嬶紝琛ㄧず涓€涓叿浣撶殑濯掍綋鍐呭锛堢數褰便€佸墽闆嗐€佽棰戠瓑锛夈€? *
 * @property id 濯掍綋椤?ID
 * @property name 濯掍綋椤瑰悕绉? * @property type 濯掍綋椤圭被鍨嬶紙Movie/Series/Episode/Video 绛夛級
 * @property imageUrl 灏侀潰鍥剧墖 URL
 */
data class EmbyMediaItem(
    val id: String,
    val name: String,
    val type: String,
    val imageUrl: String,
    val playbackProgress: Float = 0f,
    val played: Boolean = false,
    val subtitle: String = "",
    val sourceType: String = "Emby"
)

data class EmbyMediaItemDetail(
    val id: String,
    val name: String,
    val overview: String,
    val runTimeTicks: Long,
    val productionYear: Int?,
    val path: String,
    val mediaStreams: List<EmbyMediaStreamInfo>,
    val mediaSources: List<EmbyMediaSourceInfo>,
    val people: List<EmbyPersonInfo>
)

data class EmbyMediaStreamInfo(
    val type: String,
    val codec: String,
    val displayTitle: String,
    val language: String,
    val width: Int?,
    val height: Int?,
    val bitRate: Int?,
    val profile: String,
    val level: Double?,
    val aspectRatio: String,
    val scanType: String,
    val videoRange: String,
    val bitDepth: Int?,
    val pixelFormat: String,
    val refFrames: Int?,
    val frameRate: Double?,
    val channels: Int?,
    val sampleRate: Int?,
    val isDefault: Boolean,
    val isForced: Boolean
)

data class EmbyMediaSourceInfo(
    val id: String,
    val path: String,
    val container: String,
    val size: Long?,
    val bitRate: Int?,
    val runTimeTicks: Long?
)

data class EmbyPersonInfo(
    val id: String,
    val name: String,
    val role: String,
    val type: String,
    val imageUrl: String
)
