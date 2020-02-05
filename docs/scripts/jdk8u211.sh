#!/bin/bash
set -vx
JAVA_ARCHIVE="jdk-8u211-linux-x64.tar.gz"
JAVA_DIR="jdk1.8.0_211"
fileid="118CHBep4FXNZ18bw1Ver5-P8W0cjoZTL"

JCE_ARCHIVE="jce_policy-8.zip"
JCE_DIR="UnlimitedJCEPolicyJDK8"
JCE_URL="http://download.oracle.com/otn-pub/java/jce/8/$JCE_ARCHIVE"

#### CURL ####
apt install curl

#### JDK8 ####
cd /usr/local
curl -c ./cookie -s -L "https://drive.google.com/uc?export=download&id=${fileid}" > /dev/null
curl -Lb ./cookie "https://drive.google.com/uc?export=download&confirm=`awk '/download/ {print $NF}' ./cookie`&id=${fileid}" -o ${JAVA_ARCHIVE}

tar -xzvf $JAVA_ARCHIVE
ln -s $JAVA_DIR/ java
chown -R root:staff $JAVA_DIR/
update-alternatives --install /usr/bin/java java /usr/local/java/bin/java 2048
rm -Rf $JAVA_ARCHIVE

echo "" >> ~/.bashrc
echo "# Java Configurations" >> ~/.bashrc
echo "export JAVA_HOME=/usr/local/java" >> ~/.bashrc
echo "export PATH=\${JAVA_HOME}/bin:\${PATH}" >> ~/.bashrc
echo "" >> ~/.bashrc

#### JCE8 ####
cd /usr/local/java/jre/lib/security
apt-get install -y unzip
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" $JCE_URL
unzip $JCE_ARCHIVE
cd $JCE_DIR
mv *.jar ..
cd ..
rm -Rf $JCE_DIR/ $JCE_ARCHIVE
