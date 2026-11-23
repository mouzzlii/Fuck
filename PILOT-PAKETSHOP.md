# TRAKUNA – Pilotpaket für den ersten Paketshop

**WLAN- und Startanleitung:** [WLAN-STARTVORBEREITUNG.md](WLAN-STARTVORBEREITUNG.md) – Vorbereitung für den ersten Start und den DataWedge-Test.


Stand: 23. November 2026 · App-Version: 1.44 (versionCode 45) · CI-Lauf: 45227305052

## 1. Zweck und Freigabestatus

Dieses Dokument bündelt die Voraussetzungen für einen kontrollierten Pilotbetrieb. App-Version 1.44 (versionCode 45) ist als **Debug-Testpaket** erfolgreich gebaut. Unit-Tests und Android-Lint sind erfolgreich. Das ist kein Nachweis für einen produktiven Paketshop-Betrieb.

**Aktueller Status:** technischer Praxistest auf einem einzelnen Android-Gerät mit ausdrücklich erfundenen Testsendungen. Keine produktive Nutzung mit Kundensendungen, solange die Freigabepunkte in Abschnitt 7 offen sind.

## 2. Download des aktuellen Testpakets

- GitHub Actions Build 266 (vorheriger App-Code-Stand; Dokumentationsänderungen danach separat gebaut): https://github.com/mouzzlii/Fuck/actions/runs/45221331671
- Artefakt: `TRAKUNA-Android`
- SHA-256: `cc3911e62d4024bb8869559b48ad5c2b3bf35c4e81e65591dc541329ed180ab7`
- Artefakt erstellt: 23.11.2026
- Voraussichtlicher Ablauf des Artefakts: 07.12.2026

Das Artefakt im Actions-Lauf herunterladen und das enthaltene ZIP entpacken. Die Debug-APK kann eine andere Signatur als spätere Builds haben. Vor einem Update bestehende lokale Daten sichern und die App nicht deinstallieren.

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
- Zebra DataWedge ist als Broadcast-Eingangsweg vorbereitet. Die GLS- und DPD-Handgeräte werden nach den vorliegenden Gerätehinweisen als Zebra TC26 eingestuft (4G-Anzeige; endgültige Bestätigung über Typenschild/Teilenummer steht aus).\n- Für TC26 ist ein dediziertes DataWedge-Profil für TRAKUNA vorgesehen: Barcode Input + Intent Output, Action `de.trakuna.app.DATAWEDGE_SCAN`, Extra `com.symbol.datawedge.data_string`, Delivery `Broadcast Intent`. Das muss auf jedem Handgerät mit Betreiberfreigabe eingerichtet und praktisch getestet werden; eine erfolgreiche Übertragung ist bislang nicht nachgewiesen.
- Sendungsstatus und begrenzte Ereignishistorie werden lokal gespeichert.
- JSON-Backup und CSV-Export enthalten vertrauliche Daten im Klartext. Sie müssen geschützt gespeichert und übertragen werden.
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
- [ ] Hermes-Schnittstelle offiziell freigegeben und abgenommen
- [ ] Weitere benötigte Carrier-Schnittstellen einzeln geprüft
- [ ] GLS-/DPD-Zebra-TC26-Teilenummern und Android-/MX-/DataWedge-Versionen erfasst\n- [ ] TRAKUNA-DataWedge-Profil auf beiden TC26-Handgeräten mit Testetiketten praktisch getestet
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
