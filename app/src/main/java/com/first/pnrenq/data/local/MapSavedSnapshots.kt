package com.first.pnrenq.data.local

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/**
 * Reuse equal rows from the immediately preceding emission of this collector.
 * Mapping must be deterministic for the entire row, not just its key.
 * Keep at most [maxRetainedRows] raw/mapped pairs; deleted rows are dropped.
 * No cache survives collection cancellation or is shared by collectors.
 */
internal fun <Row, Key, Mapped, Result> Flow<List<Row>>.mapSavedSnapshots(
    maxRetainedRows: Int = 32,
    keyOf: (Row) -> Key,
    mapRow: suspend (Row) -> Mapped,
    assemble: (List<Mapped>) -> Result
): Flow<Result> {
    require(maxRetainedRows > 0)

    return flow {
        var previous = emptyMap<Key, Pair<Row, Mapped>>()

        collect { rows ->
            val next = HashMap<Key, Pair<Row, Mapped>>(
                minOf(rows.size, maxRetainedRows)
            )

            val mapped = ArrayList<Mapped>(rows.size)

            for (row in rows) {
                currentCoroutineContext().ensureActive()

                val key = keyOf(row)
                val cached = previous[key]

                val value = if (
                    cached != null && cached.first == row
                ) {
                    cached.second
                } else {
                    mapRow(row)
                }

                mapped.add(value)

                if (next.size < maxRetainedRows) {
                    next[key] = row to value
                }
            }

            previous = next

            emit(assemble(mapped))
        }
    }.distinctUntilChanged()
}