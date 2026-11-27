# AKTUELLER STATUSABGLEICH — 26.11.2026, 19:06 Europe/Vienna

> Dieser Abschnitt ist die maßgebliche Aktualisierung. Ältere Angaben darunter sind historische Arbeitsprotokolle; bei Abweichungen gilt dieser aktuelle Abschnitt.

## Aktueller Stand der Integration

| Bereich | Verifizierter Stand | Konsequenz |
|---|---|---|
| Android-App / PR #25 | Offen, Draft, nicht gemergt. Head: `c631fc4f8f977a312f1810101a4805dd386a1124`; 104 Commits, 35 Dateien. | Einziger kombinierter Android-Integrationsstand. Keine parallele Scanner-/Backup-Implementierung beginnen. |
| CI für PR #25 | Für exakt den aktuellen Head: Build [45868812214](https://github.com/mouzzlii/Fuck/actions/runs/45868812214) erfolgreich; Emulator-Instrumentierung [45868812255](https://github.com/mouzzlii/Fuck/actions/runs/45868812255) erfolgreich. Build-Schritte einschließlich Unit-Tests, Android-Lint, Manifest-/Offline-Grenzprüfungen und APK-Build erfolgreich. | Belegt CI auf diesem Commit, nicht reale Kamera, Autofokus, Zebra/DataWedge, Shopabläufe oder Geräte-Backup-Wiederherstellung. Release-Signierung wurde mangels Betreiber-Secrets übersprungen. |
| Governance PR #19 | Offen, nicht gemergt; enthält ausschließlich `AGENTS.md` und `TASKS.md`; GitHub meldet mergeable. | Statusdokumentation wird hier fortgeschrieben; kein Merge ausgeführt. |
| Gateway PR #4 | Offen, Draft, nicht gemergt; Head `3654623632c985db496cf4db58d2bbf4bdafb35d`; sechs Dokumentationsdateien; GitHub meldet mergeable. | Zentrale Architektur-/Carrier-Dokumentation; vor Merge vollständig reviewen. |
| App-Dokumentations-PR #33 | Offen, Draft, nicht gemergt; Head `7869c8aebe1f6217ba03679c31a64abf3fcfa046`; nur README geändert; GitHub meldet mergeable. | Abhängig von Gateway PR #4; nicht vorher mergen. |
| `main` | Unverändert durch diese Aktualisierung. PR #25, #19, #33 und Gateway #4 weiterhin ungemergt. | Hauptzweig und Produktionsdaten bleiben geschützt. |

## Aktueller nächster Arbeitsstand

- **PR #25 — automatisierte CI:** Beide Workflows sind auf dem exakten Head `c631fc4f8f977a312f1810101a4805dd386a1124` erfolgreich abgeschlossen. Der neue DataWedge-Intent-Filter-Test ist Teil dieses Head-Standes. Das ist kein Nachweis einer physischen Zebra-Geräteabnahme.
- **T-03 — reale Scanner-End-to-End-Abnahme:** offen; benötigt ein echtes Android-Gerät, reale Paketetiketten und freigegebene Zebra/DataWedge-Hardware.
- **T-07 — reale Backup-Wiederherstellungsabnahme:** offen; Export, Import, falsches Passwort, beschädigte Datei und Gerätewechsel auf einem realen Android-Gerät prüfen.
- **Release:** Betreiber-Signaturschlüssel und signiertes Produktionsrelease fehlen; Debug-APK nicht als Produktionsrelease ausgeben.
- **PR #4 vor PR #33:** Abhängigkeit bleibt bestehen. Beide Dokumentations-PRs sind offen und ungemergt.
- **Keine Änderungen an `main`, keine Force-Pushes, keine Branch-/Repository-Löschungen, keine Kosten.**
- **Nächster Code-Ticket-Schritt:** bis zur Verfügbarkeit realer Hardware keine vorgetäuschte Abnahme. Vor einem weiteren Code-Ticket zuerst den vollständigen PR #25-Diff auf Regressionen und die neue DataWedge-Filterintegration reviewen; TRAKUNA-Governance verlangt genau ein priorisiertes Ticket gleichzeitig.

---

## Konsolidierte Ticketlage

- **Abgeschlossen/integriert in PR #25:** die früheren Scanner-/Diagnose-/Backup-/CSV-Arbeiten aus den PRs #14, #26, #27–#32 sind im kombinierten Branch enthalten. #14, #26 und #32 wurden nach Vergleich/Integration geschlossen; PR #25 ersetzt die alten parallelen Integrationsstände. Nicht erneut implementieren.
- **T-03 — reale Scanner-End-to-End-Abnahme:** weiterhin offen. Benötigt echtes Android-Gerät, Paketetiketten und freigegebene Zebra/DataWedge-Hardware. Emulatorergebnisse ersetzen diese Abnahme nicht.
- **T-07 — reale Backup-Wiederherstellungsabnahme:** automatisierte Tests sind im kombinierten PR #25 enthalten; Export/Import, falsches Passwort, beschädigte Datei und Gerätewechsel müssen auf einem realen Android-Gerät geprüft werden.
- **T-08 — Carrier-/Shop-Integration:** blockiert bis schriftliche Betreiber-/Carrier-Freigabe, konkrete API-/Geräteunterlagen und autorisierter Testzugang vorliegen. YourGLS-Login ist kein API-Zugang. Keine Login-Automation, kein Scraping und keine Buchungen.
- **T-09 — weitere MainActivity-State-Entflechtung:** zurückgestellt, bis der kombinierte PR #25 geprüft und kontrolliert integriert ist. Keine vorsorgliche Refaktorierung ohne messbaren Wartbarkeitsgewinn und Regressionstests.
- **T-10 — alter PR #16:** als eigenständige Integrationsaufgabe überholt; seine relevanten Scanner-/Diagnosearbeiten sind im kombinierten PR #25 konsolidiert. Nicht separat mergen.
- **ShopScannerNeu-Archivprüfung — statischer Quellcodevergleich abgeschlossen (26.11.2026):** Das archivierte ZIP (SHA-256 `ce76d03be39929e804f0fa49342a22b665df037e57520002285a119565f1c4bf`) enthält zwei verschachtelte Android-Projekte mit 260 bzw. 354 Einträgen. Nach Normalisierung der Paketpfade sind 11 Kotlin-Dateien gemeinsam: 10 sind bytegenau identisch, nur `domain/Carrier.kt` weicht ab; im zweiten Archiv liegt zusätzlich eine zweite, nahezu duplizierte `GatewayViewModel (1).kt`. Beide Android-Teilarchive enthalten keine Android-Testdateien. Das übergeordnete Webprojekt enthält dagegen `src/lib/carriers.test.ts` und weitere Web-/Plattformtests; diese belegen keine Android-Tests.
- **Sicherheits-/Architekturbefund ShopScannerNeu:** Android-Manifest fordert `INTERNET`; `IntegrationClient` sendet Sendungsnummer, Carrier, Zeitstempel und optionale Lieferanten-ID per Webhook und akzeptiert laut ViewModel sowohl HTTP als auch HTTPS. `IntegrationStore` speichert Webhook-URL, API-Key und Lieferanten-ID in unverschlüsselten SharedPreferences; `HistoryStore` speichert Sendungshistorie ebenfalls unverschlüsselt. Netzwerkfehler/HTTP-Ergebnis werden im Client nicht sichtbar an UI/Benutzer zurückgemeldet. Das widerspricht TRAKUNAs verbindlicher Offline-first-/Datenschutzgrenze und ist kein übernahmefähiger Baustein.
- **Qualitätsbefund Carrier-Erkennung:** unbekannte Carrier-IDs fallen in `Carrier.fromId` und in der Webvariante `carrierById` still auf DHL Paket zurück; unbekannte Nummern werden ebenfalls mit niedriger Konfidenz als DHL Paket ausgegeben. Das kann falsche Carrier-Zuordnungen vortäuschen und ist nicht als fertige Lösung zu übernehmen. Die zweite `Carrier.kt`-Variante mischt deutsche Tracking-Ziele mit Österreichischer Post; die AT/DE-URL-Abweichungen sind nicht als verifizierte, ländersichere Implementierung belegt.
- **Lizenz-/Rechteprüfung:** in den untersuchten Projektwurzeln der verschachtelten Android-Archive wurde keine Projekt-Lizenzdatei gefunden. Damit ist keine Wiederverwendungsfreigabe nachgewiesen; keine Codeübernahme oder Kopie in TRAKUNA.
- **Entscheidung:** kein Archivcode wird importiert. Die Carrier-Regeln dürfen nur als Recherchehinweis dienen; jede gewünschte Übernahme müsste unabhängig neu bewertet, länderspezifisch verifiziert, lizenziert und mit Tests abgesichert werden. Der statische Vergleich ist abgeschlossen; Build, Laufzeit und Geräteverhalten der Archive wurden nicht getestet.

## Verbindliche Reihenfolge / Schutzregeln

1. Genau ein priorisiertes TRAKUNA-Ticket gleichzeitig bearbeiten.
2. Vor Codeänderungen den exakten Branch-/Commitstand, vorhandene Implementierungen, Lizenz/Rechte, Datenmodell und Tests vergleichen.
3. Keine ungeprüften Archivdateien übernehmen. Webhook-/Cloud-Funktionen aus Altständen sind nicht mit der Offline-first-Architektur vereinbar, solange keine ausdrückliche Architektur- und Sicherheitsfreigabe besteht.
4. PR #4 vor PR #33 reviewen; PR #33 nicht vor #4 mergen.
5. PR #25 erst nach vollständigem Diff-Review, realer Geräteabnahme, Backup-Praxistest und geklärter Release-Signierung zur Integration freigeben.
6. Keine Änderungen an `main`, keine Force-Pushes, keine Löschung von Repositories/Branches und keine Kosten ohne ausdrückliche Freigabe.

---

# TRAKUNA — TASKS

Stand: 2026-11-25  
Quelle: Repository-Stand `main` (Commit `8c826e5d2588c74521e7bcf7e7aa61ab132e0758`) sowie aktuelle offene PRs #14, #16, #18 und #19 (25.11.2026).  
Regel: Genau ein Ticket gleichzeitig bearbeiten. Ein Häkchen bedeutet vollständig umgesetzt und mit den für das Ticket erforderlichen Prüfungen nachgewiesen.

## Erledigt

- [x] **T-01 — Projektregeln und Aufgabensteuerung einführen.** Repository-Stack und vorhandene Schutz-/Offline-Vorgaben prüfen; `AGENTS.md` und diese `TASKS.md` ergänzen. Umsetzung auf Branch `chore/repo-governance`; noch nicht in `main` gemergt.

## Offen — priorisiert

- [x] **T-02 — Scanner-Integrations-PR #18 technisch prüfen.** Verschlüsselte Cache-Wiederherstellung, ML-Kit-Format-Allowlist, DataWedge-Labeltype-Allowlist, API-34-Absenderprüfung, Fokusverhalten, doppelte Statusbestätigung und Sicherheitsgrenzen geprüft. Für den Code-Stand `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` sind Android-Build und Emulator-Instrumentierung erfolgreich: [Build](https://github.com/mouzzlii/Fuck/actions/runs/45691683747), [Emulator](https://github.com/mouzzlii/Fuck/actions/runs/45691683698). Debug-Artefakt erstellt; Release-Signierung übersprungen. Die nachfolgenden Dokumentationsaktualisierungen auf PR #18 haben neue CI-Läufe ausgelöst; deren Ergebnisse sind T-05 zugeordnet. Echte Zebra-/Kamera-Hardwareabnahme bleibt T-03/T-08 vorbehalten. PR #18 bleibt Draft und wird nicht gemergt.

- [ ] **T-03 — Scanner-End-to-End-Abnahme vorbereiten und durchführen.** Testplan in `PRAxisTEST.md` deckt Kamera-Berechtigung/fehlende Kamera, leere und ungültige Codes, Mehrdeutigkeit, schnelle Doppelscans, Activity-/Prozessneustart, ML-Kit-Allowlist und DataWedge-API-34-Absenderprüfung ab. Vorbereitung abgeschlossen; echte Android-/Zebra-Geräte und Betreiberfreigabe fehlen weiterhin. Hardwareabnahme ist offen und darf nicht durch Emulatorergebnisse ersetzt werden.

- [x] **T-04 — Erste MainActivity-Entflechtung abschließen.** Kamera/ML-Kit, Scanmodelle/-Parsing, Partner-Handoff, Versandlabels, Sendungskarten/-liste, Diagnosepanel, Shipment-/Backup-Dialoge, Partner-App-Auswahl und Erfassungsformular wurden in eigenständige Dateien ausgelagert. `MainActivity.kt` wurde von ca. 1.223 auf 639 Zeilen reduziert. Manifestprüfung bestätigt eine einzige Launcher-Activity; die Oberfläche ist derzeit ein einzelner Compose-Screen mit Dialogen, kein separates Navigationsgraph-System. Android-Build und Emulator-Instrumentierung für den aktuellen PR-Head `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` sind erfolgreich: [Build 45698479245](https://github.com/mouzzlii/Fuck/actions/runs/45698479245), [Emulator 45698479073](https://github.com/mouzzlii/Fuck/actions/runs/45698479073). Die verbleibende Lifecycle-/State-Orchestrierung bleibt als separates T-09 geplant; T-04 deckt die abgeschlossene erste Extraktionsphase ab.

- [x] **T-05 — Versions- und Release-Dokumentation synchronisieren.** Auf PR #18 sind Gradle, README und Praxis-Testplan bereits auf Version 1.54 / versionCode 55. Betriebshandbuch, Pilotpaket und Freigabe-Checkliste wurden ebenfalls aktualisiert und verweisen auf den erfolgreichen Build-/Emulator-Stand `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb`. CI für den neuesten PR-Head `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` erfolgreich: [Build 45698479245](https://github.com/mouzzlii/Fuck/actions/runs/45698479245), [Emulator 45698479073](https://github.com/mouzzlii/Fuck/actions/runs/45698479073). `main` bleibt bis zur kontrollierten PR-Integration auf dem älteren Versionsstand. Debug- und signiertes Release-Artefakt klar unterscheiden.

- [x] **T-06 — Offline-/Sicherheitsregressionen dauerhaft absichern.** CI-Skript `scripts/check-android-security-boundaries.sh` prüft Quellmanifest-Allowlist (CAMERA/ACCESS_NETWORK_STATE), explizite INTERNET-Entfernung, optionale Kamera, Backup-/Klartextschutz, direkte Netzwerk-Client-Abhängigkeiten und bekannte Netzwerk-APIs; nach dem Build wird das paketierte Manifest geprüft. Für Code-Head `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` liefen Build, Sicherheitsprüfungen, Lint und Emulator-Instrumentierung erfolgreich. Die erneute CI für den aktuellen Dokumentations-Head `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` ist erfolgreich (Build 45698479245 und Emulator 45698479073).

- [ ] **T-07 — Backup-Wiederherstellung real abnehmen und Integrationsreihenfolge sichern.** PR #14 (`c70bdad`) ist gegenüber aktuellem `main` auf Stand `8c826e5` (31 Commits ahead, 0 behind); CI-/Emulatorlauf `45647870214` erfolgreich, 7/7 Android-Instrumentierungstests bestanden. Passwortverschlüsselung, falsches Passwort, Manipulation, Unicode und Legacy-Backup-Kompatibilität sind automatisiert abgedeckt. Reale Geräteprüfung (Export, Import, falsches Passwort, beschädigte Datei, Gerätewechsel) bleibt offen. PR #14 überschneidet sich mit PR #18 in `MainActivity.kt`, `app/build.gradle.kts`, `README.md`, `PRAxisTEST.md`, `BETRIEBSHANDBUCH.md` und dem Instrumentierungsworkflow. Nicht unabhängig mergen; vor Integration gezielt auf einen gemeinsamen Feature-Stand zusammenführen und alle Tests erneut ausführen. Release-Signaturschlüssel bleiben außerhalb des Repositories.

- [ ] **T-08 — Carrier- und Shop-Hardwareintegration nur nach Freigabe fortführen.** DPD/GLS/Hermes/UPS-Integrationsvoraussetzungen und neues Hermes-Gerät verifizieren. Bestehende Handgeräte, Drucker, Profile und produktive Abläufe bis zu ausdrücklicher Freigabe unangetastet lassen.

- [ ] **T-09 — Verbleibende MainActivity-State-Orchestrierung prüfen.** Nach der kontrollierten Integration von PR #16/#18 und der Backup-Funktion aus PR #14 die Lifecycle-/Compose-State-Verantwortung aus `MainActivity.kt` weiter trennen. `rememberSaveable`, `ON_RESUME`, DataWedge-Vordergrundprüfung und bestehende Scanner-/Backup-Workflows müssen durch Regressionstests abgesichert bleiben. Nur umsetzen, wenn die Aufteilung die Wartbarkeit messbar verbessert; keine bloße Datei-Verschiebung.

- [ ] **T-10 — PR #16 Merge-Status vor Integration klären.** PR #16 ist 26 Commits vor und 0 hinter `main`; Build und Emulatorläufe für Head `cfc148f` sind erfolgreich. GitHub meldet dennoch `mergeable=true`, aber `mergeable_state=unstable`; Commit-Status-API liefert keine Status-Kontexte, und die erforderlichen Branch-Protection-Details sind mit der aktuellen GitHub-Verbindung nicht lesbar (403). Vor einem Merge erforderliche Statuschecks/Review-Anforderungen mit Repository-Betreiber klären. `main` bis dahin unverändert lassen.

## Bekannte offene Pull Requests (Stand des Scans)

| PR | Zweck | Status / wichtige Einschränkung |
|---|---|---|
| [#19](https://github.com/mouzzlii/Fuck/pull/19) | Projektregeln und Aufgabensteuerung | Offen; reine Dokumentation; Review/Merge ausstehend |
| [#18](https://github.com/mouzzlii/Fuck/pull/18) | Übergabe-Cache-Wiederherstellung, Scanner-Härtung, Sicherheits-CI und Refaktorierung | Draft; Basis `feature/scan-diagnostics`; Code-Head `bb29cdab` mit erfolgreichem Android-Build und Emulatorlauf; Dokumentations-Head `6214d7d` mit erfolgreichem Build 45698479245 und Emulatorlauf 45698479073; Hardwareabnahme offen |
| #17 | Fokus, Codefilter und Standby-Wiederherstellung | Geschlossen, nicht gemergt; Änderungen nach Prüfung in PR #18 integriert bzw. ersetzt |
| [#16](https://github.com/mouzzlii/Fuck/pull/16) | Scan-Diagnose und GLS-Parserkorrektur | 26 Commits ahead / 0 behind `main`; Build und Emulator erfolgreich; GitHub meldet `mergeable_state=unstable`, erforderliche Branch-Protection-Checks vor Merge klären; Hardwareabnahme offen |
| [#14](https://github.com/mouzzlii/Fuck/pull/14) | Verschlüsselte Backups | Draft; 31 Commits ahead / 0 behind aktuelles `main`; CI + 7/7 Emulator-Instrumentierungstests erfolgreich; reale Geräteabnahme offen; Dateiüberschneidungen mit PR #18 erfordern gemeinsame Integration |

## Scan-Befund auf main

- Android/Kotlin-Projekt mit einem `:app`-Modul, Jetpack Compose, CameraX, ML Kit, DataWedge und lokaler verschlüsselter Speicherung.
- Gradle Kotlin DSL; AGP 8.7.3, Kotlin 2.0.21, Java 17, compile/target SDK 35, min SDK 26.
- Bestehende CI: `.github/workflows/android.yml` und `.github/workflows/android-instrumentation.yml`.
- Tests: JVM-Unit-Tests und Android-Instrumentierungstests sind vorhanden.
- Hauptdateien u. a. `MainActivity.kt`, `ShipmentStore.kt`, `SecureHandoffCache.kt`, `BackupIo.kt`, `PartnerAppConfig.kt`.
- `AGENTS.md` und `TASKS.md` waren auf `main` zum Scan-Zeitpunkt nicht vorhanden.
- `README.md` auf `main` beschreibt Version 1.53 / 54; PR #16 enthält neueren Stand 1.54 / 55. Das ist bis zur kontrollierten Integration bewusst nicht als Fehler auf `main` geändert.

## Änderungsprotokoll

- 2026-11-25: T-01 auf `chore/repo-governance` umgesetzt und verifiziert; PR #19 eröffnet. `main` unverändert. Dokumentations-PR benötigt keinen Android-Build; GitHub-Review/Merge stehen aus.
- 2026-11-25: T-02/T-06 mit erfolgreichen Build- und Emulatorläufen für Code-Head `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` aktualisiert. T-05: README, Sicherheits-, Partner-, Pilot-, Betriebs- und Freigabedokumente für Version 1.54 / 55 auf PR #18 synchronisiert; CI des aktuellen PR-Heads `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` erfolgreich (Build 45698479245, Emulator 45698479073). T-07: PR #14 statisch geprüft, CI/Emulator 7/7 erfolgreich; Dateiüberschneidungen mit PR #18 dokumentiert, keine unabhängige Integration. Keine Änderungen an `main`.

- 2026-11-25: T-05 auf PR #18 abgeschlossen: Release-/Betriebsdokumente für Version 1.54 / 55 synchronisiert; Android-Build 45698479245 und Emulator-Instrumentierung 45698479073 für Head `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` erfolgreich. PR #18 bleibt Draft wegen offener Geräteabnahme und Integrationsabhängigkeiten.
- 2026-11-25: T-04 erste Extraktionsphase abgeschlossen: MainActivity von ca. 1.223 auf 639 Zeilen reduziert; extrahierte UI-/Scanner-/Partner-Komponenten durch erfolgreichen Build und Emulatorlauf auf aktuellem PR-Head validiert. Restliche State-Orchestrierung als T-09 nach gemeinsamer Integration vorgemerkt.
- 2026-11-25: T-10 ergänzt: PR #16 hat erfolgreiche CI, aber GitHub meldet `mergeable_state=unstable`; Branch-Protection-Anforderungen sind über die aktuelle Verbindung nicht einsehbar. Kein Merge ausgeführt.