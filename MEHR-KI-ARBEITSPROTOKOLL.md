# TRAKUNA – Mehr-KI-Arbeitsprotokoll

Stand: 23.11.2026

## Zweck

Dieses Protokoll macht die Zusammenarbeit mehrerer Entwicklungswerkzeuge über dasselbe GitHub-Projekt nachvollziehbar. Es behauptet nicht, dass Gemini, Grok oder Bolt bereits verbunden sind. Ein Werkzeug gilt erst als Teammitglied, wenn es tatsächlich Zugriff erhalten, eine konkrete Aufgabe bearbeitet und ein überprüfbares Ergebnis geliefert hat.

## Gemeinsamer Arbeitsraum

- Repository: `mouzzlii/Fuck`
- Gemeinsame Referenz: Branch `main`
- Änderungen durch externe Werkzeuge: bevorzugt eigener Branch und Pull Request
- Jede Aufgabe muss auf einen konkreten Commit-Stand bezogen sein.
- Keine parallelen Änderungen an derselben Datei ohne vorherige Abstimmung.
- Keine Zugangsdaten, privaten Schlüssel, echten Sendungsnummern oder Empfängerdaten in Issues, Prompts, Commits oder Logs.

## Rollen und Verantwortlichkeiten

| Rolle | Aufgabe | Erforderlicher Nachweis |
|---|---|---|
| Koordination | Aufgaben schneiden, Abhängigkeiten und Akzeptanzkriterien festlegen | Aufgabenbeschreibung mit Basis-Commit |
| Implementierung | Genau einen abgegrenzten Änderungssatz umsetzen | Branch/PR mit Änderungsübersicht |
| Architekturprüfung | Auswirkungen auf App, Datenmodell und bestehende Abläufe prüfen | Review-Kommentar mit konkreten Fundstellen |
| Sicherheitsprüfung | Berechtigungen, Datenfluss, Datenschutz und Missbrauchswege prüfen | Review mit Risiko und Schweregrad |
| Testprüfung | Tests und reproduzierbare manuelle Prüfschritte prüfen | Testprotokoll mit Ergebnis und Umgebung |
| Release-Prüfung | Quellcode, CI, APK und Prüfsummen dem gleichen Commit zuordnen | Release-Nachweis |

Eine Person oder ein Modell darf mehrere Rollen übernehmen, aber Implementierung und unabhängige Prüfung sollen nach Möglichkeit getrennt sein.

## Standardablauf für jede Aufgabe

1. **Bestandsaufnahme:** betroffene Dateien, vorhandene Schnittstellen, offene Risiken und letzte erfolgreiche CI prüfen.
2. **Aufgabenpaket:** Ziel, Nicht-Ziele, Basis-Commit, betroffene Bereiche, Akzeptanzkriterien und Testbefehle festhalten.
3. **Umsetzung:** kleinste sinnvolle Änderung; keine unaufgeforderten Umbauten.
4. **Selbstprüfung:** Diff lesen, Tests lokal/CI ausführen, Nebenwirkungen dokumentieren.
5. **Unabhängige Prüfung:** mindestens eine zweite Instanz prüft Anforderungen, Sicherheit und Randfälle, sofern sie tatsächlich verfügbar ist.
6. **Korrektur:** konkrete Befunde beheben oder begründet als offen markieren.
7. **Zusammenführung:** nur nach erfolgreicher Prüfung und grüner CI für den exakten PR-Commit.
8. **Nachweis:** Commit, PR, CI-Lauf, Artefakt und bekannte Einschränkungen dokumentieren.

## Aufgabenpaket – Vorlage

Für neue Aufträge gibt es zusätzlich die GitHub-Vorlage [KI-/Entwicklungsauftrag](.github/ISSUE_TEMPLATE/ki-arbeitsauftrag.yml). Sie erzwingt Basis-Commit, Ziel, Nicht-Ziele, Akzeptanzkriterien, Tests und Sicherheitsgrenzen.

Jede an ein externes KI- oder Entwicklerwerkzeug übergebene Aufgabe muss mindestens enthalten:

- **Aufgabe:** ein klarer, einzelner Arbeitsauftrag
- **Basis-Commit:** vollständiger Git-SHA
- **Kontext:** relevante Dateien und vorhandene technische Verträge
- **Ziel:** beobachtbares gewünschtes Verhalten
- **Nicht-Ziele:** was ausdrücklich unverändert bleiben muss
- **Akzeptanzkriterien:** überprüfbare Bedingungen
- **Tests:** konkrete automatisierte und manuelle Prüfschritte
- **Sicherheitsgrenzen:** Daten, Berechtigungen, Geräte und Systeme, die nicht verändert werden dürfen
- **Ergebnis:** Branch/PR, geänderte Dateien, Tests, offene Punkte

## TRAKUNA-spezifische Schutzgrenzen

- Bestehende DPD-, GLS-, Hermes- und UPS-Geräte, Carrier-Apps, Scanner und Drucker bleiben unverändert, solange keine ausdrückliche Betreiber- und Carrier-Freigabe vorliegt.
- Keine Buchung, Annahme, Ausgabe, Retoure, Fahrerübergabe oder Druckbestätigung simulieren oder als echt ausgeben.
- Keine erfundenen Carrier-APIs, App-Paketnamen, Gerätefähigkeiten oder Testresultate.
- Keine Netzwerkberechtigung, Cloud-Synchronisierung, Benutzerverwaltung oder Backend-Verbindung ohne dokumentierte Architektur- und Sicherheitsprüfung ergänzen.
- Keine echten Kunden- oder Sendungsdaten an externe KI-Anbieter übertragen.
- Änderungen an Speicherformaten, Verschlüsselung, Berechtigungen oder Paketstatus benötigen gezielte Migrationstests und Sicherheitsprüfung.
- Ein grüner CI-Build beweist weder die Funktion auf den realen Zebra-Geräten noch die Freigabe durch einen Paketdienst.

## Kosten- und Zugangsregel

Der aktuell genehmigte Modus ist kostenfrei. Es dürfen keine kostenpflichtigen Abos, API-Aufrufe, Budgets, Deployments oder Schlüssel eingerichtet werden. Ein öffentlich sichtbarer Modellkatalog ist kein ausführbarer Zugang. Externe Provider werden erst aktiviert, wenn der Nutzer selbst den offiziellen Zugang autorisiert und ein kostenfreier Nutzungsweg samt möglicher Limits nachvollziehbar bestätigt ist. Zugangsdaten werden niemals im Chat abgefragt oder im Repository gespeichert.

## Wann ein Werkzeug als verbunden gilt

Ein Tool gilt nur dann als **verbunden und einsatzbereit**, wenn alle Punkte erfüllt sind:

1. Autorisierter Zugriff auf den vorgesehenen Arbeitsraum ist bestätigt.
2. Ein ungefährlicher Lesetest wurde erfolgreich durchgeführt.
3. Eine isolierte Testaufgabe kann auf einem eigenen Branch bearbeitet werden.
4. Das Ergebnis ist als Diff/PR einsehbar.
5. Das Tool kann seine Tests und Grenzen nachvollziehbar ausweisen.
6. Es ist geklärt, welche Daten übertragen werden und ob Kosten entstehen können.

Statuswerte: `nicht verfügbar`, `Zugang angefragt`, `verbunden – Lesetest offen`, `verbunden – Schreibtest offen`, `einsatzbereit`. Keine Höherstufung ohne Nachweis.

## Aktueller Status

- ChatGPT: in dieser Umgebung verfügbar; koordiniert und nutzt die verbundenen Werkzeuge.
- GitHub: verbunden; Repository lesen und dokumentierte Änderungen durchführen.
- Vercel: Team-/Projektmetadaten und Modellkatalog lesbar; daraus folgt keine aktive Modell-API-Verbindung.
- Gemini: kein ausführbarer Zugang in dieser Umgebung nachgewiesen.
- Grok: kein ausführbarer Zugang in dieser Umgebung nachgewiesen.
- Bolt: keine direkte Steuerung oder bestätigte Projektverbindung in dieser Umgebung nachgewiesen.

## Nächste sinnvolle Ausbaustufen

1. **Jetzt, ohne neue Kosten:** GitHub als gemeinsame Quelle nutzen, Aufgabenpakete und Reviews dokumentieren, CI für jede relevante Änderung prüfen.
2. **Wenn der Nutzer selbst verbindet:** Gemini/Grok/Bolt jeweils einzeln mit Lesetest, Testbranch und Kosten-/Datenschutzprüfung aufnehmen.
3. **Erst nach separater Freigabe:** einen Orchestrator ergänzen, der Aufgaben an autorisierte Provider verteilt, Antworten protokolliert, Zeitlimits/Fehler behandelt und Review-Ergebnisse zusammenführt.
4. **Vor produktivem Einsatz:** Rechte minimieren, Secrets in geschützter Umgebung verwalten, Datenminimierung und Löschfristen definieren sowie Missbrauchs- und Ausfalltests durchführen.

**Qualitätsprinzip:** Nicht „perfekt“ behaupten. Fertig ist eine Änderung erst, wenn ihre Akzeptanzkriterien messbar erfüllt, Tests nachvollziehbar bestanden und verbleibende Einschränkungen offen benannt sind.
