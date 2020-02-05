#!/bin/bash
# For installing ansible
yum install -y epel-release
yum install -y python-pip
pip install ansible==2.7
# For installing kse tool
yum install java-1.8.0-openjdk.x86_64 -y

