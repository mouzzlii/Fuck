# Carrier-Registry – gemeinsame Paketplattform

Stand: 26.11.2026

Diese Registry ist die zentrale Anbieterübersicht für TRAKUNA undhttps://auth.dc.gls-group.eu/auth/realms/gls/protocol/openid-connect/auth) | Priorität 1 für Shopbetrieb; offizielle Standort-Schnittstelle und Testzugang nicht bestätigt |
| Hermes | Österreichischer Paketshop; Geschäftskundenregistrierung ist länderabhängig | [Hermes Business Deutschland](https://business.myhermes.de/registrierung/) · [Hermes Österreich PaketShop](https://www.hermesworld.com/at/unsere-dienstleistungen/distribution/paketversand/paketshops/) | Priorität 3 für Shopbetrieb; deutsche HSI-Versand-API ist keine bestätigte österreichische Paketshop-API |
| UPS | Österreichischer Versand und Access Point | [UPS Österreich Login](https://www.ups.com/at/de) · [UPS Geschäftskonto](https://www.ups.com/) | Priorität 4 für Shopbetrieb; Access-Point-Partnerfreigabe und erlaubte App-Übergabe offen |
| GLS | Österreichischer Paketshop und Geschäftskundenversand | [GLS One Österreich](https://www.gls-one.at/) · [YourGLS Anmeldung](https://auth.dc.gls-group.eu/auth/realms/gls/protocol/openid-connect/auth) | Priorität 2 für Shopbetrieb; OIDC-Loginaufruf mit Authorization Code + PKCE erkannt, aber kein API-Zugang oder Shopprozess dadurch belegt |
| Österreichische Post | Österreichischer Versand und Post-Services | [Post Registrierung](https://www.post.at/sh/s/registrierung) | Anbieterprofil vorgemerkt; Geschäftskunden-/Paketshop-Schnittstelle noch zu prüfen |
| DHL | DHL Express / MyDHL+ und internationale Sendungen | [DHL Österreich Login-Portale](https://www.dhl.com/at-de/home/login.html) | Noch einzurichten; Produkt-/Länder- und API-Berechtigung offen |
| FedEx | Internationaler Versand | [FedEx Österreich Geschäftskonto](https://www.fedex.com/en-at/open-account.html) | Noch einzurichten; API-Zugang, Testumgebung und Tarife offen |
| LogoiX | Grenzüberschreitender Versand Österreich–Deutschland | [LogoiX](https://www.logoix.com/) | Noch einzurichten; konkrete Schnittstelle/Automatisierung offen |

## Prioritäten

**Shopbetrieb – erste Ausbaustufe:** DPD, GLS, Hermes, UPS. Diese vier Anbieter sind die bestehenden TRAKUNA-Startprioritäten. Die lokale Erfassung und Carrier-Auswahl sind noch keine offizielle Buchung.

**Versand-/Gateway-Erweiterung:** Österreichische Post, DHL, FedEx und LogoiX. Diese werden als weitere Adapterprofile geführt, sobald die tatsächlichen Versandabläufe, Länder, Vertragsbedingungen und technischen Schnittstellen geprüft wurden.

## GLS YourGLS Login – technische Einordnung

Der übermittelte GLS-Link zeigt einen OpenID Connect Authorization Code Flow mit PKCE (S256), lokalisierten Login-Einstellungen und einer Rückleitung zu `gls-group.eu/authenticate`.

Sicherheits- und Integrationsregeln:
- `state` und `code_challenge` sind sitzungsbezogene Parameter und dürfen nicht fest in die App oder in Quellcode übernommen werden.
- Der nach erfolgreicher Anmeldung zurückgelieferte Authorization Code ist kurzlebig und darf nicht protokolliert oder weitergegeben werden.
- Ein nativer Client muss PKCE korrekt umsetzen; ein Client-Secret gehört nicht in eine Android-App.
- Der Login-Link allein belegt weder einen dokumentierten API-Endpunkt noch die Erlaubnis, YourGLS zu automatisieren oder Paketshop-Buchungen auszuführen.
- Für echte Anbindung sind offizielle GLS-API-Dokumentation, Partnerberechtigung, Testzugang und bestätigte Geschäftsprozesse erforderlich.
- Keine Zugangsdaten oder Session-Cookies automatisiert aus dem Browser auslesen.

## Einheitlicher Adaptervertrag (Ziel)

Jeder Carrier-Adapter soll dieselben fachlichen Ergebniszustände liefern, ohne die anbieterspezifischen Details zu verschleiern:

- `NOT_CONFIGURED`: Anbieterprofil vorhanden, aber noch nicht eingerichtet.
- `MANUAL_ONLY`: manuelle Erfassung / externes Portal, keine API.
- `PENDING_AUTH`: gültige Authentifizierung erforderlich.
- `SUBMITTED`: Anfrage über freigegebene Schnittstelle übermittelt, Ergebnis noch offen.
- `CONFIRMED`: vom Carrier explizit bestätigte Transaktion mit externer Referenz.
- `REJECTED`: Carrier hat die Anfrage ausdrücklich abgelehnt.
- `UNKNOWN`: Timeout oder unklare Antwort; niemals als Erfolg behandeln.

Zusätzliche Pflichtfelder: Carrier-ID, Land/Region, Vorgangstyp, externe Referenz (falls bestätigt), Idempotenzschlüssel, Zeitstempel, Fehlercode und Quelle der Statusbestätigung.

## Noch zu erheben

- Tatsächlich eröffnete Konten und Vertragsarten je Anbieter (ohne Passwörter oder Tokens).
- Österreichische Länder-/Paketshop-Berechtigung, Volumengrenzen und Tarifmodelle.
- Abmessungen, Gewicht, Abholung, Retouren, Versicherung und Zielländer.
- Offizielle API-/SDK-/Intent-Dokumentation, Sandbox und Testsendungen.
- Druckformate und unterstützte Hardware je Carrier.
