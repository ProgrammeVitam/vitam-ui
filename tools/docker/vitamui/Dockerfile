#########################################################################################
# Dockerfile to install VITAMUI
# Based on CentOS
#
# Maintained by vitamui
# Image name:  docker.vitamui.com/vitamui
#########################################################################################

# Parsing build-args:
# Base image
FROM docker.vitamui.com/vitamui-base
ARG VITAMUI_VERSION

# Importing repository directory created by maven
ADD target/ /workspace/

# Creating local repository
RUN createrepo /workspace/repository/

# Make adminscript executables
RUN chmod +x /workspace/deployment/*.sh

