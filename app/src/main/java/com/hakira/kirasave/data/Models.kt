package com.hakira.kirasave.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TikTokAuthor(
    val id: String = "",
    val handle: String = "",
    val nickname: String = "",
    val avatarUrl: String? = null
)

data class TikTokStats(
    val views: Long = 0,
    val likes: Long = 0,
    val comments: Long = 0,
    val shares: Long = 0
)

data class TikTokMedia(
    val videoId: String,
    val sourceUrl: String,
    val title: String,
    val author: TikTokAuthor,
    val coverUrl: String?,
    val durationSeconds: Int = 0,
    val hdUrl: String? = null,
    val sdUrl: String? = null,
    val audioUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val hdSize: Long = 0L,
    val sdSize: Long = 0L,
    val stats: TikTokStats = TikTokStats(),
    val source: String = "tikwm",
    val isStory: Boolean = false
) {
    val isPhotoPost: Boolean get() = imageUrls.isNotEmpty()
}

enum class MediaKind { VIDEO, PHOTO, AUDIO }

enum class DownloadQuality { HD, SD, AUDIO, PHOTO }

object DownloadStatus {
    const val QUEUED = "QUEUED"
    const val DOWNLOADING = "DOWNLOADING"
    const val PAUSED = "PAUSED"
    const val COMPLETED = "COMPLETED"
    const val FAILED = "FAILED"
    const val CANCELLED = "CANCELLED"
}

fun DownloadEntity.progressFraction(): Float {
    if (status == DownloadStatus.COMPLETED) return 1f
    if (sizeBytes <= 0L) return 0f
    return (bytesDownloaded.toFloat() / sizeBytes.toFloat()).coerceIn(0f, 1f)
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "—"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    val pattern = if (unit == 0) "#" else "#.#"
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    return DecimalFormat(pattern, symbols).format(value) + " " + units[unit]
}

fun formatCount(value: Long): String {
    if (value < 1_000) return value.toString()
    if (value < 1_000_000) return trimNumber(value / 1_000.0) + "RB"
    if (value < 1_000_000_000) return trimNumber(value / 1_000_000.0) + "JT"
    return trimNumber(value / 1_000_000_000.0) + "M"
}

private fun trimNumber(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    return DecimalFormat("#.#", symbols).format(value)
}

fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "0:00"
    val minutes = seconds / 60
    val rest = seconds % 60
    return "%d:%02d".format(minutes, rest)
}

fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return ""
    return SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(timestamp))
}

fun cleanFileName(value: String, fallback: String): String {
    val cleaned = value
        .replace(Regex("[\\\\/:*?\"<>|]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .trim('.', ' ')
        .take(70)
    return if (cleaned.isBlank()) fallback else cleaned
}
