# TRAKUNA – Kurzanleitung für den kontrollierten Paketshop-Test

**Nur verwenden, wenn der Betreiber den Test ausdrücklich genehmigt hat und die freigegebene Version/Prüfsumme vorliegt.** Diese Anleitung ist für einen begrenzten Test gedacht, nicht für den normalen Paketbetrieb.

## Vor dem Start
1. Nur ein vom Betreiber freigegebenes Testgerät verwenden. Bevorzugt ein separates Test-Smartphone, nicht das Gerät für laufende Kundenabwicklung.
2. Vorhandene TRAKUNA-Daten nicht durch Deinstallation oder App-Daten-Löschen entfernen. Bei einer bereits installierten Version zuerst Betreiber/Projektleitung kontaktieren.
3. Nur fiktive oder ausdrücklich freigegebene Testsendungen und Testetiketten verwenden. Keine echten Kundennamen, Adressen, Telefonnummern oder Sendungsnummern eingeben.
4. Prüfen, dass in der App gut sichtbar **TESTMODUS** steht.
5. Keine Carrier-App öffnen, keine echte Buchung, Labelbestellung, Zahlung oder verbindliche Übergabe durchführen.

## Testablauf
- App starten und notieren, ob sie korrekt öffnet.
- Einen freigegebenen Testbarcode mit der Kamera scannen.
- Prüfen, ob die erkannte Nummer korrekt angezeigt wird. Bei Fehler: nicht raten, sondern als Fehler notieren.
- Falls nötig, eine fiktive Testnummer manuell eingeben.
- Paketdienst auswählen und **„Übergabe simulieren · TESTMODUS“** betätigen.
- Erwartetes Verhalten: TRAKUNA meldet die Simulation; keine externe Carrier-App öffnet sich; die Nummer wird nicht in die Zwischenablage kopiert.
- Prüfen, dass die Partner-App-Zuordnung gesperrt ist.
- App schließen und erneut öffnen; prüfen, ob der lokal gespeicherte Testvorgang weiterhin vorhanden ist.
- Falls eine Funktion nicht verständlich ist, abbrechen und die Beobachtung notieren.

## Testprotokoll teilen
1. In TRAKUNA **„Lokales Testprotokoll teilen“** auswählen.
2. Nur den redigierten Testbericht über einen vom Betreiber freigegebenen internen Kommunikationsweg teilen.
3. Der automatische Bericht enthält derzeit nur blockierte Übergabeversuche und technische Metadaten. Er ist noch kein vollständiges Protokoll aller Bedienhandlungen.
4. Zusätzliche Beobachtungen separat notieren: Zeitpunkt, Bildschirm/Funktion, erwartetes Verhalten, tatsächliches Verhalten und ob der Fehler wiederholbar war.
5. Keine Fotos von Kundenetiketten, Kundendaten, Zugangsdaten oder Passwörtern übermitteln.

## Manuelles Beobachtungsblatt

| Zeit | Testschritt | Erwartet | Tatsächlich | Ergebnis / Bemerkung |
|---|---|---|---|---|
| | App starten | App öffnet | | |
| | Kamera-Scan | Testcode korrekt erkannt | | |
| | Falscher/unklarer Code | Keine falsche Sendungsnummer übernehmen | | |
| | Übergabe simulieren | Externe App bleibt geschlossen | | |
| | Zwischenablage | Keine Sendungsnummer kopiert | | |
| | Testprotokoll teilen | Redigierter Bericht wird erzeugt | | |
| | Neustart | Lokaler Testbestand bleibt erhalten | | |

## Sofort abbrechen, wenn
- TESTMODUS nicht sichtbar ist;
- eine externe Carrier-App durch TRAKUNA geöffnet wird;
- eine echte Buchung, Zahlung oder Labelbestellung möglich erscheint;
- echte Kundendaten benötigt werden;
- Daten verschwinden, die App abstürzt oder ein Sicherheitsdialog unerwartet erscheint.

**Wichtig:** Der TRAKUNA-Testmodus sperrt nur die von TRAKUNA ausgelöste Partnerübergabe. Er sperrt keine separat gestarteten Carrier-Apps. Der Paketshop-Betreiber muss deshalb sicherstellen, dass Mitarbeiter während dieses Tests keine echten Vorgänge in anderen Apps ausführen.
