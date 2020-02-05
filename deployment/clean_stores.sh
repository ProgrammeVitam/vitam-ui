#!/usr/bin/env bash

#
# Script de nettoyage des stores généres pendant une installation locale
#
#


find . -name "*.jks" -exec rm {} \;
find . -name "*.p12" -exec rm {} \;
