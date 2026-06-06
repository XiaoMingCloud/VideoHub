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

/** 存储选项的蓝色主题色 */
private val StorageBlue = Color(0xFF1976D2)
/** 存储选项的紫色主题色 */
private val StoragePurple = Color(0xFF7B61FF)
/** 存储选项的橙色主题色 */
private val StorageOrange = Color(0xFFFF8A00)

/**
 * 存储分区数据列表，定义所有可添加的存储类型。
 * 分为三大类：本地存储、网络存储、云盘存储。
 */
private val storageSections = listOf(
    // 本地存储分区
    StorageSection(
        title = "本地存储",
        options = listOf(
            StorageOption("本地目录", Icons.Default.Folder, StorageBlue)
        )
    ),
    // 网络存储分区
    StorageSection(
        title = "网络存储",
        options = listOf(
            StorageOption("SMB", Icons.Default.Storage, StoragePurple),
            StorageOption("WebDAV / Alist", Icons.Default.Cloud, ActiveGreen)
        )
    ),
    // 云盘存储分区
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

/**
 * 管理文件源页面，展示所有可添加的存储类型。
 * 存储类型按本地存储、网络存储、云盘存储三个分区展示，
 * 每个分区以卡片形式呈现，点击可进入对应的添加流程。
 *
 * @param onBackClick 返回上一页的回调
 */
@Composable
fun ManageFileSourceScreen(onBackClick: () -> Unit) {
    Scaffold(containerColor = PageBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // 顶部导航栏
            AddStorageTopBar(onBackClick)

            // 存储分区列表，支持垂直滚动
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundGray)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                storageSections.forEachIndexed { index, section ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(14.dp))  // 分区之间的间距
                    }
                    StorageSectionCard(section)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 添加存储页面的顶部导航栏。
 * 左侧显示返回按钮，居中显示"添加存储"标题。
 *
 * @param onBackClick 返回按钮点击回调
 */
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

/**
 * 存储分区卡片组件。
 * 展示分区标题和该分区下的所有存储选项列表。
 *
 * @param section 存储分区数据
 */
@Composable
private fun StorageSectionCard(section: StorageSection) {
    Column {
        // 分区标题（如"本地存储"、"网络存储"、"云盘存储"）
        Text(
            text = section.title,
            color = TextGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // 卡片容器，包含该分区的所有存储选项行
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            section.options.forEachIndexed { index, option ->
                StorageOptionRow(option)
                // 非最后一项时显示分割线
                if (index != section.options.lastIndex) {
                    AppListDivider()
                }
            }
        }
    }
}

/**
 * 存储选项行组件。
 * 由图标（带半透明背景色）、标题文字和右侧箭头组成。
 *
 * @param option 存储选项数据
 */
@Composable
private fun StorageOptionRow(option: StorageOption) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }  // 点击事件（待实现具体逻辑）
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标容器，带半透明背景色和圆角裁剪
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

        // 存储选项标题文字
        Text(
            text = option.title,
            color = PrimaryText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            letterSpacing = 0.sp
        )

        // 右侧箭头图标，指示可点击
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * 存储分区数据模型，包含分区标题和该分区下的存储选项列表。
 *
 * @property title 分区标题
 * @property options 该分区下的存储选项列表
 */
@Immutable
private data class StorageSection(
    val title: String,
    val options: List<StorageOption>
)

/**
 * 存储选项数据模型，定义单个存储类型的展示信息。
 *
 * @property title 存储类型名称
 * @property icon 存储类型图标
 * @property tint 图标和背景的主题色
 */
@Immutable
private data class StorageOption(
    val title: String,
    val icon: ImageVector,
    val tint: Color
)
