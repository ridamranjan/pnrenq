package com.first.pnrenq.ui.theme

import org.junit.Assert.*
import org.junit.Test

class PnrBoundedCacheTest {

    @Test
    fun repeatedKeysComputeOnlyOnceUntilEviction() {
        val cache = PnrBoundedCache<Int, String>(64)

        var computations = 0

        repeat(100) {
            for (key in 0 until 16) {
                assertEquals(
                    "color-$key",
                    cache.getOrCompute(key) {
                        computations++
                        "color-$key"
                    }
                )
            }
        }

        assertEquals(
            16,
            computations
        )

        assertEquals(
            16,
            cache.size
        )
    }

    @Test
    fun leastRecentlyUsedEntryIsEvictedAtCapacity() {
        val cache = PnrBoundedCache<Int, String>(2)

        cache.getOrCompute(1) {
            "one"
        }

        cache.getOrCompute(2) {
            "two"
        }

        cache.getOrCompute(1) {
            error("cache miss")
        }

        cache.getOrCompute(3) {
            "three"
        }

        assertEquals(
            2,
            cache.size
        )

        assertEquals(
            "one",
            cache.getOrCompute(1) {
                error("cache miss")
            }
        )

        assertEquals(
            "new two",
            cache.getOrCompute(2) {
                "new two"
            }
        )

        assertEquals(
            2,
            cache.size
        )
    }

    @Test
    fun manyDistinctKeysNeverExceedCapacity() {
        val cache = PnrBoundedCache<Int, Int>(64)

        repeat(1000) { key ->
            assertEquals(
                key * 2,
                cache.getOrCompute(key) {
                    key * 2
                }
            )

            assertTrue(
                cache.size <= 64
            )
        }

        assertEquals(
            64,
            cache.size
        )
    }

    @Test
    fun failedCalculationDoesNotEvictOrPoisonEntries() {
        val cache = PnrBoundedCache<Int, String>(1)

        cache.getOrCompute(1) {
            "one"
        }

        val failure = runCatching {
            cache.getOrCompute(2) {
                error("expected failure")
            }
        }

        assertTrue(
            failure.isFailure
        )

        assertEquals(
            1,
            cache.size
        )

        assertEquals(
            "one",
            cache.getOrCompute(1) {
                error("cache miss")
            }
        )

        assertEquals(
            "two",
            cache.getOrCompute(2) {
                "two"
            }
        )

        assertEquals(
            1,
            cache.size
        )
    }

    @Test
    fun independentCachesNeverShareValuesAndInvalidCapacityIsRejected() {
        val first = PnrBoundedCache<Int, String>(1)
        val second = PnrBoundedCache<Int, String>(1)

        assertEquals(
            "first",
            first.getOrCompute(1) {
                "first"
            }
        )

        assertEquals(
            "second",
            second.getOrCompute(1) {
                "second"
            }
        )

        assertTrue(
            runCatching {
                PnrBoundedCache<Int, Int>(0)
            }.isFailure
        )

        assertTrue(
            runCatching {
                PnrBoundedCache<Int, Int>(-1)
            }.isFailure
        )
    }
}