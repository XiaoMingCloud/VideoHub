package com.liujiaming.videohub.feature.filesource

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.FormBorderGray
import com.liujiaming.videohub.ui.theme.FormHintGray
import com.liujiaming.videohub.ui.theme.FormInputBackground
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

@Composable
fun AddLocalStorageScreen(
    onBackClick: () -> Unit,
    onAdded: () -> Unit = onBackClick
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("我的本地目录") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("") }

    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
        selectedUri = uri
        selectedName = resolveDirectoryName(context, uri)
        if (name.isBlank() || name == "我的本地目录") {
            name = selectedName.ifBlank { "我的本地目录" }
        }
    }

    Scaffold(containerColor = PageBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            AddLocalTopBar(onBackClick)

            Spacer(modifier = Modifier.height(24.dp))

            LocalNameField(value = name, onValueChange = { name = it })

            Spacer(modifier = Modifier.height(18.dp))

            DirectoryPickerCard(
                selectedName = selectedName,
                selectedUri = selectedUri?.toString().orEmpty(),
                onPickClick = { directoryPicker.launch(null) }
            )

            Spacer(modifier = Modifier.height(34.dp))

            Button(
                onClick = {
                    val uri = selectedUri
                    if (uri == null) {
                        Toast.makeText(context, "请先选择本地目录", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val displayName = name.trim().ifBlank { selectedName.ifBlank { "我的本地目录" } }
                    LocalFileSourceStore.save(
                        context,
                        LocalFileSource(
                            id = UUID.randomUUID().toString(),
                            name = displayName,
                            uri = uri.toString(),
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    Toast.makeText(context, "已添加本地目录", Toast.LENGTH_SHORT).show()
                    onAdded()
                },
                enabled = selectedUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveGreen,
                    contentColor = Color.White,
                    disabledContainerColor = ActiveGreen.copy(alpha = 0.38f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                elevation = null
            ) {
                Text(
                    text = "添加",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

@Composable
private fun AddLocalTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = PrimaryText
            )
        }

        Text(
            text = "添加存储",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun LocalNameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "名称",
            color = PrimaryText,
            fontSize = 16.sp,
            modifier = Modifier.width(92.dp),
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(FormInputBackground, RoundedCornerShape(8.dp))
                .border(1.dp, FormBorderGray, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = PrimaryText,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (value.isEmpty()) {
                Text(
                    text = "我的本地目录",
                    color = FormHintGray,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

@Composable
private fun DirectoryPickerCard(
    selectedName: String,
    selectedUri: String,
    onPickClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = if (selectedUri.isBlank()) Icons.Default.Folder else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (selectedUri.isBlank()) Color(0xFFFFB300) else ActiveGreen
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedName.ifBlank { "未选择目录" },
                        color = PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedUri.ifBlank { "点击下方按钮打开手机文件管理器" },
                        color = TextGray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onPickClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ActiveGreen)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedUri.isBlank()) "选择本地目录" else "重新选择目录",
                    fontSize = 15.sp,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

private fun resolveDirectoryName(context: android.content.Context, uri: Uri): String {
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull().orEmpty()
    val documentUri = runCatching { DocumentsContract.buildDocumentUriUsingTree(uri, documentId) }.getOrNull()
    val queriedName = documentUri?.let { docUri ->
        runCatching {
            context.contentResolver.query(docUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    } else {
                        null
                    }
                }
        }.getOrNull()
    }
    if (!queriedName.isNullOrBlank()) return queriedName

    val rawName = documentId.substringAfterLast(':').substringAfterLast('/')
    val decoded = runCatching {
        URLDecoder.decode(rawName, StandardCharsets.UTF_8.name())
    }.getOrDefault(rawName)
    return decoded.ifBlank { "内部存储" }
}
