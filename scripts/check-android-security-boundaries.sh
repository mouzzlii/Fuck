#!/usr/bin/env bash
set -euo pipefail

mode="${1:-source}"
source_manifest="app/src/main/AndroidManifest.xml"
packaged_manifest="app/build/intermediates/packaged_manifests/debug/processDebugManifestForPackage/AndroidManifest.xml"

python3 - "$mode" "$source_manifest" "$packaged_manifest" <<'PY'
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

mode, source_manifest, packaged_manifest = sys.argv[1:4]
android = "{http://schemas.android.com/apk/res/android}"
tools = "{http://schemas.android.com/tools}"
internet = "android.permission.INTERNET"
# Google Code Scanner is hosted by Google Play services; TRAKUNA must not request CAMERA.
allowed_source_permissions = {
    "android.permission.ACCESS_NETWORK_STATE",
}

def fail(message: str) -> None:
    print(f"FEHLER: {message}", file=sys.stderr)
    raise SystemExit(1)

def read_manifest(path: str):
    manifest = Path(path)
    if not manifest.is_file():
        fail(f"Manifest fehlt: {path}")
    try:
        return ET.parse(manifest).getroot()
    except ET.ParseError as exc:
        fail(f"Manifest ist kein gültiges XML ({path}): {exc}")

def app_element(root):
    app = root.find("application")
    if app is None:
        fail("Manifest enthält kein application-Element.")
    return app

def check_source_manifest() -> None:
    root = read_manifest(source_manifest)
    permissions = root.findall("uses-permission")
    names = {node.attrib.get(android + "name") for node in permissions}
    names.discard(None)

    unexpected = names - allowed_source_permissions - {internet}
    if unexpected:
        fail("Nicht freigegebene Quellberechtigungen: " + ", ".join(sorted(unexpected)))

    for node in permissions:
        name = node.attrib.get(android + "name")
        remove_rule = node.attrib.get(tools + "node")
        if name == internet and remove_rule != "remove":
            fail("INTERNET darf nur mit tools:node=\"remove\" im Quellmanifest stehen.")
        if name in allowed_source_permissions and remove_rule is not None:
            fail(f"Unerwartete tools:node-Regel für {name}.")

    missing = allowed_source_permissions - names
    if missing:
        fail("Erforderliche dokumentierte Berechtigungen fehlen: " + ", ".join(sorted(missing)))
    if internet not in names:
        fail("Explizite INTERNET-Entfernungsregel für die ML-Kit-Transport-Abhängigkeit fehlt.")

    app = app_element(root)
    if app.attrib.get(android + "allowBackup") != "false":
        fail('android:allowBackup muss ausdrücklich "false" sein.')
    if app.attrib.get(android + "usesCleartextTraffic") != "false":
        fail('android:usesCleartextTraffic muss ausdrücklich "false" sein.')

    print("OK: Quellmanifest enthält keine Kamera-Berechtigung; INTERNET wird entfernt, Backup/Klartextverkehr sind deaktiviert.")

def check_declared_dependencies_and_sources() -> None:
    gradle_file = Path("app/build.gradle.kts")
    if not gradle_file.is_file():
        fail("app/build.gradle.kts fehlt.")
    gradle_text = gradle_file.read_text(encoding="utf-8")
    forbidden_dependencies = [
        r"com\.squareup\.okhttp3:okhttp",
        r"com\.squareup\.retrofit2:retrofit",
        r"io\.ktor:ktor-client",
        r"com\.android\.volley:volley",
        r"org\.chromium\.net:cronet",
        r"io\.socket:socket\.io-client",
        r"io\.grpc:grpc-okhttp",
    ]
    for pattern in forbidden_dependencies:
        if re.search(pattern, gradle_text, re.IGNORECASE):
            fail(f"Direkte Netzwerk-Client-Abhängigkeit ist nicht freigegeben: {pattern}")

    forbidden_apis = [
        "okhttp3.okhttpclient",
        "retrofit2.retrofit",
        "io.ktor.client",
        "org.chromium.net.cronetengine",
        "com.android.volley.requestqueue",
        "io.socket.client.io",
        "java.net.http.httpclient",
        "java.net.httpurlconnection",
        "java.net.socket",
    ]
    source_root = Path("app/src/main/java")
    if source_root.exists():
        for source in source_root.rglob("*"):
            if source.suffix not in {".kt", ".java"}:
                continue
            content = source.read_text(encoding="utf-8").lower()
            for api in forbidden_apis:
                if api in content:
                    fail(f"Nicht freigegebene Netzwerk-API {api} in {source}")

    print("OK: Keine direkte HTTP-/Socket-Client-Abhängigkeit oder entsprechende Netzwerk-API im App-Quellcode deklariert.")

def check_packaged_manifest() -> None:
    root = read_manifest(packaged_manifest)
    permissions = {
        node.attrib.get(android + "name")
        for node in root.findall("uses-permission")
    }
    if internet in permissions:
        fail("Das tatsächlich paketierte Manifest fordert INTERNET an.")
    app = app_element(root)
    if app.attrib.get(android + "allowBackup") != "false":
        fail('Das paketierte Manifest muss android:allowBackup="false" enthalten.')
    if app.attrib.get(android + "usesCleartextTraffic") != "false":
        fail('Das paketierte Manifest muss android:usesCleartextTraffic="false" enthalten.')
    print("OK: Paketmanifest enthält kein INTERNET und erzwingt allowBackup=false sowie usesCleartextTraffic=false.")

if mode == "source":
    check_source_manifest()
    check_declared_dependencies_and_sources()
elif mode == "merged":
    check_declared_dependencies_and_sources()
    check_packaged_manifest()
else:
    fail("Aufruf: check-android-security-boundaries.sh [source|merged]")
PY
