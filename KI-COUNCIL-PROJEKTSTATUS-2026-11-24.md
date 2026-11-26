# KI-Council – Projektübergreifender Status und priorisierte Restarbeiten

Stand: 24.11.2026  
Quelle: aktuelle README- und Statusdokumente der vier verbundenen GitHub-Repositories. Dies ist eine dokumentengestützte Bestandsaufnahme, kein vollständiger Quellcode-Audit und keine Vor-Ort-Abnahme.

## Gesamturteil

Die vier Projekte sind nicht auf demselben Reifegrad. Keines darf pauschal als produktionsfertig bezeichnet werden. Die gemeinsame Koordination ist dokumentiert, aber ein echtes demokratisches Multi-KI-Council ist noch nicht betriebsbereit: In dieser Umgebung sind ChatGPT und GitHub verfügbar; Gemini, Grok und Bolt sind nicht als ausführbare Provider-Verbindungen nachgewiesen. Es wurden daher keine externen KI-Stimmen simuliert oder als echt gezählt.

## Aktueller Stand

| Projekt | Nachweisbarer Stand | Noch nicht freigegeben |
|---|---|---|
| TRAKUNA | Version 1.53 / versionCode 54; CI-Lauf 45293985061 mit Unit-Tests, Instrumentierungstest-Kompilierung, Lint, Manifestprüfung, Debug-APK und Artefakt-Upload erfolgreich. Der separate Emulatorlauf 45295645386 scheiterte vor Testausführung am abgestürzten Instrumentierungsprozess (0 Tests gestartet). Ein überarbeiteter Lauf 45301653970 mit Hardwarebeschleunigung und Diagnoseartefakt läuft derzeit; ein Abschluss ist noch nicht nachgewiesen. Lokale Archivierung, Backup-Schema 2, manuelle und Kamera-Erfassung sind dokumentiert. | Debug-APK ist nicht produktiv signiert. Kein bestätigter Carrier-Datentransfer oder echte Buchung. Hermes-Nachfolgegerät noch nicht inventarisiert. DataWedge und reale Shopgeräte nicht abgenommen. Mehrgeräte-Synchronisierung und Rollen fehlen. |
| SCAN-PROFIT | README weist Version 2.0.66, 155 automatisierte Tests, statischen Build und Smoke-Test aus. Vercel hat einen Production-Deployment-Status READY und Alias https://scan-profit-app.vercel.app bestätigt. | Das Deployment basiert auf Commit bf6af8d; der neuere GitHub-Stand ist noch nicht deployt. Externe Browser-Erreichbarkeit nicht unabhängig bestätigt. Android-Kamera/Installation/Offline-Praxistest offen. Vollständiges bearbeitbares Next.js-/Test-Quellpaket ist laut README noch nicht vollständig im GitHub-Repository. |
| CHRONOS | Milestone 0.5 mit Desktop-Index, SHA-256-Duplikaten, SQLite, EXIF, Thumbnails, lokaler Oberfläche, Backup/Restore und Android-Offline-Scanner dokumentiert. CI-Matrix für Python 3.11–3.13 sowie Android-Build vorhanden. | Keine Authentifizierung/TLS für Netzwerkbetrieb; keine Android-Desktop-Synchronisierung, Android-Backup/Export oder echte Gerätevalidierung. Million-Foto-Lasttest, Android-Prozessabbruch/Wiederherstellung, Verschlüsselung der Backups sowie Security-/DSGVO-Prüfung offen. |
| KREISLAUF | Demo-Entwicklungsstand; zusätzlicher Draft-PR #31 ergänzt direkten und kombinierten Tausch, beidseitige Abschlussbestätigung, gerichteten Karma-Ausgleich und inseratgebundene Chats samt /nachrichten-Inbox. CI auf aktuellem Feature-Head 7d0227475b0922c4e4c85a6841c478ec3a326a3d: 200 bestehende + 72 Feature-/PGlite-Tests, TypeScript, Lint und Build erfolgreich. | PR bleibt Draft, nicht gemergt. Migrationen nur auf isoliertem PGlite ausgeführt. Streitfall-Moderationsworkflow, Push-/E-Mail-Benachrichtigung, ID-Austria-Liveabnahme, produktive DB und Datenschutzabnahme offen. |

## Ergänzende Prüfung seit dem letzten Status
- **CHRONOS:** Ein separates Face-Hardening-0.2-Paket wurde unabhängig geprüft (22 Tests aus entpacktem ZIP bestanden; Python-Kompilierung und Testkopie-/Idempotenzprüfung erfolgreich). Es ist nicht in CHRONOS main integriert und nicht produktionsbereit: keine realen Foto-/Biometrietests, Embeddings nicht zusätzlich verschlüsselt, Token-Verwaltungsoberfläche und Einwilligungs-/Löschworkflow fehlen.
- **TRAKUNA:** CI-Artefakt ZIP und enthaltene Debug-APK von Run 45293985061 wurden heruntergeladen und SHA-256-verifiziert. Das belegt Artefaktidentität, nicht Emulator- oder Shopgeräteabnahme.
- **KREISLAUF:** Draft-PR #31 aktueller Stand 7d0227475b0922c4e4c85a6841c478ec3a326a3d; Run 45302298184 vollständig grün mit 272 Tests (200 bestehende + 72 Feature-/PGlite-Tests), TypeScript, Lint und Build.

## Priorisierte Restarbeiten – unabhängig vom Council-Provider-Ausbau

### P0 – Sicherheits- und Release-Sperren

1. **TRAKUNA:** Gerätetest ausschließlich mit erfundenen Testsendungen; Hardware/OS/Scanner/Drucker inventarisieren. Hermes-Nachfolger erst nach tatsächlicher Ankunft prüfen. Keine Änderung am bestehenden Hermes-Ablauf ohne Betreiberfreigabe. Dauerhaften Signaturschlüssel ausschließlich als geschütztes Repository-Secret durch den Betreiber einrichten; danach signierten Updatepfad und Restore testen.
2. **SCAN-PROFIT:** vollständigen Quellstand in GitHub versionieren und mit dem produktiven Deployment-Commit abgleichen. Vor dem nächsten Deployment Tageslimit/Kostenfreigabe beachten. Externe Erreichbarkeit, Android-Kamera, PWA-Installation und Offline-Fälle auf Zielgeräten testen.
3. **CHRONOS:** API weiterhin nur an Loopback binden. Erst nach Authentifizierung, Autorisierung, TLS, verschlüsselter Sicherung und Datenschutzprüfung Netzwerk-/Mehrbenutzerfunktionen erwägen. Backup enthält private Pfade/Metadaten und ist derzeit nicht verschlüsselt.
4. **KREISLAUF:** Demo mit deaktivierter Authentifizierung ausschließlich lokal bzw. in einem kontrollierten Testnetz betreiben; niemals öffentlich exponieren. Authentifizierung und produktive Datenbank müssen vor Veröffentlichung aktiviert und getestet sein.

### P1 – Funktionsabnahme

- TRAKUNA: Kamera, manuelle Eingabe, DataWedge, Archiv-Wiederherstellung, Backup-Schema 1/2 und alle vier Shopabläufe auf den tatsächlichen Geräten prüfen; Carrier-Buchungen bis zur bestätigten Schnittstelle manuell im offiziellen System durchführen.
- SCAN-PROFIT: vollständigen Quellcode reproduzierbar installieren/builden; Zielgerätetests für Kamera, UPC/CORS, PWA und Offline-Speicherung dokumentieren.
- CHRONOS: kleine und mittlere Testbibliothek, beschädigte Dateien, EXIF-Fallback, Duplikate, Restore und Prozessabbruch auf realen Desktop-/Android-Geräten prüfen; anschließend 100.000 und 1.000.000 Dateien benchmarken.
- KREISLAUF: getrennte Testkonten und vollständige Szenarien für Inserat → Anfrage → Nachricht → Treuhand → Übergabe → Bewertung; Karma-Ledger, Gemeinschaftsmechanik, Moderation und Missbrauchsfälle prüfen.

### P2 – gemeinsame Council-Infrastruktur

1. Eigenständiges Council-Repository anlegen, sobald die GitHub-Verbindung dies erlaubt; die Council-Software nicht in TRAKUNA oder ein anderes Produkt-Repository mischen.
2. Lokale Abstimmungsengine durch Lifecycle-, Rollen-, Audit- und Wiederherstellungstests absichern.
3. Provider-Adapter standardmäßig deaktiviert implementieren; pro Anbieter Datenklassen, Kostenlimit, Rate-Limit, Secrets und Abschaltweg dokumentieren.
4. Erst nach echten unabhängigen Antworten mindestens zweier externer KI-Mitglieder darf ein formeller Council-Beschluss protokolliert werden.
5. GitHub-Änderungen über Branches und Pull Requests führen; automatische Merge-, Release-, Carrier-, Zahlungs- oder Löschaktionen bleiben gesperrt.

## Verbindliche Entscheidungsregeln

- Keine nicht durchgeführten Tests oder externen KI-Stimmen behaupten.
- Keine Secrets, Kundendaten, privaten Foto-Pfade oder echten Sendungsdaten an externe Provider übertragen.
- Keine kostenpflichtigen API-Aufrufe, Deployments oder Abonnements ohne ausdrückliche Kostenfreigabe.
- Keine produktive Freigabe durch bloße CI-Erfolge ersetzen.
- Reversible und isolierte Änderungen bevorzugen; Originaldaten und bestehende Shopabläufe schützen.
- Nutzerfreigabe bleibt für Kosten, Datenmigration, externe Aktionen, Veröffentlichung und irreversible Änderungen erforderlich.

## Nächster objektiver Meilenstein

Ein projektübergreifender Status ist nun dokumentiert. Der nächste belastbare Meilenstein ist nicht eine weitere Behauptung von „Produktionsreife“, sondern je Projekt ein reproduzierbarer Abnahmelauf mit konkreten Artefakten: Commit, CI-Lauf, Testprotokoll, Geräte-/Umgebungsangaben und offene Restrisiken. Die externen Provider-Zugänge und das dedizierte Council-Repository bleiben separate Voraussetzungen.
