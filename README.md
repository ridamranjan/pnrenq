
# PNR EXPERT

**PNR EXPERT** is a lightweight, offline-first Android application for checking, saving, and managing Indian Railways Passenger Name Record (PNR) information.

Built with **Kotlin, Jetpack Compose, Material 3, and Room**, the application provides a modern interface, customizable themes, locally saved journey information, and manually initiated PNR checks.

Live PNR information is retrieved through a separately configured serverless backend.

> **Disclaimer:** PNR EXPERT is an independent application and is not affiliated with Indian Railways, IRCTC, or the Government of India.

---

## Features

### PNR Management

- Check 10-digit Indian Railways PNR numbers.
- Automatically save successfully retrieved PNR information.
- View saved PNRs without an internet connection.
- Refresh reservation status manually.
- Delete saved PNR records.
- Prevent duplicate saved PNR entries.
- Preserve existing information when refreshing fails.
- Copy PNR numbers to the clipboard.
- View the last successful check timestamp.
- Organize saved PNRs into upcoming and past journeys.

**Automatic background refresh is not performed.**

### Journey Details

Depending on the information returned by the provider, the app displays:

- PNR number
- Train name and number
- Journey date
- Departure and arrival stations
- Departure and arrival times
- Journey duration
- Travel class
- Reservation quota
- Boarding station
- Destination station
- Chart preparation status
- Ticket fare
- Booking date
- Ticket type
- Journey distance
- Passenger booking status
- Passenger current status
- Coach and berth allocation
- VIKALP information
- Last successful status-check time

Unavailable fields are omitted when appropriate.

---

## Reservation Information

PNR EXPERT includes a dedicated **Reservation Information** card inside the PNR Details screen.

The card explains the meaning of the current reservation statuses found in the selected PNR.

### Features

- Displays only relevant reservation statuses.
- Avoids repeating explanations for multiple passengers with the same status.
- Supports confirmed, RAC, waiting-list, and other recognized reservation statuses.
- Uses a seat icon in the card header.
- Displays status abbreviations on the left.
- Displays their meanings and explanations on the right.
- Maintains consistent horizontal alignment for all status codes.
- Works completely offline using saved PNR information.

### Example

Passenger information:

```text
P1     RAC 7 / RAC 3
P2     WL 12 / CNF
P3     WL 15 / WL 5
```

Reservation Information:

```text
RAC   Reservation Against Cancellation
      A passenger can travel with RAC
      accommodation. A full berth may
      become available after cancellations.

CNF   Confirmed
      The passenger's reservation is
      confirmed.

WL    Waiting List
      The reservation has not yet been
      confirmed. Check the final status
      and applicable travel rules.
```

Each distinct current status appears only once.

### Supported Status Codes

The explanation system recognizes common status codes, including:

| Status | Meaning |
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
| CKWL | Tatkal Waiting List (older designation) |
| CAN | Cancelled |
| MOD | Modified Passenger |
| REGRET | No More Booking Permitted |
| REL | Released |
| WEBCAN | Counter Ticket Cancelled Online |
| WEBCANRF | Online Cancellation, Refund Collected |
| NT | Passenger Not Turned Up |
| TDR | Ticket Deposit Receipt |
| NOSB | No Seat or Berth |

The card recognizes status codes from saved passenger information. When a current status cannot be identified, the booking status may be used as a fallback.

### Status Colors

Reservation Information badges use fixed colors independent of the selected app theme.

Each recognized status has its own predefined color scheme.

The explanation text follows the selected appearance mode and color palette.

The status column uses a consistent width so that all explanations begin at the same horizontal position.

---

## Passenger Status Display

Passenger reservation information is displayed in compact, color-coded badges.

For example:

```text
P1       RAC 7 / RAC 3
P2       WL 12 / CNF
P3       CNF B1 25
```

The passenger list uses the following visual rules:

- Booking status uses red.
- Current status uses green.
- Passenger identifiers use theme-aware surfaces.
- Confirmed passengers with allocated berths may display only the current-status badge.
- Coach and berth details are formatted to avoid duplication.
- Passenger-status colors remain separate from the selected theme.

The Reservation Information card appears below the passenger list rather than repeating explanations under each passenger.

---

## Appearance and Personalization

PNR EXPERT supports an extensive Material 3 theme system.

### Appearance Modes

Eight appearance modes are available:

| Mode | Description |
|---|---|
| System Default | Follows the device appearance |
| Light | Standard light appearance |
| Dark | Standard dark appearance |
| AMOLED | OLED-friendly dark appearance |
| Soft Light | Softer light surfaces |
| Soft Dark | Muted dark surfaces |
| High Contrast Light | Increased contrast on light surfaces |
| High Contrast Dark | Increased contrast on dark surfaces |

### Color Palettes

Eight color palettes are available:

| Palette | Color family |
|---|---|
| Ocean | Blue |
| Forest | Green |
| Sunset | Orange |
| Midnight | Violet |
| Rose | Pink |
| Lagoon | Teal |
| Amber | Golden yellow |
| Slate | Steel blue |

All palettes work with all supported appearance modes.

### Background Styles

Two background styles are provided.

**Simplified**

- Clean, solid-color backgrounds.
- Minimal visual distractions.
- Solid theme-aware cards.
- Solid accent surfaces.
- AMOLED-friendly surfaces in AMOLED mode.

**Gradient**

- Palette-aware screen backgrounds.
- Gradient accent cards.
- Gradient-themed primary UI surfaces.
- Theme-aware text gradients.
- Contrast-aware foreground colors.
- Distinct background patterns for individual screens.

### Screen-Specific Backgrounds

Each screen has its own background pattern while using the selected color palette.

| Screen | Background |
|---|---|
| Home | Vertical palette gradient |
| PNR Details | Journey-focused vertical gradient |
| Add PNR | Diagonal gradient |
| Settings | Ambient corner-glow gradient |
| Policies & Legal | Settings-style background |

Backgrounds are drawn using Jetpack Compose.

The application does not require external background images.

### Gradient Accent Components

The following UI elements support coordinated palette gradients:

- PNR EXPERT header
- Home Settings button
- Home Add PNR button
- Train Summary card
- Last Update card
- Live Preview card
- Current Preferences card
- Your Journey Saved card
- Add PNR numeric badge

These components retain their original solid background colors in Simplified mode.

The Add PNR button uses a single rounded gradient surface to prevent mismatched rectangular background layers.

### Text and Contrast

The app uses theme-aware colors for:

- Screen headings
- Card titles
- Information labels
- Navigation icons
- Form text
- Journey information
- Settings controls
- Legal information

Gradient text is applied where appropriate while maintaining readable foreground colors.

Some UI elements deliberately retain solid text, including passenger status badges, input digits, and certain native Material controls.

---

## Settings

The Settings screen supports:

- Appearance selection
- Color Palette selection
- Background Style selection
- Time Format selection
- Live Preview
- Current Preferences
- Reset to Default
- Policies & Legal
- Report a Bug
- Source Code & Licenses

### Expandable Selection Cards

Appearance and Color Palette use collapsible selection cards.

- The selected option appears when collapsed.
- Tapping expands the available options.
- Selecting an option applies it immediately.
- The section collapses after selection.
- Only one expandable section remains open at a time.

### Time Format

Two time formats are available:

| Format | Example |
|---|---|
| 12-hour | 8:35 PM |
| 24-hour | 20:35 |

Preferences persist across application restarts.

### Reset to Default

Reset to Default restores:

| Setting | Default |
|---|---|
| Appearance | System Default |
| Palette | Ocean |
| Background Style | Simplified |
| Time Format | 24-hour |

Resetting appearance preferences does not delete saved PNR information.

---

## User Interface

PNR EXPERT uses Jetpack Compose and Material 3.

### Home Screen

The Home screen includes:

- Compact PNR EXPERT header
- Upcoming journeys
- Past journeys
- Compact saved-PNR cards
- PNR copy action
- PNR deletion action
- Reservation status summary
- Last successful check timestamp
- Settings button at the bottom-left
- Add PNR button at the bottom-right

The Settings button uses a 48 dp surface.

The Add PNR button uses a single continuous rounded background in both Simplified and Gradient modes.

### Add PNR Screen

The Add PNR screen includes:

- Icon-only Back button
- 10-digit numeric input
- Input validation
- Digit counter
- Loading state
- Network error feedback
- Manual Check PNR action
- Offline-saving information card

Successful PNR lookups are saved locally.

### PNR Details Screen

The Details screen contains:

1. Train Summary
2. PNR copy, refresh, and delete actions
3. Journey timeline
4. Passenger Information
5. Reservation Information
6. Journey Information
7. Ticket Information
8. Last Update

The Ticket Information section retains its money icon and separate fare panel.

### Navigation

The application uses Navigation Compose.

In-app navigation transitions are disabled:

- No forward slide animation
- No forward fade animation
- No Back navigation slide animation
- No Back navigation fade animation
- No Navigation Compose predictive-back screen transition, where supported

Navigation uses icon-only Back buttons.

Android system-level gesture animations are controlled separately by the operating system.

---

## Policies & Legal

PNR EXPERT includes a dedicated in-app Policies & Legal screen.

It is accessible through:

```text
Settings
   |
   v
Policies & Legal
```

The screen contains information about:

- Privacy Policy
- Information used by the app
- Local PNR storage
- Internet requests
- Data retention and deletion
- User accounts
- Bug reports
- Terms of Use
- Railway information disclaimer
- Third-party services
- Software licenses
- Developer contact

The legal screen follows the selected app theme and uses the same background system as Settings.

**Important:** In-app legal text must be reviewed against the actual deployed backend and third-party provider practices before a public release.

An in-app privacy notice does not replace the need for a publicly accessible privacy-policy URL where required by an app distribution platform.

---

## Bug Reporting and Project Links

The Settings screen includes:

**Report a Bug**

Opens the configured project's issue tracker in an external browser.

**Source Code & Licenses**

Opens the configured project repository.

**View Project License**

Available from the Policies & Legal screen.

Project-specific links are not hardcoded into the Android Kotlin source.

Instead, the repository URL is provided through the locally ignored `local.properties` file and exposed to the application through a generated BuildConfig field.

The Android application uses the base URL to construct links to:

```text
/issues
/blob/main/LICENSE.md
```

If the repository URL is not configured, project-link actions display an error message rather than crashing.

The configured repository address is not a secret and can still be discovered from a compiled APK or an opened browser page.

---

## Technology Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI framework | Jetpack Compose |
| Design system | Material 3 |
| Navigation | Navigation Compose |
| State management | ViewModel |
| Data layer | Repository pattern |
| Local database | Room / SQLite |
| Networking | Retrofit / OkHttp |
| JSON processing | Gson |
| Asynchronous operations | Kotlin Coroutines / Flow |
| Backend | Serverless HTTPS API |
| Build system | Gradle Kotlin DSL |
| Symbol processing | KSP |

---

## Architecture

PNR EXPERT uses an offline-first Android client with a separately configured serverless backend.

```text
Android Application
        |
        +-- Home Screen
        +-- Add PNR Screen
        +-- PNR Details Screen
        |       |
        |       +-- Passenger Information
        |       +-- Reservation Information
        |       +-- Journey Information
        |       +-- Ticket Information
        |
        +-- Settings Screen
        |       |
        |       +-- Theme Preferences
        |       +-- Policies & Legal
        |       +-- External Project Links
        |
        v
   ViewModel Layer
        |
        v
   Repository Layer
        |
        +-------------------------+
        |                         |
        v                         v
   Room Database             HTTPS Backend
        |                         |
        v                         v
   Offline Access          PNR Data Providers
                                  |
                                  v
                          Normalized Response
                                  |
                                  v
                             Repository
                                  |
                                  v
                            Room Database
```

### Android Architecture

**UI Layer**

- Jetpack Compose screens
- Navigation
- Reusable UI components
- Theme and appearance management

**State Layer**

- ViewModel
- UI state
- Loading and error states
- Navigation events

**Domain Layer**

- PNR models
- Validation
- Reservation information rules
- Business logic

**Data Layer**

- Room persistence
- Remote API communication
- Data mapping
- Repository operations

### Backend Responsibilities

The separately configured serverless backend is designed to handle:

- Input validation
- Provider selection
- Provider fallback
- Request protection
- Usage limits
- Error handling
- Normalized PNR responses
- Protection of third-party API credentials

The backend's actual provider configuration and deployment status are maintained separately from the Android application.

Third-party provider API keys must not be embedded in the Android APK.

---

## Offline-First Behavior

### Adding a PNR

```text
Enter PNR
    |
    v
Validate Input
    |
    v
Request Live Status
    |
    v
Successful Response?
    |
    +-- Yes --> Save Locally --> Show Details
    |
    +-- No  --> Show Error
```

### Viewing a Saved PNR

```text
Open Home
    |
    v
Read Room Database
    |
    v
Display Saved Journeys
    |
    v
Select PNR
    |
    v
Show Saved Information
```

Opening a saved PNR does not automatically initiate an online request.

### Manual Refresh

```text
Tap Refresh
    |
    v
Request Current Status
    |
    +-- Success --> Update Saved Record
    |
    +-- Failure --> Preserve Existing Record
```

Offline information represents the last successful check and may be outdated.

The Reservation Information card works using saved passenger status values and does not require an additional API request.

---

## Project Structure

```text
pnrenq/
|
+-- app/
|   |
|   +-- src/
|   |   |
|   |   +-- main/
|   |   |   |
|   |   |   +-- java/com/first/pnrenq/
|   |   |   |   |
|   |   |   |   +-- data/
|   |   |   |   +-- domain/
|   |   |   |   |
|   |   |   |   +-- ui/
|   |   |   |       |
|   |   |   |       +-- home/
|   |   |   |       +-- add/
|   |   |   |       +-- details/
|   |   |   |       +-- settings/
|   |   |   |       +-- legal/
|   |   |   |       +-- navigation/
|   |   |   |       +-- state/
|   |   |   |       +-- common/
|   |   |   |       +-- theme/
|   |   |   |
|   |   |   +-- res/
|   |   |
|   |   +-- test/
|   |   +-- androidTest/
|   |
|   +-- build.gradle.kts
|
+-- gradle/
+-- build.gradle.kts
+-- settings.gradle.kts
+-- gradle.properties
+-- README.md
+-- SETUP.md
+-- LICENSE.md
+-- .gitignore
```

### Important UI Components

```text
ui/theme/
    Theme.kt
    ThemePreferences.kt
    ThemeText.kt
    AppScreenBackground.kt
    PnrAccentSurface.kt

ui/details/
    DetailsScreen.kt
    ReservationInformationCard.kt

ui/legal/
    PoliciesScreen.kt

ui/navigation/
    AppNavigation.kt
```

The theme system and reservation explanations are implemented separately from the network and database layers.

---

## Installation and Setup

### Requirements

- Android Studio
- Compatible Gradle JDK
- Android SDK
- Android emulator or physical device
- Internet connection for live PNR checks
- Configured serverless API endpoint

### Android Configuration

| Property | Value |
|---|---|
| Application ID | `com.first.pnrenq` |
| Minimum SDK | 26 |
| Target SDK | 37 |
| Compile SDK | 37 |
| Java compatibility | 11 |

Other build-tool and library versions are defined by the Gradle configuration.

### Open the Project

1. Clone or download the project.
2. Open it in Android Studio.
3. Install the SDK components requested by Gradle.
4. Configure `local.properties`.
5. Sync Gradle.
6. Build and run the Android application.

Detailed setup information is available in [SETUP.md](SETUP.md).

---

## Local Configuration

PNR EXPERT reads environment-specific configuration from the root `local.properties` file.

This file must remain excluded from Git.

Required properties:

| Property | Purpose |
|---|---|
| `pnr.api.baseUrl` | HTTPS serverless backend address |
| `pnr.githubRepoUrl` | Optional project repository address |

The backend address must:

- Use HTTPS.
- End with `/`.
- Not contain query parameters.
- Not contain fragments.
- Not contain embedded credentials.

The repository address is optional for building the app, but required for its configured external project links to work.

The Gradle build generates these BuildConfig fields:

```kotlin
BuildConfig.PNR_API_BASE_URL
BuildConfig.GITHUB_REPO_URL
```

The actual configured URLs are not committed to the repository.

Ensure the root `.gitignore` contains:

```gitignore
local.properties
```

**Security note:** BuildConfig values are included in the compiled application. This configuration prevents committing local URLs to Git, but it is not a mechanism for hiding secrets in the APK.

Sensitive provider credentials must remain on the serverless backend.

---

## Backend API

The Android application expects a compatible HTTPS backend.

### Health Check

```http
GET /health
```

### PNR Check

```http
POST /v1/pnr/check
Content-Type: application/json
```

Example request:

```json
{
  "pnr": "1234567890"
}
```

The example number is illustrative.

The backend response must match the application's supported PNR data models.

Backend deployment, API credentials, provider quotas, and failover configuration are maintained separately.

---

## Privacy and Security

PNR EXPERT is designed around local persistence and serverless live lookups.

- Successfully retrieved PNR records are stored locally using Room.
- Theme preferences are stored locally.
- Saved PNR details can be viewed offline.
- Live lookups require a network request.
- The backend may forward PNR requests to external information providers.
- Individual saved PNRs can be deleted from the app.
- No user account is required for the current workflow.
- Third-party provider credentials are not embedded in the app.

PNR numbers and railway reservation information may reveal personal travel details.

Users should avoid publishing real PNRs, passenger information, tickets, access tokens, or API credentials.

Backend logs, request metadata, provider retention, and any applicable device-backup behavior must be assessed according to the actual deployed services.

For privacy information, open **Settings → Policies & Legal**.

---

## Testing Checklist

### PNR Functions

- [ ] Valid 10-digit PNR input
- [ ] Invalid PNR validation
- [ ] Successful PNR lookup
- [ ] Automatic local saving
- [ ] Duplicate prevention
- [ ] Offline access
- [ ] Manual refresh
- [ ] Failed refresh preserves saved data
- [ ] Copy PNR
- [ ] Delete PNR
- [ ] Passenger status display

### Reservation Information

- [ ] CNF explanation
- [ ] RAC explanation
- [ ] WL explanation
- [ ] Quota-specific waiting-list explanations
- [ ] Unique status entries
- [ ] Current status preferred over booking status
- [ ] Fixed-width status column
- [ ] Theme-independent badge colors
- [ ] Theme-aware explanation text
- [ ] Offline functionality

### Themes

- [ ] All eight appearance modes
- [ ] All eight color palettes
- [ ] Simplified background style
- [ ] Gradient background style
- [ ] Screen-specific backgrounds
- [ ] Gradient accent surfaces
- [ ] Gradient Add PNR button shape
- [ ] Readable text contrast
- [ ] AMOLED appearance
- [ ] High-contrast appearances
- [ ] Persistent theme preferences
- [ ] Reset to Default

### Navigation and Legal

- [ ] Home navigation
- [ ] Add PNR navigation
- [ ] Details navigation
- [ ] Settings navigation
- [ ] Policies & Legal navigation
- [ ] Icon-only Back buttons
- [ ] Disabled screen transitions
- [ ] Report a Bug external link
- [ ] Source Code external link
- [ ] License external link
- [ ] Missing project URL handled gracefully
- [ ] Browser unavailable handled gracefully

---

## Current Limitations

PNR EXPERT focuses on checking and saving PNR reservation information.

It does not currently provide:

- Railway ticket booking
- Ticket cancellation
- Online payments
- Live GPS train tracking
- Automatic background reservation refresh
- Official Indian Railways account integration

Live lookup availability and accuracy depend on the configured backend and external data providers.

---

## Disclaimer

PNR EXPERT is an independent application.

It is **not affiliated with, endorsed by, or officially operated by Indian Railways, IRCTC, or the Government of India**.

Railway reservation information may change after the last successful check.

For important travel decisions, verify information through official railway channels.

---

## License

The project includes an MIT License.

See [LICENSE.md](LICENSE.md).

Third-party libraries, information providers, and external services remain subject to their respective licenses and terms.

---

## Application Information

| Property | Value |
|---|---|
| Application name | PNR EXPERT |
| Platform | Android |
| Language | Kotlin |
| UI | Jetpack Compose / Material 3 |
| Architecture | Offline-first |
| Local storage | Room / SQLite |
| Backend | Serverless HTTPS API |
| Application ID | `com.first.pnrenq` |
| Minimum Android SDK | 26 |

**PNR EXPERT — Your journeys, saved.**
