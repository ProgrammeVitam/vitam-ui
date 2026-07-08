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

package fr.gouv.vitamui.referential.server.service.preservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.preservation.PreservationScenarioModel;
import fr.gouv.vitamui.commons.api.dtos.OperationIdDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.preservation.scenario.PreservationScenario;
import fr.gouv.vitamui.referential.server.security.TenantQueryService;
import fr.gouv.vitamui.referential.server.service.AbstractService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static fr.gouv.vitamui.commons.api.CommonConstants.X_REQUEST_ID_HEADER;

@Service
public class PreservationScenarioService extends AbstractService {

    private final AdminExternalClient adminExternalClient;
    private final ObjectMapper objectMapper;
    private final TenantQueryService tenantQueryService;

    public PreservationScenarioService(
        SecurityService securityService,
        AdminExternalClient adminExternalClient,
        ObjectMapper objectMapper,
        TenantQueryService tenantQueryService
    ) {
        super(securityService);
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
        this.tenantQueryService = tenantQueryService;
    }

    public List<PreservationScenario> getAll() throws VitamClientException, InvalidCreateOperationException {
        RequestResponseOK<PreservationScenarioModel> payload = (RequestResponseOK<
                PreservationScenarioModel
            >) adminExternalClient.findPreservationScenario(
            buildVitamContext(),
            tenantQueryService.applyTenant(new Select()).getFinalSelect()
        );

        return payload
            .getResults()
            .stream()
            .map(result -> objectMapper.convertValue(result, PreservationScenario.class))
            .toList();
    }

    public ResponseEntity<OperationIdDto> put(List<PreservationScenario> scenarios)
        throws VitamClientException, IOException, AccessExternalClientException {
        var response = adminExternalClient.importPreservationScenario(
            buildVitamContext(),
            toInputStream(scenarios),
            "update_scenarios.json"
        );
        return ResponseEntity.status(response.getStatus()).body(
            new OperationIdDto(response.getHeaderString(X_REQUEST_ID_HEADER))
        );
    }

    public void update(PreservationScenario scenario)
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        List<PreservationScenario> nextScenarios = getAll()
            .stream()
            .map(currentScenario -> {
                if (currentScenario.identifier().equals(scenario.identifier())) {
                    return scenario;
                }
                return currentScenario;
            })
            .toList();

        this.put(nextScenarios);
    }

    public void delete(PreservationScenario scenario)
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        List<PreservationScenario> nextScenarios = getAll()
            .stream()
            .filter(currentScenario -> currentScenario.identifier().equals(scenario.identifier()))
            .toList();

        this.put(nextScenarios);
    }

    private InputStream toInputStream(List<PreservationScenario> scenarios) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        objectMapper.writeValue(out, scenarios);
        return new ByteArrayInputStream(out.toByteArray());
    }
}
