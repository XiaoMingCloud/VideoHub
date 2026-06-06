package com.liujiaming.videohub.feature.emby

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Emby 首页数据拉取调试状态存储。
 * 使用 SharedPreferences 持久化首页数据拉取的状态信息（加载中/成功/失败），
 * 用于在媒体库页面展示调试信息，帮助开发者排查问题。
 */
object EmbyHomeDebugStore {
    /** SharedPreferences 文件名 */
    private const val PREFERENCES_NAME = "emby_home_debug"
    /** 存储状态的 key */
    private const val KEY_STATE = "state"

    /**
     * 标记首页数据正在加载中。
     *
     * @param context 应用上下文
     */
    fun markLoading(context: Context) {
        save(context, EmbyHomeDebugState(status = "正在拉取 Emby 媒体库数据..."))
    }

    /**
     * 标记首页数据拉取成功，并记录各媒体库的摘要信息。
     *
     * @param context 应用上下文
     * @param home 拉取成功的首页数据
     */
    fun markSuccess(context: Context, home: EmbyMediaHome) {
        save(
            context,
            EmbyHomeDebugState(
                status = "Emby 媒体库数据拉取成功，已缓存到本地",
                // 记录每个媒体库的名称、项目数和集合类型
                libraries = home.libraries.map { "${it.name} (${it.itemCount}) [${it.collectionType.ifBlank { "unknown" }}]" }
            )
        )
    }

    /**
     * 标记首页数据拉取失败，并记录错误详情（异常类名、消息、首行堆栈）。
     *
     * @param context 应用上下文
     * @param error 失败时的异常对象
     */
    fun markFailure(context: Context, error: Throwable) {
        save(
            context,
            EmbyHomeDebugState(
                status = "Emby 媒体库数据拉取失败",
                error = buildString {
                    append(error::class.java.simpleName)  // 异常类名
                    append(": ")
                    append(error.message ?: "未知错误")    // 异常消息
                    // 附加第一行堆栈信息，便于快速定位问题
                    val firstStackLine = error.stackTrace.firstOrNull()
                    if (firstStackLine != null) {
                        append("\n")
                        append(firstStackLine.toString())
                    }
                }
            )
        )
    }

    /**
     * 从本地存储加载调试状态。
     *
     * @param context 应用上下文
     * @return 调试状态，如果无存储数据或解析失败则返回 null
     */
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

    /**
     * 将调试状态序列化为 JSON 并保存到 SharedPreferences。
     */
    private fun save(context: Context, state: EmbyHomeDebugState) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STATE, state.toJson().toString())
            .apply()
    }

    /** 将 [EmbyHomeDebugState] 序列化为 JSONObject */
    private fun EmbyHomeDebugState.toJson(): JSONObject {
        val libraryArray = JSONArray()
        libraries.forEach { libraryArray.put(it) }
        return JSONObject()
            .put("status", status)
            .put("libraries", libraryArray)
            .put("error", error.orEmpty())
    }

    /** 将 JSONArray 安全地转换为字符串列表 */
    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }
}

/**
 * Emby 首页数据拉取的调试状态。
 *
 * @property status 状态描述文本（加载中/成功/失败）
 * @property libraries 媒体库摘要信息列表（成功时有值）
 * @property error 错误详情（失败时有值）
 */
data class EmbyHomeDebugState(
    val status: String,
    val libraries: List<String> = emptyList(),
    val error: String? = null
)
