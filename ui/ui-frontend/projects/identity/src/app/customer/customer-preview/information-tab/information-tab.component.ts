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
import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { merge, of } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { Customer, diff, OtpState } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';

import { CustomerService } from '../../../core/customer.service';
import { CustomerCreateValidators } from '../../customer-create/customer-create.validators';

const UPDATE_DEBOUNCE_TIME = 200;

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss']
})
export class InformationTabComponent implements OnInit, OnDestroy {
  public readonly form: FormGroup;

  previousValue: {
    code: string,
    identifier: string,
    name: string,
    companyName: string,
    passwordRevocationDelay: number,
    otp: OtpState,
    address: {
      street: string,
      zipCode: string,
      city: string,
      country: string,
    },
    language: string,
    emailDomains: string[],
    defaultEmailDomain: string
    gdprAlert: boolean,
    gdprAlertDelay: number,
  };

  @Input()
  set customer(customer: Customer) {
    this._customer = customer;
    this.resetForm(this.customer);
  }

  get customer(): Customer { return this._customer; }
  private _customer: Customer;
  private _gdprReadOnlyStatus: boolean;
  get gdprReadOnlyStatus(): boolean { return this._gdprReadOnlyStatus; }


  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      if(this._gdprReadOnlyStatus){
        this.form.get('gdprAlertDelay').disable({ emitEvent: false });
        this.form.get('gdprAlert').disable({ emitEvent: false });
        this.form.get('identifier').disable({ emitEvent: false });
      }
    }
  }


  @Input()
  set gdprReadOnlyStatus(gdprReadOnlyStatus : boolean){
    this._gdprReadOnlyStatus = gdprReadOnlyStatus;
    if(gdprReadOnlyStatus ) {
      this.form.get('gdprAlertDelay').disable({ emitEvent: false });
      this.form.get('gdprAlert').disable({ emitEvent: false });
    }
  };


  private sub: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private customerService: CustomerService,
    private customerCreateValidators: CustomerCreateValidators
  ) {
    this.form = this.formBuilder.group({
      id: [null, Validators.required],
      identifier: [{ value: null, disabled: true }, Validators.required],
      code: [
        null,
        [Validators.required, Validators.pattern(/^[0-9]{4,25}$/)],
        this.customerCreateValidators.uniqueCode(),
      ],
      name: [null, Validators.required],
      companyName: [null, Validators.required],
      passwordRevocationDelay: [null, Validators.required],
      otp: [null],
      address: this.formBuilder.group({
        street: [null, Validators.required],
        zipCode: [null, Validators.required],
        city: [null, Validators.required],
        country: [null, Validators.required],
      }),
      internalCode: [null],
      language: [null, Validators.required],
      emailDomains: [null, Validators.required],
      defaultEmailDomain: [null, Validators.required],
      gdprAlert: false,
      gdprAlertDelay: [
        72,
        [Validators.required, Validators.min(1), Validators.pattern(/^[0-9]{1,20}$/)]]
    });
  }

  ngOnInit() {
    this.sub = merge(this.form.statusChanges, this.form.valueChanges)
    .pipe(
      debounceTime(UPDATE_DEBOUNCE_TIME),
      filter(() => this.form.valid),
      map(() => diff(this.form.value, this.previousValue)),
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.customer.id }, formData)),
      switchMap((formData) => this.customerService.patch(formData).pipe(catchError(() => of(null))))
    )
    .subscribe((customer: Customer) => this.resetForm(customer));
  }


  ngOnDestroy() {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  private resetForm(customer: Customer) {
    if (customer) {
      this.form.get('code').setAsyncValidators(this.customerCreateValidators.uniqueCode(customer.code));
    }
    this.form.reset(customer, { emitEvent: false });
    this.previousValue = this.form.value;
  }

}
