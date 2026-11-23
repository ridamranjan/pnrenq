
# PNR EXPERT - Setup Guide

This guide explains how to build and run the Android application.

Backend account information, external service identifiers, credentials, and deployment-specific configuration are intentionally excluded from this public repository.

## 1. Requirements

- Android Studio
- Android SDK Platform 37
- Compatible Gradle JDK
- Android emulator or physical device
- Internet connection for live PNR checks

### Android Configuration

| Property | Value |
|---|---|
| Application ID | `com.first.pnrenq` |
| Minimum SDK | 26 |
| Target SDK | 37 |
| Compile SDK | 37 |
| Android Gradle Plugin | 9.5.2 |
| Kotlin | 2.2.10 |
| Gradle wrapper | 9.6.0 |

Use the Gradle wrapper and project configuration supplied in the repository.

## 2. Open the Project

1. Open Android Studio.
2. Select **File → Open**.
3. Select the project root directory.
4. Allow Gradle synchronization to finish.
5. Install any missing Android SDK components.

Do not open only the `app` folder.

## 3. Configure the Backend Endpoint

PNR EXPERT communicates with a separately deployed HTTPS backend.

The backend address must not be hard-coded in public Android source files.

Open the local file:

```text
local.properties
```

Add:

```properties
pnr.api.baseUrl=https://YOUR-BACKEND-ENDPOINT/
```

Replace the placeholder with the URL supplied by the backend administrator.

Requirements:

- The address must use HTTPS.
- The address must end with `/`.
- Do not include query parameters.
- Do not include credentials in the URL.

The project's Gradle configuration reads this value locally and generates the corresponding Android BuildConfig property.

### Important

`local.properties` is excluded from Git.

Do not publish this file or include it in screenshots, issue reports, or shared archives.

The backend URL is configuration, not an authentication secret. It may be discoverable in a compiled APK.

## 4. Configure Android Networking

The application uses:

```text
data/remote/PnrNetworkConfig.kt
data/remote/RemotePnrChecker.kt
```

Both should read the generated backend URL rather than contain a hard-coded deployment address.

Third-party API credentials must not be included in the Android application.

## 5. Backend Requirements

A compatible backend must provide:

### Health Endpoint

```http
GET /health
```

### PNR Lookup Endpoint

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

The number above is illustrative.

### Successful Response

```json
{
  "success": true,
  "data": {
    "pnr": "1234567890",
    "trainNumber": "12345",
    "trainName": "Example Train",
    "dateOfJourney": "2026-12-23",
    "sourceStation": "ABC",
    "destinationStation": "XYZ",
    "departureTime": "20:35",
    "arrivalTime": "02:33",
    "arrivalDate": "2026-12-25",
    "duration": "29:58",
    "chartStatus": "Chart Not Prepared",
    "passengers": [
      {
        "serialNumber": 1,
        "bookingStatus": "RAC 7",
        "currentStatus": "RAC 3"
      }
    ]
  },
  "checkedAtUtc": "2026-11-23T10:39:16.701Z"
}
```

The example demonstrates the expected response structure. Its values do not represent a real reservation.

Additional optional fields may be provided.

The backend is responsible for returning standardized data, performing request validation, applying usage limits, and handling external service failures.

Backend source code and deployment configuration are maintained privately.

## 6. Build the Application

1. Open the project in Android Studio.
2. Confirm that `local.properties` is configured.
3. Select **File → Sync Project with Gradle Files**.
4. Select **Build → Make Project**.
5. Run the `app` configuration on an emulator or device.

The application should appear as:

```text
PNR EXPERT
```

The Home screen header should display:

```text
PNR
```

## 7. Test PNR Checking

1. Launch the app.
2. Select Add PNR.
3. Enter a valid 10-digit PNR.
4. Select Check PNR status.
5. Open the resulting ticket details.

A successful response is automatically saved to the local database.

## 8. Verify Offline Storage

1. Save a PNR while online.
2. Disable internet access.
3. Restart the app.
4. Open the saved PNR.

Previously saved information should remain visible.

## 9. Verify Manual Refresh

1. Open a saved ticket.
2. Enable internet access.
3. Select Refresh status.
4. Check that available data is updated.

A failed refresh should not delete the previously saved result.

## 10. Verify Appearance

Available color palettes:

- Ocean
- Forest
- Sunset
- Midnight

Available appearance modes:

- System
- Light
- Dark
- AMOLED

Selected preferences should remain saved when the application restarts.

## 11. Test Navigation and Startup

Verify animations when:

- Opening Add PNR
- Opening ticket details
- Opening Themes
- Returning to previous screens

Fully stop and relaunch the app to test the splash-screen transition.

## 12. Common Build Problems

### Missing backend configuration

If Gradle reports that `pnr.api.baseUrl` is missing, add it to the root `local.properties` file.

### Invalid backend URL

Ensure the value uses HTTPS and ends with `/`.

### Missing Android SDK

Install SDK Platform 37 using Android Studio's SDK Manager.

### XML declaration error

Ensure XML declarations appear at the beginning of the file, or omit them for Android resource XML files.

### Network errors

Verify:

- Internet access
- Backend configuration
- Backend health
- Backend availability
- Request limits

### Theme settings not retained

Inspect the local theme preferences implementation.

## 13. Repository Security

Never commit:

- Backend account identifiers
- Private deployment hostnames
- API keys
- Access tokens
- Credential files
- Local environment configuration
- Signing keys
- Real passenger or reservation data
- Backend administrative configuration

Do not add real endpoint examples to README files, source code comments, screenshots, or automated test fixtures.

Before publishing, review all pending changes in Android Studio's Commit window or GitHub Desktop.

The `.gitignore` file protects untracked local configuration files, but does not remove information already committed to Git history.

## 14. Release Notes

The application requires an appropriately configured backend for live PNR checks.

A production release should be tested with:

- Online and offline operation
- Invalid PNR input
- Network failures
- Service unavailability
- Database persistence
- All appearance modes
- Small-screen layouts

Do not commit release signing credentials.
