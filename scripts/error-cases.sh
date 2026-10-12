#!/usr/bin/env bash
# Usage: ./scripts/error-cases.sh

BASE="${1:-http://localhost:8080/api/v1}"
STATION="ST-001"

run() {
  local label="$1"; shift
  printf '\n## %s\n' "$label"
  curl -s -w '\n[HTTP %{http_code}]\n' "$@"
}

for ACCEPT in application/json application/xml; do
  printf '\n# Accept: %s \n' "$ACCEPT"
  H=(-H "Accept: $ACCEPT")

  run "unknown station (404)"            "${H[@]}" "$BASE/stations/ST-999"
  run "latest, unknown station (404)"    "${H[@]}" "$BASE/stations/ST-999/measurements/latest"
  run "history, unknown station (404)"   "${H[@]}" "$BASE/stations/ST-999/measurements"
  run "from after to (400)"              "${H[@]}" "$BASE/stations/$STATION/measurements?from=2026-10-10T00:00:00Z&to=2026-10-09T00:00:00Z"
  run "invalid date format (400)"        "${H[@]}" "$BASE/stations/$STATION/measurements?from=yesterday"
  run "invalid warningLevel (400)"       "${H[@]}" "$BASE/stations?warningLevel=FOO"
  run "negative limit (400)"             "${H[@]}" "$BASE/stations/$STATION/measurements?limit=-5"
  run "limit too large (400)"            "${H[@]}" "$BASE/stations/$STATION/measurements?limit=999999"
  run "wrong HTTP method (405)"          "${H[@]}" -X DELETE "$BASE/alerts"
  run "unsupported Content-Type (415)"   "${H[@]}" -X POST -H "Content-Type: text/plain" -d "hello" "$BASE/stations"
  run "broken JSON body (400)"           "${H[@]}" -X POST -H "Content-Type: application/json" -d '{"id": ' "$BASE/stations"
  run "duplicate station (409)"          "${H[@]}" -X POST -H "Content-Type: application/json" \
      -d '{"id":"'"$STATION"'","name":"Dup","river":"Donau","location":{"latitude":48.2,"longitude":16.4},"normalWaterLevel":150,"warningWaterLevel":175,"criticalWaterLevel":200,"isActive":true}' \
      "$BASE/stations"
  run "unknown URL (404)"                "${H[@]}" "$BASE/does-not-exist"
done

# 406 only makes sense with an Accept header the API can't produce
run "unsupported format (406)" -H "Accept: text/csv" "$BASE/stations"