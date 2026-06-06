package com.liujiaming.videohub.feature.jellyfin

import androidx.compose.runtime.Composable
import com.liujiaming.videohub.feature.server.AddMediaServerFormScreen

/**
 * 添加 Jellyfin 服务器页面。
 * 复用通用的 [AddMediaServerFormScreen] 表单组件，服务器名称为“Jellyfin”，默认端口为 8096。
 * 当前仅提供基础表单，尚未实现具体的认证逻辑。
 *
 * @param onBackClick 返回上一页的回调
 */
@Composable
fun AddJellyfinScreen(onBackClick: () -> Unit) {
    AddMediaServerFormScreen(
        serverName = "Jellyfin",
        defaultPort = "8096",           // Jellyfin 默认端口
        onBackClick = onBackClick
    )
}
