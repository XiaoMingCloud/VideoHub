package com.liujiaming.videohub.feature.emby

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

object EmbyHomeClient {
    fun fetchHome(session: EmbyAuthSession): EmbyMediaHome {
        val libraries = fetchLibraries(session)
        val resumeItems = runCatching {
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
        }.getOrDefault(emptyList())
        val latestItems = runCatching {
            fetchLatestItems(session)
        }.getOrDefault(emptyList())

        return EmbyMediaHome(
            sourceName = session.serverName.ifBlank { "Emby" },
            libraries = libraries,
            resumeItems = resumeItems,
            latestTitle = libraries.firstOrNull()?.name ?: "最新媒体",
            latestItems = latestItems
        )
    }

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
                        itemCount = fetchLibraryItemCount(session, id, fallbackCount),
                        imageUrl = imageUrl(session, id, 240)
                    )
                )
            }
        }
    }

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
                    "Limit" to "0"
                )
            )
            json.optInt("TotalRecordCount", fallbackCount)
        }.getOrDefault(fallbackCount)
    }

    private fun fetchLatestItems(session: EmbyAuthSession): List<EmbyMediaItem> {
        val jsonArray = getJsonArrayResponse(
            session = session,
            path = "/Users/${session.userId}/Items/Latest",
            query = mapOf("Limit" to "10")
        )

        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                add(item.toMediaItem(session))
            }
        }
    }

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

    private fun JSONObject.toMediaItem(session: EmbyAuthSession): EmbyMediaItem {
        val id = optString("Id")
        return EmbyMediaItem(
            id = id,
            name = optString("Name", "未命名"),
            type = optString("Type"),
            imageUrl = if (id.isBlank()) {
                ""
            } else {
                imageUrl(session, id, 360)
            }
        )
    }

    private fun imageUrl(session: EmbyAuthSession, itemId: String, height: Int): String {
        return "${session.serverUrl}/Items/$itemId/Images/Primary?fillHeight=$height&quality=90&api_key=${encode(session.accessToken)}"
    }

    private fun getJsonObject(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String> = emptyMap()
    ): JSONObject {
        return JSONObject(getTextResponse(session, path, query))
    }

    private fun getJsonArrayResponse(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String> = emptyMap()
    ): JSONArray {
        return JSONArray(getTextResponse(session, path, query))
    }

    private fun getTextResponse(
        session: EmbyAuthSession,
        path: String,
        query: Map<String, String>
    ): String {
        val url = buildUrl(session.serverUrl, path, query)
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

        if (responseCode !in 200..299) {
            throw EmbyAuthException("获取 Emby 媒体库失败($responseCode): $response")
        }

        return response
    }

    private fun buildUrl(baseUrl: String, path: String, query: Map<String, String>): String {
        val queryText = query.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        return if (queryText.isBlank()) "$baseUrl$path" else "$baseUrl$path?$queryText"
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }
}

data class EmbyMediaHome(
    val sourceName: String,
    val libraries: List<EmbyLibrarySummary>,
    val resumeItems: List<EmbyMediaItem>,
    val latestTitle: String,
    val latestItems: List<EmbyMediaItem>
)

data class EmbyLibrarySummary(
    val id: String,
    val name: String,
    val collectionType: String,
    val itemCount: Int,
    val imageUrl: String = ""
)

data class EmbyMediaItem(
    val id: String,
    val name: String,
    val type: String,
    val imageUrl: String
)
