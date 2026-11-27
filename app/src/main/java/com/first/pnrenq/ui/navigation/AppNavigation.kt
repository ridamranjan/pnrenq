package com.first.pnrenq.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.first.pnrenq.BuildConfig
import com.first.pnrenq.ui.add.AddPnrScreen
import com.first.pnrenq.ui.details.DetailsScreen
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.legal.PoliciesScreen
import com.first.pnrenq.ui.settings.SettingsScreen

import com.first.pnrenq.ui.state.PnrNavigationEvent
import com.first.pnrenq.ui.state.PnrViewModel
import com.first.pnrenq.ui.state.homeFeedback
import com.first.pnrenq.ui.state.detailsFeedback
import com.first.pnrenq.ui.state.busyPnrs
import com.first.pnrenq.ui.state.recordState
import com.first.pnrenq.ui.state.databaseReadFailed

import com.first.pnrenq.ui.theme.AppScreenBackground
import com.first.pnrenq.ui.theme.PnrScreenType
import com.first.pnrenq.ui.theme.AppThemeMode
import com.first.pnrenq.ui.theme.AppThemePalette
import com.first.pnrenq.ui.theme.AppThemeStyle
import com.first.pnrenq.ui.theme.AppTimeFormat
import com.first.pnrenq.ui.theme.ThemeSettings

import java.util.UUID
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

/* =========================================================
   APPLICATION NAVIGATION
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
    val navController = rememberNavController()
    val context = LocalContext.current
    val pnrViewModel: PnrViewModel = viewModel()

    val lifecycleOwner = LocalLifecycleOwner.current
    val state by pnrViewModel.state.collectAsStateWithLifecycle()

    /* =====================================================
       EXTERNAL LINK HANDLING
    ===================================================== */

    fun openExternalLink(url: String) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            ).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }

            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No browser available to open this link.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (_: SecurityException) {
            Toast.makeText(
                context,
                "Unable to open this external link.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (_: IllegalArgumentException) {
            Toast.makeText(
                context,
                "Invalid external link.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /* =====================================================
       PROJECT LINKS

       Repository URL comes from BuildConfig,
       generated from ignored local.properties.

       No repository URL is hardcoded here.
    ===================================================== */

    fun openProjectLink(path: String = "") {
        val repo = BuildConfig.GITHUB_REPO_URL
            .trim()
            .trimEnd('/')

        if (repo.isBlank()) {
            Toast.makeText(
                context,
                "Project link is not configured.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val parsedRepo = Uri.parse(repo)

        if (
            parsedRepo.scheme != "https" ||
            parsedRepo.host != "github.com" ||
            parsedRepo.encodedQuery != null ||
            parsedRepo.encodedFragment != null
        ) {
            Toast.makeText(
                context,
                "Invalid project repository URL.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        openExternalLink(repo + path)
    }

    /* =====================================================
       VIEWMODEL NAVIGATION EVENTS
    ===================================================== */

    LaunchedEffect(navController, pnrViewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Do not acknowledge events until NavHost has an active entry.
            if (navController.currentBackStackEntry == null) {
                navController.currentBackStackEntryFlow.first()
            }

            pnrViewModel.navigationEvents.collect { event ->
                val entry = navController.currentBackStackEntry
                val route = entry?.destination?.route

                val requestId = if (route == Routes.ADD) {
                    entry?.savedStateHandle?.get<String>(ADD_REQUEST_KEY)
                } else {
                    null
                }

                if (
                    shouldHandleNavigationEvent(
                        event = event,
                        route = route,
                        activeEntryId = entry?.id,
                        activePnr = entry?.arguments?.getString(Routes.DETAILS_ARG),
                        activeAddRequestId = requestId
                    )
                ) {
                    when (event) {
                        is PnrNavigationEvent.OpenDetails -> {
                            navController.navigate(Routes.details(event.pnr)) {
                                popUpTo(Routes.ADD) { inclusive = true }
                            }
                        }

                        is PnrNavigationEvent.ReturnAfterDelete -> {
                            navController.popBackStack()
                        }
                    }
                }

                // Stale events are deliberately acknowledged without navigation.
            }
        }
    }

    /* =====================================================
       NAVIGATION HOST

       All regular and predictive-back screen
       transitions remain disabled.
    ===================================================== */

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        predictivePopEnterTransition = { EnterTransition.None },
        predictivePopExitTransition = { ExitTransition.None }
    ) {

        /* =================================================
           HOME
        ================================================= */

        composable(Routes.HOME) { entry ->
            AppScreenBackground(screen = PnrScreenType.HOME) {
                val openAddPnr: () -> Unit = {
                    if (navController.currentBackStackEntry?.id == entry.id) {
                        pnrViewModel.clearAddError()

                        navController.navigate(Routes.ADD) {
                            launchSingleTop = true
                        }
                    }
                }

                HomeScreen(
                    pnrs = state.pnrs,
                    isLoading = state.isLoading,
                    loadError = state.loadError,
                    unreadablePnrs = state.unreadablePnrs,
                    deletingPnrs = state.deletingPnrs,
                    busyPnrs = state.busyPnrs(),
                    feedback = state.homeFeedback(),
                    onFeedbackShown = pnrViewModel::clearOperationMessage,
                    onRetry = { pnrViewModel.loadSavedPnrs() },
                    onAddClick = openAddPnr,
                    onPnrClick = { pnr ->
                        if (
                            navController.currentBackStackEntry?.id == entry.id &&
                            pnr !in pnrViewModel.state.value.deletingPnrs
                        ) {
                            navController.navigate(Routes.details(pnr)) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onDeletePnr = { pnr ->
                        if (
                            navController.currentBackStackEntry?.id == entry.id &&
                            !pnrViewModel.state.value.isBusy(pnr)
                        ) {
                            pnrViewModel.deletePnr(
                                pnr,
                                originEntryId = entry.id
                            )
                        }
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        /* =================================================
           ADD PNR
        ================================================= */

        composable(Routes.ADD) { entry ->
            AppScreenBackground(screen = PnrScreenType.ADD_PNR) {
                AddPnrScreen(
                    isChecking = state.isChecking,
                    errorMessage = state.addError,
                    onBack = {
                        if (
                            navController.currentBackStackEntry?.id == entry.id &&
                            !pnrViewModel.state.value.isChecking
                        ) {
                            navController.popBackStack()
                        }
                    },
                    onCheck = { pnr ->
                        val current = pnrViewModel.state.value

                        if (
                            navController.currentBackStackEntry?.id == entry.id &&
                            !current.isChecking &&
                            !current.isBusy(pnr)
                        ) {
                            val requestId = UUID.randomUUID().toString()

                            entry.savedStateHandle[ADD_REQUEST_KEY] = requestId

                            pnrViewModel.checkPnr(pnr, requestId)
                        }
                    },
                    onInputChanged = {
                        if (
                            navController.currentBackStackEntry?.id == entry.id &&
                            !pnrViewModel.state.value.isChecking
                        ) {
                            entry.savedStateHandle.remove<String>(ADD_REQUEST_KEY)
                            pnrViewModel.clearAddError()
                        }
                    }
                )
            }
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
            AppScreenBackground(screen = PnrScreenType.DETAILS) {
                val pnrNumber = entry.arguments
                    ?.getString(Routes.DETAILS_ARG)
                    .orEmpty()

                val currentPnr = state.pnrs.firstOrNull {
                    it.pnrNumber == pnrNumber
                }

                DetailsScreen(
                    pnr = currentPnr,
                    isLoading = state.isLoading,
                    recordState = state.recordState(pnrNumber),
                    loadError = state.loadError.takeIf {
                        state.databaseReadFailed()
                    },
                    isRefreshing =
                        pnrNumber in state.refreshingPnrs ||
                                pnrNumber in state.deletingPnrs,
                    isBusy = state.isBusy(pnrNumber),
                    feedback = state.detailsFeedback(pnrNumber),
                    onFeedbackShown = pnrViewModel::clearOperationMessage,
                    onRetry = { pnrViewModel.loadSavedPnrs() },
                    onRefresh = {
                        if (
                            navController.currentBackStackEntry?.id == entry.id &&
                            !pnrViewModel.state.value.isBusy(pnrNumber)
                        ) {
                            pnrViewModel.refreshPnr(pnrNumber)
                        }
                    },
                    onDelete = {
                        if (
                            navController.currentBackStackEntry?.id == entry.id &&
                            !pnrViewModel.state.value.isBusy(pnrNumber)
                        ) {
                            pnrViewModel.deletePnr(
                                pnrNumber,
                                originEntryId = entry.id
                            )
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        /* =================================================
           SETTINGS
        ================================================= */

        composable(Routes.SETTINGS) {
            AppScreenBackground(screen = PnrScreenType.SETTINGS) {
                SettingsScreen(
                    settings = themeSettings,
                    onModeSelected = onThemeModeSelected,
                    onPaletteSelected = onThemePaletteSelected,
                    onStyleSelected = onThemeStyleSelected,
                    onTimeFormatSelected = onTimeFormatSelected,
                    onReset = onThemeReset,
                    onBack = { navController.popBackStack() },
                    onPoliciesClick = {
                        navController.navigate(Routes.POLICIES) {
                            launchSingleTop = true
                        }
                    },
                    onReportBug = { openProjectLink("/issues") },
                    onViewSource = { openProjectLink() }
                )
            }
        }

        /* =================================================
           POLICIES & LEGAL
        ================================================= */

        composable(Routes.POLICIES) {
            AppScreenBackground(screen = PnrScreenType.SETTINGS) {
                PoliciesScreen(
                    onBack = { navController.popBackStack() },
                    onReportBug = { openProjectLink("/issues") },
                    onOpenLicense = {
                        openProjectLink("/blob/main/LICENSE.md")
                    }
                )
            }
        }
    }
}