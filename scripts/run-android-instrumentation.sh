#!/usr/bin/env bash
set -uo pipefail

report_dir="app/build/reports/trakuna-instrumentation"
mkdir -p "$report_dir"

gradle_log="$report_dir/gradle-instrumentation.log"
logcat_file="$report_dir/emulator-logcat.txt"
diagnostics_file="$report_dir/device-diagnostics.txt"

capture_diagnostics() {
  {
    echo "=== UTC timestamp ==="
    date -u +"%Y-%m-%dT%H:%M:%SZ"
    echo
    echo "=== adb devices ==="
    adb devices -l || true
    echo
    echo "=== instrumentation registrations ==="
    adb shell pm list instrumentation || true
    echo
    echo "=== package installation ==="
    adb shell pm list packages | grep -E 'de\\.trakuna\\.app|androidx\\.test' || true
    echo
    echo "=== boot and memory properties ==="
    adb shell getprop sys.boot_completed || true
    adb shell getprop ro.build.version.sdk || true
    adb shell cat /proc/meminfo | head -n 8 || true
  } > "$diagnostics_file" 2>&1
  adb logcat -d -v threadtime > "$logcat_file" 2>&1 || true
}

echo "ADB-Emulator-Verbindung prüfen."
adb start-server
adb wait-for-device

state=""
for attempt in $(seq 1 60); do
  state="$(adb get-state 2>/dev/null || true)"
  if [[ "$state" == "device" ]]; then
    break
  fi
  if [[ "$state" == "offline" ]]; then
    adb reconnect offline >/dev/null 2>&1 || true
  fi
  sleep 2
done

if [[ "$state" != "device" ]]; then
  echo "Emulator wurde nicht innerhalb von 120 Sekunden ADB-bereit."
  capture_diagnostics
  cat "$diagnostics_file"
  exit 1
fi

boot_completed=""
for attempt in $(seq 1 120); do
  boot_completed="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  if [[ "$boot_completed" == "1" ]]; then
    break
  fi
  sleep 2
done

if [[ "$boot_completed" != "1" ]]; then
  echo "Android meldet nach 240 Sekunden keinen abgeschlossenen Boot."
  capture_diagnostics
  cat "$diagnostics_file"
  exit 1
fi

# Remove pre-test emulator noise so a failure log starts near the actual test.
adb logcat -c >/dev/null 2>&1 || true

echo "Android-Instrumentierungstests starten."
gradle --no-daemon connectedDebugAndroidTest --stacktrace 2>&1 | tee "$gradle_log"
result=${PIPESTATUS[0]}

if [[ "$result" -eq 0 ]]; then
  echo "Android-Instrumentierungstests erfolgreich."
  exit 0
fi

echo "Android-Instrumentierungstests fehlgeschlagen (Exit-Code $result); Geräte- und Logcat-Diagnose wird gesichert."
capture_diagnostics

echo "=== Instrumentierungs-Registrierung und Geräteinformationen ==="
cat "$diagnostics_file"

echo "=== Relevante Logcat-Fehler ==="
grep -E -i -C 3 \
  'FATAL EXCEPTION|AndroidRuntime|am_crash|am_anr|Process .*died|Instrumentation|TestRunner|Keystore|KeyStore|SecurityException|Unable to start' \
  "$logcat_file" | tail -n 320 || true

exit "$result"
