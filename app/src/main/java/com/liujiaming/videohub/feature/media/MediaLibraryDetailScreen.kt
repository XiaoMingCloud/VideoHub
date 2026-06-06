package com.liujiaming.videohub.feature.media

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.feature.emby.EmbyHomeCache
import com.liujiaming.videohub.feature.emby.EmbyImageCache
import com.liujiaming.videohub.feature.emby.EmbyLibrarySection
import com.liujiaming.videohub.feature.emby.EmbyMediaItem
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.DividerGray
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MediaLibraryDetailScreen(
    libraryId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var section by remember(libraryId) { mutableStateOf<EmbyLibrarySection?>(null) }
    var errorText by remember(libraryId) { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(MediaDetailTab.Video) }

    LaunchedEffect(libraryId) {
        val session = EmbySessionStore.load(context)
        if (session == null) {
            errorText = "请先登录媒体服务器"
            return@LaunchedEffect
        }

        val cachedHome = withContext(Dispatchers.IO) {
            EmbyHomeCache.load(context, session.userId)?.home
        }
        val targetSection = cachedHome?.librarySections?.firstOrNull { it.libraryId == libraryId }
        section = targetSection
        errorText = if (targetSection == null) {
            "没有找到该媒体库缓存，请在设置-资源中刷新在线影视数据"
        } else {
            null
        }
    }

    val currentSection = section
    val title = currentSection?.title ?: "媒体内容"
    val allItems = currentSection?.items.orEmpty()
    val visibleItems = when (selectedTab) {
        MediaDetailTab.Video -> allItems.filterNot { it.isFolderItem() }
        MediaDetailTab.Folder -> allItems.filter { it.isFolderItem() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
    ) {
        MediaDetailTopBar(
            title = title,
            onBackClick = onBackClick
        )

        MediaDetailTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        if (errorText != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorText.orEmpty(),
                    color = TextGray,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp
                )
            }
        } else if (visibleItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == MediaDetailTab.Video) "暂无视频" else "暂无文件夹",
                    color = TextGray,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 10.dp,
                    top = 14.dp,
                    end = 10.dp,
                    bottom = 20.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(visibleItems, key = { it.id.ifBlank { it.name } }) { item ->
                    MediaDetailGridItem(item)
                }
            }
        }
    }
}

@Composable
private fun MediaDetailTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = PrimaryText
            )
        }

        Text(
            text = title,
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            letterSpacing = 0.sp
        )

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "筛选",
                tint = PrimaryText
            )
        }
    }
}

@Composable
private fun MediaDetailTabs(
    selectedTab: MediaDetailTab,
    onTabSelected: (MediaDetailTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MediaDetailTabButton(
                text = "视频",
                selected = selectedTab == MediaDetailTab.Video,
                onClick = { onTabSelected(MediaDetailTab.Video) }
            )
            MediaDetailTabButton(
                text = "文件夹",
                selected = selectedTab == MediaDetailTab.Folder,
                onClick = { onTabSelected(MediaDetailTab.Folder) }
            )
        }
    }
}

@Composable
private fun MediaDetailTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(92.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(if (selected) ActiveGreen else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextGray,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun MediaDetailGridItem(item: EmbyMediaItem) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(8.dp))
                .background(BackgroundGray)
        ) {
            CachedMediaDetailImage(
                imageUrl = item.imageUrl,
                modifier = Modifier.fillMaxSize()
            )

            if (item.played) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.Black.copy(alpha = 0.48f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "已观看",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            if (item.playbackProgress > 0f) {
                LinearProgressIndicator(
                    progress = item.playbackProgress,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.24f)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = item.name,
            color = PrimaryText,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun CachedMediaDetailImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching { EmbyImageCache.loadBitmap(context, imageUrl) }.getOrNull()
        }
    }

    val currentBitmap = bitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(CardBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = DividerGray,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

private enum class MediaDetailTab {
    Video,
    Folder
}

private fun EmbyMediaItem.isFolderItem(): Boolean {
    return type.equals("Folder", ignoreCase = true) ||
        type.equals("CollectionFolder", ignoreCase = true)
}
