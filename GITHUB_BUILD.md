# Build the APK with GitHub Actions

This project includes a GitHub Actions workflow that builds an installable Android debug APK.

## Upload to GitHub

1. Create a new GitHub repository, for example `FamilyBubbles`.
2. Upload the **contents** of this project to the root of the repository.
3. Commit the files to the `main` branch.
4. Open the repository's **Actions** tab.
5. Open **Build FamilyBubbles APK**.
6. When the run has completed successfully, open it and download the artifact named **FamilyBubbles-debug-apk**.
7. Unzip the downloaded artifact and install `app-debug.apk` on the Android phone.

The APK is debug-signed by the Android build tools, so it can be installed for testing without creating a release signing key.
