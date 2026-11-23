# TRAKUNA – sichere Mehrgerätearchitektur

Stand: 23. November 2026  
Status: Architekturentscheidung vorbereitet – **noch nicht implementiert**.

## 1. Ziel und klare Grenze

DPD-, GLS-, Hermes- und UPS-Arbeitsplätze sollen später denselben Paketshop-Bestand sehen können. Ein WLAN-Symbol auf einem Handgerät beweist weder, dass andere Geräte erreichbar sind, noch dass eine Carrier-App Daten freigibt.

TRAKUNA verwendet deshalb **keine offene Peer-to-Peer-Synchronisierung und keinen ungeschützten HTTP-Server auf einem Zebra-Gerät**. Der sichere Zielweg ist:

1. Jedes freigegebene Android-Gerät arbeitet offline weiter.
2. Das Gerät sendet freigegebene Änderungen über HTTPS/TLS an einen zentralen, authentifizierten Dienst.
3. Der Dienst prüft Identität, Rolle, Paketshop-Zugehörigkeit, Vorgangsregeln und Wiederholungen.
4. Andere berechtigte Geräte erhalten bestätigte Änderungen über denselben Dienst.
5. Carrier-Buchungen bleiben ausschließlich im jeweils autorisierten Carrier-System, bis ein Carrier eine echte Schnittstelle schriftlich freigibt.

Eine Cloud- oder Backend-Verbindung ist eine zusätzliche Voraussetzung. WLAN allein reicht nicht; für einen Cloud-Dienst muss auch Internetzugang vorhanden sein. Ein rein lokaler Server wäre eine getrennte Architekturentscheidung und bräuchte ein dauerhaft betriebenes, abgesichertes Gerät.

## 2. Sicherheitsmodell

### Geräteidentität
- Jedes Gerät erhält eine zufällige, nicht erratbare Geräte-ID.
- Aufnahme in einen Paketshop erfolgt durch einen einmaligen Pairing-Code mit kurzer Gültigkeit und serverseitiger Einmalverwendung.
- Ein Administrator kann ein verlorenes oder ausgemustertes Gerät sperren.
- Gerätegeheimnisse werden nie in Git, Screenshots, QR-Codes mit dauerhafter Gültigkeit oder Chat-Nachrichten abgelegt.
- Die Geräte-ID ist kein Passwort und allein keine Berechtigung.

### Benutzer und Rollen
Vorgesehene Rollen, serverseitig geprüft:

| Rolle | Erlaubnisse |
|---|---|
| Mitarbeiter | Vorgänge erfassen und zulässige Statusschritte ausführen |
| Schichtleitung | Korrekturen und Konfliktprüfung nach dokumentierten Regeln |
| Betreiber/Admin | Geräte verwalten, Benutzer sperren, Exporte und Aufbewahrung verwalten |
| Support | Technische Diagnose mit minimierten Daten; keine automatische Paketfreigabe |

Ein Gerät darf nicht allein aufgrund seiner WLAN-Verbindung Schreibrechte erhalten.

### Transport und Datenspeicherung
- Ausschließlich HTTPS/TLS; kein Klartextverkehr.
- Kurze Zugriffstoken, erneuerbare Sitzungen und Widerrufsmöglichkeit.
- Serverseitige Autorisierung pro Paketshop und Datensatz.
- Lokale Warteschlange verschlüsselt; Schlüssel im Android Keystore.
- Keine Sendungsnummern, Empfängernamen, Tokens oder Notizen in Debug-Logs.
- Backups und Exporte werden als vertrauliche personenbezogene Daten behandelt.
- Aufbewahrungs- und Löschfristen müssen vor Produktivbetrieb festgelegt werden.

## 3. Datenmodell für Synchronisierung

Die Synchronisierung überträgt **Ereignisse**, nicht unkontrolliert komplette lokale Datenbankkopien.

### Shipment
- id: stabile UUID
- shop_id: Paketshop-Zuordnung
- tracking_number: normalisierte Sendungsnummer
- carrier: manuell gewählt oder vorsichtig erkannter Hinweis
- flow: Eingang für Kunden, Kundenabgabe oder Retoure
- status: serverseitig validierter aktueller Status
- created_at, updated_at: UTC-Zeitpunkte

### ShipmentEvent (append-only)
- event_id: UUID als Idempotenzschlüssel
- shipment_id: zugehöriger Vorgang
- shop_id, device_id, user_id
- event_type: Erfassung, Statusänderung, Detailkorrektur, Storno usw.
- from_status, to_status
- client_created_at: Gerätezeit nur als Hinweis
- server_received_at: maßgeblicher Eingangszeitpunkt
- schema_version: Version des Ereignisformats
- reason: verpflichtende Begründung für Korrekturen/Stornos, ohne unnötige Kundendaten

Ereignisse werden nicht stillschweigend überschrieben. Korrekturen erzeugen ein neues Ereignis. Die bisherige lokale Löschfunktion entfernt derzeit einen Datensatz samt Historie; vor einem produktiven Mehrgerätebetrieb muss sie durch einen nachvollziehbaren Storno-/Archivierungsprozess ersetzt werden.

## 4. Offlinebetrieb und Konfliktbehandlung
- Jedes lokale Ereignis erhält vor dem Versand eine UUID.
- Wiederholte Übertragung derselben UUID erzeugt serverseitig keine zweite Buchung.
- Die Warteschlange bleibt erhalten, bis der Server die Speicherung bestätigt.
- Netzwerkfehler führen zu begrenzten Wiederholungen mit wachsendem Zeitabstand.
- Statusübergänge werden serverseitig anhand des erlaubten Workflows geprüft.
- Zwei widersprüchliche Statusänderungen werden nicht per „letzter Schreibvorgang gewinnt“ aufgelöst.
- Konflikte landen in einer sichtbaren Prüfliste für Schichtleitung/Betreiber.
- Eine lokale Erfolgsmeldung darf nicht als serverseitige oder Carrier-Bestätigung ausgegeben werden.

## 5. Carrier-Trennung

| Paketdienst | Geplante Rolle von TRAKUNA | Nicht erlaubt ohne Freigabe |
|---|---|---|
| DPD | Lokale Erfassung und später freigegebene Ereignisübergabe | Bestehende DPD-App, Scannerprofil oder Druckerkopplung ersetzen |
| GLS | Lokale Erfassung und später freigegebene Ereignisübergabe | Bestehende GLS-App oder Druckerkopplung verändern |
| Hermes | Nach Ankunft des angekündigten Nachfolgegeräts dessen Android-/Scanner-Kompatibilität prüfen; TRAKUNA nur auf einem ausdrücklich freigegebenen Gerät betreiben | APK auf dem bisherigen MC3090BT installieren oder den laufenden Hermes-Ablauf ohne Freigabe verändern |
| UPS | Lokale Erfassung nach Erhebung der tatsächlichen Shop-Hardware | Nicht freigegebene UPS-App installieren oder Buchungen simulieren |

Eine Sendungsnummer oder ein Status in TRAKUNA ist keine bestätigte Carrier-Buchung.

## 6. Technische Umsetzungsphasen

### Phase A – vor Backend-Code
- [x] Offline-App und lokale AES-GCM-Speicherung als Ausgangspunkt dokumentiert.
- [x] WLAN-Anzeige ausdrücklich als Status des aktuellen Geräts begrenzt.
- [x] Keine INTERNET-Berechtigung im aktuellen Manifest.
- [ ] Betreiber entscheidet, ob ein zentraler Cloud-Dienst oder ein lokaler Shop-Server zulässig ist.
- [ ] Datenschutz, Verantwortlichkeit, Aufbewahrung und Mitarbeiterzugriff festlegen.
- [ ] Schriftliche Carrier-Freigaben und Testzugänge getrennt pro Paketdienst einholen.

### Phase B – Backend-Grundlage
- [ ] Backend-Projekt und Hosting-Region festlegen.
- [ ] Authentifizierung, Shop-Mandantentrennung und Rollen implementieren.
- [ ] Geräte-Pairing und Gerätewiderruf implementieren.
- [ ] Ereignis-API mit Idempotenz und serverseitigen Statusregeln implementieren.
- [ ] Datenbankregeln, Backups, Monitoring und Datenschutz-Löschkonzept testen.
- [ ] Sicherheitsprüfung der Backend-Endpunkte.

### Phase C – Android-Synchronisierung
- [ ] Lokale persistente Outbox ergänzen.
- [ ] HTTPS-Client mit sicherer Tokenverwaltung ergänzen.
- [ ] Ereignisse idempotent hochladen und Bestätigungen speichern.
- [ ] Änderungen anderer Geräte abrufen.
- [ ] Konfliktanzeige und manuelle Auflösung ergänzen.
- [ ] Migration bestehender lokaler Daten ohne Verlust testen.
- [ ] Offline-, Wiederholungs-, Geräteverlust- und Mehrbenutzertests durchführen.

### Phase D – Pilotabnahme
- [ ] Mindestens zwei freigegebene Android-Geräte im selben Paketshop testen.
- [ ] Gleichzeitige Erfassung derselben Testsendung prüfen.
- [ ] WLAN-Ausfall, Internet-Ausfall, Serverausfall und App-Neustart prüfen.
- [ ] Gerätewiderruf und Mitarbeiterabmeldung prüfen.
- [ ] Datenexport, Wiederherstellung und Löschprozess prüfen.
- [ ] Alle vier Carrier-Prozesse mit freigegebenen Testsendungen separat abnehmen.

## 7. Architekturentscheidung

**Empfehlung:** Ein zentraler HTTPS-Dienst mit mandantenfähiger Datenbank und strikten serverseitigen Regeln ist für mehrere mobile Geräte robuster als ein Zebra-Handgerät als lokaler Server. Ein Dienst wie Supabase kann technisch geprüft werden, ist aber noch nicht als TRAKUNA-Backend ausgewählt oder verbunden. Es werden keine vorhandenen Projekte oder Zugangsdaten ohne ausdrückliche Zuordnung verwendet.

## 8. Aktueller Implementierungsstatus
- Die App ist derzeit ein lokales Offline-System.
- Die WLAN-Anzeige prüft nur die aktive WLAN-Verbindung des gerade laufenden Geräts.
- Es gibt keine Geräteerkennung, keine Synchronisierung, keine Benutzerkonten und keine zentrale Datenbank.
- Die Android-CI-Prüfung hält die INTERNET-Berechtigung, Android-Backup und Klartextverkehr bis zur bewussten Architekturfreigabe auf dem aktuellen Sicherheitsstand.
- Diese Datei ist ein Umsetzungsplan, kein Nachweis einer bereits vorhandenen Mehrgerätefunktion.