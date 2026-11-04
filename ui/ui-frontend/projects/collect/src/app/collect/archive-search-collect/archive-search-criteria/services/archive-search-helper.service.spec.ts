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
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ArchiveSearchHelperService } from './archive-search-helper.service';

type SharedDataMock = Record<
  | 'emitSearchCriteriaChange'
  | 'sendAppraisalFromMainSearchCriteriaAction'
  | 'sendAccessFromMainSearchCriteriaAction'
  | 'sendStorageFromMainSearchCriteriaAction'
  | 'sendReuseFromMainSearchCriteriaAction'
  | 'sendDisseminationFromMainSearchCriteriaAction'
  | 'addSimpleSearchCriteriaSubject'
  | 'sendRemoveFromChildSearchCriteriaAction'
  | 'emitNodeTarget',
  ReturnType<typeof vi.fn>
>;

const RULE_SENDERS: [SearchCriteriaTypeEnum, keyof SharedDataMock][] = [
  [SearchCriteriaTypeEnum.APPRAISAL_RULE, 'sendAppraisalFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.ACCESS_RULE, 'sendAccessFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.STORAGE_RULE, 'sendStorageFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.REUSE_RULE, 'sendReuseFromMainSearchCriteriaAction'],
  [SearchCriteriaTypeEnum.DISSEMINATION_RULE, 'sendDisseminationFromMainSearchCriteriaAction'],
];

const criteriaOf = (
  key: string,
  category: SearchCriteriaTypeEnum,
  values: CriteriaValue[],
  dataType: string = CriteriaDataType.STRING,
  status = SearchCriteriaStatusEnum.NOT_INCLUDED,
): CriteriaSearchCriteria => ({
  key,
  category,
  operator: CriteriaOperator.EQ,
  keyTranslated: false,
  valueTranslated: false,
  dataType,
  values: values.map((value) => ({ value, label: value.value, valueShown: true, status, keyTranslated: false, valueTranslated: false })),
});

const node = (id: string, children: FilingHoldingSchemeNode[] = [], extra: Partial<FilingHoldingSchemeNode> = {}) =>
  ({ id, title: `Title ${id}`, vitamId: id, children, checked: false, hidden: true, count: 3, ...extra }) as FilingHoldingSchemeNode;

describe('ArchiveSearchHelperService', () => {
  let service: ArchiveSearchHelperService;
  let shared: SharedDataMock;
  let criterias: Map<string, CriteriaSearchCriteria>;
  let keys: string[];

  const add = (
    key: string,
    value: CriteriaValue,
    category: SearchCriteriaTypeEnum,
    emit = false,
    dataType: string = CriteriaDataType.STRING,
    label = 'label',
  ) => service.addCriteria(criterias, keys, 0, key, value, label, true, CriteriaOperator.EQ, category, false, dataType, emit);

  beforeEach(() => {
    shared = {
      emitSearchCriteriaChange: vi.fn(),
      sendAppraisalFromMainSearchCriteriaAction: vi.fn(),
      sendAccessFromMainSearchCriteriaAction: vi.fn(),
      sendStorageFromMainSearchCriteriaAction: vi.fn(),
      sendReuseFromMainSearchCriteriaAction: vi.fn(),
      sendDisseminationFromMainSearchCriteriaAction: vi.fn(),
      addSimpleSearchCriteriaSubject: vi.fn(),
      sendRemoveFromChildSearchCriteriaAction: vi.fn(),
      emitNodeTarget: vi.fn(),
    };
    TestBed.configureTestingModule({ providers: [{ provide: ArchiveSharedDataService, useValue: shared }] });
    service = TestBed.inject(ArchiveSearchHelperService);
    criterias = new Map();
    keys = [];
  });

  describe('addCriteria', () => {
    it('should ignore an empty key', () => {
      add('', { id: 'a', value: 'a' }, SearchCriteriaTypeEnum.FIELDS, true);

      expect(criterias.size).toBe(0);
      expect(shared.emitSearchCriteriaChange).not.toHaveBeenCalled();
    });

    it('should do nothing when there is no criteria map', () => {
      expect(() =>
        service.addCriteria(
          null,
          keys,
          0,
          'TITLE',
          { id: 'a', value: 'a' },
          'l',
          true,
          'EQ',
          SearchCriteriaTypeEnum.FIELDS,
          false,
          'STRING',
          true,
        ),
      ).not.toThrow();
      expect(keys).toEqual([]);
      expect(shared.emitSearchCriteriaChange).not.toHaveBeenCalled();
    });

    it('should create a new criteria with a NOT_INCLUDED value and append its key', () => {
      add('TITLE', { id: 'id1', value: 'foo' }, SearchCriteriaTypeEnum.FIELDS, false, CriteriaDataType.STRING, 'Foo');

      expect(keys).toEqual(['TITLE']);
      expect(criterias.get('TITLE')).toEqual({
        key: 'TITLE',
        values: [
          {
            value: { id: 'id1', value: 'foo' },
            label: 'Foo',
            id: 'id1',
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
      expect(shared.emitSearchCriteriaChange).toHaveBeenCalledWith(criterias);
      expect(shared.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
    });

    it('should put NODES keys first and other keys last', () => {
      keys.push('TITLE');
      add('NODE', { id: 'n1', value: 'n1' }, SearchCriteriaTypeEnum.NODES);
      add('DESCRIPTION', { id: 'd', value: 'd' }, SearchCriteriaTypeEnum.FIELDS);

      expect(keys).toEqual(['NODE', 'TITLE', 'DESCRIPTION']);
    });

    it('should not duplicate a key already present in the keys list', () => {
      keys.push('TITLE');
      add('TITLE', { id: 'a', value: 'a' }, SearchCriteriaTypeEnum.FIELDS);

      expect(keys).toEqual(['TITLE']);
    });

    it('should append a new value to an existing STRING criteria but ignore a duplicate value', () => {
      add('TITLE', { id: 'a', value: 'a' }, SearchCriteriaTypeEnum.FIELDS);
      add('TITLE', { id: 'b', value: 'b' }, SearchCriteriaTypeEnum.FIELDS);
      add('TITLE', { id: 'a2', value: 'a' }, SearchCriteriaTypeEnum.FIELDS);

      expect(criterias.get('TITLE').values.map((v) => v.value.value)).toEqual(['a', 'b']);
    });

    it('should deduplicate DATE criteria by value', () => {
      add('START_DATE', { id: 'd', value: '2020-01-01' }, SearchCriteriaTypeEnum.FIELDS, false, CriteriaDataType.DATE);
      add('START_DATE', { id: 'd', value: '2020-01-01' }, SearchCriteriaTypeEnum.FIELDS, false, CriteriaDataType.DATE);

      expect(criterias.get('START_DATE').values.length).toBe(1);
    });

    it('should deduplicate VIRTUAL criteria by value and real parent id', () => {
      add('VIRTUAL', { id: 'v', value: 'v1', virtualNodeRealParentId: 'p1' }, SearchCriteriaTypeEnum.NODES);
      add('VIRTUAL', { id: 'v', value: 'v1', virtualNodeRealParentId: 'p2' }, SearchCriteriaTypeEnum.NODES);
      add('VIRTUAL', { id: 'v', value: 'v1', virtualNodeRealParentId: 'p1' }, SearchCriteriaTypeEnum.NODES);

      expect(criterias.get('VIRTUAL').values.map((v) => v.value.virtualNodeRealParentId)).toEqual(['p1', 'p2']);
    });

    it('should deduplicate INTERVAL criteria by bounds', () => {
      const interval = (beginInterval: string, endInterval: string) => ({ id: 'i', beginInterval, endInterval });
      add('SIZE', interval('1', '10'), SearchCriteriaTypeEnum.FIELDS, false, CriteriaDataType.INTERVAL);
      add('SIZE', interval('1', '10'), SearchCriteriaTypeEnum.FIELDS, false, CriteriaDataType.INTERVAL);
      add('SIZE', interval('1', '20'), SearchCriteriaTypeEnum.FIELDS, false, CriteriaDataType.INTERVAL);

      expect(criterias.get('SIZE').values.map((v) => v.value.endInterval)).toEqual(['10', '20']);
    });

    it('should leave an existing criteria untouched when no value is given', () => {
      add('TITLE', { id: 'a', value: 'a' }, SearchCriteriaTypeEnum.FIELDS);
      add('TITLE', null, SearchCriteriaTypeEnum.FIELDS);

      expect(criterias.get('TITLE').values.length).toBe(1);
    });

    it.each(RULE_SENDERS)('should notify the %s facet when emit is true', (category, sender) => {
      const value = { id: 'r', value: 'r' };
      add('RULE_ID', value, category, true);

      expect(shared[sender]).toHaveBeenCalledWith({ keyElt: 'RULE_ID', valueElt: value, action: 'ADD' });
      expect(shared.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
    });

    it.each(RULE_SENDERS)('should not notify the %s facet when emit is false', (category, sender) => {
      add('RULE_ID', { id: 'r', value: 'r' }, category, false);

      expect(shared[sender]).not.toHaveBeenCalled();
    });

    it.each([SearchCriteriaTypeEnum.FIELDS, SearchCriteriaTypeEnum.NODES])(
      'should forward %s criteria to the simple search when emit is true',
      (category) => {
        const value = { id: 'x', value: 'x' };
        add('KEY', value, category, true, CriteriaDataType.STRING, 'X');

        expect(shared.addSimpleSearchCriteriaSubject).toHaveBeenCalledWith({
          keyElt: 'KEY',
          valueElt: value,
          labelElt: 'X',
          keyTranslated: true,
          operator: CriteriaOperator.EQ,
          category,
          valueTranslated: false,
          dataType: CriteriaDataType.STRING,
        });
      },
    );

    describe('ORIGIN_WAITING_RECALCULATE value', () => {
      const origin = { id: 'ORIGIN_WAITING_RECALCULATE', value: 'ORIGIN_WAITING_RECALCULATE' };

      it('should be replaced by a WAITING_RECALCULATE fields criteria', () => {
        add('APPRAISAL_RULE_ORIGIN', origin, SearchCriteriaTypeEnum.APPRAISAL_RULE, false);

        expect(criterias.has('APPRAISAL_RULE_ORIGIN')).toBe(false);
        const criteria = criterias.get('WAITING_RECALCULATE');
        expect(criteria.category).toBe(SearchCriteriaTypeEnum.FIELDS);
        expect(criteria.keyTranslated).toBe(true);
        expect(criteria.valueTranslated).toBe(true);
        expect(criteria.values[0].value).toEqual({ id: 'WAITING_RECALCULATE', value: 'true' });
      });

      it.each(RULE_SENDERS)('should notify the %s facet with the original key/value', (category, sender) => {
        add('RULE_ORIGIN', origin, category, false);

        expect(shared[sender]).toHaveBeenCalledWith({ keyElt: 'RULE_ORIGIN', valueElt: origin, action: 'ADD' });
        RULE_SENDERS.filter(([, other]) => other !== sender).forEach(([, other]) => expect(shared[other]).not.toHaveBeenCalled());
      });
    });
  });

  describe('removeCriteria', () => {
    it('should do nothing with an empty criteria map', () => {
      service.removeCriteria('TITLE', { id: 'a', value: 'a' }, true, keys, criterias, 0);

      expect(shared.emitSearchCriteriaChange).not.toHaveBeenCalled();
    });

    it('should remove a single value and keep the criteria when other values remain', () => {
      criterias.set(
        'TITLE',
        criteriaOf('TITLE', SearchCriteriaTypeEnum.FIELDS, [
          { id: 'a', value: 'a' },
          { id: 'b', value: 'b' },
        ]),
      );
      keys.push('TITLE');

      service.removeCriteria('TITLE', { id: 'a', value: 'a' }, false, keys, criterias, 0);

      expect(criterias.get('TITLE').values.map((v) => v.value.value)).toEqual(['b']);
      expect(keys).toEqual(['TITLE']);
      expect(shared.emitSearchCriteriaChange).toHaveBeenCalledWith(criterias);
      expect(shared.sendRemoveFromChildSearchCriteriaAction).not.toHaveBeenCalled();
    });

    it('should delete the criteria and its key when the last value is removed, and notify child', () => {
      const value = { id: 'a', value: 'a' };
      criterias.set('TITLE', criteriaOf('TITLE', SearchCriteriaTypeEnum.FIELDS, [value]));
      keys.push('OTHER', 'TITLE');

      service.removeCriteria('TITLE', value, true, keys, criterias, 0);

      expect(criterias.has('TITLE')).toBe(false);
      expect(keys).toEqual(['OTHER']);
      expect(shared.sendRemoveFromChildSearchCriteriaAction).toHaveBeenCalledWith({ keyElt: 'TITLE', valueElt: value, action: 'REMOVE' });
    });

    it('should only remove the VIRTUAL value matching the real parent id and emit the composite node target', () => {
      criterias.set(
        'VIRTUAL',
        criteriaOf('VIRTUAL', SearchCriteriaTypeEnum.NODES, [
          { id: 'v', value: 'v1', virtualNodeRealParentId: 'p1' },
          { id: 'v', value: 'v1', virtualNodeRealParentId: 'p2' },
        ]),
      );

      service.removeCriteria('VIRTUAL', { id: 'v', value: 'v1', virtualNodeRealParentId: 'p1' }, true, keys, criterias, 0);

      expect(criterias.get('VIRTUAL').values.map((v) => v.value.virtualNodeRealParentId)).toEqual(['p2']);
      expect(shared.emitNodeTarget).toHaveBeenCalledWith('v1-p1');
    });

    it.each(['NODE', ORPHANS_NODE_ID])('should emit the node target when removing a %s criteria', (key) => {
      criterias.set(key, criteriaOf(key, SearchCriteriaTypeEnum.NODES, [{ id: 'n1', value: 'n1' }]));

      service.removeCriteria(key, { id: 'n1', value: 'n1' }, true, keys, criterias, 0);

      expect(shared.emitNodeTarget).toHaveBeenCalledWith('n1');
    });

    it('should not emit anything to other components when emit is false', () => {
      criterias.set('NODE', criteriaOf('NODE', SearchCriteriaTypeEnum.NODES, [{ id: 'n1', value: 'n1' }]));

      service.removeCriteria('NODE', { id: 'n1', value: 'n1' }, false, keys, criterias, 0);

      expect(criterias.size).toBe(0);
      expect(shared.emitNodeTarget).not.toHaveBeenCalled();
      expect(shared.sendRemoveFromChildSearchCriteriaAction).not.toHaveBeenCalled();
    });

    it.each(RULE_SENDERS)('should notify the %s facet on removal', (category, sender) => {
      const value = { id: 'r', value: 'r' };
      criterias.set('RULE', criteriaOf('RULE', category, [value]));

      service.removeCriteria('RULE', value, true, keys, criterias, 0);

      expect(shared[sender]).toHaveBeenCalledWith({ keyElt: 'RULE', valueElt: value, action: 'REMOVE' });
      expect(shared.sendRemoveFromChildSearchCriteriaAction).not.toHaveBeenCalled();
    });

    it('should translate a WAITING_RECALCULATE removal to ORIGIN_WAITING_RECALCULATE for every rule facet and child', () => {
      const value = { id: 'WAITING_RECALCULATE', value: 'true' };
      const origin = { id: 'ORIGIN_WAITING_RECALCULATE', value: 'ORIGIN_WAITING_RECALCULATE' };
      criterias.set('WAITING_RECALCULATE', criteriaOf('WAITING_RECALCULATE', SearchCriteriaTypeEnum.FIELDS, [value]));
      keys.push('WAITING_RECALCULATE');

      service.removeCriteria('WAITING_RECALCULATE', value, true, keys, criterias, 0);

      expect(criterias.size).toBe(0);
      expect(keys).toEqual([]);
      RULE_SENDERS.forEach(([, sender]) =>
        expect(shared[sender]).toHaveBeenCalledWith({ keyElt: 'WAITING_RECALCULATE', valueElt: origin, action: 'REMOVE' }),
      );
      expect(shared.sendRemoveFromChildSearchCriteriaAction).toHaveBeenCalledWith({
        keyElt: 'WAITING_RECALCULATE',
        valueElt: origin,
        action: 'REMOVE',
      });
    });

    it('should remove the WAITING_RECALCULATE criteria when an ORIGIN_WAITING_RECALCULATE value is removed', () => {
      criterias.set(
        'WAITING_RECALCULATE',
        criteriaOf('WAITING_RECALCULATE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'WAITING_RECALCULATE', value: 'true' }]),
      );
      criterias.set('TITLE', criteriaOf('TITLE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'a', value: 'a' }]));

      service.removeCriteria(
        'APPRAISAL_RULE_ORIGIN',
        { id: 'ORIGIN_WAITING_RECALCULATE', value: 'ORIGIN_WAITING_RECALCULATE' },
        false,
        keys,
        criterias,
        0,
      );

      expect([...criterias.keys()]).toEqual(['TITLE']);
    });
  });

  describe('prepareUAIdList', () => {
    const criteriaList: SearchCriteriaEltDto[] = [
      { criteria: 'TITLE', values: [{ id: 'a', value: 'a' }], operator: 'EQ', category: 'FIELDS', dataType: 'STRING' },
    ];
    const include = [{ id: 'ua1', value: 'ua1' }];
    const exclude = [{ id: 'ua2', value: 'ua2' }];

    it('should return an empty list when there is no search criteria', () => {
      expect(service.prepareUAIdList([], include, exclude, true, false)).toEqual([]);
      expect(service.prepareUAIdList(null, include, exclude, true, false)).toEqual([]);
    });

    it('should only include explicit GUIDs when nothing is globally checked', () => {
      expect(service.prepareUAIdList(criteriaList, include, [], false, false)).toEqual([
        { criteria: 'GUID', values: include, operator: CriteriaOperator.EQ, category: 'FIELDS', dataType: CriteriaDataType.STRING },
      ]);
    });

    it.each([
      [true, false],
      [false, true],
    ])('should keep search criteria and exclusions when allChecked=%s / indeterminate=%s', (all, indeterminate) => {
      expect(service.prepareUAIdList(criteriaList, [], exclude, all, indeterminate)).toEqual([
        criteriaList[0],
        { criteria: 'GUID', values: exclude, operator: CriteriaOperator.NOT_EQ, category: 'FIELDS', dataType: CriteriaDataType.STRING },
      ]);
    });
  });

  describe('updateCriteriaStatus', () => {
    it('should only change values matching the old status', () => {
      const c = criteriaOf('TITLE', SearchCriteriaTypeEnum.FIELDS, [
        { id: 'a', value: 'a' },
        { id: 'b', value: 'b' },
      ]);
      c.values[1].status = SearchCriteriaStatusEnum.INCLUDED;
      criterias.set('TITLE', c);

      service.updateCriteriaStatus(criterias, SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);

      expect(c.values.map((v) => v.status)).toEqual([SearchCriteriaStatusEnum.IN_PROGRESS, SearchCriteriaStatusEnum.INCLUDED]);
    });
  });

  describe('findDefaultFacetTabIndex', () => {
    const withCategories = (...categories: SearchCriteriaTypeEnum[]) =>
      new Map(categories.map((category, i) => [`K${i}`, criteriaOf(`K${i}`, category, [])]));

    it('should default to 0 without criteria', () => {
      expect(service.findDefaultFacetTabIndex(new Map())).toBe(0);
      expect(service.findDefaultFacetTabIndex(null)).toBe(0);
      expect(service.findDefaultFacetTabIndex(withCategories(SearchCriteriaTypeEnum.FIELDS))).toBe(0);
    });

    it.each([
      [SearchCriteriaTypeEnum.STORAGE_RULE, 0],
      [SearchCriteriaTypeEnum.APPRAISAL_RULE, 1],
      [SearchCriteriaTypeEnum.ACCESS_RULE, 3],
      [SearchCriteriaTypeEnum.DISSEMINATION_RULE, 4],
      [SearchCriteriaTypeEnum.REUSE_RULE, 5],
      [SearchCriteriaTypeEnum.CLASSIFICATION_RULE, 6],
    ])('should map a %s criteria to tab %s', (category, index) => {
      expect(service.findDefaultFacetTabIndex(withCategories(SearchCriteriaTypeEnum.FIELDS, category))).toBe(index);
    });

    it('should pick the lowest tab among several rule criteria regardless of order', () => {
      expect(
        service.findDefaultFacetTabIndex(
          withCategories(SearchCriteriaTypeEnum.CLASSIFICATION_RULE, SearchCriteriaTypeEnum.ACCESS_RULE, SearchCriteriaTypeEnum.REUSE_RULE),
        ),
      ).toBe(3);
    });
  });

  describe('checkIfRulesFacetsCanBeComputed', () => {
    it('should be false without management rule criteria', () => {
      expect(service.checkIfRulesFacetsCanBeComputed(null)).toBe(false);
      criterias.set('TITLE', criteriaOf('TITLE', SearchCriteriaTypeEnum.FIELDS, []));
      criterias.set('C', criteriaOf('C', SearchCriteriaTypeEnum.CLASSIFICATION_RULE, []));
      expect(service.checkIfRulesFacetsCanBeComputed(criterias)).toBe(false);
    });

    it.each(RULE_SENDERS.map(([category]) => category))('should be true with a %s criteria', (category) => {
      criterias.set('R', criteriaOf('R', category, []));
      expect(service.checkIfRulesFacetsCanBeComputed(criterias)).toBe(true);
    });

    it.each(['WAITING_RECALCULATE', 'ORIGIN_WAITING_RECALCULATE', 'ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE'])(
      'should be true with a %s fields criteria',
      (key) => {
        criterias.set(key, criteriaOf(key, SearchCriteriaTypeEnum.FIELDS, []));
        expect(service.checkIfRulesFacetsCanBeComputed(criterias)).toBe(true);
      },
    );
  });

  describe('query builders', () => {
    it('buildFieldsCriteriaListForQUery should push FIELDS criteria values and mark them in progress', () => {
      criterias.set('TITLE', criteriaOf('TITLE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'a', value: 'a' }]));
      criterias.set('NODE', criteriaOf('NODE', SearchCriteriaTypeEnum.NODES, [{ id: 'n', value: 'n' }]));
      const list: SearchCriteriaEltDto[] = [];

      service.buildFieldsCriteriaListForQUery(criterias, list);

      expect(list).toEqual([
        { criteria: 'TITLE', values: [{ id: 'a', value: 'a' }], operator: CriteriaOperator.EQ, category: 'FIELDS', dataType: 'STRING' },
      ]);
      expect(criterias.get('TITLE').values[0].status).toBe(SearchCriteriaStatusEnum.IN_PROGRESS);
    });

    it('buildManagementRulesCriteriaListForQuery should strip the rule type suffix from the key', () => {
      criterias.set(
        'RULE_IDENTIFIER_APPRAISAL_RULE',
        criteriaOf('RULE_IDENTIFIER_APPRAISAL_RULE', SearchCriteriaTypeEnum.APPRAISAL_RULE, [{ id: 'R1', value: 'R1' }]),
      );
      criterias.set('ACCESS', criteriaOf('ACCESS', SearchCriteriaTypeEnum.ACCESS_RULE, [{ id: 'A', value: 'A' }]));
      const list: SearchCriteriaEltDto[] = [];

      service.buildManagementRulesCriteriaListForQuery('APPRAISAL_RULE', criterias, list);

      expect(list).toEqual([
        {
          criteria: 'RULE_IDENTIFIER',
          values: [{ id: 'R1', value: 'R1' }],
          operator: CriteriaOperator.EQ,
          category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
          dataType: 'STRING',
        },
      ]);
      expect(criterias.get('RULE_IDENTIFIER_APPRAISAL_RULE').values[0].status).toBe(SearchCriteriaStatusEnum.IN_PROGRESS);
    });

    it('buildNodesListForQUery should map NODES criteria and VIRTUAL nodes to their real parent', () => {
      criterias.set('NODE', criteriaOf('NODE', SearchCriteriaTypeEnum.NODES, [{ id: 'n1', value: 'n1' }]));
      criterias.set(
        'VIRTUAL',
        criteriaOf('VIRTUAL', SearchCriteriaTypeEnum.NODES, [{ id: 'v', value: 'v1', virtualNodeRealParentId: 'p1' }]),
      );
      criterias.set('TITLE', criteriaOf('TITLE', SearchCriteriaTypeEnum.FIELDS, [{ id: 'a', value: 'a' }]));
      const list: SearchCriteriaEltDto[] = [];

      service.buildNodesListForQUery(criterias, list);

      expect(list).toEqual([
        { criteria: 'NODE', values: [{ id: 'n1', value: 'n1' }], operator: CriteriaOperator.EQ, category: 'NODES', dataType: 'STRING' },
        {
          criteria: 'NODE',
          values: [{ id: 'p1', value: 'p1', virtualNodeRealParentId: 'p1' }],
          operator: CriteriaOperator.EQ,
          category: 'NODES',
          dataType: 'STRING',
        },
      ]);
    });
  });

  describe('nodes helpers', () => {
    it('recursiveCheck should reset display state on the whole tree', () => {
      const tree = [node('a', [node('b', [node('c')])])];

      service.recursiveCheck(tree, true);

      const all = [tree[0], tree[0].children[0], tree[0].children[0].children[0]];
      all.forEach((n) => {
        expect(n.checked).toBe(true);
        expect(n.hidden).toBe(false);
        expect(n.count).toBeNull();
      });
    });

    it('fillNodeTitle should check the nested node and add a NODE criteria labelled with its title', () => {
      const target = node('c');
      const tree = [node('a', [node('b', [target])]), node('d')];

      service.fillNodeTitle(tree, 'c', criterias, keys, 0);

      expect(target.checked).toBe(true);
      expect(target.hidden).toBe(false);
      expect(tree[1].checked).toBe(false);
      expect(keys).toEqual(['NODE']);
      const criteria = criterias.get('NODE');
      expect(criteria.category).toBe(SearchCriteriaTypeEnum.NODES);
      expect(criteria.values[0]).toEqual(expect.objectContaining({ value: { id: 'c', value: 'c' }, label: 'Title c' }));
      expect(shared.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
    });

    it('fillNodeTitle should not add anything when the node is not found', () => {
      service.fillNodeTitle([node('a', [node('b')])], 'zzz', criterias, keys, 0);

      expect(criterias.size).toBe(0);
    });
  });
});
