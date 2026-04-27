# Spool Studio

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)
![Unofficial](https://img.shields.io/badge/Project-Unofficial-orange)

Spool Studio is an Android app for managing 3D printer filament spools using NFC, Spoolman, and direct printer integration.

This project is based on the open-source project **SpoolPainter** by ni4223 and has been significantly extended.

---

## ✨ Features

* NFC tag read/write (OpenSpool format)
* Spoolman integration
* Filament management (material, color, temperature, brand)
* Printer mapping (Moonraker / Klipper)
* Direct integration with Snapmaker U1 (paxx12 Extended Firmware)
* Read and write active spools via Moonraker API
* Synchronization between app, printer and Spoolman
* Bambu Lab RFID tag support (extended functionality)

---

## 🖨️ Printer Integration

Spool Studio supports direct communication with a Klipper-based printer via Moonraker.

**Tested setup:**

* Snapmaker U1 with paxx12 Extended Firmware
* Moonraker API
* Fluidd interface
* Spoolman backend

**Capabilities:**

* Reading current toolhead ↔ spool assignments
* Updating spool mappings (E0–E3 / Toolhead 1–4)
* Synchronizing active spool information
* Preparing integration with RFID-based spool detection

➡️ Enables a seamless workflow between:
**App ↔ Printer ↔ Spoolman**

---

## 📸 Screenshots

### Main Screen

<p align="center">
  <img src="docs/images/01_main_1.png" width="250"/>
  <img src="docs/images/01_main_2.png" width="250"/>
</p>

### Spool Details

<p align="center">
  <img src="docs/images/04_additional_spool__info.png" width="250"/>
</p>

### Settings (Spoolman & Moonraker)

<p align="center">
  <img src="docs/images/02_settings.png" width="250"/>
</p>

### Spoolman Integration

<p align="center">
  <img src="docs/images/03_spoolmaker_selection.png" width="250"/>
  <img src="docs/images/03_spoolmaker_selected.png" width="250"/>
</p>

### Printer Mapping (Snapmaker U1 / Moonraker)

<p align="center">
  <img src="docs/images/05_snapmaker_u1_mapping_dialog.png" width="250"/>
  <img src="docs/images/06_snapmaker_u1_loaded_mapping.png" width="250"/>
</p>

---

## 🧰 Tech Stack

* Kotlin
* Jetpack Compose
* Material 3
* NFC API
* Coroutines
* MVVM architecture
* Moonraker API
* Klipper ecosystem

---

## 🙏 Credits

* Original project: https://github.com/ni4223/SpoolPainter
* Original developer: ni4223
* This version: extended and maintained by Hovi (unofficial)

---

## ⚖️ License

This project is based on the original SpoolPainter project by ni4223.

No license was provided in the original repository.
All rights remain with the original author.

This repository contains modifications and extensions for personal and educational use.

---

## ⚠️ Disclaimer

This is an unofficial project and is not affiliated with the original developer.
This project is tested with Snapmaker U1 but is not affiliated with Snapmaker.

### Bambu Lab RFID Notice

Bambu Lab RFID tags use proprietary encryption and access keys which are **not publicly available**.

* This project does **not provide any official keys**
* Any key usage must be supplied by the user
* The developer assumes **no responsibility** for:

    * misuse
    * reverse engineering
    * potential legal implications

Use this functionality at your own risk.

---

## 📱 Requirements

* Android device with NFC
* Android API 21+
