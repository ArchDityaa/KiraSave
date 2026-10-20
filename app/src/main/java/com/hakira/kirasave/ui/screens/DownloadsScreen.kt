package com.hakira.kirasave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hakira.kirasave.data.DownloadEntity
import com.hakira.kirasave.data.DownloadStatus
import com.hakira.kirasave.data.MediaKind
import com.hakira.kirasave.data.MediaStoreSaver
import com.hakira.kirasave.data.formatBytes
import com.hakira.kirasave.data.progressFraction
import com.hakira.kirasave.ui.MainViewModel
import com.hakira.kirasave.ui.components.CoverImage
import com.hakira.kirasave.ui.components.EmptyState
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel
import com.hakira.kirasave.ui.components.StatChip
import com.hakira.kirasave.ui.theme.KiraInk
import com.hakira.kirasave.ui.theme.KiraOliveDeep
import com.hakira.kirasave.ui.theme.KiraOliveLight
import com.hakira.kirasave.ui.theme.KiraOrangeDeep
import com.hakira.kirasave.ui.theme.KiraPeach

private enum class DownloadFilter(val label: String) {
    ALL("Semua"),
    ACTIVE("Aktif"),
    DONE("Selesai"),
    FAILED("Gagal")
}

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    onPlay: (String, String) -> Unit,
    onExplore: () -> Unit
) {
    val all by viewModel.downloads.collectAsState()
    val context = LocalContext.current
    var filter by remember { mutableStateOf(DownloadFilter.ALL) }
    var pendingDelete by remember { mutableStateOf<DownloadEntity?>(null) }

    val active = all.filter {
        it.status == DownloadStatus.QUEUED ||
            it.status == DownloadStatus.DOWNLOADING ||
            it.status == DownloadStatus.PAUSED
    }
    val done = all.filter { it.status == DownloadStatus.COMPLETED }
    val failed = all.filter {
        it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELLED
    }
    val items = when (filter) {
        DownloadFilter.ALL -> all
        DownloadFilter.ACTIVE -> active
        DownloadFilter.DONE -> done
        DownloadFilter.FAILED -> failed
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Unduhan",
            subtitle = if (active.isEmpty()) {
                "Semua antrean selesai"
            } else {
                "${active.size} item dalam antrean"
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    DownloadFilter.entries.forEach { entry ->
                        item {
                            FilterPill(
                                label = entry.label,
                                count = when (entry) {
                                    DownloadFilter.ALL -> all.size
                                    DownloadFilter.ACTIVE -> active.size
                                    DownloadFilter.DONE -> done.size
                                    DownloadFilter.FAILED -> failed.size
                                },
                                selected = filter == entry,
                                onClick = { filter = entry }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            if (items.isEmpty()) {
                item {
                    EmptyState(
                        icon = when (filter) {
                            DownloadFilter.FAILED -> Icons.Rounded.ErrorOutline
                            DownloadFilter.DONE -> Icons.Rounded.CheckCircle
                            else -> Icons.Rounded.Download
                        },
                        title = when (filter) {
                            DownloadFilter.ACTIVE -> "Antrean kosong"
                            DownloadFilter.DONE -> "Belum ada unduhan selesai"
                            DownloadFilter.FAILED -> "Tidak ada unduhan gagal"
                            DownloadFilter.ALL -> "Belum ada unduhan"
                        },
                        body = "Mulai dari Beranda dengan menempelkan link TikTok.",
                        actionLabel = "Buka Beranda",
                        onAction = onExplore
                    )
                }
            }

            items(items, key = { it.id }) { entity ->
                DownloadCard(
                    entity = entity,
                    onPause = { viewModel.pause(entity.id) },
                    onResume = { viewModel.resume(entity.id) },
                    onCancel = { viewModel.cancel(entity.id) },
                    onRetry = { viewModel.retry(entity.id) },
                    onDelete = { pendingDelete = entity },
                    onFavorite = { viewModel.setFavorite(entity.id, !entity.isFavorite) },
                    onExtract = { viewModel.extractAudio(entity.id) },
                    onShare = {
                        MediaStoreSaver.share(
                            context,
                            entity.fileUri,
                            entity.mimeType,
                            "Bagikan file"
                        )
                    },
                    onOpen = {
                        if (entity.mediaType == MediaKind.VIDEO.name) {
                            entity.fileUri?.let { onPlay(it, entity.title) }
                        } else {
                            MediaStoreSaver.open(context, entity.fileUri, entity.mimeType)
                        }
                    }
                )
            }
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Hapus unduhan?") },
            text = {
                Text(
                    "File akan dihapus dari perangkat dan tidak bisa dikembalikan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete?.let { viewModel.remove(it.id) }
                        pendingDelete = null
                    }
                ) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun DownloadCard(
    entity: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    onFavorite: () -> Unit,
    onExtract: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit
) {
    val active = entity.status == DownloadStatus.QUEUED ||
        entity.status == DownloadStatus.DOWNLOADING ||
        entity.status == DownloadStatus.PAUSED
    val fraction = entity.progressFraction()

    KiraCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        color = if (active) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        onClick = if (entity.status == DownloadStatus.COMPLETED) onOpen else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box {
                    CoverImage(
                        url = entity.coverUrl,
                        modifier = Modifier
                            .width(68.dp)
                            .height(88.dp),
                        cornerRadius = 16.dp
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(5.dp),
                        shape = CircleShape,
                        color = KiraInk.copy(alpha = 0.72f)
                    ) {
                        Text(
                            text = entity.quality,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entity.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "@${entity.authorHandle.ifBlank { "tiktok" }} · ${entity.mediaType.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = statusLabel(entity),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor(entity)
                        )
                        Text(
                            text = "  ·  ${formatBytes(entity.sizeBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (entity.status == DownloadStatus.COMPLETED) {
                    IconButton(onClick = onFavorite) {
                        Icon(
                            imageVector = if (entity.isFavorite) {
                                Icons.Rounded.Favorite
                            } else {
                                Icons.Rounded.FavoriteBorder
                            },
                            contentDescription = "Favorit",
                            tint = if (entity.isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (active) {
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (entity.sizeBytes > 0) {
                            "${(fraction * 100).toInt()}% · ${formatBytes(entity.bytesDownloaded)} / ${formatBytes(entity.sizeBytes)}"
                        } else {
                            "Menghubungkan…"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    when (entity.status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> RoundAction(
                            icon = Icons.Rounded.Pause,
                            contentDescription = "Jeda",
                            onClick = onPause
                        )

                        DownloadStatus.PAUSED -> RoundAction(
                            icon = Icons.Rounded.PlayArrow,
                            contentDescription = "Lanjutkan",
                            onClick = onResume
                        )
                    }
                    Spacer(Modifier.width(5.dp))
                    RoundAction(
                        icon = Icons.Rounded.Cancel,
                        contentDescription = "Batalkan",
                        onClick = onCancel
                    )
                }
            } else if (entity.status == DownloadStatus.FAILED ||
                entity.status == DownloadStatus.CANCELLED
            ) {
                if (!entity.error.isNullOrBlank()) {
                    Text(
                        text = entity.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ActionChip(
                        icon = Icons.Rounded.Refresh,
                        label = "Coba lagi",
                        onClick = onRetry
                    )
                    ActionChip(
                        icon = Icons.Rounded.Delete,
                        label = "Hapus",
                        onClick = onDelete
                    )
                }
            } else if (entity.status == DownloadStatus.COMPLETED) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ActionChip(
                        icon = Icons.Rounded.Share,
                        label = "Bagikan",
                        onClick = onShare
                    )
                    if (entity.mediaType == MediaKind.VIDEO.name) {
                        ActionChip(
                            icon = Icons.Rounded.AudioFile,
                            label = "M4A",
                            onClick = onExtract
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (count > 0) {
                Spacer(Modifier.width(5.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

@Composable
private fun RoundAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun statusLabel(entity: DownloadEntity): String = when (entity.status) {
    DownloadStatus.QUEUED -> "Menunggu"
    DownloadStatus.DOWNLOADING -> "Mengunduh"
    DownloadStatus.PAUSED -> "Dijeda"
    DownloadStatus.COMPLETED -> "Selesai"
    DownloadStatus.FAILED -> "Gagal"
    DownloadStatus.CANCELLED -> "Dibatalkan"
    else -> entity.status
}

@Composable
private fun statusColor(entity: DownloadEntity): Color = when (entity.status) {
    DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
    DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
    DownloadStatus.PAUSED -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}
