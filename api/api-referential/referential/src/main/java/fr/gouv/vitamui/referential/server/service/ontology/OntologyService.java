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
package fr.gouv.vitamui.referential.server.service.ontology;

import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.OntologyModel;
import fr.gouv.vitam.common.model.administration.OntologyOrigin;
import fr.gouv.vitam.common.model.administration.OntologyType;
import fr.gouv.vitam.common.model.administration.StringSize;
import fr.gouv.vitam.common.model.administration.TypeDetail;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.utils.OntologyServiceReader;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.HistoryEventDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.OntologyDto;
import fr.gouv.vitamui.referential.common.dto.OntologyResponseDto;
import fr.gouv.vitamui.referential.common.service.OntologyCommonService;
import fr.gouv.vitamui.referential.server.service.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OntologyService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyService.class);

    private OntologyCommonService ontologyCommonService;

    private ObjectMapper objectMapper;

    private OntologyConverter converter;

    private LogbookService logbookService;

    @Value("${internal_ontology_file_path}")
    private String internalOntologieFilePath;

    public OntologyService(
        OntologyCommonService ontologyCommonService,
        ObjectMapper objectMapper,
        OntologyConverter converter,
        LogbookService logbookService,
        SecurityService securityService
    ) {
        super(securityService);
        this.ontologyCommonService = ontologyCommonService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public OntologyDto getOne(VitamContext vitamContext, String identifier) {
        try {
            RequestResponse<OntologyModel> requestResponse = ontologyCommonService.findOntologyById(
                vitamContext,
                identifier
            );
            final OntologyResponseDto accessContractResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                OntologyResponseDto.class
            );
            if (accessContractResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(accessContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JacksonException e) {
            throw new InternalServerException("Unable to get Ontology", e);
        }
    }

    public List<OntologyDto> getAll(VitamContext vitamContext) {
        final RequestResponse<OntologyModel> requestResponse;
        try {
            requestResponse = ontologyCommonService.findOntologies(vitamContext, new Select().getFinalSelect());
            final OntologyResponseDto ontologyResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                OntologyResponseDto.class
            );

            return converter.convertVitamsToDtos(ontologyResponseDto.getResults());
        } catch (VitamClientException | JacksonException e) {
            throw new InternalServerException("Unable to find ontologies", e);
        }
    }

    public PaginatedValuesDto<OntologyDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction,
        VitamContext vitamContext,
        Optional<String> criteria
    ) {
        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;

        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find ontologies with pagination", ioe);
        } catch (IOException e) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        OntologyResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<OntologyDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    private OntologyResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<OntologyModel> requestResponse;
        try {
            requestResponse = ontologyCommonService.findOntologies(vitamContext, query);

            final OntologyResponseDto ontologyResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                OntologyResponseDto.class
            );

            return ontologyResponseDto;
        } catch (VitamClientException | JacksonException e) {
            throw new InternalServerException("Unable to find ontologies", e);
        }
    }

    public Boolean check(VitamContext vitamContext, OntologyDto ontologyDto) {
        try {
            return !ontologyCommonService.checkAbilityToCreateOntologyInVitam(
                converter.convertDtosToVitams(Arrays.asList(ontologyDto)),
                vitamContext
            );
        } catch (ConflictException e) {
            return true;
        }
    }

    public OntologyDto create(VitamContext context, OntologyDto ontologyDto) {
        List<OntologyDto> ontologies = getAll(context);
        ontologyDto.setOrigin(OntologyOrigin.EXTERNAL);
        ontologies.add(ontologyDto);
        try {
            RequestResponse requestResponse = ontologyCommonService.importOntologies(
                context,
                converter.convertDtosToVitams(ontologies)
            );
            VitamRestUtils.checkResponse(requestResponse);
            OntologyResponseDto ontologyResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                OntologyResponseDto.class
            );
            List<OntologyDto> ontologyDtos = converter.convertVitamsToDtos(ontologyResponseDto.getResults());
            return (ontologyDtos == null || ontologyDtos.isEmpty()) ? ontologyDto : ontologyDtos.get(0);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Unable to create ontology", e);
        }
    }

    public void delete(VitamContext context, String identifier) {
        List<OntologyDto> ontologies = getAll(context);
        try {
            RequestResponse<?> requestResponse = ontologyCommonService.importOntologies(
                context,
                converter.convertDtosToVitams(
                    ontologies
                        .stream()
                        .filter(
                            ontologyDto ->
                                !(ontologyDto.getId().equals(identifier) &&
                                    OntologyOrigin.EXTERNAL.equals(ontologyDto.getOrigin()))
                        )
                        .collect(Collectors.toList())
                )
            );
            VitamRestUtils.checkResponse(requestResponse);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Unable to delete ontology", e);
        }
    }

    public OntologyDto patch(VitamContext vitamContext, final Map<String, Object> partialDto) {
        try {
            if (vitamContext != null) {
                LOGGER.info("Update Ontology EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            }
            final List<OntologyDto> ontologies = getAll(vitamContext);

            final OntologyDto ontologyDto = ontologies
                .stream()
                .filter(ontology -> partialDto.get("id").equals(ontology.getId()))
                .findFirst()
                .orElseThrow(() -> new InternalServerException("No ontology matched for update"));

            this.patchFields(ontologyDto, partialDto);

            ontologyCommonService.importOntologies(vitamContext, converter.convertDtosToVitams(ontologies));

            return ontologyDto;
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Unable to patch agency", e);
        }
    }

    private void patchFields(OntologyDto ontologyToPatch, Map<String, Object> fieldsToApply) {
        if (fieldsToApply.containsKey("shortName")) {
            ontologyToPatch.setShortName((String) fieldsToApply.get("shortName"));
        }
        if (fieldsToApply.containsKey("description")) {
            ontologyToPatch.setDescription((String) fieldsToApply.get("description"));
        }
        if (fieldsToApply.containsKey("type")) {
            ontologyToPatch.setType(OntologyType.valueOf((String) fieldsToApply.get("type")));
        }
        if (fieldsToApply.containsKey("typeDetail")) {
            ontologyToPatch.setTypeDetail(TypeDetail.valueOf((String) fieldsToApply.get("typeDetail")));
        }
        if (fieldsToApply.containsKey("stringSize")) {
            ontologyToPatch.setStringSize(
                Optional.ofNullable(fieldsToApply.get("stringSize"))
                    .map(v -> StringSize.valueOf((String) v))
                    .orElse(null)
            );
        }
        if (fieldsToApply.containsKey("collections")) {
            ontologyToPatch.setCollections((List<String>) fieldsToApply.get("collections"));
        }
    }

    public List<HistoryEventDto> findHistoryByIdentifier(VitamContext vitamContext, final String id)
        throws VitamClientException {
        try {
            return logbookService.toHistoryEvents(
                logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id), vitamContext),
                List.of("STP_UPDATE_ONTOLOGY", "STP_IMPORT_ONTOLOGY")
            );
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Unable to fetch history", e);
        }
    }

    public JsonNode importOntologies(VitamContext context, String fileName, MultipartFile file) {
        try {
            final RequestResponse<?> requestResponse = ontologyCommonService.importOntologies(context, fileName, file);
            VitamRestUtils.checkResponse(requestResponse, 200, 204);
            return requestResponse.toJsonNode();
        } catch (
            InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException e
        ) {
            throw new InternalServerException("Unable to import ontology file " + fileName + " : ", e);
        }
    }

    /**
     * Read internal ontology fields list from a file
     *
     * @throws IOException : throw an exception while parsing ontology fields file
     */
    public List<VitamUiOntologyDto> readInternalOntologyFromFile() throws IOException {
        LOGGER.debug("get default internal ontologie file from path : {} ", internalOntologieFilePath);
        return OntologyServiceReader.readInternalOntologyFromFile(internalOntologieFilePath);
    }

    public OntologyDto getOne(String identifier) {
        VitamContext vitamContext = buildVitamContext();
        return this.getOne(vitamContext, identifier);
    }

    public List<OntologyDto> getAll() {
        VitamContext vitamContext = buildVitamContext();
        return this.getAll(vitamContext);
    }

    public PaginatedValuesDto<OntologyDto> getAllPaginated(
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) {
        VitamContext vitamContext = buildVitamContext();
        return this.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    public Boolean check(OntologyDto ontologyDto) {
        VitamContext vitamContext = buildVitamContext();
        return this.check(vitamContext, ontologyDto);
    }

    public OntologyDto create(OntologyDto ontologyDto) {
        VitamContext vitamContext = buildVitamContext();
        return this.create(vitamContext, ontologyDto);
    }

    public void delete(String identifier) {
        VitamContext vitamContext = buildVitamContext();
        this.delete(vitamContext, identifier);
    }

    public OntologyDto patch(final Map<String, Object> partialDto) {
        VitamContext vitamContext = buildVitamContext();
        return this.patch(vitamContext, partialDto);
    }

    public JsonNode importOntologies(String fileName, MultipartFile file) {
        VitamContext vitamContext = buildVitamContext();
        return this.importOntologies(vitamContext, fileName, file);
    }

    public List<HistoryEventDto> findHistoryById(String id) throws VitamClientException {
        VitamContext vitamContext = buildVitamContext();

        return this.findHistoryByIdentifier(vitamContext, id);
    }
}
