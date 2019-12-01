#!/usr/bin/env bash

./gradlew build && docker-compose up -d --build backend
