# TRAKUNA — TASKS

Stand: 2026-11-25  
Quelle: Repository-Stand `main` (Commit `8c826e5d2588c74521e7bcf7e7aa61ab132e0758`) sowie aktuelle offene PRs #14, #16, #18 und #19 (25.11.2026).  
Regel: Genau ein Ticket gleichzeitig bearbeiten. Ein Häkchen bedeutet vollständig umgesetzt und mit den für das Ticket erforderlichen Prüfungen nachgewiesen.

## Erledigt

- [x] **T-01 — Projektregeln und Aufgabensteuerung einführen.** Repository-Stack und vorhandene Schutz-/Offline-Vorgaben prüfen; `AGENTS.md` und diese `TASKS.md` ergänzen. Umsetzung auf Branch `chore/repo-governance`; noch nicht in `main` gemergt.

## Offen — priorisiert

- [x] **T-02 — Scanner-Integrations-PR #18 technisch prüfen.** Verschlüsselte Cache-Wiederherstellung, ML-Kit-Format-Allowlist, DataWedge-Labeltype-Allowlist, API-34-Absenderprüfung, Fokusverhalten, doppelte Statusbestätigung und Sicherheitsgrenzen geprüft. Für den Code-Stand `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` sind Android-Build und Emulator-Instrumentierung erfolgreich: [Build](https://github.com/mouzzlii/Fuck/actions/runs/45691683747), [Emulator](https://github.com/mouzzlii/Fuck/actions/runs/45691683698). Debug-Artefakt erstellt; Release-Signierung übersprungen. Die nachfolgenden Dokumentationsaktualisierungen auf PR #18 haben neue CI-Läufe ausgelöst; deren Ergebnisse sind T-05 zugeordnet. Echte Zebra-/Kamera-Hardwareabnahme bleibt T-03/T-08 vorbehalten. PR #18 bleibt Draft und wird nicht gemergt.

- [ ] **T-03 — Scanner-End-to-End-Abnahme vorbereiten und durchführen.** Testplan in `PRAxisTEST.md` deckt Kamera-Berechtigung/fehlende Kamera, leere und ungültige Codes, Mehrdeutigkeit, schnelle Doppelscans, Activity-/Prozessneustart, ML-Kit-Allowlist und DataWedge-API-34-Absenderprüfung ab. Vorbereitung abgeschlossen; echte Android-/Zebra-Geräte und Betreiberfreigabe fehlen weiterhin. Hardwareabnahme ist offen und darf nicht durch Emulatorergebnisse ersetzt werden.

- [ ] **T-04 — MainActivity schrittweise entflechten.** Auf `integrate/handoff-cache-resume` wurden Kamera/ML-Kit, Scanmodelle/-Parsing, Partner-Handoff, Versandlabels, Sendungskarten/-liste, Diagnosepanel, Shipment-/Backup-Dialoge, Partner-App-Auswahl und Erfassungsformular ausgelagert. `MainActivity.kt` umfasst am aktuellen geprüften Stand noch 639 Zeilen (Ausgangsstand ca. 1.223). Build und Emulator-Instrumentierung für Code-Head `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` sind erfolgreich. T-04 bleibt offen, bis die verbleibende Activity-Verantwortung und UI-/Navigationsstruktur geprüft und eine weitere Zerlegung fachlich sinnvoll abgeschlossen ist.

- [x] **T-05 — Versions- und Release-Dokumentation synchronisieren.** Auf PR #18 sind Gradle, README und Praxis-Testplan bereits auf Version 1.54 / versionCode 55. Betriebshandbuch, Pilotpaket und Freigabe-Checkliste wurden ebenfalls aktualisiert und verweisen auf den erfolgreichen Build-/Emulator-Stand `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb`. CI für den neuesten PR-Head `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` erfolgreich: [Build 45698479245](https://github.com/mouzzlii/Fuck/actions/runs/45698479245), [Emulator 45698479073](https://github.com/mouzzlii/Fuck/actions/runs/45698479073). `main` bleibt bis zur kontrollierten PR-Integration auf dem älteren Versionsstand. Debug- und signiertes Release-Artefakt klar unterscheiden.

- [x] **T-06 — Offline-/Sicherheitsregressionen dauerhaft absichern.** CI-Skript `scripts/check-android-security-boundaries.sh` prüft Quellmanifest-Allowlist (CAMERA/ACCESS_NETWORK_STATE), explizite INTERNET-Entfernung, optionale Kamera, Backup-/Klartextschutz, direkte Netzwerk-Client-Abhängigkeiten und bekannte Netzwerk-APIs; nach dem Build wird das paketierte Manifest geprüft. Für Code-Head `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` liefen Build, Sicherheitsprüfungen, Lint und Emulator-Instrumentierung erfolgreich. Die erneute CI für den aktuellen Dokumentations-Head bleibt T-05 zugeordnet.

- [ ] **T-07 — Backup-Wiederherstellung real abnehmen und Integrationsreihenfolge sichern.** PR #14 (`c70bdad`) ist gegenüber aktuellem `main` auf Stand `8c826e5` (31 Commits ahead, 0 behind); CI-/Emulatorlauf `45647870214` erfolgreich, 7/7 Android-Instrumentierungstests bestanden. Passwortverschlüsselung, falsches Passwort, Manipulation, Unicode und Legacy-Backup-Kompatibilität sind automatisiert abgedeckt. Reale Geräteprüfung (Export, Import, falsches Passwort, beschädigte Datei, Gerätewechsel) bleibt offen. PR #14 überschneidet sich mit PR #18 in `MainActivity.kt`, `app/build.gradle.kts`, `README.md`, `PRAxisTEST.md`, `BETRIEBSHANDBUCH.md` und dem Instrumentierungsworkflow. Nicht unabhängig mergen; vor Integration gezielt auf einen gemeinsamen Feature-Stand zusammenführen und alle Tests erneut ausführen. Release-Signaturschlüssel bleiben außerhalb des Repositories.

- [ ] **T-08 — Carrier- und Shop-Hardwareintegration nur nach Freigabe fortführen.** DPD/GLS/Hermes/UPS-Integrationsvoraussetzungen und neues Hermes-Gerät verifizieren. Bestehende Handgeräte, Drucker, Profile und produktive Abläufe bis zu ausdrücklicher Freigabe unangetastet lassen.

## Bekannte offene Pull Requests (Stand des Scans)

| PR | Zweck | Status / wichtige Einschränkung |
|---|---|---|
| [#19](https://github.com/mouzzlii/Fuck/pull/19) | Projektregeln und Aufgabensteuerung | Offen; reine Dokumentation; Review/Merge ausstehend |
| [#18](https://github.com/mouzzlii/Fuck/pull/18) | Übergabe-Cache-Wiederherstellung, Scanner-Härtung, Sicherheits-CI und Refaktorierung | Draft; Basis `feature/scan-diagnostics`; Code-Head `bb29cdab` mit erfolgreichem Android-Build und Emulatorlauf; aktuelle Dokumentations-Commits auf Head `6214d7d` haben neue CI ausgelöst (Build 45698479245 und Emulator 45698479073 laufen); Hardwareabnahme offen |
| #17 | Fokus, Codefilter und Standby-Wiederherstellung | Geschlossen, nicht gemergt; Änderungen nach Prüfung in PR #18 integriert bzw. ersetzt |
| [#16](https://github.com/mouzzlii/Fuck/pull/16) | Scan-Diagnose und GLS-Parserkorrektur | Offen; dokumentierter APK-/CI-Nachweis vorhanden; reale Hardwareabnahme offen |
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
- 2026-11-25: T-02/T-06 mit erfolgreichen Build- und Emulatorläufen für Code-Head `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` aktualisiert. T-05: README, Sicherheits-, Partner-, Pilot-, Betriebs- und Freigabedokumente für Version 1.54 / 55 auf PR #18 synchronisiert; CI des aktuellen PR-Heads `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` läuft. T-07: PR #14 statisch geprüft, CI/Emulator 7/7 erfolgreich; Dateiüberschneidungen mit PR #18 dokumentiert, keine unabhängige Integration. Keine Änderungen an `main`.

- 2026-11-25: T-05 auf PR #18 abgeschlossen: Release-/Betriebsdokumente für Version 1.54 / 55 synchronisiert; Android-Build 45698479245 und Emulator-Instrumentierung 45698479073 für Head `6214d7d0cf2203d99fa6c3c5bc4b39d9c6803f37` erfolgreich. PR #18 bleibt Draft wegen offener Geräteabnahme und Integrationsabhängigkeiten.