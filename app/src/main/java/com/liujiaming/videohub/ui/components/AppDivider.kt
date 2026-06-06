package com.liujiaming.videohub.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liujiaming.videohub.ui.theme.DividerGray

/**
 * 应用列表分割线组件。
 * 左侧留有 60dp 的内边距，用于对齐列表项中的图标区域，
 * 使用 [DividerGray] 颜色，1dp 厚度。
 */
@Composable
fun AppListDivider() {
    Divider(
        modifier = Modifier.padding(start = 60.dp),  // 左侧缩进，与列表内容对齐
        thickness = 1.dp,                              // 分割线厚度
        color = DividerGray                            // 使用主题感知的分割线颜色
    )
}
