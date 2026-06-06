package com.liujiaming.videohub.feature.server

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.components.AppListDivider
import com.liujiaming.videohub.ui.components.BottomNavItem
import com.liujiaming.videohub.ui.components.FloatingBottomNav
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import com.liujiaming.videohub.ui.theme.VideoHubTheme

@Composable
fun MediaServerScreen(
    onEmbyClick: () -> Unit = {},
    onJellyfinClick: () -> Unit = {},
    onFnosClick: () -> Unit = {},
    onMediaClick: () -> Unit = {},
    onFileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            FloatingBottomNav(
                activeItem = BottomNavItem.Server,
                onMediaClick = onMediaClick,
                onFileClick = onFileClick,
                onServerClick = {},
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            TopHeader()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "连接到...",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    ServerListItem("Emby", Icons.Default.PlayCircle, Color(0xFF43A047), onEmbyClick)
                    AppListDivider()
                    ServerListItem("Jellyfin", Icons.Default.Storage, Color(0xFF5E56D8), onJellyfinClick)
                    AppListDivider()
                    ServerListItem("Plex", Icons.Default.VideoLibrary, Color(0xFFFFB300))
                    AppListDivider()
                    ServerListItem("飞牛私有云", Icons.Default.Cloud, Color(0xFF1E88E5), onFnosClick)
                }
            }
        }
    }
}

@Composable
private fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "影视服务器",
            color = PrimaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = PrimaryText
            )
        }
    }
}

@Composable
private fun ServerListItem(
    title: String,
    icon: ImageVector,
    iconColor: Color,
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
            tint = iconColor,
            modifier = Modifier.size(28.dp)
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

@Preview(showBackground = true)
@Composable
private fun MediaServerScreenPreview() {
    VideoHubTheme {
        MediaServerScreen()
    }
}
