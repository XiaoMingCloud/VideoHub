package com.liujiaming.videohub.feature.settings

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
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
import java.util.Locale
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.DividerGray
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

// ========================================================================
// 下载管理页面
// ========================================================================

/**
 * 下载设置页面
 *
 * 展示下载文件列表，包含：
 * - "正在下载" 和 "已下载" 两个标签页
 * - 空状态提示（当前无下载文件）
 * - 底部磁盘空间使用指示器
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun DownloadSettingsScreen(onBackClick: () -> Unit) {
    // 当前选中的标签页索引（0=正在下载，1=已下载）
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = PageBackground,
        bottomBar = { DiskSpaceIndicator() } // 底部磁盘空间指示器
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // 顶部导航栏（含"编辑"按钮）
            DownloadTopBar(onBackClick)
            // 标签页切换
            DownloadTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // 内容区域 - 当前显示空状态
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyDownloadState()
            }
        }
    }
}

// ========================================================================
// 辅助组件
// ========================================================================

/**
 * 下载页面顶部导航栏
 *
 * 居中显示"下载"标题，左侧返回按钮，右侧"编辑"操作按钮。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
private fun DownloadTopBar(onBackClick: () -> Unit) {
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
            text = "下载",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )

        // 右侧"编辑"按钮
        Text(
            text = "编辑",
            color = ActiveGreen,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { }
                .padding(horizontal = 20.dp, vertical = 10.5.dp)
        )
    }
}

/**
 * 下载标签页切换组件
 *
 * 包含"正在下载"和"已下载"两个标签，选中标签底部有绿色指示条。
 *
 * @param selectedTab 当前选中标签索引
 * @param onTabSelected 标签选择回调
 */
@Composable
private fun DownloadTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("正在下载", "已下载")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.25.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标签文字
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selectedTab == index) ActiveGreen else TextGray,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal,
                            letterSpacing = 0.sp
                        )
                    }

                    // 底部指示条（选中时显示绿色）
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .fillMaxWidth(0.32f)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(if (selectedTab == index) ActiveGreen else Color.Transparent)
                    )
                }
            }
        }

        // 标签页底部分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerGray)
        )
    }
}

/**
 * 空下载状态占位组件
 *
 * 当没有下载文件时，显示文件夹图标和提示文字。
 */
@Composable
private fun EmptyDownloadState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 28.dp)
    ) {
        // 文件夹图标（根据深色/浅色模式切换颜色）
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = if (SettingsMemory.resolvedDarkMode) Color(0xFF3A3A3D) else Color(0xFFE1E1E6),
            modifier = Modifier.size(112.dp)
        )

        Text(
            text = "没有下载文件",
            color = TextGray,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * 底部磁盘空间指示器
 *
 * 显示设备磁盘使用情况的进度条和文字信息（已用/可用）。
 */
@Composable
private fun DiskSpaceIndicator() {
    // 读取磁盘空间信息（仅在首次组合时计算）
    val diskSpace = remember { readDeviceDiskSpace() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PageBackground)
            .padding(horizontal = 20.dp, vertical = 15.85.dp)
    ) {
        // 磁盘使用进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DividerGray)
        ) {
            // 已使用部分（绿色填充）
            Box(
                modifier = Modifier
                    .fillMaxWidth(diskSpace.usedRatio)
                    .height(4.dp)
                    .background(ActiveGreen)
            )
        }

        // 已使用/可用空间文字
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${diskSpace.usedText} 已使用",
                color = ActiveGreen,
                fontSize = 13.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${diskSpace.availableText} 可用",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                letterSpacing = 0.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ========================================================================
// 数据模型与工具函数
// ========================================================================

/**
 * 磁盘空间数据模型
 *
 * @property usedText 已使用空间的格式化文本（如 "25.30 GB"）
 * @property availableText 可用空间的格式化文本（如 "100.50 GB"）
 * @property usedRatio 已使用空间占比（0.0 ~ 1.0），用于进度条显示
 */
private data class DiskSpace(
    val usedText: String,
    val availableText: String,
    val usedRatio: Float
)

/**
 * 读取设备磁盘空间信息
 *
 * 通过 [StatFs] 获取设备数据目录的总容量、可用容量，
 * 计算已使用容量和使用比例。
 *
 * @return 磁盘空间数据对象
 */
private fun readDeviceDiskSpace(): DiskSpace {
    val statFs = StatFs(Environment.getDataDirectory().path)
    val totalBytes = statFs.totalBytes.coerceAtLeast(1L)
    val availableBytes = statFs.availableBytes.coerceAtLeast(0L)
    val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

    return DiskSpace(
        usedText = formatStorageSize(usedBytes),
        availableText = formatStorageSize(availableBytes),
        usedRatio = (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
    )
}

/**
 * 将字节数格式化为可读的存储大小字符串
 *
 * 优先以 GB 为单位显示，不足 1 GB 时以 MB 为单位显示。
 *
 * @param bytes 字节数
 * @return 格式化后的字符串，如 "25.30 GB" 或 "512.00 MB"
 */
private fun formatStorageSize(bytes: Long): String {
    val gb = bytes / 1024f / 1024f / 1024f
    if (gb >= 1f) {
        return String.format(Locale.US, "%.2f GB", gb)
    }

    val mb = bytes / 1024f / 1024f
    return String.format(Locale.US, "%.2f MB", mb)
}
