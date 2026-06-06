package com.liujiaming.videohub.feature.fnos

import androidx.compose.runtime.Composable
import com.liujiaming.videohub.feature.server.AddMediaServerFormScreen

@Composable
fun AddFnosScreen(onBackClick: () -> Unit) {
    AddMediaServerFormScreen(
        serverName = "飞牛私有云",
        defaultPort = "5666",
        onBackClick = onBackClick
    )
}
