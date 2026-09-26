Write-Host "Installing App to Phone..." -ForegroundColor Cyan

$adb = "D:\android studeo @@\platform-tools\adb.exe"
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
$packageName = "com.example.omnidown"

if (!(Test-Path $apkPath)) {
    Write-Host "APK file not found! Please run .\build.ps1 first." -ForegroundColor Red
    Pause
    exit
}

Write-Host "`n[1/3] Checking for connected devices..." -ForegroundColor Yellow
& $adb devices

Write-Host "`n[2/3] Uninstalling old app (if it exists)..." -ForegroundColor Yellow
& $adb uninstall $packageName

Write-Host "`n[3/3] Installing new app..." -ForegroundColor Yellow
& $adb install -r $apkPath

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nApp installed successfully! Launching it now..." -ForegroundColor Green
    & $adb shell am start -n "$packageName/.MainActivity"
} else {
    Write-Host "`nInstallation failed. Please check if your phone is connected properly and unlocked." -ForegroundColor Red
}

Pause
