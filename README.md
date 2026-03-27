Semestralny projekt z predmetu VAVA na FIIT STU 2025/2026

Prosim robte pull request/merge request nech tu nemame potom bordel v tom a nech sa to lahsie merguje potom

## Development setup

### Prerequisites

- **Java 25+**
- **Maven 3.8+**

> ⚠️ If you have multiple java versions installed set `JAVA_HOME` to the correct version

```
export JAVA_HOME=/path/to/jdk-25
```

Verify with `mvn -version`

```bash
user@example:~/projects/VAVA$ mvn -version
Apache Maven 3.8.7
Maven home: /usr/share/maven
Java version: 25.0.2, vendor: Ubuntu, runtime: /usr/lib/jvm/java-25-openjdk-amd64
...
```

### Clone the repository

```bash
git clone git@github.com:Balrasko/VAVA.git
cd VAVA
```

### Create DB

The compose files are split into:

- `docker-compose.yml` as the base setup for local development
- `docker-compose.prod.yml` as an override that adds persistent PostgreSQL storage

The base setup starts:

- `db`
- `pgadmin`

and does not persist PostgreSQL data, so init scripts are applied from a clean state.

Start the default development setup with:

```bash
docker-compose down && docker-compose up -d
```

For a persistent production-style database, use the override file:

```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Seed DB
```bash
zsh ./seed.sh
```

### ENV set
```bash
zsh ./run.sh
```
### Run the application

```bash
mvn clean javafx:run
```

## Code organization

The source code lives under `src/main/java/dev/vavateam1/` and follows a standard MVC layout:

| Package / File | Purpose                                                                                         |
| -------------- | ----------------------------------------------------------------------------------------------- |
| `App.java`     | Application entry point; extends `javafx.application.Application` and launches the JavaFX stage |
| `controller/`  | JavaFX controllers – handle UI events and mediate between view and service layers               |
| `model/`       | Plain data/domain objects (POJOs, records, enums)                                               |
| `service/`     | Business logic and data-access services consumed by controllers                                 |
| `view/`        | Custom JavaFX components or FXML-related view helpers                                           |

Resources are organised under `src/main/resources/` and copied to the classpath root by Maven at build time:

| Folder  | Purpose                                 |
| ------- | --------------------------------------- |
| `view/` | FXML layout files loaded by controllers |
| `css/`  | Stylesheets applied to JavaFX scenes    |

## Seed DB users

Users (id, name, email, password)
1, Admin User, admin@vava.com, admin123
2, Test Manager, manager@vava.com, manager123
3, Jožko Čašník, waiter@vava.com, waiter123
4, Fero Kuchár, chef1@vava.com, chef123
