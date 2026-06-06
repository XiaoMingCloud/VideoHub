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

@Composable
fun AddEmbyScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }

    AddMediaServerFormScreen(
        serverName = "Emby",
        defaultPort = "8096",
        onBackClick = onBackClick,
        submitText = "登录",
        isSubmitting = isSubmitting,
        submitEnabled = { form ->
            form.address.isNotBlank() &&
                form.username.isNotBlank() &&
                form.protocol.isNotBlank()
        },
        onSubmit = { form ->
            if (isSubmitting) return@AddMediaServerFormScreen
            isSubmitting = true
            Thread {
                val result = runCatching {
                    val deviceId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    ) ?: "VideoHubAndroid"

                    val session = EmbyAuthClient.authenticate(
                        protocol = form.protocol,
                        address = form.address,
                        port = form.port,
                        username = form.username,
                        password = form.password,
                        deviceId = deviceId
                    )

                    EmbySessionStore.save(context, session)
                    EmbyHomeDebugStore.markLoading(context)

                    runCatching {
                        val home = EmbyHomeClient.fetchHome(session)
                        EmbyHomeCache.save(context, session.userId, home)
                        EmbyImageCache.prefetchHomeImages(context, home)
                        EmbyHomeDebugStore.markSuccess(context, home)
                    }.onFailure { error ->
                        EmbyHomeDebugStore.markFailure(context, error)
                    }
                    session
                }

                (context as? android.app.Activity)?.runOnUiThread {
                    isSubmitting = false
                    result
                        .onSuccess {
                            Toast.makeText(context, "Emby 登录成功，正在首页显示媒体库调试信息", Toast.LENGTH_SHORT).show()
                            onBackClick()
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
