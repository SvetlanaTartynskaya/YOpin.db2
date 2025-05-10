@echo off
echo ===== YOpin App Builder =====
echo.

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java JDK 11 or higher and set JAVA_HOME
    echo.
    pause
    exit /b 1
)

REM Clean and build the app
echo Cleaning and building the app...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to clean the project
    pause
    exit /b 1
)

call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to build the app
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
echo Debug APK is located in app/build/outputs/apk/debug/
echo.
pause 