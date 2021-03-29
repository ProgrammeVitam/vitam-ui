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
package fr.gouv.vitamui.archives.search.external.server.utils;

import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaElementsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriasDto;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;

import java.time.OffsetDateTime;
import java.util.Arrays;

public class Utils {

    /*public static SearchCriteriaHistory buildSearchCriteriaHistory() {
        SearchCriteriaHistory searchCriteriaHistory = new SearchCriteriaHistory();

        searchCriteriaHistory.setName("Search Name");
        searchCriteriaHistory.setUserId("999");
        searchCriteriaHistory.setSavingDate(OffsetDateTime.now());

        SearchCriteriasDto searchCriterias = new SearchCriteriasDto();
        searchCriterias.setNodes(Arrays.asList("node1", "node2", "node3"));

        SearchCriteriaElementsDto searchCriteriaElements1 = new SearchCriteriaElementsDto();
        searchCriteriaElements1.setCriteria("some_criteria 1");
        searchCriteriaElements1.setValues(Arrays.asList("value11", "value12", "value13"));

        SearchCriteriaElementsDto searchCriteriaElements2 = new SearchCriteriaElementsDto();
        searchCriteriaElements2.setCriteria("some_criteria 2");
        searchCriteriaElements2.setValues(Arrays.asList("value21", "value22", "value23"));

        searchCriterias.setCriteriaList(Arrays.asList(searchCriteriaElements1, searchCriteriaElements2));

        searchCriteriaHistory.setSearchCriteriaList(Arrays.asList(searchCriterias, searchCriterias));
        return searchCriteriaHistory;
    }*/

    public static AuthUserDto buildAuthUserDto() {
        final AuthUserDto extUserDto = new AuthUserDto(buildUserDto("1","eee@eee.fr","groupId","customerId","Level"));
        return extUserDto;
    }

    public static UserDto buildUserDto(final String id, final String email, final String groupId, final String customerId, final String level) {
        final UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setEmail(email);
        userDto.setFirstname("Machin");
        userDto.setLastname("Bidule");
        userDto.setCustomerId(customerId);
        userDto.setGroupId(groupId);
        userDto.setOtp(true);
        userDto.setIdentifier("code");
        userDto.setStatus(UserStatusEnum.ENABLED);
        userDto.setType(UserTypeEnum.NOMINATIVE);
        userDto.setLanguage(LanguageDto.FRENCH.toString());
        userDto.setLevel(level);
        userDto.setMobile("+3312345678");
        userDto.setPhone("+3387654321");
        userDto.setAddress(new AddressDto());
        return userDto;
    }
}
