# Age in Motion — Nothing OS 3.2 Edition

**A community-inspired Android widget for Nothing Phone users who cannot run the original Age in Motion widget on Nothing OS 4.0+.**

This independent recreation brings the Age in Motion concept to Nothing Phone (1) and other compatible Android devices using standard Android home-screen widgets.

### ❤️ Special Thanks & Credits

A huge salute and heartfelt appreciation to **Mr. Rahul**, the original creator of **Age in Motion** on Nothing Playground.

His brilliant idea and design inspired this compatibility edition. Full credit for the original concept goes to him.

**Original Creation:** [Age in Motion by Rahul — Nothing Playground](https://playground.nothing.tech/detail/app/A0iu5DKA7eGhHhZD)

*This is an unofficial, independently developed, community-inspired recreation. It is not affiliated with or endorsed by Nothing Technology Limited or Rahul.*

## Why This Edition Exists

The original Age in Motion widget requires **Nothing OS 4.0 or later**, leaving Nothing Phone (1) users on Nothing OS 3.2 unable to install it.

This project provides an alternative built with standard Android widget APIs, without requiring an OS upgrade, root access, or a third-party widget launcher.

## Features

- Minimalist black widget with white dot-matrix digits.
- Real-time age calculation based on your date of birth.
- Live decimal-age counter inside the application.
- Home-screen age display with periodic updates.
- Seconds-ticking home-screen chronometer.
- Birthday saved locally on your device.
- No advertisements, tracking, account registration, or internet permission.
- Lightweight design focused on compatibility and battery efficiency.

## Compatibility

- **Confirmed working:** Nothing Phone (1) — Nothing OS 3.2.
- **Minimum Android version:** Android 8.0 (API 26).
- Other compatible Android devices may work but have not been individually verified.

## Download & Installation

Download the APK from the project's GitHub Releases section when a release is available.

To obtain the current GitHub Actions build:

1. Open the repository's **Actions** tab.
2. Select **Build Android APK**.
3. Open the latest successful workflow run.
4. Download the APK artifact.
5. Extract the ZIP to obtain `app-debug.apk`.
6. Transfer the APK to your phone and install it.
7. Open the app and select your birthday.
8. Long-press your home screen, open **Widgets**, and add **Age in Motion**.

GitHub Actions artifact downloads require a GitHub account with repository read access.

## Build from Source

The project can be built using GitHub Actions without installing Android Studio.

Developers can also build it locally using Android Studio, JDK 17, Android SDK 35, and a compatible Gradle installation.

## Important Limitation

The age digits on the home-screen widget update periodically rather than continuously every fraction of a second because of standard Android widget limitations.

The seconds chronometer continues ticking, while the app provides a more frequently updated decimal-age display.

## Privacy

Your birthday is stored locally using Android SharedPreferences. The app does not request internet access or contain analytics, advertisements, or tracking services.

## Project Status

✅ Successfully built using GitHub Actions.

✅ Reported working on Nothing Phone (1), Nothing OS 3.2.

This is an independent compatibility project created with respect and appreciation for Rahul's original work.

**Made for the Nothing community. Inspired by Rahul. ❤️**
