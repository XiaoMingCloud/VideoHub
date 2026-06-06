package com.liujiaming.videohub.feature.emby

import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.liujiaming.videohub.feature.server.AddMediaServerFormScreen

/**
 * 添加 Emby 服务器页面。
 * 复用通用的 [AddMediaServerFormScreen] 表单组件，默认端口为 8096。
 * 提交时在后台线程中执行 Emby 认证、保存会话、拉取首页媒体数据并预加载封面图片。
 *
 * @param onBackClick 返回上一页的回调
 */
@Composable
fun AddEmbyScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    // 是否正在提交中，防止重复点击
    var isSubmitting by remember { mutableStateOf(false) }

    AddMediaServerFormScreen(
        serverName = "Emby",
        defaultPort = "8096",              // Emby 默认端口
        onBackClick = onBackClick,
        submitText = "登录",
        // 表单验证：地址、用户名和协议不能为空
        submitEnabled = { form ->
            form.address.isNotBlank() &&
                form.username.isNotBlank() &&
                form.protocol.isNotBlank()
        },
        onSubmit = { form ->
            // 防止重复提交
            if (isSubmitting) return@AddMediaServerFormScreen
            isSubmitting = true
            // 在后台线程中执行网络请求
            Thread {
                val result = runCatching {
                    // 获取设备唯一标识符，用于 Emby 认证
                    val deviceId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    ) ?: "VideoHubAndroid"

                    // 调用 Emby API 进行用户认证
                    val session = EmbyAuthClient.authenticate(
                        protocol = form.protocol,
                        address = form.address,
                        port = form.port,
                        username = form.username,
                        password = form.password,
                        deviceId = deviceId
                    )

                    // 保存认证会话信息到本地
                    EmbySessionStore.save(context, session)
                    // 标记首页数据正在加载状态
                    EmbyHomeDebugStore.markLoading(context)

                    // 拉取首页媒体数据并缓存
                    runCatching {
                        val home = EmbyHomeClient.fetchHome(session)
                        EmbyHomeCache.save(context, session.userId, home)        // 缓存首页数据
                        EmbyImageCache.prefetchHomeImages(context, home)        // 预加载封面图片
                        EmbyHomeDebugStore.markSuccess(context, home)            // 标记加载成功
                    }.onFailure { error ->
                        EmbyHomeDebugStore.markFailure(context, error)           // 标记加载失败
                    }
                    session
                }

                // 回到主线程更新 UI 状态
                (context as? android.app.Activity)?.runOnUiThread {
                    isSubmitting = false
                    result
                        .onSuccess {
                            Toast.makeText(context, "Emby 登录成功，正在首页显示媒体库调试信息", Toast.LENGTH_SHORT).show()
                            onBackClick()  // 登录成功后返回上一页
                        }
                        .onFailure { error ->
                            EmbyHomeDebugStore.markFailure(context, error)
                            Toast.makeText(
                                context,
                                error.message ?: "Emby 登录或媒体库拉取失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }.start()
        }
    )
}
