# BlockCraft

A native Android voxel sandbox built with Java and OpenGL ES 2.0.

## Build
Open this repository in Android Studio, or run:
`./gradlew assembleDebug`

The GitHub Actions workflow builds a debug APK and publishes it as an artifact on every push and pull request. A tagged release also produces a signed-by-GitHub unsigned APK asset (Android APKs do not require signing for local testing).

## Current game systems
- Landscape orientation and immersive fullscreen
- Procedurally generated block terrain and trees
- OpenGL ES 2.0 rendering with face culling at mesh generation
- On-screen movement, look, jump, break and place controls
- Android API 23+ support

## Performance notes
The world is rendered as a single generated mesh rather than issuing one draw call per block. The world is intentionally bounded to keep memory and GPU use predictable on entry-level Android devices.

## Roadmap
Persistent worlds, chunk streaming, inventory/hotbar, crafting, survival progression, audio, and expanded block types are planned follow-on systems. They are not represented as completed features yet.
