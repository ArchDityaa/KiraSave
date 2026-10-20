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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hakira.kirasave.data.DownloadQuality
import com.hakira.kirasave.data.formatBytes
import com.hakira.kirasave.ui.BatchItem
import com.hakira.kirasave.ui.MainViewModel
import com.hakira.kirasave.ui.components.CoverImage
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel

@Composable
fun BatchScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onDownloads: () -> Unit
) {
    val items by viewModel.batchItems.collectAsState()
    val running by viewModel.batchRunning.collectAsState()
    val clipboard = LocalClipboardManager.current
    var raw by rememberSaveable { mutableStateOf("") }
    var quality by rememberSaveable { mutableStateOf("HD") }

    val ready = items.count { it.media != null }
    val failures = items.count { it.error != null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            ScreenHeader(
                title = "Unduh massal",
                subtitle = "Hingga 20 link sekaligus",
                onBack = onBack,
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearBatch, enabled = !running) {
                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = "Bersihkan daftar"
                            )
                        }
                    }
                }
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KiraCard {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TEMPEL LINK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.weight(1f))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                onClick = {
                                    val text = clipboard.getText()?.text
                                    if (!text.isNullOrBlank()) {
                                        raw = (raw + "\n" + text).trim()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.ContentPaste,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        "Tempel",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = raw,
                            onValueChange = { raw = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "https://vt.tiktok.com/...\nhttps://www.tiktok.com/@user/video/...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            minLines = 4,
                            maxLines = 7,
                            shape = RoundedCornerShape(18.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                        Text(
                            text = "Satu link per baris. Link duplikat diabaikan otomatis.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.hakira.kirasave.ui.components.PrimaryPill(
                    text = if (running) "Menganalisis…" else "Analisis semua",
                    icon = Icons.Rounded.Link,
                    loading = running,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (raw.isNotBlank()) viewModel.analyzeBatch(raw)
                    }
                )
                if (items.isNotEmpty() && !running) {
                    com.hakira.kirasave.ui.components.GhostPill(
                        text = "Ulangi gagal",
                        icon = Icons.Rounded.Refresh,
                        onClick = {
                            items.forEachIndexed { index, item ->
                                if (item.media == null) viewModel.retryBatchItem(index)
                            }
                        }
                    )
                }
            }
        }

        if (items.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionLabel(
                        text = "Hasil",
                        trailing = "$ready siap · $failures gagal"
                    )
                }
            }
            itemsIndexed(items, key = { index, _ -> index }) { index, item ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    BatchCard(
                        item = item,
                        onRetry = { viewModel.retryBatchItem(index) }
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf("HD", "SD").forEach { option ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = if (quality == option) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                onClick = { quality = option }
                            ) {
                                Text(
                                    text = if (option == "HD") "Unduh HD" else "Unduh SD",
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (quality == option) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    com.hakira.kirasave.ui.components.PrimaryPill(
                        text = "Unduh $ready item ke antrean",
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = ready > 0 && !running,
                        onClick = {
                            viewModel.downloadBatch(
                                if (quality == "HD") DownloadQuality.HD else DownloadQuality.SD
                            )
                            onDownloads()
                        }
                    )
                }
            }
        } else if (!running) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    KiraCard(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Rounded.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Cocok untuk banyak tautan",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Tempel daftar link dari catatan atau chat. KiraSave akan menganalisis satu per satu, menghormati batas layanan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchCard(item: BatchItem, onRetry: () -> Unit) {
    KiraCard(contentPadding = PaddingValues(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    item.loading -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )

                    item.media != null -> CoverImage(
                        url = item.media.coverUrl,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 16.dp
                    )

                    else -> Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = item.media?.title?.ifBlank { item.url } ?: item.url
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = when {
                        item.loading -> "Menganalisis…"
                        item.media != null -> buildString {
                            append("@${item.media.author.handle}")
                            item.media.hdSize.takeIf { it > 0 }?.let {
                                append(" · HD ${formatBytes(it)}")
                            }
                        }

                        else -> item.error ?: "Gagal dianalisis"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        item.error != null -> MaterialTheme.colorScheme.error
                        item.media != null -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!item.loading && item.media == null) {
                IconButton(onClick = onRetry) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Coba lagi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            if (item.media != null) {
                Icon(
                    Icons.Rounded.Download,
                    contentDescription = "Siap",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
