package fr.gouv.vitamui.iam.common.utils;

import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.metadata.OidcOpMetadataResolver;
import org.pac4j.oidc.profile.creator.TokenValidator;

/** Custom OIDC metadata resolver. */
public class CustomOidcOpMetadataResolver extends OidcOpMetadataResolver {

    public CustomOidcOpMetadataResolver(final OidcConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected TokenValidator createTokenValidator() {
        return new CustomTokenValidator(configuration, this.loaded);
    }
}
