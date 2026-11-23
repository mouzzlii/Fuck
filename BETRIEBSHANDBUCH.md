# TRAKUNA – Betriebshandbuch

Stand: App-Version 1.47 (versionCode 48) · CI erfolgreich: Lauf 45248166890

Dieses Handbuch erklärt die lokale Testversion Schritt für Schritt. TRAKUNA ist noch keine freigegebene Produktivsoftware. Ausschließlich mit ausdrücklich freigegebenen Testsendungen bzw. erfundenen Testnummern arbeiten.

## 1. Aktuelles Testpaket installieren

1. Den [erfolgreichen Android-Build 45248166890 öffnen](https://github.com/mouzzlii/Fuck/actions/runs/45248166890).
2. Im Abschnitt **Artifacts** das Paket **TRAKUNA-Android** herunterladen.
3. Das ZIP entpacken und die enthaltene Datei `app-debug.apk` auf das Android-Gerät übertragen.
4. Die APK öffnen und installieren. Android kann eine einmalige Erlaubnis für die Installation aus dieser Datei-App verlangen. Nur der vertrauenswürdigen Datei-App diese Erlaubnis geben und danach wieder deaktivieren.
5. TRAKUNA starten und zuerst mit erfundenen Testdaten prüfen.

**Prüfsummen des bereitgestellten Testpakets**

- APK-SHA-256: `6ac5eedac4bc130da3cb001f8bf3388b29dc2319fa39581b5031db5fbf968463`
- ZIP-SHA-256: `3191ae35b86a88f7f677a837ae2dd63c2bc954088724ac6339c1ba7a6d17bc7f`
- Erstellt: 23.11.2026, 19:23 UTC
- Artefakt verfügbar bis: 07.12.2026, 19:23 UTC

Es ist ein Debug-Testpaket, kein signiertes Produktionsrelease.

## 2. Vorhandene Daten bei Updates schützen

**TRAKUNA vor einem Update nicht deinstallieren.** Lokale Sendungsdaten und der zugehörige Android-Keystore-Schlüssel können bei einer Deinstallation verloren gehen.

1. In TRAKUNA eine vollständige JSON-Datensicherung erstellen.
2. Die Datei an einem geschützten Ort ablegen. Sie enthält Sendungsnummern, Empfängernamen und Notizen im Klartext.
3. Prüfen, ob die Sicherungsdatei tatsächlich vorhanden ist.
4. Erst dann das neue APK installieren und – wenn Android es anbietet – die bestehende App aktualisieren, nicht deinstallieren.
5. Nach dem Update einige vorhandene Testvorgänge kontrollieren.

Wenn Android eine abweichende Signatur meldet, Installation abbrechen. Die bestehende App nicht löschen und keine App-Daten entfernen. Debug-Builds können unterschiedliche Signaturen besitzen.

## 3. DPD, GLS, Hermes und UPS getrennt zuordnen

In Version 1.47 kann jeder der vier Start-Paketdienste einer eigenen installierten Betriebssoftware zugeordnet werden. Die Zuordnung wird lokal auf diesem Android-Gerät gespeichert.

1. TRAKUNA öffnen.
2. **Betriebssoftware pro Paketdienst zuordnen** antippen.
3. Im Auswahlfeld den Paketdienst wählen: **DPD**, **GLS**, **Hermes** oder **UPS**.
4. Die tatsächlich zu diesem Paketdienst gehörende, installierte App auswählen.
5. Falls sie nicht in der ersten Liste erscheint, **Alle installierten Apps anzeigen** antippen.
6. Die Anzeige unter der Hauptschaltfläche kontrollieren.
7. Für die anderen drei Paketdienste wiederholen.

Wichtig:

- Nur Apps auswählen, die der Betreiber bzw. Paketdienst für diesen Standort freigegeben hat.
- Keine App nur anhand eines ähnlich klingenden Namens zuordnen.
- Sobald mindestens eine Einzelzuordnung eingerichtet ist, wird für einen anderen Paketdienst ohne eigene Zuordnung keine fremde Carrier-App als Ersatz geöffnet.
- Eine Zuordnung öffnet später nur die ausgewählte App. Sie beweist nicht, dass diese App die Sendungsnummer übernommen oder eine Buchung ausgeführt hat.
- Die Zuordnung ist gerätebezogen. Sie wird nicht mit anderen Handgeräten synchronisiert.
- Eine Zuordnung kann über die Schaltfläche **Zuordnung für [Paketdienst] entfernen** wieder gelöscht werden.

## 4. Sendungsnummer erfassen und übergeben

1. Den passenden Paketdienst in der Hauptmaske auswählen.
2. Die Sendungsnummer entweder manuell eingeben oder – bei eingerichteter Kamera – scannen.
3. Die angezeigte Nummer und den Paketdienst vor jeder weiteren Aktion kontrollieren.
4. **An Partner-App übergeben** antippen.
5. TRAKUNA speichert die Übergabe lokal verschlüsselt und versucht, die für diesen Paketdienst zugeordnete App zu öffnen.
6. Falls keine App zugeordnet oder installiert ist, bleibt die Nummer im TRAKUNA-Cache; gegebenenfalls wird sie zusätzlich in die Zwischenablage kopiert.

**Nicht als Buchungserfolg werten:** Das Öffnen einer Partner-App ist keine Bestätigung, dass diese die Nummer empfangen, validiert oder einen Vorgang angelegt hat. TRAKUNA besitzt derzeit keine bestätigte DPD-, GLS-, Hermes- oder UPS-Buchungsschnittstelle.

## 5. Lokale Paketshop-Vorgänge

Über **Paketshop-Sendungen verwalten** können Testvorgänge lokal erfasst und ihre Status fortgeschrieben werden:

- Sendung für einen Kunden annehmen und als im Shop eingetroffen führen.
- Eine Sendung als abholbereit markieren und lokal an den Kunden übergeben.
- Eine Kundenabgabe bzw. Retoure erfassen und den lokalen Ablauf bis zur Übergabe an den Paketdienst fortschreiben.

Die lokalen Status ersetzen keine Buchung oder Bestätigung im Carrier-System. Vorhandene Carrier-Programme bleiben führend.

## 6. Zebra TC26 und DataWedge (DPD/GLS)

Die DPD- und GLS-Handgeräte wurden anhand der bisherigen Angaben als Zebra TC26 vermutet; das genaue Modell muss am Typenschild bestätigt werden.

Für einen TRAKUNA-Scan auf einem Android-Zebra-Gerät ist ein eigenes DataWedge-Profil erforderlich. Die bestehende DPD-/GLS-Konfiguration nicht überschreiben. Das Profil muss TRAKUNA zugeordnet sein und den Intent-Eingang mit der Action `de.trakuna.app.DATAWEDGE_SCAN` sowie dem Extra `com.symbol.datawedge.data_string` verwenden. Die vollständige Anleitung und die Sicherheitshinweise stehen in [PARTNER_INTEGRATION.md](PARTNER_INTEGRATION.md).

Ein Scan wird nur verarbeitet, solange TRAKUNA im Vordergrund aktiv ist. Die DataWedge-Konfiguration und der echte Scan-Empfang sind auf den Shopgeräten noch nicht vor Ort abgenommen.

## 7. Hermes – vorhandenes MC3090BT unverändert lassen

Das gemeldete Hermes-Handgerät ist ein Zebra/Symbol MC3090BT. Zebra führt diese Baureihe mit Windows CE 5.0 bzw. Windows Mobile 6.1; sie kann die Android-App TRAKUNA nicht ausführen. Das MC3090BT und der vorhandene Hermes-Drucker bleiben für den bisherigen Hermes-Ablauf unangetastet.

Für einen TRAKUNA-Hermes-Test ist ein separates, vom Betreiber und Hermes freigegebenes Android-Gerät erforderlich. Bis eine offizielle Schnittstelle bestätigt ist, kann die Sendungsnummer dort nur manuell als lokaler Testvorgang erfasst werden.

## 8. UPS – App-Anforderung beachten

Die offizielle App **UPS Access Point** ist für registrierte und autorisierte UPS-Access-Point-Standorte vorgesehen. Die Google-Play-Angabe nennt Android 9.0 oder höher und den Paketnamen `com.ups.accesspoint`. Wenn die UPS-App auf demselben Gerät wie TRAKUNA laufen soll, muss dieses Gerät daher mindestens Android 9 verwenden. TRAKUNA selbst hat eine niedrigere Mindestversion; die UPS-App bestimmt hier die höhere Anforderung.

Die UPS-App nicht eigenmächtig installieren oder konfigurieren. Vorhandene App-Version, Gerät und Standortfreigabe müssen vom Betreiber bestätigt werden.

## 9. Datensicherung und Datenschutz

- Lokale Daten liegen nur auf diesem Gerät; es gibt keine zentrale Synchronisierung und keine Cloud-Verbindung.
- Es gibt keine Mitarbeiterkonten oder Rollen.
- Maximal 500 Vorgänge werden lokal gespeichert.
- JSON-Backups und CSV-Exporte enthalten vertrauliche Daten im Klartext. Nur geschützt speichern und nur an berechtigte Personen weitergeben.
- Keine echten Kundendaten in öffentliche Screenshots, GitHub-Issues oder Chat-Nachrichten aufnehmen.
- Vor dem produktiven Einsatz sind Datenschutzprüfung, Mehrgeräte-/Mitarbeitermodell, Backup-Wiederherstellung und signiertes Produktionsrelease erforderlich.

## 10. Verbindliche Unterlagen

- [Pilotpaket und Produktionssperren](PILOT-PAKETSHOP.md)
- [Praxis-Testplan für DPD, GLS, Hermes und UPS](PRAxisTEST.md)
- [Freigabe-Checkliste](FREIGABE-CHECKLISTE.md)
- [Carrier-Integrationsrecherche](CARRIER-INTEGRATIONS.md)
- [Partner-Schnittstellenvertrag](PARTNER_INTEGRATION.md)
- [Sicherheitsstatus](SECURITY.md)
