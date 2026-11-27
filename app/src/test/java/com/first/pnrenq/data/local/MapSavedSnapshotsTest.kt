package com.first.pnrenq.data.local

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MapSavedSnapshotsTest {

    private data class Row(
        val id: Int,
        val payload: String,
        val metadata: Int = 0
    )

    @Test
    fun unchangedRowsAreReusedAndEquivalentResultsAreSuppressed() =
        runBlocking {
            val original = (1..16).map {
                Row(it, "original")
            }

            val changed = original.map {
                if (it.id == 1) {
                    it.copy(payload = "changed")
                } else {
                    it
                }
            }

            val metadataOnly = changed.map {
                if (it.id == 1) {
                    it.copy(metadata = 1)
                } else {
                    it
                }
            }

            var calls = 0

            val results = flowOf(
                original,
                original,
                changed,
                metadataOnly
            ).mapSavedSnapshots(
                keyOf = { it.id },

                mapRow = {
                    calls++
                    it.payload
                },

                assemble = { it }
            ).toList()

            assertEquals(
                listOf(
                    original.map { it.payload },
                    changed.map { it.payload }
                ),
                results
            )

            // 16 initial + changed row + metadata-only row.
            assertEquals(18, calls)
        }

    @Test
    fun nullMappingsAreReusedButRepairsAndDeletionAreVisible() =
        runBlocking {
            val bad = Row(1, "bad")
            val repaired = bad.copy(payload = "valid")

            var calls = 0

            val results = flowOf(
                listOf(bad),
                listOf(bad),
                listOf(repaired),
                emptyList(),
                listOf(bad)
            ).mapSavedSnapshots(
                keyOf = { it.id },

                mapRow = {
                    calls++

                    it.payload.takeUnless { value ->
                        value == "bad"
                    }
                },

                assemble = { it }
            ).toList()

            assertEquals(
                listOf(
                    listOf(null),
                    listOf("valid"),
                    emptyList(),
                    listOf(null)
                ),
                results
            )

            assertEquals(3, calls)
        }

    @Test
    fun retainedRowsAreBoundedAndEveryRowStillAppearsInOrder() =
        runBlocking {
            val rows = (1..40).map {
                Row(it, "value-$it")
            }

            val calls = mutableMapOf<Int, Int>()

            val results = flowOf(
                rows,
                rows,
                rows.reversed()
            ).mapSavedSnapshots(
                maxRetainedRows = 32,

                keyOf = { it.id },

                mapRow = {
                    calls[it.id] =
                        (calls[it.id] ?: 0) + 1

                    it.payload
                },

                assemble = { it }
            ).toList()

            assertEquals(
                listOf(
                    rows.map { it.payload },
                    rows.reversed().map { it.payload }
                ),
                results
            )

            assertTrue(
                (1..32).all {
                    calls[it] == 1
                }
            )

            assertTrue(
                (33..40).all {
                    calls[it] == 3
                }
            )
        }

    @Test
    fun independentCollectionsStartWithEmptyCaches() =
        runBlocking {
            val rows = listOf(
                Row(1, "one"),
                Row(2, "two")
            )

            var calls = 0

            val observed = flowOf(
                rows,
                rows
            ).mapSavedSnapshots(
                keyOf = { it.id },

                mapRow = {
                    calls++
                    it.payload
                },

                assemble = { it }
            )

            assertEquals(
                listOf(listOf("one", "two")),
                observed.toList()
            )

            assertEquals(
                listOf(listOf("one", "two")),
                observed.toList()
            )

            assertEquals(4, calls)
        }

    @Test
    fun mappingCancellationPropagatesWithoutEmittingPartialSnapshot() =
        runBlocking {
            val rows = listOf(
                Row(1, "one"),
                Row(2, "two"),
                Row(3, "three")
            )

            val visited = mutableListOf<Int>()
            val emitted = mutableListOf<List<String>>()

            val failure = runCatching {
                flowOf(rows).mapSavedSnapshots(
                    keyOf = { it.id },

                    mapRow = {
                        visited.add(it.id)

                        if (it.id == 2) {
                            throw CancellationException(
                                "synthetic cancellation"
                            )
                        }

                        it.payload
                    },

                    assemble = { it }
                ).toList(emitted)
            }.exceptionOrNull()

            assertTrue(
                failure is CancellationException
            )

            assertEquals(
                listOf(1, 2),
                visited
            )

            assertTrue(
                emitted.isEmpty()
            )
        }
}