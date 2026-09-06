@echo off
setlocal
set APP_HOME=%~dp0
set WRAPPER=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if not exist "%WRAPPER%" (
  echo Gradle wrapper JAR not present; downloading Gradle 8.9 wrapper...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-8.9-wrapper.jar' -OutFile '%WRAPPER%'"
  if errorlevel 1 (
    echo Unable to download the Gradle wrapper. Open the project in Android Studio and configure Gradle 8.9.
    exit /b 1
  )
)
java -classpath "%WRAPPER%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
