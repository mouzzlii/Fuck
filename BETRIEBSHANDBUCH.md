# TRAKUNA – Betriebshandbuch

Stand: App-Version 1.53 (versionCode 54) · CI erfolgreich: Lauf 45293985061 (Debug-Testpaket)

Dieses Handbuch erklärt die lokale Testversion Schritt für Schritt. TRAKUNA ist noch keine freigegebene Produktivsoftware. Ausschließlich mit ausdrücklich freigegebenen Testsendungen bzw. erfundenen Testnummern arbeiten.

## 1. Aktuelles Testpaket installieren

1. Den [erfolgreichen Android-Build 45293985061 öffnen](https://github.com/mouzzlii/Fuck/actions/runs/45293985061).
2. Im Abschnitt **Artifacts** das Paket **TRAKUNA-Android** herunterladen.
3. Das ZIP entpacken und die enthaltene Datei `app-debug.apk` auf das Android-Gerät übertragen.
4. Die Prüfsumme im entpackten Ordner mit `sha256sum -c SHA256SUMS.txt` prüfen; erwartet wird `app-debug.apk: OK`. Unter Windows kann alternativ `Get-FileHash .\app-debug.apk -Algorithm SHA256` mit dem oben genannten APK-Hash verglichen werden.
5. Die APK öffnen und installieren. Android kann eine einmalige Erlaubnis für die Installation aus dieser Datei-App verlangen. Nur der vertrauenswürdigen Datei-App diese Erlaubnis geben und danach wieder deaktivieren.
6. TRAKUNA starten und zuerst mit erfundenen Testdaten prüfen.

**Prüfsummen des bereitgestellten Testpakets**

- APK-SHA-256: `ce7ed6093e213af7167e5bb2ebfc9c97fd89123fc4231f82fb94b8a4e8143f2b`
- ZIP-SHA-256: `b2857b0eeb9c452f929ecc9fb2f1258de768897e7f9e26c0eb7912c8c45b6b59`
- Erstellt: 24.11.2026, 00:05 UTC
- Artefakt verfügbar bis: 08.12.2026, 00:05 UTC

Es ist ein Debug-Testpaket, kein signiertes Produktionsrelease. Version 1.53 enthält die Archivierungs- und Löschschutzfunktionen sowie neue Android-Instrumentierungstestfälle. Diese Instrumentierungstestfälle wurden im Haupt-Build kompiliert. Der separate Android-15-Emulatorlauf 45308245723 auf Main-Commit `fe3314e4f5b94fdc8c89a51d9f8588bcb4d3d318` ist erfolgreich abgeschlossen; die Instrumentierung meldet 5/5 Tests bestanden. CI bestätigte Unit-Tests, Kompilierung der Android-Instrumentierungstestfälle, Android-Lint, Manifestprüfungen, APK-Build und Artefakt-Upload. Die Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen.

### Entwicklungsversion 1.54 – noch nicht als APK verfügbar

Im Arbeitszweig `fix/backup-crypto-background-2026-11-24` wird eine passwortgeschützte Sicherung mit der Dateiendung `.trakuna` vorbereitet. Das Passwort muss 12 bis 128 Zeichen lang sein und getrennt von der Datei aufbewahrt werden. Die Sicherung verwendet AES-256-GCM und PBKDF2-HMAC-SHA256 mit 600.000 Iterationen. Alte Klartext-JSON-Sicherungen sollen weiterhin importiert werden; beim Import muss eine deutliche Warnung erscheinen. CSV-Exporte bleiben weiterhin Klartext.

Diese Version ist noch nicht in `main` übernommen und es gibt noch kein geprüftes 1.54-Installationspaket. Bis dahin gelten die oben beschriebenen Schritte für Version 1.53: JSON-Sicherungen enthalten personenbezogene Daten im Klartext.

## 2. Vorhandene Daten bei Updates schützen

**TRAKUNA vor einem Update nicht deinstallieren.** Lokale Sendungsdaten und der zugehörige Android-Keystore-Schlüssel können bei einer Deinstallation verloren gehen.

1. In TRAKUNA eine vollständige JSON-Datensicherung erstellen.
2. Die Datei an einem geschützten Ort ablegen. Sie enthält Sendungsnummern, Empfängernamen und Notizen im Klartext.
3. Prüfen, ob die Sicherungsdatei tatsächlich vorhanden ist.
4. Erst dann das neue APK installieren und – wenn Android es anbietet – die bestehende App aktualisieren, nicht deinstallieren.
5. Nach dem Update einige vorhandene Testvorgänge kontrollieren.

Wenn Android eine abweichende Signatur meldet, Installation abbrechen. Die bestehende App nicht löschen und keine App-Daten entfernen. Debug-Builds können unterschiedliche Signaturen besitzen.

## 3. DPD, GLS, Hermes und UPS getrennt zuordnen

In Version 1.53 kann jeder der vier Start-Paketdienste einer eigenen installierten Betriebssoftware zugeordnet werden. Die Zuordnung wird lokal auf diesem Android-Gerät gespeichert.

1. TRAKUNA öffnen.
2. **Betriebssoftware pro Paketdienst zuordnen** antippen.
3. Im Auswahlfeld den Paketdienst wählen: **DPD**, **GLS**, **Hermes** oder **UPS**.
4. Die tatsächlich zu diesem Paketdienst gehörende, installierte App auswählen.
5. Falls sie nicht in der ersten Liste erscheint, **Alle installierten Apps anzeigen** antippen.
6. Die Anzeige unter der Hauptschaltfläche kontrollieren.
7. Für die anderen drei Paketdienste wiederholen.

Wichtig:

- Nur Apps auswählen, die der Betreiber bzw. Paketdienst für diesen Standort freigegeben hat.
- Keine App nur anhand eines ähnlich klingenden Namens zuordnen.
- Sobald mindestens eine Einzelzuordnung eingerichtet ist, wird für einen anderen Paketdienst ohne eigene Zuordnung keine fremde Carrier-App als Ersatz geöffnet.
- Eine Zuordnung öffnet später nur die ausgewählte App. Sie beweist nicht, dass diese App die Sendungsnummer übernommen oder eine Buchung ausgeführt hat.
- Die Zuordnung ist gerätebezogen. Sie wird nicht mit anderen Handgeräten synchronisiert.
- Eine Zuordnung kann über die Schaltfläche **Zuordnung für [Paketdienst] entfernen** wieder gelöscht werden.

## 4. Sendungsnummer erfassen und übergeben

1. Den passenden Paketdienst in der Hauptmaske auswählen.
2. Die Sendungsnummer entweder manuell eingeben oder – bei eingerichteter Kamera – scannen.
3. Die angezeigte Nummer und den Paketdienst vor jeder weiteren Aktion kontrollieren.
4. **An Partner-App übergeben** antippen.
5. TRAKUNA speichert die Übergabe lokal verschlüsselt und versucht, die für diesen Paketdienst zugeordnete App zu öffnen.
6. Falls keine App zugeordnet oder installiert ist, bleibt die Nummer im TRAKUNA-Cache; gegebenenfalls wird sie zusätzlich in die Zwischenablage kopiert.

**Nicht als Buchungserfolg werten:** Das Öffnen einer Partner-App ist keine Bestätigung, dass diese die Nummer empfangen, validiert oder einen Vorgang angelegt hat. TRAKUNA besitzt derzeit keine bestätigte DPD-, GLS-, Hermes- oder UPS-Buchungsschnittstelle.

## 5. Lokale Paketshop-Vorgänge

Über **Paketshop-Sendungen verwalten** können Testvorgänge lokal erfasst und ihre Status fortgeschrieben werden:

- Sendung für einen Kunden annehmen und als im Shop eingetroffen führen.
- Eine Sendung als abholbereit markieren und lokal an den Kunden übergeben.
- Eine Kundenabgabe bzw. Retoure erfassen und den lokalen Ablauf bis zur Übergabe an den Paketdienst fortschreiben.

Die lokalen Status ersetzen keine Buchung oder Bestätigung im Carrier-System. Eine Übergabe an DPD, GLS, Hermes oder UPS wird in Version 1.53 ausdrücklich als **lokale Dokumentation** angezeigt. TRAKUNA erhält keine Carrier-Bestätigung; erst das offizielle Carrier-System und dessen Quittung sind maßgeblich. Vorhandene Carrier-Programme bleiben führend.

## 6. Zebra TC26 und DataWedge (DPD/GLS)

Die DPD- und GLS-Handgeräte wurden anhand der bisherigen Angaben als Zebra TC26 vermutet; das genaue Modell muss am Typenschild bestätigt werden.

Für einen TRAKUNA-Scan auf einem Android-Zebra-Gerät ist ein eigenes DataWedge-Profil erforderlich. Die bestehende DPD-/GLS-Konfiguration nicht überschreiben. Das Profil muss TRAKUNA zugeordnet sein und den Intent-Eingang mit der Action `de.trakuna.app.DATAWEDGE_SCAN` sowie dem Extra `com.symbol.datawedge.data_string` verwenden. Die vollständige Anleitung und die Sicherheitshinweise stehen in [PARTNER_INTEGRATION.md](PARTNER_INTEGRATION.md).

Ein Scan wird nur verarbeitet, solange TRAKUNA im Vordergrund aktiv ist. Die DataWedge-Konfiguration und der echte Scan-Empfang sind auf den Shopgeräten noch nicht vor Ort abgenommen.

## 7. Hermes – neues Handgerät angekündigt

Das bisherige Hermes-Handgerät MC3090BT ist laut Betreiber zu alt und soll laut letzter Betreiberinformation in der kommenden Woche durch ein neues Gerät ersetzt werden; die tatsächliche Ankunft ist noch zu bestätigen. Das alte Gerät bleibt bis zur regulären Umstellung ausschließlich für den bestehenden Hermes-Ablauf vorgesehen; TRAKUNA verändert daran nichts.

**Nach Ankunft des neuen Geräts** werden zuerst Hersteller, exaktes Modell, Betriebssystem-/Android-Version, Hermes-App-Version, Scannerart, Druckermodell und Verbindung dokumentiert. Danach prüfen wir ausschließlich mit Betreiberzustimmung, ob ein freigegebener Scan- oder Datenaustauschweg existiert. Eine Android-Installation, DataWedge-Konfiguration oder Änderung der Hermes-App erfolgt nicht ohne ausdrückliche Freigabe.

Bis dahin bleibt Hermes in TRAKUNA nur manuell auswählbar und lokal dokumentierbar. Eine offizielle Hermes-Schnittstelle oder Buchungsbestätigung ist weiterhin nicht vorhanden.

## 8. UPS – App-Anforderung beachten

Die offizielle App **UPS Access Point** ist für registrierte und autorisierte UPS-Access-Point-Standorte vorgesehen. Die Google-Play-Angabe nennt Android 9.0 oder höher und den Paketnamen `com.ups.accesspoint`. Wenn die UPS-App auf demselben Gerät wie TRAKUNA laufen soll, muss dieses Gerät daher mindestens Android 9 verwenden. TRAKUNA selbst hat eine niedrigere Mindestversion; die UPS-App bestimmt hier die höhere Anforderung.

Die UPS-App nicht eigenmächtig installieren oder konfigurieren. Vorhandene App-Version, Gerät und Standortfreigabe müssen vom Betreiber bestätigt werden.

## 9. Datensicherung und Datenschutz

- Lokale Daten liegen nur auf diesem Gerät; es gibt keine zentrale Synchronisierung und keine Cloud-Verbindung.
- Es gibt keine Mitarbeiterkonten oder Rollen.
- Maximal 500 Vorgänge werden lokal gespeichert; archivierte Vorgänge belegen weiterhin einen Platz.
- JSON-Backups und CSV-Exporte enthalten vertrauliche Daten im Klartext. Nur geschützt speichern und nur an berechtigte Personen weitergeben.
- Der CSV-Export enthält zusätzlich die lokal gespeicherten Historieneinträge mit Zeitstempel und Ereignistyp. Diese Historie ist weiterhin nur lokal und nicht manipulationssicher oder zentral bestätigt.
- Keine echten Kundendaten in öffentliche Screenshots, GitHub-Issues oder Chat-Nachrichten aufnehmen.
- Vor dem produktiven Einsatz sind Datenschutzprüfung, Mehrgeräte-/Mitarbeitermodell, Backup-Wiederherstellung und signiertes Produktionsrelease erforderlich.

## 10. Vorgänge archivieren, wiederherstellen und endgültig löschen

Abgeschlossene Vorgänge können über **Vorgang archivieren** aus der aktiven Liste ausgeblendet werden. Die Daten und die lokale Historie bleiben dabei erhalten.

1. Einen Vorgang erst dann archivieren, wenn er tatsächlich an den Kunden bzw. Paketdienst übergeben und der Status in TRAKUNA entsprechend abgeschlossen wurde.
2. In der aktiven Liste **Vorgang archivieren** antippen.
3. Über **Archivierte Vorgänge anzeigen** kann der Vorgang gesucht und geprüft werden.
4. Mit **Wiederherstellen** kommt er in die aktive Liste zurück. Eine Wiederherstellung wird verhindert, wenn dieselbe Sendungsnummer bereits als anderer aktiver Vorgang geführt wird.
5. Erst im Archiv wird die Option zur endgültigen Löschung angezeigt. Sie entfernt Datensatz und Historie dauerhaft vom Gerät.

Archivierte Vorgänge belegen weiterhin einen Platz des lokalen Limits von 500 Datensätzen. Nur eine endgültige Löschung gibt den Platz wieder frei. Vor jeder endgültigen Löschung sind Aufbewahrungspflichten und Betreiberregeln zu prüfen.

Die Archivierung ist keine zentrale oder manipulationssichere Aufbewahrung. Das Gerät kann weiterhin verloren gehen oder lokale Daten können beschädigt werden. Vor wichtigen Änderungen eine geschützte Datensicherung erstellen.

## 11. Verbindliche Unterlagen

- [Pilotpaket und Produktionssperren](PILOT-PAKETSHOP.md)
- [Praxis-Testplan für DPD, GLS, Hermes und UPS](PRAxisTEST.md)
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md)
- [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md)
- [Partner-Schnittstellenvertrag](PARTNER_INTEGRATION.md)
- [Sicherheitsstatus](SECURITY.md)
