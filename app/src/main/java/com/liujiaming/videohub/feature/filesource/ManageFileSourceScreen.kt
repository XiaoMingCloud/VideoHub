package com.liujiaming.videohub.feature.filesource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.components.AppListDivider
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

private val StorageBlue = Color(0xFF1976D2)
private val StoragePurple = Color(0xFF7B61FF)
private val StorageOrange = Color(0xFFFF8A00)

private val storageSections = listOf(
    StorageSection(
        title = "本地存储",
        options = listOf(
            StorageOption("本地目录", Icons.Default.Folder, StorageBlue)
        )
    ),
    StorageSection(
        title = "网络存储",
        options = listOf(
            StorageOption("SMB", Icons.Default.Storage, StoragePurple),
            StorageOption("WebDAV / Alist", Icons.Default.Cloud, ActiveGreen)
        )
    ),
    StorageSection(
        title = "云盘存储",
        options = listOf(
            StorageOption("阿里云盘", Icons.Default.CloudDownload, StorageOrange),
            StorageOption("百度网盘", Icons.Default.CloudDownload, StorageBlue),
            StorageOption("夸克网盘", Icons.Default.CloudDownload, ActiveGreen),
            StorageOption("123云盘", Icons.Default.CloudDownload, StoragePurple),
            StorageOption("天翼云盘", Icons.Default.CloudDownload, StorageOrange),
            StorageOption("迅雷云盘", Icons.Default.CloudDownload, StorageBlue),
            StorageOption("OneDrive", Icons.Default.CloudDownload, StorageBlue),
            StorageOption("Google Drive", Icons.Default.CloudDownload, ActiveGreen),
            StorageOption("Dropbox", Icons.Default.CloudDownload, StoragePurple),
            StorageOption("Premiumize", Icons.Default.CloudDownload, StorageOrange)
        )
    )
)

@Composable
fun ManageFileSourceScreen(onBackClick: () -> Unit) {
    Scaffold(containerColor = PageBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            AddStorageTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundGray)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                storageSections.forEachIndexed { index, section ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    StorageSectionCard(section)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AddStorageTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(PageBackground),
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
private fun StorageSectionCard(section: StorageSection) {
    Column {
        Text(
            text = section.title,
            color = TextGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            section.options.forEachIndexed { index, option ->
                StorageOptionRow(option)
                if (index != section.options.lastIndex) {
                    AppListDivider()
                }
            }
        }
    }
}

@Composable
private fun StorageOptionRow(option: StorageOption) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(option.tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = option.tint,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = option.title,
            color = PrimaryText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            letterSpacing = 0.sp
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Immutable
private data class StorageSection(
    val title: String,
    val options: List<StorageOption>
)

@Immutable
private data class StorageOption(
    val title: String,
    val icon: ImageVector,
    val tint: Color
)
