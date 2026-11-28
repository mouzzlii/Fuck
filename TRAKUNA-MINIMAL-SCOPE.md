# TRAKUNA – Minimaler Produktumfang

**Stand:** 27.11.2026  
**Geltung:** Verbindliche Produkt- und UX-Leitlinie für die weitere Reduktion von TRAKUNA.  
**Leitentscheidung:** TRAKUNA muss den Paketshop-Alltag vereinfachen. Die App wird auf die wenigen notwendigen Kernabläufe reduziert. Zusätzliche Funktionen sind kein Selbstzweck.

## 1. Der eigentliche Zweck

TRAKUNA wird für den Paketshop des Betreibers benötigt. Der wichtigste Nutzen ist, Paket-Barcodes schnell und zuverlässig zu erfassen und die passende Paketshop-Aktion ohne unnötige manuelle Eingaben auszuführen.

Die Bedienung soll primär über Scanner erfolgen. Ein Scan soll unmittelbar in die Sendungserfassung führen – nicht erst in Diagnoseansichten, Einstellungen oder eine Folge unnötiger Dialoge.

**Pflicht:** Hermes gehört zum tatsächlichen Paketshop-Betrieb und darf nicht als optionaler Anbieter behandelt werden. DPD, GLS und UPS gehören ebenfalls zum vorgesehenen Startumfang, soweit die jeweilige Erkennung/Zuordnung verlässlich möglich ist.

## 2. Unverzichtbarer Kernumfang

1. **Scannen:** QR-Codes und Strichcodes mit dem nativen Google Code Scanner aus Google Play Services erfassen (play-services-code-scanner), einschließlich Autofokus/Auto-Zoom. Keine eigene CameraX-/ML-Kit-Kameraoberfläche und keine CAMERA-Berechtigung in TRAKUNA. Vorhandene kompatible Zebra-Scanner müssen als Eingabeweg berücksichtigt werden; ein Zebra-/DataWedge-Scan soll denselben Erfassungsablauf auslösen wie ein Kamerascan.
2. **Direkte Erfassung:** Der erkannte Wert wird ohne Umwege in den Paketvorgang übernommen. Manuelle Eingabe bleibt nur als zuverlässiger Rückfallweg bei Scanproblemen.
3. **Anbieter erkennen:** Paketdienst automatisch erkennen, wenn die Barcode-/QR-Daten eine eindeutige und geprüfte Zuordnung erlauben. Hermes ist Pflicht. Bei Unsicherheit nicht raten, sondern eine kurze manuelle Auswahl anbieten.
4. **Paketshop-Aktionen:** Die App muss die tatsächlichen Abläufe abbilden:
   - Paket für einen Kunden annehmen und zur Abholung führen;
   - Paket an den Kunden ausgeben;
   - Sendung vom Kunden annehmen;
   - Retoure vom Kunden annehmen;
   - Übergabe an Fahrer/Paketdienst lokal dokumentieren.
5. **Aktive Vorgänge:** Eine einfache Liste zeigt offene Vorgänge mit Sendungsnummer, Paketdienst, verständlichem Status und – nur wenn wirklich nötig – Empfänger. Suchen muss schnell und unkompliziert sein.
6. **Status korrekt führen:** Ein Vorgang darf mit möglichst wenigen klaren Aktionen zum nächsten passenden Status gebracht werden. Eine lokale Statusänderung darf niemals als bestätigte Carrier-Buchung oder externe Quittung ausgegeben werden.
7. **Datenschutz und Datenbestand:** Lokale Sendungsdaten bleiben geschützt und verschlüsselt. Bestehende Daten dürfen durch Vereinfachungen nicht verloren gehen. Eine sichere, verständliche Datensicherung/Wiederherstellung bleibt als Wartungsfunktion verfügbar, nicht als Teil des täglichen Erfassungsablaufs.
8. **Sicherer Testbetrieb:** Der aktuell festgelegte Shadow-/Testmodus bleibt aktiv, bis die erforderlichen realen Freigaben und Abnahmen vorliegen. Keine echten Buchungen, produktiven Versandlabels oder produktiven Druckaufträge.

## 3. Zielablauf – so wenig Schritte wie möglich

**Scan → Vorgang auswählen → speichern → fertig.**

- Der Scan durch Kamera oder kompatiblen Zebra-Scanner füllt die Sendungsnummer direkt aus.
- Der Paketdienst wird automatisch gesetzt, wenn sicher erkannt; sonst genügt eine kurze Auswahl.
- Es erscheinen nur die für diesen Vorgang notwendigen Aktionen.
- Empfängername und Notiz sind keine Pflichtfelder und dürfen den Scan-/Erfassungsweg nicht aufhalten.
- Der gespeicherte Vorgang erscheint unmittelbar in der aktiven Liste.
- Folgeaktionen wie Kundenausgabe oder Fahrerübergabe erfolgen direkt am betroffenen Vorgang mit einer klaren Statusaktion.

Keine automatische Navigation in Browser oder Carrier-Portale unmittelbar nach jedem Scan, wenn sie den Erfassungsablauf unterbricht. Ein externer Portalaufruf darf nur eine bewusst gewählte, klar beschriftete Aktion sein und darf keine Übertragung/Buchung suggerieren.

## 4. Bedienoberfläche auf das Minimum reduzieren

Die Hauptansicht soll im Alltag nur enthalten:

- eine dominante Schaltfläche **„Scannen“**;
- ein Sendungsnummernfeld als Rückfallweg;
- die nötige Anbieteranzeige/-auswahl, möglichst automatisch;
- die je nach Vorgang notwendigen Aktionen in klarer, verständlicher Sprache;
- einen direkten Zugang zu **„Offene Sendungen“**;
- eine knappe Rückmeldung, ob der Vorgang erfolgreich gespeichert wurde.

Die offene Sendungsliste soll pro Eintrag nur die wesentlichen Informationen und die gerade zulässige nächste Aktion zeigen. Seltene Wartungs- und Supportfunktionen gehören in einen getrennten, unaufdringlichen Bereich.

## 5. Aus dem täglichen Ablauf entfernen oder zurückstellen

Diese Funktionen dürfen den Kernablauf nicht länger, unübersichtlicher oder fehleranfälliger machen:

- Scan-Diagnose, Rohbarcode-Anzeige und Diagnose-Kopierfunktion: nur für gezielte Supportfälle, nicht im normalen Mitarbeiterablauf;
- WLAN-Statusanzeige: entfernen, solange sie keine konkrete tägliche Aktion ermöglicht;
- CSV-Export: zurückstellen; er ist für den Kernablauf nicht erforderlich und erzeugt unverschlüsselte Datenkopien;
- Archivverwaltung, Wiederherstellen und endgültiges Löschen: aus der normalen Arbeitsansicht entfernen; Aufbewahrungs-/Löschregeln zuerst klären;
- ausführliche Vorgangshistorie und Bearbeitung optionaler Notizen: aus der Standardansicht entfernen;
- manuelle Portal-Konfiguration und Portalverwaltung: nicht in den normalen Scan-/Erfassungsablauf mischen;
- Teilen von Shadow-Testprotokollen: Supportfunktion, nicht Mitarbeiterfunktion;
- gespeicherte Übergabe wiederaufnehmen und Cache-Verwaltung: nur behalten, wenn ein realer, notwendiger Arbeitsablauf dies nachweislich verlangt;
- zusätzliche Carrier-Apps, Integrationen, Rollen, Synchronisierung oder Automationen: nicht ergänzen, solange kein konkreter Bedarf und keine autorisierte technische Grundlage vorliegen.

**Wichtig:** „Zurückstellen“ bedeutet nicht, dass bestehende Daten oder Sicherheitsmechanismen ungeprüft gelöscht werden. Vor jedem Entfernen Codeabhängigkeiten, Datenmigration, Rückwärtskompatibilität, Wiederherstellbarkeit und Tests prüfen. Bestehende Daten und Backup-Wiederherstellung haben Vorrang.

## 6. Hardware- und Integrationsgrenzen

- Der Paketshop verwendet Zebra-Scanner. Der bestehende Zebra-/DataWedge-Eingang ist daher anhand der tatsächlich vorhandenen Geräte zu bewerten und darf nicht allein aufgrund einer früheren Dokumentationsnotiz als bedeutungsloser Legacy-Code entfernt werden.
- Für Hermes wurde ein Zebra/Symbol MC3090BT (Bluetooth, älteres Gerät) genannt. Seine technische Kompatibilität mit der aktuellen Android-App ist nicht belegt. Bestehendes Hermes-System und Gerät nicht verändern; zunächst nur Kompatibilität und freigegebene Integrationsmöglichkeiten feststellen.
- Genannte Etikettendrucker: Zebra CD421 (GLS) und Zebra CD420 (DPD). Ein Hermes-Handgerät und Hermes-Drucker wurden ebenfalls erwähnt, das genaue Druckermodell/der Anschluss ist nicht belegt.
- Ein Druckweg ist derzeit nicht als umgesetzt oder real getestet zu behandeln. Für den geschlossenen technischen Shop-Test sind echte Versandlabels, echte Drucke und produktive Druckaufträge ausdrücklich ausgeschlossen.
- Keine Carrier-API, Partnerfreigabe, Portal-Datenübertragung oder externe Buchung behaupten, solange sie nicht tatsächlich autorisiert, implementiert und getestet ist.

## 7. Nicht-Ziele der Minimalversion

Nicht Teil des einfachen Kernprodukts sind derzeit:
- Cloud-/Mehrgeräte-Synchronisierung und Mitarbeiterkonten;
- ein eigenes umfangreiches Archiv- oder Berichtssystem;
- ein allgemeines Diagnose-/Monitoring-Dashboard;
- ein frei konfigurierbares Integrations- oder Automationssystem;
- eigenständige Carrier-Buchungen ohne autorisierte Schnittstelle;
- produktive Labelbestellung oder produktiver Druck.

Diese Themen können nur nach einem konkreten betrieblichen Bedarf und separater Sicherheits-/Technikprüfung wieder aufgenommen werden.

## 8. Abnahmekriterien für jede Vereinfachung

Eine Änderung ist nur dann eine Verbesserung, wenn:

- ein Mitarbeiter den Kernvorgang mit weniger Schritten und weniger Entscheidungen erledigt;
- Kamera- und kompatible Zebra-Scans denselben verlässlichen Erfassungsweg nutzen;
- Hermes als Pflichtanbieter berücksichtigt wird und unbekannte Werte nicht falsch zugeordnet werden;
- Annahme, Ausgabe, Kundensendung, Retoure und Fahrerübergabe korrekt unterscheidbar bleiben;
- kein bestehender Datensatz verloren geht oder unbemerkt verändert wird;
- der Testmodus weiterhin echte externe Aktionen verhindert;
- relevante Tests für den exakten Commit erfolgreich sind.

Bei jeder neuen Funktion ist zuerst zu begründen, warum sie für den täglichen Paketshop-Ablauf unverzichtbar ist. Ohne diesen Nachweis wird sie nicht gebaut.

---

**Produktmaßstab:** Nicht möglichst viele Funktionen. Nicht möglichst viel Technik. Sondern: Ein Paketshop-Mitarbeiter scannt, wählt den richtigen Vorgang, speichert – und kann sofort weiterarbeiten.
