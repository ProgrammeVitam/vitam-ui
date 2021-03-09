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

import { Component, forwardRef, Input, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AsyncValidator, ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule, Validator } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { Customer, OtpState } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { CustomerService } from '../../../core/customer.service';
import { CustomerCreateValidators } from '../../customer-create/customer-create.validators';
import { InformationTabComponent } from './information-tab.component';

let expectedCustomer: Customer = {
    id: '11',
    identifier : '11',
    code: '011000',
    name: 'Kouygues Telecom',
    companyName: 'Kouygues Telecom',
    enabled: true,
    readonly: false,
    hasCustomGraphicIdentity: false,
    language: 'FRENCH',
    passwordRevocationDelay: 1,
    otp: OtpState.OPTIONAL,
    idp: false,
    emailDomains: [
        'kouygues.com',
    ],
    defaultEmailDomain: 'kouygues.com',
    address: {
        street: '13 rue faubourg',
        zipCode: '75009',
        city: 'paris',
        country: 'france'
    },
    internalCode: '1',
    owners: [],
    themeColors: {},
    gdprAlert : false,
    gdprAlertDelay : 72
};

@Component({
  selector: 'app-editable-domain-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => EditableDomainInputStubComponent),
    multi: true
  }]
})
class EditableDomainInputStubComponent implements ControlValueAccessor {
  @Input() validator: Validator;
  @Input() asyncValidator: AsyncValidator;
  @Input() defaultDomain: string;

  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}
@Component({
  selector: 'app-customer-colors-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => CustomerColorsInputStubComponent),
    multi: true,
  }]
})
class CustomerColorsInputStubComponent implements ControlValueAccessor {
    @Input() placeholder: string;
    @Input() spinnerDiameter = 25;
    writeValue() {}
    registerOnChange() {}
    registerOnTouched() {}
}

@Component({
  template: `<app-information-tab [customer]="customer" [readOnly]="readOnly" [gdprReadOnlyStatus]="gdprReadOnlyStatus">></app-information-tab>`
})
class TestHostComponent {
    customer = expectedCustomer;
    readOnly = false;
    gdprReadOnlyStatus = false;

    @ViewChild(InformationTabComponent, { static: false }) component: InformationTabComponent;
}

describe('Customer InformationTabComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    expectedCustomer = {
      id: '11',
      identifier : '11',
      code: '011000',
      name: 'Kouygues Telecom',
      companyName: 'Kouygues Telecom',
      enabled: true,
      readonly: false,
      hasCustomGraphicIdentity: false,
      language: 'FRENCH',
      passwordRevocationDelay: 1,
      otp: OtpState.OPTIONAL,
      idp: false,
      emailDomains: [
        'kouygues.com',
      ],
      defaultEmailDomain: 'kouygues.com',
      address: {
        street: '13 rue faubourg',
        zipCode: '75009',
        city: 'paris',
        country: 'france'
      },
      internalCode: '1',
      owners: [],
      themeColors: {},
      gdprAlert : false,
      gdprAlertDelay : 72
    };
    const customerServiceSpy = jasmine.createSpyObj('CustomerService', { patch: of({}) });
    const customerCreateValidatorsSpy = jasmine.createSpyObj(
      'CustomerCreateValidators',
    { uniqueCode: () => of(null), uniqueDomain: () => of(null) }
    );

    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
      ],
      declarations: [
        InformationTabComponent,
        TestHostComponent,
        EditableDomainInputStubComponent,
        CustomerColorsInputStubComponent,
      ],
      providers: [
        { provide: CustomerService, useValue: customerServiceSpy },
        { provide: CustomerCreateValidators, useValue: customerCreateValidatorsSpy },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();

    testhost.customer = expectedCustomer;
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should have the correct fields', () => {
    expect(testhost.component.form.get('id')).not.toBeNull();
    expect(testhost.component.form.get('code')).not.toBeNull();
    expect(testhost.component.form.get('name')).not.toBeNull();
    expect(testhost.component.form.get('companyName')).not.toBeNull();
    expect(testhost.component.form.get('passwordRevocationDelay')).not.toBeNull();
    expect(testhost.component.form.get('otp')).not.toBeNull();
    expect(testhost.component.form.get('address.street')).not.toBeNull();
    expect(testhost.component.form.get('address.zipCode')).not.toBeNull();
    expect(testhost.component.form.get('address.city')).not.toBeNull();
    expect(testhost.component.form.get('address.country')).not.toBeNull();
    expect(testhost.component.form.get('internalCode')).not.toBeNull();
    expect(testhost.component.form.get('language')).not.toBeNull();
    expect(testhost.component.form.get('emailDomains')).not.toBeNull();
    expect(testhost.component.form.get('defaultEmailDomain')).not.toBeNull();
  });

  it('should have the required validator', () => {
    testhost.component.form.setValue({
      id: null,
      identifier: null,
      code: null,
      name: null,
      companyName: null,
      passwordRevocationDelay: null,
      otp: null,
      address: {
        street: null,
        zipCode: null,
        city: null,
        country: null
      },
      internalCode: null,
      language: null,
      emailDomains: null,
      defaultEmailDomain: null,
      gdprAlert : null,
      gdprAlertDelay : null
    });
    expect(testhost.component.form.get('id').valid).toBeFalsy('id');
    expect(testhost.component.form.get('code').valid).toBeFalsy('code');
    expect(testhost.component.form.get('name').valid).toBeFalsy('name');
    expect(testhost.component.form.get('companyName').valid).toBeFalsy('companyName');
    expect(testhost.component.form.get('passwordRevocationDelay').valid).toBeFalsy('passwordRevocationDelay');
    expect(testhost.component.form.get('otp').valid).toBeTruthy('otp');
    expect(testhost.component.form.get('address.street').valid).toBeFalsy('street');
    expect(testhost.component.form.get('address.zipCode').valid).toBeFalsy('zipCode');
    expect(testhost.component.form.get('address.city').valid).toBeFalsy('city');
    expect(testhost.component.form.get('address.country').valid).toBeFalsy('country');
    expect(testhost.component.form.get('language').valid).toBeFalsy('language');
    expect(testhost.component.form.get('emailDomains').valid).toBeFalsy('emailDomains');
    expect(testhost.component.form.get('defaultEmailDomain').valid).toBeFalsy('defaultEmailDomain');
  });

  it('should have the pattern validator', () => {
    const codeControl = testhost.component.form.get('code');
    codeControl.setValue('a');
    expect(codeControl.valid).toBeFalsy();
    codeControl.setValue('123456a');
    expect(codeControl.valid).toBeFalsy();
    codeControl.setValue('aaaaaa');
    expect(codeControl.valid).toBeFalsy();
    codeControl.setValue('1234');
    expect(codeControl.valid).toBeTruthy();
  });

  it('should be valid and call update()', () => {
    testhost.component.form.setValue({
      id: expectedCustomer.id,
      identifier: expectedCustomer.identifier,
      code: expectedCustomer.code,
      name: expectedCustomer.name,
      companyName: expectedCustomer.companyName,
      passwordRevocationDelay: expectedCustomer.passwordRevocationDelay,
      otp: expectedCustomer.otp,
      address: {
        street: expectedCustomer.address.street,
        zipCode: expectedCustomer.address.zipCode,
        city: expectedCustomer.address.city,
        country: expectedCustomer.address.country,
      },
      internalCode: expectedCustomer.internalCode,
      language: expectedCustomer.language,
      emailDomains: expectedCustomer.emailDomains,
      defaultEmailDomain: expectedCustomer.defaultEmailDomain,
      gdprAlert : expectedCustomer.gdprAlert,
      gdprAlertDelay : expectedCustomer.gdprAlertDelay
    });
    expect(testhost.component.form.valid).toBeTruthy();
  });

  it('should disable then enable the form', () => {
    testhost.readOnly = true;
    fixture.detectChanges();
    expect(testhost.component.form.disabled).toBe(true);
    testhost.readOnly = false;
    fixture.detectChanges();
    expect(testhost.component.form.disabled).toBe(false);
  });
});
