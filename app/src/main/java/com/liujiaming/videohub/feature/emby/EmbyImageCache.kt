package com.liujiaming.videohub.feature.emby

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Emby 封面图片本地缓存管理器。
 * 将 Emby 服务器的媒体封面图片下载到本地缓存目录，
 * 使用 URL 的 MD5 哈希值作为文件名，避免文件名冲突。
 * 支持按需加载和批量预加载两种模式。
 */
object EmbyImageCache {
    /**
     * 加载指定图片 URL 对应的 Bitmap。
     * 如果本地已有缓存则直接读取，否则先下载再解码。
     *
     * @param context 应用上下文
     * @param imageUrl 图片的完整 URL
     * @return 解码后的 Bitmap，如果 URL 为空或加载失败则返回 null
     */
    fun loadBitmap(context: Context, imageUrl: String): Bitmap? {
        if (imageUrl.isBlank()) return null
        val file = imageFile(context, imageUrl)
        if (!file.exists()) {
            downloadToFile(imageUrl, file)  // 本地无缓存，先下载
        }
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    /**
     * 预加载首页所有媒体项的封面图片。
     * 收集所有非空的图片 URL 并去重，逐个下载到本地缓存。
     * 单个图片下载失败不影响其他图片的预加载。
     *
     * @param context 应用上下文
     * @param home 首页媒体数据
     */
    fun prefetchHomeImages(context: Context, home: EmbyMediaHome) {
        // 收集首页所有媒体项的图片 URL（去重）
        val urls = buildList {
            home.libraries.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            home.resumeItems.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            home.latestItems.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            home.librarySections.forEach { section ->
                section.items.mapNotNullTo(this) { it.imageUrl.takeIf(String::isNotBlank) }
            }
        }.distinct()

        // 逐个下载，单个失败不影响整体
        urls.forEach { imageUrl ->
            runCatching {
                val file = imageFile(context, imageUrl)
                if (!file.exists()) {
                    downloadToFile(imageUrl, file)
                }
            }
        }
    }

    /**
     * 将图片从网络下载到本地文件。
     * 使用临时文件 + 重命名的策略，确保下载完成后文件完整。
     *
     * @param imageUrl 图片 URL
     * @param file 目标本地文件
     * @throws EmbyAuthException 下载失败时抛出
     */
    private fun downloadToFile(imageUrl: String, file: File) {
        file.parentFile?.mkdirs()  // 确保父目录存在
        // 使用临时文件写入，下载完成后再重命名，避免写入中断导致文件损坏
        val tempFile = File(file.parentFile, "${file.name}.tmp")
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000   // 连接超时 10 秒
            readTimeout = 15000      // 读取超时 15 秒
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            connection.disconnect()
            throw EmbyAuthException("下载 Emby 封面失败($responseCode): $imageUrl")
        }

        // 将响应流写入临时文件
        connection.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        connection.disconnect()

        // 临时文件非空时重命名为目标文件，否则删除临时文件
        if (tempFile.length() > 0) {
            tempFile.renameTo(file)
        } else {
            tempFile.delete()
        }
    }

    /**
     * 根据图片 URL 获取对应的本地缓存文件路径。
     * 文件存储在应用缓存目录的 emby_image_cache 子目录下，
     * 文件名为 URL 的 MD5 哈希值。
     *
     * @param context 应用上下文
     * @param imageUrl 图片 URL
     * @return 本地缓存文件对象
     */
    private fun imageFile(context: Context, imageUrl: String): File {
        val directory = File(context.applicationContext.cacheDir, "emby_image_cache")
        return File(directory, "${md5(imageUrl)}.img")
    }

    /**
     * 计算字符串的 MD5 哈希值，返回 32 位十六进制小写字符串。
     * 用于将图片 URL 转换为安全的文件名。
     */
    private fun md5(value: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
