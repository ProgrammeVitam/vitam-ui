/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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
                throw new BadJWTException(
                    "[AGENTCONNECT] Bad acr claim in the ID token: it must match the provided value in the acr_values custom param"
                );
            }
        }

        return claimsSet;
    }
}
