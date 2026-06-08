package com.liujiaming.videohub.feature.filesource

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object LocalFileSourceStore {
    private const val PREFS_NAME = "local_file_sources"
    private const val KEY_SOURCES = "sources"

    fun load(context: Context): List<LocalFileSource> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SOURCES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val uri = item.optString("uri")
                    if (uri.isBlank()) continue
                    add(
                        LocalFileSource(
                            id = item.optString("id", uri),
                            name = item.optString("name", "我的本地目录"),
                            uri = uri,
                            createdAt = item.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, source: LocalFileSource) {
        val existing = load(context).filterNot { it.uri == source.uri }
        saveAll(context, listOf(source) + existing)
    }

    private fun saveAll(context: Context, sources: List<LocalFileSource>) {
        val array = JSONArray()
        sources.forEach { source ->
            array.put(
                JSONObject()
                    .put("id", source.id)
                    .put("name", source.name)
                    .put("uri", source.uri)
                    .put("createdAt", source.createdAt)
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SOURCES, array.toString())
            .apply()
    }
}

data class LocalFileSource(
    val id: String,
    val name: String,
    val uri: String,
    val createdAt: Long
)
