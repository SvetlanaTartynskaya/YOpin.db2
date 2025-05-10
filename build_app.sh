#!/bin/bash

echo "===== YOpin App Builder ====="
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java JDK 11 or higher and set JAVA_HOME"
    echo
    read -p "Press Enter to continue..."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Clean and build the app
echo "Cleaning and building the app..."
./gradlew clean
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to clean the project"
    read -p "Press Enter to continue..."
    exit 1
fi

./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to build the app"
    read -p "Press Enter to continue..."
    exit 1
fi

echo
echo "Build completed successfully!"
echo "Debug APK is located in app/build/outputs/apk/debug/"
echo
read -p "Press Enter to continue..." 