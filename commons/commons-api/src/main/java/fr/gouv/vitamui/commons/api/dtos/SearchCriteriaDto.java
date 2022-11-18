/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.commons.api.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class
SearchCriteriaDto implements Serializable {
    /**
     * Criteria list for searching archive units
     */

    private List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
    private List<String> fieldsList = new ArrayList<>();
    private SearchCriteriaSort sortingCriteria;
    private Integer pageNumber = 0;
    private Integer size = 1;
    private String language = Locale.FRENCH.getLanguage();
    private boolean trackTotalHits;
    private boolean computeFacets;
    private Long threshold;


    public List<String> extractNodesCriteria() {
        return this.getCriteriaList().stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> ArchiveSearchConsts.CriteriaCategory.NODES
                .equals(searchCriteriaEltDto.getCategory())).flatMap(criteria -> criteria.getValues().stream())
            .map(CriteriaValue::getValue).collect(Collectors.toList());
    }

    public List<SearchCriteriaEltDto> extractCriteriaListByCategory(ArchiveSearchConsts.CriteriaCategory category) {
        return this.getCriteriaList().stream().filter(
            Objects::nonNull).filter(searchCriteriaEltDto -> category
            .equals(searchCriteriaEltDto.getCategory())).collect(Collectors.toList());
    }

    public List<SearchCriteriaEltDto> extractMgtRuleCriteriaList() {
        return this.getCriteriaList().stream().filter(
            Objects::nonNull).filter(searchCriteriaEltDto -> (ArchiveSearchConsts.CriteriaMgtRulesCategory
            .contains(searchCriteriaEltDto.getCategory().name()))).collect(Collectors.toList());
    }

    public List<SearchCriteriaEltDto> extractCriteriaListByCategoryAndFieldNames(
        ArchiveSearchConsts.CriteriaCategory category, List<String> fieldNames) {
        return this.getCriteriaList().stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> category
                .equals(searchCriteriaEltDto.getCategory()) && fieldNames.contains(searchCriteriaEltDto.getCriteria()))
            .collect(Collectors.toList());
    }
}
