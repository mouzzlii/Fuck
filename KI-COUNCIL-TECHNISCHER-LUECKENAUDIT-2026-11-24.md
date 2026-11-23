# KI-Council – Technischer Lücken- und Freigabeaudit

Stand: 24.11.2026, 00:31 CET  
Prüfart: Abgleich der vier aktuellen README-Dateien über die verbundene GitHub-Integration. Kein vollständiger Quellcode-, Geräte- oder Penetrationstest.

## Ergebnis in einem Satz

Die Repositories dokumentieren nützliche Entwicklungsstände und teils erfolgreiche CI-Läufe, aber **keines der vier Produkte ist auf Basis der hier verfügbaren Nachweise vollständig produktionsfreigegeben**. Dieser Audit trennt belegte Funktionen, offene Risiken und konkrete Abnahmekriterien, damit keine Dokumentationsaussage mit einem realen Praxistest verwechselt wird.

## 1. TRAKUNA – Paketshop-App

### Belegt
- README beschreibt Version 1.53 / versionCode 54 und CI-Lauf 45292752377 (Unit-Tests, Instrumentierungstest-Kompilierung, Lint, Manifestprüfung, Debug-APK und Artefakt-Upload erfolgreich; separater Emulator-Workflow 4 für aktuellen Code-Commit noch in Bearbeitung).
- Lokale verschlüsselte Speicherung, Archivpflicht für abgeschlossene Vorgänge und Backup-Schema 2 sind dokumentiert.
- Manuelle Erfassung und Kamera-/DataWedge-Unterstützung sind dokumentiert.

### Freigabesperren
- Vorhanden ist laut README eine **Debug-APK**, kein dauerhaft signiertes Produktionspaket.
- Carrier-Auswahl ist eine lokale Kennzeichnung; echte Carrier-Buchungen oder bestätigte Datenübernahmen sind nicht implementiert/nachgewiesen.
- Das neue Hermes-Gerät ist noch nicht eingetroffen bzw. nicht inventarisiert. Das vorhandene Hermes-System darf nicht verändert werden.
- Vor-Ort-Abnahme auf den tatsächlichen Zebra-/Shopgeräten fehlt.

### Abnahmekriterien
1. Nach Ankunft Hermes-Nachfolger nur passiv inventarisieren: Modell, Android-Version, Scanner, Drucker, installierte Apps, erlaubte Schnittstellen. Keine Änderung am laufenden System ohne Betreiberfreigabe.
2. Auf jedem Zielgerät Kamera, manuelle Eingabe, DataWedge, Druck, Archivieren/Wiederherstellen und Backup-Restore mit ausschließlich synthetischen Testsendungen prüfen.
3. Dauerhaften Signaturschlüssel durch den Betreiber als geschütztes Secret einrichten; Release-APK signieren und Update über eine bestehende Installation testen.
4. Carrier-Integrationen erst nach schriftlicher Partnerfreigabe und dokumentiertem Testkonto als integriert bezeichnen.

## 2. SCAN-PROFIT – Reseller-Cockpit

### Belegt
- README beschreibt Version 2.0.65, local-first Browser-Speicherung, manuelle Eingabe, optionale Barcode-Kamera, Kalkulation, Inventar, Backup/Import und CSV.
- README dokumentiert 154 Tests, statischen Build/Smoke-Test und 7.000 lokale HTTP-Anfragen.
- README nennt ein Vercel-Deployment READY mit Alias `https://scan-profit-app.vercel.app`, basierend auf Commit `bf6af8d`; der neuere Stand sei nicht deployt und externe Erreichbarkeit nicht unabhängig bestätigt.

### Kritische Unklarheit
Die README sagt einerseits, das App-Dokument samt PWA-Shell liege im Repository, andererseits sei das vollständige bearbeitbare Next.js-/Test-Quellpaket noch nicht vollständig in GitHub übertragen. Die Repo-Metadaten melden außerdem Größe 0 KB, obwohl die README abrufbar ist. Damit ist die Vollständigkeit des Repository-Inhalts anhand der verfügbaren GitHub-Antworten **nicht belastbar bestätigt**.

### Abnahmekriterien
1. Git-Tree und Commit-Hash des vollständigen Quellpakets festhalten; erforderliche Quell-, Test-, Lock- und Build-Dateien in einem sauberen Checkout nachweisen.
2. Reproduzierbarer Installations- und Build-Lauf aus einem frischen Checkout; Testprotokoll mit Commit-Hash und Node-Version speichern.
3. Produktiv-Deployment-Commit mit geprüftem Quellcommit abgleichen. Kein Deployment gegen Tageslimit oder Kostenlimit erzwingen.
4. Auf einem realen Android-Zielgerät HTTPS, Kamera, manuelle Eingabe, PWA-Installation, Offline-Verhalten, Backup-Export und Restore testen.
5. Keine Live-Marktpreise oder automatische Inseratveröffentlichung versprechen; diese Grenzen bleiben in der UI sichtbar.

## 3. CHRONOS – privates Fotoarchiv

### Belegt
- README dokumentiert read-only Scan, SHA-256-Duplikate, SQLite-Index, EXIF, Thumbnails, Ähnlichkeitskandidaten, localhost-Dashboard, Desktop-Backup/Restore und Android-Offline-Scanner.
- Ähnlichkeitssuche wird ausdrücklich als unvollständige Kandidatensuche beschrieben; Originale werden nicht gelöscht.
- Die API ist bewusst auf Loopback begrenzt.

### Kritische Risiken
- Backup-ZIP ist nicht verschlüsselt und enthält private Pfade, Metadaten, Hashes, EXIF-Daten und Thumbnails.
- Android-Power-Loss-/Resume-Verhalten, physische Gerätekompatibilität, Million-Datei-Performance und Sync sind nicht validiert.
- API besitzt keine Authentifizierung, Autorisierung oder TLS. Eine Netzwerkfreigabe wäre daher unsicher.

### Abnahmekriterien
1. Bis zur Implementierung und Prüfung von Authentifizierung, Autorisierung und TLS ausschließlich Loopback verwenden.
2. Backups vor Transport mit vertrauenswürdiger Geräte-/Speicherverschlüsselung schützen; Restore in ein neues leeres Zielverzeichnis testen.
3. Fehlerfälle testen: beschädigte Archive, fehlende Dateien, ungültige EXIF-Daten, gesperrte Dateien, Datenträger voll, Prozessabbruch und Wiederaufnahme.
4. Android-Worker auf realen Geräten mit Akkuoptimierung, App-Neustart, Geräte-Neustart und Speicherberechtigungswechsel testen.
5. Erst nach kleineren reproduzierbaren Benchmarks den 100k-/1M-Fototest durchführen; Originaldateien bleiben unverändert.

## 4. KREISLAUF – regionales Tauschsystem

### Belegt
- README kennzeichnet das Produkt ausdrücklich als Demo/Entwicklungsstand.
- Biete/Suche/Verschenke, Inseratdetails, Karte, Karma-Ledger, Profile, Bewertungen, Übergabe-/Treuhand-Statusmodell und responsive Oberfläche sind dokumentiert.
- Demo-Authentifizierung ist deaktiviert; PGlite ist Entwicklungsstandard, Neon kann über `DATABASE_URL` angebunden werden.
- Karma wird als internes Gemeinschaftsguthaben ohne Auszahlbarkeit beschrieben.

### Kritische Risiken
- Gemeinsamer Demo-Nutzer ist keine Benutzeridentität und darf nicht öffentlich exponiert werden.
- Ein Treuhand-Statusmodell ist noch kein sicherer Zahlungs-/Escrow-Dienst. Es darf keine echte Geldverwahrung suggerieren.
- Reale Nutzerkonten, ID Austria, produktive Datenhaltung, getrennte Rollen, Moderationsabläufe und Ende-zu-Ende-Abnahme sind nicht nachgewiesen.

### Abnahmekriterien
1. Demo nur lokal oder in kontrolliertem Testnetz betreiben; vor öffentlicher Exposition Authentifizierung erzwingen.
2. Produktive Datenbank, Migrationen, Backups, Restore und Zugriffspolicies mit getrennten Testkonten validieren.
3. Szenario vollständig testen: Inserat → Anfrage → Nachricht → Zustimmung → Übergabe-/Treuhandstatus → Abschluss → Bewertung; Abbruch und Streitfall einschließen.
4. Karma-Ledger auf Idempotenz, Parallelität, negative Salden, Rundung, Missbrauch und Auditierbarkeit testen.
5. Moderationsrechte, Mehr-Augen-Prinzip, Einspruch, Sperre und Protokollierung testen.
6. Begriffe wie „Treuhand“ nur so verwenden, dass klar bleibt: kein regulierter Geldverwahrungsdienst und keine Nutzer-zu-Nutzer-Geldzahlung.

## 5. Gemeinsame Council- und Release-Regeln

- Ein CI-Erfolg ist kein Geräte-, Sicherheits-, Datenschutz- oder Produktionsnachweis.
- Jede Freigabe benötigt mindestens: Commit-SHA, Testumgebung, konkrete Testfälle, Ergebnis, Artefakt-Hash und offene Restrisiken.
- Kein Provider-Votum ohne echte, unabhängig abgerufene Provider-Antwort. Simulationen sind als Simulationen zu markieren und zählen nicht zum Quorum.
- Keine Secrets oder realen Kunden-, Sendungs- oder Foto-Metadaten an externe KI-Provider senden.
- Kostenpflichtige API-Aufrufe, Deployments, Datenmigrationen, Carrier-Aktionen und irreversible Änderungen benötigen vorherige ausdrückliche Freigabe.
- Änderungen an Produktcode gehören in das jeweilige Produkt-Repository und einen nachvollziehbaren Branch/PR. Dieses Audit ist nur ein Governance-Artefakt und ersetzt keine Codeänderung.

## Priorität der nächsten Arbeit

1. **SCAN-PROFIT:** Quellpaket-Vollständigkeit und Deploy-Commit-Widerspruch auflösen.
2. **TRAKUNA:** Hermes-Nachfolger nach tatsächlicher Ankunft inventarisieren; parallel Signier- und Geräteabnahme vorbereiten.
3. **CHRONOS:** Backup-Vertraulichkeit und Restore-/Abbruchtests härten; Netzwerkzugriff gesperrt halten.
4. **KREISLAUF:** Demo-Exposition verhindern; getrennte Konten, produktive Datenbank und vollständigen Transaktionsablauf abnehmen.
5. **Council:** lokale Abstimmungsengine erst nach Codezugriff mit Lifecycle-, Rollen-, Audit- und Wiederherstellungstests weiterentwickeln. Kein fertiger Mehr-KI-Betrieb, solange Provider-Adapter und echte Stimmen fehlen.
