# MotionX

**See the motion you can't see.**

MotionX is a 90-minute Android hackathon prototype that combines camera-based visual motion tracking with the phone's accelerometer readings. Both the repository and app are named **MotionX**.

Everything runs locally on the phone. Prioritize a working prototype, real sensor data, a simple polished UI, and a reliable demo. No backend, networking, Firebase, login, database, or complex architecture.

## Current handoff — 2026-10-07

- **Implemented:** single-module Android scaffold, Kotlin/Compose/Material 3 configuration, Gradle wrapper and version catalog, launcher resources, dark one-screen UI shell, shared data models, and lifecycle-aware collection of ViewModel `StateFlow`.
- **Not yet implemented:** runtime camera permission flow, live preview/tracking, accelerometer collection/analysis, graph rendering, and monitoring controls. Camera and graph areas are placeholders; readings are unavailable (`—`) and start is disabled. Camera permission is declared in the manifest but not requested yet.
- **Validation:** `./gradlew :app:assembleDebug :app:lintDebug --no-daemon` passed using JDK 21 and SDK 36. Debug APK generated. Lint has zero errors and 14 warnings (13 dependency/tool upgrade notices and one Android 12+ backup-rule advisory). XML/catalog parsing, wrapper shell syntax, and `git diff --check` passed. Physical-device installation, launch, forced-stop/relaunch, and visual shell checks passed on the Samsung Galaxy S24 FE (SM-S721B), Android 16 / API 36. No emulator test performed.
- **Current phase:** Phase 0 complete. Phase 1 is ready to start; camera and sensor functionality remain unimplemented.
- **Next step:** Developer 1 implements runtime camera permission and lifecycle-bound CameraX preview in `camera/`, then marker tracking and visual readings through `MotionXViewModel`. Sensor implementation starts in Phase 2 after visual motion works reliably.
- **Open decisions:** tracking marker, displacement reference, filtering/window settings, and vibration thresholds. Record actual choices here when implemented.

## Keep this README current

Every developer and AI assistant should read this file before starting work and update it whenever a meaningful change affects implementation status, setup, interfaces, behavior, validation, or next steps. Update it in the same change as the code so another developer can give their AI assistant this repository and continue without the previous chat.

At each handoff, record:

- What is implemented, in progress, and still missing; never describe planned work as working.
- Exact build/run instructions and required tools once available.
- Shared model/API changes, units, timestamps, filtering parameters, and decisions with a short rationale.
- Checks actually run and their results, including device model for hardware tests.
- Known issues, blockers, next concrete task, and responsible developer when known.

Do not put credentials, machine-specific SDK paths, or private conversation history in this file. Repository-wide AI instructions are in [AGENTS.md](AGENTS.md).

## Development phases

| Phase | Scope | Completion gate |
| --- | --- | --- |
| **0 — Setup and physical-device readiness** | Project initialization, dependencies, SDK/JDK setup, build, installation, and app-shell launch | App installs and opens reliably on the Samsung Galaxy S24 FE |
| **1 — Visual motion implementation** | Camera permission, live preview, marker tracking, displacement, visual UI, and camera lifecycle | Real visual motion updates reliably on the phone |
| **2 — Physical sensors and integration** | Accelerometer collection, filtering, vibration metrics, graph/status, and integration with the visual pipeline | Both real data streams work together reliably on the phone |

Follow this order. Building and installing the shell belongs to Phase 0; it does not complete Phase 1. Sensor dependencies and models already in the scaffold are preparation for Phase 2, not an implemented sensor pipeline.

## Phase 0 — Setup and physical-device readiness

Target device: **Samsung Galaxy S24 FE**. Stack: Kotlin, Jetpack Compose, Material 3, CameraX (Phase 1), Android `SensorManager` (Phase 2), and coroutines/`StateFlow`.

| Setting | Value |
| --- | --- |
| Namespace / application ID | `com.motionx.app` |
| Minimum / compile / target SDK | 26 / 36 / 36 |
| Android Gradle Plugin / Gradle | 8.11.1 / 8.14 |
| Kotlin / Compose compiler plugin | 2.1.20 (both) |
| Compose BOM / Material components | 2025.06.01 / Material 3 |
| CameraX | 1.4.2 |
| JVM bytecode target | Java 17 |

Versions are pinned in `gradle/libs.versions.toml`. API 26 keeps the prototype modern while supporting the target phone. The build follows [AGP compatibility requirements](https://developer.android.com/build/releases/agp-8-11-0-release-notes) and uses the matching Kotlin [Compose compiler plugin](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler).

### Open and run

1. Open this repository root in Android Studio with support for AGP 8.11.1 or newer. If an existing generic IDE project opens without an `app` module, import/link `settings.gradle.kts` as a Gradle project.
2. Install Android SDK Platform 36, Build Tools 35.0.0, and Platform Tools through SDK Manager.
3. Use JDK 17 or 21 for Gradle (Android Studio's bundled JDK 21 is suitable). For terminal commands, set `JAVA_HOME` to your JDK.
4. Let Android Studio create `local.properties` with your SDK location, or set `ANDROID_HOME` to your local SDK directory. Machine-specific paths stay untracked.
5. Sync Gradle and select the `app` configuration. Initial dependency downloads require internet; the app itself has no network permission. An API 26+ emulator can help check the shell, but Phase 0 requires a physical-device run.
6. Enable Developer options and USB debugging on the Samsung Galaxy S24 FE. Connect it with a data-capable USB cable, unlock it, and accept the computer's debugging authorization prompt.
7. Select the phone in Android Studio and run `app`, or use the installation command below. Open MotionX on the phone and confirm the shell renders without crashing.

From the repository root (Windows: use `gradlew.bat`):

```sh
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
# With a device connected and authorized:
./gradlew :app:installDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. The first launch displays the UI shell; live monitoring will be added next. `local.properties`, new local IDE files, Gradle caches, and build artifacts are ignored; pre-existing tracked `.idea` metadata is unchanged.

Known scaffold limitations: dependency versions are deliberately pinned, and lint suggests newer releases. `allowBackup=false` is set, but explicit Android 12+ data-extraction rules remain a follow-up if persistence is introduced. Gradle also reports deprecated plugin behavior ahead of Gradle 9; use the supplied Gradle 8.14 wrapper. There are no algorithm tests yet because neither analyzer exists; add focused tests when implementing signal processing.

### Phase 0 acceptance

- [x] Kotlin/Compose app scaffold, dependencies, resources, and Gradle wrapper exist.
- [x] Debug APK builds and lint finishes without errors (warnings documented above).
- [x] Physical Samsung Galaxy S24 FE is connected and authorized for debugging.
- [x] Debug app installs and launches on the phone without crashing.
- [x] App shell renders correctly and can be closed and reopened.
- [x] Record the device's Android version, install/launch result, and any setup blockers in the current handoff.

### Physical-device verification — 2026-10-07

- **Device:** Samsung Galaxy S24 FE, model SM-S721B, Android 16 / API 36, connected and authorized over USB.
- **Installation:** `adb install -r app/build/outputs/apk/debug/app-debug.apk` returned `Success`.
- **Launch:** `adb shell am start -W -n com.motionx.app/.MainActivity` returned `Status: ok`; the activity was confirmed in the foreground.
- **Reopen:** force-stopped only `com.motionx.app`, launched it again, and confirmed a running foreground activity. Both cold launches succeeded.
- **Visual check:** inspected on-device screenshots of the header, camera placeholder, unavailable measurement cards, vibration-history placeholder, and lower controls reached by scrolling. The disabled monitoring button and unavailable message match the current scaffold.
- **Runtime check:** no `AndroidRuntime` error entries were returned for the reopened app process during the smoke check. This verifies basic startup, not long-running stability or future camera/sensor behavior.
- **Blockers:** none remaining for Phase 0. Run these checks again on the connected phone as later phases change behavior.

Camera permission handling, live preview, and tracking are Phase 1 work; live physical sensor data is Phase 2 work.

## Phase 1 — Visual motion implementation

Begin after Phase 0 is complete. Deliver a working camera-based visual motion experience on the physical phone.

### Camera pipeline and UI

CameraX preview → lightweight frame analysis → high-contrast marker/region tracking → X/Y position and displacement in pixels.

- Start with a simple marker; avoid sophisticated computer vision.
- Implement runtime camera permission handling, including denial and retry, and a live CameraX preview.
- Show a tracking indicator over the detected location.
- Expose real X/Y position and displacement through `MotionXViewModel`; display visual motion in pixels.
- Expose tracking validity so a lost marker does not appear as a valid zero-motion reading.
- Process frames off the main thread, close analysis frames, and bind camera use to the activity lifecycle.
- Document whether displacement is relative to the initial position or the preceding frame, and how analysis coordinates map to preview rotation/cropping.
- Enable start/stop for visual monitoring and handle background/resume correctly. Physical vibration readings remain unavailable until Phase 2.

### Phase 1 acceptance and demo

- [ ] Camera permission grant, denial, and retry behave correctly on the phone.
- [ ] Live preview appears with the correct orientation and tracking-overlay alignment.
- [ ] A high-contrast marker produces live X/Y position and displacement; losing it shows an unavailable state.
- [ ] Start/stop controls visual monitoring; background/resume releases and restores camera resources correctly.
- [ ] Visual monitoring remains responsive during a rehearsed physical-device demo.

Demo: keep the phone steady, point it at a high-contrast marker, start monitoring, and move the marker to show visual displacement changing live. Camera motion also affects displacement. Accelerometer readings are not required to complete this phase.

## Phase 2 — Physical sensors and integration

Begin after Phase 1 is stable. Implement the accelerometer pipeline, then combine it with the working visual pipeline. Additional physical sensors can be scoped here later; none beyond the accelerometer are currently specified.

### Accelerometer pipeline and UI

`SensorManager` → raw X/Y/Z acceleration → gravity suppression → smoothing → magnitude, RMS, peak, and status.

- Raw magnitude is `sqrt(x² + y² + z²)`; raw readings include gravity. Suppress the gravity/baseline component before reporting vibration.
- Show current vibration, RMS, peak, X/Y/Z, and status using real readings.
- Use states `NORMAL`, `VIBRATING`, and `HIGH_VIBRATION` (the UI may label the last state `HIGH`).
- Document filter parameters, sample rate, RMS window, peak/reset behavior, and calibrated status thresholds when implemented.
- Register listeners only while monitoring is active and the app is in the foreground; unregister on stop/background. Handle unavailable sensors explicitly.
- Connect sensor readings to `MotionXViewModel`, add the bounded vibration-history graph, and extend start/stop to control both pipelines.

### Phase 2 acceptance and demo

- [ ] Physical motion updates current vibration, RMS, peak, axes, graph, and status live.
- [ ] Stationary readings settle after gravity suppression; thresholds are tested on the target phone.
- [ ] Start/stop and background/resume do not leak or duplicate sensor listeners or camera resources.
- [ ] Missing sensors are handled explicitly without fabricated readings.
- [ ] Both pipelines remain responsive together during a rehearsed physical-device demo.

Demo: show both live streams while moving a marker and gently moving the phone. The accelerometer measures **the phone's motion**, not a remote object's vibration: moving only the marker should primarily affect the camera reading. Use a suitable mechanically coupled setup when comparing a common vibration source.

Demo narrative: “The camera tells us what we can see moving. The accelerometer tells us what the phone physically feels.” This is a prototype with uncalibrated thresholds, not a validated measurement instrument.

## Shared data contract — models implemented, producers pending

Data classes are in `app/src/main/java/com/motionx/app/model/`. Keep these definitions synchronized with the code. `MotionXUiState` starts with null readings to distinguish unavailable data from measured zero.

| Model | Fields | Units/meaning |
| --- | --- | --- |
| `VibrationData` | `accelerationX`, `accelerationY`, `accelerationZ` | Raw accelerometer axes in m/s², including gravity |
| `VibrationData` | `magnitude`, `rms`, `peak` | Gravity-suppressed vibration in g; convert m/s² using 9.80665 m/s² per g |
| `VibrationData` | `status`, `timestamp` | `VibrationStatus` enum; `SensorEvent.timestamp` in nanoseconds since boot |
| `VisualMotionData` | `x`, `y`, `displacement` | Analysis-frame pixels; displacement is a nonnegative movement magnitude with reference defined by the implementation |
| `VisualMotionData` | `timestamp`, `isTracking` | CameraX `ImageInfo.timestamp` in nanoseconds; clock alignment is unverified; validity flag |

The ViewModel currently exposes an empty UI state. Connect the visual pipeline in Phase 1 and the physical sensor pipeline in Phase 2. Do not assume camera and sensor timestamps share a clock without verification. Phase 2 compares the readings side by side; pixel displacement and acceleration in g are different quantities and should not be presented as equivalent measurements.

## One-screen UI

Phase 0 provides the shell. Phase 1 activates the camera and visual readings. Phase 2 adds physical readings, vibration history, and vibration status. Keep unimplemented measurements unavailable rather than displaying sample values as real data.

- App name and tagline at the top.
- Large live camera preview with a small tracking overlay.
- Large visual motion value in px and physical vibration value in g; include RMS and peak, with compact X/Y/Z readouts.
- Compact live vibration graph with a bounded history.
- Status indicator and **START MONITORING / STOP MONITORING** button.

Use a dark background, subtle rounded cards, large readable numbers, one primary accent color, minimal shadows, and restrained animation. Use neutral/green for normal, amber for warning, and red for high vibration. Show permission, tracking-loss, and sensor availability states clearly. Keep the style like a professional engineering instrument.

## Developer ownership and structure

The scaffold uses one app module and no dependency injection or repository layers. `camera/` is reserved for Phase 1 and `sensors/` for Phase 2.

```text
gradle/libs.versions.toml       # Dependency and plugin versions
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    res/                       # Strings, launch theme, icon
    java/com/motionx/app/
      MainActivity.kt
      camera/                  # Phase 1: CameraX preview + CameraAnalyzer.kt
      sensors/                 # Phase 2: VibrationAnalyzer.kt
      model/
        MotionXUiState.kt
        VibrationData.kt
        VisualMotionData.kt
      ui/
        MainScreen.kt          # Route, screen, and Compose preview
        Components.kt
        theme/Theme.kt
      viewmodel/MotionXViewModel.kt
```

| Owner | Responsibilities | Phase deliverables |
| --- | --- | --- |
| Developer 1: Android/UI/camera | Setup, Compose/Material 3, camera permission, preview/tracking, ViewModel and UI integration | Phase 0: installed app shell; Phase 1: working visual motion; Phase 2: physical metrics/graph integration |
| Developer 2: sensor/algorithm | Assist device validation first; implement SensorManager, filtering, magnitude/RMS/peak, and thresholds in Phase 2 | Phase 0: assist setup checks; Phase 1: assist visual validation; Phase 2: tested sensor data through the agreed model |
| Both | Phase acceptance, shared interfaces, lifecycle checks, and device demo | Complete each phase's acceptance checks before starting the next |

In Phase 2, Developer 2 should focus on sensor logic rather than UI. Coordinate shared model and ViewModel edits before changing interfaces.

## Build sequence and time budget

The original target is a 90-minute hackathon build. Follow the phase gates rather than starting camera and sensor work simultaneously. Adjust time per phase based on actual device testing.

1. **Phase 0:** finish initialization, build/install, and physical-device launch validation.
2. **Phase 1:** implement and validate the complete visual motion path on the phone.
3. **Phase 2:** implement physical sensor analysis and integrate it with visual monitoring.
4. **Final 15 minutes of the session:** freeze features, fix issues, polish, and rehearse the last stable phase. If time runs out after Phase 1, demo visual motion and leave Phase 2 explicitly pending.

## Optional enhancements — after Phase 2

These are no longer the definition of Phase 2. Once the visual and sensor implementations are stable, choose at most one: visual motion amplification, dominant-frequency analysis, or baseline-based anomaly detection. Label amplified motion clearly and retain the actual measured value. No optional enhancement is currently implemented.
