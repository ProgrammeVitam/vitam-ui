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
package fr.gouv.vitamui.archive.internal.server.searchcriteria.dao;

import fr.gouv.vitamui.archive.internal.server.TestMongoConfig;
import fr.gouv.vitamui.archive.internal.server.searchcriteria.domain.SearchCriteriaHistory;
import fr.gouv.vitamui.archive.internal.server.utils.Utils;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@Import({TestMongoConfig.class})
@EnableMongoRepositories(basePackageClasses = SearchCriteriaHistoryRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class SearchCriteriaHistoryRepositoryTest {

    @Autowired
    private SearchCriteriaHistoryRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testSaveSearchCriteriaHistory() {
        final SearchCriteriaHistory
            s = repository.save(Utils.buildSearchCriteriaHistory());
        assertThat(s.getUserId()).isEqualTo("999");
    }

    @Test
    public void testGetSearchCriteriaHistoryDtos() {
        final SearchCriteriaHistory
            s1 = repository.save(Utils.buildSearchCriteriaHistory());

        final SearchCriteriaHistory
            s2 = repository.save(Utils.buildSearchCriteriaHistory());
        List<SearchCriteriaHistory> list = (List<SearchCriteriaHistory>) repository.findAll();
        assertThat(list.size()).isEqualTo(2);
    }

}
