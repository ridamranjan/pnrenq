package com.first.pnrenq.ui.common

import com.first.pnrenq.domain.model.PnrDetails
import java.math.BigDecimal
import java.math.RoundingMode

/** Round only for presentation; never modify the stored Double. */
private fun roundedNonNegative(
    value: Double?,
    scale: Int
): BigDecimal? {
    if (
        value == null ||
        !value.isFinite() ||
        value < 0.0
    ) {
        return null
    }

    return BigDecimal.valueOf(value)
        .setScale(scale, RoundingMode.HALF_UP)
}

/** Stable Indian grouping: three rightmost digits, then groups of two. */
private fun indianNumber(
    amount: BigDecimal
): String {
    val plain = amount.toPlainString()

    val whole = plain.substringBefore('.')
    val fraction = plain.substringAfter('.', "")

    val grouped = if (whole.length <= 3) {
        whole
    } else {
        val leading = whole.dropLast(3)
            .reversed()
            .chunked(2)
            .joinToString(",")
            .reversed()

        "$leading,${whole.takeLast(3)}"
    }

    return if (fraction.isEmpty()) {
        grouped
    } else {
        "$grouped.$fraction"
    }
}

/** Whole rupees stay compact; fractional rupees always show two paise digits. */
internal fun formatFare(
    value: Double?
): String? {
    val amount = roundedNonNegative(
        value,
        2
    ) ?: return null

    val display = if (
        amount.stripTrailingZeros().scale() <= 0
    ) {
        amount.setScale(0)
    } else {
        amount
    }

    return "₹${indianNumber(display)}"
}

/** Keep the existing one-decimal distance precision, independent of currency. */
internal fun formatDistanceKm(
    value: Double?
): String? {
    val amount = roundedNonNegative(
        value,
        1
    ) ?: return null

    return "${indianNumber(amount.stripTrailingZeros())} km"
}

internal data class FarePresentation(
    val primaryLabel: String,
    val primaryAmount: String,
    val secondaryTicketAmount: String? = null
)

/** Prefer a valid booking fare; suppress duplicate amounts at displayed precision. */
internal fun farePresentation(
    details: PnrDetails
): FarePresentation? {
    val booking = formatFare(
        details.bookingFare
    )

    val ticket = formatFare(
        details.ticketFare
    )

    return when {
        booking != null -> FarePresentation(
            primaryLabel = "Booking fare",
            primaryAmount = booking,
            secondaryTicketAmount = ticket?.takeIf {
                it != booking
            }
        )

        ticket != null -> FarePresentation(
            primaryLabel = "Ticket fare",
            primaryAmount = ticket
        )

        else -> null
    }
}