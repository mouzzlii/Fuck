# TRAKUNA – Hardwareinventar des ersten Paketshops

Stand: 23. November 2026  
Quelle: Vor-Ort-Angaben des Paketshop-Mitarbeiters und Fotos/Ablesungen am Hermes-Handgerät.

## 1. Erfasster Bestand

| Paketdienst | Handgerät | Etikettendrucker | Verbindung | Angegebene Version |
|---|---|---|---|---|
| DPD | Zebra TC26 (wahrscheinlich; 4G-Anzeige und Geräteansicht, Typenschild noch zu bestätigen) | Zebra CD420 (Bezeichnung laut Betreiber; Typenschild noch zu prüfen) | Bluetooth zwischen Handgerät und Drucker | V.1.16.11 (genaue Zuordnung innerhalb des DPD-Systems offen) |
| GLS | Zebra TC26 (wahrscheinlich; 4G-Anzeige und Geräteansicht, Typenschild noch zu bestätigen) | Zebra CD421 (Bezeichnung laut Betreiber; Typenschild noch zu prüfen) | Bluetooth zwischen Handgerät und Drucker | V.7.0.1632 (genaue Zuordnung innerhalb des GLS-Systems offen) |
| Hermes | Symbol/Zebra MC3090BT, eigenständiges Handterminal | Eigener Hermes-Drucker; Modell noch offen | Eigenständiger Hermes-Geräteablauf; Druckeranschluss noch nicht verifiziert | V.2.1.1.0 (Hermes-Anwendung laut Vor-Ort-Angabe) |
| UPS | Handgerät/Arbeitsplatz noch nicht erhoben | Drucker noch nicht erhoben | Verbindung noch nicht erhoben | Anwendungsversion noch nicht erhoben |

### Hermes-Handgerät – sichtbare/abgelesene Informationen

- Modellaufschrift: MC3090BT
- Herstellungskennzeichnung: MFD: 18OCT06
- Angegebene Anwendungsversion: V.2.1.1.0
- Sichtbares Hermes-Menü: „P020 Kunde“
- Sichtbare Menüpunkte: Retoure, PrivatPaketService, Zustellung, Rücklauf, Hermes Karte

Die zusätzlich genannten Versionen V.7.0.1632 (GLS) und V.1.16.11 (DPD) sind noch nicht eindeutig einer konkreten Softwarekomponente zugeordnet. Sie dürfen nicht als Betriebssystemversion oder Drucker-Firmware ausgegeben werden.

### Wichtige Kompatibilitätsgrenze des Hermes-MC3090BT

Die offizielle Zebra-Kompatibilitätsdokumentation führt den MC3090 unter Windows CE 5.0 bzw. Windows Mobile 6.1. TRAKUNA ist dagegen eine Android-App mit Mindestanforderung Android 8.0 (API 26). **Die aktuelle TRAKUNA-APK kann daher nicht auf dem vorhandenen MC3090BT installiert werden.** Das Hermes-Gerät bleibt für den bestehenden Hermes-Ablauf erhalten; TRAKUNA muss für Hermes auf einem separaten, freigegebenen Android-Gerät laufen oder Hermes muss eine freigegebene moderne Geräte-/Softwarelösung bereitstellen. Ein direkter DataWedge-Scan vom MC3090BT in TRAKUNA ist mit diesem Modell nicht möglich.

Quelle: [Zebra – Enterprise Browser Gerätekompatibilität (MC3090: Windows CE 5.0 / Windows Mobile 6.1)](https://www.zebra.com/us/en/support-downloads/software/mobile-computer-software/enterprise-browser.html).

### GLS-/DPD-Handgeräte – Identifikationsstand

- Beide Handgeräte werden anhand der übermittelten Gerätebeschreibungen der Zebra-TC21/TC26-Baureihe zugeordnet.
- Wegen der sichtbaren 4G-Mobilfunkanzeige ist Zebra TC26 das wahrscheinlichste Modell. Die endgültige Bestätigung über Typenschild bzw. „Über das Gerät“ steht aus.
- Teilenummer, Android-Version, Zebra-MX-/DataWedge-Version und Scanner-Engine sind noch unbekannt.

## 1a. UPS – noch offene Bestandsaufnahme

UPS gehört neben DPD, GLS und Hermes zu den vier Start-Prioritäten. Für UPS liegen für diesen konkreten Standort noch keine verifizierten Angaben zu Handgerät, tatsächlich installierter UPS-Scanning-App, Drucker, Verbindung oder App-Version vor. UPS Österreich beschreibt öffentlich eine bereitgestellte Scanning-App für PC, Android oder Apple-Geräte sowie Scans bei Paketannahme und Fahrerübergabe. Ob und wie diese App am Standort eingesetzt wird, muss vor Ort erhoben werden. Bis dahin darf TRAKUNA nur lokale Testvorgänge kennzeichnen; es darf keine UPS-Buchung oder Etikettenerstellung behaupten.

## 2. Bestätigte Betriebsweise

- DPD und GLS haben jeweils ein eigenes Handgerät mit eigenem Programm.
- Die Paketbearbeitung für DPD und GLS erfolgt direkt am jeweiligen Handgerät, nicht an einem separaten Paketshop-Computer.
- Beide Handgeräte sind jeweils per Bluetooth mit ihrem eigenen Etikettendrucker verbunden.
- Der Etikettendruck bei DPD und GLS erfolgt laut Vor-Ort-Angabe automatisch.
- Hermes verwendet ein eigenständiges MC3090BT-Handgerät mit eigenem Hermes-Menü und eigenem Drucker.
- Für UPS ist die Betriebsweise noch nicht erhoben.

### Bereits vorhandener technischer Ansatz in TRAKUNA

Der aktuelle Android-Code enthält einen DataWedge-Broadcast-Empfänger:

- Intent action: `de.trakuna.app.DATAWEDGE_SCAN`
- Daten-Extra: `com.symbol.datawedge.data_string`
- Der Empfänger verarbeitet Scans nur, solange TRAKUNA im Vordergrund aktiv ist.
- Überlange oder nicht eindeutig interpretierbare Inhalte werden abgelehnt bzw. zur manuellen Prüfung gemeldet.
- Der empfangene Code wird in das Sendungsnummernfeld übernommen; es wird dadurch keine Carrier-Buchung ausgelöst.

Für einen TC26-Praxistest muss auf jedem Gerät ein eigenes DataWedge-Profil für TRAKUNA eingerichtet werden: Barcode Input aktivieren, Intent Output aktivieren, Action exakt `de.trakuna.app.DATAWEDGE_SCAN` setzen und als Delivery „Broadcast Intent“ wählen. Das Extra muss `com.symbol.datawedge.data_string` liefern. Profilkonfiguration und tatsächliche Scan-Zustellung sind auf den Shopgeräten noch nicht getestet.

## 3. Konsequenzen für TRAKUNA

TRAKUNA darf die bestehenden DPD-, GLS- und Hermes-Systeme zunächst nicht ersetzen oder verändern. Die erste Integrationsstufe ist eine begleitende Scan- und Vorgangszentrale:

1. TRAKUNA erfasst lokale Testvorgänge unabhängig von den Carrier-Geräten.
2. Vorhandene DPD-/GLS-Handgeräte und Bluetooth-Druckverbindungen bleiben unverändert.
3. Der bestehende Hermes-Ablauf auf dem MC3090BT bleibt unverändert.
4. Ein Scan in TRAKUNA gilt nicht als Carrier-Buchung.
5. Ein Etikettendruck in der vorhandenen Carrier-App gilt nicht automatisch als in TRAKUNA bestätigter Druck.
6. Erst eine offiziell freigegebene API, ein SDK, eine dokumentierte Geräteintegration oder ein vereinbarter Datenaustausch kann eine echte Integration begründen.

## 4. Noch zu verifizierende Angaben

| Offener Punkt | Warum benötigt |
|---|---|
| Typenschild und Teilenummer der DPD-/GLS-Handgeräte | Geräte-/Scanner-Kompatibilität |
| Exakte Modellbezeichnung der Drucker CD420/CD421 | Richtige Druckersprache/Treiber |
| Modell und Anschluss des Hermes-Druckers | Druck- und Schnittstellenprüfung |
| Genaue Zuordnung der GLS-Version V.7.0.1632 | Keine falsche OS-/Firmware-Annahme |
| Genaue Zuordnung der DPD-Version V.1.16.11 | Keine falsche OS-/Firmware-Annahme |
| Android-, MX- und DataWedge-Version der TC26-Geräte | Scanner-/Intent-Kompatibilität |
| UPS-Handgerät, Arbeitsplatz, Drucker und Software | UPS-Integrationsplanung |
| Netzwerkverbindung der jeweiligen Geräte | Offline-/Netzwerkarchitektur |
| Paketshop-/Versandsoftware und Hersteller | Offizielle Schnittstellen und Freigabe |
| API-/SDK-/Intent-Dokumentation und Testzugang | Sichere echte Buchungsintegration |
| Betreiberzustimmung und Carrier-Freigabe | Zulässigkeit der Integration |

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
