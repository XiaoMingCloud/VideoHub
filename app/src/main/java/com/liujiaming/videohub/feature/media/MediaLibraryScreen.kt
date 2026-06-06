package com.liujiaming.videohub.feature.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
fun MediaLibraryScreen(
    onAddFileSourceClick: () -> Unit,
    onAddServerClick: () -> Unit,
    onFileClick: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            FloatingBottomNav(
                activeItem = BottomNavItem.Media,
                onMediaClick = {},
                onFileClick = onFileClick,
                onServerClick = onServerClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PageBackground)
        ) {
            PosterBackdrop()
            TopActions(modifier = Modifier.align(Alignment.TopCenter))
            EmptyMediaLibraryContent(
                onAddFileSourceClick = onAddFileSourceClick,
                onAddServerClick = onAddServerClick,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun PosterBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tileWidth = size.width / 3.4f
            val tileHeight = tileWidth * 1.45f
            val colors = listOf(
                Color(0xFF2D6CDF),
                Color(0xFFE4A33A),
                Color(0xFF9C4E97),
                Color(0xFF3A8B7A),
                Color(0xFFB94B4B),
                Color(0xFF455A64)
            )

            for (row in -1..4) {
                for (col in -1..3) {
                    val index = (row * 4 + col + colors.size * 4) % colors.size
                    rotate(
                        degrees = -10f,
                        pivot = Offset(size.width / 2f, size.height / 2f)
                    ) {
                        drawRoundRect(
                            color = colors[index].copy(alpha = 0.16f),
                            topLeft = Offset(
                                x = col * tileWidth * 1.08f,
                                y = row * tileHeight * 0.82f
                            ),
                            size = Size(tileWidth, tileHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx())
                        )
                    }
                }
            }
        }

        val scrim = PageBackground
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            scrim.copy(alpha = 0.74f),
                            scrim.copy(alpha = 0.94f),
                            scrim
                        )
                    )
                )
        )
    }
}

@Composable
private fun TopActions(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "VidHub",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(42.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = PrimaryText
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
private fun EmptyMediaLibraryContent(
    onAddFileSourceClick: () -> Unit,
    onAddServerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(bottom = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "VidHub",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = buildAnnotatedString {
                append("欢迎来到 ")
                withStyle(SpanStyle(color = ActiveGreen)) {
                    append("VidHub")
                }
            },
            color = PrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "媒体库为空，请添加文件源或影视服务器，享受您的私人影院。",
            color = TextGray,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.fillMaxWidth(0.82f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        MediaActionButton(
            text = "添加文件源",
            icon = Icons.Default.Folder,
            onClick = onAddFileSourceClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        MediaActionButton(
            text = "添加影视服务器",
            icon = Icons.Default.VideoLibrary,
            onClick = onAddServerClick
        )
    }
}

@Composable
private fun MediaActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.84f)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ActiveGreen,
            contentColor = Color.White
        ),
        elevation = null
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}
