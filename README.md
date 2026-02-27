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

### Run the application

```bash
mvn clean javafx:run
```

## Code organization

TBD
