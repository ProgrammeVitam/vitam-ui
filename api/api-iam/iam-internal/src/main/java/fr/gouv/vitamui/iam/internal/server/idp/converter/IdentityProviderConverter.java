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
package fr.gouv.vitamui.iam.internal.server.idp.converter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;

public class IdentityProviderConverter implements Converter<IdentityProviderDto, IdentityProvider> {

    public static final String NAME_KEY = "Nom";

    public static final String INTERNAL_KEY = "Type";

    public static final String ENABLED_KEY = "Statut";

    public static final String PATTERNS_KEY = "Pattern";

    public static final String TECHNICAL_NAME_KEY = "Nom technique";

    public static final String KEYSTORE_BASE_64_KEY = "Keystore";

    public static final String KEYSTORE_PASSWORD_KEY = "Mot de passe du keystore";

    public static final String PRIVATE_KEY_PASSWORD_KEY = "Mot de passe de la clé privé";

    public static final String IDP_METADATA_KEY = "Idp Metadata";

    public static final String SP_METADATA_KEY = "Sp metadata";

    public static final String MAIL_ATTRIBUTE_KEY = "Attribut mail";

    public static final String MAXIMUM_AUTHENTICATION_LIFE_TIME = "Temps maximum de connexion";

    private final SpMetadataGenerator spMetadataGenerator;

    public IdentityProviderConverter(final SpMetadataGenerator spMetadataGenerator) {
        this.spMetadataGenerator = spMetadataGenerator;
    }

    @Override
    public String convertToLogbook(final IdentityProviderDto dto) {
        final Map<String, String> logbookData = new LinkedHashMap<>();
        logbookData.put(NAME_KEY, dto.getName());
        logbookData.put(TECHNICAL_NAME_KEY, dto.getTechnicalName());
        logbookData.put(INTERNAL_KEY, String.valueOf(dto.getInternal()));
        logbookData.put(ENABLED_KEY, String.valueOf(dto.getEnabled()));
        logbookData.put(PATTERNS_KEY, dto.getPatterns().toString());
        logbookData.put(MAIL_ATTRIBUTE_KEY, String.valueOf(dto.getMailAttribute()));
        logbookData.put(MAXIMUM_AUTHENTICATION_LIFE_TIME, String.valueOf(dto.getMaximumAuthenticationLifetime()));
        return ApiUtils.toJson(logbookData);
    }

    @Override
    public IdentityProvider convertDtoToEntity(final IdentityProviderDto dto) {
        final IdentityProvider provider = new IdentityProvider();
        provider.setId(dto.getId());
        provider.setIdentifier(dto.getIdentifier());
        provider.setName(dto.getName());
        provider.setEnabled(dto.getEnabled());
        provider.setInternal(dto.getInternal());
        provider.setTechnicalName(dto.getTechnicalName());
        convertPatterns(dto, provider);
        provider.setKeystoreBase64(dto.getKeystoreBase64());
        provider.setKeystorePassword(dto.getKeystorePassword());
        provider.setPrivateKeyPassword(dto.getKeystorePassword());
        dto.setPrivateKeyPassword(dto.getKeystorePassword());
        provider.setIdpMetadata(dto.getIdpMetadata());
        provider.setMailAttribute(dto.getMailAttribute());


        final String spMetadata = spMetadataGenerator.generate(dto);
        provider.setSpMetadata(spMetadata);
        provider.setCustomerId(dto.getCustomerId());
        provider.setMaximumAuthenticationLifetime(dto.getMaximumAuthenticationLifetime());
        provider.setReadonly(dto.isReadonly());
        return provider;
    }

    private void convertPatterns(final IdentityProviderDto dto, final IdentityProvider provider) {
        if (dto.getPatterns() != null && dto.getPatterns().size() > 0) {
            dto.setPatterns(dto.getPatterns().stream().map(s -> s.startsWith(".*@") ? s : ".*@" + s).collect(Collectors.toList()));
        }
        provider.setPatterns(dto.getPatterns());
    }

    /**
     * Build technicalName for an IDP.
     * @param name
     * @return
     */
    public String buildTechnicalName(final String name) {
        final int nameHash = Math.abs(name.hashCode());
        final String sHash = StringUtils.substring(String.valueOf(nameHash), 0, 6);
        return "idp" + sHash;
    }

    @Override
    public IdentityProviderDto convertEntityToDto(final IdentityProvider provider) {
        final IdentityProviderDto dto = new IdentityProviderDto();
        VitamUIUtils.copyProperties(provider, dto);
        return dto;
    }
}
