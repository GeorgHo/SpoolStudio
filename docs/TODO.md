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
- Normalize remaining filament input:
  - Round or format to two decimal places.
- Add searchable or autocomplete selection fields for materials, variants, brands, and locations.
- Highlight low remaining filament values:
  - Thresholds: `150 g`, `100 g`, `50 g`.
  - Show warning color in the remaining filament field.
  - Show warning color in the Spool Info popup remaining percentage.
- Improve visibility and wording of Spoolman and RFID read/write feedback.
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
