package com.hakira.kirasave.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hakira.kirasave.ui.components.DotPattern
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel
import com.hakira.kirasave.ui.components.openUrl
import com.hakira.kirasave.ui.theme.ArchivoFamily
import com.hakira.kirasave.ui.theme.KiraCream
import com.hakira.kirasave.ui.theme.KiraInk
import com.hakira.kirasave.ui.theme.KiraOliveDeep
import com.hakira.kirasave.ui.theme.KiraOliveLight

private data class Faq(val question: String, val answer: String)

private val faqs = listOf(
    Faq(
        "Kenapa link tidak bisa dianalisis?",
        "Pastikan postingan publik dan link masih aktif. Coba buka link di browser, lalu tempel ulang. Jika parser perangkat gagal, KiraSave otomatis memakai layanan cadangan gratis. Tunggu beberapa detik jika layanan sedang sibuk."
    ),
    Faq(
        "Apakah video benar-benar tanpa watermark?",
        "KiraSave memakai stream bitrate dari halaman TikTok bila tersedia. Jika tidak ada, layanan cadangan mengembalikan versi bersih yang tersedia. Kalau sumber hanya menyediakan versi ber-watermark, media tidak akan ditandai sebagai HD bersih."
    ),
    Faq(
        "Kenapa resolusinya bukan 1080p?",
        "TikTok tidak selalu menyimpan 1080p untuk semua video. KiraSave menampilkan ukuran dan kualitas yang benar-benar tersedia, bukan klaim palsu. Video lama atau akun tertentu bisa hanya punya bitrate rendah."
    ),
    Faq(
        "Di mana file disimpan?",
        "Video ke Movies/KiraSave, foto ke Pictures/KiraSave, dan audio ke Music/KiraSave. Semuanya muncul di galeri dan bisa dibuka dari tab Koleksi."
    ),
    Faq(
        "Bagaimana cara mendapatkan audio M4A?",
        "Unduh videonya lebih dulu. Buka Unduhan atau Koleksi, tekan menu pada video, lalu pilih Ekstrak audio M4A. File baru masuk ke Music/KiraSave tanpa menghapus video asli."
    ),
    Faq(
        "Apakah Stories selalu tersedia?",
        "Tidak. Stories bersifat kondisional karena bergantung pada endpoint publik gratis dan hanya bisa diakses untuk akun dengan Stories publik. Jika tidak ada data, KiraSave menampilkan pesan bahwa Stories tidak tersedia."
    ),
    Faq(
        "Apakah saya perlu akun TikTok?",
        "Tidak. KiraSave tidak meminta login, cookie, atau kata sandi. Hanya link publik yang diproses."
    ),
    Faq(
        "Apakah link saya dikirim ke pihak ketiga?",
        "Parser perangkat dicoba lebih dulu. Jika gagal, URL dikirim ke TikWM sebagai layanan cadangan gratis. Tidak ada analytics, iklan, atau penjualan data."
    ),
    Faq(
        "Kenapa unduhan berhenti?",
        "Bisa karena koneksi terputus, layanan sumber menolak permintaan, atau file dipindahkan. Buka Unduhan lalu tekan Coba lagi. Jika memakai mode WiFi-only, sambungkan WiFi."
    )
)

@Composable
fun HelpScreen(onBack: () -> Unit, onPrivacy: () -> Unit) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { ScreenHeader(title = "Bantuan", onBack = onBack) }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(30.dp),
                color = KiraOliveDeep
            ) {
                Box {
                    DotPattern(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(160.dp),
                        color = KiraCream.copy(alpha = 0.2f)
                    )
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "PUSAT BANTUAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = KiraOliveLight.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Ada yang\nmacet?",
                            fontFamily = ArchivoFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            lineHeight = 38.sp,
                            color = KiraOliveLight
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Jawaban singkat untuk hal yang paling sering terjadi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KiraOliveLight.copy(alpha = 0.78f)
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
                SectionLabel("Pertanyaan umum")
                faqs.forEach { faq ->
                    FaqCard(faq)
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionLabel("Cek cepat")
                KiraCard {
                    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        TipRow(Icons.Rounded.Wifi, "Internet stabil", "Coba matikan VPN atau ganti jaringan.")
                        TipRow(Icons.Rounded.Download, "Penyimpanan cukup", "Sisakan ruang untuk file video HD.")
                        TipRow(Icons.Rounded.HelpOutline, "Postingan publik", "Video privat, dibayar, atau dibatasi wilayah tidak didukung.")
                    }
                }
            }
        }

        item {
            KiraCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Column {
                    Text(
                        text = "Masih butuh bantuan?",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "Hubungi pengembang lewat WhatsApp. Sertakan link dan pesan kesalahan agar lebih cepat diperiksa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(13.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { openUrl(context, "https://wa.me/6281553362795") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "WhatsApp pengembang",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(7.dp))
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .clickable(onClick = onPrivacy)
                    .padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Baca privasi & disclaimer",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FaqCard(faq: Faq) {
    var open by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (open) 45f else 0f, label = "faqRotation")
    KiraCard(
        contentPadding = PaddingValues(0.dp),
        onClick = { open = !open }
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(19.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = open) {
                Text(
                    text = faq.answer,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 17.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TipRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
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
