package com.first.pnrenq.ui.state

import com.first.pnrenq.domain.model.PnrInfo

enum class PnrMessageKind {
    REFRESH,
    DELETE
}

data class PnrOperationMessage(
    val id: Long,
    val kind: PnrMessageKind,
    val text: String
)

data class PnrUiState(
    val pnrs: List<PnrInfo> = emptyList(),
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val isChecking: Boolean = false,
    val refreshingPnrs: Set<String> = emptySet(),
    val deletingPnrs: Set<String> = emptySet(),
    val addError: String? = null,
    // Legacy presentation fields retained until the screens adopt scoped feedback.
    val detailsMessage: String? = null,
    val messagePnr: String? = null,
    val unreadablePnrs: List<String> = emptyList(),
    val checkingPnr: String? = null,
    val operationMessages: Map<String, PnrOperationMessage> = emptyMap()
) {
    fun isBusy(pnr: String): Boolean =
        checkingPnr == pnr || pnr in refreshingPnrs || pnr in deletingPnrs
}