# TRAKUNA – Schnittstellenvertrag für Partner-Betriebssoftware

Stand: App-Version 1.40

## Zweck

Dieses Dokument beschreibt die minimale technische Vereinbarung, damit eine Paketshop- oder Carrier-App eine Sendungsnummer aus TRAKUNA übernehmen kann. Es ist **keine Behauptung**, dass eine bestehende Post-, DHL-, DPD-, GLS- oder UPS-App diese Schnittstelle bereits unterstützt.

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

Ohne diese Angaben bleibt die Integration absichtlich ein sicherer Öffnungs-/Übergabeversuch mit lokalem Cache und Zwischenablage-Fallback. Es werden keine Carrier-Endpunkte, Paketbuchungen oder Zugangsdaten erfunden.

## Zebra DataWedge – Barcode-Eingang

TRAKUNA kann Scan-Ergebnisse von Zebra DataWedge über einen Android-Broadcast empfangen.

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

**Sicherheitsgrenze:** Die DataWedge-Allowlist und Signaturprüfung müssen im Zebra-Profil tatsächlich aktiviert sein. TRAKUNA kann nicht erkennen oder erzwingen, ob ein Administrator diese Geräteeinstellung vorgenommen hat. Ohne korrekt eingerichtete Component Information bleibt der dynamische Android-Empfänger grundsätzlich durch andere Apps auf demselben Gerät nachahmbar. Eingehende Werte sind daher weiterhin nicht vertrauenswürdige Eingaben und müssen vor jeder Buchung geprüft werden.
