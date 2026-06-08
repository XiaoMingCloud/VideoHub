package com.liujiaming.videohub.feature.server

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.feature.emby.EmbyAuthSession
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.feature.bilibili.BilibiliSession
import com.liujiaming.videohub.feature.bilibili.BilibiliSessionStore
import com.liujiaming.videohub.feature.media.MediaSourceSelectionStore
import com.liujiaming.videohub.feature.media.MediaSourceType
import com.liujiaming.videohub.ui.components.AppListDivider
import com.liujiaming.videohub.ui.components.BottomNavItem
import com.liujiaming.videohub.ui.components.FloatingBottomNav
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import com.liujiaming.videohub.ui.theme.VideoHubTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

@Composable
fun MediaServerScreen(
    onEmbyClick: () -> Unit = {},
    onBilibiliClick: () -> Unit = {},
    onEditConnectedEmbyClick: () -> Unit = onEmbyClick,
    onJellyfinClick: () -> Unit = {},
    onFnosClick: () -> Unit = {},
    onMediaClick: () -> Unit = {},
    onFileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var embySession by remember { mutableStateOf(EmbySessionStore.load(context)) }
    var bilibiliSession by remember { mutableStateOf(BilibiliSessionStore.load(context)) }
    var topConnectionType by remember { mutableStateOf(ServerConnectionOrderStore.loadTopType(context)) }

    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            FloatingBottomNav(
                activeItem = BottomNavItem.Server,
                onMediaClick = onMediaClick,
                onFileClick = onFileClick,
                onServerClick = {},
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            TopHeader()
            Spacer(modifier = Modifier.height(24.dp))

            if (embySession != null || bilibiliSession != null) {
                Text(
                    text = "已连接",
                    color = TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                    letterSpacing = 0.sp
                )

                connectedServerOrder(
                    embySession = embySession,
                    bilibiliSession = bilibiliSession,
                    topType = topConnectionType
                ).forEach { type ->
                    when (type) {
                        MediaSourceType.Emby -> embySession?.let { session ->
                            ConnectedEmbyCard(
                                session = session,
                                onEditClick = onEditConnectedEmbyClick,
                                onDeleteClick = {
                                    EmbySessionStore.clear(context)
                                    embySession = null
                                    if (MediaSourceSelectionStore.load(context) == MediaSourceType.Emby) {
                                        MediaSourceSelectionStore.save(context, MediaSourceType.Bilibili)
                                    }
                                    Toast.makeText(context, "已删除 Emby 连接", Toast.LENGTH_SHORT).show()
                                },
                                onMoveToTopClick = {
                                    topConnectionType = MediaSourceType.Emby
                                    ServerConnectionOrderStore.saveTopType(context, MediaSourceType.Emby)
                                    Toast.makeText(context, "已移至顶部", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        MediaSourceType.Bilibili -> bilibiliSession?.let { session ->
                            ConnectedBilibiliCard(
                                session = session,
                                onEditClick = onBilibiliClick,
                                onDeleteClick = {
                                    BilibiliSessionStore.clear(context)
                                    bilibiliSession = null
                                    if (MediaSourceSelectionStore.load(context) == MediaSourceType.Bilibili) {
                                        MediaSourceSelectionStore.save(context, MediaSourceType.Emby)
                                    }
                                    Toast.makeText(context, "已删除 Bilibili 连接", Toast.LENGTH_SHORT).show()
                                },
                                onMoveToTopClick = {
                                    topConnectionType = MediaSourceType.Bilibili
                                    ServerConnectionOrderStore.saveTopType(context, MediaSourceType.Bilibili)
                                    Toast.makeText(context, "已移至顶部", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        else -> Unit
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = "连接到...",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                letterSpacing = 0.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    ServerListItem("Emby", Icons.Default.PlayCircle, Color(0xFF43A047), onEmbyClick)
                    AppListDivider()
                    ServerListItem("Bilibili", Icons.Default.PlayCircle, Color(0xFF00A1D6), onBilibiliClick)
                    AppListDivider()
                    ServerListItem("Jellyfin", Icons.Default.Storage, Color(0xFF5E56D8), onJellyfinClick)
                    AppListDivider()
                    ServerListItem("Plex", Icons.Default.VideoLibrary, Color(0xFFFFB300))
                    AppListDivider()
                    ServerListItem("飞牛私有云", Icons.Default.Cloud, Color(0xFF1E88E5), onFnosClick)
                }
            }
        }
    }
}

@Composable
private fun ConnectedEmbyCard(
    session: EmbyAuthSession,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveToTopClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.75.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmbyAvatar(session)
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.serverName.ifBlank { "Emby 媒体库" },
                    color = PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Emby · ${session.username}",
                    color = TextGray,
                    fontSize = 13.sp,
                    letterSpacing = 0.sp
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多操作",
                        tint = TextGray
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("移至顶部") },
                        onClick = {
                            menuExpanded = false
                            onMoveToTopClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedBilibiliCard(
    session: BilibiliSession,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveToTopClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.75.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BilibiliAvatar(session)
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bilibili",
                    color = PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Bilibili · ${session.username}",
                    color = TextGray,
                    fontSize = 13.sp,
                    letterSpacing = 0.sp
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多操作",
                        tint = TextGray
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("重新登录") },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("移至顶部") },
                        onClick = {
                            menuExpanded = false
                            onMoveToTopClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmbyAvatar(session: EmbyAuthSession) {
    RemoteUserAvatar(
        imageUrl = session.embyUserAvatarUrl(),
        fallbackText = session.username.firstOrNull()?.uppercase() ?: "E",
        fallbackColor = Color(0xFF43A047),
        requestHeaders = mapOf("X-Emby-Token" to session.accessToken)
    )
}

@Composable
private fun BilibiliAvatar(session: BilibiliSession) {
    RemoteUserAvatar(
        imageUrl = session.face,
        fallbackText = session.username.firstOrNull()?.uppercase() ?: "B",
        fallbackColor = Color(0xFF00A1D6)
    )
}

@Composable
private fun RemoteUserAvatar(
    imageUrl: String,
    fallbackText: String,
    fallbackColor: Color,
    requestHeaders: Map<String, String> = emptyMap()
) {
    val context = LocalContext.current
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching { loadAvatarBitmap(context, imageUrl, requestHeaders) }.getOrNull()
        }
    }

    val currentBitmap = bitmap
    Box(
        modifier = Modifier
            .size(41.5.dp)
            .clip(CircleShape)
            .background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
private fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "影视服务器",
            color = PrimaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = PrimaryText
            )
        }
    }
}

@Composable
private fun ServerListItem(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            color = PrimaryText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            letterSpacing = 0.sp
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "进入$title",
            tint = Color(0xFFC7C7CC),
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun EmbyAuthSession.embyUserAvatarUrl(): String {
    val tagQuery = userPrimaryImageTag.takeIf { it.isNotBlank() }?.let { "&tag=$it" }.orEmpty()
    return "$serverUrl/Users/$userId/Images/Primary?fillHeight=96&quality=90$tagQuery&api_key=$accessToken"
}

private fun loadAvatarBitmap(
    context: Context,
    imageUrl: String,
    requestHeaders: Map<String, String>
): Bitmap? {
    if (imageUrl.isBlank()) return null
    val file = avatarCacheFile(context, imageUrl)
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
        setRequestProperty("Referer", "https://www.bilibili.com")
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

private fun avatarCacheFile(context: Context, imageUrl: String): File {
    val directory = File(context.applicationContext.cacheDir, "server_avatar_cache")
    return File(directory, "${md5(imageUrl)}.img")
}

private fun md5(value: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

private fun connectedServerOrder(
    embySession: EmbyAuthSession?,
    bilibiliSession: BilibiliSession?,
    topType: MediaSourceType?
): List<MediaSourceType> {
    val available = buildList {
        if (embySession != null) add(MediaSourceType.Emby)
        if (bilibiliSession != null) add(MediaSourceType.Bilibili)
    }
    val top = topType?.takeIf { it in available }
    return buildList {
        if (top != null) add(top)
        available.filterNot { it == top }.forEach { add(it) }
    }
}

private object ServerConnectionOrderStore {
    private const val PREFERENCES_NAME = "server_connection_order"

    fun loadTopType(context: Context): MediaSourceType? {
        val value = context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString("topType", null)
        return value?.let { runCatching { MediaSourceType.valueOf(it) }.getOrNull() }
    }

    fun saveTopType(context: Context, type: MediaSourceType) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("topType", type.name)
            .apply()
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaServerScreenPreview() {
    VideoHubTheme {
        MediaServerScreen()
    }
}
