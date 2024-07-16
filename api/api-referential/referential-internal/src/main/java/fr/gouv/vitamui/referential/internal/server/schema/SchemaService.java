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

package fr.gouv.vitamui.referential.internal.server.schema;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.SchemaDto;
import fr.gouv.vitamui.referential.common.model.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    private static final Logger log = LoggerFactory.getLogger(SchemaService.class);
    private final InternalSecurityService internalSecurityService;
    private final AdminExternalClient adminExternalClient;

    @Autowired
    public SchemaService(
        final InternalSecurityService internalSecurityService,
        final AdminExternalClient adminExternalClient
    ) {
        this.internalSecurityService = internalSecurityService;
        this.adminExternalClient = adminExternalClient;
    }

    public Optional<SchemaDto> getSchema(final Collection collection) {
        try {
            final RequestResponse<SchemaResponse> payload = getSchemaModels(collection).orElseThrow();
            final List<SchemaResponse> schemaModels = ((RequestResponseOK<SchemaResponse>) payload).getResults();
            final SchemaModelToSchemaElementDtoConverter converter = new SchemaModelToSchemaElementDtoConverter();
            final SchemaDto schemaDto = new SchemaDto();
            schemaDto.addAll(schemaModels.stream().map(converter::convert).collect(Collectors.toList()));
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

    private Optional<RequestResponse<SchemaResponse>> getSchemaModels(Collection collection)
        throws VitamClientException {
        final VitamContext vitamContext = internalSecurityService.getVitamContext();
        if (Objects.equals(collection, Collection.ARCHIVE_UNIT)) {
            return Optional.of(adminExternalClient.getUnitSchema(vitamContext));
        }
        if (Objects.equals(collection, Collection.OBJECT_GROUP)) {
            return Optional.of(adminExternalClient.getObjectGroupSchema(vitamContext));
        }
        return Optional.empty();
    }
}
