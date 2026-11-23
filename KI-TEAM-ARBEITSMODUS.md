# KI-Team und kostenfreier Entwicklungsmodus

Stand: 23.11.2026

## Ziel

TRAKUNA wird schrittweise mit nachvollziehbarer, unabhängiger Prüfung weiterentwickelt. Externe KI-Dienste dürfen nur eingesetzt werden, wenn ein tatsächlich verfügbarer und autorisierter Zugang besteht. Es werden keine kostenpflichtigen Zugänge, API-Schlüssel, Abonnements oder Budgets ohne ausdrückliche Freigabe eingerichtet.

## Tatsächlich verfügbare Arbeitsmittel

| System | Aktueller Status | Zulässige Verwendung |
|---|---|---|
| ChatGPT | In dieser Arbeitsumgebung verfügbar | Planung, Code-/Dokumentationsprüfung, Tool-Orchestrierung |
| GitHub `mouzzlii/Fuck` | Verbunden | Quellcode, Dokumentation, Commits, Actions und Artefakte |
| Vercel-Team `Kreislauf` | Konto-/Team-Metadaten und öffentlicher AI-Gateway-Modellkatalog lesbar | Vorprüfung von Modellen und Plattformoptionen |
| Gemini | Kein eigener ausführbarer Zugang nachgewiesen | Erst nach freiwilliger, autorisierter Verbindung einsetzen |
| Grok | Kein eigener ausführbarer Zugang nachgewiesen | Erst nach freiwilliger, autorisierter Verbindung einsetzen |
| Bolt | Keine direkte Steuerung/Verbindung nachgewiesen | Kann später über einen freigegebenen GitHub-Workflow eingebunden werden |

**Wichtig:** Ein Modell im öffentlichen Katalog zu sehen bedeutet nicht, dass eine Anfrage an dieses Modell ausgeführt werden kann. Zum Zeitpunkt dieses Dokuments wurden im Vercel-Team keine virtuellen AI-Gateway-Modelle und keine Team-Budgets gefunden. Es wurde kein neuer API-Schlüssel angelegt und keine kostenpflichtige Verbindung aktiviert.

## Arbeitsmodus ohne zusätzliche Kosten

1. GitHub ist die gemeinsame, nachvollziehbare Quelle für Quellcode und technische Entscheidungen.
2. Änderungen werden klein, prüfbar und mit klarer Begründung vorgenommen.
3. Vor Änderungen werden betroffene Dateien und bestehende Verträge/Schnittstellen gelesen.
4. Nach Änderungen werden die verfügbaren automatischen Prüfungen ausgeführt. Ein grüner CI-Lauf wird nur für den tatsächlich geprüften Commit behauptet.
5. Externe Modelle werden nicht als beteiligt dargestellt, solange keine echte Anfrage und Antwort über einen autorisierten Zugang stattgefunden hat.
6. Kostenpflichtige Provider, API-Schlüssel, Abonnements, Budgets und Deployments mit möglichen Kosten benötigen vorher eine ausdrückliche Freigabe.
7. Keine Zugangsdaten, Kundendaten, Sendungsdaten oder privaten Schlüssel in Prompts, Issues, Logs oder Commits ablegen.

## Vorgeschlagene Rollen, sobald mehrere Modelle autorisiert verfügbar sind

- **Koordination:** Aufgaben zerlegen, Akzeptanzkriterien und Zusammenführung verwalten.
- **Implementierung:** Einen klar abgegrenzten Änderungsvorschlag erstellen.
- **Unabhängige Prüfung:** Sicherheits-, Fehler- und Randfallprüfung ohne bloße Wiederholung der Implementierung.
- **Testprüfung:** Tests und reproduzierbare Prüfschritte entwerfen und ausführen.
- **Release-Prüfung:** Prüfen, ob Quellcode, CI-Ergebnis, Artefakt und Dokumentation zum selben Commit gehören.

Mehrere Modellnamen allein bilden noch kein Team. Zusammenarbeit gilt erst dann als erfolgt, wenn getrennte Modellaufrufe, ihre Ergebnisse und die daraus entstandenen Änderungen nachvollziehbar dokumentiert sind.

## Sicherheits- und Betriebsgrenzen für TRAKUNA

- Keine Änderungen an bestehenden DPD-, GLS-, Hermes- oder UPS-Geräten und -Apps ohne Betreiber-/Carrier-Freigabe.
- Keine echten Carrier-Buchungen, Statusmeldungen oder Druckvorgänge vortäuschen.
- Hermes-Nachfolgegerät erst nach Ankunft inventarisieren und mit Freigabe prüfen.
- Keine Produktivfreigabe aus einem Debug-APK ableiten.
- Kein Zugriff auf reale Paketshop- oder Kundendaten für KI-Tests.
- Keine Änderung an Produktionssystemen ohne explizite Freigabe und Rückfallplan.

## Offene Voraussetzungen für ein echtes Multi-KI-Team

- Ein tatsächlich ausführbarer und autorisierter Zugang zu jedem gewünschten Provider.
- Klare Festlegung, welche Daten an externe Provider übertragen werden dürfen.
- Ein kostenloser oder ausdrücklich genehmigter Kostenrahmen.
- Ein technischer Orchestrator, der Aufgaben, Antworten, Fehler und Zeitlimits verwaltet.
- Automatische Tests und menschliche Freigabe vor sicherheits- oder produktionsrelevanten Änderungen.

Bis diese Voraussetzungen erfüllt sind, bleibt der kostenfreie Modus aktiv: vorhandene GitHub-/Vercel-Werkzeuge nutzen, lokale/CI-Prüfungen durchführen und keine externe KI-Verbindung vortäuschen.
