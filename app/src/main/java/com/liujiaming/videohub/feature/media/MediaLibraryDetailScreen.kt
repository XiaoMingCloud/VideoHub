package com.liujiaming.videohub.feature.media

import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.snapshotFlow
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
import com.liujiaming.videohub.feature.emby.EmbyAuthSession
import com.liujiaming.videohub.feature.emby.EmbyHomeCache
import com.liujiaming.videohub.feature.emby.EmbyHomeClient
import com.liujiaming.videohub.feature.emby.EmbyImageCache
import com.liujiaming.videohub.feature.emby.EmbyLibraryItemsCache
import com.liujiaming.videohub.feature.emby.EmbyMediaItem
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.DividerGray
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 20
private const val LOAD_MORE_THRESHOLD = 6
private const val INITIAL_LOADING_TIMEOUT_MS = 30000L
private const val TAG = "MediaLibraryDetail"

@Composable
fun MediaLibraryDetailScreen(
    libraryId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    var session by remember(libraryId) { mutableStateOf<EmbyAuthSession?>(null) }
    var title by remember(libraryId) { mutableStateOf("媒体内容") }
    var items by remember(libraryId) { mutableStateOf<List<EmbyMediaItem>>(emptyList()) }
    var totalRecordCount by remember(libraryId) { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(MediaDetailTab.Video) }
    var isInitialLoading by remember(libraryId) { mutableStateOf(true) }
    var isLoadingMore by remember(libraryId) { mutableStateOf(false) }
    var reachedEnd by remember(libraryId) { mutableStateOf(false) }
    var errorText by remember(libraryId) { mutableStateOf<String?>(null) }

    suspend fun loadPage(currentSession: EmbyAuthSession, startIndex: Int) {
        if (isLoadingMore || reachedEnd) {
            Log.d(
                TAG,
                "skip loadPage libraryId=$libraryId startIndex=$startIndex " +
                    "isLoadingMore=$isLoadingMore reachedEnd=$reachedEnd items=${items.size}"
            )
            return
        }

        isLoadingMore = true
        errorText = null
        Log.d(
            TAG,
            "loadPage start libraryId=$libraryId startIndex=$startIndex limit=$PAGE_SIZE cachedItems=${items.size}"
        )
        try {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (libraryId.isBlank()) {
                        error("媒体库 ID 为空，无法加载内容")
                    }
                    EmbyHomeClient.fetchLibraryItemsPage(
                        session = currentSession,
                        libraryId = libraryId,
                        startIndex = startIndex,
                        limit = PAGE_SIZE
                    )
                }
            }

            result.onSuccess { page ->
                val mergedItems = mergeById(items, page.items)
                items = mergedItems
                totalRecordCount = page.totalRecordCount
                reachedEnd = isPageEndReached(
                    loadedItemsCount = mergedItems.size,
                    pageItemsCount = page.items.size,
                    totalRecordCount = page.totalRecordCount,
                    pageSize = PAGE_SIZE
                )
                Log.d(
                    TAG,
                    "loadPage success libraryId=$libraryId startIndex=$startIndex " +
                        "pageItems=${page.items.size} mergedItems=${mergedItems.size} " +
                        "total=${page.totalRecordCount} reachedEnd=$reachedEnd"
                )

                withContext(Dispatchers.IO) {
                    EmbyLibraryItemsCache.save(
                        context = context,
                        userId = currentSession.userId,
                        libraryId = libraryId,
                        items = mergedItems,
                        totalRecordCount = page.totalRecordCount
                    )
                }
            }.onFailure { error ->
                Log.w(TAG, "loadPage failed libraryId=$libraryId startIndex=$startIndex", error)
                errorText = error.message ?: "加载媒体库内容失败"
            }
        } finally {
            isLoadingMore = false
            isInitialLoading = false
        }
    }

    LaunchedEffect(libraryId, isInitialLoading) {
        if (isInitialLoading) {
            delay(INITIAL_LOADING_TIMEOUT_MS)
            if (isInitialLoading) {
                isInitialLoading = false
                isLoadingMore = false
                errorText = "加载超时：请检查服务器连接，或在设置-资源中刷新在线影视数据"
            }
        }
    }

    LaunchedEffect(libraryId) {
        val loadedSession = EmbySessionStore.load(context)
        if (loadedSession == null) {
            errorText = "请先登录媒体服务器"
            isInitialLoading = false
            return@LaunchedEffect
        }
        session = loadedSession

        val cachedHome = withContext(Dispatchers.IO) {
            EmbyHomeCache.load(context, loadedSession.userId)?.home
        }
        val librarySummary = cachedHome?.libraries?.firstOrNull { it.id == libraryId }
        val homeSection = cachedHome?.librarySections?.firstOrNull { it.libraryId == libraryId }
        title = librarySummary?.name ?: homeSection?.title ?: "媒体内容"

        val cachedPage = withContext(Dispatchers.IO) {
            EmbyLibraryItemsCache.load(context, loadedSession.userId, libraryId)
        }
        val hasCachedPage = cachedPage?.items?.isNotEmpty() == true
        val seedItems = cachedPage?.items?.takeIf { it.isNotEmpty() } ?: homeSection?.items.orEmpty()
        items = seedItems
        totalRecordCount = cachedPage?.totalRecordCount?.takeIf { it > 0 }
            ?: librarySummary?.itemCount
            ?: seedItems.size
        reachedEnd = hasCachedPage &&
            seedItems.size >= PAGE_SIZE &&
            totalRecordCount > 0 &&
            seedItems.size >= totalRecordCount
        isInitialLoading = seedItems.isEmpty()
        Log.d(
            TAG,
            "seed libraryId=$libraryId source=${if (hasCachedPage) "detailCache" else "homeCache"} " +
                "seedItems=${seedItems.size} total=$totalRecordCount reachedEnd=$reachedEnd " +
                "hasCachedPage=$hasCachedPage"
        )

        if (seedItems.isEmpty()) {
            loadPage(currentSession = loadedSession, startIndex = 0)
        } else if (!reachedEnd && seedItems.size < PAGE_SIZE) {
            loadPage(currentSession = loadedSession, startIndex = seedItems.size)
        }
    }

    val visibleItems = when (selectedTab) {
        MediaDetailTab.Video -> items.filterNot { it.isFolderItem() }
        MediaDetailTab.Folder -> items.filter { it.isFolderItem() }
    }

    LaunchedEffect(libraryId, selectedTab, session) {
        snapshotFlow {
            val currentItems = items
            val visibleCount = when (selectedTab) {
                MediaDetailTab.Video -> currentItems.count { !it.isFolderItem() }
                MediaDetailTab.Folder -> currentItems.count { it.isFolderItem() }
            }
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val closeToEnd = lastVisibleIndex >= visibleCount - LOAD_MORE_THRESHOLD
            LoadMoreSnapshot(
                shouldLoadMore = closeToEnd && currentItems.isNotEmpty() && !isLoadingMore && !reachedEnd,
                visibleCount = visibleCount,
                itemsCount = currentItems.size
            )
        }
            .distinctUntilChanged()
            .collect { snapshot ->
                val currentSession = session
                Log.d(
                    TAG,
                    "loadMoreTrigger libraryId=$libraryId selectedTab=$selectedTab " +
                        "shouldLoadMore=${snapshot.shouldLoadMore} " +
                        "visible=${snapshot.visibleCount} items=${snapshot.itemsCount} " +
                        "isLoadingMore=$isLoadingMore reachedEnd=$reachedEnd"
                )
                if (snapshot.shouldLoadMore && currentSession != null) {
                    loadPage(currentSession = currentSession, startIndex = items.size)
                }
            }
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

        when {
            isInitialLoading -> LoadingBox(text = "加载中...")
            visibleItems.isEmpty() && errorText != null -> EmptyTextBox(errorText.orEmpty())
            visibleItems.isEmpty() -> EmptyTextBox(
                text = if (selectedTab == MediaDetailTab.Video) "暂无视频" else "暂无文件夹"
            )
            else -> MediaDetailGrid(
                items = visibleItems,
                isLoadingMore = isLoadingMore,
                reachedEnd = reachedEnd,
                errorText = errorText,
                gridState = gridState
            )
        }
    }
}

@Composable
private fun MediaDetailGrid(
    items: List<EmbyMediaItem>,
    isLoadingMore: Boolean,
    reachedEnd: Boolean,
    errorText: String?,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
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
        items(items, key = { it.id.ifBlank { it.name } }) { item ->
            MediaDetailGridItem(item)
        }

        if (isLoadingMore || errorText != null || reachedEnd) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FooterStateRow(
                    isLoading = isLoadingMore,
                    errorText = errorText,
                    reachedEnd = reachedEnd
                )
            }
        }
    }
}

@Composable
private fun FooterStateRow(
    isLoading: Boolean,
    errorText: String?,
    reachedEnd: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = ActiveGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("加载中", color = TextGray, fontSize = 13.sp, letterSpacing = 0.sp)
            }
            errorText != null -> Text(errorText, color = TextGray, fontSize = 13.sp, letterSpacing = 0.sp)
            reachedEnd -> Text("已加载全部", color = TextGray, fontSize = 13.sp, letterSpacing = 0.sp)
        }
    }
}

@Composable
private fun LoadingBox(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = ActiveGreen
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, color = TextGray, fontSize = 14.sp, letterSpacing = 0.sp)
        }
    }
}

@Composable
private fun EmptyTextBox(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextGray,
            fontSize = 14.sp,
            letterSpacing = 0.sp
        )
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

private data class LoadMoreSnapshot(
    val shouldLoadMore: Boolean,
    val visibleCount: Int,
    val itemsCount: Int
)

private fun EmbyMediaItem.isFolderItem(): Boolean {
    return type.equals("Folder", ignoreCase = true) ||
        type.equals("CollectionFolder", ignoreCase = true)
}

private fun mergeById(
    currentItems: List<EmbyMediaItem>,
    newItems: List<EmbyMediaItem>
): List<EmbyMediaItem> {
    val merged = LinkedHashMap<String, EmbyMediaItem>()
    (currentItems + newItems).forEachIndexed { index, item ->
        val key = item.id.ifBlank { "${item.name}_$index" }
        merged[key] = item
    }
    return merged.values.toList()
}

private fun isPageEndReached(
    loadedItemsCount: Int,
    pageItemsCount: Int,
    totalRecordCount: Int,
    pageSize: Int
): Boolean {
    if (pageItemsCount == 0) return true
    if (pageItemsCount < pageSize) return true
    return totalRecordCount > pageSize && loadedItemsCount >= totalRecordCount
}
