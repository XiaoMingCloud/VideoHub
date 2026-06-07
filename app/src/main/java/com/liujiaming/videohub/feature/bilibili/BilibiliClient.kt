package com.liujiaming.videohub.feature.bilibili

import android.content.Context
import android.util.Log
import com.liujiaming.videohub.feature.emby.EmbyLibraryItemsPage
import com.liujiaming.videohub.feature.emby.EmbyLibrarySection
import com.liujiaming.videohub.feature.emby.EmbyLibrarySummary
import com.liujiaming.videohub.feature.emby.EmbyMediaHome
import com.liujiaming.videohub.feature.emby.EmbyMediaItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

object BilibiliClient {
    private const val TAG = "BilibiliClient"
    private const val BASE_URL = "https://api.bilibili.com"
    private const val PASSPORT_URL = "https://passport.bilibili.com"

    fun generateQrCode(): BilibiliQrCode {
        val json = getJsonObject("$PASSPORT_URL/x/passport-login/web/qrcode/generate")
        val data = json.optJSONObject("data") ?: error("Bilibili QR response missing data")
        return BilibiliQrCode(
            url = data.optString("url"),
            qrcodeKey = data.optString("qrcode_key")
        )
    }

    fun pollQrCode(qrcodeKey: String): BilibiliQrPollResult {
        val response = getTextResponse(
            url = "$PASSPORT_URL/x/passport-login/web/qrcode/poll?qrcode_key=${encode(qrcodeKey)}",
            includeHeaders = true
        )
        val json = JSONObject(response.body)
        val data = json.optJSONObject("data") ?: JSONObject()
        val code = data.optInt("code", -1)
        val message = data.optString("message", json.optString("message"))
        val cookie = response.headers["Set-Cookie"]
            .orEmpty()
            .joinToString("; ") { it.substringBefore(";") }
        return BilibiliQrPollResult(
            code = code,
            message = message,
            cookie = cookie,
            isSuccess = code == 0 && cookie.isNotBlank()
        )
    }

    fun fetchSessionFromCookie(cookie: String): BilibiliSession {
        val json = getJsonObject("$BASE_URL/x/web-interface/nav", cookie = cookie)
        val data = json.optJSONObject("data") ?: error("Bilibili nav response missing data")
        if (!data.optBoolean("isLogin", false)) {
            error("Bilibili 登录态无效")
        }
        return BilibiliSession(
            mid = data.optLong("mid", 0L).toString(),
            username = data.optString("uname", "Bilibili 用户"),
            face = data.optString("face"),
            cookie = cookie
        )
    }

    fun fetchHome(session: BilibiliSession): EmbyMediaHome {
        val folders = fetchFavoriteFolders(session)
        val sections = folders.take(8).map { folder ->
            EmbyLibrarySection(
                libraryId = folder.id,
                title = folder.name,
                items = runCatching {
                    fetchFavoriteResources(session, folder.id, startIndex = 0, limit = 12).items
                }.getOrDefault(emptyList())
            )
        }
        val latestItems = sections.firstOrNull { it.items.isNotEmpty() }?.items.orEmpty()
        return EmbyMediaHome(
            sourceName = "Bilibili · ${session.username}",
            libraries = folders,
            resumeItems = emptyList(),
            latestTitle = folders.firstOrNull()?.name ?: "Bilibili 收藏",
            latestItems = latestItems,
            librarySections = sections
        )
    }

    fun fetchFavoriteFolders(session: BilibiliSession): List<EmbyLibrarySummary> {
        val json = getJsonObject(
            "$BASE_URL/x/v3/fav/folder/created/list-all?up_mid=${encode(session.mid)}",
            cookie = session.cookie
        )
        val data = json.optJSONObject("data") ?: JSONObject()
        val list = data.optJSONArray("list") ?: JSONArray()
        return buildList {
            for (index in 0 until list.length()) {
                val item = list.optJSONObject(index) ?: continue
                val id = item.optLong("id", 0L).toString()
                if (id == "0") continue
                add(
                    EmbyLibrarySummary(
                        id = id,
                        name = item.optString("title", "Bilibili 收藏夹"),
                        collectionType = "BilibiliFavorite",
                        itemCount = item.optInt("media_count", 0),
                        imageUrl = item.optString("cover")
                    )
                )
            }
        }
    }

    fun fetchFavoriteResources(
        session: BilibiliSession,
        mediaId: String,
        startIndex: Int,
        limit: Int
    ): EmbyLibraryItemsPage {
        val pageNumber = startIndex / limit + 1
        val json = getJsonObject(
            "$BASE_URL/x/v3/fav/resource/list?media_id=${encode(mediaId)}&pn=$pageNumber&ps=$limit&platform=web",
            cookie = session.cookie
        )
        val data = json.optJSONObject("data") ?: JSONObject()
        val medias = data.optJSONArray("medias") ?: JSONArray()
        val info = data.optJSONObject("info")
        val total = info?.optInt("media_count", 0) ?: data.optInt("total", startIndex + medias.length())
        Log.d(TAG, "fetchFavoriteResources mediaId=$mediaId pn=$pageNumber ps=$limit items=${medias.length()} total=$total")
        return EmbyLibraryItemsPage(
            items = buildList {
                for (index in 0 until medias.length()) {
                    medias.optJSONObject(index)?.toMediaItem()?.let { add(it) }
                }
            },
            totalRecordCount = total
        )
    }

    fun fetchVideoDetail(session: BilibiliSession, bvid: String): BilibiliVideoDetail {
        val json = getJsonObject(
            "$BASE_URL/x/web-interface/view?bvid=${encode(bvid)}",
            cookie = session.cookie
        )
        val data = json.optJSONObject("data") ?: JSONObject()
        val owner = data.optJSONObject("owner")
        return BilibiliVideoDetail(
            bvid = data.optString("bvid", bvid),
            title = data.optString("title"),
            description = data.optString("desc"),
            cover = data.optString("pic"),
            authorName = owner?.optString("name").orEmpty(),
            authorFace = owner?.optString("face").orEmpty(),
            durationSeconds = data.optLong("duration", 0L)
        )
    }

    private fun JSONObject.toMediaItem(): EmbyMediaItem? {
        val bvid = optString("bvid").ifBlank { optString("id") }
        if (bvid.isBlank()) return null
        val upper = optJSONObject("upper")
        return EmbyMediaItem(
            id = bvid,
            name = optString("title", "Bilibili 视频"),
            type = "Video",
            imageUrl = optString("cover"),
            playbackProgress = 0f,
            played = false,
            subtitle = upper?.optString("name").orEmpty(),
            sourceType = "Bilibili"
        )
    }

    private fun getJsonObject(url: String, cookie: String = ""): JSONObject {
        return JSONObject(getTextResponse(url, cookie).body)
    }

    private fun getTextResponse(
        url: String,
        cookie: String = "",
        includeHeaders: Boolean = false
    ): BilibiliHttpResponse {
        Log.d(TAG, "GET ${URL(url).path}")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Mozilla/5.0 VideoHub Android")
            setRequestProperty("Referer", "https://www.bilibili.com")
            if (cookie.isNotBlank()) {
                setRequestProperty("Cookie", cookie)
            }
        }
        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        val headers = if (includeHeaders) connection.headerFields else emptyMap()
        connection.disconnect()
        if (responseCode !in 200..299) {
            throw IllegalStateException("Bilibili 请求失败($responseCode): $body")
        }
        return BilibiliHttpResponse(body = body, headers = headers)
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }
}

object BilibiliSessionStore {
    private const val PREFERENCES_NAME = "bilibili_session"

    fun save(context: Context, session: BilibiliSession) {
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("mid", session.mid)
            .putString("username", session.username)
            .putString("face", session.face)
            .putString("cookie", session.cookie)
            .apply()
    }

    fun load(context: Context): BilibiliSession? {
        val preferences = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val mid = preferences.getString("mid", null)
        val cookie = preferences.getString("cookie", null)
        if (mid.isNullOrBlank() || cookie.isNullOrBlank()) return null
        return BilibiliSession(
            mid = mid,
            username = preferences.getString("username", "Bilibili 用户") ?: "Bilibili 用户",
            face = preferences.getString("face", "") ?: "",
            cookie = cookie
        )
    }

    fun clear(context: Context) {
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

data class BilibiliQrCode(
    val url: String,
    val qrcodeKey: String
)

data class BilibiliQrPollResult(
    val code: Int,
    val message: String,
    val cookie: String,
    val isSuccess: Boolean
)

data class BilibiliSession(
    val mid: String,
    val username: String,
    val face: String,
    val cookie: String
)

data class BilibiliVideoDetail(
    val bvid: String,
    val title: String,
    val description: String,
    val cover: String,
    val authorName: String,
    val authorFace: String,
    val durationSeconds: Long
)

private data class BilibiliHttpResponse(
    val body: String,
    val headers: Map<String, List<String>>
)
