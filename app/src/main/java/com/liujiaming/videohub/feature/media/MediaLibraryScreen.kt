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

/**
 * 媒体库主页面，作为应用首页。
 * 根据 Emby 登录状态展示不同内容：
 * - 未登录：展示海报背景 + 欢迎信息 + 添加文件源/服务器按钮
 * - 已登录：展示 Emby 媒体库数据（我的媒体、继续观看、各库分区）
 *
 * @param onAddFileSourceClick 添加文件源按钮点击回调
 * @param onAddServerClick 添加服务器按钮点击回调
 * @param onFileClick 底部导航"文件源"点击回调
 * @param onServerClick 底部导航"影视服务器"点击回调
 * @param onSettingsClick 底部导航"设置"点击回调
 */
@Composable
fun MediaLibraryScreen(
    onAddFileSourceClick: () -> Unit,
    onAddServerClick: () -> Unit,
    onFileClick: () -> Unit,
    onServerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    // 从本地存储加载 Emby 会话
    val embySession = remember { EmbySessionStore.load(context) }
    // Emby 首页媒体数据（响应式状态）
    var mediaHome by remember(embySession?.accessToken) { mutableStateOf<EmbyMediaHome?>(null) }
    // 加载错误信息
    var loadError by remember(embySession?.accessToken) { mutableStateOf<String?>(null) }
    // Emby 调试状态
    var debugState by remember(embySession?.accessToken) { mutableStateOf<EmbyHomeDebugState?>(null) }

    // 当 Emby 会话变化时，异步加载缓存的首页数据
    LaunchedEffect(embySession?.accessToken) {
        if (embySession == null) return@LaunchedEffect

        val cachedHome = withContext(Dispatchers.IO) {
            EmbyHomeCache.load(context, embySession.userId)
        }
        debugState = withContext(Dispatchers.IO) {
            EmbyHomeDebugStore.load(context)
        }
        if (cachedHome?.home != null) {
            // 有缓存数据，清洗后使用
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
            // 底部悬浮导航栏，当前高亮"媒体库"项
            FloatingBottomNav(
                activeItem = BottomNavItem.Media,
                onMediaClick = {},      // 当前已在媒体库页面
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
                // 未登录状态：海报背景 + 顶部操作栏 + 空状态内容
                PosterBackdrop()
                TopActions(modifier = Modifier.align(Alignment.TopCenter))
                EmptyMediaLibraryContent(
                    onAddFileSourceClick = onAddFileSourceClick,
                    onAddServerClick = onAddServerClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // 已登录状态：展示 Emby 媒体库数据
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

/**
 * 海报背景组件，用于未登录状态的装饰性背景。
 * 使用 Canvas 绘制倾斜排列的彩色圆角矩形网格，
 * 上层覆盖垂直渐变遮罩，使内容可读。
 */
@Composable
private fun PosterBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Canvas 绘制倾斜的海报网格
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tileWidth = size.width / 3.4f
            val tileHeight = tileWidth * 1.45f
            // 海报颜色数组，循环使用
            val colors = listOf(
                Color(0xFF2D6CDF),
                Color(0xFFE4A33A),
                Color(0xFF9C4E97),
                Color(0xFF3A8B7A),
                Color(0xFFB94B4B),
                Color(0xFF455A64)
            )

            // 绘制 6x5 的网格，每个格子旋转 -10 度
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

        // 垂直渐变遮罩，从上到下逐渐不透明
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

/**
 * 已连接状态下的媒体库内容区域。
 * 包含顶部栏、调试日志卡片、各媒体分区（我的媒体、继续观看、各库分区）。
 *
 * @param sourceName 数据源名称
 * @param mediaHome Emby 首页数据（可为 null）
 * @param errorText 错误提示文字
 * @param debugState 调试状态信息
 */
@Composable
private fun ConnectedMediaLibraryContent(
    sourceName: String,
    mediaHome: EmbyMediaHome?,
    errorText: String?,
    debugState: EmbyHomeDebugState?
) {
    // 如果无数据则构造空数据占位
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
        // 顶部标题栏（显示数据源名称）
        ConnectedTopBar(sourceName)

        // Emby 调试信息卡片（如有）
        if (debugState != null) {
            EmbyDebugCard(debugState)
        }

        // 错误提示文字（如有）
        if (errorText != null) {
            Text(
                text = errorText,
                color = TextGray,
                fontSize = 13.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // "我的媒体"分区 - 横向滚动的媒体库卡片
        SectionTitle("我的媒体")
        HorizontalLibraryRow(home.libraries)

        // "继续观看"分区 - 横向滚动的媒体海报
        SectionTitle("继续观看")
        HorizontalMediaRow(home.resumeItems, emptyText = "暂无继续观看")

        // 各媒体库分区 - 动态展示每个媒体库的内容
        MediaLibrarySections(home)
        // 以下"最新媒体"网格布局目前被禁用（if(false)）
        if (false) {
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
}

/**
 * 媒体库分区展示组件。
 * 遍历所有媒体库分区（librarySections），为每个分区展示标题和横向媒体海报列表。
 * 如果无分区数据，则根据媒体库列表生成空的占位分区。
 *
 * @param home Emby 首页数据
 */
@Composable
private fun MediaLibrarySections(home: EmbyMediaHome) {
    // 优先使用实际的分区数据，无数据时根据库列表生成空分区
    val sections = if (home.librarySections.isNotEmpty()) {
        home.librarySections
    } else {
        home.libraries.map { library ->
            com.liujiaming.videohub.feature.emby.EmbyLibrarySection(
                libraryId = library.id,
                title = library.name,
                items = emptyList()
            )
        }
    }

    // 为每个分区渲染标题和媒体海报行
    sections.forEach { section ->
        SectionTitle(section.title)
        HorizontalMediaRow(
            items = section.items,
            emptyText = "暂无${section.title}内容"
        )
    }
}

/**
 * Emby 调试日志卡片。展示数据拉取状态，成功时绿色，失败时红色。
 */
@Composable
private fun EmbyDebugCard(state: EmbyHomeDebugState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(text = "Emby 调试日志", color = PrimaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = state.status, color = if (state.error == null) ActiveGreen else Color(0xFFE53935), fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 6.dp))
            if (state.libraries.isNotEmpty()) {
                Text(text = state.libraries.joinToString("\n") { "媒体库：$it" }, color = PrimaryText, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
            }
            if (state.error != null) {
                Text(text = state.error, color = TextGray, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

/** 已连接状态的顶部栏，显示数据源名称 + 搜索/更多按钮 */
@Composable
private fun ConnectedTopBar(sourceName: String) {
    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = sourceName, color = PrimaryText, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "搜索", tint = PrimaryText) }
            IconButton(onClick = { }) { Icon(Icons.Default.MoreHoriz, contentDescription = "更多", tint = PrimaryText) }
        }
    }
}

/** 分区标题组件 */
@Composable
private fun SectionTitle(text: String) {
    Text(text = text, color = PrimaryText, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 10.dp))
}

/** 横向滚动的媒体库卡片列表 */
@Composable
private fun HorizontalLibraryRow(libraries: List<EmbyLibrarySummary>) {
    if (libraries.isEmpty()) { EmptySectionCard("暂无媒体库，请在设置-资源中刷新在线影视数据"); return }
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        libraries.forEach { LibraryCard(it) }
    }
}

/** 空状态占位卡片 */
@Composable
private fun EmptySectionCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBackground), elevation = CardDefaults.cardElevation(0.dp)) {
        Text(text = text, color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp))
    }
}

/** 单个媒体库卡片，展示封面图、名称和项目数量 */
@Composable
private fun LibraryCard(library: EmbyLibrarySummary) {
    Card(modifier = Modifier.width(140.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBackground), elevation = CardDefaults.cardElevation(0.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(86.dp).background(BackgroundGray), contentAlignment = Alignment.Center) {
                CachedEmbyImage(imageUrl = library.imageUrl, modifier = Modifier.fillMaxSize(), fallback = { LibraryPlaceholder() })
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                Text(text = library.name, color = PrimaryText, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${library.itemCount} 个项目", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

/** 横向滚动的媒体海报行 */
@Composable
private fun HorizontalMediaRow(items: List<EmbyMediaItem>, emptyText: String) {
    if (items.isEmpty()) { EmptySectionCard(emptyText); return }
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { MediaPosterCard(it, width = 154.dp) }
    }
}

/** 媒体项网格布局（2列），目前被 if(false) 禁用 */
@Composable
private fun MediaGrid(items: List<EmbyMediaItem>) {
    if (items.isEmpty()) { EmptySectionCard("暂无媒体内容"); return }
    val rows = items.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                rowItems.forEach { item -> Box(modifier = Modifier.weight(1f)) { MediaPosterCard(item, modifier = Modifier.fillMaxWidth()) } }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** 媒体海报卡片，展示封面图（带播放按钮叠加层）和名称 */
@Composable
private fun MediaPosterCard(item: EmbyMediaItem, modifier: Modifier = Modifier, width: androidx.compose.ui.unit.Dp? = null) {
    val cardModifier = if (width != null) modifier.width(width) else modifier
    Card(modifier = cardModifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBackground), elevation = CardDefaults.cardElevation(0.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)), contentAlignment = Alignment.Center) {
                CachedEmbyImage(imageUrl = item.imageUrl, modifier = Modifier.fillMaxSize(), fallback = { RemotePoster() })
                if (item.type.equals("Episode", true) || item.type.equals("Movie", true)) {
                    Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Text(text = item.name, color = PrimaryText, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
        }
    }
}

/** 缓存图片加载组件，异步从 EmbyImageCache 加载，加载期间显示 fallback */
@Composable
private fun CachedEmbyImage(imageUrl: String, modifier: Modifier = Modifier, fallback: @Composable () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(imageUrl) { bitmap = withContext(Dispatchers.IO) { runCatching { EmbyImageCache.loadBitmap(context, imageUrl) }.getOrNull() } }
    if (bitmap != null) {
        Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    } else { Box(modifier = modifier) { fallback() } }
}

/** 媒体库占位符，封面图加载失败时的默认图标 */
@Composable
private fun LibraryPlaceholder() {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundGray), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = TextGray, modifier = Modifier.size(34.dp))
    }
}

/** 远程海报占位符，渐变色背景 + 白色图标 */
@Composable
private fun RemotePoster() {
    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF455A64), Color(0xFF263238)))), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(28.dp))
    }
}

/** 清洗首页数据，过滤无效条目并限制数量 */
private fun EmbyMediaHome.sanitized(): EmbyMediaHome {
    return copy(
        sourceName = sourceName.ifBlank { "Emby" },
        libraries = libraries.filter { it.id.isNotBlank() || it.name.isNotBlank() }.take(20),
        resumeItems = resumeItems.filter { it.id.isNotBlank() || it.name.isNotBlank() }.take(20),
        latestTitle = latestTitle.ifBlank { libraries.firstOrNull()?.name ?: "最新媒体" },
        latestItems = latestItems.filter { it.id.isNotBlank() || it.name.isNotBlank() }.take(30),
        librarySections = librarySections.filter { it.libraryId.isNotBlank() || it.title.isNotBlank() }.take(20)
            .map { s -> s.copy(items = s.items.filter { it.id.isNotBlank() || it.name.isNotBlank() }.take(20)) }
    )
}

/** 未登录状态的顶部操作栏，Logo + 搜索 + 更多 */
@Composable
private fun TopActions(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.5.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(R.drawable.logo), contentDescription = "VideoHub", contentScale = ContentScale.Crop, modifier = Modifier.size(42.dp).shadow(8.dp, CircleShape).clip(CircleShape))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "搜索", tint = PrimaryText) }
            IconButton(onClick = { }) { Icon(Icons.Default.MoreHoriz, contentDescription = "更多", tint = PrimaryText) }
        }
    }
}

/** 未登录空状态内容，Logo + 欢迎语 + 添加按钮 */
@Composable
private fun EmptyMediaLibraryContent(onAddFileSourceClick: () -> Unit, onAddServerClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(bottom = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(R.drawable.logo), contentDescription = "VideoHub", contentScale = ContentScale.Crop, modifier = Modifier.size(92.dp).clip(RoundedCornerShape(24.dp)))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = buildAnnotatedString { append("欢迎来到 "); withStyle(SpanStyle(color = ActiveGreen)) { append("VideoHub") } }, color = PrimaryText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "媒体库为空，请添加文件源或影视服务器，享受您的私人影院。", color = TextGray, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.fillMaxWidth(0.82f))
        Spacer(modifier = Modifier.height(40.dp))
        MediaActionButton("添加文件源", Icons.Default.Folder, onAddFileSourceClick)
        Spacer(modifier = Modifier.height(16.dp))
        MediaActionButton("添加影视服务器", Icons.Default.VideoLibrary, onAddServerClick)
    }
}

/** 媒体操作按钮，带图标的绿色圆角按钮 */
@Composable
private fun MediaActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(0.84f).height(49.5.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen, contentColor = Color.White), elevation = null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
