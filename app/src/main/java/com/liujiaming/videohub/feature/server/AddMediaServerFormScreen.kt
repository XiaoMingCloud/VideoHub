package com.liujiaming.videohub.feature.server

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liujiaming.videohub.ui.theme.ButtonDisabledGray
import com.liujiaming.videohub.ui.theme.FormBorderGray
import com.liujiaming.videohub.ui.theme.FormHintGray
import com.liujiaming.videohub.ui.theme.FormInputBackground
import com.liujiaming.videohub.ui.theme.PageBackground
import com.liujiaming.videohub.ui.theme.PrimaryText
import com.liujiaming.videohub.ui.theme.TextGray

/**
 * 媒体服务器表单状态数据模型。
 * 封装添加媒体服务器时需要填写的所有表单字段。
 *
 * @property name 服务器名称（选填，可自动获取）
 * @property protocol 协议（HTTP/HTTPS）
 * @property address 服务器地址（IP 或域名）
 * @property port 端口号
 * @property username 用户名
 * @property password 密码
 */
data class MediaServerFormState(
    val name: String,
    val protocol: String,
    val address: String,
    val port: String,
    val username: String,
    val password: String
)

/**
 * 通用媒体服务器添加表单页面。
 * 被 Emby、Jellyfin、FnOS 等不同的服务器类型复用。
 * 包含名称、协议、服务器地址、端口号、用户名、密码六个输入字段，
 * 以及一个提交按钮。
 *
 * @param serverName 服务器类型名称（如 "Emby"、"Jellyfin"）
 * @param defaultPort 默认端口号
 * @param onBackClick 返回上一页的回调
 * @param onSubmit 提交表单时的回调，传入表单状态；为 null 时不显示提交按钮
 * @param submitEnabled 判断提交按钮是否可用的条件函数
 * @param isSubmitting 是否正在提交中（用于显示加载状态）
 * @param submitText 提交按钮的文字
 */
@Composable
fun AddMediaServerFormScreen(
    serverName: String,
    defaultPort: String,
    onBackClick: () -> Unit,
    onSubmit: ((MediaServerFormState) -> Unit)? = null,
    submitEnabled: (MediaServerFormState) -> Boolean = { false },
    isSubmitting: Boolean = false,
    submitText: String = "添加"
) {
    // 各表单字段的响应式状态
    var name by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("HTTP") }
    var address by remember { mutableStateOf("") }
    var port by remember { mutableStateOf(defaultPort) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // 将各字段聚合为表单状态对象
    val formState = MediaServerFormState(
        name = name,
        protocol = protocol,
        address = address,
        port = port,
        username = username,
        password = password
    )

    Scaffold(
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // 顶部导航栏
            MediaServerFormTopBar(
                title = "添加 $serverName",
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 表单输入字段列表
            FormInputField(label = "名称", value = name, onValueChange = { name = it }, hint = "选填(自动获取)")
            FormInputField(label = "协议", value = protocol, onValueChange = { protocol = it })
            FormInputField(label = "服务器地址", value = address, onValueChange = { address = it }, hint = "127.0.0.1/sample.com")
            FormInputField(label = "端口号", value = port, onValueChange = { port = it })
            FormInputField(label = "用户名", value = username, onValueChange = { username = it }, hint = "必填")
            FormInputField(label = "密码", value = password, onValueChange = { password = it }, hint = "选填", isPassword = true)

            Spacer(modifier = Modifier.height(32.dp))

            // 提交按钮，居中显示，提交中时显示"登录中..."
            Button(
                onClick = { onSubmit?.invoke(formState) },
                enabled = !isSubmitting && onSubmit != null && submitEnabled(formState),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(42.25.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.liujiaming.videohub.ui.theme.ActiveGreen,
                    contentColor = Color.White,
                    disabledContainerColor = ButtonDisabledGray,
                    disabledContentColor = Color.White
                ),
                elevation = null
            ) {
                Text(
                    text = if (isSubmitting) "登录中..." else submitText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

/**
 * 媒体服务器表单页面的顶部导航栏。
 * 左侧显示返回按钮，居中显示标题（如"添加 Emby"）。
 *
 * @param title 标题文字
 * @param onBackClick 返回按钮点击回调
 */
@Composable
private fun MediaServerFormTopBar(
    title: String,
    onBackClick: () -> Unit
) {
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
            text = title,
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

/**
 * 表单输入字段组件。
 * 左侧显示标签文字，右侧显示输入框（支持密码类型）。
 * 密码类型的字段额外显示一个可见/隐藏切换按钮。
 *
 * @param label 字段标签文字
 * @param value 当前输入值
 * @param onValueChange 输入值变化的回调
 * @param hint 输入框为空时的提示文字
 * @param isPassword 是否为密码类型字段
 */
@Composable
private fun FormInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    isPassword: Boolean = false
) {
    // 密码可见性切换状态
    var passwordVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧字段标签
        Text(
            text = label,
            color = PrimaryText,
            fontSize = 16.sp,
            modifier = Modifier.width(84.dp),
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 右侧输入框容器，带背景和边框
        Box(
            modifier = Modifier
                .weight(1f)
                .height(39.dp)
                .background(FormInputBackground, RoundedCornerShape(8.dp))
                .border(1.dp, FormBorderGray, RoundedCornerShape(8.dp))
                .padding(start = 12.dp, end = if (isPassword) 42.dp else 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // 基础文本输入框
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = PrimaryText,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp
                ),
                // 密码字段在不可见时使用密码转换
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier.fillMaxWidth()
            )

            // 输入框为空时显示提示文字
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    color = FormHintGray,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp
                )
            }

            // 密码字段的可见性切换按钮
            if (isPassword) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
