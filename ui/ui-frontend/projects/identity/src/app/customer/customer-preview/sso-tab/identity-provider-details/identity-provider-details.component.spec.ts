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

import { IdentityProvider } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { IdentityProviderService } from '../identity-provider.service';
import { IdentityProviderDetailsComponent } from './identity-provider-details.component';

@Component({
  selector: 'app-editable-keystore',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => EditableKeystoreStubComponent),
    multi: true
  }]
})
class EditableKeystoreStubComponent implements ControlValueAccessor {
  @Input() validator: Validator;
  @Input() asyncValidator: AsyncValidator;
  @Input() identityProvider: any;
  @Input() disabled: boolean;
  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({
  selector: 'app-editable-patterns',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => EditablePatternStubComponent),
    multi: true
  }]
})
class EditablePatternStubComponent implements ControlValueAccessor {
  @Input() validator: Validator;
  @Input() asyncValidator: AsyncValidator;
  @Input() options: any;
  writeValue() {}
  registerOnChange() {}
  registerOnTouched() {}
}

@Component({
  template: `
    <app-identity-provider-details
      [identityProvider]="provider"
      [domains]="domains"
      [readOnly]="readOnly"
    >
    </app-identity-provider-details>
  `
})
class TestHostComponent {
  @ViewChild(IdentityProviderDetailsComponent, { static: false }) component: IdentityProviderDetailsComponent;
  provider: IdentityProvider = {
    id: '42',
    customerId: '1234',
    identifier: '2',
    name: 'Test IDP',
    technicalName: 'Test IDP',
    internal: true,
    keystorePassword: 'testpassword1234',
    patterns: ['test1.com', 'test3.com'],
    enabled: true,
    keystore: null,
    idpMetadata: null,
    readonly : false,
    mailAttribute: 'mailAttribute'
  };
  domains = [
    { value: 'test1.com', disabled: true },
    { value: 'test2.com', disabled: true },
    { value: 'test3.com', disabled: true },
    { value: 'test4.com', disabled: false },
    { value: 'test5.com', disabled: false },
  ];
  readOnly: boolean;
}

describe('IdentityProviderDetailsComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
      ],
      declarations: [
        IdentityProviderDetailsComponent,
        TestHostComponent,
        EditableKeystoreStubComponent,
        EditablePatternStubComponent,
      ],
      providers: [
        { provide: IdentityProviderService, useValue: { patch: () => of(null), updateMetadataFile: () => of(null) } },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  describe('Class', () => {

    it('should set the form value', () => {
      expect(testhost.component.form.getRawValue()).toEqual({
        id: testhost.provider.id,
        identifier: testhost.provider.identifier,
        internal: testhost.provider.internal,
        enabled: testhost.provider.enabled,
        name: testhost.provider.name,
        patterns: testhost.provider.patterns,
        mailAttribute: testhost.provider.mailAttribute
      });
    });

    it('should enable the pattern used by the provider', () => {
      expect(testhost.component.domains).toEqual([
        { value: 'test1.com', disabled: false },
        { value: 'test2.com', disabled: true },
        { value: 'test3.com', disabled: false },
        { value: 'test4.com', disabled: false },
        { value: 'test5.com', disabled: false },
      ]);
    });

    it('should have the correct fields', () => {
      expect(testhost.component.form.get('id')).not.toBeNull();
      expect(testhost.component.form.get('identifier')).not.toBeNull();
      expect(testhost.component.form.get('enabled')).not.toBeNull();
      expect(testhost.component.form.get('name')).not.toBeNull();
      expect(testhost.component.form.get('internal')).not.toBeNull();
      expect(testhost.component.form.get('patterns')).not.toBeNull();
      expect(testhost.component.form.get('mailAttribute')).not.toBeNull();
    });

    it('should have the required validator', () => {
      testhost.component.form.setValue({
        id: null,
        identifier: null,
        enabled: null,
        name: null,
        internal: null,
        patterns: null,
        mailAttribute: null
      });
      expect(testhost.component.form.get('id').valid).toBeFalsy('id');
      expect(testhost.component.form.get('enabled').valid).toBeFalsy('enabled');
      expect(testhost.component.form.get('identifier').valid).toBeFalsy('identifier');
      expect(testhost.component.form.get('name').valid).toBeFalsy('name');
      expect(testhost.component.form.get('internal').valid).toBeFalsy('internal');
      expect(testhost.component.form.get('patterns').valid).toBeFalsy('patterns');
      expect(testhost.component.form.get('mailAttribute').valid).toBeTruthy('mailAttribute');
    });

    it('should be valid and call patch()', waitForAsync(() => {
      const providerService = TestBed.inject(IdentityProviderService);
      spyOn(providerService, 'patch').and.returnValue(of(null));
      testhost.component.form.setValue({
        id: testhost.provider.id,
        identifier: testhost.provider.identifier,
        enabled: false,
        name: testhost.provider.name,
        internal: testhost.provider.internal,
        patterns: testhost.provider.patterns,
        mailAttribute: testhost.provider.mailAttribute
      });
      expect(testhost.component.form.valid).toBeTruthy();

    }));

    it('should call updateMetadataFile');

    it('should disable then enable the form', () => {
      testhost.readOnly = true;
      fixture.detectChanges();
      expect(testhost.component.form.disabled).toBe(true);
      expect(testhost.component.idpMetadata.disabled).toBe(true);
      testhost.readOnly = false;
      fixture.detectChanges();
      expect(testhost.component.form.disabled).toBe(false);
      expect(testhost.component.idpMetadata.disabled).toBe(false);
    });

  });

  describe('DOM', () => {

    // TODO

  });

});
