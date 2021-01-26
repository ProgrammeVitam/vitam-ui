#!/usr/bin/env bash
set -e

for rpm_file in `find . -name "*.rpm"`
do
    rpm_name=$(basename $rpm_file)
    curl -v --netrc-file /etc/curl.netrc --upload-file $rpm_file https://nexus.teamdlab.com/repository/yum-xelians/$1/$rpm_name
done

