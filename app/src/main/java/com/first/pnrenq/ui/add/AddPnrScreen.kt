@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.add

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.first.pnrenq.domain.validation.PNR_LENGTH
import com.first.pnrenq.domain.validation.isValidPnr
import com.first.pnrenq.domain.validation.sanitizePnrInput
import com.first.pnrenq.ui.common.PnrAdaptiveTopBar
import com.first.pnrenq.ui.common.pnrControlColor
import com.first.pnrenq.ui.theme.PnrAccentSurface
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.ThemeText as Text

@Composable
fun AddPnrScreen(
    isChecking: Boolean,
    onCheck: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onInputChanged: () -> Unit = {}
) {
    var input by rememberSaveable {
        mutableStateOf("")
    }

    var showValidationError by rememberSaveable {
        mutableStateOf(false)
    }

    // Match the existing disabled toolbar Back button while checking.
    if (!LocalInspectionMode.current) {
        BackHandler {
            if (!isChecking) onBack()
        }
    }

    fun submit() {
        if (isChecking) return

        if (isValidPnr(input)) {
            showValidationError = false
            onCheck(input)
        } else {
            showValidationError = true
        }
    }

    val scheme = MaterialTheme.colorScheme
    val valid = isValidPnr(input)
    val cardBackground = scheme.surfaceContainerLow

    val focusedControlColor = pnrControlColor(
        scheme.primary,
        cardBackground
    )

    val unfocusedControlColor = pnrControlColor(
        scheme.outline,
        cardBackground
    )

    val errorControlColor = pnrControlColor(
        scheme.error,
        cardBackground
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = scheme.onBackground,
        topBar = {
            PnrAdaptiveTopBar(
                title = "Add PNR",
                onBack = onBack,
                enabled = !isChecking
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // The existing circle supports gradients and can grow with text.
            PnrAccentSurface(
                backgroundColor = scheme.primaryContainer,
                foregroundColor = scheme.onPrimaryContainer,
                shape = CircleShape,
                strength = 1.25f
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(
                            minWidth = 88.dp,
                            minHeight = 88.dp
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "10",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "Track a new journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = scheme.onBackground
            )

            Text(
                text = "Enter your 10-digit PNR number to " +
                        "check the latest railway reservation status.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = scheme.onBackground
            )

            // Input card stays solid.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBackground,
                    contentColor = scheme.onSurface
                ),
                border = BorderStroke(
                    1.dp,
                    scheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "PNR NUMBER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )

                    OutlinedTextField(
                        value = input,
                        onValueChange = { value ->
                            val sanitized = sanitizePnrInput(value)

                            if (!isChecking && sanitized != input) {
                                input = sanitized
                                showValidationError = false
                                onInputChanged()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "PNR number, 10 digits"

                                if (showValidationError) {
                                    error("Enter exactly 10 digits")
                                }
                            },
                        enabled = !isChecking,
                        singleLine = true,
                        isError = showValidationError,
                        placeholder = {
                            Text(
                                text = "10-digit PNR",
                                color = scheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = scheme.onSurface
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = scheme.onSurface,
                            unfocusedTextColor = scheme.onSurface,
                            disabledTextColor = scheme.onSurfaceVariant,
                            errorTextColor = scheme.onSurface,

                            focusedPlaceholderColor = scheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = scheme.onSurfaceVariant,

                            focusedSupportingTextColor = scheme.onSurfaceVariant,
                            unfocusedSupportingTextColor = scheme.onSurfaceVariant,
                            errorSupportingTextColor = scheme.error,

                            focusedBorderColor = focusedControlColor,
                            unfocusedBorderColor = unfocusedControlColor,
                            errorBorderColor = errorControlColor,

                            cursorColor = focusedControlColor,
                            errorCursorColor = errorControlColor,

                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submit() }
                        ),
                        supportingText = {
                            PnrInputSupportingText(
                                length = input.length,
                                showValidationError = showValidationError
                            )
                        }
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            color = scheme.errorContainer,
                            contentColor = scheme.onErrorContainer,
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                modifier = Modifier
                                    .padding(14.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                    },
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onErrorContainer
                            )
                        }
                    }

                    Button(
                        onClick = { submit() },
                        enabled = !isChecking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .semantics {
                                if (isChecking) {
                                    stateDescription = "Checking PNR"
                                    liveRegion = LiveRegionMode.Polite
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary,

                            // Keep the disabled appearance but make the active
                            // operation's explanatory text readable.
                            disabledContainerColor = scheme.onSurface
                                .copy(alpha = 0.12f)
                                .compositeOver(cardBackground),

                            disabledContentColor = scheme.onSurface
                        )
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = scheme.onSurface
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = "Checking PNR...",
                                color = scheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = if (valid) {
                                    "Check PNR status →"
                                } else {
                                    "Check PNR status"
                                },
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Existing information card: gradient in Gradient mode.
            PnrAccentSurface(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = scheme.secondaryContainer,
                foregroundColor = scheme.onSecondaryContainer,
                shape = RoundedCornerShape(20.dp),
                strength = 1.10f
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "Your journey, saved",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSecondaryContainer
                    )

                    Text(
                        text = "Successful PNR checks are saved " +
                                "automatically. You can view them " +
                                "offline and refresh whenever you choose.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * Keep supporting text compatible with OutlinedTextField's intrinsic measurement.
 *
 * Do not use BoxWithConstraints or another SubcomposeLayout in this slot.
 * At ordinary font sizes the weighted message may wrap beside the counter.
 */
@Composable
private fun PnrInputSupportingText(
    length: Int,
    showValidationError: Boolean
) {
    val stacked =
        LocalDensity.current.fontScale >= 1.3f ||
                LocalConfiguration.current.screenWidthDp < 320

    if (stacked) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PnrValidationMessage(
                showValidationError = showValidationError
            )

            PnrInputCounter(
                length = length,
                modifier = Modifier.align(Alignment.End)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PnrValidationMessage(
                showValidationError = showValidationError,
                modifier = Modifier.weight(1f)
            )

            PnrInputCounter(length)
        }
    }
}

@Composable
private fun PnrValidationMessage(
    showValidationError: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Text(
        modifier = modifier.semantics {
            if (showValidationError) {
                liveRegion = LiveRegionMode.Polite
            }
        },
        text = if (showValidationError) {
            "Enter exactly 10 digits"
        } else {
            "Numbers only"
        },
        color = if (showValidationError) {
            scheme.error
        } else {
            scheme.onSurfaceVariant
        }
    )
}

@Composable
private fun PnrInputCounter(
    length: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$length/$PNR_LENGTH",
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true)
@Composable
private fun AddPnrPreview() {
    PnrenqTheme {
        AddPnrScreen(
            isChecking = false,
            onCheck = {},
            onBack = {}
        )
    }
}