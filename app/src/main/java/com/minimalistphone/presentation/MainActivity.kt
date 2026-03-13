package com.minimalistphone.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.minimalistphone.presentation.delay.AppDelayScreen
import com.minimalistphone.presentation.focusmode.FocusModeScreen
import com.minimalistphone.presentation.home.HomeScreen
import com.minimalistphone.presentation.home.HomeViewModel
import com.minimalistphone.presentation.home.AppLaunchEvent
import com.minimalistphone.presentation.screentime.ScreenTimeScreen
import com.minimalistphone.presentation.search.SearchScreen
import com.minimalistphone.presentation.settings.SettingsScreen
import com.minimalistphone.ui.theme.MinimalistTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MinimalistTheme {
                val navController = rememberNavController()
                val homeViewModel: HomeViewModel = hiltViewModel()
                val pinnedApps by homeViewModel.pinnedApps.collectAsState()
                val searchableApps by homeViewModel.searchableApps.collectAsState()
                val allApps by homeViewModel.allApps.collectAsState()
                val pinnedPackages by homeViewModel.pinnedPackages.collectAsState()
                val hiddenPackages by homeViewModel.hiddenPackages.collectAsState()
                val blockedPackages by homeViewModel.blockedPackages.collectAsState()
                val notifControl by homeViewModel.notifControl.collectAsState()
                val grayscaleMode by homeViewModel.grayscaleMode.collectAsState()
                val mindfulnessPrompts by homeViewModel.mindfulnessPrompts.collectAsState()

                LaunchedEffect(homeViewModel) {
                    homeViewModel.launchEvent.collect { event ->
                        when (event) {
                            is AppLaunchEvent.RequiresDelay -> {
                                navController.navigate("delay/${event.packageName}")
                            }
                            is AppLaunchEvent.Blocked -> {
                                navController.navigate("blocked")
                            }
                        }
                    }
                }

                val grayscaleModifier = if (grayscaleMode) {
                    Modifier.drawWithContent {
                        val saturationPaint = androidx.compose.ui.graphics.Paint().apply {
                            asFrameworkPaint().colorFilter = android.graphics.ColorMatrixColorFilter(
                                android.graphics.ColorMatrix().apply { setSaturation(0f) }
                            )
                        }
                        drawIntoCanvas { canvas ->
                            canvas.saveLayer(
                                androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                                saturationPaint
                            )
                            drawContent()
                            canvas.restore()
                        }
                    }
                } else Modifier

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = grayscaleModifier
                ) {
                    composable("home") {
                        HomeScreen(
                            pinnedApps = pinnedApps,
                            onAppClick = { packageName -> homeViewModel.onAppClicked(packageName) },
                            onSwipeLeft = { navController.navigate("search") },
                            showMindfulness = mindfulnessPrompts
                        )
                    }
                    composable("search") {
                        SearchScreen(
                            allApps = searchableApps,
                            onAppClick = { packageName -> homeViewModel.onAppClicked(packageName) },
                            onGoBack = { navController.popBackStack() },
                            onNavigateSettings = { navController.navigate("settings") },
                            onHideApp = { homeViewModel.toggleHidden(it) },
                            onBlockApp = { homeViewModel.toggleBlocked(it) }
                        )
                    }
                    composable("delay/{packageName}") { backStackEntry ->
                        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                        AppDelayScreen(
                            appName = packageName.split(".").last().capitalize(),
                            onCancel = { navController.popBackStack() },
                            onContinue = { 
                                homeViewModel.launchAppDirectly(packageName)
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("focus") {
                        FocusModeScreen(
                            onEndSession = {
                                homeViewModel.setFocusMode(false)
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            allApps = allApps,
                            pinnedPackages = pinnedPackages,
                            hiddenPackages = hiddenPackages,
                            blockedPackages = blockedPackages,
                            notifControl = notifControl,
                            grayscaleMode = grayscaleMode,
                            mindfulnessPrompts = mindfulnessPrompts,
                            onTogglePinned = { homeViewModel.togglePinned(it) },
                            onToggleHidden = { homeViewModel.toggleHidden(it) },
                            onToggleBlocked = { homeViewModel.toggleBlocked(it) },
                            onNavigateFocusSession = {
                                homeViewModel.setFocusMode(true)
                                navController.navigate("focus")
                            },
                            onNavigateScreenTime = { navController.navigate("screentime") },
                            onCycleNotifControl = { homeViewModel.cycleNotifControl() },
                            onToggleGrayscale = { homeViewModel.toggleGrayscale() },
                            onToggleMindfulness = { homeViewModel.toggleMindfulness() },
                            onGoBack = { navController.popBackStack() }
                        )
                    }
                    composable("screentime") {
                        ScreenTimeScreen(
                            onGoBack = { navController.popBackStack() },
                            onNavigateSettings = { navController.navigate("settings") },
                            onNavigateFocus = { navController.navigate("focus") }
                        )
                    }
                    composable("blocked") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(com.minimalistphone.ui.theme.TrueBlack)
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                androidx.compose.material3.Text(
                                    text = "This app is blocked right now.",
                                    fontSize = 20.sp,
                                    color = com.minimalistphone.ui.theme.MinimalistGrey,
                                    fontWeight = FontWeight.Light
                                )
                                androidx.compose.material3.Text(
                                    text = "Stay focused.",
                                    fontSize = 14.sp,
                                    color = com.minimalistphone.ui.theme.MinimalistGrey.copy(alpha = 0.5f)
                                )
                                androidx.compose.material3.Text(
                                    text = "Tap to go back",
                                    fontSize = 11.sp,
                                    color = com.minimalistphone.ui.theme.MinimalistGrey.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(top = 32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Deprecated("Deprecated in Java", ReplaceWith("moveTaskToBack(true)"))
    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}