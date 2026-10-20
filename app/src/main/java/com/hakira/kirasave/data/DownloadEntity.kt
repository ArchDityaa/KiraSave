package com.hakira.kirasave.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "downloads",
    indices = [
        Index(value = ["videoId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val videoId: String,
    val sourceUrl: String,
    val title: String,
    val authorHandle: String,
    val authorName: String,
    val coverUrl: String?,
    val mediaType: String,
    val quality: String,
    val mediaUrl: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long = 0L,
    val bytesDownloaded: Long = 0L,
    val status: String = DownloadStatus.QUEUED,
    val error: String? = null,
    val fileUri: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
