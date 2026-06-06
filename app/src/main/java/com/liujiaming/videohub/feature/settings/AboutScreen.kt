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
            AboutTopBar(onBackClick)

            Spacer(modifier = Modifier.height(36.dp))

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

            Text(
                text = "VideoHub",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1.3.2",
                color = TextGray,
                fontSize = 14.sp,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            AboutLink("第三方许可信息")
            Spacer(modifier = Modifier.height(16.dp))
            AboutLink("开源项目信息")

            Spacer(modifier = Modifier.height(32.dp))

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

            Text(
                text = "TMDB",
                color = Color(0xFF01B4E4),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.weight(1f))

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
