# TRAKUNA – Schnittstellenvertrag für Partner-Betriebssoftware

Stand: TRAKUNA 1.17

## Zweck

Dieses Dokument beschreibt die minimale technische Vereinbarung, damit eine Paketshop- oder Carrier-App eine Sendungsnummer aus TRAKUNA übernehmen kann. Es ist **keine Behauptung**, dass eine bestehende Post-, DHL-, DPD-, GLS- oder UPS-App diese Schnittstelle bereits unterstützt.

## Aktueller Übergabeversuch

TRAKUNA versucht, die installierte Ziel-App über deren Android-Launcher-Intent zu öffnen und hängt folgende Extras an:

| Schlüssel | Typ | Inhalt |
|---|---|---|
| `de.trakuna.app.extra.TRACKING_NUMBER` | String | Normalisierte Sendungsnummer |
| `de.trakuna.app.extra.CARRIER` | String | Erkannter/ausgewählter Paketdienst |
| `android.intent.extra.TEXT` | String | Sendungsnummer als generischer Fallback |

Die Übergabe startet die Ziel-App. Android bestätigt damit **nicht**, dass die Ziel-App die Extras gelesen, validiert oder einen Vorgang angelegt hat. TRAKUNA darf deshalb nur „Partner-App geöffnet“ melden, nicht „Sendung im Partnersystem gebucht“.

## Empfohlene Integration für Partner

Der Partner sollte einen ausdrücklich dokumentierten, nicht exportierten oder geschützt exportierten Android-Einstiegspunkt bzw. eine authentifizierte API bereitstellen.

Mindestanforderungen:
1. Eindeutige Paket-ID und unterstützte App-Versionen.
2. Dokumentierte Intent-Aktion oder API-Endpunkt und genaue Feldnamen.
3. Empfangsbestätigung mit Ergebniskennung; keine Erfolgsannahme allein aus `startActivity()`.
4. Idempotenzschlüssel, damit wiederholte Scans keine Doppelbuchung erzeugen.
5. Klare Fehlercodes für ungültige Nummer, unbekannten Carrier, fehlende Berechtigung und Offlinebetrieb.
6. Keine Zugangsdaten oder personenbezogenen Daten in Logs.
7. Transportverschlüsselung und Authentisierung bei Netzwerk-APIs.
8. Sandbox/Testkonto und Testetiketten für Abnahme.

## Abnahmetests

- Gültige Sendungsnummer wird exakt und ohne zusätzliche Zeichen übernommen.
- Ungültige Nummer wird mit verständlichem Fehler abgelehnt.
- Wiederholte Übergabe erzeugt keinen doppelten Vorgang.
- Ziel-App nicht installiert: TRAKUNA zeigt einen klaren Hinweis; lokaler Cache bleibt erhalten.
- Ziel-App installiert, aber ohne Schnittstelle: TRAKUNA behauptet keine automatische Übernahme.
- Offline-/Timeout-Fall: kein falscher Erfolgsstatus und keine verlorene lokale Erfassung.
- Rückmeldung der Partner-App wird auf korrekte Sendungsnummer und Vorgangs-ID geprüft.

## Für die konkrete Anbindung noch erforderlich

- Name des Betriebssoftware-Anbieters und Produktversion.
- Android-Paketname der App.
- Offizielle API-/Intent-Dokumentation oder Kontakt zum Anbieter.
- Testzugang und freigegebene Testsendungen.
- Beschreibung, welche Paketshop-Aktionen die Schnittstelle tatsächlich unterstützt (Annahme, Kundenausgabe, Retoure, Übergabe an Zusteller).

Ohne diese Angaben bleibt die Integration absichtlich ein sicherer Öffnungs-/Übergabeversuch mit lokalem Cache und Zwischenablage-Fallback. Es werden keine Carrier-Endpunkte, Paketbuchungen oder Zugangsdaten erfunden.
