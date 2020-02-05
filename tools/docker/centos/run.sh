#!/usr/bin/env bash

docker-compose build
docker-compose up -d
docker-compose exec centos bash
