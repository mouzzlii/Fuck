# TRAKUNA – Betriebshandbuch

Stand: App-Version 1.45 (versionCode 46) · CI erfolgreich: Lauf 45231195765

Dieses Handbuch erklärt die Bedienung der aktuellen TRAKUNA-Testversion. Es richtet sich an Paketshop-Mitarbeiter ohne technische Vorkenntnisse.

## 1. Wichtiger Status vor dem ersten Einsatz

TRAKUNA 1.45 ist eine **lokale Entwicklungs- und Praxistestversion**, noch keine freigegebene Produktivsoftware.

- Sendungsdaten liegen nur auf diesem Android-Gerät. Es gibt keine zentrale Synchronisierung zwischen mehreren Geräten.
- Es gibt noch keine Mitarbeiter-Anmeldung, Benutzerrollen oder persönliche Zuordnung von Bearbeitern.
- Eine ausgewählte Partner-App wird geöffnet und erhält einen Übergabeversuch. Eine tatsächliche Buchung in der Partner-Software ist damit nicht bestätigt.
- Die Paketdienst-Erkennung ist nur ein Hinweis. Nummer und Paketdienst müssen geprüft werden.
- Maximal 500 Vorgänge können lokal gespeichert werden.
- JSON- und CSV-Dateien enthalten vertrauliche Sendungs- und Empfängerdaten unverschlüsselt.
- Das vorhandene Hermes-Handgerät MC3090BT ist kein kompatibles Android-Gerät für TRAKUNA. Es bleibt für den bisherigen Hermes-Ablauf bestehen; TRAKUNA muss für Hermes auf einem separaten, freigegebenen Android-Gerät laufen.
- Vor echtem Betrieb sind Zebra-Gerätetests, echte Etikettentests, Partnerabnahme, Datenschutzprüfung und ein signiertes Produktionsrelease erforderlich.

Bitte zuerst ausschließlich mit Testsendungen arbeiten.

## 2. Aktuelles Testpaket herunterladen

1. Den [erfolgreichen Android-Build 45231195765 öffnen](https://github.com/mouzzlii/Fuck/actions/runs/45231195765).
2. Im Build den Bereich **Artifacts** öffnen.
3. Das Paket **TRAKUNA-Android** herunterladen.
4. Das ZIP auf dem Android-Gerät entpacken.
5. Die enthaltene Datei `app-debug.apk` öffnen und installieren.
6. Android kann eine einmalige Freigabe für die Installation aus dieser Datei-App verlangen. Nur für die vertrauenswürdige Datei-App freigeben und die Freigabe danach wieder deaktivieren.
7. Die App starten und zunächst die Tests aus `PRAxisTEST.md` durchführen.

**Aktuelles Artefakt Version 1.45:** APK-SHA-256: `989ee18003ab6c3e0768400e259118f5dbd3abae5125929e9488b5dcbc550743`. Das ZIP-Artefakt ist bis 07.12.2026, 17:48 UTC verfügbar. Es handelt sich um ein Debug-Testpaket, nicht um ein signiertes Produktionsrelease.

## 3. Sicher aktualisieren – vorhandene Daten schützen

**Wichtig: TRAKUNA nicht zuerst deinstallieren.** Die Sendungsdaten liegen lokal auf dem Gerät. Eine Deinstallation kann diese Daten und den dazugehörigen Android-Keystore-Schlüssel löschen.

Vor jedem Update:

1. TRAKUNA öffnen und **Vollständige Datensicherung erstellen** wählen.
2. Die JSON-Sicherung an einem geschützten Ort speichern. Sie enthält Sendungsnummern, Empfängernamen und Notizen im Klartext. Nicht öffentlich teilen und nicht ungeschützt weiterleiten.
3. Prüfen, dass die Sicherungsdatei tatsächlich vorhanden ist und sich am gewählten Ort wiederfinden lässt.
4. Erst danach das neue APK installieren. Wenn Android eine Aktualisierung anbietet, die vorhandene App aktualisieren und nicht deinstallieren.
5. Nach dem Update TRAKUNA öffnen und einige vorhandene Vorgänge kontrollieren.

**Wenn Android meldet „App nicht installiert“, „Signatur stimmt nicht überein“ oder eine ähnliche Meldung:** Installation abbrechen. Die bestehende App nicht löschen und keine Daten löschen. Debug-Testpakete aus verschiedenen Build-Läufen können unterschiedlich signiert sein. In diesem Fall muss zuerst ein Update mit passender Signatur bereitgestellt oder ein sicherer Datenexport und Wiederherstellungsweg vorbereitet werden.

Eine erfolgreiche Installation ist noch kein Funktionsnachweis. Nach dem Update mindestens einen Testvorgang mit einer ausdrücklich erfundenen Testnummer durchführen und danach wieder entfernen. Keine echte Kundensendung für einen Funktionstest verwenden.

## 4. Weitere Unterlagen

- [Pilotpaket für den ersten Paketshop](PILOT-PAKETSHOP.md)
- [Praxis-Testplan](PRAxisTEST.md)
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md)
- [Partner-Schnittstellenvertrag](PARTNER_INTEGRATION.md)
- [Sicherheitsstatus](SECURITY.md)

Die restlichen Kapitel dieses Handbuchs zur Sendungserfassung, den vier Abläufen, DataWedge und Datensicherung bleiben gültig.
