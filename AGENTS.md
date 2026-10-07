# Repository instructions

## Project context

Read `README.md` before working. MotionX is a local-only Android hackathon prototype targeting the Samsung Galaxy S24 FE. Keep the app and project name MotionX. Favor a compiling, reliable Phase 1 using Kotlin, Compose, CameraX, and SensorManager. Keep the architecture simple and follow the scope and developer split in the README.

## Required developer/AI handoff

Update `README.md` in the same change whenever work changes implementation status, setup/build steps, shared interfaces, behavior, validation, or next steps. The user requires this documentation so fellow developers and their AI assistants can continue the project.

- Clearly separate implemented features from plans.
- Record relevant decisions, units, algorithm parameters, and interface contracts.
- Report only checks actually performed; explicitly list untested hardware behavior.
- Keep the current handoff date, known issues, and next concrete task accurate.
- Never commit secrets or machine-specific environment paths.

Use real camera/sensor readings. Handle permissions and lifecycle cleanup, keep frame processing off the main thread, and prioritize Phase 1 stability before optional features.
