# SpoolStudio TODO

## Release 2.0 status

- Real-device phone test passed; Spool Studio 2.0 is ready for release preparation.

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

## Release

- Version is set to `2.0`.
- Release documentation is available in `docs/RELEASE_2.0.md`.
- Remaining manual steps: create the signed APK in Android Studio, create the GitHub release with tag `v2.0`, and upload the APK.
