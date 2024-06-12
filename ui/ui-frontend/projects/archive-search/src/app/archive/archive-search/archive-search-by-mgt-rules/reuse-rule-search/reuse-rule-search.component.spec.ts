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
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ArchiveSharedDataService } from 'projects/archive-search/src/app/core/archive-shared-data.service';
import { of } from 'rxjs';
import {
  BASE_URL,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  InjectorModule,
  LoggerModule,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { RuleValidator } from '../../rule.validator';
import { ReuseRuleSearchComponent } from './reuse-rule-search.component';

const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

const ruleValidatorMock = jasmine.createSpyObj('RuleValidator', {
  ruleIdPattern: () => of(),
  uniqueRuleId: () => of(),
});

const archiveSharedDataServiceSpy = {
  receiveReuseFromMainSearchCriteriaSubject: () => of({}),
  addSimpleSearchCriteriaSubject: () => of({}),
  sendRemoveFromChildSearchCriteriaAction: () => of({}),
};

describe('ReuseRuleSearchComponent', () => {
  let component: ReuseRuleSearchComponent;
  let fixture: ComponentFixture<ReuseRuleSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReuseRuleSearchComponent],
      imports: [
        InjectorModule,
        VitamUICommonTestModule,
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        BrowserAnimationsModule,
      ],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ArchiveSharedDataService, useValue: archiveSharedDataServiceSpy },
        { provide: RuleValidator, useValue: ruleValidatorMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReuseRuleSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it(' component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call addSimpleSearchCriteriaSubject when keyElt and CriteriaValue are not null', () => {
    // Given
    const criteriaValue: CriteriaValue = {
      id: 'criteriaId',
      value: 'criteriaValue',
    };
    spyOn(archiveSharedDataServiceSpy, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria('keyElt', criteriaValue, 'labelElt', true, CriteriaOperator.EQ, true, CriteriaDataType.DATE);

    // Then
    expect(archiveSharedDataServiceSpy.addSimpleSearchCriteriaSubject).toHaveBeenCalled();
  });

  it('should return true', () => {
    component.updateEndDateInterval(true);
    expect(component.endDateInterval).toBeTruthy();
  });

  it('should not call addSimpleSearchCriteriaSubject when keyElt is null', () => {
    // Given
    const criteriaValue: CriteriaValue = {
      id: 'criteriaId',
      value: 'criteriaValue',
    };
    spyOn(archiveSharedDataServiceSpy, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria(null, criteriaValue, 'labelElt', true, CriteriaOperator.EQ, true, CriteriaDataType.STRING);

    // Then
    expect(archiveSharedDataServiceSpy.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 10 rows', () => {
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('.row');
      expect(elementRow.length).toBe(10);
    });

    it('should have 4 text titles', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('.title-text');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(4);
      expect(formTitlesHtmlElements[1].textContent).toContain('ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.FIELDS.END_DATE_REUSE');
    });

    it('should have 2 vitamui editable input  ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementVitamuiInput = nativeElement.querySelectorAll('vitamui-common-editable-input');

      // Then
      expect(elementVitamuiInput).toBeTruthy();
      expect(elementVitamuiInput.length).toBe(2);
    });
  });
});
