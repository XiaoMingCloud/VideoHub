package com.liujiaming.videohub.feature.jellyfin

import androidx.compose.runtime.Composable
import com.liujiaming.videohub.feature.server.AddMediaServerFormScreen

@Composable
fun AddJellyfinScreen(onBackClick: () -> Unit) {
    AddMediaServerFormScreen(
        serverName = "Jellyfin",
        defaultPort = "8096",
        onBackClick = onBackClick
    )
}
