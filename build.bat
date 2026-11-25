@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1" %*
if errorlevel 1 (
  echo Build failed. Check Java 17, internet connection, and the error above.
  pause
  exit /b 1
)
echo Build complete. See build\libs\
pause
