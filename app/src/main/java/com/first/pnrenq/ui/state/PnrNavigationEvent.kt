
package com.first.pnrenq.ui.state

sealed interface PnrNavigationEvent {
    data class OpenDetails(val pnr: String) : PnrNavigationEvent
    data class ReturnAfterDelete(val pnr: String) : PnrNavigationEvent
}
