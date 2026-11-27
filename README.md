# TRAKUNA – Paketshop mit möglichst wenigen Schritten

TRAKUNA soll den Paketshop-Alltag vereinfachen. Die App konzentriert sich auf vier Vorgänge und läuft im aktuellen Zielbetrieb auf **einem Android-Smartphone**.

## Die vier Kernvorgänge

1. **Annahme** – Sendung für einen Kunden erfassen.
2. **Abholung** – Sendung finden und tatsächliche Ausgabe dokumentieren.
3. **Retoure** – Rücksendung erfassen.
4. **Versand** – Sendung finden und tatsächliche Übergabe dokumentieren.

## So funktioniert der Kern

- Barcode mit der Smartphone-Kamera scannen oder Sendungsnummer manuell eingeben.
- Eindeutigen Paketdienst automatisch erkennen; sonst einmal manuell angeben.
- Sendung lokal speichern und Duplikate verhindern.
- Bei bestehenden Sendungen nur den nächsten zulässigen Status anzeigen und vor einer Statusänderung bestätigen.
- Suche über Sendungsnummer, Empfänger oder Paketdienst.

## Bewusst nicht Teil des einfachen Ablaufs

- Keine Zebra-/DataWedge-Steuerung und kein externer Zebra-Broadcast-Scanpfad.
- Keine Partner-App-Zuordnung, App-Auswahl oder automatische Übergabe.
- Keine WLAN-/Gerätestatus-Dashboards, CSV-Exporte oder umfangreichen Verlaufs-/Bearbeitungsdialoge.
- Keine Cloud, Mehrgeräte-Synchronisierung, Rollenverwaltung oder KI-Automationen.

## Daten und Sicherheit

- Sendungsdaten bleiben lokal und verschlüsselt.
- Scan und manuelle Eingabe, Duplikatprüfung und sichere Statusübergänge bleiben erhalten.
- Backup und Wiederherstellung bleiben verfügbar, damit Gerätewechsel oder Defekte nicht zum Datenverlust führen.
- Bereits vorhandene archivierte Sendungen bleiben gespeichert und in der Suche sichtbar; eine separate Archivverwaltung ist nicht Teil der normalen Oberfläche.
- Eine lokal dokumentierte Übergabe ist keine Bestätigung des Paketdienstes.

## Druck und Carrier-Portale

Ziel ist **ein gemeinsamer Etikettendrucker für alle unterstützten Paketdienste**, direkt vom Smartphone aus. Das ist noch nicht umgesetzt oder auf realer Hardware abgenommen. Es werden keine Carrier-APIs, Portalübergaben oder Druckbestätigungen vorgetäuscht. Vor einer Umsetzung müssen Druckermodell, unterstützte Protokolle und autorisierte Carrier-Abläufe geprüft werden.

## Entwicklungsstand

- Vereinfachungsarbeiten: [PR #36 – TRAKUNA auf den notwendigen Kern reduzieren](https://github.com/mouzzlii/Fuck/pull/36) (Draft, nicht gemergt).
- Minimalumfang und Abnahmekriterien: [TRAKUNA-MINIMAL-KONZEPT.md](TRAKUNA-MINIMAL-KONZEPT.md).
- GitHub-Instrumentierung für den aktuellen Code-Stand `e737286` erfolgreich: [Run 46068641940](https://github.com/mouzzlii/Fuck/actions/runs/46068641940), einschließlich `:app:connectedDebugAndroidTest`. Der Lauf für den vorherigen UI-Stand `5929eb8` war ebenfalls erfolgreich.
- Noch kein Test auf echtem Android-Gerät; Kamera-Scan, reale Shopabläufe und physischer Druck bleiben offen.
- Kein echter Android-Gerätetest, keine Kamera-/Scanner-Abnahme und kein physischer Druckertest nachgewiesen.
- PR #25 bleibt getrennt und unangetastet.

## Betriebs- und Sicherheitsunterlagen

- [Sicherheitsstatus und Produktionsfreigabe](SECURITY.md)
- [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md)
- [Hardwareinventar des ersten Paketshops](HARDWARE-INVENTAR.md)
- [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md)
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md)
- [Betriebshandbuch](BETRIEBSHANDBUCH.md)
- [TRAKUNA Multi-Carrier Gateway](https://github.com/mouzzlii/multi-carrier-gateway)

**Produktregel:** Jede neue Funktion muss nachweislich einen der vier Kernvorgänge einfacher, sicherer oder zuverlässiger machen. Wenn sie nur zusätzliche Auswahl oder Verwaltung erzeugt, gehört sie nicht in den normalen Ablauf.
