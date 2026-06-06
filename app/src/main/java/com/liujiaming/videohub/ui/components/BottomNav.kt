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

/**
 * 底部导航栏项目枚举，定义四个主要功能模块。
 */
enum class BottomNavItem {
    /** 媒体库模块 */
    Media,
    /** 文件源模块 */
    File,
    /** 影视服务器模块 */
    Server,
    /** 设置模块 */
    Settings
}

/**
 * 悬浮式底部导航栏组件。
 * 采用圆形胶囊形状设计，带有阴影效果，悬浮在页面底部。
 * 包含四个导航项：媒体库、文件源、影视服务器、设置。
 * 当前激活的导航项以绿色高亮显示。
 *
 * @param activeItem 当前选中的导航项
 * @param onMediaClick 点击“媒体库”时的回调
 * @param onFileClick 点击“文件源”时的回调
 * @param onServerClick 点击“影视服务器”时的回调
 * @param onSettingsClick 点击“设置”时的回调
 */
@Composable
fun FloatingBottomNav(
    activeItem: BottomNavItem,
    onMediaClick: () -> Unit,
    onFileClick: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // 外层容器，填充最大宽度并适配导航栏高度，居中对齐导航栏内容
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()  // 适配系统导航栏高度，避免遮挡
            .padding(horizontal = 16.dp, vertical = 8.9.dp),
        contentAlignment = Alignment.Center
    ) {
        // 导航栏主体行，应用圆形裁剪、阴影和背景色
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,             // 阴影高度，营造悬浮效果
                    shape = CircleShape,            // 圆形胶囊形状
                    ambientColor = Color.Black.copy(alpha = 0.08f),  // 环境光阴影颜色
                    spotColor = Color.Black.copy(alpha = 0.12f)      // 聚光阴影颜色
                )
                .clip(CircleShape)                  // 圆形裁剪
                .background(CardBackground)         // 使用主题感知的卡片背景色
                .padding(horizontal = 8.dp, vertical = 8.9.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,  // 导航项均匀分布
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 渲染四个导航项，根据 activeItem 判断当前选中状态
            NavItem("媒体库", Icons.Default.Home, activeItem == BottomNavItem.Media, onMediaClick)
            NavItem("文件源", Icons.Default.Folder, activeItem == BottomNavItem.File, onFileClick)
            NavItem("影视服务器", Icons.Default.VideoLibrary, activeItem == BottomNavItem.Server, onServerClick)
            NavItem("设置", Icons.Default.Settings, activeItem == BottomNavItem.Settings, onSettingsClick)
        }
    }
}

/**
 * 单个导航项组件，由图标和文字纵向排列组成。
 * 选中状态以 [ActiveGreen] 绿色显示，未选中状态以 [TextGray] 灰色显示。
 *
 * @param title 导航项文字标签
 * @param icon 导航项图标
 * @param isActive 是否为当前选中状态
 * @param onClick 点击时的回调函数
 */
@Composable
private fun NavItem(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit = {}
) {
    // 根据选中状态决定颜色：选中为绿色，未选中为灰色
    val color = if (isActive) ActiveGreen else TextGray

    // 导航项容器：固定宽度 72dp，圆角裁剪，点击响应
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(18.dp))  // 圆角裁剪，增强点击反馈
            .clickable(onClick = onClick)        // 点击事件
            .padding(vertical = 3.6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 导航项图标
        Icon(
            imageVector = icon,
            contentDescription = title,           // 无障碍描述文字
            tint = color,                          // 图标着色
            modifier = Modifier.size(24.dp)        // 图标尺寸 24dp
        )
        Spacer(modifier = Modifier.height(3.6.dp))  // 图标与文字间距
        // 导航项文字标签
        Text(
            text = title,
            color = color,                          // 文字颜色与图标一致
            fontSize = 10.sp,                       // 小字号
            letterSpacing = 0.sp,                   // 无字间距
            maxLines = 1                            // 单行显示
        )
    }
}
