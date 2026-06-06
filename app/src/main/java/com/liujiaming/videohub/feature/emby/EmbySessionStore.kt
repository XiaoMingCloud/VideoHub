package com.liujiaming.videohub.feature.emby

import android.content.Context

/**
 * Emby 认证会话持久化存储。
 * 使用 SharedPreferences 保存和读取 Emby 登录后的会话信息，
 * 包括服务器信息、用户信息和访问令牌等。
 * 应用重启后可自动恢复登录状态。
 */
object EmbySessionStore {
    /** SharedPreferences 文件名 */
    private const val PREFERENCES_NAME = "emby_session"

    /**
     * 将认证会话信息保存到本地。
     * 同时设置 isLoggedIn 标志为 true。
     *
     * @param context 应用上下文
     * @param session 认证成功的会话信息
     */
    fun save(context: Context, session: EmbyAuthSession) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("isLoggedIn", true)
            .putString("serverName", session.serverName)
            .putString("serverId", session.serverId)
            .putString("serverUrl", session.serverUrl)
            .putString("userId", session.userId)
            .putString("username", session.username)
            .putString("userPrimaryImageTag", session.userPrimaryImageTag)
            .putString("accessToken", session.accessToken)
            .apply()
    }

    /**
     * 从本地存储加载认证会话信息。
     * 验证必要字段（accessToken、serverUrl、userId）是否存在且非空，
     * 任一缺失则视为未登录，返回 null。
     *
     * @param context 应用上下文
     * @return 会话信息，未登录或数据不完整时返回 null
     */
    fun load(context: Context): EmbyAuthSession? {
        val preferences = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        // 未登录或已登出则返回 null
        if (!preferences.getBoolean("isLoggedIn", false)) return null

        // 校验必要字段的完整性
        val accessToken = preferences.getString("accessToken", null)
        val serverUrl = preferences.getString("serverUrl", null)
        val userId = preferences.getString("userId", null)
        if (accessToken.isNullOrBlank() || serverUrl.isNullOrBlank() || userId.isNullOrBlank()) {
            return null
        }

        return EmbyAuthSession(
            serverName = preferences.getString("serverName", "Emby") ?: "Emby",
            serverId = preferences.getString("serverId", "") ?: "",
            serverUrl = serverUrl,
            userId = userId,
            username = preferences.getString("username", "Emby 用户") ?: "Emby 用户",
            userPrimaryImageTag = preferences.getString("userPrimaryImageTag", "") ?: "",
            accessToken = accessToken
        )
    }

    /**
     * 检查用户是否已登录 Emby。
     *
     * @param context 应用上下文
     * @return true 表示已登录
     */
    fun isLoggedIn(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean("isLoggedIn", false)
    }

    fun clear(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun moveToTop(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong("pinnedAt", System.currentTimeMillis())
            .apply()
    }
}
