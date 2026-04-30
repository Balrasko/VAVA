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

### Build and run executable JAR

Build the executable JAR with:

```bash
mvn clean package
```

Run it from the repository root so the application can read `.env`:

```bash
java -jar target/vavateam1-1.0-SNAPSHOT.jar
```

The JAR does not contain PostgreSQL. Start the Docker database first:

```bash
docker-compose up -d db
```

When the JAR runs on the host machine, keep the default `.env` database host and port:

```env
RESTAURANT_DB_HOST=localhost
RESTAURANT_DB_PORT=5433
```

If the Java application is ever moved into the same Docker Compose network, use `RESTAURANT_DB_HOST=db` and `RESTAURANT_DB_PORT=5432` instead.

### Run PostgreSQL on minikube with Helm

The local Helm chart lives in `charts/vava`. It deploys PostgreSQL with the same schema and seed data used by Docker Compose:

```bash
minikube start
helm upgrade --install vava ./charts/vava --namespace vava --create-namespace --kube-context minikube
kubectl --context minikube -n vava rollout status statefulset/vava-postgresql
helm test vava -n vava --kube-context minikube
```

Useful Helm commands:

```bash
helm upgrade --install vava ./charts/vava --namespace vava --create-namespace --kube-context minikube
helm test vava -n vava --kube-context minikube
helm uninstall vava -n vava --kube-context minikube
```

Use `kubectl port-forward` to connect the desktop JavaFX JAR to the minikube database:

```bash
kubectl --context minikube -n vava port-forward svc/vava-postgresql 5433:5432
java -jar target/vavateam1-1.0-SNAPSHOT.jar
```

If Docker Compose PostgreSQL is already using port `5433`, either stop it or forward minikube to another local port and update `.env`.

For a presentation, other people on the same network can connect to the minikube PostgreSQL through your machine if you bind the port-forward to all interfaces:

```bash
kubectl --context minikube -n vava port-forward --address 0.0.0.0 svc/vava-postgresql 15432:5432
```

Then they use your Mac's LAN IP address and port `15432`:

```bash
ipconfig getifaddr en0
```

```env
RESTAURANT_DB_HOST=<your-mac-lan-ip>
RESTAURANT_DB_PORT=15432
RESTAURANT_DB_NAME=vava-restaurant
RESTAURANT_DB_USER=postgres
RESTAURANT_DB_PASSWORD=postgres
```

This is only a local demo setup. Keep it on a trusted network and stop the port-forward with `Ctrl+C` after the presentation.

More details are in `charts/vava/README.md`.

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
3, Jožko Čašník, waiter1@vava.com, waiter123
4, Fero Kuchár, chef1@vava.com, chef123
