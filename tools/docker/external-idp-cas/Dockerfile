# Documentation:
#  https://www.keycloak.org/server/containers

ARG KEYCLOAK_VERSION

FROM quay.io/keycloak/keycloak:$KEYCLOAK_VERSION as builder

ENV KC_FEATURES="token-exchange,scripts,preview"

WORKDIR /opt/keycloak

RUN /opt/keycloak/bin/kc.sh build --cache=ispn --health-enabled=true --metrics-enabled=true

FROM quay.io/keycloak/keycloak:$KEYCLOAK_VERSION

LABEL image.version=$KEYCLOAK_VERSION

COPY --from=builder /opt/keycloak/ /opt/keycloak/

# https://github.com/keycloak/keycloak/issues/19185#issuecomment-1480763024
USER root
RUN sed -i '/disabledAlgorithms/ s/ SHA1,//' /etc/crypto-policies/back-ends/java.config

USER keycloak

RUN /opt/keycloak/bin/kc.sh show-config

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "-v", "start-dev","--import-realm"]
