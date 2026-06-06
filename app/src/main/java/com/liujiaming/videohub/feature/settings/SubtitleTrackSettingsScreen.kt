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

@Composable
fun SubtitleTrackSettingsScreen(onBackClick: () -> Unit) {
    var builtInSubtitleLanguage by remember { mutableStateOf("简体中文") }
    var onlineSubtitleLanguage by remember { mutableStateOf("简体中文") }
    var defaultAudioLanguage by remember { mutableStateOf("简体中文") }
    var languageDialogTarget by remember { mutableStateOf<LanguageDialogTarget?>(null) }

    Scaffold(containerColor = BackgroundGray) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            SubtitleTrackTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SubtitleTrackCard {
                    SettingValueItem("自动加载外挂字幕规则", "同目录...字幕")
                    ItemDivider()
                    SettingSwitchItem("自动加载内置字幕", true)
                    ItemDivider()
                    SettingValueItem(
                        title = "自动加载内置字幕语言",
                        value = builtInSubtitleLanguage,
                        onClick = { languageDialogTarget = LanguageDialogTarget.BuiltInSubtitle }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "在线字幕搜索语言",
                        value = onlineSubtitleLanguage,
                        onClick = { languageDialogTarget = LanguageDialogTarget.OnlineSubtitle }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SubtitleTrackCard {
                    SettingValueItem(
                        title = "默认音轨语言",
                        value = defaultAudioLanguage,
                        onClick = { languageDialogTarget = LanguageDialogTarget.DefaultAudio }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    languageDialogTarget?.let { target ->
        LanguageSelectionDialog(
            title = target.title,
            selectedLanguage = when (target) {
                LanguageDialogTarget.BuiltInSubtitle -> builtInSubtitleLanguage
                LanguageDialogTarget.OnlineSubtitle -> onlineSubtitleLanguage
                LanguageDialogTarget.DefaultAudio -> defaultAudioLanguage
            },
            onLanguageSelected = { language ->
                when (target) {
                    LanguageDialogTarget.BuiltInSubtitle -> builtInSubtitleLanguage = language
                    LanguageDialogTarget.OnlineSubtitle -> onlineSubtitleLanguage = language
                    LanguageDialogTarget.DefaultAudio -> defaultAudioLanguage = language
                }
                languageDialogTarget = null
            },
            onDismiss = { languageDialogTarget = null }
        )
    }
}

@Composable
private fun SubtitleTrackTopBar(onBackClick: () -> Unit) {
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
            text = "字幕和音轨",
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun SubtitleTrackCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun LanguageSelectionDialog(
    title: String,
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
                LanguageOptions.commonLanguages.forEach { language ->
                    Text(
                        text = language,
                        color = if (language == selectedLanguage) ActiveGreen else PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = if (language == selectedLanguage) FontWeight.Medium else FontWeight.Normal,
                        letterSpacing = 0.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

private enum class LanguageDialogTarget(val title: String) {
    BuiltInSubtitle("自动加载内置字幕语言"),
    OnlineSubtitle("在线字幕搜索语言"),
    DefaultAudio("默认音轨语言")
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
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
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
