package com.liujiaming.videohub.feature.media

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Folder
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
import com.liujiaming.videohub.feature.emby.EmbyImageCache
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
    MediaLibraryDetailScreen(
        initialRequest = MediaBrowseRequest(MediaSourceType.Emby, libraryId),
        onBackClick = onBackClick
    )
}

@Composable
fun MediaLibraryDetailScreen(
    initialRequest: MediaBrowseRequest,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    var browseStack by remember(initialRequest) {
        mutableStateOf(listOf(initialRequest))
    }
    val browseRequest = browseStack.last()
    val dataSource = remember(browseRequest.sourceType) {
        MediaBrowseDataSources.forType(browseRequest.sourceType)
    }
    var title by remember(browseRequest) { mutableStateOf("媒体内容") }
    var items by remember(browseRequest) { mutableStateOf<List<MediaBrowseItem>>(emptyList()) }
    var folderItems by remember(browseRequest) { mutableStateOf<List<MediaBrowseItem>>(emptyList()) }
    var totalRecordCount by remember(browseRequest) { mutableStateOf(0) }
    var folderTotalRecordCount by remember(browseRequest) { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(MediaDetailTab.Video) }
    var isInitialLoading by remember(browseRequest) { mutableStateOf(true) }
    var isLoadingMore by remember(browseRequest) { mutableStateOf(false) }
    var isLoadingFolders by remember(browseRequest) { mutableStateOf(false) }
    var reachedEnd by remember(browseRequest) { mutableStateOf(false) }
    var reachedFolderEnd by remember(browseRequest) { mutableStateOf(false) }
    var hasLoadedFolders by remember(browseRequest) { mutableStateOf(false) }
    var folderNextStartIndex by remember(browseRequest) { mutableStateOf(0) }
    var errorText by remember(browseRequest) { mutableStateOf<String?>(null) }
    var folderErrorText by remember(browseRequest) { mutableStateOf<String?>(null) }

    BackHandler(enabled = browseStack.size > 1) {
        browseStack = browseStack.dropLast(1)
    }

    suspend fun loadPage(startIndex: Int) {
        if (isLoadingMore || reachedEnd) {
            Log.d(
                TAG,
                "skip loadPage request=$browseRequest startIndex=$startIndex " +
                    "isLoadingMore=$isLoadingMore reachedEnd=$reachedEnd items=${items.size}"
            )
            return
        }

        isLoadingMore = true
        errorText = null
        Log.d(
            TAG,
            "loadPage start request=$browseRequest startIndex=$startIndex limit=$PAGE_SIZE cachedItems=${items.size}"
        )
        try {
            val result = runCatching {
                if (browseRequest.containerId.isBlank()) {
                    error("媒体容器 ID 为空，无法加载内容")
                }
                dataSource.fetchPage(
                    context = context,
                    request = browseRequest,
                    startIndex = startIndex,
                    limit = PAGE_SIZE
                )
            }

            result.onSuccess { page ->
                val mergedItems = mergeById(items, page.items)
                items = mergedItems
                totalRecordCount = page.totalRecordCount
                reachedEnd = isPageEndReached(
                    loadedItemsCount = mergedItems.size,
                    pageItemsCount = page.returnedItemCount,
                    totalRecordCount = page.totalRecordCount,
                    pageSize = PAGE_SIZE
                )
                Log.d(
                    TAG,
                    "loadPage success request=$browseRequest startIndex=$startIndex " +
                        "pageItems=${page.items.size} mergedItems=${mergedItems.size} " +
                        "returnedItems=${page.returnedItemCount} " +
                        "total=${page.totalRecordCount} reachedEnd=$reachedEnd"
                )

                dataSource.savePage(
                    context = context,
                    request = browseRequest,
                    items = mergedItems,
                    totalRecordCount = page.totalRecordCount
                )
            }.onFailure { error ->
                Log.w(TAG, "loadPage failed request=$browseRequest startIndex=$startIndex", error)
                errorText = error.message ?: "加载媒体库内容失败"
            }
        } finally {
            isLoadingMore = false
            isInitialLoading = false
        }
    }

    suspend fun loadFolderPage(startIndex: Int) {
        if (isLoadingFolders || reachedFolderEnd) {
            Log.d(
                TAG,
                "skip loadFolderPage request=$browseRequest startIndex=$startIndex " +
                    "isLoadingFolders=$isLoadingFolders reachedFolderEnd=$reachedFolderEnd folders=${folderItems.size}"
            )
            return
        }

        isLoadingFolders = true
        folderErrorText = null
        hasLoadedFolders = true
        Log.d(
            TAG,
            "loadFolderPage start request=$browseRequest startIndex=$startIndex limit=$PAGE_SIZE cachedFolders=${folderItems.size}"
        )
        var continueScanning = false
        try {
            val result = runCatching {
                if (browseRequest.containerId.isBlank()) {
                    error("媒体容器 ID 为空，无法加载文件夹")
                }
                dataSource.fetchFolderPage(
                    context = context,
                    request = browseRequest,
                    startIndex = startIndex,
                    limit = PAGE_SIZE
                )
            }

            result.onSuccess { page ->
                val mergedItems = mergeById(folderItems, page.items)
                val nextStartIndex = startIndex + page.returnedItemCount
                folderItems = mergedItems
                folderNextStartIndex = nextStartIndex
                folderTotalRecordCount = page.totalRecordCount
                reachedFolderEnd = isPageEndReached(
                    loadedItemsCount = nextStartIndex,
                    pageItemsCount = page.returnedItemCount,
                    totalRecordCount = page.totalRecordCount,
                    pageSize = PAGE_SIZE
                )
                Log.d(
                    TAG,
                    "loadFolderPage success request=$browseRequest startIndex=$startIndex " +
                        "folderItems=${page.items.size} mergedFolders=${mergedItems.size} " +
                        "returnedItems=${page.returnedItemCount} total=${page.totalRecordCount} " +
                        "nextStartIndex=$nextStartIndex reachedFolderEnd=$reachedFolderEnd"
                )
                continueScanning = mergedItems.isEmpty() && !reachedFolderEnd
            }.onFailure { error ->
                Log.w(TAG, "loadFolderPage failed request=$browseRequest startIndex=$startIndex", error)
                folderErrorText = error.message ?: "加载文件夹失败"
            }
        } finally {
            isLoadingFolders = false
        }
        if (continueScanning) {
            loadFolderPage(startIndex = folderNextStartIndex)
        }
    }

    LaunchedEffect(browseRequest, isInitialLoading) {
        if (isInitialLoading) {
            delay(INITIAL_LOADING_TIMEOUT_MS)
            if (isInitialLoading) {
                isInitialLoading = false
                isLoadingMore = false
                errorText = "加载超时：请检查服务器连接，或在设置-资源中刷新在线影视数据"
            }
        }
    }

    LaunchedEffect(browseRequest) {
        val seedResult = runCatching {
            dataSource.loadSeed(context, browseRequest)
        }
        seedResult.onSuccess { seed ->
            title = seed.title
            items = seed.items
            totalRecordCount = seed.totalRecordCount
            reachedEnd = seed.isFromDetailCache &&
                seed.items.size >= PAGE_SIZE &&
                seed.totalRecordCount > 0 &&
                seed.items.size >= seed.totalRecordCount
            isInitialLoading = seed.items.isEmpty()
            Log.d(
                TAG,
                "seed request=$browseRequest source=${if (seed.isFromDetailCache) "detailCache" else "homeCache"} " +
                    "seedItems=${seed.items.size} total=${seed.totalRecordCount} reachedEnd=$reachedEnd"
            )

            if (seed.items.isEmpty()) {
                loadPage(startIndex = 0)
            } else if (!reachedEnd && seed.items.size < PAGE_SIZE) {
                loadPage(startIndex = seed.items.size)
            }
        }.onFailure { error ->
            Log.w(TAG, "seed failed request=$browseRequest", error)
            errorText = error.message ?: "加载媒体内容失败"
            isInitialLoading = false
        }
    }

    val visibleItems = when (selectedTab) {
        MediaDetailTab.Video -> items.filterNot { it.isFolder }
        MediaDetailTab.Folder -> folderItems
    }
    val activeLoading = when (selectedTab) {
        MediaDetailTab.Video -> isInitialLoading
        MediaDetailTab.Folder -> isLoadingFolders && folderItems.isEmpty()
    }
    val activeLoadingMore = when (selectedTab) {
        MediaDetailTab.Video -> isLoadingMore
        MediaDetailTab.Folder -> isLoadingFolders
    }
    val activeReachedEnd = when (selectedTab) {
        MediaDetailTab.Video -> reachedEnd
        MediaDetailTab.Folder -> reachedFolderEnd
    }
    val activeErrorText = when (selectedTab) {
        MediaDetailTab.Video -> errorText
        MediaDetailTab.Folder -> folderErrorText
    }

    LaunchedEffect(browseRequest, selectedTab) {
        if (selectedTab == MediaDetailTab.Folder && !hasLoadedFolders) {
            loadFolderPage(startIndex = 0)
        }
    }

    LaunchedEffect(browseRequest, selectedTab) {
        snapshotFlow {
            val currentItems = if (selectedTab == MediaDetailTab.Folder) folderItems else items
            val visibleCount = when (selectedTab) {
                MediaDetailTab.Video -> currentItems.count { !it.isFolder }
                MediaDetailTab.Folder -> currentItems.size
            }
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val closeToEnd = lastVisibleIndex >= visibleCount - LOAD_MORE_THRESHOLD
            LoadMoreSnapshot(
                shouldLoadMore = closeToEnd && currentItems.isNotEmpty() && !activeLoadingMore && !activeReachedEnd,
                visibleCount = visibleCount,
                itemsCount = currentItems.size
            )
        }
            .distinctUntilChanged()
            .collect { snapshot ->
                Log.d(
                    TAG,
                    "loadMoreTrigger request=$browseRequest selectedTab=$selectedTab " +
                        "shouldLoadMore=${snapshot.shouldLoadMore} " +
                        "visible=${snapshot.visibleCount} items=${snapshot.itemsCount} " +
                        "isLoadingMore=$activeLoadingMore reachedEnd=$activeReachedEnd"
                )
                if (snapshot.shouldLoadMore) {
                    if (selectedTab == MediaDetailTab.Folder) {
                        loadFolderPage(startIndex = folderNextStartIndex)
                    } else {
                        loadPage(startIndex = items.size)
                    }
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
            onBackClick = {
                if (browseStack.size > 1) {
                    browseStack = browseStack.dropLast(1)
                } else {
                    onBackClick()
                }
            }
        )

        MediaDetailTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        when {
            activeLoading -> LoadingBox(text = "加载中...")
            visibleItems.isEmpty() && activeErrorText != null -> EmptyTextBox(activeErrorText.orEmpty())
            visibleItems.isEmpty() -> EmptyTextBox(
                text = when {
                    selectedTab == MediaDetailTab.Video -> "暂无视频"
                    browseRequest.folderContentMode -> "暂无内容"
                    else -> "暂无文件夹"
                }
            )
            else -> MediaDetailGrid(
                items = visibleItems,
                isLoadingMore = activeLoadingMore,
                reachedEnd = activeReachedEnd,
                errorText = activeErrorText,
                gridState = gridState,
                onFolderClick = { folder ->
                    browseStack = browseStack + MediaBrowseRequest(
                        sourceType = browseRequest.sourceType,
                        containerId = folder.id,
                        title = folder.name,
                        recursive = true,
                        folderContentMode = true
                    )
                }
            )
        }
    }
}

@Composable
private fun MediaDetailGrid(
    items: List<MediaBrowseItem>,
    isLoadingMore: Boolean,
    reachedEnd: Boolean,
    errorText: String?,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onFolderClick: (MediaBrowseItem) -> Unit
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
            MediaDetailGridItem(
                item = item,
                onFolderClick = onFolderClick
            )
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
private fun MediaDetailGridItem(
    item: MediaBrowseItem,
    onFolderClick: (MediaBrowseItem) -> Unit
) {
    Column(
        modifier = if (item.isFolder) {
            Modifier.clickable { onFolderClick(item) }
        } else {
            Modifier
        }
    ) {
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

            if (item.isFolder) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            if (!item.isFolder && item.played) {
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

            if (!item.isFolder && item.playbackProgress > 0f) {
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

private fun mergeById(
    currentItems: List<MediaBrowseItem>,
    newItems: List<MediaBrowseItem>
): List<MediaBrowseItem> {
    val merged = LinkedHashMap<String, MediaBrowseItem>()
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
