package com.hakira.kirasave.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hakira.kirasave.AppContainer
import com.hakira.kirasave.data.AppSettings
import com.hakira.kirasave.data.DownloadEntity
import com.hakira.kirasave.data.DownloadQuality
import com.hakira.kirasave.data.DownloadStatus
import com.hakira.kirasave.data.TikTokMedia
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLDecoder

data class BatchItem(
    val url: String,
    val loading: Boolean = true,
    val media: TikTokMedia? = null,
    val error: String? = null
)

data class StoriesState(
    val loading: Boolean = false,
    val query: String = "",
    val items: List<TikTokMedia> = emptyList(),
    val error: String? = null,
    val attempted: Boolean = false
)

class MainViewModel(private val container: AppContainer) : ViewModel() {
    private val dao = container.database.downloadDao()
    private val repository = container.repository
    private val engine = container.downloads
    private val settingsRepository = container.settings

    val settings: StateFlow<AppSettings?> = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val downloads: StateFlow<List<DownloadEntity>> = dao.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val completed: StateFlow<List<DownloadEntity>> = dao.observeCompleted().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val favorites: StateFlow<List<DownloadEntity>> = dao.observeFavorites().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val activeDownloads: StateFlow<List<DownloadEntity>> = dao.observeActive().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val totalBytes: StateFlow<Long> = dao.observeTotalBytes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0L
    )

    private val _input = MutableStateFlow("")
    val input = _input

    private val _analyzing = MutableStateFlow(false)
    val analyzing = _analyzing

    private val _analysis = MutableStateFlow<TikTokMedia?>(null)
    val analysis = _analysis

    private val _analyzeError = MutableStateFlow<String?>(null)
    val analyzeError = _analyzeError

    private val _batchItems = MutableStateFlow<List<BatchItem>>(emptyList())
    val batchItems = _batchItems

    private val _batchRunning = MutableStateFlow(false)
    val batchRunning = _batchRunning

    private val _stories = MutableStateFlow(StoriesState())
    val stories = _stories

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 12)
    val events = _events.asSharedFlow()

    private val _sharedLink = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sharedLink = _sharedLink.asSharedFlow()

    init {
        viewModelScope.launch {
            engine.messages.collect { _events.emit(it) }
        }
    }

    fun setInput(value: String) {
        _input.value = value
        if (_analyzeError.value != null) _analyzeError.value = null
    }

    fun analyze(onSuccess: (() -> Unit)? = null) {
        val value = _input.value.trim()
        if (value.isBlank()) {
            _analyzeError.value = "Tempel link TikTok terlebih dahulu."
            return
        }
        if (_analyzing.value) return
        viewModelScope.launch {
            _analyzing.value = true
            _analyzeError.value = null
            repository.analyze(value)
                .onSuccess {
                    _analysis.value = it
                    onSuccess?.invoke()
                }
                .onFailure {
                    _analyzeError.value = it.message ?: "Video tidak dapat dianalisis."
                }
            _analyzing.value = false
        }
    }

    fun analyzeDirect(url: String, onSuccess: (() -> Unit)? = null) {
        _input.value = url
        analyze(onSuccess)
    }

    fun clearAnalysis() {
        _analysis.value = null
        _analyzeError.value = null
    }

    fun download(quality: DownloadQuality, onQueued: (() -> Unit)? = null) {
        val media = _analysis.value ?: return
        enqueue(media, quality, onQueued)
    }

    fun enqueue(media: TikTokMedia, quality: DownloadQuality, onQueued: (() -> Unit)? = null) {
        viewModelScope.launch {
            val count = engine.enqueue(media, quality)
            if (count > 0) onQueued?.invoke()
        }
    }

    fun pause(id: Long) = engine.pause(id)
    fun resume(id: Long) = engine.resume(id)
    fun retry(id: Long) = engine.retry(id)
    fun cancel(id: Long) = engine.cancel(id)
    fun remove(id: Long) = engine.remove(id)
    fun setFavorite(id: Long, favorite: Boolean) = engine.setFavorite(id, favorite)
    fun extractAudio(id: Long) = engine.extractAudio(id)

    fun analyzeBatch(raw: String) {
        if (_batchRunning.value) return
        val matches = Regex("""(?:https?://)?(?:www\.|m\.|vm\.|vt\.)?tiktok\.com/\S+""")
            .findAll(raw)
            .map { it.value.trim().trimEnd('.', ',', ')', ']', '"', '\'') }
            .map { if (it.startsWith("http")) it else "https://$it" }
            .distinct()
            .take(20)
            .toList()
        if (matches.isEmpty()) {
            _events.tryEmit("Tidak ada link TikTok yang terbaca.")
            return
        }
        _batchItems.value = matches.map { BatchItem(it) }
        viewModelScope.launch {
            _batchRunning.value = true
            matches.forEachIndexed { index, url ->
                val result = repository.analyze(url)
                _batchItems.value = _batchItems.value.mapIndexed { itemIndex, item ->
                    if (itemIndex != index) item
                    else result.fold(
                        onSuccess = { item.copy(loading = false, media = it, error = null) },
                        onFailure = {
                            item.copy(
                                loading = false,
                                media = null,
                                error = it.message ?: "Gagal dianalisis"
                            )
                        }
                    )
                }
            }
            _batchRunning.value = false
            val ok = _batchItems.value.count { it.media != null }
            _events.tryEmit("$ok dari ${matches.size} link berhasil dianalisis.")
        }
    }

    fun downloadBatch(quality: DownloadQuality) {
        val items = _batchItems.value.mapNotNull { it.media }
        if (items.isEmpty()) {
            _events.tryEmit("Belum ada link yang berhasil dianalisis.")
            return
        }
        viewModelScope.launch {
            var count = 0
            items.forEach { media -> count += engine.enqueue(media, quality) }
            _events.tryEmit("$count item masuk ke antrean.")
        }
    }

    fun clearBatch() {
        if (!_batchRunning.value) _batchItems.value = emptyList()
    }

    fun loadStories(query: String) {
        if (_stories.value.loading) return
        val clean = query.trim().removePrefix("@")
        if (clean.isBlank()) {
            _stories.value = StoriesState(error = "Masukkan username TikTok.", attempted = true)
            return
        }
        _stories.value = StoriesState(loading = true, query = clean, attempted = true)
        viewModelScope.launch {
            repository.stories(clean)
                .onSuccess { list ->
                    _stories.value = StoriesState(
                        query = clean,
                        items = list,
                        attempted = true,
                        error = if (list.isEmpty()) {
                            "Tidak ada Stories publik yang bisa diambil saat ini."
                        } else {
                            null
                        }
                    )
                }
                .onFailure {
                    _stories.value = StoriesState(
                        query = clean,
                        attempted = true,
                        error = it.message ?: "Stories tidak tersedia."
                    )
                }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingDone(true) }
    }

    fun setTheme(value: String) {
        viewModelScope.launch { settingsRepository.setTheme(value) }
    }

    fun setDefaultQuality(value: String) {
        viewModelScope.launch { settingsRepository.setDefaultQuality(value) }
    }

    fun setWifiOnly(value: Boolean) {
        viewModelScope.launch { settingsRepository.setWifiOnly(value) }
    }

    fun setMaxConcurrent(value: Int) {
        viewModelScope.launch { settingsRepository.setMaxConcurrent(value) }
    }

    fun setAutoPaste(value: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoPaste(value) }
    }

    fun setNotifications(value: Boolean) {
        viewModelScope.launch { settingsRepository.setNotifications(value) }
    }

    fun emit(message: String) {
        _events.tryEmit(message)
    }

    fun retryBatchItem(index: Int) {
        val item = _batchItems.value.getOrNull(index) ?: return
        if (item.loading) return
        _batchItems.value = _batchItems.value.mapIndexed { i, current ->
            if (i == index) current.copy(loading = true, error = null) else current
        }
        viewModelScope.launch {
            val result = repository.analyze(item.url)
            _batchItems.value = _batchItems.value.mapIndexed { i, current ->
                if (i != index) current
                else result.fold(
                    onSuccess = { current.copy(loading = false, media = it, error = null) },
                    onFailure = {
                        current.copy(
                            loading = false,
                            media = null,
                            error = it.message ?: "Gagal dianalisis"
                        )
                    }
                )
            }
        }
    }

    fun parseSharedText(text: String) {
        val url = Regex("""https?://[^\s]+""").find(text)?.value
        if (url != null && url.contains("tiktok.com")) {
            _input.value = url.trimEnd('.', ',', ')', ']', '"', '\'')
            _events.tryEmit("Link dari aplikasi lain siap dianalisis.")
            _sharedLink.tryEmit(Unit)
        } else if (text.contains("tiktok.com")) {
            _input.value = text.trim()
            _sharedLink.tryEmit(Unit)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(container) as T
                }
            }
    }
}
