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
 */

import { SearchCriteriaAddAction, SearchCriteriaTypeEnum } from './search-criteria.interface';
import { CriteriaDataType, CriteriaOperator } from './criteria.enums';
import { searchCriteriaConfigs } from './search-criteria-configs';
import { Injectable } from '@angular/core';

export type ArchiveUnitType =
  | 'ARCHIVE_UNIT_FILING_UNIT'
  | 'ARCHIVE_UNIT_HOLDING_UNIT'
  | 'ARCHIVE_UNIT_WITH_OBJECTS'
  | 'ARCHIVE_UNIT_WITHOUT_OBJECTS'
  | 'FINAL_ACTION_TYPE'
  | 'ALL_ARCHIVE_UNIT_TYPES'
  | string;

@Injectable({
  providedIn: 'root',
})
export class SearchCriteriaService {
  private splittableValues = ['guid', 'guidopi'];

  toSearchCriteria(
    object: unknown,
    criteriaList: SearchCriteriaAddAction[] = [],
    splittableValues = this.splittableValues,
  ): SearchCriteriaAddAction[] {
    const next: SearchCriteriaAddAction[] = Object.entries(object)
      .filter(([_key, value]) => typeof value === 'boolean' || Boolean(value))
      .flatMap(([key, value]) => {
        if (value instanceof Date) return this.entryToSearchCriteria([key, value.toISOString()], splittableValues);
        if (typeof value === 'string') return this.entryToSearchCriteria([key, value], splittableValues);
        if (typeof value === 'object' && Object.entries(value).length) return this.toSearchCriteria(value, criteriaList);

        console.error('Unhandled case', object, key, value);

        return [];
      });

    return [...criteriaList, ...next];
  }

  isValueTranslated(type: ArchiveUnitType) {
    return type === 'FINAL_ACTION_TYPE' || type === 'ALL_ARCHIVE_UNIT_TYPES';
  }

  private entryToSearchCriteria(
    [key, value]: [key: string, value: string],
    splittableValues = this.splittableValues,
  ): SearchCriteriaAddAction[] {
    const fragments = splittableValues.includes(key) ? value.trim().split(',') : [value.trim()];

    return fragments.map((fragment) => {
      const formattedValue = fragment.trim();

      const defaultCriteriaConfig: Partial<SearchCriteriaAddAction> = {
        valueElt: { id: key, value: formattedValue },
        labelElt: formattedValue,
        keyTranslated: false,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        dataType: CriteriaDataType.STRING,
      };

      const completeCriteriaConfig: SearchCriteriaAddAction = {
        ...defaultCriteriaConfig,
        ...(searchCriteriaConfigs[key] || { keyElt: key }),
      } as SearchCriteriaAddAction;

      return {
        ...completeCriteriaConfig,
        valueTranslated: this.isValueTranslated(completeCriteriaConfig.keyElt),
      } as SearchCriteriaAddAction;
    });
  }
}
