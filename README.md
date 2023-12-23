# kotlin-native-sb-hex-example

Sample web application using:

- Kotlin
- Spring Boot 3 (3.1.x)
- Reactive programming (Webflux)
- Native image build (GraalVM)

## Requirements

- JDK 17
- Docker

## Build native OCI image (using Gradle and buildpacks)

```shell
./gradlew bootBuildImage --no-daemon --createdDate now
```

## Running the app

### Start

Start everything by running:

```shell
docker compose up -d
```

This will spin up a PostgreSQL database with some preloaded data and the previously built app itself

There are two endpoints you can interact with: one which reads and writes from and to the database, and another one
which makes an HTTP GET request to an external API.

```shell
# This request makes a transfer from account 1 to account 2, saving the transaction in the database
curl -v --request POST 'http://localhost:8080/accounts/1/transfers' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  --data-raw '{ "target_account": 2, "amount": 10 }'
```

```shell
# This request queries the current exchange rates for US Dollars
curl -v 'http://localhost:8080/exchange-rates'
```

### Tear-down

Simply run `docker compose down`.

## Where to go from here?

There are still several things missing in the code base:

- Higher code coverage (code coverage is pretty decent right now but it could still be improved).
- Refactor HTTP interceptors to log requests and responses.
- Spring Boot 3.2 added support for Java 21's Virtual Threads, might want to take a look at it.
- Using cloud config client to get externalised configuration.
