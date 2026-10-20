package com.hakira.kirasave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hakira.kirasave.data.DownloadEntity
import com.hakira.kirasave.data.MediaKind
import com.hakira.kirasave.data.MediaStoreSaver
import com.hakira.kirasave.data.formatBytes
import com.hakira.kirasave.ui.MainViewModel
import com.hakira.kirasave.ui.components.CoverImage
import com.hakira.kirasave.ui.components.EmptyState
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.SearchField
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.StorageStrip
import com.hakira.kirasave.ui.theme.KiraInk

private enum class LibraryFilter(val label: String) {
    ALL("Semua"),
    VIDEO("Video"),
    PHOTO("Foto"),
    AUDIO("Audio"),
    FAVORITE("Favorit")
}

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onPlay: (String, String) -> Unit,
    onExplore: () -> Unit
) {
    val completed by viewModel.completed.collectAsState()
    val totalBytes by viewModel.totalBytes.collectAsState()
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }
    var grid by rememberSaveable { mutableStateOf(true) }
    var pendingDelete by remember { mutableStateOf<DownloadEntity?>(null) }

    val filtered = completed.filter { item ->
        val matchesQuery = query.isBlank() ||
            item.title.contains(query, ignoreCase = true) ||
            item.authorHandle.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            LibraryFilter.ALL -> true
            LibraryFilter.VIDEO -> item.mediaType == MediaKind.VIDEO.name
            LibraryFilter.PHOTO -> item.mediaType == MediaKind.PHOTO.name
            LibraryFilter.AUDIO -> item.mediaType == MediaKind.AUDIO.name
            LibraryFilter.FAVORITE -> item.isFavorite
        }
        matchesQuery && matchesFilter
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = when {
            !grid -> 1
            maxWidth > 1100.dp -> 4
            maxWidth > 760.dp -> 3
            else -> 2
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ScreenHeader(
                    title = "Koleksi",
                    subtitle = "${completed.size} file tersimpan",
                    actions = {
                        IconButton(onClick = { grid = !grid }) {
                            Icon(
                                imageVector = if (grid) Icons.Rounded.ViewList
                                else Icons.Rounded.GridView,
                                contentDescription = "Ubah tampilan"
                            )
                        }
                    }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                StorageStrip(totalBytes = totalBytes, itemCount = completed.size)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchField(value = query, onValueChange = { query = it })
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    LibraryFilter.entries.forEach { entry ->
                        item {
                            LibraryFilterPill(
                                label = entry.label,
                                selected = filter == entry,
                                onClick = { filter = entry }
                            )
                        }
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (filtered.isEmpty()) "Tidak ada hasil" else "${filtered.size} media",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (filtered.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        icon = Icons.Rounded.VideoLibrary,
                        title = if (completed.isEmpty()) "Koleksi masih kosong" else "Tidak ditemukan",
                        body = if (completed.isEmpty()) {
                            "Video, foto, dan audio yang selesai diunduh akan muncul di sini."
                        } else {
                            "Coba kata kunci lain atau ganti filter."
                        },
                        actionLabel = if (completed.isEmpty()) "Mulai unduh" else null,
                        onAction = if (completed.isEmpty()) onExplore else null
                    )
                }
            }

            items(filtered, key = { it.id }) { item ->
                LibraryCard(
                    item = item,
                    listMode = !grid,
                    onOpen = {
                        if (item.mediaType == MediaKind.VIDEO.name) {
                            item.fileUri?.let { onPlay(it, item.title) }
                        } else {
                            MediaStoreSaver.open(context, item.fileUri, item.mimeType)
                        }
                    },
                    onShare = {
                        MediaStoreSaver.share(
                            context,
                            item.fileUri,
                            item.mimeType,
                            "Bagikan file"
                        )
                    },
                    onFavorite = { viewModel.setFavorite(item.id, !item.isFavorite) },
                    onExtract = { viewModel.extractAudio(item.id) },
                    onDelete = { pendingDelete = item }
                )
            }
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Hapus dari koleksi?") },
            text = { Text("File juga akan dihapus dari galeri perangkat.") },
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
private fun LibraryCard(
    item: DownloadEntity,
    listMode: Boolean,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    onExtract: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    KiraCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(7.dp),
        onClick = onOpen
    ) {
        Column {
            Box {
                CoverImage(
                    url = item.coverUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (listMode) 178.dp else 146.dp),
                    cornerRadius = 20.dp
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = CircleShape,
                    color = KiraInk.copy(alpha = 0.55f)
                ) {
                    Box {
                        IconButton(
                            onClick = { menu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(
                                text = { Text("Buka") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                                },
                                onClick = {
                                    menu = false
                                    onOpen()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bagikan") },
                                leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                                onClick = {
                                    menu = false
                                    onShare()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(if (item.isFavorite) "Hapus favorit" else "Favoritkan")
                                },
                                leadingIcon = {
                                    Icon(
                                        if (item.isFavorite) Icons.Rounded.Favorite
                                        else Icons.Rounded.FavoriteBorder,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menu = false
                                    onFavorite()
                                }
                            )
                            if (item.mediaType == MediaKind.VIDEO.name) {
                                DropdownMenuItem(
                                    text = { Text("Ekstrak audio M4A") },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.AudioFile, contentDescription = null)
                                    },
                                    onClick = {
                                        menu = false
                                        onExtract()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Hapus") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
                if (item.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(23.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = "Favorit",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(7.dp),
                    shape = CircleShape,
                    color = KiraInk.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = when (item.mediaType) {
                            MediaKind.VIDEO.name -> item.quality
                            MediaKind.PHOTO.name -> "FOTO"
                            else -> "AUDIO"
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    minLines = if (listMode) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${item.authorHandle.ifBlank { "tiktok" }}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatBytes(item.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryFilterPill(
    label: String,
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
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
