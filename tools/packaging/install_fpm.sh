#!/usr/bin/env bash

if [ -f /etc/debian_version ]; then
    echo ">> Debian system detected"
    sudo apt install  ruby ruby-dev rubygems build-essential rpm
elif [ -f /etc/redhat-release ]; then
    echo ">> Redhat system detected"
    sudo yum install ruby-devel gcc make rpm-build rubygems
else
    echo "system not supported for setting up deployer?"
fi

sudo gem install fpm
