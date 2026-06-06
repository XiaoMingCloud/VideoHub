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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

@Composable
fun PlaybackSettingsScreen(onBackClick: () -> Unit) {
    var fastForwardInterval by remember { mutableStateOf("10秒") }
    var rewindInterval by remember { mutableStateOf("10秒") }
    var intervalDialogTarget by remember { mutableStateOf<SeekIntervalTarget?>(null) }

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

                PlaybackCard {
                    SettingValueItem(
                        title = "快进间隔",
                        value = fastForwardInterval,
                        onClick = { intervalDialogTarget = SeekIntervalTarget.FastForward }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "快退间隔",
                        value = rewindInterval,
                        onClick = { intervalDialogTarget = SeekIntervalTarget.Rewind }
                    )
                    ItemDivider()
                    SettingValueItem("默认画质", "自动")
                    ItemDivider()
                    SettingValueItem("默认播放速度", "1.0x")
                    ItemDivider()
                    SettingValueItem("字幕偏移", "0秒")
                    ItemDivider()
                    SettingSwitchItem("连续播放", true)
                    ItemDivider()
                    SettingSwitchItem("记住播放进度", true)
                    ItemDivider()
                    SettingSwitchItem("后台继续播放", false)
                }

                Spacer(modifier = Modifier.height(16.dp))

                PlaybackCard {
                    SettingSwitchItem("自动消除隔行扫描", false)
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

    intervalDialogTarget?.let { target ->
        PlaybackOptionDialog(
            title = target.title,
            options = PlaybackOptions.seekIntervals,
            selectedOption = when (target) {
                SeekIntervalTarget.FastForward -> fastForwardInterval
                SeekIntervalTarget.Rewind -> rewindInterval
            },
            onOptionSelected = { option ->
                when (target) {
                    SeekIntervalTarget.FastForward -> fastForwardInterval = option
                    SeekIntervalTarget.Rewind -> rewindInterval = option
                }
                intervalDialogTarget = null
            },
            onDismiss = { intervalDialogTarget = null }
        )
    }
}

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

@Composable
private fun PlaybackCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
    initialState: Boolean
) {
    var checked by remember { mutableStateOf(initialState) }

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
            letterSpacing = 0.sp
        )

        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
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

private enum class SeekIntervalTarget(val title: String) {
    FastForward("快进间隔"),
    Rewind("快退间隔")
}
