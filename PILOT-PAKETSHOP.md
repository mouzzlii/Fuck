# TRAKUNA – Pilotpaket für den ersten Paketshop

**WLAN- und Startanleitung:** [WLAN-STARTVORBEREITUNG.md](WLAN-STARTVORBEREITUNG.md) – Vorbereitung für den ersten Start und den DataWedge-Test.


Stand: 24. November 2026 · Testversion 1.53 (versionCode 54) · CI-Lauf 45293985061 erfolgreich (Debug-Testpaket)

## 1. Zweck und Freigabestatus

Dieses Dokument bündelt die Voraussetzungen für einen kontrollierten Pilotbetrieb. Der aktuelle Stand ist eine lokale Testversion; kein Nachweis für produktiven Paketshop-Betrieb.

### Start-Priorität der Paketdienste

Für den ersten Pilot stehen **DPD, GLS, Hermes und UPS** im Vordergrund. Die TRAKUNA-Oberfläche bietet dafür eine direkte manuelle Auswahl in dieser Reihenfolge. Die Auswahl dient ausschließlich der Zuordnung des lokalen Vorgangs. Sie ersetzt keine Carrier-Authentifizierung, Buchung, Annahmebestätigung oder Label-Erstellung.

- DPD/GLS: TC26-/DataWedge-Scan und bestehende Bluetooth-Druckabläufe getrennt und ohne Eingriff in die Carrier-Systeme testen.
- Hermes: Das bisherige MC3090BT ist laut Betreiber zu alt und soll laut letzter Betreiberinformation in der kommenden Woche durch ein neues Gerät ersetzt werden; die tatsächliche Ankunft ist noch zu bestätigen. Das bestehende Hermes-System bis zur regulären Umstellung unverändert lassen. Nach Ankunft des Nachfolgers zuerst Modell, Betriebssystem, Hermes-App, Scanner und Drucker erfassen; anschließend nur mit Freigabe einen Integrationsweg prüfen.
- UPS: bekannte 1Z-Nummernstruktur ist nur ein Format-Hinweis; Hardware, Betriebssoftware und offizielle Schnittstelle sind noch zu erheben. Die offizielle App UPS Access Point (`com.ups.accesspoint`) verlangt Android 9.0 oder höher, wenn sie auf demselben Gerät laufen soll.


Version 1.53 wurde erfolgreich durch CI gebaut. Unit-Tests, Kompilierung der Android-Instrumentierungstestfälle, Android-Lint, Manifestprüfungen, APK-Build und Artefakt-Upload waren erfolgreich. Die optionale Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen. Die neuen Android-Instrumentierungstestfälle sind vorhanden und erfolgreich kompiliert. Ihre Laufzeitprüfung erfolgt separat im Emulator-Workflow 45295645386, der für den aktuellen Code-Commit noch läuft. Die Übergabe an einen Paketdienst wird in TRAKUNA ausdrücklich nur als lokale Dokumentation geführt, nicht als vom Carrier bestätigter Vorgang. Das ist kein Nachweis für einen produktiven Paketshop-Betrieb.

**Aktueller Status:** technischer Praxistest auf einem einzelnen Android-Gerät mit ausdrücklich erfundenen Testsendungen. Keine produktive Nutzung mit Kundensendungen, solange die Freigabepunkte in Abschnitt 7 offen sind.

## 2. Download des aktuellen Testpakets

Version 1.53 (versionCode 54) enthält Archivierung/Wiederherstellung, Backup-Schema 2, Archivzeitpunkt im CSV-Export und den zusätzlichen Löschschutz für ungültige Archivdatensätze. [Erfolgreichen CI-Lauf 45293985061 öffnen](https://github.com/mouzzlii/Fuck/actions/runs/45293985061).

- Unit-Tests: erfolgreich
- Android-Lint: erfolgreich
- Debug-APK-Erstellung: erfolgreich
- APK-SHA-256: `ce7ed6093e213af7167e5bb2ebfc9c97fd89123fc4231f82fb94b8a4e8143f2b`
- ZIP-SHA-256: `b2857b0eeb9c452f929ecc9fb2f1258de768897e7f9e26c0eb7912c8c45b6b59`
- ZIP-Artefakt: `TRAKUNA-Android`, erstellt 24.11.2026, 00:05 UTC; Ablauf 08.12.2026, 00:05 UTC

[Neues Testpaket herunterladen (GitHub Actions)](https://github.com/mouzzlii/Fuck/actions/runs/45293985061)

**Historischer Build 1.44** (ohne Carrier-Schnellauswahl): [CI-Lauf 45227305052](https://github.com/mouzzlii/Fuck/actions/runs/45227305052). Nicht mehr als aktuelles Testpaket verwenden.

Das Artefakt im Actions-Lauf 45293985061 herunterladen und das enthaltene ZIP entpacken. Debug-Signaturen können sich zwischen CI-Läufen unterscheiden; bei abweichender Signatur verweigert Android ein Update über die bestehende Installation. Vor jedem Update ein JSON-Backup erstellen und die Wiederherstellung prüfen. Nicht deinstallieren, solange keine verifizierte Sicherung vorliegt. Wiederholbare Updates benötigen einen dauerhaft vom Betreiber verwalteten Signaturschlüssel.

### Sicherheitsverbesserung in Version 1.52

Entschlüsselte Bytes des lokalen Sendungsbestands und des separaten Partner-Übergabe-Caches werden strikt als UTF-8 dekodiert. Fehlerhafte Bytefolgen werden als Speicherfehler behandelt und nicht unbemerkt verändert.

### Gerätefunktionen in Version 1.52

Abgeschlossene Vorgänge können archiviert und wiederhergestellt werden. Offene Sendungen können nicht archiviert werden. Endgültiges Löschen ist nur in der Archivansicht möglich. Der Archivstatus bleibt in JSON-Backups (Schema 2) und im CSV-Export erhalten; ältere Schema-1-Sicherungen bleiben lesbar. Archivierte Vorgänge zählen weiterhin zum lokalen Limit von 500 Datensätzen.

### Neue Gerätefunktion in Version 1.49

Die App zeigt, ob das aktuelle Gerät über WLAN verbunden ist. Dies ist nur eine lokale Statusanzeige: andere Geräte werden nicht erkannt, es findet keine Netzwerksuche statt und es werden keine Daten übertragen. Die WLAN-Anzeige ist kein Nachweis, dass alle vier Handgeräte im selben Netzwerk erreichbar sind.

### Neue Gerätefunktion in Version 1.48

Für jeden der vier Start-Paketdienste kann auf dem Android-Gerät eine eigene installierte Betriebssoftware ausgewählt werden. Die Zuordnung wird lokal gespeichert. Sobald mindestens eine Einzelzuordnung eingerichtet ist, wird für einen anderen Paketdienst ohne eigene Zuordnung keine fremde Partner-App geöffnet. Das verhindert eine versehentliche Übergabe an das falsche Carrier-System. Die Zuordnung und das Öffnen einer App bestätigen weiterhin keine Datenübernahme oder Buchung.

## 3. Unterstützte lokale Abläufe

| Ablauf | Lokaler Funktionsumfang | Externe Buchung |
|---|---|---|
| Paket für Kunden annehmen | Erfassen und Status lokal führen | Nicht integriert |
| Paket an Kunden ausgeben | Status lokal fortschreiben | Nicht integriert |
| Retoure vom Kunden annehmen | Retoure lokal erfassen | Nicht integriert |
| Sendung an Paketdienst übergeben | Lokalen Vorgang fortschreiben und Partner-App-Übergabe versuchen | Nicht bestätigt |

Die Partner-App-Übergabe ist derzeit ein Android-Intent-Versuch. Das Öffnen einer App bestätigt weder den Empfang der Daten noch eine Buchung.

## 4. Technische Basis und Grenzen

- Lokale Sendungsdaten werden verschlüsselt im Android Keystore-gestützten Speicher gehalten.
- Kamera-Scan und manuelle Eingabe sind vorgesehen.
- Das bisherige Hermes-MC3090BT ist kein Android-Zielgerät für TRAKUNA. Zebra führt die MC3090-Reihe mit Windows CE 5.0 bzw. Windows Mobile 6.1; TRAKUNA benötigt Android 8.0/API 26 oder neuer. Ob das angekündigte Nachfolgegerät mit TRAKUNA kompatibel ist, wird erst nach Ankunft anhand des genauen Modells und Betriebssystems geprüft. [Zebra-Kompatibilitätsübersicht](https://www.zebra.com/us/en/support-downloads/software/mobile-computer-software/enterprise-browser.html).
- Zebra DataWedge ist als Broadcast-Eingangsweg vorbereitet. TRAKUNA akzeptiert DataWedge-Scans ausschließlich ab Android 14 (API 34), wenn Android den ursprünglichen Absender als `com.symbol.datawedge` bestätigt. Auf Android 13 (API 33) und älter bleibt dieser Eingang deaktiviert; dort Kamera-Scanner oder manuelle Eingabe verwenden. Die GLS- und DPD-Handgeräte werden nach den vorliegenden Gerätehinweisen als Zebra TC26 eingestuft (4G-Anzeige; endgültige Bestätigung über Typenschild/Teilenummer steht aus).
- Für TC26 ist ein dediziertes DataWedge-Profil für TRAKUNA vorgesehen: Barcode Input + Intent Output, Action `de.trakuna.app.DATAWEDGE_SCAN`, Extra `com.symbol.datawedge.data_string`, Delivery `Broadcast Intent`. Das muss auf jedem Handgerät mit Betreiberfreigabe eingerichtet und praktisch getestet werden; eine erfolgreiche Übertragung ist bislang nicht nachgewiesen.
- Sendungsstatus und begrenzte Ereignishistorie werden lokal gespeichert.
- Abgeschlossene Vorgänge können archiviert und wiederhergestellt werden. Offene Sendungen bleiben aktiv; endgültiges Löschen ist nur aus dem Archiv möglich.
- Archivierte Vorgänge bleiben im lokalen 500er-Limit enthalten. JSON-Backup-Schema 2 speichert den Archivstatus; ältere Schema-1-Sicherungen bleiben lesbar.
- Der CSV-Export enthält einen zusätzlichen Archivierungszeitpunkt.
- JSON-Backup und CSV-Export enthalten vertrauliche Daten im Klartext. Sie müssen geschützt gespeichert und übertragen werden.
- Der CSV-Export enthält zusätzlich die lokal gespeicherten Historieneinträge. Diese können als lokale Arbeitsdokumentation dienen, sind aber kein manipulationssicherer oder zentral bestätigter Nachweis.
- Es gibt keine zentrale Synchronisierung, keine Mitarbeiterkonten und keine rollenbasierte Zugriffskontrolle.
- Ein Geräteverlust kann ohne vorhandene, geprüfte Sicherung zum Datenverlust führen.
- Die Carrier-Erkennung ist eine Hilfestellung; Nummer und Paketdienst sind durch Mitarbeiter zu kontrollieren.

## 5. Vor-Ort-Testablauf

1. Nur Testetiketten und erfundene Empfängerdaten verwenden.
2. Android-Gerät, Android-Version und TRAKUNA-Build dokumentieren.
3. Alle vier Abläufe aus Abschnitt 3 durchspielen.
4. Je Ablauf einen normalen, einen fehlerhaften und einen wiederholten Scan testen.
5. Kamera-Scan, manuelle Eingabe und Zebra/DataWedge getrennt testen.
6. App schließen und neu öffnen; Bestand und Historie kontrollieren.
7. JSON-Sicherung erstellen, Datei geschützt ablegen und Wiederherstellung auf einem Testgerät prüfen.
8. Partner-App auswählen und nur die tatsächliche Übergabe prüfen. Keine Buchung behaupten, solange die Partner-App keine bestätigte Rückmeldung liefert.
9. Fehler, Screenshots ohne Kundendaten, Gerätedaten und Ergebnis in einem Testprotokoll festhalten.

## 6. Angaben, die für echte Integrationen benötigt werden

Für jede Paketshop-Software bzw. jeden Carrier sind folgende Informationen erforderlich:

- Anbieter/Hersteller und exakter Produktname
- Installierte App-/Softwareversion
- Betreiberfreigabe zur Integration
- Offizielle API-, SDK- oder Android-Intent-Dokumentation
- Sandbox/Testkonto und nicht-personenbezogene Testetiketten
- Authentifizierungs- und Berechtigungsmodell
- Rückmeldungen für Erfolg, Ablehnung, Timeout und Wiederholung
- Idempotenzregel gegen doppelte Buchungen
- Technischer Ansprechpartner beim Anbieter

**Hermes ist für den geplanten Pilotshop eine Pflichtintegration.** Bis offizielle Partnerdokumentation und Testzugang vorliegen, bleibt Hermes manuell auswählbar; automatische Erkennung oder Buchung wird nicht zugesagt.

## 7. Sperrkriterien vor produktivem Einsatz

Produktivbetrieb bleibt gesperrt, bis alle zutreffenden Punkte nachgewiesen sind:

- [ ] Betreiber und konkrete Paketshop-Software identifiziert
- [ ] Neues Hermes-Handgerät nach Ankunft vollständig inventarisiert (Modell, Betriebssystem, Hermes-App, Scanner, Drucker und Verbindung)
- [ ] Betreiber-/Hermes-Freigabe für einen TRAKUNA-Test auf dem neuen Hermes-Gerät eingeholt
- [ ] Sicherer Parallelbetrieb bzw. freigegebener Umstellungsplan ohne Unterbrechung des bestehenden Hermes-Ablaufs festgelegt
- [ ] Hermes-Schnittstelle offiziell freigegeben und abgenommen
- [ ] Weitere benötigte Carrier-Schnittstellen einzeln geprüft
- [ ] GLS-/DPD-Zebra-TC26-Teilenummern und Android-/MX-/DataWedge-Versionen erfasst
- [ ] TRAKUNA-DataWedge-Profil auf beiden TC26-Handgeräten mit Testetiketten praktisch getestet
- [ ] Signierter Release-Build mit dauerhaft gesichertem Betreiber-Schlüssel erstellt
- [ ] Update von der vorherigen Version mit Datenbestand geprüft
- [ ] Mehrgeräte- und Mitarbeitermodell entschieden und sicher umgesetzt, falls benötigt
- [ ] Audit-Protokoll mit Mitarbeiteridentität und nachvollziehbaren Änderungen umgesetzt, falls mehrere Mitarbeiter arbeiten
- [ ] Verschlüsselte Backup- und Wiederherstellungsstrategie abgenommen
- [ ] Datenschutz- und Sicherheitsprüfung für die tatsächlichen Datenflüsse abgeschlossen
- [ ] Paketshop-Betreiber hat alle Abläufe schriftlich abgenommen

## 8. Empfohlene Zielarchitektur für Mehrgerätebetrieb

Mehrgerätebetrieb darf nicht durch das Kopieren lokaler Dateien oder das Teilen eines gemeinsamen Kontos simuliert werden. Vor der Umsetzung sind Betreiber, Datenstandort, Aufbewahrung, Rollen und Offlineverhalten festzulegen.

Die Zielarchitektur benötigt mindestens:

1. Individuelle Mitarbeiteridentitäten und Mehr-Faktor-Schutz für privilegierte Konten.
2. Rollen mit minimalen Rechten (Mitarbeiter, Schichtleitung, Betreiber, Support).
3. Serverseitige Autorisierung jeder Datenänderung; App-seitige Sichtbarkeit allein ist keine Zugriffskontrolle.
4. Serverseitige, append-only Audit-Ereignisse mit Akteur, Zeit, Aktion, Objekt-ID und Ergebnis.
5. Idempotente Schreiboperationen und Konfliktregeln für parallele Scans.
6. Transportverschlüsselung, sichere Geheimnisverwaltung, Datenminimierung und definierte Löschfristen.
7. Verschlüsselte Backups, Wiederherstellungstests und dokumentierten Notfallzugriff.
8. Klare Offline-Regeln: lokal zwischengespeicherte Vorgänge dürfen nicht als serverseitig synchronisiert gelten, bevor der Server sie bestätigt.

Diese Architektur ist ein Umsetzungsvorschlag, keine bereits vorhandene Funktion.

## 9. Entscheidung des Pilotbetreibers

Für den nächsten Integrationsschritt wird genau eine Information benötigt: **Welche konkrete Paketshop- bzw. Versandsoftware verwendet der erste Paketshop (Hersteller, Produktname und möglichst Version)?**

Keine Passwörter, API-Schlüssel oder Kundendaten übermitteln. Offizielle Dokumentation und ein Testzugang können später über einen sicheren Anbieterweg eingebunden werden.


## Zusätzlicher DPD-Abnahmepunkt

DPD Deutschland veröffentlicht ein Pickup-Partnerhandbuch (01/2026) mit Kundenabgabe, Kundenausgabe, Zusteller-Anlieferung, Zusteller-Abholung, Gegenscan/QR-Bestätigung und Rückführung nicht abgeholter Sendungen. Dieses Handbuch ist eine fachliche Referenz, aber nicht automatisch für Österreich gültig. DPD Österreich muss die entsprechenden Abläufe für den Pilotstandort bestätigen. Bis dahin sind diese DPD-Abläufe nicht als durch TRAKUNA umgesetzt oder abgenommen zu verstehen.
