# TRAKUNA – Hardwareinventar des ersten Paketshops

Stand: 23. November 2026  
Quelle: Vor-Ort-Angaben des Paketshop-Mitarbeiters und Fotos/Ablesungen am Hermes-Handgerät.

## 1. Erfasster Bestand

| Paketdienst | Handgerät | Etikettendrucker | Verbindung | Angegebene Version |
|---|---|---|---|---|
| GLS | Eigenes Handgerät; Hersteller/Modell noch offen | Zebra CD421 (Bezeichnung vom Etikettendrucker laut Betreiber) | Bluetooth zwischen Handgerät und Drucker | V.7.0.1632 (genaue Zuordnung innerhalb des GLS-Systems noch offen) |
| DPD | Eigenes Handgerät; Hersteller/Modell noch offen | Zebra CD420 (Bezeichnung vom Etikettendrucker laut Betreiber) | Bluetooth zwischen Handgerät und Drucker | V.1.16.11 (genaue Zuordnung innerhalb des DPD-Systems noch offen) |
| Hermes | Symbol/Zebra MC3090BT, eigenständiges Handterminal | Eigener Hermes-Drucker; Modell noch offen | Eigenständiger Hermes-Geräteablauf; Druckeranschluss noch nicht verifiziert | V.2.1.1.0 (Hermes-Anwendung laut Vor-Ort-Angabe) |

### Hermes-Handgerät – sichtbare/abgelesene Informationen

- Modellaufschrift: MC3090BT
- Herstellungskennzeichnung: MFD: 18OCT06
- Angegebene Anwendungsversion: V.2.1.1.0
- Sichtbares Hermes-Menü: „P020 Kunde“
- Sichtbare Menüpunkte: Retoure, PrivatPaketService, Zustellung, Rücklauf, Hermes Karte

Die zusätzlich genannte Version V.7.0.1632 wurde GLS zugeordnet; V.1.16.11 wurde DPD zugeordnet. Es ist noch nicht geklärt, ob diese Nummern die Handgeräte-Anwendung, ein Gerätemodul oder eine andere Systemkomponente bezeichnen. Sie dürfen deshalb nicht als Betriebssystemversion oder Drucker-Firmware ausgegeben werden.

## 2. Bestätigte Betriebsweise

- GLS und DPD haben jeweils ein eigenes Handgerät mit eigenem Programm.
- Die Paketbearbeitung für GLS und DPD erfolgt direkt am jeweiligen Handgerät, nicht an einem separaten Paketshop-Computer.
- GLS- und DPD-Handgeräte sind jeweils per Bluetooth mit ihrem Etikettendrucker verbunden.
- Der Etikettendruck bei GLS und DPD erfolgt laut Vor-Ort-Angabe automatisch.
- Hermes verwendet ein eigenständiges MC3090BT-Handgerät mit eigenem Hermes-Menü und eigenem Drucker.

## 3. Was daraus für TRAKUNA folgt

TRAKUNA darf die drei bestehenden Carrier-Systeme zunächst nicht ersetzen oder verändern. Die erste Integrationsstufe ist eine **begleitende Scan- und Vorgangszentrale** mit folgenden Grenzen:

1. TRAKUNA erfasst und verwaltet lokale Testvorgänge unabhängig von den Carrier-Geräten.
2. Die vorhandenen GLS-/DPD-Handgeräte und deren Bluetooth-Druckverbindungen bleiben unverändert.
3. Der bestehende Hermes-Ablauf auf dem MC3090BT bleibt unverändert.
4. Ein Scan in TRAKUNA gilt nicht als Carrier-Buchung.
5. Ein Etikettendruck in der vorhandenen Carrier-App gilt nicht automatisch als in TRAKUNA bestätigter Druck.
6. Erst eine offiziell freigegebene API, ein SDK, eine dokumentierte Geräteintegration oder ein vereinbarter Datenaustausch kann eine echte Integration begründen.

## 4. Noch zu verifizierende Angaben

| Offener Punkt | Warum benötigt |
|---|---|
| Hersteller und Modell des GLS-Handgeräts | Geräte-/Scanner-Kompatibilität |
| Hersteller und Modell des DPD-Handgeräts | Geräte-/Scanner-Kompatibilität |
| Modell und Anschluss des Hermes-Druckers | Druck- und Schnittstellenprüfung |
| Drucker-Modellbezeichnung CD421/CD420 am Typenschild verifizieren | Richtige Treiber-/Druckersprache ermitteln |
| Genaue Zuordnung der GLS-Version V.7.0.1632 | Keine falsche OS-/Firmware-Annahme |
| Genaue Zuordnung der DPD-Version V.1.16.11 | Keine falsche OS-/Firmware-Annahme |
| Internetverbindung der Handgeräte (WLAN/SIM/andere) | Offline-/Netzwerkarchitektur |
| Paketdienst-/Paketshop-Anbieter und Produktname | Offizielle Schnittstellen und Freigabe |
| Offizielle API-/SDK-/Intent-Dokumentation und Testzugang | Sichere, echte Buchungsintegration |
| Schriftliche Zustimmung des Betreibers und Carrier-Freigabe | Zulässigkeit der Integration |

## 5. Verbindlicher Vor-Ort-Testplan

Nur mit Zustimmung des Betreibers und ausschließlich mit freigegebenen Testetiketten bzw. fiktiven Testdaten:

- [ ] Typenschilder aller Handgeräte und Drucker fotografieren; Seriennummern nicht öffentlich teilen.
- [ ] GLS-Handgerät: Modell, Betriebssystem-/App-Version und Scan-Ausgabe erfassen.
- [ ] DPD-Handgerät: Modell, Betriebssystem-/App-Version und Scan-Ausgabe erfassen.
- [ ] Hermes MC3090BT: genaue System-/Anwendungsversion nur ablesen, keine Einstellungen verändern.
- [ ] Bluetooth-Kopplung und automatischen Druck bei GLS/DPD nur beobachten, nicht neu koppeln oder zurücksetzen.
- [ ] Feststellen, ob die Geräte einen freigegebenen Export, eine dokumentierte Schnittstelle oder einen Hersteller-Supportweg besitzen.
- [ ] TRAKUNA parallel mit fiktiven Daten testen; niemals eine echte Carrier-Buchung durch bloßes Scannen behaupten.
- [ ] Fehlerfälle, doppelte Scans, Offline-Verhalten und Wiederanlauf dokumentieren.

## 6. Sicherheit und Datenschutz

Keine Kundennamen, Adressen, echten Sendungsnummern, Zugangsdaten, Passwörter, API-Schlüssel oder vollständigen Seriennummern in öffentliche Screenshots, GitHub-Issues oder Chat-Nachrichten aufnehmen. Testmaterial muss vom Betreiber bzw. Carrier freigegeben sein.

## 7. Status

**Hardware-Bestandsaufnahme: teilweise abgeschlossen.**  
**Carrier-Schnittstellen: nicht nachgewiesen.**  
**Produktive Integration: nicht freigegeben.**

Dieses Inventar dokumentiert ausschließlich die bislang vor Ort genannten und abgelesenen Informationen. Unbekannte technische Eigenschaften bleiben ausdrücklich offen.
