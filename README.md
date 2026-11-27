# PNR EXPERT

**Your journeys, saved.**

PNR EXPERT is an offline-first Android application for checking, saving, and managing Indian Railways Passenger Name Record (PNR) information.

Built with Kotlin, Jetpack Compose, Material 3, and Room, the application combines a customizable interface with reliable local persistence, manually initiated live lookups, and detailed railway reservation information.

Live PNR information is retrieved through a separately configured HTTPS backend.

> PNR EXPERT is an independent application and is not affiliated with, endorsed by, or operated by Indian Railways, IRCTC, or the Government of India.

---

## Application Information

| Property | Value |
|---|---|
| Application | PNR EXPERT |
| Version | 1.0 |
| Application ID | `com.first.pnrenq` |
| Platform | Android |
| Language | Kotlin |
| UI framework | Jetpack Compose |
| Design system | Material 3 |
| Architecture | Offline-first |
| Local database | Room / SQLite |
| Database schema version | 2 |
| Minimum Android SDK | 26 |
| Target SDK | 37 |
| Compile SDK | 37 |
| License | MIT |

---

## Features

### PNR Management

- Check ten-digit Indian Railways PNR numbers.
- Preserve leading zeros in PNR numbers.
- Automatically save successful PNR responses.
- Prevent duplicate saved reservations.
- View saved reservations without internet access.
- Refresh reservation information manually.
- Preserve existing saved information when a refresh fails.
- Remove individual saved reservations.
- Copy PNR numbers using sensitive clipboard marking.
- Display the last successful check timestamp.
- Organize saved journeys into Upcoming and Past sections.
- Automatically update journey classification while the Home screen is active.

Automatic background network refresh is intentionally not performed.

Opening an existing saved reservation does not automatically consume a live lookup.

### Journey Information

Depending on provider availability, the application displays:

- Train number and train name.
- Journey date.
- Departure and arrival stations.
- Departure and arrival times.
- Journey duration.
- Travel class and reservation quota.
- Boarding point and reservation destination.
- Chart preparation status.
- Booking fare and ticket fare.
- Booking date and ticket type.
- Journey distance.
- Passenger booking and current status.
- Coach and berth allocation.
- VIKALP information.
- Last successful status-check time.

Unavailable optional information is omitted or represented appropriately.

### Passenger Information

Passenger information is presented using distinct booking and current-status badges.

The application:

- Interprets recognized railway reservation codes.
- Preserves relevant original provider status text.
- Separates waiting-list positions from allocated berth numbers.
- Avoids duplicating coach and berth information.
- Distinguishes confirmed, RAC, waiting-list, cancelled, and unknown statuses.
- Preserves conflicting provider information rather than guessing.
- Shows booking status when it remains relevant.
- Uses responsive passenger layouts for narrow screens and enlarged fonts.

Booking badges use red-based colors, while current-status badges use green-based colors. Their foregrounds are adjusted for readable contrast against their backgrounds.

---

## Reservation Information

The Details screen includes a dedicated Reservation Information card explaining the distinct current reservation statuses associated with the saved passengers.

The card works offline and does not initiate additional API requests.

Recognized reservation codes include:

| Code | Meaning |
|---|---|
| CNF | Confirmed |
| RAC | Reservation Against Cancellation |
| WL | Waiting List |
| GNWL | General Waiting List |
| RLWL | Remote Location Waiting List |
| PQWL | Pooled Quota Waiting List |
| TQWL | Tatkal Quota Waiting List |
| RQWL | Roadside Quota Waiting List |
| DPWL | Duty Pass Waiting List |
| RSWL | Roadside Station Waiting List |
| RLGN | Roadside Waiting List |
| CKWL | Tatkal Waiting List, older designation |
| CAN | Cancelled |
| WEBCAN | Counter Ticket Cancelled Online |
| WEBCANRF | Online Cancellation, Refund Collected |
| MOD | Modified Passenger |
| REGRET | No More Booking Permitted |
| REL | Released |
| NT | Passenger Not Turned Up |
| TDR | Ticket Deposit Receipt |
| NOSB | No Seat or Berth |
| MIXED | Multiple reservation statuses |
| UNKNOWN | Unrecognized or unavailable status |

Repeated current status codes are explained only once.

The application does not infer a current reservation status from an unrelated booking status when current information is unknown.

Reservation explanations are informational and do not establish eligibility to travel. Verify applicable railway rules through official channels.

---

## Appearance and Personalization

PNR EXPERT provides a persistent Material 3 appearance system.

### Appearance Modes

| Mode | Description |
|---|---|
| System Default | Follows device appearance |
| Light | Standard light appearance |
| Dark | Standard dark appearance |
| AMOLED | OLED-friendly black background |
| Soft Light | Softer light surfaces |
| Soft Dark | Muted dark surfaces |
| High Contrast Light | Increased foreground contrast |
| High Contrast Dark | Increased foreground contrast on dark surfaces |

### Color Palettes

- Ocean
- Forest
- Sunset
- Midnight
- Rose
- Lagoon
- Amber
- Slate

### Background Styles

**Simplified**

- Clean solid-color backgrounds.
- Theme-aware cards and accent surfaces.
- Minimal visual distractions.
- AMOLED-friendly surfaces.

**Gradient**

- Palette-aware screen backgrounds.
- Coordinated gradient accent surfaces.
- Contrast-aware gradient text.
- Screen-specific background patterns.

| Screen | Background pattern |
|---|---|
| Home | Vertical palette gradient |
| Details | Journey-focused vertical gradient |
| Add PNR | Diagonal gradient |
| Settings | Ambient corner glows |
| Policies & Legal | Settings-style background |

### Time Formats

- 12-hour format, such as 8:35 PM.
- 24-hour format, such as 20:35.

Railway journey classification uses the Asia/Kolkata time zone rather than the device's arbitrary local time zone.

Last-check timestamps are presented consistently using the selected time format.

Fares use Indian digit grouping and appropriate paise precision. Distance values retain appropriate decimal precision.

### Default Preferences

| Setting | Default |
|---|---|
| Appearance | System Default |
| Palette | Ocean |
| Background style | Simplified |
| Time format | 24-hour |

Appearance preferences persist across application restarts.

Resetting appearance preferences does not delete saved reservations.

---

## User Interface

### Home

- PNR EXPERT header.
- Upcoming and Past journey sections.
- Saved reservation cards.
- Reservation status summaries.
- Last successful check information.
- Copy and delete actions.
- Settings and Add PNR controls.
- Loading, empty, error, and retry states.

### Add PNR

- Ten-digit ASCII input validation.
- Support for typing and pasting PNR numbers.
- Digit counter.
- Loading and submission protection.
- Validation and network feedback.
- Local saving after a successful response.

### Details

- Train summary.
- Copy, refresh, and delete actions.
- Journey timeline.
- Passenger Information.
- Reservation Information.
- Journey Information.
- Ticket Information.
- Last Update.

### Settings and Legal

- Appearance and palette selection.
- Background style selection.
- Time format selection.
- Live appearance preview.
- Current preferences.
- Reset to Default.
- Policies & Legal.
- Report a Bug.
- Source Code & Licenses.

Regular Navigation Compose screen transitions and supported predictive-back screen transitions are intentionally disabled.

---

## Accessibility

The application includes accessibility and readability improvements for:

- Enlarged system font sizes.
- Narrow screen layouts.
- Wrapping long train and station names.
- Readable reservation status badges.
- Accessible navigation and action descriptions.
- Minimum-size interactive touch targets.
- Selected and expanded-state semantics.
- Loading and error announcements.
- Masked PNR identifiers in relevant accessibility action labels.
- Contrast-aware text and enabled controls.
- Responsive journey timelines, settings controls, and fare panels.

Regression tests cover text overflow, relevant accessibility semantics, and combinations of supported themes.

These checks improve accessibility but do not constitute a formal accessibility certification.

---

## Architecture

PNR EXPERT uses a separation of presentation, domain, and data responsibilities.

### UI Layer

- Compose screens and reusable components.
- Navigation.
- Appearance preferences and theme rendering.
- ViewModel state.
- Loading, error, and operation feedback.

### Domain Layer

- PNR and passenger models.
- Input validation.
- Reservation-status interpretation.
- Passenger allocation presentation.
- Railway date and time calculations.
- Operation coordination.

### Data Layer

- Room database.
- Transactional persistence.
- Entity and domain mapping.
- Saved-record observation.
- HTTPS network communication.
- Strict backend-response parsing.

### Backend

A separately maintained backend performs live reservation lookups and returns normalized JSON responses.

The backend is responsible for provider communication, request protection, quota management, provider fallback, and protection of upstream credentials.

Private backend source and deployment configuration are not included in this public Android repository.

---

## Offline-First Behavior

### Adding a Reservation

1. Validate the PNR.
2. Request live reservation information.
3. Validate the complete backend response.
4. Save a successful result transactionally.
5. Open the saved reservation details.

An unsuccessful response does not create an invalid saved reservation.

### Opening a Saved Reservation

1. Read the saved local record.
2. Display the last successfully retrieved information.
3. Interpret passenger and reservation statuses locally.

An internet connection is not required for this workflow.

### Refreshing a Reservation

1. Start a manually requested lookup.
2. Validate the returned response.
3. Update the existing reservation if it is still saved.
4. Preserve previous information if the refresh fails.

Conditional refresh protection prevents a refresh operation from silently recreating a deleted reservation.

---

## Reliability and Performance

The application includes:

- Strict request and response validation.
- HTTPS-only backend configuration.
- Bounded network-response processing.
- Explicit network timeout configuration.
- Disabled automatic HTTP retries and redirects.
- Transactional parent and passenger persistence.
- Existing-record-only refresh operations.
- Corrupted-record isolation.
- Lifecycle-aware state and feedback handling.
- Cancellation-aware operations.
- Duplicate-operation protection.
- Scoped operation messages.
- Bounded theme-color computation caches.
- Reuse of unchanged database-record mappings.
- Suppression of equivalent observable-state results.
- Efficient reservation-status aggregation.
- Reuse of journey classifications between departure boundaries.
- Shared strict railway date parsing.
- Bounded PNR input sanitization.

Optimizations are supported by deterministic regression tests and reduced redundant-operation counts.

No application-wide startup, memory, battery, or frame-rate improvement is claimed without representative measurements.

---

## Build Environment

| Component | Version |
|---|---|
| Gradle Wrapper | 9.9.2 |
| Android Gradle Plugin | 9.5.2 |
| Kotlin / Compose compiler configuration | 2.2.10 |
| KSP | 2.3.6 |
| Gradle daemon Java | 25 |
| Java source and target compatibility | 11 |
| Compose BOM | 2026.02.01 |
| Room | 2.8.4 |
| Retrofit | 2.12.0 |
| OkHttp | 4.12.0 |

The version catalog is the authoritative source of declared dependency versions.

Actual transitive dependency selections are determined by Gradle.

The Gradle daemon's embedded Kotlin version is separate from the application's Kotlin compiler configuration.

---

## Getting Started

### Requirements

- Android Studio compatible with the configured Android Gradle Plugin.
- Android SDK Platform 37.
- Java 25 for the Gradle daemon.
- Android emulator or physical device running API 26 or later.
- Internet access for initial dependency downloads and live PNR checks.
- Access to a compatible HTTPS backend.

### Local Configuration

Create or update the ignored root `local.properties` file:

```properties
pnr.api.baseUrl=https://example.invalid/
pnr.githubRepoUrl=https://github.com/YOUR-USERNAME/YOUR-REPOSITORY
```

These are illustrative values and must be replaced locally.

The backend URL must:

- Use HTTPS.
- End with `/`.
- Have a valid hostname and port.
- Contain no credentials.
- Contain no query parameters.
- Contain no fragment.

The GitHub repository URL is optional. Configure it with the public GitHub repository address to enable project links.

Never commit the actual local configuration.

### Build

From the repository root in Windows Command Prompt:

```cmd
gradlew.bat :app:assembleDebug
```

For Linux or macOS:

```bash
./gradlew :app:assembleDebug
```

See [SETUP.md](SETUP.md) for complete setup, testing, security, and release-verification instructions.

---

## Backend API

### Health Check

```http
GET /health
```

### PNR Lookup

```http
POST /v1/pnr/check
Content-Type: application/json
```

Illustrative request:

```json
{
  "pnr": "0123456789"
}
```

The example PNR is synthetic and must not be used for live lookups.

Successful responses contain a Boolean `success` value, normalized `data`, and a check timestamp.

Unsuccessful responses contain a structured error.

The private backend implementation is maintained separately. Access credentials and deployment-specific details are not published here.

---

## Privacy and Security

PNR numbers and reservation information may reveal personal travel details.

### Local Storage

- Successfully retrieved PNR information is stored in app-private Room storage.
- Appearance settings are stored locally.
- Android application backup is disabled in the manifest.
- Database and preference files are excluded by configured backup and device-transfer rules.
- Actual backup behavior may vary by device and Android version.

The application does not implement additional database-level encryption.

### Live Requests

Live checks send the requested PNR to the configured backend, which may forward it to external information providers.

Backend and provider retention practices depend on the deployed services.

No permanent upstream provider API secret should be embedded in the Android application.

BuildConfig values can be extracted from a compiled APK. Local configuration exclusions are not a substitute for authentication.

### Clipboard

Copy PNR actions mark the clipboard content as sensitive.

Supported Android versions may suppress the clipboard preview.

This marking does not encrypt clipboard contents, prevent pasting, or guarantee deletion of clipboard history.

### Deletion and Screen Visibility

Deleting a saved reservation removes its local application record.

It does not guarantee secure erasure or deletion of data held by the backend, external providers, or clipboard services.

Screenshots and screen sharing are not blocked by the application.

### Reporting Problems

Do not include real PNR numbers, passenger information, tickets, access tokens, private backend URLs, or API credentials in public issues or diagnostic attachments.

Open Settings → Policies & Legal for the in-app privacy notice.

---

## Project Structure

```text
pnrenq/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/first/pnrenq/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   ├── remote/
│   │   │   │   │   └── mock/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── operation/
│   │   │   │   │   ├── time/
│   │   │   │   │   └── validation/
│   │   │   │   └── ui/
│   │   │   │       ├── add/
│   │   │   │       ├── common/
│   │   │   │       ├── details/
│   │   │   │       ├── home/
│   │   │   │       ├── legal/
│   │   │   │       ├── navigation/
│   │   │   │       ├── settings/
│   │   │   │       ├── state/
│   │   │   │       └── theme/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── schemas/
│   └── build.gradle.kts
├── gradle/
│   ├── wrapper/
│   ├── gradle-daemon-jvm.properties
│   └── libs.versions.toml
├── gradlew
├── gradlew.bat
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
├── .gitattributes
├── README.md
├── SETUP.md
└── LICENSE.md
```

Generated builds, local configuration, private backend files, and diagnostic reports are excluded from version control.

---

## Testing

The accepted Android regression inventory contains 264 test methods.

| Test category | Methods |
|---|---:|
| JVM unit tests | 155 |
| Android instrumentation tests | 109 |
| Total | 264 |

The original repository contained 47 test methods.

The current inventory includes 217 additional regression methods covering application correctness, reliability, privacy, accessibility, and efficiency.

The complete inventory was reconciled against the uploaded source and test execution reports. All 264 methods were subsequently reported successful under Gradle 9.9.2.

### JVM Tests

```cmd
gradlew.bat :app:testDebugUnitTest
```

### Android Instrumentation Tests

With a dedicated emulator or test device connected:

```cmd
gradlew.bat :app:connectedDebugAndroidTest
```

Some clipboard regression tests intentionally publish synthetic clipboard content.

### Android Lint

```cmd
gradlew.bat :app:lintDebug :app:lintRelease
```

### Release Compilation

```cmd
gradlew.bat :app:assembleRelease
```

Successful release compilation does not by itself establish release signing, optimized runtime correctness, or production security clearance.

---

## Development History

### Phase 0: Repository and Architecture Baseline

- Established the Android project structure.
- Established offline-first reservation management.
- Reviewed application architecture, build configuration, and existing functionality.
- Recorded the original regression inventory.

### Batch 1: Build Reliability

- Reviewed build-tool and SDK configuration.
- Corrected baseline build and test issues.
- Established reproducible setup and verification instructions.

### Batch 2: Network Reliability and Security

- Enforced valid HTTPS backend configuration.
- Added bounded response processing.
- Established explicit network timeouts.
- Preserved cancellation and controlled request behavior.

### Batch 3: Backend Response Validation

- Added strict response parsing.
- Validated requested PNR identity.
- Validated train, journey, passenger, and timestamp fields.
- Prevented malformed responses from replacing saved information.

### Batch 4: Database Integrity

- Established transactional reservation updates.
- Preserved creation timestamps.
- Added conditional refresh protection.
- Isolated unreadable saved records.
- Preserved existing Room schemas and migration compatibility.

### Batch 5: State Management and Navigation

- Improved operation coordination.
- Prevented conflicting and duplicate operations.
- Improved lifecycle-aware navigation.
- Added scoped operation feedback and error recovery.

### Batch 6: Journey and Reservation Correctness

- Improved railway date and time interpretation.
- Established time-zone-aware journey scheduling.
- Improved reservation-code interpretation.
- Improved passenger coach and berth presentation.
- Improved fare and distance formatting.

### Batch 7: Privacy and Accessibility

- Added sensitive clipboard marking.
- Improved privacy disclosures.
- Improved accessibility semantics and touch targets.
- Added contrast-aware text and controls.
- Improved responsive layouts and enlarged-font readability.

### Batch 8: Performance and Maintainability

- Added bounded theme-color reuse.
- Reduced redundant saved-record mapping.
- Optimized reservation aggregation.
- Optimized journey boundary scheduling.
- Consolidated shared railway-date parsing.
- Improved input sanitization and busy-state collection handling.

### Batch 9A: Consolidated Repository Audit

- Reconciled the current source tree and test reports.
- Confirmed the 264-method regression inventory.
- Reviewed build configuration and resolved dependencies.
- Identified release-artifact and dependency-security findings.

### Batch 9A.1: Gradle Security Remediation

- Upgraded Gradle to 9.9.2.
- Regenerated the official Gradle Wrapper.
- Verified published distribution and Wrapper checksums.
- Preserved existing application dependencies.
- Reverified the complete application regression baseline locally.

### Batch 9A.2: Dependency Security Assessment

- Reviewed affected build-time dependencies.
- Evaluated upstream remediation options.
- Assessed dependency verification and locking.

A complete current vulnerability scan and remaining dependency remediation have not been finalized.

### Private Backend: Worker v1.2.0

A separately maintained Worker implementation includes improved quota reservation, provider fallback, bounded waiting, response normalization, and sanitized error handling.

The Worker implementation passed 23 deterministic local regression tests.

Live deployment and production behavior require separate verification.

---

## Known Limitations

PNR EXPERT does not provide:

- Railway ticket booking.
- Ticket cancellation or modification.
- Online payments.
- Live GPS train tracking.
- Automatic background reservation refresh.
- Official railway account integration.

Live information depends on external provider availability and may be delayed or outdated.

Additional database encryption, exhaustive dependency-security clearance, formal accessibility certification, and representative production-performance benchmarks are not established.

The application remains subject to separate release and backend deployment verification.

---

## Contributing

Contributions should preserve:

- Offline functionality.
- Existing saved reservation compatibility.
- Strict input and response validation.
- Transactional persistence.
- Accessibility and readability.
- Appearance preferences.
- Existing regression tests.

Use synthetic data in tests and examples.

Do not publish credentials, private backend implementations, local databases, generated diagnostic logs, or real passenger information.

---

## License

PNR EXPERT is distributed under the MIT License.

See [LICENSE.md](LICENSE.md).

Third-party libraries and services retain their respective licenses and terms.

---

## Disclaimer

PNR EXPERT is an independent informational application.

It does not guarantee ticket confirmation, berth allocation, railway operations, or eligibility to travel.

Always verify important journey information using official railway channels.

**PNR EXPERT — Your journeys, saved.**
