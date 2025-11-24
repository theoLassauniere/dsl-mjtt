#!/bin/bash
java8
# Utilise le premier argument s'il est fourni, sinon utilise 'scripts/Switch.groovy' par défaut.
GROOVY_SCRIPT_PATH="${1:-scripts/Switch.groovy}"

# Exécute le jar avec le chemin du script Groovy déterminé et redirige la sortie.
java -jar target/dsl-groovy-1.0-jar-with-dependencies.jar "$GROOVY_SCRIPT_PATH" > result.ino
