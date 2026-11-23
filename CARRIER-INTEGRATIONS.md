# TRAKUNA – Integrationsrecherche für die vier Start-Paketdienste

Stand: 23. November 2026  
Priorität: **DPD · GLS · Hermes · UPS**

Dieses Dokument trennt öffentlich dokumentierte Versand-APIs von der tatsächlich benötigten Paketshop-Integration. Eine API zum Erstellen von Versandlabels ist nicht automatisch eine Schnittstelle für Paketannahme, Kundenausgabe, Retourenannahme oder Fahrerübergabe in einem autorisierten Paketshop.

## 1. DPD

**Öffentlich dokumentiert:** DPD beschreibt Webservices für die Integration in eigene Versandlösungen. Genannt werden unter anderem Paketscheindruck, Paketverfolgung und Pickup-Paketshop-Finder. DPD verweist auf ein Entwicklerportal mit Dokumentationen und Testumgebung/Sandbox. Die DPD-Webservice-FAQ beschreibt eine SOAP-basierte Webservice-API; weitere Formate und Integrationswege werden im Entwicklerbereich behandelt.

Offizielle Einstiege:
- [DPD IT-Anbindung und Schnittstellen](https://www.dpd.com/de/de/support/it-anbindung-und-dpd-schnittstellen/)
- [DPD Entwicklerportal](https://esolutions.dpd.com/entwickler/dpdwebservices/dokumentationen.aspx)
- [DPD Developer Integration](https://www.dpd.com/de/de/entwickler-integration-in-ihre-versandloesung/)

**Was noch nicht belegt ist:** Ob diese Geschäftskunden-Versandservices für den konkreten österreichischen Pickup-Paketshop, dessen bestehendes TC26-Handgerät und die dortigen Vorgänge freigegeben sind. Die API-Dokumentation darf nicht als Erlaubnis verstanden werden, das vorhandene DPD-Programm zu ersetzen.

**Nächster technischer Schritt:** Vom DPD-Ansprechpartner bzw. Softwareanbieter die für diesen Standort zugelassene Schnittstelle, App-/Geräteintegration, Testzugang, Testsendungen und Rückmeldungen für Annahme, Ausgabe, Retoure und Fahrerübergabe anfordern. Bestehendes DPD-Handgerät und Bluetooth-Drucker unverändert lassen.

## 2. GLS

**Öffentlich dokumentiert:** GLS Österreich beschreibt YourGLS für kleinere Versandmengen und ShipIT für höhere Volumen. Für GLS-gehostetes ShipIT wird eine REST-/SOAP-API genannt; Labels können als PDF, PNG oder natives ZPL bereitgestellt werden. GLS weist darauf hin, dass für Schnittstellen und Plugins individuelle GLS-Zugangsdaten erforderlich sind.

Offizielle Einstiege:
- [GLS Österreich – Versandsysteme und Anbindungen](https://gls-group.com/AT/de/pakete-versenden/versendinfos/versandsysteme-anbindungen/)
- [GLS Österreich – PaketShop-Partner FAQ](https://assistantapi.gls-group.eu/AT/de/faq/paketshop-partner/)

**Was noch nicht belegt ist:** Ob ShipIT oder eine der veröffentlichten Versand-APIs die benötigten PaketShop-Prozesse des konkreten Standorts abdeckt. Die öffentlich beschriebene API bezieht sich auf Versandabwicklung; daraus folgt keine Berechtigung zur Übernahme der bestehenden GLS-Handgeräte-App oder zu Paketshop-Buchungen.

**Nächster technischer Schritt:** GLS nach der für diesen Standort freigegebenen PaketShop-Software-/Geräteschnittstelle fragen. Falls ShipIT für den Betreiber tatsächlich zulässig ist, API-Version, Zugangsdaten, Testumgebung, Label- und Statusformate sowie die vier Paketshop-Vorgänge schriftlich bestätigen lassen. GLS-TC26 und Bluetooth-Drucker nicht verändern.

## 3. Hermes

**Öffentlich dokumentiert:** Hermes Geschäftskunden beschreibt das Hermes Shipping Interface (HSI) als REST-API mit Authentifizierung und sicherer Datenübertragung. Genannt werden Versandlabel, Retouren, Abholung und Sendungsinformationen. Hermes nennt außerdem eine Offline-Vollintegration für sehr hohe Volumina. Die alte ProPS-SOAP-API wurde laut Hermes durch HSI ersetzt.

Offizielle Einstiege:
- [Hermes IT-Anbindungen](https://www.myhermes.de/geschaeftskunden/produkte-und-services/it-anbindungen/)
- [Hermes FAQ zur Umstellung auf HSI](https://www.myhermes.de/geschaeftskunden/faq-umstellung-hsi/)
- [Hermes Partnersysteme](https://www.myhermes.de/geschaeftskunden/produkte-und-services/partnersysteme/)

**Wichtige Einschränkung:** Die öffentlich beschriebenen HSI-Angebote sind Geschäftskunden-/Versandintegrationen. Es ist nicht nachgewiesen, dass HSI die Funktionen des österreichischen Hermes-Paketshops oder das vorhandene MC3090BT-Handgerät abdeckt. HSI ist daher ein möglicher technischer Gesprächseinstieg, aber noch keine bestätigte Lösung für den Standort.

**Nächster technischer Schritt:** Hermes-Paketshop-Support bzw. zuständigen Vertragspartner gezielt nach einer freigegebenen Schnittstelle für das vorhandene MC3090BT-System und die konkrete Hermes-Paketshop-Software fragen. Dabei ausdrücklich nach Paketannahme, Kundenausgabe, Retourenannahme, Rücklauf/Fahrerübergabe, Quittung und Drucker fragen. Bestehenden Hermes-Ablauf nicht verändern.

## 4. UPS

**Öffentlich dokumentiert:** UPS betreibt ein Entwicklerportal mit APIs und einem Authentifizierungsmodell auf OAuth-Basis. Für die direkte Integration muss eine Anwendung im UPS-Profil erstellt und mit einem Versenderkonto verknüpft werden. UPS beschreibt eine Customer Integration Environment (CIE) bzw. Sandbox zum Testen. UPS Access Point ist ein eigenes Retail-Partnerprogramm für berechtigte Paketabgabe und -abholung.

Offizielle Einstiege:
- [UPS Developer Portal – Start](https://developer.ups.com/get-started?loc=de_AT)
- [UPS API-Katalog](https://developer.ups.com/catalog?loc=de_AT)
- [UPS Österreich – Access Point](https://www.ups.com/at/de/business-solutions/expand-your-online-business/ups-access-point)

**Was noch nicht belegt ist:** Ob ein vorhandenes UPS-Konto und die öffentlich angebotenen APIs für die Paketshop-Rolle dieses Standorts zugelassen sind. Versand-/Tracking-APIs allein bilden nicht automatisch Access-Point-Annahme, Kundenausgabe oder Rückgabe an Fahrer ab.

**Nächster technischer Schritt:** UPS Access Point Partnerbetreuung nach der für diesen Standort autorisierten Betriebssoftware, Geräte-/Scannerintegration, Partner-API bzw. zertifizierter Drittsoftware und Testzugang fragen. Erst nach Bestätigung kann eine konkrete UPS-Integration entworfen werden.

## 5. Gemeinsame Integrationsarchitektur für TRAKUNA

Für jeden Carrier wird dieselbe Adapter-Grenze vorgesehen, aber die technischen Implementierungen bleiben getrennt:

1. **Scan-Eingang:** Kamera, manuelle Eingabe oder freigegebener Scanner-/DataWedge-Eingang.
2. **Lokale Vorgangserfassung:** Sendungsnummer, ausgewählter Carrier, Vorgangstyp und Zeitstempel. Die Carrier-Auswahl ist keine Buchungsbestätigung.
3. **Carrier-Adapter:** Nur offiziell dokumentierte und für den Standort freigegebene Schnittstelle. Zugangsdaten bleiben serverseitig bzw. in sicherem, betreiberkontrolliertem Speicher; keine Secrets in APK, GitHub oder Logs.
4. **Explizite Antwort:** Erfolg, Ablehnung, Offline/Timeout und Partner-Vorgangs-ID werden getrennt gespeichert. Ein Timeout darf nicht als erfolgreiche Buchung behandelt werden.
5. **Idempotenz:** Wiederholte Anfrage mit derselben eindeutigen Vorgangs-ID darf keine doppelte Buchung erzeugen.
6. **Druck:** Nur über den vom Carrier freigegebenen Druckpfad. Die vorhandenen Bluetooth-Drucker und Partner-Apps werden nicht ohne Freigabe umkonfiguriert.
7. **Abnahme:** Jeder Carrier wird einzeln mit Testkonto, Testetiketten und den vier Standortprozessen geprüft.

## 6. Pflichtfragen an jeden Paketdienst / Softwareanbieter

- Welche konkrete Software und Version ist für diesen Paketshop autorisiert?
- Gibt es eine offizielle Android-Intent-, SDK-, REST-/SOAP- oder Datei-Schnittstelle?
- Sind Drittsysteme neben dem vorhandenen Handgerät ausdrücklich erlaubt?
- Welche Vorgänge werden unterstützt: Paketannahme, Kundenausgabe, Retoure, Fahrerübergabe?
- Welche Antwort bestätigt die tatsächliche Buchung bzw. Statusänderung?
- Gibt es Testkonto, Sandbox und Testetiketten?
- Wie werden Label gedruckt und Druckfehler behandelt?
- Welche Authentifizierung, Zertifizierung, Datenschutz- und Vertragsbedingungen gelten?
- Wer ist technischer Ansprechpartner für den Standort?

## 7. Aktueller Status

| Carrier | Öffentlich dokumentierte Technik | Für den konkreten Paketshop bestätigt? | Blocker |
|---|---|---|---|
| DPD | Webservices, SOAP-API, Entwicklerportal/Sandbox | Nein | PaketShop-Freigabe und Geräteschnittstelle |
| GLS | YourGLS, ShipIT REST/SOAP, Versand-Plugins | Nein | PaketShop-Freigabe und Geräte-/Vorgangsschnittstelle |
| Hermes | HSI REST-API für Geschäftskunden; Vollintegration | Nein | HSI-Relevanz für österreichischen Hermes-Paketshop und MC3090BT klären |
| UPS | UPS APIs, OAuth, CIE/Sandbox; Access Point Partnerprogramm | Nein | Partnerkonto, autorisierte Shopsoftware und Access-Point-Schnittstelle |

**Freigaberegel:** Kein Carrier wird als integriert oder produktionsbereit markiert, bevor die konkrete Schnittstelle für diesen Standort schriftlich freigegeben, technisch implementiert und mit echten autorisierten Testfällen erfolgreich abgenommen wurde.
