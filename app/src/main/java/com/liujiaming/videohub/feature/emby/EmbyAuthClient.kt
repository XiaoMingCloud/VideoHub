package com.liujiaming.videohub.feature.emby

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object EmbyAuthClient {
    fun authenticate(
        protocol: String,
        address: String,
        port: String,
        username: String,
        password: String,
        deviceId: String
    ): EmbyAuthSession {
        val baseUrl = buildBaseUrl(protocol, address, port)
        val connection = (URL("$baseUrl/Users/AuthenticateByName").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty(
                "X-Emby-Authorization",
                """Emby Client="VideoHub", Device="Android", DeviceId="$deviceId", Version="1.0""""
            )
        }

        val body = JSONObject()
            .put("Username", username)
            .put("Pw", password)
            .toString()

        connection.outputStream.use { output ->
            output.write(body.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        val responseText = readResponseText(connection, responseCode)
        connection.disconnect()

        if (responseCode !in 200..299) {
            throw EmbyAuthException(parseErrorMessage(responseText, responseCode))
        }

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

    private fun readResponseText(connection: HttpURLConnection, responseCode: Int): String {
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }

    private fun parseErrorMessage(responseText: String, responseCode: Int): String {
        return runCatching {
            JSONObject(responseText).optString("Message")
        }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Emby 登录失败($responseCode)"
    }
}

data class EmbyAuthSession(
    val serverName: String,
    val serverId: String,
    val serverUrl: String,
    val userId: String,
    val username: String,
    val userPrimaryImageTag: String,
    val accessToken: String
)

class EmbyAuthException(message: String) : Exception(message)
