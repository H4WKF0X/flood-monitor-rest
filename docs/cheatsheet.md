# Cheatsheet

This file explains how the project was built, so the work can be reproduced. It also lists the problems that came up
along the way and how they were solved.

## The Task

The task is a REST API for a flood early warning system. Simulated stations create measurements, and the API
provides stations, measurements, statistics and alerts in JSON and XML. A jQuery web client shows the data, and the
API is documented with OpenAPI.

## Reproducing the Work

The project starts from the course starter repository
([DEZSYS_GK71_FLOODMONITOR_REST_INCOMPLETE](https://github.com/ThomasMicheler/DEZSYS_GK71_FLOODMONITOR_REST_INCOMPLETE)).
Its README has small warm-up tasks: a new field, a single-station endpoint, a count endpoint, a 404, a `POST`,
XML support and Springdoc. These are done first.

After that, all work steps are in the [GitHub issues](https://github.com/H4WKF0X/flood-monitor-rest/issues) of this
repository. Each issue has a goal, a "Done when" checklist and hints. Follow them in order, from #1 to #14.
Most commits reference their issue, so the history shows the code for each step.

The issues are grouped into [milestones](https://github.com/H4WKF0X/flood-monitor-rest/milestones) that match the
grading levels:

- **1 · Core (überwiegend erfüllt):** data model, repositories, warning levels, simulator, the station,
  measurement, alert and statistics endpoints, JSON and XML (#1–#9)
- **2 · Complete (zur Gänze erfüllt):** filters and central error handling (#10–#11)
- **3 · Submission deliverables:** tests, web client, OpenAPI and documentation (#12–#14, #19)
- **4 and 5 · Extended:** one process per station, a central server, admin endpoints and security (#15–#18).
  These belong to the follow-up task and are not part of this submission.

## Spring Concepts Used

- **`@RestController`, `@RequestMapping("/api/v1")`:** base URL for all endpoints of a controller
- **`produces` / `consumes`:** the formats an endpoint can return or read. `consumes` belongs only on endpoints
  with a body.
- **`@PathVariable`, `@RequestParam`:** URL parts and query parameters. `required = false` makes a filter optional.
- **Records:** for the model and the DTOs, since the values don't change after creation
- **Jackson XML annotations:** `@JacksonXmlRootElement`, `@JacksonXmlProperty`, `@JacksonXmlElementWrapper`
  for the element names and lists
- **`@RestControllerAdvice`:** one place that converts exceptions into error responses
- **`@Scheduled`, `@EnableScheduling`:** run the simulation at a fixed rate
- **`@ConditionalOnProperty`:** create the scheduler only if `simulation.enabled=true`
- **`Clock` bean:** all code reads the time from it, so the tests can use a fixed time
- **`@SpringBootTest`, `MockMvc`:** send requests to the application in tests without a running server
- **springdoc annotations:** `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`

## Problems and Solutions

- **Port 8080 in use:** another program used the port. `ss -ltnp | grep 8080` shows which one. Either stop it or
  start with a different port (`--server.port=8081`).
- **415 on every GET:** `consumes` was set on the controller class, so it applied to the GET endpoints too.
  It moved to the `@PostMapping`.
- **Simulation didn't run:** `@Scheduled` does nothing without `@EnableScheduling`.
- **Water level in metres:** the first version used metres, but the task defines cm. All levels and thresholds were
  changed to cm.
- **Every station was UNKNOWN:** a measurement counted as outdated after 30 s, but the interval was 60 s.
  The limit is now three intervals.
- **Wrong total rainfall:** the statistics added up the rain intensities in mm/h. To get mm, each value is multiplied
  by the interval length in hours first.
- **500 without filters:** optional parameters like `from` and `to` are `null` when they are missing, which caused a
  NullPointerException. The services now treat `null` as "no filter".
- **Lists in XML:** a plain list has no root element. Wrapper DTOs like `StationListResponse` with Jackson XML
  annotations give `<stations totalCount="5"><station>…</station></stations>`.
- **406 without a body:** if the client doesn't accept JSON or XML, Spring can't write the error in any format the
  client wants. The handler for 406 always returns JSON.
- **Random test failures:** the simulation and the current time made test results change. The tests switch the
  simulation off and use a fixed `Clock`.
- **Swagger lost the success responses:** as soon as a method has one `@ApiResponse`, springdoc no longer adds the
  200 response. Every endpoint now declares it.
- **UNKNOWN right after startup:** the history is simulated after the server already accepts requests, so for a few
  seconds the stations have no measurements. This is not fixed.
