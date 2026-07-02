# SpoolStudio TODO

## Refactoring first

- Continue reducing `SpoolStudioScreen.kt` where cohesive sections can be moved without behavior risk.
- Continue reducing `MainViewModel.kt` by moving remaining workflows into focused helpers/use cases:
  - Settings load/save state handling.
  - Spoolman catalog refresh state handling.
- Add or extend unit tests for remaining extracted workflows.

## Release

- Add release documentation:
  - Version bump.
  - Android Studio Gradle sync.
  - Signed APK build.
  - GitHub release creation.
