# Anonymouse

Prevent data leaks from test environments by erasing all Personally Identifiable Information, but still retaining relationships and semantics for bug reproduction and forensics.

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

# References

Amazon Macie Data Classification:
https://docs.aws.amazon.com/pt_br/macie/latest/userguide/macie-classify-objects-pii.html

NIST-80-122: Guide to Protecting the Confidentiality of Personally Identifiable Information (PII) 
https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-122.pdf

FIPS PUB 199: Standards for Security Categorization of Federal Information and Information Systems
https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.199.pdf

# Automated Reports

Jacoco: https://s3-us-west-2.amazonaws.com/anonymouse.caravana.cloud/jacoco/index.html

