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
 *
 *
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { ManagementRulesValidatorService } from 'projects/archive-search/src/app/archive/validators/management-rules-validator.service';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ActionsRules, ManagementRules, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { BlockRulesInheritanceComponent } from './block-rules-inheritance.component';

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

const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open', 'close']);
matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });

const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'close']);
matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

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

const managementRulesValidatorServiceMock = jasmine.createSpyObj('ManagementRulesValidatorService', {
  uniquePreventRuleId: () => of({}),
  uniqueRuleId: () => of({}),
  ruleIdPattern: () => of({}),
  checkRuleIdExistence: () => of({}),
});

describe('BlockRulesInheritanceComponent', () => {
  let component: BlockRulesInheritanceComponent;
  let fixture: ComponentFixture<BlockRulesInheritanceComponent>;

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
        MatSnackBarModule,
      ],
      declarations: [BlockRulesInheritanceComponent],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: ManagementRulesValidatorService, useValue: managementRulesValidatorServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BlockRulesInheritanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call getManagementRules and emitManagementRules of ManagementRulesSharedDataService', () => {
    spyOn(managementRulesSharedDataServiceMock, 'getManagementRules').and.callThrough();
    spyOn(managementRulesSharedDataServiceMock, 'emitManagementRules').and.callThrough();
    // When
    component.blockRuleInheritance();

    // Then
    expect(managementRulesSharedDataServiceMock.getManagementRules).toHaveBeenCalled();
    expect(managementRulesSharedDataServiceMock.emitManagementRules).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 1 title ', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('.title-text');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(1);
      expect(formTitlesHtmlElements[0].textContent).toContain('RULES.APRAISAL_RULES.SOURCE_RULE');
    });

    it('should have 1 vitamui editable input', () => {
      const elementVitamuiInput = fixture.nativeElement.querySelectorAll('vitamui-common-editable-input');
      expect(elementVitamuiInput.length).toBe(1);
    });
  });
});
