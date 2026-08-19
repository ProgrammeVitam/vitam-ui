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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { of } from 'rxjs';
import {
  BASE_URL,
  InjectorModule,
  LoggerModule,
  SearchCriteriaDto,
  Tenant,
  TenantSelectionService,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { UpdateUnitManagementRuleService } from '../../../../common-services/update-unit-management-rule.service';
import { RuleTypeEnum } from '../../../../models/rule-type-enum';
import { ActionsRules, ManagementRules, RuleCategoryAction } from '../../../../models/ruleAction.interface';
import { ArchiveUnitRulesComponent } from './archive-unit-rules.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

const accessContract = 'AccessContract';

const ruleCategoryAction: RuleCategoryAction = {
  rules: [],
  finalAction: 'keep',
  preventInheritance: false,
};
const managementRules: ManagementRules[] = [
  {
    category: 'category',
    ruleCategoryAction,
    actionType: 'actionType',
  },
];
const ruleActions: ActionsRules[] = [
  {
    ruleType: 'AppraisalRule',
    actionType: 'UPDATE_PROPERTY',
    id: 1,
    ruleId: '',
    stepValid: false,
  },
  {
    ruleType: 'AppraisalRule',
    actionType: 'UNLOCK_CATEGORY_INHERITANCE',
    id: 2,
    ruleId: '',
    stepValid: false,
  },
  {
    ruleType: 'AccessRule',
    actionType: 'DELETE_RULES',
    id: 3,
    ruleId: '',
    stepValid: true,
  },
  {
    ruleType: 'StorageRule',
    actionType: 'UPDATE_PROPERTY',
    id: 4,
    ruleId: '',
    stepValid: true,
  },
  {
    ruleType: 'StorageRule',
    actionType: 'ADD_RULES',
    id: 5,
    ruleId: '',
    stepValid: true,
  },
];

const searchCriteriaDto: SearchCriteriaDto = {
  criteriaList: [
    {
      criteria: 'GUID',
      values: [
        {
          value: 'aeaqaaaaaeh54ftgaamraamatl3yixiaaaaq',
          id: 'aeaqaaaaaeh54ftgaamraamatl3yixiaaaaq',
        },
        {
          value: 'aeaqaaaaaeh54ftgaay7aamac2xhibyaaaba',
          id: 'aeaqaaaaaeh54ftgaay7aamac2xhibyaaaba',
        },
        {
          value: 'aeaqaaaaaehmay6yaaqhual6ysiaariaaaba',
          id: 'aeaqaaaaaehmay6yaaqhual6ysiaariaaaba',
        },
        {
          value: 'aeaqaaaaaeh54ftgaay7aamac2xhibyaaaca',
          id: 'aeaqaaaaaeh54ftgaay7aamac2xhibyaaaca',
        },
        {
          value: 'aeaqaaaaaeh54ftgaay7aamac2xzgcyaaaba',
          id: 'aeaqaaaaaeh54ftgaay7aamac2xzgcyaaaba',
        },
      ],
      operator: 'EQ',
      category: 'FIELDS',
      dataType: 'STRING',
    },
  ],
  pageNumber: 0,
  size: 10,
  language: 'fr',
  trackTotalHits: false,
};

const tenant: Tenant = {
  identifier: 1,
  name: '',
  ownerId: '',
  customerId: '',
  enabled: false,
  proof: false,
  readonly: false,
  ingestContractHoldingIdentifier: '',
  itemIngestContractIdentifier: '',
  accessContractHoldingIdentifier: '',
  accessContractLogbookIdentifier: '',
  id: '',
};

describe('ArchiveUnitRulesComponent', () => {
  let component: ArchiveUnitRulesComponent;
  let fixture: ComponentFixture<ArchiveUnitRulesComponent>;

  const matDialogRefSpy = {
    open: vi.fn().mockName('MatDialogRef.open'),
    close: vi.fn().mockName('MatDialogRef.close'),
  };
  const matDialogSpy = {
    open: vi.fn().mockName('MatDialog.open'),
    close: vi.fn().mockName('MatDialog.close'),
  };

  const managementRulesSharedDataServiceMock = {
    getCriteriaSearchDSLQuery: () => of(searchCriteriaDto),
    getManagementRules: () => of(managementRules),
    getAccessContract: () => of(accessContract),
    getselectedItems: () => of(527851),
    getCriteriaSearchListToSave: () => of({}),
    getRuleActions: () => of(ruleActions),
    emitManagementRules: () => of({}),
    emitRuleActions: () => of({}),
    emitIsRuleDuplicated: () => of({}),
    getIsRuleDuplicated: () => of(false),
  };

  const updateUnitManagementRuleServiceMock = {
    getRuleManagementCategory: () => of({}),
  };

  const tenantSelectionServiceMock = {
    getSelectedTenant: () => of(tenant),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VitamUICommonTestModule, InjectorModule, LoggerModule.forRoot(), RouterTestingModule, ArchiveUnitRulesComponent],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: UpdateUnitManagementRuleService, useValue: updateUnitManagementRuleServiceMock },
        { provide: TenantSelectionService, useValue: tenantSelectionServiceMock },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitRulesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should component creation be truthy', () => {
    expect(component).toBeTruthy();
  });

  it('should the step be valid after update confirmation', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;

    // When
    component.confirmStep(2);

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.ruleActions.find((ruleAction) => ruleAction.id === 2).stepValid).toBeTruthy();
  });

  it('should return only the defined actions on AccessRule category', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.ACCESSRULE;
    component.ruleActions = ruleActions;

    // When
    component.getActionByRuleType();

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.getActionByRuleType().length).not.toBeNull();
    expect(component.getActionByRuleType().length).toBe(1);
    expect(component.getActionByRuleType()).toEqual(['DELETE_RULES']);
  });

  it('should return all the defined rule actions on AppraisalRule category', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;

    // When
    component.getActions();

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.getActions().length).not.toBeNull();
    expect(component.getActions().length).toBe(2);
  });

  it('the step should not be valid after cancelation of the update', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;

    // When
    component.cancelStep(2);

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.ruleActions.find((ruleAction) => ruleAction.id === 2).stepValid).toBeFalsy();
  });

  it('should return all the defined rule actions on AccessRule category', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.ACCESSRULE;
    component.ruleActions = ruleActions;

    // When
    component.getActions();

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.getActions().length).not.toBeNull();
    expect(component.getActions().length).toBe(1);
  });

  it('should return only the defined actions on AppraisalRule category', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;

    // When
    component.getActionByRuleType();

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.getActionByRuleType().length).not.toBeNull();
    expect(component.getActionByRuleType().length).toBe(2);
    expect(component.getActionByRuleType()).toEqual(['UPDATE_PROPERTY', 'UNLOCK_CATEGORY_INHERITANCE']);
  });

  it('should call getRuleActions of ManagementRulesSharedDataService', () => {
    vi.spyOn(managementRulesSharedDataServiceMock, 'getRuleActions');
    // When
    component.cancelStep(1);

    // Then
    expect(managementRulesSharedDataServiceMock.getRuleActions).toHaveBeenCalled();
  });

  it('should call getRuleActions of ManagementRulesSharedDataService', () => {
    vi.spyOn(managementRulesSharedDataServiceMock, 'getRuleActions');
    // When
    component.deleteForm(1, 'ruleId', 'ADD_PROPERTY');

    // Then
    expect(managementRulesSharedDataServiceMock.getRuleActions).toHaveBeenCalled();
  });
});
