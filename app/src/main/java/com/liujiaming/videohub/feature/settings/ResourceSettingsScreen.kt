package com.liujiaming.videohub.feature.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.liujiaming.videohub.feature.emby.EmbyHomeCache
import com.liujiaming.videohub.feature.emby.EmbyHomeClient
import com.liujiaming.videohub.feature.emby.EmbyImageCache
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.DividerGray
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

@Composable
fun ResourceSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            ResourceTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                ResourceCard(
                    isRefreshing = isRefreshing,
                    onRefreshOnlineMetadata = {
                        if (isRefreshing) return@ResourceCard
                        val session = EmbySessionStore.load(context)
                        if (session == null) {
                            Toast.makeText(context, "请先登录 Emby", Toast.LENGTH_SHORT).show()
                            return@ResourceCard
                        }

                        isRefreshing = true
                        Thread {
                            val result = runCatching {
                                EmbyHomeClient.fetchHome(session)
                            }
                            (context as? Activity)?.runOnUiThread {
                                isRefreshing = false
                                result
                                    .onSuccess { home ->
                                        EmbyHomeCache.save(context, session.userId, home)
                                        EmbyImageCache.prefetchHomeImages(context, home)
                                        Toast.makeText(context, "媒体库缓存已刷新", Toast.LENGTH_SHORT).show()
                                    }
                                    .onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            error.message ?: "刷新失败",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }.start()
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ResourceTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = PrimaryText
            )
        }

        Text(
            text = "资源",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun ResourceCard(
    isRefreshing: Boolean,
    onRefreshOnlineMetadata: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.75.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "媒体库",
                color = PrimaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(label = "电影", value = "0", modifier = Modifier.weight(1f))
                StatBox(label = "电视剧", value = "0", modifier = Modifier.weight(1f))
                StatBox(label = "其他", value = "0", modifier = Modifier.weight(1f))
            }

            Text(
                text = "总视频文件数：0\n上次更新时间：2026年2月23日 00:41",
                color = TextGray,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Divider(
                modifier = Modifier.padding(vertical = 14.15.dp),
                thickness = 1.dp,
                color = DividerGray
            )

            ResourceActionItem("重新扫描文件")
            ResourceActionItem(
                title = if (isRefreshing) "正在刷新在线影视数据..." else "刷新在线影视数据",
                onClick = onRefreshOnlineMetadata
            )
            CacheActionItem(title = "缓存图片和视频数据", size = "6.39 MB")
            CacheActionItem(title = "缓存字幕文件", size = "0 B")
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val statBackground = if (SettingsMemory.resolvedDarkMode) {
        Color(0xFF2A2A2D)
    } else {
        Color(0xFFF5F5F5)
    }

    Column(
        modifier = modifier
            .height(54.75.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(statBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = PrimaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        )

        Text(
            text = label,
            color = TextGray,
            fontSize = 13.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ResourceActionItem(
    title: String,
    onClick: () -> Unit = {}
) {
    Text(
        text = title,
        color = ActiveGreen,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.5.dp)
    )
}

@Composable
private fun CacheActionItem(
    title: String,
    size: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title ($size)",
            color = PrimaryText,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "清除",
            color = ActiveGreen,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
            modifier = Modifier.clickable { }
        )
    }
}
