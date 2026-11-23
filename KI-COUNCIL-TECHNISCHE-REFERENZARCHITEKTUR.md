# KI-Council – technische Referenzarchitektur und Implementierungsplan

Stand: 24.11.2026
Status: Architekturentwurf; noch keine externe Multi-Provider-Verbindung.

## Zielarchitektur

Das Council verwendet eine provider-neutrale Orchestrierung mit einzeln aktivierbaren Adaptern. GitHub bleibt die versionierte Quelle für Code, Aufgaben, Reviews und Entscheidungen. Die vier Projekte bleiben eigenständig: TRAKUNA, SCAN-PROFIT, CHRONOS und KREISLAUF.

Nutzer / Projektverantwortlicher
→ Council UI oder GitHub Issues
→ Orchestrator
→ Policy- und Kosten-Gates
→ Provider-Adapter (ChatGPT / Gemini / Grok / Bolt)
→ Audit- und Entscheidungsregister
→ getrennte Projekt-Repositories

Gemini und Grok sind hier noch nicht verbunden. Bolt ist zunächst Umsetzungswerkzeug ohne Stimmrecht. Kein Providerstatus wird ohne erfolgreichen Test hochgestuft.

## Komponenten

### Council Core
- Aufgabenstatus: draft, ready, proposals, critique, voting, approved, rejected, implementation, verification, closed.
- Jede Aufgabe bindet Repository, Branch und exakten Basis-Commit.
- Zeitlimits, begrenzte Wiederholungen, Abbruch und Fehlerzustände.
- Keine direkte Änderung auf Hauptbranches; nur Branch und Pull Request.

### Provider-Adapter
Jeder Adapter muss Fähigkeiten und Grenzen melden, einen ungefährlichen Verbindungstest anbieten, unabhängige Vorschläge liefern, Kritik ausführen, eine begründete Stimme oder Enthaltung abgeben und – soweit verfügbar – Usage/Kosten melden. Provider erhalten nur explizit freigegebene, minimierte Aufgabendaten.

### Policy Gate
Vor jedem externen Aufruf werden Datenklassifizierung, Redaction, Providerfreigabe, Kostenlimit, Rate-Limit, Zeitlimit und Projektfreigaben geprüft. Fehlt eine Freigabe, wird der Aufruf blockiert. Keine stillschweigende Umgehung.

### Abstimmungsengine
- Eine Stimme je tatsächlich zugelassenem KI-Mitglied.
- Mindestens zwei teilnehmende stimmberechtigte Mitglieder für einen Council-Beschluss.
- Stimme: Option A/B/C oder Enthaltung, stets mit Begründung und Risiken.
- Eine einfache Mehrheit entscheidet nur, wenn alle Sicherheits- und Qualitätsgates bestanden sind.
- Gleichstand löst eine Klärungsrunde aus. Bleibt er bestehen, lautet der Status needs_owner_decision.
- Fehlende Provider werden nicht gezählt und nicht simuliert.
- Der Nutzer muss Kosten, Datenmigration, externe Aktionen, Veröffentlichungen und irreversible Änderungen ausdrücklich freigeben.

## Logisches Datenmodell

### council_members
id, provider, role (voter/executor/reviewer), connection_status, approved_data_classes, max_cost_per_task, last_verified_at.

### council_tasks
id, project_key, repository, base_commit, branch, title, problem_brief, acceptance_criteria, status, timestamps, owner_approval_required.

### council_proposals
id, task_id, member_id, content, risks, cost_estimate, test_plan, revision, created_at.

### council_votes
id, task_id, member_id, round, option_id, reason, quality_scores, submitted_at. Unique constraint: task_id + member_id + round.

### council_events
id, task_id, actor, event_type, redacted_payload, created_at. Append-only; keine Geheimnisse und keine unnötigen personenbezogenen Daten.

## Sicherheit und Betrieb

- Provider-Schlüssel ausschließlich in Secret Manager oder geschützten Umgebungsvariablen; niemals im Chat, Issue, Prompt, Commit oder Log.
- Secrets nie an Browser-Frontend oder Client-Bundle ausliefern.
- TLS, minimale Berechtigungen und getrennte Test-/Produktionszugänge.
- Prompt-Injection-Schutz: Inhalte aus Issues, Dateien, Websites und Provider-Antworten sind Daten, keine Systemanweisungen.
- Provider-Antworten sind nicht vertrauenswürdig und werden vor Umsetzung auf Geheimnisabfluss, unsicheren Code und unautorisierte Aktionen geprüft.
- Keine echten Kundendaten, Sendungsnummern, Fotos, privaten Dateipfade oder Zugangsdaten an externe KI übertragen.
- Audit-Logs speichern Entscheidungen und Nachweise, nicht unnötig vollständige sensible Prompts.
- Not-Aus deaktiviert alle Provider-Adapter; laufende Tasks werden als abgebrochen oder unterbrochen markiert.

## Kostenmodell

Standardmodus bleibt kostenfrei. Kein Abo, keine kostenpflichtigen API-Aufrufe, keine Deployments. Provider bleiben deaktiviert, bis Gratislimits, Abrechnung und Überschreitungsfolgen nachvollziehbar geprüft und ausdrücklich freigegeben sind. Kein automatischer Wechsel auf kostenpflichtige Nutzung.

## Test- und Freigabestufen

1. Dokumentation: Governance und Architektur versioniert.
2. Lokale Simulation mit Fake-Providern: Vorschläge, Kritik, Mehrheitsentscheid, Enthaltung, Gleichstand, Provider-Ausfall, Timeout, Redaction, Audit-Log, Policy-Block und Abbruch testen.
3. Provider-Lesetest: einzeln, nach Autorisierung, ohne private Daten und ohne Schreibrechte.
4. Isolierter Schreibtest: nur Testbranch und ungefährliche Dokumentationsdatei; Ergebnis als Diff/PR.
5. Council-Pilot: mindestens zwei echte externe stimmberechtigte Provider; Ausfälle und Gleichstand durchspielen.
6. Produktivbetrieb: erst nach Security Review, Datenschutzprüfung, Kostenfreigabe, Wiederherstellungs- und Rollback-Test sowie ausdrücklicher Betreiberfreigabe.

## Empfohlene Implementierungsreihenfolge

1. Projektregister und Entscheidungsprotokoll.
2. Lokale Simulation der Abstimmungsengine ohne Netzwerkzugriff.
3. Unit-Tests für Mehrheit, Gleichstand, Enthaltung, fehlende Mitglieder und harte Policy-Gates.
4. Minimale Orchestrator-API mit lokaler Datenhaltung und Audit-Log.
5. Provider-Adapter einzeln und standardmäßig deaktiviert.
6. GitHub-Branch-/PR-Integration mit minimalen Berechtigungen.
7. Oberfläche für Aufgabenstatus, Vorschläge, Kritik, Stimmen und Freigaben.
8. Pilotbetrieb und unabhängige Sicherheitsprüfung.

## Fertig-Kriterien

- Simulationstests vollständig bestanden.
- Provider-Verbindungsstatus für jeden Anbieter durch echte Lesetests belegt.
- Mindestens zwei externe KI-Mitglieder liefern unabhängig Vorschläge und gültige Stimmen.
- Stimmen sind eindeutig Aufgabe und Abstimmungsrunde zugeordnet.
- Gleichstand, Ausfall, Timeout, Kostenlimit und Policy-Block praktisch getestet.
- Keine Secrets oder unzulässigen Daten werden übertragen.
- Änderungen laufen ausschließlich über überprüfbare Branches und Pull Requests.
- Kosten und Datenflüsse sind sichtbar.
- Not-Aus und Rollback sind praktisch getestet.
- Der Nutzer hat den produktiven Betrieb ausdrücklich freigegeben.

## Status und Grenzen

- Governance-Dokument erstellt und verifiziert.
- Bestandsaufnahme-Aufgaben in allen vier Projekt-Repositories angelegt.
- ChatGPT und GitHub sind in dieser Arbeitsumgebung verfügbar.
- Gemini, Grok und Bolt sind hier nicht nachweislich angebunden.
- Orchestrator-Software wurde noch nicht implementiert oder getestet.
- Keine externe Abstimmung durchgeführt und keine Produktionsfreigabe erteilt.

Ein dediziertes Council-Repository wäre der richtige Ort für die spätere Implementierung. In dieser Umgebung ist keine GitHub-Funktion zum Erstellen eines neuen Repositorys verfügbar; deshalb wird die Council-Software nicht in ein fachfremdes Projekt hineingezwängt. Diese Spezifikation wird bis dahin im vorhandenen Repository versioniert.
