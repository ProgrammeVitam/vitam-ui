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
package fr.gouv.vitamui.iam.internal.server.externalParameters.dao;

import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link ExternalParametersRepository}
 *
 */

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = ExternalParametersRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class ExternalParametersRepositoryTest {

    @Autowired
    private ExternalParametersRepository repository;

    private static final String EXTERNAL_PARAMETERS_ID = "external_param_default";

    @AfterAll
    public void cleanUp() {
        repository.deleteById(EXTERNAL_PARAMETERS_ID);
    }

    @Test
    public void testSave() {
    	final ExternalParameters parameters = new ExternalParameters();
    	parameters.setId(EXTERNAL_PARAMETERS_ID);

        final ExternalParameters created  = repository.save(parameters);
        assertThat(created.getId()).isEqualTo(EXTERNAL_PARAMETERS_ID);
    }

    @Test
    public void testFindById() {
    	Query query = new Query();
        query.addCriteria(Criteria.where("id").is(EXTERNAL_PARAMETERS_ID));

        final Optional<ExternalParameters> externalParameters = repository.findOne(query);

        assertNotNull(externalParameters);
        assertNotNull(externalParameters.get());
        assertEquals(externalParameters.get().getId(), EXTERNAL_PARAMETERS_ID);
    }
}
