# Build and Run Instructions for StrathTank 2.0

## Prerequisites
- Android Studio installed
- Java JDK 17 or higher
- Android SDK installed (minSdk 24, targetSdk 35)

## Method 1: Android Studio (Recommended)

1. **Open Project**
   - Open Android Studio
   - File → Open → Select `StrathTank_2.0` folder
   - Wait for Gradle sync to complete

2. **Set Up Device/Emulator**
   - **Physical Device:**
     - Enable USB Debugging: Settings → About Phone → Tap "Build Number" 7 times
     - Go back → Developer Options → Enable "USB Debugging"
     - Connect phone via USB
   
   - **Emulator:**
     - Tools → Device Manager → Create Device
     - Select device (e.g., Pixel 5)
     - Download and select system image (API 24 or higher)
     - Click Finish and Start

3. **Run the App**
   - Click the green ▶️ Run button (or press `Shift + F10`)
   - Select your device/emulator
   - Wait for build to complete
   - App will install and launch automatically

## Method 2: Command Line (PowerShell)

### Build Debug APK
```powershell
cd "C:\Users\sharo\Desktop\3.2\Mobile Dev Project\StrathTank_2.0"
.\gradlew.bat assembleDebug
```

The APK will be generated at:
`app\build\outputs\apk\debug\app-debug.apk`

### Install on Connected Device
```powershell
.\gradlew.bat installDebug
```

### Run Directly
```powershell
.\gradlew.bat installDebug
adb shell am start -n com.example.strathtankalumni/.MainActivity
```

### Clean Build
```powershell
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

## Troubleshooting

### Gradle Sync Fails
- Check internet connection
- File → Invalidate Caches → Invalidate and Restart
- Sync Project with Gradle Files

### Build Errors
- Make sure Android SDK is installed
- Check Java version: `java -version` (should be 17+)
- Clean and rebuild: Build → Clean Project, then Build → Rebuild Project

### Device Not Detected
- Check USB connection
- Enable USB debugging
- Run: `adb devices` to see connected devices
- Try different USB cable/port

### Emulator Issues
- Close other emulators
- Cold Boot: Device Manager → Actions → Cold Boot Now
- Increase emulator RAM in AVD settings

## Testing the New Page

To test the `AlumniAddProjectsPage`:

1. Run the app
2. Login/Register as an alumni user
3. Navigate to the **Projects** tab (bottom navigation)
4. Tap the **+ (Plus)** floating action button
5. You should see the new "Add New Project" screen with:
   - Image backdrop section
   - Title overlay
   - Description field
   - Links section
   - Categories selection

## Build Variants

- **Debug**: `.\gradlew.bat assembleDebug` (for development)
- **Release**: `.\gradlew.bat assembleRelease` (for production)

Note: Release builds require signing configuration in `app/build.gradle.kts`

