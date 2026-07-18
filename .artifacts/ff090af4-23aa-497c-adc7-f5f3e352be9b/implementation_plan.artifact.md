# Update AGP to Fix R8/Kotlin Metadata Warning

The project is showing a warning because R8 (bundled with AGP 8.8.2) does not fully support Kotlin 2.3.0 metadata. Upgrading to AGP 8.13.2 will provide a newer R8 version that supports Kotlin 2.3.0.

## User Review Required

> [!IMPORTANT]
> **Git Push**: I can perform local `git commit` operations as per your instructions. However, I do not have access to your GitHub credentials to perform a `git push`. You will need to manually push the changes to GitHub.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Harvey/StudioProjects/GamesPC-new-version/gradle/libs.versions.toml)
- Update `agp` version from `8.8.2` to `8.13.2`.

## Verification Plan

### Automated Tests
- Run Gradle Sync to ensure the new AGP version is applied correctly.
- Build the project using `gradlew assembleDebug` to verify the warning is gone and the build is successful.

### Manual Verification
- Verify in Logcat/Build Output that the "An error occurred when parsing kotlin metadata" warning no longer appears.
