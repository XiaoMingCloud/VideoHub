package com.liujiaming.videohub.feature.media

import android.app.Activity
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.feature.emby.EmbyHomeClient
import com.liujiaming.videohub.feature.emby.EmbyImageCache
import com.liujiaming.videohub.feature.emby.EmbyMediaItemDetail
import com.liujiaming.videohub.feature.emby.EmbyMediaStreamInfo
import com.liujiaming.videohub.feature.emby.EmbyPersonInfo
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.feature.bilibili.BilibiliClient
import com.liujiaming.videohub.feature.bilibili.BilibiliSessionStore
import com.liujiaming.videohub.feature.bilibili.BilibiliVideoDetail
import com.liujiaming.videohub.feature.settings.SettingsMemory
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val DetailBackground: Color
    get() = PageBackground
private val DetailCard: Color
    get() = CardBackground
private val DetailText: Color
    get() = PrimaryText
private val DetailSubText: Color
    get() = TextGray

@Composable
fun MediaItemDetailScreen(
    item: MediaBrowseItem?,
    onBackClick: () -> Unit,
    onPlayClick: (MediaBrowseItem) -> Unit = {}
) {
    if (item == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DetailBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("未选择媒体", color = DetailSubText, fontSize = 14.sp, letterSpacing = 0.sp)
        }
        return
    }
    val context = LocalContext.current
    val view = LocalView.current
    var detail by remember(item.id) { mutableStateOf<EmbyMediaItemDetail?>(null) }
    var bilibiliDetail by remember(item.id) { mutableStateOf<BilibiliVideoDetail?>(null) }
    var detailError by remember(item.id) { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !SettingsMemory.resolvedDarkMode
        }
        onDispose {
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !SettingsMemory.resolvedDarkMode
            }
        }
    }

    LaunchedEffect(item.id, item.sourceType) {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                if (item.sourceType == MediaSourceType.Bilibili) {
                    val session = BilibiliSessionStore.load(context) ?: error("请先扫码登录 Bilibili")
                    BilibiliDetailResult(BilibiliClient.fetchVideoDetail(session, item.id))
                } else {
                    val session = EmbySessionStore.load(context) ?: error("请先登录媒体服务器")
                    EmbyDetailResult(EmbyHomeClient.fetchItemDetail(session, item.id))
                }
            }
        }
        result.onSuccess { value ->
            when (value) {
                is EmbyDetailResult -> detail = value.detail
                is BilibiliDetailResult -> bilibiliDetail = value.detail
            }
            detailError = null
        }.onFailure {
            detailError = it.message ?: "加载媒体详情失败"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBackground)
            .verticalScroll(rememberScrollState())
    ) {
        DetailHero(item = item, onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = item.name,
                color = DetailText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 25.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = buildMetaText(item, detail, bilibiliDetail),
                color = DetailSubText,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 7.dp)
            )

            DetailActions(item = item, onPlayClick = onPlayClick)

            SectionLabel("简介")
            Text(
                text = bilibiliDetail?.description?.takeIf { it.isNotBlank() }
                    ?: detail?.overview?.takeIf { it.isNotBlank() }
                    ?: detailError
                    ?: "正在加载简介...",
                color = DetailSubText,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (item.sourceType == MediaSourceType.Bilibili) {
                BilibiliAuthorSection(bilibiliDetail)
            } else {
                CastPeopleSection(detail = detail)
            }

            SectionLabel("音视频字幕信息")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TechnicalInfoCard(
                    title = "视频信息",
                    rows = videoInfoRowsV2(detail),
                    modifier = Modifier.weight(1f)
                )
                TechnicalInfoCard(
                    title = "音频信息",
                    rows = audioInfoRowsV2(item, detail),
                    modifier = Modifier.weight(1f)
                )
            }

            PathInfoCard(detail = detail)
        }
    }
}

@Composable
private fun DetailHero(
    item: MediaBrowseItem,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(345.dp)
            .background(Color.Black)
    ) {
        CachedDetailImage(
            imageUrl = item.imageUrl,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingIconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            FloatingIconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = Color.White)
            }
        }
    }
}

@Composable
private fun DetailActions(
    item: MediaBrowseItem,
    onPlayClick: (MediaBrowseItem) -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onPlayClick(item) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ActiveGreen,
                contentColor = Color.White
            ),
            elevation = null
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text("播放", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)
        }

        Spacer(modifier = Modifier.size(24.dp))
        IconButton(onClick = { isFavorite = !isFavorite }) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                tint = if (isFavorite) Color(0xFFE53935) else DetailText
            )
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Flag, contentDescription = "标记", tint = DetailText)
        }
    }
}

@Composable
private fun CastSection(detail: EmbyMediaItemDetail?) {
    val actors = detail?.people.orEmpty()
        .filter { it.type.equals("Actor", ignoreCase = true) }
        .filter { it.name.isNotBlank() }
        .take(20)
    if (actors.isEmpty()) return

    SectionLabel("演职人员")
    LazyRow(
        modifier = Modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(actors, key = { it.id.ifBlank { it.name } }) { actor ->
            CastCard(actor)
        }
    }
}

@Composable
private fun CastCard(actor: EmbyPersonInfo) {
    Column(
        modifier = Modifier.width(78.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CachedDetailImage(
            imageUrl = actor.imageUrl,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DetailCard)
        )
        Text(
            text = actor.name,
            color = DetailText,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
        if (actor.role.isNotBlank()) {
            Text(
                text = actor.role,
                color = DetailSubText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun CastPeopleSection(detail: EmbyMediaItemDetail?) {
    val people = detail?.people.orEmpty()
        .filter { it.name.isNotBlank() }
        .take(20)
    if (people.isEmpty()) return

    SectionLabel("演职人员")
    LazyRow(
        modifier = Modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(people, key = { it.id.ifBlank { it.name } }) { person ->
            CastPersonCard(person)
        }
    }
}

@Composable
private fun CastPersonCard(person: EmbyPersonInfo) {
    Column(
        modifier = Modifier.width(78.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CachedDetailImage(
            imageUrl = person.imageUrl,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DetailCard)
        )
        Text(
            text = person.name,
            color = DetailText,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
        val subtitle = person.role.ifBlank { person.type }
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                color = DetailSubText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun BilibiliAuthorSection(detail: BilibiliVideoDetail?) {
    val author = detail?.authorName?.takeIf { it.isNotBlank() } ?: return
    SectionLabel("作者")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CachedDetailImage(
            imageUrl = detail.authorFace,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(DetailCard)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = author,
                color = DetailText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            )
            Text(
                text = "Bilibili UP 主",
                color = DetailSubText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
private fun PathInfoCard(detail: EmbyMediaItemDetail?) {
    val source = detail?.mediaSources?.firstOrNull()
    val path = source?.path?.takeIf { it.isNotBlank() }
        ?: detail?.path?.takeIf { it.isNotBlank() }
        ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DetailCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = path,
                color = DetailSubText,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.sp
            )
            Text(
                text = fileMetaText(detail),
                color = DetailSubText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TechnicalInfoCard(
    title: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DetailCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = DetailText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label,
                        color = DetailSubText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp
                    )
                    Text(
                        value,
                        color = DetailText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = DetailText,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
        modifier = Modifier.padding(top = 24.dp)
    )
}

@Composable
private fun FloatingIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, content = content)
    }
}

@Composable
private fun CachedDetailImage(
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
            modifier = modifier.background(DetailCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = DetailSubText,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

private fun buildMetaText(
    item: MediaBrowseItem,
    detail: EmbyMediaItemDetail?,
    bilibiliDetail: BilibiliVideoDetail? = null
): String {
    if (item.sourceType == MediaSourceType.Bilibili) {
        return listOfNotNull(
            bilibiliDetail?.durationSeconds?.takeIf { it > 0L }?.let { formatSeconds(it) },
            item.subtitle.takeIf { it.isNotBlank() },
            "Bilibili"
        ).joinToString(" ")
    }
    val video = detail?.mediaStreams?.firstOrNull { it.type.equals("Video", ignoreCase = true) }
    return listOfNotNull(
        detail?.runTimeTicks?.takeIf { it > 0L }?.let { formatRuntime(it) },
        video?.height?.let { "${it}p" },
        video?.codec?.uppercase(),
        detail?.productionYear?.toString(),
        item.type.takeIf { it.isNotBlank() }
    ).joinToString(" ")
}

private fun formatSeconds(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun formatProgress(progress: Float): String {
    return if (progress > 0f) "${(progress * 100).toInt()}%" else "0%"
}

private fun videoInfoRowsV2(detail: EmbyMediaItemDetail?): List<Pair<String, String>> {
    val video = detail?.mediaStreams?.firstOrNull { it.type.equals("Video", ignoreCase = true) }
    return listOf(
        "标题" to video?.displayTitle.orDash(),
        "编码" to video?.codec.orDash().uppercase(),
        "配置文件" to video?.profile.orDash(),
        "等级" to video?.level.formatLevel(),
        "分辨率" to video?.resolutionText().orDash(),
        "画面比例" to video?.aspectRatio.orDash(),
        "隔行扫描" to video?.scanType.formatScanType(),
        "帧率" to video?.frameRate?.let { String.format(java.util.Locale.US, "%.3f", it) }.orDash(),
        "比特率" to video?.bitRate.formatKbps(),
        "视频范围" to video?.videoRange.orDash(),
        "位深度" to video?.bitDepth?.let { "$it bit" }.orDash(),
        "像素格式" to video?.pixelFormat.orDash(),
        "参考帧" to video?.refFrames?.toString().orDash()
    )
}

private fun audioInfoRowsV2(
    item: MediaBrowseItem,
    detail: EmbyMediaItemDetail?
): List<Pair<String, String>> {
    val audio = detail?.mediaStreams?.firstOrNull { it.type.equals("Audio", ignoreCase = true) }
    val subtitles = detail?.mediaStreams.orEmpty()
        .filter { it.type.equals("Subtitle", ignoreCase = true) }
    return listOf(
        "编码" to audio?.codec.orDash().uppercase(),
        "声道" to audio?.channels?.let { "$it 声道" }.orDash(),
        "码率" to audio?.bitRate.formatKbpsUpper(),
        "采样率" to audio?.sampleRate?.let { "${it} Hz" }.orDash(),
        "语言" to audio?.language.orDash(),
        "字幕" to if (subtitles.isEmpty()) "无" else subtitles.joinToString(" / ") {
            it.displayTitle.ifBlank { it.language.ifBlank { "字幕" } }
        },
        "进度" to formatProgress(item.playbackProgress)
    )
}

private fun fileMetaText(detail: EmbyMediaItemDetail?): String {
    val source = detail?.mediaSources?.firstOrNull()
    return listOfNotNull(
        source?.runTimeTicks?.let { formatRuntime(it) }
            ?: detail?.runTimeTicks?.takeIf { it > 0L }?.let { formatRuntime(it) },
        source?.size.formatBytes(),
        source?.container?.takeIf { it.isNotBlank() }?.uppercase(),
        source?.bitRate.formatBitRate().takeIf { it != "-" }
    ).joinToString("  ")
}

private fun videoInfoRows(
    item: MediaBrowseItem,
    detail: EmbyMediaItemDetail?
): List<Pair<String, String>> {
    val video = detail?.mediaStreams?.firstOrNull { it.type.equals("Video", ignoreCase = true) }
    return listOf(
        "类型" to item.type.ifBlank { "Video" },
        "分辨率" to video?.resolutionText().orDash(),
        "编码" to video?.codec.orDash().uppercase(),
        "码率" to video?.bitRate.formatBitRate(),
        "状态" to if (item.played) "已观看" else "未观看"
    )
}

private fun audioInfoRows(
    item: MediaBrowseItem,
    detail: EmbyMediaItemDetail?
): List<Pair<String, String>> {
    val audio = detail?.mediaStreams?.firstOrNull { it.type.equals("Audio", ignoreCase = true) }
    val subtitles = detail?.mediaStreams.orEmpty()
        .filter { it.type.equals("Subtitle", ignoreCase = true) }
    return listOf(
        "编码" to audio?.codec.orDash().uppercase(),
        "声道" to audio?.channels?.let { "$it 声道" }.orDash(),
        "采样率" to audio?.sampleRate?.let { "${it}Hz" }.orDash(),
        "语言" to audio?.language.orDash(),
        "字幕" to if (subtitles.isEmpty()) "无" else subtitles.joinToString(" / ") { it.displayTitle.ifBlank { it.language.ifBlank { "字幕" } } },
        "进度" to formatProgress(item.playbackProgress)
    )
}

private fun EmbyMediaStreamInfo.resolutionText(): String {
    return if (width != null && height != null) {
        "${width}x${height}"
    } else {
        displayTitle
    }
}

private fun Int?.formatBitRate(): String {
    val value = this ?: return "-"
    return if (value >= 1_000_000) {
        "${value / 1_000_000} Mbps"
    } else {
        "${value / 1_000} Kbps"
    }
}

private fun Int?.formatKbps(): String {
    val value = this ?: return "-"
    return "${value / 1_000} kbps"
}

private fun Int?.formatKbpsUpper(): String {
    val value = this ?: return "-"
    return "${value / 1_000} Kbps"
}

private fun Double?.formatLevel(): String {
    val value = this ?: return "-"
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", value)
    }
}

private fun String?.formatScanType(): String {
    val value = this?.takeIf { it.isNotBlank() } ?: return "-"
    return if (value.equals("Progressive", ignoreCase = true)) "否" else value
}

private fun Long?.formatBytes(): String? {
    val value = this ?: return null
    val gb = value / 1024.0 / 1024.0 / 1024.0
    val mb = value / 1024.0 / 1024.0
    return if (gb >= 1.0) {
        String.format(java.util.Locale.US, "%.2f GB", gb)
    } else {
        String.format(java.util.Locale.US, "%.2f MB", mb)
    }
}

private fun String?.orDash(): String {
    return this?.takeIf { it.isNotBlank() } ?: "-"
}

private fun formatRuntime(runTimeTicks: Long): String {
    val totalSeconds = runTimeTicks / 10_000_000L
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}小时${minutes}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "${totalSeconds}秒"
    }
}

private sealed interface MediaDetailLoadResult

private data class EmbyDetailResult(
    val detail: EmbyMediaItemDetail
) : MediaDetailLoadResult

private data class BilibiliDetailResult(
    val detail: BilibiliVideoDetail
) : MediaDetailLoadResult
