#########################################################################################
# Dockerfile t o run VITAMUI
# Based on CentOS
#
# Maintained by vitamui
# Image name: docker.vitamui.com/vitamui-base
#########################################################################################

# Set the base image to Centos 7
FROM centos:7
MAINTAINER vitamui

#########################################################################################
#  Upgrade system packages

# Make sure the package repository and packages are up to date.
RUN yum install -y epel-release && yum -y update && yum -y upgrade && yum clean all

#########################################################################################
#  Configure systemd

# Hint for systemd that we are running inside a container
ENV container docker

## Remove useless units
RUN (cd /lib/systemd/system/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done); \
	rm -f /lib/systemd/system/multi-user.target.wants/*;\
	rm -f /etc/systemd/system/*.wants/*;\
	rm -f /lib/systemd/system/local-fs.target.wants/*; \
	rm -f /lib/systemd/system/sockets.target.wants/*udev*; \
	rm -f /lib/systemd/system/sockets.target.wants/*initctl*; \
	rm -f /lib/systemd/system/basic.target.wants/*;\
	rm -f /lib/systemd/system/anaconda.target.wants/*;

#########################################################################################
#  Setup user and ssh

# Setup a user vitamuiadmin with vitamuiadmin password and sudoer rights with password
RUN yum install -y sudo
RUN adduser -d /vitamuiadmin/ -m vitamuiadmin
RUN echo "vitamuiadmin ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers.d/vitamuiadmin
RUN echo "vitamuiadmin:vitamuiadmin" | chpasswd

# Setup ssh
RUN yum install -y openssh-server
ADD ssh/sshd_config /etc/ssh/sshd_config
ADD ssh/vitamuiadmin_rsa.pub  /vitamuiadmin/vitamuiadmin_rsa.pub
RUN chmod 644 /vitamuiadmin/vitamuiadmin_rsa.pub

# To avoid "System is booting up. See pam_nologin(8)" error.
# Can also remove /var/run/nologin file
RUN  ln  -s /dev/null /etc/tmpfiles.d/systemd-nologin.conf

# Expose ssh port non standard
EXPOSE 6622


#########################################################################################
# End config
ENV TERM xterm
CMD ["/usr/sbin/init"]




