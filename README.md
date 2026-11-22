# TRAKUNA – Android Scan-Zentrale

TRAKUNA 1.31 ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Integrations- und Sicherheitsdokumentation

- [Partner-Schnittstelle und Abnahmekriterien](PARTNER_INTEGRATION.md): beschreibt den aktuellen Android-Übergabeversuch, den notwendigen Vertrag mit dem Softwareanbieter und die Tests für eine echte Integration.
- [Sicherheitsstatus und Produktionsfreigabe](SECURITY.md): dokumentiert umgesetzte Schutzmaßnahmen, bekannte Restrisiken und verbindliche Freigabekriterien.

## Neu in Version 1.31

- Vor dem CSV-Export erscheint jetzt wie bei der JSON-Sicherung eine deutliche Warnung: Die Datei enthält Sendungsnummern, Empfängerdaten und Notizen und ist unverschlüsselt.
- Die Hauptoberfläche berücksichtigt sichere Display-Ränder und die Bildschirmtastatur, damit Inhalte auf neueren Android-Versionen und kleinen Displays nicht hinter Systemleisten oder Tastatur verschwinden.
- Die Auswahl installierter Betriebssoftware ist auf kleinen Bildschirmen scrollbar.

## Neu in Version 1.30

- In der Sendungsliste werden Erfassungszeitpunkt und letzte Änderung direkt angezeigt.
- CSV-Exporte enthalten Erfassungs- und Änderungszeitpunkte als eindeutige UTC-Zeitstempel im ISO-8601-Format.
- Empfänger-, Paketdienst-, Notiz- und Suchfelder begrenzen Eingaben bereits während der Eingabe auf ihre vorgesehenen Feldlängen.

## Neu in Version 1.29

- Zebra-Scans und Kamera-Barcodes werden vor der Auswertung auf eine maximale Eingabelänge begrenzt. Überlange Broadcast-Inhalte werden ausdrücklich abgelehnt.
- Die manuelle Sendungsnummerneingabe ist begrenzt, damit versehentlich eingefügte sehr lange Inhalte nicht unnötig verarbeitet werden.
- Der Hinweis bei beschädigtem lokalem Speicher erklärt jetzt korrekt, dass neue Vorgänge und Exporte gesperrt sind, eine Wiederherstellung aus einer Sicherung aber möglich bleibt.

## Neu in Version 1.28

- Statuswechsel werden vor dem Speichern bestätigt. Bei tatsächlichen Übergaben an Kunden oder Paketdienste erinnert die Bestätigung daran, erst nach der physischen Übergabe zu bestätigen.
- Die Sendungsliste zeigt mehr als 100 Treffer auf ausdrückliche Anforderung in weiteren Blöcken an; ältere Vorgänge bleiben damit erreichbar, ohne die Liste sofort vollständig aufzubauen.

## Neu in Version 1.27

- Wenn die Kamera mehrere unterschiedliche Sendungsnummern gleichzeitig erkennt, wählt TRAKUNA nicht mehr willkürlich eine davon aus. Der Scan wird abgebrochen und der Bediener wird zum einzelnen Scannen oder zur manuellen Eingabe aufgefordert.

## Neu in Version 1.26

- Bereits laufende Barcode-Erkennungen werden nach dem Schließen des Scanner-Bildschirms verworfen. Ein verspätetes Kamera-Ergebnis kann die Oberfläche nicht mehr nachträglich verändern.

## Neu in Version 1.25

- Der Kamera-Scanner beendet seine Analyse sauber, wenn der Scanner-Bildschirm geschlossen wird. Ein verzögert startender Kamera-Provider darf die Kamera nach dem Schließen nicht erneut an die Oberfläche binden.

## Neu in Version 1.24

- Eine geprüfte Datensicherung kann jetzt auch dann zur Wiederherstellung ausgewählt werden, wenn der lokale verschlüsselte Datenspeicher beschädigt ist. Die Wiederherstellung bleibt ausdrücklich bestätigungspflichtig und wird nur als erfolgreich gemeldet, wenn die verschlüsselten Daten dauerhaft gespeichert wurden.

## Neu in Version 1.23

- Kamera-Scans werden nur noch in das Sendungsnummernfeld übernommen. Eine Partner-App wird nicht mehr automatisch nach einem Scan geöffnet; vor der Übergabe kann der Bediener die Nummer prüfen.

## Neu in Version 1.22

- Der verschlüsselte Übergabe-Cache unterscheidet jetzt zwischen „kein gespeicherter Wert“ und einem Entschlüsselungs-/Lesefehler. Ein beschädigter Cache wird nicht mehr stillschweigend als leer ausgegeben.
- Fehler beim Speichern des Übergabe-Caches liefern eine konkrete Fehlermeldung; ein App-Wechsel wird weiterhin nur nach bestätigtem Speichern gestartet.

## Neu in Version 1.20

- Lokale Paketdatensätze werden mit AES-256-GCM verschlüsselt; der Schlüssel wird im Android Keystore verwahrt.
- Vorhandene unverschlüsselte lokale Datensätze werden beim ersten Start kontrolliert migriert. Die alte Kopie wird erst zusammen mit dem erfolgreich gespeicherten verschlüsselten Bestand entfernt.
- Bei fehlgeschlagener Migration, beschädigten Chiffretexten oder nicht verfügbarem Schlüssel zeigt die App einen Speicherfehler, statt einen leeren Datenbestand vorzutäuschen.
- Verschlüsselte Speicherwerte und Klartext vor der Verschlüsselung sind größenbegrenzt, damit beschädigte oder übergroße lokale Einträge nicht unbegrenzt in den Speicher geladen werden.
- Auch die zuletzt an die Partner-App übergebene Sendungsnummer und Paketdienstangabe werden separat mit AES-GCM und einem eigenen Android-Keystore-Schlüssel verschlüsselt; vorhandene Klartext-Cachewerte werden einmalig migriert.
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
- Kamera- und DataWedge-Scans übernehmen die Nummer zunächst nur zur Prüfung; es erfolgt keine automatische Partnerübergabe oder Buchung.
- Eine gültige Datensicherung kann nach ausdrücklicher Bestätigung auch bei beschädigtem lokalem Speicher wiederhergestellt werden.

## Praxis-Test

Der deutschsprachige [Praxis-Testplan](PRAxisTEST.md) enthält konkrete Prüfschritte für Scan, manuelle Eingabe, Paketshop-Abläufe, Bearbeitung, CSV-Export und Partnerübergabe.

## Bauen

Voraussetzungen: JDK 17 und Gradle 8.9.

```bash
gradle assembleDebug
```

APK-Ausgabe: `app/build/outputs/apk/debug/app-debug.apk`.

## Wichtige Grenzen – noch kein produktiver Paketdienstbetrieb

- Die Datenspeicherung erfolgt lokal in Android SharedPreferences; die Paketdatensätze werden mit AES-256-GCM verschlüsselt und der Schlüssel liegt im Android Keystore. Es gibt weiterhin keine zentrale Datenbank. Die Sendungsnummer wird vor der Partnerübergabe synchron in einen verschlüsselten lokalen Cache geschrieben; bei einem Schreibfehler wird der Übergabeversuch gestoppt. Es gibt keine Synchronisierung zwischen Geräten, Benutzerkonten, Rollen oder revisionssichere Audit-Protokolle.
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
