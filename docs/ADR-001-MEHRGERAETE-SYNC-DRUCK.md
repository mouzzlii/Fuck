# TRAKUNA – ADR-001: Mehrgerätebetrieb, gemeinsame Datenbasis und zentraler Etikettendruck

- Status: **Vorschlag zur technischen Umsetzung**
- Erstellt: 26.11.2026
- Gültigkeitsbereich: zukünftiger Mehrgeräte-Paketshopbetrieb
- Ausgangsstand: PR #25, Integrations-Head `5620f6f111ad32eca23fab7901853823deacf1fc`
- Sicherheitsgrenze: Der bestehende Pilot bleibt im **SHADOW MODE**. Dieses Dokument aktiviert weder Netzwerkzugriff noch Partnerübergaben, Buchungen oder Druck.

## 1. Entscheidung und Ziel

TRAKUNA soll von mehreren berechtigten Mitarbeiter-Smartphones parallel bedient werden. Alle Geräte arbeiten auf einer gemeinsamen, serverseitig maßgeblichen Datenbasis. Alle Carrier-/Versandprofile verwenden genau eine zentrale Druckwarteschlange und denselben physischen Etikettendrucker.

Zielbild:

```text
Mitarbeiter-Smartphones (Kamera / manuelle Eingabe)
          | HTTPS API / kontrollierte Offline-Synchronisierung
          v
TRAKUNA Backend
  - Identität und Rollen
  - Sendungen und Statusregeln
  - append-only Ereignisjournal
  - Geräte- und Sitzungsverwaltung
  - Outbox / Idempotenz / Konfliktprüfung
          |
          v
Zentrale Drucksteuerung und eine Queue
          |
          v
Ein physischer Etikettendrucker
```

**Nicht Bestandteil dieser ADR:** konkrete Carrier-Buchungs-APIs, produktive Etikettenerstellung ohne Betreiberfreigabe, Auswahl eines kostenpflichtigen Hosters, Hardwarekauf oder Freigabe des Shadow Mode.

## 2. Architekturgrundsätze

1. **Server-authoritative:** Der Server ist die maßgebliche Quelle für globale Sendungszustände. Lokale Geräte dürfen keine globale Bestätigung vortäuschen.
2. **Offline-tolerant, nicht offline-magisch:** Offline erfasste Aktionen werden lokal verschlüsselt vorgemerkt und sichtbar als ausstehend markiert. Erst eine Serverbestätigung macht sie global wirksam.
3. **Idempotent:** Jede schreibende Geräteaktion besitzt eine stabile UUID. Wiederholte Übertragung darf keine doppelte Wirkung erzeugen.
4. **Keine blinde Last-write-wins-Regel:** Statusübergänge werden fachlich validiert. Konflikte werden explizit aufgelöst.
5. **Append-only Ereignisjournal:** Ereignisse werden ergänzt, nicht still überschrieben. Korrekturen sind neue Ereignisse mit Referenz auf den korrigierten Vorgang.
6. **Least privilege:** Mitarbeiter, Supervisoren und Administratoren erhalten getrennte Rechte; jede Anfrage wird serverseitig autorisiert.
7. **Eine Druckqueue:** Alle Mitarbeitergeräte und Carrier-Profile liefern Druckaufträge an dieselbe zentrale Queue. Keine geräte- oder carrier-eigenen Parallelqueues.
8. **Fail-safe bei unklarem Druckstatus:** Ein unklarer Auftrag wird nicht automatisch erneut gedruckt.
9. **Datensparsamkeit:** Sendungsnummern, Empfängerdaten und Notizen gehören nicht in allgemeine Logs, Telemetrie oder Fehlermeldungen.
10. **Kostenlimit:** Entwicklung und Tests zunächst lokal bzw. mit vorhandener kostenloser Infrastruktur. Keine kostenpflichtigen Dienste oder Ressourcen ohne gesonderte Freigabe.

## 3. Vorgeschlagenes Datenmodell

Alle Tabellen verwenden UUID-Primärschlüssel, UTC-Zeitstempel und explizite Fremdschlüssel. Bei einer Installation für mehrere Paketshops ist `shop_id` verpflichtend und Teil jeder Autorisierungs- und Eindeutigkeitsprüfung. Selbst wenn zunächst nur ein Shop existiert, verhindert diese Trennung spätere Datenvermischung.

### 3.1 `shops`

- `id UUID PRIMARY KEY`
- `name TEXT NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL`
- `status TEXT CHECK (status IN ('ACTIVE','SUSPENDED'))`

### 3.2 `staff_users`

- `id UUID PRIMARY KEY`
- `shop_id UUID NOT NULL REFERENCES shops(id)`
- `external_auth_subject TEXT NOT NULL` – stabile ID des Authentifizierungsproviders, niemals ein Passwort
- `display_name TEXT NOT NULL`
- `role TEXT CHECK (role IN ('STAFF','SUPERVISOR','ADMIN'))`
- `status TEXT CHECK (status IN ('ACTIVE','SUSPENDED','REVOKED'))`
- `created_at`, `updated_at TIMESTAMPTZ`
- Unique: `(shop_id, external_auth_subject)`

### 3.3 `device_registrations`

- `id UUID PRIMARY KEY`
- `shop_id UUID NOT NULL`
- `staff_user_id UUID NOT NULL REFERENCES staff_users(id)`
- `device_label TEXT NOT NULL` – frei wählbarer Gerätename, keine unnötige Geräte-Fingerprinting-ID
- `platform TEXT NOT NULL`
- `app_version TEXT NOT NULL`
- `registered_at TIMESTAMPTZ NOT NULL`
- `last_seen_at TIMESTAMPTZ`
- `revoked_at TIMESTAMPTZ NULL`
- `status TEXT CHECK (status IN ('ACTIVE','REVOKED'))`

Gerätezugang ist widerrufbar. Ein Geräte-Token ist geheim, kurzlebig bzw. rotierbar und wird niemals im Klartext protokolliert. Keine gemeinsame statische API-Kennung in der APK.

### 3.4 `shipments`

- `id UUID PRIMARY KEY` – stabile Vorgangs-ID
- `shop_id UUID NOT NULL`
- `tracking_number_ciphertext BYTEA NOT NULL` – verschlüsselt, wenn die Betriebsumgebung dies auf Datenbank-/Anwendungsebene vorsieht
- `tracking_number_fingerprint BYTEA NOT NULL` – keyed HMAC zur Duplikatprüfung; kein einfacher ungesalzener Hash
- `carrier_code TEXT NOT NULL`
- `flow TEXT NOT NULL` – fachlich validierter Vorgangstyp
- `status TEXT NOT NULL` – nur erlaubte Zustandswerte
- `recipient_ciphertext BYTEA NULL`
- `note_ciphertext BYTEA NULL`
- `version BIGINT NOT NULL DEFAULT 1`
- `created_at`, `updated_at TIMESTAMPTZ NOT NULL`
- `created_by_user_id UUID NOT NULL`
- `created_by_device_id UUID NOT NULL`
- `archived_at TIMESTAMPTZ NULL`

Eindeutigkeit aktiver Sendungen wird pro Shop über den keyed Fingerprint abgesichert. Der HMAC-Schlüssel liegt außerhalb der Datenbank in einem geeigneten Secret-/Key-Store. Rotations- und Migrationsverfahren müssen vor Produktivbetrieb festgelegt werden. Die Sendungsnummer darf nicht als Klartext in Indexen, Logs oder Fehlermeldungen auftauchen.

### 3.5 `shipment_events` – append-only

- `id UUID PRIMARY KEY` – vom Client erzeugte Event-ID
- `shop_id UUID NOT NULL`
- `shipment_id UUID NOT NULL REFERENCES shipments(id)`
- `device_id UUID NOT NULL REFERENCES device_registrations(id)`
- `actor_user_id UUID NOT NULL REFERENCES staff_users(id)`
- `event_type TEXT NOT NULL`
- `payload JSONB NOT NULL` – nur erlaubte, validierte Felder; sensible Werte minimieren/verschlüsseln
- `expected_version BIGINT NULL`
- `resulting_version BIGINT NULL`
- `occurred_at_client TIMESTAMPTZ NULL`
- `received_at_server TIMESTAMPTZ NOT NULL`
- `idempotency_key UUID NOT NULL`
- `outcome TEXT NOT NULL`
- Unique: `(shop_id, idempotency_key)`

Der Server setzt Actor und Device aus dem authentifizierten Kontext, nicht aus frei vertrauenswürdigen Client-Feldern. Normale App-Rollen dürfen bestehende Ereignisse nicht ändern oder löschen. Korrekturen erfolgen über neue Korrekturereignisse.

### 3.6 `device_sync_cursors`

- `device_id UUID PRIMARY KEY REFERENCES device_registrations(id)`
- `shop_id UUID NOT NULL`
- `last_acked_event_id UUID NULL`
- `last_server_sequence BIGINT NOT NULL DEFAULT 0`
- `updated_at TIMESTAMPTZ NOT NULL`

Der Cursor ist eine Optimierung, kein alleiniger Beweis für Vollständigkeit. Ereignisse werden anhand stabiler IDs und serverseitiger Sequenzen abgeglichen.

### 3.7 `print_jobs`

- `id UUID PRIMARY KEY` – vom Backend erzeugte, unveränderliche Druckauftrags-ID
- `shop_id UUID NOT NULL`
- `shipment_id UUID NOT NULL REFERENCES shipments(id)`
- `requested_by_user_id UUID NOT NULL`
- `requested_by_device_id UUID NOT NULL`
- `carrier_code TEXT NOT NULL`
- `service_code TEXT NOT NULL`
- `template_version TEXT NOT NULL`
- `payload_reference TEXT NOT NULL` – Referenz auf geschützte Druckdaten, nicht öffentlich abrufbar
- `idempotency_key UUID NOT NULL`
- `status TEXT NOT NULL`
- `attempt_count INTEGER NOT NULL DEFAULT 0`
- `created_at`, `updated_at TIMESTAMPTZ NOT NULL`
- `claimed_at TIMESTAMPTZ NULL`
- `submitted_at TIMESTAMPTZ NULL`
- `printer_reported_at TIMESTAMPTZ NULL`
- `last_error_code TEXT NULL`
- Unique: `(shop_id, idempotency_key)`

### 3.8 `printer_devices` und `printer_queue_events`

`printer_devices` beschreibt genau den freigegebenen zentralen Drucker je Shop: Geräte-ID, Modell, Anschlussart, Queue-Name, Status, letzte Statusmeldung und Wartungs-/Freigabestatus.

`printer_queue_events` protokolliert append-only jede Zustandsänderung eines Druckjobs: Zeitpunkt, Queue-Worker, Drucker-ID, alter/neuer Status, technische Antwort und Fehlercode. Keine vollständigen Etiketteninhalte in diesem Protokoll.

## 4. Zustands- und Konfliktregeln

### 4.1 Sendungen

Die vorhandenen lokalen Statuswerte werden nicht ungeprüft als globale API-Statuswerte übernommen. Vor Implementierung wird eine zentrale Zustandsmaschine erstellt, die pro `flow` erlaubte Übergänge definiert.

Jede Statusänderung enthält:
- `event_id` (UUID)
- `shipment_id`
- `expected_version`
- `requested_transition`
- minimale Zusatzdaten
- Client-Zeit als Diagnosewert, nicht als maßgebliche Reihenfolge

Der Server führt innerhalb einer Datenbanktransaktion aus:
1. Authentifizierung und Rollenprüfung.
2. Prüfung, ob die Event-ID bereits verarbeitet wurde.
3. Laden und Sperren des aktuellen Sendungsdatensatzes.
4. Vergleich von `expected_version` mit `shipments.version`.
5. Prüfung des fachlich erlaubten Übergangs.
6. Anlegen des Ereignisses und Erhöhen der Version atomar.
7. Rückgabe des verbindlichen Serverstands.

Bei Versionskonflikt: HTTP 409 mit aktuellem Serverstand und Konfliktkennung. Der Client darf nicht automatisch den neueren Stand überschreiben. Ein Supervisor kann nach Prüfung eine separate Korrekturaktion auslösen.

### 4.2 Offline-Outbox

- Lokale Aktionen erhalten vor Speicherung eine UUID.
- Outbox-Einträge werden lokal verschlüsselt gespeichert.
- UI zeigt pro Vorgang „Ausstehend – noch nicht synchronisiert“.
- Wiederholungen senden dieselbe Event-ID und denselben Idempotenzschlüssel.
- Serverantworten werden lokal quittiert; bestätigte Events werden nicht erneut als neue Aktion erzeugt.
- Bei Widerruf eines Geräts werden noch nicht synchronisierte Aktionen nicht stillschweigend angenommen. Sie bleiben lokal sichtbar und werden als nicht übertragbar markiert.
- Gerätezeit ist nicht vertrauenswürdig; Serverzeit und Serversequenz bestimmen die globale Reihenfolge.
- Datenlöschung/Abmeldung darf eine nicht synchronisierte Outbox nicht unbemerkt vernichten. Ein verständlicher Export-/Supportprozess ist vor Produktivbetrieb festzulegen.

## 5. API-Vertrag – erste Version

Basis: versionierte HTTPS-API unter `/api/v1`. JSON, UTF-8, UUIDs, ISO-8601-Zeitstempel. TLS ist außerhalb einer streng isolierten lokalen Entwicklungsumgebung zwingend. Alle Endpunkte prüfen Shop-Zuordnung und Rolle serverseitig.

| Methode und Pfad | Zweck |
|---|---|
| `POST /devices/register` | Gerät nach erfolgreicher Anmeldung registrieren |
| `POST /devices/{id}/revoke` | Gerät sperren (Supervisor/Admin) |
| `GET /me` | Benutzer, Rolle und Gerätestatus |
| `GET /shipments?cursor=...` | paginierter Delta-Abgleich |
| `GET /shipments/{id}` | aktuellen Sendungsstand und erlaubte Aktionen lesen |
| `POST /shipments` | neue Sendung mit Idempotenzschlüssel erfassen |
| `POST /shipments/{id}/events` | Status-/Metadatenereignis übermitteln |
| `POST /sync/push` | begrenztes Batch von Outbox-Ereignissen senden |
| `GET /sync/pull?after=...` | bestätigte Serveränderungen seit Sequenz lesen |
| `POST /print-jobs` | Druckauftrag an zentrale Queue übergeben |
| `GET /print-jobs/{id}` | Druckstatus und Verlauf abfragen |
| `GET /printer/status` | gemeinsamen Drucker-/Queue-Status lesen |
| `POST /print-jobs/{id}/resolve-unknown` | UNKNOWN-Fall nach Prüfung durch berechtigte Person auflösen |

Jeder schreibende Endpunkt akzeptiert einen stabilen Idempotenzschlüssel. Derselbe Schlüssel mit identischem Request liefert das gespeicherte Ergebnis zurück. Derselbe Schlüssel mit abweichendem Request wird abgelehnt (HTTP 409). Batch-Größen und Payload-Größen sind begrenzt.

### Beispiel: Offline-Ereignis

```json
{
  "eventId": "UUID",
  "shipmentId": "UUID",
  "expectedVersion": 7,
  "eventType": "STATUS_TRANSITION_REQUESTED",
  "requestedTransition": "READY_FOR_PICKUP",
  "clientOccurredAt": "2026-11-26T18:30:00Z"
}
```

Die Identität des Mitarbeiters und des Geräts wird aus der gültigen Sitzung abgeleitet. Der Client darf sie nicht als vertrauenswürdige Identität vorgeben.

### Antworttypen

- `200/201`: Aktion angenommen/erstellt, mit Serverversion und Event-ID.
- `202`: Auftrag angenommen, Verarbeitung noch ausstehend.
- `401`: nicht angemeldet/Token ungültig.
- `403`: Rolle, Shop oder Gerät nicht berechtigt.
- `409`: Idempotenz- oder Versionskonflikt.
- `422`: fachlich ungültiger Statusübergang oder unzulässige Daten.
- `429`: Rate-Limit.
- `5xx`: temporärer Serverfehler; Client darf idempotent erneut versuchen.

Fehlerantworten enthalten keine Sendungsnummern, Empfängerdaten, Tokens oder Stacktraces.

## 6. Zentrale Druckarchitektur

### 6.1 Komponenten

- Eine logische Druckqueue pro Shop.
- Ein zentraler Druckdienst/Worker mit exklusivem Lease für die Queue.
- Ein physischer, vom Betreiber freigegebener Etikettendrucker.
- Carrier-/Service-spezifische Templates, die alle in dieselbe Queue münden.
- Statusabfrage und Fehleranzeige auf allen berechtigten Smartphones.

CUPS/OpenPrinting mit IPP ist ein zu prüfender Kandidat, nicht bereits als kompatibel bestätigt. Druckermodell, Verbindung (USB/Netzwerk), Treiber, Etikettenformat und offizielle Carrier-Vorgaben sind vor einer Entscheidung zu ermitteln.

### 6.2 Druckstatus

Erlaubte Statuswerte:

- `REQUESTED`: durch Mitarbeiter angefordert.
- `QUEUED`: in der zentralen Queue gespeichert.
- `CLAIMED`: Worker hat den Auftrag exklusiv übernommen.
- `SUBMITTED`: an das Drucksystem übergeben.
- `PRINTER_ACCEPTED`: Drucksystem meldet Annahme.
- `PRINTER_REPORTED_COMPLETE`: Drucksystem meldet Abschluss.
- `FAILED`: sicher festgestellter Fehler vor erfolgreicher Annahme.
- `UNKNOWN`: Ausgang unklar, z. B. Timeout nach möglicher Annahme.
- `CANCELED`: vor Übergabe an das Drucksystem sicher abgebrochen.

**Wichtig:** `SUBMITTED`, `PRINTER_ACCEPTED` und `PRINTER_REPORTED_COMPLETE` sind unterschiedliche Aussagen. Auch ein vom Spooler als abgeschlossen gemeldeter Job beweist nicht in jedem Setup, dass das Etikett physisch korrekt gedruckt und entnommen wurde. Eine echte physische Bestätigung kann eine Bedienerprüfung oder ein zusätzlicher Prozess erfordern.

### 6.3 Doppel-Druck-Schutz

- Der Server vergibt die Druckjob-ID vor der Übergabe.
- Unique Constraint auf Idempotenzschlüssel.
- Queue-Worker beansprucht einen Auftrag mit atomarem Lease.
- Ein Auftrag in `UNKNOWN` wird nicht automatisch neu gesendet.
- Automatische Wiederholung ist nur erlaubt, wenn nachweisbar ist, dass der Druckauftrag das Drucksystem noch nicht erreicht hat.
- Supervisor-Auflösung dokumentiert Grund, Person, Zeit und Entscheidung.
- Etikettendaten werden nur kurz und geschützt vorgehalten; konkrete Aufbewahrungsfrist muss der Betreiber festlegen.

## 7. Sicherheit und Datenschutz – Freigabekriterien

Vor jeder Backend-Integration müssen folgende Punkte umgesetzt und geprüft werden:

- Authentifizierung mit widerrufbaren Sitzungen und Gerätebindung.
- Serverseitige Rollenprüfung auf jedem Endpunkt.
- Shop-Isolation und Datenbank-Row-Level-Security bzw. gleichwertige Zugriffskontrolle.
- TLS, sichere Secret-Verwahrung, Schlüsselrotation und Backup-Verschlüsselung.
- Verschlüsselung sensibler Sendungs-/Empfängerdaten; HMAC-Fingerprints für Duplikatprüfung.
- Rate-Limits, Request-Größenlimits, Eingabevalidierung und Schutz gegen Replay.
- Append-only Audit-Events; normale Nutzer können Historie nicht ändern.
- Datensparsame Logs und definierte Aufbewahrungs-/Löschregeln.
- Backup-/Restore-Test einschließlich Schlüsselwiederherstellung.
- Bedrohungsmodell, Datenschutzprüfung und unabhängige Sicherheitsprüfung vor realem Betrieb.
- Kein Klartext-Export oder Debug-Logging sensibler Nutzdaten ohne ausdrücklich freigegebenen Prozess.

## 8. Backend- und Infrastrukturentscheidung

**Bevorzugte technische Richtung:** PostgreSQL als transaktionale Datenbasis plus kleine, eigenständige API und separater Druck-Worker. Damit bleiben Geschäftsregeln, Authentifizierung, Sync und Druck vom Android-Client getrennt.

Zu prüfende Implementierungsvarianten:
1. Vorhandene, bereits freigegebene Betreiber-Infrastruktur mit PostgreSQL und API – nur wenn Ressourcen, Betrieb, Netzwerk und Verantwortlichkeiten nachgewiesen sind.
2. Supabase self-hosted – nur nach Ressourcen-/Wartungsprüfung und separater Projektisolation.
3. Gehosteter Free-Tier – nur nach aktueller Quoten-, Datenschutz-, Verfügbarkeits- und Kostenprüfung sowie ausdrücklicher Betreiberfreigabe.

PocketBase wird wegen des dokumentierten Vor-1.0-Kompatibilitätsvorbehalts nicht als Produktionsbasis empfohlen. Keine bestehende KREISLAUF-Datenbank für TRAKUNA mitverwenden. Kein Anbieter, Hosting, DNS, Domain oder kostenpflichtiger Dienst wird durch diese ADR gebucht oder eingerichtet.

## 9. Migrations- und Implementierungsphasen

### Phase A – Spezifikation (diese ADR)
- Zielbild, Datenobjekte, API, Sync- und Druckzustände definiert.
- Noch keine Code-/Datenbankänderung.

### Phase B – Backend-Prototyp ohne echte Daten
- isolierter Entwicklungsbranch und lokale Testdaten
- Schema-/Migrationsentwurf
- Auth-/Rollen-Prototyp
- Idempotenz und parallele Statusänderungen
- automatisierte Tests mit zwei oder mehr simulierten Geräten

### Phase C – Drucksimulation
- eine logische Queue
- Fake-/Testdruckeradapter
- parallele Druckanforderungen
- Timeout nach möglicher Annahme, UNKNOWN und manuelle Klärung
- keine echten Etiketten oder Carrier-Zugänge

### Phase D – Android-Integration im Shadow/Testmodus
- Netzwerkberechtigung und Security-CI nur nach genehmigtem Architektur- und Threat-Model-Review anpassen
- lokale verschlüsselte Outbox
- Sync-Status und Konfliktanzeige
- mehrere Testgeräte
- bestehende lokale Datenmigration und Backup-Kompatibilität schützen

### Phase E – Betreiberabnahme
- Hardware- und Netzwerkprüfung
- echte Testgeräte und Testetiketten
- Drucker-/Carrier-Berechtigungen
- Datenschutz-/Sicherheitsabnahme
- erst danach gesonderte Freigabe für Live-Funktionen

## 10. Abnahmetests (Mindestumfang)

### Synchronisierung und Parallelität
1. Zwei Geräte legen gleichzeitig unterschiedliche Sendungen an – beide erscheinen auf beiden Geräten nach Sync.
2. Zwei Geräte legen dieselbe aktive Sendungsnummer an – nur ein Vorgang wird angenommen.
3. Zwei Geräte ändern dieselbe Sendung mit derselben erwarteten Version – genau eine konkurrierende Änderung wird angenommen; die andere erhält 409.
4. Wiederholtes Senden derselben Event-ID erzeugt nur ein Ereignis.
5. Netzwerkabbruch nach Server-Commit, aber vor Client-Antwort: Wiederholung liefert dasselbe Ergebnis ohne Duplikat.
6. Offline erfasste Aktionen bleiben nach App-Neustart erhalten und sichtbar ausstehend.
7. Gerätewiderruf verhindert weitere Serveränderungen; bereits lokal ausstehende Daten werden nicht stillschweigend gelöscht.
8. Falscher Shop-/Geräte-/Rollen-Kontext wird serverseitig abgewiesen.
9. Uhrzeitabweichung eines Smartphones verändert nicht die globale Ereignisreihenfolge.
10. Backup und Wiederherstellung erhalten Outbox-/Quittierungszustand konsistent oder melden klar, was nicht wiederhergestellt werden kann.

### Druck
1. Alle Mitarbeitergeräte stellen Aufträge an dieselbe Queue.
2. Gleichzeitige Aufträge werden serialisiert.
3. Derselbe Idempotenzschlüssel erzeugt keinen zweiten Druckjob.
4. Sicherer Fehler vor Druckerannahme kann kontrolliert wiederholt werden.
5. Timeout nach möglicher Annahme erzeugt `UNKNOWN`, keinen automatischen Zweitdruck.
6. Drucker offline/kein Papier/Fehlerzustand ist für alle Geräte sichtbar.
7. Carrier-Templates werden auf demselben Drucker korrekt ausgewählt.
8. Spooler-Abschluss wird nicht als physische Entnahme/korrekte Etikettenqualität ausgegeben.
9. Supervisor-Auflösung eines UNKNOWN-Falls ist nachvollziehbar protokolliert.

### Sicherheit
1. Nicht angemeldete, widerrufene oder nicht berechtigte Geräte erhalten keinen Zugriff.
2. Mitarbeiter eines Shops können keine Daten eines anderen Shops lesen.
3. Sensible Daten erscheinen nicht in Logs, Crashreports oder Fehlermeldungen.
4. Idempotenz-/Replay- und Eingabevalidierungstests bestehen.
5. Backup-Restore mit korrekten und falschen Schlüsseln ist geprüft.

## 11. Offene Betreiberinformationen – vor Hardware-/Produktionsentscheidung

Folgende Fakten dürfen nicht geraten werden:
- Anzahl der Mitarbeiter und gleichzeitig aktiven Smartphones.
- Android-Versionen und verwaltete/private Geräteeigentümerschaft.
- Verfügbare lokale Server-/PC-Hardware, Betriebssystem, RAM, Speicher und Netzwerk.
- Druckermodell, Anschlussart, Treiber, Etikettenformat und Herstellerstatusabfragen.
- Paketshop-Software und offizielle Schnittstellen-/Nutzungsfreigaben.
- Verantwortliche Person für Benutzerverwaltung, Backups, Schlüssel und Datenschutz.
- Aufbewahrungsfristen und zulässige Empfänger-/Sendungsdaten.

Diese Angaben sind erst vor der jeweiligen Umsetzungsphase nötig; bis dahin wird kein Betreiber nach Zugangsdaten oder Geheimnissen gefragt.

## 12. Aktueller Umsetzungsstatus

| Teil | Status |
|---|---|
| Mehrgeräte-Zielbild | spezifiziert |
| Datenmodell | konzeptionell spezifiziert |
| API-Vertrag | konzeptionell spezifiziert |
| Idempotenz/Konfliktregeln | spezifiziert |
| Offline-Outbox | spezifiziert |
| Zentrale Druckqueue | spezifiziert |
| Backend/DB eingerichtet | nein |
| Android-Netzwerkzugriff | weiterhin gesperrt |
| Drucker angebunden | nein |
| Implementiert/getestet | nein |

Diese ADR ist ein Architekturvorschlag und kein Implementierungs- oder Testnachweis. Änderungen am bestehenden Pilotcode, Shadow Mode, Manifest, Datenbestand, Carrier-Portalen oder Drucker erfolgen erst in den vorgesehenen getrennten Phasen und mit den erforderlichen Freigaben.
