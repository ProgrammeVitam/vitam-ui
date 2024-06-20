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
import { ComponentType } from '@angular/cdk/portal';
import { Component, Inject, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { Observable, Subject, merge } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { ConfirmDialogService, CountryOption, CountryService, Customer, Logo, OtpState, StartupService } from 'vitamui-library';
import { CustomerService } from '../../core/customer.service';
import { TenantFormValidators } from '../tenant-create/tenant-form.validators';
import { CustomerAlertingComponent } from './customer-alerting/customer-alerting.component';
import { ALPHA_NUMERIC_REGEX, CUSTOMER_CODE_MAX_LENGTH, CustomerCreateValidators } from './customer-create.validators';

interface CustomerInfo {
  code: string;
  name: string;
  companyName: string;
}

@Component({
  selector: 'app-customer-create',
  templateUrl: './customer-create.component.html',
  styleUrls: ['./customer-create.component.scss'],
})
export class CustomerCreateComponent implements OnInit, OnDestroy {
  public customerCodeMaxLength = CUSTOMER_CODE_MAX_LENGTH;
  public maxStreetLength: number;
  public stepIndex = 0;
  public stepCount = 6;
  public form: FormGroup;
  public hasError = true;
  public message: string;
  public creating = false;
  public customerInfo: CustomerInfo = {
    code: null,
    name: null,
    companyName: null,
  };
  public get customerForm(): FormGroup {
    return this._customerForm;
  }
  public set customerForm(form: FormGroup) {
    this._customerForm = form;
  }
  public logos: Logo[];
  public countries: CountryOption[];
  public portalTitles: { [language: string]: string };
  public portalMessages: { [language: string]: string };
  public get homepageMessageForm(): FormGroup {
    return this._homepageMessageForm;
  }
  public set homepageMessageForm(form: FormGroup) {
    this._homepageMessageForm = form;
  }
  gdprReadOnlyStatus: boolean;
  private destroy = new Subject<void>();
  // tslint:disable-next-line: variable-name
  private _homepageMessageForm: FormGroup;
  // tslint:disable-next-line: variable-name
  private _customerForm: FormGroup;

  customer: Customer;

  constructor(
    public dialogRef: MatDialogRef<CustomerCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private customerService: CustomerService,
    private customerCreateValidators: CustomerCreateValidators,
    private confirmDialogService: ConfirmDialogService,
    private tenantFormValidators: TenantFormValidators,
    private countryService: CountryService,
    private startupService: StartupService,
    private matDialog: MatDialog,
  ) {
    this.maxStreetLength = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
    this.form = this.formBuilder.group({
      gdprAlert: true,
      gdprAlertDelay: [72, Validators.min(1)],
      enabled: [true, Validators.required],
      code: [
        null,
        [Validators.required, Validators.pattern(ALPHA_NUMERIC_REGEX), Validators.maxLength(this.customerCodeMaxLength)],
        this.customerCreateValidators.uniqueCode(),
      ],
      name: [null, Validators.required],
      companyName: [null, Validators.required],
      passwordRevocationDelay: 6,
      otp: OtpState.OPTIONAL,
      address: this.formBuilder.group({
        street: [null, [Validators.required, Validators.maxLength(this.maxStreetLength)]],
        zipCode: [null, Validators.required],
        city: [null, Validators.required],
        country: ['FR', Validators.required],
      }),
      internalCode: [null],
      language: ['FRENCH', Validators.required],
      emailDomains: [null, Validators.required],
      defaultEmailDomain: [null, Validators.required],
      hasCustomGraphicIdentity: false,
      themeColors: [null],
      portalMessages: [null],
      portalTitles: [null],
      owners: this.formBuilder.array([this.formBuilder.control(null, Validators.required)]),
      tenantName: [null, [Validators.required], this.tenantFormValidators.uniqueName()],
    });

    if (this.data) {
      this.gdprReadOnlyStatus = this.data.gdprReadOnlySettingStatus;
    }

    this.form.get('gdprAlert').valueChanges.subscribe(() => {
      if (!this.form.get('gdprAlert').value) {
        this.confirm(CustomerAlertingComponent).subscribe(() => this.form.get('gdprAlert').setValue(true));
      }
    });
  }

  ngOnInit() {
    this.onChanges();
    this.customerService.getMyCustomer().subscribe((customerDetails) => {
      this.customer = customerDetails;
    });

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.destroy))
      .subscribe(() => this.onCancel());

    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values;
    });
  }

  ngOnDestroy() {
    this.destroy.next();
  }

  onChanges() {
    merge(this.form.get('code').valueChanges, this.form.get('name').valueChanges, this.form.get('companyName').valueChanges).subscribe(
      () => {
        this.customerInfo = {
          code: this.form.get('code').value,
          name: this.form.get('name').value,
          companyName: this.form.get('companyName').value,
        };
      },
    );
  }

  getOwnerName(): string {
    return this.form.get(['owners', 0]).value ? this.form.get(['owners', 0]).value.name : '';
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  confirm(componentOrTemplateRef: TemplateRef<unknown> | ComponentType<unknown>): Observable<boolean> {
    return this.matDialog
      .open(componentOrTemplateRef, { panelClass: 'vitamui-dialog' })
      .beforeClosed()
      .pipe(filter((result) => !result));
  }

  onSubmit() {
    if (this.lastStepIsInvalid() || this.stepIndex !== this.stepCount - 1) {
      return;
    }
    this.creating = true;
    const customer: Customer = this.getCustomerFromForm();

    this.customerService
      .create(customer, this.logos)
      .pipe(takeUntil(this.destroy))
      .subscribe(
        () => {
          this.dialogRef.close(true);
        },
        (error) => {
          this.creating = false;
          console.error(error);
        },
      );
  }

  public getCustomerFromForm(): Customer {
    let customer = this.form.value;

    if (this.customerForm && this.customerForm.get('hasCustomGraphicIdentity').value === true) {
      customer = {
        ...this.form.value,
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
          id: this.homepageMessageForm.get('id').value,
          portalTitles: this.portalTitles,
          portalMessages: this.portalMessages,
        },
      };
    }

    return customer;
  }

  isDurationNotValid(): boolean {
    if (this.form.get('gdprAlert').value) {
      return this.form.get('gdprAlertDelay').invalid || this.form.get('gdprAlertDelay').pending;
    } else {
      this.form.get('gdprAlert').valueChanges.subscribe(() => {
        this.form.get('gdprAlertDelay').setValue(72);
      });
      return false;
    }
  }

  firstStepInvalid(): boolean {
    return (
      this.form.get('code').invalid ||
      this.form.get('code').pending ||
      this.form.get('name').invalid ||
      this.form.get('name').pending ||
      this.form.get('companyName').invalid ||
      this.form.get('companyName').pending ||
      this.form.get('address.street').invalid ||
      this.form.get('address.street').pending ||
      this.form.get('address.zipCode').invalid ||
      this.form.get('address.zipCode').pending ||
      this.form.get('address.city').invalid ||
      this.form.get('address.city').pending ||
      this.isDurationNotValid() ||
      this.form.get('internalCode').invalid ||
      this.form.get('internalCode').pending ||
      this.form.get('address.country').invalid ||
      this.form.get('address.country').pending
    );
  }

  secondStepInvalid(): boolean {
    return (
      this.form.get('passwordRevocationDelay').invalid ||
      this.form.get('otp').invalid ||
      this.form.get('emailDomains').invalid ||
      this.form.get('defaultEmailDomain').invalid
    );
  }

  public thirdStepValid(): boolean {
    return !this.customerForm || (this.customerForm && this.customerForm.valid);
  }

  public fourthStepValid(): boolean {
    return this.homepageMessageForm && this.homepageMessageForm.valid && this.checkValidation(this.homepageMessageForm.value.translations);
  }

  private checkValidation(forms: FormGroup[]): boolean {
    let isValid = true;
    forms.forEach((x) => {
      if (!x.valid) {
        isValid = false;
      }
    });
    return isValid;
  }

  lastStepIsInvalid(): boolean {
    const invalid = this.firstStepInvalid() || this.secondStepInvalid() || !this.thirdStepValid() || !this.fourthStepValid();
    return this.form.pending || this.form.invalid || invalid || this.creating;
  }
}
