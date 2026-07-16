# Spool Studio 2.0

## GitHub Release

Tag:

```text
v2.0
```

Title:

```text
Spool Studio 2.0
```

Release notes:

```markdown
## Highlights

- Complete visual redesign with the new Spool Studio v2 interface.
- Added a modern dark technical theme across the main screen, settings, startup/about screen, dialogs, dropdowns, RFID status overlays, printer mapping and Bambu dump views.
- Reworked the main workflow around existing spools, "New from selected" and fully new spool creation.
- Added a dedicated "New spool" flow for creating a completely new Spoolman spool from safe defaults.
- Added optional empty spool weight handling, including remembered brand/weight combinations.
- Added Spoolman delete support for selected spools.
- Added Spoolman catalog statistics in Settings.
- Improved RFID read/write status handling and user-facing NFC timeout messages.

## Spoolman

- Create new Spoolman spools from selected filament data or from an empty default state.
- Update existing Spoolman spools from the main form.
- Delete selected Spoolman spools from the app.
- Edit remaining filament weight, empty spool weight, location, lot number and comment.
- Add custom locations directly from the location selector.
- Add custom brands from the brand selector.
- Improved URL handling in Settings: bare addresses such as `10.201.0.1:8000` are normalized automatically.
- Added Settings overview for loaded spools, active/archived spools, brands, materials, locations and colors.

## RFID / NFC

- Read and write OpenSpool RFID tags.
- Improved NFC read/write waiting states and timeout feedback.
- Improved Bambu Lab RFID parsing and apply flow.
- Added a redesigned Bambu dump/debug view for easier inspection.
- Cleared transient fields correctly when applying Bambu tag data or creating a new spool from selected data.

## Printer Mapping

- Redesigned Snapmaker U1 / Moonraker printer mapping dialog.
- Improved active spool mapping workflow.
- Replaced raw error dumps with clearer user-facing error messages.
- Kept Printer Mapping as a direct main-screen action because it is used frequently.

## UI / Interaction

- Redesigned dropdowns with search, pinned defaults and clearer grouping.
- Improved color selection, color name recognition and HEX handling.
- Added clearer enabled/disabled button states.
- Added low-filament warning colors and remaining percentage display.
- Added compact progress/status notifications for Spoolman, RFID and printer actions.
- Adjusted Android back-button behavior to avoid accidental app exits.
- Removed the old first splash screen and updated the remaining startup/about screen to the v2 style.

## Code Review / Refactoring

- Split the main ViewModel workflow into focused helpers and use cases.
- Extracted screen sections, form state, form loaders, action buttons, header, effects and settings helpers into smaller files.
- Added reusable v2 design tokens/components for the new visual system.
- Added or extended unit tests for Spoolman save/update logic, settings helpers, form state, Bambu RFID handling, color recognition and spool mode behavior.
- Cleaned up deprecated APIs and several edge-case behaviors found during Android Studio and phone testing.

## Verification

- Debug build passed with `assembleDebug`.
- Android Studio test run passed.
- Real-device phone test passed before release preparation.
- Spoolman URL was verified with `10.201.0.1:8000`.

## Notes

- Signed APK creation is done manually in Android Studio.
- Create the GitHub release with tag `v2.0`.
- Upload the signed APK to the GitHub release.
```

## Local Release Checklist

- Sync Gradle after the version bump.
- Build signed APK in Android Studio.
- Install signed APK on the phone.
- Verify normal app startup.
- Verify Spoolman connection, spool selection, create/update/delete flow and remaining filament update.
- Verify OpenSpool RFID read/write on phone NFC hardware.
- Verify Bambu RFID read/apply flow on phone NFC hardware.
- Verify Printer Mapping against the repaired printer scripts.
- Create GitHub release with tag `v2.0`.
- Upload signed APK to the GitHub release.
