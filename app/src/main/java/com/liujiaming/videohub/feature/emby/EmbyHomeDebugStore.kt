package com.liujiaming.videohub.feature.emby

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object EmbyHomeDebugStore {
    private const val PREFERENCES_NAME = "emby_home_debug"
    private const val KEY_STATE = "state"

    fun markLoading(context: Context) {
        save(context, EmbyHomeDebugState(status = "正在拉取 Emby 媒体库数据..."))
    }

    fun markSuccess(context: Context, home: EmbyMediaHome) {
        save(
            context,
            EmbyHomeDebugState(
                status = "Emby 媒体库数据拉取成功，已缓存到本地",
                libraries = home.libraries.map { "${it.name} (${it.itemCount}) [${it.collectionType.ifBlank { "unknown" }}]" }
            )
        )
    }

    fun markFailure(context: Context, error: Throwable) {
        save(
            context,
            EmbyHomeDebugState(
                status = "Emby 媒体库数据拉取失败",
                error = buildString {
                    append(error::class.java.simpleName)
                    append(": ")
                    append(error.message ?: "未知错误")
                    val firstStackLine = error.stackTrace.firstOrNull()
                    if (firstStackLine != null) {
                        append("\n")
                        append(firstStackLine.toString())
                    }
                }
            )
        )
    }

    fun load(context: Context): EmbyHomeDebugState? {
        val jsonText = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_STATE, null) ?: return null

        return runCatching {
            val json = JSONObject(jsonText)
            EmbyHomeDebugState(
                status = json.optString("status"),
                libraries = json.optJSONArray("libraries").toStringList(),
                error = json.optString("error").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    private fun save(context: Context, state: EmbyHomeDebugState) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STATE, state.toJson().toString())
            .apply()
    }

    private fun EmbyHomeDebugState.toJson(): JSONObject {
        val libraryArray = JSONArray()
        libraries.forEach { libraryArray.put(it) }
        return JSONObject()
            .put("status", status)
            .put("libraries", libraryArray)
            .put("error", error.orEmpty())
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }
}

data class EmbyHomeDebugState(
    val status: String,
    val libraries: List<String> = emptyList(),
    val error: String? = null
)
