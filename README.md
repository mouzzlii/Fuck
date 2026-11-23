# TRAKUNA – Android Scan-Zentrale

TRAKUNA 1.45 ist eine lokale Android-App zur Erfassung von Paket-Barcodes und zur Unterstützung einfacher Paketshop-Abläufe.

## Aktueller Entwicklungsstand – Version 1.45 (versionCode 46)

- Neue direkte Carrier-Auswahl für die vier Start-Prioritäten: **DPD, GLS, Hermes und UPS**.
- Der Auswahlknopf kennzeichnet den lokalen Vorgang; er führt keine externe Buchung aus.
- CI-Prüfung für Version 1.45: [Android-Workflow öffnen](https://github.com/mouzzlii/Fuck/actions/workflows/android.yml). Der Build muss erfolgreich abgeschlossen sein, bevor das neue APK verteilt wird.
- Der letzte nachweislich erfolgreiche APK-Build ist Version 1.44 (versionCode 45), CI-Lauf 45227305052. Er enthält den neuen Carrier-Auswahlknopf noch nicht.

**Wichtig:** Die letzte verfügbare Version 1.44 ist ein Debug-Testpaket, kein produktiv signiertes Release. Vor einem Update vorhandene Daten sichern und die App nicht deinstallieren. Ein erfolgreicher CI-Build ersetzt keinen Test auf den tatsächlichen Shopgeräten.

## Aktuelle Betriebsgrenzen

- Die Daten werden lokal auf diesem Gerät gespeichert und verschlüsselt.
- Es gibt derzeit keine zentrale Synchronisierung zwischen mehreren Geräten und keine Cloud-Verbindung.
- WLAN ist für den lokalen Kamera-/DataWedge-Scan nicht erforderlich und aktiviert keine Synchronisierung.
- DataWedge-Empfangscode ist vorhanden; das Profil muss auf dem jeweiligen Zebra-Gerät eingerichtet und vor Ort getestet werden.
- Hermes ist als Paketdienst manuell auswählbar; eine offizielle Hermes-Buchungs-/Paketshop-Schnittstelle ist noch nicht umgesetzt. Hermes bleibt Pflicht-Blocker für den vorgesehenen Pilot.
- Die vorhandenen GLS-/DPD-/Hermes-Systeme und Druckerkopplungen dürfen nicht verändert werden.

## Release-Build

Der CI-Workflow kann zusätzlich ein signiertes Release-APK erstellen, sobald der Betreiber die vier Signierungs-Secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` und `ANDROID_KEY_PASSWORD` in den GitHub-Repository-Secrets hinterlegt hat. Der private Keystore bleibt außerhalb des Repositorys. Ohne diese Betreiber-Schlüssel wird bewusst nur die Debug-Testversion veröffentlicht.

## Integrations- und Sicherheitsdokumentation

- [Hardwareinventar des ersten Paketshops](HARDWARE-INVENTAR.md): vor Ort erfasste GLS-/DPD-/Hermes-Geräte, Verbindungen, Versionsangaben und offene Prüfpunkte.
- [WLAN- und Startvorbereitung](WLAN-STARTVORBEREITUNG.md): WLAN-Verbindung, DataWedge-Einrichtung, sicherer Start und Abnahmetest.
- [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md): aktueller Build, Testablauf, Zielarchitektur, Integrationsvoraussetzungen und Produktionssperren.
- [Partner-Schnittstelle und Abnahmekriterien](PARTNER_INTEGRATION.md): beschreibt den aktuellen Android-Übergabeversuch, den notwendigen Vertrag mit dem Softwareanbieter und die Tests für eine echte Integration.
- [Sicherheitsstatus und Produktionsfreigabe](SECURITY.md): dokumentiert umgesetzte Schutzmaßnahmen, bekannte Restrisiken und verbindliche Freigabekriterien.
- [Betriebshandbuch](BETRIEBSHANDBUCH.md): einfache Schritt-für-Schritt-Anleitung für Paketannahme, Kundenausgabe, Retouren, Zebra DataWedge, Partner-App und Datensicherung.
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md): verbindliche Nachweise und externe Voraussetzungen vor einem echten Paketshop-Einsatz.
- [Praxis-Testplan](PRAxisTEST.md): Testfälle für den kontrollierten Gerätetest.

## Neu in Version 1.44

- Kamera-Scans werden über mehrere aufeinanderfolgende Bilder gesammelt. Wenn in diesem Bestätigungsfenster mehrere unterschiedliche Sendungsnummern erkannt werden, wird keine davon automatisch ausgewählt.
- QR-Links werden vorsichtiger ausgewertet: allgemeine IDs und beliebige URL-Pfadnummern reichen nicht mehr aus; mehrdeutige Treffer werden abgelehnt.
- Sendungsnummern aus Etikettentexten werden nur bei eindeutig erkannten Formaten extrahiert. Ein gültig aussehender Teil innerhalb eines längeren Codes wird nicht als eigene Nummer übernommen.
- Paketdienstnamen werden bei bekannten manuellen Eingaben vereinheitlicht. Die DHL-Erkennung verlangt jetzt ein vollständiges bekanntes Muster; unklare JD-Präfixe werden nicht mehr automatisch als DHL eingestuft.
- Dieselbe aktive Sendungsnummer kann nicht mehr versehentlich in mehreren Vorgangsarten angelegt werden. Die Rückmeldung zeigt die bereits vorhandene Vorgangsart.
- Kamera-Barcodes verwenden den Anzeigewert als Rückfall, wenn der Rohwert leer ist.
