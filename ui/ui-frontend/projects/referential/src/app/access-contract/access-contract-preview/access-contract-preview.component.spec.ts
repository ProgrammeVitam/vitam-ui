/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { AccessContractApiService, BASE_URL, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { AccessContractPreviewComponent } from './access-contract-preview.component';

const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

describe('AccessContractPreviewComponent', () => {
  let component: AccessContractPreviewComponent;
  let fixture: ComponentFixture<AccessContractPreviewComponent>;

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'close']);

  const accessContractApiServiceMock = {
    getAccessContractById: () => of({}),
    getAllAccessContracts: () => of({}),
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
      ],
      declarations: [AccessContractPreviewComponent],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: AccessContractApiService, useValue: accessContractApiServiceMock },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should have 5 mat-tab', () => {
    // When
    const nativeElement = fixture.nativeElement;
    const matTabElements = nativeElement.querySelectorAll('mat-tab');

    // Then
    expect(matTabElements.length).toEqual(5);
  });

  it('should have 1 mat-tab-group', () => {
    // When
    const nativeElement = fixture.nativeElement;
    const matTabGrpElements = nativeElement.querySelectorAll('mat-tab-group');

    // Then
    expect(matTabGrpElements.length).toEqual(1);
  });
});
