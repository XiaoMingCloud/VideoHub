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

@Composable
fun DownloadSettingsScreen(onBackClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = PageBackground,
        bottomBar = { DiskSpaceIndicator() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            DownloadTopBar(onBackClick)
            DownloadTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerGray)
        )
    }
}

@Composable
private fun EmptyDownloadState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 28.dp)
    ) {
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

@Composable
private fun DiskSpaceIndicator() {
    val diskSpace = remember { readDeviceDiskSpace() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PageBackground)
            .padding(horizontal = 20.dp, vertical = 15.85.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DividerGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(diskSpace.usedRatio)
                    .height(4.dp)
                    .background(ActiveGreen)
            )
        }

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

private data class DiskSpace(
    val usedText: String,
    val availableText: String,
    val usedRatio: Float
)

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

private fun formatStorageSize(bytes: Long): String {
    val gb = bytes / 1024f / 1024f / 1024f
    if (gb >= 1f) {
        return String.format(Locale.US, "%.2f GB", gb)
    }

    val mb = bytes / 1024f / 1024f
    return String.format(Locale.US, "%.2f MB", mb)
}
