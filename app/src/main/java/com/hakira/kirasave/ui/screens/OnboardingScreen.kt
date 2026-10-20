package com.hakira.kirasave.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hakira.kirasave.ui.components.BrandMark
import com.hakira.kirasave.ui.components.DotPattern
import com.hakira.kirasave.ui.components.PrimaryPill
import com.hakira.kirasave.ui.theme.ArchivoFamily
import com.hakira.kirasave.ui.theme.KiraCream
import com.hakira.kirasave.ui.theme.KiraInk
import com.hakira.kirasave.ui.theme.KiraOliveLight
import com.hakira.kirasave.ui.theme.KiraOrangeDeep
import com.hakira.kirasave.ui.theme.KiraPeach

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var accepted by rememberSaveable { mutableStateOf(false) }
    val background = MaterialTheme.colorScheme.background
    val isLight = background.luminance() > 0.5f
    val gradientTop = if (isLight) Color(0xFFF8EEE2) else Color(0xFF1D120C)
    val gradientBottom = if (isLight) Color(0xFFEAD7C2) else Color(0xFF24150E)
    val dotColor = if (isLight) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        KiraPeach.copy(alpha = 0.13f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        gradientTop,
                        background,
                        gradientBottom
                    )
                )
            )
    ) {
        DotPattern(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(300.dp),
            color = dotColor,
            spacing = 16.dp
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(size = 40.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "KiraSave",
                    fontFamily = ArchivoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        "V1.0",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(50.dp))
            Surface(
                shape = CircleShape,
                color = KiraOrangeDeep.copy(alpha = 0.85f)
            ) {
                Text(
                    "TIKTOK, TANPA RIBET",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = KiraCream
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "SIMPAN\nYANG\nBERARTI.",
                fontFamily = ArchivoFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 55.sp,
                lineHeight = 52.sp,
                letterSpacing = (-2.2).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Video bersih, carousel foto, dan audio asli. Semua dalam satu aplikasi yang tenang — tanpa iklan, tanpa login.",
                modifier = Modifier.fillMaxWidth(0.92f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(30.dp))
            OnboardFeature(
                icon = Icons.Rounded.Download,
                title = "HD bila tersedia",
                body = "Tampilkan kualitas dan ukuran yang nyata, bukan klaim palsu."
            )
            Spacer(Modifier.height(11.dp))
            OnboardFeature(
                icon = Icons.Rounded.PhotoLibrary,
                title = "Video, foto, audio",
                body = "Carousel dan MP3 asli ikut tersimpan rapi di galeri."
            )
            Spacer(Modifier.height(11.dp))
            OnboardFeature(
                icon = Icons.Rounded.MusicNote,
                title = "M4A dari video",
                body = "Ekstrak audio dari unduhan lama kapan saja."
            )

            Spacer(Modifier.height(26.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { accepted = !accepted }
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(Modifier.width(5.dp))
                    Column {
                        Text(
                            text = "Saya setuju",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Saya hanya akan menyimpan konten publik yang saya miliki atau berhak gunakan, dan memahami bahwa KiraSave tidak berafiliasi dengan TikTok.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            PrimaryPill(
                text = "Mulai menyimpan",
                icon = Icons.Rounded.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                enabled = accepted,
                onClick = onDone
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Tanpa iklan · Tanpa analytics · Dibuat oleh Hakira Aditya",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun OnboardFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
