package com.example.navigation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.HyperOsIconRepository
import com.example.ui.HosViewModel
import com.example.ui.screens.IconCustomizationScreen
import com.example.ui.screens.IconLibraryScreen
import com.example.ui.screens.MyWidgetsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HosSurface

@Composable
fun HosNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    hosViewModel: HosViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Automatically synchronize with AppWidgetManager whenever returning from the launcher
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hosViewModel.refreshWidgets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val searchQuery by hosViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by hosViewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedOsVersion by hosViewModel.selectedOsVersion.collectAsStateWithLifecycle()
    val filteredIcons by hosViewModel.filteredIcons.collectAsStateWithLifecycle()
    val configuredWidgets by hosViewModel.configuredWidgets.collectAsStateWithLifecycle()
    val userSettings by hosViewModel.userSettings.collectAsStateWithLifecycle()
    val manualPinGuide by hosViewModel.manualPinGuide.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = HosDestinations.ROUTE_ICON_LIBRARY,
        modifier = modifier
            .fillMaxSize()
            .background(HosSurface),
        enterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(initialOffsetX = { it / 8 }, animationSpec = tween(220))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(200))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutHorizontally(targetOffsetX = { it / 8 }, animationSpec = tween(180))
        }
    ) {
        composable(route = HosDestinations.ROUTE_ICON_LIBRARY) {
            IconLibraryScreen(
                icons = filteredIcons,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                selectedOsVersion = selectedOsVersion,
                configuredWidgetCount = configuredWidgets.size,
                onSearchQueryChange = hosViewModel::updateSearchQuery,
                onCategorySelected = hosViewModel::selectCategory,
                onOsVersionSelected = hosViewModel::selectOsVersion,
                onIconSelected = { appIcon ->
                    navController.navigate(HosDestinations.customizeRoute(appIcon.id))
                },
                onOpenMyWidgets = {
                    navController.navigate(HosDestinations.ROUTE_MY_WIDGETS)
                },
                onOpenSettings = {
                    navController.navigate(HosDestinations.ROUTE_SETTINGS)
                }
            )
        }

        composable(
            route = HosDestinations.ROUTE_CUSTOMIZE,
            arguments = listOf(
                navArgument(HosDestinations.ARG_APP_ID) {
                    type = NavType.StringType
                    defaultValue = "security"
                }
            )
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString(HosDestinations.ARG_APP_ID) ?: "security"
            val appIcon = HyperOsIconRepository.getIconById(appId)

            IconCustomizationScreen(
                appIcon = appIcon,
                initialSize = userSettings.defaultSize,
                showLabelOn2x2 = userSettings.showLabelOn2x2,
                manualPinGuide = manualPinGuide,
                onDismiss = { navController.popBackStack() },
                onAddWidgetToHomeScreen = { chosenSize ->
                    hosViewModel.requestAddWidgetToHomeScreen(
                        context = context,
                        appIcon = appIcon,
                        sizeOption = chosenSize
                    )
                },
                onTestLaunchApp = {
                    hosViewModel.launchApp(context, appIcon)
                },
                onDismissManualPinGuide = hosViewModel::dismissManualPinGuide
            )
        }

        composable(route = HosDestinations.ROUTE_MY_WIDGETS) {
            MyWidgetsScreen(
                widgets = configuredWidgets,
                onBack = { navController.popBackStack() },
                onSyncWidgets = {
                    hosViewModel.refreshWidgets()
                    Toast.makeText(
                        context,
                        context.getString(R.string.widgets_synced_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onOpenCustomize = { appIcon ->
                    navController.navigate(HosDestinations.customizeRoute(appIcon.id))
                },
                onLaunchApp = { appIcon ->
                    hosViewModel.launchApp(context, appIcon)
                },
                onRemoveWidget = { instance ->
                    hosViewModel.removeWidgetConfiguration(
                        context = context,
                        appWidgetId = instance.appWidgetId,
                        appName = instance.appIcon.name
                    )
                },
                onBrowseIcons = {
                    navController.popBackStack(
                        route = HosDestinations.ROUTE_ICON_LIBRARY,
                        inclusive = false
                    )
                }
            )
        }

        composable(route = HosDestinations.ROUTE_SETTINGS) {
            SettingsScreen(
                userSettings = userSettings,
                isPinSupported = hosViewModel.isPinSupported(context),
                onBack = { navController.popBackStack() },
                onToggleShowLabelOn2x2 = { enabled ->
                    hosViewModel.setShowLabelOn2x2(context, enabled)
                },
                onDefaultSizeSelected = { size ->
                    hosViewModel.setDefaultWidgetSize(context, size)
                },
                onRefreshAllWidgets = {
                    hosViewModel.refreshWidgets()
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_refresh_done),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onCleanUpStaleWidgets = {
                    hosViewModel.pruneStaleWidgets(context)
                }
            )
        }
    }
}
