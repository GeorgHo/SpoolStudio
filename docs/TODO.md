# SpoolStudio TODO

## Refactoring status

- MainViewModel workflow extraction is complete for the planned scope:
  - Settings load/save state handling.
  - Spoolman catalog state handling.
  - Connection tests.
  - Printer mapping.
  - Spoolman save/update.
  - NFC/Bambu/OpenSpool read/write helpers.
- Screen/component extraction is complete for the planned scope:
  - SpoolStudio effects and derived state.
  - Settings helpers/components.
  - Color selector helpers, color wheel, photo helpers and photo dialog.
- Unit tests were added or extended for the extracted non-UI logic.

## Optional structure cleanup

- Keep reducing large UI files only when a cohesive component can be extracted without behavior risk:
  - `ColorSelector.kt`
  - `SpoolInfoCard.kt`
  - `SpoolStudioScreen.kt`
  - `PrinterMappingDialog.kt`

## Features

- Add search/autocomplete support to remaining selection fields where list size makes scrolling awkward.
- Review low-filament warning colors after real device testing; consider two levels such as yellow and red.
- Review snackbar/status display duration and placement after real device testing.
- Make any remaining action buttons disabled when their preconditions are not met.

## Release

- Add release documentation:
  - Version bump.
  - Android Studio Gradle sync.
  - Signed APK build.
  - GitHub release creation.
