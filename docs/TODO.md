# SpoolStudio TODO

## Refactoring first

- Continue reducing `SpoolStudioScreen.kt` where cohesive sections can be moved without behavior risk.
- Continue reducing `MainViewModel.kt` by moving remaining workflows into focused helpers/use cases:
  - NFC tag read state handling.
  - Printer mapping load/save flow.
  - Connection test state handling.
- Add or extend unit tests for:
  - NFC tag read state handling.
  - Printer mapping load/save flow.
  - Connection test error mapping.

## Release

- Add release documentation:
  - Version bump.
  - Android Studio Gradle sync.
  - Signed APK build.
  - GitHub release creation.
