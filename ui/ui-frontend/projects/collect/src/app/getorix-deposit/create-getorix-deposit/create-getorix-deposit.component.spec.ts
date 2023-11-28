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

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { of } from 'rxjs';
import { AuthService, BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { GetorixDeposit } from '../core/model/getorix-deposit.interface';
import { GetorixDepositService } from '../getorix-deposit.service';
import { CreateGetorixDepositComponent } from './create-getorix-deposit.component';

describe('CreateGetorixDepositComponent', () => {
  let component: CreateGetorixDepositComponent;
  let fixture: ComponentFixture<CreateGetorixDepositComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open', 'closeAll']);

  const routerMock = {
    navigate: () => {},
    url: 'https://localhost/collect/getorix-deposit/tenant/1/create',
    params: of({ tenantIdentifier: 1 }),
    data: of({ appId: 'GETORIX_DEPOSIT_APP' }),
    events: of({}),
  };

  const getorixDepositServiceMock = jasmine.createSpyObj('GetorixDepositService', {
    createGetorixDeposit: () => of({}),
  });

  const authServiceMock = { user: { level: '', id: 'userId' } };

  beforeEach(async () => {
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

    await TestBed.configureTestingModule({
      declarations: [CreateGetorixDepositComponent],
      imports: [HttpClientTestingModule, TranslateModule.forRoot(), InjectorModule, LoggerModule.forRoot()],
      providers: [
        {
          provide: Router,
          useValue: routerMock,
        },
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            navigate: () => {},
            params: of({ tenantIdentifier: 1 }),
            data: of({ appId: 'GETORIX_DEPOSIT_APP' }),
            events: of({}),
          },
        },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: GetorixDepositService, useValue: getorixDepositServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateGetorixDepositComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should the creation of second officer to be enabled', () => {
    component.addNewScientificOfficer();
    expect(component.showSecondScientifOfficer).toBeTruthy();
    expect(component.showOfficerAction).toBeFalsy();
  });

  it('the attribute should not be valid when no value given', () => {
    component.checkInputValidation('originatingAgency');
    const result = component.depositFormError.find((deposit) => deposit.inputName == 'originatingAgency');
    expect(result).not.toBeNull();
    expect(result.isValid).toBeFalsy();
  });

  it('should set the operation type value', () => {
    const operationType = 'SEARCH';
    let getorixDeposit = {} as GetorixDeposit;
    component.getOperationTypeValue(operationType, getorixDeposit);
    expect(getorixDeposit).not.toBeNull();
    expect(getorixDeposit.operationType).toEqual('SEARCH');
  });

  it('should disable the operation type input', () => {
    const event = { value: 'SEARCH', type: 'input' };
    component.checkOperationType(event);
    expect(component.operationType).toEqual('SEARCH');
    expect(component.operationTypeDisabled).toBeTruthy();
  });

  it('should show the creation form', () => {
    component.showForm = false;
    component.startDepositCreation();
    expect(component.showForm).toBeTruthy();
  });

  it('should redirect to /getorix-deposit/tenant/1', () => {
    const router = TestBed.inject(Router);
    spyOn(routerMock, 'navigate').and.callThrough();
    component.returnToMainPage();
    expect(router.navigate).toHaveBeenCalled();
  });

  it('should return the object without empty values', () => {
    const initialObject = {
      attribute: 'value',
      attribute2: 'value2',
      attribute3: '',
      attribute4: 'value4',
      object: { attribute1: 'value1', attribute3: '', attribute4: 'value4' },
    };
    let excpectedObject = {
      attribute: 'value',
      attribute2: 'value2',
      attribute4: 'value4',
      object: { attribute1: 'value1', attribute4: 'value4' },
    };
    const result = component.removeEmptyValue(initialObject);
    expect(excpectedObject).toEqual(result);
  });

  it('should versatileServiceDisabled have the correct value', () => {
    component.versatileServiceDisabled = false;
    component.checkVersatileServiceBoxChange();
    expect(component.checkVersatileServiceBoxChange).toBeTruthy();
  });

  it('should furniture value be true', () => {
    const event = { value: 'YES' };
    component.checkFurniture(event);
    expect(component.getorixDepositform.get('furniture')).toBeTruthy();
  });

  it('should call close for all close dialog', () => {
    const matDialogSpyTest = TestBed.inject(MatDialog);
    component.exitWithoutSave();
    expect(matDialogSpyTest.closeAll).toHaveBeenCalled();
  });

  it('should call exit without save when there is no form changes', () => {
    const matDialogSpyTest = TestBed.inject(MatDialog);
    component.formChanged = false;
    component.returnToGetorixInitialPage();
    expect(matDialogSpyTest.closeAll).toHaveBeenCalled();
    expect(component.showForm).toBeFalsy();
  });

  it('the attribute should be valid', () => {
    component.getorixDepositform.patchValue({ originatingAgency: 'test' });
    component.checkInputValidation('originatingAgency');
    const result = component.depositFormError.find((deposit) => deposit.inputName == 'originatingAgency');
    expect(result).not.toBeNull();
    expect(result.isValid).toBeTruthy();
  });

  it('should select the correct category', () => {
    component.onMouseMove('agencyDetails');
    const result = component.operationCategoryList.find((element) => element.target == 'agencyDetails');
    expect(result.isSelected).toBeTruthy();
  });

  it('should set the operation type value from the form', () => {
    const operationType = 'OTHER';
    let getorixDeposit = {} as GetorixDeposit;
    component.getOperationTypeValue(operationType, getorixDeposit);
    expect(getorixDeposit).not.toBeNull();
    expect(getorixDeposit.operationType).toEqual('');
  });

  it('should enable the operation type input', () => {
    const event = { value: 'OTHER', type: 'input' };
    component.checkOperationType(event);
    expect(component.operationType).toEqual('OTHER');
    expect(component.operationTypeDisabled).toBeFalsy();
  });

  it('should the creation of second officer to be disabled', () => {
    component.deleteSecondScientificOfficer();
    expect(component.showSecondScientifOfficer).toBeFalsy();
    expect(component.showOfficerAction).toBeTruthy();
  });
});
