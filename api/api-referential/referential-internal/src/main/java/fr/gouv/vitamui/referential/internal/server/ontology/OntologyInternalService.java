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
package fr.gouv.vitamui.referential.internal.server.ontology;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.OntologyDto;
import fr.gouv.vitamui.referential.common.dto.OntologyResponseDto;
import fr.gouv.vitamui.referential.common.service.OntologyService;
import fr.gouv.vitamui.referential.internal.server.fileformat.FileFormatInternalService;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OntologyInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OntologyInternalService.class);

    private OntologyService ontologyService;

    private ObjectMapper objectMapper;

    private OntologyConverter converter;

    private LogbookService logbookService;


    @Autowired
    public OntologyInternalService(OntologyService ontologyService, ObjectMapper objectMapper, OntologyConverter converter,
                                 LogbookService logbookService) {
        this.ontologyService = ontologyService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
    }

    public OntologyDto getOne(VitamContext vitamContext, String identifier) {
        try {
            RequestResponse<OntologyModel> requestResponse = ontologyService.findOntologyById(vitamContext, identifier);
            final OntologyResponseDto accessContractResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), OntologyResponseDto.class);
            if (accessContractResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(accessContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Ontology", e);
        }
    }

    public List<OntologyDto> getAll(VitamContext vitamContext) {
        final RequestResponse<OntologyModel> requestResponse;
        try {
            requestResponse = ontologyService.findOntologies(vitamContext, new Select().getFinalSelect());
            final OntologyResponseDto ontologyResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), OntologyResponseDto.class);

            return converter.convertVitamsToDtos(ontologyResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find ontologies", e);
        }
    }

    public PaginatedValuesDto<OntologyDto> getAllPaginated(final Integer pageNumber, final Integer size,
                                                         final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
                                                         Optional<String> criteria) {

        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;

        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find ontologies with pagination", ioe);
        } catch ( IOException e ) {
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
            requestResponse = ontologyService.findOntologies(vitamContext, query);

            final OntologyResponseDto ontologyResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), OntologyResponseDto.class);

            return ontologyResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find ontologies", e);
        }
    }

    public Boolean check(VitamContext vitamContext, OntologyDto ontologyDto) {
        try {
            return !ontologyService.checkAbilityToCreateOntologyInVitam(converter.convertDtosToVitams(Arrays.asList(ontologyDto)), vitamContext);
        } catch (ConflictException e) {
            return true;
        }
    }

    public OntologyDto create(VitamContext context, OntologyDto ontologyDto) {
        List<OntologyDto> ontologies = getAll(context);
        ontologyDto.setOrigin(OntologyOrigin.EXTERNAL);
        ontologies.add(ontologyDto);
        try {
            RequestResponse requestResponse = ontologyService.importOntologies(context, converter.convertDtosToVitams(ontologies));
            OntologyResponseDto ontologyResponseDto = objectMapper.treeToValue(requestResponse.toJsonNode(), OntologyResponseDto.class);
            List<OntologyDto> ontologyDtos = converter.convertVitamsToDtos(ontologyResponseDto.getResults());
            return (ontologyDtos == null || ontologyDtos.isEmpty())? ontologyDto : ontologyDtos.get(0);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException  e) {
            throw new InternalServerException("Unable to create ontology", e);
        }
    }

    public void delete(VitamContext context, String identifier) {
        List<OntologyDto> ontologies = getAll(context);
        try {
            ontologyService.importOntologies(context, converter.convertDtosToVitams(ontologies
                .stream()
                .filter(ontologyDto ->
                    !(ontologyDto.getId().equals(identifier) && OntologyOrigin.EXTERNAL.equals(ontologyDto.getOrigin()))
                    ).collect(Collectors.toList()))
            );
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException  e) {
            throw new InternalServerException("Unable to delete ontology", e);
        }
    }

    public OntologyDto patch(VitamContext vitamContext,final Map<String, Object> partialDto) {
        final OntologyDto ontologyDto = this.getOne(vitamContext, (String) partialDto.get("identifier"));
        partialDto.forEach((key,value) ->
        {
            if ("type".equals(key)) {
                ontologyDto.setType(OntologyType.valueOf((String) value));
            } else if (!"id".equals(key)) {
                try {
                    BeanUtilsBean.getInstance().copyProperty(ontologyDto, key, value);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new InternalServerException("Unable to copy properties to DTO", e);
                }
            }

        });

        try {
            updateOntology(vitamContext, (String) partialDto.get("id"), ontologyDto);
            return ontologyDto;
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException e) {
            throw new InternalServerException("Unable to patch agency", e);
        }
    }

    private RequestResponse<?> updateOntology(final VitamContext vitamContext, final String id, OntologyDto patchOntology)
            throws InvalidParseOperationException, AccessExternalClientException, IOException {
        if(vitamContext != null) {
            LOGGER.info("Update Ontology EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
        }
        final List<OntologyDto> ontologies = getAll(vitamContext);

        ontologies.stream()
                .filter( ontology -> id.equals(ontology.getId()) )
                .forEach( ontology -> this.patchFields(ontology, patchOntology) );

        return ontologyService.importOntologies(vitamContext, converter.convertDtosToVitams(ontologies));
    }

    private void patchFields(OntologyDto ontologyToPatch, OntologyDto fieldsToApply) {
        if (fieldsToApply.getShortName() != null) {
            ontologyToPatch.setShortName(fieldsToApply.getShortName());
        }

        if (fieldsToApply.getDescription() != null) {
            ontologyToPatch.setDescription(fieldsToApply.getDescription());
        }

        if (fieldsToApply.getType() != null) {
            ontologyToPatch.setType(fieldsToApply.getType());
        }

        if (fieldsToApply.getCollections() != null) {
            ontologyToPatch.setCollections(fieldsToApply.getCollections());
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String id) throws VitamClientException {
        try {
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(id),vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
        	throw new InternalServerException("Unable to fetch history", e);
        }
    }

    public JsonNode importOntologies(VitamContext context, String fileName, MultipartFile file) {
        try {
            return ontologyService.importOntologies(context, fileName, file).toJsonNode();
        } catch (InvalidParseOperationException |AccessExternalClientException |VitamClientException | IOException e) {
            LOGGER.error("Unable to ontology file {}: {}", fileName, e.getMessage());
            throw new InternalServerException("Unable to import ontology file " + fileName + " : ", e);
        }
    }
}
