package com.liujiaming.videohub.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.components.AppListDivider
import com.liujiaming.videohub.ui.components.BottomNavItem
import com.liujiaming.videohub.ui.components.FloatingBottomNav
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.SettingsIconGreen

@Composable
fun SettingsScreen(
    onGeneralClick: () -> Unit,
    onPlaybackClick: () -> Unit,
    onPlayerInterfaceClick: () -> Unit,
    onSubtitleTrackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onMediaClick: () -> Unit,
    onFileClick: () -> Unit,
    onServerClick: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            FloatingBottomNav(
                activeItem = BottomNavItem.Settings,
                onMediaClick = onMediaClick,
                onFileClick = onFileClick,
                onServerClick = onServerClick,
                onSettingsClick = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "设置",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 16.dp)
            )

            SettingsCard {
                SettingsItem("通用", Icons.Default.Settings, onGeneralClick)
                AppListDivider()
                SettingsItem("资源", Icons.Default.Storage)
                AppListDivider()
                SettingsItem("下载", Icons.Default.Download)
                AppListDivider()
                SettingsItem("同步", Icons.Default.Sync)
            }

            SettingsCard {
                SettingsItem("播放", Icons.Default.PlayCircle, onPlaybackClick)
                AppListDivider()
                SettingsItem("播放器界面", Icons.Default.VideoLibrary, onPlayerInterfaceClick)
                AppListDivider()
                SettingsItem("字幕和音轨", Icons.Default.Subtitles, onSubtitleTrackClick)
            }

            SettingsCard {
                SettingsItem("使用教程", Icons.Default.Info)
                AppListDivider()
                SettingsItem("分享给朋友", Icons.Default.Share)
                AppListDivider()
                SettingsItem("检查更新", Icons.Default.Download)
                AppListDivider()
                SettingsItem("关于", Icons.Default.Info, onAboutClick)
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SettingsIconGreen,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            color = PrimaryText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            letterSpacing = 0.sp
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "进入$title",
            tint = Color(0xFFC7C7CC),
            modifier = Modifier.size(24.dp)
        )
    }
}
