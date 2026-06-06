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

// ========================================================================
// 资源设置主页面
// ========================================================================

/**
 * 资源设置页面
 *
 * 展示媒体库统计信息和资源管理操作：
 * - 媒体库视频数量统计（电影、电视剧、其他）
 * - 重新扫描文件
 * - 刷新在线影视数据（后台线程拉取 Emby 数据并缓存）
 * - 缓存图片和视频数据管理
 * - 缓存字幕文件管理
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun ResourceSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    // 是否正在刷新在线影视数据
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

                // 资源管理卡片（含统计和操作项）
                ResourceCard(
                    isRefreshing = isRefreshing,
                    onRefreshOnlineMetadata = {
                        // 防止重复刷新
                        if (isRefreshing) return@ResourceCard
                        // 检查 Emby 登录状态
                        val session = EmbySessionStore.load(context)
                        if (session == null) {
                            Toast.makeText(context, "请先登录 Emby", Toast.LENGTH_SHORT).show()
                            return@ResourceCard
                        }

                        isRefreshing = true
                        // 在后台线程拉取 Emby 首页数据
                        Thread {
                            val result = runCatching {
                                EmbyHomeClient.fetchHome(session)
                            }
                            (context as? Activity)?.runOnUiThread {
                                isRefreshing = false
                                result
                                    .onSuccess { home ->
                                        // 保存首页数据缓存
                                        EmbyHomeCache.save(context, session.userId, home)
                                        // 预加载首页图片
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

// ========================================================================
// 辅助组件
// ========================================================================

/**
 * 资源设置页面顶部导航栏
 *
 * 居中显示"资源"标题，左侧放置返回按钮。
 *
 * @param onBackClick 返回按钮点击回调
 */
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

/**
 * 资源管理卡片
 *
 * 展示媒体库统计信息和资源操作项：
 * - 分类统计（电影、电视剧、其他）
 * - 总视频数和更新时间
 * - 重新扫描文件 / 刷新在线影视数据
 * - 缓存图片和视频 / 缓存字幕文件
 *
 * @param isRefreshing 是否正在刷新中
 * @param onRefreshOnlineMetadata 刷新在线影视数据回调
 */
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
            // 卡片标题
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

            // 分类统计行（电影、电视剧、其他）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(label = "电影", value = "0", modifier = Modifier.weight(1f))
                StatBox(label = "电视剧", value = "0", modifier = Modifier.weight(1f))
                StatBox(label = "其他", value = "0", modifier = Modifier.weight(1f))
            }

            // 总视频数和上次更新时间
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

            // 分隔线
            Divider(
                modifier = Modifier.padding(vertical = 14.15.dp),
                thickness = 1.dp,
                color = DividerGray
            )

            // 操作项列表
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

/**
 * 统计数值方块组件
 *
 * 显示某一分类的视频数量和标签（如"电影 0"），
 * 背景色根据深色/浅色模式自适应。
 *
 * @param label 分类标签文本（如"电影"、"电视剧"）
 * @param value 数值文本（如"0"）
 * @param modifier 布局修饰符
 */
@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    // 根据深色/浅色模式选择不同的背景色
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
        // 数值
        Text(
            text = value,
            color = PrimaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        )

        // 分类标签
        Text(
            text = label,
            color = TextGray,
            fontSize = 13.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * 资源操作项（可点击的绿色文本按钮）
 *
 * @param title 操作项标题
 * @param onClick 点击回调（默认空操作）
 */
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

/**
 * 缓存管理操作项
 *
 * 左侧显示标题和缓存大小，右侧显示"清除"操作按钮。
 *
 * @param title 缓存项标题
 * @param size 缓存大小文本（如 "6.39 MB"）
 */
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
        // 标题 + 缓存大小
        Text(
            text = "$title ($size)",
            color = PrimaryText,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // "清除"按钮
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
