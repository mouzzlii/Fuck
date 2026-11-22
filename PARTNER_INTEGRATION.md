# TRAKUNA – Schnittstellenvertrag für Partner-Betriebssoftware

Stand: TRAKUNA 1.18

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


## Zebra DataWedge – Barcode-Eingang

TRAKUNA 1.18 kann Scan-Ergebnisse von Zebra DataWedge über einen Android-Broadcast empfangen.

Konfiguration auf dem Zebra-Gerät:
1. DataWedge öffnen und ein Profil erstellen oder das für TRAKUNA zugewiesene Profil bearbeiten.
2. Barcode input aktivieren.
3. Intent output aktivieren und als Delivery **Broadcast Intent** auswählen.
4. Intent action exakt auf `de.trakuna.app.DATAWEDGE_SCAN` setzen; Kategorie leer lassen.
5. Sicherstellen, dass DataWedge den dekodierten Text als `com.symbol.datawedge.data_string` übergibt.
6. Das Profil der TRAKUNA-App (`de.trakuna.app`) zuordnen.

TRAKUNA liest den Scan ein und zeigt ihn zur Prüfung im Sendungsnummernfeld an. Eine Buchung oder Übergabe an eine Partner-App erfolgt nicht automatisch. So wird vermieden, dass ein zufälliger Scan sofort einen betrieblichen Vorgang auslöst.

**Sicherheitsgrenze:** Der dynamische Empfänger muss Broadcasts aus DataWedge annehmen. Ein Android-Broadcast kann von anderen Apps auf demselben Gerät nachgeahmt werden. Deshalb ist die eingehende Nummer nur ein unbestätigter Scanwert und muss vor jeder Buchung geprüft werden. Für besonders restriktive Unternehmensgeräte sollte der Geräteadministrator die installierten Apps und DataWedge-Profile verwalten.
