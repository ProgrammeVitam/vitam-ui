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

package fr.gouv.vitamui.referential.server.rest;

import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.referential.common.dto.preservation.scenario.PreservationScenario;
import fr.gouv.vitamui.referential.server.security.TenantScope;
import fr.gouv.vitamui.referential.server.service.preservation.PreservationScenarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_GET_PRESERVATION_SCENARIOS;
import static fr.gouv.vitamui.commons.api.domain.ServicesData.ROLE_UPDATE_PRESERVATION_SCENARIOS;
import static fr.gouv.vitamui.referential.common.rest.RestApi.PRESERVATION_SCENARIOS_PATH;

@Slf4j
@RestController
@RequestMapping(PRESERVATION_SCENARIOS_PATH)
public class PreservationScenarioController {

    private final PreservationScenarioService preservationScenarioService;

    public PreservationScenarioController(PreservationScenarioService preservationScenarioService) {
        this.preservationScenarioService = preservationScenarioService;
    }

    @GetMapping
    @Secured(ROLE_GET_PRESERVATION_SCENARIOS)
    public List<PreservationScenario> getPreservationScenarios()
        throws VitamClientException, InvalidCreateOperationException {
        return this.preservationScenarioService.getAll();
    }

    @PutMapping
    @TenantScope
    @Secured(ROLE_UPDATE_PRESERVATION_SCENARIOS)
    public void putPreservationScenarios(@RequestBody List<PreservationScenario> scenarios)
        throws AccessExternalClientException, VitamClientException, IOException {
        this.preservationScenarioService.put(scenarios);
    }

    @PostMapping
    @TenantScope
    @Secured(ROLE_UPDATE_PRESERVATION_SCENARIOS)
    public void updatePreservationScenario(@RequestBody PreservationScenario scenario)
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        this.preservationScenarioService.update(scenario);
    }

    @DeleteMapping
    @TenantScope
    @Secured(ROLE_UPDATE_PRESERVATION_SCENARIOS)
    public void deletePreservationScenario(@RequestBody PreservationScenario scenario)
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        this.preservationScenarioService.delete(scenario);
    }
}
