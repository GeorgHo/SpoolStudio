# SpoolStudio 1.2.7

## GitHub Release

Tag:

```text
v1.2.7
```

Title:

```text
SpoolStudio 1.2.7
```

Release notes:

```markdown
## Highlights

- Added editing and saving of remaining filament weight for Spoolman spools.
- Added support for applying parsed Bambu RFID data into the spool form.
- Improved Snapmaker U1 printer mapping dialog structure and active spool handling.
- Fixed display issues caused by broken special-character encoding in dialogs and mapping labels.

## Code Review / Refactoring

- Split the main screen into smaller components:
  - spool form card
  - action button section
  - validation message card
  - app header
  - Bambu RFID dialog host
  - printer mapping dialog host
- Moved form validation, request creation, OpenSpool tag creation, Bambu RFID parsing, and Bambu form application into focused helper/state classes.
- Replaced deprecated Material divider and dropdown anchor APIs.
- Removed noisy debug logs and replaced one forced default-material unwrap with a safer fallback.
- Added or extended unit coverage for remaining filament and Bambu RFID form application logic.

## Verification

- Gradle unit tests passed with `testDebugUnitTest`.
- Debug build passed with `assembleDebug`.
- Android Studio smoke tests passed during the refactoring steps.

## Notes

- Final Bambu RFID read/apply verification should be done on the phone with NFC hardware before publishing the GitHub release.
- Signed APK creation is done manually in Android Studio.
```

## Local Release Checklist

- Sync Gradle after the version bump.
- Build signed APK in Android Studio.
- Install signed APK on the phone.
- Verify normal app startup.
- Verify Spoolman selection, remaining filament update, and printer mapping.
- Verify Bambu RFID read/apply flow on phone NFC hardware.
- Create GitHub release with tag `v1.2.7`.
- Upload signed APK to the GitHub release.
