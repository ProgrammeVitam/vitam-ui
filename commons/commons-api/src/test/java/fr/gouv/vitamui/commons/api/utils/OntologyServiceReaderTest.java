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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class OntologyServiceReaderTest {

    @Test
    void testReadOntologiesWhenFileExist() throws IOException {
        // Given
        final String ontologyFilePath = "src/test/resources/ontologies/test_external_ontology_fields.json";
        // When

        // Then
        assertThat(OntologyServiceReader.readExternalOntologiesFromFile(1, ontologyFilePath)).isNotNull();
        assertThat(OntologyServiceReader.readExternalOntologiesFromFile(1, ontologyFilePath)).hasSize(2);
        assertThat(OntologyServiceReader.readExternalOntologiesFromFile(19, ontologyFilePath)).hasSize(3);
    }

    @Test
    void testReadOntologiesForTenantId() throws IOException {
        // Given
        final String ontologyFilePath = "src/test/resources/ontologies/test_external_ontology_fields.json";
        // When

        // Then
        assertThat(OntologyServiceReader.readExternalOntologiesFromFile(2, ontologyFilePath)).isNotNull();
        assertThat(OntologyServiceReader.readExternalOntologiesFromFile(2, ontologyFilePath)).hasSize(3);
    }

    @Test
    void testReadOntologiesWithBadFilePath() throws IOException {
        // Given
        final String ontologyFilePath = "src/test/resources/test/ontologies/external_ontology_fields.json";
        // When

        // Then
        assertThat(OntologyServiceReader.readExternalOntologiesFromFile(1, ontologyFilePath)).isEmpty();
    }

    @Test
    void testReadInternalOntologyFromFileWhenFileExist() throws IOException {
        // Given
        final String ontologyFilePath = "src/test/resources/ontologies/test_internal_ontology_fields.json";
        // When

        // Then
        assertThat(OntologyServiceReader.readInternalOntologyFromFile(ontologyFilePath)).isNotNull();
        assertThat(OntologyServiceReader.readInternalOntologyFromFile(ontologyFilePath)).hasSize(4);
    }

    @Test
    void testReadInternalOntologyFromFileWithBadFilePath() throws IOException {
        // Given
        final String ontologyFilePath = "src/test/resources/test/ontologies/internal_ontology_fields.json";
        // When

        // Then
        assertThat(OntologyServiceReader.readInternalOntologyFromFile(ontologyFilePath)).isEmpty();
    }
}
