package com.liujiaming.videohub.feature.settings

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

// ========================================================================
// 播放设置主页面
// ========================================================================

/**
 * 播放设置页面
 *
 * 包含以下设置项：
 * - **快进/快退间隔**：通过弹窗选择预设时间间隔
 * - **打开网盘视频时**：选择视频打开方式
 * - **继续播放模式**：选择续播行为
 * - **连续播放 / 记住播放进度 / 硬件加速**：开关设置
 * - **默认播放速度 / 字幕偏移**：值显示
 * - **后台继续播放**：开关设置
 * - **自动消除隔行扫描**：开关设置（含说明文字）
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun PlaybackSettingsScreen(onBackClick: () -> Unit) {
    // 快进/快退间隔选择弹窗的目标类型
    val intervalDialogTarget = remember { mutableStateOf<SeekIntervalTarget?>(null) }
    // 网盘视频打开方式弹窗
    val showCloudVideoOpenModeDialog = remember { mutableStateOf(false) }
    // 续播模式弹窗
    val showResumePlaybackModeDialog = remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            PlaybackTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // === 基本播放设置卡片 ===
                PlaybackCard {
                    SettingValueItem(
                        title = "快进间隔",
                        value = SettingsMemory.fastForwardInterval,
                        onClick = { intervalDialogTarget.value = SeekIntervalTarget.FastForward }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "快退间隔",
                        value = SettingsMemory.rewindInterval,
                        onClick = { intervalDialogTarget.value = SeekIntervalTarget.Rewind }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "打开网盘视频时",
                        value = SettingsMemory.cloudVideoOpenMode,
                        onClick = { showCloudVideoOpenModeDialog.value = true }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "继续播放",
                        value = SettingsMemory.resumePlaybackMode,
                        onClick = { showResumePlaybackModeDialog.value = true }
                    )
                    ItemDivider()
                    SettingSwitchItem(
                        title = "连续播放",
                        checked = SettingsMemory.continuousPlayback,
                        onCheckedChange = SettingsMemory::updateContinuousPlayback
                    )
                    ItemDivider()
                    SettingSwitchItem(
                        title = "记住播放进度",
                        checked = SettingsMemory.rememberPlaybackProgress,
                        onCheckedChange = SettingsMemory::updateRememberPlaybackProgress
                    )
                    ItemDivider()
                    SettingSwitchItem(
                        title = "硬件加速",
                        checked = SettingsMemory.hardwareAcceleration,
                        onCheckedChange = SettingsMemory::updateHardwareAcceleration
                    )
                    ItemDivider()
                    SettingValueItem("默认播放速度", SettingsMemory.defaultPlaybackSpeed)
                    ItemDivider()
                    SettingValueItem("字幕偏移", SettingsMemory.subtitleOffset)
                    ItemDivider()
                    SettingSwitchItem(
                        title = "后台继续播放",
                        checked = SettingsMemory.backgroundPlayback,
                        onCheckedChange = SettingsMemory::updateBackgroundPlayback
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === 高级播放设置卡片（隔行扫描消除） ===
                PlaybackCard {
                    SettingSwitchItem(
                        title = "自动消除隔行扫描",
                        checked = SettingsMemory.autoDeinterlace,
                        onCheckedChange = SettingsMemory::updateAutoDeinterlace
                    )
                    // 功能说明文字
                    Text(
                        text = "删除交错视频中的隔行扫描线。可能会影响非交错视频的播放。请谨慎打开。",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        letterSpacing = 0.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 快进/快退间隔选择弹窗
    intervalDialogTarget.value?.let { target ->
        PlaybackOptionDialog(
            title = target.title,
            options = PlaybackOptions.seekIntervals,
            selectedOption = when (target) {
                SeekIntervalTarget.FastForward -> SettingsMemory.fastForwardInterval
                SeekIntervalTarget.Rewind -> SettingsMemory.rewindInterval
            },
            onOptionSelected = { option ->
                when (target) {
                    SeekIntervalTarget.FastForward -> SettingsMemory.updateFastForwardInterval(option)
                    SeekIntervalTarget.Rewind -> SettingsMemory.updateRewindInterval(option)
                }
                intervalDialogTarget.value = null
            },
            onDismiss = { intervalDialogTarget.value = null }
        )
    }

    // 网盘视频打开方式选择弹窗
    if (showCloudVideoOpenModeDialog.value) {
        PlaybackOptionDialog(
            title = "打开网盘视频时",
            options = PlaybackOptions.cloudVideoOpenModes,
            selectedOption = SettingsMemory.cloudVideoOpenMode,
            onOptionSelected = { option ->
                SettingsMemory.updateCloudVideoOpenMode(option)
                showCloudVideoOpenModeDialog.value = false
            },
            onDismiss = { showCloudVideoOpenModeDialog.value = false }
        )
    }

    // 续播模式选择弹窗
    if (showResumePlaybackModeDialog.value) {
        PlaybackOptionDialog(
            title = "继续播放",
            options = PlaybackOptions.resumePlaybackModes,
            selectedOption = SettingsMemory.resumePlaybackMode,
            onOptionSelected = { option ->
                SettingsMemory.updateResumePlaybackMode(option)
                showResumePlaybackModeDialog.value = false
            },
            onDismiss = { showResumePlaybackModeDialog.value = false }
        )
    }
}

// ========================================================================
// 辅助组件
// ========================================================================

/**
 * 播放设置页面顶部导航栏
 *
 * 居中显示"播放"标题，左侧放置返回按钮。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
private fun PlaybackTopBar(onBackClick: () -> Unit) {
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
            text = "播放",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

/**
 * 播放设置卡片容器
 *
 * 圆角卡片样式，用于包裹播放设置项列表。
 *
 * @param content 卡片内容组合
 */
@Composable
private fun PlaybackCard(content: @Composable ColumnScope.() -> Unit) {
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

/** 播放设置项之间的分隔线 */
@Composable
private fun ItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Color(0xFFF0F0F0)
    )
}

/**
 * 值显示型播放设置项
 *
 * 左侧显示标题，右侧以绿色文本显示当前值，点击可触发选择弹窗。
 *
 * @param title 设置项标题
 * @param value 当前设置值文本
 * @param onClick 点击回调（默认空操作）
 */
@Composable
private fun SettingValueItem(
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
            letterSpacing = 0.sp
        )

        Text(
            text = value,
            color = ActiveGreen,
            fontSize = 16.sp,
            letterSpacing = 0.sp
        )
    }
}

/**
 * 播放选项选择弹窗
 *
 * 以列表形式展示播放相关选项，当前选中项以绿色高亮显示。
 *
 * @param title 弹窗标题
 * @param options 可选项列表
 * @param selectedOption 当前选中的选项
 * @param onOptionSelected 选择选项后的回调
 * @param onDismiss 关闭弹窗回调
 */
@Composable
private fun PlaybackOptionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
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
                // 遍历所有选项
                options.forEach { option ->
                    Text(
                        text = option,
                        color = if (option == selectedOption) ActiveGreen else PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = if (option == selectedOption) FontWeight.Medium else FontWeight.Normal,
                        letterSpacing = 0.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 10.5.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * 开关型播放设置项
 *
 * 左侧显示标题文本，右侧显示 Switch 开关。
 *
 * @param title 设置项标题
 * @param checked 当前开关状态
 * @param onCheckedChange 开关状态变更回调
 */
@Composable
private fun SettingSwitchItem(
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
 * 快进/快退间隔目标枚举
 *
 * 用于标识当前弹窗是在设置快进间隔还是快退间隔。
 *
 * @property title 弹窗标题文本
 */
private enum class SeekIntervalTarget(val title: String) {
    /** 快进间隔 */
    FastForward("快进间隔"),
    /** 快退间隔 */
    Rewind("快退间隔")
}
