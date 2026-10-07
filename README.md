# MotionX

**See the motion you can't see.**

MotionX is a 90-minute Android hackathon prototype that combines camera-based visual motion tracking with the phone's accelerometer readings. Both the repository and app are named **MotionX**.

Everything runs locally on the phone. Prioritize a working prototype, real sensor data, a simple polished UI, and a reliable demo. No backend, networking, Firebase, login, database, or complex architecture.

## Current handoff — 2026-10-07

- **Implemented:** project brief and developer/AI handoff documentation only.
- **Not yet implemented:** Android project, Gradle configuration/wrapper, app code, camera pipeline, sensor pipeline, and UI.
- **Validation:** documentation reviewed against the supplied brief and repository contents. No build or device tests are possible yet.
- **Next step:** Developer 1 scaffolds the Android app and gets a minimal screen compiling and running before adding tracking. Developer 2 implements the sensor pipeline against the shared contract below once the module/package exists.
- **Open decisions:** application ID/package, Android SDK levels, dependency versions, tracking marker, filtering/window settings, and vibration thresholds. Record actual choices here when implemented.

## Keep this README current

Every developer and AI assistant should read this file before starting work and update it whenever a meaningful change affects implementation status, setup, interfaces, behavior, validation, or next steps. Update it in the same change as the code so another developer can give their AI assistant this repository and continue without the previous chat.

At each handoff, record:

- What is implemented, in progress, and still missing; never describe planned work as working.
- Exact build/run instructions and required tools once available.
- Shared model/API changes, units, timestamps, filtering parameters, and decisions with a short rationale.
- Checks actually run and their results, including device model for hardware tests.
- Known issues, blockers, next concrete task, and responsible developer when known.

Do not put credentials, machine-specific SDK paths, or private conversation history in this file. Repository-wide AI instructions are in [AGENTS.md](AGENTS.md).

## Stack and setup

Target device: **Samsung Galaxy S24 FE**. Planned stack: Kotlin, Jetpack Compose, Material 3, CameraX, Android `SensorManager`, and coroutines/`StateFlow` where useful. Choose a sensible modern minimum Android API supported by the target device.

There is currently no runnable Android project. The scaffold should include the Gradle wrapper, manifest, camera permission, and dependency configuration. After scaffolding, replace this paragraph with verified prerequisites, SDK/JDK versions, build commands, and physical-device installation instructions. Keep `local.properties` and build artifacts out of version control.

## Phase 1: required behavior

### Camera pipeline

CameraX preview → lightweight frame analysis → high-contrast marker/region tracking → X/Y position and displacement in pixels.

- Start with a simple marker; avoid sophisticated computer vision.
- Show a tracking indicator over the detected location.
- Expose tracking validity so a lost marker does not appear as a valid zero-motion reading.
- Process frames off the main thread, close analysis frames, and bind camera use to the activity lifecycle.
- Document whether displacement is relative to the initial position or the preceding frame, and how analysis coordinates map to preview rotation/cropping.

### Accelerometer pipeline

`SensorManager` → raw X/Y/Z acceleration → gravity suppression → smoothing → magnitude, RMS, peak, and status.

- Raw magnitude is `sqrt(x² + y² + z²)`; raw readings include gravity. Suppress the gravity/baseline component before reporting vibration.
- Show current vibration, RMS, peak, X/Y/Z, and status using real readings.
- Use states `NORMAL`, `VIBRATING`, and `HIGH_VIBRATION` (the UI may label the last state `HIGH`).
- Document filter parameters, sample rate, RMS window, peak/reset behavior, and calibrated status thresholds when implemented.
- Register listeners only while monitoring is active and the app is in the foreground; unregister on stop/background. Handle unavailable sensors explicitly.

### Shared data contract — proposed, not implemented

Agree on this contract before integrating. Keep these definitions synchronized with the code.

| Model | Fields | Proposed units/meaning |
| --- | --- | --- |
| `VibrationData` | `accelerationX`, `accelerationY`, `accelerationZ` | Raw accelerometer axes in m/s², including gravity |
| `VibrationData` | `magnitude`, `rms`, `peak` | Gravity-suppressed vibration in g; convert m/s² using 9.80665 m/s² per g |
| `VibrationData` | `status`, `timestamp` | Status enum; monotonic nanoseconds with documented clock source |
| `VisualMotionData` | `x`, `y`, `displacement` | Analysis-frame pixels; displacement is a nonnegative movement magnitude with reference defined by the implementation |
| `VisualMotionData` | `timestamp`, `isTracking` | Frame timestamp with documented clock source; validity flag |

The ViewModel exposes both pipelines through one UI state. Do not assume camera and sensor timestamps share a clock without verification. Phase 1 compares the readings side by side; pixel displacement and acceleration in g are different quantities and should not be presented as equivalent measurements.

## One-screen UI

- App name and tagline at the top.
- Large live camera preview with a small tracking overlay.
- Large visual motion value in px and physical vibration value in g; include RMS and peak, with compact X/Y/Z readouts.
- Compact live vibration graph with a bounded history.
- Status indicator and **START MONITORING / STOP MONITORING** button.

Use a dark background, subtle rounded cards, large readable numbers, one primary accent color, minimal shadows, and restrained animation. Use neutral/green for normal, amber for warning, and red for high vibration. Show permission, tracking-loss, and sensor availability states clearly. Keep the style like a professional engineering instrument.

## Developer ownership and planned structure

Paths below are relative to the eventual Kotlin application package and **do not exist yet**.

```text
app/src/main/java/<application-package>/
  MainActivity.kt
  camera/CameraAnalyzer.kt
  sensors/VibrationAnalyzer.kt
  model/VibrationData.kt
  model/VisualMotionData.kt
  ui/MainScreen.kt
  ui/Components.kt
  viewmodel/MotionXViewModel.kt
```

| Owner | Responsibilities | First deliverable |
| --- | --- | --- |
| Developer 1: Android/UI/camera | Project scaffold, Compose/Material 3, permission flow, CameraX preview and tracking, ViewModel integration, graph, UI | Compiling app shell with live camera preview |
| Developer 2: sensor/algorithm | SensorManager, raw readings, gravity suppression, smoothing, magnitude/RMS/peak, status thresholds, hardware testing | Sensor pipeline exposing clean data through the agreed model |
| Both | Shared data models, integration, lifecycle checks, device demo | Stable simultaneous live readings |

Developer 2 should focus on sensor logic rather than UI. Coordinate shared model and ViewModel edits before changing interfaces.

## 90-minute build sequence

| Time | Developer 1 | Developer 2 |
| --- | --- | --- |
| 0–10 min | Scaffold, Compose screen, permission, camera preview | SensorManager, raw X/Y/Z, analyzer skeleton |
| 10–30 min | Marker tracking and displacement | Gravity suppression, filtering, RMS, peak, status |
| 30–45 min | Integrate both pipelines into UI state | Integrate and verify sensor model/units |
| 45–60 min | Finish UI, graph, status | Calibrate thresholds and stabilize readings |
| 60–75 min | Test on S24 FE; fix permissions, lifecycle, crashes | Test motion levels and sensor lifecycle |
| 75–90 min | Freeze features, polish, rehearse demo | Freeze features, polish, rehearse demo |

## Phase 1 acceptance and demo

These are pending checks, not completed test results:

- [ ] App builds, installs, and launches on the Samsung Galaxy S24 FE.
- [ ] Camera permission grant, denial, and retry behave correctly.
- [ ] Start/stop controls collection; background/resume does not leak or duplicate camera/sensor listeners.
- [ ] Tracking a high-contrast marker updates visual motion and its overlay live; losing it shows an unavailable state.
- [ ] Physical motion updates current vibration, RMS, peak, axes, graph, and status live.
- [ ] Stationary readings settle after gravity suppression; thresholds are tested on the target phone.
- [ ] Both pipelines remain responsive together during a rehearsed demo.

For the demo, point the camera at a high-contrast marker on a moving object, start monitoring, and show both live streams. The accelerometer measures **the phone's motion**, not a remote object's vibration: moving only the marker should primarily affect the camera reading. Move the phone gently to demonstrate the physical stream, or use a suitable mechanically coupled setup when comparing a common vibration source. Camera motion also affects visual displacement.

Demo narrative: “The camera tells us what we can see moving. The accelerometer tells us what the phone physically feels.” This is a prototype with uncalibrated thresholds, not a validated measurement instrument.

## Phase 2 — only after Phase 1 is stable

Choose at most one: visual motion amplification, dominant-frequency analysis, or baseline-based anomaly detection. Label amplified motion clearly and retain the actual measured value. No Phase 2 feature is currently implemented; ship Phase 1 if time is tight.
