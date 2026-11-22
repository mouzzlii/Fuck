# TRAKUNA – Android Scan-Zentrale

TRAKUNA ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Funktionen im aktuellen Entwicklungsstand

- Kamera-Scan für gängige 1D-Barcodes und QR-/2D-Codes (Google ML Kit).
- Manuelle Eingabe von Sendungsnummern als Alternative zum Kamerascan.
- Erkennung einiger verbreiteter Formate für UPS, Österreichische Post, DHL, DPD und GLS. Die Erkennung ist eine Heuristik und muss mit echten Labels der jeweiligen Partner geprüft werden.
- Zwischenspeicherung der letzten Sendungsnummer und Kopie in die Zwischenablage.
- Auswahl einer installierten Betriebssoftware und Übergabeversuch per Android-Intent.
- Lokale Paketshop-Liste mit Eingang für Kunden, Annahme von Kundensendungen und Retouren.
- Statusfortschritt, Empfänger-/Notizfelder (nachträglich bearbeitbar), Suche und Löschen einzelner Vorgänge.
- Schutz vor unbemerktem Erreichen der lokalen Speichergrenze: maximal 500 Vorgänge; vor weiteren Einträgen wird ein CSV-Export und Aufräumen verlangt.
- Anzeige der belegten Speicherkapazität und verständlicher Hinweis bei Suchanfragen ohne Treffer.
- CSV-Export als echte Datei über den Android-Dateidialog (Speicherort frei wählbar).
- Vollständige lokale Datensicherung als versioniertes JSON über den Android-Dateidialog.
- Wiederherstellung mit Format-/Versions-/Datensatzprüfung, Prüfung der Vorgangsstatus, Schutz vor doppelten aktiven Vorgängen, Größenlimit für Sicherungsdateien (2 MiB) und ausdrücklicher Bestätigung; der vorhandene lokale Bestand wird beim Wiederherstellen ersetzt.
- Schreibvorgänge melden einen Speicherfehler, statt eine nicht bestätigte Speicherung als erfolgreich darzustellen.
- Strikte Statusübergänge je Vorgangsart, damit Retouren und ausgehende Sendungen nicht versehentlich vermischt werden.

## Praxis-Test

Der deutschsprachige [Praxis-Testplan](PRAxisTEST.md) enthält konkrete Prüfschritte für Scan, manuelle Eingabe, Paketshop-Abläufe, Bearbeitung, CSV-Export und Partnerübergabe.

## Bauen

Voraussetzungen: JDK 17 und Gradle 8.9.

```bash
gradle assembleDebug
```

APK-Ausgabe: `app/build/outputs/apk/debug/app-debug.apk`.

## Wichtige Grenzen – noch kein produktiver Paketdienstbetrieb

- Die Datenspeicherung erfolgt derzeit lokal in Android SharedPreferences (JSON), nicht in einer zentralen Datenbank. Es gibt keine Synchronisierung zwischen Geräten, Benutzerkonten, Rollen oder revisionssichere Audit-Protokolle.
- Der CSV-Export erstellt eine Datei am vom Nutzer gewählten Speicherort. Es gibt keine automatische oder verschlüsselte Cloud-Sicherung; die JSON-Datei muss manuell erstellt und sicher aufbewahrt werden.
- Datensicherungen enthalten Sendungsnummern, Empfängernamen und interne Notizen im Klartext. Die Datei muss wie ein vertrauliches Dokument geschützt und sicher gespeichert werden.
- Eine Partner-App wird nur geöffnet und mit Intent-Extras gestartet. Eine automatische Übernahme der Sendungsnummer funktioniert nur, wenn die Zielsoftware diese Extras oder eine passende API ausdrücklich unterstützt. TRAKUNA kann die Unterstützung fremder Apps nicht erzwingen.
- Carrier-Erkennung ist nicht verbindlich; Sendungsnummern müssen vor der Übergabe geprüft werden. Es bestehen keine direkten Tracking- oder Versanddienst-API-Verbindungen.
- Es fehlen noch Tests mit realen Paketetiketten, echten Betriebssoftware-Versionen, verschiedenen Android-Geräten sowie ein Datenschutz-/Sicherheitsaudit.

## Vor einem produktiven Einsatz erforderlich

1. Ziel-Betriebssoftware und deren dokumentierte Integrationsschnittstelle festlegen.
2. Datenschutz- und Sicherheitsanforderungen des Paketshops klären.
3. Backend mit Authentifizierung, Rollen, verschlüsselter Übertragung, zentraler Speicherung und Audit-Log ergänzen, falls mehrere Geräte/Benutzer benötigt werden.
4. Reale Scanner- und Paketlabeltests für alle eingesetzten Carrier durchführen.
5. Abnahme- und Wiederherstellungstests einschließlich Fehlerfällen durchführen.

**Status:** Entwicklungs-/Praxis-Testversion. Ein erfolgreicher Android-Build bestätigt nur, dass das APK gebaut wurde – nicht, dass alle Abläufe im Paketshop produktiv funktionieren.
