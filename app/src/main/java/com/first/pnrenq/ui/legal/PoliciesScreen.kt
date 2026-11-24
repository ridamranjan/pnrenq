
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.legal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.first.pnrenq.ui.common.PnrBackButton
import com.first.pnrenq.ui.theme.PnrAccentSurface
import com.first.pnrenq.ui.theme.ThemeText as Text

@Composable
fun PoliciesScreen(
    onBack: () -> Unit,
    onReportBug: () -> Unit,
    onOpenLicense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = scheme.onBackground,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Policies & Legal",
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    PnrBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = scheme.onBackground,
                    navigationIconContentColor =
                        scheme.onBackground
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            PnrAccentSurface(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = scheme.primaryContainer,
                foregroundColor = scheme.onPrimaryContainer,
                shape = RoundedCornerShape(22.dp),
                strength = 1.10f
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "PNR EXPERT",
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer
                    )

                    Text(
                        text = "Privacy, transparency and " +
                                "responsible railway information",
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimaryContainer
                    )

                    Text(
                        text = "Last updated: 24 November 2026",
                        style =
                            MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimaryContainer
                    )
                }
            }

            LegalSectionCard(
                title = "Privacy Policy",
                paragraphs = listOf(
                    "PNR EXPERT allows you to check railway " +
                            "PNR information and save successful " +
                            "results on your Android device.",

                    "This notice explains the application's " +
                            "intended handling of PNR numbers, " +
                            "reservation information and service requests."
                )
            )

            LegalSectionCard(
                title = "1. Information Used",
                paragraphs = listOf(
                    "When you enter a PNR number, the app " +
                            "uses it to request reservation " +
                            "information. The result can contain " +
                            "train, journey, passenger and " +
                            "reservation details.",

                    "The app also stores your selected " +
                            "appearance, color palette, " +
                            "background style and time format."
                )
            )

            LegalSectionCard(
                title = "2. Local Storage",
                paragraphs = listOf(
                    "Successfully checked PNR records are " +
                            "saved in the application's local " +
                            "Room database so you can view them offline.",

                    "Opening a saved record does not " +
                            "automatically start another online check."
                )
            )

            LegalSectionCard(
                title = "3. Internet Requests",
                paragraphs = listOf(
                    "When you add or manually refresh a PNR, " +
                            "the PNR number is sent through HTTPS " +
                            "to the configured serverless backend.",

                    "The backend may forward the PNR to " +
                            "third-party railway information " +
                            "providers to obtain a status response.",

                    "These services may also process " +
                            "technical request information, " +
                            "such as network metadata. Their " +
                            "data handling and retention depend " +
                            "on the services actually deployed."
                )
            )

            LegalSectionCard(
                title = "4. Data Retention and Deletion",
                paragraphs = listOf(
                    "Saved journey records remain in the " +
                            "app's local storage until you delete " +
                            "them or the application's local " +
                            "data is cleared.",

                    "You can remove an individual saved PNR " +
                            "using the Delete action. Clearing app " +
                            "data also removes locally stored records.",

                    "Device backup and restore settings may " +
                            "affect whether local data is backed up. " +
                            "Deleting a local record does not itself " +
                            "request deletion of data or logs " +
                            "held by external services."
                )
            )

            LegalSectionCard(
                title = "5. Accounts and Bug Reports",
                paragraphs = listOf(
                    "The current PNR EXPERT workflow does not " +
                            "require creating a user account.",

                    "Bug reports are submitted through GitHub " +
                            "Issues, a public third-party service. " +
                            "Do not include real PNR numbers, " +
                            "passenger names, contact details, " +
                            "tickets, API keys or other personal " +
                            "information in public issues."
                )
            )

            LegalSectionCard(
                title = "Terms of Use",
                paragraphs = listOf(
                    "PNR EXPERT is provided as an independent " +
                            "tool for informational railway " +
                            "reservation lookups.",

                    "The app does not book, cancel or modify " +
                            "railway tickets. It does not guarantee " +
                            "ticket confirmation, berth allocation " +
                            "or train operations.",

                    "Use the service lawfully. Do not " +
                            "attempt to abuse the application, " +
                            "its backend, rate limits or " +
                            "third-party information providers."
                )
            )

            LegalSectionCard(
                title = "Railway Information Disclaimer",
                paragraphs = listOf(
                    "PNR EXPERT is not affiliated with, " +
                            "endorsed by or operated by Indian " +
                            "Railways, IRCTC or the Government of India.",

                    "Railway status information may be " +
                            "delayed, unavailable or changed " +
                            "after the latest successful check.",

                    "Verify important journey information " +
                            "using official railway channels, " +
                            "especially before traveling."
                )
            )

            LegalSectionCard(
                title = "Availability and Third Parties",
                paragraphs = listOf(
                    "Live lookups require internet access " +
                            "and a functioning backend. Provider " +
                            "outages, request limits or changes " +
                            "may temporarily prevent a lookup.",

                    "Third-party APIs, GitHub and other " +
                            "external services are governed by " +
                            "their applicable terms and privacy " +
                            "practices."
                )
            )

            LegalSectionCard(
                title = "Software License",
                paragraphs = listOf(
                    "The PNR EXPERT source repository " +
                            "is published under the MIT License.",

                    "Third-party libraries and services " +
                            "retain their own licenses. " +
                            "Refer to their respective license " +
                            "notices for applicable conditions."
                )
            )

            LegalActionCard(
                title = "View Project License",
                description = "Read the MIT License on GitHub",
                onClick = onOpenLicense
            )

            LegalSectionCard(
                title = "Developer and Contact",
                paragraphs = listOf(
                    "Developer: Ridam Ranjan.",

                    "You can report non-sensitive app problems " +
                            "through the project's GitHub Issues page.",

                    "For private privacy or data inquiries, " +
                            "use the developer contact information " +
                            "published with the app. Do not share " +
                            "personal data in public bug reports."
                )
            )

            LegalActionCard(
                title = "Report a Bug",
                description = "Open GitHub Issues in your browser",
                onClick = onReportBug
            )

            Text(
                text = "Review the privacy notice against " +
                        "the deployed backend and provider " +
                        "practices before public release.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LegalSectionCard(
    title: String,
    paragraphs: List<String>
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),

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
                Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )

            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LegalActionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.secondaryContainer,
            contentColor = scheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSecondaryContainer
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSecondaryContainer
                )
            }

            Text(
                text = "↗",
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSecondaryContainer
            )
        }
    }
}
