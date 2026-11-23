# TRAKUNA – Android Scan-Zentrale

TRAKUNA 1.52 ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Aktueller Entwicklungsstand – Version 1.52 (versionCode 53)

- Neue Archivfunktion für abgeschlossene Vorgänge: Archivieren, Archiv anzeigen und Wiederherstellen.
- Offene Sendungen können nicht archiviert werden. Endgültiges Löschen wird erst in der Archivansicht angeboten. Die Speicherschicht erzwingt dieselbe Archivpflicht unabhängig von der Oberfläche.
- Archivstatus und Archivereignisse bleiben in der verschlüsselten lokalen Datenhaltung und in JSON-Backups erhalten. Backup-Schema 2 wird geschrieben; ältere Schema-1-Sicherungen bleiben lesbar.
- CSV-Export enthält jetzt zusätzlich den Archivierungszeitpunkt.
- 58 Unit-Tests im Testbestand. Version 1.52 ergänzt strikte UTF-8-Dekodierung auch für entschlüsselte lokale Speicherdaten. CI-Lauf 45285731190 für versionCode 53 war erfolgreich: Unit-Tests, Android-Lint, Manifestprüfungen, APK-Build und Artefakt-Upload bestanden. Die optionale Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen; bereitgestellt wurde eine Debug-APK.
- Debug-APK-SHA-256: `9e721394925265c1a9316089afefa04233aeb58b9461c8f8314b74c7f8a5eae7`.
- ZIP-Artefakt-SHA-256: `e496571dc273fa43f7b2d077d143eb47da8f20f7691e726db095e27c54891587`.
- Das ZIP-Artefakt wurde am 23.11.2026, 23:07 UTC erstellt und läuft am 07.12.2026, 23:07 UTC ab.
- CI-Build: https://github.com/mouzzlii/Fuck/actions/runs/45285731190

**Wichtig:** Version 1.51 ist ein Debug-Testpaket, kein produktiv signiertes Release. Vor einem Update vorhandene Daten sichern und die App nicht deinstallieren. Ein erfolgreicher CI-Build ersetzt keinen Test auf den tatsächlichen Shopgeräten.

## Aktuelle Betriebsgrenzen

- Die Daten werden lokal auf diesem Gerät gespeichert und verschlüsselt.
- Es gibt derzeit keine zentrale Synchronisierung zwischen mehreren Geräten und keine Cloud-Verbindung.
- WLAN ist für den lokalen Kamera-/DataWedge-Scan nicht erforderlich und aktiviert keine Synchronisierung. Version 1.49 zeigt den WLAN-Verbindungsstatus dieses einzelnen Geräts an; andere Geräte werden noch nicht gesucht.
- DataWedge-Empfangscode ist vorhanden; das Profil muss auf dem jeweiligen Zebra-Gerät eingerichtet und vor Ort getestet werden.
- DPD, GLS, Hermes und UPS sind die vier Start-Prioritäten und in Version 1.51 direkt auswählbar. Die Auswahl ist nur eine lokale Kennzeichnung. Die App-Zuordnung öffnet die gewählte App, bestätigt aber keine Datenübernahme. Offizielle Carrier-Buchungen und bestätigte Schnittstellen sind noch nicht umgesetzt. **Das bisherige Hermes-Handgerät MC3090BT ist zu alt und soll laut letzter Betreiberinformation in der kommenden Woche durch ein neues Gerät ersetzt werden; die tatsächliche Ankunft ist noch zu bestätigen.** Modell, Betriebssystem, Scanner, Drucker und freigegebene Integrationsmöglichkeiten des Nachfolgers sind noch unbekannt. Bis zur Ankunft bleibt das bestehende Hermes-System unangetastet; danach wird das neue Gerät zunächst nur inventarisiert und geprüft, ohne Einstellungen oder den laufenden Hermes-Ablauf eigenmächtig zu verändern.
- Die vorhandenen Carrier-Systeme, Handgeräte und Druckerkopplungen dürfen ohne ausdrückliche Betreiber- und Carrier-Freigabe nicht verändert werden.

## Release-Build

Der CI-Workflow kann zusätzlich ein signiertes Release-APK erstellen, sobald der Betreiber die vier Signierungs-Secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` und `ANDROID_KEY_PASSWORD` in den GitHub-Repository-Secrets hinterlegt hat. Der private Keystore bleibt außerhalb des Repositorys. Ohne diese Betreiber-Schlüssel wird bewusst nur die Debug-Testversion veröffentlicht.

## Integrations- und Sicherheitsdokumentation

- [Hardwareinventar des ersten Paketshops](HARDWARE-INVENTAR.md): vor Ort erfasste DPD-/GLS-/Hermes-Geräte sowie offene UPS-Hardwareangaben, Verbindungen, Versionsangaben und Prüfpunkte.
- [WLAN- und Startvorbereitung](WLAN-STARTVORBEREITUNG.md): WLAN-Verbindung, DataWedge-Einrichtung, sicherer Start und Abnahmetest.
- [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md): aktueller Build, Testablauf, Zielarchitektur, Integrationsvoraussetzungen und Produktionssperren.
- [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md): offizielle DPD-/GLS-/Hermes-/UPS-Einstiege, technische Grenzen und konkrete nächste Schritte.
- [Anfrage zur Carrier-Freigabe](ANFRAGE-CARRIER-FREIGABE.md): vorbereitete Partneranfrage mit konkreten Fragen für DPD, GLS, Hermes und UPS in Österreich.
- [Partner-Schnittstelle und Abnahmekriterien](PARTNER_INTEGRATION.md): beschreibt den aktuellen Android-Übergabeversuch, den notwendigen Vertrag mit dem Softwareanbieter und die Tests für eine echte Integration.
- [Sicherheitsstatus und Produktionsfreigabe](SECURITY.md): dokumentiert umgesetzte Schutzmaßnahmen, bekannte Restrisiken und verbindliche Freigabekriterien.
- [Sichere Mehrgerätearchitektur](MEHRGERAETE-ARCHITEKTUR.md): Zielmodell für Geräteidentität, Rollen, Offline-Warteschlange, Ereignissynchronisierung und Konfliktbehandlung; noch nicht implementiert.
- [Mehr-KI-Arbeitsprotokoll](MEHR-KI-ARBEITSPROTOKOLL.md): gemeinsamer GitHub-Arbeitsablauf, Aufgabenpakete, unabhängige Reviews, Zugangsstatus und Kosten-/Datenschutzgrenzen.
- [Betriebshandbuch](BETRIEBSHANDBUCH.md): einfache Schritt-für-Schritt-Anleitung für Paketannahme, Kundenausgabe, Retouren, Zebra DataWedge, Partner-App und Datensicherung.
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md): verbindliche Nachweise und externe Voraussetzungen vor einem echten Paketshop-Einsatz.
- [Praxis-Testplan](PRAxisTEST.md): Testfälle für den kontrollierten Gerätetest.

## CI-Schutzprüfungen

Der Android-Workflow startet nur bei App-/Build-Änderungen und prüft vor dem Build, dass die aktuelle Offline-Version keine INTERNET-Berechtigung anfordert, Android-Backups deaktiviert und Klartextverkehr deaktiviert bleibt. Diese Schranke muss bei einer späteren Backend-Einführung bewusst und zusammen mit dem Sicherheitskonzept angepasst werden.

## Neu in Version 1.52

- Die UTF-8-Dekodierung verschlüsselter lokaler Speicherdaten ist strikt: ungültige Bytefolgen werden als Speicherfehler behandelt und nicht stillschweigend durch Ersatzzeichen verändert.
- Ein zusätzlicher Unit-Test deckt die Ablehnung fehlerhafter UTF-8-Sequenzen direkt ab.

## Neu in Version 1.51

- Abgeschlossene Vorgänge können archiviert und später wiederhergestellt werden; offene Vorgänge bleiben in der aktiven Liste.
- Endgültiges Löschen ist nur noch aus der Archivansicht möglich.
- JSON-Backup-Schema 2 erhält den Archivstatus und akzeptiert weiterhin ältere Schema-1-Sicherungen.
- CSV-Export enthält einen eigenen Archivierungszeitpunkt.

## Neu in Version 1.49

- Die App zeigt an, ob das aktuelle Android-Gerät mit einem WLAN verbunden ist. Der Status wird beim Zurückkehren in die App aktualisiert.
- Dafür wird ausschließlich `ACCESS_NETWORK_STATE` verwendet. Es gibt weiterhin keine `INTERNET`-Berechtigung, keinen Netzwerkzugriff, keine lokale Gerätesuche und keine Synchronisierung.

## Neu in Version 1.48

- Status bei Paketdienst-Übergaben wird ausdrücklich als lokale Dokumentation bezeichnet. Die Bestätigungsabfrage weist darauf hin, dass TRAKUNA keine Bestätigung von DPD, GLS, Hermes oder UPS erhält und der Vorgang erst nach Abschluss/Quittierung im offiziellen Carrier-System lokal markiert werden soll.
- Die Pilotunterlagen enthalten jetzt die österreichischen GLS-Belegregeln, die offiziellen Hermes-Partnerabläufe sowie DPD-Pickup-Referenzabläufe (Deutschland ausdrücklich nicht ungeprüft auf Österreich übertragbar).

- Für DPD, GLS, Hermes und UPS lässt sich jeweils eine eigene, auf dem Android-Gerät installierte Betriebssoftware zuordnen.
- Sobald Einzelzuordnungen aktiv sind, wird eine fehlende Zuordnung nicht durch die App eines anderen Paketdienstes ersetzt.
- Die Paketnamen werden nicht vorgegeben: Der Betreiber wählt die tatsächlich installierte App aus. Die Zuordnung und das Öffnen einer App bestätigen keine Datenübernahme oder Carrier-Buchung.

- Direkte Carrier-Schnellauswahl für die vier Start-Prioritäten DPD, GLS, Hermes und UPS.
- Manuelle Änderung der Auswahl bleibt möglich; die Auswahl kennzeichnet nur den lokalen Vorgang und löst keine externe Buchung aus.

## Neu in Version 1.44

- Kamera-Scans werden über mehrere aufeinanderfolgende Bilder gesammelt. Wenn in diesem Bestätigungsfenster mehrere unterschiedliche Sendungsnummern erkannt werden, wird keine davon automatisch ausgewählt.
- QR-Links werden vorsichtiger ausgewertet: allgemeine IDs und beliebige URL-Pfadnummern reichen nicht mehr aus; mehrdeutige Treffer werden abgelehnt.
- Sendungsnummern aus Etikettentexten werden nur bei eindeutig erkannten Formaten extrahiert. Ein gültig aussehender Teil innerhalb eines längeren Codes wird nicht als eigene Nummer übernommen.
- Paketdienstnamen werden bei bekannten manuellen Eingaben vereinheitlicht. Die DHL-Erkennung verlangt jetzt ein vollständiges bekanntes Muster; unklare JD-Präfixe werden nicht mehr automatisch als DHL eingestuft.
- Dieselbe aktive Sendungsnummer kann nicht mehr versehentlich in mehreren Vorgangsarten angelegt werden. Die Rückmeldung zeigt die bereits vorhandene Vorgangsart.
- Kamera-Barcodes verwenden den Anzeigewert als Rückfall, wenn der Rohwert leer ist.
