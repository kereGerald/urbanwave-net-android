# UrbanWave Net — Android App

## Architecture

Same philosophy as the Windows desktop app (`urbanWave-net-cl`): a thin native
shell, **not** a repackaged copy of the frontend. Capacitor's WebView loads
`https://urbanwave-billingsystem.onrender.com` directly — the real, live
production app, same backend, same Supabase, same everything. No frontend
build is bundled into the APK. Every push to the main billing system's
`main` branch is instantly reflected here too, with zero new APK needed.

```
Android Phone
  └─ UrbanWave Net.apk (Capacitor shell)
       └─ loads https://urbanwave-billingsystem.onrender.com
             └─ Render (Node backend) → Supabase + MikroTik, unchanged
```

## What's in this project

- `capacitor.config.json` — points the WebView at the live production URL
- `android/` — the generated native Android project
  - `app/src/main/java/com/urbanwave/net/MainActivity.java` — the only
    hand-written native code: back-button behavior (navigate WebView history,
    or minimize the app at the root screen) and external-link handling
    (anything outside the app's own domain opens in the system browser
    instead of getting trapped in-app)
  - `app/src/main/res/` — generated launcher icon (adaptive, all densities)
    and splash screen, both from the official logo
  - `app/src/main/res/values/colors.xml` — brand colors (dark navy background
    matching the Electron shell, green sampled directly from the logo)
- `assets/` — the source images used to generate all the Android icon/splash
  resources (kept for regenerating if the logo ever changes)
- `.github/workflows/build-android.yml` — builds a debug APK on
  `ubuntu-latest` via GitHub Actions

## Application details

- **App name:** UrbanWave Net
- **Application ID:** `com.urbanwave.net`
- **Capacitor version:** 7.6.9
- **Minimum Android version:** 6.0 (API 23) — Capacitor 7's default
- **Target/compile SDK:** Android 15 (API 35)
- **Permissions:** `INTERNET` only — nothing else. No camera, location,
  Bluetooth, or storage permissions, because the actual application doesn't
  use any of those APIs (verified by inspecting the source before adding
  anything).

## Why remote-URL instead of bundling the frontend

|  | Remote URL (this project) | Bundle the frontend build |
|---|---|---|
| Updates | Instant, no new APK | New APK needed for every frontend change |
| Consistency | Same approach as the Windows app | Different approach per platform |
| Play Store review | More scrutiny for "wrapped website" apps | Looks more like a native app |

Chosen for consistency with the already-working Windows app and because this
project changes frequently — bundling would mean rebuilding and
redistributing an APK for every small dashboard tweak.

## Building the debug APK

**Requires:** JDK 17, Android SDK (API 35, build-tools), Gradle (via the
included wrapper — downloads automatically with internet access).

```bash
npm install
npx cap sync android
cd android
./gradlew assembleDebug
```

**Output:** `android/app/build/outputs/apk/debug/app-debug.apk`

This could not be built inside the assistant's sandbox — its network egress
blocks `services.gradle.org` (confirmed with a direct 403 on the Gradle
wrapper download), the same category of restriction that blocked a local
Wine-based Windows build earlier in this project. GitHub Actions'
`ubuntu-latest` runners have full internet access and will build this
normally — see the workflow below.

## Building via GitHub Actions

Push this project to its own repository (separate from both
`Urbanwave-Billing-System` and `urbanWave-net-cl`, matching the existing
separation between the web app and the Windows app). The workflow
(`.github/workflows/build-android.yml`) triggers on push to `main`, pull
requests, and manual dispatch. It:

1. Checks out the repo
2. Sets up Node 20 + JDK 17 + the Android SDK (`android-actions/setup-android`)
3. Runs `npm install` and `npx cap sync android`
4. Runs `./gradlew assembleDebug`
5. Uploads the resulting APK as a workflow artifact named
   `UrbanWave-Net-debug-apk`

No Play Store signing or release build is configured yet — debug only, as
requested. Release signing is a separate, later step (parallel to the
code-signing work on the Windows side).

## What still needs real-device verification

Everything here has been inspected and configured correctly as far as static
analysis allows, but **has not been run on an actual Android device or
emulator** (no such environment was available to test in). Once you have a
built APK, please verify:

- [ ] App installs and opens to the real production login screen
- [ ] Login works
- [ ] Navigation between tabs works
- [ ] M-Pesa/payment interfaces work
- [ ] File upload controls (Portal Designer logo upload, profile picture,
      complaints, vouchers, analytics, payments) actually open a working file
      picker inside the WebView
- [ ] Hardware back button navigates WebView history, then minimizes the app
      at the root screen (doesn't just crash or exit unexpectedly)
- [ ] Any external link opens in the system browser, not trapped in-app
- [ ] Launcher icon and splash screen show the correct official logo
