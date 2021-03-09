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
package fr.gouv.vitamui.ingest.service;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.ingest.external.client.IngestExternalRestClient;
import fr.gouv.vitamui.ingest.external.client.IngestExternalWebClient;
import fr.gouv.vitamui.ingest.thread.IngestThread;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

/**
 * Ingest Service
 */
@Service
public class IngestService extends AbstractPaginateService<LogbookOperationDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestService.class);

    private final IngestExternalWebClient ingestExternalWebClient;
    private final IngestExternalRestClient ingestExternalRestClient;
    private CommonService commonService;

    @Autowired
    public IngestService(final CommonService commonService, final IngestExternalRestClient ingestExternalRestClient,
        final IngestExternalWebClient ingestExternalWebClient) {
        this.commonService = commonService;
        this.ingestExternalRestClient = ingestExternalRestClient;
        this.ingestExternalWebClient = ingestExternalWebClient;
    }

    @Override
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(final Integer page, final Integer size, final Optional<String> criteria,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, final ExternalHttpContext context) {
        return super.getAllPaginated(page, size, criteria, orderBy, direction, context);
    }

    public LogbookOperationDto getOne(final ExternalHttpContext context, final String id) {
        return super.getOne(context,id);
    }

    @Override
    protected Integer beforePaginate(final Integer page, final Integer size) {
        return commonService.checkPagination(page, size);
    }

    public void upload(final ExternalHttpContext context, InputStream in, final String contextId, final String action,
        final String originalFilename) {

        final IngestThread
            ingestThread =
            new IngestThread(ingestExternalWebClient, context, in, contextId, action, originalFilename);

        ingestThread.start();
    }

     public ResponseEntity<byte[]> generateODTReport(ExternalHttpContext context, String id) {
        return ingestExternalRestClient.generateODTReport(context, id);
    }

    public IngestExternalRestClient getClient() {
        return ingestExternalRestClient;
    }

}
