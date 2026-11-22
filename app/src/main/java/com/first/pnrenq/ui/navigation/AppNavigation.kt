
package com.first.pnrenq.ui.navigation

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
import com.first.pnrenq.ui.common.PlaceholderScreen
import com.first.pnrenq.ui.details.DetailsScreen
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.state.PnrNavigationEvent
import com.first.pnrenq.ui.state.PnrViewModel

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SETTINGS = "settings"
    const val DETAILS_ARG = "pnr"
    const val DETAILS = "details/{$DETAILS_ARG}"

    fun details(pnr: String) = "details/$pnr"
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    val pnrViewModel: PnrViewModel = viewModel()
    val state by pnrViewModel.state.collectAsState()

    LaunchedEffect(navController, pnrViewModel) {
        pnrViewModel.navigationEvents.collect { event ->
            val currentEntry = navController.currentBackStackEntry
            val currentRoute = currentEntry?.destination?.route

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

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                pnrs = state.pnrs,
                isLoading = state.isLoading,
                loadError = state.loadError,
                onRetry = {
                    pnrViewModel.loadSavedPnrs()
                },
                onAddClick = {
                    pnrViewModel.clearAddError()
                    navController.navigate(Routes.ADD)
                },
                onPnrClick = { pnr ->
                    pnrViewModel.clearDetailsMessage()
                    navController.navigate(Routes.details(pnr))
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

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

            val message =
                if (state.messagePnr == pnrNumber) {
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

        composable(Routes.SETTINGS) {
            PlaceholderScreen(
                title = "Settings",
                message = "Settings screen arrives in a later batch.",
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
