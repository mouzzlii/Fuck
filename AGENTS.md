# AGENTS.md — TRAKUNA

## Projektziel
TRAKUNA ist eine lokale, offlinefähige Android-App zur Erfassung und Bearbeitung von Paketvorgängen. Die App darf keine Carrier-Buchung oder externe Datenübernahme vortäuschen. Externe Carrier-Geräte und -Abläufe bleiben ohne ausdrückliche Betreiber- und Carrier-Freigabe unverändert.

## Verbindlicher Stack
- Android-App mit Kotlin und Jetpack Compose
- Android Gradle Plugin 8.7.3, Kotlin 2.0.21, JDK 17
- compileSdk/targetSdk 35, minSdk 26
- Lokale verschlüsselte Speicherung; KameraX und ML Kit für Kamera-Scans; Zebra DataWedge als Broadcast-Quelle
- Gradle Kotlin DSL; GitHub Actions für Build, Lint und Tests

## Architekturregeln
1. UI (Compose), Domänen-/Geschäftslogik, Hardware-/Scanneradapter und Persistenz voneinander trennen.
2. Keine neue Funktion direkt in die bereits sehr große `MainActivity.kt` einbauen, wenn sie in eine eigenständige Klasse/Datei gehört. Bestehende Dateien nur schrittweise und mit Regressionstests zerlegen.
3. Neue Kotlin-Dateien erhalten passende Paketnamen unter `de.trakuna.app`; Imports, Lifecycle und Nullability explizit prüfen.
4. Keine Datei oder Methode künstlich kürzen. Keine Platzhalter, TODO-Implementierungen oder erfundenen Testergebnisse.
5. Keine unnötigen Dependencies. Versionen und Android-Berechtigungen begründen und in CI absichern.

## Betriebs- und Sicherheitsregeln
- Offline-Betrieb ist Standard. Keine INTERNET-Berechtigung, Netzwerkzugriffe, Cloud-Synchronisierung oder Telemetrie ohne ausdrücklich beschlossene Architekturänderung und Sicherheitsprüfung.
- Lokale Daten, Backups und Übergabe-Caches müssen verschlüsselt bzw. manipulationsgeschützt bleiben. Fehler beim Lesen/Entschlüsseln fail-closed behandeln.
- Android-Backups und Klartextverkehr bleiben deaktiviert, solange das Sicherheitskonzept nichts anderes freigibt.
- Kamera-/DataWedge-Eingaben validieren; unbekannte Formate und mehrdeutige Sendungsnummern nicht automatisch übernehmen.
- Doppelte aktive Sendungsnummern verhindern. Eingaben bei Activity-/Prozesswiederherstellung nicht ungewollt überschreiben.
- Berechtigungsablehnung, fehlende Kamera, Scanner-Ausfall, leere/fehlerhafte Daten und fehlendes Internet dürfen keinen Absturz verursachen.
- Keine Carrier-App, Zebra-Konfiguration, Druckerkopplung oder Shop-Hardware ohne ausdrückliche Freigabe verändern.
- Keine Passwörter, API-Schlüssel, privaten Keystores, Kundendaten oder vertraulichen Erfindungsdetails committen.

## Git- und Änderungsregeln
- `main` bleibt geschützt. Änderungen ausschließlich in einem thematisch benannten Feature-/Fix-/Chore-Branch.
- Vor Änderungen Repository-Stand, offene PRs, Branch-Basen und vorhandene Implementierungen prüfen.
- Genau ein priorisiertes Ticket gleichzeitig bearbeiten; abhängige Arbeiten erst nach dessen Abschluss beginnen.
- Keine offenen PRs überschreiben oder veraltete Branches ungeprüft integrieren. PR-Basis und Head-SHA vor jeder Integration kontrollieren.
- Kleine, nachvollziehbare Commits; keine Force-Pushes auf gemeinsam genutzte Branches.

## Pflichtprüfung vor Abschluss
- Betroffene Unit-Tests ausführen.
- Android-Lint und `assembleDebug` erfolgreich.
- Instrumentierungstests ausführen, sofern betroffen und Runner verfügbar; sonst als offen kennzeichnen.
- CI-Ergebnisse müssen zum exakten geprüften Commit-SHA gehören.
- APK-Artefakt, SHA-256 und Debug-/Release-Signaturstatus korrekt unterscheiden.
- Reale Kamera-/Zebra-/Carrier-Hardwaretests nie aus Emulator- oder CI-Ergebnissen ableiten.
- Änderungen anhand des Diffs prüfen; keine unbeteiligten Dateien verändern.

## Arbeitsablauf
`TASKS.md` ist die priorisierte Arbeitsliste. Ein Ticket gilt erst als erledigt, wenn Implementierung/Dokumentation, erforderliche Tests und eine nachvollziehbare Prüfung abgeschlossen sind. Statusangaben müssen zwischen geplant, umgesetzt, CI-getestet und real am Gerät getestet unterscheiden.
