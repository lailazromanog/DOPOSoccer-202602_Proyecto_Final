#!/bin/bash

# Script de compilacion, pruebas y cobertura para DOPOSoccer
# Uso: bash run_tests.sh


# Colores para la salida en consola
VERDE="\033[0;32m"
ROJO="\033[0;31m"
RESET="\033[0m"

# Separador del classpath: en Windows (Git Bash) es punto y coma, en Linux y Mac son dos puntos
SEP=":"
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
esac

echo ""
echo "=== DOPOSoccer: compilacion + pruebas + cobertura ==="
echo ""

# --- 1. Limpiar y preparar la carpeta de compilados ---
mkdir -p build
rm -rf build/*
echo "Carpeta build lista."

# --- 2. Compilar solo el codigo fuente principal (sin tests ni presentacion aun) ---
echo ""
echo "Compilando dominio y presentacion..."
javac --release 8 -d build \
      -cp "lib/junit-4.13.2.jar${SEP}lib/hamcrest-core-1.3.jar${SEP}src" \
      src/domain/*.java src/presentation/*.java

if [ $? -ne 0 ]; then
    echo -e "${ROJO}ERROR: fallo la compilacion del codigo principal.${RESET}"
    exit 1
fi
echo -e "${VERDE}Compilacion principal exitosa.${RESET}"

# --- 3. Compilar los tests (necesitan el codigo principal ya compilado) ---
echo ""
echo "Compilando tests..."
javac --release 8 -d build \
      -cp "lib/junit-4.13.2.jar${SEP}lib/hamcrest-core-1.3.jar${SEP}src${SEP}build" \
      test/*.java

if [ $? -ne 0 ]; then
    echo -e "${ROJO}ERROR: fallo la compilacion de los tests.${RESET}"
    exit 1
fi
echo -e "${VERDE}Compilacion de tests exitosa.${RESET}"

# --- 4. Correr los tests con el agente JaCoCo para medir cobertura ---
# El agente genera jacoco.exec con los datos de cobertura
echo ""
echo "Corriendo tests con JaCoCo..."
java -javaagent:lib/jacocoagent.jar=destfile=jacoco.exec \
     -cp "build${SEP}lib/junit-4.13.2.jar${SEP}lib/hamcrest-core-1.3.jar" \
     org.junit.runner.JUnitCore \
     DOPOSoccerTest PowerTest FactoryTest ProfileTest PersistenceTest ProgressTest HealthTest

RESULTADO=$?
echo ""

if [ $RESULTADO -ne 0 ]; then
    echo -e "${ROJO}Algunas pruebas fallaron. Revisa el detalle arriba.${RESET}"
else
    echo -e "${VERDE}Todas las pruebas pasaron.${RESET}"
fi

# --- 5. Generar reporte HTML de cobertura con jacococli ---
# REQUISITO: tener lib/jacococli.jar descargado.
# Descargalo desde: https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.11/org.jacoco.cli-0.8.11-nodeps.jar
# y guardalo como lib/jacococli.jar

if [ -f "lib/jacococli.jar" ]; then
    echo ""
    echo "Generando reporte HTML de cobertura..."
    mkdir -p coverage-report
    java -jar lib/jacococli.jar report jacoco.exec \
         --classfiles build \
         --sourcefiles src \
         --html coverage-report \
         --xml coverage-report/jacoco.xml

    if [ $? -eq 0 ]; then
        echo -e "${VERDE}Reporte generado en: coverage-report/index.html${RESET}"
        echo "Abri ese archivo en tu navegador para ver el porcentaje de cobertura."
    else
        echo -e "${ROJO}ERROR: no se pudo generar el reporte HTML.${RESET}"
    fi
else
    echo ""
    echo "Para generar el reporte HTML descarga jacococli.jar:"
    echo "  URL: https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.11/org.jacoco.cli-0.8.11-nodeps.jar"
    echo "  Guardalo como: lib/jacococli.jar"
    echo "  Despues vuelve a ejecutar este script."
fi

echo ""
echo "=== Fin ==="
