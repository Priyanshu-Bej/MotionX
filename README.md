# MotionX

**See the motion you can't see.**

MotionX is a 90-minute Android hackathon prototype that combines camera-based visual motion tracking with the phone's accelerometer readings. Both the repository and app are named **MotionX**.

Everything runs locally on the phone. Prioritize a working prototype, real sensor data, a simple polished UI, and a reliable demo. No backend, networking, Firebase, login, database, or complex architecture.

Showcase preparation: [demo plan and presenter script](docs/DEMO.md) covers the problem statement, intended users, use cases, a four-minute live sequence, five-slide outline, and judge questions. It includes sourced real-life examples from Samsung (washer vibration), Fluke (rotating equipment), and phyphox (student spring experiments), plus a ready-to-say opening. Position MotionX as a motion-observation and user-threshold alert prototype for students/makers; technician use is a potential pilot, not validated fault diagnosis.

## Current handoff — 2026-10-07

- **Implemented:** Phase 1 camera/marker tracking plus Phase 2 accelerometer readings, gravity suppression, smoothed magnitude, RMS/peak, physical status, raw axes, and separate live visual-displacement (px) and physical-vibration (g) graphs. Both pipelines share start/stop and foreground lifecycle handling.
- **Integration:** merged `origin/feature/phase2-accelerometer` (`ba432db`) into `develop`. Kept Phase 1 camera code and shared data contracts, combined README changes, and removed duplicate JUnit declarations introduced by the merge.
- **Validation:** merged debug build and 14 focused tests passed (8 vibration-analyzer + 6 ViewModel). Installed on S24 FE / Android 16; observed real magnitude, RMS, peak, axes, physical status, and graph with the camera active. Backgrounding released both sensor and camera connections; returning showed idle controls and cleared readings. No AndroidRuntime errors appeared during this smoke check. Existing camera tests and lint were not rerun. The user subsequently reported passing the stationary, marker-motion/loss, and combined tab-switch checks; see the user acceptance record below.
- **Current phase:** Phase 3 dominant-frequency estimation implemented, with focused automated validation passed; known-frequency hardware validation remains pending. Phase 1 and Phase 2 basic functional acceptance was reported by the user. Numerical calibration, rotation/overlay alignment, permission edge cases, and a longer demo remain separate follow-ups.
- **Next step:** confirm audible feedback and a marker-driven Visual threshold alert, then compare the new Hz estimate with a mechanically coupled, known-frequency source within the displayed range. Record source/reference Hz, observed sample rate, estimated Hz, and motion level. Numerical amplitude/noise calibration remains separate; do not repeat passed functional checks without a relevant change.
- **Open decisions:** on-device threshold calibration and observed sampling rate; default filter parameters are documented below.
- **Showcase handoff:** added `docs/DEMO.md` on 2026-10-07 using the implemented camera, accelerometer, graphs, Hz estimate, and threshold alerts. Added primary-source real-life references and presenter wording for appliance vibration, workshop equipment, and physics education; these motivate proposed uses and do not establish MotionX deployments or diagnostic accuracy. The spring example notes the app's nominal 2 Hz lower frequency limit. No runtime change or new hardware validation in this documentation update. Audible feedback and the optional Visual alert stage need one final rehearsal before being promised live; the plan records fallbacks and separates calibration/roadmap from demonstrated behavior.
- **Latest change:** replaced the brief threshold beep with a warning buzzer: three 400 ms low-pitched bursts with 120 ms gaps (about 1.44 seconds). Both alert channels use it; the UI calls it **Play warning buzzer**. Stop/background cancels queued pulses. Notification-volume behavior and the existing cooldown/rearm policy remain unchanged. `assembleDebug` passed; updated APK installed and launched successfully on the connected S24 FE. No sensor or policy test suite rerun for this sound-pattern change. The actual buzzer sound and interruption mid-pattern still need an on-device listening check.
- **Previous alert change:** independent Visual (px) and Physical (smoothed g) threshold alerts, dashed graph limits, optional beep/haptic feedback, and a shared last-alert indicator. Defaults are disabled; enable/configure under each graph. Build and 19 focused tests passed. Installed on S24 FE; a real 0.1 g physical crossing showed the dashed threshold and last-alert message. Android vibrator service recorded completed 120 ms MotionX effects. Audible tone and a marker-driven Visual alert remain unconfirmed. Read the alert policy below before changing sampling, thresholds, or lifecycle.
- **Previous frequency change:** Phase 3 adds a dominant-frequency card and observed sample rate to Physical, with Hz/range/limitations explained in Guide. Uses signed full-rate linear acceleration before magnitude smoothing and UI throttling. Build and 26 focused tests passed (9 frequency, 9 vibration, 8 ViewModel). Installed/launched on S24 FE: frequency quality message and observed rate rendered with live amplitude readings and camera TRACKING; no AndroidRuntime errors appeared during the brief check. Known-frequency accuracy remains pending.
- **Previous UI change:** separated the UI into **Visual**, **Physical**, and **Guide** tabs with tap/swipe navigation and independent scroll positions. The shared start/stop control stays visible. All pages remain composed so tab switches preserve the camera binding, tracking reference, sensor session, and histories; both pipelines continue while monitoring on any tab. Guide explanations are now a full page instead of a dialog. Debug build passed and the APK installed/launched on the S24 FE. A focused device check showed all three tabs, the full-page Guide, live physical readings after tab switches, and the shared Stop control. Camera service confirmed MotionX remained connected while Physical was selected (no rebind during those switches). The user subsequently confirmed stream continuity across tab switches. Rotation and measurement calibration remain unverified. No unit suite or lint rerun for this navigation change; algorithms and shared model contracts are unchanged.
- **Previous graph validation:** debug build and all 8 affected ViewModel tests passed, including history bounds, lost-tracking gaps, sensor-state preservation, and resets. Updated APK installed and launched successfully on the connected S24 FE. Camera/sensor algorithm tests and lint were not repeated for that graph-only change. Physical-marker graph behavior was subsequently reported working by the user.

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
| **3 — Dominant vibration frequency** | Timestamp-based spectral analysis of physical acceleration, Hz card, signal-quality states, and sampling/range explanation | Focused signal tests and a known-frequency hardware comparison |

Phase numbers describe feature scope. Phase 1 and Phase 2 were developed in parallel after Phase 0 and are now integrated. The merged app has passed a basic device check, and the user has reported working marker tracking, stationary settling, and combined monitoring across tabs. Measurement calibration and the remaining edge-case checks are still pending.

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

Known scaffold limitations: dependency versions are deliberately pinned, and lint suggests newer releases. `allowBackup=false` is set, but explicit Android 12+ data-extraction rules remain a follow-up if persistence is introduced. Gradle also reports deprecated plugin behavior ahead of Gradle 9; use the supplied Gradle 8.14 wrapper. Marker tracking and visual ViewModel behavior have JVM tests. Phase 2 includes eight focused signal-processing tests.

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

### Live visual motion graph

`VisualMotionGraph` plots the same displacement shown by the visual-motion card, in camera-buffer pixels, over the latest 10 seconds. The vertical scale adjusts automatically (minimum 1 px); the horizontal axis runs from −10 s to Now. It uses camera timestamps only, not sensor timestamps, and updates at the analyzer's existing maximum 15 readings/second.

`MotionXUiState.visualMotionHistory` retains at most 150 samples within the time window, including invalid readings to mark gaps. Invalid samples are never plotted as zero; loss/reacquisition and frame gaps longer than 0.5 seconds break the trace. Reacquisition resets the tracker's displacement reference, so each segment starts from its own origin. Stop, background, camera failure, and a fresh session clear the graph; backwards timestamps reset its history. An empty graph prompts the user to acquire a marker.

Focused validation: `./gradlew :app:assembleDebug :app:testDebugUnitTest --tests 'com.motionx.app.viewmodel.MotionXViewModelTest' --no-daemon` passed (8 tests). This verifies history/state behavior; live graph movement with a physical marker was subsequently reported working by the user.

### Phase 1 acceptance and demo

- [x] Missing/revoked permission shows the access controls; requesting permission and restoring access returns to live preview on the phone.
- [ ] Complete repeated denial/permanent-denial and app-settings recovery checks.
- [x] Live rear-camera preview appears on the phone in portrait orientation.
- [ ] Verify tracking-overlay alignment with an actual marker, including device rotation.
- [x] Marker displacement and the visual graph respond to movement; removing the marker shows tracking loss (user-reported check on 2026-10-07).
- [x] Start/stop controls visual monitoring; background/resume releases and restores camera resources correctly on the S24 FE.
- [ ] Visual monitoring remains responsive during a rehearsed physical-device demo.

Demo: keep the phone steady, point it at a high-contrast marker, start monitoring, and move the marker to show visual displacement changing live. Camera motion also affects displacement. Accelerometer readings are not required to complete this phase.

Automated checks: seven tracker tests cover known translation, reference reset, lost tracking, low contrast/noise, clipped markers/jump rejection, padded/cropped buffers with pixel stride, and downsampling units. Three ViewModel tests cover readiness gating, rejecting late readings after stop, and camera-error recovery. These tests use synthetic luminance buffers; they do not replace the physical-marker demo.

Keep follow-up validation narrow: reuse the passing build/lint/unit-test results unless relevant code changes. The user has now reported passing marker movement/loss and graph checks. The remaining camera checks are rotation/overlay alignment and permission edge cases. Avoid repeating setup and lifecycle tests already verified above.

Follow-up on 2026-10-07: confirmed the connected phone was displaying the live preview in `CAMERA READY`; the scene contained no marker. The user chose to test with a marker later. No build, lint, or unit tests were rerun during this follow-up, and marker acceptance was unchecked at that point; the later user acceptance record below supersedes that deferral.

Device checks on 2026-10-07: installed the final Phase 1 APK on SM-S721B / Android 16. Confirmed portrait preview, start → searching/stop control, unavailable values with no valid marker, and camera permission restoration after revocation. Backgrounded an active monitoring session and confirmed `Active Camera Clients: []`; returning restored the preview in `CAMERA READY` with `START MONITORING` and cleared readings. Preview clipping was corrected and visually rechecked. The observed scene did not contain a suitable black-on-white marker, so live displacement, marker-loss recovery, rotation alignment, and a longer demo were pending at that point. The later user check covers movement and tracking loss; rotation alignment and a longer demo remain pending. No sensor implementation was added in this change.

## Phase 2 — Physical sensors and integration

The fellow developer’s pipeline from `feature/phase2-accelerometer` is integrated with Phase 1. Validate the combined behavior before treating the measurements as calibrated. Additional physical sensors can be scoped here later; none beyond the accelerometer are currently specified.

### Accelerometer pipeline and UI

`SensorManager` → raw X/Y/Z acceleration → gravity suppression → smoothing → magnitude, RMS, peak, and status.

- Raw magnitude is `sqrt(x² + y² + z²)`; raw readings include gravity. Suppress the gravity/baseline component before reporting vibration.
- Show current vibration, RMS, peak, X/Y/Z, and status using real readings.
- Use states `NORMAL`, `VIBRATING`, and `HIGH_VIBRATION` (the UI may label the last state `HIGH`).
- Document filter parameters, sample rate, RMS window, peak/reset behavior, and calibrated status thresholds when implemented.
- Register listeners only while monitoring is active and the app is in the foreground; unregister on stop/background. Handle unavailable sensors explicitly.
- Connect sensor readings to `MotionXViewModel`, add the bounded vibration-history graph, and extend start/stop to control both pipelines.

### Phase 2 implementation and integration

Source branch: `feature/phase2-accelerometer`, commit `ba432db`. The source and tests arrived unverified; merged validation is recorded in the current handoff.

- `sensors/VibrationAnalyzer.kt` — pure Kotlin, no Android dependencies. `process(x, y, z, timestampNanos)` takes raw m/s² and returns `VibrationData`; `reset()` clears peak/RMS/gravity state. Not thread-safe; feed it from one thread.
- `sensors/AccelerometerSource.kt` — `SensorManager` wrapper. `isAvailable` reports whether `TYPE_ACCELEROMETER` exists. `readings(): Flow<VibrationData>` registers the listener when collection starts and unregisters it (and stops its `HandlerThread`) when collection is cancelled. Events arrive on a background thread. If no sensor exists or registration fails, the flow fails with `SensorUnavailableException`. Each collection uses a new analyzer, so peak/RMS restart per monitoring session. No runtime permission is needed.
- `app/src/test/.../VibrationAnalyzerTest.kt` — JVM tests with synthetic 100 Hz input: stationary → 0, tilt settles, 0.1 g/0.5 g sine at 10 Hz → expected RMS and status, RMS decay, peak hold/reset, warm-up exclusion. JUnit 4.13.2 added as `testImplementation`.
- **Integrated:** the route collects the sensor flow only during a foreground monitoring session. Cancellation unregisters the listener and stops its thread, including registration failures. The ViewModel preserves camera fields, rejects stale session callbacks, publishes sensor UI updates at most 10 times/second, and retains at most 100 samples spanning the latest 10 seconds. Stop/background clears readings/history; restarting constructs a fresh analyzer. Missing/failed sensors show an explicit message while visual monitoring continues.
- **UI:** live magnitude, RMS, peak, raw X/Y/Z, physical status, and a timestamp-based graph of smoothed magnitude in g. The graph labels its automatic vertical scale. The existing camera-ready start requirement remains; independent sensor-only mode is not added.

Analyzer parameters (defaults in `VibrationAnalyzer.Config`; filter coefficients use real timestamp intervals; RMS is a sample-weighted mean within the time window and can still depend on sampling/jitter):

| Parameter | Value | Notes |
| --- | --- | --- |
| Requested sample period | 10 000 µs (100 Hz) | Phase 3 observed about 124.7 samples/s in one S24 FE window; rate is device-dependent, not guaranteed |
| Gravity suppression | Per-axis first-order low-pass, τ = 0.2 s (≈0.8 Hz cutoff); linear = raw − gravity | Seeded with the first sample so output starts near 0. Slow motion below ~1 Hz is partly treated as gravity; rotating the phone causes a brief transient |
| Magnitude | `sqrt(lx² + ly² + lz²) / 9.80665`, EMA-smoothed with τ = 0.05 s | In g |
| RMS | Unsmoothed linear magnitude over a 1.0 s sliding time window | In g |
| Peak | Max smoothed magnitude since the session started or `reset()`; ignores the first 0.6 s warm-up | Held until reset |
| Sample gap | A gap > 0.5 s (or a backwards timestamp) restarts the analyzer, including peak | Avoids filter jumps after stalls |
| Status (from RMS) | `NORMAL` < 0.03 g ≤ `VIBRATING` < 0.15 g ≤ `HIGH_VIBRATION` | **Uncalibrated**; tune on the S24 FE. No hysteresis |

### Merge validation — 2026-10-07

Ran `./gradlew :app:assembleDebug :app:testDebugUnitTest --tests 'com.motionx.app.sensors.VibrationAnalyzerTest' --tests 'com.motionx.app.viewmodel.MotionXViewModelTest' --no-daemon`: build succeeded, 14 tests passed. New integration tests verify preserving visual fields, bounded/throttled history, clearing stopped readings, rejecting old sensor sessions, and keeping visual monitoring active when the sensor is unavailable.

On SM-S721B / Android 16, the live screen showed physical magnitude 0.09 g, RMS 0.07 g, peak 0.91 g, raw axes, a `VIBRATING` state, and a changing-history trace. These are observed readings, not calibrated reference values. Sensor service confirmed a successful 10,000 µs registration. After backgrounding, MotionX had zero active sensor connections and the camera client list was empty. On return, the camera was ready, monitoring was stopped, and physical values were unavailable. At merge time the actual sample rate was not measured; Phase 3 later displayed an observed window rate (below). Numerical stationary noise floor and status thresholds remain unvalidated. The user later reported passing qualitative stationary settling and combined monitoring checks.

### Phase 2 acceptance and demo

- [x] Real sensor data updates current vibration, RMS, peak, axes, graph, and status live on the S24 FE.
- [x] Stationary readings settle after gravity suppression (user-reported 20-second desk check on 2026-10-07; no numerical results supplied).
- [ ] Quantify the stationary noise floor and calibrate thresholds on the target phone.
- [x] Shared monitoring starts both pipelines; background cancels collection, releases sensor/camera connections, and clears readings on return (short S24 FE check).
- [x] Missing-sensor state clears physical readings and preserves visual monitoring in a focused ViewModel test; absent-hardware registration failure has not been reproduced on a physical device.
- [x] Both streams continue without resetting while gently moving the phone and switching tabs (user-reported short check on 2026-10-07).
- [ ] Complete a longer rehearsed physical-device demo.

Demo: show both live streams while moving a marker and gently moving the phone. The accelerometer measures **the phone's motion**, not a remote object's vibration: moving only the marker should primarily affect the camera reading. Use a suitable mechanically coupled setup when comparing a common vibration source.

Demo narrative: “The camera tells us what we can see moving. The accelerometer tells us what the phone physically feels.” This is a prototype with uncalibrated thresholds, not a validated measurement instrument.

### User-reported functional acceptance — 2026-10-07

After receiving the three-step check, the user confirmed completing all steps and that the app was working:

1. Phone flat on a solid desk for 20 seconds: physical RMS settles near zero.
2. Phone held fixed while a black marker moves and is removed: displacement, visual graph, and tracking-loss behavior work.
3. Gentle phone movement and tab switching: both streams continue without resetting.

These are user-reported functional results, not independently observed measurements from this follow-up. No numerical readings, reference signal, or calibration data were supplied. No code, build, hardware check, or test suite was run for this documentation update. Preserve the passing checks; do not ask the user to repeat them without a specific regression or relevant change.

## Shared data contract and Phase 2 merge handoff

Data classes are in `app/src/main/java/com/motionx/app/model/`. Keep these definitions synchronized with the code. `MotionXUiState` starts with null readings to distinguish unavailable data from measured zero.

| Model | Fields | Units/meaning |
| --- | --- | --- |
| `VibrationData` | `accelerationX`, `accelerationY`, `accelerationZ` | Raw accelerometer axes in m/s², including gravity |
| `VibrationData` | `magnitude`, `rms`, `peak` | Gravity-suppressed vibration in g; convert m/s² using 9.80665 m/s² per g |
| `VibrationData` | `frequency` | Optional `FrequencyData` (default null for existing callers): status, nullable dominant Hz, observed samples/s, and conservative upper frequency limit |
| `VibrationData` | `status`, `timestamp` | `VibrationStatus` enum; `SensorEvent.timestamp` in nanoseconds since boot |
| `VisualMotionData` | `x`, `y`, `displacement` | Full unrotated camera-buffer pixels; displacement is distance from the first valid position in the current tracking segment |
| `VisualMotionData` | `timestamp`, `isTracking` | CameraX `ImageInfo.timestamp` in nanoseconds; clock alignment is unverified; validity flag |

The ViewModel exposes both live streams while monitoring; `vibration` is null while stopped or unavailable. Do not assume camera and sensor timestamps share a clock without verification. Phase 2 compares the readings side by side; pixel displacement and acceleration in g are different quantities and should not be presented as equivalent measurements.

### Integrated producer contract

- `AccelerometerSource(context).readings(): Flow<VibrationData>` is the actual API (replacing the earlier suggested start/stop API). Each collector owns a fresh analyzer and sensor thread; cancel collection to stop. `isAvailable` reports hardware availability, and failures propagate through the flow.
- `MotionXRoute` owns the single collection tied to monitoring/session/foreground state. `MotionXViewModel.onVibration(reading, session)` merges data atomically without replacing visual fields. `monitoringSession` prevents late results from an old collector contaminating a new run.
- Raw X/Y/Z remain m/s² including gravity; magnitude/RMS/peak are gravity-suppressed g, with the branch’s filter and peak semantics preserved. Neither model changed during the Phase 2 merge. Phase 3 appends the default-null `frequency` field to `VibrationData`; existing constructor calls remain source-compatible.
- Sensor failures clear physical readings and leave visual monitoring active. A camera failure stops the shared monitoring session. On background both pipelines stop; returning restores camera preview, and monitoring requires Start again.
- Timestamp clock alignment between camera and sensors is still unverified. Each graph uses its own producer's timestamps; no cross-sensor correlation is implied. Phase 3 frequency uses only the accelerometer timestamps, separately from both history graphs.

## Tabbed UI

Phase 0 provides the shell. Phase 1 activates the camera and visual readings. Phase 2 adds physical readings, vibration history, and vibration status. Keep unimplemented measurements unavailable rather than displaying sample values as real data.

- App name and tagline at the top.
- **Visual:** live camera preview and tracking overlay, marker instructions, displacement in px, visual graph, and marker X/Y coordinates.
- **Physical:** current vibration in g, dominant frequency in Hz and observed sample rate, RMS/peak, physical status and thresholds, raw axes, and physical graph.
- **Guide:** scrollable explanations for all measurements, units, graphs, filters, and resets.
- Pinned tabs and shared status plus **START MONITORING / STOP MONITORING** controls. Start still requires a ready camera; the footer directs users to Visual for permission/error recovery.
- `HorizontalPager` retains all three pages (`beyondViewportPageCount = 2`) so switching tabs does not dispose `CameraPreview` or reset its analyzer. Keep this behavior when changing navigation; backgrounding still releases both pipelines through the existing lifecycle controls.

Use a dark background, subtle rounded cards, large readable numbers, one primary accent color, minimal shadows, and restrained animation. Use neutral/green for normal, amber for warning, and red for high vibration. Show permission, tracking-loss, and sensor availability states clearly. Keep the style like a professional engineering instrument.

### Measurement explanations in the app

Short descriptions appear directly on measurement cards. The Physical tab explains **1 g ≈ 9.81 m/s²**, gravity removal, raw axes including gravity, and the RMS status boundaries. The Guide tab contains the full scrollable **Measurement guide** without changing monitoring state.

- Visual displacement and marker coordinates use full unrotated camera-buffer pixels, not millimeters or display pixels. Tracking loss resets the reference.
- Physical magnitude is smoothed gravity-suppressed acceleration; RMS uses unsmoothed magnitudes over the configured window; peak holds the highest smoothed value after warm-up. RMS can therefore exceed the displayed peak.
- Raw axes are phone-fixed m/s² including gravity; the guide describes their directions and the approximate face-up stationary reading.
- The guide explains separate graph units/timestamps, automatic scales, missing data, rounding, requested sampling versus UI refresh, filter settings, and reset behavior. Dominant physical frequency is an estimate; no visual frequency or calibrated distance is claimed.
- Thresholds, RMS window, warm-up, filter constants, and reset gap shown in the guide come from `VibrationAnalyzer.Config()` defaults; requested sampling comes from `AccelerometerSource.SAMPLING_PERIOD_US`. The route currently uses those same defaults. If configurable sessions are introduced, pass the active configuration to the guide as well. UI refresh/graph descriptions must also stay synchronized if those policies change.
- Thresholds remain explicitly labeled uncalibrated prototype values, not safety limits.

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
      sensors/                 # VibrationAnalyzer, FrequencyAnalyzer, AccelerometerSource
      model/
        MotionXUiState.kt
        VibrationData.kt
        VisualMotionData.kt
      ui/
        MainScreen.kt          # Screen and Compose preview
        MeasurementGuide.kt    # Scrollable parameter explanations and status boundaries
        MotionXRoute.kt        # Permission, lifecycle, sensor collection
        VibrationGraph.kt      # Bounded physical-vibration trace
        VisualMotionGraph.kt   # Visual displacement trace with tracking gaps
        Components.kt
        theme/Theme.kt
      viewmodel/MotionXViewModel.kt
  src/test/java/com/motionx/app/
    sensors/VibrationAnalyzerTest.kt  # JVM unit tests for the analyzer
```

| Owner | Responsibilities | Phase deliverables |
| --- | --- | --- |
| Developer 1: Android/UI/camera | Setup, Compose/Material 3, camera permission, preview/tracking, ViewModel and UI integration | Phase 0: installed app shell; Phase 1: working visual motion; Phase 2: physical metrics/graph integration |
| Developer 2: sensor/algorithm | Independently implement SensorManager, filtering, magnitude/RMS/peak, thresholds, and sensor tests | Phase 2: merged sensor pipeline; next, threshold/noise calibration |
| Both | Phase acceptance, shared interfaces, lifecycle checks, and device demo | Validate each pipeline, then validate their merged behavior |

In Phase 2, Developer 2 should focus on sensor logic rather than UI. Coordinate shared model and ViewModel edits before changing interfaces.

## Build sequence and time budget

The original target is a 90-minute hackathon build. Phase 0 and the parallel pipeline implementation/merge are complete. Basic marker, stationary, and combined-tab checks have now been reported passing by the user. Focus remaining validation on numerical accuracy and the outstanding edge cases, then rehearse the demo.

1. **Phase 0:** finish initialization, build/install, and physical-device launch validation.
2. **Parallel work:** Developer 1 implements and validates Phase 1 visual motion; Developer 2 independently implements and tests Phase 2 sensor analysis.
3. **Merge:** integrate Phase 2 into the shared UI state and controls, then test both pipelines together.
4. **Final 15 minutes of the session:** freeze features, fix issues, polish, and rehearse the last stable phase. If time runs out after Phase 1, demo visual motion and leave Phase 2 explicitly pending.

## Phase 3 — Dominant vibration frequency

Implemented in `sensors/FrequencyAnalyzer.kt`, called by `VibrationAnalyzer` on the accelerometer thread for every sample, before flow conflation and ViewModel throttling. Input is signed, gravity-suppressed X/Y/Z in g. Avoid using magnitude: rectifying a single-axis sine can double its apparent frequency. Existing magnitude/RMS/peak/status formulas are unchanged.

- Retain a rolling 2-second window with one preceding sample for interpolation, bounded to 1,024 samples. Recompute at most every 0.5 seconds.
- Linearly resample by actual timestamps to 256 uniform points, remove each axis mean, apply a Hann window, compute a bounded direct DFT, and sum axis powers. Shared trigonometric tables avoid repeated trig work. This is a DFT, not an FFT implementation; no new dependency is needed.
- Report the strongest bin in 0.5 Hz steps, with a nominal 2–40 Hz range. Upper limit is `min(40, 0.4 / largest sample interval in seconds)` for a conservative margin below Nyquist; observed sample rate is interval count divided by elapsed time. Resampling does not create additional sensor bandwidth.
- Require centered vector RMS of at least 0.005 g and at least 60% of non-DC spectral power in the strongest bin plus its two neighbors. The strongest bin must lie in the supported range. These quality gates are prototype defaults, not calibrated confidence probabilities.
- States: `COLLECTING` until a full window, `LOW_SIGNAL` for weak motion, `NO_CLEAR_PEAK` for ambiguous/out-of-range dominant content, and `READY` with Hz. Only READY contains a frequency. Current window estimates may take about 2 seconds to respond fully when motion changes.
- Non-increasing timestamps, non-finite frequency inputs, or sensor gaps over 50 ms reset frequency collection. This is intentionally stricter than the existing 0.5-second reset of the amplitude analyzer; a frequency-only reset does not erase amplitude peak. A normal analyzer/session reset clears both. Stop/background/sensor failure clears the enclosing reading through existing state handling.
- `VibrationData.frequency` defaults to null for source compatibility. `FrequencyData` includes `status`, nullable `hz`, `sampleRateHz`, and `upperLimitHz`. Histories may carry this metadata but continue plotting magnitude only. Camera interfaces are unchanged.
- Physical shows a one-decimal Hz card, quality message, actual sample rate, and current upper range. Guide explains Hz versus g, sampling versus vibration frequency, and limitations.

Limitations: strongest component is not necessarily the fundamental or motor RPM. Tilts, transients, harmonics, and mixed signals can affect results. Above-band signals can alias into plausible lower frequencies; the software range cap cannot replace hardware anti-alias filtering. No camera-frequency analysis or spectrum chart is included. Hardware accuracy remains unverified against a reference.

Signal-processing background: [NI: FFTs and windowing](https://www.ni.com/en/shop/data-acquisition/measurement-fundamentals/analog-fundamentals/understanding-ffts-and-windowing.html). The window, thresholds, and range policies above are MotionX implementation choices.

Validation: `./gradlew :app:assembleDebug :app:testDebugUnitTest --tests 'com.motionx.app.sensors.*' --tests 'com.motionx.app.viewmodel.MotionXViewModelTest' --no-daemon` passed. 26 tests cover signed-axis frequency (including off-bin and range endpoints), axes/DC, actual rate/jitter, weak signals, noise/competing tones, out-of-range peaks, invalid timestamps/gaps, invalid samples, stop-of-motion clearing, integration through the amplitude analyzer, and existing state/lifecycle contracts. Camera tests and lint were not rerun. Installed and launched on SM-S721B / Android 16. Screenshot showed real physical readings, camera TRACKING, the no-clear-frequency state, an observed rate of 124.7 samples/s, and a 2–40 Hz range. Stopping returned the screen to idle, cleared amplitude/history, and restored the frequency start prompt. No AndroidRuntime errors were returned during this brief check. This verifies integration and unavailable-state rendering, not the accuracy of a positive Hz result. No known-frequency source comparison performed.

## Threshold alerts — Visual and Physical

Implemented as an addition to the two existing measurement tabs; no microphone or gyro pipeline was added.

- Controls below each graph enable that channel, edit/apply its threshold, and independently select sound and phone vibration. Defaults: disabled, 10 px for Visual / 0.1 g for Physical, both feedback options on when enabled. Allowed thresholds: positive finite values up to 10,000 px / 100 g. Invalid edits do not replace the applied value. The graph includes a dashed threshold line while enabled and data is available; its scale includes that threshold.
- Visual compares valid tracked displacement; Physical compares the smoothed magnitude plotted on its graph, **not RMS, peak, or Hz**. Existing NORMAL/VIBRATING/HIGH classifications still use the original RMS thresholds. User-set alert levels are not calibrated safety limits.
- `MotionXUiState.visualAlert` / `physicalAlert` hold `ThresholdAlertSettings`; `setAlert(channel, settings)` validates updates without replacing readings. Settings survive tab switches, start/stop, and configuration changes through the ViewModel, but are not persisted across process death.
- `ThresholdAlertEngine` evaluates published samples (camera up to 15/s, physical up to 10/s). Very short peaks between published readings can be missed; this is not a full-rate impact alarm. Missing/invalid readings cannot fire or rearm. Producer timestamps identify fresh samples; cooldown uses elapsed realtime and never compares camera/sensor clocks.
- Reaching/exceeding the limit triggers once. Repeated high readings do not repeat. Rearm requires a fresh reading **below 90%** of the threshold, after the shared 3-second cooldown. Settings changes skip the existing reading; subsequent fresh samples use the new setting. Simultaneous crossings share one feedback event and identify both channels.
- A beep or haptic alert disarms both channels because either output may mechanically affect the phone/camera. Both must independently see a valid low reading after cooldown to rearm. Crossings during cooldown are discarded, not delayed. This policy reduces feedback loops; it does **not** clean contaminated graph/RMS/peak/Hz data. Disable feedback for clean measurements. Both output toggles off still allows the visual last-alert indication.
- `AlertFeedback` requests three 400 ms notification-stream warning-buzzer bursts with 120 ms gaps, and/or a 120 ms vibration, following device sound/vibration settings; it does not raise volume or bypass silent/DND policies. `VIBRATE` is a normal manifest permission; no new runtime permission prompt. Failure or unavailable hardware does not stop monitoring. The last-alert message confirms a threshold event, not guaranteed audible/tactile delivery.
- Route collection is foreground-only and independent of the selected tab. Stop, camera failure, and backgrounding clear alert state and cancel feedback; disposal releases tone resources. Stop also removes delayed buzzer callbacks so later pulses cannot play after cancellation. Sensor failure leaves valid visual alerts available. No background service, notification, or audio recording is introduced.
- Guide includes the policy and measurement-interference caveat. `alerts/` owns the pure policy and Android feedback adapter; sensor formulas/contracts remain unchanged.

Validation: build and 19 focused tests passed (10 alert-policy + 9 ViewModel). Coverage includes equality, repeated highs, hysteresis, global feedback/cooldown suppression, missing/duplicate/backwards samples, settings edits, independent output choices, session reset, validation, and preservation of readings/settings. Existing sensor algorithms, camera tests, and lint were not rerun. On SM-S721B / Android 16, installed and launched successfully; inspected the controls, default-disabled switch, threshold editor, and feedback switches. A real Physical crossing at the applied 0.1 g threshold produced the shared last-alert indicator and dashed graph line. Android vibrator service recorded completed 120 ms MotionX effects; no AndroidRuntime errors were returned during the check. Audible beep delivery, Visual marker-triggered feedback, and silent/DND edge cases have not been independently confirmed. Restarted the app after the check to clear the temporary threshold draft and restore disabled defaults; no test threshold was persisted.

Buzzer implementation uses `ToneGenerator.TONE_PROP_NACK` (300/400/500 Hz combined) at the existing 70% generator volume, scheduled at 0, 520, and 1,040 ms on the main handler. Each pulse rechecks ringer mode; playback does not change system volume. No new sound asset, dependency, or permission is needed.

Platform references: [Android haptic APIs](https://developer.android.com/develop/ui/views/haptics/haptics-apis), [ToneGenerator](https://developer.android.com/reference/android/media/ToneGenerator).

### Alert sound troubleshooting — 2026-10-07

When the user reported no sound, a read-only audio-service check showed the notification stream muted with effective volume 0, while media was unmuted at 9/15. MotionX uses notification volume, so media playback working does not establish that the beep is audible. Later checks in the same diagnosis showed internal/external ringer mode NORMAL, DND off, and notification audio unmuted at 8/15 on the speaker. No phone settings or playback code were changed by the assistant during this diagnosis, and audible playback was not independently confirmed.

For a beep, enable the channel and its **Play warning buzzer** switch, use Sound mode and nonzero **Notifications** volume. After changing phone sound settings, allow the 3-second cooldown to expire, bring the reading below 90% of its alert threshold, then cross it again (or restart monitoring). A previously suppressed beep is not replayed merely by unmuting the phone. No build or test rerun was needed for this documentation-only diagnosis.

## Later optional enhancements

Visual motion amplification and baseline-based anomaly detection remain unimplemented. Validate Phase 3 against a known-frequency source before expanding scope. If amplification is added, label it clearly and retain actual measured values.
