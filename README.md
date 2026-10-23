# Age in Motion • Nothing OS 3.2 compatibility edition

A standalone **Android home-screen widget** inspired by the "Age in Motion" concept. This is an **independent recreation, not an official Nothing Playground widget** and is not associated with Nothing Technology Limited or Rahul.

## Why this edition exists

The official community widget requires Nothing OS 4.0+. Nothing Phone (1) on Nothing OS 3.2 uses Android 15 and can install ordinary Android app widgets, so this project uses standard Android `AppWidgetProvider` APIs instead of Nothing Playground.

## What is included

- Black rounded 2×2 home-screen widget, white handcrafted **5×7 dot-matrix** age digits.
- Birthday picker; date is stored **locally only** in Android SharedPreferences.
- Age computed from calendar anniversaries, including leap years, local midnight, and local time zone.
- Live age decimal to **eight places inside the open app**, redrawing about every 300 ms.
- Home-screen widget age decimal to **four places**, redrawn at system-controlled intervals **around every 30 minutes**; tapping the widget or Refresh forces a redraw.
- A seconds-ticking **"TODAY HH:MM:SS" chronometer** on the home-screen widget, using Android's native Chronometer—no constantly running background service, internet, special permission, root, or additional widget launcher is needed.
- Supports Android 8.0 and later (API 26+), targeting Android 15; intended for Nothing Phone (1), Nothing OS 3.2.

**Important limitation:** Android limits native widget refreshes (`updatePeriodMillis` is at least 30 minutes). Therefore, the decimal AGE number **does not change every split second on the HOME SCREEN**, unlike some special OS 4.0 Playground widgets. The seconds line *does* tick, and the full-screen app shows a continuously updating decimal age. This behavior is deliberate for compatibility and battery life.

## Build an APK on your Windows PC

1. Install **Android Studio**: https://developer.android.com/studio
2. Extract this ZIP to a normal folder, e.g. `C:\Users\YourName\Documents\AgeInMotionCompat`.
3. In Android Studio select **File → Open** and choose the extracted project folder (the folder containing `settings.gradle`).
4. Make sure Android SDK Platform 35 is installed from **Tools → SDK Manager**, and use a JDK 17 or newer compatible with Android Gradle Plugin 8.7.3. You may need a Gradle 8.9 distribution: this archive deliberately does not contain the generated Gradle wrapper JAR because it cannot be generated in this environment. Android Studio may help you configure Gradle; if prompted, select/use **Gradle 8.9**.
5. Let Android Studio sync and resolve the Android Gradle Plugin, then click **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Find the generated file: `app/build/outputs/apk/debug/app-debug.apk`.
7. Copy the APK to your Nothing Phone (1), open it, and allow installation from your file-manager app if prompted. Only install APKs you built yourself from sources you trust.
8. Open **Age in Motion** on the phone, tap **Choose My Birthday**, select your real birthday.
9. Long-press your home screen → **Widgets** → **Age in Motion Widget**, then drag onto your home screen. Resize to 2×2 or wider.

### Troubleshooting

- If `No Gradle wrapper` is reported, select/configure an installed Gradle 8.9 installation in Android Studio's Gradle settings, or run `gradle wrapper --gradle-version 8.9` using an installed Gradle command inside the project root (this will generate the standard wrapper scripts, JAR, and properties).
- If Android SDK Platform 35 is missing, install it via the SDK Manager.
- If the widget shows `00.0000`, open the app and select a birthday.
- If digits look small, resize the widget to 3×2 or 4×2.
- If the age doesn't immediately change on the home screen, open the app and press **Refresh Home-Screen Widget**. Android controls automatic update timing.

## No network / tracking

No `INTERNET` permission, analytics, ads, account login, network requests, or background services are included. Birthdate stays on the device unless the user copies/export it themselves.

## Build status

**Source project provided; APK not compiled or tested on a physical device here.** This environment does not include the Android SDK or Gradle, so test on your device after building in Android Studio. Code is delivered as a complete Android Gradle source project, not a prebuilt app.
