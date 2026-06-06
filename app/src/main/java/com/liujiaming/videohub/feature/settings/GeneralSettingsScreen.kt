package com.liujiaming.videohub.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

// === 颜色常量 ===

/** 步进器按钮背景色 */
private val StepperBackground = Color(0xFFE5E5EA)

/** 步进器中间分隔线颜色 */
private val StepperDivider = Color(0xFFC7C7CC)

// ========================================================================
// 通用设置主页面
// ========================================================================

/**
 * 通用设置页面
 *
 * 包含以下设置分组：
 * - **通用**：隐藏剧透、触感反馈、媒体/文件排序方式、外观、在线影视数据语言
 * - **下载**：蜂窝网络开关、切换提醒、并发数、悬浮窗、播放时暂停
 *
 * 通过弹窗选择外观模式和在线影视数据语言。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun GeneralSettingsScreen(onBackClick: () -> Unit) {
    // 外观选择弹窗显示状态
    val showAppearanceDialog = remember { mutableStateOf(false) }
    // 在线影视数据语言选择弹窗显示状态
    val showOnlineMetadataLanguageDialog = remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // 顶部导航栏
            GeneralTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // === 通用设置分组 ===
                GeneralSettingsCard {
                    SettingsSwitchItem(
                        title = "隐藏剧透",
                        checked = SettingsMemory.hideSpoilers,
                        onCheckedChange = SettingsMemory::updateHideSpoilers
                    )
                    ItemDivider()
                    SettingsSwitchItem(
                        title = "触感反馈",
                        checked = SettingsMemory.hapticFeedback,
                        onCheckedChange = SettingsMemory::updateHapticFeedback
                    )
                    ItemDivider()
                    SettingsValueItem("媒体排序方式", SettingsMemory.mediaSort)
                    ItemDivider()
                    SettingsValueItem("文件排序方式", SettingsMemory.fileSort)
                    ItemDivider()
                    SettingsValueItem(
                        title = "外观",
                        value = SettingsMemory.appearance,
                        onClick = { showAppearanceDialog.value = true }
                    )
                    ItemDivider()
                    SettingsValueItem(
                        title = "在线影视数据语言",
                        value = SettingsMemory.onlineMetadataLanguage,
                        onClick = { showOnlineMetadataLanguageDialog.value = true }
                    )
                }

                // === 下载设置分组标题 ===
                Text(
                    text = "下载",
                    color = TextGray,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(start = 32.dp, top = 24.dp, bottom = 8.dp)
                )

                // === 下载设置分组 ===
                GeneralSettingsCard {
                    SettingsSwitchItem(
                        title = "允许使用蜂窝网络",
                        checked = SettingsMemory.allowCellular,
                        onCheckedChange = SettingsMemory::updateAllowCellular
                    )
                    ItemDivider()
                    SettingsSwitchItem(
                        title = "蜂窝网络切换提醒",
                        checked = SettingsMemory.cellularSwitchReminder,
                        onCheckedChange = SettingsMemory::updateCellularSwitchReminder
                    )
                    ItemDivider()
                    SettingsStepperItem(
                        title = "并发数",
                        value = SettingsMemory.downloadConcurrency,
                        onValueChange = SettingsMemory::updateDownloadConcurrency
                    )
                    ItemDivider()
                    SettingsSwitchItem(
                        title = "显示悬浮窗",
                        checked = SettingsMemory.showFloatingWindow,
                        onCheckedChange = SettingsMemory::updateShowFloatingWindow
                    )
                    ItemDivider()
                    SettingsSwitchItem(
                        title = "播放时暂停",
                        checked = SettingsMemory.pauseWhenPlaying,
                        onCheckedChange = SettingsMemory::updatePauseWhenPlaying
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 外观选择弹窗
    if (showAppearanceDialog.value) {
        AppearanceSelectionDialog(
            selectedAppearance = SettingsMemory.appearance,
            onAppearanceSelected = { appearance ->
                SettingsMemory.updateAppearance(appearance)
                showAppearanceDialog.value = false
            },
            onDismiss = { showAppearanceDialog.value = false }
        )
    }

    // 在线影视数据语言选择弹窗
    if (showOnlineMetadataLanguageDialog.value) {
        LanguageSelectionDialog(
            title = "在线影视数据语言",
            options = MetadataLanguageOptions.languages,
            selectedLanguage = SettingsMemory.onlineMetadataLanguage,
            onLanguageSelected = { language ->
                SettingsMemory.updateOnlineMetadataLanguage(language)
                showOnlineMetadataLanguageDialog.value = false
            },
            onDismiss = { showOnlineMetadataLanguageDialog.value = false }
        )
    }
}

// ========================================================================
// 辅助组件
// ========================================================================

/**
 * 通用设置页面顶部导航栏
 *
 * 居中显示"通用"标题，左侧放置返回按钮。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
private fun GeneralTopBar(onBackClick: () -> Unit) {
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
            text = "通用",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

/**
 * 通用设置卡片容器
 *
 * 圆角卡片样式，用于包裹设置项列表。
 *
 * @param content 卡片内容组合
 */
@Composable
private fun GeneralSettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

/**
 * 设置项之间的分隔线
 */
@Composable
private fun ItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Color(0xFFF0F0F0)
    )
}

/**
 * 开关型设置项
 *
 * 左侧显示标题文本，右侧显示 Switch 开关。
 *
 * @param title 设置项标题
 * @param checked 当前开关状态
 * @param onCheckedChange 开关状态变更回调
 */
@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 16.sp,
            letterSpacing = 0.sp
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ActiveGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD1D1D6),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/**
 * 值显示型设置项
 *
 * 左侧显示标题，右侧以绿色文本显示当前值，点击可触发选择弹窗。
 *
 * @param title 设置项标题
 * @param value 当前设置值文本
 * @param onClick 点击回调（默认空操作）
 */
@Composable
private fun SettingsValueItem(
    title: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.85.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            color = ActiveGreen,
            fontSize = 16.sp,
            letterSpacing = 0.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.25f)
        )
    }
}

/**
 * 外观模式选择弹窗
 *
 * 以列表形式展示所有外观选项（如"跟随系统"、"浅色"、"深色"），
 * 当前选中项以绿色高亮显示。
 *
 * @param selectedAppearance 当前选中的外观模式
 * @param onAppearanceSelected 选择外观模式后的回调
 * @param onDismiss 关闭弹窗回调
 */
@Composable
private fun AppearanceSelectionDialog(
    selectedAppearance: String,
    onAppearanceSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "外观",
                color = PrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 遍历所有外观选项
                AppearanceOptions.modes.forEach { appearance ->
                    Text(
                        text = appearance,
                        color = if (appearance == selectedAppearance) ActiveGreen else PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = if (appearance == selectedAppearance) FontWeight.Medium else FontWeight.Normal,
                        letterSpacing = 0.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppearanceSelected(appearance) }
                            .padding(vertical = 10.5.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * 语言选择弹窗
 *
 * 通用的语言列表选择弹窗，用于选择在线影视数据语言等。
 * 当前选中语言以绿色高亮显示。
 *
 * @param title 弹窗标题
 * @param options 可选语言列表
 * @param selectedLanguage 当前选中的语言
 * @param onLanguageSelected 选择语言后的回调
 * @param onDismiss 关闭弹窗回调
 */
@Composable
private fun LanguageSelectionDialog(
    title: String,
    options: List<String>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = PrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 遍历所有语言选项
                options.forEach { language ->
                    Text(
                        text = language,
                        color = if (language == selectedLanguage) ActiveGreen else PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = if (language == selectedLanguage) FontWeight.Medium else FontWeight.Normal,
                        letterSpacing = 0.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 10.5.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * 步进器型设置项（用于数值调节）
 *
 * 左侧显示标题，右侧显示当前值和增减按钮。
 * 最小值限制为 1。
 *
 * @param title 设置项标题
 * @param value 当前数值
 * @param onValueChange 数值变更回调
 */
@Composable
private fun SettingsStepperItem(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 16.sp,
            letterSpacing = 0.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // 当前数值显示
            Text(
                text = value.toString(),
                color = TextGray,
                fontSize = 16.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // 步进器按钮组（减少 | 增加）
            Row(
                modifier = Modifier
                    .height(28.25.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StepperBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 减少按钮（最小值为1）
                IconButton(
                    onClick = { if (value > 1) onValueChange(value - 1) },
                    modifier = Modifier.width(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "减少",
                        tint = PrimaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 中间分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(14.15.dp)
                        .background(StepperDivider)
                )

                // 增加按钮
                IconButton(
                    onClick = { onValueChange(value + 1) },
                    modifier = Modifier.width(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "增加",
                        tint = PrimaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
