# TRAKUNA – Betriebshandbuch

Stand: Integrationsversion 1.54 (versionCode 55) · Build 45769123996 und API-35-Emulator 45769124107 erfolgreich · 8/8 Instrumentierungstests (Debug-Testpaket, noch nicht in main)

Dieses Handbuch erklärt die lokale Testversion Schritt für Schritt. TRAKUNA ist noch keine freigegebene Produktivsoftware. Ausschließlich mit ausdrücklich freigegebenen Testsendungen bzw. erfundenen Testnummern arbeiten.

## 1. Aktuelles Testpaket installieren

1. Den [erfolgreichen Android-Build 45769123996 öffnen](https://github.com/mouzzlii/Fuck/actions/runs/45769123996).
2. Im Abschnitt **Artifacts** das Paket **TRAKUNA-Android** herunterladen (Artifact-ID `15093243107`).
3. Das ZIP entpacken und die enthaltene Datei `app-debug.apk` auf das Android-Gerät übertragen.
4. Die Prüfsumme im entpackten Ordner mit `sha256sum -c SHA256SUMS.txt` prüfen; erwartet wird `app-debug.apk: OK`. Unter Windows kann alternativ `Get-FileHash .\\app-debug.apk -Algorithm SHA256` mit dem Wert in `SHA256SUMS.txt` verglichen werden.
5. Die APK öffnen und installieren. Android kann eine einmalige Erlaubnis für die Installation aus dieser Datei-App verlangen. Nur der vertrauenswürdigen Datei-App diese Erlaubnis geben und danach wieder deaktivieren.
6. TRAKUNA starten und zuerst mit erfundenen Testdaten prüfen.

**Stand des Testpakets**

- App-Version: 1.54 (versionCode 55)
- Geprüfter Commit: `8d9f4058b754084e47d177f0b1190a0bc2860c07`
- Android-Build: [45769123996 – erfolgreich](https://github.com/mouzzlii/Fuck/actions/runs/45769123996)
- Emulator-Instrumentierung: [45769124107 – erfolgreich, 8/8 Tests](https://github.com/mouzzlii/Fuck/actions/runs/45769124107)
- Artefakt: `TRAKUNA-Android`, Artifact-ID `15093243107`
- Verfügbar bis: 10.12.2026, 06:37 UTC
- APK-SHA-256: `18a4ec27365f31a0a21d2fd8dc04342dde4f9d4f7e6150a18eb56438de1e4dc3`

Das Artefakt enthält `SHA256SUMS.txt`; ausschließlich den dort angegebenen Hash für genau diese APK verwenden. Es ist ein Debug-Testpaket, kein signiertes Produktionsrelease. Die Release-Signierung wurde mangels Betreiber-Schlüsseln übersprungen. Die CI-Prüfung ersetzt keine reale Kamera-, Drucker-/Portal- oder Betreiberabnahme.

## 2. Vorhandene Daten bei Updates schützen

**TRAKUNA vor einem Update nicht deinstallieren.** Lokale Sendungsdaten und der zugehörige Android-Keystore-Schlüssel können bei einer Deinstallation verloren gehen.

1. In TRAKUNA eine vollständige passwortgeschützte Datensicherung erstellen.
2. Ein Sicherungspasswort mit 12 bis 128 Zeichen eingeben und bestätigen. Das Passwort wird nicht in TRAKUNA gespeichert.
3. Die `.trakuna`-Datei an einem geschützten Ort ablegen; das Passwort getrennt von der Datei aufbewahren. Ohne das Passwort ist die Sicherung nicht wiederherstellbar.
4. Prüfen, ob die Datei tatsächlich vorhanden ist und eine plausible Dateigröße hat. Die Wiederherstellung ersetzt den gesamten aktuellen Bestand und darf nicht unbedacht auf dem produktiven Gerät getestet werden.
5. Erst dann das neue APK installieren und – wenn Android es anbietet – die bestehende App aktualisieren, nicht deinstallieren.
6. Nach dem Update einige vorhandene Testvorgänge kontrollieren.

Wenn Android eine abweichende Signatur meldet, Installation abbrechen. Die bestehende App nicht löschen und keine App-Daten entfernen. Debug-Builds können unterschiedliche Signaturen besitzen.

## 3. Partner-App-Zuordnung – im Pilot gesperrt

Die aktuelle App-Version 1.55 läuft fest im **SHADOW MODE**. Die Zuordnung externer Partner-Apps und der Start solcher Apps durch TRAKUNA sind in dieser Pilotversion gesperrt. Die vorhandenen Zuordnungsfunktionen sind Legacy-Code und keine Voraussetzung für den Smartphone-/Portalablauf.

- Keine DPD-, GLS-, Hermes- oder UPS-App in TRAKUNA zuordnen.
- Keine externe Carrier-App als von TRAKUNA ausgelöste Übergabe erwarten.
- Bereits separat installierte oder gestartete Carrier-Apps werden durch den lokalen Shadow Mode nicht kontrolliert. Für den Pilotbetrieb dürfen darin keine echten Buchungen oder Labelbestellungen ausgelöst werden.

## 4. Sendungsnummer erfassen und Übergabe simulieren

1. Den passenden Paketdienst in der Hauptmaske auswählen, falls er nicht automatisch erkannt wurde.
2. Die Sendungsnummer manuell eingeben oder mit der Kamera scannen.
3. Die angezeigte Nummer und den Paketdienst vor dem Erfassen kontrollieren.
4. **„Übergabe simulieren · TESTMODUS“** betätigen, sofern dieser Testschritt benötigt wird.
5. TRAKUNA speichert den letzten Übergabewert lokal verschlüsselt. Im Shadow Mode wird keine Partner-App geöffnet, nichts in die Zwischenablage kopiert und keine externe Buchung oder Labelbestellung ausgelöst.

**Lebensdauer und Wiederaufnahme:** Wenn nach einem Scan ein offizielles Paketdienstportal geöffnet wird, speichert TRAKUNA die Sendungsnummer zusätzlich lokal verschlüsselt als Wiederaufnahmeoption. Der Wert läuft nach 24 Stunden ab. TRAKUNA setzt ihn nach einem App-Neustart oder bei Rückkehr aus dem Hintergrund nicht automatisch in das Eingabefeld ein. Stattdessen erscheint – solange der Cache gültig ist – die Schaltfläche **„Letzte Sendungsnummer wiederaufnehmen“**. Die Sendungsnummer wird erst nach ausdrücklicher Bestätigung übernommen. Unter **Einstellungen** kann der Cache jederzeit manuell gelöscht werden. Nach erfolgreicher Erfassung oder Abschluss löscht TRAKUNA nur den Cache-Eintrag, dessen Nummer zum jeweiligen Vorgang passt.

**Nicht als Buchungserfolg werten:** Der Shadow Mode ist eine lokale Simulation. TRAKUNA besitzt derzeit keine bestätigte DPD-, GLS-, Hermes- oder UPS-Buchungsschnittstelle. Eine erfolgreiche lokale Erfassung ist keine Carrier-Bestätigung.

## 5. Lokale Paketshop-Vorgänge

Über **Sendungen verwalten** können Testvorgänge lokal erfasst und ihre Status fortgeschrieben werden:

- Sendung für einen Kunden annehmen und als im Shop eingetroffen führen.
- Eine Sendung als abholbereit markieren und lokal an den Kunden übergeben.
- Eine Kundenabgabe bzw. Retoure erfassen und den lokalen Ablauf bis zur Übergabe an den Paketdienst fortschreiben.

Die lokalen Status ersetzen keine Buchung oder Bestätigung im Carrier-System. TRAKUNA erhält keine Carrier-Bestätigung; erst das offizielle Carrier-System und dessen Quittung sind maßgeblich. Vorhandene Carrier-Programme bleiben führend.

## 6. Zebra TC26 und DataWedge (DPD/GLS)

**Legacy-Hinweis (26.11.2026):** Dieser Abschnitt beschreibt ausschließlich den vorhandenen DataWedge-Codepfad. Die verbindliche Zielarchitektur verwendet ein Android-Smartphone als zentrale TRAKUNA-Bedienoberfläche und die Kamera bzw. manuelle Eingabe. Zebra-Handgeräte sind in diesem Ablauf nur Internet-/Portalgeräte. Es wird keine neue DataWedge- oder Zebra-Hardwareintegration geplant; die folgenden Einrichtungsdetails sind keine Voraussetzung für den neuen Ablauf. Bestehende Shopgeräte und Profile nicht verändern.

Die DPD- und GLS-Handgeräte wurden anhand der bisherigen Angaben als Zebra TC26 vermutet; das genaue Modell und die Android-Version müssen am Gerät bzw. Typenschild bestätigt werden.

**Wichtige Android-Voraussetzung:** TRAKUNA verarbeitet DataWedge-Broadcasts nur ab Android 14 (API 34), wenn Android den Absender eindeutig als `com.symbol.datawedge` bestätigt. Auf Android 13 (API 33) und älteren Versionen ist der DataWedge-Eingang in TRAKUNA aus Sicherheitsgründen deaktiviert. Dort bitte den Kamera-Scanner oder die manuelle Eingabe verwenden.

Für Android 14 oder höher ist ein eigenes DataWedge-Profil erforderlich. Die bestehende DPD-/GLS-Konfiguration nicht überschreiben. Das Profil muss TRAKUNA zugeordnet sein und den Intent-Eingang mit der Action `de.trakuna.app.DATAWEDGE_SCAN`, Delivery **Broadcast Intent** sowie dem Extra `com.symbol.datawedge.data_string` verwenden. Unter **Component Information** ausschließlich das Paket `de.trakuna.app` als Empfänger eintragen und die **Signaturprüfung aktivieren**. Diese DataWedge-seitige Prüfung begrenzt die Zustellung zusätzlich; TRAKUNA prüft unabhängig davon den von Android gemeldeten ursprünglichen Absender exakt auf `com.symbol.datawedge`. Beide Schutzschichten sind für den produktiven Geräteeinsatz vorgesehen. [Offizielle Zebra-Anleitung](https://techdocs.zebra.com/datawedge/latest/guide/output/intent/). Bei Debug-Testpaketen kann sich die Signatur zwischen CI-Läufen ändern; nach jeder Neuinstallation muss die Component-Auswahl erneut geprüft werden. Für den Produktivbetrieb ist ein stabiler Betreiber-Release-Signaturschlüssel erforderlich.

Die vollständige Anleitung und die Sicherheitshinweise stehen in [PARTNER_INTEGRATION.md](PARTNER_INTEGRATION.md) und [WLAN-STARTVORBEREITUNG.md](WLAN-STARTVORBEREITUNG.md).

Ein DataWedge-Scan wird – sofern dieser historische Legacy-Pfad separat weiter genutzt werden soll – nur verarbeitet, solange TRAKUNA im Vordergrund aktiv ist. Dieser Pfad ist keine Voraussetzung des neuen Smartphone-/Portalablaufs. Eine Prüfung wäre nur nach gesonderter Betreiberfreigabe erforderlich.

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
- Neue `.trakuna`-Datensicherungen sind mit AES-256-GCM und PBKDF2-HMAC-SHA256 (600.000 Iterationen) passwortgeschützt. Das Passwort muss 12–128 Zeichen lang sein und getrennt von der Datei aufbewahrt werden.
- Alte JSON-Sicherungen bleiben importierbar, sind aber unverschlüsselt. TRAKUNA kennzeichnet sie vor dem Ersetzen des aktuellen Bestands ausdrücklich als Klartext.
- CSV-Exporte enthalten weiterhin vertrauliche Daten im Klartext. Nur geschützt speichern und nur an berechtigte Personen weitergeben.
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
