/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, PagedResult, SearchCriteriaDto, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { UpdateUnitManagementRuleService } from '../../../../../common-services/update-unit-management-rule.service';
import { RuleTypeEnum } from '../../../../../models/rule-type-enum';
import { ActionsRules, ManagementRules, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { UnlockCategoryInheritanceComponent } from './unlock-category-inheritance.component';

const translations: any = { TEST: 'Mock translate test' };
const accessContract = 'AccessContract';

const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

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

const vitamSearchResult: PagedResult = {
  pageNumbers: 1,
  totalResults: 55,
  results: [
    {
      '#id': 'aeaqaaaaaehgnz5dabg42amave3wcliaaaba',
      Title: '009734_20130456_0001_20120229_DI_AN_PJL_programmation_execution_des_peines_dernier_mot.pdf.pdf',
      DescriptionLevel: 'Item',
      OriginatingAgencyArchiveUnitIdentifier: [''],
      TransactedDate: '2012-10-22T13:28:02',
      '#tenant': 1,
      '#object': 'aebaaaaaaehgnz5dabg42amave3wcgyaaabq',
      '#unitups': ['aeaqaaaaaehgnz5dabg42amave3wclqaaaba'],
      '#min': 1,
      '#max': 5,
      '#allunitups': [
        'aeaqaaaaaehgnz5dabg42amave3wclqaaaba',
        'aeaqaaaaaehgnz5dabg42amave3wcmiaaaca',
        'aeaqaaaaaehgnz5dabg42amave3wcmiaaaba',
        'aeaqaaaaaehgnz5dabg42amave3wcmiaaada',
      ],
      '#unitType': 'INGEST',
      '#operations': [
        'aeeaaaaaaghefnffaaykaamave3vaaqaaaaq',
        'aeeaaaaaaghefnffaaxrwamavumc2baaaaaq',
        'aeeaaaaaaghefnffaaxrwamavumxooaaaaaq',
        'aeeaaaaaaghefnffaaxrwamavupqsyyaaaaq',
      ],
      '#opi': 'aeeaaaaaaghefnffaaykaamave3vaaqaaaaq',
      '#originating_agency': 'Vitam',
      '#originating_agencies': ['Vitam'],
      '#management': {},
      Xtag: [''],
      Vtag: [''],
      '#storage': {
        strategyId: 'default',
      },
      '#qualifiers': [''],
      OriginatingSystemId: [''],
      PhysicalAgency: [''],
      PhysicalStatus: [''],
      PhysicalType: [''],
      Keyword: [''],
      originating_agencyName: 'Equipe projet interministérielle Vitam',
    },
  ],

  facets: [
    {
      name: 'COUNT_BY_NODE',
      buckets: [
        {
          value: 'aeaqaaaaaehgnz5dabg42amave3wclqaaaba',
          count: 1,
        },
        {
          value: 'aeaqaaaaaehgnz5dabg42amave3wcmiaaaba',
          count: 1,
        },
        {
          value: 'aeaqaaaaaehgnz5dabg42amave3wcmiaaaca',
          count: 1,
        },
        {
          value: 'aeaqaaaaaehgnz5dabg42amave3wcmiaaada',
          count: 1,
        },
      ],
    },
  ],
};

describe('UnlockCategoryInheritanceComponent', () => {
  let component: UnlockCategoryInheritanceComponent;
  let fixture: ComponentFixture<UnlockCategoryInheritanceComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open', 'close']);
  matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'close']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

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
    getRuleManagementCategory: () => of({}),
  };

  const archiveServiceMock = {
    searchArchiveUnitsByCriteria: () => of(vitamSearchResult),
    getTotalTrackHitsByCriteria: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [UnlockCategoryInheritanceComponent],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: UpdateUnitManagementRuleService, useValue: updateUnitManagementRuleServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UnlockCategoryInheritanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call getManagementRules of ManagementRulesSharedDataService', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleTypeDUA = ruleCategoryAction;
    spyOn(managementRulesSharedDataServiceMock, 'getManagementRules').and.callThrough();

    // When
    component.onCancelUnlockCategoryInheritance();

    // Then
    expect(managementRulesSharedDataServiceMock.getManagementRules).toHaveBeenCalled();
  });

  it('should the stepValid parameter be true after an unlock of category inheritance', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;
    component.criteriaSearchDSLQuery = searchCriteriaDto;

    // When
    component.onUnlockCategoryInheritance();

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.showText).toBeTruthy();
    expect(
      component.ruleActions.find(
        (ruleAction) =>
          ruleAction.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE && ruleAction.ruleType === RuleTypeEnum.APPRAISALRULE
      ).stepValid
    ).toBeTruthy();
  });

  it('should the criteria Search DSL Query have 3 elements for the update operation', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;
    component.criteriaSearchDSLQuery = searchCriteriaDto;

    // When
    component.addControlQuery();

    // Then
    expect(component.criteriaSearchDSLQuery).not.toBeNull();
    expect(component.criteriaSearchDSLQuery.criteriaList.length).toBe(3);
  });

  it('should not call the standard search service when the exactAccount parameter is true', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;
    component.criteriaSearchDSLQuery = searchCriteriaDto;
    spyOn(archiveServiceMock, 'searchArchiveUnitsByCriteria').and.callThrough();

    // When
    component.hasExactCount = true;
    component.addControlQuery();

    // Then
    expect(component.criteriaSearchDSLQuery).not.toBeNull();
    expect(archiveServiceMock.searchArchiveUnitsByCriteria).toHaveBeenCalledTimes(0);
    expect(archiveServiceMock.searchArchiveUnitsByCriteria).not.toHaveBeenCalled();
  });

  it('should call getManagementRules of ManagementRulesSharedDataService', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleTypeDUA = ruleCategoryAction;
    spyOn(managementRulesSharedDataServiceMock, 'getManagementRules').and.callThrough();

    // When
    component.onUnlockCategoryInheritance();

    // Then
    expect(managementRulesSharedDataServiceMock.getManagementRules).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 2 buttons ', () => {
      const nativeElement = fixture.nativeElement;
      const elementBtn = nativeElement.querySelectorAll('button[type=button]');
      expect(elementBtn.length).toBe(2);
    });
  });
});
