#!/bin/bash
set -evx
ECLIPSE_ARCHIVE="eclipse-jee-oxygen-3-linux-gtk-x86_64.tar.gz"
ECLIPSE_DIR="eclipse"
ECLIPSE_URL="https://archive.eclipse.org/technology/epp/downloads/release/oxygen/3/$ECLIPSE_ARCHIVE"
# Published in $ECLIPSE_URL.sha512
ECLIPSE_SHA512="dfcfde9550fbf9fb4ecda5900bccbcc4c2d3c8812ecd126690de24f5187e2f10e8e7078269423f186c441d61e77e022181a7d550138b57c5ce0dd56f9ecce7f2"


#### ECLIPSE ####
cd /usr/local
curl -fsSL --proto '=https' "$ECLIPSE_URL" -o "$ECLIPSE_ARCHIVE"
echo "$ECLIPSE_SHA512  $ECLIPSE_ARCHIVE" | sha512sum -c -
tar -xzvf $ECLIPSE_ARCHIVE
chown -R root:staff $ECLIPSE_DIR/

echo "" >> ~/.bashrc
echo "# Eclipse Configurations" >> ~/.bashrc
echo "export ECLIPSE_HOME=/usr/local/eclipse" >> ~/.bashrc
echo "export PATH=\${ECLIPSE_HOME}:\${PATH}" >> ~/.bashrc
echo "" >> ~/.bashrc
rm -Rf $ECLIPSE_ARCHIVE
