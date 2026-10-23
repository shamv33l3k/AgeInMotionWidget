# Age in Motion • Nothing OS 3.2 • Live Motion v2

An independent Android home-screen widget for Nothing Phone (1) and other supported Android phones. Inspired by **Rahul's original Age in Motion widget**, available on Nothing Playground for Nothing OS 4.0+.

❤️ **Respect and full credit to Mr. Rahul for the original concept and inspiration.** [See the original by Rahul](https://playground.nothing.tech/detail/app/A0iu5DKA7eGhHhZD).

This is an independent compatibility project, not an official Nothing Playground widget and not affiliated with Nothing Technology Limited or Rahul.

## What's new in v2

- **NEW: Live Motion** switch in the app. Switch it ON to redraw the *actual age digits on the home screen* approximately every **2 seconds**, with **8 decimal places**.
- The dot-matrix numbers animate when the display is on. When the screen turns off, the service pauses image redraws to reduce battery use, and resumes when the screen turns on.
- Shows an ongoing Android foreground-service notification while Live Motion is enabled. Stop it inside the app or from the notification.
- Switch Live Motion OFF to use the original low-power mode: age digits refresh approximately every 30 minutes, and the TODAY seconds chronometer still ticks.
- No ads, trackers, network permissions, account, root, launcher replacement, or auto-start at boot.

## What to expect

* Android widgets do **not** support unrestricted continuously animated custom images. This workaround uses a user-started foreground service and full widget redraws. Android/Nothing OS may still delay or stop redraws during battery saving, and frequent redraws consume extra battery. This is **not exactly the same animation mechanism as Nothing OS 4.0 Playground**.
* The 8-decimal digits are smaller on compact 2×2 widgets. Resize the widget to 3×2 or 4×2 if you want easier-to-read numbers.
* Live Motion is **OFF by default** and **does not restart by itself after reboot**. Reopen the app and press START LIVE MOTION when you want animated digits again.
* Your date of birth remains stored locally in Android SharedPreferences.

## Install using GitHub Actions (no Android Studio)

1. Upload/commit the project files into your GitHub repository, retaining the existing working `.github/workflows/build-apk.yml` workflow.
2. Open **Actions → Build Android APK**, and open the successful build.
3. Under **Artifacts**, download the APK artifact ZIP, unzip it, then copy `app-debug.apk` to your phone.
4. Install the APK, open **Age in Motion**, select your birthday, and add the **Age in Motion** home-screen widget.
5. Press **START LIVE MOTION (2 SEC)** in the app. Approve notification permission if prompted.
6. Return to the home screen and observe the age digits. If updates stop, reopen the app to see if Live Motion is still enabled, and review Nothing OS battery restrictions.

### Very important: updating an existing debug APK

**GitHub Actions' automatically generated debug signing key can change between builds.** If Android says the new APK cannot be installed as an update, you must **uninstall the old Age in Motion app**, install this one, then enter your birthday again. The stored birthday from the previous version may be lost. For public releases or seamless upgrades, configure a persistent private release-signing key in GitHub Actions.

## Technical specifications

- Compatible with Android 8+ (minSdk 26). Developed for Android 15 / Nothing OS 3.2.
- Standard AppWidgetProvider and RemoteViews, with a 5×7 programmatically drawn dot-matrix font.
- Android foreground service type `specialUse`, declared with the required foreground-service permissions for Android 14+.
- Foreground service is started **only when the user presses the switch in the app**, and shows a persistent notification (subject to notification permission).
- Refresh loop is every 2 seconds when the display is interactive; no wake lock, no background location, no network.
- Normal widget updates are every approximately 30 minutes, plus date/time/timezone changes.
- Running persistent foreground services may require additional policy declarations if this app is distributed through Google Play. GitHub APK distribution does not mean the feature is approved for Play Store publication.

## Build notes

This source package was edited and statically reviewed here; **the v2 build and live animation have not yet been tested on your physical phone**. Use your existing GitHub Actions workflow to compile the updated APK and check its logs for any error.

## Original creator

[Age in Motion by Rahul — Nothing Playground](https://playground.nothing.tech/detail/app/A0iu5DKA7eGhHhZD)

*Community-inspired compatibility edition. Not endorsed by Rahul or Nothing Technology Limited.*
