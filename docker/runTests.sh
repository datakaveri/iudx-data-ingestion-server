#!/bin/bash

nohup mvn clean compile exec:java@data-ingestion-server & 
sleep 20
mvn clean test-compile surefire:test surefire-report:report
cp -r target /tmp/test/