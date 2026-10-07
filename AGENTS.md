# Repository instructions

## Project context

Read `README.md` before working. MotionX is a local-only Android hackathon prototype targeting the Samsung Galaxy S24 FE. Keep the app and project name MotionX. Phase 0 is complete. Per the user's updated plan, Developer 1 implements Phase 1 camera/visual motion while the fellow developer implements Phase 2 sensors independently; merge and validate both afterward. Keep sensor models stable and document integration contracts in the README. Use Kotlin, Compose, CameraX, and SensorManager as appropriate. Keep the architecture simple.

## Required developer/AI handoff

Update `README.md` in the same change whenever work changes implementation status, setup/build steps, shared interfaces, behavior, validation, or next steps. The user requires this documentation so fellow developers and their AI assistants can continue the project.

- Clearly separate implemented features from plans.
- Record relevant decisions, units, algorithm parameters, and interface contracts.
- Report only checks actually performed; explicitly list untested hardware behavior.
- Keep the current handoff date, known issues, and next concrete task accurate.
- Never commit secrets or machine-specific environment paths.

Use real camera/sensor readings. Handle permissions and lifecycle cleanup and keep frame processing off the main thread. Validate each pipeline and the merged behavior before declaring it complete. Optional enhancements come after the combined app is stable.

## Validation scope

The user prefers short, focused checks. Reuse passing build/lint/test results until a relevant change or failure warrants rerunning them. For the remaining Phase 1 acceptance, check the actual marker, displacement, and rotation on the phone; do not repeat the full suite or previously verified setup/lifecycle checks without a reason.
