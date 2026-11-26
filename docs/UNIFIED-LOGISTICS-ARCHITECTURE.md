# Unified Logistics Platform – Konsolidierungsplan

Stand: 26.11.2026

## Verbindliche Projektentscheidung

**TRAKUNA (Repository `mouzzlii/Fuck`) ist die kanonische Android-Anwendung und das führende Repository für die Paket- und Shopprozesse.** Die bereits vorhandene Scan-Zentrale, lokale verschlüsselte Sendungsverwaltung, Backup-/Wiederherstellungslogik, Zebra-DataWedge-Eingang, Kamera-/ML-Kit-Scan und Tests sind derzeit die reifste tatsächlich sichtbare Implementierung.

Die Multi-Carrier-Gateway-Funktion wird als **eigenständiges Modul innerhalb derselben Produktarchitektur** geführt – nicht als zweites konkurrierendes Produkt. Carrier-Adapter bleiben fachlich getrennt und werden nur über eine gemeinsame Schnittstelle angebunden.

## Zielstruktur

- **TRAKUNA Android:** Scan, manuelle Eingabe, lokale Vorgänge, Shop-Bedienung, Offlinebetrieb, sichere Backups.
- **Carrier Core:** gemeinsame Carrier-Identität, Sendungsnummern-Normalisierung, Status-/Fehler-/Idempotenzmodell.
- **Carrier Adapter:** je Anbieter separat implementierte, offiziell dokumentierte und ausdrücklich freigegebene Schnittstelle.
- **Gateway Service (spätere Ausbaustufe):** serverseitige Authentifizierung und API-Vermittlung, falls echte Online-Integration benötigt und freigegeben wird.
- **Admin/Operations:** Konfiguration, Audit, Support und Integrationsstatus.

Es gibt keine gemeinsame universelle API-Annahme: Jeder Anbieter hat eigene Authentifizierung, Datenmodelle, Berechtigungen und zulässige Vorgänge.

## Auswahl der besseren vorhandenen Lösung

| Quelle | Befund | Entscheidung |
|---|---|---|
| `Fuck` / TRAKUNA | Umfangreiche Android-App mit Kamera-/ML-Kit, DataWedge, verschlüsseltem Speicher, Backups und Unit-/Instrumentierungstests | **Behalten und weiterentwickeln – kanonische Basis** |
| `multi-carrier-gateway` | Sichtbarer Hauptzweck ist Integrations-/Koordinationsdokumentation; private Repo-Version | Konzepte und Dokumentation in die kanonische Architektur übernehmen; keine zweite App pflegen |
| `multi-carrier-gateway-neu` | Großer ZIP-/Prototypstand und einzelne Android-/HTML-Dateien; Inhalt des ZIP ist über die aktuelle Textschnittstelle nicht zuverlässig prüfbar | Als Migrationsquelle vormerken; vor Übernahme entpacken, Lizenz/Dateien/Diffs und Build prüfen |
| `ShopScannerNeu` | Enthält im sichtbaren Main-Tree vor allem ZIP-Artefakt; zusätzlich offener PR zum Verschieben des ZIP aus dem Workflow-Verzeichnis | ZIP als Quelle prüfen; PR nicht ungeprüft übernehmen |
| `Pakete` | README und MainActivity belegen frühen UI-/Tracking-Prototyp; fehleranfällige Zuordnung nach Präfix/Länge | Nicht als Produktbasis verwenden |
| `Paketescanner` | Einfache Carrier-Auswahl, Scan-Normalisierung und URL-Weiterleitung; keine vergleichbare Sicherheits-/Speicherbasis | Nützliche Einzelideen nur nach Tests übernehmen |
| `Pakete-neu` | Main enthält laut aktuellem Tree nur README und Rechtehinweis | Keine eigenständige Implementierung |
| `Pakete-Scanner` | Workflow-/Dokumentationsreste, unter anderem Verweise auf TRAKUNA | Kein separates Produkt; Referenzen konsolidieren |

**Wichtig:** Das ist eine Architektur- und Repository-Konsolidierung. Noch wurden keine fremden ZIP-Inhalte in TRAKUNA importiert und keine Quellrepositorys gelöscht, archiviert oder verändert. Ungeprüfte Dateien werden nicht blind kopiert.

## Migrationsregeln

1. TRAKUNA bleibt die einzige aktive Android-Produktlinie.
2. Gleiche Funktionen werden nur einmal umgesetzt. Vor jeder Übernahme werden Verhalten, Tests, Sicherheit, Lizenz und Wartbarkeit verglichen.
3. Die bessere, getestete Implementierung gewinnt; nützliche Funktionen aus anderen Ständen werden gezielt portiert und anschließend getestet.
4. Keine Änderungen an bestehenden produktiven Carrier-Geräten, Partner-Apps, Druckern oder Konten ohne schriftliche Freigabe.
5. Keine Zugangsdaten, OAuth-Codes, Tokens, API-Schlüssel oder privaten Signaturschlüssel in App, Git oder Logs.
6. Timeouts, Offlinezustände und unbekannte Antworten dürfen niemals als erfolgreiche Buchung gelten.
7. Bestehende Repositories bleiben vorerst unverändert als nachvollziehbare Quellen/Backups. Stilllegung erst nach vollständiger Migration, reproduzierbarem Build, Tests und gesicherter Historie.
8. Alle Änderungen erfolgen auf Feature-Branches und über PRs. `main` und produktive Daten bleiben geschützt.

## Noch offene technische Arbeiten

- ZIP-Artefakte aus `multi-carrier-gateway-neu` und `ShopScannerNeu` sicher entpacken und vollständig gegen TRAKUNA vergleichen.
- Wiederverwendbare Carrier-Modelle/Adaptergrenze aus dem bestehenden TRAKUNA-Code herauslösen, ohne den lokalen Offlinebetrieb zu gefährden.
- Acht Anbieterprofile in der Carrier-Registry pflegen; die ersten vier Shop-Prioritäten bleiben DPD, GLS, Hermes und UPS.
- Für jeden Anbieter offizielle Schnittstelle, Länderfreigabe, Vertragsberechtigung, Testzugang und Rückmeldeverhalten verifizieren.
- Erst nach Freigabe einen Gateway-Service planen; bis dahin bleibt TRAKUNA bewusst lokal/offline.

## Statusdefinition

- **Umgesetzt:** Entscheidung für TRAKUNA als kanonische Basis und diese Konsolidierungsdokumentation.
- **Noch nicht umgesetzt:** tatsächlicher Quellcode-Merge der ZIP-/Prototypstände und technische Gateway-Adapter.
- **Nicht getestet:** vollständiger Build und End-to-End-Tests dieser noch nicht portierten Stände.
