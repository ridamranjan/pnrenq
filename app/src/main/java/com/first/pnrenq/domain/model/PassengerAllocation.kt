package com.first.pnrenq.domain.model

/** Presentation data; booking and current information are never merged together. */
internal data class PassengerStatusPresentation(
    val booking: String?,
    val current: String
)

private data class AllocationPresentation(
    val text: String?,
    val complete: Boolean = false
)

private fun allocationPresentation(
    status: ResolvedReservation,
    coach: String?,
    berthNumber: Int?,
    legacyBerth: String?,
    berthType: String?
): AllocationPresentation {
    if (
        status.code != ReservationCode.CNF &&
        status.code != ReservationCode.RAC
    ) {
        // A stale coach/berth must not turn a waiting or cancelled record into an allocation.
        return AllocationPresentation(status.text)
    }

    val embedded = status.allocation

    val explicitCoach = coachOf(coach)

    val explicitBerth = berthNumber?.takeIf {
        it > 0
    }

    val legacyNumber = reservationText(legacyBerth)
        ?.toIntOrNull()
        ?.takeIf { it > 0 }

    val explicitType = berthTypeOf(berthType)
    val legacyType = berthTypeOf(legacyBerth)

    val number = explicitBerth ?: legacyNumber
    val type = explicitType ?: legacyType

    fun <T> disagree(a: T?, b: T?): Boolean =
        a != null && b != null && a != b

    if (
        disagree(explicitBerth, legacyNumber) ||
        disagree(explicitType, legacyType) ||
        disagree(embedded.coach, explicitCoach) ||
        disagree(embedded.berth, number) ||
        disagree(embedded.type, type)
    ) {
        // Preserve the original status and do not hide booking history on ambiguous allocation.
        return AllocationPresentation(
            "${status.text ?: status.code.name} (allocation differs)"
        )
    }

    val merged = SeatAllocation(
        coach = embedded.coach ?: explicitCoach,
        berth = embedded.berth ?: number,
        type = embedded.type ?: type,
        quota = embedded.quota
    )

    // Do not display a number as an allocated berth unless its coach is known.
    if (merged.coach == null) {
        return AllocationPresentation(status.text)
    }

    // A berth type without an assigned berth may be a preference rather than an allocation.
    val text = listOfNotNull(
        status.code.name,
        status.position,
        merged.coach,
        merged.berth?.toString(),
        merged.type.takeIf { merged.complete },
        merged.quota
    ).joinToString(" ")

    return AllocationPresentation(
        text,
        merged.complete
    )
}

internal fun passengerStatusPresentation(
    passenger: PassengerStatus
): PassengerStatusPresentation {
    val current = passenger.currentReservation()

    val currentDisplay = allocationPresentation(
        current,
        passenger.coach,
        passenger.currentBerthNo,
        passenger.berth,
        passenger.currentBerthCode
    )

    val booking = if (
        current.code == ReservationCode.CNF &&
        currentDisplay.complete
    ) {
        // Preserve the existing compact layout only for a reliably allocated confirmation.
        null
    } else {
        allocationPresentation(
            passenger.bookingReservation(),
            passenger.bookingCoach,
            passenger.bookingBerthNo,
            null,
            passenger.bookingBerthCode
        ).text
    }

    return PassengerStatusPresentation(
        booking,
        currentDisplay.text ?: "Unknown"
    )
}