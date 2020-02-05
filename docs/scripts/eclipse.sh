#!/bin/bash
set -vx
ECLIPSE_ARCHIVE="eclipse-jee-oxygen-3-linux-gtk-x86_64.tar.gz"
ECLIPSE_DIR="eclipse"
ECLIPSE_URL="https://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/oxygen/3/$ECLIPSE_ARCHIVE"


#### ECLIPSE ####
cd /usr/local
wget --no-cookies --no-check-certificate $ECLIPSE_URL -O $ECLIPSE_ARCHIVE
tar -xzvf $ECLIPSE_ARCHIVE
chown -R root:staff $ECLIPSE_DIR/

echo "" >> ~/.bashrc
echo "# Eclipse Configurations" >> ~/.bashrc
echo "export ECLIPSE_HOME=/usr/local/eclipse" >> ~/.bashrc
echo "export PATH=\${ECLIPSE_HOME}:\${PATH}" >> ~/.bashrc
echo "" >> ~/.bashrc
rm -Rf $ECLIPSE_ARCHIVE
