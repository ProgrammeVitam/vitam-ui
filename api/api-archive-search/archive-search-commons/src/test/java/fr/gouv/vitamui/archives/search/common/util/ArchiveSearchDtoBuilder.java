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
package fr.gouv.vitamui.archives.search.common.util;

import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaElementsDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;

import java.time.OffsetDateTime;
import java.util.Arrays;

public class ArchiveSearchDtoBuilder {

    public static SearchCriteriaHistoryDto buildSearchCriteriaHistoryDto(final String id) {
        SearchCriteriaHistoryDto searchCriteriaHistoryDto = new SearchCriteriaHistoryDto();

        searchCriteriaHistoryDto.setName("Search Name");
        searchCriteriaHistoryDto.setUserId("999");
        searchCriteriaHistoryDto.setSavingDate(OffsetDateTime.now());

        SearchCriteriaElementsDto searchCriteriaElements1 = new SearchCriteriaElementsDto();
        searchCriteriaElements1.setCriteria("some_criteria 1");
        searchCriteriaElements1.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaElements1.setValues(
            Arrays.asList(new CriteriaValue("value11", "value12"), new CriteriaValue("value13"))
        );

        SearchCriteriaElementsDto searchCriteriaElements2 = new SearchCriteriaElementsDto();
        searchCriteriaElements2.setCriteria("some_criteria 2");
        searchCriteriaElements2.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);

        searchCriteriaElements2.setValues(
            Arrays.asList(new CriteriaValue("value21"), new CriteriaValue("value22"), new CriteriaValue("value23"))
        );

        SearchCriteriaElementsDto searchCriteriaElementsNodes = new SearchCriteriaElementsDto();
        searchCriteriaElementsNodes.setCriteria("NODE");
        searchCriteriaElementsNodes.setCategory(ArchiveSearchConsts.CriteriaCategory.NODES);
        searchCriteriaElementsNodes.setValues(
            Arrays.asList(new CriteriaValue("node1"), new CriteriaValue("node2"), new CriteriaValue("node3"))
        );

        searchCriteriaHistoryDto.setSearchCriteriaList(
            Arrays.asList(searchCriteriaElements1, searchCriteriaElements2, searchCriteriaElementsNodes)
        );
        return searchCriteriaHistoryDto;
    }
}
