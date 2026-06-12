#!/bin/bash -e
set -euo pipefail

VENV_BASE=".virtualenvs"
VENV_NAME="vitam-ui"
VENV_PATH="$VENV_BASE/$VENV_NAME"

echo "--- Checking Python version ---"
read python_ver_major python_ver_minor <<< $(python3 -c 'import sys; print(sys.version_info[0], sys.version_info[1])')

echo "Detected Python version: $python_ver_major.$python_ver_minor"

if [ "$python_ver_major" -eq 3 ] && [ "$python_ver_minor" -lt 9 ]; then
    ANSIBLE_VERSION="2.9.27"
else
    ANSIBLE_VERSION="2.14"
fi

echo "Targeting Ansible version: $ANSIBLE_VERSION"

echo "--- Creating VirtualEnv: $VENV_PATH ---"
python3 -m venv "$VENV_PATH"

source "$VENV_PATH/bin/activate"

python -m pip install --upgrade pip
python -m pip install "ansible==$ANSIBLE_VERSION"

ansible --version
