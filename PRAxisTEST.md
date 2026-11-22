# TRAKUNA – Praxis-Testplan (Version 1.17)

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
| 9 | Paketdienst manuell eintragen oder korrigieren | Beim Erfassen wird der manuell eingetragene Paketdienst gespeichert und hat Vorrang vor der automatischen Erkennung. Bei leerem Feld wird nur der bestmögliche Erkennungshinweis verwendet. |\n| 10 | Formular nach erfolgreicher Erfassung | Empfänger, manuelle Paketdienst-Auswahl und Notiz sind nach erfolgreicher Erfassung leer; ein neuer Vorgang übernimmt keine alten Angaben. |\n| 11 | Retoure annehmen und weitergeben | Retoure kann bis zur Übergabe an den Paketdienst fortgeschrieben werden. |
| 12 | Empfänger und Notiz bearbeiten | Geänderte Angaben bleiben nach erneutem Öffnen erhalten. |
| 13 | Suche verwenden | Treffer werden anhand Sendungsnummer, Empfänger, Paketdienst oder Status gefiltert. |
| 14 | CSV-Datei exportieren | Android-Dateidialog erscheint; die gespeicherte Datei enthält die Sendungsliste. |
| 15 | Einzelnen Vorgang löschen | Nach Bestätigung wird nur der gewählte Vorgang entfernt. |
| 16 | App schließen und neu öffnen | Lokal gespeicherte Vorgänge sind weiterhin vorhanden. |
| 17 | Partner-App auswählen und übergeben | Cache wird vor dem Wechsel dauerhaft gespeichert; Clipboard-Ausfall darf keinen Absturz verursachen, Android 13+ markiert den Clipboard-Inhalt als sensibel; Ziel-App wird nur bei installierter App geöffnet. |

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

Ein grüner GitHub-Build bestätigt nur, dass Tests und APK-Erstellung im CI-Lauf erfolgreich waren. Er bestätigt nicht die Kompatibilität mit einer fremden Betriebssoftware, die Erkennung aller Paketetiketten oder die Eignung für den produktiven Paketshop-Betrieb.

## 6. Noch offene Voraussetzungen für Produktivbetrieb

- Name und dokumentierte Schnittstelle der tatsächlich verwendeten Betriebssoftware
- Reale Paketlabeltests je eingesetztem Carrier
- Datenschutz- und Sicherheitsprüfung
- Zentrale Datenhaltung, Benutzerkonten, Rollen und Audit-Protokoll, falls mehrere Geräte oder Mitarbeiter zusammenarbeiten
- Backup- und Wiederherstellungsverfahren
- Praxistest und Freigabe durch den Paketshop-Betreiber


## Datensicherung und Wiederherstellung
1. In „Paketshop-Sendungen verwalten“ eine vollständige JSON-Datensicherung erstellen und in einem sicheren Speicherort ablegen.
2. Prüfen, dass die Datei `TRAKUNA-Sicherung.json` vorhanden und nicht leer ist.
3. Für eine Wiederherstellung die Sicherungsdatei auswählen. TRAKUNA prüft Format, Version, Einträge und Höchstmenge.
4. Vor dem Ersetzen des lokalen Bestands erscheint eine Bestätigungsabfrage. Ohne Bestätigung bleiben die aktuellen Daten unverändert.
5. Nach Wiederherstellung die angezeigte Anzahl mit dem erwarteten Datenbestand vergleichen.

**Wichtig:** Die Sicherungsdatei enthält personenbezogene Daten im Klartext. Nur auf einem geschützten Speicherort ablegen und nicht unverschlüsselt weitergeben.

6. Eine Sicherungsdatei mit verändertem Zähler, doppelter Vorgangs-ID oder doppelter aktiver Sendung testen. TRAKUNA muss sie ablehnen und den bisherigen Bestand unverändert lassen.

7. Beschädigten lokalen Datenspeicher simulieren (nur in einer Testinstallation). TRAKUNA muss einen Warnhinweis anzeigen und Änderungen sowie CSV-/JSON-Exporte blockieren; vorhandene Rohdaten dürfen nicht automatisch überschrieben werden.
