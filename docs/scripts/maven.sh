#!/bin/bash
set -vx
MAVEN_ARCHIVE="apache-maven-3.5.2-bin.tar.gz"
MAVEN_DIR="apache-maven-3.5.2"
MAVEN_URL="http://archive.apache.org/dist/maven/maven-3/3.5.2/binaries/$MAVEN_ARCHIVE"


#### MAVEN ####
cd /usr/local
wget --no-cookies --no-check-certificate $MAVEN_URL
tar -xzvf $MAVEN_ARCHIVE
ln -s $MAVEN_DIR/ apache-maven
chown -R root:staff $MAVEN_DIR/

echo "" >> ~/.bashrc
echo "# Maven Configurations" >> ~/.bashrc
echo "export M2_HOME=/usr/local/apache-maven" >> ~/.bashrc
echo "export PATH=\${M2_HOME}/bin:\${PATH}" >> ~/.bashrc
echo "" >> ~/.bashrc
rm -Rf $MAVEN_ARCHIVE
