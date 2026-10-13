# Fragestellungen

## Was sind die Hauptmerkmale von JSON?

JSON (JavaScript Object Notation) ist ein textbasiertes Format für den Datenaustausch. Es ist für Menschen gut lesbar
und für Programme einfach zu parsen und zu erzeugen. JSON baut auf zwei Strukturen auf: Objekte mit Name/Wert-Paaren
in `{}` und Arrays als geordnete Listen in `[]`. Ein Wert ist ein String, eine Zahl, `true`, `false`, `null`, ein
Objekt oder ein Array. Objekte und Arrays können beliebig verschachtelt werden.

JSON hat nur einen Zahlentyp und keinen Datumstyp. Deshalb werden Zeitpunkte in diesem Projekt als ISO-8601-String
übertragen. Kommentare sind nicht erlaubt, und ein Schema ist nicht eingebaut (dafür gibt es separat JSON Schema).
Zwischen Systemen muss JSON in UTF-8 kodiert sein. Weil die Syntax kompakt ist und JavaScript JSON direkt versteht,
ist es heute das übliche Format für REST-APIs.

Quellen:

- [RFC 8259: The JavaScript Object Notation (JSON) Data Interchange Format](https://www.rfc-editor.org/rfc/rfc8259), Abschnitte 1–6 und 8.1
- [Einführung in JSON, json.org](https://www.json.org/json-de.html)

## Was sind die Hauptmerkmale von XML?

XML (Extensible Markup Language) ist eine Auszeichnungssprache. Anders als bei HTML gibt es keine vordefinierten
Tags, die Elemente legt man selbst fest. Ein Dokument besteht aus verschachtelten Elementen mit Start- und End-Tag.
Elemente können zusätzlich Attribute haben, zum Beispiel `<stations totalCount="5">`. Am Anfang kann eine
XML-Deklaration mit Version und Encoding stehen.

Ein XML-Dokument muss wohlgeformt sein:

- Es gibt genau ein Wurzelelement.
- Jedes Element wird geschlossen.
- Elemente dürfen sich nicht überschneiden.
- Groß- und Kleinschreibung wird unterschieden.

Mit einer DTD oder einem XML Schema kann man zusätzlich prüfen, ob ein Dokument einer festgelegten Struktur
entspricht. Dann ist es nicht nur wohlgeformt, sondern auch gültig. Namespaces verhindern Konflikte, wenn Elemente
aus verschiedenen Vokabularen im selben Dokument vorkommen.

XML ist ausführlicher als JSON, weil jedes Element einen End-Tag braucht. In diesem Projekt sieht man außerdem die
Regel mit dem Wurzelelement: Eine Liste von Stationen kann nicht direkt zurückgegeben werden, deshalb steckt sie in
einem `<stations>`-Element.

Quellen:

- [Extensible Markup Language (XML) 1.0, W3C](https://www.w3.org/TR/xml/), Abschnitt 2.1 (Well-Formed XML Documents) und 2.8 (Prolog and Document Type Declaration)
- [XML introduction, MDN](https://developer.mozilla.org/en-US/docs/Web/XML/Guides/XML_introduction)
- [Namespaces in XML 1.0, W3C](https://www.w3.org/TR/xml-names/)

## Wie unterscheidet die Applikation in diesem Beispiel, ob die Rückgabewerte in JSON oder XML zurückgegeben werden?

Über den `Accept`-Header der Anfrage. Dieses Verfahren heißt Content Negotiation. Die Controller-Methoden selbst
wissen nichts vom Format, sie geben nur Java-Objekte zurück. Die Entscheidung trifft Spring:

1. Die Controller geben in `@RequestMapping` mit `produces` an, dass sie `application/json` und `application/xml`
   liefern können.
2. Spring vergleicht den `Accept`-Header mit diesen Typen und wählt den passenden. Ohne `Accept`-Header oder mit
   `*/*` wird der erste Typ verwendet, also JSON.
3. Für den gewählten Typ sucht Spring einen `HttpMessageConverter`, der das Objekt umwandelt. Für JSON ist das
   Jackson. Der XML-Converter kommt aus `jackson-dataformat-xml`, das in `build.gradle` eingebunden ist. Spring Boot
   registriert ihn automatisch, sobald die Bibliothek vorhanden ist. In `AppConfig` wird er angepasst, damit die
   XML-Deklaration mit ausgegeben wird.
4. Passt kein Typ, zum Beispiel bei `Accept: text/csv`, antwortet die Applikation mit `406 Not Acceptable`.

Für den Body eines `POST` gilt dasselbe in die andere Richtung. Dort entscheidet der `Content-Type`-Header, welcher
Converter den Body liest. Ein nicht unterstützter Typ ergibt `415 Unsupported Media Type`.

Quellen:

- [Content negotiation, MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Content_negotiation)
- [Accept, MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Accept)
- [Mapping Requests: Producible Media Types, Spring Framework](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html#mvc-ann-requestmapping-produces)
- [HttpMessageConverters, Spring Boot](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.message-converters)

## Was zeichnet ein REST Interface aus und warum wird es heute sehr oft als standardisierte Schnittstelle verwendet?

REST (Representational State Transfer) ist ein Architekturstil, den Roy Fielding im Jahr 2000 in seiner Dissertation
beschrieben hat. REST ist kein Protokoll, sondern eine Reihe von Einschränkungen:

- **Client-Server:** Client und Server sind getrennt und können sich unabhängig voneinander ändern.
- **Zustandslos:** Jede Anfrage enthält alle Informationen, die der Server braucht. Der Server speichert keinen
  Sitzungszustand.
- **Cache:** Antworten sind als cachebar oder nicht cachebar gekennzeichnet.
- **Einheitliche Schnittstelle:** Ressourcen werden über URIs identifiziert und über Repräsentationen wie JSON oder
  XML verändert. Nachrichten beschreiben sich selbst, und Links in den Antworten führen zu weiteren Aktionen (HATEOAS).
- **Schichtensystem:** Zwischen Client und Server können Proxys oder Load Balancer liegen, ohne dass der Client das merkt.
- **Code-on-Demand:** Der Server kann optional Code an den Client schicken.

Mit HTTP bedeutet das in der Praxis: Jede Ressource hat eine URL, wie `/api/v1/stations/ST-001`. Die HTTP-Methode
gibt die Aktion an und der Statuscode das Ergebnis. HATEOAS setzen die meisten APIs nicht um, diese auch nicht.

REST ist so verbreitet, weil es direkt auf HTTP aufbaut. HTTP gibt es in jedem Browser und jeder Programmiersprache,
und Firewalls lassen es durch. Man braucht keine speziellen Bibliotheken, für einen Test reicht `curl`. Weil der
Server keinen Zustand speichert, kann jede Anfrage von einem beliebigen Server bearbeitet werden, was die Skalierung
einfach macht. Im Vergleich zu SOAP ist REST außerdem leichter zu verstehen und nicht an ein Format gebunden.

Quellen:

- [Fielding, R.: Architectural Styles and the Design of Network-based Software Architectures, Kapitel 5](https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm), Abschnitte 5.1.2–5.1.7
- [REST, MDN](https://developer.mozilla.org/en-US/docs/Glossary/REST)

## Was ist der Unterschied zwischen den HTTP Methoden GET, POST, PUT und PATCH?

Zwei Begriffe sind dafür wichtig. Eine Methode ist **sicher**, wenn sie nur liest und auf dem Server nichts ändert.
Sie ist **idempotent**, wenn mehrere gleiche Anfragen dieselbe Wirkung haben wie eine einzige.

- **GET** liest eine Ressource. GET ist sicher und idempotent, und die Antwort kann gecacht werden. Parameter stehen in
  der URL, ein Body ist nicht vorgesehen.
- **POST** schickt Daten, die der Server verarbeitet, meistens um eine neue Ressource anzulegen. Die URL der neuen
  Ressource vergibt der Server. POST ist weder sicher noch idempotent, zweimal senden kann zwei Ressourcen erzeugen.
  In diesem Projekt legt `POST /api/v1/stations` eine Station an und antwortet mit `201 Created` und einem
  `Location`-Header.
- **PUT** ersetzt die Ressource unter der angegebenen URL vollständig durch den mitgeschickten Inhalt, oder legt sie
  dort an. PUT ist idempotent, weil nach jeder Wiederholung derselbe Zustand vorliegt.
- **PATCH** ändert eine Ressource nur teilweise. Es werden nur die Änderungen geschickt, zum Beispiel nur
  `isActive: false` für eine Station. PATCH ist nicht automatisch idempotent.

Quellen:

- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110), Abschnitte 9.2.1 (Safe Methods), 9.2.2 (Idempotent Methods), 9.3.1 (GET), 9.3.3 (POST), 9.3.4 (PUT)
- [RFC 5789: PATCH Method for HTTP](https://www.rfc-editor.org/rfc/rfc5789)
- [HTTP request methods, MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods)

## Ich will eine neue Request Methode / Pfad in meinem REST Interface zur Verfügung stellen, welche Stellen des Quellcodes muss ich verändern?

Am Beispiel von `DELETE /api/v1/stations/{id}` in diesem Projekt:

1. **Controller:** In `StationController` eine neue Methode mit `@DeleteMapping("/stations/{id}")` anlegen. Die
   Parameter kommen über `@PathVariable`, `@RequestParam` oder `@RequestBody`. Basis-URL und `produces` gelten
   automatisch, weil sie in `@RequestMapping` auf der Klasse stehen.
2. **Service:** Die Logik kommt in `StationService`, nicht in den Controller. Dort wird zum Beispiel geprüft, ob die
   Station existiert.
3. **Repository:** Nur nötig, wenn ein neuer Datenzugriff gebraucht wird. `StationRepository.delete` gibt es schon.
4. **DTO:** Nur nötig, wenn die Antwort eine neue Struktur hat. Für XML braucht sie einen Namen mit
   `@JacksonXmlRootElement`.
5. **Fehlerbehandlung:** Für neue Fehlerfälle eine Exception anlegen und in `GlobalExceptionHandler` einem Statuscode
   zuordnen.
6. **Dokumentation und Tests:** OpenAPI-Annotationen (`@Operation`, `@ApiResponse`) ergänzen und einen MockMvc-Test
   schreiben. Wenn der Webclient den Endpunkt nutzen soll, kommt ein Aufruf in `app.js` dazu.

Quellen:

- [Building a RESTful Web Service, Spring](https://spring.io/guides/gs/rest-service/)
- [Mapping Requests, Spring Framework](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html)

## Kann man OpenAPI und Swagger vergleichen? Wenn ja, dann vergleichen Sie beide Themengebiete. Wenn nein, dann begründe die Antwort.

Nur bedingt, weil die beiden Begriffe heute verschiedene Dinge bezeichnen. Früher war Swagger der Name der
Spezifikation selbst. 2015 hat SmartBear die Swagger-Spezifikation an die OpenAPI Initiative der Linux Foundation
übergeben. Seitdem heißt sie OpenAPI Specification.

**OpenAPI** ist heute die Spezifikation. Sie legt ein Format in JSON oder YAML fest, mit dem man eine REST-API
beschreibt: Endpunkte, Parameter, Request- und Response-Bodies, Statuscodes und Authentifizierung. Die Beschreibung
ist unabhängig von Programmiersprache und Hersteller.

**Swagger** steht heute für Werkzeuge von SmartBear, die mit dieser Spezifikation arbeiten:

- **Swagger UI** zeigt eine OpenAPI-Beschreibung als interaktive Dokumentation an, in der man Anfragen direkt
  ausprobieren kann.
- **Swagger Editor** ist ein Editor zum Schreiben von OpenAPI-Beschreibungen.
- **Swagger Codegen** erzeugt aus einer Beschreibung Server-Code und Client-Bibliotheken.

OpenAPI ist also die Beschreibung der API, Swagger eine Sammlung von Tools dafür. In diesem Projekt erzeugt
springdoc-openapi aus dem Code eine OpenAPI-Beschreibung (`/v3/api-docs`, exportiert in `docs/openapi.yaml`), und
Swagger UI zeigt sie unter `/swagger-ui.html` an.

Quellen:

- [OpenAPI Initiative FAQ](https://www.openapis.org/faq)
- [OpenAPI Specification](https://spec.openapis.org/oas/latest.html)
- [Swagger Tools](https://swagger.io/tools/)

## Wo liegt im folgenden Code-Snippet der Fehler? Begründe die Antwort.

```java
@GetMapping("/stations/add")
public String createStation(@RequestParam String name, @RequestParam double warning) {
    stationService.save(new Station(name, warning));
    return "Station successfully created!";
}
```

Der Hauptfehler ist, dass eine Station mit **GET** angelegt wird. GET muss sicher sein, darf also auf dem Server
nichts ändern. Browser, Suchmaschinen und Prefetching rufen GET-Links teilweise automatisch auf, und Clients oder
Proxys dürfen GET-Anfragen bei einem Verbindungsfehler automatisch wiederholen. Jedes Neuladen der Seite würde eine weitere Station anlegen. Außerdem stehen
die Daten in der URL und landen so in Server-Logs und im Browserverlauf.

Dazu kommen weitere Probleme:

- Der Pfad `/stations/add` enthält ein Verb. Bei REST gibt die Methode die Aktion an, richtig wäre `POST /stations`.
- Die Daten sollten als JSON- oder XML-Body mit `@RequestBody` kommen, nicht als Query-Parameter.
- Die Antwort ist ein Text mit Status `200`. Richtig wäre `201 Created` mit einem `Location`-Header und der neuen
  Station als Body.
- Die Station hat nur einen Namen und einen Warnwert. Id, Fluss, Position und kritischer Wert fehlen, und nichts wird
  geprüft.

So sieht der Endpunkt in diesem Projekt aus:

```java
@PostMapping("/stations")
public ResponseEntity<Station> postStation(@RequestBody Station station) {
    stationService.addStation(station);
    URI location = URI.create("/api/v1/stations/" + station.id());
    return ResponseEntity.created(location).body(station);
}
```

Quellen:

- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110), Abschnitte 9.2.1 (Safe Methods), 9.2.2 (Idempotent Methods), 9.3.1 (GET), 9.3.3 (POST), 15.3.2 (201 Created)
- [Safe (HTTP Methods), MDN](https://developer.mozilla.org/en-US/docs/Glossary/Safe/HTTP)
