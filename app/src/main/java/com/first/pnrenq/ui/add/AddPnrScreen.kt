
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.add

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

import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.ThemeText as Text

@Composable
fun AddPnrScreen(
    isChecking: Boolean,
    onCheck: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    var input by rememberSaveable {
        mutableStateOf("")
    }

    var showValidationError by rememberSaveable {
        mutableStateOf(false)
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

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,

        // Fixes black text on the transparent dark background.
        contentColor = scheme.onBackground,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add PNR",
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        enabled = !isChecking
                    ) {
                        Text(
                            text = "← Back",
                            color = scheme.onBackground
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = scheme.onBackground,
                    navigationIconContentColor = scheme.onBackground,
                    actionIconContentColor = scheme.onBackground
                )
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

            Surface(
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.size(88.dp),
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
                color = scheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enter your 10-digit PNR number to " +
                        "check the latest railway reservation status.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onBackground.copy(alpha = 0.88f),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),

                colors = CardDefaults.cardColors(
                    containerColor = scheme.surfaceContainerLow,
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
                            input = sanitizePnrInput(value)
                            showValidationError = false
                        },

                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChecking,
                        singleLine = true,

                        placeholder = {
                            Text(
                                text = "10-digit PNR",
                                color = scheme.onSurfaceVariant
                            )
                        },

                        shape = RoundedCornerShape(16.dp),
                        isError = showValidationError,

                        // Explicitly set the typed digits' color.
                        textStyle =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                                color = scheme.onSurface
                            ),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = scheme.onSurface,
                            unfocusedTextColor = scheme.onSurface,
                            disabledTextColor =
                                scheme.onSurfaceVariant,
                            errorTextColor = scheme.onSurface,

                            focusedPlaceholderColor =
                                scheme.onSurfaceVariant,
                            unfocusedPlaceholderColor =
                                scheme.onSurfaceVariant,

                            focusedSupportingTextColor =
                                scheme.onSurfaceVariant,
                            unfocusedSupportingTextColor =
                                scheme.onSurfaceVariant,
                            errorSupportingTextColor =
                                scheme.error,

                            focusedBorderColor = scheme.primary,
                            unfocusedBorderColor = scheme.outline,
                            errorBorderColor = scheme.error,

                            cursorColor = scheme.primary,
                            errorCursorColor = scheme.error,

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
                            onDone = {
                                submit()
                            }
                        ),

                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text(
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

                                Text(
                                    text = "${input.length}/$PNR_LENGTH",
                                    color = scheme.onSurfaceVariant
                                )
                            }
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
                                modifier = Modifier.padding(14.dp),
                                color = scheme.onErrorContainer,
                                style =
                                    MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            submit()
                        },
                        enabled = !isChecking,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary
                        )
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = scheme.onPrimary
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = "Checking PNR...",
                                color = scheme.onPrimary
                            )
                        } else {
                            Text(
                                text = if (valid) {
                                    "Check PNR status  →"
                                } else {
                                    "Check PNR status"
                                },
                                color = scheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "Your journey, saved",
                        color = scheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Successful PNR checks are saved " +
                                "automatically. You can view them " +
                                "offline and refresh whenever you choose.",
                        color = scheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
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
