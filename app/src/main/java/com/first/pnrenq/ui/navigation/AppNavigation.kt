package com.first.pnrenq.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.first.pnrenq.data.mock.MockPnrChecker
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.add.AddPnrScreen
import com.first.pnrenq.ui.common.PlaceholderScreen
import com.first.pnrenq.ui.home.HomeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SETTINGS = "settings"
    const val DETAILS_ARG = "pnr"
    const val DETAILS = "details/{$DETAILS_ARG}"
    fun details(pnr: String) = "details/$pnr"
}

/** Prototype wiring: the list lives in memory only. Room replaces this in Phase 2. */
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val pnrs = remember { mutableStateListOf<PnrInfo>().apply { addAll(MockPnrData.sample()) } }
    var checking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                pnrs = pnrs,
                onAddClick = { navController.navigate(Routes.ADD) },
                onPnrClick = { navController.navigate(Routes.details(it)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.ADD) {
            AddPnrScreen(
                isChecking = checking,
                onBack = { navController.popBackStack() },
                onCheck = { pnr ->
                    if (!checking) {
                        checking = true
                        scope.launch {
                            delay(800)
                            val result = MockPnrChecker.simulatedCheck(pnr)
                            val index = pnrs.indexOfFirst { it.pnrNumber == result.pnrNumber }
                            if (index >= 0) pnrs[index] = result else pnrs.add(0, result)
                            checking = false
                            navController.navigate(Routes.details(pnr)) {
                                popUpTo(Routes.ADD) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument(Routes.DETAILS_ARG) { type = NavType.StringType })
        ) { entry ->
            val pnr = entry.arguments?.getString(Routes.DETAILS_ARG).orEmpty()
            PlaceholderScreen(
                title = "PNR $pnr",
                message = "Details screen arrives in the next batch.",
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            PlaceholderScreen(
                title = "Settings",
                message = "Settings screen arrives in a later batch.",
                onBack = { navController.popBackStack() }
            )
        }
    }
}