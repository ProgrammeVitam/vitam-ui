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
import { merge, Subject } from 'rxjs';
import {ConfirmDialogService, Customer, Logo, OtpState} from 'ui-frontend-common';

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { takeUntil } from 'rxjs/operators';
import { CustomerService } from '../../core/customer.service';
import { TenantFormValidators } from '../tenant-create/tenant-form.validators';
import { CustomerCreateValidators } from './customer-create.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;

interface CustomerInfo {
   code: string;
   name: string;
   companyName: string;
}

@Component({
  selector: 'app-customer-create',
  templateUrl: './customer-create.component.html',
  styleUrls: ['./customer-create.component.scss']
})
export class CustomerCreateComponent implements OnInit, OnDestroy {

  private destroy = new Subject();
  public form: FormGroup;
  public stepIndex = 0;
  public hasError = true;
  public message: string;
  public creating = false;
  public customerInfo: CustomerInfo = {
    code: null,
    name: null,
    companyName: null,
  };

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 4;

  // tslint:disable-next-line: variable-name
  private _customerForm: FormGroup;
  public get customerForm(): FormGroup { return this._customerForm; }
  public set customerForm(form: FormGroup) {
    this._customerForm = form;
  }

  public logos: Logo[];

  constructor(
    public dialogRef: MatDialogRef<CustomerCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private customerService: CustomerService,
    private customerCreateValidators: CustomerCreateValidators,
    private confirmDialogService: ConfirmDialogService,
    private tenantFormValidators: TenantFormValidators,
  ) {
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      enabled: [true, Validators.required],
      code: [
        null,
        [Validators.required, Validators.pattern(/^[0-9]{4,25}$/)],
        this.customerCreateValidators.uniqueCode(),
      ],
      name: [null, Validators.required],
      companyName: [null, Validators.required],
      passwordRevocationDelay: 6,
      otp: OtpState.OPTIONAL,
      address: this.formBuilder.group({
        street: [null, Validators.required],
        zipCode: [null, Validators.required],
        city: [null, Validators.required],
        country: ['FR', Validators.required]
      }),
      internalCode: [null],
      language: ['FRENCH', Validators.required],
      emailDomains: [null, Validators.required],
      defaultEmailDomain: [null, Validators.required],
      hasCustomGraphicIdentity: false,
      themeColors: [null],
      owners: this.formBuilder.array([
        this.formBuilder.control(null, Validators.required),
      ]),
      tenantName: [
        null,
        [Validators.required],
        this.tenantFormValidators.uniqueName(),
      ]
    });


    this.onChanges();

    this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef)
    .pipe(takeUntil(this.destroy))
    .subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.destroy.next();
  }

  onChanges() {
    merge(
      this.form.get('code').valueChanges,
      this.form.get('name').valueChanges,
      this.form.get('companyName').valueChanges,
    )
    .subscribe(() => {
      // reset object to trigger customerInfo input update in child component
      this.customerInfo = {
        code: this.form.get('code').value,
        name: this.form.get('name').value,
        companyName: this.form.get('companyName').value,
      };
    });
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

  onSubmit() {
    if (this.lastStepIsInvalid()) { return; }
    this.creating = true;
    const customer: Customer = this.updateForCustomerModel(this.form.value);

    this.customerService.create(customer, this.logos)
      .pipe(takeUntil(this.destroy))
      .subscribe(
      () => {
        this.dialogRef.close(true);
      },
      (error) => {
        this.creating = false;
        console.error(error);
      });
  }

  private updateForCustomerModel(formValue: any): Customer {
    let customer = formValue;
    if (this.customerForm && this.customerForm.get('hasCustomGraphicIdentity').value === true) {
      customer = {...formValue, ...{
        id : this.customerForm.get('id').value,
        hasCustomGraphicIdentity: this.customerForm.get('hasCustomGraphicIdentity').value,
        themeColors: this.customerForm.get('themeColors').value,
        portalTitle: this.customerForm.get('portalTitle').value,
        portalMessage: this.customerForm.get('portalMessage').value,
      }};
    }

    return customer;
  }

  firstStepInvalid(): boolean {
    return this.form.get('code').invalid || this.form.get('code').pending ||
      this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('companyName').invalid || this.form.get('companyName').pending ||
      this.form.get('address.street').invalid || this.form.get('address.street').pending ||
      this.form.get('address.zipCode').invalid || this.form.get('address.zipCode').pending ||
      this.form.get('address.city').invalid || this.form.get('address.city').pending ||
      this.form.get('address.country').invalid || this.form.get('address.country').pending ||
      this.form.get('internalCode').invalid || this.form.get('internalCode').pending;
  }

  secondStepInvalid(): boolean {
    return this.form.get('passwordRevocationDelay').invalid ||
      this.form.get('otp').invalid ||
      this.form.get('emailDomains').invalid ||
      this.form.get('defaultEmailDomain').invalid;
  }

  public thirdStepValid(): boolean {
    return !this.customerForm || (this.customerForm && this.customerForm.valid);
  }

  lastStepIsInvalid(): boolean {
      const invalid = this.firstStepInvalid() || this.secondStepInvalid() || !this.thirdStepValid();
      return this.form.pending || this.form.invalid || invalid || this.creating;
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

}
