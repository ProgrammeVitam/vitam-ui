/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.archives.search.external.server.service;


import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.archive.internal.client.ArchiveInternalRestClient;
import fr.gouv.archive.internal.client.ArchiveSearchInternalWebClient;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


/**
 * The service to create vitam Archive-Search.
 */
@Getter
@Setter
@Service
public class ArchivesSearchExternalService extends AbstractResourceClientService<ArchiveUnitsDto, ArchiveUnitsDto> {

    @Autowired
    private final ArchiveInternalRestClient archiveInternalRestClient;


    @Autowired
    private final ArchiveSearchInternalWebClient archiveSearchInternalWebClient;

    public ArchivesSearchExternalService(@Autowired ArchiveInternalRestClient archiveInternalRestClient,
        ArchiveSearchInternalWebClient archiveSearchInternalWebClient,
        final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.archiveInternalRestClient = archiveInternalRestClient;
        this.archiveSearchInternalWebClient = archiveSearchInternalWebClient;
    }

    @Override
    protected ArchiveInternalRestClient getClient() {
        return archiveInternalRestClient;
    }


    public ArchiveUnitsDto searchArchiveUnitsByCriteria(final SearchCriteriaDto query) {
        return getClient().searchArchiveUnitsByCriteria(getInternalHttpContext(), query);
    }

    public ResponseEntity<ResultsDto> findUnitById(String id) {
        return archiveInternalRestClient.findUnitById(id, getInternalHttpContext());
    }

    public ResponseEntity<ResultsDto> findObjectById(String id) {
        return archiveInternalRestClient.findObjectById(id, getInternalHttpContext());
    }

    public VitamUISearchResponseDto getFilingHoldingScheme() {
        return archiveInternalRestClient.getFilingHoldingScheme(getInternalHttpContext());
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String id, String usage, Integer version) {
        return archiveSearchInternalWebClient
            .downloadObjectFromUnit(id, usage, version, getInternalHttpContext());
    }

    public Resource exportCsvArchiveUnitsByCriteria(final SearchCriteriaDto query) {
        return archiveInternalRestClient.exportCsvArchiveUnitsByCriteria(query, getInternalHttpContext());
    }

    public String exportDIPByCriteria(final ExportDipCriteriaDto exportDipCriteriaDto) {
        return archiveInternalRestClient.exportDIPByCriteria(exportDipCriteriaDto, getInternalHttpContext());
    }

    public ResponseEntity<JsonNode> startEliminationAnalysis(final SearchCriteriaDto query) {
        return archiveInternalRestClient.startEliminationAnalysis(getInternalHttpContext(), query);
    }

    public ResponseEntity<JsonNode> startEliminationAction(final SearchCriteriaDto query) {
        return archiveInternalRestClient.startEliminationAction(getInternalHttpContext(), query);
    }

    public String updateArchiveUnitsRules(final RuleSearchCriteriaDto ruleSearchCriteriaDto) {
        return archiveInternalRestClient.updateArchiveUnitsRules(ruleSearchCriteriaDto, getInternalHttpContext());
    }
}
