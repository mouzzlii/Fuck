# TRAKUNA — TASKS

Stand: 2026-11-25  
Quelle: Repository-Scan von `main` (Commit `8c826e5d2588c74521e7bcf7e7aa61ab132e0758`) und offene PRs #16–#18.  
Regel: Genau ein Ticket gleichzeitig bearbeiten. Ein Häkchen bedeutet vollständig umgesetzt und mit den für das Ticket erforderlichen Prüfungen nachgewiesen.

## Erledigt

- [x] **T-01 — Projektregeln und Aufgabensteuerung einführen.** Repository-Stack und vorhandene Schutz-/Offline-Vorgaben prüfen; `AGENTS.md` und diese `TASKS.md` ergänzen. Umsetzung auf Branch `chore/repo-governance`; noch nicht in `main` gemergt.

## Offen — priorisiert

- [x] **T-02 — Scanner-Integrations-PR #18 technisch prüfen.** Diff und gestapelte Branch-Basis gegen `feature/scan-diagnostics` geprüft; verschlüsselte Cache-Wiederherstellung, ML-Kit-Format-Allowlist, DataWedge-Labeltype-Allowlist, API-34-Absenderprüfung, Fokusverhalten und doppelte Statusbestätigung geprüft. Für den exakten Head `e5991fe713ff68a4cb5c9ebb87a27dfcc3ad173a` sind Android-Build und Emulator-Instrumentierung erfolgreich: [Build](https://github.com/mouzzlii/Fuck/actions/runs/45686066011), [Emulator](https://github.com/mouzzlii/Fuck/actions/runs/45686065956). Debug-Artefakt erstellt; Release-Signierung übersprungen. Echte Zebra-/Kamera-Hardwareabnahme bleibt T-03/T-08 vorbehalten. PR #18 bleibt Draft und wird nicht gemergt.

- [ ] **T-03 — Scanner-End-to-End-Abnahme vorbereiten und durchführen.** Testplan in `PRAxisTEST.md` deckt Kamera-Berechtigung/fehlende Kamera, leere und ungültige Codes, Mehrdeutigkeit, schnelle Doppelscans, Activity-/Prozessneustart, ML-Kit-Allowlist und DataWedge-API-34-Absenderprüfung ab. Vorbereitung abgeschlossen; echte Android-/Zebra-Geräte und Betreiberfreigabe fehlen weiterhin. Hardwareabnahme ist offen und darf nicht durch Emulatorergebnisse ersetzt werden.

- [ ] **T-04 — MainActivity schrittweise entflechten.** Laufende Refaktorierung auf `integrate/handoff-cache-resume` (aktueller Head `ecfc336`): `MainActivity.kt` von 1.223 auf 627 Zeilen reduziert. Kamera/ML-Kit, Scanmodelle/-Parsing, Partner-Handoff, Versandlabels, Sendungskarten/-liste, Diagnosepanel, Shipment-/Backup-Dialoge, Partner-App-Auswahl und Erfassungsformular in eigene Dateien ausgelagert. Statische Referenz-/Importkontrolle bestanden; CI für den neuen Head `3df79fa` wurde durch die GitHub-Verbindung nicht ausgelöst und ist noch offen. T-04 erst nach erfolgreichem Build, Instrumentierung und weiterer UI-/Navigationsprüfung abschließen.

- [ ] **T-05 — Versions- und Release-Dokumentation synchronisieren.** `main` dokumentiert in README/Gradle noch Version 1.53 / versionCode 54; PR #16 dokumentiert 1.54 / 55. Erst nach geklärter Integrationsreihenfolge Gradle, README, Release- und Testdokumentation konsistent aktualisieren. Debug- und signiertes Release-Artefakt klar unterscheiden.

- [ ] **T-06 — Offline-/Sicherheitsregressionen dauerhaft absichern.** CI-Skript `scripts/check-android-security-boundaries.sh` ergänzt: Quellmanifest-Allowlist (CAMERA/ACCESS_NETWORK_STATE), explizite INTERNET-Entfernung, optionale Kamera, Backup-/Klartextschutz, direkte Netzwerk-Client-Abhängigkeiten und bekannte Netzwerk-APIs; nach dem Build wird das paketierte Manifest geprüft. Workflow-Pfadfilter und SECURITY/README-Dokumentation angepasst. Statische Kontrolle bestanden; CI für aktuellen Head `ecfc336` wurde noch nicht ausgeführt. T-06 bleibt bis zu einem erfolgreichen Build offen.

- [ ] **T-07 — Backup-Wiederherstellung real abnehmen.** Für PR #14 die dokumentierten Emulator-/CI-Ergebnisse von realer Geräteprüfung trennen; Export, Import, falsches Passwort, beschädigte Datei und Gerätewechsel mit Betreiberfreigabe testen. Release-Signaturschlüssel bleiben außerhalb des Repositories.

- [ ] **T-08 — Carrier- und Shop-Hardwareintegration nur nach Freigabe fortführen.** DPD/GLS/Hermes/UPS-Integrationsvoraussetzungen und neues Hermes-Gerät verifizieren. Bestehende Handgeräte, Drucker, Profile und produktive Abläufe bis zu ausdrücklicher Freigabe unangetastet lassen.

## Bekannte offene Pull Requests (Stand des Scans)

| PR | Zweck | Status / wichtige Einschränkung |
|---|---|---|
| [#19](https://github.com/mouzzlii/Fuck/pull/19) | Projektregeln und Aufgabensteuerung | Offen; reine Dokumentation; Review/Merge ausstehend |
| [#18](https://github.com/mouzzlii/Fuck/pull/18) | Übergabe-Cache-Wiederherstellung, Scanner-Härtung und Refaktorierung | Draft; Basis `feature/scan-diagnostics`; CI für Vor-Refactor-Head `e5991fe` erfolgreich; aktueller Head `ecfc336` ohne CI-Nachweis; Hardwareabnahme offen |
| [#17](https://github.com/mouzzlii/Fuck/pull/17) | Fokus, Codefilter und Standby-Wiederherstellung | Draft; laut aktuellem PR-Stand nicht mergebar; nicht separat vor #18 integrieren |
| [#16](https://github.com/mouzzlii/Fuck/pull/16) | Scan-Diagnose und GLS-Parserkorrektur | Offen; dokumentierter APK-/CI-Nachweis vorhanden; reale Hardwareabnahme offen |
| [#14](https://github.com/mouzzlii/Fuck/pull/14) | Verschlüsselte Backups | Draft; automatisierte Prüfungen dokumentiert; reale Geräteabnahme und Betreiber-Signierung offen |

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
