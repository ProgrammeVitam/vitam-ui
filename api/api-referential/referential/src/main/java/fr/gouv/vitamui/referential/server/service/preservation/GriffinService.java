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

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.preservation.GriffinModel;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.preservation.griffin.Griffin;
import fr.gouv.vitamui.referential.server.service.AbstractService;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class GriffinService extends AbstractService {

    private final AdminExternalClient adminExternalClient;
    private final ObjectMapper objectMapper;

    public GriffinService(
        SecurityService securityService,
        AdminExternalClient adminExternalClient,
        ObjectMapper objectMapper
    ) {
        super(securityService);
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
    }

    public List<Griffin> getAll() throws VitamClientException {
        RequestResponseOK<GriffinModel> payload = (RequestResponseOK<GriffinModel>) adminExternalClient.findGriffin(
            buildVitamContext(),
            new Select().getFinalSelect()
        );

        return payload.getResults().stream().map(result -> objectMapper.convertValue(result, Griffin.class)).toList();
    }

    public void put(List<Griffin> griffins) throws VitamClientException, IOException, AccessExternalClientException {
        adminExternalClient.importGriffin(buildVitamContext(), toInputStream(griffins), "update_griffins.json");
    }

    public void update(Griffin griffin) throws VitamClientException, AccessExternalClientException, IOException {
        List<Griffin> nextGriffins = getAll()
            .stream()
            .map(currentGriffin -> {
                if (currentGriffin.id().equals(griffin.id())) {
                    return griffin;
                }
                return currentGriffin;
            })
            .toList();

        this.put(nextGriffins);
    }

    public void delete(Griffin griffin) throws VitamClientException, AccessExternalClientException, IOException {
        List<Griffin> nextGriffins = getAll()
            .stream()
            .filter(currentGriffin -> currentGriffin.id().equals(griffin.id()))
            .toList();

        this.put(nextGriffins);
    }

    private InputStream toInputStream(List<Griffin> griffins) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        objectMapper.writeValue(out, griffins);
        return new ByteArrayInputStream(out.toByteArray());
    }
}
