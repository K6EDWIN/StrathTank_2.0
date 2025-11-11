# StrathTank 2.0

A Kotlin Multiplatform Mobile (KMM) application built with Jetpack Compose.

## Project Structure

This is a Kotlin Multiplatform project that can target both Android and other platforms:

```
StrathTank_2.0/
├── src/
│   ├── commonMain/kotlin/com/strathtank/app/    # Shared Kotlin code
│   └── androidMain/                             # Android-specific code
│       ├── kotlin/com/strathtank/app/           # Android Kotlin code
│       ├── res/                                 # Android resources
│       └── AndroidManifest.xml                  # Android manifest
├── build.gradle.kts                             # Project build configuration
├── settings.gradle.kts                          # Project settings
└── gradle.properties                            # Gradle properties
```

## Prerequisites

Before running this project, make sure you have:

1. **Java Development Kit (JDK) 8 or higher**
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
   - Verify installation: `java -version`

2. **Android Studio** (recommended)
   - Download from [Android Studio](https://developer.android.com/studio)
   - Install Android SDK (API level 24 or higher)

3. **Gradle** (included with project)
   - The project includes Gradle wrapper, so no separate installation needed

## Setup Instructions

### Option 1: Using Android Studio (Recommended)

1. **Open the project:**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to this directory and select it

2. **Sync the project:**
   - Android Studio will automatically detect the Gradle files
   - Click "Sync Now" when prompted
   - Wait for the sync to complete

3. **Run the app:**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon) or press `Shift + F10`

### Option 2: Using Command Line

1. **Navigate to project directory:**
   ```bash
   cd "C:\Users\sharo\Desktop\3.2\Mobile Dev Project\StrathTank_2.0"
   ```

2. **Build the project:**
   ```bash
   .\gradlew.bat build
   ```

3. **Run on Android device/emulator:**
   ```bash
   .\gradlew.bat installDebug
   ```

## Features

- **Kotlin Multiplatform**: Shared business logic between platforms
- **Jetpack Compose**: Modern declarative UI framework
- **Material Design**: Beautiful, consistent UI components
- **Android Support**: Native Android app with proper manifest and resources

## Development

### Adding Dependencies

Edit `build.gradle.kts` to add new dependencies:

```kotlin
dependencies {
    // Common dependencies go in commonMain
    implementation("your.dependency:version")
    
    // Android-specific dependencies go in androidMain
    implementation("androidx.some:library:version")
}
```

### Project Structure

- **Common Code**: Place shared business logic in `src/commonMain/kotlin/`
- **Android Code**: Place Android-specific code in `src/androidMain/kotlin/`
- **Resources**: Android resources go in `src/androidMain/res/`

## Troubleshooting

### Common Issues

1. **Gradle sync fails:**
   - Check your internet connection
   - Ensure you have JDK 8+ installed
   - Try: `.\gradlew.bat --refresh-dependencies`

2. **Build fails:**
   - Clean the project: `.\gradlew.bat clean`
   - Rebuild: `.\gradlew.bat build`

3. **Android device not detected:**
   - Enable Developer Options and USB Debugging on your device
   - Check that device is properly connected
   - Try: `adb devices` to verify connection

## Next Steps

1. **Customize the app**: Modify the UI in `src/commonMain/kotlin/com/strathtank/app/MainActivity.kt`
2. **Add features**: Create new Kotlin files for different screens/features
3. **Add dependencies**: Update `build.gradle.kts` with required libraries
4. **Test**: Run the app on different devices and screen sizes

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
