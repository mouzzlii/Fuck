# TRAKUNA – WLAN- und Startvorbereitung für den ersten Paketshop

Stand: 23. November 2026

## Ziel

Der erste Start soll vor Ort mit möglichst wenigen Handgriffen möglich sein: TRAKUNA starten und einen kontrollierten Funktionstest durchführen. Eine WLAN-Verbindung ist für die aktuelle lokale Testversion optional und nicht erforderlich.

**Wichtig:** WLAN allein stellt noch keine Verbindung zu DPD, GLS, Hermes oder UPS her. Eine solche Carrier-Integration benötigt eine offiziell freigegebene Schnittstelle und Zugangsdaten/Testzugänge des jeweiligen Anbieters. Bestehende Carrier-Programme und Druckerkopplungen bleiben unverändert.

## 1. Welche Verbindung wofür benötigt wird

| Verbindung | Zweck | Aktueller Stand |
|---|---|---|
| TC26 ↔ eigenes TRAKUNA auf demselben Gerät | Barcode-Übergabe von Zebra DataWedge an TRAKUNA | Empfänger im App-Code vorhanden; Profil und echter Scan am Shopgerät noch nicht getestet |
| TC26 ↔ Shop-WLAN | Internetzugang für spätere freigegebene Online-Dienste | WLAN-Fähigkeit des wahrscheinlichen TC26 vorhanden; Shop-Zugangsdaten/Netzrichtlinien vor Ort unbekannt |
| GLS-Handgerät ↔ GLS-Drucker | Bestehender GLS-Etikettendruck | Laut Betreiber Bluetooth; nicht verändern |
| DPD-Handgerät ↔ DPD-Drucker | Bestehender DPD-Etikettendruck | Laut Betreiber Bluetooth; nicht verändern |
| Hermes MC3090BT ↔ Hermes-Drucker/System | Bestehender Hermes-Ablauf | Eigenständiger Altgeräteablauf; nicht verändern |
| UPS-Gerät/Arbeitsplatz ↔ UPS-System/Drucker | UPS-Paketshop-Ablauf | Hardware und Verbindung noch nicht erhoben; vor Ort aufnehmen, nichts verändern |
| Alle vier Zebra-Handhelds ↔ WLAN | Netzwerkzugang der vorhandenen Geräte | Laut Betreiber sind alle vier Handhelds mit WLAN verbunden; gemeinsame SSID/Subnetz und gegenseitige Erreichbarkeit noch nicht geprüft |
| TRAKUNA ↔ andere TRAKUNA-Geräte | Gemeinsame Daten/Synchronisierung | Noch nicht implementiert; lokale Datenspeicherung ist derzeit nicht automatisch zwischen Geräten synchronisiert |

## 2. Vorbereitete Startreihenfolge vor Ort

Diese Schritte erst mit Zustimmung des Paketshop-Betreibers ausführen.

1. Für den rein lokalen Funktionstest WLAN überspringen; TRAKUNA benötigt derzeit keine Netzwerkverbindung.
2. Nur wenn der Betreiber das Gerät ausdrücklich für den Shop-Internetzugang freigibt, das freigegebene WLAN auswählen. Ein WLAN-Symbol allein beweist keinen Internetzugang.
3. TRAKUNA installieren bzw. öffnen. Für Updates möglichst dieselbe App-Signatur verwenden; Debug-Testpakete können anders signiert sein als ein späteres Release.
4. Kamera-Berechtigung nur dann erlauben, wenn der Kamera-Scanner verwendet werden soll.
5. In Zebra DataWedge ein eigenes TRAKUNA-Profil einrichten:
   - Barcode input aktivieren.
   - Intent output aktivieren.
   - Delivery: Broadcast Intent.
   - Action exakt: `de.trakuna.app.DATAWEDGE_SCAN`.
   - Daten-Extra: `com.symbol.datawedge.data_string`.
   - Profil der App `de.trakuna.app` zuordnen.
6. Im DataWedge-Bereich „Component Information“ ausschließlich TRAKUNA als Empfänger zulassen und – soweit angeboten – die Signaturprüfung aktivieren.
7. Mit einem vom Betreiber freigegebenen Testbarcode prüfen, ob die Nummer in TRAKUNA erscheint.
8. Prüfen, dass ein Scan allein keine Buchung und keinen Etikettendruck auslöst.
9. Erst nach dokumentierter Freigabe weitere Tests durchführen.

Die DataWedge-Konfiguration muss auf jedem Zebra-Gerät separat vorgenommen werden. Sie ist nicht automatisch durch die Installation von TRAKUNA eingerichtet. DataWedge Component Information schützt die Zustellung **von DataWedge an TRAKUNA** (Paketname und optional App-Signatur). Es schützt nicht den dynamischen Broadcast-Empfänger von TRAKUNA vor beliebigen Apps, die auf demselben Gerät einen gleichartigen Broadcast fälschen. Deshalb bleiben Eingaben nicht vertrauenswürdig und müssen geprüft werden.

## 3. Netzwerkanforderungen

Für den derzeitigen lokalen TRAKUNA-Funktionsumfang ist kein direkter WLAN-Zugriff auf die GLS-, DPD- oder Hermes-Geräte erforderlich. Die aktuelle Android-Manifestdatei fordert `ACCESS_NETWORK_STATE`, aber keine `INTERNET`-Berechtigung an. Version 1.49 kann deshalb den WLAN-Transport des **aktuellen Geräts** anzeigen, aber keine Netzwerkdienste aufrufen, keine anderen Geräte suchen und keine Daten über WLAN oder 4G synchronisieren. WLAN ist für den lokalen Scannerbetrieb nicht erforderlich. Es wird auch kein Netzwerk-Port an den Paketshop-Routern geöffnet.

Erst wenn eine freigegebene Online-Funktion oder zentrale Synchronisierung entwickelt wird, sind vorab festzulegen:
- ein vom Betreiber freigegebener HTTPS-Dienst,
- Benutzerkonten und Rollen,
- verschlüsselte Übertragung und sichere Geräteauthentisierung,
- Offline-Warteschlange mit Konfliktbehandlung,
- nachvollziehbare Vorgangs- und Änderungsprotokolle,
- definierte Aufbewahrungs- und Löschregeln.

Keine Portweiterleitung ins Internet und kein ungeschützter lokaler Webserver auf einem Handgerät einrichten.

## 4. Android-Kompatibilität und lokale Netzwerke

Der aktuelle App-Build zielt auf Android API 35. Die App enthält derzeit keinen lokalen Netzwerk-Server und keine automatische Suche nach anderen Geräten. Die WLAN-Anzeige prüft nur den aktiven Netzwerktransport des aktuellen Geräts. Deshalb wird derzeit keine Berechtigung für lokalen Netzwerkzugriff angefordert.

Bei späterer Anhebung des Target SDK auf API 37 oder höher muss die Android-17-Regelung für lokalen Netzwerkzugriff berücksichtigt werden. Android sieht dafür die Laufzeitberechtigung `ACCESS_LOCAL_NETWORK` oder – für die gezielte Auswahl einzelner Dienste – den systemvermittelten NSD-Service-Picker vor. Das wird erst implementiert, wenn ein konkreter Netzwerkdienst feststeht.

Offizielle Android-Dokumentation:
- https://developer.android.com/privacy-and-security/local-network-permission
- https://developer.android.com/reference/android/net/nsd/NsdManager

## 5. Starttest – Abnahmeprotokoll

- [ ] Shop-WLAN ausgewählt und Internetzugang separat geprüft.
- [ ] TRAKUNA startet ohne Absturz.
- [ ] Kamera-Berechtigung und Kamerascanner funktionieren (falls genutzt).
- [ ] Manuelle Sendungsnummerneingabe funktioniert.
- [ ] DataWedge-Profil ist nur TRAKUNA zugeordnet.
- [ ] Component Information / Signaturprüfung ist eingerichtet.
- [ ] Freigegebener Testbarcode erscheint im richtigen Eingabefeld.
- [ ] Unbekannte oder mehrdeutige Scans werden nicht automatisch gebucht.
- [ ] Bestehender GLS-/DPD-/Hermes-Betrieb blieb unverändert.
- [ ] Datensicherung und Wiederherstellung wurden mit Testdaten geprüft.
- [ ] Offline-Verhalten wurde geprüft.
- [ ] Betreiber hat den Test und die weitere Nutzung freigegeben.

## 6. Was vor dem Start noch nicht automatisch erledigt werden kann

Diese Punkte hängen von Informationen oder Freigaben außerhalb des Repositorys ab:
- WLAN-Name, WLAN-Passwort und etwaige Geräte-/Gastnetzrichtlinien des Betreibers (nur nötig, wenn ein späterer Online-Dienst tatsächlich freigegeben wird).
- Installation und Freigabe auf den tatsächlichen Shopgeräten.
- DataWedge-Profil auf jedem TC26 und der echte Scan-Test.
- Stabiler Release-Signaturschlüssel des Betreibers.
- Offizielle Carrier-/Paketshop-Schnittstellen, insbesondere Hermes als Pflichtanforderung für den Pilot.
- Freigabe der Verarbeitung echter Kunden- und Sendungsdaten.

Es werden keine WLAN-Passwörter, Carrier-Passwörter, privaten Schlüssel oder API-Secrets in GitHub, App-Code oder Chat hinterlegt.

## 7. Ehrlicher Status

**Vorbereitet:** Android-App, lokale Erfassung, Kamera-/manuelle Eingabe, DataWedge-Empfangscode, WLAN-Statusanzeige des aktuellen Geräts, Hardwareinventar, DataWedge-Konfigurationsanleitung und dieser Startplan. Die aktuelle App läuft lokal; WLAN schaltet keine noch nicht vorhandene Cloud-Synchronisierung frei. Der Betreiber hat bestätigt, dass alle vier Zebra-Handhelds mit WLAN verbunden sind. Das bestätigt noch nicht dieselbe SSID/dasselbe Subnetz oder die gegenseitige Erreichbarkeit.

**Noch nicht vor Ort verifiziert:** WLAN-Zugang, DataWedge-Profil, echter TC26-Scan, Hermes-Anbindung, zentrale Synchronisierung und produktive Freigabe.

Ein erfolgreicher CI-Build ist kein Ersatz für den Gerätetest im Paketshop.
