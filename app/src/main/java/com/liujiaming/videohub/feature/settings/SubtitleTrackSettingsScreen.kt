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

// ========================================================================
// 字幕和音轨设置主页面
// ========================================================================

/**
 * 字幕和音轨设置页面
 *
 * 包含以下设置项：
 * - **自动加载外挂字幕规则**：选择外挂字幕匹配规则
 * - **自动加载内置字幕**：开关设置
 * - **自动加载内置字幕语言**：选择语言
 * - **在线字幕搜索语言**：选择语言
 * - **默认音轨语言**：选择语言
 *
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun SubtitleTrackSettingsScreen(onBackClick: () -> Unit) {
    // 外挂字幕规则选择弹窗
    val showExternalSubtitleRuleDialog = remember { mutableStateOf(false) }
    // 语言选择弹窗的目标类型（内置字幕/在线字幕/默认音轨）
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

                // === 字幕设置卡片 ===
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

                // === 音轨设置卡片 ===
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

    // 外挂字幕规则选择弹窗
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

    // 语言选择弹窗（根据目标类型动态显示不同内容）
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

// ========================================================================
// 辅助组件
// ========================================================================

/**
 * 字幕和音轨设置页面顶部导航栏
 *
 * 居中显示"字幕和音轨"标题，左侧放置返回按钮。
 *
 * @param onBackClick 返回按钮点击回调
 */
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

/**
 * 字幕和音轨设置卡片容器
 *
 * 圆角卡片样式，用于包裹设置项列表。
 *
 * @param content 卡片内容组合
 */
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

/** 字幕和音轨设置项之间的分隔线 */
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
 * 标题和值均限制为单行显示，值过长时以省略号截断。
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

/**
 * 将外挂字幕规则值转换为简短的显示文本
 *
 * 对于较长的规则名（如"同目录下的同名字幕"），截断为"同目录...字幕"以节省空间。
 *
 * @return 截断后的显示文本
 */
private fun String.toExternalSubtitleRuleDisplayText(): String {
    return when (this) {
        "同目录下的同名字幕",
        "同目录下的所有字幕" -> "同目录...字幕"
        else -> this
    }
}

/**
 * 选项选择弹窗
 *
 * 以列表形式展示外挂字幕规则等选项，当前选中项以绿色高亮显示。
 *
 * @param title 弹窗标题
 * @param options 可选项列表
 * @param selectedOption 当前选中的选项
 * @param onOptionSelected 选择选项后的回调
 * @param onDismiss 关闭弹窗回调
 */
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
 * 语言选择弹窗
 *
 * 展示常用语言列表，当前选中语言以绿色高亮显示。
 * 语言列表来源于 [LanguageOptions.commonLanguages]。
 *
 * @param title 弹窗标题
 * @param selectedLanguage 当前选中的语言
 * @param onLanguageSelected 选择语言后的回调
 * @param onDismiss 关闭弹窗回调
 */
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
                // 遍历常用语言列表
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
                            .padding(vertical = 10.5.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * 语言选择弹窗目标枚举
 *
 * 用于标识当前弹窗是在设置哪种语言（内置字幕/在线字幕/默认音轨）。
 *
 * @property title 弹窗标题文本
 */
private enum class LanguageDialogTarget(val title: String) {
    /** 自动加载内置字幕语言 */
    BuiltInSubtitle("自动加载内置字幕语言"),
    /** 在线字幕搜索语言 */
    OnlineSubtitle("在线字幕搜索语言"),
    /** 默认音轨语言 */
    DefaultAudio("默认音轨语言")
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
