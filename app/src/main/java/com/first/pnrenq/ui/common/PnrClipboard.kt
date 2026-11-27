package com.first.pnrenq.ui.common

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle
import android.widget.Toast
import com.first.pnrenq.domain.validation.isValidPnr

/** Keep the exact PNR, including leading zeros, but mark its preview as sensitive. */
internal fun createSensitivePnrClip(pnr: String): ClipData? {
    if (!isValidPnr(pnr)) return null

    return ClipData.newPlainText("PNR", pnr).apply {
        // The String constant is inlined with compileSdk >= 33.
        // ClipDescription extras are supported throughout our minSdk 26 range.
        description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
}

/** Explicit user action only; do not read, log or automatically clear clipboard data. */
internal fun copyPnrToClipboard(context: Context, pnr: String): Boolean {
    val clip = createSensitivePnrClip(pnr)

    val copied = if (clip == null) {
        false
    } else {
        try {
            val clipboard = context.getSystemService(
                ClipboardManager::class.java
            )

            if (clipboard == null) {
                false
            } else {
                clipboard.setPrimaryClip(clip)
                true
            }
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalStateException) {
            false
        }
    }

    if (!copied) {
        Toast.makeText(
            context,
            "Unable to copy PNR.",
            Toast.LENGTH_SHORT
        ).show()
    }

    return copied
}