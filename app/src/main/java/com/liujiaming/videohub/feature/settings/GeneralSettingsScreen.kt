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

private val StepperBackground = Color(0xFFE5E5EA)
private val StepperDivider = Color(0xFFC7C7CC)

@Composable
fun GeneralSettingsScreen(onBackClick: () -> Unit) {
    val showAppearanceDialog = remember { mutableStateOf(false) }
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
            GeneralTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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

                Text(
                    text = "下载",
                    color = TextGray,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(start = 32.dp, top = 24.dp, bottom = 8.dp)
                )

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

@Composable
private fun ItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Color(0xFFF0F0F0)
    )
}

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
            Text(
                text = value.toString(),
                color = TextGray,
                fontSize = 16.sp,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Row(
                modifier = Modifier
                    .height(28.25.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StepperBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(14.15.dp)
                        .background(StepperDivider)
                )

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
