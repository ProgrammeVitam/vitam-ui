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

package fr.gouv.vitamui.referential.external.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.SchemaDto;
import fr.gouv.vitamui.referential.common.model.Collection;
import fr.gouv.vitamui.referential.internal.client.SchemaInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.SchemaInternalWebClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@Service
public class SchemaExternalService {

    private final SchemaInternalRestClient schemaInternalRestClient;
    private final SchemaInternalWebClient schemaInternalWebClient;

    public SchemaExternalService(
        SchemaInternalRestClient schemaInternalRestClient,
        SchemaInternalWebClient schemaInternalWebClient
    ) {
        this.schemaInternalRestClient = schemaInternalRestClient;
        this.schemaInternalWebClient = schemaInternalWebClient;
    }

    public List<SchemaDto> getSchemas(final InternalHttpContext internalHttpContext, final Set<Collection> collections)
        throws URISyntaxException {
        return schemaInternalRestClient.getSchemas(internalHttpContext, collections);
    }

    public SchemaDto getArchiveUnitProfileSchema(
        final InternalHttpContext internalHttpContext,
        final String archiveUnitProfileId
    ) throws URISyntaxException {
        return schemaInternalRestClient.getArchiveUnitProfileSchema(internalHttpContext, archiveUnitProfileId);
    }

    public JsonNode importUnitSchemas(
        final InternalHttpContext internalHttpContext,
        String originalFilename,
        MultipartFile file
    ) {
        if (originalFilename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        return schemaInternalWebClient.importUnitSchema(internalHttpContext, originalFilename, file);
    }
}
