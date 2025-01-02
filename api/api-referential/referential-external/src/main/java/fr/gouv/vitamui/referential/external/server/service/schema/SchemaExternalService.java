/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.referential.external.server.service.schema;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.CombinedSchemaModel;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitamui.commons.api.dtos.ErrorImportFile;
import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.ImportSchemaDto;
import fr.gouv.vitamui.referential.common.dto.SchemaDto;
import fr.gouv.vitamui.referential.common.model.Collection;
import fr.gouv.vitamui.referential.common.service.ImportSchemaService;
import fr.gouv.vitamui.referential.external.server.service.AbstractService;
import fr.gouv.vitamui.referential.external.server.service.utils.ImportCSVUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SchemaExternalService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaExternalService.class);

    private static final String IMPORT_UNIT_SCHEMA = "IMPORT_UNIT_SCHEMA";
    private final ImportSchemaService importSchemaService;
    private final ImportSchemaConverter converter;
    private final ApplicationInternalRestClient applicationInternalRestClient;
    private final AdminExternalClient adminExternalClient;
    private final ExternalSecurityService externalSecurityService;

    @Autowired
    public SchemaExternalService(
        final AdminExternalClient adminExternalClient,
        final ExternalSecurityService externalSecurityService,
        ImportSchemaService importSchemaService,
        ImportSchemaConverter converter,
        ApplicationInternalRestClient applicationInternalRestClient
    ) {
        super(externalSecurityService);
        this.externalSecurityService = externalSecurityService;
        this.adminExternalClient = adminExternalClient;
        this.importSchemaService = importSchemaService;
        this.converter = converter;
        this.applicationInternalRestClient = applicationInternalRestClient;
    }

    public Optional<SchemaDto> getSchema(final Collection collection) {
        try {
            final RequestResponse<SchemaResponse> payload = getSchemaModels(collection).orElseThrow();
            final List<SchemaResponse> schemaModels = ((RequestResponseOK<SchemaResponse>) payload).getResults();
            final SchemaModelToSchemaElementDtoConverter converter = new SchemaModelToSchemaElementDtoConverter();
            final SchemaDto schemaDto = new SchemaDto();
            schemaDto.addAll(schemaModels.stream().map(converter::convert).toList());
            return Optional.of(schemaDto);
        } catch (VitamClientException e) {
            throw new SchemaLoadingException(e);
        }
    }

    public List<SchemaDto> getSchemas(final Set<Collection> collections) {
        return collections
            .stream()
            .map(this::getSchema)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public SchemaDto getArchiveUnitProfileSchema(final String archiveUnitProfileId) throws VitamClientException {
        final VitamContext vitamContext = buildVitamContext();
        final RequestResponse<CombinedSchemaModel> payload = adminExternalClient.getArchiveUnitProfileSchema(
            vitamContext,
            archiveUnitProfileId
        );
        final List<CombinedSchemaModel> schemaModels = ((RequestResponseOK<CombinedSchemaModel>) payload).getResults();
        final CombinedSchemaModelToSchemaElementDtoConverter converter =
            new CombinedSchemaModelToSchemaElementDtoConverter();
        final SchemaDto schemaDto = new SchemaDto();
        schemaDto.addAll(schemaModels.stream().map(converter::convert).toList());
        return schemaDto;
    }

    private Optional<RequestResponse<SchemaResponse>> getSchemaModels(Collection collection)
        throws VitamClientException {
        final VitamContext vitamContext = buildVitamContext();
        if (Objects.equals(collection, Collection.ARCHIVE_UNIT)) {
            return Optional.of(adminExternalClient.getUnitSchema(vitamContext));
        }
        if (Objects.equals(collection, Collection.OBJECT_GROUP)) {
            return Optional.of(adminExternalClient.getObjectGroupSchema(vitamContext));
        }
        return Optional.empty();
    }

    public ResponseEntity<Void> importUnitSchema(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        if (file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        Boolean isIdentifierMandatory = applicationInternalRestClient
            .isApplicationExternalIdentifierEnabled(
                InternalHttpContext.buildFromExternalHttpContext(externalSecurityService.getHttpContext()),
                IMPORT_UNIT_SCHEMA
            )
            .getBody();

        if (isIdentifierMandatory == null) {
            throw new InternalServerException("The result of the API call should not be null");
        }
        ImportSchemaCSVUtils.checkImportFile(file);
        LOGGER.debug("Schema file {} has been validated before parsing it", file.getOriginalFilename());

        List<ImportSchemaDto> importSchemaDtos = convertCsvFileToImportDto(file);
        LOGGER.debug("Schema file {} has been parsed in schema List", file.getOriginalFilename());

        RequestResponse<?> result;
        final VitamContext vitamContext = buildVitamContext();
        try {
            result = importSchemaService.importUnitSchema(
                vitamContext,
                converter.convertDtosToVitams(importSchemaDtos)
            );
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Can't create schema", e);
        }

        if (HttpStatus.OK.value() == result.getHttpCode()) {
            LOGGER.debug("Schemas file {} has been successfully import to VITAM", file.getOriginalFilename());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        throw new BadRequestException(
            "The CSV file has been rejected by vitam",
            null,
            List.of(
                ImportCSVUtils.errorToJson(
                    ErrorImportFile.builder()
                        .error(ErrorImportFileMessage.REJECT_BY_VITAM_CHECK_LOGBOOK_OPERATION_APP)
                        .build()
                )
            )
        );
    }

    private List<ImportSchemaDto> convertCsvFileToImportDto(MultipartFile file) {
        try (Reader reader = new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8)) {
            CsvToBean<ImportSchemaDto> csvToBean = new CsvToBeanBuilder<ImportSchemaDto>(reader)
                .withType(ImportSchemaDto.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                .withSeparator(';')
                .build();

            return csvToBean.parse();
        } catch (RuntimeException | IOException e) {
            throw new BadRequestException("Unable to read schema CSV file ", e);
        }
    }
}
