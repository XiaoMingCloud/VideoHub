package com.liujiaming.videohub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.TextGray

enum class BottomNavItem {
    Media,
    File,
    Server,
    Settings
}

@Composable
fun FloatingBottomNav(
    activeItem: BottomNavItem,
    onMediaClick: () -> Unit,
    onFileClick: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(CircleShape)
                .background(CardBackground)
                .padding(horizontal = 8.dp, vertical = 8.9.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("媒体库", Icons.Default.Home, activeItem == BottomNavItem.Media, onMediaClick)
            NavItem("文件源", Icons.Default.Folder, activeItem == BottomNavItem.File, onFileClick)
            NavItem("影视服务器", Icons.Default.VideoLibrary, activeItem == BottomNavItem.Server, onServerClick)
            NavItem("设置", Icons.Default.Settings, activeItem == BottomNavItem.Settings, onSettingsClick)
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit = {}
) {
    val color = if (isActive) ActiveGreen else TextGray

    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.6.dp))
        Text(
            text = title,
            color = color,
            fontSize = 10.sp,
            letterSpacing = 0.sp,
            maxLines = 1
        )
    }
}
