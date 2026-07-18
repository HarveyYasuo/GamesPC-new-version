# Implementation Plan - Fix buildEnvironment and Dependency Issues

The goal is to resolve the issues encountered when running `./gradlew :app:buildEnvironment` and align the project with the specific requirements in `GEMINI.md`.

## User Review Required

> [!IMPORTANT]
> **Environment Variables Conflict**: The build fails because both `ANDROID_PREFS_ROOT` and `ANDROID_USER_HOME` environment variables are set on your system.
> **Action Required**: Please remove the `ANDROID_PREFS_ROOT` environment variable from your system settings. AGP recommends using only `ANDROID_USER_HOME`.
> I will attempt to work around this in my local shell sessions, but for a permanent fix in Android Studio, you should clean up your environment variables.

> [!NOTE]
> **Kotlin Version Downgrade**: I will downgrade Kotlin from `2.3.0` to `2.1.10` to match the version specified in your `GEMINI.md` file.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/gradle/libs.versions.toml)
- Downgrade `kotlin` version to `2.1.10`.
- Update `ksp` version to match Kotlin 2.1.10 (e.g., `2.1.10-1.0.29` or similar stable).
- Update `agp` version if 8.8.1 causes issues with Gradle 8.12.1, but I'll try to keep it for now unless it fails.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/app/build.gradle.kts)
- Migrate `kotlinOptions.jvmTarget` to the modern `compilerOptions` DSL to resolve the build error.

#### [MODIFY] [gradle-wrapper.properties](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/gradle/wrapper/gradle-wrapper.properties)
- Align Gradle version with `GEMINI.md` (downgrade from `8.14.5` to `8.12.1`).

## Verification Plan

### Automated Tests
- Run `./gradlew :app:buildEnvironment` (with `ANDROID_PREFS_ROOT` unset in the session).
- Run `./gradlew assembleDebug` to ensure the project still compiles.

### Manual Verification
- Verify that the sync completes successfully in the IDE.
