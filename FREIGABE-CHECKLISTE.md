# TRAKUNA – Freigabe-Checkliste für den echten Paketshop

Stand: App-Version 1.54 (versionCode 55) · CI-Build 45691683747 und Emulator-Instrumentierung 45691683698 erfolgreich (Debug-Testpaket; nicht produktiv signiert)

Diese Liste trennt automatisiert prüfbare Softwarequalität von Nachweisen, die nur am echten Gerät, mit dem jeweiligen Paketdienst oder durch den Betreiber erbracht werden können. Ein leeres Feld bedeutet **nicht freigegeben**.

## A. Softwarestand

- [x] CI-Lauf 45691683747 für Version 1.54 / versionCode 55 auf Commit `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` erfolgreich – Unit-Tests, Instrumentierungstest-Kompilierung, Android-Lint, Manifest-/Offline-Sicherheitsprüfungen, APK-Build und Artefakt-Upload
- [x] Vorhandene Unit-Tests erfolgreich
- [x] Android-Lint erfolgreich
- [x] Zusammengeführtes Manifest ohne INTERNET-Berechtigung, aktiviertes Android-Backup oder Klartextverkehr geprüft
- [x] Debug-APK und zugehörige `SHA256SUMS.txt` für Version 1.54 als Artefakt `15058952938` bereitgestellt
- [x] Android-Instrumentierungstestfälle für Keystore-Verschlüsselung und lokale Speicherfunktionen auf Emulator ausgeführt: [Run 45691683698 erfolgreich](https://github.com/mouzzlii/Fuck/actions/runs/45691683698)
- [ ] Archivieren/Wiederherstellen und Backup-Restore auf einem echten Android-Gerät praktisch geprüft
- [ ] Release-Version und Versionsnummer fachlich geprüft
- [ ] Produktionssignatur mit dauerhaft gesichertem Betreiber-Schlüssel erstellt
- [ ] Stabiler Betreiber-Signaturschlüssel für wiederholbare Test-/Release-Updates eingerichtet und Update ohne Datenverlust geprüft
- [ ] Update von der vorherigen freigegebenen Version getestet
- [ ] Archivieren nur abgeschlossener Vorgänge, Wiederherstellen und Konfliktfall mit bereits aktiver Sendungsnummer getestet
- [ ] JSON-Backup-Schema 2 inklusive Archivstatus exportiert und wiederhergestellt; ältere Schema-1-Sicherung geprüft

Letzter erfolgreicher Build für Version 1.54: [Android-Build 45691683747](https://github.com/mouzzlii/Fuck/actions/runs/45691683747), Commit `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb`. Der separate [Emulator-Instrumentierungslauf 45691683698](https://github.com/mouzzlii/Fuck/actions/runs/45691683698) war für denselben Commit erfolgreich. Das Debug-Artefakt `TRAKUNA-Android` (Artifact-ID `15058952938`) ist bis 09.12.2026, 20:08 UTC verfügbar. Die enthaltene `SHA256SUMS.txt` enthält die Prüfsumme der APK; es wird hier kein Hash eines älteren Builds wiederverwendet. Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen. Die automatisierten Prüfungen sind kein Ersatz für reale Geräte-, Carrier- und Betreiberabnahme.


## B. Geräte- und Scannerabnahme

- [ ] Jedes tatsächlich eingesetzte Android-/Zebra-Modell dokumentiert
- [ ] Android-Version und Gerätemanagement dokumentiert
- [ ] Kamera-Scan mit echten Testetiketten je Paketdienst bestanden
- [ ] Manuelle Eingabe einschließlich fehlerhafter/ungewöhnlicher Nummern geprüft
- [ ] Android-Version jedes Zebra-Geräts dokumentiert; DataWedge nur auf Android 14/API 34+ eingeplant und getestet
- [ ] Zebra DataWedge-Profil auf jedem unterstützten Gerät geprüft
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
