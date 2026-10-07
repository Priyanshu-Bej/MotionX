# Repository instructions

## Project context

Read `README.md` before working. MotionX is a local-only Android hackathon prototype targeting the Samsung Galaxy S24 FE. Keep the app and project name MotionX. Follow the phase order: Phase 0 is project setup, build, installation, and physical-device launch; Phase 1 is camera/visual motion implementation; Phase 2 is physical sensor implementation and integration. Use Kotlin, Compose, CameraX, and SensorManager as appropriate to each phase. Keep the architecture simple and follow the scope and developer split in the README.

## Required developer/AI handoff

Update `README.md` in the same change whenever work changes implementation status, setup/build steps, shared interfaces, behavior, validation, or next steps. The user requires this documentation so fellow developers and their AI assistants can continue the project.

- Clearly separate implemented features from plans.
- Record relevant decisions, units, algorithm parameters, and interface contracts.
- Report only checks actually performed; explicitly list untested hardware behavior.
- Keep the current handoff date, known issues, and next concrete task accurate.
- Never commit secrets or machine-specific environment paths.

Use real camera/sensor readings. Handle permissions and lifecycle cleanup, keep frame processing off the main thread, and complete each phase's acceptance checks before starting the next. Optional enhancements come after Phase 2 is stable.
