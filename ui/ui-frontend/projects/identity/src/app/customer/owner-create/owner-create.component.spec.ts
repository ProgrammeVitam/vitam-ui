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
import { EMPTY, of } from 'rxjs';
import { ConfirmDialogService, Owner } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';


import { Component, forwardRef, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { OwnerFormValidators } from '../owner-form/owner-form.validators';
import { OwnerService } from '../owner.service';
import { TenantFormValidators } from '../tenant-create/tenant-form.validators';
import { TenantService } from '../tenant.service';
import { OwnerCreateComponent } from './owner-create.component';

@Component({
  selector: 'app-owner-form',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => OwnerFormStubComponent),
    multi: true,
  }]
})
class OwnerFormStubComponent implements ControlValueAccessor {
  @Input() customerId: any;
  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

let owner: Owner;

describe('OwnerCreateComponent', () => {
  let component: OwnerCreateComponent;
  let fixture: ComponentFixture<OwnerCreateComponent>;

  beforeEach(waitForAsync(() => {
    owner = {
      id: '5ad5f14c894e6a414edc7b67',
      identifier : '1',
      customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
      name: 'Mr Président',
      code: '02234512',
      companyName: 'Electricité de france',
      address: {
        street: '22-30 Avenue de WAGRAM',
        zipCode: '75008',
        city: 'Paris',
        country: 'France'
      },
      readonly : false
    };

    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const ownerServiceSpy = jasmine.createSpyObj('OwnerService', { create: of(owner) });
    const ownerFormValidatorsSpy = jasmine.createSpyObj('OwnerFormValidators', { uniqueCode: () => of(null) });
    const tenantFormValidatorsSpy = jasmine.createSpyObj('TenantFormValidators', {
      uniqueName: () => of(null)
    });
    const tenantServiceSpy = jasmine.createSpyObj('TenantService', { create: of({}) });

    TestBed.configureTestingModule({
      imports: [
        MatProgressBarModule,
        ReactiveFormsModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
      ],
      declarations: [
        OwnerCreateComponent,
        OwnerFormStubComponent,
      ],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: { customer: { id: '42', name: 'OwnerName' } } },
        { provide: OwnerService, useValue: ownerServiceSpy },
        { provide: OwnerFormValidators, useValue: ownerFormValidatorsSpy },
        { provide: TenantFormValidators, useValue: tenantFormValidatorsSpy },
        { provide: TenantService, useValue: tenantServiceSpy },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OwnerCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call dialogRef.close', () => {
    const matDialogRef =  TestBed.inject(MatDialogRef);
    component.onCancel();
    expect(matDialogRef.close).toHaveBeenCalledTimes(1);
  });

  it('should not call ownerService.create()', () => {
    const ownerService =  TestBed.inject(OwnerService);
    component.onOwnerSubmit();
    expect(ownerService.create).toHaveBeenCalledTimes(0);
  });

  it('should call ownerService.create()', () => {
    const ownerService =  TestBed.inject(OwnerService);
    const matDialogRef =  TestBed.inject(MatDialogRef);
    component.ownerForm.setValue({ owner });
    component.onOwnerSubmit();
    expect(ownerService.create).toHaveBeenCalledTimes(1);
    expect(matDialogRef.close).toHaveBeenCalledTimes(1);
  });

  it('should not call tenantService.create()', () => {
    const tenantService =  TestBed.inject(TenantService);
    component.onTenantSubmit();
    expect(tenantService.create).not.toHaveBeenCalled();
  });

  it('should call tenantService.create()', () => {
    const tenantService =  TestBed.inject(TenantService);
    const matDialogRef =  TestBed.inject(MatDialogRef);
    component.ownerForm.setValue({ owner });
    const tenant = { name: 'tenant name', ownerId: owner.id, customerId: '42', enabled: true };
    component.tenantForm.setValue(tenant);
    component.onTenantSubmit();
    expect(tenantService.create).toHaveBeenCalledWith(
      { name: tenant.name, ownerId: tenant.ownerId, customerId: tenant.customerId, enabled: tenant.enabled },
      owner.name
    );
    expect(matDialogRef.close).toHaveBeenCalledTimes(1);
  });
});
