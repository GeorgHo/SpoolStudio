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

- Add release documentation:
  - Version bump.
  - Android Studio Gradle sync.
  - Signed APK build.
  - GitHub release creation.
