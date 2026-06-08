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
import java.security.MessageDigest

object BilibiliClient {
    private const val TAG = "BilibiliClient"
    private const val BASE_URL = "https://api.bilibili.com"
    private const val PASSPORT_URL = "https://passport.bilibili.com"
    private val WBI_MIXIN_KEY_ENC_TAB = intArrayOf(
        46, 47, 18, 2, 53, 8, 23, 32,
        15, 50, 10, 31, 58, 3, 45, 35,
        27, 43, 5, 49, 33, 9, 42, 19,
        29, 28, 14, 39, 12, 38, 41, 13,
        37, 48, 7, 16, 24, 55, 40, 61,
        26, 17, 0, 1, 60, 51, 30, 4,
        22, 25, 54, 21, 56, 59, 6, 63,
        57, 62, 11, 36, 20, 34, 44, 52
    )

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
        val previewsByFolderId = folders.associate { folder ->
            folder.id to runCatching {
                fetchFavoriteResources(session, folder.id, startIndex = 0, limit = 12).items
            }.getOrDefault(emptyList())
        }
        val foldersWithCover = folders.map { folder ->
            val previewCover = previewsByFolderId[folder.id]
                ?.firstOrNull { it.imageUrl.isNotBlank() }
                ?.imageUrl
                .orEmpty()
            if (folder.imageUrl.isBlank() && previewCover.isNotBlank()) {
                folder.copy(imageUrl = previewCover)
            } else {
                folder
            }
        }
        val sections = foldersWithCover.take(8).map { folder ->
            EmbyLibrarySection(
                libraryId = folder.id,
                title = folder.name,
                items = previewsByFolderId[folder.id].orEmpty()
            )
        }
        val latestItems = sections.firstOrNull { it.items.isNotEmpty() }?.items.orEmpty()
        return EmbyMediaHome(
            sourceName = "Bilibili · ${session.username}",
            libraries = foldersWithCover,
            resumeItems = emptyList(),
            latestTitle = foldersWithCover.firstOrNull()?.name ?: "Bilibili 收藏",
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
        val apiCode = json.optInt("code", -1)
        if (apiCode != 0) {
            error("Bilibili 视频详情接口返回错误 code=$apiCode message=${json.optString("message")}")
        }
        val data = json.optJSONObject("data") ?: JSONObject()
        val owner = data.optJSONObject("owner")
        val firstPage = data.optJSONArray("pages")?.optJSONObject(0)
        return BilibiliVideoDetail(
            bvid = data.optString("bvid", bvid),
            title = data.optString("title"),
            description = data.optString("desc"),
            cover = data.optString("pic"),
            authorName = owner?.optString("name").orEmpty(),
            authorFace = owner?.optString("face").orEmpty(),
            durationSeconds = data.optLong("duration", 0L),
            cid = firstPage?.optLong("cid", 0L)?.takeIf { it > 0L }
                ?: data.optLong("cid", 0L)
        )
    }

    fun fetchPlaybackSource(session: BilibiliSession, bvid: String): BilibiliPlaybackSource {
        val detail = fetchVideoDetail(session, bvid)
        val cid = detail.cid
        if (cid <= 0L) error("无法获取视频分片 ID")

        val headers = playbackHeaders(session, bvid)
        val attempts = listOf(
            runCatching {
                val signedUrl = buildWbiPlayUrl(
                    session = session,
                    bvid = bvid,
                    cid = cid,
                    qn = "64",
                    fnval = "0",
                    platform = "html5"
                )
                parsePlaybackJson(
                    json = getJsonObject(signedUrl, cookie = session.cookie),
                    headers = headers,
                    label = "wbi-html5"
                )
            },
            runCatching {
                val signedUrl = buildWbiPlayUrl(
                    session = session,
                    bvid = bvid,
                    cid = cid,
                    qn = "64",
                    fnval = "1",
                    platform = "pc"
                )
                parsePlaybackJson(
                    json = getJsonObject(signedUrl, cookie = session.cookie),
                    headers = headers,
                    label = "wbi-pc"
                )
            },
            runCatching {
                parsePlaybackJson(
                    json = getJsonObject(
                        "$BASE_URL/x/player/playurl?bvid=${encode(bvid)}&cid=$cid&qn=64&platform=html5&fnval=0&fnver=0&fourk=0",
                        cookie = session.cookie
                    ),
                    headers = headers,
                    label = "legacy-html5"
                )
            }
        )

        attempts.firstOrNull { it.isSuccess }?.getOrNull()?.let { return it }
        attempts.forEachIndexed { index, attempt ->
            attempt.exceptionOrNull()?.let { error ->
                Log.w(TAG, "playurl attempt#$index failed bvid=$bvid cid=$cid: ${error.message}")
            }
        }
        val message = attempts.mapIndexedNotNull { index, attempt ->
            attempt.exceptionOrNull()?.let { error ->
                "#$index ${error.message ?: error::class.java.simpleName}"
            }
        }.joinToString(" | ")
        error(message.ifBlank { "Bilibili 播放地址获取失败" })
    }

    fun fetchPlaybackUrl(session: BilibiliSession, bvid: String): String {
        return fetchPlaybackSource(session, bvid).url
    }

    private fun parsePlaybackJson(
        json: JSONObject,
        headers: Map<String, String>,
        label: String
    ): BilibiliPlaybackSource {
        val apiCode = json.optInt("code", -1)
        if (apiCode != 0) {
            error("$label 返回错误 code=$apiCode message=${json.optString("message")}")
        }
        val data = json.optJSONObject("data")
            ?: error("$label 响应缺少 data")
        val durl = data.optJSONArray("durl")
        val firstUrl = durl?.optJSONObject(0)?.optString("url")
            ?: durl?.optJSONObject(0)?.optJSONArray("backup_url")?.optString(0)
        if (!firstUrl.isNullOrBlank()) {
            Log.d(TAG, "playurl success label=$label type=durl quality=${data.optInt("quality")} format=${data.optString("format")}")
            return BilibiliPlaybackSource(url = firstUrl, headers = headers)
        }

        val dashVideo = data.optJSONObject("dash")?.optJSONArray("video")
        val dashUrl = dashVideo?.optJSONObject(0)?.optString("baseUrl")
            ?: dashVideo?.optJSONObject(0)?.optString("base_url")
        if (!dashUrl.isNullOrBlank()) {
            Log.d(TAG, "playurl success label=$label type=dash quality=${data.optInt("quality")} format=${data.optString("format")}")
            return BilibiliPlaybackSource(url = dashUrl, headers = headers)
        }
        error("$label 无法解析视频流地址 quality=${data.optInt("quality")} format=${data.optString("format")}")
    }

    private fun buildWbiPlayUrl(
        session: BilibiliSession,
        bvid: String,
        cid: Long,
        qn: String,
        fnval: String,
        platform: String
    ): String {
        val params = linkedMapOf(
            "bvid" to bvid,
            "cid" to cid.toString(),
            "qn" to qn,
            "platform" to platform,
            "fnval" to fnval,
            "fnver" to "0",
            "fourk" to "0"
        )
        val signed = signWbiParams(session, params)
        return "$BASE_URL/x/player/wbi/playurl?" + signed.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
    }

    private fun signWbiParams(
        session: BilibiliSession,
        params: Map<String, String>
    ): Map<String, String> {
        val nav = getJsonObject("$BASE_URL/x/web-interface/nav", cookie = session.cookie)
        val wbiImg = nav.optJSONObject("data")?.optJSONObject("wbi_img")
            ?: error("无法获取 Bilibili WBI 签名图片")
        val imgKey = wbiImg.optString("img_url").substringAfterLast('/').substringBefore('.')
        val subKey = wbiImg.optString("sub_url").substringAfterLast('/').substringBefore('.')
        val mixinKey = WBI_MIXIN_KEY_ENC_TAB.joinToString("") { index ->
            (imgKey + subKey).getOrNull(index)?.toString().orEmpty()
        }.take(32)
        val signed = params.toMutableMap()
        signed["wts"] = (System.currentTimeMillis() / 1000L).toString()
        val query = signed.toSortedMap().entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value.filterWbiValue())}"
        }
        signed["w_rid"] = md5(query + mixinKey)
        return signed.toSortedMap()
    }

    private fun playbackHeaders(session: BilibiliSession, bvid: String): Map<String, String> {
        return mapOf(
            "User-Agent" to "Mozilla/5.0 VideoHub Android",
            "Referer" to "https://www.bilibili.com/video/$bvid",
            "Origin" to "https://www.bilibili.com",
            "Cookie" to session.cookie
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

    private fun String.filterWbiValue(): String {
        return filterNot { it in "!'()*" }
    }

    private fun md5(value: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
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
    val durationSeconds: Long,
    val cid: Long = 0L
)

data class BilibiliPlaybackSource(
    val url: String,
    val headers: Map<String, String>
)

private data class BilibiliHttpResponse(
    val body: String,
    val headers: Map<String, List<String>>
)
