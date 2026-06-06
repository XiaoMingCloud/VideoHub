package com.liujiaming.videohub.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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

// ========================================================================
// 播放器界面设置主页面
// ========================================================================

/**
 * 播放器界面设置页面
 *
 * 包含以下设置项：
 * - **自动隐藏播放控制面板**：选择面板自动隐藏的时长
 * - **进度条右侧时间**：选择时间显示模式
 * - **长按倍速播放**：选择长按时的播放倍速
 * - **双击屏幕两侧快进/快退**：开关设置
 * - **默认横向显示**：开关设置
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun PlayerInterfaceSettingsScreen(onBackClick: () -> Unit) {
    // 自动隐藏控制面板时长选择弹窗
    val showAutoHideControlPanelDialog = remember { mutableStateOf(false) }
    // 进度条右侧时间显示模式弹窗
    val showProgressRightTimeDialog = remember { mutableStateOf(false) }
    // 长按倍速播放速度选择弹窗
    val showLongPressSpeedDialog = remember { mutableStateOf(false) }

    Scaffold(containerColor = BackgroundGray) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            PlayerInterfaceTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // === 播放器界面设置卡片 ===
                PlayerInterfaceCard {
                    SettingValueItem(
                        title = "自动隐藏播放控制面板",
                        value = SettingsMemory.autoHideControlPanel,
                        onClick = { showAutoHideControlPanelDialog.value = true }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "进度条右侧时间",
                        value = SettingsMemory.progressRightTime,
                        onClick = { showProgressRightTimeDialog.value = true }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "长按倍速播放",
                        value = SettingsMemory.longPressSpeed,
                        onClick = { showLongPressSpeedDialog.value = true }
                    )
                    ItemDivider()
                    SettingSwitchItem(
                        title = "双击屏幕两侧快进/快退",
                        checked = SettingsMemory.doubleTapSeek,
                        onCheckedChange = SettingsMemory::updateDoubleTapSeek
                    )
                    ItemDivider()
                    SettingSwitchItem(
                        title = "默认横向显示",
                        checked = SettingsMemory.defaultLandscape,
                        onCheckedChange = SettingsMemory::updateDefaultLandscape
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 自动隐藏控制面板时长弹窗
    if (showAutoHideControlPanelDialog.value) {
        PlayerInterfaceOptionDialog(
            title = "自动隐藏播放控制面板",
            options = PlayerInterfaceOptions.autoHideControlPanelDurations,
            selectedOption = SettingsMemory.autoHideControlPanel,
            onOptionSelected = { option ->
                SettingsMemory.updateAutoHideControlPanel(option)
                showAutoHideControlPanelDialog.value = false
            },
            onDismiss = { showAutoHideControlPanelDialog.value = false }
        )
    }

    // 进度条右侧时间显示模式弹窗
    if (showProgressRightTimeDialog.value) {
        PlayerInterfaceOptionDialog(
            title = "进度条右侧时间",
            options = PlayerInterfaceOptions.progressRightTimeModes,
            selectedOption = SettingsMemory.progressRightTime,
            onOptionSelected = { option ->
                SettingsMemory.updateProgressRightTime(option)
                showProgressRightTimeDialog.value = false
            },
            onDismiss = { showProgressRightTimeDialog.value = false }
        )
    }

    // 长按倍速播放速度选择弹窗
    if (showLongPressSpeedDialog.value) {
        PlayerInterfaceOptionDialog(
            title = "长按倍速播放",
            options = PlayerInterfaceOptions.longPressSpeeds,
            selectedOption = SettingsMemory.longPressSpeed,
            onOptionSelected = { option ->
                SettingsMemory.updateLongPressSpeed(option)
                showLongPressSpeedDialog.value = false
            },
            onDismiss = { showLongPressSpeedDialog.value = false }
        )
    }
}

// ========================================================================
// 辅助组件
// ========================================================================

/**
 * 播放器界面设置页面顶部导航栏
 *
 * 居中显示"播放器界面"标题，左侧放置返回按钮。
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
private fun PlayerInterfaceTopBar(onBackClick: () -> Unit) {
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
            text = "播放器界面",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

/**
 * 播放器界面设置卡片容器
 *
 * 圆角卡片样式，用于包裹设置项列表。
 *
 * @param content 卡片内容组合
 */
@Composable
private fun PlayerInterfaceCard(content: @Composable ColumnScope.() -> Unit) {
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

/** 播放器界面设置项之间的分隔线 */
@Composable
private fun ItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Color(0xFFF0F0F0)
    )
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
 * 播放器界面选项选择弹窗
 *
 * 以列表形式展示选项，当前选中项以绿色高亮显示。
 *
 * @param title 弹窗标题
 * @param options 可选项列表
 * @param selectedOption 当前选中的选项
 * @param onOptionSelected 选择选项后的回调
 * @param onDismiss 关闭弹窗回调
 */
@Composable
private fun PlayerInterfaceOptionDialog(
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
            Column(modifier = Modifier.fillMaxWidth()) {
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
 * 开关型设置项
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
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
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
