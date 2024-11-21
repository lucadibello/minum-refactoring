#!/usr/bin/env bash

# Run the SonarQube analysis
sonar-scanner \
  -Dsonar.projectKey=minum \
  -Dsonar.sources=minum/src \
  -Dsonar.java.binaries=minum/target/classes \
  -Dsonar.scm.exclusions.disabled=true \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password="Password&1!" \
  -Dsonar.scanner.skipJreProvisioning=true
