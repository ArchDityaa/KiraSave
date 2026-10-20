package com.hakira.kirasave.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hakira.kirasave.BuildConfig
import com.hakira.kirasave.data.AppSettings
import com.hakira.kirasave.data.formatBytes
import com.hakira.kirasave.ui.MainViewModel
import com.hakira.kirasave.ui.components.KiraCard
import com.hakira.kirasave.ui.components.ScreenHeader
import com.hakira.kirasave.ui.components.SectionLabel
import com.hakira.kirasave.ui.components.SocialBadge
import com.hakira.kirasave.ui.components.openUrl
import com.hakira.kirasave.ui.theme.KiraInk
import com.hakira.kirasave.ui.theme.KiraOliveLight
import com.hakira.kirasave.ui.theme.KiraOrangeDeep
import com.hakira.kirasave.ui.theme.KiraPeach

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
    onPrivacy: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val totalBytes by viewModel.totalBytes.collectAsState()
    val current = settings ?: AppSettings()
    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotifications(granted)
        if (!granted) viewModel.emit("Izin notifikasi ditolak. Progres tetap terlihat di aplikasi.")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Setelan",
                subtitle = "Sesuaikan KiraSave dengan cara kamu menyimpan"
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionLabel("Unduhan")
                KiraCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                    Column {
                        SettingSwitchRow(
                            icon = Icons.Rounded.Wifi,
                            title = "Hanya lewat WiFi",
                            subtitle = "Tunda antrean saat memakai data seluler",
                            checked = current.wifiOnly,
                            onCheckedChange = viewModel::setWifiOnly
                        )
                        SettingDivider()
                        SettingSwitchRow(
                            icon = Icons.Rounded.Notifications,
                            title = "Notifikasi",
                            subtitle = "Progres dan hasil unduhan",
                            checked = current.notifications,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= 33) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotifications(checked)
                                }
                            }
                        )
                        SettingDivider()
                        SettingSwitchRow(
                            icon = Icons.Rounded.AutoAwesome,
                            title = "Tempel otomatis",
                            subtitle = "Ambil link TikTok dari clipboard saat membuka Beranda",
                            checked = current.autoPaste,
                            onCheckedChange = viewModel::setAutoPaste
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
                SectionLabel("Kualitas default")
                KiraCard(contentPadding = PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Kualitas yang dipilih otomatis setelah analisis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SegmentedRow(
                            options = listOf("HD" to "HD", "SD" to "SD"),
                            selected = current.defaultQuality,
                            onSelect = viewModel::setDefaultQuality
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
                SectionLabel("Antrean paralel")
                KiraCard(contentPadding = PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Jumlah unduhan yang berjalan bersamaan. Lebih banyak lebih cepat, tetapi lebih berat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SegmentedRow(
                            options = listOf(
                                "1" to "1",
                                "2" to "2",
                                "3" to "3",
                                "4" to "4"
                            ),
                            selected = current.maxConcurrent.toString(),
                            onSelect = { viewModel.setMaxConcurrent(it.toIntOrNull() ?: 2) }
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
                SectionLabel("Tampilan")
                KiraCard(contentPadding = PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Rounded.SettingsBrightness)
                            Spacer(Modifier.width(11.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tema", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Gelap hangat, terang, atau ikuti sistem",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            ThemeChip(
                                label = "Gelap",
                                icon = Icons.Rounded.DarkMode,
                                selected = current.theme == "DARK",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setTheme("DARK") }
                            )
                            ThemeChip(
                                label = "Terang",
                                icon = Icons.Rounded.LightMode,
                                selected = current.theme == "LIGHT",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setTheme("LIGHT") }
                            )
                            ThemeChip(
                                label = "Sistem",
                                icon = Icons.Rounded.SettingsBrightness,
                                selected = current.theme == "SYSTEM",
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setTheme("SYSTEM") }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIcon(Icons.Rounded.Language)
                            Spacer(Modifier.width(11.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bahasa", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Indonesia",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    "ID",
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
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
                SectionLabel("Info")
                KiraCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                    Column {
                        SettingLinkRow(
                            icon = Icons.Rounded.Storage,
                            title = "Penyimpanan",
                            subtitle = "${formatBytes(totalBytes)} tersimpan · Movies/KiraSave",
                            onClick = {}
                        )
                        SettingDivider()
                        SettingLinkRow(
                            icon = Icons.Rounded.HelpOutline,
                            title = "Bantuan & FAQ",
                            subtitle = "Cara memakai dan pemecahan masalah",
                            onClick = onHelp
                        )
                        SettingDivider()
                        SettingLinkRow(
                            icon = Icons.Rounded.PrivacyTip,
                            title = "Privasi & disclaimer",
                            subtitle = "Data, hak cipta, dan batasan layanan",
                            onClick = onPrivacy
                        )
                        SettingDivider()
                        SettingLinkRow(
                            icon = Icons.Rounded.Info,
                            title = "Tentang KiraSave",
                            subtitle = "Versi ${BuildConfig.VERSION_NAME} · dibuat oleh Hakira Aditya",
                            onClick = onAbout
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
                SectionLabel("Pengembang")
                KiraCard(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "HA",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Hakira Aditya",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    "Pengembang independen · Indonesia",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable(onClick = onAbout)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            SocialBadge(
                                label = "TT",
                                name = "TikTok",
                                color = KiraOliveLight,
                                onClick = {
                                    openUrl(context, "https://www.tiktok.com/@kiraadityaa")
                                }
                            )
                            SocialBadge(
                                label = "IG",
                                name = "Instagram",
                                color = KiraPeach,
                                onClick = {
                                    openUrl(context, "https://instagram.com/aaaditz_")
                                }
                            )
                            SocialBadge(
                                label = "WA",
                                name = "WhatsApp",
                                color = Color(0xFF9AE6B4),
                                onClick = { openUrl(context, "https://wa.me/6281553362795") }
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "KiraSave tidak berafiliasi dengan TikTok. Semua merek adalah milik pemiliknya.",
                modifier = Modifier.padding(horizontal = 28.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon)
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon)
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 15.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    )
}

@Composable
private fun SegmentedRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            options.forEach { (label, value) ->
                val active = selected == value
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    onClick = { onSelect(value) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
