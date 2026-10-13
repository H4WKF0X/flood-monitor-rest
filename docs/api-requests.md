# API Requests

Start the application before you run the commands (`./gradlew bootRun`).
All commands use `http://localhost:8080`. The `-i` option shows the status line and the headers.
`scripts/success-cases.sh` and `scripts/error-cases.sh` run the same requests all at once in JSON and XML.

## Stations

### All stations

```bash
curl -i -H "Accept: application/json" http://localhost:8080/api/v1/stations
```

```bash
curl -i -H "Accept: application/xml" http://localhost:8080/api/v1/stations
```

Without an `Accept` header the API returns JSON:

```bash
curl -i http://localhost:8080/api/v1/stations
```

### Filters

```bash
curl -i "http://localhost:8080/api/v1/stations?river=Donau"
```

```bash
curl -i "http://localhost:8080/api/v1/stations?isActive=true"
```

```bash
curl -i "http://localhost:8080/api/v1/stations?warningLevel=NORMAL"
```

```bash
curl -i -H "Accept: application/xml" "http://localhost:8080/api/v1/stations?river=donau&isActive=true"
```

### One station

```bash
curl -i -H "Accept: application/json" http://localhost:8080/api/v1/stations/ST-001
```

```bash
curl -i -H "Accept: application/xml" http://localhost:8080/api/v1/stations/ST-001
```

### Create a station

JSON body:

```bash
curl -i -X POST -H "Content-Type: application/json" -H "Accept: application/json" \
  -d '{"id":"ST-101","name":"Krems","river":"Donau","location":{"latitude":48.41,"longitude":15.60},"normalWaterLevel":150,"warningWaterLevel":175,"criticalWaterLevel":200,"isActive":true}' \
  http://localhost:8080/api/v1/stations
```

XML body:

```bash
curl -i -X POST -H "Content-Type: application/xml" -H "Accept: application/xml" \
  -d '<station><id>ST-102</id><name>Hallein</name><river>Salzach</river><location><latitude>47.68</latitude><longitude>13.10</longitude></location><normalWaterLevel>150</normalWaterLevel><warningWaterLevel>175</warningWaterLevel><criticalWaterLevel>200</criticalWaterLevel><isActive>true</isActive></station>' \
  http://localhost:8080/api/v1/stations
```

## Measurements

### Latest measurement

```bash
curl -i -H "Accept: application/json" http://localhost:8080/api/v1/stations/ST-001/measurements/latest
```

```bash
curl -i -H "Accept: application/xml" http://localhost:8080/api/v1/stations/ST-001/measurements/latest
```

### History

```bash
curl -i -H "Accept: application/json" "http://localhost:8080/api/v1/stations/ST-001/measurements?limit=5"
```

```bash
curl -i -H "Accept: application/xml" "http://localhost:8080/api/v1/stations/ST-001/measurements?limit=5"
```

Last hour:

```bash
curl -i "http://localhost:8080/api/v1/stations/ST-001/measurements?from=$(date -u -d '-1 hour' +%FT%TZ)&to=$(date -u +%FT%TZ)"
```

Only measurements with warning level `WARNING`:

```bash
curl -i "http://localhost:8080/api/v1/stations/ST-001/measurements?warningLevel=WARNING&limit=5"
```

## Statistics

Whole history:

```bash
curl -i -H "Accept: application/json" http://localhost:8080/api/v1/stations/ST-001/statistics
```

```bash
curl -i -H "Accept: application/xml" http://localhost:8080/api/v1/stations/ST-001/statistics
```

Last 24 hours:

```bash
curl -i "http://localhost:8080/api/v1/stations/ST-001/statistics?from=$(date -u -d '-24 hours' +%FT%TZ)&to=$(date -u +%FT%TZ)"
```

## Alerts

```bash
curl -i -H "Accept: application/json" http://localhost:8080/api/v1/alerts
```

```bash
curl -i -H "Accept: application/xml" http://localhost:8080/api/v1/alerts
```

```bash
curl -i "http://localhost:8080/api/v1/alerts?warningLevel=CRITICAL"
```

```bash
curl -i "http://localhost:8080/api/v1/alerts?river=Donau"
```

## Errors

Errors use the same format as the request. Change the `Accept` header to `application/xml` to get them as XML.
`scripts/error-cases.sh` runs all of these in JSON and XML.

### Unknown station (404)

```bash
curl -i http://localhost:8080/api/v1/stations/ST-999
```

```bash
curl -i http://localhost:8080/api/v1/stations/ST-999/measurements/latest
```

### `from` after `to` (400)

```bash
curl -i "http://localhost:8080/api/v1/stations/ST-001/measurements?from=2026-10-10T00:00:00Z&to=2026-10-09T00:00:00Z"
```

### Invalid date format (400)

```bash
curl -i "http://localhost:8080/api/v1/stations/ST-001/measurements?from=yesterday"
```

### Invalid warning level (400)

```bash
curl -i "http://localhost:8080/api/v1/stations?warningLevel=FOO"
```

### Negative or too large limit (400)

```bash
curl -i "http://localhost:8080/api/v1/stations/ST-001/measurements?limit=-5"
```

```bash
curl -i "http://localhost:8080/api/v1/stations/ST-001/measurements?limit=999999"
```

### Malformed body (400)

```bash
curl -i -X POST -H "Content-Type: application/json" -d '{"id": ' http://localhost:8080/api/v1/stations
```

### Station already exists (409)

```bash
curl -i -X POST -H "Content-Type: application/json" \
  -d '{"id":"ST-001","name":"Duplicate","river":"Donau","location":{"latitude":48.2,"longitude":16.4},"normalWaterLevel":150,"warningWaterLevel":175,"criticalWaterLevel":200,"isActive":true}' \
  http://localhost:8080/api/v1/stations
```

### Unsupported body format (415)

```bash
curl -i -X POST -H "Content-Type: text/plain" -d "hello" http://localhost:8080/api/v1/stations
```

### Unsupported response format (406)

```bash
curl -i -H "Accept: text/csv" http://localhost:8080/api/v1/stations
```
