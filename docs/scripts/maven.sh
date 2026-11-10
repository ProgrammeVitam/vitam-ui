#!/bin/bash
set -evx
MAVEN_ARCHIVE="apache-maven-3.6.3-bin.tar.gz"
MAVEN_DIR="apache-maven-3.6.3"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/$MAVEN_ARCHIVE"
# Published in $MAVEN_URL.sha512
MAVEN_SHA512="c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0"


#### MAVEN ####
cd /usr/local
curl -fsSL --proto '=https' "$MAVEN_URL" -o "$MAVEN_ARCHIVE"
echo "$MAVEN_SHA512  $MAVEN_ARCHIVE" | sha512sum -c -
tar -xzvf $MAVEN_ARCHIVE
ln -s $MAVEN_DIR/ apache-maven
chown -R root:staff $MAVEN_DIR/

echo "" >> ~/.bashrc
echo "# Maven Configurations" >> ~/.bashrc
echo "export M2_HOME=/usr/local/apache-maven" >> ~/.bashrc
echo "export PATH=\${M2_HOME}/bin:\${PATH}" >> ~/.bashrc
echo "" >> ~/.bashrc
rm -Rf $MAVEN_ARCHIVE
