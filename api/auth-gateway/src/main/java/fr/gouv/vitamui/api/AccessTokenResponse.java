package fr.gouv.vitamui.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO représentant le jeton d'application enrichi.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessTokenResponse {

    private String accessToken;
    private String tokenType = "Bearer";

    /**
     * Constructor for simple usage by AuthenticationController.
     */
    public AccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
