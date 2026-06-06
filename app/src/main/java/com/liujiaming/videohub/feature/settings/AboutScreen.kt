package com.liujiaming.videohub.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.R
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

/**
 * 关于页面 - 展示应用基本信息
 *
 * 包含：Logo、应用名称、版本号、第三方许可链接、TMDB 声明及版权信息。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    Scaffold(
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部导航栏
            AboutTopBar(onBackClick)

            Spacer(modifier = Modifier.height(36.dp))

            // 应用 Logo 图片（带圆角阴影）
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "VideoHub",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .shadow(10.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 应用名称
            Text(
                text = "VideoHub",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 版本号
            Text(
                text = "1.3.2",
                color = TextGray,
                fontSize = 14.sp,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 第三方许可信息链接
            AboutLink("第三方许可信息")
            Spacer(modifier = Modifier.height(16.dp))
            // 开源项目信息链接
            AboutLink("开源项目信息")

            Spacer(modifier = Modifier.height(32.dp))

            // TMDB API 使用声明
            Text(
                text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                color = TextGray,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TMDB 品牌标识
            Text(
                text = "TMDB",
                color = Color(0xFF01B4E4), // TMDB 品牌蓝色
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )

            // 弹性空间，将版权信息推到底部
            Spacer(modifier = Modifier.weight(1f))

            // 版权信息
            Text(
                text = "© 2026 XiaoMingCloud 版权所有",
                color = TextGray,
                fontSize = 12.sp,
                letterSpacing = 0.sp,
                modifier = Modifier
                    .clickable { }
                    .padding(bottom = 32.dp)
            )
        }
    }
}

/**
 * 关于页面顶部导航栏
 *
 * 居中显示"关于"标题，左侧放置返回按钮。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
private fun AboutTopBar(onBackClick: () -> Unit) {
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
            text = "关于",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

/**
 * 关于页面的可点击链接文本组件
 *
 * 以绿色（ActiveGreen）显示可点击的文字链接。
 *
 * @param text 链接文本内容
 */
@Composable
private fun AboutLink(text: String) {
    Text(
        text = text,
        color = ActiveGreen,
        fontSize = 14.sp,
        letterSpacing = 0.sp,
        modifier = Modifier.clickable { }
    )
}
