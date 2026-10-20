package com.hakira.kirasave.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenHeader(title = "Privasi & disclaimer", onBack = onBack) }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Jelas dari awal.",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "KiraSave dibuat untuk menyimpan konten publik yang kamu berhak gunakan. Bukan alat untuk mengambil konten privat, melanggar hak cipta, atau mengumpulkan data orang lain.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        "Terakhir diperbarui · 20 Oktober 2026",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            KiraCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    PolicyBlock(
                        Icons.Rounded.Lock,
                        "Data yang diproses",
                        "KiraSave tidak meminta akun, cookie, kata sandi, atau akses ke kontak. Link yang kamu tempel dan metadata hasil analisis disimpan di perangkat untuk riwayat dan koleksi."
                    )
                    PolicyBlock(
                        Icons.Rounded.Storage,
                        "Pihak ketiga",
                        "Parser on-device dicoba lebih dulu. Jika gagal, URL dikirim ke TikWM (tikwm.com), layanan publik gratis, untuk mendapatkan stream media. Tidak ada iklan, analytics, atau penjualan data. Ketersediaan layanan pihak ketiga dapat berubah."
                    )
                    PolicyBlock(
                        Icons.Rounded.Delete,
                        "Penyimpanan & penghapusan",
                        "File disimpan di galeri perangkat: Movies/KiraSave, Pictures/KiraSave, dan Music/KiraSave. Menghapus item dari Koleksi juga menghapus file dari galeri. Menghapus aplikasi tidak otomatis menghapus file galeri."
                    )
                    PolicyBlock(
                        Icons.Rounded.Balance,
                        "Hak cipta & penggunaan wajar",
                        "Gunakan hanya video, foto, musik, atau Stories yang kamu miliki atau punya izin untuk simpan. Jangan mengunduh konten privat, berbagai akun berbayar, atau menggunakan karya orang lain untuk pelanggaran hak cipta. Tanggung jawab sepenuhnya ada pada pengguna."
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                SectionLabel("Batasan layanan")
                KiraCard(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Bullet("Hanya konten publik. Video privat, login-only, atau dibatasi wilayah tidak didukung.")
                        Bullet("Tidak ada jaminan 1080p. Kualitas mengikuti bitrate yang benar-benar disediakan sumber.")
                        Bullet("Stories bersifat kondisional dan dapat berhenti tersedia tanpa pemberitahuan.")
                        Bullet("KiraSave tidak berafiliasi dengan TikTok, ByteDance, atau TikWM.")
                        Bullet("Layanan gratis dapat mengalami rate limit, Cloudflare, atau gangguan sementara.")
                    }
                }
            }
        }

        item {
            KiraCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Column {
                    Text(
                        text = "Pertanyaan privasi",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "Hubungi Hakira Aditya melalui WhatsApp +62 815-5336-2795 atau TikTok @kiraadityaa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyBlock(icon: ImageVector, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .width(22.dp)
        )
        Spacer(Modifier.width(11.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "•",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
