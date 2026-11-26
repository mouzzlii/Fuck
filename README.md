# TRAKUNA – Android Scan-Zentrale

<div align="center">
  <img src="https://raw.githubusercontent.com/mouzzlii/logo-M-glichkeiten-/main/1795643953832.jpg" alt="W-Signatur – Blau, Original" width="180" />
  <br />
  <sub>W — Master Emblem · Blau · Technologie</sub>
  <br />
  <sub>Originaldatei unverändert · nur proportionale Skalierung · <a href="https://github.com/mouzzlii/logo-M-glichkeiten-">verbindliche Logo-Regeln</a></sub>
</div>

TRAKUNA 1.54 ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Aktueller Entwicklungsstand – Version 1.54 (versionCode 55)

- Version 1.54 ergänzt eine temporäre Scan-Diagnose für Kamera-/ML-Kit- und Zebra-DataWedge-Scans: Barcodeformat, unveränderter Rohinhalt, Anzeigeinhalt und daraus extrahierter Kandidat werden gegenübergestellt. Mehrere unterschiedliche Codes bleiben sichtbar; eine Sendungsnummer wird nur bei genau einem eindeutigen Kandidaten übernommen. Die Diagnose bleibt ausschließlich im Arbeitsspeicher und wird weder gespeichert noch protokolliert. Rohdaten können personenbezogene oder sendungsbezogene Informationen enthalten.

- Neue Archivfunktion für abgeschlossene Vorgänge: Archivieren, Archiv anzeigen und Wiederherstellen.
- Offene Sendungen können nicht archiviert werden. Endgültiges Löschen wird erst in der Archivansicht angeboten. Die Speicherschicht erzwingt dieselbe Archivpflicht unabhängig von der Oberfläche.
- Archivstatus und Archivereignisse bleiben in der verschlüsselten lokalen Datenhaltung und in JSON-Backups erhalten. Backup-Schema 2 wird geschrieben; ältere Schema-1-Sicherungen bleiben lesbar.
- CSV-Export enthält jetzt zusätzlich den Archivierungszeitpunkt.
- Portable Datensicherungen werden als passwortgeschützte `.trakuna`-Dateien exportiert: AES-256-GCM, PBKDF2-HMAC-SHA256 mit 600.000 Iterationen, zufälliger Salt und Nonce. Das Passwort muss 12–128 Zeichen haben und wird nicht in der App gespeichert. Alte unverschlüsselte JSON-Sicherungen bleiben importierbar, werden vor dem Ersetzen aber ausdrücklich als Klartext gekennzeichnet. CSV-Exporte bleiben unverschlüsselt.
- Version 1.53 ergänzte die ersten Android-Instrumentierungstestfälle für Keystore-Verschlüsselung, lokalen Speicher und Backup-Wiederherstellung. Der damalige Haupt-Build kompilierte diese Tests; die spätere Version 1.54 enthält die weiterentwickelten Scanner- und Sicherheitsprüfungen.
- **Integrationsstand 1.54 / versionCode 55:** Der aktuelle Android-Build [45722303637](https://github.com/mouzzlii/Fuck/actions/runs/45722303637) und der API-35-Emulatorlauf [45722303671](https://github.com/mouzzlii/Fuck/actions/runs/45722303671) sind für Commit `25558994d553a509ea7733fd478b5153a539ff94` erfolgreich. Geprüft wurden Unit-Tests, Kompilierung der Android-Instrumentierungstests, Android-Lint, Manifest-/Offline-Sicherheitsgrenzen, APK-Build und **8/8 Emulator-Instrumentierungstests**. Die Debug-APK-Prüfsumme wurde lokal gegen die Artefakt-Prüfsummendatei verifiziert.
- Debug-Artefakt: `TRAKUNA-Android`, Artifact-ID `15072192619`, verfügbar bis 09.12.2026, 23:26 UTC. Die enthaltene `SHA256SUMS.txt` enthält die Prüfsumme der APK. Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen.
- Der Integrationsstand liegt in Draft-PR #25 und ist noch nicht in `main` übernommen. Reale Kamera-/Autofokusprüfung mit Paketetiketten, Drucker-/Portal-End-to-End-Abnahme sowie manuelle Backup-Wiederherstellung bleiben offen. Zebra/DataWedge ist nur vorhandener Legacy-Code und keine Freigabebedingung der neuen Zielarchitektur.
- **Nachintegration PR #35 – Code-CI-Stand:** `a372591b131888a144f59b5780ccab7730d6db18`. Vollständiger Android-Build/Unit-Tests/Lint/Manifest-/Offline-Prüfungen erfolgreich (Run [45883226923](https://github.com/mouzzlii/Fuck/actions/runs/45883226923)); API-35-Emulator-Instrumentierung erfolgreich (Run [45883226995](https://github.com/mouzzlii/Fuck/actions/runs/45883226995)). Spätere Commits haben ausschließlich Dokumentation aktualisiert; diese CI-Läufe sind kein Testnachweis für die Dokumentations-Commits. PR #25 bleibt Draft und ungemergt; reale Kamera-/Autofokusprüfung, Drucker-/WLAN-/Bluetooth-Abnahme und Backup-Restore auf einem echten Gerät bleiben offen.

**Wichtig:** Der aktuelle Integrationsstand ist ein Debug-Testpaket, kein produktiv signiertes Release. Debug-Signaturen können sich zwischen CI-Läufen unterscheiden; Android kann dann ein Update über eine vorhandene Installation verweigern. Vor jedem Update eine passwortgeschützte `.trakuna`-Sicherung erstellen und das Passwort getrennt aufbewahren. Die App nicht deinstallieren, solange keine verifizierte Sicherungsdatei vorliegt. CSV-Exporte bleiben Klartext. Für wiederholbare Updates ist ein dauerhaft vom Betreiber verwalteter Signaturschlüssel erforderlich. Erfolgreiche CI-Tests ersetzen keine Tests auf den tatsächlichen Shopgeräten.

## Aktuelle Betriebsgrenzen

**Aktueller Pilot-Sicherheitsmodus:** Die Partnerübergabe ist fest auf SHADOW MODE geschaltet. Die Sendungsnummer wird lokal im geschützten TRAKUNA-Bestand abgelegt; die Übergabe wird simuliert. Es wird keine externe Carrier-/Partner-App geöffnet, keine Sendungsnummer in die Zwischenablage kopiert und keine Buchung oder Labelbestellung ausgelöst. Das lokale Testprotokoll enthält nur redigierte Ereignismetadaten, keine Sendungsnummer. Eine Freigabe des Live-Handoffs erfordert eine separate geprüfte Änderung und Betreiberfreigabe.

- Die Daten werden lokal auf diesem Gerät gespeichert und verschlüsselt.
- Es gibt derzeit keine zentrale Synchronisierung zwischen mehreren Geräten und keine Cloud-Verbindung.
- WLAN ist für lokale Kamera-/manuelle Scans nicht erforderlich und aktiviert keine Synchronisierung. Der künftige lokale Druckweg soll WLAN oder Bluetooth nutzen; dafür ist noch kein Druckcode freigegeben oder real getestet. Version 1.49 zeigt den WLAN-Verbindungsstatus dieses einzelnen Geräts an; andere Geräte werden nicht gesucht.
- Vorhandener DataWedge-Empfangscode bleibt bis zur gesonderten Abhängigkeitsprüfung im Bestand. Er ist keine Voraussetzung und keine neue Hardwareanforderung des aktuellen Smartphone-/Portal-/Druckablaufs. Zebra-Handgeräte dienen darin ausschließlich als Internet-/Portalgeräte; keine neue Zebra-Scanner-, DataWedge- oder Drucker-Hardwareintegration dafür planen.
- DPD, GLS, Hermes und UPS waren die ersten lokalen Auswahlprioritäten in Version 1.53. Die Auswahl ist nur eine lokale Kennzeichnung. Das Öffnen eines Anbieterportals bestätigt keine Datenübernahme oder Buchung. Acht Carrier-Profile werden zentral im Gateway dokumentiert; Profile sind keine Live-Integrationen. Offizielle Freigaben und bestätigte Schnittstellen sind je Anbieter weiterhin offen. Zebra-Handgeräte sind in der neuen Zielarchitektur ausschließlich Internet-/Portalgeräte; tatsächliche Modelle und freigegebene Integrationsmöglichkeiten bleiben zu inventarisieren. Bestehende Carrier-Systeme und Shopgeräte werden nicht eigenmächtig verändert.
- Die vorhandenen Carrier-Systeme, Handgeräte und Druckerkopplungen dürfen ohne ausdrückliche Betreiber- und Carrier-Freigabe nicht verändert werden.

## Release-Build

Der CI-Workflow kann zusätzlich ein signiertes Release-APK erstellen, sobald der Betreiber die vier Signierungs-Secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` und `ANDROID_KEY_PASSWORD` in den GitHub-Repository-Secrets hinterlegt hat. Der private Keystore bleibt außerhalb des Repositorys. Ohne diese Betreiber-Schlüssel wird bewusst nur die Debug-Testversion veröffentlicht.

## Integrations- und Sicherheitsdokumentation

### Verbindliche Zielarchitektur ab 26.11.2026

- Ein Android-Smartphone ist die zentrale Bedienoberfläche für alle unterstützten Carrier.
- Ein gemeinsamer Etikettendrucker wird direkt vom Smartphone aus genutzt; WLAN und Bluetooth sind die vorgesehenen Verbindungswege, abhängig von der tatsächlichen Druckerkompatibilität.
- Die offiziellen Carrier-Portale bleiben nutzbar. Labels dürfen nur über offizielle APIs oder dokumentierte Download-/Export-/Teilen-Funktionen übernommen werden.
- Zebra-Handgeräte dienen in diesem Ablauf ausschließlich als Internet-/Portalgeräte. Keine neue Zebra-Scanner-, DataWedge- oder Drucker-Hardwareintegration dafür planen.
- Vorhandener DataWedge-Code bleibt bis zur gesonderten Diff-/Abhängigkeitsprüfung erhalten, ist aber kein Bestandteil der neuen Zielstrecke.
- Kein zusätzlicher PC, Cloud-Printserver oder separater Drucker ist vorausgesetzt.
- WLAN-/Bluetooth-Druckcode, Druckerkompatibilität und reale Etikettenabnahme sind noch nicht umgesetzt.
- Ein angenommener Druckauftrag ist keine physische Druckbestätigung. Unklare Timeouts bleiben UNKNOWN und dürfen nicht automatisch einen zweiten Druck auslösen.

### Aktueller Integrationsnachweis

Der offene Android-PR [#25](https://github.com/mouzzlii/Fuck/pull/25) enthält den aktuellen Integrationsstand auf Head `a372591b131888a144f59b5780ccab7730d6db18`. Android-Build, Unit-Tests, Lint, Manifest-/Offline-Prüfungen und API-35-Emulator-Instrumentierung liefen erfolgreich (Runs [45883226923](https://github.com/mouzzlii/Fuck/actions/runs/45883226923) und [45883226995](https://github.com/mouzzlii/Fuck/actions/runs/45883226995)). PR #25 bleibt Draft und ungemergt; reale Kamera-/Druckerhardware, Backup-Restore auf echtem Gerät und Carrier-API-Freigaben sind nicht abgenommen.


- [Hardwareinventar des ersten Paketshops](HARDWARE-INVENTAR.md): vor Ort erfasste DPD-/GLS-/Hermes-Geräte sowie offene UPS-Hardwareangaben, Verbindungen, Versionsangaben und Prüfpunkte.
- [WLAN- und Startvorbereitung](WLAN-STARTVORBEREITUNG.md): historische Hinweise zum früheren Geräte-/DataWedge-Ablauf; nicht als Zielarchitektur des neuen Smartphone-Portalablaufs verwenden.
- [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md): aktueller Build, Testablauf, Zielarchitektur, Integrationsvoraussetzungen und Produktionssperren.
- [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md): historische Recherche; die zentrale Carrier-Registry im Gateway ist die aktuelle Architekturquelle.
- [Anfrage zur Carrier-Freigabe](ANFRAGE-CARRIER-FREIGABE.md): vorbereitete Partneranfrage mit konkreten Fragen für DPD, GLS, Hermes und UPS in Österreich.
- [Partner-Schnittstelle und Abnahmekriterien](PARTNER_INTEGRATION.md): ältere Übergabe-/Integrationsnotizen; vor Umsetzung mit dem zentralen Gateway-Adaptervertrag abgleichen.
- [Sicherheitsstatus und Produktionsfreigabe](SECURITY.md): dokumentiert umgesetzte Schutzmaßnahmen, bekannte Restrisiken und verbindliche Freigabekriterien.
- [Sichere Mehrgerätearchitektur](MEHRGERAETE-ARCHITEKTUR.md): Zielmodell für Geräteidentität, Rollen, Offline-Warteschlange, Ereignissynchronisierung und Konfliktbehandlung; noch nicht implementiert.
- [Mehr-KI-Arbeitsprotokoll](MEHR-KI-ARBEITSPROTOKOLL.md): gemeinsamer GitHub-Arbeitsablauf, Aufgabenpakete, unabhängige Reviews, Zugangsstatus und Kosten-/Datenschutzgrenzen.
- [Betriebshandbuch](BETRIEBSHANDBUCH.md): einfache Schritt-für-Schritt-Anleitung für Paketannahme, Kundenausgabe, Retouren, Zebra DataWedge, Partner-App und verschlüsselte Datensicherung.
- [Rechtehinweis](PROPRIETARY-NOTICE.md): Rechtevorbehalt, Drittmaterialien und Schutz vertraulicher Informationen.
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md): verbindliche Nachweise und externe Voraussetzungen vor einem echten Paketshop-Einsatz.
- [Praxis-Testplan](PRAxisTEST.md): Testfälle für den kontrollierten Gerätetest.

## CI-Schutzprüfungen

Der Android-Workflow startet bei App-/Build-Änderungen sowie bei Änderungen am Sicherheitsprüfskript. `scripts/check-android-security-boundaries.sh` kontrolliert vor dem Build die Quellberechtigungen (nur CAMERA und ACCESS_NETWORK_STATE), die ausdrückliche Entfernung von INTERNET, optionale Kamera-Hardware, deaktiviertes Android-Backup, deaktivierten Klartextverkehr und das Fehlen direkter HTTP-/Socket-Client-Abhängigkeiten bzw. bekannter Netzwerk-APIs. Nach dem APK-Bau wird zusätzlich das tatsächlich paketierte Manifest geprüft. Diese Schranke muss bei einer späteren Backend-Einführung bewusst und zusammen mit dem Sicherheitskonzept angepasst werden; sie ersetzt keinen vollständigen Audit transitiver Bibliotheken.

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

---

## Rights and confidentiality

See [PROPRIETARY-NOTICE.md](PROPRIETARY-NOTICE.md) for the bilingual rights notice, third-party materials disclaimer, and rules for confidential information. Do not place unpublished invention details, customer data, passwords, API keys, or private signing keys in this repository.
