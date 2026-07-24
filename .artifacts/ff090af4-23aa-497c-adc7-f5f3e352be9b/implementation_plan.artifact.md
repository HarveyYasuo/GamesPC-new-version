# Upgrade Google Play Billing Library

The project is using Google Play Billing Library version `7.1.1`. Google Play now requires apps to use version `8.0.0` or higher (recommending `9.0.0` or later) to ensure a secure experience and access to the latest monetization features.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/gradle/libs.versions.toml)
- Update the `billing` version from `7.1.1` to `9.1.0` (latest stable version).

## Verification Plan

### Automated Tests
- Run Gradle Sync to ensure the new version is applied correctly.
- Build the project using `./gradlew assembleDebug` to verify that the dependency update doesn't cause any build-time issues.

### Manual Verification
- Verify that the project compiles successfully. Since there is currently no code in the project directly calling the Billing Library API (it appears to be an unused dependency), no code adjustments are expected to be necessary.
