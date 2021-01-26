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
import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDividerModule } from '@angular/material/divider';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, Subject } from 'rxjs';

import { Owner, Tenant } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { VitamUISnackBar } from '../../../shared/vitamui-snack-bar';
import { OwnerFormValidators } from '../../owner-form/owner-form.validators';
import { OwnerService } from '../../owner.service';
import { TenantFormValidators } from '../../tenant-create/tenant-form.validators';
import { TenantService } from '../../tenant.service';
import { InformationTabComponent } from './information-tab.component';

const expectedOwner: Owner = {
  id: '5ad5f14c894e6a414edc7b63',
  identifier: '1',
  customerId: '42',
  name: 'Julien Cornille',
  code: '10234665',
  companyName: 'vitamui',
  address: {
    street: '73 rue du Faubourg Poissonnière ',
    zipCode: '75009',
    city: 'Paris',
    country: 'France'
  },
  internalCode: '1',
  readonly : false
};

const expectedTenant: Tenant = {
  id: '5ad5f14c894e6a414edc7b61adad48f0b8124fcda07b0ec1886c8d5d61c8f713',
  ownerId: '5ad5f14c894e6a414edc7b62',
  customerId: '42',
  name: 'Emmanuel Deviller',
  identifier: 7,
  enabled: true,
  proof: true,
  readonly : false,
  accessContractHoldingIdentifier: 'AC-000001',
  accessContractLogbookIdentifier: 'AC-000002',
  ingestContractHoldingIdentifier: 'IC-000001',
  itemIngestContractIdentifier: 'IC-000001'
};

const owner = {
  id: '5ad5f14c894e6a414edc7b62',
  identifier : '5ad5f14c894e6a414edc7b62',
  customerId: '42',
  name: 'Emmanuel Deviller',
  code: '10234501',
  companyName: 'vitamui',
  address: {
    street: '73 rue du Faubourg Poissonnière ',
    zipCode: '75009',
    city: 'Paris',
    country: 'France'
  },
  internalCode: '1',
  readonly : false
};

@Component({
  template: `
    <app-information-tab
      [owner]="owner"
      [tenant]="tenant"
      [readOnly]="readonly"
    ></app-information-tab>
  `
})
class TestHostComponent {
  tenant: Tenant;
  owner: Owner;
  readonly = false;

  @ViewChild(InformationTabComponent, { static: false }) component: InformationTabComponent;
}

describe('Owner InformationTabComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    const ownerServiceSpy = {
      get: () => of(owner),
      patch: () => of(owner),
      updated: new Subject()
    };
    const ownerFormValidatorsSpy = jasmine.createSpyObj('OwnerFormValidators', { uniqueCode: () => of(null) });
    const tenantFormValidatorsSpy = jasmine.createSpyObj('TenantFormValidators', {
      uniqueName: () => of(null)
    });
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatDividerModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
      ],
      declarations: [
        TestHostComponent,
        InformationTabComponent,
      ],
      providers: [
        { provide: OwnerService, useValue: ownerServiceSpy },
        { provide: OwnerFormValidators, useValue: ownerFormValidatorsSpy },
        { provide: TenantFormValidators, useValue: tenantFormValidatorsSpy },
        { provide: TenantService, useValue: { patch: () => of(expectedTenant) } },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  describe('Owner Form', () => {
    beforeEach(() => {
      testhost.owner = expectedOwner;
      fixture.detectChanges();
    });

    it('should have the correct fields', () => {
      expect(testhost.component.ownerForm.get('id')).toBeDefined();
      expect(testhost.component.ownerForm.get('customerId')).toBeDefined();
      expect(testhost.component.ownerForm.get('code')).toBeDefined();
      expect(testhost.component.ownerForm.get('name')).toBeDefined();
      expect(testhost.component.ownerForm.get('companyName')).toBeDefined();
      expect(testhost.component.ownerForm.get('address.street')).toBeDefined();
      expect(testhost.component.ownerForm.get('address.zipCode')).toBeDefined();
      expect(testhost.component.ownerForm.get('address.city')).toBeDefined();
      expect(testhost.component.ownerForm.get('address.country')).toBeDefined();
    });

    it('should have the required validator', () => {
      testhost.component.ownerForm.setValue({
        id: null,
        identifier: null,
        customerId: null,
        code: null,
        name: null,
        companyName: null,
        address: {
          street: null,
          zipCode: null,
          city: null,
          country: null
        },
        internalCode: null
      });
      expect(testhost.component.ownerForm.get('id').valid).toBeFalsy('id');
      expect(testhost.component.ownerForm.get('customerId').valid).toBeFalsy('customerId');
      expect(testhost.component.ownerForm.get('code').valid).toBeFalsy('code');
      expect(testhost.component.ownerForm.get('name').valid).toBeFalsy('name');
      expect(testhost.component.ownerForm.get('companyName').valid).toBeFalsy('companyName');
      expect(testhost.component.ownerForm.get('address.street').valid).toBeTruthy('street');
      expect(testhost.component.ownerForm.get('address.zipCode').valid).toBeTruthy('zipCode');
      expect(testhost.component.ownerForm.get('address.city').valid).toBeTruthy('city');
      expect(testhost.component.ownerForm.get('address.country').valid).toBeTruthy('country');
      expect(testhost.component.ownerForm.get('internalCode').valid).toBeTruthy('internalCode');
    });

    it('should have the pattern validator', () => {
      const codeControl = testhost.component.ownerForm.get('code');
      codeControl.setValue('a');
      expect(codeControl.valid).toBeFalsy();
      codeControl.setValue('123456a');
      expect(codeControl.valid).toBeFalsy();
      codeControl.setValue('aaaaaa');
      expect(codeControl.valid).toBeFalsy();
      codeControl.setValue('1234');
      expect(codeControl.valid).toBeFalsy();
    });

    it('should be valid and call patch()', () => {
      testhost.component.ownerForm.setValue({
        id: expectedOwner.id,
        identifier: expectedOwner.identifier,
        customerId: expectedOwner.customerId,
        code: expectedOwner.code,
        name: expectedOwner.name,
        companyName: expectedOwner.companyName,
        address: {
          street: expectedOwner.address.street,
          zipCode: expectedOwner.address.zipCode,
          city: expectedOwner.address.city,
          country: expectedOwner.address.country,
        },
        internalCode: expectedOwner.internalCode
      });
      expect(testhost.component.ownerForm.valid).toBeTruthy();
    });
  });

  describe('Tenant Form', () => {
    beforeEach(() => {
      testhost.tenant = expectedTenant;
      fixture.detectChanges();
    });

    it('should have the correct fields', () => {
      expect(testhost.component.tenantForm.get('id')).toBeDefined();
      expect(testhost.component.tenantForm.get('identifier')).toBeDefined();
      expect(testhost.component.tenantForm.get('customerId')).toBeDefined();
      expect(testhost.component.tenantForm.get('ownerId')).toBeDefined();
      expect(testhost.component.tenantForm.get('name')).toBeDefined();
      expect(testhost.component.tenantForm.get('enabled')).toBeDefined();
    });

    it('should have the required validator', () => {
      testhost.component.tenantForm.setValue({
        id: null,
        customerId: null,
        ownerId: null,
        identifier: null,
        name: null,
        enabled: null,
        ingestContractHoldingIdentifier: null,
        itemIngestContractIdentifier: null,
        accessContractHoldingIdentifier: null,
        accessContractLogbookIdentifier: null
      });
      expect(testhost.component.tenantForm.get('id').valid).toBeFalsy('id');
      expect(testhost.component.tenantForm.get('customerId').valid).toBeFalsy('customerId');
      expect(testhost.component.tenantForm.get('ownerId').valid).toBeFalsy('ownerId');
      expect(testhost.component.tenantForm.get('identifier').valid).toBeFalsy('identifier');
      expect(testhost.component.tenantForm.get('name').valid).toBeFalsy('name');
      expect(testhost.component.tenantForm.get('enabled').valid).toBeFalsy('name');
    });

    it('should be valid and call patch()', () => {
      testhost.component.tenantForm.setValue({
        id: expectedTenant.id,
        ownerId: expectedTenant.ownerId,
        customerId: expectedTenant.customerId,
        identifier: expectedTenant.identifier,
        name: expectedTenant.name,
        enabled: expectedTenant.enabled,
        ingestContractHoldingIdentifier: expectedTenant.ingestContractHoldingIdentifier,
        itemIngestContractIdentifier: expectedTenant.itemIngestContractIdentifier,
        accessContractHoldingIdentifier: expectedTenant.accessContractHoldingIdentifier,
        accessContractLogbookIdentifier: expectedTenant.accessContractLogbookIdentifier
      });
      expect(testhost.component.tenantForm.valid).toBeTruthy();
    });

  });
});
