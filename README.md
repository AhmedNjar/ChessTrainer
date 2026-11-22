# Chess Trainer

Offline-first chess training app scaffold using Kotlin Multiplatform and Compose Multiplatform. The shared Compose UI currently recreates the four supplied screens: Home, Opening Trainer, Puzzle Victory, and Settings.

## Current app pieces

- Shared Compose screens styled from the Figma Make project at `C:\Users\Lenovo\Downloads\Chess Training App Screens`.
- A ten-puzzle offline starter set taken from the supplied Lichess CSV.
- SQLDelight schema and platform drivers for Android, desktop, and iOS.
- A CSV row parser and streaming importer. Android and desktop expose a file picker; imports run in batches so the full 1.14 GB source is not loaded into memory.
- Shared UCI controller interface, PGN move-text parser, and trainer state/event/effect types.

The current board interactions are a visual prototype. Full legal move generation, variation-aware PGN trees, connected Stockfish binaries, and finished iOS file picking remain follow-up work.

## Android SDK command-line tools

Install a JDK 17 or newer, the Android SDK Command-Line Tools, and Gradle 9.5 or newer. Set `JAVA_HOME` and `ANDROID_HOME`, then install the SDK packages and accept licenses:

```powershell
sdkmanager --sdk_root="$env:ANDROID_HOME" "platform-tools" "platforms;android-36" "build-tools;36.0.0"
sdkmanager --licenses
```

Use the Android CLI helper from this directory:

```powershell
.\scripts\android-cli.ps1 check
.\scripts\android-cli.ps1 build
.\scripts\android-cli.ps1 install
```

The app module is `androidApp`; Gradle builds an APK under `androidApp/build/outputs/apk/debug/`. The `install` command uses the Gradle install task, which requires a connected device or running emulator.

## Desktop and iOS

Run desktop with `gradle :composeApp:run` on Windows, macOS, or Linux. iOS frameworks are configured for device and simulator targets. iOS compilation and running require macOS with Xcode; open the `iosApp` wrapper from Xcode after creating/adding it to an Xcode project.

## Import the full puzzle database

Open **Settings → Offline library → Import full Lichess CSV** and choose `lichess_db_puzzle.csv`. The import streams rows and commits them to SQLite in batches. The importer validates the CSV header before replacing the starter library.

## Opening trainer and offline repertoire

The opening trainer is a White repertoire trainer with legal-square hints, tap-to-move, drag-and-drop, animated piece travel, and automatic moves for Black. Its starter catalog covers sixteen major Ruy Lopez lines, including Morphy Closed, Berlin, Exchange, Schliemann, Classical, Steinitz, Arkhangelsk, Modern Arkhangelsk, Cozio, Bird, Norwegian, Worrall, Smyslov, Chigorin, Zaitsev, and Marshall. Each White move has an explanatory guide. The shared catalog source is `sharedOpenings/src/main/kotlin/com/knight/chesstrainer/openings/OpeningRepertoire.kt`.

The Ktor server exposes `GET /api/repertoires`; it seeds the catalog in its SQLite database on first start. In the app, **Train → Sync** downloads and merges catalog lines into the on-device SQLDelight database. Starter lines are also seeded locally, so training works before the first sync and when offline.

### Stockfish play

The Android ARM64 build includes the official Stockfish 19 UCI engine. On the Train screen, choose **Play** under **Practice with Stockfish** to start the selected opening against Stockfish as Black. The repertoire move guide stays active while play follows that line; after a different move, the game continues against the engine as free play. Strength is adjustable from 0 to 20 during a match. The engine runs locally, so no server connection is needed for play.

The bundled engine is under `androidApp/src/main/jniLibs/arm64-v8a/`. The matching GPL notice, authors, and source URL are in `androidApp/src/main/assets/stockfish/`. The Android executable is ARM64; the current Desktop and iOS hosts do not package a Stockfish process.

## Source references

- Design: `C:\Users\Lenovo\Downloads\Chess Training App Screens\src\App.tsx` and `src\index.css`
- Full puzzle CSV: `C:\Users\Lenovo\Downloads\lichess_db_puzzle.csv\lichess_db_puzzle.csv`

## Local Ktor authentication backend

The `authServer` module provides email/password sign-up, sign-in, `/api/auth/me`, and token-revoking sign-out. It stores accounts in SQLite and stores only PBKDF2-HMAC-SHA256 password hashes. The server requires a secret from the environment and refuses to start with a missing or short secret.

Start the backend from the project root in PowerShell:

```powershell
$env:CHESS_AUTH_JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
.\gradlew.bat :authServer:run
```

For a phone connected through ADB, forward its loopback port to the computer:

```powershell
F:\android\Sdk\platform-tools\adb.exe reverse tcp:8080 tcp:8080
```

The Android debug app defaults to `http://127.0.0.1:8080`. Override the backend URL with Gradle property `-PchessAuthBaseUrl=http://host:port` or environment variable `CHESS_AUTH_BASE_URL`. The debug manifest allows cleartext HTTP for local development only; production deployment needs HTTPS and a production secret. The local SQLite file is `authServer/data/users.sqlite` by default.
