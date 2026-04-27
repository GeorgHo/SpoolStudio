# Anpassungen für Spoolman + OpenSpool

Diese Version von Spool Studio basiert auf SpoolPainter und wurde für folgenden Workflow angepasst::

1. `variant` wird aus dem Spoolman-Filament-Extra-Feld `variant` gelesen.
2. `lot_nr` wird beim Schreiben auf den NFC-Tag übernommen und beim Anlegen einer neuen Spule in Spoolman gesetzt.
3. Beim Schreiben kann die App fehlende Datensätze in Spoolman anlegen:
   - Vendor
   - Filament
   - Spool
4. Material-, Marken- und Varianten-Auswahl kombiniert lokale Presets mit Werten aus Spoolman.

## Wichtige Annahmen

- Spoolman läuft ohne Authentifizierung oder mit offen erreichbarer API.
- Das Extra-Feld für Filamente heißt exakt `variant`.
- Für Spulen wird `lot_nr` sowohl im direkten Spool-Feld als auch zusätzlich im `extra`-Payload mitgegeben.
- Für neue Filamente werden Standardwerte für Dichte und Durchmesser gesetzt, wenn Spoolman diese beim Erstellen verlangt.

## Hinweise

- Das Projekt wurde im Container umgebaut, aber hier konnte kein Gradle-Build ausgeführt werden, weil der Gradle-Download im Container keinen Internetzugang hatte.
- Falls deine Spoolman-API andere Pflichtfelder verlangt, müssen die Create-Requests ggf. noch leicht angepasst werden.
