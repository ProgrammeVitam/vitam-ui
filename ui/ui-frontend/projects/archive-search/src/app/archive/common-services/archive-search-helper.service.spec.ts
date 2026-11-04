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
import { TestBed } from '@angular/core/testing';
import {
  CriteriaDataType,
  CriteriaOperator,
  CriteriaSearchCriteria,
  CriteriaValue,
  FilingHoldingSchemeNode,
  ORPHANS_NODE_ID,
  SearchCriteriaEltDto,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../core/archive-shared-data.service';
import { ArchiveService } from '../archive.service';
import { ArchiveSearchHelperService } from './archive-search-helper.service';

type SharedDataMock = Record<
  | 'emitSearchCriteriaChange'
  | 'emitNodeTarget'
  | 'addSimpleSearchCriteriaSubject'
  | 'sendAppraisalFromMainSearchCriteriaAction'
  | 'sendAccessFromMainSearchCriteriaAction'
  | 'sendStorageFromMainSearchCriteriaAction'
  | 'sendReuseFromMainSearchCriteriaAction'
  | 'sendDisseminationFromMainSearchCriteriaAction'
  | 'sendRemoveFromChildSearchCriteriaAction',
  ReturnType<typeof vi.fn>
>;

const RULE_CATEGORY_SENDERS: [SearchCriteriaTypeEnum, keyof SharedDataMock][] = [
  [SearchCriteriaTypeEnum.APPRAISAL_RULE, 'sendAppraisalFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.ACCESS_RULE, 'sendAccessFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.STORAGE_RULE, 'sendStorageFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.REUSE_RULE, 'sendReuseFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.DISSEMINATION_RULE, 'sendDisseminationFromMainSearchCriteriaAction'],
];

const criteria = (
  key: string,
  category: SearchCriteriaTypeEnum,
  values: CriteriaValue[],
  dataType: string = CriteriaDataType.STRING,
  status = SearchCriteriaStatusEnum.NOT_INCLUDED,
): CriteriaSearchCriteria => ({
  key,
  category,
  operator: CriteriaOperator.EQ,
  dataType,
  keyTranslated: false,
  valueTranslated: false,
  values: values.map((value) => ({ value, label: value.value, status, keyTranslated: false, valueTranslated: false })),
});

const toMap = (...criteriaList: CriteriaSearchCriteria[]) => new Map(criteriaList.map((c) => [c.key, c]));

describe('ArchiveSearchHelperService', () => {
  let service: ArchiveSearchHelperService;
  let sharedData: SharedDataMock;

  beforeEach(() => {
    sharedData = {
      emitSearchCriteriaChange: vi.fn(),
      emitNodeTarget: vi.fn(),
      addSimpleSearchCriteriaSubject: vi.fn(),
      sendAppraisalFromMainSearchCriteriaAction: vi.fn(),
      sendAccessFromMainSearchCriteriaAction: vi.fn(),
      sendStorageFromMainSearchCriteriaAction: vi.fn(),
      sendReuseFromMainSearchCriteriaAction: vi.fn(),
      sendDisseminationFromMainSearchCriteriaAction: vi.fn(),
      sendRemoveFromChildSearchCriteriaAction: vi.fn(),
    };
    // The criteria classification helpers of ArchiveService are pure functions: reuse the real implementation.
    const proto = ArchiveService.prototype;
    const archiveServiceMock = {
      isStorageRuleCriteria: proto.isStorageRuleCriteria,
      isAppraisalRuleCriteria: proto.isAppraisalRuleCriteria,
      isAccessRuleCriteria: proto.isAccessRuleCriteria,
      isDisseminationRuleCriteria: proto.isDisseminationRuleCriteria,
      isReuseRuleCriteria: proto.isReuseRuleCriteria,
      isClassificationRuleCriteria: proto.isClassificationRuleCriteria,
      isWaitingToRecalculateCriteria: proto.isWaitingToRecalculateCriteria,
      isEliminationTenchnicalIdCriteria: proto.isEliminationTenchnicalIdCriteria,
    };
    TestBed.configureTestingModule({
      providers: [
        { provide: ArchiveSharedDataService, useValue: sharedData },
        { provide: ArchiveService, useValue: archiveServiceMock },
      ],
    });
    service = TestBed.inject(ArchiveSearchHelperService);
  });

  describe('addCriteria', () => {
    const add = (
      map: Map<string, CriteriaSearchCriteria>,
      keys: string[],
      key: string,
      value: CriteriaValue,
      category = SearchCriteriaTypeEnum.FIELDS,
      emit = true,
      dataType: string = CriteriaDataType.STRING,
    ) => service.addCriteria(map, keys, 0, key, value, 'label', true, CriteriaOperator.EQ, category, false, dataType, emit);

    it('should do nothing when the key or the value is missing', () => {
      const map = new Map<string, CriteriaSearchCriteria>();
      add(map, [], '', { id: 'a', value: 'a' });
      add(map, [], 'TITLE', null);

      expect(map.size).toBe(0);
      expect(sharedData.emitSearchCriteriaChange).not.toHaveBeenCalled();
    });

    it('should create a new criteria, register its key and notify the simple search', () => {
      const map = new Map<string, CriteriaSearchCriteria>();
      const keys = ['OTHER'];

      add(map, keys, 'TITLE', { id: 'foo', value: 'foo' });

      expect(keys).toEqual(['OTHER', 'TITLE']);
      expect(map.get('TITLE')).toEqual({
        key: 'TITLE',
        values: [
          {
            value: { id: 'foo', value: 'foo' },
            label: 'label',
            id: 'foo',
            valueShown: true,
            status: SearchCriteriaStatusEnum.NOT_INCLUDED,
            keyTranslated: true,
            valueTranslated: false,
          },
        ],
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        keyTranslated: true,
        valueTranslated: false,
        dataType: CriteriaDataType.STRING,
      });
      expect(sharedData.emitSearchCriteriaChange).toHaveBeenCalledWith(map);
      expect(sharedData.addSimpleSearchCriteriaSubject).toHaveBeenCalledWith({
        keyElt: 'TITLE',
        valueElt: { id: 'foo', value: 'foo' },
        labelElt: 'label',
        keyTranslated: true,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        valueTranslated: false,
        dataType: CriteriaDataType.STRING,
      });
    });

    it('should put node keys first and not notify the simple search when emit is false', () => {
      const map = new Map<string, CriteriaSearchCriteria>();
      const keys = ['TITLE'];

      add(map, keys, 'NODE', { id: 'n1', value: 'n1' }, SearchCriteriaTypeEnum.NODES, false);

      expect(keys).toEqual(['NODE', 'TITLE']);
      expect(map.has('NODE')).toBe(true);
      expect(sharedData.emitSearchCriteriaChange).toHaveBeenCalled();
      expect(sharedData.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
    });

    it('should not register a key twice', () => {
      const map = new Map<string, CriteriaSearchCriteria>();
      const keys = ['TITLE'];

      add(map, keys, 'TITLE', { id: 'foo', value: 'foo' });

      expect(keys).toEqual(['TITLE']);
    });

    it('should append a new value to an existing string criteria but ignore an already present value', () => {
      const map = toMap(criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'foo', value: 'foo' }]));

      add(map, ['TITLE'], 'TITLE', { id: 'foo', value: 'foo' });
      expect(map.get('TITLE').values.length).toBe(1);

      add(map, ['TITLE'], 'TITLE', { id: 'bar', value: 'bar' });
      expect(map.get('TITLE').values.map((v) => v.value.value)).toEqual(['foo', 'bar']);
      expect(map.get('TITLE').values[1].status).toBe(SearchCriteriaStatusEnum.NOT_INCLUDED);
    });

    it('should distinguish virtual nodes by their real parent', () => {
      const map = toMap(criteria('VIRTUAL', SearchCriteriaTypeEnum.NODES, [{ id: 'v', value: 'v', virtualNodeRealParentId: 'p1' }]));

      add(map, ['VIRTUAL'], 'VIRTUAL', { id: 'v', value: 'v', virtualNodeRealParentId: 'p1' }, SearchCriteriaTypeEnum.NODES);
      expect(map.get('VIRTUAL').values.length).toBe(1);

      add(map, ['VIRTUAL'], 'VIRTUAL', { id: 'v', value: 'v', virtualNodeRealParentId: 'p2' }, SearchCriteriaTypeEnum.NODES);
      expect(map.get('VIRTUAL').values.map((v) => v.value.virtualNodeRealParentId)).toEqual(['p1', 'p2']);
    });

    it('should compare interval criteria on both bounds', () => {
      const interval = { id: 'i', beginInterval: '2020', endInterval: '2021' };
      const map = toMap(criteria('DATE_RANGE', SearchCriteriaTypeEnum.FIELDS, [interval], CriteriaDataType.INTERVAL));

      add(map, [], 'DATE_RANGE', { ...interval }, SearchCriteriaTypeEnum.FIELDS, true, CriteriaDataType.INTERVAL);
      expect(map.get('DATE_RANGE').values.length).toBe(1);

      add(map, [], 'DATE_RANGE', { ...interval, endInterval: '2022' }, SearchCriteriaTypeEnum.FIELDS, true, CriteriaDataType.INTERVAL);
      expect(map.get('DATE_RANGE').values.length).toBe(2);
    });

    it.each(RULE_CATEGORY_SENDERS)('should forward an added %s criteria to its facet when emit is true', (category, sender) => {
      const value = { id: 'R1', value: 'R1' };

      add(new Map(), [], 'RULE_ID', value, category, true);
      expect(sharedData[sender]).toHaveBeenCalledWith({ keyElt: 'RULE_ID', valueElt: value, action: 'ADD' });
      expect(sharedData.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
    });

    it.each(RULE_CATEGORY_SENDERS)('should not forward an added %s criteria when emit is false', (category, sender) => {
      add(new Map(), [], 'RULE_ID', { id: 'R1', value: 'R1' }, category, false);
      expect(sharedData[sender]).not.toHaveBeenCalled();
    });

    it.each(RULE_CATEGORY_SENDERS)(
      'should translate ORIGIN_WAITING_RECALCULATE of %s into a WAITING_RECALCULATE field criteria',
      (category, sender) => {
        const map = new Map<string, CriteriaSearchCriteria>();
        const keys: string[] = [];
        const value = { id: 'ORIGIN_WAITING_RECALCULATE', value: 'ORIGIN_WAITING_RECALCULATE' };

        add(map, keys, 'ORIGIN_KEY', value, category, false);

        expect(keys).toEqual(['WAITING_RECALCULATE']);
        expect(map.has('ORIGIN_KEY')).toBe(false);
        expect(map.get('WAITING_RECALCULATE')).toEqual(
          expect.objectContaining({ category: SearchCriteriaTypeEnum.FIELDS, keyTranslated: true, valueTranslated: true }),
        );
        expect(map.get('WAITING_RECALCULATE').values[0].value).toEqual({ id: 'WAITING_RECALCULATE', value: 'true' });
        expect(sharedData[sender]).toHaveBeenCalledWith({ keyElt: 'ORIGIN_KEY', valueElt: value, action: 'ADD' });
      },
    );
  });

  describe('removeCriteria', () => {
    it('should do nothing on an empty criteria map', () => {
      service.removeCriteria('TITLE', { id: 'foo', value: 'foo' }, true, [], new Map(), 0);
      expect(sharedData.emitSearchCriteriaChange).not.toHaveBeenCalled();
    });

    it('should remove only the matching value and keep the criteria when values remain', () => {
      const map = toMap(
        criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, [
          { id: 'foo', value: 'foo' },
          { id: 'bar', value: 'bar' },
        ]),
      );
      const keys = ['TITLE'];

      service.removeCriteria('TITLE', { id: 'foo', value: 'foo' }, false, keys, map, 1);

      expect(map.get('TITLE').values.map((v) => v.value.value)).toEqual(['bar']);
      expect(keys).toEqual(['TITLE']);
      expect(sharedData.emitSearchCriteriaChange).toHaveBeenCalledWith(map);
      expect(sharedData.sendRemoveFromChildSearchCriteriaAction).not.toHaveBeenCalled();
    });

    it('should drop the criteria and its key when its last value is removed, and notify the simple search', () => {
      const map = toMap(
        criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'foo', value: 'foo' }]),
        criteria('DESCRIPTION', SearchCriteriaTypeEnum.FIELDS, [{ id: 'd', value: 'd' }]),
      );
      const keys = ['TITLE', 'DESCRIPTION'];

      service.removeCriteria('TITLE', { id: 'foo', value: 'foo' }, true, keys, map, 2);

      expect(map.has('TITLE')).toBe(false);
      expect(map.has('DESCRIPTION')).toBe(true);
      expect(keys).toEqual(['DESCRIPTION']);
      expect(sharedData.sendRemoveFromChildSearchCriteriaAction).toHaveBeenCalledWith({
        keyElt: 'TITLE',
        valueElt: { id: 'foo', value: 'foo' },
        action: 'REMOVE',
      });
    });

    it.each(['NODE', ORPHANS_NODE_ID])('should untick the %s node in the tree', (key) => {
      const map = toMap(criteria(key, SearchCriteriaTypeEnum.NODES, [{ id: 'n1', value: 'n1' }]));

      service.removeCriteria(key, { id: 'n1', value: 'n1' }, true, [key], map, 1);

      expect(sharedData.emitNodeTarget).toHaveBeenCalledWith('n1');
    });

    it('should untick a virtual node using its real parent id', () => {
      const value = { id: 'v', value: 'v', virtualNodeRealParentId: 'p' };
      const map = toMap(criteria('VIRTUAL', SearchCriteriaTypeEnum.NODES, [value, { ...value, virtualNodeRealParentId: 'other' }]));

      service.removeCriteria('VIRTUAL', value, true, ['VIRTUAL'], map, 1);

      expect(map.get('VIRTUAL').values.map((v) => v.value.virtualNodeRealParentId)).toEqual(['other']);
      expect(sharedData.emitNodeTarget).toHaveBeenCalledWith('v-p');
    });

    it('should not notify the tree nor facets when emit is false', () => {
      const map = toMap(criteria('NODE', SearchCriteriaTypeEnum.NODES, [{ id: 'n1', value: 'n1' }]));

      service.removeCriteria('NODE', { id: 'n1', value: 'n1' }, false, ['NODE'], map, 1);

      expect(map.size).toBe(0);
      expect(sharedData.emitNodeTarget).not.toHaveBeenCalled();
      expect(sharedData.sendRemoveFromChildSearchCriteriaAction).not.toHaveBeenCalled();
    });

    it.each(RULE_CATEGORY_SENDERS)('should forward a removed %s criteria to its facet', (category, sender) => {
      const value = { id: 'R1', value: 'R1' };
      const map = toMap(criteria('RULE_ID', category, [value]));

      service.removeCriteria('RULE_ID', value, true, ['RULE_ID'], map, 1);

      expect(sharedData[sender]).toHaveBeenCalledWith({ keyElt: 'RULE_ID', valueElt: value, action: 'REMOVE' });
      expect(sharedData.sendRemoveFromChildSearchCriteriaAction).not.toHaveBeenCalled();
    });

    it('should reset ORIGIN_WAITING_RECALCULATE in every rule facet when WAITING_RECALCULATE is removed', () => {
      const map = toMap(criteria('WAITING_RECALCULATE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'WAITING_RECALCULATE', value: 'true' }]));
      const keys = ['WAITING_RECALCULATE'];
      const originValue = { id: 'ORIGIN_WAITING_RECALCULATE', value: 'ORIGIN_WAITING_RECALCULATE' };

      service.removeCriteria('WAITING_RECALCULATE', { id: 'WAITING_RECALCULATE', value: 'true' }, true, keys, map, 1);

      expect(map.size).toBe(0);
      expect(keys).toEqual([]);
      RULE_CATEGORY_SENDERS.forEach(([, sender]) =>
        expect(sharedData[sender]).toHaveBeenCalledWith({ keyElt: 'WAITING_RECALCULATE', valueElt: originValue, action: 'REMOVE' }),
      );
      expect(sharedData.sendRemoveFromChildSearchCriteriaAction).toHaveBeenCalledWith({
        keyElt: 'WAITING_RECALCULATE',
        valueElt: originValue,
        action: 'REMOVE',
      });
    });

    it('should also remove the WAITING_RECALCULATE criteria when ORIGIN_WAITING_RECALCULATE is removed', () => {
      const map = toMap(criteria('WAITING_RECALCULATE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'WAITING_RECALCULATE', value: 'true' }]));
      const keys = ['WAITING_RECALCULATE'];

      service.removeCriteria('ORIGIN_KEY', { id: 'ORIGIN_WAITING_RECALCULATE', value: 'ORIGIN_WAITING_RECALCULATE' }, false, keys, map, 1);

      expect(map.size).toBe(0);
      expect(keys).toEqual([]);
    });
  });

  describe('prepareUAIdList', () => {
    const searchCriteria: SearchCriteriaEltDto[] = [
      { criteria: 'TITLE', values: [{ id: 'foo', value: 'foo' }], operator: 'EQ', category: 'FIELDS', dataType: 'STRING' },
    ];
    const included = [{ id: 'ua1', value: 'ua1' }];
    const excluded = [{ id: 'ua2', value: 'ua2' }];

    it('should return an empty list without search criteria', () => {
      expect(service.prepareUAIdList([], included, excluded, true, false)).toEqual([]);
      expect(service.prepareUAIdList(null, included, excluded, true, false)).toEqual([]);
    });

    it('should keep the search criteria and add GUID inclusion/exclusion criteria when all items are selected', () => {
      expect(service.prepareUAIdList(searchCriteria, [], excluded, true, false)).toEqual([
        searchCriteria[0],
        { criteria: 'GUID', values: excluded, operator: CriteriaOperator.NOT_EQ, category: 'FIELDS', dataType: CriteriaDataType.STRING },
      ]);
    });

    it('should keep the search criteria when selection is indeterminate', () => {
      expect(service.prepareUAIdList(searchCriteria, null, null, false, true)).toEqual(searchCriteria);
    });

    it('should only use the selected GUIDs when items are hand-picked', () => {
      expect(service.prepareUAIdList(searchCriteria, included, [], false, false)).toEqual([
        { criteria: 'GUID', values: included, operator: CriteriaOperator.EQ, category: 'FIELDS', dataType: CriteriaDataType.STRING },
      ]);
    });
  });

  describe('updateCriteriaStatus', () => {
    it('should only update values having the old status', () => {
      const map = toMap(criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'a', value: 'a' }]));
      map.get('TITLE').values.push({ value: { id: 'b', value: 'b' }, status: SearchCriteriaStatusEnum.INCLUDED } as never);

      service.updateCriteriaStatus(map, SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);

      expect(map.get('TITLE').values.map((v) => v.status)).toEqual([
        SearchCriteriaStatusEnum.IN_PROGRESS,
        SearchCriteriaStatusEnum.INCLUDED,
      ]);
    });
  });

  describe('findDefaultFacetTabIndex', () => {
    const rule = (category: SearchCriteriaTypeEnum) => criteria(`KEY_${category}`, category, [{ id: 'x', value: 'x' }]);

    it('should default to the first tab without rule criteria', () => {
      expect(service.findDefaultFacetTabIndex(new Map())).toBe(0);
      expect(service.findDefaultFacetTabIndex(null)).toBe(0);
      expect(service.findDefaultFacetTabIndex(toMap(rule(SearchCriteriaTypeEnum.FIELDS)))).toBe(0);
    });

    it.each([
      [[SearchCriteriaTypeEnum.STORAGE_RULE, SearchCriteriaTypeEnum.ACCESS_RULE], 0],
      [[SearchCriteriaTypeEnum.ACCESS_RULE, SearchCriteriaTypeEnum.APPRAISAL_RULE], 1],
      [[SearchCriteriaTypeEnum.REUSE_RULE, SearchCriteriaTypeEnum.ACCESS_RULE], 2],
      [[SearchCriteriaTypeEnum.DISSEMINATION_RULE, SearchCriteriaTypeEnum.CLASSIFICATION_RULE], 3],
      [[SearchCriteriaTypeEnum.REUSE_RULE], 4],
      [[SearchCriteriaTypeEnum.CLASSIFICATION_RULE, SearchCriteriaTypeEnum.FIELDS], 5],
    ])('should select the tab of the first rule category in tab order for %s', (categories, expected) => {
      expect(service.findDefaultFacetTabIndex(toMap(...categories.map(rule)))).toBe(expected);
    });
  });

  describe('checkIfRulesFacetsCanBeComputed', () => {
    it('should be false without criteria or with non-rule criteria only', () => {
      expect(service.checkIfRulesFacetsCanBeComputed(null)).toBe(false);
      expect(service.checkIfRulesFacetsCanBeComputed(toMap(criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, [])))).toBe(false);
      expect(service.checkIfRulesFacetsCanBeComputed(toMap(criteria('CLASS', SearchCriteriaTypeEnum.CLASSIFICATION_RULE, [])))).toBe(false);
    });

    it.each([
      criteria('A', SearchCriteriaTypeEnum.APPRAISAL_RULE, []),
      criteria('B', SearchCriteriaTypeEnum.ACCESS_RULE, []),
      criteria('C', SearchCriteriaTypeEnum.STORAGE_RULE, []),
      criteria('D', SearchCriteriaTypeEnum.REUSE_RULE, []),
      criteria('E', SearchCriteriaTypeEnum.DISSEMINATION_RULE, []),
      criteria('WAITING_RECALCULATE', SearchCriteriaTypeEnum.FIELDS, []),
      criteria('ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE', SearchCriteriaTypeEnum.FIELDS, []),
    ])('should be true with a management rule related criteria ($key)', (crit) => {
      expect(service.checkIfRulesFacetsCanBeComputed(toMap(criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, []), crit))).toBe(true);
    });
  });

  describe('query builders', () => {
    it('should build field criteria for the query and mark them as in progress', () => {
      const map = toMap(
        criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, [
          { id: 'a', value: 'a' },
          { id: 'b', value: 'b' },
        ]),
        criteria('NODE', SearchCriteriaTypeEnum.NODES, [{ id: 'n', value: 'n' }]),
      );
      const list: SearchCriteriaEltDto[] = [];

      service.buildFieldsCriteriaListForQUery(map, list);

      expect(list).toEqual([
        {
          criteria: 'TITLE',
          values: [
            { id: 'a', value: 'a' },
            { id: 'b', value: 'b' },
          ],
          operator: CriteriaOperator.EQ,
          category: 'FIELDS',
          dataType: CriteriaDataType.STRING,
        },
      ]);
      expect(map.get('TITLE').values.every((v) => v.status === SearchCriteriaStatusEnum.IN_PROGRESS)).toBe(true);
    });

    it('should build management rule criteria with the category suffix stripped from the key', () => {
      const map = toMap(
        criteria('RULE_IDENTIFIER_APPRAISAL_RULE', SearchCriteriaTypeEnum.APPRAISAL_RULE, [{ id: 'APP-1', value: 'APP-1' }]),
        criteria('RULE_IDENTIFIER_ACCESS_RULE', SearchCriteriaTypeEnum.ACCESS_RULE, [{ id: 'ACC-1', value: 'ACC-1' }]),
      );
      const list: SearchCriteriaEltDto[] = [];

      service.buildManagementRulesCriteriaListForQuery('APPRAISAL_RULE', map, list);

      expect(list).toEqual([
        {
          criteria: 'RULE_IDENTIFIER',
          values: [{ id: 'APP-1', value: 'APP-1' }],
          operator: CriteriaOperator.EQ,
          category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
          dataType: CriteriaDataType.STRING,
        },
      ]);
      expect(map.get('RULE_IDENTIFIER_APPRAISAL_RULE').values[0].status).toBe(SearchCriteriaStatusEnum.IN_PROGRESS);
    });

    it('should build node criteria, resolving virtual nodes to their real parent', () => {
      const map = toMap(
        criteria('NODE', SearchCriteriaTypeEnum.NODES, [{ id: 'n1', value: 'n1' }]),
        criteria('VIRTUAL', SearchCriteriaTypeEnum.NODES, [{ id: 'v1', value: 'v1', virtualNodeRealParentId: 'real-1' }]),
        criteria('TITLE', SearchCriteriaTypeEnum.FIELDS, [{ id: 't', value: 't' }]),
      );
      const list: SearchCriteriaEltDto[] = [];

      service.buildNodesListForQUery(map, list);

      expect(list).toEqual([
        {
          criteria: 'NODE',
          values: [{ id: 'n1', value: 'n1', label: 'n1' }],
          operator: CriteriaOperator.EQ,
          category: 'NODES',
          dataType: CriteriaDataType.STRING,
        },
        {
          criteria: 'NODE',
          values: [{ id: 'real-1', value: 'real-1', virtualNodeRealParentId: 'real-1' }],
          operator: CriteriaOperator.EQ,
          category: 'NODES',
          dataType: CriteriaDataType.STRING,
        },
      ]);
    });
  });

  describe('fillNodeTitle', () => {
    const node = (id: string, title: string, children: FilingHoldingSchemeNode[] = []): FilingHoldingSchemeNode =>
      ({ id, title, children, checked: false, hidden: true, vitamId: id }) as FilingHoldingSchemeNode;

    it('should find the node in the tree, check it and add a NODE criteria labelled with its title', () => {
      const target = node('n3', 'Deep node');
      const tree = [node('n1', 'Root', [node('n2', 'Sibling'), node('n4', 'Parent', [target])])];
      const map = new Map<string, CriteriaSearchCriteria>();
      const keys = ['TITLE'];

      service.fillNodeTitle(tree, 'n3', map, keys, 0);

      expect(target.checked).toBe(true);
      expect(target.hidden).toBe(false);
      expect(tree[0].checked).toBe(false);
      expect(keys).toEqual(['NODE', 'TITLE']);
      expect(map.get('NODE').values).toEqual([expect.objectContaining({ value: { id: 'n3', value: 'n3' }, label: 'Deep node' })]);
      expect(sharedData.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
    });

    it('should leave the criteria untouched when the node is not in the tree', () => {
      const map = new Map<string, CriteriaSearchCriteria>();

      service.fillNodeTitle([node('n1', 'Root', [node('n2', 'Child')])], 'unknown', map, [], 0);

      expect(map.size).toBe(0);
    });
  });
});
