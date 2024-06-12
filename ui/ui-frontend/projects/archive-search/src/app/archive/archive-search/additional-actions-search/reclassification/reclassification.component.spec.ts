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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import {
  BASE_URL,
  ConfirmDialogService,
  CriteriaDataType,
  CriteriaOperator,
  InjectorModule,
  LoggerModule,
  SearchCriteriaDto,
  SearchCriteriaTypeEnum,
  StartupService,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ArchiveService } from '../../../archive.service';
import { ArchiveUnitValidatorService } from '../../../validators/archive-unit-validator.service';
import { ReclassificationComponent } from './reclassification.component';

const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

const startupServiceStub = {
  getPortalUrl: () => '',
  getConfigStringValue: () => '',
  getReferentialUrl: () => '',
};

const confirmDialogServiceMock = {
  confirm: () => of(true),
  listenToEscapeKeyPress: () => of({}),
  confirmBeforeClosing: () => of(),
};

const archiveServiceMock = {
  archive: () => of('test archive'),
  search: () => of([]),
  getAccessContractById: () => of({}),
  reclassification: () => of({}),
  searchArchiveUnitsByCriteria: () => of({}),
  getTotalTrackHitsByCriteria: () => of({}),
  openSnackBarForWorkflow: () => of({}),
};

describe('ReclassificationComponent', () => {
  let component: ReclassificationComponent;
  let fixture: ComponentFixture<ReclassificationComponent>;

  const archiveUnitValidatorServicesSpy = jasmine.createSpyObj('ArchiveUnitValidatorService', {
    alreadyExistParents: () => of(),
    existArchiveUnit: () => of(),
  });
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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReclassificationComponent],
      imports: [
        InjectorModule,
        VitamUICommonTestModule,
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        BrowserAnimationsModule,
        MatSnackBarModule,
      ],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: StartupService, useValue: startupServiceStub },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            itemSelected: 25,
            reclassificationCriteria: searchCriteriaDto,
            accessContract: 'ContratTNR',
            tenantIdentifier: '2',
            selectedItemCountKnown: true,
            archiveUnitGuidSelected: 'erer545ddfd87f5dfdf1d2fes1df2sdfs5er4e5r',
            archiveUnitAllunitup: [],
          },
        },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ArchiveUnitValidatorService, useValue: archiveUnitValidatorServicesSpy },
        { provide: ConfirmDialogService, useValue: confirmDialogServiceMock },
        { provide: ArchiveService, useValue: archiveServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReclassificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it(' component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call reclassification of archiveService', () => {
    // Given
    spyOn(archiveServiceMock, 'reclassification').and.callThrough();

    // When
    component.onSubmit();

    // Then
    expect(archiveServiceMock.reclassification).toHaveBeenCalled();
  });

  it('items Selected should be grather than 0 ', () => {
    expect(component.itemSelected).toBeGreaterThan(0);
    expect(component.itemSelected).toEqual(25);
  });

  it('Should have an accessContract ', () => {
    expect(component.data.accessContract).toBeDefined();
    expect(component.data.accessContract).not.toBeNull();
    expect(component.data.accessContract).toEqual('ContratTNR');
  });

  it('Should have a tenant identifier ', () => {
    expect(component.data.tenantIdentifier).toBeDefined();
    expect(component.data.tenantIdentifier).not.toBeNull();
    expect(component.data.tenantIdentifier).toEqual('2');
  });

  it('should call searchArchiveUnitsByCriteria of archiveService', () => {
    // Given
    spyOn(archiveServiceMock, 'searchArchiveUnitsByCriteria').and.callThrough();

    // When
    component.calculateChilds();

    // Then
    expect(component.pendingGetChilds).toBeFalsy();
    expect(archiveServiceMock.searchArchiveUnitsByCriteria).toHaveBeenCalled();
  });

  it('should call searchArchiveUnitsByCriteria of archiveService', () => {
    // Given
    spyOn(archiveServiceMock, 'searchArchiveUnitsByCriteria').and.callThrough();

    // When
    component.calculateChilds();

    // Then
    expect(archiveServiceMock.searchArchiveUnitsByCriteria).toHaveBeenCalled();
  });

  it('should call getTotalTrackHitsByCriteria of archiveService', () => {
    // Given
    spyOn(archiveServiceMock, 'getTotalTrackHitsByCriteria').and.callThrough();

    // When
    component.loadExactCount();

    // Then
    expect(archiveServiceMock.getTotalTrackHitsByCriteria).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 2 cdk steps', () => {
      const elementCdkStep = fixture.nativeElement.querySelectorAll('cdk-step');
      expect(elementCdkStep.length).toBe(2);
    });

    it('should have 3 mat options  ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementMatOption = nativeElement.querySelectorAll('mat-option');

      // Then
      expect(elementMatOption).toBeTruthy();
      expect(elementMatOption.length).toBe(3);
    });
  });
});
