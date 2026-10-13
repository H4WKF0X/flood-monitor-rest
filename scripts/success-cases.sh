#!/usr/bin/env bash
# Usage: ./scripts/success-cases.sh

BASE="${1:-http://localhost:8080/api/v1}"
STATION="ST-001"
FROM_1H="$(date -u -d '-1 hour' +%FT%TZ)"
FROM_24H="$(date -u -d '-24 hours' +%FT%TZ)"
NOW="$(date -u +%FT%TZ)"

run() {
  local label="$1"; shift
  printf '\n## %s\n' "$label"
  curl -s -w '\n[HTTP %{http_code}]\n' "$@"
}

for ACCEPT in application/json application/xml; do
  printf '\n# Accept: %s \n' "$ACCEPT"
  H=(-H "Accept: $ACCEPT")

  run "all stations (200)"                 "${H[@]}" "$BASE/stations"
  run "stations by river (200)"            "${H[@]}" "$BASE/stations?river=Donau"
  run "active stations (200)"              "${H[@]}" "$BASE/stations?isActive=true"
  run "stations by warning level (200)"    "${H[@]}" "$BASE/stations?warningLevel=NORMAL"
  run "one station (200)"                  "${H[@]}" "$BASE/stations/$STATION"
  run "latest measurement (200)"           "${H[@]}" "$BASE/stations/$STATION/measurements/latest"
  run "history, limit 5 (200)"             "${H[@]}" "$BASE/stations/$STATION/measurements?limit=5"
  run "history, last hour, limit 5 (200)"  "${H[@]}" "$BASE/stations/$STATION/measurements?from=$FROM_1H&to=$NOW&limit=5"
  run "history, WARNING only (200)"        "${H[@]}" "$BASE/stations/$STATION/measurements?warningLevel=WARNING&limit=5"
  run "statistics, whole history (200)"    "${H[@]}" "$BASE/stations/$STATION/statistics"
  run "statistics, last 24 hours (200)"    "${H[@]}" "$BASE/stations/$STATION/statistics?from=$FROM_24H&to=$NOW"
  run "alerts (200)"                       "${H[@]}" "$BASE/alerts"
  run "alerts, CRITICAL only (200)"        "${H[@]}" "$BASE/alerts?warningLevel=CRITICAL"
  run "alerts by river (200)"              "${H[@]}" "$BASE/alerts?river=Donau"
done

run "no Accept header returns JSON (200)" "$BASE/stations"

# POST runs once per format, since a second run with the same id returns 409
run "create station, JSON body (201)" -H "Accept: application/json" -H "Content-Type: application/json" \
    -d '{"id":"ST-101","name":"Krems","river":"Donau","location":{"latitude":48.41,"longitude":15.60},"normalWaterLevel":150,"warningWaterLevel":175,"criticalWaterLevel":200,"isActive":true}' \
    "$BASE/stations"
run "create station, XML body (201)" -H "Accept: application/xml" -H "Content-Type: application/xml" \
    -d '<station><id>ST-102</id><name>Hallein</name><river>Salzach</river><location><latitude>47.68</latitude><longitude>13.10</longitude></location><normalWaterLevel>150</normalWaterLevel><warningWaterLevel>175</warningWaterLevel><criticalWaterLevel>200</criticalWaterLevel><isActive>true</isActive></station>' \
    "$BASE/stations"
