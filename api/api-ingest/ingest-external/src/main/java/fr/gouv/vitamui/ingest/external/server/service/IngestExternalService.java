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
package fr.gouv.vitamui.ingest.external.server.service;

import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.ingest.internal.client.IngestInternalRestClient;
import fr.gouv.vitamui.ingest.internal.client.IngestInternalWebClient;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The service to create vitam ingest.
 *
 */
@Getter
@Setter
@Service
public class IngestExternalService extends AbstractResourceClientService<LogbookOperationDto, LogbookOperationDto> {

    @Autowired
    private final IngestInternalRestClient ingestInternalRestClient;

    @Autowired
    private final IngestInternalWebClient ingestInternalWebClient;

    public IngestExternalService(@Autowired IngestInternalRestClient ingestInternalRestClient,
        IngestInternalWebClient ingestInternalWebClient,
            final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.ingestInternalRestClient = ingestInternalRestClient;
        this.ingestInternalWebClient = ingestInternalWebClient;
    }

    public Mono<RequestResponseOK> upload(InputStream in, String action, String contextId) {
        return ingestInternalWebClient.upload(getInternalHttpContext(), in, action, contextId);
    }

    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(final Integer page, final Integer size, final Optional<String> criteria,
            final Optional<String> orderBy, final Optional<DirectionDto> direction) {

        ParameterChecker.checkPagination(size, page);
        final PaginatedValuesDto<LogbookOperationDto> result = getClient().getAllPaginated(getInternalHttpContext(), page, size, criteria, orderBy, direction);
        return new PaginatedValuesDto<>(
                result.getValues().stream().map(element -> converterToExternalDto(element)).collect(Collectors.toList()),
                result.getPageNum(),
                result.getPageSize(),
                result.isHasMore());
    }

    public LogbookOperationDto getOne(final String id) {
        return ingestInternalRestClient.getOne(getInternalHttpContext(), id);

    }

     public ResponseEntity<byte[]> generateODTReport(String id) {
        return ingestInternalRestClient.generateODTReport(getInternalHttpContext(), id);
    }

    @Override
    protected IngestInternalRestClient getClient() {
        return ingestInternalRestClient;
    }
}
