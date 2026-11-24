# TRAKUNA – Praxis-Testplan (Version 1.53 · versionCode 54)

Aktueller Teststand: Version 1.53 ergänzt die sichere Archivierung abgeschlossener Vorgänge, Wiederherstellung und Backup-Schema 2. Der erfolgreiche CI-Lauf 45293985061 auf Commit `45a02f7ec8de966100c406bbfdd8e3e572052b05` bestätigt Unit-Tests, Kompilierung der Android-Instrumentierungstestfälle, Android-Lint, Manifest-Sicherheitsprüfungen, Debug-APK-Build und Artefakt-Upload. Die Android-Instrumentierungstests werden separat ausgeführt; Emulator-Workflow 45295645386 für den aktuellen Code-Commit ist noch in Bearbeitung. Ein erfolgreicher Lauf ist noch nicht bestätigt. [Build öffnen und Testpaket herunterladen](https://github.com/mouzzlii/Fuck/actions/runs/45293985061). APK-SHA-256: `ce7ed6093e213af7167e5bb2ebfc9c97fd89123fc4231f82fb94b8a4e8143f2b`.

Dieser Testplan hilft, die App vor einem echten Paketshop-Einsatz kontrolliert zu prüfen. Bitte zuerst nur mit Testsendungen bzw. nicht kritischen Sendungsdaten arbeiten.

## 1. Voraussetzungen

- Android-Gerät mit Android 8.0 oder neuer.
- TRAKUNA-APK aus dem zugehörigen erfolgreichen GitHub-Actions-Lauf.
- Kameraberechtigung für den Scan-Test.
- Für die Partnerübergabe eine installierte Betriebssoftware. Eine automatische Übernahme ist nur möglich, wenn die Ziel-App die TRAKUNA-Intent-Extras oder eine dokumentierte API unterstützt.

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
| 17 | Zebra-Scanner mit DataWedge-Profil scannen | Sendungsnummer erscheint im Eingabefeld; es erfolgt keine automatische Buchung. Ungültige Inhalte werden abgelehnt. |
| 18 | Partner-App auswählen und übergeben | Cache wird vor dem Wechsel dauerhaft gespeichert; Clipboard-Ausfall darf keinen Absturz verursachen, Android 13+ markiert den Clipboard-Inhalt als sensibel; Ziel-App wird nur bei installierter App geöffnet. |
| 19 | DPD, GLS, Hermes und UPS jeweils über „Betriebssoftware pro Paketdienst zuordnen“ einer installierten App zuweisen | Jede Zuordnung wird getrennt auf diesem Gerät gespeichert und in der Übersicht beim richtigen Carrier angezeigt. |
| 20 | Carrier-Schnellauswahl öffnen und DPD, GLS, Hermes, UPS nacheinander auswählen | Der Auswahlknopf zeigt jeweils den gewählten Paketdienst; die Reihenfolge lautet DPD, GLS, Hermes, UPS. Die manuelle Auswahl wird bei allen drei lokalen Vorgangsarten gespeichert und an den Partner-App-Übergabeversuch weitergegeben. Es wird keine externe Buchung behauptet. |
| 21 | „Ohne Auswahl“ wählen und einen bekannten UPS-1Z-Testcode eingeben | Die manuelle Auswahl wird geleert; der bekannte UPS-Format-Hinweis darf als Hinweis erscheinen. Der Format-Hinweis bestätigt keine Existenz oder Buchung. |
| 22 | Nur DPD einer App zuordnen, danach einen GLS-Übergabeversuch starten | Die DPD-App darf nicht als Ersatz für GLS geöffnet werden. TRAKUNA meldet die fehlende GLS-Zuordnung; die lokale Cache-Sicherung bleibt erhalten. |
| 23 | Eine Carrier-Zuordnung entfernen und denselben Übergabeversuch wiederholen | Es wird keine fremde Carrier-App als Fallback geöffnet. Die Nummer bleibt lokal gesichert; die Meldung erklärt die fehlende Zuordnung. |


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
| DPD | DPD im Schnellauswahlmenü | TC26/DataWedge und manuelle Nummer getrennt testen; keine automatische DPD-Erkennung voraussetzen | Bestehendes DPD-Handgerät und Bluetooth-Drucker parallel beobachten; offizielle Übergabe-/API-Freigabe klären | Offen |
| GLS | GLS im Schnellauswahlmenü | TC26/DataWedge und manuelle Nummer getrennt testen; keine automatische GLS-Erkennung voraussetzen | Bestehendes GLS-Handgerät und Bluetooth-Drucker parallel beobachten; offizielle Übergabe-/API-Freigabe klären | Offen |
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


- Zebra-/DataWedge-Broadcast ohne Sendungsnummern-Extra oder mit einem falsch typisierten Extra prüfen: Der Scan wird ignoriert und TRAKUNA bleibt bedienbar.

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
5. Wenn ein verwalteter Gerätetest möglich ist, einen gleichartigen Broadcast aus einer nicht autorisierten Test-App senden lassen. Er darf nicht als vertrauenswürdiger Scan behandelt werden. Diesen Test nur auf einem Testgerät und mit einer eigens dafür erstellten Test-App durchführen.

Die App kann nicht selbst bestätigen, ob die geräteseitige Allowlist oder Signaturprüfung aktiviert ist. Die Konfiguration muss daher vom Geräteadministrator dokumentiert werden.

## Datensicherung und Wiederherstellung

1. In „Paketshop-Sendungen verwalten“ eine vollständige JSON-Datensicherung erstellen und in einem sicheren Speicherort ablegen.
2. Prüfen, dass die Datei `TRAKUNA-Sicherung.json` vorhanden und nicht leer ist.
3. Für eine Wiederherstellung die Sicherungsdatei auswählen. TRAKUNA liest höchstens 4 MiB ein und prüft UTF-8-Kodierung, Format, Version, Einträge und Höchstmenge. Beschädigte Textkodierung wird abgelehnt.
4. Vor dem Ersetzen des lokalen Bestands erscheint eine Bestätigungsabfrage. Ohne Bestätigung bleiben die aktuellen Daten unverändert.
5. Nach Wiederherstellung die angezeigte Anzahl mit dem erwarteten Datenbestand vergleichen.

**Wichtig:** Die Sicherungsdatei enthält personenbezogene Daten im Klartext. Nur auf einem geschützten Speicherort ablegen und nicht unverschlüsselt weitergeben.

6. Eine Sicherungsdatei mit verändertem Zähler, doppelter Vorgangs-ID oder doppelter aktiver Sendung testen. TRAKUNA muss sie ablehnen und den bisherigen Bestand unverändert lassen.
7. Beschädigten lokalen Datenspeicher simulieren (nur in einer Testinstallation). TRAKUNA muss einen Warnhinweis anzeigen und Änderungen sowie CSV-/JSON-Exporte blockieren; vorhandene Rohdaten dürfen nicht automatisch überschrieben werden.


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

60. TRAKUNA öffnen und im Vordergrund lassen; einen DataWedge-Testbarcode scannen. Die erkannte Nummer muss im Sendungsnummernfeld erscheinen.
61. TRAKUNA in den Hintergrund wechseln, ohne die App zu beenden; einen weiteren Barcode scannen. Der Hintergrundscan darf die Eingabemaske nicht verändern.
62. TRAKUNA wieder in den Vordergrund holen und erneut scannen. Der neue Scan muss wieder zur manuellen Prüfung übernommen werden.
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

## 10. Ergänzende Android-Instrumentierungstests (Version 1.53)

Die folgenden Android-Instrumentierungstestfälle liegen zusätzlich zum JVM-Testbestand im Repository. Sie wurden in CI erfolgreich kompiliert; ein Emulator- oder Gerätelauf ist noch offen:

- [ ] **Keystore-Verschlüsselung:** Testsendung speichern, SharedPreferences kontrollieren und bestätigen, dass Sendungsnummer, Empfänger und Notiz nicht im Klartext vorkommen.
- [ ] **Speicher-Wiederöffnung:** Neue `ShipmentStore`-Instanz erstellen und kontrollieren, dass der verschlüsselte Bestand vollständig wieder lesbar ist.
- [ ] **Fail-closed beim beschädigten Bestand:** Verschlüsselten Datensatz beschädigen; die App muss einen Speicherfehler anzeigen und weitere Schreibvorgänge ablehnen.
- [ ] **Übergabe-Cache-Verschlüsselung:** Sendungsnummer und Paketdienst speichern; Klartext darf nicht in SharedPreferences stehen und muss nach Neuinitialisierung lesbar sein.
- [ ] **Beschädigter Übergabe-Cache:** Verschlüsselten Cachewert beschädigen; die App muss einen Fehler melden und darf keinen leeren Cache vortäuschen.
- [ ] **Backup-Recovery:** Gültige Sicherung erstellen, lokalen verschlüsselten Bestand beschädigen und anschließend die gültige Sicherung wiederherstellen. Die wiederhergestellten Daten müssen nach erneuter Leseprüfung vorhanden sein.

