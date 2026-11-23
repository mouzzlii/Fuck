# TRAKUNA – Anfrage an die vier Paketdienst-Partner

Stand: 23. November 2026

Dieses Dokument ist eine vorbereitete Anfrage für die zuständigen Partnerbetreuungen. Es werden keine API-Zugangsdaten, Passwörter oder Kundendaten benötigt oder mitgesendet.

## 1. Gemeinsame Anfrage

**Betreff:** Technische Integrationsklärung für unseren PaketShop – DPD / GLS / Hermes / UPS

Guten Tag,

wir bereiten für unseren PaketShop eine zusätzliche Android-Anwendung namens TRAKUNA vor. Sie soll Paketannahme, Kundenausgabe, Retouren und die Übergabe an den Zusteller zentral dokumentieren.

Wichtig: Wir möchten Ihre bestehende, von Ihnen bereitgestellte PaketShop-Software, Handgeräte, Drucker und Abläufe nicht eigenmächtig ersetzen oder verändern. TRAKUNA soll zunächst nur ergänzend eingesetzt werden. Eine technische Integration erfolgt ausschließlich mit Ihrer ausdrücklichen Freigabe.

Bitte teilen Sie uns mit:

1. Welche PaketShop-Software und Geräte sind für unseren Standort offiziell vorgesehen?
2. Gibt es eine freigegebene API, ein SDK, einen Android-Intent, eine Scanner-Schnittstelle oder einen anderen offiziellen Datenaustausch?
3. Welche PaketShop-Vorgänge dürfen darüber abgewickelt werden: Paketannahme, Einlagerung, Kundenausgabe, Retourenannahme und Übergabe an den Zusteller?
4. Können Sendungsnummern oder Statusereignisse aus Ihrer Anwendung an eine zusätzliche Anwendung übergeben werden?
5. Gibt es eine Sandbox, Testkonten, Testetiketten und einen Ansprechpartner für die technische Abnahme?
6. Welche Voraussetzungen gelten für Datenschutz, Authentifizierung, Gerätefreigabe und Zertifizierung?
7. Ist eine zusätzliche lokale Dokumentations-App wie TRAKUNA zulässig, wenn Ihr bestehendes System weiterhin das führende Buchungssystem bleibt?

Wir möchten ausschließlich mit freigegebenen Testsendungen und nach Ihrer schriftlichen Zustimmung testen.

Vielen Dank und freundliche Grüße

[Name / PaketShop / Partnernummer – intern ergänzen]

---

## 2. DPD Österreich – standortspezifische Ergänzung

Vorhandene Ausstattung laut Betreiberangabe:
- Eigenes DPD-Handgerät, wahrscheinlich Zebra TC26 (Modellnummer noch zu bestätigen)
- Eigener Bluetooth-Etikettendrucker, Bezeichnung laut Betreiber: Zebra CD420
- Angegebene Softwareversion: V.1.16.11 (genaue Komponente noch offen)

Bitte bestätigen Sie insbesondere:
- Welche DPD-PaketShop-Anwendung und Version am Standort eingesetzt wird.
- Ob die vorhandene TC26-Anwendung eine offiziell unterstützte Scan-/Datenübergabe an eine zusätzliche Android-App zulässt.
- Ob DPD Österreich eine Partner-Schnittstelle für die vier PaketShop-Abläufe anbietet und wie diese freigeschaltet wird.
- Ob Testgeräte bzw. Testsendungen für eine Abnahme bereitgestellt werden können.

Offizielle Einstiege:
- [DPD Österreich – PaketShop-Partner werden](https://www.dpd.com/at/de/pickup-paketshops/paketshop-werden/)
- [DPD Österreich – Kontakt](https://www.dpd.com/at/de/support/kontakt/)

## 3. GLS Österreich – standortspezifische Ergänzung

Vorhandene Ausstattung laut Betreiberangabe:
- Eigenes GLS-Handgerät, wahrscheinlich Zebra TC26 (Modellnummer noch zu bestätigen)
- Eigener Bluetooth-Etikettendrucker, Bezeichnung laut Betreiber: Zebra CD421
- Angegebene Softwareversion: V.7.0.1632 (genaue Komponente noch offen)

Bitte bestätigen Sie insbesondere:
- Welche GLS-PaketShop-Anwendung und Version am Standort eingesetzt wird.
- Ob die vorhandene TC26-Anwendung eine offiziell unterstützte Scan-/Datenübergabe an eine zusätzliche Android-App zulässt.
- Ob GLS Österreich eine Partner-Schnittstelle für Paketannahme, Ausgabe, Retouren und Fahrerübergabe anbietet.
- Ob die bestehende Druckerkopplung unverändert bleiben muss und welche Testmöglichkeiten bestehen.

Offizielle Einstiege:
- [GLS Österreich – Versandsysteme und Anbindungen](https://gls-group.com/AT/de/pakete-versenden/versendinfos/versandsysteme-anbindungen/)
- [GLS Österreich – PaketShop-Partner FAQ](https://assistantapi.gls-group.eu/AT/de/faq/paketshop-partner/)

## 4. Hermes Österreich – standortspezifische Ergänzung

Bisherige Ausstattung laut Betreiberangabe (wird nächste Woche ersetzt):
- Eigenständiges Symbol/Zebra MC3090BT-Handterminal, laut Betreiber zu alt
- Herstellungskennzeichnung: 18. Oktober 2006
- Hermes-Anwendungsversion laut Betreiber: V.2.1.1.0
- Eigenständiger Hermes-Drucker; Modell und Anschluss noch offen
- Sichtbare Funktionen: Retoure, PrivatPaketService, Zustellung, Rücklauf und Hermes Karte

Angekündigter Nachfolger:
- Neues Hermes-Gerät wird laut Betreiber nächste Woche bereitgestellt.
- Hersteller, Modell, Betriebssystem, Hermes-App-Version, Scanner und Drucker sind noch unbekannt und werden nach Erhalt aufgenommen.

Bitte bestätigen Sie insbesondere:
- Welches genaue Nachfolgegerät und welche Hermes-PaketShop-Software für den Standort bereitgestellt werden.
- Welche Betriebssystem-/App-Version und welche Scanner-/Druckerkonfiguration vorgesehen sind.
- Ob das bisherige MC3090BT bis zur Umstellung weiter genutzt werden soll und wie ein unterbrechungsfreier Wechsel erfolgt.
- Ob eine offizielle Schnittstelle oder ein freigegebener Nachfolgeprozess existiert.
- Wie die Vorgänge Annahme, Ausgabe, Retoure und Rücklauf/Fahrerübergabe technisch bestätigt werden.
- Ob eine technische Modernisierung erforderlich ist und wie diese ohne Unterbrechung des laufenden Shopbetriebs erfolgt.

Hermes Österreich beschreibt öffentlich PaketShop-Annahme, Retourenannahme und Kundenausgabe. Für das bisherige MC3090BT-System und den angekündigten Nachfolger ist bislang keine Entwickler-Schnittstelle nachgewiesen.

Offizielle Einstiege:
- [Hermes Österreich – PaketShop-Partner](https://www.hermesworld.com/de/ueber-uns/hermes-gruppe/hermes-logistik-at/paketshop-partner/)
- Telefon laut offizieller Partnerseite: +43 (0)50 4376 37

## 5. UPS Österreich – standortspezifische Ergänzung

Die konkrete UPS-Hardware und die tatsächlich installierte UPS-Scanning-App des Standorts sind noch nicht erhoben. Im Google Play Store ist die offizielle Android-App **UPS Access Point** (Paketname `com.ups.accesspoint`) gelistet. Laut Store-Beschreibung deckt sie Fahrerlieferung, Selbstabholung, Kundenabgabe und Bestandsverwaltung ab und benötigt Android 9.0 oder höher. Ob diese App am Standort vorgesehen und freigeschaltet ist, muss UPS bestätigen.

UPS Österreich beschreibt für Access-Point-Standorte eine Scanning-App für PC, Android oder Apple-Geräte sowie das Scannen von Kundenabgaben und Fahrersendungen. Bitte teilen Sie uns mit:
- Ist die offizielle Android-App UPS Access Point (`com.ups.accesspoint`) die für unseren Standort vorgesehene App? Falls nein: Wie lautet der genaue App-Name und offizielle Bezugsweg?
- Welche UPS-Scanning-App und Version für unseren Standort bereitgestellt wurde (Google Play führt „UPS Access Point“, Paketname `com.ups.accesspoint`, Android 9.0+).
- Auf welchem Gerät diese App betrieben werden soll und ob Zebra-Geräte unterstützt werden.
- Ob die App einen offiziellen Datenexport, Android-Intent oder eine andere freigegebene Übergabe an TRAKUNA unterstützt.
- Wie die Übergabe an UPS-Fahrer und die Annahme von Fahrersendungen bestätigt werden.
- Wie Testsendungen und eine technische Abnahme organisiert werden.

Offizielle Einstiege:
- [UPS Österreich – Access Point Standort werden](https://www.ups.com/at/de/business-solutions/grow-your-business)
- [UPS Österreich – Access Point Überblick](https://www.ups.com/at/de/business-solutions/expand-your-online-business/ups-access-point)
- [UPS Access Point – offizielle Android-App im Google Play Store](https://play.google.com/store/apps/details?id=com.ups.accesspoint&hl=de_AT)
- [UPS Developer Portal](https://developer.ups.com/get-started?loc=de_AT)

---

## 6. Was wir nicht übermitteln

- Keine Passwörter, PINs, API-Secrets oder privaten Schlüssel per E-Mail oder Chat.
- Keine echten Empfänger- oder Kundendaten.
- Keine Fotos mit lesbaren Sendungsnummern oder Adressen, sofern der Anbieter sie nicht ausdrücklich über einen sicheren Kanal anfordert.
- Keine unfreigegebenen APKs oder Änderungen an bestehenden Carrier-Geräten.

## 7. Freigabeprotokoll

Je Carrier getrennt ausfüllen, sobald eine Antwort eintrifft:

| Paketdienst | Ansprechpartner | Freigegebener Integrationsweg | Testzugang/Testetiketten | Schriftliche Freigabe | Abnahmedatum |
|---|---|---|---|---|---|
| DPD | Offen | Offen | Offen | Offen | Offen |
| GLS | Offen | Offen | Offen | Offen | Offen |
| Hermes | Offen | Offen | Offen | Offen | Offen |
| UPS | Offen | Offen | Offen | Offen | Offen |

Bis zur bestätigten Freigabe bleibt das jeweilige Originalsystem das führende Buchungssystem. TRAKUNA dokumentiert nur den lokalen Testvorgang und darf keine erfolgreiche Carrier-Buchung behaupten.


## Zusätzliche DPD-Fragen (Pickup-Shop, Österreich)

DPD Deutschland stellt ein Pickup-Partnerhandbuch mit Abläufen für Kundenabgabe, Kundenausgabe, Zusteller-Anlieferung, Zusteller-Abholung und Rückführung nicht abgeholter Pakete bereit. Bitte bestätigen Sie für unseren österreichischen Standort:

- Welche dieser Abläufe gelten hier verbindlich?
- Wie erfolgt die Bestätigung des Haftungsübergangs bei Kundenabgabe und Kundenausgabe?
- Gibt es einen Gegenscan bzw. QR-Code-Ablauf zwischen Partnergerät und Fahrergerät?
- Welche Ereignisse und Status darf eine Drittsoftware lokal erfassen, und welche müssen ausschließlich in der DPD-Partner-App bestätigt werden?
- Gibt es für den österreichischen Pickup-Shop eine offiziell freigegebene API, Intent-Schnittstelle oder Geräteintegration?

Referenz (Deutschland, nicht ungeprüft auf Österreich übertragbar): [DPD Pickup Partnerhandbuch 01/2026](https://www.dpd.com/wp-content/uploads/sites/227/2026/01/Pickup-Partner-Handbuch.pdf).


## Zusätzliche GLS-Fragen

- Welche offiziellen GLS-Quittungen und Übergabebelege müssen bei Annahme und Abholung im österreichischen PaketShop erzeugt werden?
- Welche Daten bzw. Status dürfen wir in TRAKUNA ergänzend lokal dokumentieren, ohne eine GLS-Quittung oder GLS-Bestätigung zu ersetzen?
- Gibt es eine freigegebene Schnittstelle für das bestehende GLS-PaketShop-Programm bzw. den Zebra-Handheld?
- Ist eine parallele Nutzung von TRAKUNA zulässig, und welche technischen Anforderungen gelten für Smartphone, Bluetooth-Drucker und Scanner?
