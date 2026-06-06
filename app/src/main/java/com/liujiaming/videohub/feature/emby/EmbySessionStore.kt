package com.liujiaming.videohub.feature.emby

import android.content.Context

object EmbySessionStore {
    private const val PREFERENCES_NAME = "emby_session"

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

    fun load(context: Context): EmbyAuthSession? {
        val preferences = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        if (!preferences.getBoolean("isLoggedIn", false)) return null

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

    fun isLoggedIn(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean("isLoggedIn", false)
    }
}
