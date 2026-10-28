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
import { ComponentType } from '@angular/cdk/portal';
import { Component, inject, OnDestroy, OnInit, TemplateRef, signal, ChangeDetectorRef, computed } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { FormGroup } from '@angular/forms';
import { applyEach, disabled, FieldTree, form, FormField, FormRoot, maxLength, min, pattern, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialog, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { finalize, Observable, Subscription } from 'rxjs';
import { filter, skip, tap } from 'rxjs/operators';
import {
  ConfirmDialogService,
  CountryOption,
  CountryService,
  Customer,
  DialogHeaderComponent,
  InputComponent,
  Logo,
  NextStepComponent,
  Option,
  OtpState,
  PreviousStepComponent,
  requiredValue,
  SelectComponent,
  SlideToggleComponent,
  StartupService,
  StepperComponent,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { CustomerService } from '../../core/customer.service';
import { TenantFormValidators } from '../tenant-create/tenant-form.validators';
import { CustomerAlertingComponent } from './customer-alerting/customer-alerting.component';
import { ALPHA_NUMERIC_REGEX, CUSTOMER_CODE_MAX_LENGTH, CustomerCreateValidators } from './customer-create.validators';
import { TenantService } from '../tenant.service';
import { CdkStep, CdkStepperNext } from '@angular/cdk/stepper';
import { NgTemplateOutlet } from '@angular/common';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { DomainsInputComponent } from '../../shared/domains-input/domains-input.component';
import { GraphicIdentityComponent } from '../customer-preview/graphic-identity-tab/graphic-identity/graphic-identity.component';
import {
  HomepageMessageComponent,
  HomepageMessageModel,
} from '../customer-preview/homepage-message-tab/homepage-message/homepage-message.component';
import { OwnerFormComponent, OwnerFormModel } from '../owner-form/owner-form.component';
import { TranslatePipe } from '@ngx-translate/core';
import { enabledValue } from '../../shared/signal-forms/signal-form-utils';

export interface CustomerCreateModel {
  gdprAlert: boolean;
  gdprAlertDelay: number;
  enabled: boolean;
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
  hasCustomGraphicIdentity: boolean;
  themeColors: { [key: string]: string };
  portalMessages: { [language: string]: string };
  portalTitles: { [language: string]: string };
  owners: OwnerFormModel[];
  tenantName: string;
  tenantId: string;
}

@Component({
  selector: 'app-customer-create',
  templateUrl: './customer-create.component.html',
  styleUrls: ['./customer-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormField,
    FormRoot,
    StepperComponent,
    CdkStep,
    MatDialogContent,
    InputComponent,
    SelectComponent,
    SlideToggleComponent,
    MatDialogActions,
    NextStepComponent,
    NgTemplateOutlet,
    MatButtonToggleGroup,
    MatButtonToggle,
    DomainsInputComponent,
    PreviousStepComponent,
    GraphicIdentityComponent,
    HomepageMessageComponent,
    OwnerFormComponent,
    CdkStepperNext,
    TranslatePipe,
  ],
})
export class CustomerCreateComponent implements OnInit, OnDestroy {
  private readonly cdr = inject(ChangeDetectorRef);
  dialogRef = inject<MatDialogRef<CustomerCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  private readonly customerService = inject(CustomerService);
  private readonly customerCreateValidators = inject(CustomerCreateValidators);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly tenantFormValidators = inject(TenantFormValidators);
  private readonly countryService = inject(CountryService);
  private readonly startupService = inject(StartupService);
  private readonly tenantService = inject(TenantService);
  private readonly matDialog = inject(MatDialog);

  public customerCodeMaxLength = CUSTOMER_CODE_MAX_LENGTH;
  public maxStreetLength: number = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
  public stepIndex = 0;
  public stepCount = 6;
  gdprReadOnlyStatus: boolean = this.data?.gdprReadOnlySettingStatus;
  private readonly model = signal<CustomerCreateModel>({
    gdprAlert: true,
    gdprAlertDelay: 72,
    enabled: true,
    code: null,
    name: null,
    companyName: null,
    passwordRevocationDelay: 6,
    otp: OtpState.OPTIONAL,
    address: {
      street: null,
      zipCode: null,
      city: null,
      country: 'FR',
    },
    internalCode: null,
    language: 'FRENCH',
    emailDomains: null,
    defaultEmailDomain: null,
    hasCustomGraphicIdentity: false,
    themeColors: null,
    portalMessages: null,
    portalTitles: null,
    owners: [null],
    tenantName: null,
    tenantId: null,
  });
  public readonly form = form(
    this.model,
    (path) => {
      disabled(path.gdprAlert, () => !!this.gdprReadOnlyStatus);
      required(path.gdprAlertDelay);
      min(path.gdprAlertDelay, 1);
      disabled(path.gdprAlertDelay, ({ valueOf }) => !!this.gdprReadOnlyStatus || !valueOf(path.gdprAlert));
      requiredValue(path.enabled);
      required(path.code);
      pattern(path.code, ALPHA_NUMERIC_REGEX);
      maxLength(path.code, this.customerCodeMaxLength);
      this.customerCreateValidators.uniqueCodeRule(path.code);
      required(path.name);
      maxLength(path.name, 100);
      required(path.companyName);
      maxLength(path.companyName, 250);
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
      applyEach(path.owners, (owner) => requiredValue(owner));
      required(path.tenantName);
      this.tenantFormValidators.uniqueNameRule(path.tenantName);
      required(path.tenantId);
    },
    { submission: { action: async () => this.onSubmit(), ignoreValidators: 'all' } },
  );
  public hasError = true;
  public message: string;
  public readonly isLoading = signal(false);
  public readonly customerInfo = computed(() => ({
    code: this.form.code().value(),
    name: this.form.name().value(),
    companyName: this.form.companyName().value(),
  }));

  public get customerForm(): FormGroup {
    return this._customerForm;
  }

  public set customerForm(form: FormGroup) {
    this._customerForm = form;
  }

  public logos: Logo[];
  public countries: Option[];
  public portalTitles: { [language: string]: string };
  public portalMessages: { [language: string]: string };

  public homepageMessageForm: FieldTree<HomepageMessageModel>;

  private subscription: Subscription;
  private _customerForm: FormGroup;
  // Once the GDPR alert has been disabled, each change of the GDPR alert resets its delay
  private resetGdprAlertDelayOnChange = false;

  customer: Customer;
  tenantOptions: VitamuiSelectOptions;

  constructor() {
    toObservable(computed(() => this.form.gdprAlert().value()))
      .pipe(skip(1))
      .subscribe((gdprAlert) => {
        if (this.resetGdprAlertDelayOnChange) {
          this.form.gdprAlertDelay().value.set(72);
        }
        if (!gdprAlert) {
          this.resetGdprAlertDelayOnChange = true;
          this.confirm(CustomerAlertingComponent).subscribe(() => this.form.gdprAlert().value.set(true));
        }
      });
  }

  ngOnInit() {
    this.customerService.getMyCustomer().subscribe((customerDetails) => {
      this.customer = customerDetails;
      this.cdr.markForCheck();
    });
    this.tenantService
      .getAvailableTenants()
      .pipe(
        tap((tenantIds) => {
          const ascendingSort = (a: number, b: number) => a - b;
          tenantIds.sort(ascendingSort);

          const [first, second, _rest] = tenantIds;
          const options = tenantIds.map((id) => ({ key: `${id}`, label: `${id}` }));
          const defaultTenant = `${second ? second : first}`;
          this.tenantOptions = { options };
          this.form.tenantId().value.set(defaultTenant);
          this.cdr.markForCheck();
        }),
      )
      .subscribe();
    this.subscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  getOwnerName(): string {
    const owner = this.form.owners[0]().value();
    return owner ? owner.name : '';
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  confirm(componentOrTemplateRef: TemplateRef<unknown> | ComponentType<unknown>): Observable<boolean> {
    return this.matDialog
      .open(componentOrTemplateRef)
      .beforeClosed()
      .pipe(filter((result) => !result));
  }

  onSubmit() {
    if (this.lastStepIsInvalid() || this.stepIndex !== this.stepCount - 1) {
      return;
    }
    this.isLoading.set(true);
    const customer: Customer = this.getCustomerFromForm();
    this.customerService
      .create(customer, this.logos)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: (error) => console.error(error),
      });
  }

  public getCustomerFromForm(): Customer {
    let customer: any = this.model();

    if (this.customerForm && this.customerForm.get('hasCustomGraphicIdentity').value === true) {
      customer = {
        // Disabled fields are excluded, as in the value of a reactive form
        ...enabledValue(this.form),
        ...{
          hasCustomGraphicIdentity: this.customerForm.get('hasCustomGraphicIdentity').value,
          themeColors: this.customerForm.get('themeColors').value,
        },
      };
    }

    if (this.homepageMessageForm) {
      customer = {
        ...customer,
        ...{
          id: this.homepageMessageForm.id().value(),
          portalTitles: this.portalTitles,
          portalMessages: this.portalMessages,
        },
      };
    }
    return customer;
  }

  isDurationNotValid(): boolean {
    if (this.form.gdprAlert().value()) {
      return this.form.gdprAlertDelay().invalid() || this.form.gdprAlertDelay().pending();
    }
    return false;
  }

  firstStepInvalid(): boolean {
    const fields = [
      this.form.code,
      this.form.name,
      this.form.companyName,
      this.form.address.street,
      this.form.address.zipCode,
      this.form.address.city,
      this.form.internalCode,
      this.form.address.country,
    ];
    return this.isDurationNotValid() || fields.some((field) => field().invalid() || field().pending());
  }

  secondStepInvalid(): boolean {
    return (
      this.form.passwordRevocationDelay().invalid() ||
      this.form.otp().invalid() ||
      this.form.emailDomains().invalid() ||
      this.form.defaultEmailDomain().invalid()
    );
  }

  public thirdStepValid(): boolean {
    return !this.customerForm || (this.customerForm && this.customerForm.valid);
  }

  public fourthStepValid(): boolean {
    return !!this.homepageMessageForm && this.homepageMessageForm().valid();
  }

  lastStepIsInvalid(): boolean {
    const invalid = this.firstStepInvalid() || this.secondStepInvalid() || !this.thirdStepValid() || !this.fourthStepValid();
    return this.form().pending() || this.form().invalid() || invalid || this.isLoading();
  }
}
