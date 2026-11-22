# TRAKUNA – Praxis-Testplan (Version 1.22)

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
| 3 | Kamera-Scan erlauben und Barcode scannen | Erkannte Nummer wird eingetragen und der Übergabeablauf startet. |
| 4 | Kamerazugriff verweigern | Manuelle Eingabe bleibt verfügbar; Hinweis auf fehlende Kameraberechtigung erscheint. |
| 5 | Paket für Kunden annehmen | Vorgang erscheint als „Im Shop eingetroffen“. |
| 6 | Abholbereit markieren | Status wechselt auf „Abholbereit“. |
| 7 | An Kunden übergeben | Status wechselt auf „An Kunden übergeben“. |
| 8 | Sendung vom Kunden annehmen | Vorgang erscheint im passenden Annahme-Status. |
| 9 | Paketdienst manuell eintragen oder korrigieren | Beim Erfassen wird der manuell eingetragene Paketdienst gespeichert und hat Vorrang vor der automatischen Erkennung. Bei leerem Feld wird nur der bestmögliche Erkennungshinweis verwendet. |
| 10 | Formular nach erfolgreicher Erfassung | Empfänger, manuelle Paketdienst-Auswahl und Notiz sind nach erfolgreicher Erfassung leer; ein neuer Vorgang übernimmt keine alten Angaben. |
| 11 | Retoure annehmen und weitergeben | Retoure kann bis zur Übergabe an den Paketdienst fortgeschrieben werden. |
| 12 | Empfänger und Notiz bearbeiten | Geänderte Angaben bleiben nach erneutem Öffnen erhalten. |
| 13 | Suche verwenden | Treffer werden anhand Sendungsnummer, Empfänger, Paketdienst oder Status gefiltert. |
| 14 | CSV-Datei exportieren | Android-Dateidialog erscheint; die gespeicherte Datei enthält die Sendungsliste. |
| 15 | Einzelnen Vorgang löschen | Nach Bestätigung wird nur der gewählte Vorgang entfernt. |
| 16 | App schließen und neu öffnen | Lokal gespeicherte Vorgänge sind weiterhin vorhanden. |
| 17 | Zebra-Scanner mit DataWedge-Profil scannen | Sendungsnummer erscheint im Eingabefeld; es erfolgt keine automatische Buchung. Ungültige Inhalte werden abgelehnt. |
| 18 | Partner-App auswählen und übergeben | Cache wird vor dem Wechsel dauerhaft gespeichert; Clipboard-Ausfall darf keinen Absturz verursachen, Android 13+ markiert den Clipboard-Inhalt als sensibel; Ziel-App wird nur bei installierter App geöffnet. |

## 3. Sicherheits- und Fehlerprüfungen

- Ungültige oder zu kurze Sendungsnummern dürfen nicht als neuer Vorgang gespeichert werden.
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
3. Für eine Wiederherstellung die Sicherungsdatei auswählen. TRAKUNA prüft Format, Version, Einträge und Höchstmenge.
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


## Zusätzliche Tests für Version 1.21 – verschlüsselter Übergabe-Cache

27. **Übergabe speichern:** Eine Testsendungsnummer an die Partner-App übergeben; TRAKUNA muss sie vor dem App-Wechsel dauerhaft speichern.
28. **Cache nach Neustart:** App schließen und erneut öffnen; die zuletzt übergebene Sendungsnummer muss aus dem verschlüsselten Cache lesbar sein.
29. **Migration:** Auf einem isolierten Testgerät einen vorhandenen Klartext-Cache aktualisieren; Nummer muss erhalten bleiben und die alten Klartextfelder müssen nach erfolgreichem Commit entfernt sein.
30. **Beschädigter Cache:** Verschlüsselten Testwert beschädigen; TRAKUNA darf keinen falschen Wert ausgeben und muss einen sicheren Fehlerzustand beibehalten.
31. **Partner-Auswahl:** Wechseln und Entfernen der gespeicherten Partner-App darf den verschlüsselten Sendungsnummern-Cache nicht löschen.
32. **Cache-Lesefehler:** Auf einem isolierten Testgerät den verschlüsselten Cache beschädigen oder den Keystore-Schlüssel entfernen. Ein Lesezugriff muss einen Fehler melden und darf nicht so tun, als wäre lediglich noch keine Sendungsnummer gespeichert.
33. **Cache-Schreibfehler:** Auf einem isolierten Testgerät einen Speicherausfall simulieren. Die Partner-App darf nicht gestartet werden, wenn das vorherige sichere Speichern nicht bestätigt wurde; die Fehlermeldung muss den Fehler verständlich benennen.
