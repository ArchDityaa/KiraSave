package com.hakira.kirasave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hakira.kirasave.data.DownloadQuality
import com.hakira.kirasave.data.TikTokMedia
import com.hakira.kirasave.ui.MainViewModel
import com.hakira.kirasave.ui.components.CoverImage
import com.hakira.kirasave.ui.components.DotPattern
import com.hakira.kirasave.ui.components.ErrorBanner
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.PrimaryPill
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel
import com.hakira.kirasave.ui.theme.KiraCream
import com.hakira.kirasave.ui.theme.KiraInk
import com.hakira.kirasave.ui.theme.KiraOliveDeep
import com.hakira.kirasave.ui.theme.KiraOliveLight

@Composable
fun StoriesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onPlay: (String, String) -> Unit,
    onDownloads: () -> Unit
) {
    val state by viewModel.stories.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 158.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 36.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ScreenHeader(
                title = "Stories",
                subtitle = "Publik, sementara, dan kondisional",
                onBack = onBack
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = KiraOliveDeep
            ) {
                Box {
                    DotPattern(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(140.dp),
                        color = KiraCream.copy(alpha = 0.18f)
                    )
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.History,
                                contentDescription = null,
                                tint = KiraOliveLight,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "KONDISIONAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = KiraOliveLight.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Stories yang sedang tayang, selama masih publik.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = KiraOliveLight
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Fitur ini memakai endpoint publik gratis. Jika akun tidak punya Stories aktif atau layanan sedang membatasi permintaan, KiraSave akan memberi tahu — bukan menampilkan data palsu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KiraOliveLight.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            KiraCard(contentPadding = PaddingValues(13.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Username TikTok, contoh: kiraadityaa",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    )
                    PrimaryPill(
                        text = if (state.loading) "Mencari Stories…" else "Lihat Stories",
                        icon = Icons.Rounded.Visibility,
                        loading = state.loading,
                        enabled = query.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.loadStories(query) }
                    )
                }
            }
        }

        if (state.error != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorBanner(
                    message = state.error ?: "",
                    onRetry = { viewModel.loadStories(state.query) }
                )
            }
        }

        if (state.loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Menghormati batas 1 permintaan/detik…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.items.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionLabel(
                    text = "Stories @${state.query}",
                    trailing = "${state.items.size} item"
                )
            }
            items(state.items, key = { it.videoId.ifBlank { it.sourceUrl } }) { story ->
                StoryCard(
                    story = story,
                    onPreview = { story.hdUrl?.let { onPlay(it, story.title) } },
                    onDownload = {
                        viewModel.enqueue(story, DownloadQuality.HD)
                        onDownloads()
                    }
                )
            }
        }

        if (state.attempted && !state.loading && state.items.isEmpty() && state.error == null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Tidak ada Stories untuk ditampilkan.",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Pastikan username benar dan Stories masih aktif.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!state.attempted && !state.loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                KiraCard(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Cara kerja",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Masukkan username tanpa @. KiraSave mencari ID akun publik, lalu meminta daftar Stories yang sedang tayang. Tidak ada login dan tidak ada akses ke akun privat.",
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

@Composable
private fun StoryCard(
    story: TikTokMedia,
    onPreview: () -> Unit,
    onDownload: () -> Unit
) {
    KiraCard(
        contentPadding = PaddingValues(6.dp),
        onClick = onPreview
    ) {
        Column {
            Box {
                CoverImage(
                    url = story.coverUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f),
                    cornerRadius = 19.dp
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(37.dp)
                        .clip(CircleShape)
                        .background(KiraInk.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Pratinjau",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(7.dp),
                    shape = CircleShape,
                    color = KiraOliveDeep.copy(alpha = 0.92f)
                ) {
                    Text(
                        "STORY",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = KiraOliveLight
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${story.author.handle}",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Story publik",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onDownload
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Download,
                            contentDescription = "Unduh story",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
