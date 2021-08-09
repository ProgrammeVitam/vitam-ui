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
package fr.gouv.vitamui.archive.internal.server.service;

import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConst;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

/**
 * Service to build DSL Query for simple fields criteria for extracting archive units
 */
@Service
public class ArchivesSearchFieldsQueryBuilderService implements IArchivesSearchAppraisalQueryBuilderService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchFieldsQueryBuilderService.class);

    @Override
    public void fillQueryFromCriteriaList(BooleanQuery queryToFill, List<SearchCriteriaEltDto> criteriaList)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(criteriaList)) {
            for (SearchCriteriaEltDto searchCriteria : criteriaList) {
                if (ArchiveSearchConst.TITLE_OR_DESCRIPTION.equals(searchCriteria.getCriteria())) {
                    queryToFill.add(buildTitleAndDescriptionQuery(searchCriteria.getValues(),
                        ArchiveSearchConst.CriteriaOperators.valueOf(searchCriteria.getOperator())));
                } else {
                    if (searchCriteria.getCriteria() == null) {
                        LOGGER.error("Field not mapped correctly  " + searchCriteria.getCriteria());
                        throw new IllegalArgumentException("Field not mapped correctly  ");
                    }
                    VitamQueryHelper.addParameterCriteria(queryToFill,
                        ArchiveSearchConst.CriteriaOperators.valueOf(searchCriteria.getOperator()),
                        searchCriteria.getCriteria(), searchCriteria.getValues());
                }
            }
        }
    }

    private Query buildTitleAndDescriptionQuery(final List<String> searchValues,
        ArchiveSearchConst.CriteriaOperators operator)
        throws InvalidCreateOperationException {
        BooleanQuery subQueryAnd = and();
        if (!CollectionUtils.isEmpty(searchValues)) {
            for (String value : searchValues) {
                BooleanQuery subQueryOr = or();
                subQueryOr
                    .add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConst.DESCRIPTION, value, operator));
                subQueryOr.add(VitamQueryHelper.buildSubQueryByOperator(ArchiveSearchConst.TITLE, value, operator));
                subQueryAnd.add(subQueryOr);
            }
        }
        return subQueryAnd;
    }
}

