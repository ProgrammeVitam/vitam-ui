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
 *
 *
 */

package fr.gouv.vitamui.commons.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.dtos.OntologyDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OntologyServiceReader {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(OntologyServiceReader.class);

    private static final Integer DEFAULT_TENANT_IDENTIFIER = 1;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<OntologyDto> readExternalOntologiesFromFile(Integer tenantId, String ontologiesFilePath)
        throws IOException {

        List<OntologyDto> ontologyDtoList;
        LOGGER.debug("Read ontologies list from file {} for tenant {}", ontologiesFilePath, tenantId);

        File file = new File(ontologiesFilePath);

        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader
            = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException notFoundException) {
            LOGGER.info("No external ontologies file provided ");
            return Collections.emptyList();
        }
        try {
            ontologyDtoList = objectMapper.readValue(resultStringBuilder.toString(), new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            LOGGER.error("Can not parse the ontologies file {}", ontologiesFilePath);
            return Collections.emptyList();
        }

        return ontologyDtoList.stream()
            .filter(ontologyDto -> ontologyDto.getTenantIds().contains(DEFAULT_TENANT_IDENTIFIER) ||
                ontologyDto.getTenantIds().contains(tenantId))
            .collect(Collectors.toList());
    }

}
