package com.hakira.kirasave.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hakira.kirasave.ui.components.BrandMark
import com.hakira.kirasave.ui.components.KiraBottomBar
import com.hakira.kirasave.ui.components.KiraNavigationRail
import com.hakira.kirasave.ui.components.mainTabs
import com.hakira.kirasave.ui.screens.AboutScreen
import com.hakira.kirasave.ui.screens.BatchScreen
import com.hakira.kirasave.ui.screens.DownloadsScreen
import com.hakira.kirasave.ui.screens.HelpScreen
import com.hakira.kirasave.ui.screens.HomeScreen
import com.hakira.kirasave.ui.screens.LibraryScreen
import com.hakira.kirasave.ui.screens.OnboardingScreen
import com.hakira.kirasave.ui.screens.PlayerScreen
import com.hakira.kirasave.ui.screens.PrivacyScreen
import com.hakira.kirasave.ui.screens.ResultScreen
import com.hakira.kirasave.ui.screens.SettingsScreen
import com.hakira.kirasave.ui.screens.StoriesScreen
import kotlinx.coroutines.launch

@Composable
fun KiraSaveRoot(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val navController = rememberNavController()
    var permissionsAsked by rememberSaveable { mutableStateOf(false) }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    val storagePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    var playerRequest by remember { mutableStateOf<Pair<String, String>?>(null) }
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "home"
    val showTabs = mainTabs.any { it.route == route }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbar.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sharedLink.collect {
            navController.navigate("home") {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(settings?.onboardingDone) {
        if (settings?.onboardingDone == true && !permissionsAsked) {
            permissionsAsked = true
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT <= 28 &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    if (settings == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            BrandMark(size = 58.dp)
        }
        return
    }

    if (settings?.onboardingDone != true) {
        OnboardingScreen(onDone = viewModel::completeOnboarding)
        return
    }

    BackHandler(enabled = playerRequest != null) {
        playerRequest = null
    }

    val navigateTab: (String) -> Unit = { destination ->
        navController.navigate(destination) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Keep the NavHost at a single stable call site. Recreating it when tabs hide
    // (result/batch/settings sub-pages) or when the player opens would reset navigation.
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val wide = maxWidth >= 840.dp
        Row(modifier = Modifier.fillMaxSize()) {
            if (wide && showTabs) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight()
                ) {
                    KiraNavigationRail(
                        currentRoute = route,
                        onSelect = navigateTab
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppNavHost(
                            navController = navController,
                            viewModel = viewModel,
                            onPlay = { uri, title -> playerRequest = uri to title },
                            onTab = navigateTab,
                            modifier = if (!wide && showTabs) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding()
                            }
                        )
                        SnackbarHost(
                            hostState = snackbar,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp)
                        )
                    }
                    if (!wide && showTabs) {
                        KiraBottomBar(
                            currentRoute = route,
                            onSelect = navigateTab
                        )
                    }
                }
            }
        }

        playerRequest?.let { active ->
            PlayerScreen(
                uri = active.first,
                title = active.second,
                onBack = { playerRequest = null }
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    onPlay: (String, String) -> Unit,
    onTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val openDownloads = { onTab("downloads") }
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAnalyzeSuccess = { navController.navigate("result") },
                onBatch = { navController.navigate("batch") },
                onStories = { navController.navigate("stories") },
                onDownloads = openDownloads,
                onLibrary = { onTab("library") },
                onSettings = { onTab("settings") },
                onHelp = { navController.navigate("help") },
                onAbout = { navController.navigate("about") }
            )
        }
        composable("downloads") {
            DownloadsScreen(
                viewModel = viewModel,
                onPlay = onPlay,
                onExplore = { onTab("home") }
            )
        }
        composable("library") {
            LibraryScreen(
                viewModel = viewModel,
                onPlay = onPlay,
                onExplore = { onTab("home") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onAbout = { navController.navigate("about") },
                onHelp = { navController.navigate("help") },
                onPrivacy = { navController.navigate("privacy") }
            )
        }
        composable("result") {
            ResultScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPlay = onPlay,
                onDownloads = openDownloads
            )
        }
        composable("batch") {
            BatchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDownloads = openDownloads
            )
        }
        composable("stories") {
            StoriesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPlay = onPlay,
                onDownloads = openDownloads
            )
        }
        composable("about") {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onPrivacy = { navController.navigate("privacy") }
            )
        }
        composable("help") {
            HelpScreen(
                onBack = { navController.popBackStack() },
                onPrivacy = { navController.navigate("privacy") }
            )
        }
        composable("privacy") {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }
    }
}
