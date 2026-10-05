# Flood Monitor REST API – Starter Project

## Learning Objectives

In this exercise you will learn how to:

1. Set up a basic **Spring Boot** REST application using **Gradle**.
2. Create a **model class** (POJO) and understand JSON serialization.
3. Build a **REST controller** and expose a `GET` endpoint.
4. Test your REST API using a browser or a tool like **Postman** or **curl**.

---

## Project Structure

```
src/main/java/com/example/floodmonitor/
├── FloodMonitorApplication.java   ← Entry point
├── model/
│   └── Station.java               ← Data model (POJO)
└── controller/
    └── StationController.java     ← REST controller
```

---

## Running the Application

### Prerequisites
- Java 17 or higher
- No additional installation required (Gradle Wrapper included)

### Start the application
```bash
./gradlew bootRun
```

The server starts on **http://localhost:8080**.

### Test the endpoint
Open your browser or use curl:
```bash
curl http://localhost:8080/api/v1/stations
```

Expected response (JSON array of stations):
```json
[
  {
    "id": "1",
    "stationName": "Station Vienna",
    "timestamp": "2024-01-15T10:00:00",
    "waterLevel": 2.35,
    "waterTemperature": 8.5,
    "unit": "m"
  },
  ...
]
```

---

## Next Tasks

### Tasks 1
- [ ] Run the application and verify the `/api/v1/stations` endpoint works.
- [ ] Add a new field `waterFlow` (double) to the `Station` model. Verify the new field appears in the JSON response.
- [ ] Add a new endpoint `GET /api/v1/stations/{id}` that returns a single station by its ID.

### Tasks 2
- [ ] Add a `GET /api/v1/stations/count` endpoint that returns the number of stations as a plain integer.
- [ ] Return an appropriate HTTP status code (`404 Not Found`) when a station with the given ID does not exist.
- [ ] Add a `POST /api/v1/stations` endpoint that accepts a JSON body and adds a new station to the (in-memory) list.

### Tasks 3
- [ ] Add XML support: return XML instead of JSON when the client sends `Accept: application/xml`.
- [ ] Integrate **Springdoc OpenAPI** (Swagger UI) to document your API.

