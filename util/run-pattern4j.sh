#!/usr/bin/env bash

# Start the Pattern4J analysis
java -Xms32m -Xmx512m \
  -jar ./tools/pattern4.jar \
  -target "./jinput/coreAPI/target/classes"
