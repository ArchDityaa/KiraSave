package com.hakira.kirasave.ui.screens

import android.content.ClipboardManager
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hakira.kirasave.data.DownloadEntity
import com.hakira.kirasave.data.formatBytes
import com.hakira.kirasave.data.formatDate
import com.hakira.kirasave.ui.MainViewModel
import com.hakira.kirasave.ui.components.CoverImage
import com.hakira.kirasave.ui.components.DotPattern
import com.hakira.kirasave.ui.components.ErrorBanner
import com.hakira.kirasave.ui.components.HomeHeader
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.PrimaryPill
import com.hakira.kirasave.ui.components.SectionLabel
import com.hakira.kirasave.ui.components.Wordmark
import com.hakira.kirasave.ui.theme.ArchivoFamily
import com.hakira.kirasave.ui.theme.KiraCream
import com.hakira.kirasave.ui.theme.KiraInk
import com.hakira.kirasave.ui.theme.KiraOlive
import com.hakira.kirasave.ui.theme.KiraOliveDeep
import com.hakira.kirasave.ui.theme.KiraOliveLight
import com.hakira.kirasave.ui.theme.KiraOrangeDeep
import com.hakira.kirasave.ui.theme.KiraPeach
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAnalyzeSuccess: () -> Unit,
    onBatch: () -> Unit,
    onStories: () -> Unit,
    onDownloads: () -> Unit,
    onLibrary: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAbout: () -> Unit
) {
    val context = LocalContext.current
    val input by viewModel.input.collectAsState()
    val analyzing by viewModel.analyzing.collectAsState()
    val error by viewModel.analyzeError.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val completed by viewModel.completed.collectAsState()
    val active by viewModel.activeDownloads.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoPasted by rememberSaveable { mutableStateOf(false) }

    fun pasteFromClipboard() {
        val manager = context.getSystemService(ClipboardManager::class.java)
        val value = manager?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
            .orEmpty()
        if (value.contains("tiktok.com")) {
            viewModel.setInput(value.substringBefore("\n").trim())
            viewModel.emit("Link ditempel dari clipboard.")
        } else {
            viewModel.emit("Clipboard tidak berisi link TikTok.")
        }
    }

    LaunchedEffect(settings?.autoPaste) {
        if (!autoPasted && settings?.autoPaste == true) {
            autoPasted = true
            val manager = context.getSystemService(ClipboardManager::class.java)
            val value = manager?.primaryClip
                ?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)
                ?.coerceToText(context)
                ?.toString()
                .orEmpty()
            if (value.contains("tiktok.com") && input.isBlank()) {
                viewModel.setInput(value.substringBefore("\n").trim())
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontal = if (maxWidth > 900.dp) ((maxWidth - 820.dp) / 2) else 20.dp
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeHeader(onSettings = onSettings, onHelp = onHelp)
            }

            item {
                HeroPanel(
                    activeCount = active.size,
                    onStart = {
                        scope.launch { listState.animateScrollToItem(2) }
                    }
                )
            }

            item {
                KiraCard(
                    modifier = Modifier.padding(horizontal = horizontal),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        SectionLabel(text = "Tempel link", trailing = "TikTok")
                        OutlinedTextField(
                            value = input,
                            onValueChange = viewModel::setInput,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "https://www.tiktok.com/@user/video/…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable { pasteFromClipboard() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.ContentPaste,
                                        contentDescription = "Tempel",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        )
                        if (error != null) {
                            ErrorBanner(message = error!!)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Link pendek & share sheet didukung",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PrimaryPill(
                                text = if (analyzing) "Memeriksa" else "Analisis",
                                icon = Icons.Rounded.ArrowForward,
                                loading = analyzing,
                                onClick = { viewModel.analyze(onSuccess = onAnalyzeSuccess) }
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = horizontal),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    SectionLabel(text = "Pintasan", trailing = "${active.size} aktif")
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        FeatureTile(
                            modifier = Modifier.weight(1f),
                            title = "Batch",
                            body = "Banyak link",
                            icon = Icons.Rounded.PlaylistAdd,
                            background = KiraOlive,
                            foreground = KiraOliveLight,
                            onClick = onBatch
                        )
                        FeatureTile(
                            modifier = Modifier.weight(1f),
                            title = "Stories",
                            body = "Publik",
                            icon = Icons.Rounded.AutoAwesome,
                            background = KiraOrangeDeep,
                            foreground = KiraPeach,
                            onClick = onStories
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        FeatureTile(
                            modifier = Modifier.weight(1f),
                            title = "Unduhan",
                            body = if (active.isEmpty()) "Antrean" else "${active.size} berjalan",
                            icon = Icons.Rounded.DownloadDone,
                            background = MaterialTheme.colorScheme.surface,
                            foreground = MaterialTheme.colorScheme.primary,
                            onClick = onDownloads
                        )
                        FeatureTile(
                            modifier = Modifier.weight(1f),
                            title = "Koleksi",
                            body = "${completed.size} file",
                            icon = Icons.Rounded.CollectionsBookmark,
                            background = MaterialTheme.colorScheme.surface,
                            foreground = MaterialTheme.colorScheme.onSurface,
                            onClick = onLibrary
                        )
                    }
                }
            }

            if (completed.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        SectionLabel(
                            text = "Terbaru",
                            modifier = Modifier.padding(horizontal = horizontal),
                            trailing = "Lihat semua"
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = horizontal),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(completed.take(8), key = { it.id }) { item ->
                                RecentCard(item = item, onClick = onLibrary)
                            }
                        }
                    }
                }
            }

            item {
                KiraCard(
                    modifier = Modifier.padding(horizontal = horizontal),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionLabel(text = "Cara pakai")
                        StepRow("01", "Salin link", "Dari TikTok, tekan Bagikan lalu Salin tautan.")
                        StepRow("02", "Analisis", "Tempel di sini. KiraSave memilih media terbaik yang tersedia.")
                        StepRow("03", "Simpan", "Pilih HD, SD, atau audio. File masuk ke galeri dan Koleksi.")
                    }
                }
            }

            item {
                KiraCard(
                    modifier = Modifier.padding(horizontal = horizontal),
                    color = KiraOliveDeep
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(KiraOliveLight.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = KiraOliveLight,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Privat secara desain",
                                style = MaterialTheme.typography.titleMedium,
                                color = KiraOliveLight
                            )
                            Text(
                                text = "Tanpa akun, tanpa iklan, tanpa pelacakan. URL hanya dikirim ke layanan cadangan saat parser perangkat gagal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KiraOliveLight.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = horizontal),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dibuat oleh Hakira Aditya",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onAbout)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "KiraSave tidak berafiliasi dengan TikTok. Gunakan hanya untuk konten yang Anda miliki atau berhak simpan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPanel(
    activeCount: Int,
    onStart: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(KiraOrangeDeep, Color(0xFFD86B25), Color(0xFFE88B4D))
                    )
                )
                .padding(24.dp)
        ) {
            DotPattern(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(210.dp),
                color = KiraCream.copy(alpha = 0.3f),
                spacing = 12.dp,
                radius = 1.3.dp
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = KiraInk.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "TIKTOK DOWNLOADER",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KiraCream,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = CircleShape,
                        color = KiraCream.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = if (activeCount > 0) "$activeCount AKTIF" else "GRATIS",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KiraCream
                        )
                    }
                }
                Spacer(Modifier.height(34.dp))
                Text(
                    text = "VIDEO BERSIH.",
                    fontFamily = ArchivoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 39.sp,
                    lineHeight = 39.sp,
                    letterSpacing = (-1.4).sp,
                    color = KiraCream
                )
                Text(
                    text = "TANPA WATERMARK.",
                    fontFamily = ArchivoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.6).sp,
                    color = KiraCream.copy(alpha = 0.82f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "HD bila tersedia. Audio asli, carousel foto, dan antrean unduhan dalam satu tempat.",
                    modifier = Modifier.fillMaxWidth(0.82f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = KiraCream.copy(alpha = 0.86f)
                )
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onStart),
                        shape = CircleShape,
                        color = KiraCream
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mulai simpan",
                                style = MaterialTheme.typography.labelLarge,
                                color = KiraInk
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = KiraInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Tanpa login",
                        style = MaterialTheme.typography.bodySmall,
                        color = KiraCream.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureTile(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    icon: ImageVector,
    background: Color,
    foreground: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(124.dp),
        shape = RoundedCornerShape(26.dp),
        color = background,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(37.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(foreground.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = foreground.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = foreground
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = foreground.copy(alpha = 0.68f)
                )
            }
        }
    }
}

@Composable
private fun StepRow(number: String, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = number,
            fontFamily = ArchivoFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(13.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentCard(item: DownloadEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Column {
            CoverImage(
                url = item.coverUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp),
                cornerRadius = 18.dp
            )
            Column(modifier = Modifier.padding(11.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.quality,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
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
