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

import { ReclassificationDialogComponent } from './reclassification-dialog.component';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ConfirmDialogService } from '../../../app/modules/components/common-confirm-dialog/confirm-dialog.service';
import { BASE_URL, WINDOW_LOCATION } from '../../../app/modules/injection-tokens';
import { LoggerModule } from '../../../app/modules/logger/logger.module';
import { CriteriaDataType, CriteriaOperator } from '../../../app/modules/models/criteria/criteria.enums';
import { PagedResult, SearchCriteriaDto, SearchCriteriaTypeEnum } from '../../../app/modules/models/criteria/search-criteria.interface';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Observable, of } from 'rxjs';
import { ReclassificationService } from '../../../app/modules/services/reclassification.service';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';

const matDialogRefSpy = {
  close: vi.fn().mockName('MatDialogRef.close'),
};
const matDialogSpy = {
  open: vi.fn().mockName('MatDialog.open'),
};

const confirmDialogServiceMock = {
  confirm: () => of(true),
  listenToEscapeKeyPress: () => of({}),
  confirmBeforeClosing: () => of(),
};

const reclassificationServiceMock = {
  reclassification: () => of({}),
  searchArchiveUnitsByCriteria: (): Observable<PagedResult> => of({ results: [], pageNumbers: 0, totalResults: 0 }),
  openSnackBarForWorkflow: () => of({}),
  getTotalTrackHitsByCriteria: () => of({}),
};

describe('ReclassificationDialogComponent', () => {
  let component: ReclassificationDialogComponent;
  let fixture: ComponentFixture<ReclassificationDialogComponent>;

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
      imports: [NoopAnimationsModule, MatSnackBarModule, LoggerModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            appName: 'COLLECT',
            itemSelected: 25,
            reclassificationCriteria: searchCriteriaDto,
            accessContract: 'ContratTNR',
            tenantIdentifier: 2,
            transactionId: '1234567890',
            selectedItemCountKnown: true,
            archiveUnitGuidSelected: 'erer545ddfd87f5dfdf1d2fes1df2sdfs5er4e5r',
            archiveUnitAllunitup: [],
          },
        },
        { provide: ConfirmDialogService, useValue: confirmDialogServiceMock },
        { provide: ReclassificationService, useValue: reclassificationServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReclassificationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call reclassification of reclassificationService', () => {
    // Given
    vi.spyOn(reclassificationServiceMock, 'reclassification');
    component.form.controls.action.setValue('PULL' as any);

    // When
    component.onSubmit();

    // Then
    expect(reclassificationServiceMock.reclassification).toHaveBeenCalled();
  });

  it('items Selected should be greater than 0', () => {
    expect(component.data.itemSelected).toBeGreaterThan(0);
    expect(component.data.itemSelected).toEqual(25);
  });

  it('Should have an app Name', () => {
    expect(component.data.appName).toBeDefined();
    expect(component.data.appName).not.toBeNull();
    expect(component.data.appName).toEqual('COLLECT');
  });

  it('Should have a tenant identifier', () => {
    expect(component.data.tenantIdentifier).toBeDefined();
    expect(component.data.tenantIdentifier).not.toBeNull();
    expect(component.data.tenantIdentifier).toEqual(2);
  });

  it('Should have a transactionId', () => {
    expect(component.data.transactionId).toBeDefined();
    expect(component.data.transactionId).not.toBeNull();
    expect(component.data.transactionId).toEqual('1234567890');
  });
});
