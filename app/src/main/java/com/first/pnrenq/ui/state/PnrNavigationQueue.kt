package com.first.pnrenq.ui.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** In-memory FIFO for one navigation consumer, retained across collector gaps. */
internal class PnrNavigationQueue {
    // Identity, not event equality, distinguishes repeated identical events.
    private class Pending(val event: PnrNavigationEvent)

    private val lock = Any()
    private val pending = MutableStateFlow<List<Pending>>(emptyList())
    private val consumer = Mutex()

    fun enqueue(event: PnrNavigationEvent) {
        synchronized(lock) {
            pending.value = pending.value + Pending(event)
        }
    }

    val events: Flow<PnrNavigationEvent> = flow {
        consumer.withLock {
            pending.collect { items ->
                val next = items.firstOrNull() ?: return@collect

                // Acknowledge only after the direct collector returns.
                // If it is cancelled or throws, the event remains pending.
                emit(next.event)

                synchronized(lock) {
                    if (pending.value.firstOrNull() === next) {
                        pending.value = pending.value.drop(1)
                    }
                }
            }
        }
    }
}