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
            TutorialTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "更新时间 Dec 11, 2024",
                    color = TextGray,
                    fontSize = 12.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "文件源页面主要用于管理本地、网盘、网络共享上的影片文件。下文中所有的长按操作均为移动端版本，对应桌面端版本为鼠标右击操作。",
                    color = PrimaryText,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                TutorialCard(title = "单文件操作") {
                    TutorialParagraph(
                        title = "重新扫描文件",
                        body = "长按已添加的文件源或文件夹，选择重新扫描，自动同步变动。也可进入文件夹后通过右上角菜单重新扫描。"
                    )
                    TutorialParagraph(
                        title = "重命名",
                        body = "VidHub 内的改名操作将同步至原始存储，媒体库将基于新文件名自动重新刮削。"
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
        Column(modifier = Modifier.padding(20.dp)) {
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
