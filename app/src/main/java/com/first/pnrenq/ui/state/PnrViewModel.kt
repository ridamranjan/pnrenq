package com.first.pnrenq.ui.state

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.first.pnrenq.data.local.LocalPnrRepository
import com.first.pnrenq.data.local.PnrDatabaseProvider
import com.first.pnrenq.data.remote.PnrCheckException
import com.first.pnrenq.data.remote.RemotePnrChecker
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.operation.PnrOperationCoordinator
import com.first.pnrenq.domain.validation.isValidPnr
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PnrViewModel internal constructor(
    application: Application,
    private val repository: PnrViewModelDataSource,
    private val lookup: suspend (String) -> PnrInfo
) : AndroidViewModel(application) {

    // Preserve the constructor used by the default AndroidViewModel factory.
    constructor(application: Application) : this(
        application,
        LocalPnrViewModelDataSource(
            LocalPnrRepository(
                PnrDatabaseProvider.get(application).pnrDao()
            )
        ),
        RemotePnrChecker::check
    )

    private val coordinator = PnrOperationCoordinator()
    private val navigationQueue = PnrNavigationQueue()
    private val _state = MutableStateFlow(PnrUiState())
    val state: StateFlow<PnrUiState> = _state.asStateFlow()
    val navigationEvents: Flow<PnrNavigationEvent> = navigationQueue.events

    private var loadJob: Job? = null
    private var loadGeneration = 0L
    private var nextMessageId = 0L

    init {
        loadSavedPnrs()
    }

    // UI actions and all state mutations are confined to the main thread.
    private fun updateState(transform: (PnrUiState) -> PnrUiState) {
        _state.value = transform(_state.value)
    }

    @MainThread
    fun loadSavedPnrs() {
        loadGeneration += 1
        val generation = loadGeneration
        loadJob?.cancel()

        updateState {
            // Retain the previous warning until a successful read replaces it.
            it.copy(isLoading = true)
        }

        loadJob = viewModelScope.launch {
            try {
                repository.observeAll().collect { result ->
                    if (generation == loadGeneration) {
                        updateState {
                            it.copy(
                                pnrs = result.pnrs,
                                isLoading = false,
                                loadError = unreadableMessage(result.unreadablePnrs),
                                unreadablePnrs = result.unreadablePnrs
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (generation == loadGeneration) {
                    updateState {
                        it.copy(
                            isLoading = false,
                            loadError = "Unable to load saved PNRs. Please retry.",
                            unreadablePnrs = emptyList()
                        )
                    }
                }
            }
        }
    }

    private fun unreadableMessage(pnrs: List<String>): String? {
        if (pnrs.isEmpty()) return null

        val endings = pnrs.take(3).joinToString(", ") {
            "…${it.takeLast(4)}"
        }
        val more = if (pnrs.size > 3) {
            ", and ${pnrs.size - 3} more"
        } else {
            ""
        }

        return "Unable to read ${pnrs.size} saved PNR(s) ($endings$more). " +
                "Re-add the affected PNR using Add PNR to repair it."
    }

    @MainThread
    fun clearAddError() {
        updateState { it.copy(addError = null) }
    }

    /** Compatibility for the existing screens; never discard scoped feedback. */
    @MainThread
    fun clearDetailsMessage() {
        updateState {
            it.copy(detailsMessage = null, messagePnr = null)
        }
    }

    /** A late acknowledgement must not clear a newer message for this PNR. */
    @MainThread
    fun clearOperationMessage(pnr: String, messageId: Long) {
        updateState { state ->
            if (state.operationMessages[pnr]?.id == messageId) {
                state.withoutOperationMessage(pnr)
            } else {
                state
            }
        }
    }

    private fun PnrUiState.withoutOperationMessage(pnr: String): PnrUiState =
        copy(
            operationMessages = operationMessages - pnr,
            detailsMessage = if (messagePnr == pnr) null else detailsMessage,
            messagePnr = if (messagePnr == pnr) null else messagePnr
        )

    private fun PnrUiState.withOperationMessage(
        pnr: String,
        kind: PnrMessageKind,
        text: String
    ): PnrUiState {
        val message = PnrOperationMessage(++nextMessageId, kind, text)

        return copy(
            operationMessages = operationMessages + (pnr to message),
            detailsMessage = text,
            messagePnr = pnr
        )
    }

    @MainThread
    fun checkPnr(pnr: String, requestId: String? = null) {
        if (_state.value.isChecking) return

        if (!isValidPnr(pnr)) {
            updateState {
                it.copy(addError = "Enter a valid 10-digit PNR")
            }
            return
        }

        // Acquire inside the coroutine: if the scope is already cancelled,
        // no operation lock or busy state is created.
        viewModelScope.launch {
            if (_state.value.isChecking || !coordinator.tryAcquire(pnr)) {
                return@launch
            }

            var saved = false

            try {
                updateState {
                    it.copy(
                        isChecking = true,
                        checkingPnr = pnr,
                        addError = null
                    )
                }

                val existing = repository.getByPnr(pnr)

                if (existing == null) {
                    val result = lookup(pnr)
                    currentCoroutineContext().ensureActive()
                    repository.save(result)
                }

                saved = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: PnrCheckException) {
                updateState { it.copy(addError = e.message) }
            } catch (_: Exception) {
                updateState {
                    it.copy(
                        addError = "Unable to save PNR. Please try again."
                    )
                }
            } finally {
                coordinator.release(pnr)
                updateState {
                    it.copy(isChecking = false, checkingPnr = null)
                }
            }

            if (saved) {
                currentCoroutineContext().ensureActive()

                navigationQueue.enqueue(
                    PnrNavigationEvent.OpenDetails(pnr, requestId)
                )
            }
        }
    }

    @MainThread
    fun refreshPnr(pnr: String) {
        if (!isValidPnr(pnr)) return

        viewModelScope.launch {
            if (!coordinator.tryAcquire(pnr)) return@launch

            var message: String? = null

            try {
                updateState {
                    it.withoutOperationMessage(pnr).copy(
                        refreshingPnrs = it.refreshingPnrs + pnr
                    )
                }

                val result = lookup(pnr)
                currentCoroutineContext().ensureActive()

                message = if (repository.saveIfExists(result)) {
                    "PNR status updated successfully."
                } else {
                    "Saved PNR was not found."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: PnrCheckException) {
                message = e.message
            } catch (_: Exception) {
                message = "Unable to refresh. Your saved result is unchanged."
            } finally {
                coordinator.release(pnr)

                updateState { state ->
                    val idle = state.copy(
                        refreshingPnrs = state.refreshingPnrs - pnr
                    )

                    val text = message

                    if (text == null) {
                        idle
                    } else {
                        idle.withOperationMessage(
                            pnr,
                            PnrMessageKind.REFRESH,
                            text
                        )
                    }
                }
            }
        }
    }

    @MainThread
    fun deletePnr(pnr: String, originEntryId: String? = null) {
        if (!isValidPnr(pnr)) return

        viewModelScope.launch {
            if (!coordinator.tryAcquire(pnr)) return@launch

            var deleted = false
            var message: String? = null

            try {
                updateState {
                    it.withoutOperationMessage(pnr).copy(
                        deletingPnrs = it.deletingPnrs + pnr
                    )
                }

                repository.delete(pnr)
                deleted = true
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message = "Unable to delete PNR. Please try again."
            } finally {
                coordinator.release(pnr)

                updateState { state ->
                    val idle = state.copy(
                        deletingPnrs = state.deletingPnrs - pnr
                    )

                    val text = message

                    if (text == null) {
                        idle
                    } else {
                        idle.withOperationMessage(
                            pnr,
                            PnrMessageKind.DELETE,
                            text
                        )
                    }
                }
            }

            if (deleted) {
                currentCoroutineContext().ensureActive()

                navigationQueue.enqueue(
                    PnrNavigationEvent.ReturnAfterDelete(pnr, originEntryId)
                )
            }
        }
    }
}