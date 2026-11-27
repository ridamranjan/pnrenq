package com.first.pnrenq.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeHostDiagnosticTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun composeHostCanDisplayBasicContent() {

        rule.setContent {
            MaterialTheme {
                Text("Compose host ready")
            }
        }

        rule.activityRule.scenario.onActivity { activity ->

            assertEquals(
                "Test Activity is not resumed",
                Lifecycle.State.RESUMED,
                activity.lifecycle.currentState
            )
        }

        rule.onNodeWithText("Compose host ready")
            .assertIsDisplayed()
    }
}