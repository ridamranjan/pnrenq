package com.first.pnrenq.domain.model

import java.util.Locale

/** Canonical interpretation only; the stored provider strings are never rewritten. */
internal enum class ReservationCode(val category: StatusCategory) {
    CNF(StatusCategory.CONFIRMED),
    RAC(StatusCategory.RAC),
    WL(StatusCategory.WAITING),
    GNWL(StatusCategory.WAITING),
    RLWL(StatusCategory.WAITING),
    PQWL(StatusCategory.WAITING),
    TQWL(StatusCategory.WAITING),
    RQWL(StatusCategory.WAITING),
    DPWL(StatusCategory.WAITING),
    RSWL(StatusCategory.WAITING),
    RLGN(StatusCategory.WAITING),
    CKWL(StatusCategory.WAITING),
    CAN(StatusCategory.CANCELLED),
    WEBCAN(StatusCategory.CANCELLED),
    WEBCANRF(StatusCategory.CANCELLED),
    MOD(StatusCategory.UNKNOWN),
    REGRET(StatusCategory.UNKNOWN),
    REL(StatusCategory.UNKNOWN),
    NT(StatusCategory.UNKNOWN),
    TDR(StatusCategory.UNKNOWN),
    NOSB(StatusCategory.UNKNOWN),
    MIXED(StatusCategory.UNKNOWN),
    UNKNOWN(StatusCategory.UNKNOWN)
}

internal fun reservationText(value: String?): String? =
    value?.trim()?.takeIf {
        it.isNotEmpty() &&
                it.uppercase(Locale.ROOT) !in missingValues
    }

private val aliases = mapOf(
    "CONFIRMED" to ReservationCode.CNF,
    "CONFIRM" to ReservationCode.CNF,
    "WAITLIST" to ReservationCode.WL,
    "WAITLISTED" to ReservationCode.WL,
    "WAITING" to ReservationCode.WL,
    "CANCELLED" to ReservationCode.CAN,
    "CANCELED" to ReservationCode.CAN,
    "MODIFIED" to ReservationCode.MOD,
    "RELEASED" to ReservationCode.REL
)

private val codes =
    ReservationCode.entries.associateBy { it.name } + aliases

private val missingValues = setOf(
    "NULL", "UNKNOWN", "NOT AVAILABLE", "N/A",
    "NA", "NONE", "-", "--"
)

private val headPattern = Regex("^([A-Z]+)(.*)$")
private val separators = Regex("[\\s/]+")
private val coachPattern = Regex("([A-Z]{1,3})([0-9]{1,3})")
private val racCoachPattern = Regex("R[0-9]{1,3}")
private val numberPattern = Regex("[0-9]+")

// Reuse the existing normalization patterns instead of recompiling per status.
private val waitListPattern =
    Regex("^WAIT(?:ING)? LIST(?=$|[\\s/0-9])")

private val statusHyphenPattern =
    Regex("(?<=[A-Z0-9])-(?=[A-Z0-9])")

private val regretPattern =
    Regex("REGRET(?:[\\s/]+WL)?")

private val quotas = setOf(
    "GN", "TQ", "CK", "LD", "HO", "SS", "DP",
    "HP", "PT", "FT", "DF", "PH", "PQ", "RL", "RS"
)

private val berthTypes = mapOf(
    "LB" to "LB",
    "LOWER" to "LB",
    "LOWER BERTH" to "LB",

    "MB" to "MB",
    "MIDDLE" to "MB",
    "MIDDLE BERTH" to "MB",

    "UB" to "UB",
    "UPPER" to "UB",
    "UPPER BERTH" to "UB",

    "SL" to "SL",
    "SIDE LOWER" to "SL",
    "SIDE LOWER BERTH" to "SL",

    "SU" to "SU",
    "SIDE UPPER" to "SU",
    "SIDE UPPER BERTH" to "SU",

    "WS" to "WS",
    "WINDOW" to "WS",
    "WINDOW SEAT" to "WS",

    "AS" to "AS",
    "AISLE" to "AS",
    "AISLE SEAT" to "AS",

    "MS" to "MS",
    "MIDDLE SEAT" to "MS",
    "CB" to "CB"
)

internal fun berthTypeOf(value: String?): String? =
    reservationText(value)
        ?.uppercase(Locale.ROOT)
        ?.let(berthTypes::get)

internal fun coachOf(value: String?): String? {
    val text = reservationText(value)
        ?.uppercase(Locale.ROOT)
        ?: return null

    val match = coachPattern.matchEntire(text)
        ?: return null

    // A compact status such as WL5 or RAC7 is not a coach identifier.
    if (
        match.groupValues[1] in codes ||
        match.groupValues[2].toInt() == 0
    ) {
        return null
    }

    return text
}

internal data class SeatAllocation(
    val coach: String? = null,
    val berth: Int? = null,
    val type: String? = null,
    val quota: String? = null
) {
    val complete: Boolean
        get() = coach != null && berth != null
}

internal data class ParsedReservation(
    val code: ReservationCode,
    val allocation: SeatAllocation = SeatAllocation(),
    val position: String? = null
)

private fun positiveNumber(value: String?): Int? =
    value
        ?.takeIf(numberPattern::matches)
        ?.toIntOrNull()
        ?.takeIf { it > 0 }

/** A bounded grammar; no search for arbitrary status substrings or last matching words. */
internal fun parseReservation(value: String?): ParsedReservation {
    val raw = reservationText(value)
        ?.uppercase(Locale.ROOT)
        ?: return ParsedReservation(ReservationCode.UNKNOWN)

    val text = raw.replace(waitListPattern, "WL")
        .replace(statusHyphenPattern, " ")

    val unknown = ParsedReservation(
        ReservationCode.UNKNOWN
    )

    val tokens = text.split(separators)
        .filter { it.isNotEmpty() }

    val first = tokens.firstOrNull()
        ?: return unknown

    // Indian Railways uses R# # to represent an allocated RAC coach and berth.
    if (racCoachPattern.matches(first)) {
        val seat = parseSeat(tokens)
            ?: return unknown

        return if (seat.complete) {
            ParsedReservation(
                ReservationCode.RAC,
                seat
            )
        } else {
            unknown
        }
    }

    if (regretPattern.matches(text)) {
        return ParsedReservation(
            ReservationCode.REGRET
        )
    }

    val head = headPattern.matchEntire(first)
        ?: return unknown

    val code = codes[head.groupValues[1]]
        ?: return unknown

    val attached = head.groupValues[2]

    if (
        attached.isNotEmpty() &&
        !numberPattern.matches(attached)
    ) {
        return unknown
    }

    val tail =
        listOfNotNull(attached.takeIf { it.isNotEmpty() }) +
                tokens.drop(1)

    if (tail.isEmpty()) {
        return ParsedReservation(code)
    }

    if (
        code.category == StatusCategory.WAITING ||
        code == ReservationCode.RAC
    ) {
        // A list position is not a berth. Some records contain two positions.
        if (
            tail.size <= 2 &&
            tail.all {
                numberPattern.matches(it) &&
                        it.toIntOrNull() != null
            }
        ) {
            return ParsedReservation(
                code,
                position = tail.joinToString(" ")
            )
        }
    }

    if (
        attached.isEmpty() &&
        (
                code == ReservationCode.CNF ||
                        code == ReservationCode.RAC
                )
    ) {
        parseSeat(tail)?.let {
            return ParsedReservation(code, it)
        }
    }

    if (
        code == ReservationCode.RAC &&
        tail.firstOrNull()?.let {
            numberPattern.matches(it)
        } == true
    ) {
        parseSeat(tail.drop(1))?.let {
            return ParsedReservation(
                code,
                it,
                tail.first()
            )
        }
    }

    return unknown
}

private fun parseSeat(tokens: List<String>): SeatAllocation? {
    val coach = coachOf(tokens.firstOrNull())
        ?: return null

    if (tokens.size == 1) {
        return SeatAllocation(coach = coach)
    }

    val berth = positiveNumber(tokens[1])
        ?: return null

    var suffix = tokens.drop(2)

    val quota = suffix.lastOrNull()
        ?.takeIf { it in quotas }

    if (quota != null) {
        suffix = suffix.dropLast(1)
    }

    val type = if (suffix.isEmpty()) {
        null
    } else {
        berthTypeOf(suffix.joinToString(" "))
            ?: return null
    }

    return SeatAllocation(
        coach,
        berth,
        type,
        quota
    )
}

internal data class ResolvedReservation(
    val code: ReservationCode,
    val text: String?,
    val allocation: SeatAllocation,
    val position: String? = null
)

/** Use structured codes when compatible; do not guess which contradictory field is newer. */
internal fun resolveReservation(
    raw: String?,
    structured: String?
): ResolvedReservation {
    val text = reservationText(raw)
    val explicit = reservationText(structured)

    val parsed = parseReservation(text)

    val structuredCode = explicit?.let {
        codes[it.uppercase(Locale.ROOT)]
    }

    fun result(
        code: ReservationCode,
        display: String? = text
    ) = ResolvedReservation(
        code,
        display,
        parsed.allocation,
        parsed.position
    )

    if (text == null) {
        return ResolvedReservation(
            structuredCode ?: ReservationCode.UNKNOWN,
            explicit,
            SeatAllocation()
        )
    }

    if (
        explicit == null ||
        structuredCode == parsed.code
    ) {
        return result(parsed.code)
    }

    if (
        parsed.code == ReservationCode.UNKNOWN &&
        (
                structuredCode == ReservationCode.CNF ||
                        structuredCode == ReservationCode.RAC
                )
    ) {
        // Some providers send the allocation as text and the status separately.
        val seat = parseSeat(
            text.uppercase(Locale.ROOT)
                .split(separators)
                .filter { it.isNotEmpty() }
        )

        if (seat?.complete == true) {
            return ResolvedReservation(
                structuredCode,
                text,
                seat
            )
        }
    }

    if (
        structuredCode != null &&
        parsed.code.category == StatusCategory.WAITING &&
        structuredCode.category == StatusCategory.WAITING
    ) {
        // A generic WL and a quota-specific waitlist code can describe the same status.
        if (structuredCode == ReservationCode.WL) {
            return result(parsed.code)
        }

        if (parsed.code == ReservationCode.WL) {
            return result(
                structuredCode,
                "$text (${structuredCode.name})"
            )
        }
    }

    // An unrecognized nonblank current status is not equivalent to an absent status.
    return result(
        ReservationCode.UNKNOWN,
        "$text (code $explicit)"
    )
}

internal fun PassengerStatus.currentReservation(): ResolvedReservation =
    resolveReservation(
        currentStatus,
        currentStatusCode
    )

internal fun PassengerStatus.bookingReservation(): ResolvedReservation =
    resolveReservation(
        bookingStatus,
        bookingStatusCode
    )

/** Glossary explanations refer only to current status, in passenger order. */
internal fun currentReservationCodes(
    pnr: PnrInfo
): List<ReservationCode> =
    if (pnr.passengers.isEmpty()) {
        listOf(
            parseReservation(pnr.currentStatus).code
        )
    } else {
        pnr.passengers.mapTo(linkedSetOf()) {
            it.currentReservation().code
        }.toList()
    }

/** Avoid treating different seats or waitlist positions as different status categories. */
internal fun homeReservationText(pnr: PnrInfo): String? {
    val passengers = pnr.passengers

    if (passengers.isEmpty()) {
        return reservationText(pnr.currentStatus)
    }

    val iterator = passengers.iterator()

    val first = iterator.next().currentReservation()
    val code = first.code
    val text = first.text

    var sameText = true

    while (iterator.hasNext()) {
        val resolved =
            iterator.next().currentReservation()

        // Mixed canonical codes always produce Mixed; no remaining row can change that.
        if (resolved.code != code) {
            return "Mixed"
        }

        if (resolved.text != text) {
            sameText = false
        }
    }

    if (code == ReservationCode.UNKNOWN) {
        return if (sameText) {
            text?.let {
                if (
                    parseReservation(it).code ==
                    ReservationCode.UNKNOWN
                ) {
                    it
                } else {
                    "Unknown ($it)"
                }
            } ?: "Unknown"
        } else {
            "Mixed"
        }
    }

    // Retain an unambiguous original label; otherwise display the canonical code.
    return text?.takeIf {
        sameText && parseReservation(it).code == code
    } ?: code.name
}