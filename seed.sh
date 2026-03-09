#!/bin/bash

echo "Kompilujem projekt..."
mvn compile -q

echo ""
echo "Spúšťam naplnenie databázy (DatabaseSeeder)..."
mvn exec:java -Dexec.mainClass="dev.vavateam1.data.initializer.DatabaseSeeder" -q

echo "Ukončené."
