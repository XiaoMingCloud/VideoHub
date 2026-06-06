package com.liujiaming.videohub.feature.fnos

import androidx.compose.runtime.Composable
import com.liujiaming.videohub.feature.server.AddMediaServerFormScreen

/**
 * 添加飞牛私有云（FnOS）服务器页面。
 * 复用通用的 [AddMediaServerFormScreen] 表单组件，服务器名称为“飞牛私有云”，默认端口为 5666。
 * 当前仅提供基础表单，尚未实现具体的认证逻辑。
 *
 * @param onBackClick 返回上一页的回调
 */
@Composable
fun AddFnosScreen(onBackClick: () -> Unit) {
    AddMediaServerFormScreen(
        serverName = "飞牛私有云",
        defaultPort = "5666",          // FnOS 默认端口
        onBackClick = onBackClick
    )
}
