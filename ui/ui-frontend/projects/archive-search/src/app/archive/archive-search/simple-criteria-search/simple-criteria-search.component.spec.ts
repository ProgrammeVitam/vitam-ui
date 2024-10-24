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
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, CriteriaDataType, CriteriaOperator, CriteriaValue, InjectorModule } from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../../core/management-rules-shared-data.service';
import { ArchiveService } from '../../archive.service';
import { SimpleCriteriaSearchComponent } from './simple-criteria-search.component';

describe('SimpleCriteriaSearchComponent', () => {
  let component: SimpleCriteriaSearchComponent;
  let fixture: ComponentFixture<SimpleCriteriaSearchComponent>;

  const archiveExchangeDataServiceMock = {
    addSimpleSearchCriteriaSubject: () => of(),
    receiveRemoveFromChildSearchCriteriaSubject: () => of(),
  };

  const archiveServiceStub = {
    loadFilingHoldingSchemeTree: () => of([]),
    getOntologiesFromJson: () => of([]),
    hasArchiveSearchRole: () => of(true),
    getAccessContractById: () => of({}),
    hasAccessContractPermissions: () => of(true),
    getExternalOntologiesList: () => of([]),
  };

  const managementRulesSharedDataServiceMock = {
    getCriteriaSearchListToSave: () => of([]),
  };

  beforeEach(async () => {
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });
    await TestBed.configureTestingModule({
      declarations: [SimpleCriteriaSearchComponent],
      providers: [
        FormBuilder,
        { provide: ArchiveService, useValue: archiveServiceStub },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ArchiveSharedDataService, useValue: archiveExchangeDataServiceMock },
        { provide: ManagementRulesSharedDataService, useValue: managementRulesSharedDataServiceMock },
        { provide: BASE_URL, useValue: '/fake-api' },
      ],
      imports: [HttpClientTestingModule, InjectorModule, TranslateModule.forRoot()],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SimpleCriteriaSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call addSimpleSearchCriteriaSubject when keyElt is null', () => {
    // Given
    const criteriaValue: CriteriaValue = {
      id: 'criteriaId',
      value: 'criteriaValue',
    };
    spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria(null, criteriaValue, 'labelElt', true, CriteriaOperator.EQ, true, CriteriaDataType.STRING);

    // Then
    expect(archiveExchangeDataServiceMock.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
  });

  it('should return false', () => {
    expect(component.isValueTranslated('test')).toBeFalsy();
  });
  it('should return true', () => {
    // Given
    const FINAL_ACTION_TYPE = 'FINAL_ACTION_TYPE';
    const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';

    // When
    const firstResult = component.isValueTranslated(FINAL_ACTION_TYPE);
    const secondResult = component.isValueTranslated(ALL_ARCHIVE_UNIT_TYPES);

    // Then
    expect(firstResult).toBeTruthy();
    expect(secondResult).toBeTruthy();
  });

  it('should call addSimpleSearchCriteriaSubject when keyElt and CriteriaValue are not null', () => {
    // Given
    const criteriaValue: CriteriaValue = {
      id: 'criteriaId',
      value: 'criteriaValue',
    };
    spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria('keyElt', criteriaValue, 'labelElt', true, CriteriaOperator.EQ, true, CriteriaDataType.DATE);

    // Then
    expect(archiveExchangeDataServiceMock.addSimpleSearchCriteriaSubject).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 6 vitamui editables inputs ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('vitamui-common-editable-input');

      // Then
      expect(elementRow.length).toBe(6);
    });
  });
});
