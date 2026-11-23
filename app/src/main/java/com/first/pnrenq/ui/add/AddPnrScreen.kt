
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.add

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

    val isComplete = isValidPnr(input)

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add PNR",
                        style = MaterialTheme.typography.titleLarge
                    )
                },

                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        enabled = !isChecking
                    ) {
                        Text("← Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            /* ---------- Header illustration ---------- */

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "10",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "Track a new journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enter your 10-digit PNR number to view the latest railway reservation status.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            /* ---------- Input card ---------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),

                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerLow
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Text("10-digit PNR")
                        },

                        shape = RoundedCornerShape(16.dp),

                        isError = showValidationError,

                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
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
                                    }
                                )

                                Text(
                                    text = "${input.length}/$PNR_LENGTH"
                                )
                            }
                        }
                    )

                    /* ---------- API error ---------- */

                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    /* ---------- Submit ---------- */

                    Button(
                        onClick = {
                            submit()
                        },

                        enabled = !isChecking,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),

                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(Modifier.width(12.dp))

                            Text("Checking PNR...")
                        } else {
                            Text(
                                text = if (isComplete) {
                                    "Check PNR status  →"
                                } else {
                                    "Check PNR status"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            /* ---------- Helpful information ---------- */

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor =
                    MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your journey, saved",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Successful PNR checks are saved automatically. You can view saved information offline and refresh only when you choose.",
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

@Preview(showBackground = true)
@Composable
private fun AddPnrLoadingPreview() {
    PnrenqTheme {
        AddPnrScreen(
            isChecking = true,
            onCheck = {},
            onBack = {}
        )
    }
}
