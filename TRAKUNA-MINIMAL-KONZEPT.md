# TRAKUNA – verbindlicher Minimalumfang

**Stand:** 27.11.2026  
**Geltung:** Produktumfang und nächste Vereinfachungsschritte für die Android-App.  
**Leitentscheidung:** TRAKUNA soll Paketshop-Arbeit vereinfachen. Die App darf keine zusätzliche Verwaltungsarbeit erzeugen.

## 1. Zweck

Eine Person erledigt die alltäglichen Paketshop-Vorgänge mit **einem Android-Smartphone**. TRAKUNA erfasst und findet Sendungen, dokumentiert die notwendigen Statusänderungen und führt den Nutzer – soweit sinnvoll – zum offiziellen Anbieterportal. Zielbild für Versandetiketten: **alle unterstützten Anbieter, ein gemeinsamer Drucker**.

TRAKUNA ersetzt keine offiziellen Carrier-Systeme und behauptet keine Buchung, Datenübermittlung oder Druckbestätigung, die technisch nicht tatsächlich erfolgt ist.

## 2. Unverzichtbarer Kern

Die Startseite bietet nur vier klar erkennbare Arbeitsaktionen:

1. **Annahme** – Paket/Sendung erfassen.
2. **Abholung** – vorhandene Sendung finden und die Ausgabe dokumentieren.
3. **Retoure** – Rücksendung erfassen.
4. **Versand** – vorhandene Sendung finden und die Übergabe dokumentieren.

Der gemeinsame Erfassungsablauf bleibt kurz:

- Sendungsnummer per Kamera scannen oder manuell eingeben.
- Paketdienst übernehmen, wenn eindeutig erkannt; andernfalls einmal auswählen.
- Kundenname nur optional und nur, wenn der Shop ihn tatsächlich benötigt.
- Vorgang speichern bzw. bei bestehenden Vorgängen den passenden Status ändern.

Die App muss Duplikate und mehrdeutige Scans sicher behandeln. Sie darf niemals stillschweigend eine falsche Sendungsnummer oder einen falschen Status übernehmen.

## 3. Minimale Navigation

- **Startseite:** vier Aktionen und ein klarer Zugang zu „Sendungen suchen“.
- **Erfassungs-/Suchansicht:** Sendungsnummer, Scan, Paketdienst und gegebenenfalls optionaler Kundenname.
- **Trefferliste:** nur die für die Auswahl nötigen Angaben; ein Treffer öffnet den Vorgang.
- **Vorgang:** Sendungsnummer, Anbieter, aktueller Status und genau die nächste sinnvolle Aktion.
- **Einstellungen:** nur notwendige Datensicherung/Wiederherstellung und tatsächlich erforderliche Grundeinstellungen.

Keine verschachtelten Menüs. Häufige Abläufe müssen mit einer Hand und wenigen gut sichtbaren Schaltflächen bedienbar sein.

## 4. Beibehalten – weil es den Kern schützt oder ermöglicht

- Kamera-Scan plus manuelle Eingabe als Rückfall.
- Lokale, verschlüsselte Speicherung; keine Cloud und keine Mehrgeräte-Synchronisierung im aktuellen Ein-Smartphone-Betrieb.
- Duplikatprüfung und klare Statusübergänge.
- Sichere, verständliche Datensicherung und Wiederherstellung, damit ein Gerätewechsel oder Defekt nicht zum Datenverlust führt.
- Offline-Nutzung für Erfassung und Statusverwaltung.
- Eindeutige Hinweise, wenn eine Aktion nur lokal dokumentiert wurde und keine Carrier-Bestätigung vorliegt.

## 5. Aus der normalen Bedienoberfläche entfernen oder zurückstellen

Diese Funktionen sind kein Teil des minimalen Kernablaufs und dürfen ihn nicht belasten:

- Zebra-/DataWedge-Steuerung und Zebra-spezifische Abläufe. Der Nutzer hat ausdrücklich festgelegt, dass TRAKUNA Zebra-Geräte nicht direkt steuern soll.
- Partner-App-Erkennung, App-Zuordnung je Paketdienst, Partner-App-Picker und automatische Übergabe/Clipboard-Handoff.
- Separate Scan-Diagnoseansichten und Rohdatenanzeigen im normalen Shopbetrieb.
- WLAN-Statuskarten ohne direkte Bedeutung für die aktuelle Aufgabe.
- CSV-Export, freie Notizfelder, umfangreiche Bearbeitungsdialoge und detaillierte Ereignis-/Verlaufansichten, solange kein konkreter betrieblicher Bedarf dafür belegt ist.
- Archiv-Untermenüs und zusätzliche Verwaltungszustände, sofern die nötige Aufbewahrung und Wiederherstellung ohne diese Bedienkomplexität gewährleistet werden kann.
- Mehrgeräte-/Cloud-Architektur, Benutzer-/Adminrollen, Dashboards und zusätzliche KI-/Automationsfunktionen.

Vor dem Entfernen bestehender Codepfade sind Abhängigkeiten, Tests und gespeicherte Datenformate zu prüfen. Bereits gespeicherte Sendungen, Backups und ältere Datenstände dürfen nicht verloren gehen. Nicht benötigte Funktionen zunächst aus der UI nehmen oder sicher deaktivieren; Code und Daten erst nach nachgewiesener Unabhängigkeit und Rückwärtskompatibilität löschen.

## 6. Anbieterportal und Ein-Drucker-Ziel

- Offizielle Anbieterportale bleiben der maßgebliche Ort für Carrier-Vorgänge.
- Ein Portal darf nur über eine geprüfte offizielle HTTPS-Adresse geöffnet werden.
- TRAKUNA darf keine Zugangsdaten speichern und keine Buchung oder Datenübertragung vortäuschen.
- Ziel ist ein gemeinsamer Etikettendrucker für alle Anbieter, bedienbar mit einem Smartphone.
- WLAN und Bluetooth sind mögliche Verbindungswege, aber erst nach Ermittlung des konkreten Druckermodells und seiner unterstützten Android-/Druckprotokolle.
- Druckintegration wird nicht mit geratenen Drucker-APIs, direkten unsicheren Socket-Verbindungen oder nicht freigegebenen Carrier-Schnittstellen umgesetzt.
- Bis eine reale, autorisierte und getestete Verbindung besteht, wird Druck als **nicht implementiert** gekennzeichnet. Ein angenommener Druckauftrag ist keine bestätigte physische Ausgabe.

## 7. Abnahmekriterien für jede Vereinfachung

Eine Änderung ist nur dann eine Verbesserung, wenn sie den Arbeitsablauf tatsächlich vereinfacht und alle folgenden Punkte erfüllt:

- Die vier Kernvorgänge bleiben vollständig und verständlich.
- Einhandbedienung auf einem Android-Smartphone.
- Scan und manuelle Eingabe funktionieren; mehrdeutige oder doppelte Eingaben werden sicher behandelt.
- Vorgänge bleiben lokal gespeichert und nach Neustart vorhanden.
- Offline-Erfassung funktioniert.
- Keine versehentliche Doppelanlage durch Doppelscan oder Doppeltipp.
- Backup und Wiederherstellung bleiben nachvollziehbar und getestet.
- Keine Abstürze in den Kernabläufen; vollständiger, kompilierbarer Code ohne Platzhalter.
- Automatisierte Tests und Build beziehen sich auf den exakten Commit.
- Nicht real getestete Hardware-, Portal- und Druckfunktionen werden ausdrücklich als offen markiert.

## 8. Vorgehensweise

1. Zuerst die Bedienoberfläche auf die vier Kernaktionen und den einfachen Such-/Erfassungsablauf reduzieren.
2. Danach nicht benötigte UI-Einstiege und zugehörige Codepfade einzeln entfernen – pro Arbeitsdurchlauf genau eine abgeschlossene Baustelle.
3. Danach Datenmodell und Statuslogik nur dort vereinfachen, wo keine Bestandsdaten oder Sicherheitsgarantien verloren gehen.
4. Drucker-/Portal-End-to-End-Funktionen erst nach realer Geräte- und Freigabeprüfung ergänzen.
5. Jede Änderung mit vollständigem Diff, Regressionstests und einem klaren Vorher-/Nachher-Vergleich dokumentieren.

**Entscheidungsmaßstab:** Weniger Schritte, weniger Auswahl, weniger Fehlerquellen und weniger Wartungsaufwand. Nicht mehr Funktionen.

## 9. Umsetzungsstand

**Im aktuellen Arbeitszweig umgesetzt (noch nicht freigegeben):**

- Startseite auf vier klare Aktionen reduziert: Paket für Kunden annehmen, Versand vom Kunden annehmen, Retoure annehmen sowie Sendungen suchen/Abholung.
- Scan und manuelle Eingabe bleiben; erkannter Paketdienst wird als Hinweis angezeigt. Wenn kein eindeutiger Treffer vorliegt, muss der Paketdienst eingegeben werden.
- WLAN-Statuskarte, Partner-App-Zuordnung/-Picker und Übergabe-Schaltfläche aus der normalen Oberfläche entfernt.
- Such-/Abholansicht verschlankt: CSV-Export, Historien-/Bearbeitungsdialoge, Archiv-Umschalter/-Aktionen und endgültige Löschaktion aus der normalen Oberfläche entfernt. Bereits archivierte Datensätze bleiben in der gemeinsamen Suche sichtbar und werden nicht gelöscht.
- Lokale Datenspeicherung, Duplikatprüfung, Statusbestätigung und Backup/Wiederherstellung bleiben erhalten.
- Partner-App-Zuordnungslogik und den externen DataWedge-/Zebra-Broadcast-Empfang aus dem normalen App-Pfad entfernt. Der Smartphone-Kamera-Scan und die manuelle Eingabe bleiben der einzige vorgesehene Scanweg. Der isolierte verschlüsselte Alt-Übergabe-Cache bleibt vorerst ausschließlich zur Kompatibilität mit älteren lokalen Daten erhalten.
- Nicht mehr benötigte Android-Paketabfragen und die Netzwerkstatus-Berechtigung entfernt; der Internetzugriff bleibt gesperrt.
- Veralteten Speicherlimit-Hinweis an die vereinfachte Oberfläche angepasst; keine Aufforderung mehr zu nicht verfügbaren CSV-/Löschfunktionen.

**Bereits nachgewiesen:** GitHub-Instrumentierung für Code-Commit `e737286` erfolgreich ([Run 46068641940](https://github.com/mouzzlii/Fuck/actions/runs/46068641940)); `:app:connectedDebugAndroidTest` endete mit `BUILD SUCCESSFUL`. Auch die vorherigen UI-Stände wurden erfolgreich instrumentiert.

**Aktueller Code-Commit `741f952`:** Speicherlimit-Hinweis an die vereinfachte Oberfläche angepasst; der zugehörige Instrumentierungsworkflow [Run 46069647978](https://github.com/mouzzlii/Fuck/actions/runs/46069647978) war beim letzten Abruf noch ausstehend.

**Noch nicht nachgewiesen:** Bedienung auf einem echten Android-Gerät, Kamera-Scan-Abnahme, reale Paketshop-Abläufe sowie Drucker- oder Carrier-End-to-End-Funktion. Der Emulatorlauf belegt keinen Praxistest. Bis diese Punkte geprüft sind, bleibt der PR Draft und wird nicht gemergt.
