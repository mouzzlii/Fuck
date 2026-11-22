# TRAKUNA – Android Scan-Zentrale

TRAKUNA 1.20 ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Integrations- und Sicherheitsdokumentation

- [Partner-Schnittstelle und Abnahmekriterien](PARTNER_INTEGRATION.md): beschreibt den aktuellen Android-Übergabeversuch, den notwendigen Vertrag mit dem Softwareanbieter und die Tests für eine echte Integration.
- [Sicherheitsstatus und Produktionsfreigabe](SECURITY.md): dokumentiert umgesetzte Schutzmaßnahmen, bekannte Restrisiken und verbindliche Freigabekriterien.

## Neu in Version 1.19

- Lokale Paketdatensätze werden mit AES-256-GCM verschlüsselt; der Schlüssel wird im Android Keystore verwahrt.
- Vorhandene unverschlüsselte lokale Datensätze werden beim ersten Start kontrolliert migriert. Die alte Kopie wird erst zusammen mit dem erfolgreich gespeicherten verschlüsselten Bestand entfernt.
- Bei fehlgeschlagener Migration, beschädigten Chiffretexten oder nicht verfügbarem Schlüssel zeigt die App einen Speicherfehler, statt einen leeren Datenbestand vorzutäuschen.
- Verschlüsselte Speicherwerte und Klartext vor der Verschlüsselung sind größenbegrenzt, damit beschädigte oder übergroße lokale Einträge nicht unbegrenzt in den Speicher geladen werden.
- JSON- und CSV-Exporte bleiben bewusst separat: exportierte Dateien sind weiterhin unverschlüsselt und müssen geschützt aufbewahrt werden.

## Funktionen im aktuellen Entwicklungsstand

- Zebra DataWedge-Intent-Eingang für integrierte Zebra-Hardware-Scanner (nach einmaliger Profilkonfiguration; siehe PARTNER_INTEGRATION.md).
- Kamera-Scan für gängige 1D-Barcodes und QR-/2D-Codes (Google ML Kit).
- Manuelle Eingabe von Sendungsnummern als Alternative zum Kamerascan.
- Erkennung einiger verbreiteter Formate für UPS, Österreichische Post, DHL, DPD und GLS. Die Heuristik ist nur ein Hinweis und muss mit echten Labels geprüft werden; beim Erfassen kann der Paketdienst jetzt manuell eingetragen oder korrigiert werden.
- Zwischenspeicherung der letzten Sendungsnummer und Kopie in die Zwischenablage.
- Auswahl einer installierten Betriebssoftware (dauerhaft gespeichert, mit Möglichkeit zum Entfernen/Wechseln) und Übergabeversuch per Android-Intent; die lokale Zwischenspeicherung wird vor dem App-Wechsel bestätigt, ein nicht verfügbarer Clipboard-Dienst blockiert den Ablauf nicht und die Zwischenablage wird auf Android 13+ als sensibel markiert.
- Lokale Paketshop-Liste mit Eingang für Kunden, Annahme von Kundensendungen und Retouren.
- Statusfortschritt, Empfänger-/Notizfelder (nachträglich bearbeitbar), Suche und Löschen einzelner Vorgänge.
- Doppelte aktive Sendungen werden erkannt und ausdrücklich als bereits vorhanden gemeldet, statt fälschlich eine neue Speicherung zu behaupten.
- Schutz vor unbemerktem Erreichen der lokalen Speichergrenze: maximal 500 Vorgänge; vor weiteren Einträgen wird ein CSV-Export und Aufräumen verlangt.
- Anzeige der belegten Speicherkapazität und verständlicher Hinweis bei Suchanfragen ohne Treffer.
- CSV-Export als echte Datei über den Android-Dateidialog (Speicherort frei wählbar); Tabellenkalkulations-Formelanfänge in Nutzereingaben werden beim Export entschärft.
- Vollständige lokale Datensicherung als versioniertes JSON über den Android-Dateidialog.
- Wiederherstellung mit Format-/Versions-/Datensatzprüfung, Prüfung der Vorgangsstatus, Schutz vor doppelten aktiven Vorgängen, Größenlimit für Sicherungsdateien (2 MiB, bereits beim Einlesen begrenzt) und ausdrücklicher Bestätigung; der vorhandene lokale Bestand wird beim Wiederherstellen ersetzt.
- Lokale Datensätze werden vor der Anzeige auf gültige IDs, Sendungsnummern, Zeitstempel, Status, Feldlängen, Duplikate und Speichergrenzen geprüft; fehlerhafte Bestände werden ausdrücklich nicht als leer dargestellt.
- Schreibvorgänge melden einen Speicherfehler, statt eine nicht bestätigte Speicherung als erfolgreich darzustellen.
- Vor dem Export einer unverschlüsselten JSON-Sicherung warnt die App vor den enthaltenen Sendungsnummern, Empfängernamen und internen Notizen.
- Strikte Statusübergänge je Vorgangsart, damit Retouren und ausgehende Sendungen nicht versehentlich vermischt werden.

## Zebra DataWedge

- Zebra DataWedge: Barcodes können per konfiguriertem Android-Broadcast direkt in das Sendungsnummernfeld übernommen werden. Der Scan löst bewusst keine automatische Buchung aus; der Mitarbeiter prüft die Nummer und startet den nächsten Schritt selbst.
- Kamera-Scan und manuelle Eingabe bleiben als Alternativen erhalten.

- Paketdienst bei jedem Paketshop-Vorgang manuell eintragen oder die automatische Erkennung überschreiben.
- Doppelte aktive Vorgänge erhalten eine eigene Rückmeldung; die App meldet nicht länger irreführend eine neue Speicherung.
- Nach erfolgreicher Erfassung werden Empfänger, manuelle Paketdienst-Auswahl und Notiz geleert, damit keine Angaben versehentlich auf das nächste Paket übertragen werden. Das verhindert, dass eine unsichere Nummern-Heuristik unbemerkt als verbindliche Carrier-Zuordnung behandelt wird.

## Praxis-Test

Der deutschsprachige [Praxis-Testplan](PRAxisTEST.md) enthält konkrete Prüfschritte für Scan, manuelle Eingabe, Paketshop-Abläufe, Bearbeitung, CSV-Export und Partnerübergabe.

## Bauen

Voraussetzungen: JDK 17 und Gradle 8.9.

```bash
gradle assembleDebug
```

APK-Ausgabe: `app/build/outputs/apk/debug/app-debug.apk`.

## Wichtige Grenzen – noch kein produktiver Paketdienstbetrieb

- Die Datenspeicherung erfolgt lokal in Android SharedPreferences; die Paketdatensätze werden mit AES-256-GCM verschlüsselt und der Schlüssel liegt im Android Keystore. Es gibt weiterhin keine zentrale Datenbank. Die Sendungsnummer wird vor der Partnerübergabe synchron in den lokalen Cache geschrieben; bei einem Schreibfehler wird der Übergabeversuch gestoppt. Es gibt keine Synchronisierung zwischen Geräten, Benutzerkonten, Rollen oder revisionssichere Audit-Protokolle.
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
