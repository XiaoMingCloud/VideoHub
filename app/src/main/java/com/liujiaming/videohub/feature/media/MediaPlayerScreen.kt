package com.liujiaming.videohub.feature.media

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.liujiaming.videohub.feature.bilibili.BilibiliPlaybackSource
import com.liujiaming.videohub.feature.bilibili.BilibiliClient
import com.liujiaming.videohub.feature.bilibili.BilibiliSessionStore
import com.liujiaming.videohub.feature.emby.EmbyHomeClient
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.feature.settings.SettingsMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val PLAYER_TAG = "MediaPlayerScreen"
private val PlayerOverlay = Color(0x80000000)
private val PlayerText = Color.White
private val PlayerMutedText = Color(0xCCFFFFFF)

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerScreen(
    item: MediaBrowseItem?,
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    if (item == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("未选择媒体", color = PlayerMutedText, fontSize = 14.sp, letterSpacing = 0.sp)
        }
        return
    }

    val context = LocalContext.current
    val view = LocalView.current
    var controlsVisible by remember { mutableStateOf(true) }
    var playerError by remember(item.id) { mutableStateOf<String?>(null) }
    var playerDebugText by remember(item.id) { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }
    var durationMs by remember { mutableStateOf(0L) }
    var positionMs by remember { mutableStateOf(0L) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    val player = remember(item.id) {
        val httpFactory = DefaultHttpDataSource.Factory().apply {
            setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to "Mozilla/5.0 VideoHub Android",
                    "Referer" to "https://www.bilibili.com"
                )
            )
        }
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
            .build()
            .apply {
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(value: Boolean) {
                isPlaying = value
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                durationMs = player.duration.takeIf { it > 0 } ?: 0L
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(PLAYER_TAG, "Playback error itemId=${item.id}: ${error.message}", error)
                playerError = error.message ?: "播放失败"
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = if (window != null) WindowInsetsControllerCompat(window, view) else null
        if (window != null && controller != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.isAppearanceLightStatusBars = false
        }
        onDispose {
            if (window != null && controller != null) {
                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.isAppearanceLightStatusBars = !SettingsMemory.resolvedDarkMode
            }
        }
    }

    LaunchedEffect(item.id, item.sourceType) {
        playerDebugText = "准备播放 source=${item.sourceType} id=${item.id}"
        val result = withContext(Dispatchers.IO) {
            runCatching {
                when (item.sourceType) {
                    MediaSourceType.Bilibili -> {
                        val session = BilibiliSessionStore.load(context) ?: error("请先扫码登录 Bilibili")
                        val source = BilibiliClient.fetchPlaybackSource(session, item.id)
                        BilibiliPlaybackTarget(
                            source = source,
                            debugText = "Bilibili session mid=${session.mid} cookieLength=${session.cookie.length} bvid=${item.id}\n" +
                                "Bilibili source host=${source.url.hostOrPrefix()} headers=${source.headers.keys.joinToString(",")}"
                        )
                    }
                    else -> {
                        val session = EmbySessionStore.load(context) ?: error("请先登录媒体服务器")
                        DirectPlaybackTarget(
                            url = EmbyHomeClient.playbackUrl(session, item.id),
                            debugText = "Emby session server=${session.serverName} itemId=${item.id}"
                        )
                    }
                }
            }
        }
        result.onSuccess { target ->
            Log.d(PLAYER_TAG, "Start playback itemId=${item.id} title=${item.name} sourceType=${item.sourceType}")
            playerError = null
            when (target) {
                is BilibiliPlaybackTarget -> {
                    playerDebugText = target.debugText
                    val httpFactory = DefaultHttpDataSource.Factory().apply {
                        setDefaultRequestProperties(target.source.headers)
                    }
                    val mediaSource = ProgressiveMediaSource.Factory(httpFactory)
                        .createMediaSource(MediaItem.fromUri(target.source.url))
                    player.setMediaSource(mediaSource)
                }
                is DirectPlaybackTarget -> {
                    playerDebugText = target.debugText
                    player.setMediaItem(MediaItem.fromUri(target.url))
                }
            }
            player.prepare()
            player.play()
        }.onFailure { error ->
            val detail = buildPlaybackErrorMessage(error)
            Log.e(PLAYER_TAG, "Prepare playback failed itemId=${item.id} sourceType=${item.sourceType}: $detail", error)
            playerDebugText = "$playerDebugText\n失败: $detail"
            playerError = if (item.sourceType == MediaSourceType.Bilibili) {
                "Bilibili 播放地址获取失败\n$detail"
            } else {
                error.message ?: "播放地址生成失败"
            }
        }
    }
    LaunchedEffect(player, isSeeking) {
        while (true) {
            durationMs = player.duration.takeIf { it > 0 } ?: durationMs
            positionMs = player.currentPosition.coerceAtLeast(0L)
            if (!isSeeking) {
                sliderPosition = positionMs.toFloat()
            }
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { controlsVisible = !controlsVisible }
    ) {
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize()
        )

        if (controlsVisible) {
            PlayerTopBar(
                title = item.name,
                onBackClick = onBackClick,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            PlayerSideTools(modifier = Modifier.align(Alignment.CenterStart))
            PlayerRightTool(modifier = Modifier.align(Alignment.CenterEnd))
            PlayerBottomBar(
                isPlaying = isPlaying,
                currentMs = if (isSeeking) sliderPosition.toLong() else positionMs,
                durationMs = durationMs,
                sliderPosition = sliderPosition,
                onSliderChange = { value ->
                    isSeeking = true
                    sliderPosition = value
                },
                onSliderFinished = {
                    player.seekTo(sliderPosition.toLong())
                    isSeeking = false
                },
                onPlayPauseClick = {
                    if (player.isPlaying) player.pause() else player.play()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        playerError?.let { message ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .background(PlayerOverlay)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message,
                    color = PlayerText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    letterSpacing = 0.sp
                )
                if (item.sourceType == MediaSourceType.Bilibili && playerDebugText.isNotBlank()) {
                    Text(
                        text = playerDebugText,
                        color = PlayerMutedText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PlayerOverlay)
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = PlayerText)
        }
        Text(
            text = title,
            color = PlayerText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = PlayerText)
        }
    }
}

@Composable
private fun PlayerSideTools(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(start = 10.dp)
            .background(PlayerOverlay)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButton(onClick = { }) {
            Icon(Icons.Default.Lock, contentDescription = "锁定", tint = PlayerText)
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.AspectRatio, contentDescription = "画幅调整", tint = PlayerText)
        }
    }
}

@Composable
private fun PlayerRightTool(modifier: Modifier = Modifier) {
    IconButton(
        onClick = { },
        modifier = modifier
            .padding(end = 10.dp)
            .background(PlayerOverlay)
    ) {
        Icon(Icons.Default.Fullscreen, contentDescription = "全屏", tint = PlayerText)
    }
}

@Composable
private fun PlayerBottomBar(
    isPlaying: Boolean,
    currentMs: Long,
    durationMs: Long,
    sliderPosition: Float,
    onSliderChange: (Float) -> Unit,
    onSliderFinished: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxPosition = durationMs.coerceAtLeast(1L).toFloat()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PlayerOverlay)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeText(formatPlayerTime(currentMs))
            Slider(
                value = sliderPosition.coerceIn(0f, maxPosition),
                onValueChange = onSliderChange,
                onValueChangeFinished = onSliderFinished,
                valueRange = 0f..maxPosition,
                colors = SliderDefaults.colors(
                    thumbColor = PlayerText,
                    activeTrackColor = PlayerText,
                    inactiveTrackColor = Color(0x66FFFFFF)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            TimeText(formatPlayerTime(durationMs))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "上一个", tint = PlayerText)
            }
            IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(58.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = PlayerText,
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "下一个", tint = PlayerText)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "1.0x",
                color = PlayerText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ClosedCaption, contentDescription = "字幕", tint = PlayerText)
            }
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.AspectRatio, contentDescription = "画面设置", tint = PlayerText)
            }
        }
    }
}

@Composable
private fun TimeText(text: String) {
    Text(
        text = text,
        color = PlayerText,
        fontSize = 12.sp,
        letterSpacing = 0.sp
    )
}

private fun formatPlayerTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private sealed interface PlaybackTarget

private data class DirectPlaybackTarget(
    val url: String,
    val debugText: String
) : PlaybackTarget

private data class BilibiliPlaybackTarget(
    val source: BilibiliPlaybackSource,
    val debugText: String
) : PlaybackTarget

private fun buildPlaybackErrorMessage(error: Throwable): String {
    val parts = buildList {
        add(error::class.java.simpleName.ifBlank { "Throwable" })
        error.message?.takeIf { it.isNotBlank() }?.let { add(it) }
        error.cause?.let { cause ->
            add("cause=${cause::class.java.simpleName}:${cause.message.orEmpty()}")
        }
    }
    return parts.joinToString(" | ").ifBlank {
        error.stackTraceToString().lineSequence().firstOrNull().orEmpty().ifBlank {
            "unknown playback error"
        }
    }
}

private fun String.hostOrPrefix(): String {
    return runCatching { java.net.URL(this).host }.getOrNull()
        ?: take(64)
}
