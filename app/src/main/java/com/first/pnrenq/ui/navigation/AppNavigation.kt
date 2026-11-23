
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

import com.first.pnrenq.ui.theme.AppScreenBackground
import com.first.pnrenq.ui.theme.PnrScreenType

import com.first.pnrenq.ui.theme.AppThemeMode
import com.first.pnrenq.ui.theme.AppThemePalette
import com.first.pnrenq.ui.theme.AppThemeStyle
import com.first.pnrenq.ui.theme.AppTimeFormat
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

    fun details(
        pnr: String
    ): String = "details/$pnr"
}

/* =========================================================
   APPLICATION NAVIGATION

   Each destination has its own background.

   HOME     -> Signature
   DETAILS  -> Journey
   ADD      -> Focus
   SETTINGS -> Ambient
========================================================= */

@Composable
fun AppNavigation(
    themeSettings: ThemeSettings,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onThemePaletteSelected: (AppThemePalette) -> Unit,
    onThemeStyleSelected: (AppThemeStyle) -> Unit,
    onTimeFormatSelected: (AppTimeFormat) -> Unit,
    onThemeReset: () -> Unit,
    modifier: Modifier = Modifier
) {

    val navController =
        rememberNavController()

    val pnrViewModel: PnrViewModel =
        viewModel()

    val state by pnrViewModel.state.collectAsState()

    /* =====================================================
       NAVIGATION EVENTS

       Preserve existing check/delete behavior.
    ===================================================== */

    LaunchedEffect(
        navController,
        pnrViewModel
    ) {

        pnrViewModel.navigationEvents.collect { event ->

            val entry =
                navController.currentBackStackEntry

            val route =
                entry?.destination?.route

            when (event) {

                is PnrNavigationEvent.OpenDetails -> {

                    if (route == Routes.ADD) {

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

                    val activePnr = entry?.arguments
                        ?.getString(
                            Routes.DETAILS_ARG
                        )

                    if (
                        route == Routes.DETAILS &&
                        activePnr == event.pnr
                    ) {

                        navController.popBackStack()
                    }
                }
            }
        }
    }

    /* =====================================================
       NAVIGATION HOST

       Existing fade/slide transitions are retained.
    ===================================================== */

    NavHost(
        navController = navController,

        startDestination = Routes.HOME,

        modifier = modifier,

        /* ---------- Forward entrance ---------- */

        enterTransition = {

            fadeIn(
                animationSpec = tween(260)
            ) +

                    slideInHorizontally(
                        initialOffsetX = {
                            it / 6
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
                animationSpec = tween(200)
            ) +

                    slideOutHorizontally(
                        targetOffsetX = {
                            -it / 12
                        },

                        animationSpec = tween(280)
                    )
        },

        /* ---------- Back entrance ---------- */

        popEnterTransition = {

            fadeIn(
                animationSpec = tween(260)
            ) +

                    slideInHorizontally(
                        initialOffsetX = {
                            -it / 8
                        },

                        animationSpec = tween(300)
                    )
        },

        /* ---------- Back exit ---------- */

        popExitTransition = {

            fadeOut(
                animationSpec = tween(200)
            ) +

                    slideOutHorizontally(
                        targetOffsetX = {
                            it / 6
                        },

                        animationSpec = tween(280)
                    )
        }
    ) {

        /* =================================================
           HOME SCREEN

           Uses Signature background.
        ================================================= */

        composable(Routes.HOME) {

            AppScreenBackground(
                screen = PnrScreenType.HOME
            ) {

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

                        navController.navigate(
                            Routes.ADD
                        )
                    },

                    onPnrClick = { pnr ->

                        pnrViewModel.clearDetailsMessage()

                        navController.navigate(
                            Routes.details(pnr)
                        )
                    },

                    onDeletePnr = { pnr ->

                        pnrViewModel.deletePnr(
                            pnr
                        )
                    },

                    onSettingsClick = {

                        navController.navigate(
                            Routes.SETTINGS
                        )
                    }
                )
            }
        }

        /* =================================================
           ADD PNR SCREEN

           Uses Focus background.
        ================================================= */

        composable(Routes.ADD) {

            AppScreenBackground(
                screen = PnrScreenType.ADD_PNR
            ) {

                AddPnrScreen(
                    isChecking = state.isChecking,

                    errorMessage = state.addError,

                    onBack = {

                        navController.popBackStack()
                    },

                    onCheck = { pnr ->

                        pnrViewModel.checkPnr(
                            pnr
                        )
                    }
                )
            }
        }

        /* =================================================
           PNR DETAILS SCREEN

           Uses Journey background.
        ================================================= */

        composable(
            route = Routes.DETAILS,

            arguments = listOf(
                navArgument(
                    Routes.DETAILS_ARG
                ) {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            AppScreenBackground(
                screen = PnrScreenType.DETAILS
            ) {

                val pnrNumber = entry.arguments
                    ?.getString(
                        Routes.DETAILS_ARG
                    )
                    .orEmpty()

                val currentPnr =
                    state.pnrs.firstOrNull {

                        it.pnrNumber == pnrNumber
                    }

                val message =
                    if (
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

                        pnrViewModel.refreshPnr(
                            pnrNumber
                        )
                    },

                    onDelete = {

                        pnrViewModel.deletePnr(
                            pnrNumber
                        )
                    },

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }
        }

        /* =================================================
           SETTINGS SCREEN

           Uses Ambient background.
        ================================================= */

        composable(Routes.SETTINGS) {

            AppScreenBackground(
                screen = PnrScreenType.SETTINGS
            ) {

                SettingsScreen(
                    settings = themeSettings,

                    onModeSelected =
                        onThemeModeSelected,

                    onPaletteSelected =
                        onThemePaletteSelected,

                    onStyleSelected =
                        onThemeStyleSelected,

                    onTimeFormatSelected =
                        onTimeFormatSelected,

                    onReset = onThemeReset,

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
