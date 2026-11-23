# TRAKUNA – Praxis-Testplan (Version 1.45 · versionCode 46)

Version 1.45 enthält die direkte Carrier-Schnellauswahl für DPD, GLS, Hermes und UPS. Das neue Testpaket darf erst nach erfolgreichem CI-Lauf verteilt werden: [Android-Workflow](https://github.com/mouzzlii/Fuck/actions/workflows/android.yml). Version 1.44 / CI-Lauf 45227305052 ist ein älteres Debug-Testpaket ohne diese Änderung.

Dieser Testplan hilft, die App vor einem echten Paketshop-Einsatz kontrolliert zu prüfen. Bitte zuerst nur mit Testsendungen bzw. nicht kritischen Sendungsdaten arbeiten.

## 1. Voraussetzungen

- Android-Gerät mit Android 8.0 oder neuer.
- TRAKUNA-APK aus dem zugehörigen erfolgreichen GitHub-Actions-Lauf.
- Kameraberechtigung für den Scan-Test.
- Für die Partnerübergabe eine installierte Betriebssoftware. Eine automatische Übernahme ist nur möglich, wenn die Ziel-App die TRAKUNA-Intent-Extras oder eine dokumentierte API unterstützt.

## 2. Funktionstests

| Nr. | Test | Erwartetes Ergebnis |
|---|---|---|
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
| 15 | Einzelnen Vorgang löschen | Nach Bestätigung wird nur der gewählte Vorgang entfernt. |
| 16 | App schließen und neu öffnen | Lokal gespeicherte Vorgänge sind weiterhin vorhanden. |
| 17 | Zebra-Scanner mit DataWedge-Profil scannen | Sendungsnummer erscheint im Eingabefeld; es erfolgt keine automatische Buchung. Ungültige Inhalte werden abgelehnt. |
| 18 | Partner-App auswählen und übergeben | Cache wird vor dem Wechsel dauerhaft gespeichert; Clipboard-Ausfall darf keinen Absturz verursachen, Android 13+ markiert den Clipboard-Inhalt als sensibel; Ziel-App wird nur bei installierter App geöffnet. |
| 19 | Eine Betriebssoftware ausdrücklich auswählen und danach eine Paketdienst-Standardzuordnung konfigurieren (falls vorhanden) | Die bewusst gespeicherte Auswahl des Mitarbeiters hat Vorrang; eine Standardzuordnung darf sie nicht unbemerkt ersetzen. |


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
9. Den Testvorgang nach Abschluss des Tests wieder löschen.

**Fehlerprotokoll:** Wenn nach Schritt 3 nichts sichtbar passiert, notieren, ob der Dialog offen bleibt, ob eine Meldung direkt im Dialog erscheint und ob der Vorgang nach erneutem Öffnen der Liste vorhanden ist. Keine zweite Buchung durch wiederholtes Tippen erzwingen, bevor der Bestand geprüft wurde.


## 3. Sicherheits- und Fehlerprüfungen

- Vorgangshistorie nach der Erfassung öffnen: Der Eintrag „Vorgang angelegt“ muss mit dem korrekten Datum und der Uhrzeit erscheinen.
- Status eines Testvorgangs ändern und Historie erneut öffnen: Der Statuswechsel muss mit altem und neuem Status erscheinen.
- Empfänger oder Notiz ändern: Ein Eintrag „Empfänger oder Notiz bearbeitet“ muss erscheinen.
- App schließen und neu öffnen: Die Historie muss weiterhin zum richtigen Vorgang gehören.
- JSON-Sicherung exportieren und auf einer Testinstallation wiederherstellen: Die vorhandenen Historieneinträge müssen erhalten bleiben.
- Einen älteren Testdatensatz bzw. eine ältere Sicherung ohne Historienfeld laden: Der Datensatz muss weiterhin lesbar sein und einen Hinweis auf fehlende Historie anzeigen.
- Einen Vorgang löschen: Die zugehörige lokale Historie darf danach nicht mehr in der App angezeigt werden.
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
3. Für eine Wiederherstellung die Sicherungsdatei auswählen. TRAKUNA liest höchstens 2 MiB ein und prüft UTF-8-Kodierung, Format, Version, Einträge und Höchstmenge. Beschädigte Textkodierung wird abgelehnt.
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
