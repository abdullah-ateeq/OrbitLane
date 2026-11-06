# OrbitLane 🪐

[![Android Studio](https://img.shields.io/badge/Android%20Studio-2026.1+-brightgreen.svg)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Min API](https://img.shields.io/badge/API-24%2B%20(Android%207.0)-orange.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**OrbitLane** is a fast-reflex arcade game built natively for Android using Jetpack Compose and Compose Canvas. Navigate a glowing player orb continuous travelling around concentric orbital paths, dodge approaching hazards, stack streak multipliers, and survive as long as possible.

---

## 🌟 Key Features

* ⭕ **Concentric 3-Lane System**: Smoothly transition between Inward, Middle, and Outward orbital paths with polished interpolation physics.
* ⚡ **Fair Spawning Engine**: Guaranteed fair obstacle generation with reaction time padding—never leaves all three lanes blocked simultaneously.
* 🔥 **Streak Multipliers & Lap Bonuses**: Dodge consecutive hazards to build up to a **4x multiplier**. Complete full 360° revolutions for lap bonus points.
* 🎮 **Intuitive Controls**:
  * **Tap**: Top half of screen shifts **Inward**, bottom half shifts **Outward**.
  * **Swipe**: Vertical swipe gestures for fast lane switching.
* 🔊 **Audio & Haptics**: Integrated low-latency `SoundPool` synth audio and tactile haptic feedback for lane switches, dodges, lap completions, and collisions.
* 🎨 **Sleek Cyberpunk Theme**: Dark neon visual palette rendered hardware-accelerated using Compose Canvas.
* 📊 **Persistent High Scores & Customization**:
  * Track local high scores and best dodge streaks per difficulty.
  * Adjust difficulty (**Easy**, **Medium**, **Hard**).
  * Audio & Haptics toggles.

---

## 🛠 Tech Stack & Architecture

Built following modern Android architecture best practices:

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose), Material 3, Navigation Compose
* **Graphics**: Hardware-accelerated Jetpack Compose `Canvas`
* **Architecture**: Clean MVVM with Unidirectional Data Flow (`StateFlow` & `AndroidViewModel`)
* **Audio & Hardware**: `SoundPool`, `Vibrator` / `VibratorManager`
* **Storage**: Android `SharedPreferences` via a clean repository layer
* **Testing**: Comprehensive JUnit 4 test suite for 60FPS Game Engine tick logic, collision math, and spawn fairness (`GameEngineTest`, `OrbitMathTest`, `ObstacleFairnessTest`)

---

## 📐 Game Engine & Physics Overview

```
                 Outer Lane (Index 2)
              Middle Lane (Index 1)
           Inner Lane (Index 0)
                 \    |    /
                  \   |   /
               ●---[ PLAYER ]----► (Counter-Clockwise Orbit)
                     /|\
                   [HAZARDS] ◄--- (Clockwise Spawn)
```

The core loop updates at **~60 FPS**:
1. **Polar Coordinate Rendering**: Player and hazard positions are calculated using polar math $(r, \theta)$ converted dynamically to cartesian coordinates $(x, y)$ on screen bounds.
2. **Lane Interpolation**: Smooth visual transitions when changing lanes.
3. **Collision Detection**: Angular threshold and lane matching with screen shake visual effects on impact.

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio** Ladybug (2024.2.1) or newer
* **JDK 11** or higher
* **Android SDK** API Level 37 (Minimum SDK 24)

### Building & Running

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/OrbitLane.git
   cd OrbitLane
   ```

2. Open the project in **Android Studio**.

3. Build and run the app on an Android Emulator or connected device:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Running Unit Tests

Run unit tests for game engine logic, collision math, and fairness constraints:
```bash
./gradlew test
```

---

## 📄 License

```text
Copyright 2026 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
