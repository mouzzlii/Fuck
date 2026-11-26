# TRAKUNA – Praxis-Testplan (Integrationsversion 1.54 · versionCode 55)

Aktueller App-Code-Stand: Commit `8d9f4058b754084e47d177f0b1190a0bc2860c07`. Android-Build [45769123996](https://github.com/mouzzlii/Fuck/actions/runs/45769123996) und API-35-Emulatorlauf [45769124107](https://github.com/mouzzlii/Fuck/actions/runs/45769124107) sind erfolgreich; 8/8 Instrumentierungstests bestanden. Geprüft wurden Unit-Tests, Android-Lint, Manifest-/Offline-Sicherheitsgrenzen, APK-Build und die Android-Keystore-/Backup-Instrumentierung. Die manuelle Kamera-/Zebra-Abnahme und die Backup-Abnahme B1–B9 auf einem realen Android-Gerät bleiben offen. Debug-APK ersetzt keine reale Hardware- und Betreiberabnahme.

Dieser Testplan hilft, die App vor einem echten Paketshop-Einsatz kontrolliert zu prüfen. Bitte zuerst nur mit Testsendungen bzw. nicht kritischen Sendungsdaten arbeiten.

## 1. Voraussetzungen

- Android-Gerät mit Android 8.0 oder neuer.
- TRAKUNA-APK aus dem zugehörigen erfolgreichen GitHub-Actions-Lauf.
- Kameraberechtigung für den Scan-Test.
- Für die Partnerübergabe eine installierte Betriebssoftware. Eine automatische Übernahme ist nur möglich, wenn die Ziel-App die TRAKUNA-Intent-Extras oder eine dokumentierte API unterstützt.

## 2.0 Kamera-Fokus und erlaubte Codeformate

- Der Kamera-Scanner ist auf QR-Code sowie lineare Strichcodes (Code 128/39/93, Codabar, EAN-13/8, ITF, UPC-A/E) begrenzt. EAN-/UPC-Retailformate werden zwar diagnostisch erkannt, aber nicht als Sendungsnummer übernommen.
- Data Matrix, PDF417, Aztec und andere Formate dürfen von der Kamera nicht als Treffer geliefert werden.
- Kamera nach Öffnen prüfen: Fokus stellt sich auf den mittleren Bildbereich ein; bei unterstützten Kameras läuft kontinuierlicher Autofokus.
- Auf einen QR-Code bzw. Strichcode tippen: Autofokus-/Belichtungsmessung muss auf diesen Bereich neu auslösen.
- Test mit QR-Code und mindestens einem Strichcode durchführen; beide müssen erkannt werden.
- Test mit Data Matrix, PDF417 und Aztec durchführen; diese dürfen keinen Scan auslösen.
- Fokus bei nahen und weiter entfernten Etiketten, bei schwachem Licht und nach Rückkehr aus dem Hintergrund prüfen.
- Der Autofokus hängt von der Kamera-Hardware und deren CameraX-Unterstützung ab; ein Gerät ohne Autofokus kann Software nicht nachträglich fokussierbar machen.

## 2.1 Scan-Diagnose (Version 1.54)

1. In TRAKUNA einen QR-Code bzw. Barcode mit der Kamera scannen.
2. Nach dem Scan die Karte **Scan-Diagnose** öffnen.
3. Barcodeformat, Rohinhalt, Anzeigeinhalt und den von TRAKUNA extrahierten Kandidaten vergleichen.
4. Den GLS-Testfall prüfen: erwartete Sendungsnummer laut Betreiber `31220879741`, bisher fälschlich erkannter Wert `8721335813708`, zusätzlich genannte GLS Track-ID `Z4EIOR5P`. Entscheidend ist zunächst, welcher Wert tatsächlich im QR-Rohinhalt steht; die Diagnose darf diese Kennungen nicht eigenmächtig gleichsetzen.
5. Den Hermes-Test erneut durchführen und sicherstellen, dass die bekannte korrekte Hermes-Erkennung unverändert bleibt.
6. Wenn ein Zebra/DataWedge-Scan verwendet wird, prüfen, ob Rohinhalt und extrahierter Kandidat ebenfalls angezeigt werden.
- Beim Zebra/DataWedge-Scan muss das Format-Extra `com.symbol.datawedge.label_type` mitgeliefert werden. Zugelassen sind QR/GS1-QR sowie Code 128/EAN-128, Code 39/93, Codabar, EAN-13/8, Interleaved 2 of 5 und UPC-A/E0/E1. Fehlendes oder unbekanntes Format sowie Data Matrix, GS1 Data Matrix, PDF417, Aztec, Micro QR und andere nicht freigegebene Typen müssen abgewiesen werden.
- Im DataWedge-Profil die Decoder möglichst zusätzlich auf die tatsächlich benötigten Formate begrenzen. Die App-Allowlist ist eine zweite Schutzschicht und ersetzt die Geräteprofil-Konfiguration nicht.
- **EAN-/UPC-Retailcode (Regressionstest):** Über Zebra/DataWedge je einen bekannten EAN-13-, EAN-8-, UPC-A- und UPC-E-Produktbarcode scannen. Das Diagnosefenster muss Format und Rohinhalt anzeigen, aber „Kein Sendungsnummern-Kandidat“ melden; das Eingabefeld darf keinen dieser Produktcodes übernehmen. Anschließend dieselbe geeignete numerische Testzeichenfolge ausdrücklich als Code 128 prüfen: Die Sperre darf nur von der gemeldeten Symbologie abhängen und keine Nummern anderer Formate pauschal blockieren. Dieselben vier Retailformate auch mit der Kamera testen. Nur freigegebene, nicht produktive Testetiketten verwenden.
- Auf Android 14 (API 34) muss TRAKUNA den ursprünglichen Broadcast-Absender `com.symbol.datawedge` verifizieren; andere oder nicht identifizierbare Absender werden abgewiesen. Auf Android 13 (API 33) und älter weist TRAKUNA DataWedge-Broadcasts vollständig zurück. Dort ausschließlich Kamera-Scanner oder manuelle Eingabe testen; DataWedge Component Information und Signaturprüfung sind kein Ersatz für die in TRAKUNA fehlende Absenderidentität.
7. Diagnosekarte schließen bzw. einen neuen Scan ausführen. Die Rohdaten dürfen nach App-Neustart nicht als dauerhaft gespeicherte Historie erscheinen.
8. Eine Sendungsnummer erfassen, TRAKUNA in den Hintergrund legen und das Gerät längere Zeit in Standby lassen. Falls Android den TRAKUNA-Prozess beendet, App wieder öffnen und prüfen, ob das aktive Eingabefeld wiederhergestellt wurde. Die Diagnose-Rohdaten sollen dabei bewusst nicht wiederhergestellt werden.

**Datenschutz:** Die Rohdatenanzeige bleibt nur temporär im Arbeitsspeicher. Sie darf nicht in Logcat, Dateien, Backups oder die dauerhafte Sendungsdatenbank geschrieben werden. Screenshots von Diagnoseinhalten vor Weitergabe auf Namen, Adressen und andere persönliche Daten prüfen.

## 2. Funktionstests

| Nr. | Test | Erwartetes Ergebnis |
|---|---|---|
| 0 | WLAN-Statusanzeige öffnen bzw. nach Rückkehr in TRAKUNA prüfen | Der Status bezieht sich nur auf das aktuelle Gerät. Es wird weder nach anderen Handgeräten gesucht noch werden Daten synchronisiert. |
| 1 | App starten | TRAKUNA öffnet ohne Absturz. |
| 2 | Sendungsnummer manuell eingeben | Eingabe bleibt sichtbar und kann übergeben werden. |
| 3 | Kamera-Scan erlauben und Barcode scannen | Erkannte Nummer wird nur ins Eingabefeld übernommen. Es erfolgt keine automatische Partner-App-Übergabe oder Buchung. |
| 4 | Kamerazugriff verweigern | Manuelle Eingabe bleibt verfügbar; Hinweis auf fehlende Kameraberechtigung erscheint. |
| 5 | Paket für Kunden annehmen | Vorgang erscheint als „Im Shop eingetroffen“. |
| 6 | Abholbereit markieren | Status wechselt auf „Abholbereit“. |
| 7 | An Kunden übergeben | Status wechselt auf „An Kunden übergeben“. |
| 8 | Sendung vom Kunden annehmen | Vorgang erscheint im passenden Annahme-Status. |
| 9 | Paketdienst manuell eintragen oder korrigieren | Die Angabe in der Hauptmaske wird bei der lokalen Erfassung und bei der Partnerübergabe verwendet und hat Vorrang vor der automatischen Erkennung. Bei leerem Feld wird nur der bestmögliche Erkennungshinweis verwendet. |
| 10 | Formular nach erfolgreicher Erfassung | Empfänger, manuelle Paketdienst-Auswahl und Notiz sind nach erfolgreicher Erfassung leer; ein neuer Vorgang übernimmt keine alten Angaben. |
| 11 | Retoure annehmen und weitergeben | Retoure kann bis zur Übergabe an den Paketdienst fortgeschrieben werden. |
| 12 | Empfänger und Notiz bearbeiten | Geänderte Angaben bleiben nach erneutem Öffnen erhalten. |
| 13 | Suche verwenden | Treffer werden anhand Sendungsnummer, Empfänger, Paketdienst oder Status gefiltert. |
| 14 | CSV-Datei exportieren | Android-Dateidialog erscheint; die gespeicherte Datei enthält die Sendungsliste. |
| 15 | Abgeschlossenen Vorgang archivieren, im Archiv wiederherstellen und erst danach endgültig löschen | Offene Vorgänge lassen sich nicht archivieren oder endgültig löschen. Wiederherstellen entfernt den Archivstatus; endgültiges Löschen ist nur nach Archivierung möglich. |
| 16 | App schließen und neu öffnen | Lokal gespeicherte Vorgänge sind weiterhin vorhanden. |
| 17 | Zebra-Scanner mit DataWedge-Profil auf Android 14+ scannen | Nur bei verifiziertem Absender und freigegebenem Labeltyp erscheint die Sendungsnummer im Eingabefeld; es erfolgt keine automatische Buchung. Auf Android 13 und älter muss DataWedge abgewiesen werden; Kamera/manuelle Eingabe bleiben nutzbar. |
| 18 | Partner-App auswählen und übergeben | Cache wird vor dem Wechsel dauerhaft gespeichert; Clipboard-Ausfall darf keinen Absturz verursachen, Android 13+ markiert den Clipboard-Inhalt als sensibel; Ziel-App wird nur bei installierter App geöffnet. |
| 18a | Nach einer erfolgreichen Übergabe das Sendungsnummernfeld leeren, TRAKUNA in den Hintergrund legen, das Gerät in den Standby schicken und TRAKUNA erneut öffnen | Die verschlüsselt gespeicherte letzte Sendungsnummer wird beim Start bzw. bei der Rückkehr in ein leeres Eingabefeld wiederhergestellt. Eine bereits eingegebene neue Nummer darf dabei nicht überschrieben werden. |
| 19 | DPD, GLS, Hermes und UPS jeweils über „Betriebssoftware pro Paketdienst zuordnen“ einer installierten App zuweisen | Jede Zuordnung wird getrennt auf diesem Gerät gespeichert und in der Übersicht beim richtigen Carrier angezeigt. |
| 20 | Carrier-Schnellauswahl öffnen und DPD, GLS, Hermes, UPS nacheinander auswählen | Der Auswahlknopf zeigt jeweils den gewählten Paketdienst; die Reihenfolge lautet DPD, GLS, Hermes, UPS. Die manuelle Auswahl wird bei allen drei lokalen Vorgangsarten gespeichert und an den Partner-App-Übergabeversuch weitergegeben. Es wird keine externe Buchung behauptet. |
| 21 | „Ohne Auswahl“ wählen und einen bekannten UPS-1Z-Testcode eingeben | Die manuelle Auswahl wird geleert; der bekannte UPS-Format-Hinweis darf als Hinweis erscheinen. Der Format-Hinweis bestätigt keine Existenz oder Buchung. |
| 22 | Nur DPD einer App zuordnen, danach einen GLS-Übergabeversuch starten | Die DPD-App darf nicht als Ersatz für GLS geöffnet werden. TRAKUNA meldet die fehlende GLS-Zuordnung; die lokale Cache-Sicherung bleibt erhalten. |
| 23 | Eine Carrier-Zuordnung entfernen und denselben Übergabeversuch wiederholen | Es wird keine fremde Carrier-App als Fallback geöffnet. Die Nummer bleibt lokal gesichert; die Meldung erklärt die fehlende Zuordnung. |
| 24 | Gerät ohne verfügbare Kamera bzw. Kamera-Hardware nicht verfügbar | Scanner meldet den fehlenden Kamerazugriff verständlich; manuelle Eingabe bleibt bedienbar und TRAKUNA stürzt nicht ab. |
| 25 | Leeres Etikett, unlesbarer Barcode und ungültiger/zu kurzer Code | Es wird kein Sendungsnummern-Kandidat übernommen oder übergeben; Diagnose zeigt den fehlenden bzw. ungültigen Kandidaten. |
| 26 | Dieselbe Sendung zweimal schnell hintereinander mit Kamera und – falls unterstützt – DataWedge scannen | Es wird keine Sendung automatisch angelegt oder an eine Partner-App übergeben. Ein lokaler Vorgang entsteht ausschließlich nach bewusster Bestätigung; doppelte Scan-Ereignisse dürfen keinen doppelten Vorgang erzeugen. |
| 27 | Activity-Neuerstellung und Prozessneustart nach Übergabe testen | Verschlüsselter letzter Übergabewert wird nur in ein leeres Eingabefeld wiederhergestellt; eine zwischenzeitlich neu eingegebene Nummer bleibt erhalten. Temporäre Scan-Rohdaten werden nicht wiederhergestellt. |


### 2a. Zusätzlicher Erfassungstest nach der Rückmeldung „Speichern reagiert nicht“

Diesen Ablauf mit einer erfundenen Testnummer durchführen, nicht mit einer echten Kundensendung:

1. **Paketshop-Sendungen verwalten** öffnen.
2. Eine gültige Testnummer mit mindestens 6 Zeichen eingeben, zum Beispiel `TRAKUNA123456789`.
3. **Paket für Kunden annehmen** wählen.
4. Erwartung: Der Dialog schließt sich nach erfolgreicher Speicherung. Der neue Vorgang erscheint in der Liste mit dem Status **Im Shop eingetroffen**.
5. Den Dialog erneut öffnen und dieselbe Nummer im selben Vorgangstyp nochmals erfassen.
6. Erwartung: TRAKUNA meldet, dass der aktive Vorgang bereits vorhanden ist. Es darf kein zweiter identischer aktiver Vorgang entstehen und es darf keine Erfolgsmeldung für eine neue Speicherung erscheinen.
7. Das Sendungsnummernfeld leeren.
8. Erwartung: Die drei Erfassungs-Schaltflächen sind deaktiviert und direkt im Dialog erscheint der Hinweis, zuerst eine Sendungsnummer einzugeben.
9. Den Testvorgang abschließen, archivieren und erst danach im Archiv endgültig löschen.

**Fehlerprotokoll:** Wenn nach Schritt 3 nichts sichtbar passiert, notieren, ob der Dialog offen bleibt, ob eine Meldung direkt im Dialog erscheint und ob der Vorgang nach erneutem Öffnen der Liste vorhanden ist. Keine zweite Buchung durch wiederholtes Tippen erzwingen, bevor der Bestand geprüft wurde.



## 2b. Pflicht-Testmatrix der vier Start-Paketdienste

Jeder Paketdienst wird getrennt mit freigegebenen Testetiketten bzw. fiktiven Testnummern geprüft. Vorhandene Carrier-Handgeräte, Apps und Drucker bleiben unverändert.

| Paketdienst | Auswahl in TRAKUNA | Scan-/Eingabetest | Partner-/Gerätetest | Freigabestatus |
|---|---|---|---|---|
| DPD | DPD im Schnellauswahlmenü | TC26/DataWedge nur bei Android 14+ mit verifiziertem Absender testen; auf älteren Android-Versionen Kamera/manuelle Eingabe. Keine automatische DPD-Erkennung voraussetzen | Bestehendes DPD-Handgerät und Bluetooth-Drucker parallel beobachten; offizielle Übergabe-/API-Freigabe klären | Offen |
| GLS | GLS im Schnellauswahlmenü | TC26/DataWedge nur bei Android 14+ mit verifiziertem Absender testen; auf älteren Android-Versionen Kamera/manuelle Eingabe. Keine automatische GLS-Erkennung voraussetzen | Bestehendes GLS-Handgerät und Bluetooth-Drucker parallel beobachten; offizielle Übergabe-/API-Freigabe klären | Offen |
| Hermes | Hermes im Schnellauswahlmenü | Für das bisherige MC3090BT wurde ein Ersatzgerät für die kommende Woche angekündigt; die tatsächliche Ankunft ist noch zu bestätigen. Nach Ankunft exaktes Modell, Betriebssystem, Hermes-App und Scanner erfassen; bis dahin keine Integrationstests am Altgerät | Bestehenden Hermes-Ablauf und Drucker nicht verändern. Neues Gerät erst nach Betreiberfreigabe untersuchen; offizielle Schnittstelle und Abnahme bleiben Pflicht | Offen – Pilotblocker |
| UPS | UPS im Schnellauswahlmenü | Bekannte 1Z-Format-Erkennung nur als Hinweis prüfen; zusätzlich manuelle Nummern testen | Prüfen, ob die offizielle App `UPS Access Point` (`com.ups.accesspoint`) installiert und vom Standort freigegeben ist; Version und Android-Gerät erfassen. Die App verlangt laut Google-Play-Angabe Android 9.0 oder höher. Nicht eigenmächtig installieren oder konfigurieren. | Offen |



**Geräteabgleich DPD/GLS:** GLS Österreich beschreibt für neue Partner ein Smartphone, einen Bluetooth-Drucker und eine eigene GLS-App. Der gemeldete TC26-/Zebra-Drucker-Bestand muss mit GLS abgeglichen werden; vorhandene Hardware bis zur ausdrücklichen Freigabe nicht verändern.

Für **jeden** der vier Paketdienste müssen mindestens diese Fälle protokolliert werden:

1. Manuelle Auswahl des richtigen Paketdienstes; Auswahl bleibt bei der lokalen Erfassung erhalten.
2. Eine Testnummer scannen und vor dem Speichern sichtbar kontrollieren.
3. Eine absichtlich falsche Paketdienst-Auswahl korrigieren; keine automatische Buchung darf ausgelöst werden.
4. Dieselbe Testnummer erneut erfassen; aktive Duplikate müssen blockiert werden.
5. Partner-App nicht installiert bzw. nicht integrationsfähig: TRAKUNA muss ehrlich melden, dass keine Buchung bestätigt wurde.
6. Annahme, Kundenausgabe, Retoure und Übergabe an den Zusteller nur dann als abgeschlossen markieren, wenn der lokale Testablauf dies ausdrücklich vorsieht bzw. das autorisierte Partnersystem die Aktion bestätigt.

Die Carrier-Schnellauswahl und die getrennten App-Zuordnungen in Version 1.49 sind **keine** offizielle Integration. Ein grüner CI-Build ersetzt keinen dieser Gerätetests und keine schriftliche Freigabe.

## 3. Sicherheits- und Fehlerprüfungen

- Vorgangshistorie nach der Erfassung öffnen: Der Eintrag „Vorgang angelegt“ muss mit dem korrekten Datum und der Uhrzeit erscheinen.
- Status eines Testvorgangs ändern und Historie erneut öffnen: Der Statuswechsel muss mit altem und neuem Status erscheinen.
- Empfänger oder Notiz ändern: Ein Eintrag „Empfänger oder Notiz bearbeitet“ muss erscheinen.
- App schließen und neu öffnen: Die Historie muss weiterhin zum richtigen Vorgang gehören.
- JSON-Sicherung exportieren und auf einer Testinstallation wiederherstellen: Die vorhandenen Historieneinträge müssen erhalten bleiben.
- Einen älteren Testdatensatz bzw. eine ältere Sicherung ohne Historienfeld laden: Der Datensatz muss weiterhin lesbar sein und einen Hinweis auf fehlende Historie anzeigen.
- Einen archivierten Vorgang endgültig löschen: Die zugehörige lokale Historie darf danach nicht mehr in der App angezeigt werden; ein offener Vorgang muss gegen endgültiges Löschen geschützt bleiben.
- Historie darf nicht als manipulationssicheres Mitarbeiter-Audit oder als Carrier-Bestätigung ausgegeben werden.


- Auf Android 14+ Zebra-/DataWedge-Broadcast ohne Sendungsnummern-Extra oder mit einem falsch typisierten Extra prüfen: Der Scan wird ignoriert und TRAKUNA bleibt bedienbar. Auf Android 13 und älter muss bereits der DataWedge-Eingang vollständig abgewiesen werden.

- Ungültige oder zu kurze Sendungsnummern dürfen nicht als neuer Vorgang gespeichert werden.
- Eine manuell eingegebene Sendungsnummer mit eingebetteten Steuerzeichen (z. B. NUL) muss abgelehnt werden; die Zeichen dürfen nicht stillschweigend entfernt und dadurch in eine andere Nummer umgewandelt werden.
- Sendungsnummern mit Unicode-Zeichen, die normalen Buchstaben oder Zahlen nur ähnlich sehen, müssen auch bei der Partnerübergabe abgelehnt werden.
- Derselbe offene Vorgang darf nicht versehentlich mehrfach angelegt werden; die App muss klar „bereits vorhanden“ melden und darf nicht „gespeichert“ vortäuschen.
- Löschvorgänge müssen eine Bestätigung verlangen.
- Kamera-Ausfall darf die manuelle Eingabe nicht blockieren.
- Keine Passwörter, Zugangsdaten oder geheimen API-Schlüssel in Testnotizen eintragen.
- Exportdateien enthalten möglicherweise personenbezogene Daten. Nur an berechtigte Empfänger weitergeben und nach dem Test sicher verwahren oder löschen.

## 4. Testprotokoll

Für jeden Test notieren:

- Gerät und Android-Version
- Datum/Uhrzeit
- Testnummer
- Ergebnis: Bestanden / Fehlgeschlagen / Nicht testbar
- Bei Fehlern: genaue Schritte, angezeigte Meldung und optional ein Screenshot ohne unnötige personenbezogene Daten

## 5. Abnahmekriterien

Ein begleiteter Praxistest ist erst bestanden, wenn alle für den konkreten Shop benötigten Abläufe auf dem tatsächlichen Android-Gerät erfolgreich getestet wurden.

Ein grüner GitHub-Build bestätigt nur, dass Tests und APK-Erstellung im CI-Lauf erfolgreich sind. Er bestätigt nicht die Kompatibilität mit einer fremden Betriebssoftware, die Erkennung aller Paketetiketten oder die Eignung für den produktiven Paketshop-Betrieb.

## 6. Noch offene Voraussetzungen für Produktivbetrieb

- Name und dokumentierte Schnittstelle der tatsächlich verwendeten Betriebssoftware
- Reale Paketlabeltests je eingesetztem Carrier
- Datenschutz- und Sicherheitsprüfung
- Zentrale Datenhaltung, Benutzerkonten, Rollen und Audit-Protokoll, falls mehrere Geräte oder Mitarbeiter zusammenarbeiten
- Backup- und Wiederherstellungsverfahren
- Praxistest und Freigabe durch den Paketshop-Betreiber

## 7. Zusätzlicher Zebra DataWedge-Sicherheitstest

1. DataWedge-Profil für TRAKUNA öffnen und unter Intent Output → Component Information das Paket `de.trakuna.app` hinzufügen.
2. Bei der Abfrage die Signaturprüfung aktivieren und kontrollieren, dass sie in der Component-Liste als aktiv angezeigt wird.
3. Einen Barcode mit dem Zebra-Auslöser scannen. Der Wert muss in TRAKUNA erscheinen.
4. TRAKUNA aktualisieren oder neu installieren und prüfen, ob die Signaturprüfung im DataWedge-Profil weiterhin aktiv ist. Bei einem Signaturwechsel die Komponente neu auswählen.
5. Auf Android 14 (API 34) einen gleichartigen Broadcast aus einer nicht autorisierten Test-App senden. TRAKUNA muss ihn wegen des abweichenden bzw. fehlenden Absenders ablehnen. Auf Android 13 (API 33) und älter darf ein DataWedge-Broadcast unabhängig von seinem Inhalt nicht übernommen werden; Kamera-Scanner und manuelle Eingabe müssen weiterhin funktionieren. Ein Spoofing-Test auf diesen älteren Versionen ist nur ergänzend und darf nicht als Ersatz für die vollständige Deaktivierung gewertet werden.

Die App kann nicht selbst bestätigen, ob die geräteseitige Allowlist oder Signaturprüfung aktiviert ist. Die Konfiguration muss daher vom Geräteadministrator dokumentiert werden.

## Datensicherung und Wiederherstellung

1. In „Paketshop-Sendungen verwalten“ eine vollständige passwortgeschützte Datensicherung erstellen.
2. Ein Passwort mit 12–128 Zeichen eingeben und bestätigen. Es wird nicht in TRAKUNA gespeichert; für eine spätere Wiederherstellung ist es zwingend erforderlich.
3. Prüfen, dass die Datei `TRAKUNA-Sicherung.trakuna` vorhanden ist. Die Datei enthält ein AES-256-GCM-verschlüsseltes Backup; der Klartext darf nicht in der Datei sichtbar sein.
4. Für eine Wiederherstellung die Datei auswählen. TRAKUNA liest höchstens 6 MiB ein, erkennt das verschlüsselte Format, fragt das Passwort ab, entschlüsselt und validiert Format, Version, Einträge und Höchstmenge.
5. Vor dem Ersetzen des lokalen Bestands erscheint eine Bestätigungsabfrage mit der Anzahl der Vorgänge. Ohne ausdrückliche Bestätigung bleiben die aktuellen Daten unverändert.
6. Falsches Passwort, beschädigter Header, Salt, Nonce oder Ciphertext müssen abgelehnt werden; der aktuelle Bestand bleibt unverändert.
7. Alte unverschlüsselte JSON-Sicherungen bleiben importierbar, müssen aber vor dem Ersetzen ausdrücklich als Klartext gekennzeichnet werden.
8. Nach Wiederherstellung die angezeigte Anzahl und einige Testvorgänge mit dem erwarteten Datenbestand vergleichen.
9. Beschädigten lokalen Datenspeicher ausschließlich in einer Testinstallation simulieren. Eine gültige Sicherung muss den beschädigten Bestand wiederherstellen können; bei ungültiger Sicherung dürfen vorhandene Daten nicht überschrieben werden.

**Wichtig:** Das Sicherungspasswort getrennt von der `.trakuna`-Datei aufbewahren. CSV-Exporte bleiben unverschlüsselt und enthalten vertrauliche Daten im Klartext.


## Zusätzliche Tests für Version 1.19 – lokale Verschlüsselung

18. **Bestandsmigration:** App mit einem bestehenden lokalen Testbestand aktualisieren; prüfen, dass alle Sendungen nach dem ersten Start unverändert vorhanden sind.
19. **Migration ohne Datenverlust:** App-Datenmigration bei knappem Gerätespeicher testen; bei Fehler darf kein leerer Bestand als Erfolg erscheinen.
20. **Neuer Datensatz:** Testsendung erfassen, App vollständig schließen und neu öffnen; Datensatz muss weiterhin lesbar sein.
21. **Verschlüsselter Bestand:** In den App-SharedPreferences darf der Wert des Paketbestands nicht mehr als lesbares JSON mit Sendungsnummern oder Empfängernamen erscheinen.
22. **Schlüssel-/Datenfehler:** Auf einem isolierten Testgerät den Keystore-Schlüssel entfernen bzw. beschädigte Testdaten verwenden; App muss einen Speicherfehler melden und darf keine neuen Datensätze über den beschädigten Bestand schreiben.
23. **Backup:** JSON-Export und Wiederherstellung testen; ausdrücklich beachten, dass exportierte Dateien weiterhin Klartext enthalten.


## Zusätzliche Tests für Version 1.20 – Größenbegrenzung

24. **Normaler Bestand:** Mehrere Testsendungen mit langen, aber zulässigen Empfängernotizen erfassen, App schließen und erneut öffnen.
25. **Grenzwertprüfung:** Auf einem isolierten Testgerät einen beschädigten/überlangen verschlüsselten Testwert simulieren; TRAKUNA muss einen Speicherfehler anzeigen und darf keine Paketdaten überschreiben.
26. **Keine Überlastung:** Überlange Sicherungsdateien importieren; der Import muss vor vollständigem Einlesen bzw. Entschlüsseln abgewiesen werden.


## Zusätzliche Tests für Version 1.21 bis 1.24 – Scan- und Wiederherstellungsabläufe

27. **Übergabe speichern:** Eine Testsendungsnummer an die Partner-App übergeben; TRAKUNA muss sie vor dem App-Wechsel dauerhaft speichern.
28. **Cache nach Neustart:** App schließen und erneut öffnen; die zuletzt übergebene Sendungsnummer muss aus dem verschlüsselten Cache lesbar sein.
29. **Migration:** Auf einem isolierten Testgerät einen vorhandenen Klartext-Cache aktualisieren; Nummer muss erhalten bleiben und die alten Klartextfelder müssen nach erfolgreichem Commit entfernt sein.
30. **Beschädigter Cache:** Verschlüsselten Testwert beschädigen; TRAKUNA darf keinen falschen Wert ausgeben und muss einen sicheren Fehlerzustand beibehalten.
31. **Partner-Auswahl:** Wechseln und Entfernen der gespeicherten Partner-App darf den verschlüsselten Sendungsnummern-Cache nicht löschen.
32. **Kamera-Scan ohne automatische Übergabe:** Ein Barcode wird erkannt und ins Feld übernommen. TRAKUNA darf nicht selbstständig eine Partner-App öffnen oder eine Sendung buchen. Erst nach bewusster Betätigung der Übergabe-Schaltfläche darf die Übergabe starten.
33. **Zebra-Scan ohne automatische Übergabe:** Einen Barcode über DataWedge senden. Die Nummer muss nur ins Eingabefeld übernommen werden; keine Partner-App darf automatisch starten.
34. **Kamera schließen während Initialisierung:** Scanner öffnen und unmittelbar wieder schließen, während die Kamera noch initialisiert. Die Kamera darf danach nicht im Hintergrund starten und es darf keine Fehlermeldung nach dem Schließen erscheinen.
35. **Scan-Ergebnis nach Schließen verwerfen:** Während ein Barcode erkannt wird, den Scanner schließen. Ein später eintreffendes Kamera-Ergebnis darf das Sendungsnummernfeld oder den Status nicht mehr verändern.
36. **Mehrere Codes gleichzeitig:** Zwei unterschiedliche Sendungsnummern gleichzeitig ins Kamerabild halten. TRAKUNA muss die Auswahl ablehnen und darf keine davon automatisch übernehmen.
37. **Übergabe-Bestätigung:** Bei einem abholbereiten Paket die Aktion „An Kunden übergeben“ wählen. Vor der Statusänderung muss eine Bestätigung erscheinen; Abbrechen darf den Status nicht ändern. Nach Bestätigung muss der Vorgang abgeschlossen sein.
38. **Weitere Suchtreffer:** Mehr als 100 Testvorgänge anlegen oder eine Sicherung mit über 100 Einträgen importieren. Die Liste muss über „Weitere Treffer anzeigen“ alle Treffer erreichbar machen; eine Suche muss die Trefferzahl und Anzeige korrekt zurücksetzen.
39. **Überlanger Zebra-Scan:** Einen DataWedge-Testbroadcast mit mehr als 4096 Zeichen senden. TRAKUNA muss ihn mit einer verständlichen Meldung ablehnen und darf keine Sendungsnummer übernehmen.
40. **Überlange manuelle Eingabe:** Mehr als 80 Zeichen in das Sendungsnummernfeld einfügen. Das Feld darf nicht unbegrenzt anwachsen; die Eingabe muss auf die definierte Länge begrenzt bleiben.
41. **Zeitstempel in der Liste:** Erfassungszeitpunkt und letzte Änderung müssen im deutschen Datumsformat in der Sendungskarte sichtbar sein.
42. **Zeitstempel im CSV:** Die Spalten „Erstellt (UTC)“ und „Aktualisiert (UTC)“ müssen ISO-8601-Zeitstempel enthalten, z. B. 1970-01-01T00:00:00Z für den Testwert 0.
43. **Feldlängen:** Empfänger, Paketdienst, Notiz und Suche mit sehr langen Texten befüllen. Die Eingabe darf die definierten Feldgrenzen nicht überschreiten.
44. **CSV-Datenschutzwarnung:** CSV-Export starten. Vor dem Dateidialog muss eine Warnung über Sendungsnummern, Empfängerdaten, Notizen und unverschlüsselte Speicherung erscheinen. Abbrechen darf keine Datei erzeugen.
45. **Kleine Displays und Tastatur:** App auf einem kleinen Android-Bildschirm mit eingeblendeter Tastatur testen. Eingabefelder und Schaltflächen dürfen nicht dauerhaft hinter Systemleisten oder Tastatur verschwinden.
46. **App-Symbol:** TRAKUNA muss mit dem neuen Waldgrün-Scan-Symbol im Android-Launcher erscheinen.
47. **Betriebssoftware-Auswahl scrollen:** Bei vielen installierten Apps muss die Auswahlliste innerhalb des Dialogs scrollbar bleiben.
48. **Migrationsprüfung verschlüsselter Daten:** Beschädigte oder strukturell ungültige verschlüsselte Paketdaten dürfen einen vorhandenen alten Klartextbestand nicht löschen.
49. **Übergabe-Cache-Feldprüfung:** Ungültige Sendungsnummern, überlange Paketdienstangaben und ungültige Zeitstempel müssen als Cachefehler erkannt werden.
50. **Wiederherstellung bei defektem lokalem Speicher:** Auf einem isolierten Testgerät den lokalen Datenspeicher beschädigen, danach eine zuvor erstellte gültige Sicherung auswählen. Die App muss die Wiederherstellung trotz Speicherfehler anbieten, vor dem Ersetzen warnen und danach die gesicherten Einträge wieder anzeigen. Bei fehlgeschlagener Verschlüsselung darf sie keinen Erfolg melden.
51. **Cache-Lesefehler:** Auf einem isolierten Testgerät den verschlüsselten Cache beschädigen oder den Keystore-Schlüssel entfernen. Ein Lesezugriff muss einen Fehler melden und darf nicht so tun, als wäre lediglich noch keine Sendungsnummer gespeichert.
52. **Cache-Schreibfehler:** Auf einem isolierten Testgerät einen Speicherausfall simulieren. Die Partner-App darf nicht gestartet werden, wenn das vorherige sichere Speichern nicht bestätigt wurde; die Fehlermeldung muss den Fehler verständlich benennen.


## 8. Zusätzliche Parser- und QR-Code-Prüfungen

Diese Prüfungen ergänzen die Kamera- und DataWedge-Tests. Sie können mit gedruckten Testetiketten oder vorbereiteten QR-Testcodes durchgeführt werden:

53. **AIM-Präfixe:** Code-128-Scan mit `]C1` und QR-Scan mit `]Q3` testen. Das Präfix darf nicht Teil der gespeicherten Sendungsnummer sein.
54. **QR-Parameter:** QR-Code mit `tracking`, `tracking_number`, `sendungsnummer` oder `id` als Parametername testen. Groß-/Kleinschreibung darf die Erkennung nicht verhindern.
55. **Mehrere Parameter:** Ein QR-Code enthält zuerst einen ungültigen Tracking-Parameter und danach einen gültigen. TRAKUNA darf nicht am ersten ungültigen Parameter abbrechen.
56. **Prozentkodierung:** Eine URL mit Prozentkodierung innerhalb der Sendungsnummer testen. Nach dem Dekodieren muss exakt die gültige Nummer erscheinen.
57. **QR-Pfad:** Eine URL testen, bei der die Sendungsnummer als letztes Pfadsegment enthalten ist. Es darf nur der erkannte Wert übernommen werden.
58. **Ungültiger Inhalt:** Beliebige URL, ungültige Nummer, Unicode-Zeichen, beschädigte Prozentkodierung und überlange Scan-Inhalte testen. Es darf kein erfundener oder stillschweigend veränderter Wert übernommen werden.
59. **Keine automatische Aktion:** Nach jeder Erkennung muss die Nummer zunächst sichtbar im Eingabefeld stehen. Ohne bewusste Bedienung darf kein Paketshop-Vorgang angelegt und keine Partner-App geöffnet werden.

Die automatisierten Parser-Tests prüfen die Regeln ohne Kamera-Hardware. Sie ersetzen nicht den Test mit echten Zebra-Geräten, echten Etiketten und der konkret eingesetzten Betriebssoftware.


## 9. Verhalten bei Hintergrundbetrieb

60. TRAKUNA auf Android 14+ öffnen und im Vordergrund lassen; einen DataWedge-Testbarcode mit freigegebenem Labeltyp scannen. Bei verifiziertem Absender muss die erkannte Nummer im Sendungsnummernfeld erscheinen. Auf Android 13 und älter muss DataWedge abgewiesen werden; Kamera und manuelle Eingabe bleiben verfügbar.
61. TRAKUNA in den Hintergrund wechseln, ohne die App zu beenden; einen weiteren Barcode scannen. Der Hintergrundscan darf die Eingabemaske nicht verändern.
62. TRAKUNA auf Android 14+ wieder in den Vordergrund holen und erneut scannen. Der neue Scan muss wieder zur manuellen Prüfung übernommen werden. Auf Android 13 und älter sind für diesen Ablauf Kamera oder manuelle Eingabe zu verwenden.
63. TRAKUNA über die Übersicht vollständig schließen und anschließend neu öffnen. Erst nach dem Öffnen darf der nächste Scan wieder verarbeitet werden.


## Zusätzliche DPD-Pilotprüfungen aus dem Partnerhandbuch

Das öffentlich gefundene Pickup-Partnerhandbuch ist von DPD Deutschland (01/2026). Die folgenden Punkte sind deshalb zunächst Prüffragen für DPD Österreich, keine bereits gültige österreichische Betriebsanweisung:

- [ ] Kundenabgabe: Label/QR scannen, Annahme im DPD-System bestätigen, Beleg-/Labelausgabe und sichere Lagerung prüfen.
- [ ] Kundenausgabe: Identität und ggf. Vollmacht nach DPD-Vorgabe prüfen; tatsächlichen Abholscan und Bestätigung des DPD-Systems abwarten.
- [ ] Zusteller-Anlieferung: DPD-Gegenscan bzw. QR-basierte Bestätigung und dokumentierten Haftungsübergang im Originalsystem beobachten.
- [ ] Zusteller-Abholung: vollständige Paketliste, Gegenscan des Fahrers und finale Quittierung im DPD-System beobachten.
- [ ] Lagerfristüberschreitung / Rückführung nach DPD-Vorgabe prüfen.
- [ ] DPD Österreich bestätigt schriftlich, welche dieser Schritte am konkreten österreichischen Standort gelten.

**Abnahmekriterium:** TRAKUNA darf den lokalen Status nicht als Carrier-bestätigt darstellen, wenn nur ein eigener Scan oder das Öffnen der Partner-App erfolgt ist.


## Zusätzliche GLS-Abnahmepunkte (Österreich)

- [ ] GLS bestätigt die für den Standort geltenden Annahme-, Abhol- und Retourenabläufe.
- [ ] Offizielle GLS-Quittung/Übergabebeleg für Kundenabgabe und Fahrerabholung wird im bestehenden GLS-System erzeugt und geprüft.
- [ ] TRAKUNA-eigene lokale Scans oder Quittungen werden nicht als GLS-Beleg oder GLS-Haftungsbestätigung bezeichnet.
- [ ] GLS bestätigt schriftlich, ob eine zusätzliche App-/Scannerintegration zulässig und technisch unterstützt ist.


## Neue Status-Sicherheitsprüfung in Version 1.48

- [ ] Einen ausgehenden Vorgang bis zur Paketdienst-Übergabe fortschreiben.
- [ ] Dialogtext kontrollieren: TRAKUNA erklärt, dass keine Carrier-Bestätigung empfangen wird und nur nach abgeschlossener Quittierung im Originalsystem lokal dokumentiert werden soll.
- [ ] Nach Bestätigung kontrollieren: Status lautet ausdrücklich „Lokal als an Paketdienst übergeben dokumentiert“ und nicht „Carrier-Buchung bestätigt“.

## 10. Ergänzende Android-Instrumentierungstests (Integrationsversion 1.54)

Der Android-Build [45769123996](https://github.com/mouzzlii/Fuck/actions/runs/45769123996) auf Commit `8d9f4058b754084e47d177f0b1190a0bc2860c07` ist erfolgreich. Der separate API-35-Emulatorlauf [45769124107](https://github.com/mouzzlii/Fuck/actions/runs/45769124107) auf demselben Commit hat **8/8 Instrumentierungstests** erfolgreich ausgeführt. Dazu gehören die verschlüsselte Backup-Wiederherstellung, falsche Passwortablehnung, Legacy-PBKDF2-Kompatibilität, verschlüsselte lokale Speicherung und der Schutz vor doppelter Statusfortschreibung. Die Tests bestätigen keinen Kamera- oder Zebra-Hardwarebetrieb.

- [ ] **Keystore-Verschlüsselung:** Testsendung speichern, SharedPreferences kontrollieren und bestätigen, dass Sendungsnummer, Empfänger und Notiz nicht im Klartext vorkommen.
- [ ] **Speicher-Wiederöffnung:** Neue `ShipmentStore`-Instanz erstellen und kontrollieren, dass der verschlüsselte Bestand vollständig wieder lesbar ist.
- [ ] **Fail-closed beim beschädigten Bestand:** Verschlüsselten Datensatz beschädigen; die App muss einen Speicherfehler anzeigen und weitere Schreibvorgänge ablehnen.
- [ ] **Übergabe-Cache-Verschlüsselung:** Sendungsnummer und Paketdienst speichern; Klartext darf nicht in SharedPreferences stehen und muss nach Neuinitialisierung lesbar sein.
- [ ] **Beschädigter Übergabe-Cache:** Verschlüsselten Cachewert beschädigen; die App muss einen Fehler melden und darf keinen leeren Cache vortäuschen.
- [ ] **Backup-Recovery:** Gültige Sicherung erstellen, lokalen verschlüsselten Bestand beschädigen und anschließend die gültige Sicherung wiederherstellen. Die wiederhergestellten Daten müssen nach erneuter Leseprüfung vorhanden sein.


## 11. Zusätzliche manuelle Abnahme für die passwortgeschützte Datensicherung

Diese Prüfungen sind trotz erfolgreicher automatisierter JVM- und Emulatorläufe noch auf einem realen Android-Zielgerät durchzuführen. Sie werden hier bewusst nicht als erledigt markiert.

| Nr. | Test | Erwartetes Ergebnis |
|---|---|---|
| B1 | Sicherung mit einem Passwort von 12–128 Zeichen erstellen | Datei endet auf `.trakuna`; Passwort wird zweimal bestätigt und nicht gespeichert. |
| B2 | Sicherungsdatei mit einem Texteditor öffnen | Sendungsnummern, Empfängernamen und Notizen sind nicht im Klartext sichtbar. |
| B3 | Datei mit richtigem Passwort importieren | Entschlüsselung und vollständige Validierung gelingen; der Bestand wird erst nach ausdrücklicher Bestätigung ersetzt. |
| B4 | Falsches Passwort eingeben | Import wird abgelehnt; der aktuelle Bestand bleibt unverändert. |
| B5 | Header, Salt, Nonce oder Ciphertext verändern | Authentifizierungsprüfung schlägt fehl; keine Daten werden wiederhergestellt. |
| B6 | Passwort mit Umlauten und Sonderzeichen verwenden | Export und Wiederherstellung funktionieren auf dem Zielgerät. |
| B7 | Alte JSON-Sicherung importieren | Import bleibt möglich, zeigt aber vor dem Ersetzen eine deutliche Klartext-Warnung. |
| B8 | Datei oberhalb der Größenbegrenzung importieren | Import wird begrenzt abgewiesen; der aktuelle Datenbestand bleibt erhalten. |
| B9 | Oberfläche während KDF und Datei-I/O beobachten | App bleibt bedienbar; Kryptografie und Dateioperationen blockieren nicht den UI-Thread. |

**Abnahmesperre:** Erst nach dokumentierter Durchführung von B1–B9 auf einem realen Android-Gerät kann die manuelle Backup-Funktion als abgenommen gelten. Ein Emulatorlauf ersetzt diese Prüfung nicht.

## 12. Zusätzliche Kamera-Abnahme für die stabile Scanbestätigung (Version 1.55)

Die Kamera übernimmt einen Sendungsnummern-Kandidaten erst nach zwölf aufeinanderfolgenden ML-Kit-Analysebildern mit genau einem eindeutigen Kandidaten. Diese Bestätigung ist eine zusätzliche Schutzmaßnahme und ersetzt nicht die Sichtprüfung durch den Mitarbeiter.

- [ ] **Stabiles Etikett:** Einen einzelnen gültigen Paketbarcode ruhig und scharf vor die Kamera halten. Erst nach stabiler Erkennung muss die Nummer ins Eingabefeld übernommen werden.
- [ ] **Kurz sichtbarer Code:** Barcode nur kurz ins Bild halten und vor Ablauf der zwölf aufeinanderfolgenden Bestätigungsbilder entfernen. Es darf keine Nummer automatisch übernommen werden.
- [ ] **Unterbrochene Folge:** Einen Barcode zunächst sichtbar halten, dann kurz aus dem Bild nehmen und erneut zeigen. Die Zählung muss nach dem leeren Bild neu beginnen.
- [ ] **Wechselnder Code:** Während der Bestätigung zwischen zwei unterschiedlichen Sendungsnummern wechseln. Ein Kandidat darf nicht aus einer unterbrochenen Folge übernommen werden.
- [ ] **Mehrere Codes gleichzeitig:** Zwei unterschiedliche Sendungsnummern gleichzeitig ins Bild halten. Die Bestätigung muss zurückgesetzt werden; keine der Nummern darf automatisch übernommen werden.
- [ ] **Produktbarcode neben Sendungscode:** Einen EAN-/UPC-Produktcode gemeinsam mit einem gültigen Versandbarcode zeigen. Der Produktcode darf nicht als Sendungsnummer gelten; ein eindeutiger Versandkandidat muss zwölf aufeinanderfolgende Bilder lang bestätigt werden.
- [ ] **Diagnosebegrenzung:** Bei wechselnden Codes prüfen, dass die Diagnose höchstens 30 unterschiedliche Rohbeobachtungen anzeigt und die App währenddessen stabil bleibt.
- [ ] **Keine automatische Buchung:** Auch nach bestätigtem Scan darf TRAKUNA keine Partner-App automatisch öffnen und keinen Paketshop-Vorgang anlegen. Der Mitarbeiter muss den nächsten Schritt bewusst auswählen.

Die automatisierte Unit-Prüfung deckt die Bestätigungslogik (stabile Folge, leeres Bild, mehrdeutiges Bild, Kandidatenwechsel) ab. Die tatsächliche Kameraerkennung, Bildrate, Autofokusleistung und Bedienbarkeit sind auf dem Android-Zielgerät zu prüfen.
