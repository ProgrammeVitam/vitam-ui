import type { MockedObject } from 'vitest';
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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';
import { ManagementRuleSearchComponent } from './management-rule-search.component';
import { MANAGEMENT_RULE_SEARCH_CONFIG, ManagementRuleCheckboxDescriptor, ManagementRuleType } from './management-rule-search.config';
import { QueryParamsService } from '../../../app/modules/url/query-params.service';
import { SearchCriteriaService } from '../../../app/modules/models/criteria/search-criteria.service';
import { MANAGEMENT_RULE_SHARED_DATA_SERVICE } from '../../models/management-rule-shared-data-service.interface';
import { ACCESS_RULE, ApplicationId, ORIGIN_WAITING_RECALCULATE, StartupService } from '../../../app/modules';
import { TranslateModule } from '@ngx-translate/core';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ManagementRuleCriteriaService } from './services/management-rule-criteria.service';

describe('ManagementRuleSearchComponent', () => {
  let component: ManagementRuleSearchComponent;
  let fixture: ComponentFixture<ManagementRuleSearchComponent>;
  let mockQueryParamsService: MockedObject<QueryParamsService>;
  let mockSearchCriteriaService: MockedObject<SearchCriteriaService>;

  let mockManagementRuleCriteriaService: MockedObject<ManagementRuleCriteriaService>;

  beforeEach(async () => {
    mockManagementRuleCriteriaService = {
      initializeFromSearchCriteria: vi.fn().mockName('ManagementRuleCriteriaService.initializeFromSearchCriteria'),
      addFromParams: vi.fn().mockName('ManagementRuleCriteriaService.addFromParams'),
      buildDateCriteria: vi.fn().mockName('ManagementRuleCriteriaService.buildDateCriteria'),
      applyDefaultOriginCriteria: vi.fn().mockName('ManagementRuleCriteriaService.applyDefaultOriginCriteria'),
    };
    mockManagementRuleCriteriaService.initializeFromSearchCriteria.mockImplementation((_obs, _keys, _criteria, _destroyed, onDefault) => {
      onDefault();
      return of().subscribe();
    });

    const mockSharedDataService = {
      searchCriteria$: of(new Map()),
      addSimpleSearchCriteriaSubject: of(),
      addSimpleSearchCriteriaSubjects: of(),
      sendRemoveFromChildSearchCriteriaAction: vi.fn(),
      getRemoveAction: vi.fn().mockReturnValue(of(null)),
    };
    const mockManagementRuleSearchConfigFactory = (startupService: StartupService) => {
      const applicationConfigurationMap = {
        [ApplicationId.COLLECT_APP]: {
          [ManagementRuleType.ACCESS]: {
            ruleType: ACCESS_RULE,
            checkboxConfig: [] as ManagementRuleCheckboxDescriptor[],
            checkboxes: [{ key: 'MyKey', labelKey: 'MyKeyLabel' }],
            id_endDate: 'myEndDateId',
          },
          [ManagementRuleType.APPRAISAL]: {
            ruleType: 'APPRAISAL_RULE',
            checkboxConfig: [] as ManagementRuleCheckboxDescriptor[],
            checkboxes: [{ key: 'MyKey', labelKey: 'MyKeyLabel' }],
          },
        },
        [ApplicationId.ARCHIVE_SEARCH_APP]: {
          [ManagementRuleType.ACCESS]: {
            ruleType: ACCESS_RULE,
            checkboxConfig: [] as ManagementRuleCheckboxDescriptor[],
            checkboxes: [
              { key: 'MyKey', labelKey: 'MyKeyLabel' },
              { key: 'MyKey2', labelKey: 'MyKeyLabel2' },
            ],
          },
          [ManagementRuleType.APPRAISAL]: {
            ruleType: 'APPRAISAL_RULE',
            checkboxConfig: [] as ManagementRuleCheckboxDescriptor[],
            checkboxes: [{ key: 'MyKey', labelKey: 'MyKeyLabel' }],
          },
        },
      };

      // @ts-ignore
      return applicationConfigurationMap[startupService.CURRENT_APP_ID];
    };

    mockQueryParamsService = {
      builder: vi.fn().mockName('QueryParamsService.builder'),
    };
    mockSearchCriteriaService = {
      addCriteria: vi.fn().mockName('SearchCriteriaService.addCriteria'),
      removeCriteria: vi.fn().mockName('SearchCriteriaService.removeCriteria'),
    };

    await TestBed.configureTestingModule({
      imports: [ManagementRuleSearchComponent, TranslateModule.forRoot()],
      providers: [
        FormBuilder,
        { provide: QueryParamsService, useValue: mockQueryParamsService },
        { provide: SearchCriteriaService, useValue: mockSearchCriteriaService },
        { provide: StartupService, useValue: { CURRENT_APP_ID: ApplicationId.COLLECT_APP } },
        { provide: MANAGEMENT_RULE_SHARED_DATA_SERVICE, useValue: mockSharedDataService },
        { provide: ManagementRuleCriteriaService, useValue: mockManagementRuleCriteriaService },
        provideNativeDateAdapter(),
        { provide: ManagementRuleCriteriaService, useValue: mockManagementRuleCriteriaService },
      ],
    })
      .overrideComponent(ManagementRuleSearchComponent, {
        set: {
          providers: [
            {
              provide: MANAGEMENT_RULE_SEARCH_CONFIG,
              useFactory: mockManagementRuleSearchConfigFactory,
              deps: [StartupService],
            },
            { provide: ManagementRuleCriteriaService, useValue: mockManagementRuleCriteriaService },
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ManagementRuleSearchComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('type', ManagementRuleType.ACCESS);
    fixture.componentRef.setInput('tenantIdentifier', 1);
    fixture.componentRef.setInput('rules', of([]));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with correct rule type configuration', () => {
    expect(component.config).toBeDefined();
    expect(component.config.ruleType).toBe(ACCESS_RULE);
  });

  it('should reset date inputs after adding criteria', () => {
    component.criteriaForm.controls.ruleStartDate.setValue('2023-01-01');
    component.addBeginDtCriteria();
    expect(component.criteriaForm.controls.ruleStartDate.value).toBeNull();
  });

  it('should reset ruleEliminationIdentifier after processing form update', () => {
    fixture.componentRef.setInput('type', ManagementRuleType.APPRAISAL);
    component.ngOnInit();
    component.criteriaForm.controls.ruleEliminationIdentifier.setValue('ELIM-123');

    // @ts-ignore
    component.processFormUpdate(component.criteriaForm.value);

    expect(component.criteriaForm.controls.ruleEliminationIdentifier.value).toBeNull();
  });

  it('should initialize ORIGIN_WAITING_RECALCULATE from hasWaitingToRecalculateCriteria input', () => {
    fixture.componentRef.setInput('hasWaitingToRecalculateCriteria', true);
    component.ngOnInit();

    expect(component.additionalCriteria.get(ORIGIN_WAITING_RECALCULATE)).toBe(true);
  });
});
