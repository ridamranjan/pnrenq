package com.first.pnrenq.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.time.JourneySections
import com.first.pnrenq.domain.time.RailwayJourneySchedule
import java.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Update local presentation only, never trigger a database or network refresh. */
@Composable
internal fun rememberJourneySections(
    pnrs: List<PnrInfo>,
    clock: Clock
): JourneySections {
    val schedule = remember(pnrs) {
        RailwayJourneySchedule(pnrs)
    }

    val tracker = remember(schedule, clock) {
        schedule.newTracker()
    }

    var sections by remember(tracker) {
        mutableStateOf(
            tracker.update(clock.instant()).sections
        )
    }

    if (!LocalInspectionMode.current) {
        val lifecycle =
            LocalLifecycleOwner.current.lifecycle

        LaunchedEffect(
            tracker,
            lifecycle,
            clock
        ) {
            if (pnrs.isEmpty()) {
                return@LaunchedEffect
            }

            lifecycle.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                while (isActive) {
                    val update =
                        tracker.update(clock.instant())

                    if (sections !== update.sections) {
                        sections = update.sections
                    }

                    delay(update.delayMillis)
                }
            }
        }
    }

    return sections
}