# TRAKUNA – Freigabe-Checkliste für den echten Paketshop

## Zusätzliche Sperren für den Paketshop-Pilot – Shadow Mode 1.55

- [ ] Exakter Commit und erfolgreicher CI-Build für Version 1.55 / versionCode 56 bestätigt
- [ ] Emulator-Instrumentierung für exakt denselben Commit erfolgreich
- [ ] Auf Testgerät sichtbar: „Übergabe simulieren · TESTMODUS“
- [ ] Partner-App wird durch TRAKUNA bei Übergabeversuch nicht geöffnet
- [ ] Zwischenablage bleibt bei simuliertem Übergabeversuch unverändert
- [ ] Partner-App-Zuordnung ist im Testmodus deaktiviert
- [ ] Redigiertes lokales Testprotokoll lässt sich teilen und enthält keine Sendungsnummern/Kundendaten
- [ ] Paketshop-Betreiber hat Testablauf und Verbot echter Buchungen in separaten Carrier-Apps bestätigt
- [ ] Reale Kamera-/Autofokus-, Backup-/Restore- und Druckertests separat abgeschlossen

**Hinweis:** Der lokale Shadow Mode blockiert den durch TRAKUNA ausgelösten Partner-App-Start. Er kontrolliert keine separat gestarteten Apps und ist keine netzwerkweite Sperre für externe Buchungen. Derzeit werden nur blockierte Partnerübergabe-Versuche automatisch protokolliert; eine vollständige Ereigniserfassung bleibt offen.


Stand: App-Version 1.54 (versionCode 55) · CI-Build 45769123996 und Emulator-Instrumentierung 45769124107 erfolgreich (Debug-Testpaket; nicht produktiv signiert)

Diese Liste trennt automatisiert prüfbare Softwarequalität von Nachweisen, die nur am echten Gerät, mit dem jeweiligen Paketdienst oder durch den Betreiber erbracht werden können. Ein leeres Feld bedeutet **nicht freigegeben**.

## A. Softwarestand

- [x] CI-Lauf 45769123996 für Version 1.54 / versionCode 55 auf Commit `8d9f4058b754084e47d177f0b1190a0bc2860c07` erfolgreich – Unit-Tests, Instrumentierungstest-Kompilierung, Android-Lint, Manifest-/Offline-Sicherheitsprüfungen, APK-Build und Artefakt-Upload
- [x] Vorhandene Unit-Tests erfolgreich
- [x] Android-Lint erfolgreich
- [x] Zusammengeführtes Manifest ohne INTERNET-Berechtigung, aktiviertes Android-Backup oder Klartextverkehr geprüft
- [x] Debug-APK und zugehörige `SHA256SUMS.txt` für Version 1.54 als Artefakt `15093243107` bereitgestellt
- [x] Android-Instrumentierungstestfälle für Keystore-Verschlüsselung und lokale Speicherfunktionen auf Emulator ausgeführt: [Run 45769124107 erfolgreich](https://github.com/mouzzlii/Fuck/actions/runs/45769124107)
- [ ] Archivieren/Wiederherstellen und Backup-Restore auf einem echten Android-Gerät praktisch geprüft
- [ ] Release-Version und Versionsnummer fachlich geprüft
- [ ] Produktionssignatur mit dauerhaft gesichertem Betreiber-Schlüssel erstellt
- [ ] Stabiler Betreiber-Signaturschlüssel für wiederholbare Test-/Release-Updates eingerichtet und Update ohne Datenverlust geprüft
- [ ] Update von der vorherigen freigegebenen Version getestet
- [ ] Archivieren nur abgeschlossener Vorgänge, Wiederherstellen und Konfliktfall mit bereits aktiver Sendungsnummer getestet
- [ ] Neue passwortgeschützte `.trakuna`-Sicherung (AES-256-GCM) mit korrektem Passwort exportiert und wiederhergestellt; falsches Passwort/beschädigte Datei abgewiesen; alte unverschlüsselte JSON-Sicherung nur als Legacy-Import geprüft

Letzter erfolgreicher Build für Version 1.54: [Android-Build 45769123996](https://github.com/mouzzlii/Fuck/actions/runs/45769123996), Commit `8d9f4058b754084e47d177f0b1190a0bc2860c07`. Der separate [Emulator-Instrumentierungslauf 45769124107](https://github.com/mouzzlii/Fuck/actions/runs/45769124107) war für denselben Commit erfolgreich. Das Debug-Artefakt `TRAKUNA-Android` (Artifact-ID `15093243107`) ist bis 10.12.2026, 06:37 UTC verfügbar. Die enthaltene `SHA256SUMS.txt` enthält die Prüfsumme der APK; es wird hier kein Hash eines älteren Builds wiederverwendet. Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen. Die APK-SHA-256 lautet `18a4ec27365f31a0a21d2fd8dc04342dde4f9d4f7e6150a18eb56438de1e4dc3`. Die automatisierten Prüfungen sind kein Ersatz für reale Geräte-, Carrier- und Betreiberabnahme.


## B. Geräte- und Scannerabnahme

**Zielarchitektur:** ein Android-Smartphone und ein gemeinsamer Etikettendrucker. Zebra-/DataWedge-Tests sind keine Freigabebedingung des neuen Ablaufs; vorhandener Legacy-Code bleibt davon getrennt.

- [ ] Vorgesehenes Android-Smartphone (Modell, Android-Version und Gerätemanagement) dokumentiert
- [ ] Kamera-/Autofokus-Scan mit echten Testetiketten je freigegebenem Carrier bestanden
- [ ] Manuelle Eingabe einschließlich fehlerhafter/ungewöhnlicher Nummern geprüft
- [ ] Ein gemeinsamer Etikettendrucker (Modell, Firmware, Medienformat und Auflösung) dokumentiert
- [ ] WLAN-Druck mit echten Testlabels geprüft
- [ ] Bluetooth-Druck mit echten Testlabels geprüft, sofern das konkrete Modell diesen Weg unterstützt
- [ ] Duplikatschutz und UNKNOWN-Status nach Timeout praktisch geprüft
- [ ] Zebra/DataWedge nur dann separat geprüft, wenn der bestehende Legacy-Pfad ausdrücklich weiterverwendet und dafür freigegeben wird; keine neue Hardwareintegration vorausgesetzt
- [ ] Getrennte Betriebssoftware-Zuordnung für DPD, GLS, Hermes und UPS geprüft; keine falsche Carrier-App wird als Fallback geöffnet
- [ ] Scan-Broadcast kann keine automatische Buchung auslösen
- [ ] Hintergrundverhalten geprüft: Scans werden nur bei aktiver TRAKUNA-App im Vordergrund verarbeitet
- [ ] Scannerverhalten bei mehreren Barcodes, schlechtem Licht und Verbindungsfehlern geprüft

## C. Partner- und Carrier-Integration

Für **jeden** Paketdienst und jede Betriebssoftware ist ein eigener Nachweis erforderlich.

- [ ] Anbieter und konkrete App-Version benannt
- [ ] Schriftlich bestätigte API-/Intent-Dokumentation des Anbieters vorhanden
- [ ] Zulässige Datenfelder und Übergabeformat bestätigt
- [ ] Authentifizierung und Berechtigungen durch den Anbieter geklärt
- [ ] Erfolgreiche Übernahme einer Testsendung in der Partner-App nachgewiesen
- [ ] Fehler- und Abbruchantworten der Partner-App geprüft
- [ ] Rückmeldung/Bestätigung einer Buchung technisch nachgewiesen – nicht nur das Öffnen der App
- [ ] Alle vier Abläufe abgenommen: Paket für Kunden annehmen, Kundenabgabe annehmen, Retoure annehmen, an Carrier übergeben
- [ ] Hermes-Integration vom Anbieter freigegeben und mit Testsendungen abgenommen

**Wichtig:** Die aktuelle TRAKUNA-Übergabe öffnet eine ausgewählte Android-App und übergibt standardisierte Extras. Sie bestätigt keine Buchung. Solange der Anbieter die Schnittstelle nicht implementiert und abnimmt, bleibt dieser Teil eine manuelle Unterstützung.

### Carrier-spezifische Pflichtfreigaben

- [ ] **DPD:** offizieller Portal-/Labelübergabeweg, Betreiberberechtigung und die benötigten Shopabläufe bestätigt
- [ ] **GLS:** offizieller Portal-/Labelübergabeweg, Betreiberberechtigung und die benötigten Shopabläufe bestätigt
- [ ] **Hermes:** angekündigtes Nachfolgegerät nach Ankunft inventarisiert und Kompatibilität geprüft; bestehender Ablauf bis zur freigegebenen Umstellung unangetastet; autorisierte Schnittstelle und alle vier Shopabläufe bestätigt
- [ ] **UPS:** Hardware/Shopsoftware erhoben, offizielle App `com.ups.accesspoint`/Android-9+-Anforderung geprüft, Access-Point-Partnerfreigabe und alle vier Shopabläufe bestätigt

Siehe [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md) und [Praxis-Testplan](PRAxisTEST.md).

## D. Betrieb, Benutzer und Daten

- [ ] Verantwortlicher Betreiber und Supportkontakt festgelegt
- [ ] Mitarbeiterkonten und Rollen umgesetzt, falls mehrere Personen arbeiten
- [ ] Zentrale Synchronisierung und Konfliktbehandlung umgesetzt, falls mehrere Geräte genutzt werden
- [ ] Nachvollziehbares Audit-Protokoll für Annahme, Bearbeitung, Ausgabe und Löschung umgesetzt
- [ ] Aufbewahrungs- und Löschfristen schriftlich festgelegt
- [ ] Backup-Verantwortung, Speicherort und Wiederherstellungsprobe festgelegt
- [ ] Exporte und Sicherungen verschlüsselt oder in einem nachweislich geschützten Speicherprozess verwahrt
- [ ] Verfahren für Geräteverlust, Mitarbeiteraustritt und Schlüsselverlust festgelegt

## E. Sicherheit und Datenschutz

- [ ] Datenschutzrechtliche Prüfung für die tatsächlichen Datenflüsse durchgeführt
- [ ] Auftragsverarbeitung und Anbieterrollen geklärt, falls erforderlich
- [ ] Berechtigungen und App-Sichtbarkeit auf das notwendige Minimum begrenzt
- [ ] Unabhängige Sicherheitsprüfung durchgeführt
- [ ] Kritische und hohe Befunde behoben und erneut geprüft
- [ ] Schutz von Sendungsnummern, Empfängerdaten, Notizen, Exporten und Backups nachgewiesen

## F. Betreiber-Abnahme

- [ ] Alle vorgesehenen Abläufe mit Testsendungen erfolgreich durchgeführt
- [ ] Mitarbeiter geschult und Betriebshandbuch übergeben
- [ ] Fallback-Verfahren bei App-, Netzwerk-, Scanner- oder Carrier-Ausfall festgelegt
- [ ] Betreiber hat den konkreten Funktionsumfang schriftlich akzeptiert
- [ ] Freigabedatum

## G. Nächster zwingender Integrationsinput

- [ ] Hersteller, Produktname und Version der Paketshop-/Versandsoftware des ersten Pilotshops erfasst

Keine Passwörter, API-Schlüssel oder Kundendaten in GitHub-Issues oder Chat-Nachrichten eintragen. Siehe [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md).


### DPD Pickup-Ablauf – zusätzliche fachliche Abnahme

- [ ] DPD Österreich hat die für diesen Standort gültigen Schritte für Kundenabgabe und -ausgabe bestätigt.
- [ ] DPD Österreich hat die Zusteller-Anlieferung und -Abholung einschließlich Gegenscan/Quittierung bestätigt.
- [ ] Haftungsübergang und Beleg-/Unterschriftsnachweise sind pro Vorgang geklärt.
- [ ] Rückführung nicht abgeholter Sendungen und Lagerfristablauf sind geklärt.
- [ ] TRAKUNA zeigt keine Carrier-Bestätigung ohne bestätigtes DPD-Ereignis.


### GLS Österreich – Beleg- und Schnittstellenabnahme

- [ ] Vorgesehene GLS-Quittungen und Übergabebelege sind für den Standort dokumentiert.
- [ ] TRAKUNA unterscheidet strikt zwischen lokalem Scan und offizieller GLS-Quittung/Bestätigung.
- [ ] GLS hat die parallele App-/Scannerintegration schriftlich freigegeben.


### Lokale Paketdienst-Übergabe

- [x] TRAKUNA kennzeichnet die Übergabe an DPD/GLS/Hermes/UPS ausdrücklich nur als lokale Dokumentation.
- [ ] Vor Ort prüfen, dass die Original-Carrier-App bzw. der Partner-Scanner die Übergabe tatsächlich quittiert.
- [ ] Schriftliche Schnittstellenfreigabe und technische Rückmeldung pro Carrier nachweisen.
