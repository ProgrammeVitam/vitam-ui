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
package fr.gouv.vitamui.referential.external.server.service.fileformat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.api.AdminCollections;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.FileFormatDto;
import fr.gouv.vitamui.referential.common.dto.FileFormatResponseDto;
import fr.gouv.vitamui.referential.common.service.VitamFileFormatService;
import fr.gouv.vitamui.referential.external.server.service.AbstractService;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FileFormatExternalService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFormatExternalService.class);

    private ObjectMapper objectMapper;

    private FileFormatConverter converter;

    private LogbookService logbookService;

    private VitamFileFormatService vitamFileFormatService;

    @Autowired
    public FileFormatExternalService(
        ObjectMapper objectMapper,
        FileFormatConverter converter,
        LogbookService logbookService,
        VitamFileFormatService vitamFileFormatService,
        ExternalSecurityService externalSecurityService
    ) {
        super(externalSecurityService);
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
        this.vitamFileFormatService = vitamFileFormatService;
    }

    public FileFormatDto getOne(VitamContext vitamContext, String identifier) {
        try {
            LOGGER.info("File Format EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<FileFormatModel> requestResponse = vitamFileFormatService.findFileFormatById(
                vitamContext,
                identifier
            );
            final FileFormatResponseDto fileFormatResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                FileFormatResponseDto.class
            );
            if (fileFormatResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(fileFormatResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get FileFormat", e);
        }
    }

    public List<FileFormatDto> getAll(VitamContext vitamContext) {
        final RequestResponse<FileFormatModel> requestResponse;
        LOGGER.debug("Get ALL File Formats !");
        try {
            LOGGER.info("All File Formats EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = vitamFileFormatService.findFileFormats(vitamContext, new Select().getFinalSelect());
            LOGGER.debug("Response: {}", requestResponse);
            final FileFormatResponseDto fileFormatResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                FileFormatResponseDto.class
            );
            return converter.convertVitamsToDtos(fileFormatResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find fileFormats", e);
        }
    }

    public PaginatedValuesDto<FileFormatDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        VitamContext vitamContext,
        Optional<String> criteria
    ) {
        LOGGER.info("All File Formats EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find fileFormats with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        FileFormatResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<FileFormatDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("Formats in page: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    private FileFormatResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<FileFormatModel> requestResponse;
        try {
            LOGGER.info("All File Formats EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            requestResponse = vitamFileFormatService.findFileFormats(vitamContext, query);

            final FileFormatResponseDto fileFormatResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                FileFormatResponseDto.class
            );

            LOGGER.debug("Formats: {}", fileFormatResponseDto);

            return fileFormatResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find fileFormats", e);
        }
    }

    public Boolean check(VitamContext vitamContext, FileFormatDto accessContractDto) {
        List<FileFormatDto> fileFormatDtoList = new ArrayList<>();
        fileFormatDtoList.add(accessContractDto);
        LOGGER.info("File Format Check EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        try {
            return !vitamFileFormatService.checkAbilityToCreateFileFormatInVitam(
                converter.convertDtosToVitams(fileFormatDtoList),
                vitamContext
            );
        } catch (ConflictException e) {
            return true;
        } catch (VitamUIException e) {
            throw new InternalServerException("Unable to check fileFormat", e);
        }
    }

    public FileFormatDto create(VitamContext vitamContext, FileFormatDto fileformatDto) {
        LOGGER.debug("Try to create File Format {} {}", fileformatDto, vitamContext);
        try {
            LOGGER.info("Create File Format EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            // TODO: Check if this file format is an external PUID
            RequestResponse<?> requestResponse = vitamFileFormatService.create(
                vitamContext,
                converter.convertDtoToVitam(fileformatDto)
            );
            final FileFormatModel fileFormatVitamDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                FileFormatModel.class
            );
            return converter.convertVitamToDto(fileFormatVitamDto);
        } catch (
            InvalidParseOperationException
            | AccessExternalClientException
            | IOException
            | VitamClientException
            | JAXBException e
        ) {
            throw new InternalServerException("Unable to create fileFormat", e);
        }
    }

    public FileFormatDto patch(VitamContext vitamContext, final Map<String, Object> partialDto) {
        LOGGER.debug("Try to patch File Format {} {}", partialDto, vitamContext);
        LOGGER.info("Patch File Format EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        FileFormatDto fileFormat = this.getOne(vitamContext, (String) partialDto.get("puid"));
        partialDto.forEach((key, value) -> {
            if (!"id".equals(key)) {
                try {
                    BeanUtilsBean.getInstance().copyProperty(fileFormat, key, value);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        });
        FileFormatModel fileFormatVitam = converter.convertDtoToVitam(fileFormat);
        try {
            String puid = (String) partialDto.get("puid");
            if (!puid.startsWith("EXTERNAL_")) {
                throw new InternalServerException("Unable to patch fileFormat: Not an external format");
            }

            RequestResponse<?> requestResponse = vitamFileFormatService.patchFileFormat(
                vitamContext,
                puid,
                fileFormatVitam
            );
            final FileFormatModel fileFormatVitamDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                FileFormatModel.class
            );
            return converter.convertVitamToDto(fileFormatVitamDto);
        } catch (
            InvalidParseOperationException
            | AccessExternalClientException
            | VitamClientException
            | IOException
            | JAXBException e
        ) {
            throw new InternalServerException("Unable to patch fileFormat", e);
        }
    }

    public void delete(VitamContext context, String id) {
        LOGGER.debug("Try to delete File Format {} {}", id, context);
        LOGGER.debug("Delete File Format EvIdAppSession : {} ", context.getApplicationSessionId());

        if (!id.startsWith("EXTERNAL_")) {
            throw new InternalServerException("Unable to patch fileFormat: Not an external format");
        }

        try {
            vitamFileFormatService.deleteFileFormat(context, id);
        } catch (
            InvalidParseOperationException
            | AccessExternalClientException
            | VitamClientException
            | IOException
            | JAXBException e
        ) {
            throw new InternalServerException("Unable to delete fileFormat", e);
        }
    }

    //TOTO A suppriemr
    public ResponseEntity<Resource> export() {
        throw new NotImplementedException("Can not export file format");
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String identifier)
        throws VitamClientException {
        LOGGER.debug("findHistoryById for identifier" + identifier);
        LOGGER.debug("File Format History EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        return logbookService
            .findEventsByIdentifierAndCollectionNames(identifier, AdminCollections.AGENCIES.getName(), vitamContext)
            .toJsonNode();
    }

    public JsonNode importFileFormats(VitamContext context, String fileName, MultipartFile file) {
        try {
            return vitamFileFormatService.importFileFormats(context, fileName, file).toJsonNode();
        } catch (
            InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e
        ) {
            LOGGER.error("Unable to file format file {}: {}", fileName, e.getMessage());
            throw new InternalServerException("Unable to import file format file " + fileName + " : ", e);
        }
    }

    public FileFormatDto getOne(String identifier) {
        VitamContext vitamContext = buildVitamContext();
        return this.getOne(vitamContext, identifier);
    }

    public List<FileFormatDto> getAll() {
        VitamContext vitamContext = buildVitamContext();
        return this.getAll(vitamContext);
    }

    public FileFormatDto create(FileFormatDto fileFormatDto) {
        VitamContext vitamContext = buildVitamContext();
        return this.create(vitamContext, fileFormatDto);
    }

    public Boolean check(FileFormatDto fileFormatDto) {
        VitamContext vitamContext = buildVitamContext();
        return this.check(vitamContext, fileFormatDto);
    }

    public FileFormatDto patch(Map<String, Object> partialDto) {
        VitamContext vitamContext = buildVitamContext();
        return this.patch(vitamContext, partialDto);
    }

    public void delete(String id) {
        VitamContext vitamContext = buildVitamContext();
        this.delete(vitamContext, id);
    }

    public LogbookOperationsResponseDto findHistoryById(String identifier) throws VitamClientException {
        VitamContext vitamContext = buildVitamContext();

        JsonNode body = this.findHistoryByIdentifier(vitamContext, identifier);
        try {
            return JsonUtils.treeToValue(body, LogbookOperationsResponseDto.class, false);
        } catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
    }

    public PaginatedValuesDto<FileFormatDto> getAllPaginated(
        Integer pageNumber,
        Integer size,
        Optional<String> criteria,
        Optional<String> orderBy,
        Optional<DirectionDto> direction
    ) {
        VitamContext vitamContext = buildVitamContext();
        return this.getAllPaginated(pageNumber, size, orderBy, direction, vitamContext, criteria);
    }

    public JsonNode importFileFormats(String fileName, MultipartFile file) {
        VitamContext vitamContext = buildVitamContext();
        return this.importFileFormats(vitamContext, fileName, file);
    }
}
