#!/usr/bin/env bash
set -e

for rpm_file in `find . -name "*.rpm"`
do
    curl  --fail --netrc-file /etc/curl.netrc -F "file[]=@${rpm_file}" "$1"
done
