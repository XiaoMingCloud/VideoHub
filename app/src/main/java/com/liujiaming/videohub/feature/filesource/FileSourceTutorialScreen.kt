package com.liujiaming.videohub.feature.filesource

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

/**
 * 文件源使用教程页面。
 * 展示文件源功能的详细说明，包括单文件操作和多文件操作的教程内容。
 * 采用卡片式布局，支持垂直滚动。
 *
 * @param onBackClick 返回上一页的回调
 */
@Composable
fun FileSourceTutorialScreen(onBackClick: () -> Unit) {
    Scaffold(
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // 顶部导航栏，含返回按钮和标题
            TutorialTopBar(onBackClick)

            // 教程正文内容，支持垂直滚动
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // 更新时间标签
                Text(
                    text = "更新时间 Dec 11, 2024",
                    color = TextGray,
                    fontSize = 12.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 教程概述说明
                Text(
                    text = "文件源页面主要用于管理本地、网盘、网络共享上的影片文件。下文中所有的长按操作均为移动端版本，对应桌面端版本为鼠标右击操作。",
                    color = PrimaryText,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 单文件操作教程卡片
                TutorialCard(title = "单文件操作") {
                    TutorialParagraph(
                        title = "重新扫描文件",
                        body = "长按已添加的文件源或文件夹，选择重新扫描，自动同步变动。也可进入文件夹后通过右上角菜单重新扫描。"
                    )
                    TutorialParagraph(
                        title = "重命名",
                        body = "VideoHub 内的改名操作将同步至原始存储，媒体库将基于新文件名自动重新刮削。"
                    )
                    TutorialParagraph(
                        title = "下载与删除",
                        body = "长按选择下载，可在设置中管理。删除操作将直接移除存储中的原始文件及媒体库信息。"
                    )
                    TutorialParagraph(
                        title = "回收站",
                        body = "如果文件源支持回收站，建议优先在对应存储服务中确认删除策略，避免误删重要文件。"
                    )
                }

                // 多文件操作教程卡片
                TutorialCard(title = "多文件操作") {
                    TutorialBody("1. 进入文件源页面选择文件夹。")
                    TutorialBody("2. 点击右上角三个点，选择编辑。")
                    TutorialBody("3. 在编辑模式下勾选多个文件，可批量处理或重新扫描目录。")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 教程页面的顶部导航栏。
 * 左侧显示返回按钮，居中显示"使用文件源"标题。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
private fun TutorialTopBar(onBackClick: () -> Unit) {
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
            text = "使用文件源",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

/**
 * 教程卡片容器，带圆角和背景色。
 * 用于将相关教程内容分组展示。
 *
 * @param title 卡片标题
 * @param content 卡片内部的 Composable 内容
 */
@Composable
private fun TutorialCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(17.75.dp)) {
            // 卡片标题
            Text(
                text = title,
                color = PrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            content()
        }
    }
}

/**
 * 教程段落组件，由加粗标题和正文内容组成。
 *
 * @param title 段落标题（加粗显示）
 * @param body 段落正文内容
 */
@Composable
private fun TutorialParagraph(
    title: String,
    body: String
) {
    Text(
        text = title,
        color = PrimaryText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        modifier = Modifier.padding(top = 12.dp)
    )
    TutorialBody(body)
}

/**
 * 教程正文文本组件，用于展示单行或多行说明文字。
 *
 * @param text 正文文本内容
 */
@Composable
private fun TutorialBody(text: String) {
    Text(
        text = text,
        color = PrimaryText,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
}
