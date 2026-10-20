package com.hakira.kirasave.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.hakira.kirasave.KiraSaveApp
import com.hakira.kirasave.data.TikTokAuthor
import com.hakira.kirasave.data.TikTokMedia
import com.hakira.kirasave.data.TikTokStats
import com.hakira.kirasave.ui.components.KiraBottomBar
import com.hakira.kirasave.ui.screens.HomeScreen
import com.hakira.kirasave.ui.screens.AboutScreen
import com.hakira.kirasave.ui.screens.OnboardingScreen
import com.hakira.kirasave.ui.screens.PrivacyScreen
import com.hakira.kirasave.ui.screens.ResultScreen
import com.hakira.kirasave.ui.screens.SettingsScreen
import com.hakira.kirasave.ui.screens.DownloadsScreen
import com.hakira.kirasave.ui.screens.LibraryScreen
import com.hakira.kirasave.ui.theme.KiraTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class VisualRegressionTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun onboardingDark360() {
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                OnboardingScreen(onDone = {})
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/onboarding_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-xhdpi")
    fun onboardingLight320() {
        compose.setContent {
            KiraTheme(themeMode = "LIGHT") {
                OnboardingScreen(onDone = {})
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/onboarding_320_light.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun navigationPillDark360() {
        compose.setContent {
            var route by remember { mutableStateOf("home") }
            KiraTheme(themeMode = "DARK") {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    KiraBottomBar(
                        currentRoute = route,
                        onSelect = { route = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/nav_home_360_dark.png"
        )
        compose.onNodeWithContentDescription("Unduhan").performClick()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/nav_downloads_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-xhdpi")
    fun navigationPillLight320() {
        compose.setContent {
            var route by remember { mutableStateOf("library") }
            KiraTheme(themeMode = "LIGHT") {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    KiraBottomBar(
                        currentRoute = route,
                        onSelect = { route = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/nav_library_320_light.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun navigationPillSettingsDark360() {
        compose.setContent {
            var route by remember { mutableStateOf("settings") }
            KiraTheme(themeMode = "DARK") {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    KiraBottomBar(
                        currentRoute = route,
                        onSelect = { route = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/nav_settings_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-xhdpi")
    fun navigationPillEdgesLight320() {
        compose.setContent {
            var route by remember { mutableStateOf("home") }
            KiraTheme(themeMode = "LIGHT") {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    KiraBottomBar(
                        currentRoute = route,
                        onSelect = { route = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/nav_home_320_light.png"
        )
        compose.onNodeWithContentDescription("Setelan").performClick()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/nav_settings_320_light.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun privacyDark360() {
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                PrivacyScreen(onBack = {})
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/privacy_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun homeDark360() {
        val app = ApplicationProvider.getApplicationContext<KiraSaveApp>()
        val viewModel = MainViewModel(app.container)
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                HomeScreen(
                    viewModel = viewModel,
                    onAnalyzeSuccess = {},
                    onBatch = {},
                    onStories = {},
                    onDownloads = {},
                    onLibrary = {},
                    onSettings = {},
                    onHelp = {},
                    onAbout = {}
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/home_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun homeLight360() {
        val app = ApplicationProvider.getApplicationContext<KiraSaveApp>()
        val viewModel = MainViewModel(app.container)
        compose.setContent {
            KiraTheme(themeMode = "LIGHT") {
                HomeScreen(
                    viewModel = viewModel,
                    onAnalyzeSuccess = {},
                    onBatch = {},
                    onStories = {},
                    onDownloads = {},
                    onLibrary = {},
                    onSettings = {},
                    onHelp = {},
                    onAbout = {}
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/home_360_light.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun resultDark360() {        val app = ApplicationProvider.getApplicationContext<KiraSaveApp>()
        val viewModel = MainViewModel(app.container)
        val analysisField = MainViewModel::class.java
            .getDeclaredField("_analysis")
            .apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val analysis = analysisField.get(viewModel) as MutableStateFlow<TikTokMedia?>
        analysis.value = TikTokMedia(
            videoId = "7000000000000000000",
            sourceUrl = "https://www.tiktok.com/@kiraadityaa/video/7000000000000000000",
            title = "Video bersih tanpa watermark dengan caption panjang untuk menguji warna teks dan tata letak di layar hasil analisis.",
            author = TikTokAuthor(
                id = "1",
                handle = "kiraadityaa",
                nickname = "Hakira Aditya"
            ),
            coverUrl = null,
            durationSeconds = 27,
            hdUrl = "https://example.com/video.mp4",
            sdUrl = "https://example.com/video-sd.mp4",
            audioUrl = "https://example.com/music.mp3",
            hdSize = 18_400_000,
            sdSize = 7_200_000,
            stats = TikTokStats(
                views = 1_240_000,
                likes = 98_400,
                comments = 1_210,
                shares = 2_300
            ),
            source = "Parser perangkat"
        )
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                ResultScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onPlay = { _, _ -> },
                    onDownloads = {}
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/result_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun downloadsEmptyDark360() {
        val app = ApplicationProvider.getApplicationContext<KiraSaveApp>()
        val viewModel = MainViewModel(app.container)
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                DownloadsScreen(
                    viewModel = viewModel,
                    onPlay = { _, _ -> },
                    onExplore = {}
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/downloads_empty_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun libraryEmptyDark360() {
        val app = ApplicationProvider.getApplicationContext<KiraSaveApp>()
        val viewModel = MainViewModel(app.container)
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                LibraryScreen(
                    viewModel = viewModel,
                    onPlay = { _, _ -> },
                    onExplore = {}
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/library_empty_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun settingsDark360() {        val app = ApplicationProvider.getApplicationContext<KiraSaveApp>()
        val viewModel = MainViewModel(app.container)
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                SettingsScreen(
                    viewModel = viewModel,
                    onAbout = {},
                    onHelp = {},
                    onPrivacy = {}
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/settings_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun settingsLight360() {
        val app = ApplicationProvider.getApplicationContext<KiraSaveApp>()
        val viewModel = MainViewModel(app.container)
        compose.setContent {
            KiraTheme(themeMode = "LIGHT") {
                SettingsScreen(
                    viewModel = viewModel,
                    onAbout = {},
                    onHelp = {},
                    onPrivacy = {}
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/settings_360_light.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun navBounceFrames() {
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                var route by remember { mutableStateOf("home") }
                Box(modifier = Modifier.fillMaxSize()) {
                    KiraBottomBar(
                        currentRoute = route,
                        onSelect = { route = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithContentDescription("Koleksi").performClick()
        var previous = 0L
        listOf(0L, 40L, 80L, 120L, 180L, 260L, 360L, 500L).forEachIndexed { index, time ->
            compose.mainClock.advanceTimeBy(time - previous)
            previous = time
            compose.onRoot().captureRoboImage(
                "build/outputs/roborazzi/nav_bounce_${index.toString().padStart(2, '0')}.png"
            )
        }
        compose.mainClock.autoAdvance = true
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun aboutDark360() {
        compose.setContent {
            KiraTheme(themeMode = "DARK") {
                AboutScreen(onBack = {}, onPrivacy = {})
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/about_360_dark.png"
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi")
    fun aboutLight360() {
        compose.setContent {
            KiraTheme(themeMode = "LIGHT") {
                AboutScreen(onBack = {}, onPrivacy = {})
            }
        }
        compose.onRoot().captureRoboImage(
            "build/outputs/roborazzi/about_360_light.png"
        )
    }
}
