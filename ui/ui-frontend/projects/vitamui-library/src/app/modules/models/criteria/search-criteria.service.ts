/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { SearchCriteriaAddAction, SearchCriteriaTypeEnum } from './search-criteria.interface';
import { CriteriaDataType, CriteriaOperator } from './criteria.enums';
import {
  ACCESS_RULE,
  ACCESS_RULE_IDENTIFIER,
  APPRAISAL_RULE,
  APPRAISAL_RULE_IDENTIFIER,
  ARCHIVE_UNIT_WITH_ERRORS,
  CLASSIFICATION_RULE,
  DISSEMINATION_RULE,
  DISSEMINATION_RULE_IDENTIFIER,
  ELIM_TECH_ID_DUA,
  END_DATE_ACCESS,
  END_DATE_DISSEMINATION,
  END_DATE_DUA,
  END_DATE_DUC,
  END_DATE_FIELDS,
  END_DATE_REUSE,
  getSearchCriteriaConfig,
  HOLD_RULE,
  ID_ACCESS,
  ID_DISSEMINATION,
  ID_DUA,
  ID_DUC,
  ID_REUSE,
  INTERVAL_DATE_ACCESS,
  INTERVAL_DATE_DISSEMINATION,
  INTERVAL_DATE_DUA,
  INTERVAL_DATE_DUC,
  INTERVAL_DATE_FIELDS,
  INTERVAL_DATE_REUSE,
  NODES,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_HAS_NO_ONE,
  REUSE_RULE,
  REUSE_RULE_IDENTIFIER,
  STORAGE_RULE,
  STORAGE_RULE_IDENTIFIER,
  TITLE_ACCESS,
  TITLE_DISSEMINATION,
  TITLE_DUA,
  TITLE_DUC,
  TITLE_REUSE,
  translatedKeys,
} from './search-criteria-configs';
import { Injectable } from '@angular/core';
import { SearchWithTypeSelectorValue } from '../../../../lib/components/search-with-type-selector/search-with-type-selector.component';
import { SchemaService } from '../../schema';
import { Collection, Schema } from '../schema';
import { firstValueFrom } from 'rxjs';

const SEPARATOR = '|';

@Injectable({
  providedIn: 'root',
})
export class SearchCriteriaService {
  private splittableValues = ['guid', 'guidopi'];
  private schemaPromise: Promise<Schema>;
  private schema: Schema;

  constructor(schemaService: SchemaService) {
    this.schemaPromise = firstValueFrom(schemaService.getSchema(Collection.ARCHIVE_UNIT));
  }

  async ready(): Promise<void> {
    await this.schemaPromise.then((schema) => (this.schema = schema));
  }

  getSchema(): Schema {
    return this.schema;
  }

  async toSearchCriteria(
    object: unknown,
    criteriaList: SearchCriteriaAddAction[] = [],
    splittableValues = this.splittableValues,
  ): Promise<SearchCriteriaAddAction[]> {
    const next: SearchCriteriaAddAction[] = (
      await Promise.all(
        Object.entries(object)
          .filter(([_key, value]) => typeof value === 'boolean' || Boolean(value))
          .flatMap(async ([key, value]): Promise<SearchCriteriaAddAction[]> => {
            if (value instanceof Date) return await this.entryToSearchCriteria([key, value.toISOString()], splittableValues);
            if (typeof value === 'string' || value instanceof Array)
              return await this.entryToSearchCriteria([key, value], splittableValues);
            if (typeof value === 'object' && value.value && value.type?.value !== undefined) {
              const searchWithTypeSelectorValue = value as SearchWithTypeSelectorValue;
              const type = searchWithTypeSelectorValue.type.value;
              const keyWithType = type ? `${key.toLowerCase()}.${type}` : key.toLowerCase();
              return await this.toSearchCriteria({ [keyWithType]: searchWithTypeSelectorValue.value });
            }
            if (typeof value === 'object' && Object.entries(value).length) return await this.toSearchCriteria(value, criteriaList);

            console.error('Unhandled case', object, key, value);

            return [];
          }),
      )
    ).flat();

    return [...criteriaList, ...next];
  }

  isValueTranslated(type: string) {
    return translatedKeys.includes(type);
  }

  private async entryToSearchCriteria(
    [key, value]: [key: string, value: string | string[]],
    splittableValues = this.splittableValues,
  ): Promise<SearchCriteriaAddAction[]> {
    const fragments = value instanceof Array ? value : splittableValues.includes(key) ? value.trim().split(',') : [value.trim()];

    return Promise.all(
      fragments.map(async (fragment) => {
        const formattedValue = fragment.trim();

        const categoryForKey = this.getCategory(key);
        const operator = this.getOperator(fragment, key);

        const beginDate = this.getBeginDate(fragment, key);
        const endDate = this.getEndDate(fragment, key);
        const dataType = this.getDataType(key);
        const titleForVirtual =
          key === 'VIRTUAL'
            ? this.setParamForVirtualPositions(fragment, fragment.split('/').length - 1)
              ? this.setParamForVirtualPositions(fragment, fragment.split('/').length - 1)
              : null
            : null;
        const idForVirtual =
          key === 'VIRTUAL'
            ? this.setParamForVirtualPositions(fragment, fragment.split('/').length - 2)
              ? this.setParamForVirtualPositions(fragment, fragment.split('/').length - 2)
              : null
            : null;

        const defaultCriteriaConfig: Partial<SearchCriteriaAddAction> = {
          valueElt: {
            id: key,
            value: key === 'VIRTUAL' ? this.setValueForVirtualPositions(fragment) : formattedValue,
            beginInterval: beginDate,
            endInterval: endDate,
            virtualNodeRealParentTitle: key === 'VIRTUAL' ? titleForVirtual : null,
            virtualNodeRealParentId: key === 'VIRTUAL' ? idForVirtual : null,
          },
          labelElt: formattedValue,
          keyTranslated: true,
          operator: operator,
          category: categoryForKey,
          dataType: dataType,
        };

        const completeCriteriaConfig: SearchCriteriaAddAction = {
          ...defaultCriteriaConfig,
          ...getSearchCriteriaConfig(fragment, key),
        } as SearchCriteriaAddAction;

        return {
          ...completeCriteriaConfig,
          valueTranslated: this.isValueTranslated(completeCriteriaConfig.keyElt),
        } as SearchCriteriaAddAction;
      }),
    );
  }

  private setValueForVirtualPositions(fragment: string) {
    const fragments = fragment.split('/');
    let acc = '';
    for (let i = 1; i < fragments.length - 2; i++) {
      acc += '/' + fragments[i];
    }
    return acc;
  }

  private setParamForVirtualPositions(fragment: string, pos: number) {
    return fragment.split('/')[pos];
  }

  private getCategory(key: string) {
    let categoryForKey = SearchCriteriaTypeEnum.FIELDS;
    switch (key) {
      case APPRAISAL_RULE:
      case ID_DUA:
      case TITLE_DUA:
      case END_DATE_DUA:
      case INTERVAL_DATE_DUA:
      case ELIM_TECH_ID_DUA:
      case APPRAISAL_RULE_IDENTIFIER:
        categoryForKey = SearchCriteriaTypeEnum.APPRAISAL_RULE;
        break;
      case ACCESS_RULE:
      case ID_ACCESS:
      case TITLE_ACCESS:
      case END_DATE_ACCESS:
      case INTERVAL_DATE_ACCESS:
      case ACCESS_RULE_IDENTIFIER:
        categoryForKey = SearchCriteriaTypeEnum.ACCESS_RULE;
        break;
      case CLASSIFICATION_RULE:
        categoryForKey = SearchCriteriaTypeEnum.CLASSIFICATION_RULE;
        break;
      case DISSEMINATION_RULE:
      case ID_DISSEMINATION:
      case TITLE_DISSEMINATION:
      case END_DATE_DISSEMINATION:
      case INTERVAL_DATE_DISSEMINATION:
      case DISSEMINATION_RULE_IDENTIFIER:
        categoryForKey = SearchCriteriaTypeEnum.DISSEMINATION_RULE;
        break;
      case REUSE_RULE:
      case ID_REUSE:
      case TITLE_REUSE:
      case END_DATE_REUSE:
      case INTERVAL_DATE_REUSE:
      case REUSE_RULE_IDENTIFIER:
        categoryForKey = SearchCriteriaTypeEnum.REUSE_RULE;
        break;
      case STORAGE_RULE:
      case ID_DUC:
      case TITLE_DUC:
      case END_DATE_DUC:
      case INTERVAL_DATE_DUC:
      case STORAGE_RULE_IDENTIFIER:
        categoryForKey = SearchCriteriaTypeEnum.STORAGE_RULE;
        break;
      case HOLD_RULE:
        categoryForKey = SearchCriteriaTypeEnum.HOLD_RULE;
        break;
      case NODES:
        categoryForKey = SearchCriteriaTypeEnum.NODES;
        break;
    }

    return categoryForKey;
  }

  private getOperator(fragment: string, key: string) {
    let operator = CriteriaOperator.EQ;
    if (fragment === ORIGIN_HAS_NO_ONE) {
      operator = CriteriaOperator.MISSING;
    } else if (fragment === ORIGIN_HAS_AT_LEAST_ONE) {
      operator = CriteriaOperator.EXISTS;
    } else if (INTERVAL_DATE_FIELDS.includes(key)) {
      operator = CriteriaOperator.BETWEEN;
    } else if (END_DATE_FIELDS.includes(key)) {
      operator = CriteriaOperator.LTE;
    } else if (fragment === ARCHIVE_UNIT_WITH_ERRORS) {
      operator = CriteriaOperator.EXISTS;
    }

    return operator;
  }

  private getBeginDate(fragment: string, key: string) {
    let beginDate = '';
    if (INTERVAL_DATE_FIELDS.includes(key)) {
      beginDate = fragment.split(SEPARATOR)[0];
    }
    return beginDate;
  }

  private getEndDate(fragment: string, key: string) {
    let endDate = '';
    if (INTERVAL_DATE_FIELDS.includes(key)) {
      endDate = fragment.split(SEPARATOR)[1];
    } else if (END_DATE_FIELDS.includes(key)) {
      endDate = fragment;
    }
    return endDate;
  }

  private getDataType(key: string) {
    let dataType: CriteriaDataType;
    if (END_DATE_FIELDS.includes(key) || INTERVAL_DATE_FIELDS.includes(key)) {
      dataType = CriteriaDataType.INTERVAL;
    } else {
      dataType = CriteriaDataType.STRING;
    }
    return dataType;
  }
}
