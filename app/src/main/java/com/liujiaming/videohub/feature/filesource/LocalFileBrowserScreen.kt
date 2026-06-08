package com.liujiaming.videohub.feature.filesource

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.feature.media.MediaBrowseItem
import com.liujiaming.videohub.feature.media.MediaSourceType
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun LocalFileBrowserScreen(
    source: LocalFileSource?,
    onBackClick: () -> Unit,
    onVideoClick: (MediaBrowseItem) -> Unit
) {
    if (source == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("未选择文件源", color = TextGray, fontSize = 14.sp, letterSpacing = 0.sp)
        }
        return
    }

    val context = LocalContext.current
    val treeUri = remember(source.uri) { Uri.parse(source.uri) }
    val rootDocumentId = remember(source.uri) {
        DocumentsContract.getTreeDocumentId(treeUri)
    }
    val pathStack = remember(source.uri) {
        mutableStateListOf(LocalDirectoryPath(rootDocumentId, source.name))
    }
    var entries by remember { mutableStateOf<List<LocalFileEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val currentPath = pathStack.last()

    fun handleBack() {
        if (pathStack.size > 1) {
            pathStack.removeAt(pathStack.lastIndex)
        } else {
            onBackClick()
        }
    }

    BackHandler(onBack = ::handleBack)

    LaunchedEffect(source.uri, currentPath.documentId) {
        isLoading = true
        errorText = null
        val result = withContext(Dispatchers.IO) {
            runCatching { queryLocalDirectory(context, treeUri, currentPath.documentId) }
        }
        result
            .onSuccess { entries = it }
            .onFailure { errorText = it.message ?: "目录读取失败" }
        isLoading = false
    }

    Scaffold(containerColor = PageBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            LocalBrowserTopBar(
                title = currentPath.title,
                onBackClick = ::handleBack
            )

            when {
                isLoading -> BrowserMessage("正在读取目录...")
                errorText != null -> BrowserMessage(errorText.orEmpty())
                entries.isEmpty() -> BrowserMessage("此目录为空")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries, key = { it.documentId }) { entry ->
                        LocalFileEntryCard(
                            entry = entry,
                            onClick = {
                                if (entry.isDirectory) {
                                    pathStack.add(LocalDirectoryPath(entry.documentId, entry.name))
                                } else if (entry.isVideo) {
                                    onVideoClick(entry.toMediaBrowseItem())
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalBrowserTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = PrimaryText)
        }
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
        IconButton(onClick = { }) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "更多", tint = PrimaryText)
        }
    }
}

@Composable
private fun LocalFileEntryCard(
    entry: LocalFileEntry,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = entry.isDirectory || entry.isVideo, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (entry.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = null,
                tint = if (entry.isDirectory) Color(0xFFFFB300) else TextGray,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    color = PrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        entry.isDirectory -> "文件夹"
                        entry.isVideo -> "视频 · ${entry.sizeText}"
                        else -> entry.sizeText
                    },
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

@Composable
private fun BrowserMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = TextGray, fontSize = 14.sp, letterSpacing = 0.sp)
    }
}

private fun queryLocalDirectory(
    context: Context,
    treeUri: Uri,
    documentId: String
): List<LocalFileEntry> {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        OpenableColumns.DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE
    )

    return context.contentResolver.query(childrenUri, projection, null, null, null)
        ?.use { cursor ->
            val documentIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
            buildList {
                while (cursor.moveToNext()) {
                    val childDocumentId = cursor.getString(documentIndex).orEmpty()
                    val childUri = DocumentsContract
                        .buildDocumentUriUsingTree(treeUri, childDocumentId)
                        .toString()
                    val mimeType = cursor.getString(mimeIndex).orEmpty()
                    val isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
                    add(
                        LocalFileEntry(
                            documentId = childDocumentId,
                            uri = childUri,
                            name = cursor.getString(nameIndex).orEmpty().ifBlank { "未命名" },
                            mimeType = mimeType,
                            sizeBytes = cursor.getLongOrNull(sizeIndex),
                            isDirectory = isDirectory
                        )
                    )
                }
            }
        }
        .orEmpty()
        .sortedWith(compareByDescending<LocalFileEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
}

private fun android.database.Cursor.getLongOrNull(index: Int): Long? {
    if (index < 0 || isNull(index)) return null
    return runCatching { getLong(index) }.getOrNull()?.takeIf { it >= 0L }
}

private val LocalFileEntry.sizeText: String
    get() {
        val bytes = sizeBytes ?: return mimeType.ifBlank { "文件" }
        val mb = bytes / 1024f / 1024f
        if (mb < 1024f) return String.format(Locale.US, "%.2f MB", mb)
        return String.format(Locale.US, "%.2f GB", mb / 1024f)
    }

private val LocalFileEntry.isVideo: Boolean
    get() = mimeType.startsWith("video/", ignoreCase = true) ||
        name.substringAfterLast('.', "").lowercase(Locale.US) in videoExtensions

private fun LocalFileEntry.toMediaBrowseItem(): MediaBrowseItem {
    return MediaBrowseItem(
        id = uri,
        name = name,
        type = "Video",
        imageUrl = "",
        subtitle = sizeText,
        sourceType = MediaSourceType.Local
    )
}

private data class LocalDirectoryPath(
    val documentId: String,
    val title: String
)

private data class LocalFileEntry(
    val documentId: String,
    val uri: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long?,
    val isDirectory: Boolean
)

private val videoExtensions = setOf(
    "mp4",
    "mkv",
    "mov",
    "avi",
    "wmv",
    "flv",
    "webm",
    "m4v",
    "3gp",
    "ts",
    "m2ts",
    "mpg",
    "mpeg"
)
