# TRAKUNA – kurzes Betriebshandbuch

**Stand:** Vereinfachungszweig PR #36 · noch nicht freigegeben

TRAKUNA ist derzeit eine lokale Android-Testversion. Vor einer Freigabe ausschließlich erfundene Testdaten oder ausdrücklich freigegebene Testsendungen verwenden. Keine echten Kundendaten in Screenshots, GitHub-Issues oder Chats veröffentlichen.

## 1. Die vier Kernvorgänge

Die Startseite bietet vier Einstiege:

1. **Paket für Kunden annehmen** – Sendung erfassen, die für einen Kunden im Shop eintrifft.
2. **Versand vom Kunden annehmen** – Sendung des Kunden lokal erfassen.
3. **Retoure annehmen** – Rücksendung lokal erfassen.
4. **Sendungen suchen / Abholung** – vorhandene Sendung finden und den nächsten zulässigen Status dokumentieren.

## 2. Sendung erfassen

1. Sendungsnummer mit der Smartphone-Kamera scannen oder manuell eingeben.
2. Erkannte Nummer prüfen. Bei mehreren oder unklaren Codes keinen Wert ungeprüft übernehmen.
3. Wenn der Paketdienst eindeutig erkannt wird, wird er vorgeschlagen. Sonst den Paketdienst einmal manuell eingeben.
4. Den passenden Vorgang antippen.
5. Die Rückmeldung kontrollieren. Bei einer bereits erfassten Sendungsnummer zeigt TRAKUNA den bestehenden Vorgang statt stillschweigend einen zweiten anzulegen.

## 3. Sendung suchen und Status ändern

1. **Sendungen suchen / Abholung** öffnen.
2. Nach Sendungsnummer, Empfänger oder Paketdienst suchen.
3. Den angezeigten Vorgang und aktuellen Status prüfen.
4. Nur die angezeigte nächste Aktion verwenden.
5. Die Statusänderung ausdrücklich bestätigen.

Eine Statusänderung in TRAKUNA dokumentiert den Vorgang lokal. Sie ist **keine** Buchung, Datenübertragung oder Bestätigung von DPD, GLS, Hermes, UPS oder einem anderen Paketdienst. Eine Übergabe erst lokal markieren, wenn sie im offiziellen Carrier-System tatsächlich erledigt und quittiert wurde.

## 4. Datensicherung und Datenschutz

- Die Daten werden lokal auf diesem Android-Gerät gespeichert; es gibt keine Cloud und keine Mehrgeräte-Synchronisierung.
- Sendungsdaten bleiben verschlüsselt gespeichert.
- Backup und Wiederherstellung sind in der Such-/Sendungsansicht erreichbar.
- Eine Sicherungsdatei enthält vertrauliche Sendungsdaten und muss an einem geschützten Ort aufbewahrt werden. Nicht öffentlich teilen.
- Vor einem Gerätewechsel oder Update eine Sicherung erstellen und sicher aufbewahren.
- Die App nicht deinstallieren und keine App-Daten löschen, solange keine benötigte Sicherung geprüft wurde.
- Bei der Wiederherstellung wird der vorhandene lokale Bestand ersetzt. Vorher den aktuellen Stand sichern und den Bestätigungstext genau lesen.

## 5. Was derzeit nicht unterstützt wird

- Keine Zebra-/DataWedge-Steuerung und kein externer Zebra-Broadcast-Scanempfang. Der vorgesehene Scanweg ist die Smartphone-Kamera; manuelle Eingabe bleibt der Rückfall.
- Keine automatische Partner-App-Auswahl oder -Übergabe.
- Keine bestätigte Carrier-Buchung oder Live-Schnittstelle.
- Kein gemeinsamer Etikettendrucker ist bisher implementiert oder auf realer Hardware getestet.
- Keine Cloud, Mehrgeräte-Synchronisierung, Mitarbeiterkonten oder Rollenverwaltung.

Das Ziel bleibt ein Android-Smartphone und ein gemeinsamer Etikettendrucker für alle unterstützten Paketdienste. Drucker, Protokolle und offizielle Carrier-Abläufe müssen vor einer Umsetzung und Abnahme real geprüft werden.

## 6. Test- und Freigabestatus

- Der Instrumentierungslauf für den UI-Stand vor Entfernung des externen DataWedge-Empfangs war erfolgreich: [Run 46067395466](https://github.com/mouzzlii/Fuck/actions/runs/46067395466).
- Die Entfernung des externen Scanpfads ist im aktuellen Code enthalten; der zugehörige Lauf ist noch abzuwarten.
- Ein erfolgreicher Emulatorlauf ersetzt keinen Test auf dem tatsächlichen Shop-Smartphone.
- Kamera-Scan, reale Sendungsabläufe, Drucker und Carrier-Portale sind noch nicht vor Ort abgenommen.
- PR #36 ist Draft und nicht gemergt. Der bestehende PR #25 bleibt getrennt.

## 7. Verbindliche Unterlagen

- [Minimalumfang und Abnahmekriterien](TRAKUNA-MINIMAL-KONZEPT.md)
- [Sicherheitsstatus und Produktionsfreigabe](SECURITY.md)
- [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md)
- [Hardwareinventar](HARDWARE-INVENTAR.md)
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md)
- [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md)
- [TRAKUNA Multi-Carrier Gateway](https://github.com/mouzzlii/multi-carrier-gateway)
