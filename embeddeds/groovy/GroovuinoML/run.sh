#!/bin/bash

# Usage:
#   ./run.sh [-b] [-k] [chemin/script.groovy]
#   -b : build du projet DSL Groovy (mvn clean install -U assembly:single)
#   -k : build du kernel dans ../../kernels/jvm (mvn clean install)

BUILD_DSL=false
BUILD_KERNEL=false

while getopts "bk" opt; do
  case "$opt" in
    b) BUILD_DSL=true ;;
    k) BUILD_KERNEL=true ;;
    *) echo "Usage: $0 [-b] [-k] [script.groovy]" >&2
       exit 1 ;;
  esac
done

shift $((OPTIND - 1))

# Build du kernel si demandé
if [ "$BUILD_KERNEL" = true ]; then
  KERNEL_DIR="../../../kernels/jvm"
  if [ ! -d "$KERNEL_DIR" ]; then
    echo "Répertoire kernel introuvable: $KERNEL_DIR" >&2
    exit 1
  fi
  (
    cd "$KERNEL_DIR" && mvn clean install
  ) || exit 1
fi

# Build du DSL Groovy si demandé
if [ "$BUILD_DSL" = true ]; then
 java8 &&  mvn clean install -U assembly:single || exit 1
fi

GROOVY_SCRIPT_PATH="${1:-scripts/Switch.groovy}"

BASENAME="$(basename "$GROOVY_SCRIPT_PATH" .groovy)"

OUT_DIR="./demo/generated"
mkdir -p "$OUT_DIR"

OUT_FILE="$OUT_DIR/$BASENAME.ino"

java8 && java -jar target/dsl-groovy-1.0-jar-with-dependencies.jar "$GROOVY_SCRIPT_PATH" > "$OUT_FILE"