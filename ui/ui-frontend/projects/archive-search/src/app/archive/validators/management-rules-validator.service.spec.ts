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
import { Injector, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { form, SchemaFn } from '@angular/forms/signals';
import { BehaviorSubject, firstValueFrom, of } from 'rxjs';
import { RuleService } from 'vitamui-library';
import { ManagementRulesSharedDataService } from '../../core/management-rules-shared-data.service';
import { ManagementRules, RuleAction, RuleCategoryAction } from '../models/ruleAction.interface';
import { ManagementRulesValidatorService } from './management-rules-validator.service';

const rules: RuleAction[] = [
  {
    rule: 'ruleId1',
    name: 'ruleName_1',
  },
  {
    rule: 'ruleId3',
    name: 'ruleName_3',
  },
  {
    rule: 'ruleId2',
    name: 'ruleName_2',
  },
];

const ruleCategoryAction: RuleCategoryAction = {
  finalAction: 'keep',
  preventInheritance: false,
  rules,
  preventRulesIdToRemove: ['preventRuleId1'],
};
const managementRules: ManagementRules[] = [
  {
    category: 'category',
    ruleCategoryAction,
    actionType: 'actionType',
  },
];

describe('ManagementRulesValidatorService', () => {
  let service: ManagementRulesValidatorService;
  const managementRulesSharedDataServiceMock = {
    getManagementRules: () => of(managementRules),
    getRuleCategory: () => of('category'),
  };
  let ruleServiceMock: { existsProperties: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    vi.useFakeTimers();
    ruleServiceMock = { existsProperties: vi.fn().mockReturnValue(of(true)) };
    TestBed.configureTestingModule({
      providers: [
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: RuleService, useValue: ruleServiceMock },
      ],
    });
    service = TestBed.inject(ManagementRulesValidatorService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  async function validate(rule: string, schema: SchemaFn<{ rule: string }>) {
    const testForm = form(signal({ rule }), schema, { injector: TestBed.inject(Injector) });
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    return testForm
      .rule()
      .errors()
      .map((error) => error.kind);
  }

  it('should the service be created', () => {
    expect(service).toBeTruthy();
  });

  it('filterRuleActions should return true when ruleId exists in the list', async () => {
    expect(await firstValueFrom(service.filterRuleActions('ruleId1'))).toBe(true);
    expect(await firstValueFrom(service.filterRuleActions('unknown'))).toBe(false);
  });

  describe('uniqueRuleId', () => {
    it('should return uniqueRuleId when ruleId exists in the list', async () => {
      expect(await validate('ruleId1', (path) => service.uniqueRuleId(path.rule))).toEqual(['uniqueRuleId']);
    });

    it('should not return any error when ruleId does not exist in the list', async () => {
      expect(await validate('unknown', (path) => service.uniqueRuleId(path.rule))).toEqual([]);
    });

    it('should not return any error when ruleId is ignored', async () => {
      expect(await validate('ruleId1', (path) => service.uniqueRuleId(path.rule, () => 'ruleId1'))).toEqual([]);
    });

    it('should not return any error when ruleId is ignored with a string code', async () => {
      expect(await validate('ruleId1', (path) => service.uniqueRuleId(path.rule, 'ruleId1'))).toEqual([]);
    });

    it('should only report the error once the debounce time has elapsed', async () => {
      const testForm = form(signal({ rule: 'ruleId1' }), (path) => service.uniqueRuleId(path.rule), {
        injector: TestBed.inject(Injector),
      });
      TestBed.tick();

      await vi.advanceTimersByTimeAsync(service.debounceTime - 1);
      expect(testForm.rule().errors()).toEqual([]);

      await vi.advanceTimersByTimeAsync(1);
      expect(
        testForm
          .rule()
          .errors()
          .map((error) => error.kind),
      ).toEqual(['uniqueRuleId']);
    });
  });

  describe('uniquePreventRuleId', () => {
    it('should return uniquePreventRuleId when ruleId exists in the prevent rules', async () => {
      expect(await validate('preventRuleId1', (path) => service.uniquePreventRuleId(path.rule))).toEqual(['uniquePreventRuleId']);
    });

    it('should not return any error when ruleId does not exist in the prevent rules', async () => {
      expect(await validate('ruleId1', (path) => service.uniquePreventRuleId(path.rule))).toEqual([]);
    });
  });

  describe('checkRuleIdExistence', () => {
    it('should not return any error when the rule exists', async () => {
      expect(await validate('ruleId1', (path) => service.checkRuleIdExistence(path.rule))).toEqual([]);
      expect(ruleServiceMock.existsProperties).toHaveBeenCalledWith({ ruleId: 'ruleId1', ruleType: 'category' });
    });

    it('should return ruleIdExists when the rule does not exist', async () => {
      ruleServiceMock.existsProperties.mockReturnValue(of(false));

      expect(await validate('ruleId1', (path) => service.checkRuleIdExistence(path.rule))).toEqual(['ruleIdExists']);
    });

    it('should not call the service when the value is empty', async () => {
      expect(await validate('', (path) => service.checkRuleIdExistence(path.rule))).toEqual([]);
      expect(ruleServiceMock.existsProperties).not.toHaveBeenCalled();
    });

    it('should not call the service when the value is the rule id to ignore', async () => {
      ruleServiceMock.existsProperties.mockReturnValue(of(false));

      expect(await validate('ruleId1', (path) => service.checkRuleIdExistence(path.rule, 'ruleId1'))).toEqual([]);
      expect(ruleServiceMock.existsProperties).not.toHaveBeenCalled();
    });
  });
});

describe('ManagementRulesValidatorService rule filters', () => {
  let service: ManagementRulesValidatorService;
  let ruleCategory$: BehaviorSubject<string>;

  const appraisalRuleCategoryAction: RuleCategoryAction = {
    finalAction: 'keep',
    preventInheritance: false,
    rules: [
      { rule: 'APP-00001', name: 'rule 1' },
      { rule: 'APP-00003', oldRule: 'APP-00002', name: 'rule 3' },
    ],
    preventRulesIdToRemove: ['APP-00010'],
  };
  const categories: ManagementRules[] = [
    { category: 'AppraisalRule', ruleCategoryAction: appraisalRuleCategoryAction, actionType: 'ADD_RULES' },
    { category: 'AccessRule', ruleCategoryAction: {}, actionType: 'ADD_RULES' },
  ];

  beforeEach(() => {
    ruleCategory$ = new BehaviorSubject('AppraisalRule');
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ManagementRulesSharedDataService,
          useValue: { getManagementRules: () => of(categories), getRuleCategory: () => ruleCategory$ },
        },
        { provide: RuleService, useValue: {} },
      ],
    });
    service = TestBed.inject(ManagementRulesValidatorService);
  });

  describe('filterRuleActions', () => {
    it('should match a rule id of the selected category on rule or oldRule', async () => {
      expect(await firstValueFrom(service.filterRuleActions('APP-00001'))).toBe(true);
      expect(await firstValueFrom(service.filterRuleActions('APP-00002'))).toBe(true);
      expect(await firstValueFrom(service.filterRuleActions('APP-00099'))).toBe(false);
    });

    it('should only look at the currently selected category', async () => {
      ruleCategory$.next('StorageRule');
      expect(await firstValueFrom(service.filterRuleActions('APP-00001'))).toBe(false);
    });

    it('should return false when the category has no rules', async () => {
      ruleCategory$.next('AccessRule');
      expect(await firstValueFrom(service.filterRuleActions('APP-00001'))).toBe(false);
    });
  });

  describe('filterPreventRulesId', () => {
    it('should match ids listed in preventRulesIdToRemove of the selected category', async () => {
      expect(await firstValueFrom(service.filterPreventRulesId('APP-00010'))).toBe(true);
      expect(await firstValueFrom(service.filterPreventRulesId('APP-00001'))).toBe(false);
    });

    it('should return false when no prevent list or no category action exists', async () => {
      ruleCategory$.next('AccessRule');
      expect(await firstValueFrom(service.filterPreventRulesId('APP-00010'))).toBe(false);
      ruleCategory$.next('Unknown');
      expect(await firstValueFrom(service.filterPreventRulesId('APP-00010'))).toBe(false);
    });
  });
});
