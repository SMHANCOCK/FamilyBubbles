#!/bin/sh
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER" ]; then
  URL="https://services.gradle.org/distributions/gradle-8.9-wrapper.jar"
  echo "Gradle wrapper JAR not present; downloading Gradle 8.9 wrapper..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$WRAPPER"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$WRAPPER" "$URL"
  else
    echo "Install curl/wget or open the project in Android Studio and configure Gradle 8.9." >&2
    exit 1
  fi
fi
exec java -classpath "$WRAPPER" org.gradle.wrapper.GradleWrapperMain "$@"
