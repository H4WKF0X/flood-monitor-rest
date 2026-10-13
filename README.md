# Flood Monitor

A REST API for a simulated flood early warning system. Five stations on the Donau, Salzach, Mur and Inn create a
measurement every minute. The API provides stations, measurements, statistics and alerts in JSON or XML, and a web
client shows them in the browser. Built with Java 17, Spring Boot 3.2 and Gradle.

## Getting Started

You need Java 17 or newer. The Gradle wrapper is included.

```bash
./gradlew bootRun
```

```bash
./gradlew test
```

The web client runs on [localhost:8080](http://localhost:8080) and Swagger UI on
[localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). At startup the application first simulates
the last seven days, so for a few seconds all stations show `UNKNOWN`. The tests switch the simulation off and use a
fixed clock, so they don't depend on random values or the current time.

## Configuration

All settings are in `src/main/resources/application.properties`. The file contains no secrets.
Values can be overridden at startup, for example `./gradlew bootRun --args='--server.port=8081'`.

```properties
server.port=8080

# time between two measurements in ms, preferably keep it at 60000 or lower
simulation.enabled=true
simulation.interval=60000

# measurements kept in memory, and the history simulated at startup after a warm-up
simulation.historyWindow=7d
simulation.prefillHistory=7d
simulation.prefillWarmUp=30d

# hours for a full battery drain and a full charge
simulation.hoursToDrain=144
simulation.hoursToCharge=4

# rain cells: average dry time and lifetime in ms, intensity in mm/h, size, speed and map borders
simulation.meanDryMs=43200000
simulation.meanLifetimeMs=86400000
simulation.meanIntensity=2.5
simulation.radiusKm=100
simulation.speedKmH=30
simulation.minLat=45.5
simulation.maxLat=49.9
simulation.minLon=8.2
simulation.maxLon=18.5

# default and maximum limit for the measurement history
api.measurements.defaultLimit=20160
api.measurements.maxLimit=20160
```

## API

All endpoints are under `/api/v1`. The API returns JSON by default and XML with `Accept: application/xml`.
Other formats return `406`.

- `GET /stations`: filters `river`, `isActive`, `warningLevel`
- `GET /stations/{id}`
- `POST /stations`: JSON or XML body
- `GET /stations/{id}/measurements/latest`
- `GET /stations/{id}/measurements`: filters `from`, `to`, `limit`, `warningLevel`
- `GET /stations/{id}/statistics`: filters `from`, `to`
- `GET /alerts`: filters `river`, `warningLevel`

The server calculates the warning level from the water level and the thresholds of the station:

- **NORMAL:** below `warningWaterLevel`
- **WARNING:** from `warningWaterLevel`
- **CRITICAL:** from `criticalWaterLevel`
- **UNKNOWN:** the station is inactive or has no measurement from the last three intervals

Errors come in the requested format with `timestamp`, `status`, `error`, `message` and `path`.
The full documentation is in Swagger UI and [docs/openapi.yaml](docs/openapi.yaml).
[docs/api-requests.md](docs/api-requests.md) has a curl command for every endpoint and error case.

## Web Client

The client is in `src/main/resources/static`. It lists the stations with their warning level, shows the latest
measurement, statistics, a chart and the history table of the selected station, and can filter by time range and
warning level. It switches between JSON and XML and updates every 30 seconds.

![Web client](docs/screenshots/webclient-overview.png)

## Documentation

- [Architecture and data model](docs/architecture.md)
- [API requests](docs/api-requests.md)
- [OpenAPI specification](docs/openapi.yaml)
