package com.liujiaming.videohub.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liujiaming.videohub.ui.theme.DividerGray

@Composable
fun AppListDivider() {
    Divider(
        modifier = Modifier.padding(start = 60.dp),
        thickness = 1.dp,
        color = DividerGray
    )
}
