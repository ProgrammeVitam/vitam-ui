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
package fr.gouv.vitamui.referential.external.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.internal.client.RuleInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.RuleInternalWebClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Getter
@Setter
@Service
public class RuleExternalService extends AbstractResourceClientService<RuleDto, RuleDto> {

    private RuleInternalRestClient ruleInternalRestClient;

    private RuleInternalWebClient ruleInternalWebClient;

    @Autowired
    public RuleExternalService(
        ExternalSecurityService externalSecurityService,
        RuleInternalRestClient ruleInternalRestClient,
        RuleInternalWebClient ruleInternalWebClient
    ) {
        super(externalSecurityService);
        this.ruleInternalRestClient = ruleInternalRestClient;
        this.ruleInternalWebClient = ruleInternalWebClient;
    }

    public List<RuleDto> getAll(final Optional<String> criteria) {
        return ruleInternalRestClient.getAll(getInternalHttpContext(), criteria);
    }

    @Override
    protected BasePaginatingAndSortingRestClient<RuleDto, InternalHttpContext> getClient() {
        return ruleInternalRestClient;
    }

    public PaginatedValuesDto<RuleDto> getAllPaginated(
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) {
        ParameterChecker.checkPagination(size, page);
        return getClient().getAllPaginated(getInternalHttpContext(), page, size, criteria, orderBy, direction);
    }

    public RuleDto getOne(String id) {
        return getClient().getOne(getInternalHttpContext(), id);
    }

    public boolean patchRule(final String id, final Map<String, Object> partialDto) {
        return ruleInternalRestClient.patchRule(getInternalHttpContext(), id, partialDto);
    }

    public boolean createRule(final RuleDto ruleDto) {
        return ruleInternalRestClient.createRule(getInternalHttpContext(), ruleDto);
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("name", "identifier");
    }

    @Override
    public LogbookOperationsResponseDto findHistoryById(final String id) {
        final JsonNode body = getClient().findHistoryById(getInternalHttpContext(), id);
        try {
            return JsonUtils.treeToValue(body, LogbookOperationsResponseDto.class, false);
        } catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
    }

    public boolean check(RuleDto accessContractDto) {
        return ruleInternalRestClient.check(getInternalHttpContext(), accessContractDto);
    }

    public boolean deleteRule(final String id) {
        return ruleInternalRestClient.deleteRule(getInternalHttpContext(), id);
    }

    public ResponseEntity<Resource> export() {
        return ruleInternalRestClient.export(getInternalHttpContext());
    }

    public JsonNode importRules(String fileName, MultipartFile file) {
        return ruleInternalWebClient.importRules(getInternalHttpContext(), fileName, file);
    }
}
