# TRAKUNA – Android Scan-Zentrale

<div align="center">
  <img src="https://raw.githubusercontent.com/mouzzlii/logo-M-glichkeiten-/main/1795643953832.jpg" alt="W-Signatur – Blau, Original" width="180" />
  <br />
  <sub>W — Master Emblem · Blau · Technologie</sub>
  <br />
  <sub>Originaldatei unverändert · nur proportionale Skalierung · <a href="https://github.com/mouzzlii/logo-M-glichkeiten-">verbindliche Logo-Regeln</a></sub>
</div>


TRAKUNA 1.53 ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Aktueller Entwicklungsstand – Version 1.53 (versionCode 54)

- Neue Archivfunktion für abgeschlossene Vorgänge: Archivieren, Archiv anzeigen und Wiederherstellen.
- Offene Sendungen können nicht archiviert werden. Endgültiges Löschen wird erst in der Archivansicht angeboten. Die Speicherschicht erzwingt dieselbe Archivpflicht unabhängig von der Oberfläche.
- Archivstatus und Archivereignisse bleiben in der verschlüsselten lokalen Datenhaltung und in JSON-Backups erhalten. Backup-Schema 2 wird geschrieben; ältere Schema-1-Sicherungen bleiben lesbar.
- CSV-Export enthält jetzt zusätzlich den Archivierungszeitpunkt.
- JSON-Backups werden kompakt exportiert; der Import akzeptiert bis zu 4 MiB, während der verschlüsselte lokale Bestand auf 2 MiB begrenzt bleibt. Damit bleibt zusätzlicher Platz für Backup-Kopf und portable Exportstruktur.
- Version 1.53 ergänzt Android-Instrumentierungstests für Keystore-Verschlüsselung, Wiederöffnung des lokalen Speichers, Fail-closed-Verhalten bei beschädigtem Sendungs- und Übergabe-Cache sowie Wiederherstellung aus einem gültigen Backup nach Beschädigung des lokalen Speichers. CI-Lauf 45293985061 für versionCode 54 war erfolgreich: Unit-Tests, Kompilierung der Android-Instrumentierungstestfälle, Android-Lint, Manifestprüfungen, APK-Build und Artefakt-Upload bestanden. **Die Emulator-Instrumentierungstests laufen separat**; Workflow 45295645386 für den aktuellen Code-Commit ist gestartet, aber noch nicht abgeschlossen. Die optionale Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen; bereitgestellt wurde eine Debug-APK.
- Debug-APK-SHA-256: `ce7ed6093e213af7167e5bb2ebfc9c97fd89123fc4231f82fb94b8a4e8143f2b`.
- ZIP-Artefakt-SHA-256: `b2857b0eeb9c452f929ecc9fb2f1258de768897e7f9e26c0eb7912c8c45b6b59`.
- Das ZIP-Artefakt wurde am 24.11.2026, 00:05 UTC erstellt und läuft am 08.12.2026, 00:05 UTC ab.
- CI-Build: https://github.com/mouzzlii/Fuck/actions/runs/45293985061

**Wichtig:** Version 1.53 ist ein Debug-Testpaket, kein produktiv signiertes Release. Debug-Signaturen können sich zwischen CI-Läufen unterscheiden; Android kann dann ein Update über eine vorhandene Installation verweigern. Vor jedem Update zuerst ein JSON-Backup erstellen und dessen Wiederherstellbarkeit prüfen. Die App nicht deinstallieren, solange keine verifizierte Sicherung vorliegt. Für wiederholbare Updates ist ein dauerhaft vom Betreiber verwalteter Signaturschlüssel erforderlich. Ein erfolgreicher CI-Build ersetzt keinen Test auf den tatsächlichen Shopgeräten.

## Aktuelle Betriebsgrenzen

- Die Daten werden lokal auf diesem Gerät gespeichert und verschlüsselt.
- Es gibt derzeit keine zentrale Synchronisierung zwischen mehreren Geräten und keine Cloud-Verbindung.
- WLAN ist für den lokalen Kamera-/DataWedge-Scan nicht erforderlich und aktiviert keine Synchronisierung. Version 1.49 zeigt den WLAN-Verbindungsstatus dieses einzelnen Geräts an; andere Geräte werden noch nicht gesucht.
- Vorhandener DataWedge-Code bleibt bis zur gesonderten Abhängigkeitsprüfung im Bestand. Er ist keine Voraussetzung und keine neue Hardwareanforderung des aktuellen Smartphone-/Portal-/Druckablaufs.
- DPD, GLS, Hermes und UPS waren die ersten lokalen Auswahlprioritäten in Version 1.53. Eine Carrier-Auswahl ist nur eine lokale Kennzeichnung; das Öffnen eines Anbieterportals bestätigt keine Datenübernahme oder Buchung. Acht Carrier-Profile werden zentral im Gateway dokumentiert; Profile sind keine Live-Integrationen. Offizielle Freigaben und bestätigte Schnittstellen sind je Anbieter weiterhin offen. Zebra-Handgeräte dienen im neuen Ablauf ausschließlich als Internet-/Portalgeräte. Ihre konkreten Modelle und die freigegebenen Integrationsmöglichkeiten bleiben zu inventarisieren; bestehende Shopgeräte und Carrier-Systeme werden nicht eigenmächtig verändert.
- Die vorhandenen Carrier-Systeme, Handgeräte und Druckerkopplungen dürfen ohne ausdrückliche Betreiber- und Carrier-Freigabe nicht verändert werden.

## Aktualisierte Zielarchitektur: ein Smartphone, ein Drucker (26.11.2026)

Die neueste Betreiberentscheidung präzisiert den künftigen Paketportal- und Etikettendruckablauf:

- Ein Android-Smartphone ist die zentrale TRAKUNA-Bedienoberfläche.
- Ein einziger Etikettendrucker soll die Labels aller vorgesehenen Paketdienste ausgeben.
- WLAN und Bluetooth sind als Druckverbindungen vorzusehen; die tatsächliche Nutzbarkeit hängt vom konkreten Druckermodell, dessen Firmware und Android-Unterstützung ab.
- Die Paketdienst-Portale werden auf dem Smartphone verwendet. TRAKUNA übernimmt fertige Etiketten nur über offizielle APIs, Downloads, den Android-Teilen-Dialog oder andere dokumentierte Übergabewege.
- Der Druck soll direkt vom Smartphone aus funktionieren. Ein zusätzlicher PC, Cloud-Druckdienst oder separater Printserver ist keine Voraussetzung.
- Zebra-Handgeräte sind für diesen Portalablauf keine von TRAKUNA gesteuerten Scanner oder Druckserver. Sie können allenfalls als Internet-/Browsergeräte für Portale dienen. Neue DataWedge-, Zebra-SDK- oder Zebra-Hardwareintegration ist für diesen Ablauf nicht vorgesehen.

**Abgrenzung zum bestehenden Code:** Version 1.53 enthält weiterhin lokale Kamera-/DataWedge-Funktionen und ältere Hardwaredokumentation. Diese beschreiben den vorhandenen Entwicklungsstand bzw. den bisherigen Betrieb, nicht die neue Zielarchitektur. Bestehende Funktionen werden nicht unkontrolliert entfernt. Offene PRs mit Scanner-/DataWedge-Änderungen sind vor einer Übernahme gegen die neue Zielrichtung zu prüfen.

**Noch nicht umgesetzt oder abgenommen:** zentraler Smartphone-Druckcode, WLAN-/Bluetooth-Erkennung und -Druck, Kompatibilität eines konkreten Ein-Drucker-Modells, automatische Labelübergabe aller Anbieter sowie physische Ende-zu-Ende-Drucktests. Die bestehende Offline-Sicherheitsgrenze (keine INTERNET-Berechtigung) darf für Netzwerkdruck nicht stillschweigend gelockert werden; mögliche Berechtigungsänderungen benötigen eine gezielte Sicherheitsprüfung und passende CI-Anpassung.

## Release-Build

Der CI-Workflow kann zusätzlich ein signiertes Release-APK erstellen, sobald der Betreiber die vier Signierungs-Secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` und `ANDROID_KEY_PASSWORD` in den GitHub-Repository-Secrets hinterlegt hat. Der private Keystore bleibt außerhalb des Repositorys. Ohne diese Betreiber-Schlüssel wird bewusst nur die Debug-Testversion veröffentlicht.

## Integrations- und Sicherheitsdokumentation

### Verbindliche Zielarchitektur ab 26.11.2026

- Ein Android-Smartphone ist die zentrale Bedienoberfläche für alle unterstützten Carrier.
- Ein gemeinsamer Etikettendrucker wird direkt vom Smartphone aus genutzt; WLAN und Bluetooth sind die vorgesehenen Verbindungswege, abhängig von der tatsächlichen Druckerkompatibilität.
- Die offiziellen Carrier-Portale bleiben nutzbar. Labels dürfen nur über offizielle APIs oder dokumentierte Download-/Export-/Teilen-Funktionen übernommen werden.
- Zebra-Handgeräte dienen in diesem Ablauf nur als Internet-/Portalgeräte. Keine neue Zebra-Scanner-, DataWedge- oder Drucker-Hardwareintegration dafür planen.
- Vorhandener DataWedge-Code bleibt bis zur gesonderten Diff-/Abhängigkeitsprüfung erhalten, ist aber kein Bestandteil der neuen Zielstrecke.
- Kein zusätzlicher PC, Cloud-Printserver oder separater Drucker ist vorausgesetzt.
- WLAN-/Bluetooth-Druckcode, Druckerkompatibilität und reale Etikettenabnahme sind noch nicht umgesetzt.
- Ein angenommener Druckauftrag ist keine physische Druckbestätigung. Unklare Timeouts bleiben UNKNOWN und dürfen nicht automatisch einen zweiten Druck auslösen.

### Aktueller Integrationsnachweis

Der offene Android-PR [#25](https://github.com/mouzzlii/Fuck/pull/25) enthält den aktuellen Integrationsstand auf Head `a372591b131888a144f59b5780ccab7730d6db18`. Android-Build, Unit-Tests, Lint, Manifest-/Offline-Prüfungen und API-35-Emulator-Instrumentierung liefen erfolgreich (Runs [45883226923](https://github.com/mouzzlii/Fuck/actions/runs/45883226923) und [45883226995](https://github.com/mouzzlii/Fuck/actions/runs/45883226995)). PR #25 bleibt Draft und ungemergt; reale Kamera-/Druckerhardware, Backup-Restore auf einem echten Gerät und Carrier-API-Freigaben sind nicht abgenommen.


- [Hardwareinventar des ersten Paketshops](HARDWARE-INVENTAR.md): vor Ort erfasste DPD-/GLS-/Hermes-Geräte sowie offene UPS-Hardwareangaben, Verbindungen, Versionsangaben und Prüfpunkte.
- [WLAN- und Startvorbereitung](WLAN-STARTVORBEREITUNG.md): historische Hinweise zum früheren Geräte-/DataWedge-Ablauf; nicht als Zielarchitektur des neuen Smartphone-Portalablaufs verwenden.
- [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md): aktueller Build, Testablauf, Zielarchitektur, Integrationsvoraussetzungen und Produktionssperren.
- [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md): historische Recherche; die zentrale Carrier-Registry im Gateway ist die aktuelle Architekturquelle.
- [Anfrage zur Carrier-Freigabe](ANFRAGE-CARRIER-FREIGABE.md): vorbereitete Partneranfrage mit konkreten Fragen für DPD, GLS, Hermes und UPS in Österreich.
- [Partner-Schnittstelle und Abnahmekriterien](PARTNER_INTEGRATION.md): ältere Übergabe-/Integrationsnotizen; vor Umsetzung mit dem zentralen Gateway-Adaptervertrag abgleichen.
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

---

## Rights and confidentiality

See [PROPRIETARY-NOTICE.md](PROPRIETARY-NOTICE.md) for the bilingual rights notice, third-party materials disclaimer, and rules for confidential information. Do not place unpublished invention details, customer data, passwords, API keys, or private signing keys in this repository.


## Vereinheitlichte Paket- und Multi-Carrier-Plattform

TRAKUNA ist die gemeinsame Produktlinie. Die Android-App bleibt in diesem Repository; die verbindliche Architektur, Repository-Konsolidierung, Carrier-Registry und Adapterverträge werden zentral im [TRAKUNA Multi-Carrier Gateway](https://github.com/mouzzlii/multi-carrier-gateway) gepflegt:

- [Projektkonsolidierung und Repository-Quellenentscheidung](https://github.com/mouzzlii/multi-carrier-gateway/blob/main/PROJECT-CONSOLIDATION.md)
- [Zentrale Carrier-Registry für DPD, GLS, Hermes, UPS, Österreichische Post, DHL, FedEx und LogoiX](https://github.com/mouzzlii/multi-carrier-gateway/blob/main/docs/CARRIER-REGISTRY.md)
- [Anbieterneutraler Carrier-Adaptervertrag](https://github.com/mouzzlii/multi-carrier-gateway/blob/main/docs/CARRIER-ADAPTER-CONTRACT.md)
- [Smartphone-only-Druckarchitektur](https://github.com/mouzzlii/multi-carrier-gateway/blob/main/docs/SMARTPHONE-UNIFIED-PRINTING.md)
- [Hermes-Systemrecherche Österreich](https://github.com/mouzzlii/multi-carrier-gateway/blob/main/docs/HERMES-SYSTEMRECHERCHE.md)

Die Dokumentation beschreibt Ziel und Freigabegrenzen. Der tatsächliche Quellcode-Merge von ZIP-/Prototypständen ist noch offen; ungeprüfte Archive werden nicht blind importiert. Bestehende Quellrepositorys bleiben bis zur verifizierten Migration erhalten.
