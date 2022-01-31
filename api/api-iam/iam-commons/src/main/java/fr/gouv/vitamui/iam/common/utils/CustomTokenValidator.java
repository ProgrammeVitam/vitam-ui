package fr.gouv.vitamui.iam.common.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.creator.TokenValidator;

/**
 * Custom OIDC token validator.
 */
public class CustomTokenValidator extends TokenValidator {

    public CustomTokenValidator(final OidcConfiguration configuration) {
        super(configuration);
    }

    public IDTokenClaimsSet validate(final JWT idToken, final Nonce expectedNonce)
        throws BadJOSEException, JOSEException {
        return super.validate(idToken, expectedNonce);
    }
}
