# 🏰 Tower Defense — LibGDX Android Game

A full-featured Tower Defense game built with **LibGDX 1.12.1** for Android.

![Build](https://github.com/YOUR_USERNAME/tower-defense/actions/workflows/build.yml/badge.svg)

---

## 🎮 Gameplay

- **25 waves** of increasingly difficult enemies
- **5 tower types** with 3 upgrade levels each
- **5 enemy types**: Basic, Fast, Tank, Flying, Boss
- Real-time strategy: place towers, manage gold, survive!

### Tower Types
| Tower  | Cost | Damage | Range | Special |
|--------|------|--------|-------|---------|
| Arrow  | 50g  | 15     | 80    | Fast attack |
| Cannon | 100g | 45     | 100   | AoE blast |
| Ice    | 80g  | 20     | 90    | Slows enemies |
| Laser  | 150g | 60     | 110   | High single target |
| Bomb   | 120g | 80     | 95    | Massive AoE |

### Enemy Types
| Enemy   | HP   | Speed | Reward |
|---------|------|-------|--------|
| Basic   | 80   | 60    | 15g |
| Fast    | 40   | 120   | 12g |
| Tank    | 300  | 30    | 40g |
| Flying  | 100  | 90    | 25g |
| Boss    | 1000 | 40    | 100g |

### Controls
- **Tap map cell** — place selected tower
- **Tap existing tower** — select for upgrade/sell
- **[1–5]** — select tower type
- **[SPACE]** — start next wave
- **[F]** — toggle 2× speed
- **[ESC]** — deselect

---

## 🚀 Build APK via GitHub Actions

### Option A: Automatic (recommended)

1. Fork / push this repo to GitHub
2. GitHub Actions runs automatically on every push to `main`
3. Go to **Actions → Build Tower Defense APK → latest run → Artifacts**
4. Download `TowerDefense-Debug-APK` and install on your device

### Option B: Tagged release

```bash
git tag v1.0.0
git push origin v1.0.0
```

This creates a GitHub Release with both debug and release APKs attached.

---

## 🔧 Local Build

### Requirements
- JDK 17+
- Android SDK (API 34)
- Android Build Tools 34.0.0

### Steps

```bash
# Clone
git clone https://github.com/YOUR_USERNAME/tower-defense.git
cd tower-defense

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew :android:assembleDebug

# APK location:
# android/build/outputs/apk/debug/android-debug.apk
```

### Install directly to device

```bash
adb install android/build/outputs/apk/debug/android-debug.apk
```

---

## 📦 Signing a Release APK

```bash
# 1. Generate keystore (once)
keytool -genkey -v -keystore tower-defense.keystore \
  -alias towerdefense -keyalg RSA -keysize 2048 -validity 10000

# 2. Build release
./gradlew :android:assembleRelease

# 3. Sign
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore tower-defense.keystore \
  android/build/outputs/apk/release/android-release-unsigned.apk \
  towerdefense

# 4. Align
zipalign -v 4 \
  android/build/outputs/apk/release/android-release-unsigned.apk \
  TowerDefense-release.apk
```

### Add secrets for CI signing

In GitHub repo → Settings → Secrets → Actions:

| Secret | Value |
|--------|-------|
| `SIGNING_KEY` | Base64-encoded `.keystore` file |
| `KEY_ALIAS` | Your key alias |
| `KEY_STORE_PASSWORD` | Keystore password |
| `KEY_PASSWORD` | Key password |

Then uncomment the signing step in `.github/workflows/build.yml`.

---

## 🏗 Project Structure

```
tower-defense/
├── core/                          # Platform-independent game logic
│   └── src/main/java/com/towerdefense/
│       ├── TowerDefenseGame.java  # Main game class
│       ├── screens/
│       │   ├── MenuScreen.java
│       │   └── GameScreen.java
│       ├── entities/
│       │   ├── Tower.java
│       │   ├── Enemy.java
│       │   └── Projectile.java
│       └── managers/
│           ├── AssetLoader.java
│           ├── GameMap.java
│           ├── GameState.java
│           ├── WaveManager.java
│           └── ParticleManager.java
├── android/                       # Android launcher
│   └── src/main/java/com/towerdefense/android/
│       └── AndroidLauncher.java
├── .github/workflows/build.yml    # CI/CD pipeline
└── README.md
```

---

## 📱 Minimum Requirements

- Android 8.0 (API 26) or higher
- ~50MB storage
- Any GPU with OpenGL ES 2.0 support

---

## 📄 License

MIT License — free to use, modify, and distribute.
