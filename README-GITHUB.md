# Build Age in Motion APK on GitHub (no Android Studio)

This repository contains a standalone home-screen widget for Nothing Phone (1) on Nothing OS 3.2, plus an automatic GitHub Actions build. It is an independent Nothing-inspired recreation, not a Nothing Playground widget.

## 1. Create a free GitHub repository

At github.com click `+` (top right) -> **New repository**. Name it **AgeInMotionWidget**, make it **Private** (recommended), leave **Add a README** unchecked, click **Create repository**.

## 2. Upload this project

Extract the ZIP to your computer first. Open the **AgeInMotion_GitHub_Ready** folder; it should contain `.github`, `app`, `build.gradle`, `settings.gradle`, and other files. Select **all contents inside this folder** (Ctrl+A), including the `.github` folder, then drag them into the repository's **uploading an existing file** page. Or use **Add file -> Upload files**. Do **not** upload the ZIP itself or upload an extra parent folder. Scroll to the bottom and **Commit changes** to the **main** branch.

After uploading, the root of your GitHub repository should show `app/`, `.github/`, `build.gradle`, and `settings.gradle` at the top level.

## 3. Let GitHub build the APK

Open the repository **Actions** tab. The **Build Android APK** workflow starts automatically on a commit to `main`. Wait until the run shows a green check mark. If it did not start, select **Build Android APK** from the left sidebar and click **Run workflow -> Run workflow**. If GitHub asks you to enable Actions, enable them for the repository.

## 4. Download the APK

Open the successful workflow run. Scroll to the bottom, **Artifacts** section, and click **AgeInMotion-NothingOS3-APK**. GitHub downloads an artifact ZIP. Extract it to get **app-debug.apk**.

## 5. Install on your Nothing Phone (1)

Copy **app-debug.apk** to your phone (USB, Google Drive, email attachment, etc.). Open the file on the phone. If prompted, allow **Install unknown apps** for the Files app you use, then install it. Turn this permission back off afterward if you prefer. Open the Age in Motion app, select your birthday, then long-press an empty area of your Nothing home screen -> **Widgets** -> **Age in Motion Widget** and place it.

## Verify the GitHub file paths

- `.github/workflows/build-apk.yml` (hidden dot-directory is important)
- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`
- `settings.gradle`

The APK is built in GitHub Actions, not on your phone or computer. The source was not compiled or tested on a physical phone in this environment, so a build error is possible; if you see a red X, open the build run and share the error log or screenshot.

## Expected widget behavior

Android's standard widget update limits mean the home-screen decimal age changes at system-controlled intervals (up to about 30 min), while the native time-today Chronometer ticks every second. Inside the full app, decimal age redraws continuously.
