package com.liujiaming.videohub.feature.emby

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object EmbyImageCache {
    fun loadBitmap(context: Context, imageUrl: String): Bitmap? {
        if (imageUrl.isBlank()) return null
        val file = imageFile(context, imageUrl)
        if (!file.exists()) {
            downloadToFile(imageUrl, file)
        }
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    fun prefetchHomeImages(context: Context, home: EmbyMediaHome) {
        val urls = buildList {
            home.libraries.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            home.resumeItems.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            home.latestItems.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            home.librarySections.forEach { section ->
                section.items.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            }
        }.distinct()

        urls.forEach { imageUrl ->
            runCatching {
                val file = imageFile(context, imageUrl)
                if (!file.exists()) {
                    downloadToFile(imageUrl, file)
                }
            }
        }
    }

    private fun downloadToFile(imageUrl: String, file: File) {
        file.parentFile?.mkdirs()
        val tempFile = File(file.parentFile, "${file.name}.tmp")
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 15000
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            connection.disconnect()
            throw EmbyAuthException("下载 Emby 封面失败($responseCode): $imageUrl")
        }

        connection.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        connection.disconnect()

        if (tempFile.length() > 0) {
            tempFile.renameTo(file)
        } else {
            tempFile.delete()
        }
    }

    private fun imageFile(context: Context, imageUrl: String): File {
        val directory = File(context.applicationContext.cacheDir, "emby_image_cache")
        return File(directory, "${md5(imageUrl)}.img")
    }

    private fun md5(value: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
