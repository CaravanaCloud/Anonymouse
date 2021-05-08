# Anonymouse

Replaces personal identifiable information with fake data ("Julio Faerman", 09/01/1980 -> "Jhon Doe", 01/01/1501).

- Prevent data leaks from test environments by erasing all Personally Identifiable Information, but still retaining relationships and semantics for bug reproduction and forensics.
- Comply with data privacy regulations (GDPR, LGPD, ...)

# Current Status

![Maven Build](https://github.com/CaravanaCloud/Anonymouse/workflows/Maven-Build/badge.svg)
[![JaCoCo coverage](https://s3-us-west-2.amazonaws.com/anonymouse.caravana.cloud/badges/jacoco.svg)](https://s3-us-west-2.amazonaws.com/anonymouse.caravana.cloud/jacoco/index.html)
![GitHub Super-Linter](https://github.com/CaravanaCloud/Anonymouse/workflows/Super-Linter/badge.svg)


Initial proof of concept.
Uses JDBC Metadata API to iterate on the database (full scan) and replace all values classified as PII.

Help more than welcome!

# HOW TO

Just point your environment variables to the database and anonymouse it:
```
export JDBC_URL=jdbc:mysql://127.0.0.1:3336/
export JDBC_USER=root
export JDBC_PASSWORD=AnonymouseIt2021
$ mvn exec:java
```

Automated tests runnable with ```$ mvn test``` or in your favorite IDE.

# Helpful commands

## MySLQ Example

### Start Container

```
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3334
export MYSQL_ROOT_USERNAME=root
export MYSQL_ROOT_PASSWORD=Masterkey321
export MYSQL_DATABASE=sampledb

docker run --rm -p $MYSQL_PORT:3306 \
--name $MYSQL_DATABASE \
-e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD \
-e MYSQL_DATABASE=$MYSQL_DATABASE \
-d mysql:latest
```

### Connect

```
mysql --host=$MYSQL_HOST --port=$MYSQL_PORT -uroot -p$MYSQL_ROOT_PASSWORD
```

### Load your backup

```
source dump.sql 
```

### Build Anonymouse

```
export QUARKUS_DATASOURCE_DB_KIND=mysql
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}
export QUARKUS_DATASOURCE_USERNAME=$MYSQL_ROOT_USERNAME
export QUARKUS_DATASOURCE_PASSWORD=$MYSQL_ROOT_PASSWORD
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=none

mvn package 
```

### Map your database 

Example: https://github.com/CaravanaCloud/Anonymouse/blob/main/src/test/config/anon_kornell.yaml
 
```
export ANONYM_CONFIG=file://${PWD}/src/test/config/anon_kornell.yaml
```

### Run Anonymouse
```
mvn exec:java
```

# References

Ama``zon Macie Data Classification:
https://docs.aws.amazon.com/pt_br/macie/latest/userguide/macie-classify-objects-pii.html

NIST-80-122: Guide to Protecting the Confidentiality of Personally Identifiable Information (PII) 
https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-122.pdf

FIPS PUB 199: Standards for Security Categorization of Federal Information and Information Systems
https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.199.pdf

# Similar Tools

[Anonimatron](https://github.com/realrolfje/anonimatron/tree/master)

# Automated Reports

Jacoco: https://s3-us-west-2.amazonaws.com/anonymouse.caravana.cloud/jacoco/index.html

