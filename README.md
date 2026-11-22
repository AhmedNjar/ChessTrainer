# Chess Trainer

A modern, offline-first Chess Training & Repertoire Application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**.

Chess Trainer allows chess enthusiasts to practice opening repertoires, train against local Stockfish engine AI, solve tactical puzzles offline, and sync repertoires with a local/cloud Ktor backend.

---

## 🌟 Key Features

- ♟️ **Opening Repertoire Trainer**: 16 comprehensive Ruy Lopez opening lines with move-by-move guides, legal destination square highlights, animated piece movements, and automated opponent responses.
- 🤖 **Local Stockfish 19 Integration**: Integrated Stockfish 19 UCI engine binary (Android ARM64) allowing real-time practice as White against Stockfish (Black) with adjustable skill levels (0–20) and seamless transition to free-play.
- 🧩 **Offline Puzzle Arena**: Built-in starter puzzle set and high-performance streaming CSV importer capable of importing full Lichess puzzle databases (`lichess_db_puzzle.csv`) directly into on-device SQLite.
- 🔐 **Ktor Authentication & Sync Backend**: Included `authServer` module supporting PBKDF2-password hashing, JWT-token authentication, token revocation, and repertoire synchronization over REST.
- 📱 **Multiplatform Core**: Shared Compose UI across Android, Desktop (JVM), and iOS with SQLDelight for cross-platform local database storage.

---

## 📂 Project Architecture

```text
ChessTrainer/
├── androidApp/          # Android application wrapper & JNI Stockfish binary
├── composeApp/          # Shared Compose Multiplatform UI, MVI state store & DB logic
│   ├── src/commonMain   # Core UI, MVI store, Chess rules engine, Puzzle parser
│   ├── src/androidMain  # Android platform drivers & Stockfish gateway
│   ├── src/desktopMain  # Desktop (JVM) window launcher & platform file pickers
│   └── src/iosMain      # iOS MainViewController entry point
├── authServer/          # Ktor backend server (JWT auth, SQLite user database, REST API)
└── sharedOpenings/      # Shared opening repertoire definitions & data models
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK**: Version 17 or higher
- **Android SDK**: API 34+ (compileSdk 36, minSdk 24)
- **Gradle**: 8.x / 9.x (wrapped with `./gradlew`)

---

### Building & Running

#### 1. Android Application (`:androidApp`)

Connect an Android device/emulator and run:

```bash
./gradlew :androidApp:installDebug
```

Alternatively, build the debug APK:

```bash
./gradlew :androidApp:assembleDebug
```

#### 2. Desktop Application (`:composeApp`)

Launch the Compose Desktop app on Windows, macOS, or Linux:

```bash
./gradlew :composeApp:run
```

#### 3. Authentication Backend (`:authServer`)

Start the local Ktor authentication & repertoire sync server:

**Linux / macOS:**
```bash
export CHESS_AUTH_JWT_SECRET=$(openssl rand -base64 32)
./gradlew :authServer:run
```

**Windows (PowerShell):**
```powershell
$env:CHESS_AUTH_JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
.\gradlew.bat :authServer:run
```

The server will listen on `http://0.0.0.0:8080` by default.

---

## 📥 Importing Full Puzzle CSV

To import the full Lichess puzzle database:
1. Download `lichess_db_puzzle.csv` from [Lichess Open Database](https://database.lichess.org/puzzles/).
2. In the app, navigate to **Settings → Offline library → Import full Lichess CSV**.
3. Select your downloaded `lichess_db_puzzle.csv`.
4. The streaming importer will parse and populate your local SQLite database without memory overhead issues.

---

## 📄 License & Attributions

- **Stockfish Engine**: Includes Stockfish 19 binary (`GPLv3`). See `androidApp/src/main/assets/stockfish/` for full GPL notices and source references.
- **Puzzles**: Puzzle data provided by Lichess (`CC0 1.0`).
- **Codebase**: Distributed under the MIT License.
