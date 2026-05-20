# iOS App - Kotlin Multiplatform

This is the iOS application for the banking app, built with SwiftUI and Kotlin Multiplatform.

## Project Structure

The `iosApp` is **not a Gradle module** - it's an Xcode project that consumes the shared framework built by Gradle. This is the standard architecture for Kotlin Multiplatform iOS apps.

```
banking-app/
├── shared/          # Kotlin Multiplatform shared module (built with Gradle)
├── androidApp/      # Android app (Gradle module)
└── iosApp/          # iOS app (Xcode project, NOT a Gradle module)
```

## Building the iOS App

### Prerequisites
- macOS with Xcode installed
- Java JDK 17+
- Gradle

### Build Steps

1. **Build the shared framework with Gradle:**
   ```bash
   ./gradlew :shared:assemble
   ```

2. **Create the XCFramework:**
   ```bash
   ./gradlew :shared:createXCFramework
   ```

3. **Open the Xcode project:**
   ```bash
   open iosApp/iosApp.xcodeproj
   ```

4. **Build and run in Xcode:**
   - Select your target device/simulator
   - Press Cmd+R to build and run

## How It Works

1. **Gradle** builds the `shared` Kotlin Multiplatform module
2. The shared module produces an **XCFramework** containing the compiled Kotlin code
3. **Xcode** links the iOS app against this XCFramework
4. The iOS app (Swift) can call Kotlin code through the shared framework

## Important Notes

- The iOS app must be built on **macOS** with Xcode
- The shared framework must be built **before** opening the Xcode project
- The Xcode project references the XCFramework at: `../../shared/build/XCFrameworks/debug/shared.xcframework`

## Troubleshooting

### "shared.framework not found"
Run `./gradlew :shared:createXCFramework` to generate the XCFramework.

### Build errors in Xcode
1. Clean the Xcode build: Product → Clean Build Folder (Cmd+Shift+K)
2. Rebuild the shared framework: `./gradlew :shared:assemble`
3. Reopen Xcode and rebuild

### Simulator vs Device
The XCFramework includes builds for:
- iOS Simulator (x64 and ARM64/M1)
- iOS Device (ARM64)