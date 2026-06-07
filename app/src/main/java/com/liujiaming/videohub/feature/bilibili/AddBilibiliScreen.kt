package com.liujiaming.videohub.feature.bilibili

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.liujiaming.videohub.feature.media.MediaSourceSelectionStore
import com.liujiaming.videohub.feature.media.MediaSourceType
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URLEncoder

@Composable
fun AddBilibiliScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var qrCode by remember { mutableStateOf<BilibiliQrCode?>(null) }
    var statusText by remember { mutableStateOf("正在生成二维码...") }
    var isLoading by remember { mutableStateOf(true) }
    var loginFinished by remember { mutableStateOf(false) }
    var refreshSignal by remember { mutableStateOf(0) }

    LaunchedEffect(refreshSignal) {
        loginFinished = false
        isLoading = true
        statusText = "正在生成二维码..."
        val result = withContext(Dispatchers.IO) {
            runCatching { BilibiliClient.generateQrCode() }
        }
        result.onSuccess {
            qrCode = it
            isLoading = false
            statusText = "请使用哔哩哔哩 App 扫码并确认登录"
        }.onFailure {
            isLoading = false
            statusText = it.message ?: "二维码生成失败"
        }
    }

    LaunchedEffect(qrCode?.qrcodeKey, loginFinished) {
        val key = qrCode?.qrcodeKey ?: return@LaunchedEffect
        while (!loginFinished) {
            delay(2500)
            val result = withContext(Dispatchers.IO) {
                runCatching { BilibiliClient.pollQrCode(key) }
            }
            result.onSuccess { poll ->
                when {
                    poll.isSuccess -> {
                        val session = withContext(Dispatchers.IO) {
                            BilibiliClient.fetchSessionFromCookie(poll.cookie)
                        }
                        BilibiliSessionStore.save(context, session)
                        MediaSourceSelectionStore.save(context, MediaSourceType.Bilibili)
                        loginFinished = true
                        statusText = "Bilibili 登录成功"
                        Toast.makeText(context, "Bilibili 登录成功", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                    poll.code == 86101 -> statusText = "等待扫码..."
                    poll.code == 86090 -> statusText = "已扫码，请在手机上确认"
                    poll.code == 86038 -> {
                        statusText = "二维码已过期，请刷新"
                        loginFinished = true
                    }
                    else -> statusText = poll.message.ifBlank { "等待登录确认..." }
                }
            }.onFailure {
                statusText = it.message ?: "轮询登录状态失败"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = PrimaryText)
            }
            Text(
                text = "添加 Bilibili",
                color = PrimaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val qr = qrCode
                    when {
                        isLoading -> CircularProgressIndicator(color = ActiveGreen)
                        qr != null -> QrCodeWebView(qr.url)
                        else -> Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Text(
                    text = statusText,
                    color = TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 18.dp),
                    letterSpacing = 0.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { refreshSignal++ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActiveGreen,
                        contentColor = Color.White
                    ),
                    elevation = null,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text("刷新二维码", letterSpacing = 0.sp)
                }
            }
        }
    }
}

@Composable
private fun QrCodeWebView(loginUrl: String) {
    val qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=240x240&margin=10&data=" +
        URLEncoder.encode(loginUrl, Charsets.UTF_8.name())
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                setBackgroundColor(android.graphics.Color.WHITE)
                loadDataWithBaseURL(
                    null,
                    """<html><body style="margin:0;display:flex;align-items:center;justify-content:center;background:#fff;"><img width="240" height="240" src="$qrImageUrl"/></body></html>""",
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                """<html><body style="margin:0;display:flex;align-items:center;justify-content:center;background:#fff;"><img width="240" height="240" src="$qrImageUrl"/></body></html>""",
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = Modifier.size(240.dp)
    )
}
