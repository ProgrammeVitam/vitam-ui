#!/bin/bash

# Start Docker Compose in detached mode
docker-compose up --build -d

# Wait for the container to be up
echo "Waiting for the container to be up..."
docker-compose ps | grep -q "vitamui-keycloak"
while [ $? -ne 0 ]; do
    sleep 1
    docker-compose ps | grep -q "vitamui-keycloak"
done


echo "Waiting for the container to be healthy..."
while true; do
    status_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8041/health/ready)
    if [ "$status_code" -eq 200 ]; then
        echo "Container is healthy. Executing curl query..."
        break
    fi
    sleep 1
done

# Execute curl query and extract access_token
echo "Container is up. connect confidential client ..."
response=$(curl --location 'http://localhost:8041/realms/vitamui-test/protocol/openid-connect/token' \
           --header 'Content-Type: application/x-www-form-urlencoded' \
           --data-urlencode 'grant_type=client_credentials' \
           --data-urlencode 'client_id=vitamui-oidc' \
           --data-urlencode 'client_secret=SLvdirMs0GAxGfWolc4qxPLtArvGhDlS')

admin_access_token=$(echo "$response" | jq -r '.access_token')


# Check if access_token is not empty
if [ -n "admin_access_token" ]; then

  # Perform create user request
  create_user_response=$(curl -s -X POST -H "Authorization: Bearer $access_token" -H 'Content-Type: application/json' -d '{"username":"demo@change-me.fr","email":"demo@change-me.fr","lastName":"vitamui","firstName":"demo oidc","enabled":"true","emailVerified":"true"}' http://localhost:8041/admin/realms/vitamui-test/users)
  create_user_response_status_code=$(echo "create_user_response" | jq -r '.status')

 # Check if the response is 200
    if [ "$create_user_response_status_code" -eq 200 ]; then
        echo "POST request successful."
    else
        echo "POST request failed with status code $create_user_response_status_code"
    fi

else
    echo "Failed to extract access token."
fi
