#!/bin/bash -e
set -euo pipefail

VENV_BASE=".virtualenvs"
VENV_NAME="vitam-ui"
VENV_PATH="$VENV_BASE/$VENV_NAME"

echo "--- Checking Python version ---"
read python_ver_major python_ver_minor <<< $(python3 -c 'import sys; print(sys.version_info[0], sys.version_info[1])')

echo "Detected Python version: $python_ver_major.$python_ver_minor"

# On adapte le package et la version selon la version de Python
if [ "$python_ver_major" -eq 3 ] && [ "$python_ver_minor" -lt 9 ]; then
    ANSIBLE_PACKAGE="ansible==2.9.27"
else
    # Pour Python 3.9+ : On cible explicitement ansible-core 2.14
    ANSIBLE_PACKAGE="ansible-core==2.14.*"
fi

echo "Targeting package: $ANSIBLE_PACKAGE"

echo "--- Creating VirtualEnv: $VENV_PATH ---"
python3 -m venv "$VENV_PATH"

source "$VENV_PATH/bin/activate"

python -m pip install --upgrade pip
# On passe directement la variable contenant le bon package et la bonne version
python -m pip install $ANSIBLE_PACKAGE

ansible --version
