
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    var showError by rememberSaveable {
        mutableStateOf(false)
    }

    fun submit() {
        if (isChecking) return

        if (isValidPnr(input)) {
            showError = false
            onCheck(input)
        } else {
            showError = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Add PNR")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        enabled = !isChecking
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Enter your 10-digit PNR number.",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = sanitizePnrInput(it)
                    showError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("PNR number")
                },
                singleLine = true,
                enabled = !isChecking,
                isError = showError,
                supportingText = {
                    Text(
                        if (showError) {
                            "Enter a valid 10-digit PNR"
                        } else {
                            "${input.length}/$PNR_LENGTH"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        submit()
                    }
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    submit()
                },
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text("Checking...")
                } else {
                    Text("Check status")
                }
            }

            Text(
                text = "A new PNR needs an internet connection. (Prototype: results are simulated.)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPnrScreenPreview() {
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
private fun AddPnrScreenCheckingPreview() {
    PnrenqTheme {
        AddPnrScreen(
            isChecking = true,
            onCheck = {},
            onBack = {}
        )
    }
}
