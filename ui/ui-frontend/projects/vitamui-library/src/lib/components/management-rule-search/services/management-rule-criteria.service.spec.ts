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
import { of, Subject } from 'rxjs';
import { Params } from '@angular/router';
import {
  CriteriaOperator,
  CriteriaSearchCriteria,
  SearchCriteriaTypeEnum,
  SearchCriteriaValue,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_INHERITE_AT_LEAST_ONE,
} from '../../../../app/modules';
import { QueryParamsService } from '../../../../app/modules/url/query-params.service';
import { SearchCriteriaService } from '../../../../app/modules/models/criteria/search-criteria.service';
import {
  MANAGEMENT_RULE_SHARED_DATA_SERVICE,
  ManagementRuleSharedDataService,
} from '../../../models/management-rule-shared-data-service.interface';
import { ManagementRuleCriteriaService } from './management-rule-criteria.service';

describe('ManagementRuleCriteriaService', () => {
  let service: ManagementRuleCriteriaService;
  let sharedDataService: any;
  let searchCriteriaService: any;
  let queryParamsService: any;
  let mockBuilder: any;

  beforeEach(() => {
    mockBuilder = {
      addQueryParam: vi.fn().mockReturnValue(mockBuilder),
      navigate: vi.fn(),
    };

    sharedDataService = {
      addSimpleSearchCriteriaSubjects: vi.fn().mockName('ManagementRuleSharedDataService.addSimpleSearchCriteriaSubjects'),
      addSimpleSearchCriteriaSubject: vi.fn().mockName('ManagementRuleSharedDataService.addSimpleSearchCriteriaSubject'),
    };

    searchCriteriaService = {
      toSearchCriteria: vi.fn().mockName('SearchCriteriaService.toSearchCriteria'),
    };

    queryParamsService = {
      builder: vi.fn().mockName('QueryParamsService.builder'),
    };
    queryParamsService.builder.mockReturnValue(mockBuilder);

    TestBed.configureTestingModule({
      providers: [
        ManagementRuleCriteriaService,
        { provide: MANAGEMENT_RULE_SHARED_DATA_SERVICE, useValue: sharedDataService },
        { provide: SearchCriteriaService, useValue: searchCriteriaService },
        { provide: QueryParamsService, useValue: queryParamsService },
      ],
    });

    service = TestBed.inject(ManagementRuleCriteriaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('initializeFromSearchCriteria', () => {
    it('should update additional criteria when filtered criterias exist', async () => {
      const mockSearchCriteria = new Map<string, CriteriaSearchCriteria>([
        [
          'key1',
          {
            values: [{ value: { value: 'value1' } } as SearchCriteriaValue],
          } as CriteriaSearchCriteria,
        ],
      ]);
      const searchCriteria$ = of(mockSearchCriteria);
      const keysList = ['key1'];
      const additionalCriteria = new Map<string, boolean>();
      const destroyed$ = new Subject<void>();
      const onDefault = vi.fn();

      service.initializeFromSearchCriteria(searchCriteria$, keysList, additionalCriteria, destroyed$, onDefault);

      setTimeout(() => {
        expect(additionalCriteria.get('value1')).toBe(true);
        expect(onDefault).not.toHaveBeenCalled();
      }, 10);
    });

    it('should call onDefault when no filtered criterias exist', async () => {
      const mockSearchCriteria = new Map<string, CriteriaSearchCriteria>();
      const searchCriteria$ = of(mockSearchCriteria);
      const keysList = ['key1'];
      const additionalCriteria = new Map<string, boolean>();
      const destroyed$ = new Subject<void>();
      const onDefault = vi.fn();

      service.initializeFromSearchCriteria(searchCriteria$, keysList, additionalCriteria, destroyed$, onDefault);

      setTimeout(() => {
        expect(onDefault).toHaveBeenCalled();
      }, 10);
    });
  });

  describe('addFromParams', () => {
    it('should convert params to search criteria and add them', async () => {
      const params: Params = { key1: 'value1', key2: 'value2' };
      const mockCriteria1 = [{ key: 'key1' }] as any;
      const mockCriteria2 = [{ key: 'key2' }] as any;

      searchCriteriaService.toSearchCriteria
        .mockReturnValueOnce(Promise.resolve(mockCriteria1))
        .mockReturnValueOnce(Promise.resolve(mockCriteria2));

      await service.addFromParams(params);

      expect(searchCriteriaService.toSearchCriteria).toHaveBeenCalledTimes(2);
      expect(sharedDataService.addSimpleSearchCriteriaSubjects).toHaveBeenCalledWith(mockCriteria1);
      expect(sharedDataService.addSimpleSearchCriteriaSubjects).toHaveBeenCalledWith(mockCriteria2);
    });
  });

  describe('buildDateCriteria', () => {
    it('should add date criteria when startDate is provided', () => {
      const baseKey = 'RULE_END_DATE';
      const ruleType = 'AccessRule';
      const dateId = 'date1';
      const operator = CriteriaOperator.LTE;
      const startDate = '2024-01-01';
      const endDate: any = null;
      const searchCriteriaType = SearchCriteriaTypeEnum.ACCESS_RULE;

      service.buildDateCriteria(baseKey, ruleType, dateId, operator, startDate, endDate, searchCriteriaType);

      expect(sharedDataService.addSimpleSearchCriteriaSubject).toHaveBeenCalled();
    });

    it('should not add criteria when startDate is null', () => {
      service.buildDateCriteria(
        'RULE_END_DATE',
        'AccessRule',
        'date1',
        CriteriaOperator.LTE,
        null,
        null,
        SearchCriteriaTypeEnum.ACCESS_RULE,
      );

      expect(sharedDataService.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
    });
  });

  describe('applyDefaultOriginCriteria', () => {
    it('should add query params and update additional criteria', () => {
      const checkboxConfig = {
        [ORIGIN_HAS_AT_LEAST_ONE]: { key: ORIGIN_HAS_AT_LEAST_ONE, prop: 'hasOrigin' },
        [ORIGIN_INHERITE_AT_LEAST_ONE]: { key: ORIGIN_INHERITE_AT_LEAST_ONE, prop: 'inheritOrigin' },
      };
      const ruleType = 'AccessRule';
      const additionalCriteria = new Map<string, boolean>();

      service.applyDefaultOriginCriteria(checkboxConfig, ruleType, additionalCriteria);

      expect(mockBuilder.addQueryParam).toHaveBeenCalledWith(ruleType, ORIGIN_HAS_AT_LEAST_ONE);
      expect(mockBuilder.addQueryParam).toHaveBeenCalledWith(ruleType, ORIGIN_INHERITE_AT_LEAST_ONE);
      expect(mockBuilder.navigate).toHaveBeenCalledWith({ replaceUrl: true });
      expect(additionalCriteria.get(ORIGIN_HAS_AT_LEAST_ONE)).toBe(true);
      expect(additionalCriteria.get(ORIGIN_INHERITE_AT_LEAST_ONE)).toBe(true);
    });

    it('should skip checkboxes not in config', () => {
      const checkboxConfig = {};
      const ruleType = 'AccessRule';
      const additionalCriteria = new Map<string, boolean>();

      service.applyDefaultOriginCriteria(checkboxConfig, ruleType, additionalCriteria);

      expect(mockBuilder.addQueryParam).not.toHaveBeenCalled();
      expect(mockBuilder.navigate).toHaveBeenCalledWith({ replaceUrl: true });
    });
  });
});
