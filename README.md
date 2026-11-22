# TRAKUNA – Android Scan-Zentrale

TRAKUNA ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Funktionen im aktuellen Entwicklungsstand

- Kamera-Scan für gängige 1D-Barcodes und QR-/2D-Codes (Google ML Kit).
- Manuelle Eingabe von Sendungsnummern als Alternative zum Kamerascan.
- Erkennung einiger verbreiteter Formate für UPS, Österreichische Post, DHL, DPD und GLS. Die Erkennung ist eine Heuristik und muss mit echten Labels der jeweiligen Partner geprüft werden.
- Zwischenspeicherung der letzten Sendungsnummer und Kopie in die Zwischenablage.
- Auswahl einer installierten Betriebssoftware und Übergabeversuch per Android-Intent.
- Lokale Paketshop-Liste mit Eingang für Kunden, Annahme von Kundensendungen und Retouren.
- Statusfortschritt, Empfänger-/Notizfelder, Suche, Löschen einzelner Vorgänge und CSV-Text-Export.

## Bauen

Voraussetzungen: JDK 17 und Gradle 8.9.

```bash
gradle assembleDebug
```

APK-Ausgabe: `app/build/outputs/apk/debug/app-debug.apk`.

## Wichtige Grenzen – noch kein produktiver Paketdienstbetrieb

- Die Datenspeicherung erfolgt derzeit lokal in Android SharedPreferences (JSON), nicht in einer zentralen Datenbank. Es gibt keine Synchronisierung zwischen Geräten, Benutzerkonten, Rollen oder revisionssichere Audit-Protokolle.
- Die CSV-Funktion teilt den Export als Textinhalt über Android Sharesheet. Sie erstellt noch keine verwaltete Dateiablage oder automatische Datensicherung.
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
