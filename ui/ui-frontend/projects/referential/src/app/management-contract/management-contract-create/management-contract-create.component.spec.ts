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
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { EMPTY, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { BASE_URL, ConfirmDialogService, InjectorModule, LoggerModule, ManagementContract, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ManagementContractToFormGroupConverterService } from '../components/management-contract-to-form-group-converter.service';
import { ManagementContractService } from '../management-contract.service';
import { ManagementContractCreateComponent } from './management-contract-create.component';

describe('ManagementContractCreateComponent', () => {
  let managementContractToFormGroupConverterService: ManagementContractToFormGroupConverterService;
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
    create: () => of({}).pipe(delay(100)),
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
        MatSelectModule,
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
        ManagementContractToFormGroupConverterService,
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
    managementContractToFormGroupConverterService = TestBed.inject(ManagementContractToFormGroupConverterService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractCreateComponent);
    component = fixture.componentInstance;
  });

  describe('when in slave mode', () => {
    beforeEach(() => {
      component.isSlaveMode = true;
      fixture.detectChanges();
    });

    it('component should be created', () => {
      expect(component).toBeTruthy();
    });

    it('form should have an "identifier" field', () => {
      expect(component.form.get('identifier')).toBeTruthy();
    });

    it('isDisabledButton should be true then false after calling create service', fakeAsync(() => {
      const managementContractForm: any = {
        identifier: 'contract_id',
        name: 'Contract name',
        description: 'description',
        status: 'INACTIVE',
        storage: {
          unitStrategy: 'default1',
          objectGroupStrategy: 'default2',
          objectStrategy: 'default3',
        },
      };
      const formGroup = managementContractToFormGroupConverterService.convert(managementContractForm as ManagementContract);
      component.form.patchValue({ ...managementContractForm, ...formGroup.value });
      component.onSubmit();
      expect(component.isDisabledButton).toBeTruthy(); // Button disabled while waiting for creation
      tick(500);
      expect(component.isDisabledButton).toBeFalsy(); // Button enabled after creation
    }));

    it('first step should be valid after async validation', fakeAsync(() => {
      spyOn(managementContractServiceMock, 'existsProperties').and.returnValue(of(false));

      const managementContractForm: any = {
        identifier: 'Contract identifier',
        name: 'Contract name',
        description: 'description',
        status: 'INACTIVE',
      };

      component.form.patchValue(managementContractForm);

      expect(component.firstStepInvalid()).toBeTruthy(); // Form is invalid first while waiting for async validation
      tick(1000); // We "wait" for async validation
      expect(component.firstStepInvalid()).toBeFalsy(); // Finally, the form is valid
    }));

    it('first step should be invalid after async validation if properties already exist', fakeAsync(() => {
      spyOn(managementContractServiceMock, 'existsProperties').and.returnValue(of(true));

      const managementContractForm: any = {
        identifier: 'Contract identifier',
        name: 'Contract name',
        description: 'description',
        status: 'INACTIVE',
      };

      component.form.patchValue(managementContractForm);

      expect(component.firstStepInvalid()).toBeTruthy(); // Form is invalid first while waiting for async validation
      tick(1000); // We "wait" for async validation
      expect(component.firstStepInvalid()).toBeTruthy(); // Form is still invalid as async validation is KO
    }));

    it('second step should be valid only if every field is filled', () => {
      expect(component.secondStepInvalid()).toBeFalsy(); // Step is valid by default as fields have default values that are valid

      component.form.patchValue({
        storage: {
          unitStrategy: '',
          objectGroupStrategy: 'default',
          objectStrategy: 'default',
        },
      });
      expect(component.secondStepInvalid()).toBeTruthy(); // Step should be invalid if one field is empty

      component.form.patchValue({
        storage: {
          unitStrategy: 'default',
          objectGroupStrategy: '',
          objectStrategy: 'default',
        },
      });
      expect(component.secondStepInvalid()).toBeTruthy(); // Step should be invalid if one field is empty

      component.form.patchValue({
        storage: {
          unitStrategy: 'default',
          objectGroupStrategy: 'default',
          objectStrategy: '',
        },
      });
      expect(component.secondStepInvalid()).toBeTruthy(); // Step should be invalid if one field is empty
    });

    it('should call create of ManagementContractService', () => {
      // Given
      spyOn(managementContractServiceMock, 'create').and.callThrough();

      // When
      const managementContractForm: any = {
        identifier: 'Contract identifier',
        name: 'Contract name',
        description: 'description',
        status: 'INACTIVE',
        storage: {
          unitStrategy: 'default',
          objectGroupStrategy: 'default',
          objectStrategy: 'default',
        },
      };
      const formGroup = managementContractToFormGroupConverterService.convert(managementContractForm as ManagementContract);
      component.form.patchValue({ ...managementContractForm, ...formGroup.value });
      component.onSubmit();

      // Then
      expect(managementContractServiceMock.create).toHaveBeenCalled();
    });
  });

  describe('when NOT in slave mode', () => {
    beforeEach(() => {
      component.isSlaveMode = false;
      fixture.detectChanges();
    });

    it('form should not have "identifier" field', () => {
      expect(component.form.get('identifier')).toBeFalsy();
    });

    it('first step should be valid after async validation', fakeAsync(() => {
      spyOn(managementContractServiceMock, 'existsProperties').and.returnValue(of(false));

      const managementContractForm: any = {
        // No "identifier" when no slave mode
        name: 'Contract name',
        description: 'description',
        status: 'INACTIVE',
      };

      component.form.patchValue(managementContractForm);

      expect(component.firstStepInvalid()).toBeTruthy(); // Form is invalid first while waiting for async validation
      tick(1000); // We "wait" for async validation
      expect(component.firstStepInvalid()).toBeFalsy(); // Finally, the form is valid
    }));

    it('first step should be invalid after async validation if properties already exist', fakeAsync(() => {
      spyOn(managementContractServiceMock, 'existsProperties').and.returnValue(of(true));

      const managementContractForm: any = {
        // No "identifier" when no slave mode
        name: 'Contract name',
        description: 'description',
        status: 'INACTIVE',
      };

      component.form.patchValue(managementContractForm);

      expect(component.firstStepInvalid()).toBeTruthy(); // Form is invalid first while waiting for async validation
      tick(1000); // We "wait" for async validation
      expect(component.firstStepInvalid()).toBeTruthy(); // Form is still invalid as async validation is KO
    }));
  });
});
