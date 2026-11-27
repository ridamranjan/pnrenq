package com.first.pnrenq.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.first.pnrenq.ui.state.ScopedPnrFeedback
import com.first.pnrenq.ui.state.presentPnrFeedback
import com.first.pnrenq.ui.theme.ThemeText as Text

/** Show only on the active destination. Cancellation leaves the message pending. */
@Composable
internal fun PnrFeedbackEffect(
    host: SnackbarHostState,
    feedback: ScopedPnrFeedback?,
    onShown: (String, Long) -> Unit,
    includePnr: Boolean = false
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val acknowledge by rememberUpdatedState(onShown)

    LaunchedEffect(host, lifecycle, feedback, includePnr) {
        val pending = feedback ?: return@LaunchedEffect
        var completed = false

        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (!completed) {
                presentPnrFeedback(
                    feedback = pending,
                    present = {
                        host.showSnackbar(
                            message = if (includePnr) {
                                pending.homeText()
                            } else {
                                pending.message.text
                            },
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    },
                    acknowledge = { pnr, id ->
                        completed = true
                        acknowledge(pnr, id)
                    }
                )
            }
        }
    }
}

/** Persistent, actionable warning; not an acknowledged operation snackbar. */
@Composable
internal fun PnrLoadWarning(
    message: String,
    isLoading: Boolean,
    onRetry: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = scheme.errorContainer,
            contentColor = scheme.onErrorContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = message,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                },
                color = scheme.onErrorContainer
            )

            TextButton(
                onClick = onRetry,
                enabled = !isLoading,
                modifier = Modifier.semantics {
                    if (isLoading) {
                        stateDescription = "Loading saved PNRs"
                    }
                }
            ) {
                Text(
                    "Try again",
                    color = scheme.onErrorContainer
                )
            }
        }
    }
}