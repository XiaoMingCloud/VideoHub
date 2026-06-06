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
            FloatingBottomNav(
                activeItem = BottomNavItem.File,
                onMediaClick = onMediaClick,
                onFileClick = {},
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
            FileSourceTopBar(
                onTutorialClick = onTutorialClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            )

            EmptyFileSourceContent(
                onAddClick = onAddFileSourceClick,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

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
        Text(
            text = "文件源",
            color = PrimaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )

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

@Composable
private fun EmptyFileSourceContent(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "VideoHub",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "文件源为空",
            color = PrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "请添加文件源，享受您的私人影院。",
            color = TextGray,
            fontSize = 14.sp,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

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
