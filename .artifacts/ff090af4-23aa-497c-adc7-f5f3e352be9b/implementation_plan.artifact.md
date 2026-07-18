# Implement Unity Ads Mediation

The goal is to properly configure Unity Ads mediation within the existing AdMob setup. Since the dependencies are already present in `build.gradle.kts`, we will focus on the programmatic configuration for privacy compliance (GDPR, CCPA) and ensuring the mediation adapter is correctly utilized.

## User Review Required

> [!IMPORTANT]
> **AdMob Dashboard Configuration**: Programmatic changes alone are not enough. You must configure Unity Ads as a mediation source in your AdMob dashboard (Game ID and Placement ID are required there).

## Proposed Changes

### Ad Logic and Privacy Compliance

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/app/src/main/java/com/harvey/gamespc/MainActivity.kt)
- Update `setUnityAdsConsent()` to include CCPA (`privacy.consent`) and COPPA (`user.nonbehavioral`) metadata.
- Ensure `MobileAds.initialize` is called after the consent state is determined.

### Build Configuration (Verification)

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/app/build.gradle.kts)
- Verify Unity Ads and Mediation Adapter versions are compatible with the current AdMob SDK version.

## Verification Plan

### Automated Tests
- Run the app and check Logcat for Unity Ads initialization logs (the adapter usually logs its status).
- Use AdMob's "Ad Inspector" to verify that Unity Ads is listed as a mediation partner and can serve ads.

### Manual Verification
- Verify that the consent flow correctly propagates to Unity Ads metadata.
