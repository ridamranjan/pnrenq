
package com.first.pnrenq.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.first.pnrenq.ui.add.AddPnrScreen
import com.first.pnrenq.ui.details.DetailsScreen
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.settings.SettingsScreen
import com.first.pnrenq.ui.state.PnrNavigationEvent
import com.first.pnrenq.ui.state.PnrViewModel
import com.first.pnrenq.ui.theme.AppThemeMode
import com.first.pnrenq.ui.theme.AppThemePalette
import com.first.pnrenq.ui.theme.ThemeSettings

/* =========================================================
   ROUTES
========================================================= */

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SETTINGS = "settings"

    const val DETAILS_ARG = "pnr"
    const val DETAILS = "details/{$DETAILS_ARG}"

    fun details(pnr: String): String {
        return "details/$pnr"
    }
}

/* =========================================================
   APP NAVIGATION
========================================================= */

@Composable
fun AppNavigation(
    themeSettings: ThemeSettings,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onThemePaletteSelected: (AppThemePalette) -> Unit,
    onThemeReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val pnrViewModel: PnrViewModel = viewModel()

    val state by pnrViewModel.state.collectAsState()

    /* =====================================================
       NAVIGATION EVENTS
    ===================================================== */

    LaunchedEffect(navController, pnrViewModel) {
        pnrViewModel.navigationEvents.collect { event ->

            val currentEntry =
                navController.currentBackStackEntry

            val currentRoute =
                currentEntry?.destination?.route

            when (event) {

                is PnrNavigationEvent.OpenDetails -> {
                    if (currentRoute == Routes.ADD) {
                        navController.navigate(
                            Routes.details(event.pnr)
                        ) {
                            popUpTo(Routes.ADD) {
                                inclusive = true
                            }
                        }
                    }
                }

                is PnrNavigationEvent.ReturnAfterDelete -> {
                    val activePnr = currentEntry
                        ?.arguments
                        ?.getString(Routes.DETAILS_ARG)

                    if (
                        currentRoute == Routes.DETAILS &&
                        activePnr == event.pnr
                    ) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    /* =====================================================
       NAV HOST
    ===================================================== */

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,

        /* ---------- Forward enter ---------- */

        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                )
            ) + slideInHorizontally(
                initialOffsetX = { width ->
                    width / 6
                },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        },

        /* ---------- Forward exit ---------- */

        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = 200
                )
            ) + slideOutHorizontally(
                targetOffsetX = { width ->
                    -width / 12
                },
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            )
        },

        /* ---------- Back re-enter ---------- */

        popEnterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 260
                )
            ) + slideInHorizontally(
                initialOffsetX = { width ->
                    -width / 8
                },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        },

        /* ---------- Back exit ---------- */

        popExitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = 200
                )
            ) + slideOutHorizontally(
                targetOffsetX = { width ->
                    width / 6
                },
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            )
        }
    ) {

        /* =================================================
           HOME SCREEN
        ================================================= */

        composable(Routes.HOME) {

            val homeError =
                state.detailsMessage?.takeIf {
                    it.startsWith(
                        "Unable to delete",
                        ignoreCase = true
                    )
                }

            HomeScreen(
                pnrs = state.pnrs,

                isLoading = state.isLoading,

                loadError = state.loadError,

                deletingPnrs = state.deletingPnrs,

                homeMessage = homeError,

                onHomeMessageShown = {
                    pnrViewModel.clearDetailsMessage()
                },

                onRetry = {
                    pnrViewModel.loadSavedPnrs()
                },

                onAddClick = {
                    pnrViewModel.clearAddError()

                    navController.navigate(Routes.ADD)
                },

                onPnrClick = { pnr ->
                    pnrViewModel.clearDetailsMessage()

                    navController.navigate(
                        Routes.details(pnr)
                    )
                },

                onSettingsClick = {
                    navController.navigate(
                        Routes.SETTINGS
                    )
                },

                onDeletePnr = { pnr ->
                    pnrViewModel.deletePnr(pnr)
                }
            )
        }

        /* =================================================
           ADD PNR
        ================================================= */

        composable(Routes.ADD) {
            AddPnrScreen(
                isChecking = state.isChecking,

                errorMessage = state.addError,

                onBack = {
                    navController.popBackStack()
                },

                onCheck = { pnr ->
                    pnrViewModel.checkPnr(pnr)
                }
            )
        }

        /* =================================================
           PNR DETAILS
        ================================================= */

        composable(
            route = Routes.DETAILS,

            arguments = listOf(
                navArgument(Routes.DETAILS_ARG) {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            val pnrNumber = entry.arguments
                ?.getString(Routes.DETAILS_ARG)
                .orEmpty()

            val currentPnr = state.pnrs.firstOrNull {
                it.pnrNumber == pnrNumber
            }

            val message = if (
                state.messagePnr == pnrNumber
            ) {
                state.detailsMessage
            } else {
                null
            }

            DetailsScreen(
                pnr = currentPnr,

                isLoading = state.isLoading,

                isRefreshing =
                    pnrNumber in state.refreshingPnrs ||
                            pnrNumber in state.deletingPnrs,

                message = message,

                onMessageShown = {
                    pnrViewModel.clearDetailsMessage()
                },

                onRefresh = {
                    pnrViewModel.refreshPnr(pnrNumber)
                },

                onDelete = {
                    pnrViewModel.deletePnr(pnrNumber)
                },

                onBack = {
                    navController.popBackStack()
                }
            )
        }

        /* =================================================
           THEMES / APPEARANCE
        ================================================= */

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = themeSettings,

                onModeSelected = onThemeModeSelected,

                onPaletteSelected = onThemePaletteSelected,

                onReset = onThemeReset,

                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
