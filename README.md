# PNR EXPERT

**PNR EXPERT** is a lightweight, offline-first Android application for checking, saving, and managing Indian Railways Passenger Name Record (PNR) information.

Built using **Kotlin, Jetpack Compose, Material 3, and Room**, the application provides a customizable interface, locally saved journey information, and manually initiated PNR status checks.

The application uses a configurable serverless backend to communicate with external PNR information providers.

> PNR EXPERT is an independent application and is not affiliated with Indian Railways or IRCTC.

---

## Features

### PNR Management

- Check Indian Railways 10-digit PNR numbers.
- Automatically save successful PNR checks.
- Access previously saved journey information offline.
- Refresh PNR status manually.
- Delete saved PNR records.
- Prevent duplicate saved PNR entries.
- Preserve existing data when refreshing fails.
- Copy PNR numbers to the clipboard.
- View the last successful status-check timestamp.

The application does not perform automatic background PNR refreshes.

### Journey Information

Depending on information returned by the provider, PNR EXPERT can display:

- PNR number
- Train name and number
- Source and destination stations
- Journey date
- Departure and arrival times
- Journey duration
- Booking and current reservation status
- Passenger-wise reservation information
- Coach and berth allocation
- Chart preparation status
- Travel class
- Reservation quota
- Boarding station
- Reservation destination
- Journey distance
- Booking and ticket fares
- Booking date
- Ticket type
- VIKALP information
- Last successful check time

Unavailable information is omitted when appropriate.

### Passenger Information

Passenger information is presented using compact, color-coded status badges.

Example:

```text
P1                [RAC 7] / [RAC 3]
P2                [WL 12] / [RAC 5]
P3                    [CNF A1 15 LB]
```

- Passenger identifiers have theme-colored backgrounds.
- Booking and current status badges appear together.
- Status details are aligned toward the right.
- Confirmed passengers with allocated berths can display a single current-status badge.
- Coach and berth information is formatted to avoid unnecessary duplication.

---

## Appearance and Personalization

PNR EXPERT includes an extensive Material 3 theme system.

### Appearance Modes

Eight appearance modes are available:

| Mode | Description |
|---|---|
| System Default | Follows device appearance |
| Light | Standard light appearance |
| Dark | Standard dark appearance |
| AMOLED | Black, OLED-friendly surfaces |
| Soft Light | Gentle light surfaces |
| Soft Dark | Muted charcoal surfaces |
| High Contrast Light | Strong text contrast on bright surfaces |
| High Contrast Dark | Strong text contrast on near-black surfaces |

### Color Palettes

Eight color palettes are available:

| Palette | Color family |
|---|---|
| Ocean | Blue |
| Forest | Green |
| Sunset | Orange / Terracotta |
| Midnight | Violet |
| Rose | Pink / Berry |
| Lagoon | Teal |
| Amber | Golden yellow |
| Slate | Steel blue |

Every palette works with every appearance mode.

### Background Styles

Two background styles are supported.

**Simplified**

- Clean, mostly solid-color backgrounds.
- Minimal visual distractions.
- Theme-coordinated cards and surfaces.
- Black backgrounds in Simplified AMOLED mode.

**Gradient**

- Palette-aware gradient backgrounds.
- Different visual patterns for individual screens.
- Gradient-styled application text where supported.
- Contrast-aware foreground colors.
- Controlled gradient intensity in dark and high-contrast modes.

There are **128 possible combinations** of appearance mode, palette, and background style.

### Individual Screen Backgrounds

Each main screen has a distinct background design:

| Screen | Background |
|---|---|
| Home | Signature vertical gradient |
| PNR Details | Journey-focused gradient |
| Add PNR | Subtle diagonal gradient |
| Settings | Ambient corner-glow gradient |

All backgrounds are generated using Jetpack Compose rather than external image assets.

The backgrounds automatically follow the selected appearance, palette, and background style.

### Text and Contrast

The application uses theme-aware foreground colors for:

- Screen headings
- Information cards
- Buttons
- Settings selections
- Text-field labels
- PNR input
- Journey information
- Passenger details

The custom gradient-text component uses subtle variations of the appropriate foreground color to preserve readability.

Certain native Material controls retain solid text colors for clarity.

### Time Format

Users can select:

- **12-hour:** `8:35 PM`
- **24-hour:** `20:35`

The preference applies to supported journey times and last-check timestamps.

### Settings Interface

Appearance and Color Palette use expandable selection cards.

- Only the selected option is visible initially.
- Tapping the card expands its available options.
- Selecting an option applies it immediately.
- The card collapses automatically after selection.
- Opening one selection card closes the other.

Background Style and Time Format remain directly selectable.

### Persistent Preferences

Theme and time-format preferences are saved locally and restored when the application restarts.

The **Reset to Default** option restores:

| Setting | Default |
|---|---|
| Appearance | System Default |
| Palette | Ocean |
| Background Style | Simplified |
| Time Format | 24-hour |

Resetting preferences does not delete saved PNR records.

---

## User Interface

PNR EXPERT uses Jetpack Compose and Material 3.

### Home Screen

The Home screen provides:

- Compact PNR EXPERT header card
- Settings button at the bottom-left
- Add PNR action at the bottom-right
- Upcoming and past journey sections
- Compact journey cards
- Copy and delete actions
- Train and station details
- Current reservation status
- Last successful check time

The Settings button uses a compact 48 dp card.

The header includes status-bar spacing to prevent overlap with Android system controls.

### PNR Details Screen

The details screen includes:

1. Train summary card
2. Refresh and delete icons
3. Copyable PNR number
4. Journey timeline
5. Passenger reservation cards
6. Journey information card
7. Ticket and fare information
8. Last update card

Information is organized into separate, theme-aware components.

### Add PNR Screen

The Add PNR screen provides:

- Numeric PNR input
- 10-digit validation
- Input-length counter
- Validation feedback
- Loading indicator
- Network error messages
- Confirmation of the offline-first workflow

Successful lookups are saved automatically.

### Navigation

The application uses Navigation Compose.

In-app screen transitions are disabled:

- No forward slide animation
- No forward fade animation
- No Back navigation slide animation
- No Back navigation fade animation
- No Navigation Compose predictive-back screen transition, where supported

System-level gesture animations are controlled by Android.

The startup splash animation remains separate from screen navigation.

---

## Technology Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI framework | Jetpack Compose |
| Design system | Material 3 |
| Navigation | Navigation Compose |
| Architecture | ViewModel and Repository |
| Local database | Room / SQLite |
| Networking | Retrofit and OkHttp |
| JSON handling | Gson |
| Asynchronous work | Kotlin Coroutines and Flow |
| Backend | Serverless HTTPS API |
| Build system | Gradle Kotlin DSL |
| Symbol processing | KSP |

---

## Architecture

PNR EXPERT uses an offline-first client with a separately deployed serverless backend.

```text
                    Android App
                         |
                  PNR input / Refresh
                         |
                         v
                 Request Validation
                         |
                         v
                  HTTPS API Request
                         |
                         v
                  Serverless Backend
                         |
             +-----------+-----------+
             |                       |
       Request Controls       Provider Management
             |                       |
             +-----------+-----------+
                         |
                         v
               External PNR Providers
                         |
                         v
               Standardized Response
                         |
                         v
                   Android App
                         |
                         v
                  Room Database
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
      Home Screen   Details Screen  Offline Access
```

### Android Architecture

The application separates responsibilities into:

**UI Layer**

- Jetpack Compose screens
- Navigation
- Theme management
- User interaction

**State Layer**

- ViewModel
- UI state
- Navigation events
- Operation coordination

**Domain Layer**

- PNR models
- Validation
- Business rules

**Data Layer**

- Room entities and repositories
- Remote API communication
- Data mapping
- Local persistence

### Backend Responsibilities

The serverless backend is responsible for:

- Validating incoming requests
- Managing requests to external PNR providers
- Applying usage limits
- Handling external service failures
- Standardizing provider responses
- Protecting third-party API credentials

The Android application should not contain third-party provider API keys.

Backend deployment, secrets, and provider configuration are managed separately from the public Android repository.

---

## Offline-First Behavior

Room provides local storage for successfully retrieved PNR information.

### Adding a PNR

```text
Enter PNR
    |
    v
Validate Input
    |
    v
Request Latest Status
    |
    v
Successful Response?
    |
    +-- Yes --> Save to Room --> Show Details
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
Show Saved Journeys
    |
    v
Open Saved PNR
    |
    v
Display Local Information
```

Opening a saved PNR does not automatically trigger a new API request.

### Manual Refresh

```text
Tap Refresh
    |
    v
Request Latest Information
    |
    +-- Success --> Update Local Record
    |
    +-- Failure --> Preserve Existing Record
```

Saved information may become outdated if it has not been refreshed recently.

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
|   |   |   |   |   +-- local/
|   |   |   |   |   +-- remote/
|   |   |   |   |   +-- mock/
|   |   |   |   |
|   |   |   |   +-- domain/
|   |   |   |   |
|   |   |   |   +-- ui/
|   |   |   |       +-- home/
|   |   |   |       +-- add/
|   |   |   |       +-- details/
|   |   |   |       +-- settings/
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
+-- gradlew
+-- gradlew.bat
+-- README.md
+-- SETUP.md
+-- LICENSE.md
+-- .gitignore
```

Important theme-related components include:

```text
ui/theme/
    Theme.kt
    ThemePreferences.kt
    ThemeText.kt
    AppScreenBackground.kt
```

Screen-specific backgrounds are managed centrally through `AppScreenBackground`.

---

## Installation and Setup

### Requirements

- Android Studio
- Android SDK Platform 37
- Compatible Gradle JDK
- Android emulator or physical Android device
- Internet connection for live PNR checking

### Android Configuration

| Property | Value |
|---|---|
| Application ID | `com.first.pnrenq` |
| Minimum SDK | 26 |
| Target SDK | 37 |
| Compile SDK | 37 |
| Android Gradle Plugin | 9.5.2 |
| Gradle wrapper | 9.6.0 |

Use the versions configured in the repository.

### Clone the Repository

```bash
git clone https://github.com/ridamranjan/pnrenq.git
```

Open the cloned project folder in Android Studio.

### Configure the Backend

The Android application requires a separately configured HTTPS API endpoint for live checks.

Open or create the root-level `local.properties` file.

Add:

```properties
pnr.api.baseUrl=https://YOUR-BACKEND-ENDPOINT/
```

Replace the placeholder with the appropriate backend URL.

The address must:

- Use HTTPS
- End with `/`
- Not contain credentials
- Not contain query parameters

The Gradle configuration reads this property and exposes it through Android BuildConfig.

**Do not commit `local.properties` to Git.**

The backend URL is not an authentication secret and can be discovered from a compiled APK.

### Build and Run

1. Open the project in Android Studio.
2. Configure `local.properties`.
3. Sync Gradle.
4. Select **Build → Make Project**.
5. Choose an emulator or connected device.
6. Run the `app` configuration.

For additional setup details, see [SETUP.md](SETUP.md).

---

## Backend API

The Android application expects a compatible backend.

### Health Check

```http
GET /health
```

### PNR Lookup

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

This is an illustrative PNR number.

The backend must return a response compatible with the application's remote data models.

Provider-specific API keys, usage-management logic, and deployment configuration are not included in this README.

---

## Privacy and Security

PNR EXPERT uses local storage for saved journey information.

- Saved PNR data is stored on the device.
- Live checks require network communication.
- The serverless backend communicates with external data providers.
- Third-party API keys must remain outside the Android APK.
- No user account is required for the current app workflow.
- Saved records can be deleted from the application.

PNR numbers and journey details may contain personal travel information.

Do not publish real PNR numbers, passenger information, credentials, or private service configuration in GitHub issues, screenshots, or logs.

Actual processing and retention by backend services depend on their deployment configuration and provider policies.

---

## Testing Checklist

Before releasing a build, verify the following.

### PNR Functions

- [ ] Valid 10-digit PNR input
- [ ] Invalid PNR validation
- [ ] Successful PNR lookup
- [ ] Automatic local saving
- [ ] Duplicate prevention
- [ ] Offline access
- [ ] Manual refresh
- [ ] Failed refresh preserves saved information
- [ ] Copy PNR
- [ ] Delete PNR
- [ ] Passenger status formatting

### Appearance

- [ ] All eight appearance modes
- [ ] All eight color palettes
- [ ] Simplified backgrounds
- [ ] Gradient backgrounds
- [ ] Four different screen backgrounds
- [ ] Dark text on light surfaces
- [ ] Light text on dark surfaces
- [ ] AMOLED contrast
- [ ] High-contrast modes
- [ ] Expandable Appearance selection
- [ ] Expandable Color Palette selection
- [ ] Theme preference persistence
- [ ] Reset to Default

### Navigation and Layout

- [ ] Instant forward navigation
- [ ] Instant Back navigation
- [ ] Predictive-back behavior
- [ ] No status-bar overlap
- [ ] Settings button at bottom-left
- [ ] Add PNR button at bottom-right
- [ ] PNR card scrolling
- [ ] Small-screen compatibility
- [ ] 12-hour time format
- [ ] 24-hour time format

---

## Current Limitations

PNR EXPERT currently focuses on checking and saving PNR reservation information.

It does not provide:

- Railway ticket booking
- Ticket cancellation
- Online payments
- Live GPS train tracking
- Automatic background reservation refresh
- Official Indian Railways account integration

Live PNR availability depends on the configured backend and external information providers.

---

## Disclaimer

PNR EXPERT is an independent application.

It is **not affiliated with, endorsed by, or officially operated by Indian Railways, IRCTC, or the Government of India**.

Railway reservation information may change. For important travel decisions, verify information through official railway channels.

---

## License

This project is released under the **MIT License**.

See [LICENSE.md](LICENSE.md).

Third-party libraries and services remain subject to their respective licenses and terms.

---

## Project Information

| Property | Value |
|---|---|
| Application | PNR EXPERT |
| Repository | [ridamranjan/pnrenq](https://github.com/ridamranjan/pnrenq) |
| Platform | Android |
| Language | Kotlin |
| UI | Jetpack Compose / Material 3 |
| Architecture | Offline-first |
| Local storage | Room |
| Backend | Serverless HTTPS API |
| Application ID | `com.first.pnrenq` |
| Minimum Android API | 26 |

**PNR EXPERT — Your journeys, saved.**
