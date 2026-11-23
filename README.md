
# PNR EXPERT

**PNR EXPERT** is a lightweight Android application for checking and saving Indian Railways Passenger Name Record (PNR) information.

The application provides a modern interface, customizable themes, offline access to saved tickets, and manually initiated live PNR checks.

## Features

### PNR Management

- Check a 10-digit PNR number.
- Automatically save successfully retrieved information.
- View previously saved PNRs offline.
- Refresh reservation status manually.
- Delete saved PNRs.
- Prevent duplicate records.
- Preserve saved information if a refresh fails.

### Journey Details

Depending on available data, the app displays:

- Train name and number
- Journey date
- Departure and arrival stations
- Departure and arrival times
- Journey duration
- Travel class
- Reservation quota
- Chart preparation status
- Ticket fare
- Boarding station
- Journey distance
- Passenger booking status
- Passenger current status
- Coach and berth details
- Last successful check time

Unavailable fields are hidden automatically.

### Appearance

PNR EXPERT supports four color palettes:

- Ocean
- Forest
- Sunset
- Midnight

Appearance modes:

- System
- Light
- Dark
- AMOLED

Theme preferences persist across application restarts.

### User Interface

- Material 3 design
- Compact ticket cards
- Detailed passenger information
- Animated navigation
- Adaptive launcher icon
- Branded startup splash screen
- Responsive layouts

## Technology Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 |
| Navigation | Navigation Compose |
| Architecture | ViewModel and Repository |
| Database | Room / SQLite |
| Networking | Retrofit and OkHttp |
| JSON | Gson |
| Async operations | Coroutines and Flow |
| Backend | Serverless HTTPS API |
| Build system | Gradle Kotlin DSL |

## Architecture

```text
Android Application
        |
        | HTTPS
        v
Serverless Backend
        |
        +-- Input validation
        +-- Request protection
        +-- Usage management
        +-- Data normalization
        |
        v
External Data Services
        |
        v
Standardized PNR Response
        |
        v
Android Application
        |
        v
Room Database
        |
        +-- Home Screen
        +-- Details Screen
        +-- Offline Access
```

The application does not store external service credentials.

Backend implementation details, account identifiers, credentials, and service configuration are maintained separately from the public Android repository.

## Offline-First Design

PNR EXPERT uses Room for local persistence.

- Successful PNR checks are saved automatically.
- Saved PNRs can be viewed without internet access.
- Opening a saved PNR does not trigger an automatic refresh.
- Refreshing occurs only when requested.
- Successful refreshes update locally saved information.
- Failed refreshes preserve the previously saved result.

Offline information reflects the last successful check and may be outdated.

## Project Structure

```text
pnrenq/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/first/pnrenq/
│       │   │   ├── data/
│       │   │   ├── domain/
│       │   │   └── ui/
│       │   └── res/
│       ├── test/
│       └── androidTest/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── SETUP.md
├── LICENSE.md
└── .gitignore
```

## Installation

1. Clone or download the repository.
2. Open the project in Android Studio.
3. Install the required Android SDK.
4. Configure the backend URL locally.
5. Sync Gradle.
6. Build and run the application.

See [SETUP.md](SETUP.md) for detailed instructions.

The backend endpoint is not included in the public repository and must be supplied locally.

## Privacy

- PNR information is stored locally on the device.
- Live checks require communication with a configured backend.
- The backend may use external services to retrieve railway information.
- Service credentials are not included in the Android source.
- The app does not require user registration for its current functionality.

PNR numbers may reveal sensitive travel information. Users should avoid publicly sharing real reservation details.

## Limitations

PNR EXPERT does not currently provide:

- Train ticket booking
- Ticket cancellation
- Payments
- Live GPS train tracking
- Automatic background PNR refresh

Availability and accuracy of live reservation information depend on external data sources.

## Disclaimer

PNR EXPERT is an independent application and is not affiliated with, endorsed by, or officially operated by Indian Railways or IRCTC.

Users should verify important travel information using official railway channels.

## License

This project is licensed under the MIT License.

See [LICENSE.md](LICENSE.md).

Third-party components remain subject to their respective licenses and terms.

## Application Information

**Application name:** PNR EXPERT

**Home screen title:** PNR

**Platform:** Android

**Minimum SDK:** 26

**Application ID:** `com.first.pnrenq`
