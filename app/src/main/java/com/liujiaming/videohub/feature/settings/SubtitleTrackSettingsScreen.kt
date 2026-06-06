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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.ActiveGreen
import com.liujiaming.videohub.ui.theme.BackgroundGray
import com.liujiaming.videohub.ui.theme.CardBackground
import com.liujiaming.videohub.ui.theme.PrimaryText

@Composable
fun SubtitleTrackSettingsScreen(onBackClick: () -> Unit) {
    val showExternalSubtitleRuleDialog = remember { mutableStateOf(false) }
    val languageDialogTarget = remember { mutableStateOf<LanguageDialogTarget?>(null) }

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
                    SettingValueItem(
                        title = "自动加载外挂字幕规则",
                        value = SettingsMemory.externalSubtitleRule.toExternalSubtitleRuleDisplayText(),
                        onClick = { showExternalSubtitleRuleDialog.value = true }
                    )
                    ItemDivider()
                    SettingSwitchItem(
                        title = "自动加载内置字幕",
                        checked = SettingsMemory.autoLoadBuiltInSubtitle,
                        onCheckedChange = SettingsMemory::updateAutoLoadBuiltInSubtitle
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "自动加载内置字幕语言",
                        value = SettingsMemory.builtInSubtitleLanguage,
                        onClick = { languageDialogTarget.value = LanguageDialogTarget.BuiltInSubtitle }
                    )
                    ItemDivider()
                    SettingValueItem(
                        title = "在线字幕搜索语言",
                        value = SettingsMemory.onlineSubtitleLanguage,
                        onClick = { languageDialogTarget.value = LanguageDialogTarget.OnlineSubtitle }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SubtitleTrackCard {
                    SettingValueItem(
                        title = "默认音轨语言",
                        value = SettingsMemory.defaultAudioLanguage,
                        onClick = { languageDialogTarget.value = LanguageDialogTarget.DefaultAudio }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showExternalSubtitleRuleDialog.value) {
        OptionSelectionDialog(
            title = "自动加载外挂字幕规则",
            options = SubtitleTrackOptions.externalSubtitleRules,
            selectedOption = SettingsMemory.externalSubtitleRule,
            onOptionSelected = { option ->
                SettingsMemory.updateExternalSubtitleRule(option)
                showExternalSubtitleRuleDialog.value = false
            },
            onDismiss = { showExternalSubtitleRuleDialog.value = false }
        )
    }

    languageDialogTarget.value?.let { target ->
        LanguageSelectionDialog(
            title = target.title,
            selectedLanguage = when (target) {
                LanguageDialogTarget.BuiltInSubtitle -> SettingsMemory.builtInSubtitleLanguage
                LanguageDialogTarget.OnlineSubtitle -> SettingsMemory.onlineSubtitleLanguage
                LanguageDialogTarget.DefaultAudio -> SettingsMemory.defaultAudioLanguage
            },
            onLanguageSelected = { language ->
                when (target) {
                    LanguageDialogTarget.BuiltInSubtitle -> SettingsMemory.updateBuiltInSubtitleLanguage(language)
                    LanguageDialogTarget.OnlineSubtitle -> SettingsMemory.updateOnlineSubtitleLanguage(language)
                    LanguageDialogTarget.DefaultAudio -> SettingsMemory.updateDefaultAudioLanguage(language)
                }
                languageDialogTarget.value = null
            },
            onDismiss = { languageDialogTarget.value = null }
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
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.weight(1f, fill = false)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            color = ActiveGreen,
            fontSize = 16.sp,
            textAlign = TextAlign.End,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun String.toExternalSubtitleRuleDisplayText(): String {
    return when (this) {
        "同目录下的同名字幕",
        "同目录下的所有字幕" -> "同目录...字幕"
        else -> this
    }
}

@Composable
private fun OptionSelectionDialog(
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
