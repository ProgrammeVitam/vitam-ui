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
/* eslint-disable max-classes-per-file, @angular-eslint/directive-selector */
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, EventEmitter, forwardRef, Input, NO_ERRORS_SCHEMA, Output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY, of } from 'rxjs';
import { BASE_URL, ConfirmDialogService, CountryService, LoggerModule, OtpState, StartupService, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { CustomerService } from '../../core/customer.service';
import { OwnerFormValidators } from '../owner-form/owner-form.validators';
import { OwnerService } from '../owner.service';
import { TenantFormValidators } from '../tenant-create/tenant-form.validators';
import { TenantService } from '../tenant.service';
import { CustomerCreateComponent } from './customer-create.component';
import { CustomerCreateValidators } from './customer-create.validators';
import { TranslateModule } from '@ngx-translate/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

@Component({
  selector: 'app-domains-input',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DomainInputStubComponent),
      multi: true,
    },
  ],
  standalone: false,
})
class DomainInputStubComponent implements ControlValueAccessor {
  @Input()
  placeholder: string;
  @Input()
  selected: string;
  @Input()
  spinnerDiameter = 25;

  @Output()
  selectedChange = new EventEmitter<string>();

  writeValue() {}

  registerOnChange() {}

  registerOnTouched() {}
}

@Component({
  selector: 'app-owner-form',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => OwnerFormStubComponent),
      multi: true,
    },
  ],
  standalone: false,
})
class OwnerFormStubComponent implements ControlValueAccessor {
  @Input()
  customerInfo: any;

  writeValue() {}

  registerOnChange() {}

  registerOnTouched() {}
}

@Component({
  selector: 'app-customer-colors-input',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CustomerColorsInputStubComponent),
      multi: true,
    },
  ],
  standalone: false,
})
class CustomerColorsInputStubComponent implements ControlValueAccessor {
  @Input()
  placeholder: string;
  @Input()
  spinnerDiameter = 25;

  writeValue() {}

  registerOnChange() {}

  registerOnTouched() {}
}

const expectedCustomer = {
  enabled: true,
  code: '424242',
  name: 'John Doe',
  companyName: 'John Co.',
  hasCustomGraphicIdentity: false,
  passwordRevocationDelay: 3,
  otp: OtpState.OPTIONAL,
  address: {
    street: 'street',
    zipCode: '12345',
    city: 'New York',
    country: 'US',
  },
  internalCode: '1',
  language: 'en',
  emailDomains: ['test.com', 'toto.co.uk'],
  defaultEmailDomain: 'test.com',
  owners: [
    {
      code: '666666',
      name: 'Alice Vans',
      companyName: 'Vans',
      address: {
        street: 'street2',
        zipCode: '43121',
        city: 'Paris',
        country: 'FR',
      },
    },
  ],
  themeColors: {},
  gdprAlert: true,
  gdprAlertDelay: 72,
  portalMessages: {},
  portalTitles: {},
  tenantName: 'tenantName',
  tenantId: 2,
};

let component: CustomerCreateComponent;
let fixture: ComponentFixture<CustomerCreateComponent>;

class Page {
  get submit() {
    return fixture.nativeElement.querySelector('button[type=submit]');
  }

  control(name: string) {
    return fixture.nativeElement.querySelector('[formControlName=' + name + ']');
  }
}

let page: Page;

describe('CustomerCreateComponent', () => {
  beforeEach(async () => {
    const matDialogRefSpy = {
      close: vi.fn().mockName('MatDialogRef.close'),
    };
    const customerServiceSpy = {
      create: vi.fn().mockName('CustomerService.create').mockReturnValue(of({})),
      getMyCustomer: vi.fn().mockName('CustomerService.getMyCustomer').mockReturnValue(of({})),
    };
    const customerCreateValidatorsSpy = {
      uniqueCode: vi
        .fn()
        .mockName('CustomerCreateValidators.uniqueCode')
        .mockReturnValue(() => of(null)),
      uniqueDomain: vi.fn().mockName('CustomerCreateValidators.uniqueDomain').mockReturnValue(of(null)),
    };
    const ownerServiceSpy = {
      create: vi.fn().mockName('OwnerService.create').mockReturnValue(of({})),
    };
    const ownerFormValidatorsSpy = {
      uniqueCode: vi
        .fn()
        .mockName('OwnerFormValidators.uniqueCode')
        .mockReturnValue(() => of(null)),
    };
    const tenantServiceSpy = {
      getTenantsByCustomerIds: vi.fn().mockName('TenantService.getTenantsByCustomerIds').mockReturnValue(of([])),
      getAvailableTenants: vi
        .fn()
        .mockName('TenantService.getAvailableTenants')
        .mockReturnValue(of([2, 3, 4])),
    };
    const tenantFormValidatorsSpy = {
      uniqueName: vi
        .fn()
        .mockName('TenantFormValidators.uniqueName')
        .mockReturnValue(() => of(null)),
    };
    await TestBed.configureTestingModule({
      declarations: [CustomerCreateComponent, OwnerFormStubComponent, CustomerColorsInputStubComponent, DomainInputStubComponent],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        LoggerModule.forRoot(),
        MatButtonToggleModule,
        MatFormFieldModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        NoopAnimationsModule,
        ReactiveFormsModule,
        TranslateModule.forRoot(),
        VitamUICommonTestModule,
      ],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: StartupService, useValue: { getConfigNumberValue: () => 100 } },
        { provide: CustomerService, useValue: customerServiceSpy },
        { provide: CustomerCreateValidators, useValue: customerCreateValidatorsSpy },
        { provide: OwnerService, useValue: ownerServiceSpy },
        { provide: OwnerFormValidators, useValue: ownerFormValidatorsSpy },
        { provide: TenantService, useValue: tenantServiceSpy },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
        { provide: TenantFormValidators, useValue: tenantFormValidatorsSpy },
        { provide: CountryService, useValue: { getAvailableCountries: () => EMPTY } },
        { provide: MatDialog, useValue: {} },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    })
      .overrideComponent(CustomerCreateComponent, {
        set: {
          template: `
            <form [formGroup]="form">
              <input formControlName="code" />
              <input formControlName="name" />
              <input formControlName="companyName" />
              <ng-container formGroupName="address">
                <input formControlName="street" />
                <input formControlName="zipCode" />
                <input formControlName="city" />
                <input formControlName="country" />
              </ng-container>
              <input formControlName="internalCode" />
              <input formControlName="gdprAlert" />
              <input formControlName="gdprAlertDelay" />
              <input formControlName="language" />
              <input formControlName="passwordRevocationDelay" />
              <input formControlName="otp" />
              <input formControlName="emailDomains" />
              <input formControlName="defaultEmailDomain" />
              <input formControlName="hasCustomGraphicIdentity" />
              <input formControlName="themeColors" />
              <input formControlName="portalTitles" />
              <input formControlName="portalMessages" />
              <ng-container formArrayName="owners">
                <input [formControlName]="0" />
              </ng-container>
              <input formControlName="tenantName" />
              <input formControlName="tenantId" />
              <button type="submit" [disabled]="form.invalid"></button>
            </form>
          `,
        },
      })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Template', () => {
    it('should have the right inputs', () => {
      expect(page.control('code')).toBeTruthy();
      expect(page.control('name')).toBeTruthy();
      expect(page.control('companyName')).toBeTruthy();
      expect(page.control('street')).toBeTruthy();
      expect(page.control('zipCode')).toBeTruthy();
      expect(page.control('city')).toBeTruthy();
      expect(page.control('country')).toBeTruthy();
      expect(page.control('language')).toBeTruthy();
      expect(page.control('passwordRevocationDelay')).toBeTruthy();
      expect(page.control('otp')).toBeTruthy();
      expect(page.control('emailDomains')).toBeTruthy();
    });
  });

  describe('Form', () => {
    it('should be invalid when empty', () => {
      expect(component.form.invalid).toBeTruthy();
    });

    it('should be valid', () => {
      component.form.setValue(expectedCustomer);
      expect(component.form.valid).toBeTruthy();
    });

    describe('Validators', () => {
      describe('code', () => {
        it('should check the code format', () => {
          expect(setControlValue('code', '').invalid).toBeTruthy();
          expect(setControlValue('code', 'A1A1AazZ').valid).toBeTruthy();
          expect(setControlValue('code', 'A1A1AazZ_').invalid).toBeTruthy();
          expect(setControlValue('code', '12345678901234567890123455').invalid).toBeTruthy();
          expect(setControlValue('code', '000000000').valid).toBeTruthy();
          expect(setControlValue('code', '999999').valid).toBeTruthy();
        });
      });

      describe('fields', () => {
        it('should be required', () => {
          expect(setControlValue('name', '').invalid).toBeTruthy();
          expect(setControlValue('name', 'n').valid).toBeTruthy();

          expect(setControlValue('companyName', '').invalid).toBeTruthy();
          expect(setControlValue('companyName', 't').valid).toBeTruthy();

          expect(setControlValue('address.street', '').invalid).toBeTruthy();
          expect(setControlValue('address.street', 't').valid).toBeTruthy();

          expect(setControlValue('address.zipCode', '').invalid).toBeTruthy();
          expect(setControlValue('address.zipCode', 't').valid).toBeTruthy();

          expect(setControlValue('address.city', '').invalid).toBeTruthy();
          expect(setControlValue('address.city', 't').valid).toBeTruthy();

          expect(setControlValue('address.country', '').invalid).toBeTruthy();
          expect(setControlValue('address.country', 't').valid).toBeTruthy();

          expect(setControlValue('language', '').invalid).toBeTruthy();
          expect(setControlValue('language', 't').valid).toBeTruthy();

          expect(setControlValue('emailDomains', '').invalid).toBeTruthy();
          expect(setControlValue('emailDomains', 't').valid).toBeTruthy();

          expect(setControlValue('defaultEmailDomain', '').invalid).toBeTruthy();
          expect(setControlValue('defaultEmailDomain', 't').valid).toBeTruthy();
        });
      });

      function setControlValue(name: string | Array<string | number>, value: any) {
        const control = component.form.get(name);
        control.setValue(value);

        return control;
      }
    });
  });

  describe('Component', () => {
    it('should call dialogRef.close', () => {
      const matDialogRef = TestBed.inject(MatDialogRef);
      component.onCancel();
      expect(matDialogRef.close).toHaveBeenCalledTimes(1);
    });

    it('should not call create()', () => {
      const customerService = TestBed.inject(CustomerService);
      component.onSubmit();
      expect(customerService.create).toHaveBeenCalledTimes(0);
    });
  });
});
