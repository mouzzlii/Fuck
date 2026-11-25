# TRAKUNA — TASKS

Stand: 2026-11-25  
Quelle: Repository-Scan von `main` (Commit `8c826e5d2588c74521e7bcf7e7aa61ab132e0758`) und offene PRs #16–#18.  
Regel: Genau ein Ticket gleichzeitig bearbeiten. Ein Häkchen bedeutet vollständig umgesetzt und mit den für das Ticket erforderlichen Prüfungen nachgewiesen.

## Erledigt

- [x] **T-01 — Projektregeln und Aufgabensteuerung einführen.** Repository-Stack und vorhandene Schutz-/Offline-Vorgaben prüfen; `AGENTS.md` und diese `TASKS.md` ergänzen. Umsetzung auf Branch `chore/repo-governance`; noch nicht in `main` gemergt.

## Offen — priorisiert

- [ ] **T-02 — Scanner-Integrations-PR #18 vollständig prüfen.** Diff und Branch-Basis gegen `feature/scan-diagnostics` prüfen; Wiederherstellung des verschlüsselten Übergabe-Caches, ML-Kit-Format-Allowlist, DataWedge-Formatprüfung und Fokusverhalten auf Regressionen untersuchen. CI für den exakten Head `4d499964e9d807fc3c029088bd3248800e1f2b72` nachweisen. PR #18 ist Draft und noch nicht gebaut/auf Hardware getestet laut PR-Beschreibung. Keine Integration ohne bestandene Prüfungen.

- [ ] **T-03 — Scanner-End-to-End-Abnahme vorbereiten und durchführen.** Testfälle für Kamera-Berechtigung verweigert, fehlende Kamera, leere/ungültige/mehrdeutige Codes, doppelte Scans, Activity-/Prozessneustart, ML Kit und DataWedge dokumentieren. Anschließend auf realen Android- und Zebra-Geräten testen; Emulatorergebnisse nicht als Hardwareabnahme ausgeben.

- [ ] **T-04 — MainActivity schrittweise entflechten.** `app/src/main/java/de/trakuna/app/MainActivity.kt` umfasst auf `main` 71 KB. Verantwortlichkeiten inventarisieren und UI, Scanneradapter, Navigation/Interaktionen sowie Dialog-/Formularlogik in kleine Dateien/Klassen auslagern. Jede Extraktion mit Regressionstests; keine Big-Bang-Umschreibung.

- [ ] **T-05 — Versions- und Release-Dokumentation synchronisieren.** `main` dokumentiert in README/Gradle noch Version 1.53 / versionCode 54; PR #16 dokumentiert 1.54 / 55. Erst nach geklärter Integrationsreihenfolge Gradle, README, Release- und Testdokumentation konsistent aktualisieren. Debug- und signiertes Release-Artefakt klar unterscheiden.

- [ ] **T-06 — Offline-/Sicherheitsregressionen dauerhaft absichern.** CI-Schranken für INTERNET-Berechtigung, Android-Backup, Klartextverkehr, Berechtigungen und Abhängigkeiten prüfen; fehlende Regressionstests ergänzen. Keine Netzwerkfunktion ohne gesonderte Architekturentscheidung.

- [ ] **T-07 — Backup-Wiederherstellung real abnehmen.** Für PR #14 die dokumentierten Emulator-/CI-Ergebnisse von realer Geräteprüfung trennen; Export, Import, falsches Passwort, beschädigte Datei und Gerätewechsel mit Betreiberfreigabe testen. Release-Signaturschlüssel bleiben außerhalb des Repositories.

- [ ] **T-08 — Carrier- und Shop-Hardwareintegration nur nach Freigabe fortführen.** DPD/GLS/Hermes/UPS-Integrationsvoraussetzungen und neues Hermes-Gerät verifizieren. Bestehende Handgeräte, Drucker, Profile und produktive Abläufe bis zu ausdrücklicher Freigabe unangetastet lassen.

## Bekannte offene Pull Requests (Stand des Scans)

| PR | Zweck | Status / wichtige Einschränkung |
|---|---|---|
| [#18](https://github.com/mouzzlii/Fuck/pull/18) | Übergabe-Cache-Wiederherstellung und Scanner-Härtung | Draft; Basis `feature/scan-diagnostics`; CI/Hardwaretest für exakten Head offen |
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

- 2026-11-25: T-01 auf `chore/repo-governance` angelegt. `main` unverändert. PR-Erstellung und CI-Prüfung dieses Dokumentationsbranches stehen noch aus.
