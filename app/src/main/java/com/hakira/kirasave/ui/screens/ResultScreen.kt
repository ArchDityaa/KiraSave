package com.hakira.kirasave.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.hakira.kirasave.data.DownloadQuality
import com.hakira.kirasave.data.TikTokMedia
import com.hakira.kirasave.data.formatBytes
import com.hakira.kirasave.data.formatCount
import com.hakira.kirasave.data.formatDuration
import com.hakira.kirasave.ui.MainViewModel
import com.hakira.kirasave.ui.components.CoverImage
import com.hakira.kirasave.ui.components.EmptyState
import com.hakira.kirasave.ui.components.ErrorBanner
import com.hakira.kirasave.ui.components.GhostPill
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.LoadingBlock
import com.hakira.kirasave.ui.components.PrimaryPill
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel
import com.hakira.kirasave.ui.components.openUrl
import com.hakira.kirasave.ui.components.StatChip
import com.hakira.kirasave.ui.theme.KiraCream
import com.hakira.kirasave.ui.theme.KiraInk

@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onPlay: (String, String) -> Unit,
    onDownloads: () -> Unit
) {
    val media by viewModel.analysis.collectAsState()
    val loading by viewModel.analyzing.collectAsState()
    val error by viewModel.analyzeError.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Hasil analisis",
            subtitle = media?.source,
            onBack = onBack
        )
        when {
            media == null && loading -> LoadingBlock(
                modifier = Modifier.fillMaxWidth(),
                label = "Mencari media terbaik…"
            )

            media == null -> {
                if (error != null) {
                    ErrorBanner(
                        message = error!!,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                EmptyState(
                    icon = Icons.Rounded.Download,
                    title = "Belum ada media",
                    body = "Tempel link TikTok di Beranda untuk mulai menganalisis.",
                    actionLabel = "Kembali",
                    onAction = onBack
                )
            }

            else -> {
                val item = media!!
                var selectedName by rememberSaveable(item.videoId) {
                    mutableStateOf(
                        when {
                            item.isPhotoPost -> DownloadQuality.PHOTO.name
                            settings?.defaultQuality == "SD" -> DownloadQuality.SD.name
                            else -> DownloadQuality.HD.name
                        }
                    )
                }
                val selected = runCatching { DownloadQuality.valueOf(selectedName) }
                    .getOrDefault(DownloadQuality.HD)
                var previewPhoto by remember { mutableStateOf<String?>(null) }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val horizontal = if (maxWidth > 900.dp) ((maxWidth - 820.dp) / 2) else 20.dp
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = horizontal, end = horizontal, bottom = 34.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            MediaHero(
                                media = item,
                                onPreview = {
                                    if (item.isPhotoPost) {
                                        previewPhoto = item.imageUrls.firstOrNull()
                                    } else {
                                        val url = item.hdUrl ?: item.sdUrl
                                        if (url != null) onPlay(url, item.title)
                                    }
                                }
                            )
                        }

                        item {
                            AuthorRow(media = item)
                        }

                        item {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(9.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                StatChip(
                                    "tonton",
                                    formatCount(item.stats.views),
                                    Modifier.weight(1f)
                                )
                                StatChip(
                                    "suka",
                                    formatCount(item.stats.likes),
                                    Modifier.weight(1f)
                                )
                                StatChip(
                                    "komentar",
                                    formatCount(item.stats.comments),
                                    Modifier.weight(1f)
                                )
                            }
                        }

                        if (item.isPhotoPost) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                                    SectionLabel("Foto carousel", trailing = "${item.imageUrls.size} foto")
                                    KiraCard(contentPadding = PaddingValues(8.dp)) {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(3),
                                            modifier = Modifier.height(
                                                (((item.imageUrls.size.coerceAtMost(6) + 2) / 3) * 118).dp
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            userScrollEnabled = false
                                        ) {
                                            items(item.imageUrls.take(6)) { url ->
                                                CoverImage(
                                                    url = url,
                                                    modifier = Modifier
                                                        .aspectRatio(0.78f)
                                                        .clickable { previewPhoto = url },
                                                    cornerRadius = 16.dp
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Semua foto disimpan ke Pictures/KiraSave.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SectionLabel("Pilih format", trailing = item.source)
                                    if (item.hdUrl != null) {
                                        FormatOption(
                                            title = "HD tanpa watermark",
                                            subtitle = buildString {
                                                append("Bitrate terbaik")
                                                if (item.hdSize > 0) append(" · ${formatBytes(item.hdSize)}")
                                            },
                                            icon = Icons.Rounded.Hd,
                                            selected = selected == DownloadQuality.HD,
                                            onClick = { selectedName = DownloadQuality.HD.name }
                                        )
                                    }
                                    if (item.sdUrl != null && item.sdUrl != item.hdUrl) {
                                        FormatOption(
                                            title = "SD tanpa watermark",
                                            subtitle = buildString {
                                                append("Hemat data")
                                                if (item.sdSize > 0) append(" · ${formatBytes(item.sdSize)}")
                                            },
                                            icon = Icons.Rounded.Download,
                                            selected = selected == DownloadQuality.SD,
                                            onClick = { selectedName = DownloadQuality.SD.name }
                                        )
                                    }
                                    if (item.audioUrl != null) {
                                        FormatOption(
                                            title = "Audio asli (MP3)",
                                            subtitle = "Musik dari postingan TikTok",
                                            icon = Icons.Rounded.AudioFile,
                                            selected = selected == DownloadQuality.AUDIO,
                                            onClick = { selectedName = DownloadQuality.AUDIO.name }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            PrimaryPill(
                                text = when {
                                    item.isPhotoPost -> "Unduh ${item.imageUrls.size} foto"
                                    selected == DownloadQuality.AUDIO -> "Unduh audio MP3"
                                    selected == DownloadQuality.SD -> "Unduh video SD"
                                    else -> "Unduh video HD"
                                },
                                icon = Icons.Rounded.Download,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    if (item.isPhotoPost) {
                                        viewModel.download(DownloadQuality.PHOTO, onQueued = onDownloads)
                                    } else {
                                        viewModel.download(selected, onQueued = onDownloads)
                                    }
                                }
                            )
                            Spacer(Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                GhostPill(
                                    text = "Pratinjau",
                                    icon = Icons.Rounded.PlayArrow,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        if (item.isPhotoPost) {
                                            previewPhoto = item.imageUrls.firstOrNull()
                                        } else {
                                            val url = item.hdUrl ?: item.sdUrl
                                            if (url != null) onPlay(url, item.title)
                                        }
                                    }
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                    GhostPill(
                                        text = "Bagikan",
                                        icon = Icons.Rounded.Share,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, item.sourceUrl)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(intent, "Bagikan link")
                                            )
                                        }
                                    )
                                    GhostPill(
                                        text = "Salin",
                                        icon = Icons.Rounded.ContentCopy,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val clipboard =
                                                context.getSystemService(ClipboardManager::class.java)
                                            clipboard?.setPrimaryClip(
                                                ClipData.newPlainText("Link TikTok", item.sourceUrl)
                                            )
                                            viewModel.emit("Link disalin.")
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            KiraCard(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "HD sesuai sumber",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Kalau TikTok hanya menyediakan bitrate rendah, KiraSave menampilkan ukuran asli tanpa klaim palsu. Ekstrak M4A tersedia dari halaman Koleksi setelah video diunduh.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { openUrl(context, item.sourceUrl) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sumber: ${item.source} · Konten publik",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Rounded.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                if (previewPhoto != null) {
                    PhotoPreviewDialog(url = previewPhoto!!, onDismiss = { previewPhoto = null })
                }
            }
        }
    }
}

@Composable
private fun MediaHero(media: TikTokMedia, onPreview: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onPreview)
    ) {
        CoverImage(
            url = media.coverUrl ?: media.imageUrls.firstOrNull(),
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 28.dp
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        1f to KiraInk.copy(alpha = 0.85f)
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Surface(shape = CircleShape, color = KiraInk.copy(alpha = 0.55f)) {
                Text(
                    text = if (media.isPhotoPost) "CAROUSEL" else "VIDEO",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = KiraCream
                )
            }
            if (media.durationSeconds > 0) {
                Surface(shape = CircleShape, color = KiraInk.copy(alpha = 0.55f)) {
                    Text(
                        text = formatDuration(media.durationSeconds),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = KiraCream
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(68.dp)
                .clip(CircleShape)
                .background(KiraCream.copy(alpha = 0.94f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = "Pratinjau",
                tint = KiraInk,
                modifier = Modifier.size(36.dp)
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = KiraCream.copy(alpha = 0.18f)) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = KiraCream,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "TANPA WATERMARK",
                        style = MaterialTheme.typography.labelSmall,
                        color = KiraCream
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = media.source,
                style = MaterialTheme.typography.labelSmall,
                color = KiraCream.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AuthorRow(media: TikTokMedia) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CoverImage(
            url = media.author.avatarUrl,
            modifier = Modifier.size(46.dp),
            cornerRadius = 23.dp
        )
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.author.nickname.ifBlank { media.author.handle },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${media.author.handle.ifBlank { "tiktok" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (media.isStory) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "STORIES",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun FormatOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(21.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoPreviewDialog(url: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Pratinjau foto",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Tutup",
                    tint = Color.White
                )
            }
        }
    }
}
