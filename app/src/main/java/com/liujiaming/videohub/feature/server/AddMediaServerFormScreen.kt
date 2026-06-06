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

data class MediaServerFormState(
    val name: String,
    val protocol: String,
    val address: String,
    val port: String,
    val username: String,
    val password: String
)

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
    var name by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("HTTP") }
    var address by remember { mutableStateOf("") }
    var port by remember { mutableStateOf(defaultPort) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
            MediaServerFormTopBar(
                title = "添加 $serverName",
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(20.dp))

            FormInputField(label = "名称", value = name, onValueChange = { name = it }, hint = "选填(自动获取)")
            FormInputField(label = "协议", value = protocol, onValueChange = { protocol = it })
            FormInputField(label = "服务器地址", value = address, onValueChange = { address = it }, hint = "127.0.0.1/sample.com")
            FormInputField(label = "端口号", value = port, onValueChange = { port = it })
            FormInputField(label = "用户名", value = username, onValueChange = { username = it }, hint = "必填")
            FormInputField(label = "密码", value = password, onValueChange = { password = it }, hint = "选填", isPassword = true)

            Spacer(modifier = Modifier.height(32.dp))

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

@Composable
private fun FormInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = PrimaryText,
            fontSize = 16.sp,
            modifier = Modifier.width(84.dp),
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(39.dp)
                .background(FormInputBackground, RoundedCornerShape(8.dp))
                .border(1.dp, FormBorderGray, RoundedCornerShape(8.dp))
                .padding(start = 12.dp, end = if (isPassword) 42.dp else 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = PrimaryText,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp
                ),
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (value.isEmpty()) {
                Text(
                    text = hint,
                    color = FormHintGray,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp
                )
            }

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
