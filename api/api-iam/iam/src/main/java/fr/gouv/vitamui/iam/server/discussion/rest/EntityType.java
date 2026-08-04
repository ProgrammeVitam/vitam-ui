package fr.gouv.vitamui.iam.server.discussion.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import lombok.Getter;

/**
 * Maps an entity type (used by discussions) with the corresponding role
 */
public enum EntityType {
    PROJECT(ServicesData.ROLE_GET_PROJECTS),
    TRANSACTION(ServicesData.ROLE_GET_TRANSACTIONS);

    @Getter
    private final String role;

    EntityType(String role) {
        this.role = role;
    }
}
