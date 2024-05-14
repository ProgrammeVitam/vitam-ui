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

package fr.gouv.vitamui.collect.internal.client;

import fr.gouv.vitamui.collect.common.dto.OperationIdDto;
import fr.gouv.vitamui.collect.common.dto.UpdateArchiveUnitDto;
import fr.gouv.vitamui.collect.common.rest.ArchiveUnitClient;
import fr.gouv.vitamui.commons.api.dtos.JsonPatch;
import fr.gouv.vitamui.commons.api.dtos.JsonPatchDto;
import fr.gouv.vitamui.commons.api.dtos.MultiJsonPatchDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.AbstractHttpContext;
import fr.gouv.vitamui.commons.rest.client.BaseRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpMethod.PATCH;

public class ArchiveUnitClientImpl extends BaseRestClient<InternalHttpContext> implements ArchiveUnitClient {

    private static final String ARCHIVE_UNITS = "archive-units";
    private static final VitamUILogger log = VitamUILoggerFactory.getInstance(ArchiveUnitClientImpl.class);

    public ArchiveUnitClientImpl(RestTemplate restTemplate, String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    public String getPathUrl() {
        return String.format("/%s", ARCHIVE_UNITS);
    }

    public OperationIdDto update(
        final AbstractHttpContext abstractHttpContext,
        final String transactionId,
        final Set<UpdateArchiveUnitDto> updateOperationDtoSet
    ) {
        final URI uri;
        try {
            uri = new URIBuilder(getBaseUrl()).setPathSegments(ARCHIVE_UNITS, transactionId).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final HttpEntity<?> httpEntity = new HttpEntity<>(updateOperationDtoSet, buildHeaders(abstractHttpContext));
        final ResponseEntity<OperationIdDto> responseEntity = restTemplate.exchange(
            uri,
            PATCH,
            httpEntity,
            OperationIdDto.class
        );
        return Objects.requireNonNull(responseEntity.getBody());
    }

    public OperationIdDto update(
        final AbstractHttpContext abstractHttpContext,
        final String transactionId,
        final String id,
        final JsonPatch jsonPatch
    ) {
        final URI uri;
        try {
            uri = new URIBuilder(getBaseUrl()).setPathSegments(ARCHIVE_UNITS, transactionId, id).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final HttpEntity<?> httpEntity = new HttpEntity<>(jsonPatch, buildHeaders(abstractHttpContext));
        final ResponseEntity<OperationIdDto> responseEntity = restTemplate.exchange(
            uri,
            PATCH,
            httpEntity,
            OperationIdDto.class
        );
        return Objects.requireNonNull(responseEntity.getBody());
    }

    public OperationIdDto update(
        final AbstractHttpContext abstractHttpContext,
        final String transactionId,
        final JsonPatchDto jsonPatchDto
    ) {
        final URI uri;
        try {
            uri = new URIBuilder(getBaseUrl())
                .setPathSegments(ARCHIVE_UNITS, transactionId, "update", "single")
                .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final HttpEntity<?> httpEntity = new HttpEntity<>(jsonPatchDto, buildHeaders(abstractHttpContext));
        final ResponseEntity<OperationIdDto> responseEntity = restTemplate.exchange(
            uri,
            PATCH,
            httpEntity,
            OperationIdDto.class
        );
        return Objects.requireNonNull(responseEntity.getBody());
    }

    public OperationIdDto update(
        final AbstractHttpContext abstractHttpContext,
        final String transactionId,
        final MultiJsonPatchDto multiJsonPatchDto
    ) {
        final URI uri;
        try {
            uri = new URIBuilder(getBaseUrl())
                .setPathSegments(ARCHIVE_UNITS, transactionId, "update", "multiple")
                .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final HttpEntity<?> httpEntity = new HttpEntity<>(multiJsonPatchDto, buildHeaders(abstractHttpContext));
        final ResponseEntity<OperationIdDto> responseEntity = restTemplate.exchange(
            uri,
            PATCH,
            httpEntity,
            OperationIdDto.class
        );
        return Objects.requireNonNull(responseEntity.getBody());
    }
}
