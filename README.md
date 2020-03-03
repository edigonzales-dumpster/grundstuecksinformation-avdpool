# grundstuecksinformation-avdpool

NOT PRODUCTION-READY. Wahrscheinlich wird der GRELT-(Jenkins)-Job-Weg weiterverfolgt. 

## Developing

```
mkdir -m 0777 ~/pgdata-grundstuecksinformation
docker run --rm --name grundstuecksinformation-db -p 54321:5432 --hostname primary \
-e PG_DATABASE=grundstuecksinformation -e PG_LOCALE=de_CH.UTF-8 -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary \
-e PG_USER=admin -e PG_PASSWORD=admin \
-e PG_PRIMARY_USER=repl -e PG_PRIMARY_PASSWORD=repl \
-e PG_ROOT_PASSWORD=secret \
-e PG_WRITE_USER=gretl -e PG_WRITE_PASSWORD=gretl \
-e PG_READ_USER=ogc_server -e PG_READ_PASSWORD=ogc_server \
-v ~/pgdata-grundstuecksinformation:/pgdata:delegated \
sogis/oereb-db:latest
```

```
./mvnw compile quarkus:dev
```

## Building

### JVM

```
./mwn clean package -Dmaven.test.skip=true
```

TODO: fix test(s)

```
docker build -f src/main/docker/Dockerfile.jvm -t sogis/grundstuecksinformation-avdpool-jvm .
```
