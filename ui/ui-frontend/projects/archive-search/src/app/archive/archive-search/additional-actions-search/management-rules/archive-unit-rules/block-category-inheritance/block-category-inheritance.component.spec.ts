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
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { RuleTypeEnum } from '../../../../../models/rule-type-enum';
import { ActionsRules, ManagementRules, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { BlockCategoryInheritanceComponent } from './block-category-inheritance.component';

const translations: any = { TEST: 'Mock translate test' };
const accessContract = 'AccessContract';

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

describe('BlockCategoryInheritanceComponent', () => {
  let component: BlockCategoryInheritanceComponent;
  let fixture: ComponentFixture<BlockCategoryInheritanceComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open', 'close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'close']);

  const managementRulesSharedDataServiceMock = {
    getCriteriaSearchDSLQuery: () => of({}),
    getManagementRules: () => of(managementRules),
    getAccessContract: () => of(accessContract),
    getselectedItems: () => of(527851),
    getCriteriaSearchListToSave: () => of({}),
    getRuleActions: () => of(ruleActions),
    emitManagementRules: () => of({}),
    emitRuleActions: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        VitamUICommonTestModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [BlockCategoryInheritanceComponent],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BlockCategoryInheritanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should the stepValid parameter be true after a block of category inheritance', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;

    // When
    component.onBlockCategoryInheritance();

    // Then
    expect(component.ruleActions).not.toBeNull();
    expect(component.ruleActions.length).toBe(5);
    expect(component.showText).toBeTruthy();
    expect(
      component.ruleActions.find(
        (ruleAction) =>
          ruleAction.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE && ruleAction.ruleType === RuleTypeEnum.APPRAISALRULE,
      ).stepValid,
    ).toBeTruthy();
  });

  it('should the paremeter of category inheritance be valid', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleActions = ruleActions;

    // When
    component.onBlockCategoryInheritance();

    // Then
    expect(component.isValidValue).toBeDefined();
    expect(component.isValidValue).toBeTruthy();
  });

  it('should call getManagementRules of ManagementRulesSharedDataService', () => {
    // Given
    component.ruleCategory = RuleTypeEnum.APPRAISALRULE;
    component.ruleTypeDUA = ruleCategoryAction;
    spyOn(managementRulesSharedDataServiceMock, 'getManagementRules').and.callThrough();

    // When
    component.onBlockCategoryInheritance();

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
