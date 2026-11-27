# KI-Council – gemeinsame Governance für vier Projekte

**Geltungsbereich:** Diese Governance regelt ausschließlich Council-Verfahren, Projektgrenzen und Abnahme für TRAKUNA, SCAN-PROFIT, CHRONOS und KREISLAUF. Die globale Arbeitsverfassung ist [MAINPROMPT 2.0.6](https://github.com/mouzzlii/KI-Arbeitsplatz/blob/main/docs/MAINPROMPT.md); dieses Dokument bildet keine zweite globale Prompt- oder Befehlsquelle.

Stand: 24.11.2026

## Auftrag und Geltungsbereich

Das Council koordiniert die vier eigenständigen Softwareprojekte TRAKUNA, SCAN-PROFIT, CHRONOS und KREISLAUF. Gemeinsame Entscheidungen, Standards und Reviews werden zentral dokumentiert; Quellcode, Daten, Releases und Berechtigungen bleiben zunächst in den jeweiligen Projekt-Repositories getrennt.

**Wichtig:** „Zusammenführen“ bedeutet eine gemeinsame Entwicklungs- und Entscheidungsorganisation, nicht das unüberlegte Verschmelzen der vier Anwendungen zu einem Programm. Eine technische Zusammenlegung erfolgt nur, wenn Architekturprüfung und Tests einen konkreten Vorteil belegen.

## Projektregister

| Projekt | GitHub-Repository | bekannte Ausgangslage |
|---|---|---|
| TRAKUNA | mouzzlii/Fuck | Version 1.53 / versionCode 54; CI-Lauf 45293985061 erfolgreich, Debug-APK und Prüfsumme unabhängig verifiziert. Unit-Tests, Lint, Manifestprüfungen und Instrumentierungstest-Kompilierung erfolgreich; separater Emulator-Workflow 45295645386 für den aktuellen Code-Commit noch in Bearbeitung. Keine bestätigte Carrier-Buchung; Hermes-Nachfolgegerät, DataWedge und Shopgeräte-Abnahme offen. |
| SCAN-PROFIT | mouzzlii/SCAN-PROFIT | README dokumentiert Version 2.0.65, 154 Tests und einen Vercel-READY-Deployment-Alias. Deployment basiert auf älterem Commit; neuerer GitHub-Stand und Android-Praxistest offen. |
| CHRONOS | mouzzlii/CHRONOS | Milestone 0.5 mit Desktop- und Android-Prototyp, lokaler API, Backup/Restore und CI. Authentifizierung, Android-Gerätetests, Verschlüsselung und Großlasttests offen. |
| KREISLAUF | mouzzlii/Kreislauf | Demo mit Marktplatz, Karma-Ledger, Profilen, Übergabe-/Treuhandmodell und PGlite. Keine öffentliche Freigabe; echte Konten, Ende-zu-Ende-Abnahme und produktive Datenhaltung offen. |

Die laufend aktualisierte, dokumentengestützte Restarbeitenliste liegt in [KI-COUNCIL-PROJEKTSTATUS-2026-11-24.md](KI-COUNCIL-PROJEKTSTATUS-2026-11-24.md). Dieses Register ist eine Momentaufnahme der eingesehenen Repository-README-Dateien, keine vollständige technische Prüfung aller Branches oder Artefakte.

## Council-Mitglieder und Rollen

### Stimmfähige KI-Mitglieder – Zielbild
- ChatGPT: Koordination, Architekturentwürfe, Aufgabenzerlegung und Protokollführung.
- Anthropic / Claude: bevorzugter unabhängiger Gegenentwurf und Review, sobald ein realer autorisierter Zugang eingerichtet und getestet ist.
- Meta / Muse oder ein echtes Meta-Llama-Modell: dritter unabhängiger Gegenentwurf und Review; Meta Muse und Llama sind technisch getrennte Providerwege und werden nicht gleichgesetzt.
- Gemini und Grok können später als zusätzliche Perspektiven hinzukommen, sofern der Nutzer dies für den Council freigibt und reale autorisierte Zugänge nachgewiesen sind.

Jede KI hat genau eine Stimme. ChatGPT erhält als Koordinator kein zusätzliches Stimmgewicht.

### Werkzeuge und Ausführung
- Bolt wird zunächst als Entwicklungs-/Umsetzungswerkzeug geführt, nicht als zusätzliche Stimme. Eine Stimme kann später nur vergeben werden, wenn ein eigenständiger, überprüfbarer KI-Beitrag und eine klare Trennung von Ausführung und Bewertung bestehen.
- GitHub ist die gemeinsame, versionierte Arbeits- und Nachweiszentrale; GitHub selbst ist kein KI-Mitglied.
- CI, Build-Systeme und Testgeräte liefern Prüfnachweise, aber keine Stimmen.

**Ist-Status:** ChatGPT und die GitHub-Verbindung sind in dieser Arbeitsumgebung verfügbar. Ein tatsächlicher ausführbarer Claude-, Meta-Muse-, Llama-, Gemini-, Grok- oder Bolt-Zugang ist hier nicht nachgewiesen. Diese Dienste gelten daher ausdrücklich noch nicht als verbunden oder einsatzbereit.

## Demokratisches Entscheidungsverfahren

### 1. Problembrief
Vor jeder Abstimmung wird ein neutraler Problembrief mit Ziel, Ausgangsstand, Nicht-Zielen, Randbedingungen, Kostenrahmen, Datenschutzgrenzen und messbaren Akzeptanzkriterien erstellt.

### 2. Unabhängige Vorschläge
Jedes tatsächlich verfügbare stimmberechtigte Mitglied erstellt zunächst unabhängig einen Lösungsvorschlag. Vorschläge enthalten:
- Architektur/Änderung und Begründung
- Vorteile und Nachteile
- Risiken und Gegenmaßnahmen
- Aufwand, laufende Kosten und externe Abhängigkeiten
- Test- und Rollback-Plan
- offene Annahmen und Quellen/Nachweise

Nicht verfügbare Mitglieder werden als „keine Stimme – Zugang fehlt“ protokolliert. Ihre Stimme wird nicht simuliert.

### 3. Kritikrunde
Die Vorschläge werden wechselseitig geprüft. Kritiker müssen konkrete Fehler, Risiken oder bessere Alternativen nennen. Danach darf jedes Mitglied seinen Vorschlag einmal überarbeiten.

### 4. Abstimmung
Jedes verfügbare stimmberechtigte Mitglied gibt genau eine begründete Stimme ab: Option A/B/C oder Enthaltung. Eine Enthaltung zählt nicht als Zustimmung.

- Bei einfacher Mehrheit gewinnt die Option mit den meisten Ja-Stimmen, sofern sie alle zwingenden Qualitäts- und Sicherheitsgates erfüllt.
- Bei Gleichstand folgt eine weitere Runde mit gezielter Klärung der strittigen Punkte.
- Bleibt der Gleichstand bestehen, wird keine technische Änderung automatisch freigegeben. Der Council dokumentiert die Optionen und empfiehlt eine reversible Testentscheidung oder legt die Grundsatzfrage dem Nutzer vor.
- Minderheitenvoten und verworfene Risiken bleiben im Entscheidungsprotokoll sichtbar.
- Eine Abstimmung ist nur gültig, wenn mindestens zwei stimmberechtigte Mitglieder tatsächlich teilgenommen haben. Ist nur eines verfügbar, kann es eine Empfehlung, aber keinen Council-Beschluss geben.

### 5. Harte Freigabegates – nicht überstimmbar
Eine Mehrheitsentscheidung darf niemals:
- Datenschutz, Einwilligung, Rechte Dritter oder geltendes Recht missachten;
- Geheimnisse, Passwörter, API-Schlüssel, private Schlüssel oder Kundendaten in Prompts, Issues, Commits oder Logs offenlegen;
- echte Buchungen, Zahlungen, Carrier-Aktionen, Löschungen oder externe Veröffentlichungen ohne passende Autorisierung auslösen;
- Sicherheitsprüfungen, erforderliche Tests oder belegte Betreiber-/Partnerfreigaben durch bloße Behauptungen ersetzen;
- Kosten, Abonnements, API-Verbrauch, Deployments oder kostenpflichtige Dienste ohne vorherige ausdrückliche Freigabe aktivieren.

Der Nutzer bleibt Eigentümer und letzte Instanz bei Grundsatzentscheidungen, Kosten, Konten, Datenmigration, Veröffentlichung und irreversiblen Aktionen.

## Gemeinsamer Entwicklungsablauf

1. Bestand und Basis-Commit im jeweiligen Repository prüfen.
2. Abhängigkeiten zwischen den vier Projekten dokumentieren.
3. Ein klar abgegrenztes Aufgabenpaket mit Akzeptanzkriterien erstellen.
4. Wenn möglich, unabhängige Vorschläge und Reviews einholen.
5. Umsetzung in einem eigenen Branch; keine parallelen Änderungen an denselben Dateien ohne Abstimmung.
6. Automatisierte Tests, statische Prüfungen und passende manuelle Gerätetests ausführen.
7. Pull Request mit Diff, Testergebnissen, Risiken und Rollback-Plan erstellen.
8. Erst nach Review und erfolgreicher CI zusammenführen.
9. Release-Artefakt, Commit, Prüfsumme, Testumgebung und bekannte Grenzen dokumentieren.

Keine Tests, externen KI-Antworten, Live-Buchungen, Geräteprüfungen oder Produktionsfreigaben behaupten, die nicht tatsächlich stattgefunden haben.

## Gemeinsame Qualitätsbewertung

Jede zulässige Lösung wird anhand dieser Gewichtung bewertet (0–5 Punkte je Kriterium, mit kurzer Begründung):

| Kriterium | Gewicht |
|---|---:|
| Funktionale Korrektheit und Erfüllung der Anforderungen | 25 % |
| Sicherheit, Datenschutz und Rechte | 25 % |
| Zuverlässigkeit und Fehlerbehandlung | 15 % |
| Benutzerfreundlichkeit und Barrierearmut | 15 % |
| Wartbarkeit, Erweiterbarkeit und Testbarkeit | 10 % |
| Kosten, Betriebsaufwand und Abhängigkeiten | 10 % |

Eine hohe Gesamtpunktzahl kann ein nicht bestandenes Sicherheits- oder Rechts-Gate nicht ausgleichen.

## Gemeinsame Architekturprinzipien

- Die vier Anwendungen bleiben eigenständige Produkte mit klaren Verantwortungsgrenzen.
- Gemeinsame Komponenten (z. B. Identität, Benachrichtigungen, Audit-Protokoll oder KI-Orchestrierung) werden nur als optionale, klar versionierte Dienste eingeführt, wenn ein nachgewiesener Bedarf besteht.
- Keine zentrale Datenbank für alle vier Projekte ohne Datenklassifizierung, Eigentümerklärung, Zugriffskonzept, Löschregeln und Migrationsplan.
- Schnittstellen werden versioniert, dokumentiert und mit Vertragstests abgesichert.
- Offline-Fähigkeit und lokale Datenhaltung der bestehenden Projekte dürfen nicht durch eine gemeinsame Plattform unbeabsichtigt verloren gehen.
- Jede Integration muss unabhängig abschaltbar sein und einen sicheren Rückfall auf den bisherigen Projektbetrieb ermöglichen.

## Zugang, Kosten und Datenschutz

Der genehmigte Standardmodus bleibt kostenfrei, bis der Nutzer etwas anderes ausdrücklich freigibt. API-Zugänge müssen vom Nutzer direkt beim jeweiligen Anbieter eingerichtet werden. Schlüssel werden ausschließlich über geschützte Provider-Secrets/Repository-Secrets oder einen Secret-Manager eingebunden, niemals im Chat oder im Git-Repository.

Vor dem Anschluss jedes Anbieters sind zu dokumentieren:
1. Anbieter und konkretes Modell/Produkt
2. erforderliche Berechtigungen und Datenübertragung
3. Kosten, Gratislimits, Rate-Limits und mögliche Abrechnung
4. Speicher-/Aufbewahrungsregeln und Opt-out-Möglichkeiten
5. erfolgreicher Lesetest
6. isolierter Schreibtest in einem Testbranch
7. dokumentierter Fehler- und Abschaltweg

Bis alle Punkte nachgewiesen sind, lautet der Status „nicht verbunden“ bzw. „nicht einsatzbereit“.

## Verbindliche Projekt-Schutzgrenzen

### TRAKUNA
- Bestehende Paketshop-Oberfläche und reale Carrier-Geräte/Apps nicht ohne Freigabe verändern.
- Keine echte Carrier-Buchung oder Statusbestätigung vortäuschen.
- Hermes bleibt eine priorisierte Pflichtintegration für den vorgesehenen Paketshop; technische Voraussetzungen und Betreiber-/Carrier-Freigabe müssen real geklärt werden.
- Lokale Offline-Sicherheit, manuelle Sendungseingabe und Scannerabläufe erhalten, solange keine geprüfte Migration beschlossen ist.

### SCAN-PROFIT
- Keine Live-Preise, Verkaufsdaten, Kamera-/UPC-Funktion oder produktive Bereitstellung als getestet ausgeben, solange Live- und Gerätetests fehlen.
- Vollständigen Quellcode und reproduzierbaren Buildstand zuerst eindeutig dem Git-Repository zuordnen.

### CHRONOS
- Originalfotos niemals automatisch verschieben, verändern oder löschen.
- Ähnlichkeitstreffer bleiben Vorschläge zur menschlichen Prüfung.
- Private Pfade, EXIF-Daten und Vorschaubilder sind schützenswerte Daten; lokale Sicherheitsgrenzen nicht ohne Prüfung öffnen.

### KREISLAUF
- Punkte, Gemeinschaftsfonds, Tausch- und Treuhandvorgänge benötigen nachvollziehbare Buchungsregeln, Auditierbarkeit, Missbrauchsschutz und klare Konfliktverfahren.
- Keine echten Nutzerkonten, Zahlungen oder produktiven Transaktionen ohne explizite Freigabe und Sicherheitsprüfung.
- Bestehende Anforderungen an Marktplatz, Gesuche/Angebote, Nachrichten und mobile Bedienung müssen als Akzeptanzkriterien übernommen werden.

## Entscheidungsprotokoll – Vorlage

Für jeden Council-Beschluss wird eine Datei oder ein Issue mit folgendem Inhalt angelegt:

- ID, Datum und betroffenes Projekt
- Problembrief und Basis-Commit
- verfügbare Mitglieder / fehlende Zugänge
- Vorschläge und Kritik
- Stimmen mit Begründung / Enthaltungen
- Ergebnis und erfüllte Qualitäts-Gates
- beschlossene Änderung und Nicht-Ziele
- Tests, CI-Lauf, PR und Commit
- offene Risiken, Rollback und Verantwortlichkeit

## Noch nicht erledigt / echte externe Voraussetzungen

1. Gemini-Zugang autorisieren und mit Lesetest verifizieren.
2. Grok-Zugang autorisieren und mit Lesetest verifizieren.
3. Bolt-Projektzugriff und genaue Rolle klären; zunächst ohne Stimmrecht.
4. Kosten- und Datenschutzprüfung für jeden Provider abschließen.
5. Eine technische Orchestrierung erst danach implementieren und mit Testaufgaben prüfen.
6. Für jedes der vier Projekte einen vollständigen Bestands- und Abhängigkeitsbericht erstellen.
7. Gemeinsame Komponenten nur nach separater Architekturentscheidung und nachgewiesener Rückwärtskompatibilität bauen.

**Fertig-Kriterium:** Das Council ist erst betriebsbereit, wenn mindestens zwei externe/verbundene stimmberechtigte KI-Mitglieder neben dem Koordinator nachweislich Vorschläge liefern und abstimmen können, die Abstimmungen reproduzierbar protokolliert werden und die Orchestrierung Datenschutz-, Kosten-, Fehler- und Abschalttests besteht. Bis dahin ist dieses Dokument die Governance-Spezifikation, nicht der Nachweis einer bereits bestehenden Multi-KI-Verbindung.
