
package com.first.pnrenq.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as SolidText

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.first.pnrenq.R
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.theme.ThemeText as Text

import java.util.Locale

/* =========================================================
   STATUS INFORMATION MODEL

   Badge colors are independent of app appearance,
   palette and background style.
========================================================= */

private data class StatusMeaning(
    val code: String,
    val title: String,
    val explanation: String,
    val badgeBackground: Color,
    val badgeForeground: Color
)

/* =========================================================
   STATUS GLOSSARY

   Common railway reservation statuses and waitlist
   variants are supported.

   Each status has a consistent, fixed color scheme.
========================================================= */

private val reservationGlossary = listOf(

    StatusMeaning(
        code = "CNF",
        title = "Confirmed",
        explanation =
            "Your reservation is confirmed. Coach and " +
                    "berth details may be assigned or finalized " +
                    "when the reservation chart is prepared.",
        badgeBackground = Color(0xFFDCFCE7),
        badgeForeground = Color(0xFF166534)
    ),

    StatusMeaning(
        code = "RAC",
        title = "Reservation Against Cancellation",
        explanation =
            "You can travel with RAC accommodation, " +
                    "usually a shared seat initially. A full " +
                    "berth may become available if another " +
                    "reservation is cancelled.",
        badgeBackground = Color(0xFFFEF3C7),
        badgeForeground = Color(0xFF92400E)
    ),

    StatusMeaning(
        code = "WL",
        title = "Waiting List",
        explanation =
            "Your reservation is on the waiting list. " +
                    "A confirmed seat or berth has not been " +
                    "allotted yet. Check the final status " +
                    "and applicable travel rules.",
        badgeBackground = Color(0xFFFEE2E2),
        badgeForeground = Color(0xFF991B1B)
    ),

    StatusMeaning(
        code = "GNWL",
        title = "General Waiting List",
        explanation =
            "Waiting list under the General quota. " +
                    "Confirmation depends on availability " +
                    "and cancellations in the relevant list.",
        badgeBackground = Color(0xFFFFE4E6),
        badgeForeground = Color(0xFF9F1239)
    ),

    StatusMeaning(
        code = "RLWL",
        title = "Remote Location Waiting List",
        explanation =
            "Waiting list for a remote-location " +
                    "reservation segment. Movement depends " +
                    "on availability in that allocation.",
        badgeBackground = Color(0xFFFFEDD5),
        badgeForeground = Color(0xFF9A3412)
    ),

    StatusMeaning(
        code = "PQWL",
        title = "Pooled Quota Waiting List",
        explanation =
            "Waiting list under a shared reservation " +
                    "pool used for certain station pairs. " +
                    "Confirmation depends on that pool's " +
                    "availability and cancellations.",
        badgeBackground = Color(0xFFFAE8FF),
        badgeForeground = Color(0xFF86198F)
    ),

    StatusMeaning(
        code = "TQWL",
        title = "Tatkal Quota Waiting List",
        explanation =
            "Waiting list for Tatkal quota bookings. " +
                    "Confirmation depends on available " +
                    "accommodation and cancellations.",
        badgeBackground = Color(0xFFFCE7F3),
        badgeForeground = Color(0xFF9D174D)
    ),

    StatusMeaning(
        code = "RQWL",
        title = "Roadside Quota Waiting List",
        explanation =
            "Waiting list associated with the " +
                    "Roadside quota. Confirmation depends " +
                    "on accommodation becoming available " +
                    "for that allocation.",
        badgeBackground = Color(0xFFEDE9FE),
        badgeForeground = Color(0xFF5B21B6)
    ),

    StatusMeaning(
        code = "DPWL",
        title = "Duty Pass Waiting List",
        explanation =
            "Waiting list under the Duty Pass " +
                    "allocation. Confirmation depends on " +
                    "availability within that quota.",
        badgeBackground = Color(0xFFDBEAFE),
        badgeForeground = Color(0xFF1D4ED8)
    ),

    StatusMeaning(
        code = "RSWL",
        title = "Roadside Station Waiting List",
        explanation =
            "Waiting list associated with a roadside " +
                    "station allocation. Confirmation depends " +
                    "on seats becoming available for it.",
        badgeBackground = Color(0xFFCCFBF1),
        badgeForeground = Color(0xFF115E59)
    ),

    StatusMeaning(
        code = "RLGN",
        title = "Roadside Waiting List",
        explanation =
            "A roadside waiting-list designation " +
                    "used in some reservation records. " +
                    "The position is subject to availability.",
        badgeBackground = Color(0xFFD1FAE5),
        badgeForeground = Color(0xFF065F46)
    ),

    StatusMeaning(
        code = "CKWL",
        title = "Tatkal Waiting List",
        explanation =
            "An older designation for the Tatkal " +
                    "waiting list. The reservation is not " +
                    "yet confirmed.",
        badgeBackground = Color(0xFFE0E7FF),
        badgeForeground = Color(0xFF3730A3)
    ),

    StatusMeaning(
        code = "CAN",
        title = "Cancelled",
        explanation =
            "The passenger reservation has been " +
                    "cancelled. It is no longer an active " +
                    "confirmed booking.",
        badgeBackground = Color(0xFFF1F5F9),
        badgeForeground = Color(0xFF334155)
    ),

    StatusMeaning(
        code = "MOD",
        title = "Modified Passenger",
        explanation =
            "The passenger's reservation details " +
                    "have been modified. Review the latest " +
                    "ticket information.",
        badgeBackground = Color(0xFFE0F2FE),
        badgeForeground = Color(0xFF075985)
    ),

    StatusMeaning(
        code = "REGRET",
        title = "No More Booking Permitted",
        explanation =
            "The reservation system is not accepting " +
                    "further bookings for this waiting list. " +
                    "This does not indicate a confirmed seat.",
        badgeBackground = Color(0xFFFCE7F3),
        badgeForeground = Color(0xFF831843)
    ),

    StatusMeaning(
        code = "REL",
        title = "Released",
        explanation =
            "The ticket was not cancelled, but " +
                    "alternative accommodation was provided. " +
                    "Check the updated allocation.",
        badgeBackground = Color(0xFFECFCCB),
        badgeForeground = Color(0xFF3F6212)
    ),

    StatusMeaning(
        code = "WEBCAN",
        title = "Counter Ticket Cancelled Online",
        explanation =
            "A railway counter ticket was cancelled " +
                    "through the internet, but the refund " +
                    "has not been collected.",
        badgeBackground = Color(0xFFFFEDD5),
        badgeForeground = Color(0xFF7C2D12)
    ),

    StatusMeaning(
        code = "WEBCANRF",
        title = "Online Cancellation, Refund Collected",
        explanation =
            "A railway counter ticket was cancelled " +
                    "online and its refund has been collected.",
        badgeBackground = Color(0xFFE2E8F0),
        badgeForeground = Color(0xFF1E293B)
    ),

    StatusMeaning(
        code = "NT",
        title = "Passenger Not Turned Up",
        explanation =
            "The passenger was recorded as not " +
                    "having turned up for the journey.",
        badgeBackground = Color(0xFFFEF9C3),
        badgeForeground = Color(0xFF713F12)
    ),

    StatusMeaning(
        code = "TDR",
        title = "Ticket Deposit Receipt",
        explanation =
            "A TDR claim has been filed. This " +
                    "represents a ticket or refund-related " +
                    "status, not a berth confirmation.",
        badgeBackground = Color(0xFFF3E8FF),
        badgeForeground = Color(0xFF6B21A8)
    ),

    StatusMeaning(
        code = "NOSB",
        title = "No Seat or Berth",
        explanation =
            "No separate seat or berth is allotted " +
                    "under this reservation entry. Check " +
                    "the passenger's booking details.",
        badgeBackground = Color(0xFFCFFAFE),
        badgeForeground = Color(0xFF155E75)
    )

).associateBy { it.code }

/* =========================================================
   STATUS NORMALIZATION

   Examples:
   RAC 7           -> RAC
   RAC/3           -> RAC
   CNF/B1/25       -> CNF
   WL 12           -> WL
   GNWL/15         -> GNWL
   RLWL/10         -> RLWL
   TQWL/4          -> TQWL
   REGRET/WL       -> REGRET

   If combined text contains a booking status
   followed by a current status, the last
   recognized code is preferred.
========================================================= */

private val statusWordPattern = Regex("[A-Z]+")

private val racSeatPattern = Regex(
    "^R\\d+\\s+\\d+\\b"
)

private val statusAliases = mapOf(
    "CONFIRMED" to "CNF",
    "CONFIRM" to "CNF",
    "WAITLIST" to "WL",
    "WAITLISTED" to "WL",
    "WAITING" to "WL",
    "CANCELLED" to "CAN",
    "CANCELED" to "CAN",
    "MODIFIED" to "MOD",
    "RELEASED" to "REL"
)

private fun identifyReservationStatus(
    rawStatus: String?
): StatusMeaning? {
    if (rawStatus.isNullOrBlank()) {
        return null
    }

    val normalized = rawStatus
        .trim()
        .uppercase(Locale.ROOT)

    if (
        normalized in setOf(
            "NA",
            "N/A",
            "NONE",
            "UNKNOWN",
            "-",
            "--"
        )
    ) {
        return null
    }

    // Special combinations must not be mistaken for WL.
    if (normalized.startsWith("REGRET")) {
        return reservationGlossary["REGRET"]
    }

    // Indian Railways also uses R# # for an
    // allocated RAC coach and berth reference.
    if (racSeatPattern.containsMatchIn(normalized)) {
        return reservationGlossary["RAC"]
    }

    val recognized = statusWordPattern
        .findAll(normalized)
        .map { it.value }
        .map { word ->
            statusAliases[word] ?: word
        }
        .mapNotNull { code ->
            reservationGlossary[code]
        }
        .toList()

    return recognized.lastOrNull()
}

/* =========================================================
   EXTRACT UNIQUE CURRENT STATUSES

   - Current status is preferred.
   - Booking status is used only if current is absent.
   - Duplicate statuses are removed.
   - Passenger order is retained.
   - General PNR status is a fallback.
========================================================= */

private fun meaningsForPnr(
    pnr: PnrInfo
): List<StatusMeaning> {
    val passengerMeanings = pnr.passengers
        .mapNotNull { passenger ->
            identifyReservationStatus(
                passenger.currentStatus
            ) ?: identifyReservationStatus(
                passenger.bookingStatus
            )
        }
        .distinctBy { it.code }

    if (passengerMeanings.isNotEmpty()) {
        return passengerMeanings
    }

    return listOfNotNull(
        identifyReservationStatus(pnr.currentStatus)
    )
}

/* =========================================================
   RESERVATION INFORMATION CARD
========================================================= */

@Composable
fun ReservationInformationCard(
    pnr: PnrInfo,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val meanings = remember(pnr) {
        meaningsForPnr(pnr)
    }

    Card(
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow,
            contentColor = scheme.onSurface
        ),

        border = BorderStroke(
            1.dp,
            scheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            /* ---------- Card heading ---------- */

            Row(
                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = scheme.secondaryContainer,
                    contentColor =
                        scheme.onSecondaryContainer,

                    shape = RoundedCornerShape(13.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_pnr_seat
                            ),

                            contentDescription = null,

                            tint = scheme.onSecondaryContainer,

                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),

                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Reservation Information",

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold,

                        color = scheme.onSurface
                    )

                    Text(
                        text = "What the current statuses mean",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color = scheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = scheme.outlineVariant
            )

            /* ---------- Dynamic status glossary ---------- */

            if (meanings.isEmpty()) {

                Text(
                    text = "Current reservation status " +
                            "information is unavailable.",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color = scheme.onSurfaceVariant
                )

            } else {

                meanings.forEachIndexed { index, meaning ->

                    ReservationStatusRow(meaning)

                    if (index < meanings.lastIndex) {

                        HorizontalDivider(
                            color = scheme.outlineVariant.copy(
                                alpha = 0.65f
                            )
                        )
                    }
                }
            }
        }
    }
}

/* =========================================================
   ALIGNED STATUS / DESCRIPTION ROW

   IMPORTANT:
   The left column has a fixed width (96dp).

   All descriptions therefore begin at exactly
   the same horizontal position, including long
   waitlist codes and quota-specific statuses.

   Description text is theme-aware.
   Status badge is theme-independent.
========================================================= */

@Composable
private fun ReservationStatusRow(
    meaning: StatusMeaning
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),

        verticalAlignment = Alignment.Top,

        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        /* ---------- Fixed-width status column ---------- */

        Box(
            modifier = Modifier.width(96.dp)
        ) {

            val badgeShape =
                RoundedCornerShape(10.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 37.dp)
                    .clip(badgeShape)
                    .background(meaning.badgeBackground)
                    .border(
                        width = 1.dp,
                        color = meaning.badgeForeground.copy(
                            alpha = 0.25f
                        ),
                        shape = badgeShape
                    )
                    .padding(
                        horizontal = 6.dp,
                        vertical = 10.dp
                    ),

                contentAlignment = Alignment.Center
            ) {

                // Solid text deliberately bypasses ThemeText.
                SolidText(
                    text = meaning.code,

                    color = meaning.badgeForeground,

                    style =
                        MaterialTheme.typography.labelMedium,

                    fontWeight = FontWeight.Bold,

                    fontSize = if (
                        meaning.code.length > 6
                    ) {
                        10.sp
                    } else {
                        12.sp
                    },

                    textAlign = TextAlign.Center,

                    maxLines = 1
                )
            }
        }

        /* ---------- Flexible information column ---------- */

        Column(
            modifier = Modifier.weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {

            Text(
                text = meaning.title,

                style =
                    MaterialTheme.typography.titleSmall,

                fontWeight = FontWeight.SemiBold,

                color = scheme.onSurface
            )

            Text(
                text = meaning.explanation,

                style =
                    MaterialTheme.typography.bodySmall,

                color = scheme.onSurfaceVariant
            )
        }
    }
}
