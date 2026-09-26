Write-Host "Starting Logcat (Press Ctrl+C to stop)..." -ForegroundColor Cyan
Write-Host "Please use the app and reproduce the crash. Logs will appear below:`n" -ForegroundColor Yellow

$adb = "D:\android studeo @@\platform-tools\adb.exe"

# Clear old logs first
& $adb logcat -c

# Show live logs and filter for OmniDown app, crashes, python, and ffmpeg
& $adb logcat | Select-String -Pattern "com.example.omnidown|AndroidRuntime|python|ffmpeg|FATAL|Exception" -IgnoreCase
