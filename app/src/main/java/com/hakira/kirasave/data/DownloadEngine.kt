package com.hakira.kirasave.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class DownloadEngine(
    private val context: Context,
    private val dao: DownloadDao,
    private val settings: SettingsRepository,
    private val client: OkHttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val active = ConcurrentHashMap<Long, Job>()
    private val calls = ConcurrentHashMap<Long, Call>()
    private val pumpLock = Mutex()
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 12)
    val messages = _messages.asSharedFlow()

    init {
        scope.launch {
            dao.pauseInterrupted()
        }
        registerNetworkCallback()
    }

    suspend fun enqueue(media: TikTokMedia, quality: DownloadQuality): Int {
        if (media.isPhotoPost || quality == DownloadQuality.PHOTO) {
            return enqueuePhotos(media)
        }

        val url = when (quality) {
            DownloadQuality.HD -> media.hdUrl ?: media.sdUrl
            DownloadQuality.SD -> media.sdUrl ?: media.hdUrl
            DownloadQuality.AUDIO -> media.audioUrl
            DownloadQuality.PHOTO -> null
        }
        if (url.isNullOrBlank()) {
            _messages.tryEmit(
                if (quality == DownloadQuality.AUDIO) {
                    "Audio tidak tersedia untuk postingan ini."
                } else {
                    "Media tidak tersedia untuk kualitas ini."
                }
            )
            return 0
        }

        val existing = dao.findCompleted(media.videoId, quality.name)
        if (existing != null) {
            _messages.tryEmit("Media ini sudah ada di koleksi.")
            return 0
        }

        val isAudio = quality == DownloadQuality.AUDIO
        val extension = if (isAudio) "mp3" else "mp4"
        val mime = if (isAudio) "audio/mpeg" else "video/mp4"
        val base = cleanFileName(
            "@${media.author.handle.ifBlank { "tiktok" }} ${media.title}",
            "KiraSave"
        )
        val entity = DownloadEntity(
            videoId = media.videoId,
            sourceUrl = media.sourceUrl,
            title = media.title,
            authorHandle = media.author.handle,
            authorName = media.author.nickname,
            coverUrl = media.coverUrl,
            mediaType = if (isAudio) MediaKind.AUDIO.name else MediaKind.VIDEO.name,
            quality = quality.name,
            mediaUrl = url,
            fileName = "${base}_${media.videoId}.$extension",
            mimeType = mime,
            sizeBytes = when (quality) {
                DownloadQuality.HD -> media.hdSize.takeIf { it > 0 } ?: media.sdSize
                DownloadQuality.SD -> media.sdSize
                else -> 0L
            }
        )
        val id = dao.insert(entity)
        pump()
        _messages.tryEmit("Ditambahkan ke antrean unduhan.")
        return if (id > 0) 1 else 0
    }

    private suspend fun enqueuePhotos(media: TikTokMedia): Int {
        if (media.imageUrls.isEmpty()) {
            _messages.tryEmit("Tidak ada foto pada postingan ini.")
            return 0
        }
        var inserted = 0
        val total = media.imageUrls.size
        media.imageUrls.forEachIndexed { index, imageUrl ->
            val photoId = "${media.videoId}-photo-${index + 1}"
            if (dao.findCompleted(photoId, DownloadQuality.PHOTO.name) != null) {
                return@forEachIndexed
            }
            val base = cleanFileName(
                "@${media.author.handle.ifBlank { "tiktok" }} ${media.title}",
                "KiraSave"
            )
            val entity = DownloadEntity(
                videoId = "${media.videoId}-photo-${index + 1}",
                sourceUrl = media.sourceUrl,
                title = if (total > 1) "${media.title} (${index + 1}/$total)" else media.title,
                authorHandle = media.author.handle,
                authorName = media.author.nickname,
                coverUrl = media.coverUrl,
                mediaType = MediaKind.PHOTO.name,
                quality = DownloadQuality.PHOTO.name,
                mediaUrl = imageUrl,
                fileName = "${base}_${media.videoId}_${index + 1}.jpg",
                mimeType = "image/jpeg"
            )
            dao.insert(entity)
            inserted++
        }
        if (inserted > 0) {
            pump()
            _messages.tryEmit("$inserted foto ditambahkan ke antrean.")
        }
        return inserted
    }

    fun pause(id: Long) {
        scope.launch {
            dao.setStatus(id, DownloadStatus.PAUSED, null)
            calls.remove(id)?.cancel()
            active.remove(id)?.cancel()
        }
    }

    fun resume(id: Long) {
        scope.launch {
            val entity = dao.byId(id) ?: return@launch
            if (entity.status != DownloadStatus.PAUSED &&
                entity.status != DownloadStatus.FAILED &&
                entity.status != DownloadStatus.CANCELLED
            ) {
                return@launch
            }
            dao.setStatus(id, DownloadStatus.QUEUED, null)
            pump()
        }
    }

    fun retry(id: Long) = resume(id)

    fun cancel(id: Long) {
        scope.launch {
            dao.setStatus(id, DownloadStatus.CANCELLED, null)
            calls.remove(id)?.cancel()
            active.remove(id)?.cancel()
        }
    }

    fun remove(id: Long) {
        scope.launch {
            calls.remove(id)?.cancel()
            active.remove(id)?.cancel()
            val entity = dao.byId(id)
            if (entity?.fileUri != null) MediaStoreSaver.delete(context, entity.fileUri)
            dao.delete(id)
        }
    }

    fun setFavorite(id: Long, favorite: Boolean) {
        scope.launch { dao.setFavorite(id, favorite) }
    }

    fun extractAudio(id: Long) {
        scope.launch {
            val entity = dao.byId(id) ?: return@launch
            if (entity.mediaType != MediaKind.VIDEO.name || entity.fileUri == null) {
                _messages.tryEmit("Audio hanya bisa diekstrak dari video yang sudah diunduh.")
                return@launch
            }
            _messages.tryEmit("Mengekstrak audio…")
            val temp = File(context.cacheDir, "downloads/extract-${System.currentTimeMillis()}.m4a")
            temp.parentFile?.mkdirs()
            val okay = AudioExtractor.extractToM4a(
                context,
                Uri.parse(entity.fileUri),
                temp
            )
            if (!okay || !temp.exists() || temp.length() == 0L) {
                temp.delete()
                _messages.tryEmit("Audio dari video ini tidak dapat diekstrak.")
                return@launch
            }
            val displayName = cleanFileName(
                "${entity.authorHandle.ifBlank { "tiktok" }} ${entity.title}",
                "KiraSave"
            ) + ".m4a"
            try {
                val uri = MediaStoreSaver.save(
                    context,
                    temp,
                    displayName,
                    "audio/mp4",
                    SaveCollection.AUDIO
                )
                dao.insert(
                    DownloadEntity(
                        videoId = "${entity.videoId}-m4a",
                        sourceUrl = entity.sourceUrl,
                        title = "${entity.title} (audio)",
                        authorHandle = entity.authorHandle,
                        authorName = entity.authorName,
                        coverUrl = entity.coverUrl,
                        mediaType = MediaKind.AUDIO.name,
                        quality = DownloadQuality.AUDIO.name,
                        mediaUrl = entity.mediaUrl,
                        fileName = displayName,
                        mimeType = "audio/mp4",
                        sizeBytes = temp.length(),
                        bytesDownloaded = temp.length(),
                        status = DownloadStatus.COMPLETED,
                        fileUri = uri.toString(),
                        completedAt = System.currentTimeMillis()
                    )
                )
                _messages.tryEmit("Audio M4A disimpan ke Music/KiraSave.")
            } catch (t: Throwable) {
                _messages.tryEmit(t.message ?: "Gagal menyimpan audio.")
            } finally {
                temp.delete()
            }
        }
    }

    private fun registerNetworkCallback() {
        runCatching {
            val manager = context.getSystemService(ConnectivityManager::class.java)
            manager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch {
                        val wifiOnly = settings.settings.first().wifiOnly
                        if (!wifiOnly || isWifiConnected()) {
                            pump()
                        }
                    }
                }
            })
        }
    }

    private suspend fun pump() {
        pumpLock.withLock {
            val prefs = settings.settings.first()
            if (prefs.wifiOnly && !isWifiConnected()) {
                _messages.tryEmit("Menunggu koneksi WiFi sesuai pengaturan.")
                return
            }
            val queued = dao.byStatus(DownloadStatus.QUEUED, limit = 25)
            if (queued.isEmpty()) {
                if (active.isEmpty()) DownloadService.stop(context)
                return
            }
            for (entity in queued) {
                if (active.size >= prefs.maxConcurrent) break
                if (!active.containsKey(entity.id)) {
                    DownloadService.start(context)
                    launchDownload(entity)
                }
            }
        }
    }

    private fun launchDownload(entity: DownloadEntity) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            runDownload(entity)
        }
        active[entity.id] = job
        job.invokeOnCompletion {
            active.remove(entity.id)
            calls.remove(entity.id)
            scope.launch { pump() }
        }
        job.start()
    }

    private suspend fun runDownload(entity: DownloadEntity) {
        val tempDir = File(context.cacheDir, "downloads").apply { mkdirs() }
        val temp = File(tempDir, "download-${entity.id}.part")
        dao.updateProgress(entity.id, DownloadStatus.DOWNLOADING, 0L, entity.sizeBytes, null)
        updateNotification(entity.fileName, 0)

        try {
            val request = Request.Builder()
                .url(entity.mediaUrl)
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://www.tiktok.com/")
                .header("Accept", "*/*")
                .get()
                .build()
            val call = client.newCall(request)
            calls[entity.id] = call
            call.execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body ?: throw IOException("Respons kosong.")
                val total = body.contentLength().takeIf { it > 0 } ?: entity.sizeBytes
                var downloaded = 0L
                var lastUpdate = 0L
                body.byteStream().use { input ->
                    temp.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            val now = System.currentTimeMillis()
                            if (now - lastUpdate > 300L) {
                                lastUpdate = now
                                dao.updateProgress(
                                    entity.id,
                                    DownloadStatus.DOWNLOADING,
                                    downloaded,
                                    total,
                                    null
                                )
                                val percent = if (total > 0) {
                                    ((downloaded * 100) / total).toInt()
                                } else {
                                    0
                                }
                                updateNotification(entity.fileName, percent)
                            }
                        }
                    }
                }
                val size = temp.length()
                val collection = when (entity.mediaType) {
                    MediaKind.PHOTO.name -> SaveCollection.IMAGE
                    MediaKind.AUDIO.name -> SaveCollection.AUDIO
                    else -> SaveCollection.VIDEO
                }
                val uri = MediaStoreSaver.save(
                    context,
                    temp,
                    entity.fileName,
                    entity.mimeType,
                    collection
                )
                dao.markCompleted(entity.id, uri.toString(), size, System.currentTimeMillis())
                if (settings.settings.first().notifications) {
                    DownloadService.complete(context, entity.fileName)
                }
                _messages.tryEmit("${entity.fileName} selesai diunduh.")
            }
        } catch (t: Throwable) {
            val current = dao.byId(entity.id)
            if (current?.status != DownloadStatus.PAUSED &&
                current?.status != DownloadStatus.CANCELLED
            ) {
                dao.setStatus(
                    entity.id,
                    DownloadStatus.FAILED,
                    friendlyDownloadError(t)
                )
            }
        } finally {
            calls.remove(entity.id)
            temp.delete()
        }
    }

    private fun updateNotification(name: String, percent: Int) {
        DownloadService.update(
            context = context,
            title = name,
            progress = percent,
            activeCount = active.size,
            indeterminate = percent <= 0
        )
    }

    private fun isWifiConnected(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun friendlyDownloadError(t: Throwable): String = when (t) {
        is IOException -> "Unduhan terhenti. Periksa koneksi lalu coba lagi."
        else -> t.message ?: "Unduhan gagal."
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
