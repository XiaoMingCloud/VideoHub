package com.liujiaming.videohub.feature.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.R
import com.liujiaming.videohub.feature.bilibili.BilibiliClient
import com.liujiaming.videohub.feature.bilibili.BilibiliHomeCache
import com.liujiaming.videohub.feature.bilibili.BilibiliSessionStore
import com.liujiaming.videohub.feature.emby.EmbyHomeCache
import com.liujiaming.videohub.feature.emby.EmbyHomeDebugState
import com.liujiaming.videohub.feature.emby.EmbyHomeDebugStore
import com.liujiaming.videohub.feature.emby.EmbyImageCache
import com.liujiaming.videohub.feature.emby.EmbyLibrarySection
import com.liujiaming.videohub.feature.emby.EmbyLibrarySummary
import com.liujiaming.videohub.feature.emby.EmbyMediaHome
import com.liujiaming.videohub.feature.emby.EmbyMediaItem
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.ui.components.BottomNavItem
import com.liujiaming.videohub.ui.components.FloatingBottomNav
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val SHOW_EMBY_DEBUG_LOG = false

@Composable
fun MediaLibraryScreen(
    onAddFileSourceClick: () -> Unit,
    onAddServerClick: () -> Unit,
    onAddBilibiliClick: () -> Unit = {},
    onFileClick: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLibraryViewAllClick: (MediaBrowseRequest) -> Unit
) {
    val context = LocalContext.current
    val embySession = remember { EmbySessionStore.load(context) }
    val bilibiliSession = remember { BilibiliSessionStore.load(context) }
    val availableSources = remember(embySession?.accessToken, bilibiliSession?.mid) {
        buildList {
            if (embySession != null) {
                add(MediaSourceOption(MediaSourceType.Emby, embySession.serverName.ifBlank { "Emby" }))
            }
            if (bilibiliSession != null) {
                add(MediaSourceOption(MediaSourceType.Bilibili, "Bilibili · ${bilibiliSession.username}"))
            }
        }
    }
    var selectedSourceType by remember { mutableStateOf(MediaSourceSelectionStore.load(context)) }
    if (availableSources.isNotEmpty() && availableSources.none { it.type == selectedSourceType }) {
        selectedSourceType = availableSources.first().type
    }
    var mediaHome by remember(selectedSourceType, embySession?.accessToken, bilibiliSession?.mid) {
        mutableStateOf<EmbyMediaHome?>(null)
    }
    var loadError by remember(selectedSourceType, embySession?.accessToken, bilibiliSession?.mid) {
        mutableStateOf<String?>(null)
    }
    var debugState by remember(selectedSourceType, embySession?.accessToken) {
        mutableStateOf<EmbyHomeDebugState?>(null)
    }
    var refreshNonce by remember { mutableStateOf(0) }
    var refreshSourceType by remember { mutableStateOf<MediaSourceType?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSourceType, embySession?.accessToken, bilibiliSession?.mid, refreshNonce) {
        val forceRefresh = refreshSourceType == selectedSourceType
        if (forceRefresh) {
            isRefreshing = true
        } else {
            mediaHome = null
        }
        loadError = null
        debugState = null

        when (selectedSourceType) {
            MediaSourceType.Emby -> {
                if (embySession == null) return@LaunchedEffect
                val cachedHome = withContext(Dispatchers.IO) {
                    EmbyHomeCache.load(context, embySession.userId)
                }
                debugState = withContext(Dispatchers.IO) {
                    EmbyHomeDebugStore.load(context)
                }

                if (cachedHome?.home != null) {
                    mediaHome = cachedHome.home.sanitized()
                } else {
                    loadError = "已连接 Emby，请在设置-资源中刷新在线影视数据"
                }
            }
            MediaSourceType.Bilibili -> {
                if (bilibiliSession == null) return@LaunchedEffect
                val cachedHome = withContext(Dispatchers.IO) {
                    BilibiliHomeCache.load(context, bilibiliSession.mid)
                }
                if (!forceRefresh && cachedHome != null && BilibiliHomeCache.isFresh(cachedHome)) {
                    mediaHome = cachedHome.home.sanitized()
                    isRefreshing = false
                    return@LaunchedEffect
                }
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        BilibiliClient.fetchHome(bilibiliSession).also { home ->
                            BilibiliHomeCache.save(context, bilibiliSession.mid, home)
                        }
                    }
                }
                result.onSuccess {
                    mediaHome = it.sanitized()
                }.onFailure {
                    if (cachedHome?.home != null) {
                        mediaHome = cachedHome.home.sanitized()
                    }
                    loadError = it.message ?: "加载 Bilibili 收藏夹失败"
                }
            }
            else -> Unit
        }
        isRefreshing = false
        if (forceRefresh) {
            refreshSourceType = null
        }
    }

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
            if (availableSources.isEmpty()) {
                PosterBackdrop()
                GuestTopBar(modifier = Modifier.align(Alignment.TopCenter))
                EmptyMediaLibraryContent(
                    onAddFileSourceClick = onAddFileSourceClick,
                    onAddServerClick = onAddServerClick,
                    onAddBilibiliClick = onAddBilibiliClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ConnectedMediaLibraryContent(
                    sourceName = mediaHome?.sourceName
                        ?: availableSources.firstOrNull { it.type == selectedSourceType }?.name
                        ?: "媒体库",
                    sourceType = selectedSourceType,
                    avatarImageUrl = currentSourceAvatarUrl(selectedSourceType, embySession, bilibiliSession),
                    avatarFallbackText = currentSourceAvatarFallback(selectedSourceType, embySession, bilibiliSession),
                    avatarFallbackColor = currentSourceAvatarColor(selectedSourceType),
                    avatarRequestHeaders = currentSourceAvatarHeaders(selectedSourceType, embySession),
                    sourceOptions = availableSources,
                    onSourceSelected = { option ->
                        selectedSourceType = option.type
                        MediaSourceSelectionStore.save(context, option.type)
                    },
                    mediaHome = mediaHome,
                    errorText = loadError,
                    debugState = debugState,
                    isRefreshing = isRefreshing && selectedSourceType == MediaSourceType.Bilibili,
                    onManualRefresh = {
                        if (selectedSourceType == MediaSourceType.Bilibili) {
                            refreshSourceType = MediaSourceType.Bilibili
                            refreshNonce++
                        }
                    },
                    onLibraryViewAllClick = onLibraryViewAllClick
                )
            }
        }
    }
}
@Composable
private fun ConnectedMediaLibraryContent(
    sourceName: String,
    sourceType: MediaSourceType,
    avatarImageUrl: String,
    avatarFallbackText: String,
    avatarFallbackColor: Color,
    avatarRequestHeaders: Map<String, String>,
    sourceOptions: List<MediaSourceOption>,
    onSourceSelected: (MediaSourceOption) -> Unit,
    mediaHome: EmbyMediaHome?,
    errorText: String?,
    debugState: EmbyHomeDebugState?,
    isRefreshing: Boolean,
    onManualRefresh: () -> Unit,
    onLibraryViewAllClick: (MediaBrowseRequest) -> Unit
) {
    val home = mediaHome ?: EmbyMediaHome(
        sourceName = sourceName,
        libraries = emptyList(),
        resumeItems = emptyList(),
        latestTitle = "最新媒体",
        latestItems = emptyList(),
        librarySections = emptyList()
    )

    val scrollState = rememberScrollState()
    var pullDistance by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(sourceType, isRefreshing) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (sourceType == MediaSourceType.Bilibili && pullDistance > 120f && !isRefreshing) {
                            onManualRefresh()
                        }
                        pullDistance = 0f
                    },
                    onDragCancel = { pullDistance = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        if (scrollState.value == 0 && dragAmount > 0f) {
                            pullDistance += dragAmount
                        }
                    }
                )
            }
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 22.dp)
    ) {
        ConnectedTopBar(
            sourceName = sourceName,
            avatarImageUrl = avatarImageUrl,
            avatarFallbackText = avatarFallbackText,
            avatarFallbackColor = avatarFallbackColor,
            avatarRequestHeaders = avatarRequestHeaders,
            sourceOptions = sourceOptions,
            onSourceSelected = onSourceSelected,
            onManualRefresh = onManualRefresh
        )

        if (SHOW_EMBY_DEBUG_LOG && debugState != null) {
            EmbyDebugCard(debugState)
        }

        if (isRefreshing) {
            Text(
                text = "正在刷新 Bilibili 缓存...",
                color = ActiveGreen,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                letterSpacing = 0.sp
            )
        }

        if (errorText != null) {
            Text(
                text = errorText,
                color = TextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                letterSpacing = 0.sp
            )
        }

        SectionTitle("我的媒体")
        HorizontalLibraryRow(
            libraries = home.libraries,
            sourceType = sourceType,
            onLibraryClick = onLibraryViewAllClick
        )

        SectionTitle("继续观看")
        HorizontalMediaRow(home.resumeItems, emptyText = "暂无继续观看")

        MediaLibrarySections(home, sourceType, onLibraryViewAllClick)
    }
}

@Composable
private fun ConnectedTopBar(
    sourceName: String,
    avatarImageUrl: String,
    avatarFallbackText: String,
    avatarFallbackColor: Color,
    avatarRequestHeaders: Map<String, String>,
    sourceOptions: List<MediaSourceOption>,
    onSourceSelected: (MediaSourceOption) -> Unit,
    onManualRefresh: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SourceAvatar(
                imageUrl = avatarImageUrl,
                fallbackText = avatarFallbackText,
                fallbackColor = avatarFallbackColor,
                requestHeaders = avatarRequestHeaders
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box {
                Text(
                    text = "$sourceName ▾",
                    color = PrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp,
                    modifier = Modifier.clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sourceOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                expanded = false
                                onSourceSelected(option)
                            }
                        )
                    }
                }
            }
        }

        IconButton(onClick = { }) {
            Icon(Icons.Default.Search, contentDescription = "搜索", tint = PrimaryText)
        }
        IconButton(onClick = onManualRefresh) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "刷新", tint = PrimaryText)
        }
    }
}

@Composable
private fun SourceAvatar(
    imageUrl: String,
    fallbackText: String,
    fallbackColor: Color,
    requestHeaders: Map<String, String>
) {
    val context = LocalContext.current
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching { loadSourceAvatarBitmap(context, imageUrl, requestHeaders) }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = fallbackText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }
    }
}
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = PrimaryText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp),
        letterSpacing = 0.sp
    )
}

@Composable
private fun LibrarySectionHeader(title: String, onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "查看全部 >",
            color = ActiveGreen,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onViewAllClick),
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun HorizontalLibraryRow(
    libraries: List<EmbyLibrarySummary>,
    sourceType: MediaSourceType,
    onLibraryClick: (MediaBrowseRequest) -> Unit
) {
    if (libraries.isEmpty()) {
        EmptySectionCard("暂无媒体库")
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(libraries, key = { it.id.ifBlank { it.name } }) { library ->
            LibraryCard(
                library = library,
                onClick = {
                    onLibraryClick(
                        MediaBrowseRequest(
                            sourceType = sourceType,
                            containerId = library.id,
                            title = library.name
                        )
                    )
                }
            )
        }
    }
}
@Composable
private fun LibraryCard(
    library: EmbyLibrarySummary,
    onClick: () -> Unit
) {
    MediaCardShell(
        modifier = Modifier.clickable(onClick = onClick),
        width = 156.dp
    ) {
        PosterFrame(height = 88.dp, imageUrl = library.imageUrl, showPlay = false) {
            LibraryPlaceholder()
        }
        CardTextBlock(
            title = library.name,
            subtitle = "${library.itemCount} 个项目",
            titleMaxLines = 1
        )
    }
}

@Composable
private fun HorizontalMediaRow(items: List<EmbyMediaItem>, emptyText: String) {
    if (items.isEmpty()) {
        EmptySectionCard(emptyText)
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { it.id.ifBlank { it.name } }) { item ->
            MediaPosterCard(item, width = 164.dp, imageHeight = 92.dp)
        }
    }
}

@Composable
private fun MediaLibrarySections(
    home: EmbyMediaHome,
    sourceType: MediaSourceType,
    onLibraryViewAllClick: (MediaBrowseRequest) -> Unit
) {
    val sections = if (home.librarySections.isNotEmpty()) {
        home.librarySections
    } else {
        home.libraries.map { library ->
            EmbyLibrarySection(
                libraryId = library.id,
                title = library.name,
                items = emptyList()
            )
        }
    }

    sections.forEach { section ->
        LibrarySectionHeader(
            title = section.title,
            onViewAllClick = {
                onLibraryViewAllClick(
                    MediaBrowseRequest(
                        sourceType = sourceType,
                        containerId = section.libraryId,
                        title = section.title
                    )
                )
            }
        )
        HorizontalLibraryContentRow(items = section.items, emptyText = "暂无${section.title}内容")
    }
}
@Composable
private fun HorizontalLibraryContentRow(items: List<EmbyMediaItem>, emptyText: String) {
    if (items.isEmpty()) {
        EmptySectionCard(emptyText)
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { it.id.ifBlank { it.name } }) { item ->
            MediaPosterCard(item, width = 164.dp, imageHeight = 92.dp)
        }
    }
}

@Composable
private fun MediaPosterCard(
    item: EmbyMediaItem,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    imageHeight: Dp
) {
    val cardModifier = if (width != null) modifier.width(width) else modifier
    MediaCardShell(modifier = cardModifier) {
        PosterFrame(
            height = imageHeight,
            imageUrl = item.imageUrl,
            showPlay = false
        ) {
            RemotePoster()
        }
        CardTextBlock(title = item.name, subtitle = item.subtitle)
    }
}

@Composable
private fun MediaCardShell(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (width != null) modifier.width(width) else modifier
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun PosterFrame(
    height: Dp,
    imageUrl: String,
    showPlay: Boolean,
    fallback: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(BackgroundGray),
        contentAlignment = Alignment.Center
    ) {
        CachedEmbyImage(
            imageUrl = imageUrl,
            modifier = Modifier.fillMaxSize(),
            fallback = fallback
        )

        if (showPlay) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CardTextBlock(
    title: String,
    subtitle: String,
    titleMaxLines: Int = 2
) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp,
            letterSpacing = 0.sp
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                color = TextGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
private fun CachedEmbyImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit
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
        Box(modifier = modifier) {
            fallback()
        }
    }
}

@Composable
private fun EmptySectionCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun LibraryPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun RemotePoster() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF455A64), Color(0xFF263238))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun EmbyDebugCard(state: EmbyHomeDebugState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = "Emby 调试日志",
                color = PrimaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            )
            Text(
                text = state.status,
                color = if (state.error == null) ActiveGreen else Color(0xFFE53935),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 6.dp),
                letterSpacing = 0.sp
            )
            if (state.libraries.isNotEmpty()) {
                Text(
                    text = state.libraries.joinToString("\n") { "媒体库：$it" },
                    color = PrimaryText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    letterSpacing = 0.sp
                )
            }
            if (state.error != null) {
                Text(
                    text = state.error,
                    color = TextGray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

private fun EmbyMediaHome.sanitized(): EmbyMediaHome {
    return copy(
        sourceName = sourceName.ifBlank { "Emby" },
        libraries = libraries
            .filter { it.id.isNotBlank() || it.name.isNotBlank() }
            .take(20),
        resumeItems = resumeItems
            .filter { it.id.isNotBlank() || it.name.isNotBlank() }
            .take(20),
        latestTitle = latestTitle.ifBlank { libraries.firstOrNull()?.name ?: "最新媒体" },
        latestItems = latestItems
            .filter { it.id.isNotBlank() || it.name.isNotBlank() }
            .take(30),
        librarySections = librarySections
            .filter { it.libraryId.isNotBlank() || it.title.isNotBlank() }
            .take(20)
            .map { section ->
                section.copy(
                    items = section.items
                        .filter { it.id.isNotBlank() || it.name.isNotBlank() }
                        .take(20)
                )
            }
    )
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
private fun GuestTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "VideoHub",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = "搜索", tint = PrimaryText)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "更多", tint = PrimaryText)
            }
        }
    }
}

@Composable
private fun EmptyMediaLibraryContent(
    onAddFileSourceClick: () -> Unit,
    onAddServerClick: () -> Unit,
    onAddBilibiliClick: () -> Unit,
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
            contentDescription = "VideoHub",
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
                    append("VideoHub")
                }
            },
            color = PrimaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "媒体库为空，请添加文件源、影视服务器或 Bilibili 收藏夹。",
            color = TextGray,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(0.82f),
            letterSpacing = 0.sp
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

        Spacer(modifier = Modifier.height(16.dp))

        MediaActionButton(
            text = "添加 Bilibili",
            icon = Icons.Default.PlayArrow,
            onClick = onAddBilibiliClick
        )
    }
}

data class MediaSourceOption(
    val type: MediaSourceType,
    val name: String
)

private fun currentSourceAvatarUrl(
    sourceType: MediaSourceType,
    embySession: com.liujiaming.videohub.feature.emby.EmbyAuthSession?,
    bilibiliSession: com.liujiaming.videohub.feature.bilibili.BilibiliSession?
): String {
    return when (sourceType) {
        MediaSourceType.Emby -> embySession?.embyUserAvatarUrl().orEmpty()
        MediaSourceType.Bilibili -> bilibiliSession?.face.orEmpty()
        else -> ""
    }
}

private fun currentSourceAvatarFallback(
    sourceType: MediaSourceType,
    embySession: com.liujiaming.videohub.feature.emby.EmbyAuthSession?,
    bilibiliSession: com.liujiaming.videohub.feature.bilibili.BilibiliSession?
): String {
    return when (sourceType) {
        MediaSourceType.Emby -> embySession?.username?.firstOrNull()?.uppercase() ?: "E"
        MediaSourceType.Bilibili -> bilibiliSession?.username?.firstOrNull()?.uppercase() ?: "B"
        else -> "V"
    }
}

private fun currentSourceAvatarColor(sourceType: MediaSourceType): Color {
    return when (sourceType) {
        MediaSourceType.Emby -> Color(0xFF43A047)
        MediaSourceType.Bilibili -> Color(0xFF00A1D6)
        else -> ActiveGreen
    }
}

private fun currentSourceAvatarHeaders(
    sourceType: MediaSourceType,
    embySession: com.liujiaming.videohub.feature.emby.EmbyAuthSession?
): Map<String, String> {
    return when (sourceType) {
        MediaSourceType.Emby -> mapOf("X-Emby-Token" to embySession?.accessToken.orEmpty())
        else -> emptyMap()
    }
}

private fun com.liujiaming.videohub.feature.emby.EmbyAuthSession.embyUserAvatarUrl(): String {
    val tagQuery = userPrimaryImageTag.takeIf { it.isNotBlank() }?.let { "&tag=$it" }.orEmpty()
    return "$serverUrl/Users/$userId/Images/Primary?fillHeight=96&quality=90$tagQuery&api_key=$accessToken"
}

private fun loadSourceAvatarBitmap(
    context: Context,
    imageUrl: String,
    requestHeaders: Map<String, String>
): Bitmap? {
    if (imageUrl.isBlank()) return null
    val file = sourceAvatarCacheFile(context, imageUrl)
    if (file.exists() && file.length() > 0L) {
        BitmapFactory.decodeFile(file.absolutePath)?.let { return it }
    }
    file.parentFile?.mkdirs()
    val tempFile = File(file.parentFile, "${file.name}.tmp")
    val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10000
        readTimeout = 15000
        setRequestProperty("User-Agent", "Mozilla/5.0 VideoHub Android")
        requestHeaders.forEach { (key, value) ->
            if (value.isNotBlank()) setRequestProperty(key, value)
        }
    }
    try {
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) return null
        connection.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (tempFile.length() > 0L) {
            tempFile.renameTo(file)
        } else {
            tempFile.delete()
        }
        return BitmapFactory.decodeFile(file.absolutePath)
    } finally {
        connection.disconnect()
    }
}

private fun sourceAvatarCacheFile(context: Context, imageUrl: String): File {
    val directory = File(context.applicationContext.cacheDir, "server_avatar_cache")
    return File(directory, "${md5(imageUrl)}.img")
}

private fun md5(value: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
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
            .height(49.5.dp),
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
