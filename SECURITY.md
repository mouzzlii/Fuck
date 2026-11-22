# Sicherheits- und Datenschutzstatus

## Bereits im Projekt umgesetzt

- Android-Backup ist deaktiviert (`allowBackup=false`).
- Unverschlüsselter Netzwerkverkehr ist standardmäßig deaktiviert.
- Lokale Datensätze werden vor Anzeige und Wiederherstellung auf Struktur, Status, IDs und Duplikate geprüft.
- Backup-Import ist auf 2 MiB begrenzt und erfordert eine Bestätigung, da der vorhandene lokale Bestand ersetzt wird.
- JSON-Backups enthalten Sendungsnummern, Empfänger und Notizen und sind derzeit nicht verschlüsselt; die App weist vor dem Export darauf hin.
- CSV-Export entschärft Tabellenkalkulations-Formelanfänge.
- Partnerübergabe speichert die Sendungsnummer synchron im lokalen Cache, bevor TRAKUNA die andere App startet.
- Clipboard-Fehler blockieren den lokalen Ablauf nicht; auf Android 13+ wird der Clip als sensibel markiert.

## Bekannte Restrisiken / nicht abgeschlossene Arbeiten

- Lokale Paketdaten und der Übergabe-Cache liegen derzeit in Android SharedPreferences und sind nicht anwendungsseitig verschlüsselt.
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

TRAKUNA registriert den DataWedge-Empfänger dynamisch, damit DataWedge Broadcast-Scans zustellen kann. Ohne geräteseitige Einschränkung kann ein anderer Prozess auf demselben Gerät einen gleichartigen Broadcast nachahmen.

**Empfohlene Gerätekonfiguration:** Im DataWedge-Profil unter Intent Output → Component Information ausschließlich das installierte TRAKUNA-Paket `de.trakuna.app` auswählen und die angebotene App-Signaturprüfung aktivieren. Zebra dokumentiert, dass DataWedge dann die Paket-ID und zusätzlich die App-Signatur vor der Zustellung prüft. Die Einstellung muss auf jedem eingesetzten Gerät bzw. verwalteten Profil kontrolliert werden; TRAKUNA kann ihren Aktivierungsstatus nicht auslesen oder garantieren.

Die Allowlist schützt die Zustellung über korrekt konfiguriertes DataWedge. Sie ist kein Ersatz für Eingabevalidierung oder die Prüfung durch den Mitarbeiter. Eingehende Scanwerte bleiben nicht vertrauenswürdige Eingaben, werden validiert und lösen niemals automatisch eine Buchung aus. Für Hochsicherheitsbetrieb sind zusätzlich zentral verwaltete Geräte, kontrollierte App-Installationen und eine vertrauenswürdige Integrationsschicht erforderlich.
