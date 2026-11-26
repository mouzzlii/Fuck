# TRAKUNA – Schnittstellenvertrag für Partner-Betriebssoftware

Stand: App-Version 1.54 (versionCode 55)

## Zweck
Dieses Dokument beschreibt die technische Vereinbarung für eine autorisierte Übergabe zwischen TRAKUNA und der jeweiligen Paketshop-Betriebssoftware.

## Verbindliche Startpriorität der Paketdienste

Für den ersten Start sind **DPD, GLS, Hermes und UPS** die vier priorisierten Paketdienste. Die Reihenfolge in der TRAKUNA-Auswahl ist DPD, GLS, Hermes, UPS. Alle vier müssen im Pilotablauf separat geprüft werden; eine manuelle Carrier-Auswahl ist zunächst nur eine Kennzeichnung des lokalen Vorgangs und keine Buchung im Partnersystem.

- **DPD und GLS:** offizielle Portal-/Labelwege und Betreiberberechtigungen separat prüfen. Bestehende Zebra-Handgeräte und Druckerkopplungen bleiben unangetastet; DataWedge ist keine Voraussetzung des neuen Smartphone-Ablaufs.
- **Hermes:** Das bisherige MC3090BT ist laut Betreiber zu alt und soll in der kommenden Woche ersetzt werden. Das bestehende System bleibt bis zur freigegebenen Umstellung unangetastet; das Nachfolgegerät wird nach Ankunft inventarisiert. Offizielle Hermes-Schnittstelle/Freigabe bleibt Pflicht.
- **UPS:** Sendungsnummern im bekannten `1Z`-Format können als Format-Hinweis erkannt werden. Das bestätigt weder Gültigkeit noch UPS-Paketshop-Berechtigung. Hardware, Shopsoftware und offizieller Integrationsweg sind noch zu ermitteln.


Die öffentlich dokumentierten Integrationswege für DPD, GLS, Hermes und UPS sind in [CARRIER-INTEGRATIONS.md](CARRIER-INTEGRATIONS.md) zusammengefasst. Diese Versand-APIs sind nicht automatisch Paketshop-Schnittstellen.

Dieses Dokument beschreibt die minimale technische Vereinbarung, damit eine Paketshop- oder Carrier-App eine Sendungsnummer aus TRAKUNA übernehmen kann. Es ist **keine Behauptung**, dass eine bestehende Post-, DHL-, DPD-, GLS- oder UPS-App diese Schnittstelle bereits unterstützt.

## Carrier-spezifische App-Zuordnung ab Version 1.47

DPD, GLS, Hermes und UPS können getrennt jeweils einer auf dem Android-Gerät installierten App zugeordnet werden. Paketnamen werden nicht fest codiert, da sie je nach Land und Betreiber variieren können. Sobald eine Einzelzuordnung existiert, wird für einen anderen Carrier ohne eigene Zuordnung kein gemeinsamer oder fremder App-Eintrag als Fallback verwendet. Eine Zuordnung ist nur ein lokaler Startparameter; sie ist kein Nachweis einer unterstützten Intent-Schnittstelle oder einer Buchung.

## Aktueller Übergabeversuch von TRAKUNA

TRAKUNA versucht, die installierte Ziel-App über deren Android-Launcher-Intent zu öffnen und hängt folgende Extras an:

| Schlüssel | Typ | Inhalt |
|---|---|---|
| `de.trakuna.app.extra.TRACKING_NUMBER` | String | Normalisierte Sendungsnummer |
| `de.trakuna.app.extra.CARRIER` | String | Erkannter/ausgewählter Paketdienst |
| `android.intent.extra.TEXT` | String | Sendungsnummer als generischer Fallback |

Die Übergabe startet die Ziel-App. Android bestätigt damit **nicht**, dass die Ziel-App die Extras gelesen, validiert oder einen Vorgang angelegt hat. TRAKUNA darf deshalb nur „Partner-App geöffnet“ melden, nicht „Sendung im Partnersystem gebucht“.

## Beispiel für einen Partner-App-Empfänger (Kotlin)

Das folgende Beispiel zeigt nur, wie eine Partner-App die Extras aus ihrem eigenen Launcher-Activity-Einstieg lesen kann. Es bucht **keine** Sendung und ersetzt weder die Validierung noch die Bestätigung durch den Paketdienst.

```kotlin
private fun handleTrakunaHandoff(intent: Intent?) {
    if (intent == null) return

    val trackingNumber = intent.getStringExtra(
        "de.trakuna.app.extra.TRACKING_NUMBER"
    ) ?: intent.getStringExtra(Intent.EXTRA_TEXT)

    val carrier = intent.getStringExtra(
        "de.trakuna.app.extra.CARRIER"
    ).orEmpty()

    if (trackingNumber.isNullOrBlank()) return

    // Partner-App: Nummer lokal validieren, dann in der eigenen
    // Oberfläche zur bewussten Prüfung durch den Mitarbeiter anzeigen.
    // Erst nach eigener erfolgreicher Buchung darf die Partner-App
    // einen Vorgang als gebucht kennzeichnen.
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleTrakunaHandoff(intent)
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleTrakunaHandoff(intent)
}
```

Die Partner-App muss außerdem ihre Activity-Konfiguration und Android-Versionen testen. Sie darf nicht allein aus dem Empfang eines Intents eine erfolgreiche Paketdienst-Buchung ableiten.

## Empfohlene Integration für Partner

Der Partner sollte einen ausdrücklich dokumentierten, möglichst eng geschützten Android-Einstiegspunkt oder eine authentifizierte API bereitstellen.

Mindestanforderungen:
1. Eindeutige Paket-ID und unterstützte App-Versionen.
2. Dokumentierte Intent-Aktion oder API-Endpunkt und genaue Feldnamen.
3. Empfangsbestätigung mit Ergebniskennung; keine Erfolgsannahme allein aus `startActivity()`.
4. Idempotenzschlüssel, damit wiederholte Scans keine Doppelbuchung erzeugen.
5. Klare Fehlercodes für ungültige Nummer, unbekannten Carrier, fehlende Berechtigung und Offlinebetrieb.
6. Keine Zugangsdaten oder personenbezogenen Daten in Logs.
7. Transportverschlüsselung und Authentisierung bei Netzwerk-APIs.
8. Sandbox/Testkonto und Testetiketten für Abnahme.
9. Dokumentierte Rückmeldung mit Partner-Vorgangs-ID und eindeutigem Buchungsergebnis.

## Abnahmetests

- Gültige Sendungsnummer wird exakt und ohne zusätzliche Zeichen übernommen.
- Ungültige Nummer wird mit verständlichem Fehler abgelehnt.
- Wiederholte Übergabe erzeugt keinen doppelten Vorgang.
- Ziel-App nicht installiert: TRAKUNA zeigt einen klaren Hinweis; lokaler Cache bleibt erhalten.
- Ziel-App installiert, aber ohne Schnittstelle: TRAKUNA behauptet keine automatische Übernahme.
- Offline-/Timeout-Fall: kein falscher Erfolgsstatus und keine verlorene lokale Erfassung.
- Rückmeldung der Partner-App wird auf korrekte Sendungsnummer, Partner-Vorgangs-ID und Buchungsergebnis geprüft.
- App-Neustart und erneute Übergabe werden auf doppelte oder veraltete Intents geprüft.

## Für die konkrete Anbindung noch erforderlich

- Name des Betriebssoftware-Anbieters und Produktversion.
- Android-Paketname der App.
- Offizielle API-/Intent-Dokumentation oder Kontakt zum Anbieter.
- Testzugang und freigegebene Testsendungen.
- Beschreibung, welche Paketshop-Aktionen die Schnittstelle tatsächlich unterstützt (Annahme, Kundenausgabe, Retoure, Übergabe an Zusteller).

Seit Version 1.42 ordnet TRAKUNA rein numerische Sendungsnummern nicht anhand ihrer Länge einem Paketdienst zu. Dieses Verhalten gilt auch in Version 1.54; der Paketdienst muss bei Bedarf manuell ausgewählt werden.

Ohne diese Angaben bleibt die Integration absichtlich ein sicherer Öffnungs-/Übergabeversuch mit lokalem Cache und Zwischenablage-Fallback. Es werden keine Carrier-Endpunkte, Paketbuchungen oder Zugangsdaten erfunden.

## Sendungsnummern-Erkennung – Sicherheitsverhalten ab Version 1.44

- Eine manuelle Sendungsnummer wird als vollständiger kompakter Wert akzeptiert, wenn sie die allgemeinen Eingaberegeln erfüllt. Das allein bestätigt weder Paketdienst noch Existenz der Sendung.
- Aus freiem Etikettentext werden nur die derzeit ausdrücklich hinterlegten, strukturell eindeutigen Muster extrahiert. Mehrere unterschiedliche Treffer führen zur Ablehnung statt zur willkürlichen Auswahl.
- Bei QR-Links werden ausdrücklich benannte Sendungsnummern-Felder ausgewertet. Allgemeine numerische IDs und beliebige URL-Pfadsegmente gelten nicht als ausreichender Nachweis.
- Die Kamera sammelt erkannte Nummern über mehrere Bilder. Tauchen im Bestätigungsfenster mehrere unterschiedliche Nummern auf, fordert TRAKUNA einen erneuten bzw. manuellen Scan.
- Automatische Paketdienst-Hinweise bleiben begrenzt auf UPS, Österreichische Post und bestimmte DHL-Muster. DPD, GLS und Hermes müssen bis zur Prüfung freigegebener Originaletiketten manuell ausgewählt werden.

**Wichtig:** Diese Schutzregeln verringern Fehlzuordnungen, ersetzen aber keine Prüfung mit echten, vom Paketshop freigegebenen Testetiketten. Eine automatisch extrahierte Nummer ist keine offizielle Sendungs- oder Buchungsbestätigung.

## Zebra DataWedge – Barcode-Eingang

**Legacy-Abgrenzung (26.11.2026):** Dieser Abschnitt dokumentiert ausschließlich den vorhandenen DataWedge-Codepfad. Die verbindliche Zielarchitektur verwendet ein Android-Smartphone als zentrale Bedienoberfläche, Kamera/manuelle Eingabe und einen gemeinsamen Etikettendrucker. Zebra-Handgeräte dienen dabei ausschließlich als Internet-/Portalgeräte. Keine neue DataWedge- oder Zebra-Hardwareintegration planen und keine bestehenden Shopgeräte/Profile verändern. DataWedge ist keine Abnahmebedingung des neuen Ablaufs.

TRAKUNA verarbeitet Scan-Ergebnisse von Zebra DataWedge über einen Android-Broadcast nur auf Android 14 (API 34) oder höher und nur dann, wenn Android den Absender als `com.symbol.datawedge` bestätigt. Auf Android 13 (API 33) und älter weist TRAKUNA DataWedge-Broadcasts aus Sicherheitsgründen zurück; dort Kamera-Scanner oder manuelle Eingabe verwenden.

### Grundkonfiguration auf dem Zebra-Gerät

1. DataWedge öffnen und ein Profil erstellen oder das für TRAKUNA zugewiesene Profil bearbeiten.
2. Barcode input aktivieren.
3. Intent output aktivieren und als Delivery **Broadcast Intent** auswählen.
4. Intent action exakt auf `de.trakuna.app.DATAWEDGE_SCAN` setzen; Kategorie leer lassen.
5. Sicherstellen, dass DataWedge den dekodierten Text als `com.symbol.datawedge.data_string` übergibt.
6. Das Profil der TRAKUNA-App (`de.trakuna.app`) zuordnen.

### Empfohlene Empfänger-Allowlist und Signaturprüfung

Siehe auch die [offizielle Zebra-Dokumentation zu Intent Output](https://techdocs.zebra.com/datawedge/latest/guide/output/intent/).

**Diesen Schritt zusätzlich durchführen.** Die Action allein ist kein Zugriffsschutz. DataWedge bietet unter „Component Information“ eine Empfängerbeschränkung an:

1. Im TRAKUNA-Profil den Bereich **Intent Output** öffnen.
2. **Component Information** öffnen und **New Component** wählen.
3. In der installierten App-Liste **TRAKUNA** (Paketname `de.trakuna.app`) auswählen.
4. Wenn DataWedge fragt, ob die App-Signatur geprüft werden soll, **OK / Signaturprüfung aktivieren** wählen.
5. Prüfen, dass TRAKUNA mit aktivierter Signaturprüfung in der Component-Liste steht.
6. Profil speichern und einen Testbarcode scannen.

DataWedge sendet damit Intent-Daten nur an das festgelegte Paket und prüft zusätzlich dessen Signatur. Wird später eine App-Version mit einem anderen Signaturschlüssel installiert, muss die Komponentenauswahl in DataWedge erneut geprüft bzw. aktualisiert werden. Die Signatur wird bewusst nicht als fester Text in dieser Anleitung angegeben: DataWedge kann die Signatur der installierten App selbst erfassen; ein statischer Hash wäre ohne Kenntnis des endgültigen Release-Schlüssels nicht verlässlich. **Debug-Testpakete aus unterschiedlichen CI-Läufen können unterschiedlich signiert sein.** Nach Installation eines neuen Testpakets Component Information erneut öffnen, TRAKUNA neu auswählen und die Signaturprüfung erneut aktivieren. Für den späteren Produktivbetrieb ist ein stabiler, sicher verwahrter Release-Signaturschlüssel erforderlich.

### Verhalten in TRAKUNA

TRAKUNA liest den Scan nur ein, solange die App im Vordergrund aktiv ist, und zeigt ihn zur Prüfung im Sendungsnummernfeld an. Broadcasts während die App im Hintergrund ist, werden ignoriert. Auch ein Kamera-Scan löst keine automatische Buchung oder Partnerübergabe aus. Der Bediener muss die Nummer prüfen und die Übergabe bewusst starten. So wird vermieden, dass ein zufälliger Scan sofort einen betrieblichen Vorgang auslöst.

**Sicherheitsgrenze:** Die In-App-Absenderprüfung ist maßgeblich: Ab Android 14 (API 34) akzeptiert TRAKUNA nur Broadcasts, deren von Android gemeldeter Absender exakt `com.symbol.datawedge` ist. Auf Android 13 (API 33) und älter ist der DataWedge-Eingang in TRAKUNA deaktiviert, weil dort keine gleichwertig verifizierbare ursprüngliche Senderidentität verfügbar ist. DataWedge Component Information und Signaturprüfung sind zusätzliche Zustellkontrollen im Geräteprofil; sie ersetzen die In-App-Prüfung nicht. TRAKUNA kann nicht erkennen oder erzwingen, ob ein Administrator diese zusätzlichen Geräteeinstellungen aktiviert hat. Auch ein verifizierter DataWedge-Absender macht den Barcode-Inhalt nicht vertrauenswürdig: Nummern und sonstige Eingabewerte müssen vor jeder Buchung geprüft werden.


## GLS-/DPD-Handgeräte – Zebra TC26

Nach den neuesten Vor-Ort-Angaben sind die beiden GLS-/DPD-Handgeräte sehr wahrscheinlich Zebra TC26. Ausschlaggebend ist die beschriebene TC21/TC26-Gehäuseform in Verbindung mit der sichtbaren 4G-Mobilfunkanzeige. Zebra dokumentiert den Unterschied ausdrücklich: TC21 bietet WLAN, TC26 WLAN und Mobilfunk. Die genaue TC26-Teilenummer ist vor Ort noch am Typenschild zu bestätigen.

Zebra DataWedge ist als technischer Scan-Eingang nur auf Android 14 (API 34) oder höher für TRAKUNA freigegeben. Auf Android 13 (API 33) und älter bleibt der DataWedge-Eingang deaktiviert; dort sind Kamera-Scanner oder manuelle Eingabe zu verwenden. Auf unterstützten Geräten verarbeitet TRAKUNA den Broadcast nur, wenn Android den Absender als `com.symbol.datawedge` bestätigt. Die DataWedge-Konfiguration muss auf jedem Gerät einzeln mit dem dedizierten TRAKUNA-Profil eingerichtet werden. Component Information und Signaturprüfung sind zusätzliche Zustellkontrollen und ersetzen nicht die Absenderprüfung in TRAKUNA. Es wurde noch kein Testscan auf den GLS-/DPD-Shopgeräten ausgeführt.

Offizielle Quellen:
- Zebra TC21/TC26 Datenblatt: https://www.zebra.com/de/de/products/spec-sheets/mobile-computers/handheld/tc21-tc26.html
- Zebra DataWedge Dokumentation: https://techdocs.zebra.com/datawedge/

## Hermes – Voraussetzungen für die konkrete Paketshop-Integration

Hermes ist für den vorgesehenen Pilot-Paketshop eine Pflichtanforderung. In der aktuellen App kann Hermes als Paketdienst manuell eingetragen werden; eine automatische Hermes-Format-Erkennung, Hermes-Buchung oder direkte Hermes-Schnittstelle ist **noch nicht umgesetzt**.

Vor einer technischen Implementierung müssen Betreiber bzw. Hermes-Ansprechpartner folgende Informationen bereitstellen oder bestätigen:

1. Welche Hermes-Paketshop-Leistungen führt dieser Standort aus (Paketannahme, Kundenausgabe, Retouren, Versandlabel/Frankierung, Übergabe an Fahrer)?
2. Wie heißt die aktuell verwendete Hermes-Paketshop-Software und welche Version läuft darauf?
3. Wer ist Hersteller bzw. technischer Support dieser Software?
4. Gibt es eine offiziell freigegebene Android-Integration, Intent-Schnittstelle, API oder ein SDK? Bitte technische Dokumentation und Freigabebedingungen nennen.
5. Gibt es Testzugänge, Testsendungen und eine Sandbox?
6. Welche Barcode-Symbologien und konkreten Hermes-Label-Beispiele müssen unterstützt werden? Beispiele bitte ohne echte Kundendaten bereitstellen.
7. Welche Rückmeldungen sind nach einer Annahme, Ausgabe oder Retoure erforderlich (Vorgangs-ID, Status, Quittung)?
8. Welche Geräte-/Scanner-Modelle und Zebra-DataWedge-Konfiguration sind im Standort im Einsatz?
9. Welche vertraglichen, Datenschutz- und Sicherheitsvorgaben gelten für eine zusätzliche Software?

**Keine Schnittstelle erraten:** Ohne offizielle Dokumentation und Freigabe darf TRAKUNA keine Hermes-Buchung, Sendungsannahme oder Statusmeldung behaupten. Der erste Test erfolgt mit fiktiven Sendungsnummern und parallel zum bestehenden Hermes-System, sofern der Betreiber dies genehmigt.

### Abnahmekriterien für Hermes

- Hermes kann als Paketdienst eindeutig manuell ausgewählt werden.
- Unterstützte Hermes-Barcodes werden mit realen, freigegebenen Testetiketten am konkreten Zebra-Gerät eingelesen.
- Die Sendungsnummer wird korrekt angezeigt und kann vor dem Speichern geprüft werden.
- Die vier für den Standort benötigten Abläufe werden einzeln getestet.
- Wiederholte Scans erzeugen keine unbeabsichtigten Doppelvorgänge.
- Eine offizielle Buchung gilt erst dann als erfolgreich, wenn das freigegebene Hermes-System sie ausdrücklich bestätigt.
