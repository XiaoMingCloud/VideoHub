package com.liujiaming.videohub.feature.media

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.R
import com.liujiaming.videohub.feature.emby.EmbyHomeCache
import com.liujiaming.videohub.feature.emby.EmbyHomeDebugState
import com.liujiaming.videohub.feature.emby.EmbyHomeDebugStore
import com.liujiaming.videohub.feature.emby.EmbyImageCache
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

@Composable
fun MediaLibraryScreen(
    onAddFileSourceClick: () -> Unit,
    onAddServerClick: () -> Unit,
    onFileClick: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val embySession = remember { EmbySessionStore.load(context) }
    var mediaHome by remember(embySession?.accessToken) { mutableStateOf<EmbyMediaHome?>(null) }
    var loadError by remember(embySession?.accessToken) { mutableStateOf<String?>(null) }
    var debugState by remember(embySession?.accessToken) { mutableStateOf<EmbyHomeDebugState?>(null) }

    LaunchedEffect(embySession?.accessToken) {
        if (embySession == null) return@LaunchedEffect

        val cachedHome = withContext(Dispatchers.IO) {
            EmbyHomeCache.load(context, embySession.userId)
        }
        debugState = withContext(Dispatchers.IO) {
            EmbyHomeDebugStore.load(context)
        }
        if (cachedHome?.home != null) {
            mediaHome = cachedHome.home.sanitized()
            loadError = null
        } else {
            mediaHome = null
            loadError = "已连接 Emby，请在设置-资源中刷新在线影视数据"
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
            if (embySession == null) {
                PosterBackdrop()
                TopActions(modifier = Modifier.align(Alignment.TopCenter))
                EmptyMediaLibraryContent(
                    onAddFileSourceClick = onAddFileSourceClick,
                    onAddServerClick = onAddServerClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ConnectedMediaLibraryContent(
                    sourceName = mediaHome?.sourceName ?: embySession.serverName.ifBlank { "Emby" },
                    mediaHome = mediaHome,
                    errorText = loadError,
                    debugState = debugState
                )
            }
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
private fun ConnectedMediaLibraryContent(
    sourceName: String,
    mediaHome: EmbyMediaHome?,
    errorText: String?,
    debugState: EmbyHomeDebugState?
) {
    val home = mediaHome ?: EmbyMediaHome(
        sourceName = sourceName,
        libraries = emptyList(),
        resumeItems = emptyList(),
        latestTitle = "最新媒体",
        latestItems = emptyList()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        ConnectedTopBar(sourceName)

        if (debugState != null) {
            EmbyDebugCard(debugState)
        }

        if (errorText != null) {
            Text(
                text = errorText,
                color = TextGray,
                fontSize = 13.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        SectionTitle("我的媒体")
        HorizontalLibraryRow(home.libraries)

        SectionTitle("继续观看")
        HorizontalMediaRow(home.resumeItems, emptyText = "暂无继续观看")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = home.latestTitle,
                color = PrimaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
            Text(
                text = "查看全部 >",
                color = ActiveGreen,
                fontSize = 14.sp,
                letterSpacing = 0.sp
            )
        }

        MediaGrid(home.latestItems)
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
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            if (state.libraries.isNotEmpty()) {
                Text(
                    text = state.libraries.joinToString(separator = "\n") { "媒体库：$it" },
                    color = PrimaryText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = TextGray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectedTopBar(sourceName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sourceName,
            color = PrimaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = PrimaryText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
    )
}

@Composable
private fun HorizontalLibraryRow(libraries: List<EmbyLibrarySummary>) {
    if (libraries.isEmpty()) {
        EmptySectionCard("暂无媒体库，请在设置-资源中刷新在线影视数据")
        return
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        libraries.forEach { library ->
            LibraryCard(library)
        }
    }
}

@Composable
private fun EmptySectionCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            color = TextGray,
            fontSize = 14.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp)
        )
    }
}

@Composable
private fun LibraryCard(library: EmbyLibrarySummary) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .background(BackgroundGray),
                contentAlignment = Alignment.Center
            ) {
                CachedEmbyImage(
                    imageUrl = library.imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    fallback = { LibraryPlaceholder() }
                )
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                Text(
                    text = library.name,
                    color = PrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp
                )
                Text(
                    text = "${library.itemCount} 个项目",
                    color = TextGray,
                    fontSize = 12.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun HorizontalMediaRow(
    items: List<EmbyMediaItem>,
    emptyText: String
) {
    if (items.isEmpty()) {
        EmptySectionCard(emptyText)
        return
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            MediaPosterCard(item, width = 154.dp)
        }
    }
}

@Composable
private fun MediaGrid(items: List<EmbyMediaItem>) {
    if (items.isEmpty()) {
        EmptySectionCard("暂无媒体内容")
        return
    }

    val rows = items.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        MediaPosterCard(item, modifier = Modifier.fillMaxWidth())
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MediaPosterCard(
    item: EmbyMediaItem,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null
) {
    val cardModifier = if (width != null) modifier.width(width) else modifier
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                CachedEmbyImage(
                    imageUrl = item.imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    fallback = { RemotePoster() }
                )
                if (item.type.equals("Episode", ignoreCase = true) || item.type.equals("Movie", ignoreCase = true)) {
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
            Text(
                text = item.name,
                color = PrimaryText,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
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
            modifier = Modifier.size(34.dp)
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
            .take(30)
    )
}

@Composable
private fun TopActions(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "VideoHub",
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
