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

package fr.gouv.vitamui.collect.internal.server.rest;

import fr.gouv.vitamui.collect.common.dto.OperationIdDto;
import fr.gouv.vitamui.collect.common.dto.UpdateArchiveUnitDto;
import fr.gouv.vitamui.collect.common.service.ArchiveUnitService;
import fr.gouv.vitamui.commons.api.dtos.JsonPatch;
import fr.gouv.vitamui.commons.api.dtos.JsonPatchDto;
import fr.gouv.vitamui.commons.api.dtos.MultiJsonPatchDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_PATCH_JSON;

@RestController
@RequestMapping("/archive-units/{transactionId}")
public class ArchiveUnitController {

    final ArchiveUnitService archiveUnitService;

    public ArchiveUnitController(ArchiveUnitService archiveUnitService) {
        this.archiveUnitService = archiveUnitService;
    }

    @PatchMapping
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation("Updates several archive units asynchronously by passing partial changes to apply to an archive unit")
    public ResponseEntity<OperationIdDto> update(
        @PathVariable("transactionId") String transactionId,
        @RequestBody @Validated final Set<UpdateArchiveUnitDto> updateArchiveUnitDtoSet
    ) {
        return ResponseEntity.ok(archiveUnitService.update(transactionId, updateArchiveUnitDtoSet));
    }

    @PatchMapping("/{archiveUnitId}")
    @Consumes(APPLICATION_JSON_PATCH_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation("Updates one archive unit asynchronously by passing a list of operation to do on an archive unit")
    public ResponseEntity<OperationIdDto> update(
        @PathVariable("transactionId") String transactionId,
        @RequestBody @Validated final JsonPatch jsonPatch,
        @RequestParam String archiveUnitId
    ) {
        final JsonPatchDto jsonPatchDto = new JsonPatchDto().setId(archiveUnitId).setJsonPatch(jsonPatch);
        return update(transactionId, jsonPatchDto);
    }

    @PatchMapping("/update/single")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation("Updates one archive unit asynchronously by passing a list of operation to do on this one")
    public ResponseEntity<OperationIdDto> update(
        @PathVariable("transactionId") String transactionId,
        @RequestBody @Validated final JsonPatchDto jsonPatchDto
    ) {
        return ResponseEntity.ok(archiveUnitService.update(transactionId, jsonPatchDto));
    }

    @PatchMapping("/update/multiple")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation("Updates several archive units asynchronously by passing a list of operation to do on these ones")
    public ResponseEntity<OperationIdDto> update(
        @PathVariable("transactionId") String transactionId,
        @RequestBody @Validated final MultiJsonPatchDto multiJsonPatchDto
    ) {
        return ResponseEntity.ok(archiveUnitService.update(transactionId, multiJsonPatchDto));
    }
}
