# NUS-ISS DDAS

## Requirements

- JDK 17
- Maven 3.9.x
- Docker (for a local PostgreSQL instance and a dev-mode HashiCorp Vault instance)

## Setup

1. Ensure JDK 17 is installed and set as JAVA_HOME in your environment variables
2. Ensure maven is installed. Verify by running 'mvn -version' and checking your maven and Java version.
3. In cmd, go to nusiss-ddas-springboot (eg. 'cd nusiss-ddas-springboot') and run 'docker compose up -d'. This starts Postgres AND a dev-mode HashiCorp Vault instance (used for digital-signature signing when approving documents). Schema and reference data are applied automatically by Flyway on app startup.
4. Type in 'mvn spring-boot:run'. This will download all the dependencies required.
5. Once the cmd says 'Started DdasDemo in XX.XXX seconds', go to your browser and go to 'http://localhost:8080/'
6. Register an account and login (to a currently empty page)

To connect to a different PostgreSQL instance instead of the bundled Docker one, edit `spring.datasource.*` in `src/main/resources/application.properties`.
