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
import { of } from 'rxjs';
import { SchemaService } from '../../schema/schema.service';
import { Collection } from '../schema/collection.enum';
import { Schema } from '../schema/schema.interface';
import { CriteriaDataType, CriteriaOperator } from './criteria.enums';
import {
  ARCHIVE_UNIT_WITH_ERRORS,
  ARCHIVE_UNIT_WITH_OBJECTS,
  FINAL_ACTION_HAS_FINAL_ACTION,
  FINAL_ACTION_TYPE_KEEP,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_HAS_NO_ONE,
} from './search-criteria-configs';
import { SearchCriteriaAddAction, SearchCriteriaTypeEnum } from './search-criteria.interface';
import { SearchCriteriaService } from './search-criteria.service';

describe('SearchCriteriaService', () => {
  let service: SearchCriteriaService;
  let schemaServiceMock: { getSchema: ReturnType<typeof vi.fn> };
  const schema = [{ Path: 'Title' }] as unknown as Schema;

  beforeEach(() => {
    schemaServiceMock = { getSchema: vi.fn().mockReturnValue(of(schema)) };
    TestBed.configureTestingModule({
      providers: [{ provide: SchemaService, useValue: schemaServiceMock }],
    });
    service = TestBed.inject(SearchCriteriaService);
  });

  const single = async (object: unknown): Promise<SearchCriteriaAddAction> => {
    const criteria = await service.toSearchCriteria(object);
    expect(criteria).toHaveLength(1);
    return criteria[0];
  };

  describe('schema', () => {
    it('should expose the archive unit schema only once ready', async () => {
      expect(schemaServiceMock.getSchema).toHaveBeenCalledWith(Collection.ARCHIVE_UNIT);
      expect(service.getSchema()).toBeUndefined();

      await service.ready();

      expect(service.getSchema()).toBe(schema);
    });
  });

  describe('isValueTranslated', () => {
    it('should be true only for keys whose values are translation keys', () => {
      expect(service.isValueTranslated('ALL_ARCHIVE_UNIT_TYPES')).toBe(true);
      expect(service.isValueTranslated('RULE_ORIGIN_APPRAISAL_RULE')).toBe(true);
      expect(service.isValueTranslated('TITLE')).toBe(false);
      expect(service.isValueTranslated(undefined)).toBe(false);
    });
  });

  describe('toSearchCriteria', () => {
    it('should build a simple string field criterion', async () => {
      expect(await single({ title: 'My title' })).toEqual({
        keyElt: 'TITLE',
        valueElt: {
          id: 'title',
          value: 'My title',
          beginInterval: '',
          endInterval: '',
          virtualNodeRealParentTitle: null,
          virtualNodeRealParentId: null,
        },
        labelElt: 'My title',
        keyTranslated: true,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        dataType: CriteriaDataType.STRING,
        valueTranslated: false,
      });
    });

    it('should keep the raw key as keyElt for unknown fields', async () => {
      const criterion = await single({ CustomField: 'value' });

      expect(criterion.keyElt).toEqual('CustomField');
      expect(criterion.valueElt.id).toEqual('CustomField');
      expect(criterion.category).toEqual(SearchCriteriaTypeEnum.FIELDS);
    });

    it('should trim the value and the label', async () => {
      const criterion = await single({ description: '  some text  ' });

      expect(criterion.keyElt).toEqual('DESCRIPTION');
      expect(criterion.valueElt.value).toEqual('some text');
      expect(criterion.labelElt).toEqual('some text');
    });

    it('should ignore empty, null and undefined values', async () => {
      expect(await service.toSearchCriteria({ title: '', description: null, guid: undefined, agencies: [] as string[] })).toEqual([]);
    });

    it('should return a copy of the initial criteria list when the object is empty', async () => {
      const initial = [{ keyElt: 'EXISTING' } as SearchCriteriaAddAction];

      const result = await service.toSearchCriteria({}, initial);

      expect(result).toEqual(initial);
      expect(result).not.toBe(initial);
    });

    it('should append new criteria after the given criteria list', async () => {
      const existing = { keyElt: 'EXISTING' } as SearchCriteriaAddAction;

      const result = await service.toSearchCriteria({ title: 'x' }, [existing]);

      expect(result.map((c) => c.keyElt)).toEqual(['EXISTING', 'TITLE']);
    });

    it('should split guid values on commas and trim each fragment', async () => {
      const result = await service.toSearchCriteria({ guid: ' aaa, bbb ,ccc ' });

      expect(result.map((c) => [c.keyElt, c.valueElt.value])).toEqual([
        ['GUID', 'aaa'],
        ['GUID', 'bbb'],
        ['GUID', 'ccc'],
      ]);
    });

    it('should split guidopi values and use the IN operator', async () => {
      const result = await service.toSearchCriteria({ guidopi: 'op1,op2' });

      expect(result.map((c) => [c.keyElt, c.valueElt.value, c.operator])).toEqual([
        ['GUID_OPI', 'op1', CriteriaOperator.IN],
        ['GUID_OPI', 'op2', CriteriaOperator.IN],
      ]);
    });

    it('should not split values of non splittable keys', async () => {
      expect((await single({ title: 'a, b' })).valueElt.value).toEqual('a, b');
    });

    it('should split values of the keys given as splittable values', async () => {
      const result = await service.toSearchCriteria({ title: 'a,b', guid: 'c,d' }, [], ['title']);

      expect(result.map((c) => c.valueElt.value)).toEqual(['a', 'b', 'c,d']);
    });

    it('should create one criterion per array item', async () => {
      const result = await service.toSearchCriteria({ agencies: ['AG1', 'AG2'] });

      expect(result.map((c) => [c.keyElt, c.valueElt.value, c.labelElt])).toEqual([
        ['SP_CODE', 'AG1', 'AG1'],
        ['SP_CODE', 'AG2', 'AG2'],
      ]);
    });

    it('should convert dates to ISO strings and apply the date config of the key', async () => {
      const criterion = await single({ beginDt: new Date('2020-01-02T03:04:05.000Z') });

      expect(criterion.keyElt).toEqual('START_DATE');
      expect(criterion.valueElt.value).toEqual('2020-01-02T03:04:05.000Z');
      expect(criterion.operator).toEqual(CriteriaOperator.GTE);
      expect(criterion.dataType).toEqual(CriteriaDataType.DATE);
    });

    it('should use the LTE operator for the end date field and its alias', async () => {
      const criteria = await service.toSearchCriteria({ endDt: '2020-01-01', EndDate: '2021-01-01' });

      expect(criteria.map((c) => [c.keyElt, c.operator, c.dataType])).toEqual([
        ['END_DATE', CriteriaOperator.LTE, CriteriaDataType.DATE],
        ['END_DATE', CriteriaOperator.LTE, CriteriaDataType.DATE],
      ]);
    });

    it('should flatten nested objects', async () => {
      const result = await service.toSearchCriteria({ group: { title: 't', sub: { description: 'd' } } });

      expect(result.map((c) => [c.keyElt, c.valueElt.value])).toEqual([
        ['TITLE', 't'],
        ['DESCRIPTION', 'd'],
      ]);
    });

    it('should suffix the key with the selected search type for search-with-type-selector values', async () => {
      const criterion = await single({ Title: { value: 'Mon titre', type: { label: 'Français', value: 'fr' } } });

      expect(criterion.keyElt).toEqual('Title.fr');
      expect(criterion.valueElt.id).toEqual('title.fr');
      expect(criterion.valueElt.value).toEqual('Mon titre');
      expect(criterion.keyTranslated).toBe(true);
    });

    it('should use the lower cased key when the selected search type has an empty value', async () => {
      const criterion = await single({ Title: { value: 'Mon titre', type: { label: 'Tous', value: '' } } });

      expect(criterion.keyElt).toEqual('TITLE');
      expect(criterion.valueElt.id).toEqual('title');
    });

    it.each([
      ['APPRAISAL_RULE', SearchCriteriaTypeEnum.APPRAISAL_RULE],
      ['idDua', SearchCriteriaTypeEnum.APPRAISAL_RULE],
      ['elimTechIdDua', SearchCriteriaTypeEnum.APPRAISAL_RULE],
      ['appraisalRuleIdentifier', SearchCriteriaTypeEnum.APPRAISAL_RULE],
      ['ACCESS_RULE', SearchCriteriaTypeEnum.ACCESS_RULE],
      ['titleAccess', SearchCriteriaTypeEnum.ACCESS_RULE],
      ['accessRuleIdentifier', SearchCriteriaTypeEnum.ACCESS_RULE],
      ['CLASSIFICATION_RULE', SearchCriteriaTypeEnum.CLASSIFICATION_RULE],
      ['DISSEMINATION_RULE', SearchCriteriaTypeEnum.DISSEMINATION_RULE],
      ['idDissemination', SearchCriteriaTypeEnum.DISSEMINATION_RULE],
      ['disseminationRuleIdentifier', SearchCriteriaTypeEnum.DISSEMINATION_RULE],
      ['REUSE_RULE', SearchCriteriaTypeEnum.REUSE_RULE],
      ['titleReuse', SearchCriteriaTypeEnum.REUSE_RULE],
      ['reuseRuleIdentifier', SearchCriteriaTypeEnum.REUSE_RULE],
      ['STORAGE_RULE', SearchCriteriaTypeEnum.STORAGE_RULE],
      ['idDuc', SearchCriteriaTypeEnum.STORAGE_RULE],
      ['storageRuleIdentifier', SearchCriteriaTypeEnum.STORAGE_RULE],
      ['HOLD_RULE', SearchCriteriaTypeEnum.HOLD_RULE],
      ['NODES', SearchCriteriaTypeEnum.NODES],
      ['title', SearchCriteriaTypeEnum.FIELDS],
    ])('should put criteria on key %s in category %s', async (key, category) => {
      expect((await single({ [key]: 'value' })).category).toEqual(category);
    });

    it.each([
      ['idDua', 'RULE_IDENTIFIER_APPRAISAL_RULE'],
      ['titleDuc', 'RULE_TITLE_STORAGE_RULE'],
      ['elimTechIdDua', 'ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE'],
      ['storageRuleIdentifier', 'RULE_IDENTIFIER_STORAGE_RULE'],
      ['NODES', 'NODE'],
      ['position', 'ORPHANS_NODE'],
    ])('should map key %s to keyElt %s', async (key, keyElt) => {
      expect((await single({ [key]: 'value' })).keyElt).toEqual(keyElt);
    });

    it('should build an end date rule criterion as an LTE interval', async () => {
      const criterion = await single({ endDateAccess: '2024-12-31' });

      expect(criterion).toMatchObject({
        keyElt: 'RULE_END_DATE_ACCESS_RULE',
        operator: CriteriaOperator.LTE,
        category: SearchCriteriaTypeEnum.ACCESS_RULE,
        dataType: CriteriaDataType.INTERVAL,
        valueElt: { id: 'endDateAccess', value: '2024-12-31', beginInterval: '', endInterval: '2024-12-31' },
      });
    });

    it('should build an interval date rule criterion from a "|" separated value', async () => {
      const criterion = await single({ intervalDateReuse: '2020-01-01|2021-06-30' });

      expect(criterion).toMatchObject({
        keyElt: 'RULE_END_DATE_REUSE_RULE',
        operator: CriteriaOperator.BETWEEN,
        category: SearchCriteriaTypeEnum.REUSE_RULE,
        dataType: CriteriaDataType.INTERVAL,
        valueElt: { beginInterval: '2020-01-01', endInterval: '2021-06-30' },
      });
    });

    it('should use the MISSING operator and a translated rule origin key for ORIGIN_HAS_NO_ONE', async () => {
      const criterion = await single({ APPRAISAL_RULE: ORIGIN_HAS_NO_ONE });

      expect(criterion).toMatchObject({
        keyElt: 'RULE_ORIGIN_APPRAISAL_RULE',
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
        valueTranslated: true,
      });
    });

    it('should use the EXISTS operator for ORIGIN_HAS_AT_LEAST_ONE', async () => {
      const criterion = await single({ STORAGE_RULE: ORIGIN_HAS_AT_LEAST_ONE });

      expect(criterion).toMatchObject({
        keyElt: 'RULE_ORIGIN_STORAGE_RULE',
        operator: CriteriaOperator.EXISTS,
        valueTranslated: true,
      });
    });

    it('should prefix the key for final action values', async () => {
      expect((await single({ APPRAISAL_RULE: FINAL_ACTION_HAS_FINAL_ACTION })).keyElt).toEqual('FINAL_ACTION_APPRAISAL_RULE');
      expect((await single({ STORAGE_RULE: FINAL_ACTION_TYPE_KEEP })).keyElt).toEqual('FINAL_ACTION_TYPE_STORAGE_RULE');
    });

    it('should map archive unit type values to the translated ALL_ARCHIVE_UNIT_TYPES key', async () => {
      const criterion = await single({ archiveUnitType: ARCHIVE_UNIT_WITH_OBJECTS });

      expect(criterion).toMatchObject({
        keyElt: 'ALL_ARCHIVE_UNIT_TYPES',
        operator: CriteriaOperator.EQ,
        valueTranslated: true,
        valueElt: { value: ARCHIVE_UNIT_WITH_OBJECTS },
      });
    });

    it('should map the archive unit with errors value to an EXISTS criterion on ERRORS', async () => {
      const criterion = await single({ archiveUnitType: ARCHIVE_UNIT_WITH_ERRORS });

      expect(criterion).toMatchObject({
        keyElt: 'ERRORS',
        operator: CriteriaOperator.EXISTS,
        valueTranslated: true,
      });
    });

    it('should extract the virtual path, real parent id and real parent title from a VIRTUAL value', async () => {
      const criterion = await single({ VIRTUAL: '/Folder/Sub folder/parent-id/Parent title' });

      expect(criterion.keyElt).toEqual('VIRTUAL');
      expect(criterion.valueElt).toEqual({
        id: 'VIRTUAL',
        value: '/Folder/Sub folder',
        beginInterval: '',
        endInterval: '',
        virtualNodeRealParentId: 'parent-id',
        virtualNodeRealParentTitle: 'Parent title',
      });
      expect(criterion.labelElt).toEqual('/Folder/Sub folder/parent-id/Parent title');
    });
  });
});
