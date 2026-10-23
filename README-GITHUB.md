# Age in Motion v4 — GitHub update

1. Extract this archive. Upload the extracted `app` and `README.md` into your existing AgeInMotionWidget repository, preserving paths. If GitHub’s upload page doesn’t replace existing files, use the `github.dev` editor or Git to copy over the files.
2. Keep the repository’s existing `.github/workflows/build-apk.yml` unchanged.
3. Commit to `main`, then open Actions → Build Android APK and download the successful run artifact.
4. Install the APK on a phone, choose your birthday, and set Widget Theme = Automatic. You can override its style and appearance in the app.
5. **This is source code, not a prebuilt APK, and v4 is not device-tested.** Use the build logs to resolve any compilation errors.

Android 8.0+, API26+. Home screen widget only; OEM lock-screen integration is not guaranteed.
