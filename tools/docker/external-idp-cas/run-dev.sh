#!/bin/bash



# Docker Compose service name
KEYCLOAK_SERVICE_NAME="vitamui-keycloak"
# Keycloak API URL
KEYCLOAK_API_URL="http://localhost:8041"
# Keycloak realm name
REALM="vitamui-test"
# Client ID for admin operations
ADMIN_CLIENT_ID="vitamui-internal-api"
# Client secret for admin operations
ADMIN_CLIENT_SECRET="QQXbm6947N5kYVL0yLDAHwlo3ZW2I8ui"
# Username demo to create
USERNAME="demo@change-me.fr"

# User details for creation

USER_JSON="{\"username\":\"$USERNAME\",\"email\":\"$USERNAME\",\"lastName\":\"vitamui\",\"firstName\":\"demo oidc\",\"enabled\":\"true\",\"emailVerified\":\"true\",\"credentials\":[{\"type\":\"password\",\"value\":\"ChangeIt.2024\",\"temporary\":false}]}"


# Check if JQ is installed
if ! command -v jq &> /dev/null; then
    echo "JQ is not installed. Installing..."
    # Install JQ (assuming a Debian-based system)
    sudo apt-get update
    sudo apt-get install -y jq
    # Check if installation was successful
    if ! command -v jq &> /dev/null; then
        echo "Failed to install JQ. Please install it manually."
        exit 1
    fi
    echo "JQ has been installed successfully."
fi


# Start Docker Compose in detached mode
docker-compose up --build -d

# Function to wait for the container to be up
wait_for_container() {
    echo "Waiting for the container $KEYCLOAK_SERVICE_NAME to be up..."
    docker-compose ps | grep -q "$KEYCLOAK_SERVICE_NAME"
    while [ $? -ne 0 ]; do
        sleep 1
        docker-compose ps | grep -q "$KEYCLOAK_SERVICE_NAME"
    done
}

# Wait for the Keycloak container to be up
wait_for_container

# Function to check if container is healthy
check_container_health() {
    echo "Waiting for the container to be healthy..."
    while true; do
        status_code=$(curl -s -o /dev/null -w "%{http_code}" "$KEYCLOAK_API_URL/health/ready")
        if [ "$status_code" -eq 200 ]; then
            echo "Container is healthy."
            break
        fi
        sleep 1
    done
}

# Check container health
check_container_health

# Function to obtain access token
get_access_token() {
    response=$(curl --location "$KEYCLOAK_API_URL/realms/$REALM/protocol/openid-connect/token" \
               --header 'Content-Type: application/x-www-form-urlencoded' \
               --data-urlencode "grant_type=client_credentials" \
               --data-urlencode "client_id=$ADMIN_CLIENT_ID" \
               --data-urlencode "client_secret=$ADMIN_CLIENT_SECRET")
    echo "$response" | jq -r '.access_token'
}

# Obtain admin access token
admin_access_token=$(get_access_token)

# Check if admin_access_token is not empty
if [ -n "$admin_access_token" ]; then
    # Check if the user already exists
    user_exists_response=$(curl -s -H "Authorization: Bearer $admin_access_token" -H 'Content-Type: application/json' "$KEYCLOAK_API_URL/admin/realms/$REALM/users?username=$USERNAME")
    user_exists=$(echo "$user_exists_response" | jq -r '. | length')

    if [ "$user_exists" -gt 0 ]; then
        echo "User already exists. Skipping user creation."
    else
        # Create the user
        create_user_response=$(curl -s -X POST -H "Authorization: Bearer $admin_access_token" -H 'Content-Type: application/json' -d "$USER_JSON" "$KEYCLOAK_API_URL/admin/realms/$REALM/users")
        create_user_response_status_code=$(echo "$create_user_response" | jq -r '.status')

        # Verify if the user was created
        verify_user_response=$(curl -s -H "Authorization: Bearer $admin_access_token" -H 'Content-Type: application/json' "$KEYCLOAK_API_URL/admin/realms/$REALM/users?username=$USERNAME")
        verify_user_exists=$(echo "$verify_user_response" | jq -r '. | length')

        if [ "$verify_user_exists" -gt 0 ]; then
            echo "Success: User created."
        else
            echo "Failed to verify user creation."
        fi
    fi
else
    echo "Failed to extract access token."
fi
