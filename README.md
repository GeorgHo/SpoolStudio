# Spool Studio

Spool Studio is an Android app for managing 3D printer filament spools using NFC and Spoolman integration.

This project is based on the open-source project **SpoolPainter** by ni4223 and has been significantly extended.

## Features

- NFC tag read/write (OpenSpool format)
- Spoolman integration
- Filament management (material, color, temperature, brand)
- Printer mapping (Moonraker / Klipper)
- Direct integration with Snapmaker U1 (paxx12 Extended Firmware)
- Read and write active spools via Moonraker API
- Synchronization between app, printer and Spoolman
- Bambu Lab RFID tag support (extended functionality)

## Printer Integration

Spool Studio supports direct communication with a Klipper-based printer via Moonraker.

Tested setup:
- Snapmaker U1 with paxx12 Extended Firmware
- Moonraker API
- Fluidd interface
- Spoolman backend

The app allows:
- Reading current toolhead ↔ spool assignments
- Updating spool mappings (E0–E3 / Toolhead 1–4)
- Synchronizing active spool information
- Preparing integration with RFID-based spool detection

This enables a seamless workflow between:
App ↔ Printer ↔ Spoolman

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- NFC API
- Coroutines
- MVVM architecture
- Moonraker API
- Klipper ecosystem

## Credits

- Original project: https://github.com/ni4223/SpoolPainter
- Original developer: ni4223
- This version: extended and maintained by Hovi (unofficial)

## License

This project is based on the original SpoolPainter project by ni4223.

No license was provided in the original repository.  
All rights remain with the original author.

This repository contains modifications and extensions for personal and educational use.

## Disclaimer

This is an unofficial project and is not affiliated with the original developer.
This project is tested with Snapmaker U1 but is not affiliated with Snapmaker.

## Requirements

- Android device with NFC
- Android API 21+