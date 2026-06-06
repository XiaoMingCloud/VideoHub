package com.liujiaming.videohub.feature.emby

import androidx.compose.runtime.Composable
import com.liujiaming.videohub.feature.server.AddMediaServerFormScreen

@Composable
fun AddEmbyScreen(onBackClick: () -> Unit) {
    AddMediaServerFormScreen(
        serverName = "Emby",
        defaultPort = "8096",
        onBackClick = onBackClick
    )
}
