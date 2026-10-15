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
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { UpdateUnitManagementRuleService } from 'projects/archive-search/src/app/archive/common-services/update-unit-management-rule.service';
import { ManagementRulesValidatorService } from 'projects/archive-search/src/app/archive/validators/management-rules-validator.service';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Observable, of } from 'rxjs';
import {
  CriteriaDataType,
  CriteriaOperator,
  InjectorModule,
  LoggerModule,
  Rule,
  SearchCriteriaDto,
  SearchCriteriaTypeEnum,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ActionsRules, ManagementRules, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { UpdateUnitRulesComponent } from './update-unit-rules.component';

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
    actionType: 'BLOCK_CATEGORY_INHERITANCE',
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
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      dataType: CriteriaDataType.STRING,
    },
  ],
  pageNumber: 2,
  size: 20,
  language: 'fr',
  trackTotalHits: true,
};

const matDialogRefSpy = {
  open: vi.fn().mockName('MatDialogRef.open'),
  close: vi.fn().mockName('MatDialogRef.close'),
};
matDialogRefSpy.open.mockReturnValue({ afterClosed: () => of(true) });

const matDialogSpy = {
  open: vi.fn().mockName('MatDialog.open'),
  close: vi.fn().mockName('MatDialog.close'),
};
matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });

const managementRulesSharedDataServiceMock = {
  getCriteriaSearchDSLQuery: () => of(searchCriteriaDto),
  getManagementRules: () => of(managementRules),
  getAccessContract: () => of(accessContract),
  getselectedItems: () => of(527851),
  getCriteriaSearchListToSave: () => of({}),
  getRuleActions: () => of(ruleActions),
  emitManagementRules: () => of({}),
  emitRuleActions: () => of({}),
};

const updateUnitManagementRuleServiceMock = {
  goToUpdateManagementRule: vi
    .fn()
    .mockName('UpdateUnitManagementRuleService.goToUpdateManagementRule')
    .mockReturnValue(() => of({})),
  getRuleManagementCategory: vi
    .fn()
    .mockName('UpdateUnitManagementRuleService.getRuleManagementCategory')
    .mockReturnValue(() => of({})),
};

const managementRulesValidatorServiceMock = {
  uniquePreventRuleId: vi
    .fn()
    .mockName('ManagementRulesValidatorService.uniquePreventRuleId')
    .mockReturnValue(() => of({})),
  uniqueRuleId: vi
    .fn()
    .mockName('ManagementRulesValidatorService.uniqueRuleId')
    .mockReturnValue(() => of({})),
  ruleIdPattern: vi
    .fn()
    .mockName('ManagementRulesValidatorService.ruleIdPattern')
    .mockReturnValue(() => of({})),
  checkRuleIdExistence: vi
    .fn()
    .mockName('ManagementRulesValidatorService.checkRuleIdExistence')
    .mockReturnValue(() => of({})),
};

describe('UpdateUnitRulesComponent', () => {
  let component: UpdateUnitRulesComponent;
  let fixture: ComponentFixture<UpdateUnitRulesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InjectorModule, LoggerModule.forRoot(), VitamUICommonTestModule, UpdateUnitRulesComponent],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: ManagementRulesValidatorService, useValue: managementRulesValidatorServiceMock },
        { provide: UpdateUnitManagementRuleService, useValue: updateUnitManagementRuleServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateUnitRulesComponent);
    component = fixture.componentInstance;
    component.rulesList = new Observable<Rule[]>();
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call getManagementRules and emitManagementRules of ManagementRulesSharedDataService', () => {
    vi.spyOn(managementRulesSharedDataServiceMock, 'getManagementRules');
    vi.spyOn(managementRulesSharedDataServiceMock, 'emitManagementRules');
    // When
    component.submit();

    // Then
    expect(managementRulesSharedDataServiceMock.getManagementRules).toHaveBeenCalled();
    expect(managementRulesSharedDataServiceMock.emitManagementRules).toHaveBeenCalled();
  });

  it('should call getCriteriaSearchDSLQuery of ManagementRulesSharedDataService', () => {
    vi.spyOn(managementRulesSharedDataServiceMock, 'getCriteriaSearchDSLQuery');
    // When
    component.initDSLQuery();

    // Then
    expect(managementRulesSharedDataServiceMock.getCriteriaSearchDSLQuery).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 3 titles ', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('form > div > label');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(3);
      expect(formTitlesHtmlElements[1].textContent).toContain('RULES.TARGET_MANAGEMENT_RULE');
    });

    it('should have 2 vitamui selects', () => {
      const nativeElement = fixture.nativeElement;
      const elementVitamuiInput = nativeElement.querySelectorAll('vitamui-select');
      expect(elementVitamuiInput.length).toBe(2);
    });

    it('should have 1 submit button ', () => {
      const nativeElement = fixture.nativeElement;
      const elementSubmitBtn = nativeElement.querySelectorAll('button[type=submit]');
      expect(elementSubmitBtn.length).toBe(1);
    });
  });
});
