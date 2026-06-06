package com.liujiaming.videohub.feature.filesource

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.R
import com.liujiaming.videohub.ui.components.BottomNavItem
import com.liujiaming.videohub.ui.components.FloatingBottomNav
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

/**
 * 文件源主页面。
 * 当前展示空状态界面，提示用户添加文件源。
 * 包含顶部标题栏（带使用教程入口）和底部悬浮导航栏。
 *
 * @param onAddFileSourceClick 点击"添加文件源"按钮的回调
 * @param onTutorialClick 点击"使用教程"的回调
 * @param onMediaClick 底部导航"媒体库"点击回调
 * @param onServerClick 底部导航"影视服务器"点击回调
 * @param onSettingsClick 底部导航"设置"点击回调
 */
@Composable
fun FileSourceScreen(
    onAddFileSourceClick: () -> Unit,
    onTutorialClick: () -> Unit,
    onMediaClick: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            // 底部悬浮导航栏，当前高亮"文件源"项
            FloatingBottomNav(
                activeItem = BottomNavItem.File,
                onMediaClick = onMediaClick,
                onFileClick = {},       // 当前已在文件源页面，无需跳转
                onServerClick = onServerClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 顶部标题栏，包含"使用教程"和"更多"按钮
            FileSourceTopBar(
                onTutorialClick = onTutorialClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            )

            // 空状态内容：Logo + 提示文字 + 添加按钮
            EmptyFileSourceContent(
                onAddClick = onAddFileSourceClick,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * 文件源页面顶部栏。
 * 左侧显示"文件源"大标题，右侧显示"使用教程"文本按钮和"更多"图标按钮。
 *
 * @param onTutorialClick 点击"使用教程"的回调
 * @param modifier 布局修饰符
 */
@Composable
private fun FileSourceTopBar(
    onTutorialClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 页面大标题
        Text(
            text = "文件源",
            color = PrimaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )

        // 右侧操作区：使用教程 + 更多菜单
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onTutorialClick) {
                Text(
                    text = "使用教程",
                    color = PrimaryText,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "更多",
                    tint = PrimaryText
                )
            }
        }
    }
}

/**
 * 文件源为空时的占位内容。
 * 居中展示应用 Logo、提示文字和"添加文件源"按钮。
 *
 * @param onAddClick 点击添加按钮的回调
 * @param modifier 布局修饰符
 */
@Composable
private fun EmptyFileSourceContent(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 72.dp),    // 预留底部导航栏空间
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 应用 Logo 图片，圆角 24dp 裁剪
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "VideoHub",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 主提示文字
        Text(
            text = "文件源为空",
            color = PrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 副提示文字
        Text(
            text = "请添加文件源，享受您的私人影院。",
            color = TextGray,
            fontSize = 14.sp,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 添加文件源按钮，绿色背景，圆角胶囊形状
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(46.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ActiveGreen,
                contentColor = Color.White
            ),
            elevation = null
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "添加文件源",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}
