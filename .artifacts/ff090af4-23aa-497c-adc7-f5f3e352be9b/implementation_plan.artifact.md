# Pre-Publication Check and Final Preparation

Before generating the final APK or App Bundle for publication, we need to ensure that the app is fully optimized, secure, and compatible with the new AGP version and R8 Full Mode.

## User Review Required

> [!IMPORTANT]
> **Keystore Access**: I noticed your `build.gradle.kts` points to a keystore at `C:/users/harvey/keyOG`. If this file is available, I can attempt a trial release build. If not, you will need to perform the final build manually.
> **AdMob/Unity Dashboard**: Ensure that the Production Ad IDs in `AdManager.kt` and `BannerAdView.kt` match exactly what is in your dashboards.

## Proposed Changes

### ProGuard/R8 Configuration

#### [MODIFY] [proguard-rules.pro](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/app/proguard-rules.pro)
- Add missing rules for **Unity Ads** and **Hilt** to prevent runtime crashes in release mode due to aggressive R8 Full Mode optimization.
- Ensure attributes like `Signature` and `LineNumberTable` are kept for better crash reporting.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleRelease` (if keystore is available) to verify that the release build completes without errors.
- Perform a final Lint check if necessary.

### Manual Verification
- **Testing the Release APK**: Once built, you should test the Release APK on a real device to ensure that:
  - Dependency Injection (Hilt) works (app starts correctly).
  - Ads (AdMob/Unity) load and display (even if using test IDs for the final check).
  - Firebase functionality works as expected.
