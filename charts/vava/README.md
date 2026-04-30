# VAVA Helm Chart

Local Helm chart for running the VAVA database stack on minikube.

The JavaFX application is a desktop GUI, so the default chart deploys PostgreSQL only. Run the desktop JAR on the host and connect it to PostgreSQL through `kubectl port-forward`.

## Install

```bash
minikube start
helm upgrade --install vava ./charts/vava --namespace vava --create-namespace --kube-context minikube
kubectl --context minikube -n vava rollout status statefulset/vava-postgresql
helm test vava -n vava --kube-context minikube
```

## Helm Commands

```bash
helm upgrade --install vava ./charts/vava --namespace vava --create-namespace --kube-context minikube
helm test vava -n vava --kube-context minikube
helm uninstall vava -n vava --kube-context minikube
```

## Connect the Desktop App

Forward PostgreSQL to the same port used by the existing `.env`:

```bash
kubectl --context minikube -n vava port-forward svc/vava-postgresql 5433:5432
```

Then run the app from the repository root:

```bash
java -jar target/vavateam1-1.0-SNAPSHOT.jar
```

If port `5433` is already occupied, use another local port and update `.env`:

```bash
kubectl --context minikube -n vava port-forward svc/vava-postgresql 15432:5432
```

```env
RESTAURANT_DB_HOST=localhost
RESTAURANT_DB_PORT=15432
```

## Share PostgreSQL on LAN

Minikube runs on your machine, so other people cannot connect to it directly by default. For a short presentation on a trusted network, bind the port-forward to all interfaces:

```bash
kubectl --context minikube -n vava port-forward --address 0.0.0.0 svc/vava-postgresql 15432:5432
```

Other machines can then connect through your Mac's LAN IP address:

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

Stop the port-forward with `Ctrl+C` after the demo.

## pgAdmin

pgAdmin is optional:

```bash
helm upgrade --install vava ./charts/vava --namespace vava --create-namespace --set pgadmin.enabled=true
kubectl --context minikube -n vava port-forward svc/vava-pgadmin 5050:80
```

Login:

```text
admin@vava.com / admin
```

Register the PostgreSQL server in pgAdmin with:

```text
Host: vava-postgresql
Port: 5432
User: postgres
Password: postgres
Database: vava-restaurant
```

## Reset Database

The init SQL runs only when PostgreSQL starts with an empty data directory. To reset the local minikube database:

```bash
helm uninstall vava -n vava --kube-context minikube
kubectl --context minikube -n vava delete pvc data-vava-postgresql-0
helm upgrade --install vava ./charts/vava --namespace vava --create-namespace --kube-context minikube
```

## JavaFX App Pod

`app.enabled` exists only as an experimental hook for a custom image. It is disabled because this project is a desktop JavaFX app, not an HTTP service.
