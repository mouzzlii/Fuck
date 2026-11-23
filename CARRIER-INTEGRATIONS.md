# TRAKUNA – Integrationsrecherche für die vier Start-Paketdienste

Stand: 23. November 2026  
Priorität: **DPD · GLS · Hermes · UPS**

Dieses Dokument trennt öffentlich dokumentierte Versand-APIs von der tatsächlich benötigten Paketshop-Integration. Eine API zum Erstellen von Versandlabels ist nicht automatisch eine Schnittstelle für Paketannahme, Kundenausgabe, Retourenannahme oder Fahrerübergabe in einem autorisierten Paketshop.

## 1. DPD

**Öffentlich dokumentiert:** DPD beschreibt Webservices für die Integration in eigene Versandlösungen. Genannt werden unter anderem Paketscheindruck, Paketverfolgung und Pickup-Paketshop-Finder. DPD verweist auf ein Entwicklerportal mit Dokumentationen und Testumgebung/Sandbox. Die DPD-Webservice-FAQ beschreibt eine SOAP-basierte Webservice-API; weitere Formate und Integrationswege werden im Entwicklerbereich behandelt.

Österreichischer Paketshop-Einstieg:
- [DPD Österreich – Pickup Paketshop werden](https://www.dpd.com/at/de/pickup-paketshops/paketshop-werden/): DPD beschreibt eine schnelle Einrichtung, stellt Technik und Werbematerial bereit und bietet individuelles Training. Das spricht dafür, die konkrete Integration über den österreichischen Partnerkontakt zu klären, statt eine deutsche Versand-API als Shop-Schnittstelle anzunehmen.
- [DPD Österreich – Kontakt und Paketshop-Partnerformular](https://www.dpd.com/at/de/support/kontakt/)

Allgemeine technische Versand-API-Einstiege (nicht als österreichische Paketshop-Freigabe zu verstehen):
- [DPD IT-Anbindung und Schnittstellen (Deutschland)](https://www.dpd.com/de/de/support/it-anbindung-und-dpd-schnittstellen/)
- [DPD Entwicklerportal](https://esolutions.dpd.com/entwickler/dpdwebservices/dokumentationen.aspx)
- [DPD Developer Integration](https://www.dpd.com/de/de/entwickler-integration-in-ihre-versandloesung/)

**Was noch nicht belegt ist:** Ob die deutschen Geschäftskunden-Versandservices für den konkreten österreichischen Pickup-Paketshop, dessen bestehendes TC26-Handgerät und die dortigen Vorgänge freigegeben sind. DPD Österreich beschreibt die Bereitstellung eigener PaketShop-Technik und individuelles Training; die genaue bestehende Software- und Integrationsschnittstelle muss der Standortbetreuer bestätigen. Die API-Dokumentation darf nicht als Erlaubnis verstanden werden, das vorhandene DPD-Programm zu ersetzen.

**Nächster technischer Schritt:** Vom DPD-Ansprechpartner bzw. Softwareanbieter die für diesen Standort zugelassene Schnittstelle, App-/Geräteintegration, Testzugang, Testsendungen und Rückmeldungen für Annahme, Ausgabe, Retoure und Fahrerübergabe anfordern. Bestehendes DPD-Handgerät und Bluetooth-Drucker unverändert lassen.

## 2. GLS

**Öffentlich dokumentiert:** GLS Österreich beschreibt YourGLS für kleinere Versandmengen und ShipIT für höhere Volumen. Für GLS-gehostetes ShipIT wird eine REST-/SOAP-API genannt; Labels können als PDF, PNG oder natives ZPL bereitgestellt werden. GLS weist darauf hin, dass für Schnittstellen und Plugins individuelle GLS-Zugangsdaten erforderlich sind.

Offizielle Einstiege:
- [GLS Österreich – Versandsysteme und Anbindungen](https://gls-group.com/AT/de/pakete-versenden/versendinfos/versandsysteme-anbindungen/)
- [GLS Österreich – PaketShop-Partner FAQ](https://assistantapi.gls-group.eu/AT/de/faq/paketshop-partner/)
- [GLS Österreich – Partnerausstattung und PaketShop-Abläufe](https://gls-group.com/AT/de/uber-us/unsere-partner/unsere-paketshop-partner/)

**Zusätzlicher Abgleich mit der offiziellen Partnerausstattung:** GLS Österreich beschreibt für neue PaketShop-Partner ein bereitgestelltes Smartphone, einen Bluetooth-Drucker und eine eigens entwickelte GLS-App. Der vom Betreiber beschriebene Zebra-TC26-/CD421-Bestand kann eine ältere oder abweichende Ausstattung sein. GLS muss bestätigen, ob diese Geräte und die installierte Version am konkreten Standort noch der freigegebene Betriebsstand sind.

**Was noch nicht belegt ist:** Ob ShipIT oder eine der veröffentlichten Versand-APIs die benötigten PaketShop-Prozesse des konkreten Standorts abdeckt. Die öffentlich beschriebene API bezieht sich auf Versandabwicklung; daraus folgt keine Berechtigung zur Übernahme der bestehenden GLS-Handgeräte-App oder zu PaketShop-Buchungen.

**Nächster technischer Schritt:** GLS nach der für diesen Standort freigegebenen PaketShop-Software-/Geräteschnittstelle und der Aktualität der vorhandenen TC26-/Drucker-Ausstattung fragen. Falls ShipIT für den Betreiber tatsächlich zulässig ist, API-Version, Zugangsdaten, Testumgebung, Label- und Statusformate sowie die vier Paketshop-Vorgänge schriftlich bestätigen lassen. GLS-TC26 und Bluetooth-Drucker nicht verändern.

## 3. Hermes

**Öffentlich dokumentiert:** Hermes Geschäftskunden beschreibt das Hermes Shipping Interface (HSI) als REST-API mit Authentifizierung und sicherer Datenübertragung. Genannt werden Versandlabel, Retouren, Abholung und Sendungsinformationen. Hermes nennt außerdem eine Offline-Vollintegration für sehr hohe Volumina. Die alte ProPS-SOAP-API wurde laut Hermes durch HSI ersetzt.

Österreichischer Paketshop-Einstieg:
- [Hermes Österreich – PaketShop-Partner](https://www.hermesworld.com/de/ueber-uns/hermes-gruppe/hermes-logistik-at/paketshop-partner/): beschreibt für österreichische PaketShops Annahme privater Pakete, Retourenannahme und Ausgabe von Paketen. Kontakt: Hermes Logistik GmbH, Wien, +43 (0)50 4376 37.
- [Hermes Österreich – PaketShops und Partner](https://www.hermesworld.com/at/unsere-dienstleistungen/distribution/paketversand/paketshops/)

Zusätzlicher technischer Gesprächseinstieg für Geschäftskunden-Versand (nicht als Freigabe für das österreichische MC3090BT-PaketShop-System zu verstehen):
- [Hermes IT-Anbindungen (Deutschland)](https://www.myhermes.de/geschaeftskunden/produkte-und-services/it-anbindungen/)
- [Hermes FAQ zur Umstellung auf HSI](https://www.myhermes.de/geschaeftskunden/faq-umstellung-hsi/)
- [Hermes Partnersysteme](https://www.myhermes.de/geschaeftskunden/produkte-und-services/partnersysteme/)

**Wichtige Einschränkung:** Die österreichische Hermes-Seite beschreibt die PaketShop-Abläufe, aber keine öffentlich dokumentierte Entwickler-API für das vorhandene MC3090BT-Handgerät. Die deutsche HSI-Dokumentation betrifft Geschäftskunden-/Versandintegrationen und ist keine bestätigte Lösung für den österreichischen PaketShop.

**Nächster technischer Schritt:** Hermes-Paketshop-Support bzw. zuständigen Vertragspartner gezielt nach einer freigegebenen Schnittstelle für das vorhandene MC3090BT-System und die konkrete Hermes-Paketshop-Software fragen. Dabei ausdrücklich nach Paketannahme, Kundenausgabe, Retourenannahme, Rücklauf/Fahrerübergabe, Quittung und Drucker fragen. Bestehenden Hermes-Ablauf nicht verändern.

## 4. UPS

**Österreichischer Paketshop-Ablauf – offiziell beschrieben:** UPS Österreich erklärt, dass Access-Point-Standorte eine Scanning-App für PC, Android oder Apple-Geräte erhalten. Die offizielle Google-Play-Auflistung der App **UPS Access Point** nennt den Paketnamen `com.ups.accesspoint`, richtet sich an registrierte und autorisierte Access-Point-Standorte und verlangt Android 9.0 oder höher. Wenn TRAKUNA und die UPS-App auf demselben Gerät laufen sollen, ist deshalb Android 9.0 oder höher erforderlich. Die Aufgaben umfassen die Annahme und das Scannen von Kundenpaketen zur Übergabe an UPS-Fahrer sowie die Annahme und das Scannen von UPS-Fahrersendungen zur Aufbewahrung bis zur Kundenabholung. UPS nennt Einweisung, Support und technische Unterstützung.

**Konkreter Android-App-Kandidat:** Im Google Play Store ist die offizielle App **UPS Access Point** von UPS gelistet (Paketname `com.ups.accesspoint`). Die Store-Beschreibung nennt vier Funktionen: Fahrerlieferung, Selbstabholung, Kundenabgabe und Bestandsverwaltung. Als Mindestanforderung wird Android 9.0 genannt; die Store-Seite zeigt ein Update vom 08.09.2026. Das ist ein konkreter Prüfhinweis, aber noch kein Beleg, dass genau diese App am Pilotstandort installiert oder für den Standort freigeschaltet ist. Vor Ort müssen App-Name, Version, Berechtigung und Gerät bestätigt werden.

Offizielle Einstiege:
- [UPS Österreich – Access Point Standort werden](https://www.ups.com/at/de/business-solutions/grow-your-business)
- [UPS Österreich – Access Point Überblick](https://www.ups.com/at/de/business-solutions/expand-your-online-business/ups-access-point)
- [UPS Access Point – offizielle Android-App im Google Play Store](https://play.google.com/store/apps/details?id=com.ups.accesspoint&hl=de_AT)
- [UPS Developer Portal – Start](https://developer.ups.com/get-started?loc=de_AT)
- [UPS API-Katalog](https://developer.ups.com/catalog?loc=de_AT)

**Was noch nicht belegt ist:** Welche UPS-Scanning-App, welches Gerät und welche Version der konkrete Standort verwendet; ob eine Partner-API oder ein freigegebener Datenaustausch für die bestehende App zur Verfügung steht. Das Developer-Portal betrifft allgemeine UPS APIs und ersetzt nicht die Access-Point-App oder eine Partnerfreigabe.

**Nächster technischer Schritt:** Am Standort prüfen, ob die App `UPS Access Point` (Paketname `com.ups.accesspoint`) bereits installiert ist; App-Version und Android-Gerät dokumentieren. Falls nicht, nicht eigenmächtig installieren, sondern UPS nach dem vorgesehenen offiziellen Download- und Freischaltweg fragen. Danach UPS Access Point Partnerbetreuung nach dem autorisierten Betriebsablauf, einer unterstützten Datenübergabe bzw. Partner-API und Testzugang fragen. Die offizielle UPS-App bleibt bis dahin das führende System; TRAKUNA darf keine UPS-Annahme oder Fahrerübergabe als bestätigt markieren, solange keine echte Rückmeldung vorliegt.

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

| Carrier | Öffentlich dokumentierte Technik in Österreich | Für den konkreten Paketshop bestätigt? | Blocker |
|---|---|---|---|
| DPD | DPD Österreich beschreibt PaketShop-Technik, Einrichtung und individuelles Training; allgemeine DPD-Webservices/SOAP-API sind separat dokumentiert | Nein | Bestehende TC26-App, erlaubter Datenaustausch und PaketShop-Freigabe durch DPD Österreich |
| GLS | GLS Österreich beschreibt YourGLS/ShipIT und Versand-Anbindungen | Nein | Bestehende TC26-App, PaketShop-Freigabe und Geräte-/Vorgangsschnittstelle |
| Hermes | Hermes Österreich beschreibt Annahme privater Pakete, Retouren und Kundenausgabe; deutsche HSI-REST-API ist ein separater Geschäftskunden-Versandweg | Nein | MC3090BT-Support, österreichische Shopsoftware und freigegebene Schnittstelle |
| UPS | UPS Österreich beschreibt eine Scanning-App; offizielle Android-App UPS Access Point (com.ups.accesspoint) nennt Fahrerlieferung, Selbstabholung, Kundenabgabe und Bestandsverwaltung, Android 9+ | Nein | Installierte App/Version und Hardware am Standort bestätigen; autorisierte Übergabe/API klären |

**Freigaberegel:** Kein Carrier wird als integriert oder produktionsbereit markiert, bevor die konkrete Schnittstelle für diesen Standort schriftlich freigegeben, technisch implementiert und mit echten autorisierten Testfällen erfolgreich abgenommen wurde.


## DPD Pickup – offizieller Partnerablauf als Referenz (Deutschland)

DPD Deutschland veröffentlicht ein Pickup-Partnerhandbuch mit Stand 01/2026. Es beschreibt deutlich mehr als das reine Scannen einer Sendungsnummer:

- Kundenabgabe: Paketdaten und Paketklasse kontrollieren, Kundenlabel bzw. mobilen QR-Code scannen, DPD-Label und Quittung ausgeben und das Paket sicher verwahren.
- Kundenausgabe: Identität des Abholers bzw. Vollmacht prüfen, Abholscan durchführen und Unterschrift auf dem Shop-Scanner dokumentieren.
- Zusteller-Anlieferung: Jedes Paket scannen, QR-Code am Partnergerät anzeigen, vom Zusteller scannen lassen und den Eingang durch Gegenscan bestätigen.
- Zusteller-Abholung: Pakete einzeln scannen, QR-Code anzeigen, Zusteller-Gegenscan abwarten und Übergabe quittieren.
- Lagerfristüberschreitungen: Rückführung über einen eigenen Aufgabenablauf dokumentieren.

**Wichtige Ländergrenze:** Das gefundene Handbuch ist ausdrücklich ein Handbuch von DPD Deutschland und nennt deutsche Vertrags-/Prozessdetails. Es ist daher kein Beleg, dass exakt dieselben Schritte, Maße, Haftungsregeln oder Scanner-Menüs für einen österreichischen Pickup-Standort gelten. Für TRAKUNA übernehmen wir diese Punkte als Prüf- und Anforderungsliste, nicht als verbindliche Österreich-Spezifikation. DPD Österreich muss den für diesen Shop gültigen Ablauf bestätigen.

Offizielle Referenzen:
- [DPD Pickup Partnerhandbuch (Deutschland, 01/2026, PDF)](https://www.dpd.com/wp-content/uploads/sites/227/2026/01/Pickup-Partner-Handbuch.pdf)
- [DPD Österreich – Pickup Paketshop Partner werden](https://www.dpd.com/at/de/pickup-paketshops/paketshop-werden/)
- [DPD Österreich – Kontakt und Partneranfragen](https://www.dpd.com/at/de/support/kontakt/?business_unit=dpd-at)
- [DPD Entwicklerportal / Webservices](https://esolutions.dpd.com/entwickler.aspx)

**Konsequenz für TRAKUNA:** Ein lokaler Datensatz „an Fahrer übergeben“ darf nicht als echte Übergabe gelten. Erst ein vom DPD-Partnergerät bzw. DPD-System bestätigter Gegenscan/Abschluss kann den Carrier-Vorgang bestätigen. Die DPD-Webservices, die öffentlich beschrieben sind, richten sich an Versandlösungen und belegen nicht automatisch die Berechtigung oder technische Eignung für die Pickup-Shop-Scannerabläufe.


## Hermes Österreich – bestätigte Partnerleistungen, offene Technik

Hermes Österreich beschreibt PaketShop-Aufgaben öffentlich als Annahme privater Pakete, Annahme von Retouren und Ausgabe zugestellter Pakete an Kunden. Hermes stellt nach eigener Beschreibung Unterstützung und Ausstattung für PaketShop-Partner bereit. Die öffentlich zugängliche Seite nennt aber keine technische API, keinen Intent-Vertrag und keine Freigabe zur Anbindung einer Fremd-App an den bestehenden MC3090BT.

- [Hermes Österreich – PaketShop-Partner](https://www.hermesworld.com/de/ueber-uns/hermes-gruppe/hermes-logistik-at/paketshop-partner/)
- [Hermes Österreich – Partnerübersicht und Ausstattung](https://www.myhermes.at/paketshop-partner/uebersicht/)

Für den Pilot ist Hermes deshalb weiterhin ein Pflicht-Blocker: bestehendes Gerät und Programm nicht verändern; Hermes muss die tatsächlich vorgesehene Hardware, den App-/Softwarezugang, die Freigabe für eine zusätzliche Lösung und eine mögliche Schnittstelle schriftlich bestätigen.
