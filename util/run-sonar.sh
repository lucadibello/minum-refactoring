#!/usr/bin/env bash

# Run the SonarQube analysis
sonar-scanner \
  -Dsonar.projectKey=jinput \
  -Dsonar.sources=jinput/coreAPI \
  -Dsonar.java.binaries=jinput/coreAPI/target \
  -Dsonar.scm.exclusions.disabled=true \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password="Password&1!" \
  -Dsonar.scanner.skipJreProvisioning=true
