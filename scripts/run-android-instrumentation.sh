#!/usr/bin/env bash
set -uo pipefail

log_file="${RUNNER_TEMP:-/tmp}/trakuna-instrumentation.log"

for attempt in 1 2 3; do
  echo "Android-Instrumentierungstests starten (Versuch ${attempt}/3)."
  gradle --no-daemon connectedDebugAndroidTest 2>&1 | tee "$log_file"
  result=${PIPESTATUS[0]}

  if [ "$result" -eq 0 ]; then
    echo "Android-Instrumentierungstests erfolgreich."
    exit 0
  fi

  if grep -Eq "Received status code 429|Too Many Requests" "$log_file" && [ "$attempt" -lt 3 ]; then
    echo "Maven Central hat die Abhängigkeitsauflösung gedrosselt (HTTP 429). Neuer Versuch in 30 Sekunden."
    sleep 30
    continue
  fi

  echo "Android-Instrumentierungstests fehlgeschlagen (Exit-Code $result)."
  exit "$result"
done

exit 1
