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
import { CriteriaDataType, CriteriaOperator } from '../../../app/modules/models/criteria/criteria.enums';
import { CriteriaValue, SearchCriteriaDto, SearchCriteriaTypeEnum } from '../../../app/modules/models/criteria/search-criteria.interface';
import { Direction } from '../../../app/modules/vitamui-table/direction.enum';

export function deepCopy<T>(object: T): T {
  return JSON.parse(JSON.stringify(object));
}
export function dedupe<T>(list: T[]): T[] {
  return [...new Set(list)];
}

export const computeChildrenCountQuery = (ids: string[], options: Partial<SearchCriteriaDto>): SearchCriteriaDto => {
  return {
    criteriaList: [
      {
        criteria: '#allunitups',
        values: ids.map(
          (id, index): CriteriaValue => ({
            id: `GUID_${index}`,
            value: id,
          }),
        ),
        category: SearchCriteriaTypeEnum.FIELDS,
        dataType: CriteriaDataType.STRING,
        operator: CriteriaOperator.IN,
      },
    ],
    pageNumber: 0,
    size: ids.length,
    ...options,
  };
};

const childrenCountOptions: Partial<SearchCriteriaDto> = { size: 1, includedFields: ['#id'] };

export const childrenCountQuery = (ids: string[]) => computeChildrenCountQuery(ids, { ...childrenCountOptions, trackTotalHits: false });
export const exactChildrenCountQuery = (ids: string[]) => computeChildrenCountQuery(ids, { ...childrenCountOptions, trackTotalHits: true });

export const extractIds = (query: SearchCriteriaDto): string[] =>
  query.criteriaList
    .filter((criterion) => criterion.criteria == 'GUID')
    .filter((criterion) => Boolean(criterion.values?.length))
    .map((criterion) => criterion.values[0].value);

export const isQueryContainsIds = (query: SearchCriteriaDto) => query.criteriaList.some((criterion) => criterion.criteria === 'GUID');

export const searchAllRecordsQuery = {
  criteriaList: [
    {
      criteria: 'DescriptionLevel',
      values: [{ id: 'RecordGrp', value: 'RecordGrp' }],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    },
  ],
  pageNumber: 0,
  size: 100,
  sortingCriteria: {
    criteria: 'Title',
    sorting: Direction.ASCENDANT,
  },
  trackTotalHits: false,
  computeMgtRulesFacets: false,
};

export const searchByIdsQuery = (ids: string[]): SearchCriteriaDto => {
  return {
    criteriaList: [
      {
        criteria: '#id',
        values: ids.map(
          (id, index): CriteriaValue => ({
            id: `GUID_${index}`,
            value: id,
          }),
        ),
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        dataType: CriteriaDataType.STRING,
      },
    ],
    pageNumber: 0,
    size: ids.length,
    includedFields: ['Title', 'Title_', '#id'],
  };
};

export const withAdditionalFieldsQuery = (query: SearchCriteriaDto, fields: string[]): SearchCriteriaDto => {
  const copy = deepCopy(query);

  return {
    ...copy,
    includedFields: dedupe([...(copy?.includedFields ?? []), ...fields]),
  };
};
