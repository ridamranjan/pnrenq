
package com.first.pnrenq.ui.state

import com.first.pnrenq.domain.model.PnrInfo

data class PnrUiState(
    val pnrs: List<PnrInfo> = emptyList(),
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val isChecking: Boolean = false,
    val refreshingPnrs: Set<String> = emptySet(),
    val deletingPnrs: Set<String> = emptySet(),
    val addError: String? = null,
    val detailsMessage: String? = null,
    val messagePnr: String? = null
)
