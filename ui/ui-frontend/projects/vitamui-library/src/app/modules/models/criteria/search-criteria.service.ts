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
import { searchCriteriaConfigs } from './search-criteria-configs';
import { Injectable } from '@angular/core';
import { SearchWithTypeSelectorValue } from '../../../../lib/components/search-with-type-selector/search-with-type-selector.component';
import { SchemaService } from '../../schema';
import { Collection, Schema } from '../schema';
import { firstValueFrom } from 'rxjs';

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
  private schema: Promise<Schema>;

  constructor(schemaService: SchemaService) {
    this.schema = firstValueFrom(schemaService.getSchema(Collection.ARCHIVE_UNIT));
  }

  async ready(): Promise<void> {
    await this.schema;
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

  isValueTranslated(type: ArchiveUnitType) {
    return type === 'FINAL_ACTION_TYPE' || type === 'ALL_ARCHIVE_UNIT_TYPES';
  }

  private async entryToSearchCriteria(
    [key, value]: [key: string, value: string | string[]],
    splittableValues = this.splittableValues,
  ): Promise<SearchCriteriaAddAction[]> {
    const fragments = value instanceof Array ? value : splittableValues.includes(key) ? value.trim().split(',') : [value.trim()];

    return Promise.all(
      fragments.map(async (fragment) => {
        const formattedValue = fragment.trim();
        const dataType = await this.getDataType(key);

        const defaultCriteriaConfig: Partial<SearchCriteriaAddAction> = {
          valueElt: { id: key, value: formattedValue },
          labelElt: formattedValue,
          keyTranslated: false,
          operator: CriteriaOperator.EQ,
          category: SearchCriteriaTypeEnum.FIELDS,
          dataType: dataType,
        };

        const completeCriteriaConfig: SearchCriteriaAddAction = {
          ...defaultCriteriaConfig,
          ...(searchCriteriaConfigs[key] || { keyElt: key }),
        } as SearchCriteriaAddAction;

        return {
          ...completeCriteriaConfig,
          valueTranslated: this.isValueTranslated(completeCriteriaConfig.keyElt),
        } as SearchCriteriaAddAction;
      }),
    );
  }

  private async getDataType(key: string): Promise<CriteriaDataType> {
    const type = (await this.schema).find((s) => s.ApiField === key)?.Type;
    return type === 'DATE' ? CriteriaDataType.DATE : CriteriaDataType.STRING;
  }
}
