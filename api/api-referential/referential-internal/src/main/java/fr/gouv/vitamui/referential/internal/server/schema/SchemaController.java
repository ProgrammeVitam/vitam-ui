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

import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.referential.common.dto.SchemaDto;
import fr.gouv.vitamui.referential.common.exception.NoCollectionException;
import fr.gouv.vitamui.referential.common.model.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping(CommonConstants.SCHEMAS)
public class SchemaController {

    private final SchemaService schemaService;

    @Autowired
    public SchemaController(final SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping
    @Produces(APPLICATION_JSON)
    public ResponseEntity<List<SchemaDto>> getSchemas(@RequestParam final Set<Collection> collections)
        throws NoCollectionException {
        if (CollectionUtils.isEmpty(collections)) {
            throw new NoCollectionException();
        }

        return ResponseEntity.ok(schemaService.getSchemas(collections));
    }

    @GetMapping("/archive-unit-profile/{id}")
    @Produces(APPLICATION_JSON)
    public ResponseEntity<SchemaDto> getArchiveUnitProfileSchema(@PathVariable @NotNull String id)
        throws VitamClientException {
        return ResponseEntity.ok(schemaService.getArchiveUnitProfileSchema(id));
    }
}
