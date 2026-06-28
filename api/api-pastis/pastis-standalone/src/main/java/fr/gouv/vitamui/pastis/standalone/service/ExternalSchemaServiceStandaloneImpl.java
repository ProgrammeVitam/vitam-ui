/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

package fr.gouv.vitamui.pastis.standalone.service;

import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitamui.pastis.server.service.ExternalSchemaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Profile("standalone")
public class ExternalSchemaServiceStandaloneImpl implements ExternalSchemaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSchemaServiceStandaloneImpl.class);

    public static final String EXTERNAL_SCHEMA_DEFINITION_FILE_NAME = "external-schema-definition.json";

    private final ObjectMapper objectMapper;

    @Override
    public List<SchemaResponse> getExternalSchemaModels() {
        LOGGER.info("Looking for schema definition file {}", EXTERNAL_SCHEMA_DEFINITION_FILE_NAME);
        try (InputStream inputStream = new FileInputStream(EXTERNAL_SCHEMA_DEFINITION_FILE_NAME)) {
            CollectionType collectionType = objectMapper
                .getTypeFactory()
                .constructCollectionType(List.class, SchemaResponse.class);
            return objectMapper.readValue(inputStream, collectionType);
        } catch (FileNotFoundException e) {
            LOGGER.info("File {} not found.", EXTERNAL_SCHEMA_DEFINITION_FILE_NAME);
        } catch (IOException e) {
            LOGGER.warn("Error reading schema definition file", e);
        }
        return Collections.emptyList();
    }
}
