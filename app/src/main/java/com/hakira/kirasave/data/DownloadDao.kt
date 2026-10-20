package com.hakira.kirasave.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('QUEUED', 'DOWNLOADING', 'PAUSED') ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC, createdAt DESC")
    fun observeCompleted(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE isFavorite = 1 AND status = 'COMPLETED' ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt ASC LIMIT :limit")
    suspend fun byStatus(status: String, limit: Int = 20): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun byId(id: Long): DownloadEntity?

    @Query(
        "SELECT * FROM downloads WHERE videoId = :videoId AND quality = :quality " +
            "AND status = 'COMPLETED' LIMIT 1"
    )
    suspend fun findCompleted(videoId: String, quality: String): DownloadEntity?

    @Query("SELECT COUNT(*) FROM downloads WHERE status IN ('QUEUED', 'DOWNLOADING')")
    fun observeRunningCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM downloads WHERE status = 'COMPLETED'")
    fun observeTotalBytes(): Flow<Long>

    @Insert
    suspend fun insert(entity: DownloadEntity): Long

    @Update
    suspend fun update(entity: DownloadEntity)

    @Query(
        "UPDATE downloads SET status = :status, bytesDownloaded = :bytes, sizeBytes = :size, error = :error " +
            "WHERE id = :id"
    )
    suspend fun updateProgress(
        id: Long,
        status: String,
        bytes: Long,
        size: Long,
        error: String?
    )

    @Query(
        "UPDATE downloads SET status = 'COMPLETED', fileUri = :uri, sizeBytes = :size, " +
            "bytesDownloaded = :size, completedAt = :completedAt, error = NULL WHERE id = :id"
    )
    suspend fun markCompleted(id: Long, uri: String, size: Long, completedAt: Long)

    @Query("UPDATE downloads SET status = :status, error = :error WHERE id = :id")
    suspend fun setStatus(id: Long, status: String, error: String?)

    @Query("UPDATE downloads SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE downloads SET status = 'PAUSED' WHERE status = 'DOWNLOADING'")
    suspend fun pauseInterrupted()

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun delete(id: Long)
}
