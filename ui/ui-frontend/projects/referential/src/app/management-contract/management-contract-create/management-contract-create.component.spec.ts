/**
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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { EMPTY, of } from 'rxjs';
import { BASE_URL, ConfirmDialogService, InjectorModule, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ManagementContractService } from '../management-contract.service';
import { ManagementContractCreateComponent } from './management-contract-create.component';

describe('ManagementContractCreateComponent', () => {
  let component: ManagementContractCreateComponent;
  let fixture: ComponentFixture<ManagementContractCreateComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close', 'keydownEvents']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const managementContractServiceMock = {
    get: () => of({}),
    getAll: () => of([]),
    getAllForTenant: () => of([]),
    exists: () => of(true),
    existsProperties: () => of(true),
    patch: () => of({}),
    create: () => of({}),
  };

  const confirmDialogServiceMock = {
    listenToEscapeKeyPress: () => EMPTY,
    confirmBeforeClosing: () => of(),
    confirm: () => of(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        LoggerModule.forRoot(),
        BrowserAnimationsModule,
        NoopAnimationsModule,
      ],
      declarations: [ManagementContractCreateComponent],
      providers: [
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ManagementContractService, useValue: managementContractServiceMock },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ConfirmDialogService, useValue: confirmDialogServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('isDisabledButton should be false after calling create service', () => {
    component.form.setValue({
      identifier: 'contract_id',
      name: 'Contract name',
      description: 'description',
      status: 'INACTIVE',
      storage: {
        unitStrategy: 'default1',
        objectGroupStrategy: 'default2',
        objectStrategy: 'default3',
      },
    });
    component.onSubmit();
    expect(component.isDisabledButton).toBeFalsy();
  });

  it('should return the exact percent', () => {
    expect(component.stepProgress).toBeDefined();
    expect(component.stepProgress).toEqual(50);
  });

  it('second Step should be valid', () => {
    component.form.setValue({
      identifier: 'contract_id',
      name: 'Contract name',
      description: 'description',
      status: 'INACTIVE',
      storage: {
        unitStrategy: 'default1',
        objectGroupStrategy: 'default2',
        objectStrategy: 'default3',
      },
    });
    expect(component.secondStepInvalid()).toBeFalsy();
  });

  it('should call create of ManagementContractService', () => {
    // Given
    spyOn(managementContractServiceMock, 'create').and.callThrough();

    // When
    component.form.setValue({
      identifier: 'Contract identifier',
      name: 'Contract name',
      description: 'description',
      status: 'INACTIVE',
      storage: {
        unitStrategy: 'default',
        objectGroupStrategy: 'default',
        objectStrategy: 'default',
      },
    });
    component.onSubmit();

    // Then
    expect(managementContractServiceMock.create).toHaveBeenCalled();
  });

  it('first Step should be valid', () => {
    component.form.setValue({
      identifier: 'contract_id',
      name: 'Contract name',
      description: 'description',
      status: 'INACTIVE',
      storage: {
        unitStrategy: 'default',
        objectGroupStrategy: 'default',
        objectStrategy: 'default',
      },
    });
    expect(component.firstStepInvalid()).toBeTruthy();
  });

  describe('DOM', () => {
    it('should have 4 buttons ', () => {
      const nativeElement = fixture.nativeElement;
      const elementBtn = nativeElement.querySelectorAll('button[type=button]');
      expect(elementBtn.length).toBe(4);
    });

    it('should have 1 submit button ', () => {
      const nativeElement = fixture.nativeElement;
      const elementBtn = nativeElement.querySelectorAll('button[type=submit]');
      expect(elementBtn.length).toBe(1);
    });

    it('should have 2 cdk steps', () => {
      const nativeElement = fixture.nativeElement;
      const elementCdkStep = nativeElement.querySelectorAll('cdk-step');
      expect(elementCdkStep.length).toBe(2);
    });

    it('should have 3 rows data', () => {
      const nativeElement = fixture.nativeElement;
      const rowElement = nativeElement.querySelectorAll('.row');
      expect(rowElement.length).toBe(3);
    });

    it('should have 4 vitamui input when the slave mode is not activated', () => {
      component.isSlaveMode = false;
      const nativeElement = fixture.nativeElement;
      const vitamUiInputElement = nativeElement.querySelectorAll('vitamui-common-input');
      expect(vitamUiInputElement.length).toBe(4);
    });
  });
});
