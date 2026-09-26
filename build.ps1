# Build script for Android project

# Set JAVA_HOME to Android Studio's bundled JDK
$env:JAVA_HOME = "D:\android studeo\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

Write-Host "Starting build process..." -ForegroundColor Cyan

# Clean previous build artifacts (optional, but good for a fresh build)
# .\gradlew clean

# Build the debug APK
Write-Host "Building Debug APK..." -ForegroundColor Yellow
.\gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build Successful! `nAPK is located in: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Green
} else {
    Write-Host "Build Failed. Please check the errors above." -ForegroundColor Red
}

Pause
