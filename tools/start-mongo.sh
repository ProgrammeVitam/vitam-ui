#!/bin/bash -e

# Use the centralized Ansible setup script in the same directory (tools/)
echo "--- Using centralized Ansible setup script ---"
VENV_NAME_MONGO=".virtualenvs/vitam-ui"
bash "$(dirname "$0")/setup_ansible_venv.sh" "$VENV_NAME_MONGO"
source "$(dirname "$0")/$VENV_NAME_MONGO/bin/activate"

cd "$(dirname "$0")/docker/mongo" || return 1
./start_dev.sh
