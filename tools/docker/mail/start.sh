#!/bin/bash

#########################

docker run --name vitamui-mail -d -p 3000:80 -p 2525:25 rnwood/smtp4dev:latest

sleep 2

echo "vitamui-mail is started"
