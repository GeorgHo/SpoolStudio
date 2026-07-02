# SpoolStudio TODO

## Refactoring first

- Split `SpoolStudioScreen.kt` into smaller composables.
- Introduce a `SpoolFormState` model for form values, validation, and request creation.
- Move Spoolman save validation and formatting out of the screen and view model where practical.
- Introduce a `SpoolmanRepository` between `MainViewModel` and `SpoolmanService`.
- Add use-case classes for critical flows:
  - Save or update Spoolman spool.
  - Write OpenSpool tag data.
  - Apply Bambu RFID data.
- Add unit tests for:
  - Remaining weight parsing and formatting.
  - OpenSpool material mapping.
  - Spoolman create/update decisions.
  - Bambu RFID parsing and data application.

## Features after structure cleanup

- Add a clear Spoolman sync status for the selected spool.
- Make action buttons state-aware:
  - Apply this to every action button where usage prerequisites can be determined reliably.
  - Enable Spoolman update only when form values changed.
  - Enable RFID write actions only when valid tag data is available and write mode makes sense.
  - Keep disabled states visually clear so users understand which action is currently possible.
- Add release documentation:
  - Version bump.
  - Android Studio Gradle sync.
  - Signed APK build.
  - GitHub release creation.
