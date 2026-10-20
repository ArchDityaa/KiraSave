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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hakira.kirasave.BuildConfig
import com.hakira.kirasave.ui.components.BrandMark
import com.hakira.kirasave.ui.components.DotPattern
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel
import com.hakira.kirasave.ui.components.SocialBadge
import com.hakira.kirasave.ui.components.openUrl
import com.hakira.kirasave.ui.theme.ArchivoFamily
import com.hakira.kirasave.ui.theme.KiraCream
import com.hakira.kirasave.ui.theme.KiraInk
import com.hakira.kirasave.ui.theme.KiraOliveDeep
import com.hakira.kirasave.ui.theme.KiraOliveLight
import com.hakira.kirasave.ui.theme.KiraOrangeDeep
import com.hakira.kirasave.ui.theme.KiraPeach

@Composable
fun AboutScreen(onBack: () -> Unit, onPrivacy: () -> Unit) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader(title = "Tentang", onBack = onBack) }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(30.dp),
                color = KiraOrangeDeep
            ) {
                Box {
                    DotPattern(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(180.dp),
                        color = KiraCream.copy(alpha = 0.28f)
                    )
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        BrandMark(size = 58.dp)
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "KiraSave",
                            fontFamily = ArchivoFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            color = KiraCream
                        )
                        Text(
                            text = "Penyimpan TikTok yang tenang.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = KiraCream.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = KiraInk.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "VERSI ${BuildConfig.VERSION_NAME} · TANPA IKLAN",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = KiraCream
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionLabel("Pengembang")
                KiraCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "HA",
                                    fontFamily = ArchivoFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 23.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hakira Aditya",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Pengembang independen asal Indonesia. Membuat alat kecil yang berguna tanpa iklan dan tanpa langganan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        DeveloperLink(
                            label = "TikTok",
                            handle = "@kiraadityaa",
                            badge = "TT",
                            badgeColor = KiraOliveLight,
                            onClick = { openUrl(context, "https://www.tiktok.com/@kiraadityaa") }
                        )
                        Spacer(Modifier.height(8.dp))
                        DeveloperLink(
                            label = "Instagram",
                            handle = "@aaaditz_",
                            badge = "IG",
                            badgeColor = KiraPeach,
                            onClick = { openUrl(context, "https://instagram.com/aaaditz_") }
                        )
                        Spacer(Modifier.height(8.dp))
                        DeveloperLink(
                            label = "WhatsApp",
                            handle = "+62 815-5336-2795",
                            badge = "WA",
                            badgeColor = androidx.compose.ui.graphics.Color(0xFF9AE6B4),
                            onClick = { openUrl(context, "https://wa.me/6281553362795") }
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionLabel("Yang kamu dapat")
                KiraCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FeatureLine(
                            Icons.Rounded.PlayCircle,
                            "Video tanpa watermark",
                            "HD atau SD sesuai bitrate yang benar-benar tersedia."
                        )
                        FeatureLine(
                            Icons.Rounded.PhotoLibrary,
                            "Carousel foto",
                            "Semua slide disimpan ke Pictures/KiraSave."
                        )
                        FeatureLine(
                            Icons.Rounded.MusicNote,
                            "Audio asli & M4A",
                            "Unduh MP3 dari postingan atau ekstrak M4A dari video."
                        )
                        FeatureLine(
                            Icons.Rounded.Lock,
                            "Privat",
                            "Tanpa akun, iklan, analytics, atau layanan berbayar."
                        )
                    }
                }
            }
        }

        item {
            KiraCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = KiraOliveDeep
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = KiraOliveLight,
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Hybrid engine",
                            style = MaterialTheme.typography.titleSmall,
                            color = KiraOliveLight
                        )
                        Text(
                            text = "Parser perangkat dicoba lebih dulu. Jika tidak mendapat stream bersih, URL dikirim ke API gratis TikWM sebagai cadangan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KiraOliveLight.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            KiraCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPrivacy)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Privasi & disclaimer", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Baca sebelum menyimpan konten orang lain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "KiraSave tidak berafiliasi dengan TikTok atau ByteDance. Nama dan merek pihak ketiga adalah milik pemiliknya masing-masing.",
                modifier = Modifier.padding(horizontal = 30.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DeveloperLink(
    label: String,
    handle: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(10.dp), color = badgeColor) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = KiraInk
                    )
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(handle, style = MaterialTheme.typography.titleSmall)
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FeatureLine(icon: ImageVector, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
