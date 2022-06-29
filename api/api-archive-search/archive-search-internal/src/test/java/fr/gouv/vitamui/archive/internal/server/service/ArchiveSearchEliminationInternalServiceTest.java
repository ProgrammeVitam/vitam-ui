/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.archive.internal.server.service;


import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.EliminationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ArchiveSearchEliminationInternalServiceTest {

    @MockBean(name = "eliminationService")
    private EliminationService eliminationService;

    @MockBean(name = "archiveSearchInternalService")
    private ArchiveSearchInternalService archiveSearchInternalService;

    @InjectMocks
    private ArchiveSearchEliminationInternalService archiveSearchEliminationInternalService;


    public final String ELIMINATION_ANALYSIS_QUERY = "data/elimination/query.json";
    public final String ELIMINATION_ANALYSIS_FINAL_QUERY = "data/elimination/expected_query.json";

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchEliminationInternalService =
            new ArchiveSearchEliminationInternalService(archiveSearchInternalService, eliminationService);
    }

    @Test
    public void getFinalEliminationConstructedQuery() throws Exception {
        JsonNode fromString = JsonHandler.getFromFile(PropertiesUtils.findFile(ELIMINATION_ANALYSIS_QUERY));
        EliminationRequestBody eliminationRequestBody2 =
            archiveSearchEliminationInternalService.getEliminationRequestBody(fromString);

        JsonNode resultExpected = JsonHandler.getFromFile(PropertiesUtils.findFile(ELIMINATION_ANALYSIS_FINAL_QUERY));
        Assertions.assertThat(eliminationRequestBody2.getDslRequest()).isEqualTo(resultExpected);
    }

}
