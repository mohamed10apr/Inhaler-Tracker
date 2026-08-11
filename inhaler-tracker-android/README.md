# Inhaler Tracker — Android app

Track every inhaler you use: brand, medication and strength, canister size, puffs per dose and doses per day. Log each dose with its date and time in one tap, and the app calculates total administered, remaining puffs, and an estimate of how many days of supply are left. It warns you when the canister runs low or the inhaler is about to expire, sends daily reminder notifications (real Android alarms that fire even when the app is closed), keeps a full history, and can export everything as CSV or a backup file. All data stays on the phone.

## Get the APK — choose one option

### Option A — GitHub builds it for you (no tools to install)
1. Create a free account at github.com, then click **New repository** (any name, e.g. `inhaler-tracker`; Private is fine).
2. On the new repository page click **uploading an existing file** and drag in **all files and folders from inside this folder** — including the hidden `.github` folder (turn on "show hidden files" on your computer so it gets picked up). Press **Commit changes**.
   - If `.github` didn't make it: in the repository click **Add file → Create new file**, type `.github/workflows/build-apk.yml` as the filename, paste the contents of that file from this folder, and commit.
3. Open the repository's **Actions** tab. A run called **Build APK** starts automatically — wait 3–5 minutes for the green check.
4. Click the run, scroll down to **Artifacts**, download `inhaler-tracker-apk`, and unzip it to get `app-debug.apk`.
5. Copy the APK to your phone and tap it. When Android asks, allow your file manager or browser to **install unknown apps**.

### Option B — Android Studio
1. Install Android Studio, choose **Open**, and select this folder.
2. If it offers to create or upgrade the Gradle wrapper, or asks to sync — accept.
3. **Build → Build App Bundle(s) / APK(s) → Build APK(s)**. The APK appears under `app/build/outputs/apk/debug/`.

### Option C — command line
With JDK 17, the Android SDK, and Gradle 8.7+ installed, run `gradle assembleDebug` in this folder.

## First run on the phone
- **Allow notifications** when the app asks (Android 13+), otherwise reminders cannot appear.
- On Android 12+ your phone may list an **Alarms & reminders** permission (Settings → Apps → Inhaler Tracker). Allowing it makes reminders exact to the minute; without it they still arrive, within about a 10-minute window.
- Aggressive battery savers can silence background alarms. For dependable reminders, set the app's battery usage to **Unrestricted** (wording varies by manufacturer).

## How it's put together
The entire interface is one file, `app/src/main/assets/index.html` — it even works on its own in any browser. The small Java layer only adds what a web page can't do: exact system alarms that survive reboots, and the Android share sheet for exports. There are **zero external dependencies**, which keeps the build simple and reliable.

## Good to know
- Uninstalling the app deletes its data — use **Back up data** inside the app first, and **Restore** on the new install.
- The workflow produces a debug-signed APK, which is exactly right for installing on your own phone. Publishing to the Play Store would additionally need your own signing key.
- This app is a personal tracking tool, **not medical advice** — always take the dose your doctor or pharmacist prescribed.
