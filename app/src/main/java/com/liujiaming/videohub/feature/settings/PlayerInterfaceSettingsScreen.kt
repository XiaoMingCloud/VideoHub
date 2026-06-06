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

@Composable
fun PlayerInterfaceSettingsScreen(onBackClick: () -> Unit) {
    val showAutoHideControlPanelDialog = remember { mutableStateOf(false) }
    val showProgressRightTimeDialog = remember { mutableStateOf(false) }
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

@Composable
private fun ItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Color(0xFFF0F0F0)
    )
}

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
            .padding(horizontal = 16.dp, vertical = 18.dp),
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
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
