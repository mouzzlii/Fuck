# Sicherheits- und Datenschutzstatus

## Bereits im Projekt umgesetzt

- Die Migration verschlüsselter Paketdatensätze entfernt einen alten Klartextbestand erst nach erfolgreicher Entschlüsselung und vollständiger Validierung der Datensatzstruktur.
- Der Partner-Übergabe-Cache validiert seine entschlüsselten Felder und die Sendungsnummer vor Verwendung und Migration.

- Vor dem CSV-Export weist die App ausdrücklich darauf hin, dass die Datei personenbezogene Sendungsdaten und interne Notizen unverschlüsselt enthält.

- Lokale Paketdatensätze werden mit AES-256-GCM verschlüsselt; der AES-Schlüssel wird im Android Keystore gespeichert.
- Entschlüsselte Bytes sowohl des lokalen Sendungsbestands als auch des separaten Partner-Übergabe-Caches werden strikt als UTF-8 dekodiert. Fehlerhafte oder nicht abbildbare Bytefolgen werden als Speicherfehler behandelt und nicht stillschweigend durch Ersatzzeichen verändert.
- Eine Einmalmigration übernimmt den bisherigen Klartextbestand. Der alte Eintrag wird erst nach erfolgreichem Commit des verschlüsselten Ersatzes entfernt.
- Fehler bei Migration, Entschlüsselung oder Schlüsselzugriff führen zu einem sichtbaren Speicherfehler; die App darf solche Daten nicht als leeren Bestand behandeln.
- Der verschlüsselte lokale Sendungsbestand ist auf maximal 2 MiB Nutzdaten begrenzt; Base64-Eingaben werden vor dem Dekodieren zusätzlich begrenzt. JSON-Sicherungen werden kompakt exportiert und dürfen beim Import bis zu 4 MiB groß sein, damit der Sicherungskopf und die Exportstruktur nicht dazu führen, dass ein gültiger lokaler Bestand seine eigene Sicherung nicht wiederherstellen kann.
- Der zuletzt an eine Partner-App übergebene Sendungsnummern-Cache wird separat mit AES-GCM und einem eigenen Android-Keystore-Schlüssel verschlüsselt. Vorhandene Klartext-Cachewerte werden nach erfolgreichem verschlüsseltem Commit entfernt.
- Fehler beim Lesen/Entschlüsseln des Übergabe-Caches werden nicht als leerer Cache ausgegeben; der Lesezugriff meldet einen Fehler.

- Zebra-DataWedge-Broadcasts werden nur verarbeitet, solange die TRAKUNA-Aktivität im Vordergrund (`RESUMED`) ist; im Hintergrund eingehende Broadcasts werden ignoriert.
- Fehlende oder falsch typisierte DataWedge-Extras werden defensiv ignoriert; fehlerhafte externe Broadcast-Nutzdaten sollen den Empfänger nicht zum Absturz bringen.
- Android-Backup ist deaktiviert (`allowBackup=false`).
- TRAKUNA fordert im final zusammengeführten App-Manifest keine `INTERNET`-Berechtigung an. Die Barcode-Abhängigkeiten bringen eine Google-Transport-Komponente mit, deren Manifest diese Berechtigung anfordert; TRAKUNA entfernt sie ausdrücklich per Manifest-Merger-Regel (`tools:node="remove"`). Die CI kontrolliert sowohl diese Quellregel als auch das tatsächlich paketierte Manifest. WLAN/4G aktiviert keine Cloud-Synchronisierung. Es gibt keine automatische Übertragung an einen Server.
- Unverschlüsselter Netzwerkverkehr ist standardmäßig deaktiviert.
- Lokale Datensätze werden vor Anzeige und Wiederherstellung auf Struktur, Status, IDs und Duplikate geprüft.
- Abgeschlossene Sendungen können lokal archiviert und wiederhergestellt werden; offene Sendungen werden durch die Geschäftsregel nicht archiviert. Archivstatus und Archivereignisse werden mit dem Datensatz verschlüsselt gespeichert.
- Archivierte Datensätze bleiben im 500-Einträge-Limit. Endgültiges Löschen ist in der Oberfläche nur aus dem Archiv möglich; zusätzlich verweigert auch die Speicherschicht das Löschen nicht archivierter Vorgänge. Das endgültige Löschen eines archivierten Datensatzes entfernt Datensatz und lokale Historie dauerhaft.
- JSON-Backup-Schema 2 speichert den Archivzeitpunkt; Schema-1-Backups ohne Archivfeld bleiben für die Wiederherstellung lesbar. CSV enthält den Archivzeitpunkt zusätzlich zur lokalen Historie.
- Archivierung und lokale Historie sind keine manipulationssicheren oder zentralen Audit-Nachweise.
- Die Klartext-Nutzdaten einer Sicherung sind auf 4 MiB begrenzt; die passwortgeschützte `.trakuna`-Datei auf 6 MiB. Der Import erfordert eine Bestätigung, da der vorhandene lokale Bestand ersetzt wird.
- Eine Wiederherstellung aus einer gültigen Sicherung ist auch bei beschädigtem lokalem verschlüsseltem Bestand möglich; der Erfolg wird erst nach bestätigtem verschlüsseltem Commit gemeldet.
- Neue `.trakuna`-Sicherungen enthalten Sendungsnummern, Empfänger und Notizen ausschließlich AES-256-GCM-verschlüsselt; der Schlüssel wird aus einem 12–128 Zeichen langen Passwort mit PBKDF2-HMAC-SHA256 (600.000 Iterationen) abgeleitet. Salt, Nonce und Parameter werden authentifiziert. Ältere unverschlüsselte JSON-Sicherungen bleiben nur für den Import verfügbar und werden vor dem Ersetzen ausdrücklich als Klartext gekennzeichnet. CSV-Exporte bleiben unverschlüsselt.
- CSV-Export entschärft Tabellenkalkulations-Formelanfänge auch dann, wenn unsichtbare Unicode-Leerzeichen, Zero-Width-Zeichen oder eine BOM vorangestellt sind.
- Die Partnerübergabe verwendet dieselbe Sendungsnummern-Normalisierung wie das Speichern. Steuerzeichen werden nicht stillschweigend entfernt, sondern durch die Validierung abgelehnt.
- Partnerübergabe speichert die Sendungsnummer synchron im lokalen Cache, bevor TRAKUNA die andere App startet.
- Clipboard-Fehler blockieren den lokalen Ablauf nicht; auf Android 13+ wird der Clip als sensibel markiert.

## Automatische CI-Sicherheitsprüfungen

Der Android-CI-Workflow führt `scripts/check-android-security-boundaries.sh` vor den Unit-Tests und nach dem APK-Bau aus. Die Quellprüfung erlaubt ausschließlich CAMERA und ACCESS_NETWORK_STATE; INTERNET darf nur als explizite Manifest-Entfernung für die ML-Kit-Transport-Abhängigkeit vorkommen. Zusätzlich werden optionale Kamera-Hardware, `allowBackup=false`, `usesCleartextTraffic=false`, direkte Netzwerk-Client-Abhängigkeiten und bekannte HTTP-/Socket-APIs im App-Quellcode geprüft. Nach dem Build wird das tatsächlich paketierte Manifest kontrolliert.

- Keine Android-`INTERNET`-Berechtigung, solange keine freigegebene Backend-Architektur umgesetzt ist.
- `ACCESS_NETWORK_STATE` dient ausschließlich der lokalen WLAN-Statusanzeige und gewährt keinen Netzwerkzugriff.
- `android:allowBackup="false"` und `android:usesCleartextTraffic="false"` müssen im Quell- und Paketmanifest gesetzt bleiben.
- Direkte HTTP-/Socket-Client-Abhängigkeiten und bekannte Netzwerk-APIs sind nicht freigegeben.

Ein Verstoß lässt den Build fehlschlagen. Diese Schutzschranke prüft Quellmanifest, deklarierte direkte Abhängigkeiten, bekannte Netzwerk-API-Aufrufe und das finale Paketmanifest. Sie ist kein vollständiger Audit aller transitiven Bibliotheksimplementierungen und ersetzt weder Codeanalyse noch Penetrationstest.

Für Version 1.54 / versionCode 55 waren der [Android-Build 45691683747](https://github.com/mouzzlii/Fuck/actions/runs/45691683747) und der separate [Emulator-Instrumentierungslauf 45691683698](https://github.com/mouzzlii/Fuck/actions/runs/45691683698) auf Code-Commit `bb29cdab5d8e4ac25a382641d9d4ca418c4342eb` erfolgreich. Der Build führte JVM-Unit-Tests, Android-Lint, Quell- und Paketmanifest-/Offline-Sicherheitsprüfungen sowie den APK-Build aus. Der Emulator-Workflow führte die Android-Keystore- und Speicher-Instrumentierungstests aus. Das belegt automatisierte Tests in CI/Emulator, aber keine reale Geräte-, Zebra- oder Carrier-Abnahme. Die nachfolgenden Änderungen auf PR #18 betreffen Dokumentation und besitzen eigene CI-Läufe. Wenn eine echte Synchronisierung später beschlossen wird, muss diese Schutzschranke gemeinsam mit TLS, Authentifizierung, Berechtigungen, Datenschutzkonzept und Backend-Abnahme bewusst angepasst werden.

## Release-Signierung vorbereitet

Die Gradle-Konfiguration unterstützt einen signierten Release-Build, ohne einen privaten Schlüssel im Repository abzulegen. Der CI-Workflow baut ein Release nur, wenn alle vier GitHub-Actions-Secrets gesetzt sind:

- `ANDROID_KEYSTORE_BASE64` – Base64-kodierte JKS/PKCS12-Keystore-Datei
- `ANDROID_KEYSTORE_PASSWORD` – Keystore-Passwort
- `ANDROID_KEY_ALIAS` – Alias des App-Signaturschlüssels
- `ANDROID_KEY_PASSWORD` – Passwort des privaten Schlüssels

Das CI stellt diese vier Secrets nicht global für den gesamten Build-Job bereit: Sie werden nur im separaten Konfigurationscheck und – falls alle vorhanden sind – im Release-Signierschritt eingeblendet. Unit-Tests, Lint, Manifestprüfung und Debug-Build erhalten diese Betreiber-Secrets nicht.

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

TRAKUNA registriert den DataWedge-Empfänger dynamisch. Scans werden nur verarbeitet, solange die TRAKUNA-Aktivität im Vordergrund (`RESUMED`) ist. Zusätzlich prüft TRAKUNA den ursprünglichen Absender des eingehenden Broadcasts:

- **Android 14 (API 34) und neuer:** Der Empfänger akzeptiert den Broadcast nur, wenn Android über `getSentFromPackage()` exakt `com.symbol.datawedge` als ursprüngliches Absenderpaket meldet. Kann der Absender nicht verlässlich ermittelt werden, wird der Broadcast abgelehnt.
- **Android-Versionen unter API 34:** Android stellt dem Empfänger keine entsprechende verifizierbare Absenderidentität bereit. Deshalb lehnt TRAKUNA DataWedge-Broadcast-Scans auf diesen Versionen vollständig ab. Kamera- und manuelle Eingabe bleiben davon unberührt.

**Empfohlene Gerätekonfiguration:** Im DataWedge-Profil unter Intent Output → Component Information ausschließlich das installierte TRAKUNA-Paket `de.trakuna.app` auswählen und die angebotene App-Signaturprüfung aktivieren. Zebra dokumentiert diese Prüfung als zusätzliche Kontrolle der Zustellung **von DataWedge an TRAKUNA**. Sie ersetzt nicht die Prüfung des ursprünglichen Absenders eingehender Broadcasts in TRAKUNA. Die Einstellung muss auf jedem eingesetzten Gerät bzw. verwalteten Profil kontrolliert werden; TRAKUNA kann ihren Aktivierungsstatus nicht auslesen oder garantieren.

Eingehende Scanwerte bleiben nicht vertrauenswürdige Eingaben. TRAKUNA akzeptiert nur unterstützte DataWedge-Labeltypen und validiert die Scan-Nutzdaten; ein Scan löst niemals automatisch eine Buchung aus. Die Sicherheitsprüfung des Absenders und die Labeltype-Allowlist sind zusätzliche Schutzmaßnahmen, ersetzen aber weder verwaltete Geräte noch kontrollierte App-Installationen und die Prüfung durch den Mitarbeiter. Die DataWedge-Konfiguration und die Scannerfunktion müssen vor einem produktiven Einsatz auf den tatsächlich verwendeten Zebra-Geräten getestet werden.
