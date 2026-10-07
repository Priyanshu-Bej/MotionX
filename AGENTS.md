# Repository instructions

## Project context

Read `README.md` before working. MotionX is a local-only Android hackathon prototype targeting the Samsung Galaxy S24 FE. Keep the app and project name MotionX. Phase 0 is complete. Phase 1 camera work and the fellow developer's Phase 2 accelerometer branch are now integrated on develop. Phase 3 adds physical dominant-frequency estimation; known-frequency hardware validation is pending in README. Keep sensor models source-compatible and preserve both pipelines when updating shared state or lifecycle controls. User-reported basic marker, stationary, and combined-tab checks passed; remaining edge cases and measurement calibration are documented in the README. Use Kotlin, Compose, CameraX, and SensorManager as appropriate. Keep the architecture simple.

## Required developer/AI handoff

Update `README.md` in the same change whenever work changes implementation status, setup/build steps, shared interfaces, behavior, validation, or next steps. The user requires this documentation so fellow developers and their AI assistants can continue the project.

- Clearly separate implemented features from plans.
- Record relevant decisions, units, algorithm parameters, and interface contracts.
- Report only checks actually performed; explicitly list untested hardware behavior.
- Keep the current handoff date, known issues, and next concrete task accurate.
- Never commit secrets or machine-specific environment paths.

Use real camera/sensor readings. Handle permissions and lifecycle cleanup and keep frame processing off the main thread. Validate each pipeline and the merged behavior before declaring it complete. Optional enhancements come after the combined app is stable.

## Validation scope

The user prefers short, focused checks. Reuse passing build/lint/test results until a relevant change or failure warrants rerunning them. For integration changes, run affected sensor/ViewModel tests and a short combined-device check. The user reported passing the basic physical-marker, stationary settling, and combined tab-switch checks on 2026-10-07. Do not repeat these, the full suite, or previously verified setup/lifecycle checks without a relevant change or specific regression. Numerical calibration and rotation/permission edge cases remain pending.

## Threshold alerts

Visual alerts use displacement in px; Physical alerts use the plotted smoothed magnitude in g, not RMS. Preserve the shared 3-second cooldown and valid-low rearm policy when changing feedback: the phone’s own beep/vibration can affect both pipelines. Defaults are off; settings are session-independent but not persisted across process death. Cancel feedback on stop/background and keep evaluation independent of the selected tab.
