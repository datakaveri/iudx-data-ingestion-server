#!/bin/bash

# To be executed from project root
docker build -t iudx/di-depl:latest -f docker/depl.dockerfile .
docker build -t iudx/di-dev:latest -f docker/dev.dockerfile .
