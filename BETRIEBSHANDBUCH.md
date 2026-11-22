# TRAKUNA – Betriebshandbuch

Stand: App-Version 1.33

Dieses Handbuch erklärt die Bedienung der aktuellen TRAKUNA-Testversion. Es richtet sich an Paketshop-Mitarbeiter ohne technische Vorkenntnisse.

## 1. Wichtiger Status vor dem ersten Einsatz

TRAKUNA 1.32 ist eine **lokale Entwicklungs- und Praxistestversion**, noch keine freigegebene Produktivsoftware.

- Sendungsdaten liegen nur auf diesem Android-Gerät. Es gibt keine zentrale Synchronisierung zwischen mehreren Geräten.
- Es gibt noch keine Mitarbeiter-Anmeldung, Benutzerrollen oder persönliche Zuordnung von Bearbeitern.
- Eine ausgewählte Partner-App wird geöffnet und erhält einen Übergabeversuch. Eine tatsächliche Buchung in der Partner-Software ist damit nicht bestätigt.
- Die Paketdienst-Erkennung ist nur ein Hinweis. Nummer und Paketdienst müssen geprüft werden.
- Maximal 500 Vorgänge können lokal gespeichert werden.
- JSON- und CSV-Dateien enthalten vertrauliche Sendungs- und Empfängerdaten unverschlüsselt.
- Vor echtem Betrieb sind Zebra-Gerätetests, echte Etikettentests, Partnerabnahme, Datenschutzprüfung und ein signiertes Produktionsrelease erforderlich.

Bitte zuerst ausschließlich mit Testsendungen arbeiten.

## 2. App installieren

1. Den erfolgreichen Android-Build im [GitHub-Actions-Bereich](https://github.com/mouzzlii/Fuck/actions) öffnen.
2. Den Bereich **Artifacts** öffnen und das Paket **TRAKUNA-Android** herunterladen.
3. Das ZIP auf dem Android-Gerät entpacken.
4. Die enthaltene Datei `app-debug.apk` öffnen und installieren.
5. Android kann eine einmalige Freigabe für die Installation aus dieser Datei-App verlangen. Nur für die vertrauenswürdige Datei-App freigeben und die Freigabe danach wieder deaktivieren.
6. Die App starten und zunächst die Tests aus `PRAxisTEST.md` durchführen.

Das ist ein Debug-Testpaket, kein signiertes Produktionsrelease.

## 3. Sendungsnummer erfassen

Es gibt drei Eingabewege:

### Kamera

1. **Barcode / QR-Code scannen** wählen.
2. Beim ersten Start den Kamerazugriff erlauben.
3. Ein Paketlabel möglichst einzeln und gut sichtbar in den Scanbereich halten.
4. Die erkannte Sendungsnummer erscheint im Eingabefeld.
5. Nummer und Paketdienst kontrollieren.

Wenn mehrere unterschiedliche Sendungsnummern gleichzeitig erkannt werden, übernimmt TRAKUNA keine davon. Bitte nur ein Label scannen oder manuell eingeben.

### Manuelle Eingabe

1. Sendungsnummer in **Sendungsnummer manuell eingeben** schreiben.
2. Auf richtige Zeichenfolge achten; Leerzeichen werden bei der Verarbeitung entfernt.
3. Die manuelle Eingabe bleibt auch ohne Kameraberechtigung verfügbar.

### Zebra DataWedge

Das Zebra-Gerät muss eingerichtet werden. Die genaue Konfiguration steht in `PARTNER_INTEGRATION.md`. Die [offizielle Zebra-Dokumentation zu DataWedge Intent Output](https://techdocs.zebra.com/datawedge/latest/guide/output/intent/) erklärt die Component Information und Signaturprüfung.

**Hinweis zu Testpaketen:** Debug-Testpakete aus unterschiedlichen CI-Läufen können unterschiedlich signiert sein. Nach einer Aktualisierung kann es deshalb nötig sein, TRAKUNA in DataWedge unter Component Information erneut auszuwählen und die Signaturprüfung neu zu aktivieren.

Wichtig: DataWedge muss die Action `de.trakuna.app.DATAWEDGE_SCAN` und das Extra `com.symbol.datawedge.data_string` verwenden. Im Zebra-Profil sollte unter **Intent Output → Component Information** ausschließlich TRAKUNA ausgewählt und die Signaturprüfung aktiviert werden. Diese Geräteeinstellung kann TRAKUNA nicht selbst kontrollieren.

Ein Zebra-Scan füllt nur das Sendungsnummernfeld. Er löst keine automatische Buchung aus.

## 4. Paketshop-Vorgänge

**Paketshop-Sendungen verwalten** öffnen. Die Sendungsnummer aus dem oberen Eingabefeld wird für den neuen Vorgang verwendet. Empfänger, Paketdienst und Notiz sind optional.

### Paket für einen Kunden annehmen

1. Sendungsnummer prüfen.
2. Optional Empfänger, Paketdienst und Notiz eintragen.
3. **Paket für Kunden annehmen** wählen.
4. Der Vorgang erscheint als **Im Shop eingetroffen**.
5. Wenn das Paket im Shop geprüft und abholbereit ist, beim Vorgang **Als abholbereit markieren** wählen.
6. Wenn das Paket dem Kunden tatsächlich ausgehändigt wurde, **An Kunden übergeben** wählen und die Bestätigung bewusst bestätigen.

### Sendung eines Kunden annehmen

1. Sendungsnummer des vom Kunden abgegebenen Pakets erfassen.
2. Optional Empfänger, Paketdienst und Notiz ergänzen.
3. **Sendung vom Kunden annehmen** wählen.
4. Der Vorgang erscheint als **Vom Kunden angenommen**.
5. Erst wenn das Paket tatsächlich an den Paketdienst übergeben wurde, die Statusaktion **An Paketdienst übergeben** wählen und bestätigen.

### Retoure annehmen

1. Sendungsnummer der Retoure erfassen und prüfen.
2. **Retoure annehmen** wählen.
3. Der Vorgang erscheint als **Retoure angenommen**.
4. Erst nach tatsächlicher Übergabe an den Paketdienst die Aktion **Retoure an Paketdienst übergeben** wählen und bestätigen.

**Wichtig:** Statusbestätigungen ersetzen keinen Beleg oder Scan des Paketdienstes. TRAKUNA erhält derzeit keine bestätigte Rückmeldung vom Carrier.

## 5. Suchen, bearbeiten und löschen

- Das Suchfeld filtert nach Sendungsnummer, Empfänger, Paketdienst und Status.
- Bei mehr als 100 Treffern können weitere Treffer in Blöcken eingeblendet werden.
- **Details bearbeiten** ändert Empfänger und interne Notiz.
- **Vorgang löschen** entfernt den ausgewählten Eintrag nach einer zusätzlichen Bestätigung endgültig von diesem Gerät.
- Doppelte offene Vorgänge desselben Typs werden erkannt und nicht nochmals als neu gespeichert.

## 6. Partner-Betriebssoftware

1. **Betriebssoftware auf diesem Gerät finden** öffnen.
2. Die passende installierte App auswählen. Falls sie nicht in der Vorauswahl erscheint, **Alle installierten Apps anzeigen** wählen.
3. Sendungsnummer erfassen und kontrollieren.
4. **An Partner-App übergeben** wählen.

TRAKUNA speichert die Sendungsnummer vorher verschlüsselt im lokalen Übergabe-Cache und versucht, die ausgewählte App mit definierten Android-Intent-Extras zu öffnen. Zusätzlich kann die Nummer in die Zwischenablage kopiert werden.

**Das Öffnen einer App ist keine bestätigte Buchung.** Nur wenn der Anbieter die TRAKUNA-Extras oder eine dokumentierte Schnittstelle tatsächlich unterstützt, kann eine Übernahme funktionieren. Die konkrete Betriebssoftware muss separat getestet und vom Anbieter freigegeben werden.

## 7. Datensicherung und Export

### CSV-Export

1. **CSV-Datei exportieren** wählen.
2. Die Warnung lesen: Die Datei enthält Sendungsnummern, Empfängerdaten und interne Notizen und ist unverschlüsselt.
3. Nur fortfahren, wenn ein geschützter Speicherort ausgewählt werden kann.
4. Die Datei nur an berechtigte Personen weitergeben.

### Vollständige JSON-Datensicherung

1. **Vollständige Datensicherung erstellen** wählen.
2. Warnung bestätigen und geschützten Speicherort wählen.
3. Die Datei sicher verwahren. Auch diese Sicherung ist unverschlüsselt.

### Wiederherstellung

1. **Datensicherung wiederherstellen** wählen.
2. Eine TRAKUNA-JSON-Sicherung auswählen.
3. TRAKUNA prüft Format, Version, Größe und Datensätze.
4. Vor dem Ersetzen des vorhandenen Bestands erscheint eine Bestätigung.
5. Erst nach bewusster Bestätigung wird der lokale Bestand ersetzt.
6. Anschließend die angezeigte Anzahl und einige Sendungen stichprobenartig kontrollieren.

Eine Wiederherstellung ersetzt den gesamten lokalen Datenbestand. Vorher bei Bedarf eine neue Sicherung des aktuellen Geräts erstellen.

## 8. Häufige Fehler

- **Kamera startet nicht:** Kameraberechtigung prüfen oder manuelle Eingabe verwenden.
- **Barcode wird nicht erkannt:** Label einzeln scannen, Beleuchtung verbessern oder Sendungsnummer manuell eingeben.
- **Zebra-Scan kommt nicht an:** DataWedge-Profil, Action, Extra, App-Zuordnung und Component Information kontrollieren.
- **Paketdienst wird falsch erkannt:** Paketdienst im Erfassungsformular manuell korrigieren.
- **Partner-App öffnet sich, übernimmt aber nichts:** Der Anbieter muss die Intent-Schnittstelle ausdrücklich unterstützen. Bis dahin Nummer manuell in der Partner-App übernehmen.
- **Speicherfehler wird angezeigt:** Keine weiteren Vorgänge erzwingen. Eine vertrauenswürdige Sicherung verwenden oder Support hinzuziehen. Ein beschädigter Bestand darf nicht als leer behandelt werden.
- **Speicherlimit erreicht:** Zuerst exportieren und nicht mehr benötigte Vorgänge nach den betrieblichen Aufbewahrungsregeln löschen.

## 9. Datenschutz und Aufbewahrung

Sendungsnummern, Empfängernamen und interne Notizen sind vertrauliche Betriebsdaten. Gerätezugriff beschränken, Bildschirmsperre aktivieren, Exporte schützen und nur so lange aufbewahren, wie es die betrieblichen und rechtlichen Vorgaben erlauben.

TRAKUNA bietet derzeit keine zentrale Benutzerverwaltung, keine automatische Löschfrist, keine zentrale Synchronisierung und kein revisionssicheres Mitarbeiter-Audit. Diese Punkte müssen vor einem realen Mehrmitarbeiterbetrieb gelöst werden.

## 10. Freigabe für den echten Paketshop

Vor dem Produktiveinsatz müssen Betreiber und Softwareanbieter gemeinsam bestätigen:

- alle vier benötigten Paketshop-Prozesse auf dem echten Zebra-Gerät getestet;
- echte Testetiketten der tatsächlich verwendeten Carrier erfolgreich geprüft;
- Partner-Software-Übernahme mit dokumentierter Schnittstelle und Rückmeldung abgenommen;
- Datenschutz-, Lösch-, Sicherungs- und Wiederherstellungsregeln festgelegt;
- Mitarbeiter- und Rollenmodell sowie Synchronisierung umgesetzt, falls mehrere Personen/Geräte arbeiten;
- Sicherheitsprüfung abgeschlossen und kritische Befunde behoben;
- signiertes Produktionsrelease mit sicher verwahrtem Signaturschlüssel erstellt.

Ein erfolgreicher GitHub-Build bestätigt nur automatisierte Tests und APK-Erstellung. Er ersetzt keine Geräte-, Betriebs- oder Sicherheitsabnahme.
