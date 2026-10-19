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
import { Component, computed, inject, OnChanges, OnInit, input, signal, SimpleChanges } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AsyncValidatorFn, ValidatorFn, Validators } from '@angular/forms';
import { disabled, form, FormField, maxLength, min, pattern, required, validate } from '@angular/forms/signals';
import { of } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import {
  CountryOption,
  CountryService,
  Customer,
  diff,
  EditableButtonToggleComponent,
  EditableInputComponent,
  EditableToggleGroupComponent,
  FormFieldValueWrapperComponent,
  Option,
  OtpState,
  requiredValue,
  SelectComponent,
  SlideToggleComponent,
  StartupService,
  VitamUIFieldErrorComponent,
} from 'vitamui-library';

import { CustomerService } from '../../../core/customer.service';
import { ALPHA_NUMERIC_REGEX, CUSTOMER_CODE_MAX_LENGTH, CustomerCreateValidators } from '../../customer-create/customer-create.validators';
import { EditableDomainInputComponent } from '../../../shared/editable-field/editable-domain-input/editable-domain-input.component';
import { TranslatePipe } from '@ngx-translate/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';

const UPDATE_DEBOUNCE_TIME = 200;
const GDPR_ALERT_DELAY_PATTERN = /^[0-9]{1,20}$/;

interface CustomerInformationModel {
  id: string;
  identifier: string;
  code: string;
  name: string;
  companyName: string;
  passwordRevocationDelay: number;
  otp: OtpState;
  address: {
    street: string;
    zipCode: string;
    city: string;
    country: string;
  };
  internalCode: string;
  language: string;
  emailDomains: string[];
  defaultEmailDomain: string;
  gdprAlert: boolean;
  gdprAlertDelay: number;
}

function toCustomerInformationModel(customer: Customer): CustomerInformationModel {
  return {
    id: customer?.id ?? null,
    identifier: customer?.identifier ?? null,
    code: customer?.code ?? null,
    name: customer?.name ?? null,
    companyName: customer?.companyName ?? null,
    passwordRevocationDelay: customer?.passwordRevocationDelay ?? null,
    otp: customer?.otp ?? null,
    address: {
      street: customer?.address?.street ?? null,
      zipCode: customer?.address?.zipCode ?? null,
      city: customer?.address?.city ?? null,
      country: customer?.address?.country ?? null,
    },
    internalCode: customer?.internalCode ?? null,
    language: customer?.language ?? null,
    emailDomains: customer?.emailDomains ?? null,
    defaultEmailDomain: customer?.defaultEmailDomain ?? null,
    gdprAlert: customer?.gdprAlert ?? null,
    gdprAlertDelay: customer?.gdprAlertDelay ?? null,
  };
}

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss'],
  imports: [
    FormField,
    VitamUIFieldErrorComponent,
    FormFieldValueWrapperComponent,
    SelectComponent,
    EditableDomainInputComponent,
    SlideToggleComponent,
    TranslatePipe,
    CommonModule,
    EditableButtonToggleComponent,
    EditableInputComponent,
    EditableToggleGroupComponent,
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    OverlayModule,
  ],
})
export class InformationTabComponent implements OnChanges, OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly customerCreateValidators = inject(CustomerCreateValidators);
  private readonly countryService = inject(CountryService);
  private readonly startupService = inject(StartupService);

  public maxStreetLength: number = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
  public customerCodeMaxLength = CUSTOMER_CODE_MAX_LENGTH;
  previousValue: CustomerInformationModel;

  readonly customer = input<Customer>();

  readonly readOnly = input<boolean>();

  readonly gdprReadOnlyStatus = input<boolean>();

  public countries: Option[];

  // Code of the displayed customer, ignored by the unique code validators
  private readonly customerCode = signal<string>(undefined);
  private readonly model = signal<CustomerInformationModel>(toCustomerInformationModel(null));
  // Value set by resetForm: like a reset with emitEvent: false, it must not trigger an update
  private resetValue = this.model();
  public readonly form = form(this.model, (path) => {
    disabled(path, () => !!this.readOnly());
    required(path.id);
    // The identifier is never editable
    disabled(path.identifier);
    required(path.identifier);
    maxLength(path.identifier, 12);
    required(path.code);
    pattern(path.code, ALPHA_NUMERIC_REGEX);
    maxLength(path.code, this.customerCodeMaxLength);
    this.customerCreateValidators.uniqueCodeRule(path.code, () => this.customerCode());
    required(path.name);
    maxLength(path.name, 100);
    required(path.companyName);
    maxLength(path.companyName, 250);
    required(path.passwordRevocationDelay);
    required(path.address.street);
    maxLength(path.address.street, this.maxStreetLength);
    required(path.address.zipCode);
    maxLength(path.address.zipCode, 10);
    required(path.address.city);
    maxLength(path.address.city, 100);
    required(path.address.country);
    maxLength(path.internalCode, 20);
    required(path.language);
    requiredValue(path.emailDomains);
    required(path.defaultEmailDomain);
    disabled(path.gdprAlert, () => !!this.gdprReadOnlyStatus());
    disabled(path.gdprAlertDelay, () => !!this.gdprReadOnlyStatus());
    required(path.gdprAlertDelay);
    min(path.gdprAlertDelay, 1);
    validate(path.gdprAlertDelay, ({ value }) => {
      const delay = value();
      return delay == null || String(delay) === '' || GDPR_ALERT_DELAY_PATTERN.test(String(delay)) ? undefined : { kind: 'pattern' };
    });
  });

  // Validators of the editable fields (they validate the value being edited, before it is set in the form)
  readonly codeValidator: ValidatorFn = Validators.compose([
    Validators.required,
    Validators.pattern(ALPHA_NUMERIC_REGEX),
    Validators.maxLength(this.customerCodeMaxLength),
  ]);
  readonly codeAsyncValidator = computed<AsyncValidatorFn>(() => this.customerCreateValidators.uniqueCode(this.customerCode()));
  readonly requiredValidator: ValidatorFn = Validators.required;
  readonly streetValidator: ValidatorFn = Validators.compose([Validators.required, Validators.maxLength(this.maxStreetLength)]);
  readonly gdprAlertDelayValidator: ValidatorFn = Validators.compose([
    Validators.required,
    Validators.min(1),
    Validators.pattern(GDPR_ALERT_DELAY_PATTERN),
  ]);

  constructor() {
    toObservable(computed(() => ({ value: this.model(), valid: this.form().valid() })))
      .pipe(
        filter(({ value }) => value !== this.resetValue),
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.form().valid()),
        map(() => diff(this.model(), this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.customer().id }, formData)),
        switchMap((formData) => this.customerService.patch(formData).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(),
      )
      .subscribe((customer: Customer) => this.resetForm(customer));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['customer']) {
      this.resetForm(this.customer());
    }
  }

  ngOnInit() {
    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
    });
  }

  private resetForm(customer: Customer) {
    if (customer) {
      this.customerCode.set(customer.code);
    }
    this.resetValue = toCustomerInformationModel(customer);
    this.model.set(this.resetValue);
    this.form().reset();
    this.previousValue = this.resetValue;
  }
}
