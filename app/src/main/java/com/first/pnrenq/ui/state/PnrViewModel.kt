
package com.first.pnrenq.ui.state

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.first.pnrenq.data.local.LocalPnrRepository
import com.first.pnrenq.data.local.PnrDatabaseProvider
import com.first.pnrenq.data.remote.RemotePnrChecker
import com.first.pnrenq.data.remote.PnrCheckException
import com.first.pnrenq.domain.operation.PnrOperationCoordinator
import com.first.pnrenq.domain.validation.isValidPnr
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PnrViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = LocalPnrRepository(
        PnrDatabaseProvider.get(application).pnrDao()
    )

    private val coordinator = PnrOperationCoordinator()

    private val _state = MutableStateFlow(PnrUiState())
    val state: StateFlow<PnrUiState> = _state.asStateFlow()

    private val _navigationEvents =
        MutableSharedFlow<PnrNavigationEvent>()

    val navigationEvents = _navigationEvents.asSharedFlow()

    private var loadJob: Job? = null
    private var loadGeneration = 0L

    init {
        loadSavedPnrs()
    }


    fun loadSavedPnrs() {
        loadJob?.cancel()

        loadGeneration += 1
        val generation = loadGeneration

        _state.update {
            it.copy(
                isLoading = it.pnrs.isEmpty(),
                loadError = null
            )
        }

        loadJob = viewModelScope.launch {
            try {
                repository.observeAll().collect { records ->
                    if (generation == loadGeneration) {
                        _state.update {
                            it.copy(
                                pnrs = records,
                                isLoading = false,
                                loadError = null
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (generation == loadGeneration) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadError =
                                "Unable to load saved PNRs. Please retry."
                        )
                    }
                }
            }
        }
    }


    fun clearAddError() {
        _state.update { it.copy(addError = null) }
    }

    fun clearDetailsMessage() {
        _state.update {
            it.copy(
                detailsMessage = null,
                messagePnr = null
            )
        }
    }


    fun checkPnr(pnr: String) {
        if (!isValidPnr(pnr)) {
            _state.update {
                it.copy(
                    addError = "Enter a valid 10-digit PNR"
                )
            }
            return
        }

        if (_state.value.isChecking) return
        if (!coordinator.tryAcquire(pnr)) return

        _state.update {
            it.copy(
                isChecking = true,
                addError = null
            )
        }

        viewModelScope.launch {
            var saved = false

            try {
                // Reuse a saved record without making another API call.
                val existing = repository.getByPnr(pnr)

                if (existing != null) {
                    saved = true
                } else {
                    // Live request through Cloudflare.
                    val result = RemotePnrChecker.check(pnr)

                    // Save only a successfully validated result.
                    repository.save(result)
                    saved = true
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: PnrCheckException) {
                _state.update {
                    it.copy(addError = e.message)
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        addError =
                            "Unable to save PNR. Please try again."
                    )
                }

            } finally {
                coordinator.release(pnr)

                _state.update {
                    it.copy(isChecking = false)
                }
            }

            if (saved) {
                currentCoroutineContext().ensureActive()

                _navigationEvents.emit(
                    PnrNavigationEvent.OpenDetails(pnr)
                )
            }
        }
    }



    fun refreshPnr(pnr: String) {
        if (!isValidPnr(pnr)) return
        if (!coordinator.tryAcquire(pnr)) return

        _state.update {
            it.copy(
                refreshingPnrs = it.refreshingPnrs + pnr,
                detailsMessage = null,
                messagePnr = null
            )
        }

        viewModelScope.launch {
            var message =
                "Couldn't refresh. Your saved result is unchanged."

            try {
                // Exactly one user-initiated live request.
                val result = RemotePnrChecker.check(pnr)

                val updated = withContext(Dispatchers.IO) {
                    if (repository.getByPnr(pnr) != null) {
                        repository.save(result)
                        true
                    } else {
                        false
                    }
                }

                message = if (updated) {
                    "PNR status updated successfully."
                } else {
                    "Saved PNR was not found."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: PnrCheckException) {
                message = e.message

            } catch (e: Exception) {
                message =
                    "Unable to refresh. Your saved result is unchanged."

            } finally {
                coordinator.release(pnr)

                _state.update {
                    it.copy(
                        refreshingPnrs = it.refreshingPnrs - pnr,
                        detailsMessage = message,
                        messagePnr = pnr
                    )
                }
            }
        }
    }


    fun deletePnr(pnr: String) {
        if (!isValidPnr(pnr)) return
        if (!coordinator.tryAcquire(pnr)) return

        _state.update {
            it.copy(
                deletingPnrs = it.deletingPnrs + pnr,
                detailsMessage = null,
                messagePnr = null
            )
        }

        viewModelScope.launch {
            var deleted = false

            try {
                withContext(Dispatchers.IO) {
                    repository.delete(pnr)
                }

                deleted = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        detailsMessage =
                            "Unable to delete PNR. Please try again.",
                        messagePnr = pnr
                    )
                }
            } finally {
                coordinator.release(pnr)

                _state.update {
                    it.copy(
                        deletingPnrs = it.deletingPnrs - pnr
                    )
                }
            }

            if (deleted) {
                currentCoroutineContext().ensureActive()

                _navigationEvents.emit(
                    PnrNavigationEvent.ReturnAfterDelete(pnr)
                )
            }
        }
    }
}
