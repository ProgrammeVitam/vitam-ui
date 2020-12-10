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
package fr.gouv.vitamui.identity.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.IamUtils;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderBuilder;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
import fr.gouv.vitamui.identity.domain.dto.ProviderPatchType;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;

/**
 *
 *
 */
@Service
public class ProviderService extends AbstractCrudService<IdentityProviderDto> {

    private final IamExternalRestClientFactory factory;

    @Autowired
    public ProviderService(final IamExternalRestClientFactory factory) {
        this.factory = factory;
    }

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OwnerService.class);

    @Override
    public Collection<IdentityProviderDto> getAll(final ExternalHttpContext context, final Optional<String> criteria, final Optional<String> embedded) {
        return super.getAll(context, criteria, embedded);
    }

    @Override
    public IdentityProviderDto create(final ExternalHttpContext c, final IdentityProviderDto dto) {
        return convertDtoFromApi(super.create(c, dto));
    }

    @Override
    public void beforeCreate(final IdentityProviderDto dto) {
        Assert.isTrue(StringUtils.isBlank(dto.getId()), "The DTO identifier must be null for create");
    }

    @Override
    public IdentityProviderDto getOne(final ExternalHttpContext c, final String id, final Optional<String> embedded) {
        return convertDtoFromApi(super.getOne(c, id, embedded));
    }

    @Override
    public IdentityProviderDto update(final ExternalHttpContext c, final IdentityProviderDto dto) {
        return convertDtoFromApi(super.update(c, dto));
    }

    /**
     * Method to apply for patch idp
     * @param c
     * @param partialDto
     * @param keystore
     * @param idpMetadata
     * @param id
     * @param patchType
     * @return
     */
    public IdentityProviderDto patch(final ExternalHttpContext c, final Map<String, Object> partialDto, final MultipartFile keystore,
            final MultipartFile idpMetadata, final String id, final ProviderPatchType patchType) {
        beforePatch(partialDto, keystore, idpMetadata, id, patchType);
        Optional<String> embedded = Optional.empty();
        switch (patchType) {
            case JSON :
                break;
            case KEYSTORE :
                final String keystoreBase64 = getKeystoreBase64(keystore);
                partialDto.put("keystoreBase64", keystoreBase64);
                embedded = IamUtils.buildOptionalEmbedded(ProviderEmbeddedOptions.KEYSTORE);
                break;
            case IDPMETADATA :
                final String idpMetadataFormatter = getIdpMetadata(idpMetadata);
                partialDto.put("idpMetadata", idpMetadataFormatter);
                embedded = IamUtils.buildOptionalEmbedded(ProviderEmbeddedOptions.IDPMETADATA);
                break;
            default :
                break;
        }
        return convertDtoFromApi(super.patch(c, partialDto, id));
    }

    private String getIdpMetadata(final MultipartFile idpMetadata) {
        try (final InputStream isIdpMeta = idpMetadata.getInputStream()) {
            final String idpMeta = IOUtils.toString(isIdpMeta);
            return idpMeta;
        }
        catch (final IOException e) {
            throw new InvalidFormatException("IdpMetadata is unreadable");
        }
    }

    private String getKeystoreBase64(final MultipartFile keystoreFile) {
        try (final InputStream isKeystore = keystoreFile.getInputStream()) {
            final byte[] keystore = IOUtils.toByteArray(isKeystore);
            final String keystoreBase64 = new String(Base64.getEncoder().encode(keystore), "UTF-8");
            return keystoreBase64;
        }
        catch (final IOException e) {
            throw new InvalidFormatException("Keystore is unreadable");
        }
    }

    protected void beforePatch(final Map<String, Object> partialDto, final MultipartFile keystore, final MultipartFile idpMetadata, final String id,
            final ProviderPatchType patchType) {
        super.beforePatch(partialDto, id);
        switch (patchType) {
            case JSON :
                break;
            case KEYSTORE :
                Assert.isTrue(StringUtils.isNotEmpty((String) partialDto.get("keystorePassword")) && !keystore.isEmpty(), "The keystorePassword is missing");
                break;
            case IDPMETADATA :
                Assert.isTrue(!idpMetadata.isEmpty(), "idpMetadata is missing");
                break;
            default :
                break;
        }
    }

    @Override
    public IdentityProviderExternalRestClient getClient() {
        return factory.getIdentityProviderExternalRestClient();
    }

    private void convertPatterns(final IdentityProviderDto dto) {
        final Function<String, String> mapper = s -> {
            return s.replace(".*@", "");
        };
        if (CollectionUtils.isNotEmpty(dto.getPatterns())) {
            dto.setPatterns(dto.getPatterns().stream().map(mapper).collect(Collectors.toList()));
        }
    }

    protected IdentityProviderDto convertDtoFromApi(final IdentityProviderDto apiDto) {
        final IdentityProviderDto dto = new IdentityProviderDto();
        VitamUIUtils.copyProperties(apiDto, dto);
        convertPatterns(dto);
        return dto;
    }

    protected List<IdentityProviderDto> convertDtoFromApi(final List<IdentityProviderDto> apiDto) {
        return apiDto.stream().map(dto -> convertDtoFromApi(dto)).collect(Collectors.toList());
    }

    public IdentityProviderDto create(final ExternalHttpContext c, final MultipartFile keystore, final MultipartFile idpMetadata, final String provider)
            throws Exception {
        IdentityProviderDto dto = new ObjectMapper().readValue(provider, IdentityProviderDto.class);
        final IdentityProviderBuilder builder = new IdentityProviderBuilder(dto.getName(), dto.getTechnicalName(), dto.getEnabled(), dto.getInternal(),
                dto.getPatterns(), new ByteArrayResource(keystore.getBytes()), dto.getKeystorePassword(), dto.getPrivateKeyPassword(),
                new ByteArrayResource(idpMetadata.getBytes()), dto.getCustomerId(), dto.isReadonly(), dto.getMailAttribute());
        dto = builder.build();
        return getClient().create(c, dto);
    }
}
