# Sicherheits- und Datenschutzstatus

## Bereits im Projekt umgesetzt

- Die Migration verschlüsselter Paketdatensätze entfernt einen alten Klartextbestand erst nach erfolgreicher Entschlüsselung und vollständiger Validierung der Datensatzstruktur.
- Der Partner-Übergabe-Cache validiert seine entschlüsselten Felder und die Sendungsnummer vor Verwendung und Migration.

- Vor dem CSV-Export weist die App ausdrücklich darauf hin, dass die Datei personenbezogene Sendungsdaten und interne Notizen unverschlüsselt enthält.

- Lokale Paketdatensätze werden mit AES-256-GCM verschlüsselt; der AES-Schlüssel wird im Android Keystore gespeichert.
- Eine Einmalmigration übernimmt den bisherigen Klartextbestand. Der alte Eintrag wird erst nach erfolgreichem Commit des verschlüsselten Ersatzes entfernt.
- Fehler bei Migration, Entschlüsselung oder Schlüsselzugriff führen zu einem sichtbaren Speicherfehler; die App darf solche Daten nicht als leeren Bestand behandeln.
- Verschlüsselte Speicherwerte und Klartext vor der Verschlüsselung sind auf maximal 2 MiB Nutzdaten begrenzt; Base64-Eingaben werden vor dem Dekodieren zusätzlich begrenzt.
- Der zuletzt an eine Partner-App übergebene Sendungsnummern-Cache wird separat mit AES-GCM und einem eigenen Android-Keystore-Schlüssel verschlüsselt. Vorhandene Klartext-Cachewerte werden nach erfolgreichem verschlüsseltem Commit entfernt.
- Fehler beim Lesen/Entschlüsseln des Übergabe-Caches werden nicht als leerer Cache ausgegeben; der Lesezugriff meldet einen Fehler.

- Zebra-DataWedge-Broadcasts werden nur verarbeitet, solange die TRAKUNA-Aktivität im Vordergrund (`RESUMED`) ist; im Hintergrund eingehende Broadcasts werden ignoriert.
- Fehlende oder falsch typisierte DataWedge-Extras werden defensiv ignoriert; fehlerhafte externe Broadcast-Nutzdaten sollen den Empfänger nicht zum Absturz bringen.
- Android-Backup ist deaktiviert (`allowBackup=false`).
- Das aktuelle Manifest fordert keine `INTERNET`-Berechtigung an; die App baut daher derzeit keine Netzwerkverbindung auf. WLAN/4G aktiviert keine Cloud-Synchronisierung. Es gibt keine automatische Übertragung an einen Server.
- Unverschlüsselter Netzwerkverkehr ist standardmäßig deaktiviert.
- Lokale Datensätze werden vor Anzeige und Wiederherstellung auf Struktur, Status, IDs und Duplikate geprüft.
- Backup-Import ist auf 2 MiB begrenzt und erfordert eine Bestätigung, da der vorhandene lokale Bestand ersetzt wird.
- Eine Wiederherstellung aus einer gültigen Sicherung ist auch bei beschädigtem lokalem verschlüsseltem Bestand möglich; der Erfolg wird erst nach bestätigtem verschlüsseltem Commit gemeldet.
- JSON-Backups enthalten Sendungsnummern, Empfänger und Notizen und sind derzeit nicht verschlüsselt; die App weist vor dem Export darauf hin.
- CSV-Export entschärft Tabellenkalkulations-Formelanfänge auch dann, wenn unsichtbare Unicode-Leerzeichen, Zero-Width-Zeichen oder eine BOM vorangestellt sind.
- Die Partnerübergabe verwendet dieselbe Sendungsnummern-Normalisierung wie das Speichern. Steuerzeichen werden nicht stillschweigend entfernt, sondern durch die Validierung abgelehnt.
- Partnerübergabe speichert die Sendungsnummer synchron im lokalen Cache, bevor TRAKUNA die andere App startet.
- Clipboard-Fehler blockieren den lokalen Ablauf nicht; auf Android 13+ wird der Clip als sensibel markiert.

## Release-Signierung vorbereitet

Die Gradle-Konfiguration unterstützt einen signierten Release-Build, ohne einen privaten Schlüssel im Repository abzulegen. Der CI-Workflow baut ein Release nur, wenn alle vier GitHub-Actions-Secrets gesetzt sind:

- `ANDROID_KEYSTORE_BASE64` – Base64-kodierte JKS/PKCS12-Keystore-Datei
- `ANDROID_KEYSTORE_PASSWORD` – Keystore-Passwort
- `ANDROID_KEY_ALIAS` – Alias des App-Signaturschlüssels
- `ANDROID_KEY_PASSWORD` – Passwort des privaten Schlüssels

Der Keystore muss vom Betreiber erzeugt und sicher verwahrt werden. Er darf niemals in Git eingecheckt, in Chat-Nachrichten geteilt oder zusammen mit der APK veröffentlicht werden. Ohne diese Secrets wird ausschließlich das Debug-Testpaket gebaut; es entsteht kein signiertes Produktionsrelease. Vor dem Einrichten muss der Betreiber festlegen, wer Schlüsselinhaber und Wiederherstellungsverantwortlicher ist.

## Bekannte Restrisiken / nicht abgeschlossene Arbeiten

- Exportierte JSON- und CSV-Dateien sind weiterhin unverschlüsselt und müssen wie vertrauliche Unterlagen geschützt werden.
- Es gibt keine zentrale Anmeldung, Rollen-/Rechteverwaltung oder Gerätesynchronisierung.
- Es gibt keine verifizierte Carrier-API-Anbindung.
- Die Partner-App-Übergabe bestätigt nur das Öffnen der Ziel-App, nicht die Übernahme oder Buchung.
- Es liegt kein unabhängiger Penetrationstest und keine formale Datenschutzprüfung vor.
- Ein signiertes Produktionsrelease, Schlüsselverwaltung und Play-Store-Veröffentlichung sind nicht eingerichtet.

## Release-Sperren

TRAKUNA darf erst als produktionsbereit für einen realen Paketshop bezeichnet werden, wenn mindestens folgende Punkte abgeschlossen und nachgewiesen sind:

1. Verschlüsselung sensibler lokaler Daten einschließlich Migration und Wiederherstellungstest.
2. Datenschutz- und Aufbewahrungskonzept für Sendungsnummern, Empfängerdaten, Backups und Logs.
3. Rollen-/Benutzerkonzept und sichere Synchronisierung, falls mehrere Geräte oder Mitarbeiter eingesetzt werden.
4. Partner-/Carrier-Integration mit offizieller Schnittstelle und erfolgreicher Abnahme.
5. Tests auf realen Android-Geräten mit echten Testetiketten und allen vier Paketshop-Prozessen.
6. Unabhängige Sicherheitsprüfung und Behebung aller kritischen/hohen Befunde.
7. Signiertes Release mit sicher verwahrtem Schlüssel und nachvollziehbarem Release-Prozess.

## Sicherheitsmeldungen

Bitte keine realen Sendungsnummern, Empfängernamen, Passwörter oder Schlüssel in öffentlichen Issues posten. Sicherheitsprobleme sollten dem Projektverantwortlichen privat und mit möglichst wenigen personenbezogenen Beispieldaten gemeldet werden.

## Zebra DataWedge Broadcast

TRAKUNA registriert den DataWedge-Empfänger dynamisch, damit DataWedge Broadcast-Scans zustellen kann. Der Empfänger verarbeitet Scans nur, solange die App im Vordergrund aktiv ist. Das reduziert unbeabsichtigte Hintergrundverarbeitung, authentifiziert aber nicht den Absender: Ohne geräteseitige Einschränkung kann ein anderer Prozess auf demselben Gerät einen gleichartigen Broadcast nachahmen.

**Empfohlene Gerätekonfiguration:** Im DataWedge-Profil unter Intent Output → Component Information ausschließlich das installierte TRAKUNA-Paket `de.trakuna.app` auswählen und die angebotene App-Signaturprüfung aktivieren. Zebra dokumentiert, dass DataWedge dann die Paket-ID und zusätzlich die App-Signatur vor der Zustellung prüft. Die Einstellung muss auf jedem eingesetzten Gerät bzw. verwalteten Profil kontrolliert werden; TRAKUNA kann ihren Aktivierungsstatus nicht auslesen oder garantieren.

Die Allowlist schützt die Zustellung **von DataWedge an TRAKUNA** (Paketname und optional App-Signatur) und verhindert insbesondere, dass DataWedge Scan-Daten an eine fremde App mit nur nachgeahmtem Paketnamen sendet. Sie authentifiziert nicht den Absender eingehender Broadcasts an TRAKUNA; ein anderer lokaler Prozess könnte einen gleichartigen Broadcast nachahmen. Die Allowlist schützt die Zustellung über korrekt konfiguriertes DataWedge. Sie ist kein Ersatz für Eingabevalidierung oder die Prüfung durch den Mitarbeiter. Eingehende Scanwerte bleiben nicht vertrauenswürdige Eingaben, werden validiert und lösen niemals automatisch eine Buchung aus. Für Hochsicherheitsbetrieb sind zusätzlich zentral verwaltete Geräte, kontrollierte App-Installationen und eine vertrauenswürdige Integrationsschicht erforderlich.
