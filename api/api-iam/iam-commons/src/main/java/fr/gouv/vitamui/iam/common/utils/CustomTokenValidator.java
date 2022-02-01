package fr.gouv.vitamui.iam.common.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.creator.TokenValidator;

import java.util.Arrays;
import java.util.List;

import static org.pac4j.oidc.profile.OidcProfileDefinition.*;

/**
 * Custom OIDC token validator.
 */
public class CustomTokenValidator extends TokenValidator {

    private static final List<String> AGENTCONNECT_ACR_VALUES = Arrays.asList("eidas1", "eidas2", "eidas3");

    public CustomTokenValidator(final OidcConfiguration configuration) {
        super(configuration);
    }

    public IDTokenClaimsSet validate(final JWT idToken, final Nonce expectedNonce)
        throws BadJOSEException, JOSEException {
        final IDTokenClaimsSet claimsSet = super.validate(idToken, expectedNonce);

        final String acrParam = configuration.getCustomParam("acr_values");
        if (AGENTCONNECT_ACR_VALUES.contains(acrParam)) {
            final String acrClaim = claimsSet.getStringClaim(ACR);
            if (!acrParam.equals(acrClaim)) {
                throw new BadJWTException("[AGENTCONNECT] Bad acr claim in the ID token: it must match the provided value in the acr_values custom param");
            }
        }

        return claimsSet;
    }
}
