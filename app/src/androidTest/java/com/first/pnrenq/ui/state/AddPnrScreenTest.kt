package com.first.pnrenq.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.first.pnrenq.ui.theme.PnrenqTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddPnrScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun changingInputClearsPreviousNetworkError() {
        val error = mutableStateOf<String?>("Service unavailable")

        rule.setContent {
            PnrenqTheme {
                AddPnrScreen(
                    isChecking = false,
                    onCheck = {},
                    onBack = {},
                    errorMessage = error.value,
                    onInputChanged = {
                        error.value = null
                    }
                )
            }
        }

        rule.onNodeWithText("Service unavailable")
            .assertExists()

        rule.onNode(hasSetTextAction())
            .performTextReplacement("1234567890")

        rule.onNodeWithText("Service unavailable")
            .assertDoesNotExist()

        rule.onNode(hasSetTextAction())
            .assertTextContains("1234567890")
    }

    @Test
    fun toolbarAndSystemBackAreBlockedOnlyWhileChecking() {
        val checking = mutableStateOf(true)
        var backCount = 0

        rule.setContent {
            PnrenqTheme {
                AddPnrScreen(
                    isChecking = checking.value,
                    onCheck = {},
                    onBack = {
                        backCount++
                    }
                )
            }
        }

        val back = rule.onNodeWithContentDescription("Navigate back")

        back.assertIsNotEnabled()

        rule.runOnIdle {
            rule.activity.onBackPressedDispatcher.onBackPressed()
            assertEquals(0, backCount)
            checking.value = false
        }

        back.assertIsEnabled()
            .performClick()

        rule.runOnIdle {
            assertEquals(1, backCount)

            rule.activity.onBackPressedDispatcher.onBackPressed()

            assertEquals(2, backCount)
        }
    }

    @Test
    fun enteredPnrSurvivesSaveableStateRestoration() {
        val restoration = StateRestorationTester(rule)

        restoration.setContent {
            PnrenqTheme {
                AddPnrScreen(
                    isChecking = false,
                    onCheck = {},
                    onBack = {}
                )
            }
        }

        rule.onNode(hasSetTextAction())
            .performTextReplacement("1234567890")

        restoration.emulateSavedInstanceStateRestore()

        rule.onNode(hasSetTextAction())
            .assertTextContains("1234567890")
    }

    @Test
    fun changingInputClearsLocalValidationError() {
        var submitted = ""

        rule.setContent {
            PnrenqTheme {
                AddPnrScreen(
                    isChecking = false,
                    onCheck = {
                        submitted = it
                    },
                    onBack = {}
                )
            }
        }

        val input = rule.onNode(hasSetTextAction())

        input.performTextReplacement("123")

        rule.onNodeWithText("Check PNR status")
            .performScrollTo()
            .performClick()

        rule.onNodeWithText("Enter exactly 10 digits")
            .assertExists()

        rule.runOnIdle {
            assertEquals("", submitted)
        }

        input.performScrollTo()
            .performTextReplacement("1234567890")

        rule.onNodeWithText("Enter exactly 10 digits")
            .assertDoesNotExist()

        rule.onNodeWithText("Check PNR status →")
            .performScrollTo()
            .performClick()

        rule.runOnIdle {
            assertEquals("1234567890", submitted)
        }
    }
}