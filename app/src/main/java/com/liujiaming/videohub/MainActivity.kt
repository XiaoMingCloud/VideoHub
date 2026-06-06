package com.liujiaming.videohub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.liujiaming.videohub.navigation.VideoHubApp
import com.liujiaming.videohub.ui.theme.VideoHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoHubTheme {
                VideoHubApp()
            }
        }
    }
}
