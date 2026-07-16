# Spool Studio UI Design v2

This document defines the target design before implementation.

## Screen states

The final main screen has two primary states and one secondary creation flow:

1. Existing spool loaded
   - A spool from Spoolman is selected.
   - The selector shows the selected spool name and `ID #...`.
   - Spoolman action label is `Update Spoolman`.
   - The `New from selected` action is available.

2. New spool from selected
   - The user starts a new spool from the selected material data.
   - The selector shows `New spool from selected` and a `New` pill.
   - No Spoolman ID is shown yet.
   - Spoolman action label is `Create in Spoolman`.

3. Completely new spool
   - The user starts without an existing spool as a source.
   - Entry point: main-screen action `New spool`.
   - Defaults: `PLA`, `Basic`, `Generic`, remaining filament `1000 g`, comment `Created by Spool Studio`.
   - Required before writing: filament type, variant, brand, color.
   - Visual state is the same as `New spool from selected`, but the selector/title text becomes `New empty spool`.

The older compact two-column data-card idea is discarded for this version. The main editable data areas must be stacked vertically, full width.

## Locked layout rules

- Header: no hamburger/menu is needed. `Spool Studio` stays centered, settings/config is opened with the gear icon on the right.
- Top selector: full-width, compact, dark outlined field.
- Hero: large spool preview on the left, compact status panel on the right.
- Hero status panel: fixed height in all states to avoid visual jumping when switching between existing and new spool mode.
- Existing spool status panel: does not repeat the ID row, because the selected ID is already visible in the selector pill. It shows first use, last use, initial weight, remaining weight, remaining percentage, and the fill bar from the former info page.
- New spool status panel: same height as the existing spool status panel.
- Filament data: full-width light card, rows stacked vertically.
- Optional fields: location, lot, comment, and Spoolman registration date can be shown/hidden from settings. When enabled, they appear in the first light data card.
- Comment text is detail text: normal weight and small enough that `Created by Spool Studio` fits on one line.
- Weight units are displayed directly after the number, for example `60 g`, not in a far-right suffix column.
- Temperature data: full-width light card, rows stacked vertically, but more compact than the filament card.
- Actions: stacked as in the mockup, not inside a dark card.
- No side-by-side Filament/Spool weight/Temperature/RFID cards in this version.
- Use the existing app spool icon/vector for the spool preview, not a newly generated spool drawing.
- Use the Sora font family already included in the app.
- Date/time in the info panel uses German format with the time below the date: `16.06.2026` and `10:34 Uhr`.
- The color row uses a proper edit icon on the right, not the text `edit`.
- `New from selected` sits next to `New spool` below `Update Spoolman` and `Write RFID` in update mode.
- `Printer Mapping` is an important frequent action and stays visible on the main screen. It must be visually distinct from Spoolman/RFID actions, because it belongs to printer/Moonraker state, not Spoolman data.
- The `Printer Mapping` button may be slightly slimmer than the primary action buttons and should use a neutral/dark outlined style with a small printer/mapping icon.
- Buttons and light cards should have subtle depth; avoid completely flat rectangles.

## Dropdowns

Dropdowns can and should be styled. They must follow the same dark technical style as the selector and status area:

- Popup background: dark panel, subtle border, small radius.
- Width: aligned with the opening field where possible.
- Search field: compact and inset, not edge-to-edge.
- Rows: dense but readable, approximately 44-48 dp high.
- Selected row: subtle accent border/background, not a large highlight block.
- Dividers: only for meaningful groups such as pinned defaults (`PLA`, `Basic`, `Generic`) above the rest.
- Color dropdown rows show the color dot, name, and optional HEX value.
- The right side of a row uses icons where useful, not text labels such as `edit`.

## Dialogs And Overlays

Color wheel, RFID read/write dialogs, Spoolman progress/error dialogs, and confirmation overlays use the same visual language:

- Dark rounded panel over a dimmed background.
- Header text in Sora, clear but not oversized.
- Primary action in cyan or gold depending on context.
- Secondary/cancel action as outlined button.
- Status messages use the newer compact notification style already liked in the app.
- Error dialogs show a short readable message first; technical details are hidden unless explicitly expanded.

## Settings / Config

Settings should be redesigned in the same v2 style instead of keeping the older form look:

- Dark app background, light settings cards only where they improve readability.
- Spoolman and Moonraker connection sections as separate cards.
- Spoolman section keeps the Spoolman URL input, sort option, and `Test Spoolman Connection`.
- Printer/Moonraker section keeps the printer/Moonraker URL input and `Test Moonraker Connection`.
- Bambu Lab section keeps the Bambu Lab Master Key input and validation/help text.
- Toggles for optional fields: lot, comment, location, and optional Spoolman registration date.
- Save button uses the same button system as the main screen and must not wrap text.

## Printer Mapping

Printer Mapping remains a dedicated screen/dialog because it is used often and has its own workflow. It should keep its current functional structure but adopt v2 styling:

- Dark rounded panel over dimmed app background.
- Connection status at the top, short and readable.
- Toolheads as repeated compact sections.
- Each toolhead keeps spool selector plus active-spool checkbox.
- Bottom actions follow the shared button system.
- Save is only enabled when something changed and the printer/Moonraker requirements are met.

## Bambu Lab Dump

The Bambu Lab dump/debug screen also adopts v2 styling:

- Dark technical background.
- Parsed important fields first, raw dump/details below.
- Long technical content should be in a scrollable monospace area.
- Copy/share actions use secondary outlined buttons.
- Error/warning state uses readable text first, raw data second.

## Startup / About

The old first startup screen is removed. The remaining about/start screen keeps the app icon and spool identity but adopts v2 styling:

- Dark technical background instead of the old light splash surface.
- Existing app icon and spool artwork stay.
- Version, credits, and attribution remain readable.
- The about/start screen must not feel like a separate older app.

## Interaction States

All v2 components need explicit states before implementation:

- Loading: compact status text or spinner inside the affected card/button, no full-screen blocking unless unavoidable.
- Disabled: lower contrast but still readable, consistent for every button type.
- Error: short readable message first, expandable technical details only where useful.
- Success: compact status notification style already used in the app.
- Offline/no URL: clear settings-focused message, not raw exceptions.

## Create mode transition

When the user taps `New from selected` or `New spool` in update mode, the main screen changes state without opening a separate screen:

1. Selector text changes to match the action:
   - `New spool from selected` for a copy based on the currently selected spool/material.
   - `New empty spool` for a completely new spool.
2. The hero status panel keeps the same fixed size. It changes to the new-spool status content, because showing the old first/last-use data would be misleading once no Spoolman ID is selected.
3. The update actions are replaced by create-mode actions:
   - `Create in Spoolman`
   - `Write RFID`
4. After successful creation in Spoolman, the screen switches back to existing-spool/update mode for the newly created ID.

The optional Spoolman registration date is not part of the compact hero status panel by default. The panel should stay calm and readable. If the API provides this value reliably, show it as an optional detail row in the first light data card.

## Info page

The separate spool info page is no longer required for the information currently shown there. Its useful data is visible on the main screen:

- ID in the selector pill.
- First use and last use in the status panel.
- Initial, remaining, percentage, and fill bar in the status panel.
- Location, lot, and comment in the first light data card when their settings are enabled.

## Completely new spool flow

1. User taps the main-screen action `New spool`.
2. App clears the selected Spoolman spool.
3. App keeps safe defaults: `PLA`, `Basic`, `Generic`, `1000 g`.
4. Selector shows `New empty spool` with a `New` pill.
5. User fills color and optionally location, lot number, comment.
6. `Create in Spoolman` becomes active once required fields are valid.
7. After successful creation, the screen switches to existing spool/update mode for the new Spoolman ID.

## Reference mockup

Open the deterministic browser mockup:

`docs/ui_design_v2_mockup.html`

Use this mockup as the visual source of truth for the Compose implementation.
