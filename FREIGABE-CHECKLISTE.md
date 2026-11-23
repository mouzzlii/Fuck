# TRAKUNA – Freigabe-Checkliste für den echten Paketshop

Stand: App-Version 1.53 (versionCode 54) · CI-Build 45292752377 erfolgreich (Debug-Testpaket; nicht produktiv signiert)

Diese Liste trennt automatisiert prüfbare Softwarequalität von Nachweisen, die nur am echten Gerät, mit dem jeweiligen Paketdienst oder durch den Betreiber erbracht werden können. Ein leeres Feld bedeutet **nicht freigegeben**.

## A. Softwarestand

- [x] CI-Lauf 45292752377 für Version 1.53 erfolgreich – Unit-Tests, Kompilierung der Android-Instrumentierungstestfälle, Android-Lint, Manifestprüfungen, APK-Build und Artefakt-Upload
- [x] Vorhandene Unit-Tests erfolgreich
- [x] Android-Lint erfolgreich
- [x] Zusammengeführtes Manifest ohne INTERNET-Berechtigung, aktiviertes Android-Backup oder Klartextverkehr geprüft
- [x] Debug-APK und SHA-256-Prüfsumme für Version 1.53 als Artefakt bereitgestellt
- [x] Android-Instrumentierungstestfälle für Keystore-Verschlüsselung, Speicher-Wiederöffnung, beschädigten Übergabe-Cache und Backup-Wiederherstellung nach Speicherschaden hinzugefügt und erfolgreich kompiliert
- [ ] Android-Instrumentierungstestfälle auf Emulator oder echtem Android-Gerät tatsächlich ausgeführt
- [ ] Archivieren/Wiederherstellen und Backup-Restore auf einem echten Android-Gerät praktisch geprüft
- [ ] Release-Version und Versionsnummer fachlich geprüft
- [ ] Produktionssignatur mit dauerhaft gesichertem Betreiber-Schlüssel erstellt
- [ ] Stabiler Betreiber-Signaturschlüssel für wiederholbare Test-/Release-Updates eingerichtet und Update ohne Datenverlust geprüft
- [ ] Update von der vorherigen freigegebenen Version getestet
- [ ] Archivieren nur abgeschlossener Vorgänge, Wiederherstellen und Konfliktfall mit bereits aktiver Sendungsnummer getestet
- [ ] JSON-Backup-Schema 2 inklusive Archivstatus exportiert und wiederhergestellt; ältere Schema-1-Sicherung geprüft

Letzter erfolgreicher Build 1.53: https://github.com/mouzzlii/Fuck/actions/runs/45292752377 (Commit `0b7113da02c9a568f9e48a65ad36ab8c1f7053c8`). Unit-Tests, Android-Lint, Manifestprüfungen, APK-Build, Prüfsumme und Artefakt-Upload waren erfolgreich. Die Emulator-Instrumentierungstests laufen inzwischen in einem getrennten Workflow mit verlängertem TCG-Startfenster, damit sie den normalen APK-Build nicht blockieren. Die Instrumentierungstestfälle sind im Repository vorhanden und wurden erfolgreich kompiliert, aber im Haupt-Build **nicht ausgeführt**; der separate Emulator-Workflow 4 für denselben aktuellen Code-Commit ist noch in Bearbeitung. Die optionalen Release-Signierungsschritte wurden mangels Betreiber-Schlüsseln übersprungen; das Artefakt ist eine Debug-APK.

APK-SHA-256: `33abd4bc0132f6660f8cfd89100b506df2e2ae5108f2a172ac121e8777e74f2b`
ZIP-Artefakt-SHA-256: `35b8000f6c4581eb7afe1a9028b45ebe36e3668974c1aa634cb4c63104f5b395`

ZIP-Artefakt erstellt am 23.11.2026, 23:56 UTC; Ablauf am 07.12.2026, 23:56 UTC. Produktionssignierung und Emulator-Instrumentierungstest bleiben offen.

## B. Geräte- und Scannerabnahme

- [ ] Jedes tatsächlich eingesetzte Android-/Zebra-Modell dokumentiert
- [ ] Android-Version und Gerätemanagement dokumentiert
- [ ] Kamera-Scan mit echten Testetiketten je Paketdienst bestanden
- [ ] Manuelle Eingabe einschließlich fehlerhafter/ungewöhnlicher Nummern geprüft
- [ ] Zebra DataWedge-Profil auf jedem Gerät geprüft
- [ ] DataWedge Component Information auf TRAKUNA begrenzt und Signaturprüfung aktiviert
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

- [ ] **DPD:** TC26/DataWedge-Test, autorisierte Shop-Schnittstelle und alle vier Shopabläufe bestätigt
- [ ] **GLS:** TC26/DataWedge-Test, autorisierte Shop-Schnittstelle und alle vier Shopabläufe bestätigt
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
