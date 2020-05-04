#########################################################################################
# Dockerfile to run VITAMUI
# Based on CentOS
#
# Maintained by vitamui
# Image name: docker.vitamui.com/vitamui-base
#########################################################################################

# Set the base image to Centos 7
FROM centos:7.4.1708
MAINTAINER vitamui

# Make sure the package repository and packages are up to date.
RUN yum install -y epel-release && yum -y update && yum -y upgrade && yum clean all

################################  Configure systemd  ###############################

# Hint for systemd that we are running inside a container
ENV container docker

# Remove useless units
RUN (cd /lib/systemd/system/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done); \
    rm -f /lib/systemd/system/multi-user.target.wants/*;\
    rm -f /etc/systemd/system/*.wants/*;\
    rm -f /lib/systemd/system/local-fs.target.wants/*; \
    rm -f /lib/systemd/system/sockets.target.wants/*udev*; \
    rm -f /lib/systemd/system/sockets.target.wants/*initctl*; \
    rm -f /lib/systemd/system/basic.target.wants/*;\
    rm -f /lib/systemd/system/anaconda.target.wants/*;

################################  Install build tools (rpm / maven / java)  ###############################

RUN yum install -y \
    	java-11-openjdk-devel \
    	rpm-build \
        rpmdevtools \
        initscripts.x86_64 \
        golang \
        npm \
    && yum clean all

# Add Java to configPath
ENV JAVA_HOME /usr/lib/jvm/java

# Install & configure maven
RUN curl -k https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.3.9/apache-maven-3.3.9-bin.tar.gz --output /tmp/maven.tgz \
    && tar xvzf /tmp/maven.tgz --directory /opt \
    && rm -f /tmp/maven.tgz \
    && ln -s /opt/apache-maven-3.3.9 /opt/maven \
    && mkdir -p /devhome/.m2 \
    && chmod -R 777 /devhome

# Add Maven & java to configPath
ENV M2_HOME /opt/maven
ENV PATH ${M2_HOME}/bin:${JAVA_HOME}/bin:${PATH}

################################  Install deployment tools  ###############################

# for sudo in automatic deployment ; note : ansible needs epel repo
RUN yum install -y sudo ansible openssl which && yum clean all

##################################  install git, vim, xml2  #################################

RUN yum install -y git vim xml2 && yum clean all

##################################  Declare local rpm repo  #################################

# Note : declare at the end ; else other yum commandes will fail. Ideally, we would need a "createrepo" here, but it wouldn't work, as the volume /code is normally mounter externally.
RUN yum install -y createrepo \
    && yum clean all

ENV TERM xterm


################################ RSYSLOG ######################################

RUN yum -y update && \
    yum -y install rsyslog && \
    yum clean all && \
    systemctl enable rsyslog && \
    sed -i 's/#$ModLoad imudp/$ModLoad imudp/g' /etc/rsyslog.conf && \
    sed -i 's/#$UDPServerRun 514/$UDPServerRun 514/g' /etc/rsyslog.conf && \
    sed -i 's/#$ModLoad imtcp/$ModLoad imtcp/g' /etc/rsyslog.conf && \
    sed -i 's/#$InputTCPServerRun 514/$InputTCPServerRun 514/g' /etc/rsyslog.conf

CMD ["/usr/sbin/init"]




