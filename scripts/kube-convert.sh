#!/bin/sh

cd deploy/kubernetes/
kompose -f ../../docker-compose.yml convert
