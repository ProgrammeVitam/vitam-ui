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
package fr.gouv.vitamui.cas.provider;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import org.pac4j.core.client.IndirectClient;

/**
 * Pac4j client identity provider.
 *
 *
 */
public class Pac4jClientIdentityProviderDto extends IdentityProviderDto {

    private final IndirectClient client;

    public Pac4jClientIdentityProviderDto(final IdentityProviderDto dto, final IndirectClient client) {
        setId(dto.getId());
        setName(dto.getName());
        setTechnicalName(dto.getTechnicalName());
        setInternal(dto.getInternal());
        setEnabled(dto.getEnabled());
        setPatterns(dto.getPatterns());
        setReadonly(dto.isReadonly());
        setCustomerId(dto.getCustomerId());
        setMailAttribute(dto.getMailAttribute());
        setIdentifierAttribute(dto.getIdentifierAttribute());
        setAutoProvisioningEnabled(dto.isAutoProvisioningEnabled());

        setKeystoreBase64(dto.getKeystoreBase64());
        setKeystorePassword(dto.getKeystorePassword());
        setPrivateKeyPassword(dto.getPrivateKeyPassword());
        setIdpMetadata(dto.getIdpMetadata());
        setSpMetadata(dto.getSpMetadata());
        setMaximumAuthenticationLifetime(dto.getMaximumAuthenticationLifetime());
        setAuthnRequestBinding(dto.getAuthnRequestBinding());

        setClientId(dto.getClientId());
        setClientSecret(dto.getClientSecret());
        setDiscoveryUrl(dto.getDiscoveryUrl());
        setScope(dto.getScope());
        setPreferredJwsAlgorithm(dto.getPreferredJwsAlgorithm());
        setCustomParams(dto.getCustomParams());
        setUseState(dto.getUseState());
        setUseNonce(dto.getUseNonce());
        setUsePkce(dto.getUsePkce());
        this.client = client;
    }

    public IndirectClient getClient() {
        return client;
    }
}
