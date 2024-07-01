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
import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY, of, timer } from 'rxjs';
import { map } from 'rxjs/operators';
import { BASE_URL, CountryService, LoggerModule, Owner, StartupService, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { OwnerService } from '../owner.service';
import { OwnerFormComponent } from './owner-form.component';
import { OwnerFormValidators } from './owner-form.validators';

@Component({
  template: `<app-owner-form [customerId]="customerId" [(ngModel)]="owner"></app-owner-form>`,
  standalone: true,
  imports: [MatSelectModule, ReactiveFormsModule, FormsModule, VitamUICommonTestModule, HttpClientTestingModule],
})
class TesthostComponent {
  owner: Owner = null;
  customerId = '4242';
  @ViewChild(OwnerFormComponent, { static: false }) ownerFormComponent: OwnerFormComponent;
}

let testhost: TesthostComponent;
let fixture: ComponentFixture<TesthostComponent>;

describe('OwnerFormComponent', () => {
  beforeEach(async () => {
    const ownerServiceSpy = jasmine.createSpyObj('OwnerService', { create: of({}) });
    const ownerFormValidatorsSpy = jasmine.createSpyObj('OwnerFormValidators', {
      uniqueCode: () => timer(10).pipe(map(() => null)),
    });

    await TestBed.configureTestingModule({
      imports: [
        MatSelectModule,
        ReactiveFormsModule,
        NoopAnimationsModule,
        FormsModule,
        VitamUICommonTestModule,
        HttpClientTestingModule,
        LoggerModule.forRoot(),
        OwnerFormComponent,
        TesthostComponent,
      ],
      providers: [
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: OwnerService, useValue: ownerServiceSpy },
        { provide: OwnerFormValidators, useValue: ownerFormValidatorsSpy },
        { provide: StartupService, useValue: { getConfigNumberValue: () => 100 } },
        { provide: CountryService, useValue: { getAvailableCountries: () => EMPTY } },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TesthostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should set owner to null', () => {
    expect(testhost.owner).toBe(null);
  });

  it('should set owner to a value', fakeAsync(() => {
    const owner: Owner = {
      id: null,
      identifier: null,
      customerId: '4242',
      code: '43214345345',
      name: 'Toto',
      companyName: 'Toto & Co.',
      address: {
        street: 'Street name',
        zipCode: '2134',
        city: 'Paris',
        country: 'FR',
      },
      internalCode: null,
      readonly: false,
    };

    testhost.ownerFormComponent.form.get('code').setValue(owner.code);
    testhost.ownerFormComponent.form.get('name').setValue(owner.name);
    testhost.ownerFormComponent.form.get('companyName').setValue(owner.companyName);
    testhost.ownerFormComponent.form.get('address.street').setValue(owner.address.street);
    testhost.ownerFormComponent.form.get('address.zipCode').setValue(owner.address.zipCode);
    testhost.ownerFormComponent.form.get('address.city').setValue(owner.address.city);
    testhost.ownerFormComponent.form.get('address.country').setValue(owner.address.country);
    tick(10);
    expect(testhost.owner).toEqual(owner);
  }));

  it('should set owner to null when the form not valid', () => {
    const owner: Owner = {
      id: null,
      identifier: null,
      customerId: '4242',
      code: 'invalid-code',
      name: 'Toto',
      companyName: 'Toto & Co.',
      address: {
        street: 'Street name',
        zipCode: '2134',
        city: 'Paris',
        country: 'FR',
      },
      readonly: false,
    };

    testhost.ownerFormComponent.form.get('code').setValue(owner.code);
    testhost.ownerFormComponent.form.get('name').setValue(owner.name);
    testhost.ownerFormComponent.form.get('companyName').setValue(owner.companyName);
    testhost.ownerFormComponent.form.get('address.street').setValue(owner.address.street);
    testhost.ownerFormComponent.form.get('address.zipCode').setValue(owner.address.zipCode);
    testhost.ownerFormComponent.form.get('address.city').setValue(owner.address.city);
    testhost.ownerFormComponent.form.get('address.country').setValue(owner.address.country);
    expect(testhost.owner).toBe(null);
  });

  it('should update the customerId', () => {
    testhost.customerId = '5050505050';
    fixture.detectChanges();
    expect(testhost.ownerFormComponent.form.value.customerId).toBe(testhost.customerId);
  });

  it('should emit a value when the status changes', fakeAsync(() => {
    const owner: Owner = {
      id: null,
      identifier: null,
      customerId: '4242',
      code: '43214345345',
      name: 'Toto',
      companyName: 'Toto & Co.',
      address: {
        street: 'Street name',
        zipCode: '2134',
        city: 'Paris',
        country: 'FR',
      },
      internalCode: null,
      readonly: false,
    };

    testhost.ownerFormComponent.form.get('code').setValue(owner.code);
    testhost.ownerFormComponent.form.get('name').setValue(owner.name);
    testhost.ownerFormComponent.form.get('companyName').setValue(owner.companyName);
    testhost.ownerFormComponent.form.get('address.street').setValue(owner.address.street);
    testhost.ownerFormComponent.form.get('address.zipCode').setValue(owner.address.zipCode);
    testhost.ownerFormComponent.form.get('address.city').setValue(owner.address.city);
    testhost.ownerFormComponent.form.get('address.country').setValue(owner.address.country);
    expect(testhost.ownerFormComponent.form.valid).toBe(false);
    expect(testhost.owner).toBe(null);
    tick(10);
    expect(testhost.ownerFormComponent.form.valid).toBe(true);
    expect(testhost.owner).toEqual(owner);
  }));
});
