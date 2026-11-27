package com.first.pnrenq.ui.theme

/** Small access-ordered cache, confined to its owning composition/thread. */
internal class PnrBoundedCache<K, V : Any>(
    private val capacity: Int
) {
    init {
        require(capacity > 0)
    }

    private val entries =
        LinkedHashMap<K, V>(16, 0.75f, true)

    internal val size: Int
        get() = entries.size

    fun getOrCompute(
        key: K,
        compute: () -> V
    ): V {
        entries[key]?.let {
            return it
        }

        // A failed calculation must not evict a valid entry.
        val value = compute()

        entries[key] = value

        if (entries.size > capacity) {
            val iterator =
                entries.entries.iterator()

            iterator.next()
            iterator.remove()
        }

        return value
    }
}