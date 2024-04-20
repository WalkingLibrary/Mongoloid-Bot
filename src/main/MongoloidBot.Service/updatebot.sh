#!/bin/bash

# Navigating to the project directory (the directory containing this script)
cd "$(dirname "$0")"

# Ensuring script is run as root
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

# Step 1: Pull the latest changes from Git
echo "Pulling latest changes from Git repository..."
git pull origin master
echo "Update complete."

# Step 2: Build the project using the custom Gradle task
echo "Building the project using custom task..."
./gradlew renameJar
if [ $? -ne 0 ]; then
    echo "Build failed, stopping script."
    exit 1
fi
echo "Build successful."

# Step 3: Stop the currently running service
echo "Stopping the current service..."
sudo systemctl stop mongoloidbot.service
if [ $? -ne 0 ]; then
    echo "Failed to stop the service, stopping script."
    exit 1
fi
echo "Service stopped."

# Step 4: Remove the old jar (now unnecessary as renameJar handles it)
# This step is kept just in case of unexpected failures in the delete operation in Gradle
echo "Ensuring old JAR file is removed..."
mv build/libs/mongoloidbot.jar ./
if [ $? -ne 0 ]; then
    echo "Failed to ensure the old JAR file is removed."
    exit 1
fi
echo "Old JAR file confirmed removed."

# Step 5: Move the new jar to the root of the dir (handled by renameJar, validate only)
echo "Validating new JAR file in place..."
if [ ! -f "mongoloidbot.jar" ]; then
    echo "New JAR file is missing, failed to move."
    exit 1
fi
echo "New JAR file is correctly in place."

# Step 6: Restart the service
echo "Restarting the service..."
sudo systemctl start mongoloidbot.service
if [ $? -ne 0 ]; then
    echo "Failed to start the service."
    exit 1
fi
echo "Service restarted successfully."

echo "Deployment completed successfully."
