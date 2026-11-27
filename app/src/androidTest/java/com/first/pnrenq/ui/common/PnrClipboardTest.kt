package com.first.pnrenq.ui.common

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.details.DetailsScreen
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.theme.PnrenqTheme
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrClipboardTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val pnr = "0123456789"

    private val record = PnrInfo(
        pnrNumber = pnr,
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2099, 1, 1),
        fromStation = "ABC",
        toStation = "XYZ",
        bookingStatus = "WL 5",
        currentStatus = "CNF",
        chartStatus = null,
        passengers = emptyList(),
        lastCheckedAtMillis = 1000L
    )

    @Test
    fun clipPreservesExactTextLabelAndPlainTextMimeType() {
        val clip = requireNotNull(
            createSensitivePnrClip(pnr)
        )

        assertEquals(1, clip.itemCount)

        assertEquals(
            "PNR",
            clip.description.label.toString()
        )

        assertTrue(
            clip.description.hasMimeType(
                ClipDescription.MIMETYPE_TEXT_PLAIN
            )
        )

        assertEquals(
            pnr,
            clip.getItemAt(0).text.toString()
        )
    }

    @Test
    fun clipContainsSensitiveFlagBeforePublication() {
        val clip = requireNotNull(
            createSensitivePnrClip(pnr)
        )

        assertTrue(
            clip.description.extras?.getBoolean(
                "android.content.extra.IS_SENSITIVE"
            ) == true
        )
    }

    @Test
    fun malformedPnrsDoNotProduceClipboardEntries() {
        for (
        value in listOf(
            "",
            "123456789",
            "12345678901",
            " 0123456789",
            "01234A6789",
            "०१२३४५६७८९"
        )
        ) {
            assertNull(createSensitivePnrClip(value))
        }
    }

    @Test
    fun homeCopyPublishesSensitivePnr() {
        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = listOf(record),
                    onAddClick = {},
                    onPnrClick = {}
                )
            }
        }

        verifyScreenCopy()
    }

    @Test
    fun detailsCopyPublishesSensitivePnr() {
        rule.setContent {
            PnrenqTheme {
                DetailsScreen(
                    pnr = record,
                    isRefreshing = false,
                    onRefresh = {},
                    onDelete = {},
                    onBack = {}
                )
            }
        }

        verifyScreenCopy()
    }

    private fun verifyScreenCopy() {
        // Use a dedicated test device: this test intentionally replaces its clipboard.
        val clipboard = requireNotNull(
            rule.activity.getSystemService(
                ClipboardManager::class.java
            )
        )

        val sentinel = "PNR clipboard regression test"

        rule.runOnIdle {
            assertTrue(
                "Test Activity must have window focus",
                rule.activity.hasWindowFocus()
            )

            // Distinguish a fresh copy from a clip left by a previous test.
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    "Test",
                    sentinel
                )
            )
        }

        try {
            rule.onNodeWithContentDescription(
                "Copy PNR ending in 6789"
            ).assertIsEnabled()
                .performClick()

            rule.runOnIdle {
                val clip = requireNotNull(
                    clipboard.primaryClip
                )

                assertEquals(1, clip.itemCount)

                assertEquals(
                    pnr,
                    clip.getItemAt(0).text.toString()
                )

                assertEquals(
                    "PNR",
                    clip.description.label.toString()
                )

                assertTrue(
                    clip.description.extras?.getBoolean(
                        ClipDescription.EXTRA_IS_SENSITIVE
                    ) == true
                )

                // An invalid direct copy must leave the current valid clip untouched.
                assertFalse(
                    copyPnrToClipboard(
                        rule.activity,
                        "invalid"
                    )
                )

                assertEquals(
                    pnr,
                    clipboard.primaryClip
                        ?.getItemAt(0)
                        ?.text
                        ?.toString()
                )
            }
        } finally {
            rule.runOnIdle {
                val clip = clipboard.primaryClip

                val text = clip
                    ?.getItemAt(0)
                    ?.text
                    ?.toString()

                if (text == sentinel || text == pnr) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        clipboard.clearPrimaryClip()
                    } else {
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                "Test",
                                ""
                            )
                        )
                    }
                }
            }
        }
    }
}