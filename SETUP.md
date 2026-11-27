
# PNR EXPERT: Setup and Verification Guide

This document explains how to configure, build, test, and maintain the PNR EXPERT Android application.

Private backend source, provider credentials, deployment configuration, signing keys, and real reservation information are intentionally excluded from the public repository.

---

## 1. Requirements

Install:

- Android Studio compatible with Android Gradle Plugin 9.5.2.
- Android SDK Platform 37.
- Android SDK Platform Tools.
- Java 25, installed locally or provisioned by Gradle.
- Git.
- An Android emulator or physical test device.
- Internet access for dependency downloads and live PNR checks.

A configured HTTPS backend is required for live reservation lookups.

The private backend implementation is not part of the public Android source repository.

### Build Configuration

| Property | Value |
|---|---|
| Application ID | `com.first.pnrenq` |
| Application version | 1.0 |
| Version code | 1 |
| Minimum SDK | 26 |
| Target SDK | 37 |
| Compile SDK | 37 |
| Android Gradle Plugin | 9.5.2 |
| Gradle | 9.9.2 |
| Kotlin compiler configuration | 2.2.10 |
| Compose compiler plugin | 2.2.10 |
| KSP | 2.3.6 |
| Gradle daemon JVM | 25 |
| Java source compatibility | 11 |
| Java target compatibility | 11 |
| Room schema version | 2 |

The Gradle daemon uses the criteria defined in:

```text
gradle/gradle-daemon-jvm.properties
```

These criteria take precedence over ordinary environment-based daemon JVM selection.

Java 25 is the Gradle build runtime. The application retains Java 11 source and target compatibility.

---

## 2. Obtain the Repository

Clone the public GitHub repository or download its source archive.

For example:

```cmd
git clone https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
cd YOUR-REPOSITORY
```

Replace the example repository address and directory with the actual project location.

Open the project root in Android Studio, not only its `app` directory.

Do not copy another developer's `local.properties`, Gradle caches, signing credentials, or IDE workspace files.

---

## 3. Verify the Gradle Wrapper

The accepted project configuration uses Gradle 9.9.2.

The Wrapper files must be version-controlled:

```text
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
gradlew
gradlew.bat
```

### Official Checksums

Gradle 9.9.2 binary distribution SHA-256:

```text
971fcd7150ca4ba04852c0b2ca0d2d0ab9468e14b03473eab19528809914c76b
```

Gradle 9.9.2 Wrapper JAR SHA-256:

```text
621008afc4f5d59357fcf06ffa644dd5aade5e091bdc363742710445f73cdaf8
```

Official reference:

https://gradle.org/release-checksums/

### Windows Verification

From Command Prompt in the repository root:

```cmd
certutil -hashfile gradle\wrapper\gradle-wrapper.jar SHA256
```

Compare the resulting checksum with the official Wrapper checksum.

Checksum letter capitalization is irrelevant.

If the checksum differs, do not execute the Wrapper until its integrity has been investigated.

Inspect the distribution configuration:

```cmd
type gradle\wrapper\gradle-wrapper.properties
```

The accepted file uses the official HTTPS Gradle distribution, the pinned distribution checksum, and distribution URL validation.

After verifying integrity:

```cmd
gradlew.bat --version
```

Expected Gradle version:

```text
Gradle 9.9.2
```

### Wrapper Maintenance

If a future approved Gradle upgrade is required, use the official Wrapper task and published checksums.

Do not download executable Wrapper files from unofficial sources.

If the existing Wrapper cannot be trusted, generate replacement files using a separately checksum-verified official Gradle distribution.

---

## 4. Configure Local Android Properties

Create or update the following file in the repository root:

```text
local.properties
```

Android Studio normally creates this file when configuring the Android SDK.

Retain its existing `sdk.dir` property.

Add the application configuration:

```properties
pnr.api.baseUrl=https://example.invalid/
pnr.githubRepoUrl=https://github.com/YOUR-USERNAME/YOUR-REPOSITORY
```

Replace these illustrative values with the actual configuration supplied by the project maintainer.

### Backend URL Requirements

The `pnr.api.baseUrl` value is mandatory for building the application.

It must:

- Use HTTPS.
- Contain a valid hostname.
- Use a valid port when one is specified.
- End with `/`.
- Contain no username or password.
- Contain no query parameters.
- Contain no fragment.

### Optional GitHub URL

`pnr.githubRepoUrl` enables the project's external GitHub links.

Use a public GitHub repository address in the following form:

```text
https://github.com/OWNER/REPOSITORY
```

Do not include access tokens or credentials.

### Generated Configuration

Gradle generates the following application fields:

```kotlin
BuildConfig.PNR_API_BASE_URL
BuildConfig.GITHUB_REPO_URL
```

The actual configured values must not be hard-coded into public source files.

BuildConfig values are recoverable from the compiled application.

Do not use them to store permanent API secrets.

---

## 5. Backend Requirements

A compatible backend must expose the following routes.

### Service Health

```http
GET /health
```

Illustrative response:

```json
{
  "service": "pnrenq-api",
  "status": "ok",
  "version": "1.2.0"
}
```

The actual deployed backend version may differ.

### Reservation Lookup

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

PNR values must be strings containing exactly ten ASCII digits.

The example is synthetic and is not intended for live provider calls.

### Successful Response

```json
{
  "success": true,
  "data": {
    "pnr": "0123456789",
    "trainNumber": "12345",
    "trainName": "Example Express",
    "dateOfJourney": "2026-12-25",
    "sourceStation": "ABC",
    "destinationStation": "XYZ",
    "departureTime": "20:35",
    "arrivalDate": "2026-12-26",
    "arrivalTime": "01:35",
    "duration": "5:00",
    "journeyClass": "3A",
    "quota": "GN",
    "distanceKm": 123.5,
    "bookingFare": 500.5,
    "ticketFare": 450,
    "chartStatus": "Chart Not Prepared",
    "numberOfPassengers": 1,
    "passengers": [
      {
        "serialNumber": 1,
        "bookingStatus": "WL 5",
        "currentStatus": "CNF",
        "bookingStatusCode": "WL",
        "currentStatusCode": "CNF",
        "currentCoach": "B2",
        "currentBerthNo": 35,
        "currentBerthCode": "LB"
      }
    ]
  },
  "checkedAtUtc": "2026-11-27T08:00:00Z"
}
```

This example demonstrates the supported response structure using synthetic data.

Required fields must contain valid values.

Optional fields may be absent or null when information is unavailable.

The timestamp should represent a genuine successful check and must satisfy the application's timestamp validation.

### Error Response

```json
{
  "success": false,
  "error": {
    "code": "PROVIDER_UNAVAILABLE",
    "message": "Unable to retrieve PNR status right now."
  }
}
```

The backend should return appropriate HTTP status codes.

The Android application must not save malformed or unsuccessful responses as valid reservation snapshots.

### Expected Backend Protections

The deployed backend should provide:

- Input and response validation.
- Request-size limits.
- Provider-response limits.
- Provider quota protection.
- Bounded request waiting.
- Appropriate provider fallback.
- Sanitized operational logging.
- HTTPS communication.
- Secure storage of upstream credentials.
- Appropriate abuse prevention and retention policies.

Provider details and operational configuration are maintained privately.

---

## 6. Android Network Configuration

The application's remote implementation is located under:

```text
app/src/main/java/com/first/pnrenq/data/remote/
```

Important components include:

- `PnrNetworkConfig.kt`
- `PnrNetworkClient.kt`
- `RemotePnrChecker.kt`
- `PnrResponseParser.kt`
- `PnrHealthService.kt`

Current timeout configuration:

| Setting | Value |
|---|---:|
| Connection timeout | 10 seconds |
| Read timeout | 15 seconds |
| Write timeout | 15 seconds |
| Total call timeout | 25 seconds |

The response parser accepts a bounded JSON document and validates its required fields.

The transport also limits response bytes before parsing.

Automatic HTTP retries and redirects are disabled.

The backend waiting budget should be coordinated with the Android read and total-call timeouts.

Do not increase timeout values indiscriminately to conceal provider or backend failures.

---

## 7. Build the Application

Open the repository root in Android Studio.

1. Configure `local.properties`.
2. Install the requested SDK components.
3. Confirm Java 25 daemon availability.
4. Select File → Sync Project with Gradle Files.
5. Allow dependency resolution to complete.
6. Select the `app` run configuration.
7. Run the application on an emulator or physical device.

### Windows Command Prompt

Execute commands from the repository root:

```cmd
gradlew.bat :app:assembleDebug --stacktrace
```

The debug APK is normally generated under:

```text
app/build/outputs/apk/debug/
```

### Linux or macOS

Use:

```bash
./gradlew :app:assembleDebug --stacktrace
```

If the launcher is not executable, use:

```bash
bash ./gradlew :app:assembleDebug --stacktrace
```

Stop at the first build failure and investigate it before continuing.

---

## 8. Dependency Verification

Inspect the actual selected application dependencies:

```cmd
gradlew.bat :app:dependencies --configuration debugRuntimeClasspath
gradlew.bat :app:dependencies --configuration releaseRuntimeClasspath
```

Inspect build-plugin dependencies:

```cmd
gradlew.bat buildEnvironment
gradlew.bat :app:buildEnvironment
```

Inspect specific dependency selections:

```cmd
gradlew.bat :app:dependencyInsight --configuration releaseRuntimeClasspath --dependency gson
gradlew.bat :app:dependencyInsight --configuration releaseRuntimeClasspath --dependency okhttp
gradlew.bat :app:dependencyInsight --configuration releaseRuntimeClasspath --dependency okio
gradlew.bat :app:dependencyInsight --configuration releaseRuntimeClasspath --dependency kotlinx-coroutines
```

The version catalog contains requested dependency versions.

Gradle resolution reports establish the actual selected versions.

Review unresolved dependencies, unexpected substitutions, and applicable security advisories.

Do not introduce global dependency overrides solely to remove a vulnerability warning without investigating compatibility.

The accepted Gradle upgrade addresses the identified Gradle advisories, but does not automatically remediate all build-plugin dependencies.

Dependency verification metadata and lockfiles should be reviewed before activation.

A complete current dependency vulnerability assessment remains outstanding.

---

## 9. Automated Regression Tests

The accepted Android regression inventory contains:

| Category | Methods |
|---|---:|
| JVM unit tests | 155 |
| Android instrumentation tests | 109 |
| Total | 264 |

### JVM Unit Tests

```cmd
gradlew.bat :app:testDebugUnitTest --stacktrace
```

Expected accepted baseline:

- 155 methods.
- Zero failures.
- Zero skipped tests.

### Compile Android Instrumentation Tests

```cmd
gradlew.bat :app:assembleDebugAndroidTest --stacktrace
```

### Prepare a Test Device

Ensure that Android SDK Platform Tools are available.

```cmd
adb devices
```

Keep the test device awake and unlocked.

Prefer a dedicated emulator or test device.

Some existing instrumentation tests intentionally publish synthetic clipboard content.

### Android Instrumentation Tests

```cmd
gradlew.bat :app:connectedDebugAndroidTest --stacktrace
```

Expected accepted baseline:

- 109 unique methods.
- Zero failures.
- Zero skipped tests.

Multiple connected devices may produce repeated executions of the same test methods.

Do not confuse execution count with unique test inventory.

### Test Reports

Typical report locations:

```text
app/build/test-results/testDebugUnitTest/
app/build/reports/tests/testDebugUnitTest/
app/build/outputs/androidTest-results/connected/
```

Report locations can vary with Android Gradle Plugin versions.

Retain XML reports for verification, but do not commit generated reports or sensitive diagnostic content.

### Force Fresh Verification

When validating build-tool changes or preparing a release:

```cmd
gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --rerun-tasks --stacktrace
gradlew.bat :app:testDebugUnitTest --rerun-tasks --stacktrace
gradlew.bat :app:connectedDebugAndroidTest --rerun-tasks --stacktrace
```

No test should be removed, weakened, or skipped merely to make a build successful.

---

## 10. Manual Functional Verification

Use synthetic test fixtures or reservations you are authorized to access.

### PNR Input

Verify:

- Exactly ten ASCII digits are accepted.
- Leading zeros are preserved.
- Non-ASCII digits and unrelated characters are rejected or filtered appropriately.
- Invalid input does not initiate a live lookup.
- Repeated taps do not initiate conflicting submissions.

### Saved Reservations

Verify:

- Successful lookups are saved.
- Duplicate entries are not created.
- Saved reservations remain available after restarting the app.
- Saved reservations remain available offline.
- A failed refresh preserves the previous snapshot.
- Deleting a reservation does not permit an older refresh to recreate it.
- Individual saved reservations can be removed.

### Journey and Reservation Information

Verify:

- Upcoming and Past journey grouping.
- Railway time-zone interpretation.
- Departure-boundary transitions.
- Journey date and time formatting.
- Recognized passenger statuses.
- Mixed and unknown reservation statuses.
- Coach and berth allocation.
- Conflicting allocation information.
- Indian fare formatting.
- Distance and duration formatting.

### Network Failure Recovery

Verify:

- No internet connection.
- Backend unavailability.
- Invalid backend response.
- Rate limiting.
- Exhausted provider quota.
- Request timeout.
- Refresh failure while previously saved information exists.

Do not repeatedly consume real provider quotas to reproduce failures that can be tested using synthetic responses.

---

## 11. Appearance and Accessibility Verification

Verify all supported appearance modes:

- System Default
- Light
- Dark
- AMOLED
- Soft Light
- Soft Dark
- High Contrast Light
- High Contrast Dark

Verify all palettes:

- Ocean
- Forest
- Sunset
- Midnight
- Rose
- Lagoon
- Amber
- Slate

Verify:

- Simplified and Gradient backgrounds.
- Both time formats.
- Live preview and persistent preferences.
- Reset to Default.
- Readability of text, status badges, and enabled controls.
- Large font sizes and narrow layouts.
- Long train and station names.
- Complete fare values and passenger allocation text.
- Accessible action descriptions.
- Loading and error announcements.
- Back navigation.
- Policies & Legal navigation.

Resetting appearance settings must not delete saved PNR records.

The application intentionally disables regular Navigation Compose screen transitions.

---

## 12. Privacy and Storage Verification

The application stores reservation records in app-private Room storage.

Verify that:

- The database schema remains compatible.
- Existing version 1 to version 2 migration behavior is preserved.
- Saved data survives normal application restarts.
- Backup exclusions remain configured.
- Copy PNR actions mark clipboard content as sensitive.
- Public diagnostic output does not reveal real PNR values unnecessarily.

Database schema exports under `app/schemas/` must remain version-controlled.

They are not copies of real passenger databases.

Do not publish:

- Local database files.
- Database WAL or journal files.
- Real passenger information.
- Screenshots containing actual PNRs.
- Clipboard diagnostic content.
- Unredacted logcat output.
- Backend credentials.
- Signing credentials.

The application does not provide additional database-level encryption or guaranteed secure deletion.

---

## 13. Android Lint

Run both complete Lint tasks:

```cmd
gradlew.bat :app:lintDebug :app:lintRelease --stacktrace
```

For fresh verification:

```cmd
gradlew.bat :app:lintDebug :app:lintRelease --rerun-tasks --stacktrace
```

Reports are normally located under:

```text
app/build/reports/
```

Typical reports include:

```text
lint-results-debug.html
lint-results-debug.xml
lint-results-release.html
lint-results-release.xml
```

Review actual findings rather than relying solely on task completion.

Release-vital Lint is not a substitute for reviewing the complete debug and release Lint results.

Do not introduce broad suppressions merely to hide unresolved issues.

---

## 14. Release Compilation and Verification

Generate a new release artifact:

```cmd
gradlew.bat :app:assembleRelease --rerun-tasks --stacktrace
```

For an Android App Bundle, when appropriate:

```cmd
gradlew.bat :app:bundleRelease --stacktrace
```

Typical release output locations:

```text
app/build/outputs/apk/release/
app/build/outputs/bundle/release/
app/build/outputs/mapping/release/
```

Verify that the generated files correspond to the current source and accepted build configuration.

Do not publish an older APK or reuse historical R8 mapping files as evidence of a new release.

The existing release optimization configuration must be reviewed against the effective R8 configuration.

### Signing

Use a securely managed signing key appropriate for the distribution channel.

Never commit signing passwords, private keys, keystores, or private signing configuration.

### Release Runtime Verification

Before public distribution, test the actual optimized and appropriately signed application on a test device.

Verify:

- Application launch.
- Navigation.
- Successful live PNR lookup.
- Correct JSON response parsing.
- Room persistence.
- Offline access.
- Refresh and deletion.
- Process restart.
- Appearance preferences.
- Accessibility and contrast.
- Error recovery.

Preserve relevant mapping files securely for diagnosing the corresponding release.

Successful compilation alone does not establish release runtime correctness or complete security clearance.

---

## 15. Private Backend Maintenance

The Cloudflare Worker is maintained separately from this public Android repository.

The latest reviewed implementation is Worker v1.2.0.

Its improvements include:

- Atomic quota reservation with initial-insert validation.
- Sequential provider fallback.
- Shared request waiting budget.
- Bounded request and response reads.
- Strict date and numeric normalization.
- Passenger validation.
- Sanitized operational logging.
- Structured error responses.
- Explicit upstream no-store behavior.

The implementation passed 23 deterministic local tests using synthetic provider responses and a representative SQLite quota schema.

No live provider calls were required for those tests.

The private maintainer should separately verify:

- Worker runtime compatibility settings.
- Existing database bindings and schema.
- Rate-limit configuration.
- Provider credentials.
- Actual subscription reset periods.
- Provider-specific response semantics.
- Quota accounting.
- Deployment health.
- A minimal authorized live lookup.

Do not copy the private Worker implementation or its credentials into the public Android repository.

Provider authentication, cooldown policies, and complete production security remain separate operational considerations.

---

## 16. Common Problems

### Missing Backend Configuration

If Gradle reports that `pnr.api.baseUrl` is missing, configure the root `local.properties` file.

Do not hard-code the actual endpoint into application source files.

### Invalid Backend URL

Confirm that the URL uses HTTPS, has a valid authority, ends with `/`, and contains no credentials, query parameters, or fragments.

### SDK Not Found

Install Android SDK Platform 37 using Android Studio's SDK Manager.

Confirm that the local `sdk.dir` points to the correct SDK installation.

### Gradle Checksum Mismatch

Stop before executing an unverified Wrapper or distribution.

Compare the exact file checksum with Gradle's official reference.

Do not remove checksum verification to bypass the error.

### Java or Daemon Startup Failure

Check:

- The configured daemon JVM criteria.
- Availability of Java 25.
- Android Studio's Gradle settings.
- Access to configured toolchain provisioning services.

Do not change application Java compatibility merely to match the daemon JVM version.

### Dependency Resolution Failure

Check network access to the configured repositories.

Inspect the actual dependency resolution report.

Avoid adding arbitrary repositories or forcing unrelated dependencies.

### Backend Unavailable

Check network access and the configured backend's health endpoint.

Saved reservations remain available offline.

Avoid repeated live requests when the backend reports temporary unavailability or quota exhaustion.

### Unexpected Application Behavior

Retain relevant sanitized diagnostics.

Do not uninstall the app or clear its storage solely to troubleshoot an ordinary build failure, because that can remove saved reservations.

### Incorrect Test Count

Inspect the generated XML reports and count unique class and method identifiers.

Check for multiple connected devices, filters, skipped methods, and stale reports.

---

## 17. Repository Hygiene

Keep these files and directories version-controlled:

- Android production sources.
- JVM and Android regression tests.
- Android manifest and resources.
- Room schema exports.
- Gradle configuration and version catalog.
- Verified Gradle Wrapper files.
- Reviewed dependency verification metadata and lockfiles.
- README, setup guide, and license.

Do not commit:

- `local.properties`
- Private backend directories.
- Worker deployment configuration.
- API credentials and access tokens.
- Environment files.
- Signing keys.
- IDE workspace files.
- Gradle caches.
- Build directories.
- APK and AAB files.
- Generated release mapping files.
- Local databases.
- Diagnostic and audit reports.
- Real passenger or reservation information.

The root `.gitignore` defines these exclusions.

The `.gitattributes` file preserves consistent text line endings and binary assets.

Ignored files that were previously tracked must be reviewed and explicitly removed from Git tracking when appropriate.

Adding an ignore rule does not remove files from existing Git history.

---

## 18. Publishing Changes to GitHub

Use the existing repository and remote rather than creating a new unrelated history.

From Windows Command Prompt:

```cmd
git status
git branch --show-current
git remote -v
```

Confirm the intended branch and remote.

Stage only reviewed source, configuration, tests, resources, and documentation.

Review:

```cmd
git diff --cached --stat
git diff --cached
git ls-files -ci --exclude-standard
```

Resolve unintentionally tracked ignored files before committing.

Confirm that the official Wrapper JAR remains tracked.

Commit the reviewed changes:

```cmd
git commit -m "Improve PNR EXPERT reliability, security and maintainability"
```

Push to the intended remote branch.

For an existing `main` branch on `origin`:

```cmd
git push origin main
```

Do not use force push as a routine recovery procedure.

If remote changes exist, review and reconcile them before publishing.

If credentials were previously committed, revoke or rotate them and investigate the repository history.

Removing a credential in a later commit does not remove it from earlier commits.

---

## 19. Verification Status

The accepted development baseline includes:

- Batches 1 through 8 application improvements.
- Consolidated repository and test inventory review.
- Gradle 9.9.2 security upgrade.
- Official Wrapper checksum verification.
- 155 JVM and 109 Android regression methods.
- Local verification of the 264-method application suite.

Remaining release-readiness work includes:

- Complete current dependency vulnerability assessment.
- Applicable build-time dependency remediation.
- Final dependency integrity and reproducibility decisions.
- Review of current complete Lint reports.
- Effective release optimization review.
- Optimized application runtime verification.
- Private backend deployment verification.

Do not describe the application as fully security-certified or production-verified solely because its automated regression tests pass.

---

## 20. License and Disclaimer

PNR EXPERT is released under the MIT License.

See [LICENSE.md](LICENSE.md).

PNR EXPERT is an independent informational application and is not affiliated with Indian Railways, IRCTC, or the Government of India.

Verify important reservation information using official railway channels.
