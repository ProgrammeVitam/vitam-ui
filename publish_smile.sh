#!/usr/bin/env bash
set -e

# Specific script for internal deploy on smile nexus
# Copy in vitadmin project and execute after building the rpms with :
# mvn clean install -DskipTests -pl '!cots/vitamui-mongo-express' -Pvitam-external,rpm,webpack,skipTestsRun
for rpm_file in `find . -name "*.rpm"`
do
    echo "BEGIN ${rpm_file}"
    rpm_file_clean=$(basename -- ${rpm_file})
    echo "BASENAME ${rpm_file_clean}"
#    curl --fail -v --upload-file ${rpm_file} http://nexus.vitry.intranet/repository/RPM-Vitam-SNAPSHOT/vitadmin/${rpm_file_clean}
    cp ${rpm_file} ../../documents/livraison_cea_lot1_fixes/
    echo "END ${rpm_file}"
done
