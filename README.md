# TRAKUNA – Android Scan-Zentrale

TRAKUNA 1.54 ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Aktueller Entwicklungsstand – Version 1.54 (versionCode 55)

- Neue Archivfunktion für abgeschlossene Vorgänge: Archivieren, Archiv anzeigen und Wiederherstellen.
- Offene Sendungen können nicht archiviert werden. Endgültiges Löschen wird erst in der Archivansicht angeboten. Die Speicherschicht erzwingt dieselbe Archivpflicht unabhängig von der Oberfläche.
- Archivstatus und Archivereignisse bleiben in der verschlüsselten lokalen Datenhaltung und in JSON-Backups erhalten. Backup-Schema 2 wird geschrieben; ältere Schema-1-Sicherungen bleiben lesbar. Neue Sicherungsdateien werden mit einem selbst gewählten Passwort AES-256-GCM-verschlüsselt; ältere unverschlüsselte JSON-Backups bleiben importierbar und werden vor dem Ersetzen ausdrücklich gekennzeichnet.
- CSV-Export enthält jetzt zusätzlich den Archivierungszeitpunkt.
- Entschlüsselte Backup-Daten werden vor der Wiederherstellung auf 4 MiB begrenzt; verschlüsselte .trakuna-Dateien dürfen bis zu 6 MiB groß sein. Der lokale verschlüsselte Bestand bleibt auf 2 MiB begrenzt.
- Version 1.54 ergänzt passwortverschlüsselte, portable Backups (AES-256-GCM mit PBKDF2-HMAC-SHA256, zufälligem Salt/Nonce, 12–128 Zeichen Passwort) und behält den Import älterer unverschlüsselter JSON-Sicherungen mit deutlicher Warnung bei. Die JVM-Tests decken Roundtrip, falsches Passwort, Manipulation, frische Salts/Nonces und Passwortspeicher-Löschung ab; ein Android-Instrumentierungstest prüft den Krypto-Provider auf dem Gerät. **Die 1.54-Änderung ist noch in PR #8; dafür existiert noch kein freigegebenes APK-Artefakt.** Letzter installierbarer Stand bleibt Version 1.53 / versionCode 54 (CI 45293985061).
- Letztes verfügbares 1.53-Debug-APK-SHA-256: `ce7ed6093e213af7167e5bb2ebfc9c97fd89123fc4231f82fb94b8a4e8143f2b`.
- Letztes verfügbares 1.53-ZIP-Artefakt-SHA-256: `b2857b0eeb9c452f929ecc9fb2f1258de768897e7f9e26c0eb7912c8c45b6b59`.
- Das 1.53-ZIP-Artefakt wurde am 24.11.2026, 00:05 UTC erstellt und läuft am 08.12.2026, 00:05 UTC ab.
- CI-Build: https://github.com/mouzzlii/Fuck/actions/runs/45293985061

**Wichtig:** Das derzeit verfügbare APK ist Version 1.53 und nur ein Debug-Testpaket, kein produktiv signiertes Release. Version 1.54 ist noch nicht in Main integriert. Debug-Signaturen können sich zwischen CI-Läufen unterscheiden; Android kann dann ein Update über eine vorhandene Installation verweigern. Vor jedem Update zuerst eine passwortverschlüsselte .trakuna-Sicherung erstellen und deren Wiederherstellbarkeit prüfen. Das Sicherungspasswort getrennt aufbewahren; ältere JSON-Backups sind unverschlüsselt. Die App nicht deinstallieren, solange keine verifizierte Sicherung vorliegt. Für wiederholbare Updates ist ein dauerhaft vom Betreiber verwalteter Signaturschlüssel erforderlich. Ein erfolgreicher CI-Build ersetzt keinen Test auf den tatsächlichen Shopgeräten.

## Aktuelle Betriebsgrenzen

- Die Daten werden lokal auf diesem Gerät gespeichert und verschlüsselt.
- Es gibt derzeit keine zentrale Synchronisierung zwischen mehreren Geräten und keine Cloud-Verbindung.
- WLAN ist für den lokalen Kamera-/DataWedge-Scan nicht erforderlich und aktiviert keine Synchronisierung. Version 1.49 zeigt den WLAN-Verbindungsstatus dieses einzelnen Geräts an; andere Geräte werden noch nicht gesucht.
- DataWedge-Empfangscode ist vorhanden; das Profil muss auf dem jeweiligen Zebra-Gerät eingerichtet und vor Ort getestet werden.
- DPD, GLS, Hermes und UPS sind die vier Start-Prioritäten und in Version 1.53 direkt auswählbar. Die Auswahl ist nur eine lokale Kennzeichnung. Die App-Zuordnung öffnet die gewählte App, bestätigt aber keine Datenübernahme. Offizielle Carrier-Buchungen und bestätigte Schnittstellen sind noch nicht umgesetzt. **Das bisherige Hermes-Handgerät MC3090BT ist zu alt und soll laut letzter Betreiberinformation in der kommenden Woche durch ein neues Gerät ersetzt werden; die tatsächliche Ankunft ist noch zu bestätigen.** Modell, Betriebssystem, Scanner, Drucker und freigegebene Integrationsmöglichkeiten des Nachfolgers sind noch unbekannt. Bis zur Ankunft bleibt das bestehende Hermes-System unangetastet; danach wird das neue Gerät zunächst nur inventarisiert und geprüft, ohne Einstellungen oder den laufenden Hermes-Ablauf eigenmächtig zu verändern.
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

Für die Android-Instrumentierungstests gibt es zusätzlich den getrennten Workflow [`.github/workflows/android-instrumentation.yml`](.github/workflows/android-instrumentation.yml). Er startet einen API-35-Emulator und führt Keystore-, Speicher- und Backup-Recovery-Tests aus. Dieser langsamere Emulatorlauf ist bewusst vom normalen APK-Build getrennt, damit ein nicht startender Emulator die Bereitstellung der lokalen Debug-Testversion nicht blockiert.

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
