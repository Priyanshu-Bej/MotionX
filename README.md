# MotionX

**See the motion you can't see.**

MotionX is a 90-minute Android hackathon prototype that combines camera-based visual motion tracking with the phone's accelerometer readings. Both the repository and app are named **MotionX**.

Everything runs locally on the phone. Prioritize a working prototype, real sensor data, a simple polished UI, and a reliable demo. No backend, networking, Firebase, login, database, or complex architecture.

## Current handoff — 2026-10-07

- **Implemented:** Phase 0 scaffold plus Phase 1 runtime camera permission/settings recovery, lifecycle-bound rear-camera preview, lightweight black-marker tracking, transformed tracking overlay, X/Y/displacement readings, visual start/stop, and foreground cleanup. Shared sensor data models remain unchanged.
- **Not yet integrated:** Phase 2 accelerometer collection/analysis, physical vibration status, and history graph. Physical readings remain unavailable (`—`); your fellow developer owns this pipeline.
- **Validation:** Phase 1 debug build, 10 JVM tests, and lint passed using JDK 21 / SDK 36. Lint has zero errors and 14 existing warnings (dependency/tool upgrade notices and a backup-rule advisory). Installed on the Samsung Galaxy S24 FE (SM-S721B), Android 16 / API 36; live preview, missing-permission recovery, and visual controls exercised. Physical marker motion, overlay alignment across rotations, and longer demo stability remain to be verified. See the Phase 1 checklist below.
- **Current phase:** Phase 0 complete. Phase 1 code is implemented with partial device validation; the fellow developer can implement Phase 2 in parallel and merge it afterward.
- **Next step:** The user will perform the physical-marker check later. Phase 1 implementation is ready, but marker motion/loss and rotation acceptance remain pending. Merge the fellow developer’s Phase 2 sensor pipeline when ready using the contract below, then validate the combined behavior.
- **Open decisions:** Phase 2 filtering/window settings and vibration thresholds. The Phase 1 marker and displacement reference are documented below.

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

Phase numbers describe feature scope. Per the updated team plan, Phase 1 visual work and Phase 2 sensor work proceed in parallel after Phase 0, then merge. Each pipeline and their combined behavior need validation before the integrated prototype is complete.

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

1. Open this repository root in Android Studio with support for AGP 8.11.1 or newer. If an existing generic IDE project opens without an `app` module, right-click the root `build.gradle.kts` and choose **Import Gradle Project** (or use **Link Gradle Project** in the Gradle tool window).
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
./gradlew :app:testDebugUnitTest
# With a device connected and authorized:
./gradlew :app:installDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. On launch, allow camera access, center a black marker on white paper, and start visual monitoring. `local.properties`, new local IDE files, Gradle caches, and build artifacts are ignored; pre-existing tracked `.idea` metadata is unchanged.

Known scaffold limitations: dependency versions are deliberately pinned, and lint suggests newer releases. `allowBackup=false` is set, but explicit Android 12+ data-extraction rules remain a follow-up if persistence is introduced. Gradle also reports deprecated plugin behavior ahead of Gradle 9; use the supplied Gradle 8.14 wrapper. Marker tracking and visual ViewModel behavior have JVM tests. Phase 2 should add focused signal-processing tests.

### Android Studio: Run disabled or no app module

The repository initially contained a generic Java IDE module. Opening that workspace before the Android scaffold was added can leave Android Studio without an imported Gradle model, even though command-line builds and device installation succeed.

- Import/link the root `build.gradle.kts`, then use **File → Sync Project with Gradle Files** and wait for sync/indexing to finish.
- Select the shared **app** run configuration (`.run/app.run.xml`) and the connected phone. It launches the manifest's default activity from the imported `MotionX.app` module.
- If Studio was already open when local linkage settings changed, close this project and reopen the repository root. Keep the source and `.idea` files; cache deletion is not required for this diagnosis.
- If sync fails, inspect the Sync/Build output and check the Gradle JDK (17 or 21) and local SDK path. The project uses the supplied Gradle wrapper.

On 2026-10-07, the local workspace was found to have only the old Java module and no Gradle linkage or `local.properties`. Local Gradle linkage/SDK configuration and a shared app run configuration were added. `:app:assembleDebug` passed using the local SDK configuration without the earlier terminal-only `ANDROID_HOME` override; both IDE XML files parsed successfully. Android Studio still needs to load/sync those settings before its Run button can be verified. New local IDE metadata and SDK paths remain ignored by Git. See the official [Gradle import guidance](https://www.jetbrains.com/help/idea/work-with-gradle-projects.html) and [Android run configuration guide](https://developer.android.com/studio/run/rundebugconfig).

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

### Tracking implementation and limitations

Use a single solid black dot or square on plain white paper, with even lighting. Start with a marker roughly 20–80 camera pixels wide, centered in the circular guide. Move it slowly while keeping the phone steady.

Print [docs/marker.svg](docs/marker.svg), or draw a filled black circle on white paper. The white circle shown inside the app is only an aiming guide; it is not the physical marker. A plain laptop surface or fingers will usually remain in the searching state.

- `MarkerTracker` reads the Y plane with row/pixel stride and crop support. It samples a grid with step `max(1, max(cropWidth, cropHeight) / 320)` and uses connected dark regions with a bright surrounding border. It rejects low contrast (less than 60 luma levels), tiny noise, large dark surfaces, clipped shapes, and elongated regions.
- Acquisition picks a compact marker within 30% of the crop’s shorter dimension from center. Subsequent frames choose a nearby candidate within 12% of that dimension, with an area ratio of 0.5–2. Multiple similar markers or clutter can confuse this simple tracker; it is not general-purpose object tracking.
- X/Y are contrast-weighted centroids in full, unrotated analysis-buffer pixels. Displacement is Euclidean distance from the first valid centroid. Loss invalidates the reading immediately and resets the reference; reacquisition, stop/start, or backgrounding starts a new segment. Decimal values do not imply calibrated subpixel accuracy.
- CameraX requests analysis near 640×480; actual resolution depends on the device. `KEEP_ONLY_LATEST`, one analysis executor, and a maximum 15 processed frames/second keep work bounded. Every frame is closed in `finally`.
- Preview and analysis share a `ViewPort`. CameraX output transforms map buffer coordinates to the cropped/rotated preview overlay. The transform API requires an explicit experimental opt-in in CameraX 1.4.2. See the official [image-analysis lifecycle guidance](https://developer.android.com/media/camera/camerax/analyze) and [coordinate-transform API](https://developer.android.com/reference/androidx/camera/view/transform/CoordinateTransform).
- Start/stop controls visual analysis; preview stays live while idle. Leaving the foreground unbinds camera use cases and stops monitoring. On return, preview resumes, but the user starts a new monitoring session. Session callbacks are discarded after disposal.

### Phase 1 acceptance and demo

- [x] Missing/revoked permission shows the access controls; requesting permission and restoring access returns to live preview on the phone.
- [ ] Complete repeated denial/permanent-denial and app-settings recovery checks.
- [x] Live rear-camera preview appears on the phone in portrait orientation.
- [ ] Verify tracking-overlay alignment with an actual marker, including device rotation.
- [ ] A high-contrast marker produces live X/Y position and displacement; losing it shows an unavailable state.
- [x] Start/stop controls visual monitoring; background/resume releases and restores camera resources correctly on the S24 FE.
- [ ] Visual monitoring remains responsive during a rehearsed physical-device demo.

Demo: keep the phone steady, point it at a high-contrast marker, start monitoring, and move the marker to show visual displacement changing live. Camera motion also affects displacement. Accelerometer readings are not required to complete this phase.

Automated checks: seven tracker tests cover known translation, reference reset, lost tracking, low contrast/noise, clipped markers/jump rejection, padded/cropped buffers with pixel stride, and downsampling units. Three ViewModel tests cover readiness gating, rejecting late readings after stop, and camera-error recovery. These tests use synthetic luminance buffers; they do not replace the physical-marker demo.

Keep follow-up validation narrow: reuse the passing build/lint/unit-test results unless relevant code changes. The next device check is one short marker session: acquire tracking, move the marker and confirm px changes, move it out of view to confirm unavailable readings, then rotate and check overlay alignment after restarting monitoring. Avoid repeating setup and lifecycle tests already verified above.

Follow-up on 2026-10-07: confirmed the connected phone was displaying the live preview in `CAMERA READY`; the scene contained no marker. The user chose to test with a marker later. No build, lint, or unit tests were rerun during this follow-up, and marker acceptance remains unchecked.

Device checks on 2026-10-07: installed the final Phase 1 APK on SM-S721B / Android 16. Confirmed portrait preview, start → searching/stop control, unavailable values with no valid marker, and camera permission restoration after revocation. Backgrounded an active monitoring session and confirmed `Active Camera Clients: []`; returning restored the preview in `CAMERA READY` with `START MONITORING` and cleared readings. Preview clipping was corrected and visually rechecked. The observed scene did not contain a suitable black-on-white marker, so live displacement, marker-loss recovery, rotation alignment, and a longer demo are still pending. No sensor implementation was added in this change.

## Phase 2 — Physical sensors and integration

The fellow developer implements this pipeline in parallel with Phase 1. Merge it with the visual pipeline afterward, then validate the combined app. Additional physical sensors can be scoped here later; none beyond the accelerometer are currently specified.

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

## Shared data contract and Phase 2 merge handoff

Data classes are in `app/src/main/java/com/motionx/app/model/`. Keep these definitions synchronized with the code. `MotionXUiState` starts with null readings to distinguish unavailable data from measured zero.

| Model | Fields | Units/meaning |
| --- | --- | --- |
| `VibrationData` | `accelerationX`, `accelerationY`, `accelerationZ` | Raw accelerometer axes in m/s², including gravity |
| `VibrationData` | `magnitude`, `rms`, `peak` | Gravity-suppressed vibration in g; convert m/s² using 9.80665 m/s² per g |
| `VibrationData` | `status`, `timestamp` | `VibrationStatus` enum; `SensorEvent.timestamp` in nanoseconds since boot |
| `VisualMotionData` | `x`, `y`, `displacement` | Full unrotated camera-buffer pixels; displacement is distance from the first valid position in the current tracking segment |
| `VisualMotionData` | `timestamp`, `isTracking` | CameraX `ImageInfo.timestamp` in nanoseconds; clock alignment is unverified; validity flag |

The ViewModel exposes live visual state; `vibration` remains null until Phase 2 is merged. Do not assume camera and sensor timestamps share a clock without verification. Phase 2 compares the readings side by side; pixel displacement and acceleration in g are different quantities and should not be presented as equivalent measurements.

### Fellow developer: Phase 2 integration contract

- Own `sensors/` and its tests. Preserve the existing `model/VibrationData.kt` and `VibrationStatus` names, fields, and units. No backend, persistence, networking, or UI work is required in the sensor branch.
- Suggested analyzer API: `VibrationAnalyzer(context)`, read-only `data: StateFlow<VibrationData?>`, `start()`, and `stop()`. Report absent hardware explicitly (for example, `isAvailable`); use null before samples exist. Make start/stop idempotent and unregister listeners on stop. Record the final API here if it differs.
- Raw X/Y/Z remain in m/s² including gravity; magnitude/RMS/peak are gravity-suppressed g. Preserve sensor-event timestamps. Document sample rate, gravity removal, filter constants, RMS window, peak reset, and status thresholds with tests.
- Leave `camera/`, `MainScreen`, `MotionXRoute`, and `MotionXViewModel` to Phase 1 until merge. `MotionXUiState` gained additive visual-control fields (`isMonitoring`, `cameraReady`, `cameraProblem`); its existing `vibration` field is unchanged.
- At merge, collect sensor data in the ViewModel using atomic `update { it.copy(vibration = reading) }` so sensor updates preserve visual fields. Coordinate ownership of analyzer lifecycle; start both pipelines on monitoring, stop them on stop/background, and clear stale readings. Extend camera-gated start behavior deliberately if sensor-only mode is required.
- Merge the vibration graph, physical status, and axis readouts after the producer works. Test simultaneous collection, missing permissions/hardware, stop/resume, and gravity suppression on the phone. Do not infer time synchronization from the two timestamp fields alone.

## One-screen UI

Phase 0 provides the shell. Phase 1 activates the camera and visual readings. Phase 2 adds physical readings, vibration history, and vibration status. Keep unimplemented measurements unavailable rather than displaying sample values as real data.

- App name and tagline at the top.
- Large live camera preview with a small tracking overlay.
- Large visual motion value in px and physical vibration value in g; include RMS and peak, with compact X/Y/Z readouts.
- Compact live vibration graph with a bounded history.
- Status indicator and **START MONITORING / STOP MONITORING** button.

Use a dark background, subtle rounded cards, large readable numbers, one primary accent color, minimal shadows, and restrained animation. Use neutral/green for normal, amber for warning, and red for high vibration. Show permission, tracking-loss, and sensor availability states clearly. Keep the style like a professional engineering instrument.

## Developer ownership and structure

The project uses one app module and no dependency injection or repository layers. Phase 1 owns `camera/` and visual UI; the fellow developer owns `sensors/` and sensor tests.

```text
gradle/libs.versions.toml       # Dependency and plugin versions
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    res/                       # Strings, launch theme, icon
    java/com/motionx/app/
      MainActivity.kt
      camera/                  # CameraPreview, CameraAnalyzer, MarkerTracker
      sensors/                 # Phase 2: VibrationAnalyzer.kt
      model/
        MotionXUiState.kt
        VibrationData.kt
        VisualMotionData.kt
      ui/
        MainScreen.kt          # Screen and Compose preview
        MotionXRoute.kt        # Permission and lifecycle handling
        Components.kt
        theme/Theme.kt
      viewmodel/MotionXViewModel.kt
```

| Owner | Responsibilities | Phase deliverables |
| --- | --- | --- |
| Developer 1: Android/UI/camera | Setup, Compose/Material 3, camera permission, preview/tracking, ViewModel and UI integration | Phase 0: installed app shell; Phase 1: working visual motion; Phase 2: physical metrics/graph integration |
| Developer 2: sensor/algorithm | Independently implement SensorManager, filtering, magnitude/RMS/peak, thresholds, and sensor tests | Phase 2: tested sensor data through the existing model, ready to merge |
| Both | Phase acceptance, shared interfaces, lifecycle checks, and device demo | Validate each pipeline, then validate their merged behavior |

In Phase 2, Developer 2 should focus on sensor logic rather than UI. Coordinate shared model and ViewModel edits before changing interfaces.

## Build sequence and time budget

The original target is a 90-minute hackathon build. Phase 0 is complete; develop the two pipelines in parallel and reserve time for their merge and device checks.

1. **Phase 0:** finish initialization, build/install, and physical-device launch validation.
2. **Parallel work:** Developer 1 implements and validates Phase 1 visual motion; Developer 2 independently implements and tests Phase 2 sensor analysis.
3. **Merge:** integrate Phase 2 into the shared UI state and controls, then test both pipelines together.
4. **Final 15 minutes of the session:** freeze features, fix issues, polish, and rehearse the last stable phase. If time runs out after Phase 1, demo visual motion and leave Phase 2 explicitly pending.

## Optional enhancements — after Phase 2

These are no longer the definition of Phase 2. Once the visual and sensor implementations are stable, choose at most one: visual motion amplification, dominant-frequency analysis, or baseline-based anomaly detection. Label amplified motion clearly and retain the actual measured value. No optional enhancement is currently implemented.
