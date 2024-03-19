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
package fr.gouv.vitamui.iam.external.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderBuilder;
import fr.gouv.vitamui.iam.external.server.domain.dto.ProviderPatchType;
import fr.gouv.vitamui.iam.internal.client.IdentityProviderInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The service to read, create, update and delete the identity providers.
 */
@Getter
@Setter
@Service
public class IdentityProviderExternalService
    extends AbstractResourceClientService<IdentityProviderDto, IdentityProviderDto> {

    @Autowired
    private final IdentityProviderInternalRestClient identityProviderInternalRestClient;

    public IdentityProviderExternalService(final IdentityProviderInternalRestClient identityProviderInternalRestClient,
        final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.identityProviderInternalRestClient = identityProviderInternalRestClient;
    }

    @Override
    public IdentityProviderDto create(final IdentityProviderDto dto) {
        return super.create(dto);
    }

    public IdentityProviderDto mapToIdentityProviderDto(final MultipartFile keystore, final MultipartFile idpMetadata,
        final String provider)
        throws Exception {
        IdentityProviderDto dto = new ObjectMapper().readValue(provider, IdentityProviderDto.class);
        final IdentityProviderBuilder builder =
            new IdentityProviderBuilder(dto.getName(), dto.getTechnicalName(), dto.getEnabled(), dto.getInternal(),
                dto.getPatterns(), Objects.nonNull(keystore) ? new ByteArrayResource(keystore.getBytes()) : null,
                dto.getKeystorePassword(), dto.getPrivateKeyPassword(),
                Objects.nonNull(idpMetadata) ? new ByteArrayResource(idpMetadata.getBytes()) : null,
                dto.getCustomerId(), dto.isReadonly(), dto.getMailAttribute(), dto.getIdentifierAttribute(),
                dto.getMaximumAuthenticationLifetime(), dto.getAuthnRequestBinding(),
                Objects.isNull(dto.getWantsAssertionsSigned()) ? false : dto.getWantsAssertionsSigned(),
                Objects.isNull(dto.getAuthnRequestSigned()) ? false : dto.getAuthnRequestSigned(),
                dto.isAutoProvisioningEnabled(), dto.getClientId(),
                dto.getClientSecret(), dto.getDiscoveryUrl(), dto.getScope(), dto.getPreferredJwsAlgorithm(),
                dto.getCustomParams(), dto.getUseState(), dto.getUseNonce(), dto.getUsePkce(), dto.getProtocoleType());
        return builder.build();
    }

    @Override
    public IdentityProviderDto patch(final Map<String, Object> partialDto) {
        return super.patch(partialDto);
    }

    protected void beforePatch(final Map<String, Object> partialDto, final MultipartFile keystore,
        final MultipartFile idpMetadata, final String id,
        final ProviderPatchType patchType) {
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for patch.");
        switch (patchType) {
            case KEYSTORE:
                Assert.isTrue(
                    StringUtils.isNotEmpty((String) partialDto.get("keystorePassword")) && !keystore.isEmpty(),
                    "The keystorePassword is missing");
                break;
            case IDPMETADATA:
                Assert.isTrue(!idpMetadata.isEmpty(), "idpMetadata is missing");
                break;
            default:
                break;
        }
    }

    public IdentityProviderDto patch(final Map<String, Object> partialDto, final MultipartFile keystore,
        final MultipartFile idpMetadata, final String id, final ProviderPatchType patchType) {
        beforePatch(partialDto, keystore, idpMetadata, id, patchType);
        switch (patchType) {
            case KEYSTORE:
                final String keystoreBase64 = getKeystoreBase64(keystore);
                partialDto.put("keystoreBase64", keystoreBase64);
                break;
            case IDPMETADATA:
                final String idpMetadataFormatter = getIdpMetadata(idpMetadata);
                partialDto.put("idpMetadata", idpMetadataFormatter);
                break;
            default:
                break;
        }
        return convertDtoFromApi(super.patch(partialDto));
    }

    private String getKeystoreBase64(final MultipartFile keystoreFile) {
        try (final InputStream isKeystore = keystoreFile.getInputStream()) {
            final byte[] keystore = IOUtils.toByteArray(isKeystore);
            return new String(Base64.getEncoder().encode(keystore), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new InvalidFormatException("Keystore is unreadable");
        }
    }

    private String getIdpMetadata(final MultipartFile idpMetadata) {
        try (final InputStream isIdpMeta = idpMetadata.getInputStream()) {
            return IOUtils.toString(isIdpMeta);
        } catch (final IOException e) {
            throw new InvalidFormatException("IdpMetadata is unreadable");
        }
    }

    protected IdentityProviderDto convertDtoFromApi(final IdentityProviderDto apiDto) {
        final IdentityProviderDto dto = new IdentityProviderDto();
        VitamUIUtils.copyProperties(apiDto, dto);
        convertPatterns(dto);
        return dto;
    }

    private void convertPatterns(final IdentityProviderDto dto) {
        if (CollectionUtils.isNotEmpty(dto.getPatterns())) {
            dto.setPatterns(dto.getPatterns().stream().map(s -> s.replace(".*@", "")).collect(Collectors.toList()));
        }
    }

    public Resource getMetadataProviderByProviderId(final String id, final ProviderEmbeddedOptions option,
        final Optional<String> embedded) {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        final IdentityProviderDto dto = getOne(id, embedded);
        return new ByteArrayResource(ProviderEmbeddedOptions.IDPMETADATA.equals(option) ?
            dto.getIdpMetadata().getBytes() :
            dto.getSpMetadata().getBytes());
    }

    @Override
    public IdentityProviderDto getOne(final String id, final Optional<String> embedded) {
        return super.getOne(id, embedded);
    }

    @Override
    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    public List<IdentityProviderDto> getAll(final Optional<String> criteria, final Optional<String> embedded) {
        return super.getAll(criteria, embedded);
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("id", "name", "internal", "enabled", "patterns", CUSTOMER_ID_KEY);
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        return Collections.emptyList();
    }

    @Override
    protected IdentityProviderInternalRestClient getClient() {
        return identityProviderInternalRestClient;
    }

    @Override
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V2;
    }

}
