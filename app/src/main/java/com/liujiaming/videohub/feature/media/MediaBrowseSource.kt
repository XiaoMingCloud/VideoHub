package com.liujiaming.videohub.feature.media

import android.content.Context
import com.liujiaming.videohub.feature.emby.EmbyHomeCache
import com.liujiaming.videohub.feature.emby.EmbyHomeClient
import com.liujiaming.videohub.feature.emby.EmbyLibraryItemsCache
import com.liujiaming.videohub.feature.emby.EmbyMediaItem
import com.liujiaming.videohub.feature.emby.EmbySessionStore
import com.liujiaming.videohub.feature.bilibili.BilibiliClient
import com.liujiaming.videohub.feature.bilibili.BilibiliHomeCache
import com.liujiaming.videohub.feature.bilibili.BilibiliSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class MediaSourceType {
    Emby,
    Bilibili,
    Local,
    Smb,
    WebDav
}

data class MediaBrowseRequest(
    val sourceType: MediaSourceType,
    val containerId: String,
    val title: String? = null,
    val recursive: Boolean = true,
    val folderContentMode: Boolean = false
)

data class MediaBrowseSeed(
    val title: String,
    val items: List<MediaBrowseItem>,
    val totalRecordCount: Int,
    val isFromDetailCache: Boolean
)

data class MediaBrowsePage(
    val items: List<MediaBrowseItem>,
    val totalRecordCount: Int,
    val returnedItemCount: Int = items.size
)

data class MediaBrowseItem(
    val id: String,
    val name: String,
    val type: String,
    val imageUrl: String,
    val playbackProgress: Float = 0f,
    val played: Boolean = false,
    val subtitle: String = "",
    val sourceType: MediaSourceType = MediaSourceType.Emby
) {
    val isFolder: Boolean
        get() = type.equals("Folder", ignoreCase = true) ||
            type.equals("CollectionFolder", ignoreCase = true)
}

interface MediaBrowseDataSource {
    suspend fun loadSeed(context: Context, request: MediaBrowseRequest): MediaBrowseSeed

    suspend fun fetchPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage

    suspend fun fetchFolderPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage

    suspend fun savePage(
        context: Context,
        request: MediaBrowseRequest,
        items: List<MediaBrowseItem>,
        totalRecordCount: Int
    )
}

object MediaBrowseDataSources {
    fun forType(type: MediaSourceType): MediaBrowseDataSource {
        return when (type) {
            MediaSourceType.Emby -> EmbyMediaBrowseDataSource
            MediaSourceType.Bilibili -> BilibiliMediaBrowseDataSource
            MediaSourceType.Local,
            MediaSourceType.Smb,
            MediaSourceType.WebDav -> UnsupportedMediaBrowseDataSource(type)
        }
    }
}

private object BilibiliMediaBrowseDataSource : MediaBrowseDataSource {
    override suspend fun loadSeed(
        context: Context,
        request: MediaBrowseRequest
    ): MediaBrowseSeed = withContext(Dispatchers.IO) {
        val session = BilibiliSessionStore.load(context)
            ?: error("请先扫码登录 Bilibili")
        val folders = BilibiliHomeCache.load(context, session.mid)?.home?.libraries
            ?: BilibiliClient.fetchFavoriteFolders(session)
        val folder = folders.firstOrNull { it.id == request.containerId }
        MediaBrowseSeed(
            title = request.title ?: folder?.name ?: "Bilibili 收藏",
            items = emptyList(),
            totalRecordCount = folder?.itemCount ?: 0,
            isFromDetailCache = false
        )
    }

    override suspend fun fetchPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage = withContext(Dispatchers.IO) {
        val session = BilibiliSessionStore.load(context)
            ?: error("请先扫码登录 Bilibili")
        val page = BilibiliClient.fetchFavoriteResources(
            session = session,
            mediaId = request.containerId,
            startIndex = startIndex,
            limit = limit
        )
        MediaBrowsePage(
            items = page.items.map { it.toBrowseItem(MediaSourceType.Bilibili) },
            totalRecordCount = page.totalRecordCount,
            returnedItemCount = page.items.size
        )
    }

    override suspend fun fetchFolderPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage {
        return MediaBrowsePage(emptyList(), 0, 0)
    }

    override suspend fun savePage(
        context: Context,
        request: MediaBrowseRequest,
        items: List<MediaBrowseItem>,
        totalRecordCount: Int
    ) = Unit
}

private object EmbyMediaBrowseDataSource : MediaBrowseDataSource {
    override suspend fun loadSeed(
        context: Context,
        request: MediaBrowseRequest
    ): MediaBrowseSeed = withContext(Dispatchers.IO) {
        val session = EmbySessionStore.load(context)
            ?: error("请先登录媒体服务器")
        val cachedHome = EmbyHomeCache.load(context, session.userId)?.home
        val librarySummary = cachedHome?.libraries?.firstOrNull { it.id == request.containerId }
        val homeSection = cachedHome?.librarySections?.firstOrNull { it.libraryId == request.containerId }
        val cachedPage = EmbyLibraryItemsCache.load(context, session.userId, request.containerId)
        val hasCachedPage = cachedPage?.items?.isNotEmpty() == true
        val seedItems = cachedPage?.items?.takeIf { it.isNotEmpty() } ?: homeSection?.items.orEmpty()

        MediaBrowseSeed(
            title = request.title ?: librarySummary?.name ?: homeSection?.title ?: "媒体内容",
            items = seedItems.map { it.toBrowseItem() },
            totalRecordCount = cachedPage?.totalRecordCount?.takeIf { it > 0 }
                ?: librarySummary?.itemCount
                ?: seedItems.size,
            isFromDetailCache = hasCachedPage
        )
    }

    override suspend fun fetchPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage = withContext(Dispatchers.IO) {
        val session = EmbySessionStore.load(context)
            ?: error("请先登录媒体服务器")
        val page = EmbyHomeClient.fetchLibraryItemsPage(
            session = session,
            parentId = request.containerId,
            recursive = request.recursive,
            startIndex = startIndex,
            limit = limit
        )
        MediaBrowsePage(
            items = page.items.map { it.toBrowseItem() },
            totalRecordCount = page.totalRecordCount,
            returnedItemCount = page.items.size
        )
    }

    override suspend fun fetchFolderPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage = withContext(Dispatchers.IO) {
        val session = EmbySessionStore.load(context)
            ?: error("请先登录媒体服务器")
        if (request.folderContentMode) {
            val page = EmbyHomeClient.fetchLibraryItemsPage(
                session = session,
                parentId = request.containerId,
                recursive = true,
                startIndex = startIndex,
                limit = limit
            )
            MediaBrowsePage(
                items = page.items
                    .map { it.toBrowseItem() }
                    .filterNot { it.id == request.containerId }
                    .withFolderPreviewImages(session),
                totalRecordCount = page.totalRecordCount,
                returnedItemCount = page.items.size
            )
        } else {
            val page = EmbyHomeClient.fetchDirectChildrenPage(
                session = session,
                parentId = request.containerId,
                startIndex = startIndex,
                limit = limit
            )
            MediaBrowsePage(
                items = page.items
                    .map { it.toBrowseItem() }
                    .filter { it.isFolder }
                    .withFolderPreviewImages(session),
                totalRecordCount = page.totalRecordCount,
                returnedItemCount = page.items.size
            )
        }
    }

    override suspend fun savePage(
        context: Context,
        request: MediaBrowseRequest,
        items: List<MediaBrowseItem>,
        totalRecordCount: Int
    ) = withContext(Dispatchers.IO) {
        val session = EmbySessionStore.load(context) ?: return@withContext
        EmbyLibraryItemsCache.save(
            context = context,
            userId = session.userId,
            libraryId = request.containerId,
            items = items.map { it.toEmbyItem() },
            totalRecordCount = totalRecordCount
        )
    }
}

private class UnsupportedMediaBrowseDataSource(
    private val sourceType: MediaSourceType
) : MediaBrowseDataSource {
    override suspend fun loadSeed(context: Context, request: MediaBrowseRequest): MediaBrowseSeed {
        return MediaBrowseSeed(
            title = request.title ?: "文件夹",
            items = emptyList(),
            totalRecordCount = 0,
            isFromDetailCache = false
        )
    }

    override suspend fun fetchPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage {
        error("${sourceType.name} 文件源浏览实现还未接入")
    }

    override suspend fun fetchFolderPage(
        context: Context,
        request: MediaBrowseRequest,
        startIndex: Int,
        limit: Int
    ): MediaBrowsePage {
        error("${sourceType.name} 文件源文件夹浏览实现还未接入")
    }

    override suspend fun savePage(
        context: Context,
        request: MediaBrowseRequest,
        items: List<MediaBrowseItem>,
        totalRecordCount: Int
    ) = Unit
}

private fun EmbyMediaItem.toBrowseItem(sourceTypeOverride: MediaSourceType? = null): MediaBrowseItem {
    return MediaBrowseItem(
        id = id,
        name = name,
        type = type,
        imageUrl = imageUrl,
        playbackProgress = playbackProgress,
        played = played,
        subtitle = subtitle,
        sourceType = sourceTypeOverride ?: runCatching { MediaSourceType.valueOf(sourceType) }
            .getOrDefault(MediaSourceType.Emby)
    )
}

private fun MediaBrowseItem.toEmbyItem(): EmbyMediaItem {
    return EmbyMediaItem(
        id = id,
        name = name,
        type = type,
        imageUrl = imageUrl,
        playbackProgress = playbackProgress,
        played = played,
        subtitle = subtitle,
        sourceType = sourceType.name
    )
}

private fun List<MediaBrowseItem>.withFolderPreviewImages(
    session: com.liujiaming.videohub.feature.emby.EmbyAuthSession
): List<MediaBrowseItem> {
    return map { item ->
        if (!item.isFolder) {
            item
        } else {
            val firstVideo = runCatching {
                EmbyHomeClient.fetchFirstVideoInFolder(session, item.id)
            }.getOrNull()
            firstVideo?.imageUrl?.takeIf { it.isNotBlank() }?.let { previewImage ->
                item.copy(imageUrl = previewImage)
            } ?: item
        }
    }
}
