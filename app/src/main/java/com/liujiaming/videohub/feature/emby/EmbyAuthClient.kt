package com.liujiaming.videohub.feature.emby

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Emby 认证客户端，负责与 Emby 服务器进行用户认证。
 * 通过 Emby REST API 的 AuthenticateByName 接口实现用户名/密码登录，
 * 返回认证会话信息（包括访问令牌、用户信息、服务器信息等）。
 */
object EmbyAuthClient {
    /**
     * 向 Emby 服务器发起用户认证请求。
     *
     * @param protocol 协议（http 或 https）
     * @param address 服务器地址（IP 或域名）
     * @param port 端口号
     * @param username 用户名
     * @param password 密码
     * @param deviceId 设备唯一标识符
     * @return 认证成功后的会话信息
     * @throws EmbyAuthException 认证失败时抛出
     */
    fun authenticate(
        protocol: String,
        address: String,
        port: String,
        username: String,
        password: String,
        deviceId: String
    ): EmbyAuthSession {
        // 构建服务器基础 URL
        val baseUrl = buildBaseUrl(protocol, address, port)
        // 创建 HTTP 连接，配置请求头和超时参数
        val connection = (URL("$baseUrl/Users/AuthenticateByName").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000       // 连接超时 10 秒
            readTimeout = 15000          // 读取超时 15 秒
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            // Emby 要求的客户端认证头，包含应用名称、设备信息和版本号
            setRequestProperty(
                "X-Emby-Authorization",
                """Emby Client="VideoHub", Device="Android", DeviceId="$deviceId", Version="1.0""""
            )
        }

        // 构建 JSON 请求体，包含用户名和密码
        val body = JSONObject()
            .put("Username", username)
            .put("Pw", password)
            .toString()

        // 发送请求体
        connection.outputStream.use { output ->
            output.write(body.toByteArray(Charsets.UTF_8))
        }

        // 读取响应状态码和响应内容
        val responseCode = connection.responseCode
        val responseText = readResponseText(connection, responseCode)
        connection.disconnect()

        // 非 2xx 状态码视为认证失败
        if (responseCode !in 200..299) {
            throw EmbyAuthException(parseErrorMessage(responseText, responseCode))
        }

        // 解析认证成功后的 JSON 响应，提取用户信息和访问令牌
        val json = JSONObject(responseText)
        val user = json.getJSONObject("User")
        return EmbyAuthSession(
            serverName = json.optString("ServerName").ifBlank { "Emby" },
            serverId = json.optString("ServerId"),
            serverUrl = baseUrl,
            userId = user.optString("Id"),
            username = user.optString("Name", username),
            userPrimaryImageTag = user.optString("PrimaryImageTag"),
            accessToken = json.getString("AccessToken")
        )
    }

    /**
     * 构建服务器基础 URL。
     * 对协议、地址、端口进行标准化处理，去除多余的前缀和后缀。
     *
     * @param protocol 协议（http/https）
     * @param address 服务器地址
     * @param port 端口号（可为空）
     * @return 完整的基础 URL，例如 "http://192.168.1.100:8096"
     */
    private fun buildBaseUrl(protocol: String, address: String, port: String): String {
        val normalizedProtocol = protocol.trim().lowercase().ifBlank { "http" }
            .removeSuffix("://")
        val cleanAddress = address.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
        val cleanPort = port.trim()

        return if (cleanPort.isBlank()) {
            "$normalizedProtocol://$cleanAddress"
        } else {
            "$normalizedProtocol://$cleanAddress:$cleanPort"
        }
    }

    /**
     * 读取 HTTP 响应的文本内容。
     * 根据响应码自动选择 inputStream 或 errorStream。
     *
     * @param connection HTTP 连接对象
     * @param responseCode HTTP 响应码
     * @return 响应体的文本内容
     */
    private fun readResponseText(connection: HttpURLConnection, responseCode: Int): String {
        // 成功时读取 inputStream，失败时读取 errorStream
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }

    /**
     * 从错误响应中解析错误消息。
     * 优先尝试解析 JSON 中的 "Message" 字段，失败时使用默认错误信息。
     *
     * @param responseText 响应体文本
     * @param responseCode HTTP 响应码
     * @return 可读的错误消息
     */
    private fun parseErrorMessage(responseText: String, responseCode: Int): String {
        return runCatching {
            JSONObject(responseText).optString("Message")
        }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Emby 登录失败($responseCode)"
    }
}

/**
 * Emby 认证会话数据，保存认证成功后的所有必要信息。
 *
 * @property serverName 服务器名称
 * @property serverId 服务器唯一标识
 * @property serverUrl 服务器完整 URL
 * @property userId 用户 ID
 * @property username 用户名
 * @property userPrimaryImageTag 用户头像图片标签
 * @property accessToken 访问令牌，用于后续 API 请求的鉴权
 */
data class EmbyAuthSession(
    val serverName: String,
    val serverId: String,
    val serverUrl: String,
    val userId: String,
    val username: String,
    val userPrimaryImageTag: String,
    val accessToken: String
)

/**
 * Emby 认证异常，当认证请求失败时抛出。
 *
 * @param message 错误消息描述
 */
class EmbyAuthException(message: String) : Exception(message)
